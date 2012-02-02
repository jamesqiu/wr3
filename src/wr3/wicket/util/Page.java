package wr3.wicket.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.wicket.markup.html.WebPage;

public class Page extends WebPage {

	/**
	 * get raw http servlet request.
	 * @return
	 */
	public HttpServletRequest request() {
		
		return super.getWebRequestCycle().getWebRequest().getHttpServletRequest();
	}
	
	/**
	 * get raw http servlet response.
	 * @return
	 */
	public HttpServletResponse response() {
		
		return super.getWebRequestCycle().getWebResponse().getHttpServletResponse();
	}
	
	/**
	 * <pre>
	 * get raw http session.
	 * 注意：和Wicket Session(和Wicket Application周期绑定)不同。
	 * </pre>
	 * @return
	 */
	public HttpSession session() {
		
		return request().getSession();
	}
	
	/**
	 * ajax debug switcher.
	 * @param enable
	 * @return
	 */
	public Page debug(boolean enable) {
		
		super.getApplication().getDebugSettings().setAjaxDebugModeEnabled(enable);
		return this;
	}
}
