package wr3.db;

import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.ejb.Ejb3Configuration;

/**
 * <pre>
 * config JPA EntityManager by {@link wr3.db.DbConfig}, used by {@link wr3.db.Jpa}
 * <font color=red>
 * 注意：最后记住调用close()
 * </font>
 * usage:
 *   conf = new JpaConfig("h2");
 *   conf.addClass(Employee.class);
 *   conf.dbCreate(DBCREATE_TYPE.UPDATE);
 *   entitymanager = conf.em();
 *   conf.close();
 *   
 * </pre>
 * @see Jpa
 * @author jamesqiu 2009-3-21
 */
public class JpaConfig {

	private Ejb3Configuration cfg;
	private Class<?> clazz;
	/**
	 * if not set, use default "dataSource".
	 */
	private String dbname;
	
	public JpaConfig() {
		init();
	}
	
	public JpaConfig(String dbname) {
		init();
		this.dbname = dbname;
	}

	/**
	 * default setting
	 */
	private void init() {
		 cfg = new Ejb3Configuration();
		 dbCreate(DBCREATE_TYPE.UPDATE); // 缺省用update模式
		 cfg.setProperty("javax.persistence.transactionType", "RESOURCE_LOCAL");
		 cfg.setProperty("hibernate.show_sql", "false");		 
	}
	
	public JpaConfig dbname(String dbname) {
		return this;
	}
	
	public enum DBCREATE_TYPE {
		CREATE, CREATEDROP, UPDATE, VALIDATE
	}; 

	/**
	 * 定义操作数据库表的模式；
	 * @param type
	 */
	public JpaConfig dbCreate(DBCREATE_TYPE type) {
		
		// 不设置则不校验表结构直接读写数据，适合于表结构固定的生产环境。
		if (type==null) return this;
		
		String value;
		
		switch(type) {
		case CREATE:
			value = "create";		// 更新表结构，重建数据（原来的删除）
			break;
		case CREATEDROP:
			value = "create-drop";	// 重建表结构，重建数据（原来的删除）
			break;
		case UPDATE:
			value = "update";		// 更新表结构(但不去除无关字段)，更新数据（原来的保留）
			break;
		case VALIDATE:
			value = "validate";		// 不更新表结构，更新数据（原来的保留
									// 只在操作前先校验表结构是否匹配
			break;
		default:
			value = "none";
		}
		
		cfg.setProperty("hibernate.hbm2ddl.auto", value);
		
		return this;
	}
	
	public JpaConfig set(String key, String value) {
		cfg.setProperty(key, value);
		return this;
	}
	
	public JpaConfig addClass(Class<?> clazz) {
		cfg.addAnnotatedClass(clazz);
		this.clazz = clazz;
		return this;
	}
	
	/**
	 * 得到最后一个用 {@link #addClass(Class)} 设置的entity类.
	 */
	public Class<?> clazz() {
		return clazz;
	}
	
	private EntityManagerFactory emf;
	/**
	 * get entitymanager.
	 * @return
	 */
	public EntityManager em() {
		
		cfg.addProperties(dbConfig());
		emf = cfg.buildEntityManagerFactory();
		return emf.createEntityManager();
	}
	
	/**
	 * 必须的
	 */
	public void close() {
		emf.close();
	}
	
	private Properties props = new Properties();
	// wrap DbConfig info as Properties.
	private Properties dbConfig() {
		Map<String, String> map = new DbConfig().get(dbname);
		props.put("hibernate.connection.url", map.get("url"));
		props.put("hibernate.connection.driver_class", map.get("driver"));
		props.put("hibernate.connection.username", map.get("username"));
		props.put("hibernate.connection.password", map.get("password"));
		return props;		
	}
	
	@Override
	public String toString() {
		return dbname + ": " + props + ", " + clazz.getName();
	}
}
