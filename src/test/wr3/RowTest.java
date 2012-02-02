package test.wr3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.Cell;
import wr3.Row;
import wr3.util.Numberx;

public class RowTest {

	Row row;
	Row row2;
	List<Cell> cellList = new ArrayList<Cell>();
	Cell[] cellArray;
	
	@Test
	public void create() {
		row = new Row();
		assertNotNull(row);
		
		row = new Row(cellList);
		assertNotNull(row);
		
		row = new Row(cellArray);
		assertNotNull(row);
		
		row = Row.create(10); 
		assertNotNull(row);

		row2 = row.copy();
		assertEquals(row.length(), row2.size());		
		
		row = new Row(3, null);
		assertEquals(3, row.size());
		assertEquals(null, row.cell(0));
		assertEquals(null, row.cell(1));
		assertEquals(null, row.cell(2));
		
		row = new Row(2, Cell.create(3.14));
		assertEquals(2, row.size());
		assertEquals(new Cell(3.14), row.cell(0));
		assertEquals(new Cell(3.14), row.cell(1));
		
		row = new Row(0, new Cell());
		assertEquals(0, row.size());
		assertEquals(null, row.cell(0));
		
		row = new Row(-3, Cell.create(3.14));
		assertEquals(0, row.size());
		assertEquals(null, row.cell(0));
	}
	
	@Test 
	public void createByStrings() {
		String[] ss = {"a", "", null, "cn中文"};
		row = Row.createByStrings(ss);
		assertEquals(4, row.size());
		assertEquals(ss[0], row.cell(0).value());
		assertEquals(ss[1], row.cell(1).value());
		assertEquals("", row.cell(2).value());
		assertEquals(ss[3], row.cell(3).value());
	}
	
	@Test
	public void add() {
		row = new Row();
		Cell c1 = new Cell(100);
		Cell c2 = new Cell(101);
		Cell c3 = new Cell(201);
		Cell c4 = new Cell(202);
		row.add(c1);
		row.add(c2);
		row.add(0, c3);
		row.add(-1, c4);
		c1 = c2 = c3 = c4 = new Cell("new value");
		assertEquals(4, row.size());
		assertEquals(201, row.cell(0).intValue());
		assertEquals(202, row.cell(-2).intValue());
	}
	
	@Test 
	public void initByList() {
		
		cellArray = new Cell[10];
		cellList = Arrays.asList(cellArray);
		row = new Row(cellList);
		assertEquals(10, row.size());
		assertNull(row.get(0));
		assertNull(row.get(-1));
		
		cellList = new ArrayList<Cell>();
		cellList.add(new Cell(101));
		cellList.add(new Cell(0.1414));
		row = new Row(cellList);
		assertEquals(2, row.width());
		assertEquals(101, row.get(0).intValue());
		assertEquals(0.1414, row.get(-1).doubleValue(), 0);

		cellList = new ArrayList<Cell>();
		cellList.add(new Cell(1));
		cellList.add(new Cell(0.414));
		cellList.add(new Cell("cn中国"));
		row = new Row(cellList);
		// 对cellList的改变不会改变row
		cellList.set(0, new Cell("new value1"));		
		assertEquals(1, row.get(0).intValue());
	}
	
	@Test
	public void initByArray() {
		
		Object[] array = {null, Cell.create(10), 100L, 3.1416d, "cn中文"};
		row = new Row(array);
		assertEquals(array.length, row.size());
		assertEquals(array[0], row.get(0));
		assertEquals(10, row.get(1).intValue());
		assertEquals(100L, row.get(2).longValue());
		assertEquals(3.1416d, row.get(3).doubleValue(), 0);
		assertEquals(array[4], row.get(4).value());
		
		cellArray = new Cell[]{new Cell(1), new Cell(0.414), new Cell("cn中国")};
		row = new Row(cellArray);
		// 对cellArray的改变不会改变row
		cellArray[0] = new Cell(100);
		assertEquals(1, row.get(0).intValue());
	}
	
	@Test
	public void initByTypes() {
		
		row = Row.createByTypes(null, Cell.create(10), 100L, 3.1416d, "cn中文");
		assertEquals(5, row.size());
		assertEquals(null, row.get(0));
		assertEquals(10, row.get(1).intValue());
		assertEquals(100L, row.get(2).longValue());
		assertEquals(3.1416d, row.get(3).doubleValue(), 0);
		assertEquals("cn中文", row.get(4).value());
		
		row = Row.create(5);
		assertEquals(5, row.size());
		row = Row.createByTypes(5);
		assertEquals(1, row.size());
	}
	
