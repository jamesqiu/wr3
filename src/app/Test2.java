package app;


import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tool.Tax;
import wr3.BaseConfig;
import wr3.Row;
import wr3.db.DbServer;
import wr3.db.Jpa;
import wr3.html.Link;
import wr3.html.Util;
import wr3.util.Logx;
import wr3.util.Numberx;
import wr3.web.Chart;
import wr3.web.Params;
import wr3.web.Render;
import domain.Person;

public class Test2 {

	public Render hello() {
		wr3.Table table = DbServer.create().query("select top 10 * from cust");
		return Render.html(table);
	}

	public Render link() {
		String html = Link.create("\"朱<镕>基\"").url("cn中文&&&.html").id("zhurongji").html();
		return Render.html(html);
	}

	/**
	 * 调用子目录下的类。
	 * @return
	 */
	public Render sub() {
		return new app.nonecontroller.Test2().location();
	}

	public Render jpa() {
		Logx.hibernate();
		String dbname = "h2";
		Person p1 = new Person("qh", 20, new GregorianCalendar(1984, 3, 10).getTime());
		Person p2 = new Person("邱晖", 30, new GregorianCalendar(1974, 10, 15).getTime());
		Jpa jpa = Jpa.create(dbname, domain.Person.class);
		jpa.save(new Person[] {p1, p2});
		List<?> list = jpa.list();
		return Render.body(list2table(list).toHtml("persons"));
	}

	private wr3.Table list2table(List<?> list) {

		wr3.Table table = new wr3.Table().head(Row.createByTypes("name", "age", "birthday"));
		for (Object o : list) {
			Person p = (Person) o;
			table.add(Row.createByTypes(p.name(), p.age(), p.birthday()));
		}
		return table;
	}

	/**
	 * 个人所得税对照表
	 * @see Tax
	 * @return
	 */
	public Render tax() {

		Row head = Row.createByTypes("income", "tax", "rest", "税比");
		wr3.Table table = new wr3.Table().head(head);
		for (double i = 0d; i <= 30000d; i+=1000d) {
			double tax = Tax.tax(i);
			Row row = Row.createByTypes(i, tax, i-tax, Numberx.percent(tax/i));
			table.add(row);
		}
		String chart = "<img src=\"/wr3/chart?"
			+ Chart.create().line()
				.data(table.col(0).asList(), table.col(2).asList())
				.set("valueLabelsOn", "false")
				.url()
			+ "\">";
		return Render.body(chart + "<br/>" + table.toHtml(""));
	}

	public Render resource() {
//		String css = Util.css(BaseConfig.contextPath() + "/css/min/");
		String css = Util.css(BaseConfig.contextPath() + "/css/main");
		return Render.html("<html><head>"+css+"</head><body>wr3资源测试</body></html>");
	}

	public Render body() {
		return Render.body("<h1>wr3测试body()</h1>");
	}

	/**
	 * 测试 Render.render(this);
	 * @return
	 */
	public Params params;
//	public Session session;

	public Render render() {
//		System.out.println("--- debug params=" + params);

		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("k1", Row.create(5));
		map.put("k2", new wr3.Table(3, 5));

		return Render.render(this, map);
	}
}
