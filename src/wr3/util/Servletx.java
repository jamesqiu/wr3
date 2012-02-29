package wr3.util;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class Servletx {

	/**
	 * 得到一个request的基础url，即本webapp下所有url的公共前缀
	 * @param req
	 * @return
	 */
	public static String baseurl(HttpServletRequest req) {
		
		// 假设req的url如下：
		// http://server:80/mywebapp/servlet/MyServlet/a/b;c=123?d=789
		String scheme = req.getScheme(); 			// http
		String serverName = req.getServerName(); 	// server
		int serverPort = req.getServerPort(); 		// 80
		String contextPath = req.getContextPath(); 	// /mywebapp
		
		// http://server:80/mywebapp
		String url = Stringx.printf("%s://%s:%d%s", 
				scheme, serverName, serverPort, contextPath);
		return url;
	}
	
	/**
	 * request url中不含baseurl的其他部分
	 * @param req
	 * @return
	 */
	public static String appurl(HttpServletRequest req) {
		
		String servletPath = req.getServletPath(); 	// /servlet/MyServlet
		String pathInfo = req.getPathInfo(); 		// /a/b;c=123
		String queryString = req.getQueryString(); 	// d=789

		// Reconstruct original requesting URL
		String url = servletPath;
		if (pathInfo != null) {
			url += pathInfo;
		}
		if (queryString != null) {
			url += "?" + queryString;
		}
		return url;	// /servlet/MyServlet/a/b;c=123?d=789
	}

	/**
	 * alias of {@link #url(HttpServletRequest)}
	 * @param req
	 * @return
	 */
	public static String fullurl(HttpServletRequest req) {
//		return url(req);
		// 也可以用如下实现：
		String url = req.getRequestURL().toString(); // http://hostname.com/mywebapp/servlet/MyServlet/a/b;c=123
		String queryString = req.getQueryString();   // d=789
		if (queryString != null) {
		    url += "?"+queryString;
		}
		return url;				
	}
	
	/**
	 * 得到一个request完整的url
	 * @param req
	 * @return
	 */
	public static String url(HttpServletRequest req) {
		
		return baseurl(req)+appurl(req);
	}
	
	/**
	 * 返回webapp的名称，如下url中的“mywebapp”:
	 * http://server:80/mywebapp/servlet/MyServlet/a/b;c=123?d=789
	 * @param req
	 * @return
	 */
	public static String webapp(HttpServletRequest req) {
		
		String contextPath = req.getContextPath();
		if (!Stringx.nullity(contextPath) && contextPath.startsWith("/")) {
			contextPath = contextPath.substring(1);
		}
		return contextPath;
	}
	
	/**
	 * 从非空的req中获取session，如果没有session，构建一个。
	 * @param req
	 * @return 非空的HttpSession
	 */
	public static HttpSession session(HttpServletRequest req) {
		
		return req.getSession(true);
	}
	
	/**
	 * 得到指定name的cookie值
	 * @param req
	 * @return 
	 */
	public static String cookie(HttpServletRequest req, String name) {
		
		Cookie[] cookies = req.getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(name)) {
				String v = cookie.getValue();
				try {
					return java.net.URLDecoder.decode(v, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					return v;
				}
			}
		}
		return null;
	}
	
}
