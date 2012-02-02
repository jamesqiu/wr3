package test.wr3.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import tool.DataGen;
import wr3.Row;
import wr3.Table;
import wr3.db.DbServer;
import wr3.table.ListTable;
import wr3.util.Tablex;

public class ListTableTest {

	String dbname = "h2";
	ListTable lt = ListTable.create();
	
	@Before
	public void init() {
		
		DataGen.create(dbname);
		DbServer dbs = DbServer.create(dbname);
		
		Table data = dbs.query("select top 15 * from loan -- where 1=2");
		List<Object> meta = Tablex.asList(
				dbs.query("select nam from sys_infolder where cod like 'loan.%'"));
		Map<Object, Object> dd = Tablex.asMap(
				dbs.query("select * from dd_org"));

		lt.data(data)
			.meta(meta)
			.dd("orgid", dd)
			;
		
	}
	
	@Test
	public void result() {
		
		Table rt = lt.result();
		assertEquals(4, rt.cols());
		assertEquals(15, rt.rows());
		
		assertEquals(Row.createByTypes("月份", "机构代码", "客户名", "贷款额"), 
				rt.head());
		List<?> L = Tablex.asList(DbServer.create(dbname).query("select name from dd_org"));
		for (int i = 0; i < rt.rows(); i++) {
			assertTrue(L.contains(rt.cell(i, 1).value()));
		}
	}
	
	@Test
	public void nullData() {
		
		Table data = DbServer.create(dbname).query("select * from loan where 1=2");
		lt = ListTable.create()
			.data(data);
		Table rt = lt.result();
		assertEquals(0, rt.rows());
		assertEquals(4, rt.cols());
		
		lt = ListTable.create()
			.data(new Table());
		rt = lt.result();
		assertEquals(0, rt.rows());
		assertEquals(0, rt.cols());
		
		lt = ListTable.create()
			.data(null);
		rt = lt.result();
		assertEquals(0, rt.rows());
		assertEquals(0, rt.cols());
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(ListTableTest.class.getName());
	}
}
