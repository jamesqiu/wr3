package test.wr3.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.Row;
import wr3.Table;
import wr3.db.DbServer;
import wr3.db.RowFilter;
import wr3.util.Datetime;
import wr3.util.Stringx;

public class DbServerTest {

	static DbServer server = DbServer.create("h2");

	String sql_create = "CREATE TABLE cust2(" +
			"id INT, " +
			"name VARCHAR(20), " +
			"dates VARCHAR(20))";
	String sql_insert = "INSERT INTO cust2 VALUES('%d', '%s', '%s')";
	int ROWS = 100; // 执行 sql_insert 的次数
	String sql_select = "SELECT * FROM cust2";
	String sql_update = "UPDATE cust2 SET name=concat('a',name)";
	String sql_proc1 = "UPDATE cust2 SET name='new value' WHERE id='0'; ";
	String sql_proc2 = "SELECT COUNT(*) FROM cust2; ";
	String sql_select1 = "select * from cust2 where id='0'";
	Table table;
	
	@AfterClass
	public static void close() {
		
		server.close();
	}
	
	@Test
	public void init() {
		// 建表造数
		server.update(sql_create);		
		String s;
		for (int i = 1; i <= ROWS; i++) {
			s = Stringx.printf(sql_insert, ROWS*10 + i, "记录"+i, Datetime.datetime());
			server.update(s);
		}

		// 限制行数的查询
		server.maxRows(3);
		table = server.query(sql_select);
		assertEquals(3, table.rows());
		
		// 查询
		server.maxRows(0);
		table = server.query(sql_select, ROWS-10+1, 10);
		assertEquals(10, table.rows());
		assertEquals(3, table.cols());
		assertEquals(ROWS*10+ROWS, table.cell(-1, 0).intValue());
		
		assertEquals(ROWS, server.rows("cust2"));
		
		server.process(sql_select, new RowFilter() {
			public boolean process(Row row) {
				if (row.cell(0).intValue()<=(ROWS*10+5)) {
//					System.out.println(row);
					return true;
				}
				return false;
			}
		});
		
		// 更新 
		int i = server.update(sql_update);
		assertEquals(ROWS, i);
		server.maxRows(0);
		table = server.query(sql_select);
		assertEquals(ROWS, table.rows());
		
		// 增加1行
		assertEquals(0, server.resultRows(sql_select1));
		s = Stringx.printf(sql_insert, 0, "cn中文", Datetime.datetime());
		server.update(s);
		assertEquals(1, server.resultRows(sql_select1));
		assertEquals(ROWS+1, server.rows("cust2"));
		
		// 存储过程
		table = server.procedure(sql_proc1 + sql_proc2);
		assertEquals(1, table.cell(0, 0).intValue());
		table = server.procedure(sql_proc2 + sql_proc1);
		assertEquals(ROWS+1, table.cell(0, 0).intValue());
		table = server.query(sql_select1);
		assertEquals("new value", table.cell(0, 1).value());
		
		// 带null的数据
		String sql_update1 = "INSERT INTO cust2 VALUES('0', null, null)";
		int rt = server.update(sql_update1);
		assertEquals(1, rt);
		table = server.query(sql_select1);
		assertEquals(2, table.rows());
		assertNull(table.cell(1, 1));
		assertNull(table.cell(1, 2));
	}
	
	@Test
	public void query() {
		// 测试大整数
		int I = Integer.MAX_VALUE;
		Long L = I * 1000L;
		
		table = server.query("select " + I + "," + L + ", cast(" + L + " as bigint)");
		assertEquals(Long.class, table.cell(0, 0).type());
		assertEquals(Double.class, table.cell(0, 1).type());		
		assertEquals(Long.class, table.cell(0, 2).type());
		
		assertTrue(I == table.cell(0, 0).intValue());
		assertTrue(L == table.cell(0, 1).longValue());
		assertEquals((double)L, table.cell(0, 1).doubleValue(), 0.0);
		assertTrue(L == table.cell(0, 2).longValue());

		assertFalse(I == table.cell(0, 1).intValue());
		assertFalse(I == table.cell(0, 2).intValue());
	}
	
	@Test
	public void meta() {
		assertEquals("H2", server.product());
		assertEquals("TESTDB", server.database());
		assertEquals("cust2".toUpperCase(), server.tables().get(0));
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {

		JUnitCore.main(DbServerTest.class.getName());
	}
}
