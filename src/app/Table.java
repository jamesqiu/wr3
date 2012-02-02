package app;

import tool.DataGen;
import wr3.GroovyConfig;
import wr3.db.DbServer;
import wr3.table.FormTable;
import wr3.table.ListTable;
import wr3.util.Filex;
import wr3.util.Stringx;
import wr3.web.Params;
import wr3.web.Render;

/**
 * 读取和action配套的.groovy配置文件如Table.t1.groovy，生成Table /wr3/Table/t1 --> Table#t1(),
 * Table.t1.groovy, Table.t1.html, 在t1()中 return Render.report(this);
 *
 * @author jamesqiu 2009-12-14
 */
public class Table {

	public Params params;

	private GroovyConfig prepareData() {

		DataGen.create("h2");

		String filename = Filex.resource(this, "Table.t1.groovy");
		GroovyConfig conf = GroovyConfig.create(filename);
		return conf;
	}

	public Render t1() {

		GroovyConfig conf = prepareData();

		String dbname = conf.getString("data.dataSource");
		String sql = conf.getString("data.data1");
		wr3.Table table = DbServer.create(dbname).query(sql);

		String type = conf.getString("table2.type");
		String html = "";
		if ("form".equalsIgnoreCase(type)) {
			FormTable form = FormTable.create().data(table).id("table1");
			html = form.html();
		} else if ("list".equalsIgnoreCase(type)) {
			ListTable list = ListTable.create().data(table).id("table2");
			html = list.html();
		}

		return Render.body(html);
	}

	public Render t2() {

		GroovyConfig conf = prepareData();
		String[] ks = conf.keys();
		String[] ks0 = conf.keys(ks[0]);
		System.out.println(conf.getString("data.data3"));
		System.out.println("data.type=" + conf.getString("data.type"));
		return Render.body(ks[0] + ".*:<br/>" + Stringx.join(ks0, "<br/>") + "<hr/>"
				+ Stringx.join(ks, "<br/>"));
	}
}
