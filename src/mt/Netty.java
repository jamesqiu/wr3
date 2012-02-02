package mt;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import wr3.util.Charsetx;

/**
 * Netty3 tcp/http 测试
 * @author jamesqiu 2009-6-8
 *
 */
public class Netty {

	static {
		Logger logger = Logger.getLogger("org.jboss.netty");
		logger.setLevel(Level.WARNING);
	};
	
	static String longString = "";
	static byte[] longBytes;
	static {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			sb.append(i).append("镕.");
		}
		longString = sb.toString();
		try {
			longBytes = longString.getBytes(Charsetx.UTF);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
		
	final int PORT = 8007;
	
	/**
	 * tcp server
	 */
	static final ChannelGroup allChannels = new DefaultChannelGroup("tcpd");
	
	void tcpd() {
		
		ServerBootstrap server = Nettyx.server();
		server.getPipeline().addLast("handler", new HandlerTcpd());
		server.setOption("child.tcpNoDelay", true);
		server.setOption("child.keepAlive", true);

		Channel channel = server.bind(new InetSocketAddress(PORT));
		allChannels.add(channel);
	}
    @ChannelPipelineCoverage("all")
	class HandlerTcpd extends SimpleChannelHandler {
    	
		@Override
		public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
				throws Exception {

			super.channelOpen(ctx, e);
			allChannels.add(e.getChannel());
		}

		@Override
		public void channelConnected(ChannelHandlerContext ctx,
				ChannelStateEvent e) throws Exception {
			
			/*
			ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
			buf.writeBytes(longBytes);
			ChannelFuture future = e.getChannel().write(buf);
			//*/
			ChannelFuture future = e.getChannel().write(Nettyx.string2buf(longString));
			
			// server主动关闭 
			future.addListener(ChannelFutureListener.CLOSE);
		}
		
		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
				throws Exception {
			
			e.getChannel().write(e.getMessage());
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
				throws Exception {
			e.getCause().printStackTrace();
			e.getChannel().close();
		}
	}
    
    /**
     * graceful shutdown server
     * @param factory
     */
    void closeServer(ChannelFactory factory) {
    	
		ChannelGroupFuture future = allChannels.close();
		future.awaitUninterruptibly();
		factory.releaseExternalResources();    	
    }
    
    /**
     * tcp client
     * @param s
     */
	void tcpc(String s) {

		ClientBootstrap client = Nettyx.client();
		// 多个channel pipeline共用一个channel handler
//		client.getPipeline().addLast("handler", new HandlerTcpc(s));
		// 多个channel pipeline都是使用自己的channel handler
		client.setPipelineFactory(new PipelineTcpc(s));
		client.setOption("tcpNoDelay", true);
		client.setOption("keepAlive", false);
		
		@SuppressWarnings("unused")
		ChannelFuture future1 = client.connect(new InetSocketAddress("localhost", PORT));
		@SuppressWarnings("unused")
		ChannelFuture future2 = client.connect(new InetSocketAddress("localhost", PORT));
		// gracefual shutdown client
//		closeClient(factory, future);
	}

	class PipelineTcpc implements ChannelPipelineFactory {

		String s;
		
		PipelineTcpc(String s) {
			this.s = s;
		}
	
		public ChannelPipeline getPipeline() throws Exception {

			ChannelPipeline pipeline = Channels.pipeline();
			pipeline.addLast("handler", new HandlerTcpc(s));
			return pipeline;
		}
	}
	
	class DecoderTcpc extends FrameDecoder {

		@Override
		protected Object decode(ChannelHandlerContext ctx, Channel channel,
				ChannelBuffer buffer) throws Exception {

			if (buffer.readableBytes() < longBytes.length) {
				return null;
			}
			
//			return buffer.;
			return null;//todo
		
		}
		
	}
	
    @ChannelPipelineCoverage("one")	// 不能用"all"因为有内部变量 bufTotal和s
	class HandlerTcpc extends SimpleChannelHandler {

		String s;
		/**
		 * 读一个完整package的ChannelBuffer
		 */
		final ChannelBuffer bufTotal = ChannelBuffers.dynamicBuffer();
		
		HandlerTcpc(String s) { this.s = s; }

		@Override
		public void channelConnected(ChannelHandlerContext ctx,
				ChannelStateEvent e) throws Exception {

			System.out.println("channelConnected()");
			// send request
			/*
			byte[] bytes = s.getBytes();
			ChannelBuffer buffer = ChannelBuffers.buffer(bytes.length);
			buffer.writeBytes(bytes);
			e.getChannel().write(buffer);
			System.out.println("send request: " + s);
			//*/
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
		throws Exception {
			
			// read response
			System.out.println("messageReceived()");
			ChannelBuffer buf = (ChannelBuffer) e.getMessage();
			bufTotal.writeBytes(buf);	// 本次收到的bytes读入bufTotal

			Thread.sleep(2000);
			// bufTotal收到全部信息后进行处理
			if (bufTotal.readableBytes() >= longBytes.length) {
				String s = Nettyx.buf2string(bufTotal);
				System.out.println(s);	
				System.out.println(s.length());
				e.getChannel().close();
//				System.exit(0);
			}
			/*
			byte[] bytes = new byte[s.getBytes().length];
			buf.readBytes(bytes);
			
			System.out.println("receive: " + new String(bytes));
			//*/
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
				throws Exception {

			e.getCause().printStackTrace();
			e.getChannel().close();
		}
	}
    
    /**
     * shut down the I/O threads and let the application exit gracefully
     * @param factory
     * @param future
     */
    @SuppressWarnings("unused")
	private void closeClient(ChannelFactory factory, ChannelFuture future) {

    	// to determine if the connection attempt was successful or not. 
		future.awaitUninterruptibly();
		if (!future.isSuccess()) {
			future.getCause().printStackTrace();
		}
		// wait until the connection is closed by waiting for the closeFuture
		future.getChannel().getCloseFuture().awaitUninterruptibly();
		// The only task left is to release the resources being used by ChannelFactory.
		factory.releaseExternalResources();
		System.out.println("closeClient()");
    }
	
    void test() {
    
    	ChannelBuffers.dynamicBuffer();
    }
    
	void httpd() {
		
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		
		if (args.length == 0) {
			System.out.println("Netty tcp/http test, usage: \n" + 
					"    Netty c   http client\n" +
					"    Netty s   http server\n" +
					"    Netty -t s tcp server\n" +
					"    Netty -t c tcp client\n");
			return;
		}
		
		Netty o = new Netty();
		String a0 = args[0];
		if (a0.equals("s")) {
			o.httpd();
		} else if (a0.equals("c")) {
			o.httpd();
		} else if (a0.equals("-t")) {
			String a1 = args[1];
			if (a1.equals("s")) {
				o.tcpd();
			} else {
				o.tcpc(args[2]);
			}
		}
	}

}
