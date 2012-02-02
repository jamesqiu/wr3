package test.wr3.table;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.Cell;
import wr3.Row;
import wr3.Table;
import wr3.table.GroupFilter;
import wr3.table.GroupTable;

public class GroupTableTest {

	@Test
	public void resultBound() {
		
		// 测试边界情况
		Table data = new Table();
		GroupTable t = GroupTable.create()
			.data(data)
			.group(0)
			;
		Table rt = t.result();
		
		assertEquals(0, rt.cols());
		assertEquals(0, rt.rows());
		
		// 空结果集
		t.data(new Table(5, 0));
		rt = t.result();
		assertEquals(5, rt.cols());
		assertEquals(1, rt.rows());
		assertEquals("总计", rt.cell(0, 0).value());
		assertEquals(new Cell(), rt.cell(0, 1));
		assertEquals(new Cell(), rt.cell(0, -1));
		
		// 正常情况
		data = new Table().head(Row.createHead(3))
			.add(Row.createByTypes("a", 1,  20))
			.add(Row.createByTypes("b", 2, 300))
			.add(Row.createByTypes("b", 1, 200))
			.add(Row.createByTypes("a", 3,  10))
			;

		t.data(data);
		rt = t.result();
		assertEquals(data.rows()+1+2, rt.rows());
		assertEquals(data.cols(), rt.cols());
		assertEquals("总计", rt.cell(0, 0).value());
		assertEquals("a", rt.cell(1, 0).value());
		assertEquals("", rt.cell(2, 0).value());
		assertEquals("", rt.cell(3, 0).value());
		assertEquals("b", rt.cell(4, 0).value());
		assertEquals("", rt.cell(5, 0).value());
		assertEquals("", rt.cell(6, 0).value());

		int colIndex = 1;
		t.data(data); // 必须调用data(data), 否则不处理(needProcess=false)
		t.group(colIndex); 
		rt = t.result();
		assertEquals(data.rows()+1+3, rt.rows());
		assertEquals(data.cols(), rt.cols());
		assertEquals("总计", rt.cell(0, 0).value());
		assertEquals("1", rt.cell(1, colIndex).value());
		assertEquals("", rt.cell(2, colIndex).value());
		assertEquals("", rt.cell(3, colIndex).value());
		assertEquals("2", rt.cell(4, colIndex).value());
		assertEquals("", rt.cell(5, colIndex).value());
		assertEquals("3", rt.cell(6, colIndex).value());
		assertEquals("", rt.cell(7, colIndex).value());
	}
	
	@Test
	public void groupFilter() {
		
		int i0 = 40, i1 = 2, i2 = 20, i3 = 60;
		Table data = new Table().head(Row.createHead(4))
		.add(Row.createByTypes("a", i0, "hello", 20))
		.add(Row.createByTypes("b", i1,  null, 3))
		.add(Row.createByTypes("b", i2, 3.1415, 30))
		.add(Row.createByTypes("b", i3, 10000L, 10))
		;
	
		GroupTable t = GroupTable.create()
			.data(data)
			.group(0, new GroupFilter() {
				public Cell process(Cell cell) {
					return (cell.value()=="a") ? new Cell("A组") : new Cell("B组");
				}
			})
		;
		Table rt = t.result();
		
		assertEquals(data.cols()+1, rt.cols());
		assertEquals(data.rows()+3, rt.rows());
		assertEquals("总计", rt.cell(0, 0).value());
		assertEquals("A组", rt.cell(1, 0).value());
		assertEquals("B组", rt.cell(3, 0).value());
		assertEquals(i0+i1+i2+i3, rt.cell(0, 2).intValue());
		assertEquals(i0, rt.cell(1, 2).intValue());
		assertEquals(i1+i2+i3, rt.cell(3, 2).intValue());
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(GroupTableTest.class.getName());
	}

}
