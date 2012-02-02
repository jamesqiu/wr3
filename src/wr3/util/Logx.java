package wr3.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ��������Hibernate�ȵ�log����
 * <pre>
 * usage:
 *   Logx.hibernate();
 * </pre>
 * 
 * jdk log �ļ���<br/>
 * OFF > SEVERE > WARNING > INFO > CONFIG > FINE > FINER > FINEST > ALL  
 * @author jamesqiu 2009-9-18
 *
 */
public class Logx {

	/**
	 * ��hibernate��ص�Ӧ��������С�����
	 */
	public static void hibernate() {
         Logger logger = Logger.getLogger("org.hibernate");
         logger.setLevel(Level.WARNING);
	}
}
