package test.wr3.util.tuple;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.util.tuple.Pair;
import wr3.util.tuple.Quadruple;
import wr3.util.tuple.Triple;
import wr3.util.tuple.Tuple;

/**
 * 测试Java Tuple，用于多个返回值
 * @author jamesqiu 2010-1-19
 */
public class TupleTest {

	@Test
	public void pair() {
		
		Pair<String,Integer> p1 = Tuple.from("qh", 20);
		Pair<String,Integer> p2 = Tuple.from("james", 30);
		Pair<String,Integer> p3 = Tuple.from("张三", 40);
		assertEquals("qh", Tuple.get1(p1));
		assertEquals(20, Tuple.get2(p1).intValue());
		assertEquals("james", Tuple.get1(p2));
		assertEquals(30, Tuple.get2(p2).intValue());
		assertEquals("张三", Tuple.get1(p3));
		assertEquals(40, Tuple.get2(p3).intValue());
	}
	
	@Test
	public void triple() {
		
		Triple<String, Long, String> p1 = Tuple.from("qh", 20L, "qh@mail.com");
		
		assertEquals("qh", Tuple.get1(p1));
		assertEquals(20L, Tuple.get2(p1).longValue());
		assertEquals("qh@mail.com", Tuple.get3(p1));		
	}
	
	@Test
	public void quadruple() {
		
		Quadruple<String, Integer, Integer, Integer> p1 = Tuple.from("qh", 10, 20, 30);
		assertEquals("qh", Tuple.get1(p1));
		assertEquals(10, Tuple.get2(p1).intValue());
		assertEquals(20, Tuple.get3(p1).intValue());
		assertEquals(30, Tuple.get4(p1).intValue());
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(TupleTest.class.getName());
	}
}
