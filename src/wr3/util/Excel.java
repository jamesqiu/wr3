package wr3.util;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

/**
 * 使用jacob进行Excel的读写
 * @author jamesqiu 2009-4-29
 */
public class Excel {

	ActiveXComponent xls = new ActiveXComponent("Excel.Application");
	Dispatch workbooks;
	Dispatch workbook;
	Dispatch sheets;
	Dispatch[] sheetsArray;
	Dispatch activeSheet;	// ActiveSheet
	
	Dispatch usedRange;
	boolean readonly = false; // 是否以只读方式打开
	
	/**
	 * 打开Excel文件
	 * @param filename
	 * @param visible
	 */
	public void open(String filename, boolean visible) {
		
		filename = Filex.fullpath(filename);
		xls.setProperty("Visible", new Variant(visible));
		workbooks = xls.getProperty("Workbooks").toDispatch();
		
		// workbook = workbooks.Open(filename, false, readonly)
		workbook = call(workbooks, "Open", 
				filename, new Variant(false), new Variant(readonly));
		// sheets = workbook.Sheets
		sheets = get(workbook, "Sheets");
		// sheets.Count
		int count = Dispatch.get(sheets, "Count").getInt(); 
		sheetsArray = new Dispatch[count];
		for (int i = 0; i < count; i++) {
			// sheets.Item(i+1)
			Dispatch sheet = get(sheets, "Item", new Integer(i+1));
			sheetsArray[i] = sheet;
			// sheet.UsedRange.Rows.Count
//			usedRange = get(sheet, "UsedRange");
//			Dispatch rows = get(usedRange, "Rows");
//			Dispatch cols = get(usedRange, "Columns");
//			int rowsCount = Dispatch.get(rows, "Count").getInt();
//			int colsCount = Dispatch.get(cols, "Count").getInt();
//			System.out.printf("sheet%d: %d, %d\n", (i+1), rowsCount, colsCount);
		}
		// activeSheet = workbook.ActiveSheet
		activeSheet = get(workbook, "ActiveSheet");
	}
	
	/**
	 * 返回sheets的数量
	 * @return
	 */
	public int sheets() {
		return sheetsArray.length;
	}
	
	/**
	 * 得到第i个sheet的 UsedRange.
	 * @param i
	 * @return int[2]{Rows.Count, Columns.Count}
	 */
	public int[] range(int i) {
		
		int[] rt = new int[2];
		if (validSheetIndex(i)) {
			// sheet.UsedRange.Rows.Count
			// sheet.UsedRange.Columns.Count
			Dispatch sheet = sheetsArray[i];
			usedRange = get(sheet, "UsedRange");
			Dispatch rows = get(usedRange, "Rows");
			Dispatch cols = get(usedRange, "Columns");
			rt[0] = Dispatch.get(rows, "Count").getInt();
			rt[1] = Dispatch.get(cols, "Count").getInt();			
			return rt;
		} else {
			return new int[] {0, 0};
		}
	}
	
	/**
	 * 激活使用第i个sheet. 之后的所有操作都是针对该sheet。
	 * @param i 如果i越界, 使用当前的sheet
	 * @return d当前sheet的名字。
	 */
	public String sheet(int i) {
		if (validSheetIndex(i)) {
			activeSheet = sheetsArray[i];
		} else {
			activeSheet = get(workbook, "ActiveSheet");
		}
		return Dispatch.get(activeSheet, "Name").toString();
	}
	
	private boolean validSheetIndex(int i) {
		return i>=0 && i<sheets();
	}
	
	/**
	 * 保存、关闭Excel文件
	 * @param f
	 */
	public void close() {
		boolean f = false;
		// workbook.Save()
		call(workbook, "Save");
		// wookbook.Close(f)
		Dispatch.call(workbook, "Close", new Variant(f));
		xls.invoke("Quit", new Variant[] {});
	}
	
	public final static String NONE = "0";		// 无色
	public final static String BLACK = "1";
	public final static String WHITE = "2";
	public final static String RED = "3";
	public final static String GREEN = "4";
	public final static String BLUE = "5";
	public final static String YELLOW = "6";
	public final static String MAGENTA = "7";	// 粉红
	public final static String CYAN = "8"; 		// 浅蓝
	
