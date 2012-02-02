package test.wr3.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.SQLServerDialect;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import tool.DataGen;
import wr3.Table;
import wr3.db.DbMeta;
import wr3.db.DbServer;
import wr3.util.Logx;
import wr3.util.Stringx;

/**
 * 需要启动数据库进行测试.
 * @author jamesqiu 2009-1-18
 *
 */
public class DbMetaTest {

	static String dbname = "h2";
	static DbServer dbs = DbServer.create(dbname);
	static DbMeta meta = dbs.meta();
	static DataGen datagen = DataGen.create(dbname);

	@AfterClass public static void close() {
		dbs.close();
		datagen.close();
	}
	
	@Test
	public void columns() {

		Table table = meta.columns("DD_ORG");
		assertEquals(2, table.rows());
		assertEquals("CODE", table.cell(0, 1).value());
		assertEquals("NAME", table.cell(1, 1).value());
		String ddl = meta.ddl("DD_ORG", new H2Dialect());
		String rt = "create table DD_ORG (\n" + 
				"\tCODE varchar(20),\n" + 
				"\tNAME varchar(20)\n" +
				")";
		assertEquals(rt, ddl);
	}
	
//	@Test 
	public void columns2() {
		
		DbServer dbs = DbServer.create("abs_grails");
		DbMeta meta = dbs.meta();
		System.out.println(meta.columns("Types"));
		
		dbs = DbServer.create("postgre");
		meta = dbs.meta();
		System.out.println(meta.columns("dialect"));
		dbs.close();
	}
	
	@Test
	public void keywords() {
		String s = meta.keywords();
//		System.out.println("keywords: " + s);
		assertNotNull(s);
		String[] ss = Stringx.split(s, ",");
		assertEquals(7, ss.length);
	}
	
	@Test
	public void functions() {
		String s = meta.functions(DbMeta.FUNCTION.Datetime);
		assertNotNull(s);
//		System.out.println(s);
		String[] ss = Stringx.split(s, ",");
		assertTrue(ss.length > 10);
	}
	
	@Test
	public void datatypes() {
		Table table = meta.datatypes();
//		System.out.println(table);
		assertEquals(3, table.cols());
		assertEquals("TYPE_NAME", table.head(0).value());
		assertEquals("DATA_TYPE", table.head(1).value());
		assertEquals("JDBC_TYPE", table.head(2).value());
	}
	
	@Test
	public void ddl() {
		String tablename = "DD_ORG";
		String s = meta.ddl(tablename);
		String rt = "create table DD_ORG (\n" + 
				"\tCODE VARCHAR(20),\n" + 
				"\tNAME VARCHAR(20)\n" + 
				")";
		assertEquals(rt, s);
//		System.out.println(s);
		Logx.hibernate();
		s = meta.ddl(tablename, new SQLServerDialect());
		rt = "create table DD_ORG (\n" + 
				"\tCODE varchar(20),\n" + 
				"\tNAME varchar(20)\n" + 
				")";
		assertEquals(rt, s);
//		System.out.println(s);
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {

		JUnitCore.main(DbMetaTest.class.getName());
	}
}
