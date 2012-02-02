package test.wr3.text;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.Cell;
import wr3.Table;
import wr3.text.Template;


public class TemplateTest {

	String filename1 = "./src/test/wr3/text/test.ftl";
	String filename2 = "./src/test/wr3/text/test2.ftl"; // Table
	String filename3 = "./src/test/wr3/text/test3.ftl"; //  没有任何变量
	Template template;
	String s;

	@Test
	public void test() {
		
		template = Template.create(filename1);

		template.set("name", "cn中文3");
		s = template.toString();
		assertTrue(s.indexOf("cn中文1")!=-1);
		assertTrue(s.indexOf("cn中文2")!=-1);
		assertTrue(s.indexOf("cn中文3")!=-1);
		
		template.set("name", "cn中文4");
		s = template.toString();
		assertTrue(s.indexOf("cn中文1")!=-1);
		assertTrue(s.indexOf("cn中文2")!=-1);
		assertTrue(s.indexOf("cn中文4")!=-1);
		
		template.set("name", null);
		s = template.toString();
		assertTrue(s.indexOf("yy")!=-1);
		
	}
	
	@Test
	public void test2() {
		template = Template.create(filename2);
		Table table = new Table(3,5);
		template.set("cell1", new Cell("cn中文"))
				.set("row1", table.row(-1));
		template.set("table1", table);
//		System.out.println(table);
//		System.out.println(template);
	}
	
	@Test 
	public void testWithoutSet() {
		
		template = Template.create(filename3);
		assertNotNull(template.toString());
		assertEquals("test3", template.toString().trim());
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {

		JUnitCore.main(TemplateTest.class.getName());
	}
}
