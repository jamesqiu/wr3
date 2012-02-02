package test;


import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.ejb.Ejb3Configuration;

import wr3.db.DbConfig;
import wr3.db.Jpa;
import wr3.db.JpaConfig;

/**
 * 测试一下hibernate jpa实现的基本用法.
 * 使用 {@link Jpa}, {@link JpaConfig} 来进行包装.
 * @author jamesqiu 2009-3-23
 */
public class HibernateJpaTest {

	 private EntityManagerFactory emf;
	 private EntityManager em;
	 private String dialect = new H2Dialect().getClass().getName();
	 private String dbname = "h2";

	 /**
	  * 使用Hibernate灵活构建 EntityManagerFactory
	  */
	 private void initEntityManagerFactory() {

		 Ejb3Configuration cfg = new Ejb3Configuration();
		 cfg.addProperties(getDbConfig());
         cfg.addAnnotatedClass(Greeting.class);
         emf = cfg.buildEntityManagerFactory();
	 }
	 
	 private Properties getDbConfig() {
		 Properties props =new Properties();

		 props.put("hibernate.hbm2ddl.auto", "create-drop");
		 props.put("hibernate.dialect", dialect);
		 props.put("javax.persistence.transactionType", "RESOURCE_LOCAL");
		 props.put("hibernate.show_sql", "false");
		 
		 Map<String, String> map = new DbConfig().get(dbname);
		 props.put("hibernate.connection.url", map.get("url"));
		 props.put("hibernate.connection.driver_class", map.get("driver"));
		 props.put("hibernate.connection.username", map.get("username"));
		 props.put("hibernate.connection.password", map.get("password"));
		 return props;
	 }
	 
	 private void initEntityManager() {
//	     emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		 initEntityManagerFactory();
	     em = emf.createEntityManager();
	 }

	 private void closeEntityManager() {
	     em.close();
	     emf.close();
	 }

	 private void create() {
	     em.getTransaction().begin();
	     Greeting g_en = new Greeting("hello world", "en");
	     Greeting g_es = new Greeting("hola, mundo", "es");
	     Greeting g_cn = new Greeting("你好cn中文", "cn");
	     Greeting[] greetings = new Greeting[]{g_en, g_es, g_cn};
	     for(Greeting g : greetings) {
	         em.persist(g);
	     }
	     em.getTransaction().commit();
	 }

	 private void read() {
	     Greeting g = (Greeting) em.createQuery(
	             "select g from Greeting g where g.language = :language")
	             .setParameter("language", "cn").getSingleResult();
	     System.out.println("Query returned:\n" + g);
	 }

	 // ---------------------- main() ----------------------
	 public static void main(String[] args) {
		 
		 Logger logger = Logger.getLogger("org.hibernate");
		 logger.setLevel(Level.SEVERE);
		 
	     HibernateJpaTest hello = new HibernateJpaTest();
	     hello.initEntityManager();
	     hello.create();
	     hello.read();
	     hello.closeEntityManager();
	 }
}
