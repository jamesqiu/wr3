package wr3.web;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import wr3.util.Charsetx;
import wr3.util.Stringx;

/**
 * <pre>
 * 处理request的params: 转码,设置
 * usage:
 *   s = new Session(session);
 *   map = s.toMap();
 *   s.set("user", "qh");
 *   s.get("user");
 *   s.close();
 * </pre>
 * @author jamesqiu 2009-1-9
 */
public class Session {

	private HttpSession session;
	
	public Session(HttpSession session) {
		this.session = session;
	}

	/**
	 * 得到session中的对象.
	 * @param name
	 * @return
	 */
	public Object get(String name) {
		
		if(isNull(name)) return null;

		return session.getAttribute(name);
	}
	
	/**
	 * 设置session的命名对象
	 * @param name session 中对象的命名
	 * @param value null则相当于移除该name的
	 */
	public void set(String name, Object value) {

		if(isNull(name)) return;
		
		session.setAttribute(name, value);
	}
	
	private boolean isNull(String name) {
		return session==null || Stringx.nullity(name);
	}
	
	/**
	 * 令当前session失效
	 */
	public void close() {
		if (session==null) return;
		session.invalidate();
		session = null;
	}
	
	/**
	 * 把HttpSession转换为一个Map, 把是String类型的进行转码。
	 * @return
	 */
	public Map<String, Object> toMap() {
		
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		
		if (session != null) {
			Enumeration<?> enu = session.getAttributeNames();
			for (; enu.hasMoreElements();) {
				String name = (String) enu.nextElement();
				Object value = session.getAttribute(name);
				if ((value!=null) && (value instanceof String)) {
					map.put(name, Charsetx.iso2utf((String)value));
				} else {
					map.put(name, value);
				}
			}
		}
		return map;
	}
	
	public String toString() {
		return toMap().toString();
	}
}
