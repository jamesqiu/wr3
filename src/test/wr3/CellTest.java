package test.wr3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.Cell;


public class CellTest {

	Cell cell;

	@Test
	public void create() {
		cell = new Cell();
		assertNotNull(cell);
		cell = Cell.create(1);
		cell = Cell.create(0l);		
		cell = Cell.create("cn中文");
		cell = Cell.create(10.3);
		cell = Cell.create((String)null);
		assertNotNull(cell);
		assertEquals(String.class, cell.type());
		assertEquals("", cell.value());		
		assertEquals(0, cell.intValue());		
		
		Cell cell1;
		cell = Cell.create(100L);
		cell1 = new Cell(cell);
		cell = Cell.create("new value");
		assertEquals(100L, cell1.longValue());
		assertEquals("new value", cell.value());
		
		// cell的copy().
		cell = Cell.create(3.1415);
		cell1 = new Cell(cell);//cell.copy();
		cell = Cell.create("new value");
		assertEquals("3.1415", cell1.value());
		assertEquals("new value", cell.value());
	}
	
	@Test
	public void type() {
		cell = new Cell();
		
		assertEquals(String.class, cell.type());
		assertTrue(cell.isString());
		
		cell = Cell.create(1);
		assertEquals(cell.type(), Integer.class);
		assertTrue(cell.isNumber());
		
		cell = Cell.create(1000000000000000000L);
		assertEquals(Long.class, cell.type());
		assertTrue(cell.isNumber());
		
		cell = Cell.create("cn中文");
		assertEquals(cell.type(), String.class);
		assertTrue(cell.isString());
		
		cell = Cell.create(0.1414);
		assertEquals(cell.type(), Double.class);
		assertTrue(cell.isNumber());
		assertEquals(cell.asInt().type(), Integer.class);
		assertTrue(cell.asInt().isNumber());
		assertEquals(cell.type(), Double.class);
		assertTrue(cell.isNumber());
		
		
		cell = Cell.create(10);
		assertEquals(cell.type().getSuperclass(), Number.class);
		assertTrue(cell.isNumber());
	}
	
	@Test
	public void get() {
		cell = Cell.create(0);
		assertEquals("0", cell.value());
		assertEquals(0, cell.intValue());
		assertEquals(0l, cell.longValue());
		assertEquals(0d, cell.doubleValue(), 0.0);
		assertEquals("0%", cell.percentValue());
		assertEquals("0", cell.toString());
		
		cell = Cell.create(1000000000000000000L);
		assertEquals("1000000000000000000", cell.value());
		assertEquals((int)1000000000000000000L, cell.intValue());
		assertEquals(1000000000000000000d, cell.doubleValue(), 0.0);
		assertEquals("100000000000000000000%", cell.percentValue());
		assertEquals("1000000000000000000", cell.toString());

		cell = Cell.create("3.50505");
		assertEquals("3.50505", cell.value());
		assertEquals(4, cell.intValue());
		assertEquals(3.50505d, cell.doubleValue(), 0.0);
		assertEquals("350.5%", cell.percentValue());
		assertEquals("3.50505", cell.toString());

		cell = Cell.create(3.50505);
		assertEquals("3.50505", cell.value());
		assertEquals(4, cell.intValue());
		assertEquals(3.50505d, cell.doubleValue(), 0.0);
		assertEquals("350.5%", cell.percentValue());
		assertEquals("3.50505", cell.toString());
	}
	
	@Test 
	public void as() {
		cell = new Cell("100");
		assertEquals(Integer.class, cell.asInt().type());
		assertEquals(100, cell.asInt().intValue());
		assertEquals(Long.class, cell.asLong().type());
		assertEquals(100L, cell.asLong().longValue());
		assertEquals(Double.class, cell.asDouble().type());
		assertEquals(100d, cell.asDouble().doubleValue(), 0.0);
		assertEquals(String.class, cell.asPercent().type());
		assertEquals("10000%", cell.asPercent().value());
		
	}
	
	@Test 
	public void json() {
		cell = new Cell();
		assertEquals("\"\"", cell.toJson());
		cell = new Cell(10);
		assertEquals("10", cell.toJson());
		cell = new Cell(100L);
		assertEquals("100", cell.toJson());
		cell = new Cell(3.1415d);
		assertEquals("3.1415", cell.toJson());
		cell = new Cell("cn中文");
		assertEquals("\"cn中文\"", cell.toJson());
		cell = new Cell("hello\nworld");
		assertEquals("\"hello\\nworld\"", cell.toJson());
		cell = new Cell(""+true);
		assertEquals("\"true\"", cell.toJson());
	}
	
	@Test 
	public void list() {
		
		List<Cell> l1 = new ArrayList<Cell>();
		Cell c1 = new Cell(10);
		Cell c2 = new Cell(20);
		l1.add(c1); 
		l1.add(c2);
				
		List<Cell> l2 = new ArrayList<Cell>();
		l2.add(l1.get(0));
		l2.add(l1.get(1));
		
//		l2.get(0).set(new Cell("new"));   // 这是错误的,会改变l1
		l2.set(0, new Cell("new"));
		
		assertEquals("new", l2.get(0).value());
		assertEquals("10", l1.get(0).value());
	}
	
