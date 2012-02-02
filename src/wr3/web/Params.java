package wr3.web;

import java.util.HashMap;
import java.util.Map;

import wr3.util.Charsetx;
import wr3.util.Numberx;
import wr3.util.Stringx;

/**
 * ����request��params: ת��,
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
	 * request��<String, String[]> map.
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
	 * 1) ת�����ı���;
	 * 2) <String, String[]>ת��Ϊ<String, String>
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
	 * �õ�����ת������.
	 */
	public String toString() {
		return toMap().toString();
	}

	/**
	 * <pre>
	 * �Ѿ��ж��value��request������ΪString,����:
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
	 * ��������ת��
	 * @param s
	 * @return
	 */
	private String convert(String s) {
		// jamesqiu todo: ��������ȥ����
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
	 * �õ��洢��Controller��params�����еĵ�ǰcontroller���ơ�
	 * params.get("controller")�ı�������
	 * @return
	 */
	public String controller() {
		return get("controller");
	}

	/**
	 * �õ��洢��Controller��params�����еĵ�ǰaction���ơ�
	 * params.get("action") �ı�������
	 * @return
	 */
	public String action() {
		return get("action");
	}

}