	@Test
	public void head() {
		row = Row.createHead(-1);
		assertEquals(0, row.size());
		
		row = Row.createHead(0);
		assertEquals(0, row.size());
		
		row = Row.head(3);
		assertEquals(3, row.size());
		assertEquals("c0", row.get(0).value());
		assertEquals("c1", row.get(1).value());
		assertEquals("c2", row.get(2).value());
	}
	
	@Test 
	public void copy() {
		row = new Row(10);
		int i0 = row.get(0).intValue();
		row2 = row.copy();
		// row2和row的改变不会相互影响
		row.set(0, new Cell("row"));
		assertEquals("row", row.get(0).value());
		assertEquals(i0, row2.get(0).intValue());
		
		row2.set(0, new Cell("new value"));
		assertEquals("row", row.get(0).value());
		assertEquals("new value", row2.get(0).value());
	}
	
	@Test 
	public void get() {
		row = new Row();
		assertNull(row.get(0));
		assertNull(row.get(-1));
		
		Object[] o = {10, 0.14, "cn中国", null, 100L};	
		row = new Row(o);
		assertEquals(10, row.cell(0).intValue());
		assertEquals(0.14d, row.cell(1).doubleValue(), 0);
		assertEquals(o[2], row.cell(2).value());
		assertNull(row.cell(-2));
		assertEquals(100L, row.get(-1).longValue());
		
		List<Cell> list = row.getCells();
		assertEquals(o.length, list.size());
		// 改变list不会涉及row
		list.set(0, new Cell("new value"));
		assertEquals(10, row.cell(0).intValue());
		assertEquals(null, row.cell(-2));
	}
	
	@Test
	public void compute() {
		row = Row.create(10);
		row2 = Row.create(10);
		// add
		Row row3 = row2.copy().plus(row);
		double d12 = row.get(0).doubleValue() + row2.get(0).doubleValue();
		row.set(0, new Cell(1000));
		row2.set(0, new Cell(0.14));
		assertEquals(d12, row3.get(0).doubleValue(), 0);
		// join
		int n = row.size();
		row.join(row2);
		assertEquals(n+row2.size(), row.length());
		assertEquals(1000, row.cell(0).intValue());
		assertEquals(0.14, row.cell(10).doubleValue(), 0);
	}
	
	@Test
	public void size() {
		row = new Row();
		assertEquals(0, row.size());
		
		row = new Row(3);
		assertEquals(3, row.length());
		
		cellArray = new Cell[10];
		row = new Row(cellArray);
		assertEquals(10, row.size());
		assertEquals(10, row.length());
	}
	
	@Test
	public void asNull() {
		row = Row.create(new Cell[]{new Cell(10.23), null, new Cell(100.35)});
		Row row1 = row.asInt();
		assertEquals(10, row1.get(0).intValue());
		assertEquals(100, row1.get(2).intValue());

		assertEquals(null, row1.get(1));
	}
	
	@Test
	public void as1() {
		row = Row.create(new Cell[]{new Cell(10.23), new Cell(100.35)});
		Row row1 = row.asInt();
		assertEquals(10, row1.get(0).intValue());
		assertEquals(100, row1.get(1).intValue());

		row.set(1, new Cell("111"));
		// 只改变row
		assertEquals(10.23, row.get(0).doubleValue(), 0);
		assertEquals("111", row.get(1).value());	
		// 不影响row1
		assertEquals(10, row1.get(0).intValue());
		assertEquals(100, row1.get(1).intValue());
		
		row1.set(0, new Cell("000"));
		// 只改变row1
		assertEquals("000", row1.get(0).value());
		assertEquals(100, row1.get(1).intValue());	
		// 不影响row
		assertEquals(10.23, row.get(0).doubleValue(), 0);
		assertEquals("111", row.get(1).value());	
	}
	
	@Test
	public void as2() {
		row = Row.create(10);
		int i0 = row.get(0).intValue();
		int i9 = row.get(9).intValue();
		
		Row row1 = row.asInt();
		assertEquals(i0, row1.get(0).intValue());
		assertEquals(i9, row1.get(9).intValue());
		
		row1 = row.asLong();
		assertEquals((long)i0, row1.get(0).longValue());
		assertEquals((long)i9, row1.get(9).longValue());
		
		row1 = row.asDouble();
		assertEquals(0d+i0, row1.get(0).doubleValue(), 0);
		assertEquals(0d+i9, row1.get(9).doubleValue(), 0);
		
		row1 = row.asString();
		assertEquals(""+i0, row1.get(0).value());
		assertEquals(""+i9, row1.get(9).value());
		
		row1 = row.asPercent();
		assertEquals(Numberx.toPercent(0d+i0), row1.get(0).value());
		assertEquals(Numberx.toPercent(0d+i9), row1.get(9).value());
	}
	
