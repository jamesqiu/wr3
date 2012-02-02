package wr3.table;

import tool.DataGen;
import wr3.GroovyConfig;
import wr3.Table;
import wr3.db.DbServer;
import wr3.util.Filex;
import wr3.util.Stringx;

/**
 * <pre>
 * 从Table的.groovy配置文件中读数据和报表类型等信息。
 * 支持多个数据源的配置。
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
	 * 从指定的配置文件初始化
	 * @param filename 配置文件全路径
	 * @return
	 */
	public static TableConfig create(String filename) {

		return new TableConfig(filename).load();
	}

	/**
	 * web专用，从当前Controller和action对应的配置文件初始化
	 * @param app Controller对象，含params变量
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
	 * 预先把所有sql的结果取出来保存在一个Map中
	 * @param cacheData
	 * @return
	 */
	public TableConfig cache(boolean cacheData) {
		// todo：预先把所有sql的结果取出来保存在一个Map中
		return this;
	}

	/**
	 * 得到数据源描述 "data.dataSource", 如果没定义则返回 "", 之后的操作取缺省数据源
	 * @return
	 */
	public String dataSource() {
		String rt = Stringx.s(config.getString("data.dataSource"), "");
		return rt;
	}

	/**
	 * 得到所有的data名称（即所有sql语句的标识）
	 * @return
	 */
	public String[] datas() {

		if (config==null) return new String[0];
		String[] datas = config.keys("data");
		return Stringx.remove1(datas, "dataSource");
	}

	/**
	 * 得到指定的sql语句
	 * @param id
	 * @return
	 */
	public String data(String id) {

		if (config==null || Stringx.nullity(id)) return null;

		return config.getString("data." + id);
	}

	/**
	 * 得到所有Table的id标识符。
	 * @return
	 */
	public String[] tables() {

		if (config==null) return new String[0];

		String[] all = config.keys();
		return Stringx.remove(all, new String[] {"data"});
	}

	/**
	 * 得到某Table的类型: Form/List/Cube/Frame/Aggre/Group/Cross
	 * @param id
	 * @return
	 */
	public String type(String id) {

		if (config==null || Stringx.nullity(id)) return null;

		return config.getString(id + ".type");
	}

	/**
	 * 得到某Table的html表示
	 * @param id
	 * @return
	 */
	public String table(String id) {

		if (id.equalsIgnoreCase("data")) return "can't be data";
		if (config.getString(id)==null) return "未定义该id";

		String type = type(id);

		String html = null;

		String sql = config.getString(id+".data");
		Table table = query(sql);

		// types = { "form", "list", "rotate", "aggregate", "group", "frame", "cross", "cube" };
		if (isData(id)) {
			// 直接得到sql查询的Table

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
			return "未知报表类型";
		}

		return html;
	}

	// 根据sql得到数据
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
