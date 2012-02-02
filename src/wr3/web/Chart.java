package wr3.web;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import wr3.util.Numberx;
import wr3.util.Stringx;

/**
 * <pre>
 * 生成服务器端chart servlet的url参数串.
 * usage:
 *   String url = Chart.create().line().data(list).url();
 * </pre>
 * @author jamesqiu 2009-12-9
 */
public class Chart {

	/**
	 * chart缺省参数，通过line(),bar(),pie()设置
	 */
	Map<String, String> params;
	/**
	 * chart自定义参数，通过set()设置
	 * @return
	 */
	Map<String, String> params2 = new LinkedHashMap<String, String>();

	private Chart() {}

	/**
	 * 缺省为线图，采用一组随机样例数据（可有data()覆盖）
	 * @return
	 */
	public static Chart create() {
		return new Chart().line();
	}

	/**
	 * 画线图，采用一组随机样例数据（可有data()覆盖）
	 * @return
	 */
	public Chart line() {
		params = line_params();
		return this;
	}

	/**
	 * 画柱图，采用一组随机样例数据（可有data()覆盖）
	 * @return
	 */
	public Chart bar() {
		params = bar_params();
		return this;
	}

	/**
	 * 画饼图，采用一组随机样例数据（可有data()覆盖）
	 * @return
	 */
	public Chart pie() {
		params = pie_params();
		return this;
	}

	/**
	 * 画线、柱叠加图，采用一组随机样例数据（可有data()覆盖）
	 * @return
	 */
	public Chart barline() {
		params = barline_params();
		return this;
	}

	/**
	 * <pre>
	 * 指定数据，可以是1维或者2维List, 没有数据的点用null代替,
	 * 每个list代表一条图形。
	 * 调用，Char.create().data(list1, list2, list3)
	 * </pre>
	 * @param datas
	 * @return
	 */
	public Chart data(List<?> ... datas) {

		int n = datas.length; // 有n组值

		if (n==0) return this;
		// barline只有1组值，不设置
		if (n==1 && is_barline()) return this;

		// line, bar, pie图，1组值的情况
		if (n==1) {
			String pname = "sampleValues_0";
			String pvalue = Stringx.join(datas[0], ",");
			set(pname, pvalue);
			return this;
		}

		// 以下为多组数据的情况：

		// barline 叠加图，取前2组值
		if (is_barline()) {
			String pname1 = "sampleValues";
			String pname2 = "overlay_sampleValues";
			String pvalue1 = Stringx.join(datas[0], ",");
			String pvalue2 = Stringx.join(datas[1], ",");
			set(pname1, pvalue1);
			set(pname2, pvalue2);
			return this;
		}

		// pie 多组值, 转换datas
		if (is_pie()) {
			datas = pie_datas(datas);
			n = datas.length;
		}

		// line, bar, pie(转换后) 多组值
		set("seriesCount", ""+n);
		for (int i = 0; i < n; i++) {
			String pname = "sampleValues_" + i;
			String pvalue = Stringx.join(datas[i], ",");
			set(pname, pvalue);
		}

		return this;
	}

	/**
	 * 设置图形参数值
	 * @param pname 参数名
	 * @param pvalue 参数值
	 * @return
	 */
	public Chart set(String pname, String pvalue) {

		params2.put(pname, pvalue);
		return this;
	}

	/**
	 * 得到图形servlet的url参数串（不含前缀）
	 * @return
	 */
	public String url() {

		params.putAll(params2);
		int size = params.size();
		String[] all = new String[size];

		int i = 0;
		for (Entry<String,String> e : params.entrySet()) {
			all[i] = e.getKey() + "=" + e.getValue();
			i++;
		}
		return Stringx.join(all, "&");
	}

	/**
	 * @param prefix url的前缀，如："/wr3/chart?", "http://localhost/chart?"
	 * @return
	 */
	public String url(String prefix) {

		return prefix + url();
	}

	@Override public String toString() {

		return url();
	}

	private boolean is_pie() {
		// chart=='pie'
		boolean b1 = "pie".equals(params.get("chart"));
		return b1;
	}

	/**
	 * 判断是否barline叠加图
	 * @return
	 */
	private boolean is_barline() {
		// chart=='bar' && overlay=='line'
		boolean b1 = "bar".equals(params.get("chart"));
		boolean b2 = "line".equals(params.get("overlay"));
		return b1 && b2;
	}