	/**
	 * 向单元格写入值
	 * @param position 位置,如: "A1", "AZ99"
	 * @param type 如: "Value", "Formula", "ColorIndex", "Interior" 
	 * @param value 如: "2", "=A1*2"
	 */
	public void value(String position, String type, String value) {
		// activeSheet.Range(position).type = value
		Dispatch cell = get(activeSheet, "Range", position);
		set(cell, type, value);
	}
	
	/**
	 * 向单元格写入String值
	 * @param position
	 * @param value
	 */
	public void value(String position, String value) {
		value(position, "Value", value);
	}
	
	/**
	 * 向单元格写入公式
	 * @param position
	 * @param 公式
	 */
	public void formula(String position, String value) {
		value(position, "Formula", value);
	}
	
	/**
	 * 设置单元格的前景和背景色.
	 * @param position 位置,如: "A1", "AZ99"
	 * @param fontColor like: Excel.BLUE
	 * @param bgColor like: Excel.NONE, Excel.WHITE
	 */
	public void color(String position, String fontColor, String bgColor) {
		// activeSheet.Range(position).Font.ColorIndex = fontColor
		// activeSheet.Range(position)..Interior.ColorIndex = bgColor
		Dispatch cell = get(activeSheet, "Range", position);
		// 设置字体颜色 cell.Font.ColorIndex = "3"
		Dispatch font = get(cell, "Font");
		set(font, "ColorIndex", fontColor);
		// 设置背景颜色 cell.Interior.ColorIndex = "4"
		Dispatch interior = get(cell, "Interior");
		set(interior, "ColorIndex", bgColor);		
	}
	
	/**
	 * 读取单元格的值
	 * @param position
	 * @return
	 */
	public String value(String position) {
		// activeSheet.Range(position).Value
		Dispatch cell = get(activeSheet, "Range", position);
		String value = getValue(cell);
		return value;
	}
	
	/**
	 * 执行ActiveX方法(无参数)
	 * @param o 执行方法的ActiveX对象
	 * @param name 方法名
	 */
	private void call(Dispatch o, String name) {
		Dispatch.call(o, name);
	}
	
	/**
	 * 执行ActiveX方法(带参数)
	 * @param o 执行方法的ActiveX对象
	 * @param name 方法名
	 * @param args 参数
	 * @return
	 */
	private Dispatch call(Dispatch o, String name, Object ... args) {
		return invoke(o, name, Dispatch.Method, args);
	}
	
	/**
	 * 得到属性本身(带参数)
	 * @param 
	 */
	private Dispatch get(Dispatch o, String name, Object ... args) {
		return invoke(o, name, Dispatch.Get, args); 
	}
	
	/**
	 * 得到属性(无参数)
	 * @param o ActiveX对象
	 * @param name 属性名称
	 * @return
	 */
	private Dispatch get(Dispatch o, String name) {
		return Dispatch.get(o, name).toDispatch();
	}
	
	/**
	 * 得到属性的值
	 * @param o ActiveX对象
	 * @return
	 */
	private String getValue(Dispatch o) {
		return Dispatch.get(o, "Value").toString();
	}
	
	/**
	 * 设置属性值
	 * @param o ActiveX对象
	 * @param name 属性名称
	 * @param value 属性值
	 */
	private void set(Dispatch o, String name, Object value) {
		Dispatch.put(o, name, value);
	}
	
	private Dispatch invoke(Dispatch o, String name, int wFlags, Object ... args) {
		Object[] argsArray = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			argsArray[i] = args[i];
		}
		return Dispatch.invoke(
				o, 
				name, 
				wFlags, 
				argsArray, 
				new int[1])
			.toDispatch();
	}
		
	/**
	 * 延时
	 * @param ms 毫秒, 1000代表1秒
	 */
	public static void sleep(int ms) {
		System.out.println("sleep "+ (ms/1000) +" second.");
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * 从Cells(row, col) --> Range(position),
	 * 如: Cells(5, 3) --> Range("C5")
	 * @param row 从1开始的行号, row <= 65536 (2^16)
	 * @param col 从1开始的列号, col <= 26
	 * @return
	 */
	public static String position(int row, int col) {
		return "" + (char)('A'+(col-1)) + row;
	}

}
