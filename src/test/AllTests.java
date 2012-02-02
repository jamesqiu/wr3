package test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.runner.JUnitCore;

import wr3.util.Classx;
import wr3.util.Stringx;

/**
 * test all classes under "src/test/wr3/" (class end with "Test").
 * 不包含src/test/以及其他非test.wr3.*下的FooTest.java
 * <pre>
 * usage:
 *   java test.AllTests
 * </pre>  
 * @author jamesqiu 2008-11-24
 *
 */
public class AllTests {

	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		
		List<String> tests = new ArrayList<String>();
//		tests.add("test.wr3.CellTest");
//		tests.add("test.wr3.ColTest");
//		tests.add("test.wr3.RowTest");
//		tests.add("test.wr3.TableTest");
//		tests.add("test.wr3.db.ColumnTypeTest");
//		tests.add("test.wr3.db.DbConfigTest");
//		tests.add("test.wr3.db.DbServerTest");
//		tests.add("test.wr3.db.Hb3Test");
//		tests.add("test.wr3.db.JpaConfigTest");
//		tests.add("test.wr3.db.JpaTest");

		String[] classes = Classx.getClasses(test.wr3.CellTest.class);
		int index = 0;
		for (int i = 0; i < classes.length; i++) {
			String classname = classes[i];
			if (classname.endsWith("Test")) {
				index++;
				tests.add(classname);
				System.out.println(Stringx.padLeft(""+index, 4, " ") + ") " + classname);
			}
		}
		// 不打印Hibernate的日志信息
		Logger logger = Logger.getLogger("org.hibernate");
		logger.setLevel(Level.WARNING);
				
		JUnitCore.main(tests.toArray(new String[tests.size()]));
	}

}
