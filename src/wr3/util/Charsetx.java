package wr3.util;

import java.io.UnsupportedEncodingException;
/**
 * <pre>
 * Charset Util.
 * Stringת�롢urlת�롢ȫ���ת����
 * ���ı���ԭ��˵����
 * 	1) ����̨��ӡ���������ȷ��
 * 	2) ҳ��utf�������������ȷ��
 * webһ��ת�룺
 *   web params ------> controller ------> web page(utf)
 *              iso2gbk            utf2iso
 * </pre>
 */

public class Charsetx {

	/**
	 * <pre>
	 * get the byte array of the str the specified character encoding
	 * ���ַ���str�����enc��bytes[]
	 * </pre>
	 * @param str source string
	 * @param enc encode string
	 * @return bytes of encoded string
	 */
	public static byte[] bytes(String str, String enc) {

		try {
			return (Stringx.nullity(enc)) ? str.getBytes() : str.getBytes (enc);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return str.getBytes();
		}
	} // getByte ()

	/**
	 * <pre>
	 * get the str of the byte array using the specified character encoding
	 * ��byte[]���ݰ�enc������ַ���src
	 * </pre>
	 * @param bt bytes
	 * @param enc encode string
	 * @param string from bytes encoded
	 */
	public static String string(byte[] bt, String enc) {

		try {
			return (Stringx.nullity(enc)) ? new String(bt) : new String (bt, enc);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return new String(bt);
		}
	}

	public final static String SYS = System.getProperty("file.encoding");
	public final static String GBK = "GBK";
	public final static String UTF = "UTF-8";
	public final static String ISO = "ISO8859-1";

	/**
	 * �ж�ϵͳ�Ƿ���������ַ�ϵ��.
	 * @return
	 */
	public static boolean isGB() {
		return 	SYS.equalsIgnoreCase(GBK) ||
				SYS.equalsIgnoreCase("GB2312") ||
				SYS.equalsIgnoreCase("GB18030");
	}

	/**
	 * <pre>
	 * convert string from one encoding to another encoding.
	 * ���ַ���str�����enc0��byte[]����en1�������
	 * </pre>
	 * @param str   source string
	 * @param enc0  source encoding, use system default encoding if "null" or ""
	 * @param enc1  target encoding, use system default encoding if "null" or ""
	 * @return      string with new encoding
	 */
	public static String convert (String str, String enc0, String enc1) {

		byte[] temp = bytes(str, enc0);
		return string(temp, enc1);
	}

	/**
	 * convert String from encode0 to encode1
	 * GBK -> ISO
	 * GBK -> UTF
	 * ISO -> GBK
	 * ISO -> UTF
	 * UTF -> ISO
	 * UTF -> GBK
	 */
	public static String gbk2iso (String s) {

		return convert (s, GBK, ISO);
	}
	public static String gbk2utf (String s) {

		return convert (s, GBK, UTF);
	}
	public static String iso2gbk (String s) {

		return convert (s, ISO, GBK);
	}
	public static String iso2utf (String s) {

		return convert (s, ISO, UTF);
	}
	public static String utf2iso (String s) {

		return convert (s, UTF, ISO);
	}
	public static String utf2gbk (String s) {

		return convert (s, UTF, GBK);
	}

	/**
	 * convert String from sys file encoding to ISO/GBK/UTF
	 */
	public static String sys2iso(String s) {
		return convert(s, SYS, ISO);
	}
	public static String sys2gbk(String s) {
		return convert(s, SYS, GBK);
	}
	public static String sys2utf(String s) {
		return convert(s, SYS, UTF);
	}

	/**
	 * convert by string flag. ���ڶ�ȡ�����ļ�ת�����ã�����ת�롣
	 * @param s
	 * @param flag "gbk2iso | gbk2utf | iso2gbk | iso2utf | utf2iso | utf2gbk | others"
	 * @return tranfer encode with given flag string, do NOT transfer if unknow flag.
	 */
	public static String convertByFlag (String s, String flag) {

		if (flag.equalsIgnoreCase("gbk2iso")) {
			return gbk2iso(s);
		} else if (flag.equalsIgnoreCase("gbk2utf")) {
			return gbk2utf(s);
		} else if (flag.equalsIgnoreCase("iso2gbk")) {
			return iso2gbk(s);
		} else if (flag.equalsIgnoreCase("iso2utf")) {
			return iso2utf(s);
		} else if (flag.equalsIgnoreCase("utf2iso")) {
			return utf2iso(s);
		} else if (flag.equalsIgnoreCase("utf2gbk")) {
			return utf2gbk(s);
		} else {
			return s;
		}
	}

