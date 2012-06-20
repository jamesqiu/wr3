package wr3.bank;

import wr3.util.Stringx;

/**
 * 全国组织机构代码，如 00359131-X 00002106-3
 * @author james 2012-6-20
 *
 */
public class OrgID {

	/**
	 * 把字符转换为数字，'0'->0 '9'->9 'A'->10 'Z'->35
	 * @return
	 */
	private static int c2n(char c) {
		int n = -1;
		if (c>='0' && c<='9') {
			n = c - '0';
		} else if (c>='A' && c<='Z') {
			n = c - 'A' + 10;
		}
		return n;
	}
	
	private static boolean is09AZ(char c) {
		int n = c2n(c);
		return (n>=0 && n<=35);
	}
	
	private static boolean isS8(String s8) {
		if (s8==null || s8.length()!=8) return false;
		for (char c : s8.toCharArray()) {
			if (!is09AZ(c)) return false;
		}
		return true;
	}

	private static int[] wi = {3, 7, 9, 10, 5, 8, 4, 2};
	
	private static char c9(String s8) {
		if (!isS8(s8)) return '.';
		int sum = 0;
		for (int i = 0; i < 8; i++) {
			sum += c2n(s8.charAt(i))*wi[i];
		}
		int n = 11 - sum%11;
		return (n==10) ? 'X' : (char)(n+'0');
	}
	
	public static String toid(String s8) {
		if (!isS8(s8)) return null;
		return s8+"-"+c9(s8);
	}
	
	public static boolean isid(String id) {
		String[] s8c9 = Stringx.split(id, "-");
		return 	(s8c9.length==2) &&
				isS8 (s8c9[0]) &&
				s8c9[1].equals(""+c9(s8c9[0]));
	}
	
	public static void main(String[] args) {
		System.out.println(c2n('3'));
		System.out.println(c2n('Z'));
		System.out.println(isid("00002106-3")); // 00359131-X 00002106-3
	}
}
