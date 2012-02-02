package wr3.html;

import static wr3.util.Stringx.*;

/**
 * 得到形如： <span id="id1">cn&amp;中文</span> 的html语句
 * @author jamesqiu 2009-6-27
 *
 */
public class Message implements Tag {

	String text = "";
	String id = null;

	private Message() {}

	public static Message create() {
		return new Message();
	}

	public static Message create(String text) {

		Message msg = create();
		msg.text = text;
		return msg;
	}

	public Message text(String text) {

		this.text = text;
		return this;
	}

	public Message id(String id) {
		this.id = id;
		return this;
	}

	public String html() {

		StringBuilder sb = new StringBuilder();
		sb.append(escapeHtml(text));
		if (!nullity(id)) {
			sb.insert(0, "<span id=\"" + id + "\">").append("</span>");
		}
		return sb.toString();
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		Message msg = Message.create();
		msg.id("id1");
		msg.text("cn&中文");
		System.out.println(msg.html());
	}
}
