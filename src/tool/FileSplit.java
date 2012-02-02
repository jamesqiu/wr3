package tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <pre>
 * 类似于split命令进行文件切分(统一作为2进制文件处理).
 * (1) API调用:
 * 	import tool.FileSplit;
 * 
 * 	FileSplit.split("大文件.dat", 20*1024*1024); 
 * 	FileSplit.merge("大文件.dat");
 * 
 * (2) 命令行调用:
 *  - 以每文件100M进行切分(缺省为10M)
 * 	java tool.FileSplit filename.zip 104857600
 * 	- 把 filename.zip_1, filename.zip_1, ... 进行合并
 * 	java tool.FileSplit filename.zip
 * 
 * </pre>
 * @author jamesqiu 2009-5-22
 *
 */
public class FileSplit {
	
	private static int BUFFER_SIZE = 10*1024*1024; // 10M bytes 的读取缓存

	/**
	 * 切分一个大文件为多个更小文件(以 filename_1, filename_2, ...命名)
	 * @param filename 要切分的文件名
	 * @param partSize 每个切分文件的大小(字节数, 如: 1K为1*1024, 5M为5*1024*1024)
	 * @throws IOException 
	 */
	public static void split(String filename, int partSize) throws IOException {
		
		if (!fileOk(filename) || ! sizeOk(partSize)) return;
		
		InputStream is = new FileInputStream(filename);
		int bufferSize = (partSize < BUFFER_SIZE) ? partSize : BUFFER_SIZE;
		byte[] bytes = new byte[bufferSize]; 
		int n;			// 每次实际读取的字节数
		int total = 0;	// 累计读取的字节数
		int index = 1;	// 文件后缀编号
		OutputStream os = new FileOutputStream(filename + "_" + index);				
		while ((n = is.read(bytes)) != -1) {
			total += n;
			if (total < partSize) { // 没读够1个切分文件大小, 全写入
				os.write(bytes, 0, n);
			} else { // 读够或者读多bytes了
				os.write(bytes, 0, n-(total-partSize));
				os.close();
				index++;
				os = new FileOutputStream(filename + "_" + index);
				if (total != partSize) { // 若有剩余字节, 写入下一切分文件
					os.write(bytes, n-(total-partSize), total-partSize);
				}
				total -= partSize;
			}
		}
	}
	
	/**
	 * 合并多个小文件(以 filename_1, filename_2, ...命名)为一个大文件.
	 * @param filenamePrefix 切分文件的前缀, 不含 "_"及其后数字标识
	 * @throws IOException 
	 */
	public static void merge(String filenamePrefix) throws IOException {
		
		if(!fileOk(filenamePrefix+"_1")) return;
		
		OutputStream os = new FileOutputStream(filenamePrefix);
		byte[] bytes = new byte[BUFFER_SIZE];
		for (int i = 1; ; i++) {
			File file = new File(filenamePrefix + "_" + i);
			if (!file.exists()) break;
			
			InputStream is = new FileInputStream(file);
			int n;			// 实际读取的字节数
			while ((n = is.read(bytes, 0, BUFFER_SIZE)) != -1) {
				os.write(bytes, 0, n);
				os.flush();
			}
			is.close();
		}
		os.close();
	}
	
	static boolean fileOk(String filename) {
		if (new File(filename).exists()) return true;
		System.err.println("文件不存在: " + filename);
		return false;
	}
	
	static boolean sizeOk(int size) {
		if (size > 0 && size < 2*1024*1024*1024L) return true;
		System.err.println("文件大小设置必须在1至2G之间, 错误值: " + size);
		return false;
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		
		if (args.length == 0) {
			System.out.println("文件切分\n" + 
					"\tusage: FileSplit 文件名 [每个切分文件的字节数,缺省1M]\n" +
					"\tusage: FileSplit -v 文件名(不带_数字标识)");
			return;
		}
		
		if (!args[0].equalsIgnoreCase("-v")) {
			// 切分
			String filename = args[0];
			int partSize = BUFFER_SIZE;
			if (args.length==2) {
				partSize = Integer.parseInt(args[1]);
			}			
			try {
				split(filename, partSize);
			} catch (IOException e) {
				e.printStackTrace();
			}			
		} else {
			// 合并
			String filename = args[1];
			try {
				merge(filename);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
