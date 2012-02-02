package app;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tool.DataGen;
import wr3.Cell;
//import wr3.Table;
import wr3.db.DbServer;
import wr3.table.AggregateTable;
import wr3.table.CrossTable;
import wr3.table.CubeTable;
import wr3.table.FormTable;
import wr3.table.FrameTable;
import wr3.table.GroupFilter;
import wr3.table.GroupTable;
import wr3.table.ListTable;
import wr3.table.PageBar;
import wr3.table.RotateTable;
import wr3.table.AggregateTable.Position;
import wr3.util.Filex;
import wr3.util.Stringx;
import wr3.util.Tablex;
import wr3.web.Params;
import wr3.web.Render;
import wr3.web.Session;


/**
 * 主要测试几种常用报表
 * @author jamesqiu 2009-8-30
 *
 */
public class TestTable {

	public Params params;	// 变量名必须为"params"
	public Session session;	// 变量名必须为"session"

	// 生成内存随机数据表
	private final static String dbname = "h2";
	private DbServer dbs = DbServer.create(dbname);
	static {
		DataGen.create(dbname);
	}

	public Render hello() {

		String s = new wr3.Table(10, 10).toHtml("t1")+
			"<br/>"+params.toMap();
		return Render.html(s);
	}

	public String test() {
//		return Render.head();
		return Render.body("<h1>call <u>Render.body()</u></h1>").content();
	}

	public String resource() {
		// "app/App1.class", "wr3/package.html", "org/json/HTTP.class", "java/lang/String.class"
//		return ""+Classx.url(getClass(), "app/App1.class");
		return Filex.resource(this, "App1.class");
//		return ""+Classx.loader(String.class);
//		return ""+Classx.getPath(app.nonecontroller.Test2.class);
	}

	public Render sql() {

		String sql = params.get("sql");
		if (Stringx.nullity(sql)) sql = "select month,name,sum(amount) amount from loan" +
				"	group by month,name" +
				"	order by month,name";
		wr3.Table rt = dbs.query(sql);
		return Render.html(rt.toHtml(null));
	}

	@SuppressWarnings("serial")
	private String formTable() {

		DataGen.create(dbname);
		@SuppressWarnings("unused")
		Map<?,?> m1 = new HashMap<String, String>() {{
			put("c0", "org");
			put("c1", "name");
			put("c2", "余额");
			put("month", "月份");
		}};
		List<String> m2 = Arrays.asList("贷款","活期","定期");
		List<Object> m3 = Tablex.asList(
				DbServer.create("h2").query(
						"select nam from sys_infolder where cod like 'deposit.%'"));
		wr3.Table data = DbServer.create("h2").query("select * from deposit").subrow(6, 6);
		Map<?,?> dd1 = new HashMap<String, String>() {{
			put("003", "中国银行");
		}};
		Map<?,?> dd2 = new HashMap<String, String>() {{
			put("3000", "三千");
		}};
		FormTable table = FormTable.create()
//			.id("sd")
			.data(data)
			.meta(m2)
			.meta(m3)
			.dd(-1, dd2)
			.dd("orgid", dd1)
			;
//			.view("f:/dev3/classes/wr3/table/FormTable.ftl");
		return table.html();
	}

	private String formTable2() {

		DataGen.create(dbname);
		FormTable table = FormTable.create()
			.id("form2")
			.data(new wr3.Table(5,3))
			;
		return table.html();
	}

	private String listTable() {

		DataGen.create(dbname);
		String sql_data = "select * from loan -- where 1=2";

		// 分页条
		int total = dbs.resultRows(sql_data);
		int page = params.intValue("page");
		int max = 7;
		PageBar bar = PageBar.create()
			.total(total)
			.page(page)
			.max(max)
			;

		wr3.Table data = dbs.query(sql_data, bar.beginIndex(), max);
//		Map<Object, Object> meta = TableUtil.asMap(
//				DbServer.create("h2").query(
//						"select substring(cod,6), nam from sys_infolder where cod like 'loan.%'"));
		List<Object> meta = Tablex.asList(
				dbs.query("select nam from sys_infolder where cod like 'loan.%'"));
		Map<Object, Object> dd = Tablex.asMap(
				dbs.query("select * from dd_org"));

		ListTable table = ListTable.create()
			.data(data)
			.meta(meta)
			.dd("orgid", dd)
			;
		return bar.html() + "\n" + table.html() + "\n" + bar.html();
	}

