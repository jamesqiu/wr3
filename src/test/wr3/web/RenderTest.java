package test.wr3.web;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.Cell;
import wr3.Row;
import wr3.Table;
import wr3.web.Render;


public class RenderTest {

	@Test
	public void test1() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("k1", null);
		map.put("k2", 3.14159);
		map.put("k3", new Cell("cn中文"));
		map.put("k4", new Row(5));
		map.put("k5", new Table(3, 5));
		map.put("k6", "hello \n world");
		Render render = Render.json(map);

//		System.out.println(render);

		assertEquals("application/json", render.type());
		String content = render.content();
		assertTrue(content.startsWith("{"));
		assertTrue(content.endsWith("}"));
		String[] expect = {
			"k1: null",
			"k2: 3.14159",
			"k3: \"cn中文\"",
			"k4: [",
			"k5: {head: [\"c0\", \"c1\", \"c2\"]",
			"k6: \"hello \\n world\""
		};
		for (int i = 0; i < expect.length; i++) {
			assertTrue(content.indexOf(expect[i])!=-1);
		}
	}

	@Test
	public void testCss() {
		String content = wr3.html.Util.css("main.css");
		Render r = Render.css(content);
		System.out.println(r.toString());
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {

		JUnitCore.main(RenderTest.class.getName());
	}
}
