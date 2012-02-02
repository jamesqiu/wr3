package mt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.channels.ClosedChannelException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xlightweb.BodyDataSink;
import org.xlightweb.GetRequest;
import org.xlightweb.HttpResponseHeader;
import org.xlightweb.IBodyDataHandler;
import org.xlightweb.IHttpExchange;
import org.xlightweb.IHttpRequest;
import org.xlightweb.IHttpRequestHandler;
import org.xlightweb.IHttpResponse;
import org.xlightweb.IHttpResponseHeader;
import org.xlightweb.NonBlockingBodyDataSource;
import org.xlightweb.client.HttpClientConnection;
import org.xlightweb.client.IHttpClientEndpoint;
import org.xlightweb.server.HttpServer;
import org.xsocket.MaxReadSizeExceededException;
import org.xsocket.connection.BlockingConnection;
import org.xsocket.connection.IBlockingConnection;
import org.xsocket.connection.IDataHandler;
import org.xsocket.connection.IDisconnectHandler;
import org.xsocket.connection.INonBlockingConnection;
import org.xsocket.connection.IServer;
import org.xsocket.connection.NonBlockingConnection;
import org.xsocket.connection.Server;

import wr3.util.Datetime;
import wr3.util.Stringx;

/**
 * 测试xSoket tcp及http
 * @author jamesqiu 2009-5-31
 *
 */
public class XSocket {

	static {
		Logger logger = Logger.getLogger("org.xsocket.connection");
		logger.setLevel(Level.WARNING);
	};
	
	/**
	 * tcp server
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	final int port_tcpd = 8090;
	void tcpd() throws UnknownHostException, IOException {

		IServer srv = new Server(port_tcpd, new HandlerTcpd());
		srv.setIdleTimeoutMillis(3000);
		srv.start();
	}
	
	int index = 0;
	class HandlerTcpd implements IDataHandler, IDisconnectHandler {

		public boolean onData(INonBlockingConnection conn) throws IOException {

			// read request
			String in = conn.readStringByDelimiter("\r\n");
		    // send response
			conn.write(in);
			conn.write("\r\n");
			System.out.println(++index);
			// 没必要调用 conn.close()
			return true;
		}

		public boolean onDisconnect(INonBlockingConnection conn)
				throws IOException {
			
			System.out.println("-- onDisconnect --");
			// 没必要调用 conn.close()
			return true;
		}		
	}
	
	/**
	 * tcp client blocking connection
	 * @param s
	 * @throws BufferOverflowException
	 * @throws IOException
	 */
	void tcpc1(String s) throws BufferOverflowException, IOException {

		IBlockingConnection conn = new BlockingConnection("localhost", port_tcpd);
		 
		if (Stringx.nullity(s)) s = "cn中文";
		int n = 5000;
		int n1 = 0;
		for (int i = 0; i < n; i++) {
			// send request 
			conn.write(s + i + "\r\n");
			// read response
			String rt = conn.readStringByDelimiter("\r\n");
			if (rt.startsWith(s)) n1++;
			System.out.println(rt);
		}
		System.out.println("end n1=" + n1);
	}
	
	void tcpc100(String s) {

		for (int i = 0; i < 1000; i++) {
			
			new Thread() {
			
				public void run() {
					String s = "cn中文";
					IBlockingConnection conn = null;
					try {
						conn = new BlockingConnection("localhost", port_tcpd);
						conn.setIdleTimeoutMillis(3000);
					} catch (IOException e1) {
						e1.printStackTrace();
					}

					// send request
					try {
						conn.write(s + "\r\n");
					} catch (BufferOverflowException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						sleep(10);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					// read response
					String rt = null;
					try {
						rt = conn.readStringByDelimiter("\r\n");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println(rt);
					// 关闭连接
					// 没必要调用 conn.close()
//					close(conn);
				}
			}.start();
		}		
	}
	
	/**
	 * tcp client non-blocking connection
	 * @param s
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	void tcpc2(String s) throws IOException {
		
		INonBlockingConnection conn = new NonBlockingConnection("localhost"
				, port_tcpd, new HandlerTcpc());
		
		conn.setIdleTimeoutMillis(3000);
		
		if (Stringx.nullity(s)) s = "cn中文";
		int n = 100;
		for (int i = 0; i < n; i++) {
			// send request
//			conn.write(s + i + "\r\n");
			conn.write(i);
			conn.write(s.getBytes());
			conn.write("\r\n");
			conn.flush();
		}
	}
	
	int sum = 0;
	class HandlerTcpc implements IDataHandler {

		public boolean onData(INonBlockingConnection conn)
				throws IOException, BufferUnderflowException,
				ClosedChannelException, MaxReadSizeExceededException {

			conn.setIdleTimeoutMillis(10000);
			String rt = conn.readStringByDelimiter("\r\n");
			sum++;
			System.out.println("return " + sum + ": " + rt);
		    return true;
		}
	}
	
	/**
	 * http server
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	final int port_httpd = 80;
	void httpd() throws UnknownHostException, IOException {

		HttpServer server = new HttpServer(port_httpd, new HandlerHttpd());
		server.start();
	}
	
	class HandlerHttpd implements IHttpRequestHandler {

		public void onRequest(IHttpExchange exchange) throws IOException {
		
			String uri = exchange.getRequest().getRequestUrl().toString();
			System.out.println("request arrive: " + uri);				
			// 简单回应
			/*
			IHttpResponse response = new HttpResponse(uri + ", " 
					+ Datetime.datetime() + "\n");
			exchange.send(response);
			//*/
			// 先回应head,再回应body
			IHttpResponseHeader head = new HttpResponseHeader(200, "text/plain");
			BodyDataSink body = exchange.send(head);
			body.write("server return " + uri + " at ");
			body.write(Datetime.datetime() + "\n");
			System.out.println("answer at " + Datetime.datetime());
			body.close();
		}
	}
	
	/**
	 * http client
	 * @throws IOException 
	 */
	void httpc() throws IOException {
		IHttpClientEndpoint client = new HttpClientConnection("localhost", port_httpd);
		for (int i = 0; i < 10; i++) {
			IHttpRequest request = new GetRequest("/?id=" + i);
			IHttpResponse response = client.call(request);
			String s = response.getBlockingBody().readString();
			System.out.print(s);
		}
//		response.getNonBlockingBody().setDataHandler(new HandlerHttpc());
		client.close();
	}
	
	class HandlerHttpc implements IBodyDataHandler {

		public boolean onData(NonBlockingBodyDataSource bodyDataSource)
				throws BufferUnderflowException {

			System.out.println(Datetime.datetime());
			return false;
		}
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) throws UnknownHostException, IOException {
		
		if (args.length == 0) {
			System.out.println("xSocket http test, usage: \n" + 
					"    XSocket c   http client\n" +
					"    XSocket s   http server\n" +
					"    XSocket -t s tcp server\n" +
					"    XSocket -t c tcp client\n");
			return;
		}
		
		XSocket o = new XSocket();
		String a0 = args[0];
		if (a0.equals("s")) {
			o.httpd();
		} else if (a0.equals("c")) {
			o.httpc();
		} else if (a0.equals("-t")) {
			String a1 = args[1];
			if (a1.equals("s")) {
				o.tcpd();
			} else {
				o.tcpc2(args[2]);
			}
		}
		
	}
}
