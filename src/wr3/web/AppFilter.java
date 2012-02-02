package wr3.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponseWrapper;

import wr3.BaseConfig;
import wr3.HotClass;
import wr3.util.Filex;
import wr3.util.Stringx;

/**
 * <pre>
 * Application Filter.
 * ���: uri�� "Controller[/Action/Id?k=v]" ����ʽ����controller���ڣ�
 *             1---------- 2------3--
 * ��:   ת�� "app?controller=Controller&action=Action&id=Id"
 *                            1----------       2------   3--
 *       ��AppServlet����
 * ����:
 *       �����κδ���
 *       
 * usage:
-------------------------------------------
	<!-- AppFilter -->
	<filter>
		<filter-name>AppFilter</filter-name>
		<filter-class>wr3.web.AppFilter</filter-class>
		<init-param>
			<param-name>wr3.home</param-name>
			<param-value>e:/tomcat/webapps/wr3</param-value>
		</init-param>
	</filter>
	<filter-mapping>
		<filter-name>AppFilter</filter-name>
	    <url-pattern>/*</url-pattern>
	</filter-mapping>
-------------------------------------------
 * </pre>
 * @author james 2008-7-2
 * 
 * @see HttpServletResponseWrapper
 * @see HttpServletRequestWrapper
 */
public class AppFilter implements Filter {

	private ServletContext context;
	
	public AppFilter() {
		super();
	}

	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		
		// ��web.xml����ȡwr3.home, û����ȡwebapp����Ŀ¼
		context = config.getServletContext();
		String wr3home = config.getInitParameter(BaseConfig.WR3HOME);
		if (wr3home==null) wr3home = context.getRealPath("");
		// ���Ǵ�java������wr3.home����
		System.setProperty(BaseConfig.WR3HOME, wr3home);
		
		// ����context���磺"/wr3"
		String contextPath = context.getContextPath();
		BaseConfig.set(BaseConfig.CONTEXT_PATH, contextPath);
		
		// ��ʾ��������
		System.out.printf("--[wr3 AppFilter.init()] wr3.home:     '%s'\n", wr3home);
		System.out.printf("--[wr3 AppFilter.init()] CONTEXT_PATH: '%s'\n", contextPath);
		System.out.printf("--[wr3 AppFilter.init()] app.path:     '%s'\n", BaseConfig.appPath());
		System.out.printf("--[wr3 AppFilter.init()] app.package:  '%s'\n", BaseConfig.appPackage());
	}

	/**
	 * ������Ҫ��filter������
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {
		
		HttpServletRequest req = ((HttpServletRequest) request);
//		System.out.printf("--[wr3 AppFilter]: %s\n", req.getRequestURL());
		String url0 = req.getContextPath(); // "/wr3"
		String url1 = req.getRequestURI();	// "/wr3/Test3/list"
		String uri = Stringx.right(url1, url0); // "/Test3/list"
		
		UriParser parser = new UriParser(uri);
		// �����controller[/action/id]��ʽ��controller���ڣ�ת��app����
		if (parser.ok()) {
			String controller = parser.controller();
			if (has(controller)) {
				String action = parser.action();
				String id = parser.hasId() ? ("&id="+parser.id()) : "";
				String uri_new = "/app?controller="+controller+"&action="+action+id;
				context.getRequestDispatcher(uri_new).forward(request, response);
				return;
			}
		}
		// pass������
		chain.doFilter(request, response);		
	}

	// controller����Ƿ������${app.dir}�¡�
	private boolean has(String controller) {
		// jamesqiu todo
		String classUrl = BaseConfig.get("app.path");
		String className = BaseConfig.get("app.package")+"."+controller;		
		String filepath = HotClass.filepath(classUrl, className);
	
		return Filex.has(filepath);
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
	}
}
