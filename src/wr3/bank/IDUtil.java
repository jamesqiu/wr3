package wr3.bank;

import wr3.util.Charsetx;
import wr3.util.Datetime;
import wr3.util.Numberx;
import wr3.util.Stringx;

/**
 * <pre>
 * �й��������֤
 * PRC. ID Card code 15 to 18
 * �ݴ��Թ����: #format(String)
 *
 * 15id: 532525-780310-002  18id: 532525-19780310-002-1
 *       123456 789abc def        123456 789abcde fgh i
-------------------------------------------------------------
	��ʽ���£�
������(a[i]*W[i]) mod 11 ( i = 2, 3, ..., 18 ) (1)
����"*" ��ʾ�˺�
����i--------��ʾ���֤����ÿһλ����ţ��������������Ϊ18�����Ҳ�Ϊ1��
����a[i]-----��ʾ���֤����� i λ�ϵĺ���
����W[i]-----��ʾ�� i λ�ϵ�Ȩֵ W[i] = 2^(i-1) mod 11   (^ ��ʾ���)
�������㹫ʽ (1) ����Ϊ R
	�����±��ҳ� R ��Ӧ��У���뼴ΪҪ�����֤�����У����C��
����R: 0 1 2 3 4 5 6 7 8 9 10
����C: 1 0 X 9 8 7 6 5 4 3 2
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

	// ת�������еĴ�����Ϣ������������
	private static boolean debug = false;
	public static void debug(boolean d) {
		debug = d;
	}
	/**
	 * <pre>
	 * �ж��Ƿ�Ϸ���15Ϊ���֤���Ϸ�������
	 * 15Ϊ�����֣������պϷ�
	 * <pre>
	 * @param id ���жϵ����֤���룬���Դ��ո�tab��ȫ��
	 */
	public static boolean is15(String id) {

		// ��ʽ����ʹ֮��һ���ݴ���
		id = format(id);

		// ����15λ
		if (id==null || id.length()!=15) {
			debug ("IDUtil.isId15(): len(id) is not 15");
			return false;
		}
		// ����ȫ��������
		if (!Numberx.isDigit(id)) {
			debug ("IDUtil.isId15(): not a valid 15 digit id");
			return false;
		}
		// �����ձ������
		if (!isValidDate(id.substring(6, 12))) {
			debug ("IDUtil.isId15(): error date");
			return false;
		}

		return true;
	}

	/**
	 * <pre>
	 * �ж��Ƿ�ֻȱУ��λ��17λ���֤���Ϸ�������
	 * 17λȫ�������ݣ���7��8λ������"19"����"20"��
	 * </pre>
	 * @param id
	 * @return
	 */
	public static boolean is17(String id) {

		// �����ȫ���ַ������ǣ��п��ַ���ȥ��
		id = Charsetx.SBC2DBC(id);
		id = Stringx.trim(id);

		// 17λ
		if (id==null || id.length()!=17) {
			debug ("IDUtil.isId17(id): len(id) is not 17.");
			return false;
		}
		// ����17λ����������
		if (!Numberx.isDigit (id)) {
			debug ("IDUtil.isId17(id): 17 id is not all digit.");
			return false;
		}
		// ����ں���Χ19xx��20xx
		String century = id.substring(6,8);
		if (! (century.equals("19") || century.equals("20"))) {
			debug ("IDUtil.isId17(id): 7~8 is not 19 or 20.");
			return false;
		}
		// �����պ���
		if (!isValidDate(id.substring(6, 14))) {
			debug("IDUtil.isId17(): error date");
			return false;
		}

		return true;
	}

	/**
	 * <pre>
	 * �ж��Ƿ�Ϸ���18Ϊ���֤���Ϸ�������
	 * 18λ��1~17λȫ�������ݣ���7��8λ������"19"����"20"��
	 * 18λ���������ֻ���X���ұ���У��ϸ�
	 * </pre>
	 * @param id ���жϵ����֤���룬���Դ��ո�tab��ȫ��
	 */
	public static boolean is18(String id) {

		// ��ʽ��
		id = format(id);
		// 18λ
		if (id==null || id.length()!=18) {
			debug ("IDUtil.isId18(id): len(id) is not 18.");
			return false;
		}
		// ǰ17λ����������
		String id17 = to17 (id);
		if (!Numberx.isDigit (id17)) {
			debug ("IDUtil.isId18(id): 1~17 of 18 id is not all digit.");
			return false;
		}
		// ����ں���Χ19xx��20xx
		String century = id.substring(6,8);
		if (! (century.equals("19") || century.equals("20"))) {
			debug ("IDUtil.isId18(id): 7~8 is not 19 or 20.");
			return false;
		}
		// �����պ���
		if (!isValidDate(id.substring(6, 14))) {
			debug("IDUtil.isId18(): error date");
			return false;
		}
		// У��λ�����ֻ���x/X
		char c = id.charAt(17); // last check digit
		if (!Character.isDigit(c) && c != 'x' && c != 'X') {
			debug ("IDUtil.isId18(id): the 18th digit is not [0, X].");
			return false;
		}
		// У��λ��ȷ
		char checkDigit = checkDigit (id17);
		if (checkDigit != id.charAt(17)) {
			debug ("IDUtil.isId18(id): last check digit error.");
			return false;
		}

		return true;
	}

	/**
	 * ������ܣ�ת��Ϊ��׼��ʽ��18λ���֤
	 * @param id ��ת�������֤���룬���Դ��ո�tab��ȫ��15λ����18λ
	 * @return ���ع淶��18λ���֤������null
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
	 * Ϊ����У��λ��17λ���֤����У��λ
	 * @param s17 ȫ�����ֵķ���ҵ��淶��17λ�ַ���
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
	 * �õ����֤���׶���ʽ
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
	 * �ж�һ��6λ����8λ�������ַ����Ƿ�Ϸ���
	 * @param date 6λδд����磺741015��8λд�����19741015
	 * @return
	 */
	private static boolean isValidDate(String date) {
		String yyyyMMdd = date;
		if (date.length()==6) {
			yyyyMMdd = "19" + yyyyMMdd;
		}
		return Datetime.isDate(yyyyMMdd);
	}

	// ��������
	private static String area(String areacode) {
		return Areacode.name(areacode);
	}

	// ��-��-��
	private static String date(String date) {
		StringBuffer sb = new StringBuffer(date);
		sb.insert(4, '-').insert(7, '-');
		return sb.toString();
	}

	// �Ա���/Ů
	private static String sex(String digit) {
		int i = Integer.parseInt(digit);
		return i%2==1 ? "��" : "Ů";
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
	 * ���ַ�����
	 * 1��ȫ��ת��Ϊ��ǣ�
	 * 2��trim() ȥ���м�Ŀո��tab��
	 * 3��ȡǰ15λ����18λ���磺130106501124061*02��13010619600731031X*01
	 * 4��"x" --> "X"
	 * </pre>
	 */
	private static String format (String id) {
		if (id==null) return null;
		// ȫ��ת��Ϊ���
		id = Charsetx.SBC2DBC (id);
		// trim all \t and ""
		id = Stringx.trim(id);
		// ȡǰ15λ����18λ
		if (id.length()>=16 && !Character.isDigit(id.charAt(15))) {
			// �����16Ϊ�������֣���ǰ15λ
			id = id.substring(0, 15);
		} else if (id.length()>18) {
			// �������18λ����ǰ18λ
			id = id.substring(0, 18);
		}
		// �����18λΪ"x"��תΪ"X"
		return id.toUpperCase();
	}

	/**
	 * ����ǰ17λ�õ�У��λ
	 * @param id17 �Ѿ�����Ҫ���ֻȱУ��λ�����֤�ַ���
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
	 * ����һ�����֤�ţ����15λ������18λ�����18λ������У������
	 */
	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("usage: IDUtil 13010420010716271x" +				"\n������" +				"\n  532525810923001 --> 532525198109230018" +				"\n  230121����1021��00�� --> 230121197510210019" +				"\n  11011420100213752 --> 11011420100213752X" +				"\n  511102820125203 --> 511102198201252032");
			return;
		}

		String id = args[0];
		IDUtil.debug(false);
		System.out.println(IDUtil.info(id));
	}
}
