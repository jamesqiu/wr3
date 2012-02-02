package wr3.text;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import wr3.util.Charsetx;


/**
 * <pre>
 * 文本文件行处理扫描器，调用LineFilter接口的实现类对每一行进行具体处理。
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
	 * 构造方法，设置处理文本行的filter
	 */
	private TextFile(LineFilter filter) {
		this.filter = filter;
	}
	
	/**
	 * 工厂构造方法，使用更自然：
	 * TextFileFilter.use (lineFilter).process (filename);
	 */
	public static TextFile create(LineFilter filter) {		
		return new TextFile (filter);	
	}
	
	/**
	 * 使用指定的LineFilter处理文本文件(系统编码)的每一行。
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
	 * 使用指定的LineFilter处理文本文件(utf8编码)的每一行。
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
	 * 使用指定的LineFilter,指定的文件编码处理文本文件的每一行。
	 * @param filename
	 * @param enc
	 * @throws IOException
	 */
	public void process(String filename, String enc) throws IOException {
		
		FileInputStream fis = new FileInputStream(filename);
		process(fis, enc);
	}
	
	/**
	 * 使用指定的LineFilter处理InputStream的每一行。
	 * @param is 文件输入字节流
	 */
	public void process(InputStream is) {
		try {
			process(is, Charsetx.SYS);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 使用指定的LineFilter,指定的文件编码处理 InputStream 的每一行。
	 * @param is 文件输入字节流
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

