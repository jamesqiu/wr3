package wr3.bank;

import wr3.util.Charsetx;
import wr3.util.Datetime;
import wr3.util.Numberx;
import wr3.util.Stringx;

/**
 * <pre>
 * 中国公民身份证
 * PRC. ID Card code 15 to 18
 * 容错性规则见: #format(String)
 *
 * 15id: 532525-780310-002  18id: 532525-19780310-002-1
 *       123456 789abc def        123456 789abcde fgh i
-------------------------------------------------------------
	公式如下：
　　∑(a[i]*W[i]) mod 11 ( i = 2, 3, ..., 18 ) (1)
　　"*" 表示乘号
　　i--------表示身份证号码每一位的序号，从右至左，最左侧为18，最右侧为1。
　　a[i]-----表示身份证号码第 i 位上的号码
　　W[i]-----表示第 i 位上的权值 W[i] = 2^(i-1) mod 11   (^ 表示异或)
　　计算公式 (1) 令结果为 R
	根据下表找出 R 对应的校验码即为要求身份证号码的校验码C。
　　R: 0 1 2 3 4 5 6 7 8 9 10
　　C: 1 0 X 9 8 7 6 5 4 3 2
-------------------------------------------------------------
 *
 * @author jamesqiu 2007-7-25
 */
public class IDUtil {

