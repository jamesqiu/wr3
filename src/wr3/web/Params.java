package wr3.web;

import java.util.HashMap;
import java.util.Map;

import wr3.util.Charsetx;
import wr3.util.Numberx;
import wr3.util.Stringx;

/**
 * 处理request的params: 转码,
 * @author jamesqiu 2008-12-28
 * <pre>
 * usage:
 *   ps = new Params(request.getParameterMap());
 *   ps.get(k1);
 *   ps.toMap();
 * </pre>
 */
public class Params {

	/**
	 * request的<String, String[]> map.
	 */
	private Map<String, String[]> paramsMap;

	public Params() {}

	/**
	 * @param paramsMap
	 */
	public Params(Map<String, String[]> paramsMap) {
		this.paramsMap = paramsMap;
	}

	/**
	 * 1) 转换中文编码;
	 * 2) <String, String[]>转换为<String, String>
	 * @return
	 */
	public Map<String, String> toMap() {

		Map<String, String> rt = new HashMap<String, String>();
		if (paramsMap==null) return rt;

		for (String key : paramsMap.keySet()) {
			String[] values = paramsMap.get(key);
			String val = joinValues(values);
			val = convert(val);
			rt.put(key, val);
		}
		return rt;
	}

	/**
	 * 得到经过转码的输出.
	 */
	public String toString() {
		return toMap().toString();
	}

	/**
	 * <pre>
	 * 把具有多个value的request参数变为String,例如:
	 * k1=v1a&k1=v1b --> "v1a | v1b"
	 * </pre>
	 * @param values
	 * @return
	 */
	private String joinValues (String[] values) {

		if(values==null) return "";

//		for (int i = 0; i < values.length; i++) {
//			values[i] = convert(values[i]);
//		}
		return Stringx.join(values, " | ");
	}

	/**
	 * 进行中文转码
	 * @param s
	 * @return
	 */
	private String convert(String s) {
		// jamesqiu todo: 从配置中去编码
//		System.out.println(Charsetx.convertTest(s));
		return Charsetx.iso2utf(s);
	}

	/**
	 * alias of {@link #value(String)}
	 * @param name
	 * @return
	 */
	public String get(String name) {
		return value(name);
	}

	/**
	 * put param {name : value}
	 * @param name
	 * @param value
	 */
	public void set(String name, String value) {
		paramsMap.put(name, new String[] {value});
	}

	/**
	 * get param value by name
	 * @param name
	 * @return string from String[]
	 */
	public String value(String name) {

		String[] values = paramsMap.get(name);
		return joinValues(values);
	}

	/**
	 * get param int value by name.
	 * @param name param name
	 * @return -1 if without name or is not int.
	 */
	public int intValue(String name) {

		String s = value(name);
		return Numberx.toInt(s, -1);
	}

	/**
	 * 得到存储在Controller的params变量中的当前controller名称。
	 * params.get("controller")的便利方法
	 * @return
	 */
	public String controller() {
		return get("controller");
	}

	/**
	 * 得到存储在Controller的params变量中的当前action名称。
	 * params.get("action") 的便利方法
	 * @return
	 */
	public String action() {
		return get("action");
	}

}
