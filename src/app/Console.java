package app;

import java.lang.reflect.Method;

import wr3.BaseConfig;
import wr3.util.Classx;
import wr3.util.Datetime;
import wr3.util.Filex;
import wr3.util.Stringx;
import wr3.web.Render;

/**
 * 所有的Controllers的门户。
 * @author jamesqiu 2009-8-26
 */
public class Console {

	
	public Render index() {
		
		return controllers();
	}
	
	/**
	 * 清应用服务器控制台输出
	 * @return
	 */
	public Render cls() {
		
		for (int i = 0; i < 20; i++) {
			System.out.println("");
		}
		String msg = "clear console at " + Datetime.datetime();
		System.out.println(msg);
		return Render.html(msg + "<br/>" +
				"<a href='javascript:history.back();'>Back</a>");
	}
	
	static String[] objectMethods = Classx.objectMethods();	
	/**
	 * 列出所有的Controllers及其action
	 * @return
	 */
	public Render controllers() {
		
		String appPath = BaseConfig.get("app.path");
		String[] classes = Filex.list(appPath+"app", ".class");
//		System.out.println(Stringx.join(classes));
		
		StringBuilder sb = new StringBuilder();
		
		for (String cls : classes) {
			
			String clsname = Stringx.leftback(cls, ".class");
			// 忽略衍生出来的匿名类如：Test3$1
			if (clsname.indexOf('$') > -1) continue;
			
			sb.append(clsname).append("<br/>\n");
			Object o = Classx.getHotObject(appPath, "app."+clsname);
			Method[] methods = Classx.getMethods(o);
			
			for (Method method : methods) {
				String name = method.getName();
				
				if (Stringx.in(name, objectMethods)) continue;
				String s = Stringx.printf(SPACE2 + ".<a href=\"%s/%s/%s\">%s</a>", 
						BaseConfig.webapp() ,clsname, name, name);
				sb.append(s).append(SPACE2);
			}
			
			sb.append("<br/><br/>\n");
		}
		
		return Render.body(sb.toString());
	}
	
	private final static String SPACE2 = "&nbsp;&nbsp;";
	
	public Render env() {
		
		String env = System.getProperties().toString();
		return Render.html(Stringx.replaceAll(env, ", ", "</br>"));
	}
	
	
}
