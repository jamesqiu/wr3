package test.wr3.util;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.util.Stringx;
import static wr3.util.Stringx.sub;
import static wr3.util.Stringx.*;

public class StringxTest {

	@SuppressWarnings("unchecked")
	@Test
	public void testJoinListOfQString() {

		String s = Stringx.join(Arrays.asList("[", 10, 5.0, 3.1415, 1.5f, null, "]"), ",");
		assertEquals("[,10,5,3.1415,1.5,,]", s);
	}

	@Test
	public void subTest() {
		assertEquals("123", sub("123", 0, 2));
		assertEquals(null, sub(null, 0, 2));
		assertEquals("", sub("", 0, 2));
		assertEquals("1", sub("123", 0, 3));
		assertEquals("123", sub("123", 0, -1));
		assertEquals("12", sub("123", 0, -2));
		assertEquals("1", sub("123", 0, -3));
		assertEquals("123", sub("123", -1, -3));
	}

	@Test
	public void equalsTest() {
		assertTrue(same("a", "a"));
		assertTrue(same(null, null));
		assertFalse(same(null, "a"));
		assertFalse(same("b", null));
		assertFalse(same("b", "a"));
	}

	@Test
	public void remove1Test() {
		String[] ss = {"a", "b", null, "c", "b"};
		assertArrayEquals(remove1(ss, "a"), new String[]{"b", null, "c", "b"});
		assertArrayEquals(remove1(ss, "b"), new String[]{"a", null, "c"});
		assertArrayEquals(remove1(ss, null), new String[]{"a", "b", "c", "b"});
		assertArrayEquals(remove1(ss, "e"), new String[]{"a", "b", null, "c", "b"});
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(StringxTest.class.getName());
	}

}
