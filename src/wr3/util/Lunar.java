package wr3.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * 阳历转阴历，给出阳历年月日，得到：
 * 1）阴历年月日（大小写表示）、天干、生肖、节日；
 * 2）阳历节日；
 * 
 * 算法描述:
 * 和某一个特定的时间作比较,算出差多少天,
 * 再根据月大月小瑞月这些规则,算出是农历的那年那月那日.
 * </pre>
 * 
 * @see <a
 *      href="http://www.blogjava.net/soddabao/archive/2007/01/04/91729.html">ref</a>
 * @author jamesqiu 2010-2-19
 */
public class Lunar {

	// 公历年月日
	private int year0, month0, day0;
	
	// 农历年月日
	private int year, month, day;
	
	private boolean leap;
	final static String chineseNumber[] = { "一", "二", "三", "四", "五", "六", "七",
			"八", "九", "十", "十一", "十二" };
	final static SimpleDateFormat chineseDateFormat = new SimpleDateFormat(
			"yyyy年MM月dd日");
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

	// 节日和纪念日
	private static Map<String,String> festival =new  HashMap<String,String>();
	static {
		festival.put("1-1","元旦");
		festival.put("3-8","妇女节");
		festival.put("5-1","劳动节");
		festival.put("5-4","青年节");
		festival.put("6-1","儿童节");
		festival.put("7-1","建党节");
		festival.put("8-1","建军节");
		festival.put("9-10","教师节");
		festival.put("10-1","国庆节");
		festival.put("12-25","圣诞节");
	}

	// 农历节日
	private static Map<String,String>  chinaFestival =new  HashMap<String,String>();
	static {
		chinaFestival.put("1-1","春节");
		chinaFestival.put("1-2","大年初二");
		chinaFestival.put("1-3","大年初三");
		chinaFestival.put("1-15","元宵节");
		chinaFestival.put("2-2","龙抬头");
		chinaFestival.put("5-5","端午节");
		chinaFestival.put("7-7","七夕");
		chinaFestival.put("8-15","中秋节");
		chinaFestival.put("9-9","重阳节");
		chinaFestival.put("12-8","腊八节");
		chinaFestival.put("12-30","除夕");
	}
	
	// ====== 传回农历 y年的总天数
	private int yearDays(int y) {
		int i, sum = 348;
		for (i = 0x8000; i > 0x8; i >>= 1) {
			if ((lunarInfo[y - 1900] & i) != 0)
				sum += 1;
		}
		return (sum + leapDays(y));
	}

	// ====== 传回农历 y年闰月的天数
	private int leapDays(int y) {
		if (leapMonth(y) != 0) {
			if ((lunarInfo[y - 1900] & 0x10000) != 0)
				return 30;
			else
				return 29;
		} else
			return 0;
	}

	// ====== 传回农历 y年闰哪个月 1-12 , 没闰传回 0
	private int leapMonth(int y) {
		return (int) (lunarInfo[y - 1900] & 0xf);
	}

	// ====== 传回农历 y年m月的总天数
	private int monthDays(int y, int m) {
		if ((lunarInfo[y - 1900] & (0x10000 >> m)) == 0)
			return 29;
		else
			return 30;
	}

	// ====== 传入 月日的offset 传回干支, 0=甲子
	private String cycle60(int num) {
		final String[] Gan = new String[] { "甲", "乙", "丙", "丁", "戊", "己", "庚",
				"辛", "壬", "癸" };
		final String[] Zhi = new String[] { "子", "丑", "寅", "卯", "辰", "巳", "午",
				"未", "申", "酉", "戌", "亥" };
		return (Gan[num % 10] + Zhi[num % 12]);
	}

	/**
	 * 得到形如“初一”、“廿三”，“卅十"字符串
	 * 
	 * @param day
	 * @return
	 */
	private String chinaDay(int day) {

		// String chineseTen[] = { "初", "十", "廿", "卅" };
		String chineseTen[] = { "初", "十", "廿", "三" };
		int n = day % 10 == 0 ? 9 : day % 10 - 1;
		if (day > 30)
			return "";
		if (day == 10)
			return "初十";
		else
			return chineseTen[day / 10] + chineseNumber[n];
	}

	@SuppressWarnings("unused")
	private Lunar() {}
	
