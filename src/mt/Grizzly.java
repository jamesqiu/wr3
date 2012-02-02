package mt;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import wr3.util.Datetime;

import com.sun.grizzly.Connection;
import com.sun.grizzly.TransportFactory;
import com.sun.grizzly.filterchain.FilterAdapter;
import com.sun.grizzly.filterchain.FilterChainContext;
import com.sun.grizzly.filterchain.NextAction;
import com.sun.grizzly.filterchain.TransportFilter;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.nio.transport.TCPNIOTransport;
import com.sun.grizzly.streams.StreamReader;
import com.sun.grizzly.streams.StreamWriter;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;


/**
 * <pre>
 * ≤‚ ‘Grizzly tcp server, http server
 * </pre>
 * @author jamesqiu 2009-5-31
 */
public class Grizzly {

    public static final String HOST = "localhost";
    public static final int PORT = 7777;
	
	/**
	 * tcp server
	 * @throws IOException 
	 * @see https://grizzly.dev.java.net/tutorials/tutorial-framework-filter-sample/index.html
	 */
	void tcpd() throws IOException {

		TCPNIOTransport transport = TransportFactory.getInstance().createTCPTransport();
		transport.getFilterChain().add(new TransportFilter());
		transport.getFilterChain().add(new FilterTcpd());
		transport.bind(HOST, PORT);
		transport.start();
  	}
	
	class FilterTcpd extends FilterAdapter {

		@Override
		public NextAction handleRead(FilterChainContext ctx,
				NextAction nextAction) throws IOException {

			ctx.getStreamWriter().writeStream(ctx.getStreamReader());
			return nextAction;
		}
	}
	
	/**
	 * tcp client
	 * @param s
	 * @throws IOException 
	 * @throws TimeoutException 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	@SuppressWarnings("unchecked")
	public void tcpc(String s) throws IOException, InterruptedException, ExecutionException, TimeoutException {
		
		TCPNIOTransport transport = TransportFactory.getInstance().createTCPTransport();
		transport.start();
		Future<Connection> furture = transport.connect(HOST, PORT);
		Connection connection = furture.get(10, TimeUnit.SECONDS);
		// send request
		StreamWriter writer = connection.getStreamWriter();
		writer.writeByteArray(s.getBytes());
		Future<Integer> writeFurture = writer.flush();
		writeFurture.get();
		// read response
		StreamReader reader = connection.getStreamReader();
		byte[] bytes = new byte[s.getBytes().length];
		Future<Integer> readFurture = reader.notifyAvailable(bytes.length);
		readFurture.get();
		reader.readByteArray(bytes);
		System.out.println(new String(bytes));
		
		System.exit(0);
//		connection.close();
//		transport.stop();
//		TransportFactory.getInstance().close();
	}
	
	/**
	 * http server
	 */
	public void httpd() {
		
		GrizzlyWebServer ws = new GrizzlyWebServer(8080, "./");
		ws.addGrizzlyAdapter(new Adapter1(), new String[0]);
		try {
			ws.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class Adapter1 extends GrizzlyAdapter {

		@SuppressWarnings("unchecked")
		@Override
		public void service(GrizzlyRequest request, GrizzlyResponse response) {
			try {
				response.getWriter().println(Datetime.datetime());
				response.flushBuffer();
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				response.getWriter().println("3's over");
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException, TimeoutException {
		
		if (args.length == 0) {
			System.out.println("Grizzly test, usage: \n" + 
					"    Grizzly -t s          :tcp server\n" +
					"    Grizzly -t c <string> :tcp client\n" +
					"    Grizzly -h s          :http server\n" +
					"    Grizzly -h c          :http client\n");
			return;
		}
		
		Grizzly o = new Grizzly();
		
		if (args[0].equals("-t")) {
			if (args.length==3 && args[1].equals("c")) {
				o.tcpc(args[2]);
			} else {
				o.tcpd();
			}
		} else if (args[0].equals("-h")) {
			if (args.length==2 && args[1].equals("c")) {
//				o.httpd();
			} else {
				o.httpd();
			}
		}
	}
}
