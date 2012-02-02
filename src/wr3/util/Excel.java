package wr3.util;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

/**
 * ʹ��jacob����Excel�Ķ�д
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
	boolean readonly = false; // �Ƿ���ֻ����ʽ��
	
	/**
	 * ��Excel�ļ�
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
	 * ����sheets������
	 * @return
	 */
	public int sheets() {
		return sheetsArray.length;
	}
	
	/**
	 * �õ���i��sheet�� UsedRange.
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
	 * ����ʹ�õ�i��sheet. ֮������в���������Ը�sheet��
	 * @param i ���iԽ��, ʹ�õ�ǰ��sheet
	 * @return d��ǰsheet�����֡�
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
	 * ���桢�ر�Excel�ļ�
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
	
	public final static String NONE = "0";		// ��ɫ
	public final static String BLACK = "1";
	public final static String WHITE = "2";
	public final static String RED = "3";
	public final static String GREEN = "4";
	public final static String BLUE = "5";
	public final static String YELLOW = "6";
	public final static String MAGENTA = "7";	// �ۺ�
	public final static String CYAN = "8"; 		// ǳ��
	
	/**
	 * ��Ԫ��д��ֵ
	 * @param position λ��,��: "A1", "AZ99"
	 * @param type ��: "Value", "Formula", "ColorIndex", "Interior" 
	 * @param value ��: "2", "=A1*2"
	 */
	public void value(String position, String type, String value) {
		// activeSheet.Range(position).type = value
		Dispatch cell = get(activeSheet, "Range", position);
		set(cell, type, value);
	}
	
	/**
	 * ��Ԫ��д��Stringֵ
	 * @param position
	 * @param value
	 */
	public void value(String position, String value) {
		value(position, "Value", value);
	}
	
	/**
	 * ��Ԫ��д�빫ʽ
	 * @param position
	 * @param ��ʽ
	 */
	public void formula(String position, String value) {
		value(position, "Formula", value);
	}
	
	/**
	 * ���õ�Ԫ���ǰ���ͱ���ɫ.
	 * @param position λ��,��: "A1", "AZ99"
	 * @param fontColor like: Excel.BLUE
	 * @param bgColor like: Excel.NONE, Excel.WHITE
	 */
	public void color(String position, String fontColor, String bgColor) {
		// activeSheet.Range(position).Font.ColorIndex = fontColor
		// activeSheet.Range(position)..Interior.ColorIndex = bgColor
		Dispatch cell = get(activeSheet, "Range", position);
		// ����������ɫ cell.Font.ColorIndex = "3"
		Dispatch font = get(cell, "Font");
		set(font, "ColorIndex", fontColor);
		// ���ñ�����ɫ cell.Interior.ColorIndex = "4"
		Dispatch interior = get(cell, "Interior");
		set(interior, "ColorIndex", bgColor);		
	}
	
	/**
	 * ��ȡ��Ԫ���ֵ
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
	 * ִ��ActiveX����(�޲���)
	 * @param o ִ�з�����ActiveX����
	 * @param name ������
	 */
	private void call(Dispatch o, String name) {
		Dispatch.call(o, name);
	}
	
	/**
	 * ִ��ActiveX����(������)
	 * @param o ִ�з�����ActiveX����
	 * @param name ������
	 * @param args ����
	 * @return
	 */
	private Dispatch call(Dispatch o, String name, Object ... args) {
		return invoke(o, name, Dispatch.Method, args);
	}
	
	/**
	 * �õ����Ա���(������)
	 * @param 
	 */
	private Dispatch get(Dispatch o, String name, Object ... args) {
		return invoke(o, name, Dispatch.Get, args); 
	}
	
	/**
	 * �õ�����(�޲���)
	 * @param o ActiveX����
	 * @param name ��������
	 * @return
	 */
	private Dispatch get(Dispatch o, String name) {
		return Dispatch.get(o, name).toDispatch();
	}
	
	/**
	 * �õ����Ե�ֵ
	 * @param o ActiveX����
	 * @return
	 */
	private String getValue(Dispatch o) {
		return Dispatch.get(o, "Value").toString();
	}
	
	/**
	 * ��������ֵ
	 * @param o ActiveX����
	 * @param name ��������
	 * @param value ����ֵ
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
	 * ��ʱ
	 * @param ms ����, 1000����1��
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
	 * ��Cells(row, col) --> Range(position),
	 * ��: Cells(5, 3) --> Range("C5")
	 * @param row ��1��ʼ���к�, row <= 65536 (2^16)
	 * @param col ��1��ʼ���к�, col <= 26
	 * @return
	 */
	public static String position(int row, int col) {
		return "" + (char)('A'+(col-1)) + row;
	}

}
