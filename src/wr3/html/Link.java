package wr3.html;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import static wr3.util.Stringx.escapeHtml;
import static wr3.util.Stringx.nullity;

/**
 * 生成一个<a href="..." id=".." base="..">...</a>的link
 * Link.create("label1").url("a.html").id("id1").html();
 * @author jamesqiu 2009-6-27
 *
 */
public class Link implements Tag {

	String body = "link";
	String url = "#";
	String id = null;
	String target = null;

	private Link() {}

	/**
	 * 得到一个 <a href="#">link</a>
	 * @return
	 */
	public static Link create() {
		return new Link();
	}

	/**
	 * 得到一个 <a href="#">body</a>
	 * @param body
	 * @return
	 */
	public static Link create(String body) {
		Link link = create();
		link.body(body);
		return link;
	}

	/**
	 * 设置link的显示内容
	 * @param body
	 * @return
	 */
	public Link body(String body) {
		if (!nullity(body)) this.body = body;
		return this;
	}

	/**
	 * 设置<a href>的url
	 * @param url
	 * @return
	 */
	public Link url(String url) {
		if (!nullity(url)) this.url = url;
		return this;
	}

	public Link id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * 设置<a>的target
	 * @param target frame/win name or "_self", "_top"
	 * @return
	 */
	public Link target(String target) {
		this.target = target;
		return this;
	}

	private Map<String, String> attributes = new LinkedHashMap<String, String>();
	public Link set(String key, String value) {
		attributes.put(key, value);
		return this;
	}

	/**
	 * 得到html安全的<a ...>...</a>字符串.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String html() {

		StringBuilder sb = new StringBuilder("<a href=\"" + escapeHtml(url) + "\"");
		if (!nullity(id)) sb.append(" id=\"" + escapeHtml(id) + "\"");
		if (!nullity(target)) sb.append(" target=\"" + escapeHtml(target) + "\"");
		// add other attributes:
		Set<?> entrys = attributes.entrySet();
		for (Iterator<?> iterator = entrys.iterator(); iterator.hasNext();) {
			Entry<String, String> entry = (Entry<String, String>) iterator.next();
			String k = entry.getKey();
			String v = entry.getValue();
			System.out.println(k + v);
		}
		sb.append(">" + escapeHtml(body) + "</a>");
		return sb.toString();
	}

	public String toString() {
		return html();
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {

		Link link;
		link = Link.create("首页").url("./index.html");
		link = Link.create();
		link = Link.create("hello").url("www.g.cn").target("_self").id("link1");
		link = Link.create("");
		link = Link.create("\"<hello>\"").url("./<index>.html");

		System.out.println(link.html());
	}
}
