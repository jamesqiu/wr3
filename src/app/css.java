package app;

import wr3.text.Template;
import wr3.util.Filex;
import wr3.web.Render;

/**
 * <pre>
 * 得到动态的css文件内容：
 * "<link href=\"../css/min\" rel=\"stylesheet\" type=\"text/css\"></link>"
 * 或者：
 * wr3.html.Util.css("css/min");
 * </pre>
 * @author jamesqiu 2010-11-12
 */
public class css {

	/**
	 * 仅定义了字体的最小css: "/wr3/css/min"
	 * @return
	 */
	public Render min() {
		String s = "body, a, font { font-family: Arial; }";
		return Render.css(s);
	}

	/**
	 * wr3常用的css: "/wr3/css/main"
	 * @return
	 */
	public Render main() {
		String filename = Filex.resource(this, "main.css.ftl");
		Template t = Template.create(filename);

		return Render.css(t.toString());
	}
}
