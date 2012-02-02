package test.wr3.table;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.table.CubeLayoutParser;

public class CubeLayoutParserTest {

	static String[] nullss = new String[0];
	
	@Test
	public void test1() {
		
		String express = "";
		CubeLayoutParser parser = CubeLayoutParser.create(express);
		String[] top = parser.top();
		String[] left = parser.left();
		String[] measure = parser.measure();
		boolean onTop = parser.measureOnTop();
		
		assertEquals(0, top.length);
		assertEquals(0, left.length);
		assertEquals(0, measure.length);
		assertTrue(onTop);
	}

	@Test
	public void test2() {
		
		String express = "key1,key2, key3";
		CubeLayoutParser parser = CubeLayoutParser.create(express);
		String[] top = parser.top();
		String[] left = parser.left();
		String[] measure = parser.measure();
		boolean onTop = parser.measureOnTop();
		
		assertEquals(0, top.length);
		assertEquals(3, left.length);
		assertEquals("key1", left[0]);
		assertEquals("key2", left[1]);
		assertEquals(" key3", left[2]);
		assertEquals(0, measure.length);
		assertTrue(onTop);
	}
	
	@Test
	public void test3() {
		
		String express = "\\key1,key2,key3";
		CubeLayoutParser parser = CubeLayoutParser.create(express);
		String[] top = parser.top();
		String[] left = parser.left();
		String[] measure = parser.measure();
		boolean onTop = parser.measureOnTop();
		
		assertEquals(3, top.length);
		assertEquals(0, left.length);
		assertEquals("key1", top[0]);
		assertEquals("key2", top[1]);
		assertEquals("key3", top[2]);
		assertEquals(0, measure.length);
		assertTrue(onTop);
	}
	
	@Test
	public void test4() {
		
		String express = "key1 \\ key2,key3";
		CubeLayoutParser parser = CubeLayoutParser.create(express);
		String[] top = parser.top();
		String[] left = parser.left();
		String[] measure = parser.measure();
		boolean onTop = parser.measureOnTop();
		
		assertEquals(2, top.length);
		assertEquals(1, left.length);
		assertEquals("key1", left[0]);
		assertEquals("key2", top[0]);
		assertEquals("key3", top[1]);
		assertEquals(0, measure.length);
		assertTrue(onTop);
	}
	
	@Test
	public void test5() {
		
		String express = "[m1,m2,m3]";
		CubeLayoutParser parser = CubeLayoutParser.create(express);
		String[] top = parser.top();
		String[] left = parser.left();
		String[] measure = parser.measure();
		boolean onTop = parser.measureOnTop();
		
		assertEquals(0, top.length);
		assertEquals(0, left.length);
		assertEquals(3, measure.length);
		assertEquals("m1", measure[0]);
		assertEquals("m2", measure[1]);
		assertEquals("m3", measure[2]);
		assertFalse(onTop);
	}
	
	@Test
	public void test6() {
		
		String express = "l1,l2,[m1,m2,m3]";
		CubeLayoutParser parser = CubeLayoutParser.create(express);
		String[] top = parser.top();
		String[] left = parser.left();
		String[] measure = parser.measure();
		boolean onTop = parser.measureOnTop();
		
		assertEquals(0, top.length);
		assertEquals(3, left.length);
		assertEquals(3, measure.length);
		assertEquals("l1", left[0]);
		assertEquals("l2", left[1]);
		assertEquals("",   left[2]);
		assertEquals("m1", measure[0]);
		assertEquals("m2", measure[1]);
		assertEquals("m3", measure[2]);
		assertFalse(onTop);
	}
	
	@Test
	public void test7() {
		
		String express = "l1,l2 \\ [m1,m2,m3],t1";
		CubeLayoutParser parser = CubeLayoutParser.create(express);
		String[] top = parser.top();
		String[] left = parser.left();
		String[] measure = parser.measure();
		boolean onTop = parser.measureOnTop();
		
		assertEquals(2, top.length);
		assertEquals(2, left.length);
		assertEquals(3, measure.length);
		assertEquals("", top[0]);
		assertEquals("t1", top[1]);
		assertEquals("l1", left[0]);
		assertEquals("l2", left[1]);
		assertEquals("m1", measure[0]);
		assertEquals("m2", measure[1]);
		assertEquals("m3", measure[2]);
		assertTrue(onTop);
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(CubeLayoutParserTest.class.getName());
	}
}
