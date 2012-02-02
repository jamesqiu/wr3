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
 * ���ı����ݽ���html/text/json��װ��servlet���.
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
	 * ֱ��ʹ�ú�Controller����action��Ӧ��ģ���ļ� "./$Controller.$action.html" ��
	 * �ɰ�request��session����ֱ������ģ�塣
	 * ���û���ṩģ�壬ֱ�Ӵ�ӡ��������б�
	 * usage: Render.render(this); // ��app.Foo��
	 * </pre>
	 *
	 * @param o
	 *            Controller ����һ����Controller����this
	 * @return
	 */
	public static Render render(Object o) {

		return render(o, null);
	}

	// ��map�е�<key, value>���õ�template��
	private static void fromMap(Template t, Map<String, ?> map) {
		for (Entry<String, ?> set : map.entrySet()) {
			t.set(set.getKey(), set.getValue());
		}
	}

	/**
	 * ʹ�� Params, Session ��Ϊ����ģ�������./$Controller.$action.html ��Ϊģ�壬����������õ������
	 *
	 * @param o
	 *            Controller ����һ����Controller����this
	 * @param map
	 *            ģ�����-ֵ������Params, Session����Ҳ��Ϊģ�����
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
					"<font color=red>��ʾ�����ṩ��Ӧcontroller��action��ģ���ļ�%s��" +
					"����controller�ж���params������</font><br/><br/>" +
					"params:<br/> %s<br/><br/>" +
					"session:<br/> %s<br/><br/>" +
					"�������:<br/> %s<br/><br/>",
					filename,
					params==null ? "δ����" : params,
					session==null ? "δ����" : session,
					map==null ? "δ����" : map) );
		}

		// ��params��session�������õ�ģ����
		if (params != null) fromMap(t, params.toMap());
		if (session != null) fromMap(t, session.toMap());
		// ���Զ��������������String/Cell/Row/Table�����õ�ģ����
		if (map != null) fromMap(t, map);

		return body(t.toString());
	}

	/**
	 * ʹ�� Params, Session ��Ϊ����ģ�弰��ѯ������ $Controller.$action.html ��Ϊģ�壬��
	 * $Controller.$action.groovy ȡ���ݿ������õ����
	 *
	 * @param o
	 * @return
	 */
	public static Render report(Object o) {
		// todo
		return body("todo");
	}

	/**
	 * ʹ�� Params, Session �� map ��Ϊ����ģ�弰��ѯ������ $Controller.$action.html ��Ϊģ�壬��
	 * $Controller.$action.groovy ȡ���ݿ������õ����
	 *
	 * @param o
	 * @param map
	 *            ģ�弰��ѯ����-ֵ
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
	 * ���ɰ�content����body�ڲ���head��html.
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
	 * �ӵ�ǰ��·���õ�html��head����, ��Ӧģ��Head.ftl
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
	 * ��Map<������, ֵ>תΪhtml.
	 *
	 * </pre>
	 *
	 * @param map
	 * @param ajaxable
	 *            �Ƿ���&lt;head>�а���css��js
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
	 * ��Map<������, ֵ>תΪjson����.
	 *
	 * @param map
	 *            ֵ������Cell, Row, Table, String, null
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
