package test;

import java.util.Calendar;

import wr3.Cell;
import wr3.Row;
import wr3.Table;
import wr3.util.Datetime;
import wr3.util.Stringx;

/**
 * <pre> 
 * 打印本月日历表，周一为第一天.
 * usage: 
 *   DateTable.create(2009,3).table();
 *   DateTable.create(2009,3).json();
 *   DateTable.create(2009,3).text();
 *   DateTable.create(2009,3).html();
 * </pre>
 * @author jamesqiu 2010-2-19
 */
public class CalTable {

	// 年
	private int year;
	// 月
	private int month;
	// 周1开始还是周7开始
	private boolean is7123456 = false;
	// 表头：以周一开头
	private String[] head1 = { "一", "二", "三", "四", "五", "六", "日" };
	// 表头：以周日开头
	private String[] head7 = { "日", "一", "二", "三", "四", "五", "六" };
	// 表数据，5周，每周7天
	private Table table = new Table();
	
	/**
	 * 用年月进行初始化
	 * @param year
	 * @param month
	 * @param is7123456 false: 星期1开始；true: 星期7开始 
	 */
	private CalTable(int year, int month, boolean is7123456) {
	
		this.year = year;
		this.month = month;
		this.is7123456 = is7123456;
		initTable();
		fillTable();
	}
	
	/**
	 * 构建从周一开始的表
	 * @param year
	 * @param month
	 * @return
	 */
	public static CalTable create1234567(int year, int month) {
		return new CalTable(year, month, false);
	}
	
	/**
	 * 构建从周日开始的表
	 * @param year
	 * @param month
	 * @return
	 */
	public static CalTable create7123456(int year, int month) {
		return new CalTable(year, month, true);
	}
	
	private void initTable() {
		
		// 初始化
		for (int i = 0; i < 7; i++) {
			head1[i] = "星期" + head1[i];
			head7[i] = "星期" + head7[i];
		}
		if (is7123456) {
			table.head(head7);
		} else {
			table.head(head1);			
		}
		for (int i = 0; i < 5; i++) {
			table.add(new Row(7, new Cell(" ")));
		}
	}
	
	private void fillTable() {
		
		// 得到该月的天数
		int last = Datetime.lastDay(month);
		int wi = 0;
		// foreach该月第一天到最后一天
		for (int i = 1; i <= last; i++) {
			Calendar cal = Datetime.calendar(year, month, i);
			int w = Datetime.weekDay(cal); // 得到星期几
			String s = Datetime.format(cal, "d"); // 格式化输出格式;
			Cell cell = Cell.create(s);
			if (is7123456) {
				// 星期7开始
				table.cell(wi, w%7, cell);
				if (w == 6) wi++;
			} else {
				// 星期1开始
				table.cell(wi, w-1, cell);
				if (w == 7) wi++;
			}
		}
	}
	
	/**
	 * 得到月历表的格式化文本输出
	 * @return
	 */
	public String text() {
		
		StringBuilder sb = new StringBuilder(""+year+"-"+month+"\n");
		
		for (int i = 0; i < 7; i++) {
			sb.append(table.head(i)).append(' ');
		}
		sb.append('\n');
		for (int i = 0; i < table.rows(); i++) {
			for (int j = 0; j < table.cols(); j++) {
				sb.append(Stringx.padLeft(""+table.cell(i, j), 6, " ")).append(' ');
			}
			sb.append('\n');
		}
		
		return sb.toString();
	}
	
	/**
	 * 得到月历表的Table文本输出
	 * @return
	 */
	public Table table() {

		return table;
	}

	/**
	 * 得到月历表的html输出
	 * @return
	 */
	public String html() {
		
		return table().toHtml(""+year+"-"+month);
	}
	
	public static void main(String[] args) {

		// test...
		CalTable dt = CalTable.create7123456 (2010, 3);
		
		String s = dt.text();
		System.out.println(s);
		
		System.err.println(dt.html());
	}
}
