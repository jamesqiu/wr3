package wr3.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * ����ת�������������������գ��õ���
 * 1�����������գ���Сд��ʾ������ɡ���Ф�����գ�
 * 2���������գ�
 * 
 * �㷨����:
 * ��ĳһ���ض���ʱ�����Ƚ�,����������,
 * �ٸ����´���С������Щ����,�����ũ����������������.
 * </pre>
 * 
 * @see <a
 *      href="http://www.blogjava.net/soddabao/archive/2007/01/04/91729.html">ref</a>
 * @author jamesqiu 2010-2-19
 */
public class Lunar {

	// ����������
	private int year0, month0, day0;
	
	// ũ��������
	private int year, month, day;
	
	private boolean leap;
	final static String chineseNumber[] = { "һ", "��", "��", "��", "��", "��", "��",
			"��", "��", "ʮ", "ʮһ", "ʮ��" };
	final static SimpleDateFormat chineseDateFormat = new SimpleDateFormat(
			"yyyy��MM��dd��");
	final static long[] lunarInfo = new long[] { 0x04bd8, 0x04ae0, 0x0a570,
			0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
			0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0,
			0x0ada2, 0x095b0, 0x14977, 0x04970, 0x0a4b0, 0x0b4b5, 0x06a50,
			0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970, 0x06566,
			0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0,
			0x1c8d7, 0x0c950, 0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4,
			0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, 0x06ca0, 0x0b550,
			0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950,
			0x06aa0, 0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260,
			0x0f263, 0x0d950, 0x05b57, 0x056a0, 0x096d0, 0x04dd5, 0x04ad0,
			0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6,
			0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40,
			0x0af46, 0x0ab60, 0x09570, 0x04af5, 0x04970, 0x064b0, 0x074a3,
			0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0, 0x0c960,
			0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0,
			0x092d0, 0x0cab5, 0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9,
			0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, 0x07954, 0x06aa0,
			0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65,
			0x0d530, 0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0,
			0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, 0x0b5a0, 0x056d0, 0x055b2,
			0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0 };

	// ���պͼ�����
	private static Map<String,String> festival =new  HashMap<String,String>();
	static {
		festival.put("1-1","Ԫ��");
		festival.put("3-8","��Ů��");
		festival.put("5-1","�Ͷ���");
		festival.put("5-4","�����");
		festival.put("6-1","��ͯ��");
		festival.put("7-1","������");
		festival.put("8-1","������");
		festival.put("9-10","��ʦ��");
		festival.put("10-1","�����");
		festival.put("12-25","ʥ����");
	}

	// ũ������
	private static Map<String,String>  chinaFestival =new  HashMap<String,String>();
	static {
		chinaFestival.put("1-1","����");
		chinaFestival.put("1-2","�������");
		chinaFestival.put("1-3","�������");
		chinaFestival.put("1-15","Ԫ����");
		chinaFestival.put("2-2","��̧ͷ");
		chinaFestival.put("5-5","�����");
		chinaFestival.put("7-7","��Ϧ");
		chinaFestival.put("8-15","�����");
		chinaFestival.put("9-9","������");
		chinaFestival.put("12-8","���˽�");
		chinaFestival.put("12-30","��Ϧ");
	}
	
	// ====== ����ũ�� y���������
	private int yearDays(int y) {
		int i, sum = 348;
		for (i = 0x8000; i > 0x8; i >>= 1) {
			if ((lunarInfo[y - 1900] & i) != 0)
				sum += 1;
		}
		return (sum + leapDays(y));
	}

	// ====== ����ũ�� y�����µ�����
	private int leapDays(int y) {
		if (leapMonth(y) != 0) {
			if ((lunarInfo[y - 1900] & 0x10000) != 0)
				return 30;
			else
				return 29;
		} else
			return 0;
	}

	// ====== ����ũ�� y�����ĸ��� 1-12 , û�򴫻� 0
	private int leapMonth(int y) {
		return (int) (lunarInfo[y - 1900] & 0xf);
	}

	// ====== ����ũ�� y��m�µ�������
	private int monthDays(int y, int m) {
		if ((lunarInfo[y - 1900] & (0x10000 >> m)) == 0)
			return 29;
		else
			return 30;
	}

	// ====== ���� ���յ�offset ���ظ�֧, 0=����
	private String cycle60(int num) {
		final String[] Gan = new String[] { "��", "��", "��", "��", "��", "��", "��",
				"��", "��", "��" };
		final String[] Zhi = new String[] { "��", "��", "��", "î", "��", "��", "��",
				"δ", "��", "��", "��", "��" };
		return (Gan[num % 10] + Zhi[num % 12]);
	}

	/**
	 * �õ����硰��һ������إ��������ئʮ"�ַ���
	 * 
	 * @param day
	 * @return
	 */
	private String chinaDay(int day) {

		// String chineseTen[] = { "��", "ʮ", "إ", "ئ" };
		String chineseTen[] = { "��", "ʮ", "إ", "��" };
		int n = day % 10 == 0 ? 9 : day % 10 - 1;
		if (day > 30)
			return "";
		if (day == 10)
			return "��ʮ";
		else
			return chineseTen[day / 10] + chineseNumber[n];
	}

	@SuppressWarnings("unused")
	private Lunar() {}
	
