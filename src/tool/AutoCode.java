package tool;

import java.net.URL;

import wr3.Cell;
import wr3.Row;
import wr3.Table;
import wr3.text.Template;
import wr3.util.Charsetx;
import wr3.util.Excel;
import wr3.util.Filex;

import static wr3.util.Stringx.printf;

import static wr3.util.Excel.position;

/**
 * ��Excelģ���Զ�����Wicketģ���page�����
 * @author jamesqiu 2009-5-8
 *
 */
public class AutoCode {

	String filename;
	private Excel excel = new Excel();
	Goodname goodname = Goodname.instance();
	enum HANDLER { NAME, HTML, PAGE };
	
	public AutoCode(String filename) {
		this.filename = filename;
	}
	
	public void name() {
		handle(HANDLER.NAME);
	}
	
	public void html() {
		handle(HANDLER.HTML);
	}
	
	public void page() {
		handle(HANDLER.PAGE);
	}
	
	/**
	 * ��ǰsheet�����֡�
	 */
	String sheetName;
	
	/**
	 * ɨ������sheets��������Щ�����ݵ�domain��
	 */
	private void handle(HANDLER handler) {
		
		excel.open(filename, false);
		// ��������sheet��ÿ��sheetһ��domain
		for (int i = 0; i < excel.sheets(); i++) {
			int[] range = excel.range(i);
//			System.out.printf("sheet %d: %s\n", i, position(range[0], range[1]));
			if (range[0]==1) continue; // ֻ����ͷ��û���ݵĲ�����
			sheetName = excel.sheet(i);
			switch (handler) {
			case NAME:
				name(range[0]);
				break;
			case HTML:
				html(range[0]);
				break;
			case PAGE:
				page(range[0]);
				break;
			}
		}
//		sleep(1000);
		excel.close();
	}
	
	/**
	 * �����sheet������fields��������Ӣ��ת����
	 * @param rowCount ��Ҫ�����rows����
	 */
	private void name(int rowCount) {
		
		for (int i = 2; i <= rowCount; i++) {
			String Ai = position(i, 1); // A2, A3, ..., An, �û�����
			String Bi = position(i, 2); // B2, B3, ..., Bn, �ֶ�id��
			String Ci = position(i, 3); // C2, C3, ..., Cn, �ֶ�����
			String s1 = excel.value(Ai); 
			String s2 = excel.value(Bi);
			String s3 = excel.value(Ci);
			String en = "";
			String cn = "";
			if (Charsetx.isChinese(s1)) {
				cn = s1;
				en = goodname.en(s1);
			} else {
				en = s1;
				cn = goodname.cn(s1);
			}
			if ("null".equals(s2)) {
				excel.value(Bi, en);
				excel.color(Bi, Excel.BLUE, Excel.NONE);
			}
			if ("null".equals(s3)) {
				excel.value(Ci, cn);
				excel.color(Ci, Excel.BLUE, Excel.NONE);
			}
		}
		
	}
	
	/**
	 * ��Ԥ��������������������sheets���ֶ����Ϊhtml��form
	 */
	public void html(int rowCount) {
		
		URL url = getClass().getClassLoader().getResource("tool/domainTemplate.html");
		String filename = Filex.fullpath(url.getFile());
		Template template = Template.create(filename);
		template.set("form", sheetName);
		template.set("message", "cn����");
//			.set("form", sheetName).set("message", "cn����").toString();

		Table table = new Table(2);
		for (int i = 2; i <= rowCount; i++) {
			String Bi = position(i, 2); // B2, B3, ..., Bn, �ֶ�id��
			String Ci = position(i, 3); // C2, C3, ..., Cn, �ֶ�����
			String Di = position(i, 4); // D2, D3, ..., Dn, �ֶ�����
			String Ei = position(i, 5); // E2, E3, ..., En, �ֶι���
			String id = excel.value(Bi);
			String cn = excel.value(Ci);
			String type = excel.value(Di);
			String rule = excel.value(Ei);
			String tag = tag(id, type, rule);

			Row row = new Row();
			row.add(new Cell(cn));
			row.add(new Cell(tag));
			table.add(row);
		}
		template.set("fields", table);		
		System.out.println(template.toString());
	}

	/**
	 * �õ����ֶε�html <tag>
	 * @param id
	 * @param type
	 * @param rule
	 * @return html��
	 */
	private String tag(String id, String type, String rule) {

		if ("null".equals(type)) type = "text";
		if ("null".equals(rule)) rule = "";
		String p, tag;
		if ("text".equals(type)) {
			p = "<input type=\"text\" wicket:id=\"%s\"></input>";
			tag = printf(p, id);			
		} else if ("password".equals(type)) {
			p = "<input type=\"password\" wicket:id=\"%s\"></input>";
			tag = printf(p, id);
		} else if ("checkbox".equals(type)) {
			p = "<input type=\"checkbox\" wicket:id=\"%s\">%s</input>";
			tag = printf(p, id, rule);
		} else if ("select".equals(type)) {
			p = "<select wicket:id=\"%s\"></select>";
			tag = printf(p, id);
		} else if ("date".equals(type)) {
			p = "<input type=\"text\" wicket:id=\"%s\"></input>" +
					"<span wicket:id=\"%sDatePicker\"></span>";
			tag = printf(p, id, id);
		} else {
			p = "<span wicket:id=\"%s\" id=\"%s\"></span>";
			tag = printf(p, id, id);
		}
		return tag;
	}
	
	public void page(int rowCount) {
		
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		
		
		if (args.length == 0) {
			System.out.println("��Excelģ���Զ�����WicketӦ�ô���\n" + 
					"  usage: \n" +
					"	AutoCode filename (��Ӣ��ת��)\n" +
					"	AutoCode -html filename (���html)\n" +
					"	AutoCode -page filename (���page)\n");
			return;
		}
		
		String filename;			
		
		if (!args[0].startsWith("-")) {
			filename = args[0];
			new AutoCode(filename).name();
			return;
		}
		
		if (args[0].equals("-html")) {
			filename = args[1];
			new AutoCode(filename).html();
		}
		
		if (args[0].equals("-page")) {
			filename = args[1];
			new AutoCode(filename).page();
		}
	}
}
