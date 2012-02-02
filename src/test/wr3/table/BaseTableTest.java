package test.wr3.table;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.Cell;
import wr3.Row;
import wr3.Table;
import wr3.table.BaseTable;
import wr3.table.CellFilter;

public class BaseTableTest {

	BaseTable baseTable;
	Table data = new Table();
	
	@Before
	public void init() {
		baseTable = new BaseTable();
		baseTable.data(new Table(5,3));
		baseTable.id("base");
	}
	
	@Test
	public void testCodes() {
		Row codes = baseTable.codes();
		assertEquals(Arrays.asList("c0", "c1", "c2", "c3", "c4"),
				codes.asList());
	}

	@Test
	public void meta_list() {
		
		baseTable.meta(Arrays.asList("A", "B", "C"));
		Table t = baseTable.result();
		assertEquals(Arrays.asList("A", "B", "C", "c3", "c4"), t.head().asList());
		
	}
	
	@Test 
	public void meta_map() {
		
		@SuppressWarnings("serial")
		Map<?,?> m = new HashMap<String, Object>() {{
			put("c0", "AA");
			put("c1", "BB");
			put("c2", "CC");
//			put("c3", "DD");			
			put("c4", "EE");
		}};
		baseTable.meta(m);
		Table t = baseTable.result();
		assertEquals(Arrays.asList("AA","BB","CC","c3","EE"), t.head().asList());
	}

	@Test
	public void testToString() {
		
		baseTable.toString();
	}
	
	@Test
	public void result() {
		
		data.head(Row.head(4))
			.add(Row.createByTypes(1,   2,   3,   4))
			.add(Row.createByTypes(10,  20,  30,  40))
			.add(Row.createByTypes(100, 200, 300, 400))
		;
		baseTable.data(data);
		Table rt = baseTable.result();
		assertEquals(4, rt.cols());
		assertEquals(3, rt.rows());
		assertEquals(Row.head(4), Row.head(4));
		assertEquals(Row.createByTypes(1,   2,   3,   4  ), rt.row(0));
		assertEquals(Row.createByTypes(10,  20,  30,  40 ), rt.row(1));
		assertEquals(Row.createByTypes(100, 200, 300, 400), rt.row(2));
//		System.out.println(baseTable.result());
	}
	
	@Test
	public void filter() {
		
		data.head(Row.head(4))
			.add(Row.createByTypes(1,   2,   3,   4))
			.add(Row.createByTypes(10,  20,  30,  40))
			.add(Row.createByTypes(100, 200, 300, 400))
		;
		baseTable.data(data).filter(new CellFilter() {

			public Cell process(int col, Cell cell) {
				return Cell.create(cell.value().toUpperCase());
			}

			public Cell process(int row, int col, Cell cell) {
				return Cell.create(cell.intValue()*100);
			}
		});
		
		Table rt = baseTable.result();
		for (int i = 0; i < rt.cols(); i++) {
			assertEquals(data.head(i).value().toUpperCase(), rt.head(i).value());
		}
		assertEquals(Row.createByTypes(100,200,300,400), rt.row(0));
		assertEquals(Row.createByTypes(1000,2000,3000,4000), rt.row(1));
		assertEquals(Row.createByTypes(10000,20000,30000,40000), rt.row(2));
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(BaseTableTest.class.getName());
	}

}
