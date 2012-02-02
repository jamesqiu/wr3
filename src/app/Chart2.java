package app;

import java.util.Arrays;
import java.util.List;

import wr3.text.Template;
import wr3.web.Appx;
import wr3.web.Chart;
import wr3.web.Params;
import wr3.web.Render;

/**
 * 使用固定或者随机数据绘Chart2图。
 * @author jamesqiu 2009-12-10
 */
public class Chart2 {

	public Params params;

	public Render index() {

		Template t = Appx.view(this);

		t.set("url", url());
//		System.out.println("--- Chart2.index(): params=" + params);
		return Render.html(t.toString());
	}

	/**
	 * 根据id(类型)和data(是否随机数)参数得到不同的url。
	 * @return
	 */
	public String url() {

		List<Integer> d1 = Arrays.asList(2, 4, 6, 8, 10, 9, 7, 5, 3, 1);
		List<Integer> d2 = Arrays.asList(1, 3, 5, 7, 9,  10,8, 6, 4, 2);
		List<Integer> d3 = Arrays.asList(1, 2, 3, 4, 5,  6, 7, 8, 9, 10);

		Chart chart = Chart.create();
		String id = params.get("id");
		if ("line".equals(id)) {
			chart.line();
		} else if ("bar".equals(id)) {
			chart.bar();
		} else if ("pie".equals(id)) {
			chart.pie();
		} else if ("barline".equals(id)) {
			chart.barline();
		}

		if (!"random".equals(params.get("data"))) {
			chart.data(d1, d2, d3);
		}

		return chart.url("chart?");
	}
}
