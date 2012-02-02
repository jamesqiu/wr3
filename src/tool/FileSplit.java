package tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <pre>
 * ������split��������ļ��з�(ͳһ��Ϊ2�����ļ�����).
 * (1) API����:
 * 	import tool.FileSplit;
 * 
 * 	FileSplit.split("���ļ�.dat", 20*1024*1024); 
 * 	FileSplit.merge("���ļ�.dat");
 * 
 * (2) �����е���:
 *  - ��ÿ�ļ�100M�����з�(ȱʡΪ10M)
 * 	java tool.FileSplit filename.zip 104857600
 * 	- �� filename.zip_1, filename.zip_1, ... ���кϲ�
 * 	java tool.FileSplit filename.zip
 * 
 * </pre>
 * @author jamesqiu 2009-5-22
 *
 */
public class FileSplit {
	
	private static int BUFFER_SIZE = 10*1024*1024; // 10M bytes �Ķ�ȡ����

	/**
	 * �з�һ�����ļ�Ϊ�����С�ļ�(�� filename_1, filename_2, ...����)
	 * @param filename Ҫ�зֵ��ļ���
	 * @param partSize ÿ���з��ļ��Ĵ�С(�ֽ���, ��: 1KΪ1*1024, 5MΪ5*1024*1024)
	 * @throws IOException 
	 */
	public static void split(String filename, int partSize) throws IOException {
		
		if (!fileOk(filename) || ! sizeOk(partSize)) return;
		
		InputStream is = new FileInputStream(filename);
		int bufferSize = (partSize < BUFFER_SIZE) ? partSize : BUFFER_SIZE;
		byte[] bytes = new byte[bufferSize]; 
		int n;			// ÿ��ʵ�ʶ�ȡ���ֽ���
		int total = 0;	// �ۼƶ�ȡ���ֽ���
		int index = 1;	// �ļ���׺���
		OutputStream os = new FileOutputStream(filename + "_" + index);				
		while ((n = is.read(bytes)) != -1) {
			total += n;
			if (total < partSize) { // û����1���з��ļ���С, ȫд��
				os.write(bytes, 0, n);
			} else { // �������߶���bytes��
				os.write(bytes, 0, n-(total-partSize));
				os.close();
				index++;
				os = new FileOutputStream(filename + "_" + index);
				if (total != partSize) { // ����ʣ���ֽ�, д����һ�з��ļ�
					os.write(bytes, n-(total-partSize), total-partSize);
				}
				total -= partSize;
			}
		}
	}
	
	/**
	 * �ϲ����С�ļ�(�� filename_1, filename_2, ...����)Ϊһ�����ļ�.
	 * @param filenamePrefix �з��ļ���ǰ׺, ���� "_"��������ֱ�ʶ
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
			int n;			// ʵ�ʶ�ȡ���ֽ���
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
		System.err.println("�ļ�������: " + filename);
		return false;
	}
	
	static boolean sizeOk(int size) {
		if (size > 0 && size < 2*1024*1024*1024L) return true;
		System.err.println("�ļ���С���ñ�����1��2G֮��, ����ֵ: " + size);
		return false;
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		
		if (args.length == 0) {
			System.out.println("�ļ��з�\n" + 
					"\tusage: FileSplit �ļ��� [ÿ���з��ļ����ֽ���,ȱʡ1M]\n" +
					"\tusage: FileSplit -v �ļ���(����_���ֱ�ʶ)");
			return;
		}
		
		if (!args[0].equalsIgnoreCase("-v")) {
			// �з�
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
			// �ϲ�
			String filename = args[1];
			try {
				merge(filename);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
