package test.wr3.db;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.db.DbConfig;

public class DbConfigTest {

	DbConfig config;
	Map<String,String> map;

	@Before
	public void init() {
		config = new DbConfig();
	}

	@Test
	public void config() {

		map = config.get();
		assertEquals("com.microsoft.jdbc.sqlserver.SQLServerDriver",
				map.get("driver"));
		assertEquals("jdbc:microsoft:sqlserver://localhost:1433;DatabaseName=cbs400",
				map.get("url"));
		assertEquals("sa", map.get("username"));
		assertEquals("passdb", map.get("password"));

		map = config.get("hsqlLocal");
		assertEquals("org.hsqldb.jdbcDriver", map.get("driver"));
		assertEquals("jdbc:hsqldb:webreportdb", map.get("url"));
		assertEquals("sa", map.get("username"));
		assertEquals("", map.get("password"));
	}

	@Test
	public void configJndi() {

		map = config.get("apmis");
		assertEquals("jndi", map.get("driver"));
		assertEquals("java:/jdbc/demoPool", map.get("name"));
		assertEquals(null, map.get("username"));
		assertEquals(null, map.get("password"));
	}

	@Test
	public void dbnames() {
		List<String> list = config.dbnames();
		assertTrue(list.size()>1);
	}

	@Test
	public void drivers() {
		Map<String, String> drivers = config.drivers();
		assertNotNull(drivers);
		assertTrue(drivers.size()>1);
		assertEquals("com.nasoft.webfirst.jndi.interfaces.NamingContextFactory",
				drivers.get("jndi.factory"));
		assertEquals("127.0.0.1:1099", drivers.get("jndi.url"));
		assertEquals("java:/jdbc/", drivers.get("jndi.prefix"));
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {

		JUnitCore.main(DbConfigTest.class.getName());
	}
}
