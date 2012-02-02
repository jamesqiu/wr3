package test.wr3.util;

import static org.junit.Assert.*;
import static wr3.util.Rowx.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.Cell;
import wr3.Row;
import wr3.Table;
import wr3.util.Rowx;

public class RowxTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testIndex() {
		Row row = Row.createByTypes("blah", 10, 10L, 3.14, "hello", null);
		assertEquals(1, index(new Cell(10), row));
		assertEquals(1, index(new Cell(10L), row)); // �ȶ�cell.value()
		assertEquals(3, index(new Cell(3.14), row));
		assertEquals(4, index(new Cell("hello"), row));
		assertEquals(4, index(new Cell("Hello"), row));
		assertEquals(-1, index(null, row));
		assertEquals(-1, index(new Cell(10), null));
		assertEquals(-1, index(new Cell(100), row));
	}

	@Test
	public void testUniq() {
		Row row = Row.createByTypes(
				null, 10, 10L, 10, "hello", "Hello", "hello", null);
		Row rt = uniq(row);
		assertEquals(5, rt.size());
		assertEquals(null, rt.cell(0));
		assertEquals(new Cell(10), rt.cell(1));
		assertEquals(new Cell(10L), rt.cell(2));
		assertEquals(new Cell("hello"), rt.cell(3));
		assertEquals(new Cell("Hello"), rt.cell(4));
	}
	
	@Test
	public void uniqCols() {
		
		Table table = new Table();
		table.head(Row.createByTypes("month", "name", "hq"));
		table.add(Row.createByTypes("1��", "��һ", "12593"));
		table.add(Row.createByTypes("1��", "���", "3579"));
		table.add(Row.createByTypes("1��", "����", "11721"));
		table.add(Row.createByTypes("1��", "����", "12277"));
		table.add(Row.createByTypes("1��", "����", "14558"));
		table.add(Row.createByTypes("2��", "��һ", "13014"));
		table.add(Row.createByTypes("2��", "���", "21035"));
		table.add(Row.createByTypes("2��", "����", "10914"));
		table.add(Row.createByTypes("2��", "����", "19846"));
		table.add(Row.createByTypes("2��", "����", "6557"));
		table.add(Row.createByTypes("2��", "����", "5332"));
		table.add(Row.createByTypes("3��", "��һ", "12342"));
		table.add(Row.createByTypes("3��", "���", "9470"));
		table.add(Row.createByTypes("3��", "����", "13946"));
		table.add(Row.createByTypes("3��", "����", "10897"));
		table.add(Row.createByTypes("3��", "����", "21401"));
		
		Table rt;
		rt = Rowx.uniq(table.col(0));
		assertEquals(1, rt.cols());
		assertEquals(3, rt.rows());
		assertEquals(new Cell("1��"), rt.cell(0, 0));
		assertEquals(new Cell("2��"), rt.cell(1, 0));
		assertEquals(new Cell("3��"), rt.cell(2, 0));
		
		rt = Rowx.uniq(table.col(1)); 
		// ע�⣺�����������, (int)'��' < (int)'��'
		assertEquals(1, rt.cols());
		assertEquals(6, rt.rows());
		assertEquals(new Cell("��һ"), rt.cell(0, 1));
		assertEquals(new Cell("���"), rt.cell(1, 1));
		assertEquals(new Cell("����"), rt.cell(2, 1));
		assertEquals(new Cell("����"), rt.cell(3, 1));
		assertEquals(new Cell("����"), rt.cell(4, 1));
		assertEquals(new Cell("����"), rt.cell(5, 1));
		
		rt = Rowx.uniq(table.col(0), table.col(1));
		assertEquals(2, rt.cols());
		assertEquals(table.rows(), rt.rows());
	}
	
	@Test
	public void testGroup() {
		
		Row row = Row.createByTypes(
				null, 10, 10L, 10, "hello", "Hello", "hello", null);
		Table rt = group(row);
		assertEquals(5, rt.rows());
		int cellIndex = 0, countIndex = 1;
		
		assertEquals(null, rt.cell(0,cellIndex));
		assertEquals(new Cell(10), rt.cell(1,cellIndex));
		assertEquals(new Cell(10L), rt.cell(2,cellIndex));
		assertEquals(new Cell("hello"), rt.cell(3,cellIndex));
		assertEquals(new Cell("Hello"), rt.cell(4,cellIndex));
		
		assertEquals(new Cell(2), rt.cell(0, countIndex));
		assertEquals(new Cell(2), rt.cell(1, countIndex));
		assertEquals(new Cell(1), rt.cell(2, countIndex));
		assertEquals(new Cell(2), rt.cell(3, countIndex));
		assertEquals(new Cell(1), rt.cell(4, countIndex));
	}
	
	@Test
	public void testGroup2() {
		// ����Rowx.group()���������
		Row row;
		row = null;
		assertEquals(2, group(row).cols());
		assertEquals(0, group(row).rows());
		
		row = new Row();
		assertEquals(2, group(row).cols());
		assertEquals(0, group(row).rows());
	}
	
	@Test
	public void testSum() {
		Row row;
		
		row = Row.createByTypes(
				null, 10, 10L, 10, "hello", "Hello", "hello", null);
		assertEquals(new Cell(30L), sum(row));
		
		row.add(new Cell(10d));
		assertEquals(new Cell(40d), sum(row));
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(RowxTest.class.getName());
	}

}
