package test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import wr3.text.TextFile;
import wr3.util.Filex;

/**
 * 测试对utf8文件的读写
 * @see TextFile#processUtf8(String)
 * @see Filex#readUtf8(String), {@link Filex#writeUtf8(String, String)}
 * @author jamesqiu 2009-4-12
 *
 */
public class Utf8Test {

	private static final int READ_BUFFER_SIZE = 1024;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		String filename = args[0];
		test3(filename);
//		test4(filename);
	}

	public static void test4(String filename) {
		
		Filex.write(filename, "cn中文\n客户号\n朱镕基");
		Filex.writeUtf8("utf8-" + filename, "cn中文\n客户号\n朱镕基");
	}
	
	
	public static void test3(String filename) {
		String s = Filex.readUtf8(filename);
		System.out.println(s);
	}
	
	public static void test2(String filename) throws IOException {
		
		FileInputStream fin=new FileInputStream(filename);
		InputStreamReader ins=new InputStreamReader(fin,"utf8");
		BufferedReader reader=new BufferedReader(ins);
		
		StringBuffer sb = new StringBuffer();
		char[] buf = new char[READ_BUFFER_SIZE];
		int n;	
		while((n = reader.read(buf)) > 0) {
			sb.append(buf, 0, n);
		}
		String rt = sb.toString();
		System.out.println(rt);
//		String line=reader.readLine();

	}
	
	public static void test1(String filename) throws IOException {

		StringBuffer cBuffer = new StringBuffer();
		char[] aBuffer = new char[ READ_BUFFER_SIZE ];
		int nLength;
        
		BufferedReader cReader = new BufferedReader (new FileReader (filename));
		
		nLength = cReader.read( aBuffer );

		while ( nLength > 0 ) {
			cBuffer.append( aBuffer, 0, nLength );
			nLength = cReader.read( aBuffer );
		}

		String rt = cBuffer.toString();
		System.out.println(rt);
		cReader.close ();
		
		// in unix system, output chinese
		String encoding = System.getProperty ("file.encoding");
		rt = new String (rt.getBytes (encoding), "UTF-8");
		System.out.println(rt);
		
	}
}
