package wr3.text;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import wr3.util.Charsetx;


/**
 * <pre>
 * �ı��ļ��д���ɨ����������LineFilter�ӿڵ�ʵ�����ÿһ�н��о��崦��
 * usage 1:
 * 		LineFilter lf = new LineSearch("001");
 * 		TextFile ff = TextFile.create(lf);
 * 		ff.process("a.txt");
 * 
 * usage 2:
 * 		LineFilter lf = new LineFilter () {
 * 			public void process (String line) {
 * 				...
 * 			}
 * 		};
 * 		TextFile.create(lf).process("a.txt");
 * </pre>
 * @author jamesqiu 2007-7-29
 */
public class TextFile {

	private LineFilter filter;
	
	/**
	 * ���췽�������ô����ı��е�filter
	 */
	private TextFile(LineFilter filter) {
		this.filter = filter;
	}
	
	/**
	 * �������췽����ʹ�ø���Ȼ��
	 * TextFileFilter.use (lineFilter).process (filename);
	 */
	public static TextFile create(LineFilter filter) {		
		return new TextFile (filter);	
	}
	
	/**
	 * ʹ��ָ����LineFilter�����ı��ļ�(ϵͳ����)��ÿһ�С�
	 * @param filename
	 */
	public void process(String filename) {
		try {
			process(filename, Charsetx.SYS);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ʹ��ָ����LineFilter�����ı��ļ�(utf8����)��ÿһ�С�
	 * @param filename
	 */
	public void processUtf8(String filename) {
		try {
			process(filename, Charsetx.UTF);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ʹ��ָ����LineFilter,ָ�����ļ����봦���ı��ļ���ÿһ�С�
	 * @param filename
	 * @param enc
	 * @throws IOException
	 */
	public void process(String filename, String enc) throws IOException {
		
		FileInputStream fis = new FileInputStream(filename);
		process(fis, enc);
	}
	
	/**
	 * ʹ��ָ����LineFilter����InputStream��ÿһ�С�
	 * @param is �ļ������ֽ���
	 */
	public void process(InputStream is) {
		try {
			process(is, Charsetx.SYS);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ʹ��ָ����LineFilter,ָ�����ļ����봦�� InputStream ��ÿһ�С�
	 * @param is �ļ������ֽ���
	 */
	public void process(InputStream is, String enc) throws IOException {
		
		if (nullFilter()) return;
		// open
		InputStreamReader isr = new InputStreamReader(is, enc);
		BufferedReader reader=new BufferedReader(isr);
		// read
		String line;
		while ((line=reader.readLine()) != null) {
			filter.process(line);
		}		
		// close
		reader.close();
		isr.close();
		is.close();
	}
	
	private boolean nullFilter() {
		if (filter==null) {
			new NullPointerException("LineFilter is null").printStackTrace();
			return true;
		} 
		return false;
	}
}

