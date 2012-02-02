package wr3.bank;

import java.text.*;

import wr3.util.CLI;
import wr3.util.Numberx;
import wr3.util.Stringx;

/**
 * <pre>
 * 1) ��С��1���ڵ�һ��double��ת�������ı�ʾ�Ļ����ַ���;
 * 2) �õ����ҽ����׶���ʽ;
 * </pre>
 * @author jamesqiu 2004-12-20, 2009-12-8
 */
public class Currency {

	private final static String[] cn_09 = new String[]{
		"��","Ҽ","��","��","��","��","½","��","��","��"
	};

	private final static String[] cn_gsbq = new String[]{
		"","ʰ","��","Ǫ"
	};

	private final static String[] cn_ywy = new String[]{
		"Ԫ","��","��"
	};

	private final static String[] cn_zjf = new String[]{
		"��","��","��"
	};

	/**
	 * �����������ִ�ת��Ϊ���Ļ��ұ�ʾ
	 * @param s ���������ִ�, �����Ϲ淶�����ִ�������"".
	 * @return ���Ļ��ұ�ʾ, �����Ϲ淶�����ִ�������"".
	 */
	public static String asChinese(String s) {
		
		double d = Numberx.toDouble(s, Double.MIN_VALUE);
		
		if (d==Double.MIN_VALUE) return "";

		return asChinese (d);
	}
	
	/**
	 * ��doubleת��Ϊ���Ļ��ұ�ʾ��
	 * @parm num ���ҵ����ֱ�ʾ, ����С��1ǧ�ڡ�
	 * @return ���Ļ��ң����
	 * 
	 */
	public static String asChinese(double num) {
		
		StringBuffer sb = new StringBuffer ();
		if (num < 0.0) {
			num = -num;
			sb.append('��');
		}
		if (num >= 999999999999.99) {
			System.err.println("CurrencyUtil.java numberToChinese(): ����1���ڡ�");
			sb.append (Numberx.toString(new Double(num)));
			return sb.toString();
		}
		NumberFormat formatter = new DecimalFormat("#,####.00");
		String numStr = formatter.format (num);
//		System.out.println("num=" + numStr);
		String intStr = Stringx.left(numStr, ".");
		String decStr = Stringx.right(numStr, ".");

		String[] intPart = Stringx.split (intStr, ",");
		String yiPart = null;
		String wanPart = null;
		String yuanPart = null;

		switch (intPart.length) {
		case 3:
			yiPart = intPart[0];
			wanPart = intPart[1];
			yuanPart = intPart[2];
			break;
		case 2:
			wanPart = intPart[0];
			yuanPart = intPart[1];
			break;
		case 1:
			yuanPart = intPart[0];
		}
		// �ڲ���
		if (yiPart != null) {
			sb.append(part2Chinese (yiPart)).append(cn_ywy[2]);
		} 
		// �򲿷�
		if (wanPart != null) {
			String s = part2Chinese (wanPart);
			sb.append (s);
			if (!s.equals("")) sb.append(cn_ywy[1]);
		} 
		// Ԫ����
		if (yuanPart != null) {
			sb.append (part2Chinese (yuanPart)).append(cn_ywy[0]);
		} 
		// �Ƿֲ���
		if (decStr.equals("00")) {
			if (!sb.toString().equals("")) sb.append (cn_zjf[0]); 
		} else {
			int n0 = new Integer ("" + decStr.charAt(0)).intValue();
			int n1 = new Integer ("" + decStr.charAt(1)).intValue();
			if (n0 != 0) sb.append(cn_09[n0]).append(cn_zjf[1]);
			if (n1 != 0) sb.append(cn_09[n1]).append(cn_zjf[2]);
		}
		String rts = sb.toString();
		return rts;
	} // change ()
	
	/**
	 * <pre>
	 * �õ�һ�����ֵ��׶�������ģʽ��(ֻ��1������2����0����)
	 * 1.5, 12.5 --> 2, 13
	 * 123.5 --> 120
	 * 125.5 --> 130
	 * 9234.56 --> 9200
	 * 9254.56 --> 9300
	 * 12345 --> 1.2��
	 * 12545 --> 1.3��
	 * 12340000 --> 1200��
	 * 12540000 --> 1300��
	 * 123456789 --> 1.2��
	 * 125456789 --> 1.3��
	 * </pre>
	 */
	public static String about(double n) {
		
		StringBuffer sb = new StringBuffer ();
		
		if (n < 0.0) {
			n = -n;
			sb.append('��');
		}
		long nn = Math.round(n);		// ȥ��С����
		int len = ("" + nn).length();	// ��λ��
		
		if (len < 3) return sb.append(nn).toString();	// 1λ����2λ��ֱ�ӿɶ�
		
		long n10n = (long) Math.pow(10, len-2);			// �����ֱ�Ϊֻ��2λ
		long rt = Math.round((n / n10n)) * n10n;
		
		if (rt >= 1000000000000.0) {
			sb.append (Numberx.toString(rt/1000000000000.0))
			.append("����");			
		} else if (rt >= 100000000) {
			sb.append (Numberx.toString(rt/100000000.0))
			.append('��');
		} else if (rt >= 10000) {
			sb.append (Numberx.toString(rt/10000.0))
			.append('��');
		} else {
			sb.append(rt);
		}
		return sb.toString();
	}

	// 4λΪ1�����֣��õ�1��������λ����(�����ڡ���)�����Ĵ�
	private static String part2Chinese (String part) {

		if (part.equals("0000")) return "";

		StringBuffer sb = new StringBuffer ();
		int len = part.length();
		boolean needZero = false; // �Ƿ���Ҫ��"��"
		for (int i = 0; i < len; i++) {
			int n = new Integer ("" + part.charAt(len-i-1)).intValue();
			if (n == 0) {
				if (needZero) sb.insert(0,  cn_09[0]);
				needZero = false;
			} else {
				sb.insert (0,cn_gsbq[i]);
				sb.insert (0, cn_09[n]);
				needZero = true;
			}
		} // for
		String rt = sb.toString ();
		return rt;
	}

	//----------------- main() -----------------//
	public static void main(String argv[]) {
		
		CLI cli = new CLI ()
			.set("n", "number", true, "��Ҫת���Ľ��")
			.set("a", "about", false, "��Լ����1�������������֣�")
			.parse(argv);
		
		if (argv.length==0) {
			cli.help("CurrencyUtil -n 12345.06 [-a]");	
		}
		
		if (cli.has("n")) {
			String s = cli.get("n");
			String rt = "";
			if (!cli.has("a")) {
				rt = asChinese(s);
			} else {
				rt = about(Numberx.toDouble(s, -1));				 
			}
			System.out.println(rt);
		}
	}

}