	// W[i]
	private static int[]  W =
		{7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

	// C
	private static char[] C =
		{'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

	// 转换过程中的错误信息输出（或不输出）
	private static boolean debug = false;
	public static void debug(boolean d) {
		debug = d;
	}
	/**
	 * <pre>
	 * 判断是否合法的15为身份证，合法条件：
	 * 15为的数字；年月日合法
	 * <pre>
	 * @param id 待判断的身份证号码，可以带空格、tab、全角
	 */
	public static boolean is15(String id) {

		// 格式化，使之有一定容错性
		id = format(id);

		// 必须15位
		if (id==null || id.length()!=15) {
			debug ("IDUtil.isId15(): len(id) is not 15");
			return false;
		}
		// 必须全部是数字
		if (!Numberx.isDigit(id)) {
			debug ("IDUtil.isId15(): not a valid 15 digit id");
			return false;
		}
		// 年月日必须合理
		if (!isValidDate(id.substring(6, 12))) {
			debug ("IDUtil.isId15(): error date");
			return false;
		}

		return true;
	}

	/**
	 * <pre>
	 * 判断是否只缺校验位的17位身份证，合法条件：
	 * 17位全部是数据，第7、8位必须是"19"或者"20"，
	 * </pre>
	 * @param id
	 * @return
	 */
	public static boolean is17(String id) {

		// 如果有全角字符，变半角；有空字符，去除
		id = Charsetx.SBC2DBC(id);
		id = Stringx.trim(id);

		// 17位
		if (id==null || id.length()!=17) {
			debug ("IDUtil.isId17(id): len(id) is not 17.");
			return false;
		}
		// 所有17位必须是数字
		if (!Numberx.isDigit (id)) {
			debug ("IDUtil.isId17(id): 17 id is not all digit.");
			return false;
		}
		// 年份在合理范围19xx，20xx
		String century = id.substring(6,8);
		if (! (century.equals("19") || century.equals("20"))) {
			debug ("IDUtil.isId17(id): 7~8 is not 19 or 20.");
			return false;
		}
		// 年月日合理
		if (!isValidDate(id.substring(6, 14))) {
			debug("IDUtil.isId17(): error date");
			return false;
		}

		return true;
	}

	/**
	 * <pre>
	 * 判断是否合法的18为身份证，合法条件：
	 * 18位：1~17位全部是数据，第7、8位必须是"19"或者"20"，
	 * 18位必须是数字或者X，且必须校验合格。
	 * </pre>
	 * @param id 待判断的身份证号码，可以带空格、tab、全角
	 */
	public static boolean is18(String id) {

		// 格式化
		id = format(id);
		// 18位
		if (id==null || id.length()!=18) {
			debug ("IDUtil.isId18(id): len(id) is not 18.");
			return false;
		}
		// 前17位必须是数字
		String id17 = to17 (id);
		if (!Numberx.isDigit (id17)) {
			debug ("IDUtil.isId18(id): 1~17 of 18 id is not all digit.");
			return false;
		}
		// 年份在合理范围19xx，20xx
		String century = id.substring(6,8);
		if (! (century.equals("19") || century.equals("20"))) {
			debug ("IDUtil.isId18(id): 7~8 is not 19 or 20.");
			return false;
		}
		// 年月日合理
		if (!isValidDate(id.substring(6, 14))) {
			debug("IDUtil.isId18(): error date");
			return false;
		}
		// 校验位是数字或者x/X
		char c = id.charAt(17); // last check digit
		if (!Character.isDigit(c) && c != 'x' && c != 'X') {
			debug ("IDUtil.isId18(id): the 18th digit is not [0, X].");
			return false;
		}
		// 校验位正确
		char checkDigit = checkDigit (id17);
		if (checkDigit != id.charAt(17)) {
			debug ("IDUtil.isId18(id): last check digit error.");
			return false;
		}

		return true;
	}

	/**
	 * 如果可能，转换为标准格式的18位身份证
	 * @param id 待转换的身份证号码，可以带空格、tab、全角15位或者18位
	 * @return 返回规范的18位身份证，或者null
	 */
	public static String to18(String id) {

		if (is15(id)) {
			id = format (id);
			String id17 = to17(id);
			return id17 + checkDigit(id17);
		} else if (is18(id)) {
			return format (id);
		} else if (is17(id)) {
			return gen18(id);
		} else {
			debug ("IDUtil.toId18(id): not valid 15 or 18 id.");
			return null;
		}
	}

	/**
	 * 为不含校验位的17位身份证增加校验位
	 * @param s17 全是数字的符合业务规范的17位字符串
	 * @return
	 */
	public static String gen18(String s17) {

		if(Stringx.nullity(s17) || !Numberx.isDigit(s17)) return null;
		return s17 + checkDigit(s17);
	}


	/**
	 * @param id formated 18 bit
	 * @return {area, date, sex}
	 */
	public static String[] infos(String id) {

		if (id==null || id.length()!=18) {
			debug("IDUtil.info(): len(id)!=18.");
			return null;
		}

		String areacode = id.substring (0, 6);
		String date = id.substring (6, 14);
		String sex = id.substring (16,17);
		return new String[]{
			area(areacode),
			date(date),
			sex(sex)
		};
	}

	/**
	 * 得到身份证的易读形式
	 * @param id
	 * @return
	 */
	public static String info(String id) {
		String sid = IDUtil.to18(id);
		String[] infos = infos(sid);
		String out = sid + "\n" + Stringx.join(infos, "\t");
		return out;
	}

	private static void debug(String s) {
		if (debug) System.err.println(s);
	}

	/**
	 * 判断一个6位或者8位年月日字符串是否合法。
	 * @param date 6位未写年份如：741015，8位写年份如19741015
	 * @return
	 */
	private static boolean isValidDate(String date) {
		String yyyyMMdd = date;
		if (date.length()==6) {
			yyyyMMdd = "19" + yyyyMMdd;
		}
		return Datetime.isDate(yyyyMMdd);
	}

	// 地区名称
	private static String area(String areacode) {
		return Areacode.name(areacode);
	}

	// 年-月-日
	private static String date(String date) {
		StringBuffer sb = new StringBuffer(date);
		sb.insert(4, '-').insert(7, '-');
		return sb.toString();
	}

	// 性别：男/女
	private static String sex(String digit) {
		int i = Integer.parseInt(digit);
		return i%2==1 ? "男" : "女";
	}

	/**
	 * @param id 15 or 17 or 18 bit string
	 * @return 17 string without last check digit
	 */
	private static String to17(String id) {
		int len = id.length();
		if (len==15) {
			return id.substring (0, 6) + "19" + id.substring (6);
		} else {
			return id.substring(0, 17);
		}
	}

	/**
	 * <pre>
	 * 把字符串：
	 * 1、全角转换为半角；
	 * 2、trim() 去除中间的空格和tab；
	 * 3、取前15位或者18位，如：130106501124061*02，13010619600731031X*01
	 * 4、"x" --> "X"
	 * </pre>
	 */
	private static String format (String id) {
		if (id==null) return null;
		// 全角转换为半角
		id = Charsetx.SBC2DBC (id);
		// trim all \t and ""
		id = Stringx.trim(id);
		// 取前15位或者18位
		if (id.length()>=16 && !Character.isDigit(id.charAt(15))) {
			// 如果第16为不是数字，截前15位
			id = id.substring(0, 15);
		} else if (id.length()>18) {
			// 如果大于18位，截前18位
			id = id.substring(0, 18);
		}
		// 如果第18位为"x"，转为"X"
		return id.toUpperCase();
	}

	/**
	 * 根据前17位得到校验位
	 * @param id17 已经符合要求的只缺校验位的身份证字符串
	 */
	public static char checkDigit(String id17) {
		int num = 0;
		for (int i = 2; i <= 18; i++) {
			int a = id17.charAt(18-i) - '0';
			num += (a * W[18-i]);
		}
		num = num % 11;

		return C[num];
	}

	//---------------------- main --------------------//
	/**
	 * 输入一个身份证号，如果15位，给出18位，如果18位，给出校验结果。
	 */
	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("usage: IDUtil 13010420010716271x" +				"\n样例：" +				"\n  532525810923001 --> 532525198109230018" +				"\n  230121７５1021　00１ --> 230121197510210019" +				"\n  11011420100213752 --> 11011420100213752X" +				"\n  511102820125203 --> 511102198201252032");
			return;
		}

		String id = args[0];
		IDUtil.debug(false);
		System.out.println(IDUtil.info(id));
	}
}
