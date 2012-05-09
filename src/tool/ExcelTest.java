package tool;

import wr3.util.Excel;
import wr3.util.Numberx;
import static wr3.util.Excel.BLACK;
import static wr3.util.Excel.WHITE;
import static wr3.util.Excel.position;
import static wr3.util.Excel.sleep;

/**
 * ʹ��jacob����Excel�Ķ�д
 * @author jamesqiu 2009-4-29
 */
public class ExcelTest {

	/**
	 * ����excel�ļ�ĳsheet, ���õ�Ԫ��ֵ,��ɫ, ��ȡ��Ԫ������
	 * @param args
	 */
	public static void test1(String[] args) {
		
		if (args.length == 0) {
			System.out.println("��дExcel�ļ�����\n" + "\tusage: Excel a.xls");
			return;
		}
		
		String filename = args[0];
		Excel o = new Excel();
		o.open(filename, false);
		for (int i = 0; i < o.sheets(); i++) {
			int[] range = o.range(i);
			System.out.printf("sheet%d: %d, %d\n", (i+1), range[0], range[1]);			
		}
		o.sheet(1); // ʹ�õ�2��sheet
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