	/**
	 * �ù������ڳ�ʼ��
	 * 
	 * @param year0
	 *            �����꣬��1900��2010
	 * @param month0
	 *            �����£���1��2��������12
	 * @param day0
	 *            �����գ���1��������31
	 */
	public Lunar(int year0, int month0, int day0) {
	
		this(Datetime.calendar(year0, month0, day0));
		
		this.year0 = year0;
		this.month0 = month0;
		this.day0 = day0;
	}

	/**
	 * ����y��m��d�ն�Ӧ��ũ��. yearCyl3:ũ������1864������� ? monCyl4:��1900��1��31������,������
	 * dayCyl5:��1900��1��31����������,�ټ�40 ?
	 * 
	 * @param cal
	 *            ��������
	 * @return
	 */
	public Lunar(Calendar cal) {
	
		this.year0 = Datetime.year(cal);
		this.month0 = Datetime.month(cal);
		this.day0 = Datetime.day(cal);
		
		int monCyl;
		int leapMonth = 0;
		Date baseDate = null;
		try {
			baseDate = chineseDateFormat.parse("1900��1��31��");
		} catch (ParseException e) {
			e.printStackTrace(); // To change body of catch statement use
			// Options | File Templates.
		}
	
		// �����1900��1��31����������
		int offset = (int) ((cal.getTime().getTime() - baseDate.getTime()) / 86400000L);
		monCyl = 14;
	
		// ��offset��ȥÿũ���������
		// ���㵱����ũ���ڼ���
		// i���ս����ũ�������
		// offset�ǵ���ĵڼ���
		int iYear, daysOfYear = 0;
		for (iYear = 1900; iYear < 2050 && offset > 0; iYear++) {
			daysOfYear = yearDays(iYear);
			offset -= daysOfYear;
			monCyl += 12;
		}
		if (offset < 0) {
			offset += daysOfYear;
			iYear--;
			monCyl -= 12;
		}
		// ũ�����
		year = iYear;
	
		leapMonth = leapMonth(iYear); // ���ĸ���,1-12
		leap = false;
	
		// �õ��������offset,�����ȥÿ�£�ũ��������������������Ǳ��µĵڼ���
		int iMonth, daysOfMonth = 0;
		for (iMonth = 1; iMonth < 13 && offset > 0; iMonth++) {
			// ����
			if (leapMonth > 0 && iMonth == (leapMonth + 1) && !leap) {
				--iMonth;
				leap = true;
				daysOfMonth = leapDays(year);
			} else
				daysOfMonth = monthDays(year, iMonth);
	
			offset -= daysOfMonth;
			// �������
			if (leap && iMonth == (leapMonth + 1))
				leap = false;
			if (!leap)
				monCyl++;
		}
		// offsetΪ0ʱ�����Ҹղż�����·������£�ҪУ��
		if (offset == 0 && leapMonth > 0 && iMonth == leapMonth + 1) {
			if (leap) {
				leap = false;
			} else {
				leap = true;
				--iMonth;
				--monCyl;
			}
		}
		// offsetС��0ʱ��ҲҪУ��
		if (offset < 0) {
			offset += daysOfMonth;
			--iMonth;
			--monCyl;
		}
		month = iMonth;
		day = offset + 1;
	}

	/**
	 * return ũ����֧���硰���ӡ�����������
	 */
	public String cycle60() {
		int num = year - 1900 + 36;
		return (cycle60(num));
	}

	/**
	 * @return ũ�������Ф���硰����
	 */
	public String chinaYear() {
		final String[] Animals = new String[] { "��", "ţ", "��", "��", "��", "��",
				"��", "��", "��", "��", "��", "��" };
		return Animals[(year - 4) % 12];
	}

	/**
	 * @return ũ���£��硰����
	 */
	public String chinaMonth() {

		return chineseNumber[month - 1];
	}

	/**
	 * @return ũ���գ��硰��һ������إ��������ئʮ��
	 */
	public String chinaDay() {

		return chinaDay(day);
	}

	/**
	 * @return ũ����
	 */
	public int year() {
		return year;
	}

	/**
	 * @return ũ����
	 */
	public int month() {
		return month;
	}

	/**
	 * @return ũ����
	 */
	public int day() {
		return day;
	}

	/**
	 * ��������
	 * @return
	 */
	public String festival() {
		
		String s = festival.get("" + month0 + "-" + day0);
		return s==null ? "" : s;
	}
	
	/**
	 * ũ������
	 * @return
	 */
	public String chinaFestival() {
		
		String s = chinaFestival.get("" + month + "-" + day);
		return s==null ? "" : s;
	}
	
	/**
	 * @return ��ũ������ţ��ʮ������ʮ��(2009-12-30)��Ϧ��
	 */
	public String lunarString() {
		
		String s1 = Stringx.printf("ũ��%s%s��%s��%s��(ũ%d-%d-%d)%s", cycle60(),
				chinaYear(), chinaMonth(), chinaDay(), year(), month(), day(), 
				chinaFestival());
		return s1;
	}
	/**
	 * �õ����硰��������, ũ�����������ַ���
	 */
	public String toString() {

		String s0 = Stringx.printf("����%s%s", Datetime.datetime(year0, month0, day0), festival());
		return s0 + ", " + lunarString();
	}

	// ----------------- main() -----------------//
	public static void main(String[] args) {

		System.out.println(new Lunar(2003,1,1));
		System.out.println(new Lunar(2010,2,13));
		System.out.println(new Lunar(2010,2,14));
		System.out.println(new Lunar(2010,3,17));
		
		System.out.println("today is: " + new Lunar(Datetime.today()));
	}
}