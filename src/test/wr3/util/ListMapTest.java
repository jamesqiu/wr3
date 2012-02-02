package test.wr3.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.util.Datetime;
import wr3.util.ListMap;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class ListMapTest {

	ListMap lm;
	
	@Before
	public void setup() {
		
		lm = ListMap.create();
		lm.put(6, "int key");
		lm.put(6L, "long key");
		lm.put(6.0, "double key");
		lm.put("k6", "string key");
		lm.put(Datetime.date(2009,9,10), "date key");
	}
	
	@Test
	public void testPut() {
		
		assertEquals(5, lm.size());
		
		lm.put(null, null);
		lm.put(null, "hello");
		assertEquals(6, lm.size());
		assertEquals(null, lm.key(-1));
		assertEquals("hello", lm.val(-1));
		
		lm.put("k6", null);
		assertEquals(6, lm.size());
		assertEquals(null, lm.val("k6"));
		
	}

	@Test
	public void testKeyInt() {

		assertEquals(6, lm.key(0));
		assertEquals(6L, lm.key(1));
		assertEquals(6.0, lm.key(2));
		assertEquals("k6", lm.key(-2));
		assertEquals(Datetime.date(2009,9,10), lm.key(-1));
		assertEquals(lm.key(3), lm.key(3 + lm.size()));
		
		assertTrue(lm.key(0) instanceof Integer);
		assertTrue(lm.key(-2) instanceof String);
		assertTrue(lm.key(-1) instanceof java.util.Date);
	}

	@Test
	public void testKeyIntObject() {
		
		lm.put("k1", 1024);
		assertEquals("k1", lm.key(-1));
		
		lm.key(-1, "k2");
		
		assertEquals("k2", lm.key(-1));
		assertEquals(1024, lm.val(-1));
		assertEquals(1024, lm.val("k2"));
	}

	@Test
	public void testValInt() {
		
		assertEquals("int key", lm.val(0));
		assertEquals("long key", lm.val(1));
		assertEquals("date key", lm.val(-1));
	}

	@Test
	public void testValObject() {
		
		assertEquals("int key", lm.val(new Integer(6)));
		assertEquals("long key", lm.val(new Long(6L)));
		assertEquals("date key", lm.val(-1));
		assertEquals("double key", lm.val(new Double(6.0)));
		
	}

	@Test
	public void testValIntObject() {
		
		
		assertEquals("int key", lm.val(0));
		assertEquals("date key", lm.val(-1));
		
		lm.val(0, "new int key");
		lm.val(-1, "new date key");
		
		assertEquals("new int key", lm.val(0));
		assertEquals("new date key", lm.val(-1));
	}

	@Test
	public void testSize() {
		
		assertEquals(5, lm.size());
		
		lm.put("new", "...");
		assertEquals(6, lm.size());
		lm.put("new", "***");
		assertEquals(6, lm.size());
	}

	@Test
	public void testToString() {
		
		String s = "{\n" +
				"<6> : <int key>\n" + 
				"<6> : <long key>\n" + 
				"<6.0> : <double key>\n" + 
				"<k6> : <string key>\n" + 
				"<Thu Sep 10 00:00:00 CST 2009> : <date key>\n" + 
				"}";
		assertEquals(s, lm.toString());
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(ListMapTest.class.getName());
	}

}
