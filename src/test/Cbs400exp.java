package test;

import tool.Pinyin;
import wr3.Row;
import wr3.bank.IDUtil;
import wr3.db.DbServer;
import wr3.db.RowFilter;
import wr3.util.Stringx;

/**
 * ����cbs400���ݿ���е����ݣ�cbs400��250w�û�����
 * @author jamesqiu 2011-2-14
 */
public class Cbs400exp {

	/**
	 * ��mssql��cbs400�⵼��cust�ı�
	 */
	static void test1() {

		DbServer dbs = DbServer.create();
		String sql =
			"select cusno id,crtdpt org,cusnam name,custyp type,pbktyp ptype,pbknum pid \n" +
			" from  cifm02";
		RowFilter filter = new RowFilter() {
			public boolean process(Row row) {
				System.out.println(Stringx.join(row.asList(), "\t"));
				return true;
			}
		};
		dbs.process(sql, filter);
		dbs.close();
	}

	/**
	 * ��postgresql��postgre�⵽��custpn�ı�
	 */
	static void test2() {
		DbServer dbs = DbServer.create("postgre");
		String sql = "select id,pid,name from cust where type='1' limit 10";
		RowFilter filter = new RowFilter() {
			int index = 0;
			Pinyin pinyin = Pinyin.instance();
			public boolean process(Row row) {
//				System.out.println(Stringx.join(row.asList(), "\t"));
				if (0==index) {
					index++; // ������head
				} else {
					String sid = IDUtil.to18(row.get(1).toString());
					String py  = pinyin.jp(row.get(2).toString());
					if (py.length()>3) py = py.substring(0,3);
					if (sid!=null) {
						String area = IDUtil.infos(sid)[0];
//						if (area.equals(""))
							System.out.printf("%s\t%s %s %s\n", row, sid, py, area);
					}
				}
				return true;
			}
		};
		dbs.process(sql, filter);
		dbs.close();
	}

	// ----------------- main() -----------------//
	public static void main(String[] args) {

		test2();
	}
}
