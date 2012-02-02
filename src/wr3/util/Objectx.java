package wr3.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.caucho.hessian.io.Hessian2StreamingInput;
import com.caucho.hessian.io.Hessian2StreamingOutput;

/**
 * ����Object�����л��ͷ����л�(ʹ��Hessian2)
 * @author jamesqiu 2009-7-7
 *
 */
public class Objectx {

	/**
	 * ��Object���л���byte[]
	 * @param obj 
	 * @return Hessian2�淶��byte[]
	 */
	public static byte[] toBytes(Object obj) {
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		Hessian2StreamingOutput out = new Hessian2StreamingOutput(bos);
		try {
			out.writeObject(obj);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byte[] bytes = bos.toByteArray();
	
		return bytes;
	}
	
	/**
	 * ��byte[]�����л�ΪObject
	 * @param bytes Hessian2���л���byte[]
	 * @return
	 */
	public static Object toObject(byte[] bytes) {
		
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes); 
		
		Hessian2StreamingInput in = new Hessian2StreamingInput(bis);
		Object obj = null;
		try {
			obj = in.readObject();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return obj;
	}
}
