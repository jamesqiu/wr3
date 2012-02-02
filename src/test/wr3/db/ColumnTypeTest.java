package test.wr3.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static wr3.db.ColumnType.isDouble;
import static wr3.db.ColumnType.isLong;
import static wr3.db.ColumnType.isNumber;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import tool.DataGen;
import wr3.Cell;
import wr3.Table;
import wr3.db.DbServer;

public class ColumnTypeTest {

	List<String> ss1 = Arrays.asList("int", "Int", "INT", "integer", "SmallInt");
	List<String> ss2 = Arrays.asList("FLOAT", "decimal", "Numeric", "REAL", "Double");
	List<String> ss0 = Arrays.asList("", null, "int8", "Numeric ", " REAL", "unknown");
	
	@Test
	public void testIsNumber() {
		for (String s : ss1) {
			assertTrue(isNumber(s));
		}
		for (String s : ss2) {
			assertTrue(isNumber(s));
		}
	}

	@Test
	public void testIsInteger() {
		for (String s : ss1) {
			assertTrue(isLong(s));
			assertFalse(isDouble(s));
		}
	}

	@Test
	public void testIsDouble() {
		for (String s : ss2) {
			assertTrue(isDouble(s));
			assertFalse(isLong(s));
		}
	}
	
	@Test
	public void not() {
		for (String s : ss0) {
			assertFalse(isNumber(s));
			assertFalse(isLong(s));
			assertFalse(isDouble(s));
		}
	}
	
	@Test
	public void h2Types() {
		
		String dbname = "h2";
		DataGen.create(dbname);
		DbServer dbs = DbServer.create(dbname);
		String sql = "select " +
				"cast(-2147483648 as int) int, " +
				"TRUE bool, " +
				"cast(127 as tinyint) tinyint, " +
				"cast(-32768 as smallint) smallint, " +
				"cast(9223372036854775807 as bigint) bigint, " +
				"cast(3.14 as DECIMAL(20, 2)) decimal, " +
				"cast(3.14 as double) double, " +
				"cast(3.14 as real) real, " +
				"cast('23:59:59' as time) time, " +
				"cast('2009-2-28' as date) date, " +
				"cast('2009-2-28 23:59:59.1234' as TIMESTAMP) timestamp, " +
				"cast('cn中文' as varchar(10)) varchar, " +
				"cast('cn中文' as char(10)) char, " +
				"null";
		Table t = dbs.query(sql);
		for (int i = 0, n = t.cols(); i < n; i++) {
			String head = t.head(i).value();
			Cell cell = t.cell(0, i);
			if (isLong(head)) {
				assertEquals(Long.class, cell.type());
			} else if (isDouble(head)) {
				assertEquals(Double.class, cell.type());
			} else if (cell==null){
				assertEquals(n-1, i);
				assertNull(t.cell(0, -1));
			} else {
				assertEquals(String.class, cell.type());
			}
		}
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(ColumnTypeTest.class.getName());
	}
}
