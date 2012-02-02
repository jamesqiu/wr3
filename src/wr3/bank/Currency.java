package wr3.bank;

import java.text.*;

import wr3.util.CLI;
import wr3.util.Numberx;
import wr3.util.Stringx;

/**
 * <pre>
 * 1) 把小于1万亿的一个double，转换成中文表示的货币字符串;
 * 2) 得到货币金额的易读形式;
 * </pre>
 * @author jamesqiu 2004-12-20, 2009-12-8
 */
public class Currency {

	private final static String[] cn_09 = new String[]{
		"零","壹","贰","叁","肆","伍","陆","柒","捌","玖"
	};

	private final static String[] cn_gsbq = new String[]{
		"","拾","佰","仟"
	};

	private final static String[] cn_ywy = new String[]{
		"元","万","亿"
	};

	private final static String[] cn_zjf = new String[]{
		"整","角","分"
	};

	/**
	 * 将阿拉伯数字串转换为中文货币表示
	 * @param s 阿拉伯数字串, 不符合规范的数字串将返回"".
	 * @return 中文货币表示, 不符合规范的数字串将返回"".
	 */
	public static String asChinese(String s) {
		
		double d = Numberx.toDouble(s, Double.MIN_VALUE);
		
		if (d==Double.MIN_VALUE) return "";

		return asChinese (d);
	}
	
	/**
	 * 将double转换为中文货币表示。
	 * @parm num 货币的数字表示, 必须小于1千亿。
	 * @return 中文货币，如果
	 * 
	 */
	public static String asChinese(double num) {
		
		StringBuffer sb = new StringBuffer ();
		if (num < 0.0) {
			num = -num;
			sb.append('负');
		}
		if (num >= 999999999999.99) {
			System.err.println("CurrencyUtil.java numberToChinese(): 大于1万亿。");
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
		// 亿部分
		if (yiPart != null) {
			sb.append(part2Chinese (yiPart)).append(cn_ywy[2]);
		} 
		// 万部分
		if (wanPart != null) {
			String s = part2Chinese (wanPart);
			sb.append (s);
			if (!s.equals("")) sb.append(cn_ywy[1]);
		} 
		// 元部分
		if (yuanPart != null) {
			sb.append (part2Chinese (yuanPart)).append(cn_ywy[0]);
		} 
		// 角分部分
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
	 * 得到一个数字的易读近似数模式。(只读1个或者2个非0数字)
	 * 1.5, 12.5 --> 2, 13
	 * 123.5 --> 120
	 * 125.5 --> 130
	 * 9234.56 --> 9200
	 * 9254.56 --> 9300
	 * 12345 --> 1.2万
	 * 12545 --> 1.3万
	 * 12340000 --> 1200万
	 * 12540000 --> 1300万
	 * 123456789 --> 1.2亿
	 * 125456789 --> 1.3亿
	 * </pre>
	 */
	public static String about(double n) {
		
		StringBuffer sb = new StringBuffer ();
		
		if (n < 0.0) {
			n = -n;
			sb.append('负');
		}
		long nn = Math.round(n);		// 去除小数点
		int len = ("" + nn).length();	// 几位数
		
		if (len < 3) return sb.append(nn).toString();	// 1位数和2位数直接可读
		
		long n10n = (long) Math.pow(10, len-2);			// 把数字变为只有2位
		long rt = Math.round((n / n10n)) * n10n;
		
		if (rt >= 1000000000000.0) {
			sb.append (Numberx.toString(rt/1000000000000.0))
			.append("万亿");			
		} else if (rt >= 100000000) {
			sb.append (Numberx.toString(rt/100000000.0))
			.append('亿');
		} else if (rt >= 10000) {
			sb.append (Numberx.toString(rt/10000.0))
			.append('万');
		} else {
			sb.append(rt);
		}
		return sb.toString();
	}

	// 4位为1个部分，得到1个部分四位数字(不含亿、万)的中文串
	private static String part2Chinese (String part) {

		if (part.equals("0000")) return "";

		StringBuffer sb = new StringBuffer ();
		int len = part.length();
		boolean needZero = false; // 是否需要加"零"
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
			.set("n", "number", true, "需要转换的金额")
			.set("a", "about", false, "大约数（1个或者两个数字）")
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


