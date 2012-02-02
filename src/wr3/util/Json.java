package wr3.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <pre>
 * </pre>
 * @author jamesqiu 2009-1-18
 *
 */
public class Json {

	/**
	 * 得到简单对象(null, Number, String)的json表示.
	 * 其他对象取其toString()
	 * @param o
	 * @return
	 */
	public static String toJson(Object o) {
		if (o==null) {
			return null;
		} else if (Stringx.isString(o)) {
			return JSONObject.quote((String)o);
		} else if (Numberx.isNumber(o)) {
			return number2json((Number)o);
		} else {
			return JSONObject.quote(o.toString());
		}
	}

	/**
	 * 从json化文本得到json简单对象数组
	 * @param jsontext 如: "[\"hello\\\"james\\\"\\n", 1, null, 3.1415]"
	 * @return json对象: ["hello\"james\"\n", 1, null, 3.1415]
	 */
	public static JSONArray create(String jsontext) {

		if (Stringx.nullity(jsontext)) return new JSONArray();

		try {
			return new JSONArray(jsontext);
		} catch (JSONException e) {
			e.printStackTrace();
			return new JSONArray();
		}
	}

	public static String number2json(Number o) {
		try {
			return JSONObject.numberToString(o);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

}
