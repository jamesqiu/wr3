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
 * 从配置文件(DbConfig)或者连接参数得到数据库Connection
 * <usage>
 *  ds = new DbSource()
 *  conn = ds.connect(); 		// 得到缺省的数据库连接(dbname="dataSource")
 *  conn = ds.connect("abs");	// 使用dbname="abs"
 *  conn = ds.jdbc(driver, url, username, password);
 *  conn = ds.jndi(url);
 * </pre>
 * @author jamesqiu 2009-1-9
 */
public class DbSource {

	public DbSource() {
	}

	/**
	 * 得到缺省的数据库连接(配置文件中名为dataSource)
	 * @return
	 */
	public Connection connect() {

		return connect("dataSource");
	}

	/**
	 * 得到指定名称的jdbc或者jndi数据库Connection,
	 * @param dbname
	 * @return
	 */
	public Connection connect(String dbname) {

		DbConfig config = new DbConfig();
		Map<String, String> props = config.get(dbname);
		String driver = props.get("driver");
		// 根据driver判断是jdbc还是jndi
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
	 * 获得jdbc的Connection
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
	 * 获得jndi的Connection
	 * @param name 如: "java:/jdbc/demoPool"
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
