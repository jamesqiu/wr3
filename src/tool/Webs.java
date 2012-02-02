package tool;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.webapp.WebAppContext;

import wr3.util.Datetime;

/**
 * WebServer：使用WebFirst或者Jetty作为内嵌Web/Servlet容器。
 * @author jamesqiu 2011-2-16
 */
public class Webs {

	/**
	 * 方式1：使用 Handler 进行简单服务
	 */
	static void useHandlers() {
		Handler handler = new AbstractHandler() {
			public void handle(String target, HttpServletRequest req,
					HttpServletResponse resp, int dispatch) throws IOException,
					ServletException {
				resp.setContentType("text/html; charset=utf-8");
		        resp.setStatus(HttpServletResponse.SC_OK);
		        resp.getWriter().printf("<h1>Hello cn中文.%s</h1>", Datetime.datetime());
		        ((Request)req).setHandled(true);
			}
		};
		Server s = new Server(80);
		s.setHandler(handler);
		try {
			s.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 方式2：
	 * 参考：http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty
	 */
	static void useWebapp() {

        Server server = new Server(8080);

        WebAppContext context = new WebAppContext();
//        context.setDescriptor("./webapp/WEB-INF/web.xml");
        context.setResourceBase("./webapp");
        context.setContextPath("/wr3");
        context.setParentLoaderPriority(true); // 注：这个设置很重要！
        server.setHandler(context);
        try {
        	server.start();
        	server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ----------------- main() -----------------//
	public static void main(String[] args) {
		useHandlers();
	}

}
