package test.wr3.web;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.web.UriParser;

public class UriParserTest {

	String uri;
	UriParser parser;
	
	@Test
	public void testOk() {
		
		boolean ok;
		
		uri = "/regist/add/001";
		parser = new UriParser(uri);
		ok = parser.ok();
		assertTrue(ok);
		
		uri = "/regist/";
		parser = new UriParser(uri);
		ok = parser.ok();
		assertTrue(ok);
		
		uri = "/regist/add";
		parser = new UriParser(uri);
		ok = parser.ok();
		assertTrue(ok);
		
		uri = "/regist/add/001/a";
		parser = new UriParser(uri);
		ok = parser.ok();
		assertFalse(ok);
		
		uri = "/";
		parser = new UriParser(uri);
		ok = parser.ok();
		assertFalse(ok);
		
		uri = "/abc/12a/002";
		parser = new UriParser(uri);
		ok = parser.ok();
		assertFalse(ok);
	}

	@Test
	public void testController() {
		
		uri = "/Regist/add/001";
		parser = new UriParser(uri);
		assertEquals("Regist", parser.controller());
		
		uri = "/bank.credit.Test1/add/001";
		parser = new UriParser(uri);
		assertEquals("bank.credit.Test1", parser.controller());
		
	}

	@Test
	public void testAction() {
		
		uri = "/regist/add/001";
		parser = new UriParser(uri);
		assertEquals("add", parser.action());

		uri = "/regist/1_add/001";
		parser = new UriParser(uri);
		assertEquals("1_add", parser.action());
		
		uri = "/admin.$2.regist/1_add/001";
		parser = new UriParser(uri);
		assertEquals("1_add", parser.action());
	}

	@Test
	public void testHasId() {
		uri = "/regist/add/001";
		parser = new UriParser(uri);
		assertTrue(parser.hasId());
		
		uri = "/p1.p2.regist/add/001";
		parser = new UriParser(uri);
		assertTrue(parser.hasId());
	}

	@Test
	public void testId() {
		uri = "/regist/add/001/";
		parser = new UriParser(uri);
		assertEquals("001", parser.id());
		
		uri = "/p1.p2.regist/add/001/";
		parser = new UriParser(uri);
		assertEquals("001", parser.id());
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(UriParserTest.class.getName());
	}
}
