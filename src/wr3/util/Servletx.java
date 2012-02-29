package wr3.util;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class Servletx {

	/**
	 * �õ�һ��request�Ļ���url������webapp������url�Ĺ���ǰ׺
	 * @param req
	 * @return
	 */
	public static String baseurl(HttpServletRequest req) {
		
		// ����req��url���£�
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
	 * request url�в���baseurl����������
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
		// Ҳ����������ʵ�֣�
		String url = req.getRequestURL().toString(); // http://hostname.com/mywebapp/servlet/MyServlet/a/b;c=123
		String queryString = req.getQueryString();   // d=789
		if (queryString != null) {
		    url += "?"+queryString;
		}
		return url;				
	}
	
	/**
	 * �õ�һ��request������url
	 * @param req
	 * @return
	 */
	public static String url(HttpServletRequest req) {
		
		return baseurl(req)+appurl(req);
	}
	
	/**
	 * ����webapp�����ƣ�����url�еġ�mywebapp��:
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
	 * �ӷǿյ�req�л�ȡsession�����û��session������һ����
	 * @param req
	 * @return �ǿյ�HttpSession
	 */
	public static HttpSession session(HttpServletRequest req) {
		
		return req.getSession(true);
	}
	
	/**
	 * �õ�ָ��name��cookieֵ
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
