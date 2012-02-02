package wr3.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 集中设置Hibernate等的log级别。
 * <pre>
 * usage:
 *   Logx.hibernate();
 * </pre>
 * 
 * jdk log 的级别：<br/>
 * OFF > SEVERE > WARNING > INFO > CONFIG > FINE > FINER > FINEST > ALL  
 * @author jamesqiu 2009-9-18
 *
 */
public class Logx {

	/**
	 * 给hibernate相关的应用设置最小输出。
	 */
	public static void hibernate() {
         Logger logger = Logger.getLogger("org.hibernate");
         logger.setLevel(Level.WARNING);
	}
}
