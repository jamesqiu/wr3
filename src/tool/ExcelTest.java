package tool;

import wr3.util.Excel;
import wr3.util.Numberx;
import static wr3.util.Excel.BLACK;
import static wr3.util.Excel.WHITE;
import static wr3.util.Excel.position;
import static wr3.util.Excel.sleep;

/**
 * 使用jacob进行Excel的读写
 * @author jamesqiu 2009-4-29
 */
public class ExcelTest {

	/**
	 * 操作excel文件某sheet, 设置单元格值,颜色, 读取单元格内容
	 * @param args
	 */
	public static void test1(String[] args) {
		
		if (args.length == 0) {
			System.out.println("读写Excel文件内容\n" + "\tusage: Excel a.xls");
			return;
		}
		
		String filename = args[0];
		Excel o = new Excel();
		o.open(filename, false);
		for (int i = 0; i < o.sheets(); i++) {
			int[] range = o.range(i);
			System.out.printf("sheet%d: %d, %d\n", (i+1), range[0], range[1]);			
		}
		o.sheet(1); // 使用第2个sheet
		o.value("A1", "Value", ""+Numberx.random());
		o.value("A2", "Formula", "=A1*10");
		o.value(position(5, 3), "Value", "this is c9");
		o.color(position(5, 3), WHITE, BLACK);
		sleep(1000);
		System.out.println(o.value("A2"));
		o.close();
		
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		
		test1(args);
	}
}