	/**
	 * convert s to string with 6 moded. use for testing.
	 * @param s
	 * @return
	 */
	public static String convertTest (String s) {
		String rt = "convertTest (" + s + ")";
		rt += "\ngbk2utf(): " + gbk2utf(s);
		rt += "\ngbk2iso(): " + gbk2iso(s);
		rt += "\niso2utf(): " + iso2utf(s);
		rt += "\niso2gbk(): " + iso2gbk(s);
		rt += "\nutf2iso(): " + utf2iso(s);
		rt += "\nutf2gbk(): " + utf2gbk(s);
		return rt;
	}

	/**
	 * ��convert����ѡ���Զ�ѡ��ת��������к�"cn����"�ı���ת��
	 * @param s0 ����ԭʼ���߱������ġ�cn���ġ�
	 * @return ������cn���ġ����ַ�������ԭʼ����û���ʺϵı��룩
	 */
	public static String convertAuto(String s0) {
		String s1;
		if (ok(s0)) {
			System.out.println("convert needless:"+s0);
			return s0;
		} else if ( ok(s1=gbk2iso(s0)) ) {
			System.out.println("gbk2iso(): "+s1);
			return s1;
		} else if ( ok(s1=gbk2utf(s0)) ) {
			System.out.println("gbk2utf(): "+s1);
			return s1;
		} else if ( ok(s1=utf2iso(s0)) ) {
			System.out.println("utf2iso(): "+s1);
			return s1;
		} else if ( ok(s1=utf2gbk(s0)) ) {
			System.out.println("utf2gbk(): "+s1);
			return s1;
		} else if ( ok(s1=iso2gbk(s0)) ) {
			System.out.println("iso2gbk(): "+s1);
			return s1;
		} else if ( ok(s1=iso2utf(s0)) ) {
			System.out.println("iso2utf(): "+s1);
			return s1;
		} else {
			System.out.println("convert notfound: "+s0);
			return s0;
		}
	}
	// {@link #convertAuto(String)}
	private static boolean ok(String s) {
		return s.indexOf("cn����") != -1;
	}

	/**
	 * @param s string that contains chinese words or not.
	 * @return Return true if any chinese words in string s.
	 * @see #hasChinese(String)
	 */
	public static boolean isChinese (String s) {

		if (s == null) return false;

		byte[] bs;
		try {
			bs = s.getBytes ("UTF-8");
		} catch (Exception e) {
			e.printStackTrace ();
			return false;
		}

		if (bs == null)	return false;

		boolean rt = false;
		for (int i = 0; i < bs.length; i++) {
			if ((0x80 & bs[i]) != 0) {
				rt = true;
				break;
			}
		}

		return rt;
	} // isChinese ()

	/**
	 * �ж��ַ������Ƿ���������ַ���
	 * {@link #isChinese(String)}����ǿ�汾����ȫ��ת��ǡ�
	 * @param s
	 * @return
	 * @see #isChinese(String)
	 */
	public static boolean hasChinese(String s) {

		return isChinese(SBC2DBC(s));
	}


	/**
	 * ȫ��(SBC case)ת��Ϊ���(DBC case)
	 */
	// ����ȫ���ַ� A3A0 ----------- A3FD,A1AB, ��94���ַ�
	private final static String SBC = "" +		"��������������������������������" +
		"��������������������" +
		"��������������" +
		"���£ãģţƣǣȣɣʣˣ̣ͣΣϣУѣңӣԣգ֣ףأ٣�" +
		"�ۣܣݣޣߣ�" +
		"���������������������������������" +
		"��������";
	// ��Ӧ�İ���ַ� 20 ----------- 7E, ��94���ַ�
	private final static String DBC = "" +		" !\"#$%&'()*+,-./" +
		"0123456789" +
		":;<=>?@" +
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
		"[\\]^_`" +
		"abcdefghijklmnopqrstuvwxyz" +
		"{|}~";

	/**
	 * ����ַ�������ȫ���ַ���ת��Ϊ��ǣ�
	 */
	public static String SBC2DBC (String sbc) {

		if (sbc==null) return null;

		char[] dbc_chars = DBC.toCharArray();
		char[] chars = sbc.toCharArray();
		int index;
		for (int i = 0; i < chars.length; i++) {
			index = SBC.indexOf(chars[i]);
			if ( index != -1) {
				chars[i] = dbc_chars[index];
			}
		}
		return new String(chars);
	}

	//------------------------ main --------------------------//
	public static void main(String argv[]) {
		//--- test SBC2DBC ()
		String s = "Look: ����_~�� �ҵ��ֻ��ţ�������������������������������";
		String s1 = SBC2DBC (s);
		System.out.println(s + "\n" + s1);
	} // main ()

} // class



