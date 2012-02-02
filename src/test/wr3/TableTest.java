package test.wr3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.Cell;
import wr3.Col;
import wr3.Row;
import wr3.Table;

public class TableTest {

	Table table;
	
	@Test
	public void create() {
		
		table = new Table();
		assertNotNull(table);
		assertEquals(0, table.rows());
		assertEquals(0, table.cols());
		assertNull(table.row(0));
		assertNull(table.cell(1,1));
		
		table = new Table(10, 5); // width: 10, height: 5
		assertEquals(10, table.width());
		assertEquals(5, table.height());
	}
	
	@Test
	public void head() {
		
		table = new Table();
		assertNotNull(table.head());
		assertEquals(0, table.width());
		assertNull(table.head(0));
		
		table = new Table(5);
		assertEquals("c0", table.head(0).value());
		assertEquals("c4", table.head(-1).value());
		
		table.head(0, new Cell("h1"));
		assertEquals("h1", table.head(0).value());
	}
	
	@Test 
	public void add() {
		
		table = new Table(5);
		Row r1 = new Row(3);
		Row r2 = new Row(10);
		table.add(r1).add(r2);
		int c1 = r1.cell(0).intValue();
		int c2 = r2.cell(0).intValue();
		r1.cell(0, new Cell(101));
		r2.cell(0, new Cell("new value"));
		assertNull(table.cell(0, 3));
		assertNull(table.cell(0, 4));
		assertEquals(c1, table.cell(0, 0).intValue());
		assertEquals(c2, table.cell(1, 0).intValue());
	}
	
	@Test
	public void addcol() {
		
		table = new Table(5,3);
		int rows = table.rows();
		int cols = table.cols();
		
		Row row = new Row(3);
		Col col = row.asCol();
		
		// 插入最前
		table.addcol(0, col);
		
		assertEquals(rows, table.rows());
		assertEquals(cols+1, table.cols());

		assertEquals(row.cell(0), table.cell(0, 0));
		assertEquals(row.cell(1), table.cell(1, 0));
		assertEquals(row.cell(2), table.cell(2, 0));
		
		// 插入最后
		table.addcol(col);
		
		assertEquals(rows, table.rows());
		assertEquals(cols+2, table.cols());
		
		assertEquals(row.cell(0), table.cell(0, cols+1));
		assertEquals(row.cell(1), table.cell(1, cols+1));
		assertEquals(row.cell(2), table.cell(2, cols+1));
	}
	
	@Test 
	public void get() {
		table = new Table(3, 5);
		Row row = table.row(0);
		int i = row.cell(0).intValue();
		row.cell(0, new Cell("new value"));
		assertEquals(i, table.cell(0, 0).intValue());
	}
	
	@Test
	public void set() {
		
		table = new Table();
		// 对空Table进行设置是无效的
		table.row(0, new Row(3)).cell(0,0,new Cell(123));
		assertEquals(0, table.width());
		assertEquals(0, table.height());
		
		table = new Table(3, 5);
		int i1 = table.cell(0, 0).intValue();
		// 设置最后row
		Row row = new Row(3);
		int i2 = row.cell(-1).intValue();
		table.row(-1, row);
		row = new Row(10);
		assertEquals(i1, table.cell(0, 0).intValue());
		assertEquals(i2, table.cell(-1, -1).intValue());
		
		// 设置最后一个cell
		table.cell(-1, -1, new Cell("new value"));
		assertEquals(i1, table.cell(0, 0).intValue());
		assertEquals("new value", table.cell(-1, -1).value());		
	}
	
	@Test
	public void remove() {
		table = new Table(3,5);  // 3列5行
		int i1 = table.cell(1,0).intValue();
		int i2 = table.cell(3,2).intValue();
		// rm第一行和最后一行, 中间一列
		table.rmrow(0).rmrow(-1).rmcol(-2);
		assertEquals(2, table.cols());
		assertEquals(3, table.rows());
		assertEquals(i1, table.cell(0,0).intValue());
		assertEquals(i2, table.cell(-1,-1).intValue());
		// 删除所有行, 只剩表头
		int row = table.rows();
		for (int i = 0; i < row; i++) {
			table.rmrow(0);
		}
		assertEquals(0, table.rows());
		assertEquals(2, table.cols());
		table.add(new Row(2));
		assertEquals(1, table.rows());
		// 删除所有列, 每行都空
		table = new Table(2,5);  // 2列5行
		table.rmcol(0).rmcol(-1);
		assertEquals(0, table.cols());
		assertEquals(5, table.rows());
		table.head(new Row(3));
		table.add(new Row(3));
		assertEquals(3, table.cols());
		assertEquals(6, table.rows());
	}
	
	@Test
	public void sub() {
		table = new Table(3, 5);
		
		Table t1 = table.subrow(0, 3);
		assertEquals(3, t1.cols());
		assertEquals(4, t1.rows());		
		t1 = table.subrow(-1, 0);
		assertEquals(3, t1.cols());
		assertEquals(5, t1.rows());

		t1 = table.subcol(0, 0);
		assertEquals(1, t1.cols());
		assertEquals(5, t1.rows());
		t1 = table.subcol(-1, 1);
	}
	
	@Test
	public void json() {
		table = new Table();
		assertTrue(table.toJson().indexOf("head: []")!=-1);
		assertTrue(table.toJson().indexOf("data:[]")!=-1);
		
		table = new Table(5);
		assertTrue(table.toJson().indexOf("{head: [\"c0\", \"c1\", \"c2\", \"c3\", \"c4\"]")!=-1);
		assertTrue(table.toJson().indexOf("data:[]")!=-1);
		
		table = new Table(3, 5);
		assertTrue(table.toJson().indexOf("{head: [\"c0\", \"c1\", \"c2\"]")!=-1);
		assertTrue(table.toJson().indexOf("data:[[")!=-1);
	}
	
	@Test
	public void html() {
		table = new Table(3, 5);
		int i0 = table.cell(0, 0).intValue();
		int i1 = table.cell(-1, -1).intValue();
		String html = table.toHtml("t1");
		assertTrue(html.startsWith("<table id=\"t1\">"));
		assertTrue(html.endsWith("</table>"));
		assertTrue(html.indexOf("<thead><th>c0</th><th>c1</th><th>c2</th></thead>")!=-1);
		assertTrue(html.indexOf("<tbody><tr><td>"+i0+"</td>")!=-1);
		assertTrue(html.indexOf("<td>"+i1+"</td></tr></tbody>")!=-1);
	}
	
	@Test
	public void toArray() {
		table = new Table(3,5);
		String[][] rt = table.toArray();
		assertEquals(table.rows(), rt.length);
		assertEquals(table.cols(), rt[0].length);
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {

		JUnitCore.main(TableTest.class.getName());
	}
}
