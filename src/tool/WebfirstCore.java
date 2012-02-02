package tool;

import com.nasoft.webfirst.webserver.core.Server;
import com.nasoft.webfirst.webserver.core.webapp.WebAppContext;

/**
 * <pre>
 * 调用WebFirst-core内嵌web/servlet容器，不支持jsp。
 * usage:
 *  new WebfirstCore().start();            // 使用缺省端口和缺省目录、context path
 *  new WebfirstCore(80, "./webapp", "/"); // 使用自定义的端口、目录、context path
 * </pre>
 * @author jamesqiu 2011-2-16
 */
public class WebfirstCore {

	private int port = 80;
	private String resourceBase = "./webapp";
	private String contextPath = "/";

	/**
	 * 使用缺省配置：80服务端口，应用在为"./webapp"目录，"/"访问。
	 */
	public WebfirstCore() {
	}

	/**
	 * 使用自定义配置。
	 * @param port http服务端口
	 * @param resourceBase 存放webapp的目录
	 * @param contextPath http访问路径
	 */
	public WebfirstCore(int port, String resourceBase, String contextPath) {
		this.port = port;
		this.resourceBase = resourceBase;
		this.contextPath = contextPath;
	}

	public void start() {

		Server server = new Server(port);
		WebAppContext context = new WebAppContext();
		// context.setDescriptor("./webapp/WEB-INF/web.xml");
		context.setResourceBase(resourceBase);
		context.setContextPath(contextPath);
		context.setParentLoaderPriority(true); // 注：这个设置很重要！
		server.setHandler(context);
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void printUsage() {
		System.out.println("usage:\n" +
				"  java tool.WebfirstCore <port> <resourceBase> <contextPath>\n" +
				"eg.\n" +
				"  java tool.WebfirstCore 8080 ./webapp /wr3");
	}

	private static boolean isHelp(String option) {
		String[] options = new String[]{"-h", "--h", "--help", "/h"};
		for (int i = 0; i < options.length; i++) {
			if (options[i].equals(option)) return true;
		}
		return false;
	}

	// ----------------- main() -----------------//
	public static void main(String[] args) {

		if (args.length==0) {
			new WebfirstCore().start();
		} else if (args.length==1 && isHelp(args[0])) {
			printUsage();
		} else if (args.length==3){
			int port = Integer.parseInt(args[0]);
			String resourceBase = args[1];
			String contextPath  = args[2];
			new WebfirstCore(port, resourceBase, contextPath).start();
		} else {
			printUsage();
		}
	}
}
