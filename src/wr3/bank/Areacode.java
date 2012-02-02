package wr3.bank;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import wr3.text.LineFilter;
import wr3.text.TextFile;
import wr3.util.Classx;
import wr3.util.Stringx;

/**
 * 从地区编码表（areacode.jar中的areacode/areacode.txt）查询地区名称
 *
 * @author jamesqiu 2009-12-8
 */
public class Areacode {

	private static Map<String, String> m1 = new HashMap<String, String>();
	static {
		m1.put("11", "北京");
		m1.put("12", "天津");
		m1.put("13", "河北");
		m1.put("14", "山西");
		m1.put("15", "内蒙");
		m1.put("21", "辽宁");
		m1.put("22", "吉林");
		m1.put("23", "黑龙江");
		m1.put("31", "上海");
		m1.put("32", "江苏");
		m1.put("33", "浙江");
		m1.put("34", "安徽");
		m1.put("35", "福建");
		m1.put("36", "江西");
		m1.put("37", "山东");
		m1.put("41", "河南");
		m1.put("42", "湖北");
		m1.put("43", "湖南");
		m1.put("44", "广东");
		m1.put("45", "广西");
		m1.put("46", "海南");
		m1.put("51", "四川");
		m1.put("52", "贵州");
		m1.put("53", "云南");
		m1.put("54", "西藏");
		m1.put("61", "陕西");
		m1.put("62", "甘肃");
		m1.put("63", "青海");
		m1.put("64", "宁夏");
		m1.put("65", "新疆");
	};

	/**
	 * 得到地区名称 这个方法不适合处理批量，批量可以使用一次装载文件的Areacode
	 *
	 * @param areacode
	 *            6位地区编码，如：532525
	 * @return 地区名称，如：云南红河石屏县
	 */
	public static String name(String areacode) {

		InputStream is = Classx.inputStream("areacode/areacode.txt");
		AreacodeFilter filter = new AreacodeFilter(areacode);
		TextFile.create(filter).process(is);
		String rt = filter.result();
		if (rt != null)
			return rt;
		return areacode;
	}

	private static class AreacodeFilter implements LineFilter {

		String areacode = null;
		String result;

		public AreacodeFilter(String areacode) {
			this.areacode = areacode;
		}

		public String process(String line) {
			if (result != null)
				return null;
			if (line.startsWith(areacode + "\t"))
				result = Stringx.right(line, "\t");
			return null;
		}

		public String result() {
			return result;
		}
	}

	/**
	 * 得到所有30个一级地名
	 *
	 * @return
	 */
	public static Map<String, String> areas1() {
		return new HashMap<String, String>(m1);
	}

	/**
	 * 根据一级地名得到所有二级地名Map
	 *
	 * @param code1
	 *            一级地名代码
	 * @return 对应一级地面的所有二级地名代码
	 */
	public static Map<String, String> areas2(String code1) {

		if (Stringx.nullity(code1) || code1.length()!=2) return new HashMap<String,String>();

		InputStream is = Classx.inputStream("areacode/areacode.txt");
		class Filter2 implements LineFilter {
			String code = null;
			Map<String, String> result = new HashMap<String,String>();

			public Filter2(String code) {
				this.code = code;
			}

			public String process(String line) {
				if (line.startsWith(code))
					result.put(Stringx.left(line, "\t"), Stringx.right(line, "\t"));
				return null;
			}

			public Map<String, String> result() {
				return result;
			}
		}
		Filter2 filter = new Filter2(code1);
		TextFile.create(filter).process(is);
		return filter.result();
	}

}
