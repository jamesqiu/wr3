package test;

import wr3.util.Filex;
/**
 * 测试对于5000个小文件的写入和读取耗时.
 * usage:
 * 	f:\tmp\5kfiles>ru filestest 1> all.txt 2>time.txt
 * 
 * 	f:\tmp\5kfiles>more time.txt
 * 	write time(t1-t0)=7453, read time(t2-t1)=4390
 * 
 * @author jamesqiu 2009-5-27
 *
 */

public class FilesTest {

	final static int N = 5000;

	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		
		
		long t0 = System.currentTimeMillis();
		
		for (int i = 1; i <= N; i++) {
			Filex.write(""+i+".txt", "hello world");
		}
		
		long t1 = System.currentTimeMillis();
		
		for (int i = 1; i <= N; i++) {
			String s = Filex.read(""+i+".txt");
			System.out.println(s + " " + i);
		}
		
		long t2 = System.currentTimeMillis();
		
		System.err.printf("write time(t1-t0)=%d, read time(t2-t1)=%d\n", 
				(t1-t0), (t2-t1));
	}
}