	@Test
	public void equals() {
		
		assertEquals(new Cell(), new Cell());
//		assertTrue(new Cell() == new Cell());
		
		assertEquals(new Cell(1+1), Cell.create(2));
		assertEquals(new Cell(Integer.MAX_VALUE*10L), new Cell(10L*Integer.MAX_VALUE));
		assertEquals(new Cell(3.1415), Cell.create(3.1415));
		assertEquals(new Cell("cn中文"), new Cell("cn"+"中文"));
		
		assertFalse(new Cell(1+1).equals(new Cell(3)));
		assertFalse(new Cell(2L).equals(new Cell(3L)));
		assertFalse(new Cell(3.14).equals(new Cell(3.1415)));
		assertFalse(new Cell("cn中文").equals(new Cell("cn-"+"中文")));
		
		Cell c1 = new Cell(3 + 0.1415);
		Cell c2 = c1;
		Cell c3 = new Cell(3.1415);
		
		assertTrue(c1==c2);
		assertEquals(c1, c2);
		assertFalse(c1==c3);
		assertEquals(c1, c3);
	}
	
	@Test
	public void hash() {
		
		final Cell c1 = new Cell("k1");
		final Cell c2 = new Cell("k2");
		final Cell c3 = null;
		Map<Cell, String> m = new LinkedHashMap<Cell, String>();
		m.put(c1, "v1");
		m.put(c2, "v2");
		m.put(c3, "v3");
		assertEquals("v1", m.get(c1));		
		assertEquals("v2", m.get(c2));		
		assertEquals("v3", m.get(c3));		
		assertEquals("v1", m.get(new Cell("k1")));
		assertEquals("v2", m.get(new Cell("k"+2)));
		assertEquals("v3", m.get(null));
		assertEquals(null, m.get(new Cell("k3")));
	}
	
	@Test
	public void testCompare() {
		
		assertTrue(new Cell().compareTo(null) > 0);
		// Integer
		assertTrue(new Cell(1).compareTo(new Cell(2)) < 0);
		assertTrue(new Cell(1).compareTo(new Cell(0)) > 0);
		assertTrue(new Cell(1).compareTo(new Cell(1)) == 0);
		// Long
		assertTrue(new Cell(10L).compareTo(new Cell(20L)) < 0);
		assertTrue(new Cell(10L).compareTo(new Cell(9L)) > 0);
		assertTrue(new Cell(10L).compareTo(new Cell(10L)) == 0);
		// Double
		assertTrue(new Cell(3.14).compareTo(new Cell(3.1401)) < 0);
		assertTrue(new Cell(3.14).compareTo(new Cell(3.133339)) > 0);
		assertTrue(new Cell(3.14).compareTo(new Cell(3.14)) == 0);
		// String
		assertTrue(new Cell("aa").compareTo(new Cell("ab")) < 0);
		assertTrue(new Cell("aa").compareTo(new Cell("a")) > 0);
		assertTrue(new Cell("aa").compareTo(new Cell("aa")) == 0);
		// Number
		assertTrue(new Cell(10).compareTo(new Cell(9L)) > 0);
		assertTrue(new Cell(10).compareTo(new Cell(10.3)) < 0);
		assertTrue(new Cell(10L).compareTo(new Cell(10.0)) == 0);
		// Others
		assertTrue(new Cell(8L).compareTo(new Cell("9")) < 0);
		assertTrue(new Cell(20L).compareTo(new Cell("100")) > 0);
		assertTrue(new Cell(20L).compareTo(new Cell("20")) == 0);
	}
	
	@Test
	public void testAdd() {
		
		Cell c1, c2;
		
		c1 = new Cell(10);
		c2 = null;		
		assertEquals(c1, c1.add(c2));
		assertEquals(10, c1.intValue());
		
		c1 = new Cell(10);
		c2 = new Cell(100);
		assertEquals(new Cell(110), c1.add(c2));
		assertEquals(new Cell(110), c2.add(c1));
		assertEquals(new Cell(10), c1);
		assertEquals(new Cell(110), c1.add(100));
		
		c1 = new Cell(10);
		c2 = new Cell(1L);
		assertEquals(new Cell(11L), c1.add(c2));
		assertEquals(new Cell(11L), c2.add(c1));
		assertEquals(new Cell(10), c1);
		assertEquals(new Cell(11L), c1.add(1L));
		
		c1 = new Cell(10);
		c2 = new Cell(3.14);
		assertEquals(new Cell(13.14), c1.add(c2));
		assertEquals(new Cell(13.14), c2.add(c1));
		assertEquals(new Cell(10), c1);
		assertEquals(new Cell(13.14), c1.add(3.14));
		
		c1 = new Cell(10);
		c2 = new Cell();
		assertEquals(new Cell("10"), c1.add(c2));
		assertEquals(new Cell("10"), c2.add(c1));
		assertEquals(new Cell(10), c1);
		assertEquals(new Cell("10"), c1.add(""));
		
		c1 = new Cell(10);
		c2 = new Cell("aaa");
		assertEquals(new Cell("10aaa"), c1.add(c2));
		assertEquals(new Cell("aaa10"), c2.add(c1));
		assertEquals(new Cell(10), c1);
		assertEquals(new Cell("aaa"), c2);
		assertEquals(new Cell("10aaa"), c1.add("aaa"));
		
		c1 = new Cell();
		c2 = null;
		assertEquals(new Cell(), c1.add(c2));
	}
		
	// ---------------------- main() ----------------------
	public static void main(String[] args) {

		JUnitCore.main(CellTest.class.getName());
	}
}
