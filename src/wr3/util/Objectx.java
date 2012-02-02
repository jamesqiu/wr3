package wr3.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.caucho.hessian.io.Hessian2StreamingInput;
import com.caucho.hessian.io.Hessian2StreamingOutput;

/**
 * 进行Object的序列化和反序列化(使用Hessian2)
 * @author jamesqiu 2009-7-7
 *
 */
public class Objectx {

	/**
	 * 把Object序列化成byte[]
	 * @param obj 
	 * @return Hessian2规范的byte[]
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
	 * 把byte[]反序列化为Object
	 * @param bytes Hessian2序列化的byte[]
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
