package wr3.util;

import java.io.*;

/**
 * @todo: jdk1.5+有个更好的 java.lang.ProcessBuilder 类，为进程执行提供了更多的控制 （2012-10-10）
 * 在操作系统单独进程（Process）中执行命令，获取输出
 * Runtime process execute process.
 * windows下，cmd的命令如：ver、type等，可用：
 * 	"cmd /c ver"
 * 需要标准输入的(不是需要参数的)，例如cat，date：
 * 	java com.webreport.util.ProcessUtil "cmd /c date" 2008-09-17
 * @author james 2008-9-16
 *
 */
public class Exec {

	/**
	 * 执行*不需要*标准输入阻塞的命令
	 * @param cmd
	 * @return
	 */
	public static String exec(String cmd) {

		return exec (cmd, null);
	}

	/**
	 * 执行需阻塞标准输入的命令，如："cmd /c date", cat, vi
	 * @param cmd
	 * @param args
	 * @return
	 */
	public static String exec(String cmd, String args) {

		if (Stringx.nullity(cmd)) return null;

		Process child = null;
		int rt = -1;
		try {
			child = Runtime.getRuntime().exec(cmd);
		} catch (IOException e1) {
			e1.printStackTrace();
			return e1.toString();  // 碰到没有该命令则返回错误
		}

		// Sending Input to a Command。有些命令没有输入会阻塞，例如vi, cat, date
		if (!Stringx.nullity(args)) {
			OutputStream out = child.getOutputStream();	// 接收标准输入
			try {
				out.write(args.getBytes());
				out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		// Reading Output from a Command
        InputStream in = child.getInputStream();	// 接收标准输出
        byte[] bytes = new byte[1024];
        StringBuffer sb = new StringBuffer ();
        int c;
        try {
			while ((c = in.read(bytes)) != -1) {
			    sb.append(new String (bytes, 0, c, "GBK"));
			}
	        in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Get exit value
		try {
			child.waitFor();	// 本执行线程等待
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		rt = child.exitValue();
		if (rt < 0) {
			System.err.println("ProcessUtil.exec() exit with code: " + rt);
		}

		return sb.toString();
	}

	// ----------------- main() -----------------//
	public static void main(String[] args) {

		if (args.length==0) {
			System.out.println("usage:\n" +
					"  Exec \"cmd /c date/t\"\n" +
					"  Exec cat aaaaaaaa\n" +
					"  Exec \"cmd /c date\" 2008-09-17" );
			return;
		}

		String arg1 = args.length > 1 ? args[1] : null;
		String s = exec(args[0], arg1);
		System.out.print(s);
	}

}
