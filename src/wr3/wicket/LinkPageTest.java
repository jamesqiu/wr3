package wr3.wicket;

import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;
import org.junit.runner.JUnitCore;

/**
 * 对简单FooPage进行单元测试
 * @author jamesqiu 2009-8-20
 *
 */
public class LinkPageTest {

	@Test
	public void label() {
		WicketTester wtest = new WicketTester();
		wtest.startPage(new LinkPage());
		wtest.assertComponent("msg", MultiLineLabel.class);
		wtest.assertModelValue("msg", "111111111 \n 22222222 \n 33333333");
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		JUnitCore.main(LinkPageTest.class.getName());
	}
}
