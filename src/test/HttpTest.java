package test;

import java.io.IOException;

import org.junit.Test;
import org.xml.sax.SAXException;

import wr3.util.Charsetx;
import static org.junit.Assert.*;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * <pre>
 * 连接到远程url，得到返回。
 * 可用于web测试：servlet、filter
 * </pre>
 * @author jamesqiu 2008-11-28
 *
 */
public class HttpTest {
	
	@Test
	public void init() {		
		WebRequest req = new GetMethodWebRequest("http://localhost:8080/wr3/test.jsp");
		assertNotNull(req);
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
	
		String url = "http://localhost:8080/wr3/test.jsp";
		if (args.length==0) {
			System.out.printf("usage: %s %s", HttpTest.class.getName(), url);
		} else {
			url = args[0];
		}
		
		WebRequest req = new GetMethodWebRequest(url);
		WebConversation wc = new WebConversation();
		try {
			WebResponse resp = wc.getResponse(req);
			String s = resp.getText();
			System.out.println(Charsetx.convertAuto(s));
//			System.out.println(s);
			resp.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}
}
