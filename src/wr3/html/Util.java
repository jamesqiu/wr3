package wr3.html;

import wr3.util.Stringx;

/**
 * ����htmlԪ�أ�������bgcolor��meta��js��cssд��
 * @author jamesqiu 2010-11-12
 */
public class Util {

	/**
	 * �õ����õĲ����۱���ɫ��R=196��G=B=206
	 * @return
	 */
	public static String bgColor() {
		return "background-color: #c4cece;"; // ���� wr3.util.Numberx.hex(196) ���㡣
	}

	/**
	 * �õ�html����charset��meta
	 * @param content
	 * @param charset
	 * @return
	 */
	public static String meta(String content, String charset) {
		String pattern = "<meta http-equiv=\"Content-Type\" content=\"%s; charset=%s\">";
		return Stringx.printf(pattern, content, charset);
	}

	/**
	 * �õ�html����charset��ȱʡmeta���
	 * @return
	 */
	public static String meta() {
		return meta("text/html", "utf-8");
	}

	/**
	 * �õ�html����.js�ļ������
	 * @param jsFile
	 * @return
	 */
	public static String js(String jsFile) {
		String pattern = "<script type=\"text/javascript\" src=\"%s\"></script>";
		return Stringx.printf(pattern, jsFile);
	}

	/**
	 * �õ�html����.css�ļ������
	 * @param cssFile
	 * @return
	 */
	public static String css(String cssFile) {
		String pattern = "<link href=\"%s\" rel=\"stylesheet\" type=\"text/css\"></link>";
		return Stringx.printf(pattern, cssFile);
	}

	// ----------------- main() -----------------//
	public static void main(String[] args) {
		String[] ss = {
			bgColor(),
			meta(),
			js("js/common.js"),
			css("main.css")
		};
		for (String s : ss) {
			System.out.println(s);
		}
	}

}
