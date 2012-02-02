package test.wr3.table;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import tool.DataGen;
import wr3.Cell;
import wr3.Table;
import wr3.db.DbServer;
import wr3.table.AggregateTable;
import wr3.table.AggregateTable.Position;
import wr3.util.Tablex;

public class AggregateTableTest {

	String dbname = "h2";
	DataGen dg = DataGen.create(dbname);
	DbServer dbs = DbServer.create(dbname);
	
	@Test
	public void testResult() {

		Table data0 = dbs.query("select * from loan where orgid='001' ");
		List<Object> meta = Tablex.asList(
				dbs.query("select nam from sys_infolder where cod like 'loan.%'"));
		Map<Object, Object> dd = Tablex.asMap(
				dbs.query("select * from dd_org"));
		
		AggregateTable table = AggregateTable.create()
			.data(data0)
			.meta(meta)
			.dd("orgid", dd)
			.sum(Position.FIRST)
			.sum(Position.LAST) // 会替换上面一个
			.avg()
			.max()
			.min(Position.FIRST)
			;
		Table data1 = table.result();
		
		assertEquals(data0.cols(), data1.cols());
		assertEquals(data0.rows()+4, data1.rows());
		
		assertEquals(Tablex.min(data0).cell(-1), data1.cell(0, -1));
		assertEquals(Tablex.max(data0).cell(-1), data1.cell(-1, -1));
		assertEquals(Tablex.avg(data0).cell(-1), data1.cell(-2, -1));
		assertEquals(Tablex.sum(data0).cell(-1), data1.cell(-3, -1));
	}
	
	@Test
	public void withoutData() {
		// 空结果集
		Table data0 = dbs.query("select * from loan where orgid='001' and 1=2");
		List<Object> meta = Tablex.asList(
				dbs.query("select nam from sys_infolder where cod like 'loan.%'"));
		Map<Object, Object> dd = Tablex.asMap(
				dbs.query("select * from dd_org"));
		
		AggregateTable table = AggregateTable.create()
			.data(data0)
			.meta(meta)
			.dd("orgid", dd)
			.sum(Position.FIRST)
			.sum(Position.LAST) // 会替换上面一个
			.avg()
			.max()
			.min(Position.FIRST)
			;
		Table data1 = table.result();
		assertEquals(4, data1.rows());
		assertEquals(4, data1.cols());
		assertEquals(new Cell("最小值"), data1.cell(0, 0));
		assertEquals(new Cell("最大值"), data1.cell(-1, 0));
		assertEquals(new Cell(), data1.cell(3, 3));
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(AggregateTableTest.class.getName());
	}

}
