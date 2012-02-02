package test.wr3.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import tool.DataGen;
import wr3.Cell;
import wr3.Row;
import wr3.Table;
import wr3.db.DbServer;
import wr3.table.CellFilter;
import wr3.table.FormTable;

public class FormTableTest {

	FormTable ftable;
	Table data;
	String id = "fmtable";
	
	@Before
	public void init() {
		
		DataGen.create("h2");
		DbServer dbs = DbServer.create("h2");
		String sql = "select * from dd_org";
		data = dbs.query(sql);
		
		ftable = FormTable.create()
			.data(data)
			.id(id)
			;
	}
	
	@Test
	public void testHtml() {
		assertTrue(true);
	}

	@Test
	public void testFilterCellFilter() {
	}

	@Test
	public void testCodes() {
		assertEquals(Arrays.asList("code", "name"),
				ftable.codes().asList());
	}

	@Test
	public void testResult() {
		Table t = ftable.result();
		assertEquals(2, t.cols());
		assertEquals(4, t.rows());
		assertEquals(Arrays.asList("code", "name"),
				t.head().asList());
		for (int i = 0; i < t.rows(); i++) {
			assertEquals(data.row(i).asList(),
					t.row(i).asList());
		}		
	}

	@Test
	public void meta() {
		
		List<?> list = Arrays.asList("代码", "描述");
		ftable.meta(list);
		Table t = ftable.result();
		assertEquals(list, t.head().asList());
		
	}
	@SuppressWarnings({ "serial" })
	@Test
	public void dd() {
		
		final String s1 = "2";
		final String s2 = "北京分行";
		ftable.dd(0, new LinkedHashMap<Object,Object>() {{put("002", s1);}});
		ftable.dd(1, new LinkedHashMap<Object,Object>() {{put("市分行", s2);}});
		Table t = ftable.result();
		assertEquals(s1, t.cell(1, 0).value());
		assertEquals(s2, t.cell(0, 1).value());
	}
	
	@Test
	public void filter() {

		CellFilter filter = new CellFilter() {
			public Cell process(int col, Cell cell) {
				if (col==1)
					return Cell.create(cell.value().toUpperCase());
				return null;
			}
			public Cell process(int row, int col, Cell cell) {
				return Cell.create("'" + cell.value()+ "'");
			}
		};
		
		ftable.filter(filter);
		Table rt = ftable.result();
		assertEquals(ftable.codes().cell(1).value().toUpperCase(),
				rt.head(1).value());
		for (int i = 0; i < rt.rows(); i++) {
			for (int j = 0; j < rt.cols(); j++) {
				assertEquals("'"+data.cell(i,j)+"'",
						rt.cell(i, j).value());							
			}
		}
	}
	
	@Test
	public void html() {
		
		String html = ftable.html();
		assertNotNull(html);
		Row codes = ftable.codes();
		Table rt = ftable.result();
		List<String> patterns = Arrays.asList(
				"<table id=\"table$"+id+"\" class=\"wr3table\" ",
				codes.cell(0).value(),
				codes.cell(1).value(),
				rt.cell(0,0).value(),
				rt.cell(0,1).value());
		for (String pattern : patterns) {
			assertTrue(html.indexOf(pattern) > 0);
		}
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(FormTableTest.class.getName());
	}

}
