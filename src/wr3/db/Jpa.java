package wr3.db;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import wr3.util.Stringx;

/**
 * <pre>
 * EJB3 JPA implements wrapper (use hibernate-entitymanager).
 * usage:
 *   Jpa jpa = Jpa.create("h2", Person.class);
 *   jpa.save(new Person()); 	// Create
 *   p1 = jpa.get(id);			// Read,  jpa.read(id);	for readonly access
 *   jpa.save(p1.age(10));		// Update
 *   jpa.delete(id);			// Delete
 *   jpa.close();
 * </pre>
 * @see JpaConfig
 * @author jamesqiu 2009-3-21
 */
public class Jpa {

	private JpaConfig conf;
	private EntityManager em;
	
	private Jpa() {}

	private Jpa(String dbname, Class<?> clazz) {
		conf = new JpaConfig(dbname).addClass(clazz);
		em = conf.em();
	}
	
	private Jpa(JpaConfig conf) {
		this.conf = conf;
		em = conf.em();
	}
	
	/**
	 * use default dataSource
	 * @param clazz
	 * @return
	 */
	public static Jpa create(Class<?> clazz) {
		return create(null, clazz);
	}
	
	/**
	 * @param dbname dbname defined in DataSource.groovy
	 * @param clazz entity class 该class必须在classpath中，不能在app下
	 * @return
	 */
	public static Jpa create(String dbname, Class<?> clazz) {
		return new Jpa(dbname, clazz);
	}
	
	/**
	 * 使用一个JpaConfig来进行灵活设置.
	 * @param conf JpaConfig
	 * @return
	 */
	public static Jpa create(JpaConfig conf) {
		return new Jpa(conf);
	}
	
	/**
	 * close EntityManager and EntityManagerFactory
	 */
	public void close() {
		em.close();
		conf.close();
	}
	
	/**
	 * persist(create/update) 1 obj to the database.
	 * @param obj
	 */
	public Jpa save(Object obj) {
	    em.getTransaction().begin();
		em.persist(obj);
	    em.getTransaction().commit();
	    return this;
	}
	
	/**
	 * persist(create/update) n+ objs to the database.
	 * @param objs
	 */
	public Jpa save(Object[] objs) {
	    em.getTransaction().begin();
		for (Object obj : objs) {
			em.persist(obj);
		}
	    em.getTransaction().commit();
	    return this;
	}
	
	/**
	 * delete an instance from the db.
	 * @param id
	 * @return
	 */
	public Jpa delete(Object obj) {
	    em.getTransaction().begin();
		em.remove(obj);
	    em.getTransaction().commit();
	    return this;
	}

	/**
	 * read the object back from the db.
	 * @param id e.g. new Integer(1), or just 1 by autobox
	 * @return
	 */
	public Object get(Object id) {
		return em.find(conf.clazz(), id);
	}
	
	/**
	 * 使用sql方言进行查询，返回对象列表
	 * @param sql
	 * @return Object[][]
	 */
	public List<?> sql(String sql) {
		return em.createNativeQuery(sql).getResultList();
	}
	
	/**
	 * 使用jpa sql (HQL) 进行查询，返回对象列表
	 * alias of {@link #query(String)}
	 * @param hql
	 * @return
	 */
	public List<?> hql(String hql) {
		return query(hql);
	}
	
	/**
	 * @param sql HQL, not native sql
	 * @return objects list
	 */
	public List<?> query(String sql) {
		return em.createQuery(sql).getResultList();
	}
	
	/**
	 * 得到所有的对象
	 * @return
	 */
	public List<?> list() {
		
		if (conf==null && conf.clazz()==null) return new ArrayList<Object>();
		String fullname = conf.clazz().getName();
		if (fullname.indexOf('.')!=-1) 
			fullname = Stringx.rightback(fullname, ".");
		
		return list(fullname);
	}
	
	/**
	 * 得到指定库表的所有对象
	 * @param tableName
	 * @return
	 */
	public List<?> list(String tableName) {
		return query("select o from " + tableName + " o");
	}
}