	/**
	 * <pre>
	 * 转换为pie的多组值表示. 如：表示2个pie图
	 * [{10,60,50,40}, {30,80,20,60}] -->
	 * [{10,30},{60,80},{50,20},{40,60}]
	 * </pre>
	 * @param datas
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List[] pie_datas(List ... datas) {

		int n = datas.length; // n = 2
		int size = datas[0].size(); // size = 4
		List[] rt = new List[size];

		for (int i = 0; i < size; i++) {
			List e = new ArrayList();
			for (int j = 0; j < n; j++) {
				Object o = datas[j].get(i);
				e.add(o);
			}
			rt[i] = e;
		}

		return rt;
	}

	/**
	 * 得到10个0～100的随机数据List
	 */
	private String data0() {
		int[] nn = Numberx.randoms(10);
		return Stringx.join(nn, ",");
	}

	// 线图默认参数
	private Map<String, String> line_params() {

		Map<String, String> p = new LinkedHashMap<String, String>();

		p.put("chart", "line");
		p.put("width", "500");
		p.put("height", "250");

		p.put("seriesCount", "1");
		p.put("sampleValues_0", data0());

		p.put("sampleColors", "red,blue,green");
		p.put("sampleHighlightOn", "true");
		p.put("sampleHighlightStyle", "circle_opaque,square_filled,circle_filled");
		p.put("sampleHighlightSize", "4,5,6");

		p.put("lineWidth", "2,2,2");
		p.put("valueLinesOn", "true");
		p.put("valueLabelsOn", "true");
		p.put("valueLabelStyle", "outside");
		p.put("defaultGridLinesOn", "true");

		p.put("background", "white");
		p.put("antialias", "true");

		return p;
	}

	// 柱图默认参数
	private Map<String, String> bar_params() {

		Map<String, String> p = new LinkedHashMap<String, String>();

		p.put("chart", "bar");
		p.put("width", "500");
		p.put("height", "250");

		p.put("seriesCount", "1");
		p.put("sampleValues_0", data0());

		p.put("sampleColors", "red,blue,green");

		p.put("multiColorOn", "true");
		p.put("valueLinesOn", "true");
		p.put("valueLabelsOn", "true");
		p.put("valueLabelStyle", "outside");
		p.put("defaultGridLinesOn", "true");

		p.put("background", "white");
		p.put("antialias", "true");

		return p;
	}

	// pie图默认参数
	private Map<String, String> pie_params() {

		Map<String, String> p = new LinkedHashMap<String, String>();

		p.put("chart", "pie");
		p.put("3dModeOn", "true");
		p.put("width", "500");
		p.put("height", "250");

		p.put("seriesCount", "1");
		p.put("sampleValues_0", data0());

		p.put("sampleColors", "blue,cyan,orange,pink,magenta,green,yellow,lightGray,red,gray");
		p.put("sliceSeperatorColor", "white");
		p.put("pieLabelsOn", "true");
		p.put("percentLabelsOn", "true");
		p.put("percentLabelStyle", "inside");
		p.put("valueLabelsOn", "true");
		p.put("valueLabelStyle", "pointing");

		p.put("depth", "0.2");
		p.put("angle", "30");

		p.put("background", "white");
		p.put("antialias", "true");

		return p;
	}

	// barline 叠加图默认参数
	private Map<String, String> barline_params() {

		Map<String, String> p = new LinkedHashMap<String, String>();

		p.put("chart", "bar");
		p.put("overlay", "line");
		p.put("width", "500");
		p.put("height", "250");

		p.put("sampleValues", data0());
		p.put("overlay_sampleValues", data0());

		p.put("rangePosition", "left");
		p.put("rangeColor", "blue");
		p.put("valueLinesOn", "true");
		p.put("defaultGridLinesOn", "true");
		p.put("valueLabelsOn", "true");
		p.put("valueLabelStyle", "inside");

		p.put("overlay_seriesRange_0", "2");
		p.put("overlay_sampleColors", "red");
		p.put("rangeOn_2", "true");
		p.put("rangeColor_2", "red");
		p.put("overlay_sampleHighlightOn", "true");
		p.put("overlay_sampleHighlightStyle", "circle_opaque");
		p.put("overlay_sampleHighlightSize", "15");
		p.put("overlay_valueLabelsOn", "true");
		p.put("overlay_valueLabelStyle", "point");
		p.put("overlay_lineStroke", "2");

		p.put("background", "white");
		p.put("antialias", "true");

		return p;
	}
}
