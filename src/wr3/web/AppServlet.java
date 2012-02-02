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
 * 经AppFilter转换后的url传递到该Servlet，带controller和action参数。
 * 简单初始化Params和Session后转给AppController，动态执行controller的action，
 * 返回Render类型结果(html, text, json)
 *
 * </pre>
 * @author jamesqiu 2008-11-30
 */
@SuppressWarnings("serial")
public class AppServlet extends HttpServlet {

	/**
	 * 执行 controller.action(), 输出结果
	 *
	 * @param req
	 * @param resp
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private void handle(HttpServletRequest req, HttpServletResponse resp) {

		// 取 controller, action
		Map<String, String[]> req_params = req.getParameterMap();
		String controller = req_params.get("controller")[0];
		String action = req_params.get("action")[0];
		// 取所有 params, 进行转码
		Params params = new Params(new LinkedHashMap(req_params));
		// --- 在params中增加baseurl
		params.set("baseurl", Servletx.baseurl(req)); // 如：http://localhost:8080/wr3
		params.set("webapp", BaseConfig.webapp()); // 如：/wr3
		/**
		 * 说明：上诉对params的操作完成后，controller的params变量中含 "controller", "action",
		 * "baseurl", "webapp"变量值。
		 */
		// 取session, 没有session则创建一个新的.
		Session session = new Session(req.getSession(true));
		// 执行controller，取结果
		Object rt = new AppController(controller, action, params, session)
				.run();
		output(resp, rt);
	}

	/**
	 * 根据AppController的返回值进行输出.
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
		// web页面输出编码, jetty/webfirst/tomcat皆能正确处理
		resp.setContentType(type + charset);
		// String s = Charsetx.utf2iso(s); // 上面设了，就不能再转。

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
