package wr3.web;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import wr3.BaseConfig;
import wr3.util.Servletx;

/**
 * <pre>
 * ��AppFilterת�����url���ݵ���Servlet����controller��action������
 * �򵥳�ʼ��Params��Session��ת��AppController����ִ̬��controller��action��
 * ����Render���ͽ��(html, text, json)
 *
 * </pre>
 * @author jamesqiu 2008-11-30
 */
@SuppressWarnings("serial")
public class AppServlet extends HttpServlet {

	/**
	 * ִ�� controller.action(), ������
	 *
	 * @param req
	 * @param resp
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private void handle(HttpServletRequest req, HttpServletResponse resp) {

		// ȡ controller, action
		Map<String, String[]> req_params = req.getParameterMap();
		String controller = req_params.get("controller")[0];
		String action = req_params.get("action")[0];
		// ȡ���� params, ����ת��
		Params params = new Params(new LinkedHashMap(req_params));
		// --- ��params������baseurl
		params.set("baseurl", Servletx.baseurl(req)); // �磺http://localhost:8080/wr3
		params.set("webapp", BaseConfig.webapp()); // �磺/wr3
		/**
		 * ˵�������߶�params�Ĳ�����ɺ�controller��params�����к� "controller", "action",
		 * "baseurl", "webapp"����ֵ��
		 */
		// ȡsession, û��session�򴴽�һ���µ�.
		Session session = new Session(req.getSession(true));
		// ִ��controller��ȡ���
		Object rt = new AppController(controller, action, params, session)
				.run();
		output(resp, rt);
	}

	/**
	 * ����AppController�ķ���ֵ�������.
	 *
	 * @param resp
	 */
	private void output(HttpServletResponse resp, Object rt) {

		String type, content;
		if (rt instanceof Render) {
			Render render = (Render) rt;
			type = render.type();
			content = render.content();
		} else {
			type = "text/plain";
			content = "" + rt;
		}
		// webҳ���������, jetty/webfirst/tomcat������ȷ����
		resp.setContentType(type + charset);
		// String s = Charsetx.utf2iso(s); // �������ˣ��Ͳ�����ת��

		try {
			resp.getWriter().print(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public AppServlet() {
	}

	@Override
	public void init() throws ServletException {
		super.init();
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		handle(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		handle(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		handle(req, resp);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		handle(req, resp);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		handle(req, resp);
	}

	private final static String charset = "; charset=utf-8";
}
