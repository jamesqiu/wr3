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
	 * ����ϵͳ�����ȡ�ı��ļ�����, wrap {@link #getFileText(String, String)}
	 * @param filename
	 * @return �ı��ļ��ַ���.
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
	 * ����UTF-8�����ȡUTF-8�ı��ļ�����, wrap {@link #getFileText(String, String)}
	 * @param filename
	 * @return �ı��ļ��ַ���.
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
	 * ����ָ�������ȡ�ñ����ı��ļ�������.
	 * usage��
	 *   ������Դ�ж���
	 *   getText(Classx.inputStream(getClass(), "wr3/package.html"), Charsetx.UTF);
	 * </pre>
	 * @param is �ļ�������ClassLoader����Դ����Socket��
	 * @param enc ����ʹ�� Charsetx.UTF, GBK, ISO, SYS
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
	 * �õ��ļ���InputStream
	 * @param filename �ļ��ľ���·��
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
	 * �õ�jar�л�classpath����Դ��InputStream
	 * @param clazz һ����getClass()
	 * @param resname ��Դ����
	 * @return
	 */
	public static InputStream inputStream(Class<?> clazz, String resname) {
				
		if (clazz==null) return null;
		
		return clazz.getResourceAsStream(resname);
	}
	
	/**
	 * �õ���·���µ���Դ�ļ��ľ���·����
	 * ע�⣺jar�ļ������
	 * @param o һ�����this������Ǿ�̬������Stringx, 
	 * 			�� {@link #resource(Class, String)}
	 * @param name ��"/"��ͷΪ����·����"/wr3/web/Head.ftl", ����Ϊ���·����"Head.ftl"
	 * @return
	 */
	public static String resource(Object o, String name) {
		
		if (o==null) return null;
		return resource(o.getClass(), name);
	}
	
	/**
	 * <pre>
	 * �õ���·���µ���Դ�ļ��ľ���·����
	 * ע�⣺
	 *  1���ļ�·���пո�������ַ��������
	 *  2���ļ�·�������ĵ������
	 *  3��jar�ļ������
	 * </pre>
	 * @see #resource(Object, String)
	 * @param clazz һ�����getClass()
	 * @param name ��ʹ�����·��, ��: "Head.ftl", ���߾���·����: "/java/lang.String.class"
	 * @return
	 */
	public static String resource(Class<?> clazz, String name) {
		
		URL url = clazz.getResource(name);
		if (url==null) return null;
		URI uri = url2uri(url);
		String path = uri.getPath();
		// ����jar�ļ������
		if (path==null) { 
			// ���������ַ�������: "%20"���" "
			try {
				path = URLDecoder.decode(url.getPath(), Charsetx.UTF);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return path;
	}
	
	/**
	 * ���� {@link URL#getPath()} �õ���·�����пո�ʱ�س���"%20", 
	 * ��Ҫ�õ�url����ȷ·����Ҫ�ȱ��uri���� {@link URI#getPath()}
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
	 * ��textд���ļ�, �������ļ���ϵͳ����.
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
	 * ��textд���ļ�, �������ļ���utf-8����.
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
	 * ��textд���ļ�, �������ļ���ָ����enc����.
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
	 * File����: �ļ� | Ŀ¼ | ��(������) 
	 * @author jamesqiu 2009-1-15
	 *
	 */
	public enum TYPE { FILE, DIR, NONE };
	/**
	 * �ж�filename��Ŀ¼�����ļ����ǲ�����.
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
	 * �ļ���������Ҳ�ܵõ�.
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
	 * �õ��ļ���Ŀ¼��
	 * @param filename �ļ���Ŀ¼��
	 * @return �ļ������ϼ�Ŀ¼���Ƕ���Ŀ¼�����ϼ�Ŀ¼������Ŀ¼���ر���
	 * 	�����ڻ�ûȨ�޵�Ŀ¼����null��
	 */
	public static String dir(String filename) {

		if (!has(filename)) return null;
		File dir = new File(filename).getParentFile();
		if (dir==null) { // ����Ŀ¼
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
		int sumRead = 0;	// �ۼƶ������ֽ�����Ŀ
		int numRead = 0;	// ÿ�������������ֽ���Ŀ
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
