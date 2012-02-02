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
 * ʹ��HttpUnit����Servlet.
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
		
		// ����Servlet�����л���
		server = new ServletRunner();
		// �򻷾���ע��Servlet
		server.registerServlet("AppServlet", AppServlet.class.getName());
		// ��������Servlet�Ŀͻ���
		client = server.newClient();
	}

	/**
	 * ����url����, �õ���Ӧ, ͬʱʵ����servlet
	 * @param url
	 * @return ת����response�ı�
	 */
	private String request(String url) {
		
		// ��������
		request = new GetMethodWebRequest(url);
		// �õ���Ӧ
		try {
			response = client.getResponse(request);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

		// �õ� servletʵ��
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
		
		// ���ģ�����������Ϣ
		String respText = null;
		try {
			respText = response.getText();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// ����̨���ת��
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
		// ����servlet��ĳ������
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
