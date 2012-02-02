package wr3.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * File Utility.
 * String filetext = getText ("/a.xml");
 * String[] getFileList ("/res");
 * String[] getFileList ("/res", ".html");
 * setText (filetext, "/a.xml.copy");
 * 
 * @see TestFileUtil
 */
public class Filex {

	private static final int READ_BUFFER_SIZE = 1024;

	/**
	 * alias of {@link #read(String)}
	 */
	public static String text(String filename) {
		return read(filename); 
	}
	
	/**
	 * 按照系统编码读取文本文件内容, wrap {@link #getFileText(String, String)}
	 * @param filename
	 * @return 文本文件字符串.
	 */
	public static String read(String filename) {
		try {
			return getFileText(filename, Charsetx.SYS);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 按照UTF-8编码读取UTF-8文本文件内容, wrap {@link #getFileText(String, String)}
	 * @param filename
	 * @return 文本文件字符串.
	 */
	public static String readUtf8(String filename) {
		try {
			return getFileText(filename, Charsetx.UTF);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * wrap {@link #getText(InputStream, String)}
	 * @param filename
	 * @param enc
	 * @return
	 * @throws IOException
	 */
	public static String getFileText(String filename, String enc) 
	throws IOException {

		return getText(inputStream(filename), enc);
	}
	
	/**
	 * <pre>
	 * 按照指定编码读取该编码文本文件流内容.
	 * usage：
	 *   从类资源中读：
	 *   getText(Classx.inputStream(getClass(), "wr3/package.html"), Charsetx.UTF);
	 * </pre>
	 * @param is 文件流、或ClassLoader的资源流、Socket流
	 * @param enc 可以使用 Charsetx.UTF, GBK, ISO, SYS
	 * @return
	 * @throws IOException
	 */
	public static String getText(InputStream is, String enc) 
	throws IOException {
		
		// open
		InputStreamReader isr = new InputStreamReader(is, enc);
		BufferedReader reader=new BufferedReader(isr);
		// read
		StringBuffer sb = new StringBuffer();
		char[] buf = new char[READ_BUFFER_SIZE];
		int n;	
		while((n = reader.read(buf)) > 0) {
			sb.append(buf, 0, n);
		}
		String text = sb.toString();
		// close
		reader.close();
		isr.close();
		is.close();
		
		return text;
	}
	
	/**
	 * 得到文件的InputStream
	 * @param filename 文件的绝对路径
	 * @return
	 */
	public static InputStream inputStream(String filename) {
		
		try {
			return new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 得到jar中或classpath中资源的InputStream
	 * @param clazz 一般用getClass()
	 * @param resname 资源名称
	 * @return
	 */
	public static InputStream inputStream(Class<?> clazz, String resname) {
				
		if (clazz==null) return null;
		
		return clazz.getResourceAsStream(resname);
	}
	
	/**
	 * 得到类路径下的资源文件的绝对路径。
	 * 注意：jar文件的情况
	 * @param o 一般可用this，如果是静态对象如Stringx, 
	 * 			用 {@link #resource(Class, String)}
	 * @param name 以"/"开头为绝对路径如"/wr3/web/Head.ftl", 否则为相对路径如"Head.ftl"
	 * @return
	 */
	public static String resource(Object o, String name) {
		
		if (o==null) return null;
		return resource(o.getClass(), name);
	}
	
	/**
	 * <pre>
	 * 得到类路径下的资源文件的绝对路径。
	 * 注意：
	 *  1、文件路径有空格等特殊字符的情况；
	 *  2、文件路径有中文的情况；
	 *  3、jar文件的情况
	 * </pre>
	 * @see #resource(Object, String)
	 * @param clazz 一般可用getClass()
	 * @param name 可使用相对路径, 如: "Head.ftl", 或者绝对路径如: "/java/lang.String.class"
	 * @return
	 */
	public static String resource(Class<?> clazz, String name) {
		
		URL url = clazz.getResource(name);
		if (url==null) return null;
		URI uri = url2uri(url);
		String path = uri.getPath();
		// 处理jar文件的情况
		if (path==null) { 
			// 处理特殊字符，把如: "%20"变成" "
			try {
				path = URLDecoder.decode(url.getPath(), Charsetx.UTF);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return path;
	}
	
	/**
	 * 由于 {@link URL#getPath()} 得到的路径中有空格时回出现"%20", 
	 * 需要得到url的正确路径需要先变成uri，用 {@link URI#getPath()}
	 * @param url
	 * @return
	 */
	public static URI url2uri(URL url) {
		if (url==null) return null;
		try {
			return new URI(url.toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get all file names in given directory.
	 * @param dirname Directory want to scan through files.
	 * @return Names array of all files in the directory.
	 */
	public static String[] list (String dirname) {

		File dir = new File (dirname);

		String[] children = dir.list ();

		if (children == null) {
			return new String[0];
		} 
		
		return children;

	} // getFileList ()

	/**
	 * Get file names with given postfix in given directory.
	 * @param dirname Directory want to scan through files.
	 * @param postfix Postfix of wanted filed.
	 * @return Names array of files with given postfix in the directory.
	 */
	public static String[] list (String dirname, String postfix) {

		File dir = new File (dirname);

		File[] children = dir.listFiles ();

		if (children == null) {
			return new String[0];
		} 
		
		String filename;
		List<String> rt = new ArrayList<String> ();

		for (int i = 0; i < children.length; i++) {
			
			filename = children[i].getName ();
			
			if (children[i].isFile () && 
				filename.toLowerCase().endsWith (postfix)) {

				rt.add (filename);
				
			}
		}

		return (String[]) (rt.toArray (new String[0]));

	} // getFileList ()

	/**
	 * alias of {@link #write(String, String)}
	 */
	public static void text(String filename, String text) {
		write(filename, text);
	}
	
	/**
	 * 把text写入文件, 保存后的文件是系统编码.
	 * @param filename
	 * @param text
	 */
	public static void write(String filename, String text) {
		
		try {
			setFileText(filename, text, Charsetx.SYS);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 把text写入文件, 保存后的文件是utf-8编码.
	 * @param filename
	 * @param text
	 */
	public static void writeUtf8(String filename, String text) {
		
		try {
			setFileText(filename, text, Charsetx.UTF);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 把text写入文件, 保存后的文件是指定的enc编码.
	 * @param filename
	 * @param text
	 * @param enc
	 * @throws IOException
	 */
	public static void setFileText(String filename, String text, String enc)
	throws IOException {
		
		// open
		FileOutputStream fos = new FileOutputStream(filename);
		OutputStreamWriter osw = new OutputStreamWriter(fos, enc);
		BufferedWriter writer = new BufferedWriter(osw);
		// write
		writer.write(text);
		// close
		writer.close();
		osw.close();
		fos.close();
	}

	/**
	 * check if file / directory with given name is exists.
	 * @return true if has the file/dir
	 */
	public static boolean has(String filename) {
		
		if (Stringx.nullity(filename)) return false;
		
		return new File(filename).exists(); 
	}

	/**
	 * File类型: 文件 | 目录 | 空(不存在) 
	 * @author jamesqiu 2009-1-15
	 *
	 */
	public enum TYPE { FILE, DIR, NONE };
	/**
	 * 判断filename是目录还是文件还是不存在.
	 * @param filename
	 * @return
	 */
	public static TYPE type(String filename) {
		
		if(Stringx.nullity(filename)) return TYPE.NONE;
		
		File file = new File(filename);
		if (file.isFile()) return TYPE.FILE;
		if (file.isDirectory()) return TYPE.DIR;
		return TYPE.NONE;
	}
	
	public static boolean isFile(String filename) {
		
		return type(filename)==TYPE.FILE;
	}
	
	public static boolean isDir(String filename) {
		
		return type(filename)==TYPE.DIR;
	}
	
	/**
	 * get absolute and unique path of a file
	 * 文件还不存在也能得到.
	 * @author jamesqiu 2006-11-27
	 */
	public static String fullpath (String filename) {

		if (Stringx.nullity(filename)) return "";
		
		try {
			return (new File (filename)).getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 得到文件父目录。
	 * @param filename 文件或目录。
	 * @return 文件返回上级目录，非顶级目录返回上级目录，顶级目录返回本身；
	 * 	不存在或没权限的目录返回null。
	 */
	public static String dir(String filename) {

		if (!has(filename)) return null;
		File dir = new File(filename).getParentFile();
		if (dir==null) { // 顶级目录
			return filename;
		} else {
			try {
				return dir.getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	/**
	 * get the number of bytes in the file
	 */	
	public static long size (String filename) {
		
		if (!has(filename)) return -1;
		
		return new File (filename).length ();
	}
	
	/**
	 * get 
	 * this method is not so slow, call 10000 times only take 1.4s
	 * jamesqiu 2006-11-28
	 * @return last modified timestamp of a file, return -1 if file not find.
	 */
	public static long timestamp (String filename) {
		
		if (!has(filename)) return -1;

		return new File (filename).lastModified();
	}
	
	/**
	 * reads the part contents of a file [offset, offset+length] into a byte array
	 * @offset file reading start position
	 * @length the number of bytes to read 
	 */
	public static byte[] bytes (String filename, int offset, int length) {
		
		if (!has(filename)) {
			System.err.println("FileUtil.getBytes(...): can't find file " + filename);	
			return null;
		} 
		
		InputStream is = null;
		try {
			is = new FileInputStream (filename);
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
			return null;
		}
		
		byte[] bytes = new byte[length];

		// Read in the bytes
		int sumRead = 0;	// 累计读到的字节总数目
		int numRead = 0;	// 每次真正读到的字节数目
		try {
			
			is.skip (offset);
			
			while (	sumRead < length && 
					(numRead = is.read (bytes, sumRead, length - sumRead)) >= 0) {
				sumRead += numRead;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Close the input stream and return bytes
		try {
			is.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}

		return bytes;
	}
	
	/**
	 * reads the entire contents of a file into a byte array
	 */
	public static byte[] bytes (String filename) {
		
		if (!has(filename)) return new byte[0];
		
		long length = size (filename);

		if (length > Integer.MAX_VALUE) {
			System.err.println("FileUtil.getBytes(.): file is too large.");
		}

		return bytes (filename, 0, (int) length);		
	}

	/**
	 * zip multifiles to a zipfile.
	 * @param filenames source file names array, with full path.
	 * @param zipfile zip file full path. 
	 */
	public static void zip (String[] filenames, String zipfile) {
		if (Stringx.nullity(zipfile)) {
			System.err.println("FileUtil.zip(..): params null.");
			return;
		}
		try {
			zip (filenames, new FileOutputStream(zipfile));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
	} // zip ()
	
	/**
	 * zip multifiles to a outputstream.
	 * @param filenames source file names array, with full path.
	 * @param os OutputStream of zip file stream. 
	 */
	public static void zip (String[] filenames, OutputStream os) {

		if (filenames == null || filenames.length == 0) {
			System.err.println("FileUtil.zip(..): params null.");
			return;
		}
		for (int i = 0; i < filenames.length; i++) {
			if (!has (filenames[i])) {
				System.err.println("FileUtil.zip(..): can't find file " + filenames[i]);
				return;
			}
		}
		
		byte[] buffer = new byte [READ_BUFFER_SIZE];
		
		try {
			ZipOutputStream out = new ZipOutputStream (os);
			
			for (int i = 0; i < filenames.length; i++) {
				FileInputStream in = new FileInputStream(filenames[i]);
				String name = new File (filenames[i]).getName();
				out.putNextEntry(new ZipEntry(name));
				int len;
				while ((len = in.read(buffer)) > 0) {
					out.write(buffer, 0, len);
				}
				out.closeEntry();
				in.close();
			}
			
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * unzip a zipfile to given path.
	 * @param zipfile zip file full path. 
	 * @param outpath full 
	 * @return unziped file names without outpath
	 */
	public static String[] unzip (String zipfile, String outpath) {
		
		String[] rt = new String[0];
		
		if (Stringx.nullity(zipfile) || Stringx.nullity(outpath)) {
			System.err.println("FileUtil.unzip(..): params null.");
			return rt;
		}
		
		byte[] buffer = new byte [READ_BUFFER_SIZE];
		
		List<String> filelist = new ArrayList<String> ();
		try {
			ZipInputStream in = new ZipInputStream(new FileInputStream(zipfile));
			ZipEntry entry;
			while ((entry = in.getNextEntry()) != null) {
				String filename = entry.getName();
				OutputStream out = new FileOutputStream(outpath + filename);
				int len;
				while ((len = in.read(buffer)) > 0) {
					out.write(buffer, 0, len);	
				}
				out.close();
				filelist.add (filename);
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		rt = (String[]) (filelist.toArray(new String[filelist.size()]));
		
		return rt;
		
	} // unzip ()

	public static void main(String[] args) {
//		String[] filenames = new String[]{
//			"a.xls", "a.txt"
//		};
		//FileUtil.zip (filenames, "test.zip");
		//FileUtil.unzip ("test.zip", "zip/");
	}	
} // FileUtil class
