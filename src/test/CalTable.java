package test;

import java.util.Calendar;

import wr3.Cell;
import wr3.Row;
import wr3.Table;
import wr3.util.Datetime;
import wr3.util.Stringx;

/**
 * <pre> 
 * ��ӡ������������һΪ��һ��.
 * usage: 
 *   DateTable.create(2009,3).table();
 *   DateTable.create(2009,3).json();
 *   DateTable.create(2009,3).text();
 *   DateTable.create(2009,3).html();
 * </pre>
 * @author jamesqiu 2010-2-19
 */
public class CalTable {

	// ��
	private int year;
	// ��
	private int month;
	// ��1��ʼ������7��ʼ
	private boolean is7123456 = false;
	// ��ͷ������һ��ͷ
	private String[] head1 = { "һ", "��", "��", "��", "��", "��", "��" };
	// ��ͷ�������տ�ͷ
	private String[] head7 = { "��", "һ", "��", "��", "��", "��", "��" };
	// �����ݣ�5�ܣ�ÿ��7��
	private Table table = new Table();
	
	/**
	 * �����½��г�ʼ��
	 * @param year
	 * @param month
	 * @param is7123456 false: ����1��ʼ��true: ����7��ʼ 
	 */
	private CalTable(int year, int month, boolean is7123456) {
	
		this.year = year;
		this.month = month;
		this.is7123456 = is7123456;
		initTable();
		fillTable();
	}
	
	/**
	 * ��������һ��ʼ�ı�
	 * @param year
	 * @param month
	 * @return
	 */
	public static CalTable create1234567(int year, int month) {
		return new CalTable(year, month, false);
	}
	
	/**
	 * ���������տ�ʼ�ı�
	 * @param year
	 * @param month
	 * @return
	 */
	public static CalTable create7123456(int year, int month) {
		return new CalTable(year, month, true);
	}
	
	private void initTable() {
		
		// ��ʼ��
		for (int i = 0; i < 7; i++) {
			head1[i] = "����" + head1[i];
			head7[i] = "����" + head7[i];
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
		
		// �õ����µ�����
		int last = Datetime.lastDay(month);
		int wi = 0;
		// foreach���µ�һ�쵽���һ��
		for (int i = 1; i <= last; i++) {
			Calendar cal = Datetime.calendar(year, month, i);
			int w = Datetime.weekDay(cal); // �õ����ڼ�
			String s = Datetime.format(cal, "d"); // ��ʽ�������ʽ;
			Cell cell = Cell.create(s);
			if (is7123456) {
				// ����7��ʼ
				table.cell(wi, w%7, cell);
				if (w == 6) wi++;
			} else {
				// ����1��ʼ
				table.cell(wi, w-1, cell);
				if (w == 7) wi++;
			}
		}
	}
	
	/**
	 * �õ�������ĸ�ʽ���ı����
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
	 * �õ��������Table�ı����
	 * @return
	 */
	public Table table() {

		return table;
	}

	/**
	 * �õ��������html���
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
