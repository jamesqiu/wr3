package test.wr3.db;

import static org.junit.Assert.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import test.Greeting;
import wr3.db.JpaConfig;

public class JpaConfigTest {

	/**
	 * 设置hibernate日志输出级别.
	 */
	static {
		Logger logger = Logger.getLogger("org.hibernate");
		logger.setLevel(Level.WARNING);
	};
	
	@Test
	public void testDbCreate() {
		JpaConfig conf = new JpaConfig("h2").addClass(Greeting.class);
		EntityManager em = conf.em();
		assertNotNull(em);
		assertEquals("test.Greeting", conf.clazz().getName());
		conf.close();
	}

	@Test
	public void testSet() {
	}

	@Test
	public void testAddClass() {
	}

	@Test
	public void testGet() {
	}

	@Test
	public void testGetString() {
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(JpaConfigTest.class.getName());
	}

}
