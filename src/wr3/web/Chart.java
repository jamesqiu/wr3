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
 * ���ɷ�������chart servlet��url������.
 * usage:
 *   String url = Chart.create().line().data(list).url();
 * </pre>
 * @author jamesqiu 2009-12-9
 */
public class Chart {

	/**
	 * chartȱʡ������ͨ��line(),bar(),pie()����
	 */
	Map<String, String> params;
	/**
	 * chart�Զ��������ͨ��set()����
	 * @return
	 */
	Map<String, String> params2 = new LinkedHashMap<String, String>();

	private Chart() {}

	/**
	 * ȱʡΪ��ͼ������һ������������ݣ�����data()���ǣ�
	 * @return
	 */
	public static Chart create() {
		return new Chart().line();
	}

	/**
	 * ����ͼ������һ������������ݣ�����data()���ǣ�
	 * @return
	 */
	public Chart line() {
		params = line_params();
		return this;
	}

	/**
	 * ����ͼ������һ������������ݣ�����data()���ǣ�
	 * @return
	 */
	public Chart bar() {
		params = bar_params();
		return this;
	}

	/**
	 * ����ͼ������һ������������ݣ�����data()���ǣ�
	 * @return
	 */
	public Chart pie() {
		params = pie_params();
		return this;
	}

	/**
	 * ���ߡ�������ͼ������һ������������ݣ�����data()���ǣ�
	 * @return
	 */
	public Chart barline() {
		params = barline_params();
		return this;
	}

	/**
	 * <pre>
	 * ָ�����ݣ�������1ά����2άList, û�����ݵĵ���null����,
	 * ÿ��list����һ��ͼ�Ρ�
	 * ���ã�Char.create().data(list1, list2, list3)
	 * </pre>
	 * @param datas
	 * @return
	 */
	public Chart data(List<?> ... datas) {

		int n = datas.length; // ��n��ֵ

		if (n==0) return this;
		// barlineֻ��1��ֵ��������
		if (n==1 && is_barline()) return this;

		// line, bar, pieͼ��1��ֵ�����
		if (n==1) {
			String pname = "sampleValues_0";
			String pvalue = Stringx.join(datas[0], ",");
			set(pname, pvalue);
			return this;
		}

		// ����Ϊ�������ݵ������

		// barline ����ͼ��ȡǰ2��ֵ
		if (is_barline()) {
			String pname1 = "sampleValues";
			String pname2 = "overlay_sampleValues";
			String pvalue1 = Stringx.join(datas[0], ",");
			String pvalue2 = Stringx.join(datas[1], ",");
			set(pname1, pvalue1);
			set(pname2, pvalue2);
			return this;
		}

		// pie ����ֵ, ת��datas
		if (is_pie()) {
			datas = pie_datas(datas);
			n = datas.length;
		}

		// line, bar, pie(ת����) ����ֵ
		set("seriesCount", ""+n);
		for (int i = 0; i < n; i++) {
			String pname = "sampleValues_" + i;
			String pvalue = Stringx.join(datas[i], ",");
			set(pname, pvalue);
		}

		return this;
	}

	/**
	 * ����ͼ�β���ֵ
	 * @param pname ������
	 * @param pvalue ����ֵ
	 * @return
	 */
	public Chart set(String pname, String pvalue) {

		params2.put(pname, pvalue);
		return this;
	}

	/**
	 * �õ�ͼ��servlet��url������������ǰ׺��
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
	 * @param prefix url��ǰ׺���磺"/wr3/chart?", "http://localhost/chart?"
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
	 * �ж��Ƿ�barline����ͼ
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
	 * ת��Ϊpie�Ķ���ֵ��ʾ. �磺��ʾ2��pieͼ
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
	 * �õ�10��0��100���������List
	 */
	private String data0() {
		int[] nn = Numberx.randoms(10);
		return Stringx.join(nn, ",");
	}

	// ��ͼĬ�ϲ���
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

	// ��ͼĬ�ϲ���
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

	// pieͼĬ�ϲ���
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

	// barline ����ͼĬ�ϲ���
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
