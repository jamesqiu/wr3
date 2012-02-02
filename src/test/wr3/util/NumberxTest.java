package test.wr3.util;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.util.Numberx;
import wr3.util.Stringx;
import static wr3.util.Numberx.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NumberxTest {

	@Test
	public void digits() {

		int n; 
		int[] digits;
		
		n = 2008;
		digits = Numberx.digits(n);
		assertEquals(4, digits.length);
		assertEquals(2, digits[0]);
		assertEquals(0, digits[1]);
		assertEquals(0, digits[2]);
		assertEquals(8, digits[3]);
		
		n = -543;
		digits = Numberx.digits(n);
		assertEquals(3, digits.length);
		assertEquals(5, digits[0]);
		assertEquals(4, digits[1]);
		assertEquals(3, digits[2]);
		
		n = 0;
		digits = Numberx.digits(n);
		assertEquals(1, digits.length);
		assertEquals(0, digits[0]);
	}
	
	@Test
	public void digitSet() {
		
		int n;
		Set<Integer> set;
		
		n = 2008;
		set = Numberx.digitSet(n);
		assertEquals(3, set.size());
		assertEquals(0, set.toArray()[0]);
		assertEquals(2, set.toArray()[1]);
		assertEquals(8, set.toArray()[2]);
		
		n = -3;
		set = Numberx.digitSet(n);
		assertEquals(1, set.size());
		assertTrue(set.contains(3));		
	}
	
	@Test
	public void random() {
		
		int n = 30;
		int[] rt = Numberx.randoms(n);
		assertEquals(n, rt.length);
		for (int i = 0; i < n; i++) {
			assertTrue(rt[i] >= 0);
			assertTrue(rt[i] <= 100);
		}
		
		rt = Numberx.randoms(n, 1);
		assertEquals(n, rt.length);
		for (int i = 0; i < n; i++) {
			assertTrue(rt[i] == 0 || rt[i] == 1);
		}
		
		assertTrue(Numberx.random() <= 100);
		assertTrue(Numberx.random(1) <= 1);
	}
	
	@Test
	public void bin() {
		assertEquals("0", binary(0));
		assertEquals("1", binary(1));
		assertEquals("10", binary(2));
		assertEquals("11", binary(3));
		assertEquals("100", binary(4));
		assertEquals("101", binary(5));
		assertEquals("10000", binary(1<<4));
		assertEquals("100000000", binary(1<<8));
		assertEquals("-1", binary(-1));
		assertEquals("-10000", binary(-16));
		// 31bit:   1111111111111111111111111111111,  2147483647 (‘º21“⁄)
		assertEquals(Stringx.fill("1", 32-1), binary(Integer.MAX_VALUE));
		// 32bit: -10000000000000000000000000000000, -2147483648
		assertEquals("-1" + Stringx.fill("0", 32-1), binary(Integer.MIN_VALUE));
	}

	@Test
	public void toInt() {
		Long L = Integer.MAX_VALUE * 100L;
		assertTrue(L == Numberx.toLong(""+L, 0));
		assertTrue(0 == Numberx.toInt(""+L, 0)); // overflow
	}
	
	@Test
	public void testRange() {
		
		List<Integer> r;
		r = range(1, 10);
		assertEquals(10, r.size());
		assertEquals(1, r.get(0).intValue());
		assertEquals(10, r.get(9).intValue());
		
		r = range(1, 10, 2);
		assertEquals(5, r.size());
		assertEquals(1, r.get(0).intValue());
		assertEquals(3, r.get(1).intValue());
		assertEquals(5, r.get(2).intValue());
		assertEquals(7, r.get(3).intValue());
		assertEquals(9, r.get(4).intValue());
		
		r = range(-1, -10, -1);
		assertEquals(10, r.size());
		assertEquals(-1, r.get(0).intValue());
		assertEquals(-2, r.get(1).intValue());
		assertEquals(-10, r.get(9).intValue());
		
		// »›¥Ì–‘
		r = range(-1, -10, 1); 
		assertEquals(0, r.size());
		
		r = range(1, 10, -2);
		assertEquals(0, r.size());
			
		r = range(10, 1, 1);
		assertEquals(0, r.size());
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(NumberxTest.class.getName());
	}

}
