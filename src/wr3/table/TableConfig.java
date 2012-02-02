package wr3.table;

import tool.DataGen;
import wr3.GroovyConfig;
import wr3.Table;
import wr3.db.DbServer;
import wr3.util.Filex;
import wr3.util.Stringx;

/**
 * <pre>
 * ��Table��.groovy�����ļ��ж����ݺͱ������͵���Ϣ��
 * ֧�ֶ������Դ�����á�
 * usage:
 *   conf = TableConfig.create("Table.groovy");
 *   conf.datas(); // ["data1", "data2"]
 *   conf.data("data1"); // "select * from loan"
 *   conf.tables();  // ["table1", "table2"]
 *   conf.type("table1"); // "form"
 *   conf.table("table1"); // "data1"
 *
 * </pre>
 * @author jamesqiu 2009-12-15
 */
public class TableConfig {

	private String filename;
	private GroovyConfig config;

	private TableConfig() {}

	private TableConfig(String filename) { this.filename = filename; }

	/**
	 * ��ָ���������ļ���ʼ��
	 * @param filename �����ļ�ȫ·��
	 * @return
	 */
	public static TableConfig create(String filename) {

		return new TableConfig(filename).load();
	}

	/**
	 * webר�ã��ӵ�ǰController��action��Ӧ�������ļ���ʼ��
	 * @param app Controller���󣬺�params����
	 * @return
	 */
	public static TableConfig create(Object app) {
		// TODO
		return new TableConfig().load();
	}

	private TableConfig load() {

		config = GroovyConfig.create(filename);
		return this;
	}

	/**
	 * Ԥ�Ȱ�����sql�Ľ��ȡ����������һ��Map��
	 * @param cacheData
	 * @return
	 */
	public TableConfig cache(boolean cacheData) {
		// todo��Ԥ�Ȱ�����sql�Ľ��ȡ����������һ��Map��
		return this;
	}

	/**
	 * �õ�����Դ���� "data.dataSource", ���û�����򷵻� "", ֮��Ĳ���ȡȱʡ����Դ
	 * @return
	 */
	public String dataSource() {
		String rt = Stringx.s(config.getString("data.dataSource"), "");
		return rt;
	}

	/**
	 * �õ����е�data���ƣ�������sql���ı�ʶ��
	 * @return
	 */
	public String[] datas() {

		if (config==null) return new String[0];
		String[] datas = config.keys("data");
		return Stringx.remove1(datas, "dataSource");
	}

	/**
	 * �õ�ָ����sql���
	 * @param id
	 * @return
	 */
	public String data(String id) {

		if (config==null || Stringx.nullity(id)) return null;

		return config.getString("data." + id);
	}

	/**
	 * �õ�����Table��id��ʶ����
	 * @return
	 */
	public String[] tables() {

		if (config==null) return new String[0];

		String[] all = config.keys();
		return Stringx.remove(all, new String[] {"data"});
	}

	/**
	 * �õ�ĳTable������: Form/List/Cube/Frame/Aggre/Group/Cross
	 * @param id
	 * @return
	 */
	public String type(String id) {

		if (config==null || Stringx.nullity(id)) return null;

		return config.getString(id + ".type");
	}

	/**
	 * �õ�ĳTable��html��ʾ
	 * @param id
	 * @return
	 */
	public String table(String id) {

		if (id.equalsIgnoreCase("data")) return "can't be data";
		if (config.getString(id)==null) return "δ�����id";

		String type = type(id);

		String html = null;

		String sql = config.getString(id+".data");
		Table table = query(sql);

		// types = { "form", "list", "rotate", "aggregate", "group", "frame", "cross", "cube" };
		if (isData(id)) {
			// ֱ�ӵõ�sql��ѯ��Table

		} else if(is(type, "form")) {

			html = form(table);

		} else if (is(type, "list")) {

			html = ListTable.create().data(table).html();

		} else if (is(type, "rotate")) {

			html = RotateTable.create().data(table).html();

		} else if (is(type, "aggregate")) {

			html = AggregateTable.create().data(table).html();

		} else if (is(type, "group")) {

			html = GroupTable.create().data(table).html();

		} else if (is(type, "frame")) {

			html = FrameTable.create().data(table).html();

		} else if (is(type, "cross")) {

			html = CrossTable.create().data(table).html();

		} else if (is(type, "cube")) {

			html = CubeTable.create().data(table).html();

		} else {
			return "δ֪��������";
		}

		return html;
	}

	// ����sql�õ�����
	private Table query(String sql) {
		DbServer dbs = DbServer.create(dataSource());
		Table rt = dbs.query(sql);
		dbs.close();
		return rt;
	}

	private String form(Table data) {
		return FormTable.create().data(data).html();
	}

	private boolean isData(String id) { return "data".equalsIgnoreCase(id); }
	private boolean is(String type, String typeString) { return type.equalsIgnoreCase(typeString); }

	// ----------------- main() -----------------//
	public static void main(String[] args) {

		DataGen.create("h2");

		String fn = Filex.resource(TableConfig.class.getClass(), "/wr3/table/TableConfig.groovy");
		TableConfig tc = TableConfig.create(fn);
		System.out.printf("%s\n%s\n%s\n",
			tc.dataSource(),
			Stringx.join(tc.datas()),
			Stringx.join(tc.tables())
		);
		System.out.println(tc.type("t2"));
		System.out.println(tc.table("t1"));
	}
}
