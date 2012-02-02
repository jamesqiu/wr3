package test.wr3.table;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.table.PageBar;


public class PageBarTest {

	PageBar pagebar;
	
	@Before
	public void init() {
		
		pagebar = PageBar.create().total(100).max(30).page(2);
	}
	
	@Test
	public void testBeginIndex() {
		assertEquals(31, pagebar.beginIndex());
	}

	@Test
	public void testEndIndex() {
		assertEquals(60, pagebar.endIndex());
	}

	@Test
	public void testPages() {
		assertEquals(4, pagebar.pages());
	}

	@Test
	public void testHtml() {
		assertNotNull(pagebar.html());
	}
	
	@Test
	public void overflow() {
		
		pagebar.page(1);
		assertEquals(1, pagebar.beginIndex());
		assertEquals(30, pagebar.endIndex());
		assertEquals(4, pagebar.pages());
		
		// 页数小于1不处理，用原页面
		pagebar.page(0);
		assertEquals(1, pagebar.beginIndex());
		assertEquals(30, pagebar.endIndex());
		assertEquals(4, pagebar.pages());
		pagebar.page(-1);
		assertEquals(1, pagebar.beginIndex());
		assertEquals(30, pagebar.endIndex());
		assertEquals(4, pagebar.pages());
		
		// 越界得到最后一条。
		pagebar.page(5); 
		assertEquals(100, pagebar.beginIndex());
		assertEquals(100, pagebar.endIndex());
		assertEquals(4, pagebar.pages());
		
		// total
		pagebar.page(1).max(30);
		pagebar.total(0);
		assertEquals(0, pagebar.beginIndex());
		assertEquals(0, pagebar.endIndex());
		assertEquals(0, pagebar.pages());
		pagebar.total(-10);
		assertEquals(0, pagebar.beginIndex());
		assertEquals(0, pagebar.endIndex());
		assertEquals(0, pagebar.pages());
		pagebar.total(Integer.MAX_VALUE);
		assertEquals(1, pagebar.beginIndex());
		assertEquals(30, pagebar.endIndex());
		assertEquals(Integer.MAX_VALUE/30+1, pagebar.pages());

		// max
		pagebar.total(100);
		pagebar.max(0); // 用之前的（30） 
		assertEquals(1, pagebar.beginIndex());
		assertEquals(30, pagebar.endIndex());
		assertEquals(4, pagebar.pages());
		pagebar.max(-1); // 用之前的（30）
		assertEquals(1, pagebar.beginIndex());
		assertEquals(30, pagebar.endIndex());
		assertEquals(4, pagebar.pages());
		pagebar.max(100); // max = total
		assertEquals(1, pagebar.beginIndex());
		assertEquals(100, pagebar.endIndex());
		assertEquals(1, pagebar.pages());
		pagebar.max(100+1); // max > total
		assertEquals(1, pagebar.beginIndex());
		assertEquals(100, pagebar.endIndex());
		assertEquals(1, pagebar.pages());
		pagebar.max(Integer.MAX_VALUE); // max边界
		assertEquals(1, pagebar.beginIndex());
		assertEquals(100, pagebar.endIndex());
		assertEquals(1, pagebar.pages());				
		pagebar.total(0).max(0); // max = total = 0
		assertEquals(0, pagebar.beginIndex());
		assertEquals(0, pagebar.endIndex());
		assertEquals(0, pagebar.pages());				
		pagebar.total(-10).max(0); // max = total = 0
		assertEquals(0, pagebar.beginIndex());
		assertEquals(0, pagebar.endIndex());
		assertEquals(0, pagebar.pages());				
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(PageBarTest.class.getName());
	}

}
