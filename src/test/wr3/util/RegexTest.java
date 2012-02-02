package test.wr3.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static wr3.util.Regex.find;
import static wr3.util.Regex.findAll;
import static wr3.util.Regex.match;
import static wr3.util.Regex.pattern;
import static wr3.util.Regex.patterni;
import static wr3.util.Regex.replace;
import static wr3.util.Regex.replaceByFilter;

import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.text.LineFilter;
import wr3.util.Regex;

public class RegexTest {

	private String src;
	private boolean b;
	private String s;
	private Pattern p;
	
	@Test
	public void matchTest() {
		s = "c[nm]";
		p = pattern(s);
		b = match("cn", p);
		assertTrue(b);
		b = match("cn", s);
		assertTrue(b);
		b = match("cm", p);
		assertTrue(b);
		b = match("cm", s);
		assertTrue(b);

		s = "c[nm中文]";
		p = patterni(s);
		b = match("cN", p);
		assertTrue(b);
		b = match("C文", p);
		assertTrue(b);
	}

	@Test
	public void findTest() {
		s = "c[nm]";
		src = "CN, cMail, cnm";

		p = pattern("c[nm]");
		assertEquals("cn", find(src, p));
		
		p = patterni("c[nm]");
		assertEquals("CN", find(src, p));
		
		assertEquals("cn", find(src, s));
	}

	@Test
	public void findAllTest() {

		p = patterni("c[nm]");
		src = "CN, cMail, cnm";
		String[] rt;
		
		rt = findAll(src, p);
		assertEquals(3, rt.length);
		replace(src, p, "");
		assertEquals("CN", rt[0]);
		assertEquals("cM", rt[1]);
		assertEquals("cn", rt[2]);
		assertTrue(true);
	}
	
	@Test
	public void replaceTest() {
		src = "CN, cMail, cnm";

		p = patterni("c[nm]");
		String rt = replace(src, p, "中文");
		assertEquals("中文, 中文ail, 中文m", rt);

		p = pattern("c[nm]");
		rt = replace(src, p, "中文");
		assertEquals("CN, cMail, 中文m", rt);
	}

	@Test
	public void replaceByFilterTest() {
		
		src = "CN, cMail, cnm";

		p = patterni("c[nm]");
		String rt = replaceByFilter(src, p, new LineFilter() {
			public String process(String s) { return "["+s+s+"]";}
		});
		assertEquals("[CNCN], [cMcM]ail, [cncn]m", rt);
	}
	
	@Test
	public void isVarName() {
		
		assertTrue(Regex.isVarName("var1"));
		assertTrue(Regex.isVarName("$"));
		assertTrue(Regex.isVarName("_1$"));
		assertTrue(Regex.isVarName("$abc1"));
		
		assertFalse(Regex.isVarName(""));
		assertFalse(Regex.isVarName(null));
		assertFalse(Regex.isVarName("a	b"));
		assertFalse(Regex.isVarName("1abc"));
	}
	
	@Test
	public void isPackageName() {
		
		assertTrue(Regex.isPackageName("a"));
		assertTrue(Regex.isPackageName("a.b.c.d.E"));
		assertTrue(Regex.isPackageName("$._.Class3"));
		
		assertFalse(Regex.isPackageName("a."));
		assertFalse(Regex.isPackageName(".a.b"));
		assertFalse(Regex.isPackageName("a..b"));
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(RegexTest.class.getName());
	}
}
