package wr3.util;

import java.io.*;

/**
 * @todo: jdk1.5+�и����õ� java.lang.ProcessBuilder �࣬Ϊ����ִ���ṩ�˸���Ŀ��� ��2012-10-10��
 * �ڲ���ϵͳ�������̣�Process����ִ�������ȡ���
 * Runtime process execute process.
 * windows�£�cmd�������磺ver��type�ȣ����ã�
 * 	"cmd /c ver"
 * ��Ҫ��׼�����(������Ҫ������)������cat��date��
 * 	java com.webreport.util.ProcessUtil "cmd /c date" 2008-09-17
 * @author james 2008-9-16
 *
 */
public class Exec {

	/**
	 * ִ��*����Ҫ*��׼��������������
	 * @param cmd
	 * @return
	 */
	public static String exec(String cmd) {

		return exec (cmd, null);
	}

	/**
	 * ִ����������׼���������磺"cmd /c date", cat, vi
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
			return e1.toString();  // ����û�и������򷵻ش���
		}

		// Sending Input to a Command����Щ����û�����������������vi, cat, date
		if (!Stringx.nullity(args)) {
			OutputStream out = child.getOutputStream();	// ���ձ�׼����
			try {
				out.write(args.getBytes());
				out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		// Reading Output from a Command
        InputStream in = child.getInputStream();	// ���ձ�׼���
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
			child.waitFor();	// ��ִ���̵߳ȴ�
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
