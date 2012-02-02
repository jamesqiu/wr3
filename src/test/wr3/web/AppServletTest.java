package test.wr3.web;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.ServletException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.xml.sax.SAXException;

import wr3.util.Charsetx;
import wr3.web.AppServlet;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

/**
 * 使用HttpUnit测试Servlet.
 * @author jamesqiu 2009-1-9
 *
 */
public class AppServletTest {

	ServletRunner server;
	ServletUnitClient client;
	WebRequest request;
	WebResponse response;
	AppServlet servlet;
	
	@Before
	public void init() {
		
		// 创建Servlet的运行环境
		server = new ServletRunner();
		// 向环境中注册Servlet
		server.registerServlet("AppServlet", AppServlet.class.getName());
		// 创建访问Servlet的客户端
		client = server.newClient();
	}

	/**
	 * 发送url请求, 得到响应, 同时实例化servlet
	 * @param url
	 * @return 转码后的response文本
	 */
	private String request(String url) {
		
		// 发送请求
		request = new GetMethodWebRequest(url);
		// 得到响应
		try {
			response = client.getResponse(request);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

		// 得到 servlet实例
		InvocationContext ic = null;
		try {
			ic = client.newInvocation(request);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			servlet = (AppServlet) ic.getServlet();
		} catch (ServletException e) {
			e.printStackTrace();
		}
		
		// 获得模拟服务器的信息
		String respText = null;
		try {
			respText = response.getText();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 控制台输出转码
		return Charsetx.iso2utf(respText);
	}
		
	@After
	public void close() {
		server.shutDown();
	}

	@Test
	public void request1() {
		String url = "http://localhost/AppServlet?controller=App1&action=index";
		String s = request(url);
		assertNotNull(s);
//		System.out.println(s);
		// 测试servlet的某个方法
		assertEquals(AppServlet.class.getName(), servlet.getServletName());
	}
	
//	@Test 
	public void request2() {
		String url = "http://localhost/AppServlet?controller=App1&action=session";
		String s = request(url);
		System.out.println(s);
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {

		JUnitCore.main(AppServletTest.class.getName());
	}
}
