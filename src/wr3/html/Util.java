package wr3.html;

import wr3.util.Stringx;

/**
 * 常用html元素：不刺眼bgcolor、meta、js、css写法
 * @author jamesqiu 2010-11-12
 */
public class Util {

	/**
	 * 得到常用的不刺眼背景色：R=196，G=B=206
	 * @return
	 */
	public static String bgColor() {
		return "background-color: #c4cece;"; // 可用 wr3.util.Numberx.hex(196) 来算。
	}

	/**
	 * 得到html设置charset的meta
	 * @param content
	 * @param charset
	 * @return
	 */
	public static String meta(String content, String charset) {
		String pattern = "<meta http-equiv=\"Content-Type\" content=\"%s; charset=%s\">";
		return Stringx.printf(pattern, content, charset);
	}

	/**
	 * 得到html设置charset的缺省meta语句
	 * @return
	 */
	public static String meta() {
		return meta("text/html", "utf-8");
	}

	/**
	 * 得到html包含.js文件的语句
	 * @param jsFile
	 * @return
	 */
	public static String js(String jsFile) {
		String pattern = "<script type=\"text/javascript\" src=\"%s\"></script>";
		return Stringx.printf(pattern, jsFile);
	}

	/**
	 * 得到html包含.css文件的语句
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
