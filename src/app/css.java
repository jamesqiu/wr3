package app;

import wr3.text.Template;
import wr3.util.Filex;
import wr3.web.Render;

/**
 * <pre>
 * �õ���̬��css�ļ����ݣ�
 * "<link href=\"../css/min\" rel=\"stylesheet\" type=\"text/css\"></link>"
 * ���ߣ�
 * wr3.html.Util.css("css/min");
 * </pre>
 * @author jamesqiu 2010-11-12
 */
public class css {

	/**
	 * ���������������Сcss: "/wr3/css/min"
	 * @return
	 */
	public Render min() {
		String s = "body, a, font { font-family: Arial; }";
		return Render.css(s);
	}

	/**
	 * wr3���õ�css: "/wr3/css/main"
	 * @return
	 */
	public Render main() {
		String filename = Filex.resource(this, "main.css.ftl");
		Template t = Template.create(filename);

		return Render.css(t.toString());
	}
}
