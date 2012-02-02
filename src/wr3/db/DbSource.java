package wr3.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * <pre>
 * �������ļ�(DbConfig)�������Ӳ����õ����ݿ�Connection
 * <usage>
 *  ds = new DbSource()
 *  conn = ds.connect(); 		// �õ�ȱʡ�����ݿ�����(dbname="dataSource")
 *  conn = ds.connect("abs");	// ʹ��dbname="abs"
 *  conn = ds.jdbc(driver, url, username, password);
 *  conn = ds.jndi(url);
 * </pre>
 * @author jamesqiu 2009-1-9
 */
public class DbSource {

	public DbSource() {
	}

	/**
	 * �õ�ȱʡ�����ݿ�����(�����ļ�����ΪdataSource)
	 * @return
	 */
	public Connection connect() {

		return connect("dataSource");
	}

	/**
	 * �õ�ָ�����Ƶ�jdbc����jndi���ݿ�Connection,
	 * @param dbname
	 * @return
	 */
	public Connection connect(String dbname) {

		DbConfig config = new DbConfig();
		Map<String, String> props = config.get(dbname);
		String driver = props.get("driver");
		// ����driver�ж���jdbc����jndi
		if ("jndi".equals(driver)) {
			String name = props.get("name");
			return jndi(name);
		} else {
			String url = props.get("url");
			String username = props.get("username");
			String password = props.get("password");
			return jdbc(driver, url, username, password);
		}
	}

	/**
	 * ���jdbc��Connection
	 * @param driver
	 * @param url
	 * @param username
	 * @param password
	 * @return
	 */
	public Connection jdbc(String driver, String url,
		String username, String password) {

        try {
			Class.forName (driver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}

        try {
			return DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * ���jndi��Connection
	 * @param name ��: "java:/jdbc/demoPool"
	 * @return
	 */
	public Connection jndi(String name) {

		DbConfig config = new DbConfig();
		Map<String, String> drivers = config.drivers();
		String factory = drivers.get("jndi.factory");
		String url = drivers.get("jndi.url");

		return jndi(factory, url, name);
	}

	public Connection jndi(String factory, String url, String dbsource) {

		Hashtable<String, String> env = new Hashtable<String, String>();
	    env.put(Context.INITIAL_CONTEXT_FACTORY, factory);
	    env.put(Context.PROVIDER_URL, url);

		try {
			Context ctx = new InitialContext(env);
			DataSource ds = (DataSource) (ctx.lookup(dbsource));
			return ds.getConnection();
		} catch (NamingException e) {
			e.printStackTrace();
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

}
