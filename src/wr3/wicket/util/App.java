package wr3.wicket.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.settings.Settings;

/**
 * <pre>
 * usage:
 *  App.mount(this, "/user", UserPage.class);
 *  App.log(this);
 * </pre>
 * @author jamesqiu 2009-8-21
 *
 */
public class App extends WebApplication {

	/**
	 * 在Wicket入口程序中定义主页面之外其他页面uri，不用在web.xml中定义。
	 * @param <T>
	 * @param Wicket WebApplication 入口应用
	 * @param path eg."/user"
	 * @param clazz eg. UserPage.class
	 */
	public <T extends Page> void mount(String path, Class<T> clazz) {
		
		super.mountBookmarkablePage(path, clazz);
	}
	
	/**
	 * 设置Wicket的系统log级别设到较合理的warning
	 * @param app
	 */
	public static void log(WebApplication app) {
		
		Logger logger = Logger.getLogger("org.apache.wicket");
		logger.setLevel(Level.WARNING);
	}
	
	/**
	 * 让Wicket支持HTTPS
	 * @return
	 */
	public App https() {
		super.getRequestCycleSettings().setRenderStrategy(Settings.ONE_PASS_RENDER);
		return this;
	}

	@Override
	public Class<? extends Page> getHomePage() {
		
		return null;
	}
	
	
}
