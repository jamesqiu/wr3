package wr3.web;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import wr3.BaseConfig;
import wr3.Cell;
import wr3.Row;
import wr3.Table;
import wr3.text.Template;
import wr3.util.Classx;
import wr3.util.Filex;
import wr3.util.Numberx;
import wr3.util.Stringx;

/**
 * <pre>
 * 把文本内容进行html/text/json包装供servlet输出.
 * usage:
 *   render = Render.html(content);
 *   render2 = Render.text(table);
 *   render3 = Render.json(map);
 *   render.head();
 *   render.content();
 *
 * </pre>
 *
 * @author jamesqiu 2009-1-25
 *
 *         TODO: render with template, like Render.html(template, content);
 *         Render.html(template);
 */
public class Render {

	/**
	 * contentType
	 */
	private String type = "text/plain";
	/**
	 * content
	 */
	private String content = "";

	private Render() {

	}

	/**
	 * get contentType
	 *
	 * @return
	 */
	public String type() {
		return type;
	}

	/**
	 * get content
	 */
	public String content() {
		return content;
	}

	public String toString() {
		return type + '\n' + content;
	}

	/**
	 * <pre>
	 * 直接使用和Controller对象action对应的模板文件 "./$Controller.$action.html" ，
	 * 可把request和session变量直接用于模板。
	 * 如果没有提供模板，直接打印输出变量列表。
	 * usage: Render.render(this); // 在app.Foo中
	 * </pre>
	 *
	 * @param o
	 *            Controller 对象，一般在Controller中用this
	 * @return
	 */
	public static Render render(Object o) {

		return render(o, null);
	}

	// 把map中的<key, value>设置到template中
	private static void fromMap(Template t, Map<String, ?> map) {
		for (Entry<String, ?> set : map.entrySet()) {
			t.set(set.getKey(), set.getValue());
		}
	}

	/**
	 * 使用 Params, Session 作为输入模板变量，./$Controller.$action.html 作为模板，放入变量，得到输出。
	 *
	 * @param o
	 *            Controller 对象，一般在Controller中用this
	 * @param map
	 *            模板变量-值，还有Params, Session变量也作为模板变量
	 * @return
	 */
	public static Render render(Object o, Map<String, ?> map) {

		Params params = null;
		Session session = null;

		if (Classx.hasField(o, AppController.PARAMS_NAME)) {
			params = (Params) (Classx.getField(o, AppController.PARAMS_NAME));
		}

		if (Classx.hasField(o, AppController.SESSION_NAME)) {
			session = (Session) (Classx.getField(o, AppController.SESSION_NAME));
		}

		Template t = Appx.view(o);
		if (t == null) {
			String filename = params==null ? "" : params.controller()+"."+params.action()+".html";
			return Render.body(
				Stringx .printf(
					"<font color=red>提示：请提供对应controller和action的模板文件%s，" +
					"并在controller中定义params变量。</font><br/><br/>" +
					"params:<br/> %s<br/><br/>" +
					"session:<br/> %s<br/><br/>" +
					"程序变量:<br/> %s<br/><br/>",
					filename,
					params==null ? "未定义" : params,
					session==null ? "未定义" : session,
					map==null ? "未定义" : map) );
		}

		// 把params和session变量设置到模板中
		if (params != null) fromMap(t, params.toMap());
		if (session != null) fromMap(t, session.toMap());
		// 把自定义变量（可以是String/Cell/Row/Table）设置到模板中
		if (map != null) fromMap(t, map);

		return body(t.toString());
	}

	/**
	 * 使用 Params, Session 作为输入模板及查询变量， $Controller.$action.html 作为模板，从
	 * $Controller.$action.groovy 取数据库结果，得到输出
	 *
	 * @param o
	 * @return
	 */
	public static Render report(Object o) {
		// todo
		return body("todo");
	}