	@Test
	public void set() {
		row = new Row();
		row.add(Cell.create(""));
		row.add(Cell.create(10));
		row.add(Cell.create(1000L));
		row.add(Cell.create(3.1415));
		
		row.add(0, Cell.create(0));
		row.add(3, Cell.create(4));		
		// 0, "", 10, 4, 1000L, 3.1415
		Row row1 = row.asInt();
		
		assertEquals(0, row1.get(0).intValue());
		assertEquals(0, row1.get(1).intValue());
		assertEquals(10, row1.get(2).intValue());
		assertEquals(4, row1.get(3).intValue());
		assertEquals(1000, row1.get(4).intValue());
		assertEquals(3, row1.get(5).intValue());

		// row 不会改变
		assertEquals(0, row.get(0).intValue());
		assertEquals("", row.get(1).value());
		assertEquals(10, row.get(2).intValue());
		assertEquals(4, row.get(3).intValue());
		assertEquals(1000L, row.get(4).longValue());
		assertEquals(3.1415d, row.get(5).doubleValue(), 0);
	}
	
	@Test
	public void remove() {
		row = new Row(10);
		int i1 = row.cell(1).intValue();
		int i8 = row.cell(8).intValue();
		row.remove(0);
		row.remove(-1);
		assertEquals(8, row.size());
		assertEquals(i1, row.cell(0).intValue());
		assertEquals(i8, row.cell(-1).intValue());

		Object[] o = {11,22,33,44,55};
		row = new Row(o);
		row.remove(-3);
		assertEquals(4, row.size());
		assertEquals(22, row.cell(-3).intValue());
		
		row.remove(0);row.remove(0);row.remove(0);row.remove(0);
		assertEquals(0, row.size());
		row.remove(0);
	}
	
	@Test
	/**
	 * {11,22,33,44,55}
	 * subrow(0,1): {11,22}
	 * subrow(0,4): {11,22,33,44,55}
	 * subrow(3,3): {44}
	 * subrow(4,1)==subrow(1,4): {22,33,44,55}
	 * subrow(4,4): {55}
	 * subrow(0,-1): {11,22,33,44,55}
	 * subrow(-2,-1): {44}
	 */
	public void subrow() {
		row = new Row(10);
		Row row2 = row.subrow(0, 10);
		assertEquals(row.cell(0).intValue(), row2.cell(0).intValue());
		
		Object[] o = {11,22,33,44,55};
		row = new Row(o);

		row2 = row.subrow(0,1);	// {11,22}
		assertEquals(2, row2.size());
		assertEquals(11, row2.cell(0).intValue());
		assertEquals(22, row2.cell(-1).intValue());

		row2 = row.subrow(0,4);	// {11,22,33,44,55}
		assertEquals(5, row2.size());
		assertEquals(11, row2.cell(0).intValue());
		assertEquals(55, row2.cell(-1).intValue());
		
		row2 = row.subrow(3,3);	// {44}
		assertEquals(1, row2.size());
		assertEquals(44, row2.cell(0).intValue());
		assertEquals(44, row2.cell(-1).intValue());
		
		row2 = row.subrow(4,1);	// {22,33,44,55}
		assertEquals(4, row2.size());
		assertEquals(22, row2.cell(0).intValue());
		assertEquals(55, row2.cell(-1).intValue());
		
		row2 = row.subrow(4,4);	// {55}
		assertEquals(1, row2.size());
		assertEquals(55, row2.cell(0).intValue());
		assertEquals(55, row2.cell(-1).intValue());
		
		row2 = row.subrow(0,-1);	// {11,22,33,44,55}
		assertEquals(5, row2.size());
		assertEquals(11, row2.cell(0).intValue());
		assertEquals(55, row2.cell(-1).intValue());
		
		row2 = row.subrow(-2,-1);	// {44,55}
		assertEquals(2, row2.size());
		assertEquals(44, row2.cell(0).intValue());
		assertEquals(55, row2.cell(-1).intValue());
	}
	
	@Test
	public void json() {
		row = new Row();
		assertEquals("[]", row.toJson());
		
		row = new Row(5);
		assertEquals(row.toString(), row.toJson());
		
		Object[] array = {null, Cell.create(10), 100L, 3.1416d, "cn\n中文"};
		row = new Row(array);
		assertEquals("[null, 10, 100, 3.1416, \"cn\\n中文\"]", row.toJson());
		
	}
	
