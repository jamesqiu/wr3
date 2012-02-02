package mt;

import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import wr3.util.Charsetx;

/**
 * Netty Util
 * @author jamesqiu 2009-6-10
 *
 */
public class Nettyx {

	/**
	 * create a String from buffer.
	 * @param buf
	 * @return
	 */
    public static String buf2string(ChannelBuffer buf) {
    	
    	return buf.toString(Charsetx.UTF);
    }
    
    /**
     * create a ChannelBuffer from string.
     * @param s
     * @return
     */
    public static ChannelBuffer string2buf(String s) {
    	
    	return ChannelBuffers.copiedBuffer(s, Charsetx.UTF);
    }
    
    /**
     * create a server bootstrap.
     * @return
     */
    public static ServerBootstrap server() {
    	
		ChannelFactory factory = new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(), Executors.newCachedThreadPool());

		return new ServerBootstrap(factory);  	
    }

    /**
     * create a client bootstrap.
     * @return
     */
    public static ClientBootstrap client() {

    	ChannelFactory factory = new NioClientSocketChannelFactory(
    			Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
		return new ClientBootstrap(factory);
    }

}