	private String rotateTable() {

		DataGen.create(dbname);
		wr3.Table data = dbs.query("select * from loan where orgid='002' ");
		List<Object> meta = Tablex.asList(
				dbs.query("select nam from sys_infolder where cod like 'loan.%'"));
		Map<Object, Object> dd = Tablex.asMap(
				dbs.query("select * from dd_org"));

		RotateTable table = RotateTable.create()
			.data(data)
			.meta(meta)
			.dd("orgid", dd);
			;

		return table.html();
	}

	private String aggregateTable() {

		DataGen.create(dbname);
		wr3.Table data = dbs.query("select *, amount*1.00001 from loan where orgid='001'");
		List<Object> meta = Tablex.asList(
				dbs.query("select nam from sys_infolder where cod like 'loan.%'"));
		Map<Object, Object> dd = Tablex.asMap(
				dbs.query("select * from dd_org"));

		AggregateTable table = AggregateTable.create()
			.data(data)
			.meta(meta)
			.dd("orgid", dd)
			.sum(Position.FIRST_AND_LAST) // 会替换上面一个
			.avg()
			.max()
			.min(Position.FIRST)
			;
		return table.html();
	}

	private String groupTable() {

		DataGen.create(dbname);
		String sql_data = "select *, amount*1.00001 from loan where orgid='001' ";

		// 分页条
		int total = dbs.resultRows(sql_data);
		int page = params.intValue("page");
//		int max = 30;
		int max = 10;
		PageBar bar = PageBar.create()
			.total(total)
			.page(page)
			.max(max)
		;

		wr3.Table data = dbs.query(sql_data, bar.beginIndex(), max);

		List<Object> meta = Tablex.asList(
				dbs.query("select nam from sys_infolder where cod like 'loan.%'"));
		Map<Object, Object> dd = Tablex.asMap(
				dbs.query("select * from dd_org"));

		GroupTable table = GroupTable.create()
			.data(data)
			.meta(meta)
			.dd("orgid", dd)
			.group(3, new GroupFilter() {
				public Cell process(Cell cell) {
					return cell.intValue()>50000 ? new Cell("大户") : new Cell("小户");
				}
			})
			;
//		System.out.println(table.result());
//		System.out.println(table.codes());
//		return table.html();
		return bar.html() + "\n" + table.html() + "\n" + bar.html();
	}

	private String frameTable() {

		DataGen.create(dbname);
		String sql1 = "select name, sum(hq) hq, sum(dq) dq from deposit" +
				" group by name order by name";
		String sql2 = "select name, sum(amount) amount from loan" +
				" group by name order by name desc";
		wr3.Table data1 = dbs.query(sql1);
		wr3.Table data2 = dbs.query(sql2);

		FrameTable table = FrameTable.create()
			.meta(Arrays.asList("姓名", "活期存款", "定期存款", "贷款"))
			.put(data1)
			.put(data2)
		;
//		System.out.println(data1);
//		System.out.println(data2);
		return table.html();
	}

	private String crossTable() {

		DataGen.create(dbname);
		String sql = "select month,name,sum(amount) amount from loan" +
				"	group by month,name " +
				"	order by month,name";
		wr3.Table data = dbs.query(sql);

		CrossTable table = CrossTable.create()
			.top("name").left("month").measure("amount")
			.sum(true)
			.data(data)
			.meta(Arrays.asList("月份", "姓名", "贷款额"))
		;
		return table.html();
	}

	private String cubeTable() {

		DataGen.create(dbname);
//		String sql = "select name, month, sum(hq) hq, sum(dq) dq from deposit" +
//				"	group by name, month" +
//				"	order by name, month";
		String sql = "select * from deposit";
		wr3.Table data = dbs.query(sql);

		CubeTable table = CubeTable.create(params)
//			.top("name")
//			.left("month")
//			.measure("")
			.layout(" month, name \\ orgid, [hq, dq] ")
//			.layout("month")
			.data(data)
//			.debug(true)
			.meta(Arrays.asList("月份", "机构", "姓名", "活期额", "定期额"))
		;

		return table.html();
	}

	//---------- 把上述的Table String输出作为body变成完整的html

	public Render form() {

		return Render.body(formTable());
	}

	public Render list() {

		return Render.body(listTable());
	}

	public Render form_list() {

		return Render.body(
				formTable() + "\n<br/>\n" +
				listTable() + "\n<br/>\n" +
				formTable2());
	}

	public Render rotate() {

		return Render.body(rotateTable());
	}

	public Render aggre() {

		return Render.body(aggregateTable());
	}

	public Render group() {

		return Render.body(groupTable());
	}

	public Render frame() {

		return Render.body(frameTable());
	}

	public Render cross() {

		return Render.body(crossTable());
	}

	public Render cube() {

		return Render.body(cubeTable());
	}

}