	@Test 
	public void html() {
		row = new Row(5);
		int i0 = row.cell(0).intValue();
		assertTrue(row.toHtml(null).startsWith("<table><tr>"));
		assertTrue(row.toHtml("").endsWith("</tr></table>"));
		assertTrue(row.toHtml("t1").startsWith("<table id=\"t1\">"));
		assertTrue(row.toHtml(null).indexOf("<td>"+i0+"</td>")!=-1);
						
		assertTrue(row.toHtmlTr().startsWith("<tr>"));
		assertTrue(row.toHtmlTr().endsWith("</tr>"));

		assertTrue(row.toHtmlList(null).startsWith("<ul>"));
		assertTrue(row.toHtmlList("").endsWith("</ul>"));
		assertTrue(row.toHtmlList("l2").startsWith("<ul id=\"l2\">"));
		assertTrue(row.toHtmlList("").indexOf("<li>"+i0+"</li>")!=-1);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void test1() {
		List l = new ArrayList();
		l.add(10);
		l.add("hello");
		l.add(100L);
		l.add(3.1415d);
		l.add(null);
		assertEquals(Integer.class, l.get(0).getClass());
		assertEquals(String.class, l.get(1).getClass());
		assertEquals(Long.class, l.get(2).getClass());
		assertEquals(Double.class, l.get(3).getClass());
		assertEquals(null, l.get(4));
		
		Object[] l2 = {10, "hello", 100L, 3.1415d, null};
		assertEquals(Integer.class, l2[0].getClass());
		assertEquals(String.class, l2[1].getClass());
		assertEquals(Long.class, l2[2].getClass());
		assertEquals(Double.class, l2[3].getClass());
		assertEquals(null, l2[4]);
	}
	
	@Test
	public void equals() {
		
		Row r1 = new Row();
		Row r2 = new Row();
		assertEquals(r1, r2);
		
		r1 = Row.createByTypes(11, null, "", 100L, "str");
		r2 = Row.createByTypes(11, null, "", 100L, "str");
		assertEquals(r1, r2);

		Row r3 = Row.createByTypes(11, 3.14, "", 100L, "str");
		assertFalse(r1.equals(r3));
	}
	
	@Test
	public void testHashCode() {
		
		Row r1, r2;
		
		r1 = new Row();
		r2 = new Row();
		assertEquals(r1.hashCode(), r2.hashCode());
		
		r1 = Row.createByTypes(null, 10, "20", 30L, 3.14);
		r2 = Row.createByTypes(null, 10, "20", 30L, 3.14);
		assertEquals(r1.hashCode(), r2.hashCode());
		
		r1 = Row.createByTypes(10);
		r2 = Row.createByTypes(10L);
		assertEquals(r1.hashCode(), r2.hashCode());
		
		r1 = Row.createByTypes(10);
		r2 = Row.createByTypes(10d);
		assertFalse(r1.hashCode()==r2.hashCode());
		
		r1 = Row.createByTypes(1,2,3);
		r2 = Row.createByTypes(1,3,2);
		assertFalse(r1.hashCode()==r2.hashCode());
		
		// 特殊情况：不一样的Row有一样的hashCode()；
		// 但不equals，放入Map还是不同的key
		r1 = Row.createByTypes(1,3,5);
		r2 = Row.createByTypes(-36*17, 1,3,5);
		assertEquals(r1.hashCode(), r2.hashCode());
		Map<Row, String> map;
		map = new LinkedHashMap<Row, String>();
		Row r3 = Row.createByTypes(1,3,5);
		map.put(r1, "aaa");
		map.put(r2, "bbb");
		map.put(r3, "ccc");
		assertEquals(2, map.size());
		assertEquals("ccc", map.get(r1));
		assertEquals("bbb", map.get(r2));
		
		r1 = Row.createByTypes(1,3,null,5);
		r2 = Row.createByTypes(1,3,"<null>",5);
		assertEquals(r1.hashCode(), r2.hashCode());
		map.clear();
		map.put(r1, "aaa");
		map.put(r2, "bbb");
		r3 = Row.createByTypes(1,3,null,5);
		map.put(r3, "ccc");
		assertEquals(2, map.size());
		assertEquals("ccc", map.get(r1));
		assertEquals("bbb", map.get(r2));
	}
		
	@Test
	public void testCompare() {
		
		Row r1, r2;
		
		r1 = Row.createByTypes(1,200,3,4);
		r2 = Row.createByTypes(1,30,40,50,60);
		assertTrue(r1.compareTo(r2)>0);

		r1 = Row.createByTypes(10,"",10L,3.14);
		r2 = Row.createByTypes(10,"",10L,3.14);
		assertTrue(r1.compareTo(r2)==0);
		
		r1 = Row.createByTypes(10,"",10L,3.14, null);
		r2 = Row.createByTypes(10,"",10L,3.14, null, null);
		assertTrue(r1.compareTo(r2)<0);
		
		r1 = new Row();
		r2 = Row.createByTypes();
		assertTrue(r1.compareTo(r2)==0);
		
		r1 = new Row();
		r2 = Row.createByTypes("");
		assertTrue(r1.compareTo(r2)<0);
		
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {

		JUnitCore.main(RowTest.class.getName());
	}
}