	/**
	 * 用公历日期初始化
	 * 
	 * @param year0
	 *            公历年，如1900，2010
	 * @param month0
	 *            公历月，如1，2，……，12
	 * @param day0
	 *            公历日，如1，……，31
	 */
	public Lunar(int year0, int month0, int day0) {
	
		this(Datetime.calendar(year0, month0, day0));
		
		this.year0 = year0;
		this.month0 = month0;
		this.day0 = day0;
	}

	/**
	 * 传出y年m月d日对应的农历. yearCyl3:农历年与1864的相差数 ? monCyl4:从1900年1月31日以来,闰月数
	 * dayCyl5:与1900年1月31日相差的天数,再加40 ?
	 * 
	 * @param cal
	 *            公历日期
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
			baseDate = chineseDateFormat.parse("1900年1月31日");
		} catch (ParseException e) {
			e.printStackTrace(); // To change body of catch statement use
			// Options | File Templates.
		}
	
		// 求出和1900年1月31日相差的天数
		int offset = (int) ((cal.getTime().getTime() - baseDate.getTime()) / 86400000L);
		monCyl = 14;
	
		// 用offset减去每农历年的天数
		// 计算当天是农历第几天
		// i最终结果是农历的年份
		// offset是当年的第几天
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
		// 农历年份
		year = iYear;
	
		leapMonth = leapMonth(iYear); // 闰哪个月,1-12
		leap = false;
	
		// 用当年的天数offset,逐个减去每月（农历）的天数，求出当天是本月的第几天
		int iMonth, daysOfMonth = 0;
		for (iMonth = 1; iMonth < 13 && offset > 0; iMonth++) {
			// 闰月
			if (leapMonth > 0 && iMonth == (leapMonth + 1) && !leap) {
				--iMonth;
				leap = true;
				daysOfMonth = leapDays(year);
			} else
				daysOfMonth = monthDays(year, iMonth);
	
			offset -= daysOfMonth;
			// 解除闰月
			if (leap && iMonth == (leapMonth + 1))
				leap = false;
			if (!leap)
				monCyl++;
		}
		// offset为0时，并且刚才计算的月份是闰月，要校正
		if (offset == 0 && leapMonth > 0 && iMonth == leapMonth + 1) {
			if (leap) {
				leap = false;
			} else {
				leap = true;
				--iMonth;
				--monCyl;
			}
		}
		// offset小于0时，也要校正
		if (offset < 0) {
			offset += daysOfMonth;
			--iMonth;
			--monCyl;
		}
		month = iMonth;
		day = offset + 1;
	}

	/**
	 * return 农历干支，如“甲子”、“庚寅”
	 */
	public String cycle60() {
		int num = year - 1900 + 36;
		return (cycle60(num));
	}

	/**
	 * @return 农历年的生肖，如“虎”
	 */
	public String chinaYear() {
		final String[] Animals = new String[] { "鼠", "牛", "虎", "兔", "龙", "蛇",
				"马", "羊", "猴", "鸡", "狗", "猪" };
		return Animals[(year - 4) % 12];
	}

	/**
	 * @return 农历月，如“二”
	 */
	public String chinaMonth() {

		return chineseNumber[month - 1];
	}

	/**
	 * @return 农历日，如“初一”、“廿三”，“卅十”
	 */
	public String chinaDay() {

		return chinaDay(day);
	}

	/**
	 * @return 农历年
	 */
	public int year() {
		return year;
	}

	/**
	 * @return 农历月
	 */
	public int month() {
		return month;
	}

	/**
	 * @return 农历日
	 */
	public int day() {
		return day;
	}

	/**
	 * 公历节日
	 * @return
	 */
	public String festival() {
		
		String s = festival.get("" + month0 + "-" + day0);
		return s==null ? "" : s;
	}
	
	/**
	 * 农历节日
	 * @return
	 */
	public String chinaFestival() {
		
		String s = chinaFestival.get("" + month + "-" + day);
		return s==null ? "" : s;
	}
	
	/**
	 * @return “农历己丑牛年十二月三十日(2009-12-30)除夕”
	 */
	public String lunarString() {
		
		String s1 = Stringx.printf("农历%s%s年%s月%s日(农%d-%d-%d)%s", cycle60(),
				chinaYear(), chinaMonth(), chinaDay(), year(), month(), day(), 
				chinaFestival());
		return s1;
	}
	/**
	 * 得到形如“公历……, 农历……”的字符串
	 */
	public String toString() {

		String s0 = Stringx.printf("公历%s%s", Datetime.datetime(year0, month0, day0), festival());
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