package test.wr3.table;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import tool.DataGen;
import wr3.Table;
import wr3.db.DbServer;
import wr3.table.RotateTable;
import wr3.util.Tablex;

public class RotateTableTest {

	@Test
	public void testDataTable() {

		DataGen.create("h2");
		DbServer dbs = DbServer.create("h2");
		
		Table data0 = dbs.query("select * from loan where orgid='001' ");
//		System.out.println(data);
		List<Object> meta = Tablex.asList(
				dbs.query("select nam from sys_infolder where cod like 'loan.%'"));
		Map<Object, Object> dd = Tablex.asMap(
				dbs.query("select * from dd_org"));
		
		RotateTable table = RotateTable.create()
			.data(data0)
			.meta(meta)
			.dd("orgid", dd)
			;
		Table data1 = table.result();
		assertEquals(data0.rows()+1, data1.cols());
		assertEquals(data0.cols(), data1.rows());
		assertEquals("ÔÂ·Ý", data1.cell(0, 0).value());
		assertEquals(data0.cell(0,0), data1.cell(0, 1));
		assertEquals(data0.cell(0, -1), data1.cell(-1, 1));
		assertEquals(data0.cell(-1, 0), data1.cell(0, -1));
		assertEquals(data0.cell(-1, -1), data1.cell(-1, -1));
//		System.out.println(data);
//		System.out.println(table.result());
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(RotateTableTest.class.getName());
	}
}
