package test.wr3.db;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import test.Greeting;
import wr3.Table;
import wr3.db.DbServer;
import wr3.db.Jpa;
import wr3.util.Logx;

public class JpaTest {

	Jpa jpa;
	DbServer dbs;

	@Before
	public void setUp() throws Exception {
		
		Logx.hibernate();
		
		jpa = Jpa.create("h2", Greeting.class);
		Greeting g_en = new Greeting("hello world", "en");
		Greeting g_es = new Greeting("hola, mundo", "es");
		Greeting g_cn = new Greeting("你好cn中文", "cn");
		Greeting[] greetings = new Greeting[] { g_en, g_es, g_cn };
		jpa.save(greetings);
		jpa.delete(g_cn);
		jpa.save(new Greeting("cn新", "cn"));
	}

	@After
	public void tearDown() throws Exception {
		jpa.close();
	}


	@Test
	public void testRead() {
		Greeting o = (Greeting) jpa.get(1);
		assertNotNull(o);
		assertEquals(1, o.getId());
	}
	
	@Test 
	public void query() {
		List<?> objs = jpa.query(
				"select g from Greeting g where g.language = 'cn'");
		assertEquals(1, objs.size());
		
		objs = jpa.query("select g from Greeting g");
		assertEquals(3, objs.size());
		
		objs = jpa.list();
		assertEquals(3, objs.size());
//		for (Object obj : objs) {			
//			System.out.println(obj);
//		}
	}
	
	@Test 
	public void sql() {
		List<?> objs = jpa.sql("select * from Greeting");
		assertEquals(3, objs.size());
//		for (Object row : objs) {
//			Object[] row1 = (Object[]) row;
//			for (Object cell : row1) {
//				System.out.println(cell);
//			}
//		}
	}

	@Test
	public void dbserver() {
		DbServer dbs = DbServer.create("h2");
		Table table = dbs.query("select * from greeting");
		assertEquals(3, table.rows());
		assertArrayEquals(new String[] { "ID", "LANGUAGE", "MESSAGE" }, table
				.head().asList().toArray());
		assertArrayEquals(new Object[] { 1L, "en", "hello world" }, table
				.row(0).asList().toArray());
		assertArrayEquals(new Object[] { 2L, "es", "hola, mundo" }, table
				.row(1).asList().toArray());
		assertArrayEquals(new Object[] { 4L, "cn", "cn新" }, table.row(2)
				.asList().toArray());
		dbs.close();
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		Logger logger = Logger.getLogger("org.hibernate");
		logger.setLevel(Level.WARNING);
		JUnitCore.main(JpaTest.class.getName());
	}

}
