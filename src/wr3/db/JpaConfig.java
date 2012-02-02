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
 * ע�⣺����ס����close()
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
		 dbCreate(DBCREATE_TYPE.UPDATE); // ȱʡ��updateģʽ
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
	 * ����������ݿ���ģʽ��
	 * @param type
	 */
	public JpaConfig dbCreate(DBCREATE_TYPE type) {
		
		// ��������У���ṹֱ�Ӷ�д���ݣ��ʺ��ڱ�ṹ�̶�������������
		if (type==null) return this;
		
		String value;
		
		switch(type) {
		case CREATE:
			value = "create";		// ���±�ṹ���ؽ����ݣ�ԭ����ɾ����
			break;
		case CREATEDROP:
			value = "create-drop";	// �ؽ���ṹ���ؽ����ݣ�ԭ����ɾ����
			break;
		case UPDATE:
			value = "update";		// ���±�ṹ(����ȥ���޹��ֶ�)���������ݣ�ԭ���ı�����
			break;
		case VALIDATE:
			value = "validate";		// �����±�ṹ���������ݣ�ԭ���ı���
									// ֻ�ڲ���ǰ��У���ṹ�Ƿ�ƥ��
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
	 * �õ����һ���� {@link #addClass(Class)} ���õ�entity��.
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
	 * �����
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