	/**
	 * 使用 Params, Session 和 map 作为输入模板及查询变量， $Controller.$action.html 作为模板，从
	 * $Controller.$action.groovy 取数据库结果，得到输出
	 *
	 * @param o
	 * @param map
	 *            模板及查询变量-值
	 * @return
	 */
	public static Render report(Object o, Map<String, ?> map) {
		// todo
		return body("todo");
	}

	/**
	 * text/html
	 *
	 * @return
	 */
	public static Render html(String content) {
		return result("text/html", content);
	}

	public static Render html(Table table) {
		return html(table.toHtml(""));
	}

	/**
	 * <pre>
	 * 生成把content包在body内并带head的html.
	 * </pre>
	 *
	 * @param content
	 * @return
	 */
	public static Render body(String content) {

		String html = Stringx.printf(
				"<html>\n%s\n<body>\n%s\n</body>\n</html>", head(), content);
		return html(html);
	}

	/**
	 * 从当前类路径得到html的head部分, 对应模板Head.ftl
	 *
	 * @return
	 */
	public static String head() {

		String filename = Filex.resource(Render.class, "Head.ftl");
		Template t = Template.create(filename);
		String contextPath = BaseConfig.CONTEXT_PATH;
		if (BaseConfig.has(contextPath)) {
			t.set(contextPath, BaseConfig.get(contextPath));
		}
		return t.toString();
	}

	/**
	 * <pre>
	 * 把Map<变量名, 值>转为html.
	 *
	 * </pre>
	 *
	 * @param map
	 * @param ajaxable
	 *            是否在&lt;head>中包含css和js
	 * @return
	 */
	public static Render html(Map<String, ?> map, boolean ajaxable) {

		// TODO
		return null;
	}

	/**
	 * application/json
	 *
	 * @return
	 */
	public static Render json(String content) {
		return result("application/json", content);
	}

	public static Render json(Table table) {
		return json(table.toJson());
	}

	/**
	 * 把Map<变量名, 值>转为json对象.
	 *
	 * @param map
	 *            值可以是Cell, Row, Table, String, null
	 * @return
	 */
	public static Render json(Map<String, ?> map) {

		if (map == null)
			return json("{}");

		StringBuilder sb = new StringBuilder("{\n");

		for (Iterator<String> itr = map.keySet().iterator(); itr.hasNext();) {
			String key = itr.next();
			Object val = map.get(key);
			sb.append(key).append(": ");
			if (val == null) {
				sb.append("null");
			} else if (val instanceof Cell) {
				sb.append(((Cell) val).toJson());
			} else if (val instanceof Row) {
				sb.append(((Row) val).toJson());
			} else if (val instanceof Table) {
				sb.append(((Table) val).toJson());
			} else if (Numberx.isNumber(val)) {
				sb.append(val);
			} else {
				sb.append(new Cell(val.toString()).toJson());
			}
			if (itr.hasNext())
				sb.append(",");
			sb.append("\n");
		}
		sb.append("}");

		return json(sb.toString());
	}

	/**
	 * text/xml
	 *
	 * @return
	 */
	public static Render xml(String content) {
		return result("text/xml", content);
	}

	/**
	 * text/plain
	 *
	 * @return
	 */
	public static Render text(String content) {
		return result("text/plain", content);
	}

	public static Render text(Table table) {
		return text(table.toString());
	}

	/**
	 * text/css
	 *
	 * @param type
	 * @param content
	 * @return
	 */
	public static Render css(String content) {
		return result("text/css", content);
	}

	public static Render js(String content) {
		return result("text/javascript", content);
	}

	private static Render result(String type, String content) {
		Render render = new Render();
		render.type = type;
		render.content = content;
		return render;
	}

	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Render))
			return false;
		Render r = (Render) o;
		return r.type.equals(type) && r.content.equals(content);
	}

	public int hashCode() {
		int result = 17;
		System.out.println("type:" + type.hashCode());
		System.out.println("content:" + content.hashCode());
		result = 37 * result + type.hashCode();
		result = 37 * result + content.hashCode();
		return result;
	}
}
