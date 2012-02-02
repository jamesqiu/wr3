package test.wr3.table;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.Row;
import wr3.Table;
import wr3.table.CrossTable;

public class CrossTableTest {

	Table table;
	
	@Before
	public void setUp() throws Exception {
		table = new Table()
			.head(Row.createByTypes("org","name", "value"))
			.add(Row.createByTypes("o1","n1", 1))
			.add(Row.createByTypes("o1","n2", 1))
			.add(Row.createByTypes("o1","n3", 1))
			.add(Row.createByTypes("o2","n1", 1))
			.add(Row.createByTypes("o2","n2", 1))
			.add(Row.createByTypes("o3","n2", 1))
			.add(Row.createByTypes("o3","n3", 1))
			.add(Row.createByTypes("o4","n1", 1))
			.add(Row.createByTypes("o4","n3", 1))
		;
	}

	@After
	public void tearDown() throws Exception {
		table = null;
	}

	@Test
	public void result1() {
		CrossTable t = CrossTable.create()
			// 不设定指标和维度，c_0:上维度, c_1:左维度, c_2:指标
			.data(table)
		;
		Table rt = t.result();
		assertEquals(5, rt.cols());
		assertEquals(4, rt.rows());
		assertEquals(Row.createByTypes("c0","c1","c2","c3","c4"),
				rt.head());
		assertEquals(Row.createByTypes("",  "o1","o2","o3","o4"),
				rt.row(0));
		assertEquals(Row.createByTypes("n1", 1,   1,   null,1),
				rt.row(1));
		assertEquals(Row.createByTypes("n2", 1,   1,   1,   null),
				rt.row(2));
		assertEquals(Row.createByTypes("n3", 1,   null,1,   1),
				rt.row(3));
	}
	
	@Test
	public void result2() {
		CrossTable t = CrossTable.create()
			.data(table)
			// 自行设定维度和指标
			.left("ORG").top("Name").measure("value")
		;
		Table rt = t.result();
		assertEquals(4, rt.cols());
		assertEquals(5, rt.rows());
		assertEquals(Row.createByTypes("c0","c1","c2","c3"),
				rt.head());
		assertEquals(Row.createByTypes("",  "n1","n2","n3"),
				rt.row(0));
		assertEquals(Row.createByTypes("o1", 1,   1,   1),
				rt.row(1));
		assertEquals(Row.createByTypes("o2", 1,   1,   null),
				rt.row(2));
		assertEquals(Row.createByTypes("o3", null,1,   1),
				rt.row(3));
		assertEquals(Row.createByTypes("o4", 1,   null,1),
				rt.row(4));
	}
	
	@Test
	public void result3() {
		CrossTable t = CrossTable.create()
		.data(table)
		// 上维度==左维度
		.left("name").top("NAME").measure("value")
		;
		Table rt = t.result();
		assertEquals(4, rt.cols());
		assertEquals(4, rt.rows());
		assertEquals(Row.createByTypes("c0","c1","c2","c3"),
				rt.head());
		assertEquals(Row.createByTypes("",  "n1","n2","n3"),
				rt.row(0));
		assertEquals(Row.createByTypes("n1", 1,   null,null),
				rt.row(1));
		assertEquals(Row.createByTypes("n2", null,1,   null),
				rt.row(2));
		assertEquals(Row.createByTypes("n3", null,null,1),
				rt.row(3));
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(CrossTableTest.class.getName());
	}

}
