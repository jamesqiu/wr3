package wr3.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * <pre>
 * usage:<code>
 *   Datetime.date();
 *   Datetime.time();
 *   Datetime.datetime();
 * </code></pre>
 * @author jamesqiu 2008-11-24
 * @see DatetimeTest
 */
public class Datetime {

	/**
	 * �õ����ڵ�Date
	 * @return
	 */
	public static Calendar today() {
		return Calendar.getInstance();
	}

	/**
	 * get: "2008-11-24", "2010-3-4" alike
	 * @return
	 */
	public static String date() {
		return date(today());
	}

	public static String date(Calendar cal) {
//		return DateFormat.getDateInstance().format(asDate(cal));
		return new SimpleDateFormat("yyyy-MM-dd").format(asDate(cal));
	}

	public static String date(Date date) {
		return date(asCalendar(date));
	}

	/**
	 * �õ�ָ��x��x��x�յ� Datetime, ��ʹ��1-12, ������0-11
	 * @param year
	 * @param month
	 * @param day
	 * @return �磺2010-3-9
	 */
	public static String datetime(int year, int month, int day) {
		return date(calendar(year, month, day));
	}

	/**
	 * get: "20:30:59" alike
	 * @return
	 */
	public static String time() {
		return time(today());
	}

	public static String time(Calendar cal) {
		return DateFormat.getTimeInstance().format(asDate(cal));
	}

	/**
	 * get: "2008-11-24 20:30:59" alike
	 * @return
	 */
	public static String datetime() {
		return datetime(today());
	}

	public static String datetime(Calendar cal) {
//		return DateFormat.getDateTimeInstance().format(asDate(cal));
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(asDate(cal));
	}

	public static String format(Calendar cal,  String pattern) {
		return new SimpleDateFormat(pattern).format(asDate(cal));
	}

	/**
	 * get this year
	 * @return int like 2008
	 */
	public static int year() {
		return today().get(Calendar.YEAR);
	}

	public static int year(Calendar cal) {
		return cal.get(Calendar.YEAR);
	}

	/**
	 * get this month
	 * @return int in [1..12]
	 */
	public static int month() {
		return today().get(Calendar.MONDAY)+1;
	}

	public static int month(Calendar cal) {
		return cal.get(Calendar.MONDAY)+1;
	}

	/**
	 * get this day
	 * @return int in [1..31]
	 */
	public static int day() {
		return today().get(Calendar.DATE);
	}

	public static int day(Calendar cal) {
		return cal.get(Calendar.DATE);
	}

	/**
	 * �õ����µ����һ��
	 * @return
	 */
	public static int lastDay() {

		return lastDay(month());
	}

	/**
	 * �õ�����ĳ�µ����һ��
	 * @param month
	 * @return
	 */
	public static int lastDay(int month) {

		return lastDay(year(), month);
	}

	/**
	 * ȡĳ��ĳ�µ����һ��
	 * @param year
	 * @param month [1..12]
	 * @return
	 */
	public static int lastDay(int year, int month) {

		Calendar cal = calendar(year, month, 1);
		return cal.getActualMaximum(Calendar.DATE);
	}

	/**
	 * �õ����x��x��x�յ�ĳ��
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public static Calendar add(int year, int month, int day) {
		return add(today(), year, month, day);
	}

	public static Calendar add(Calendar cal, int year, int month, int day) {
		cal.add(Calendar.YEAR, year);
		cal.add(Calendar.MONTH, month);
		cal.add(Calendar.HOUR, 24*day);
		return cal;
	}

	/**
	 * �����������ڲ������
	 * @param c0
	 * @param c1
	 * @return
	 */
	public static int days(Calendar c0, Calendar c1) {
		long diff = c1.getTimeInMillis() - c0.getTimeInMillis();
		return (int)(Math.round(diff/(1000.0*60*60*24)));
	}

	/**
	 * Calendar ת��Ϊ Date
	 * @param cal
	 * @return
	 */
	public static Date asDate(Calendar cal) {
		return cal.getTime();
	}

	/**
	 * Date ת��Ϊ Calendar
	 * @param date
	 * @return
	 */
	public static Calendar asCalendar(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	/**
	 * �õ�ָ��x��x��x�յ� Calendar, ��ʹ��1-12, ������0-11
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public static Calendar calendar(int year, int month, int day) {
		return new GregorianCalendar(year, month-1, day);
	}

	/**
	 * �õ�ָ��x��x��x�յ� Date, ��ʹ��1-12, ������0-11
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public static Date date(int year, int month, int day) {
		return asDate(calendar(year, month, day));
	}

	/**
	 * <pre>
	 * ���������(yyyyMMdd)�Ƿ�Ϸ�,
	 * MM [1,12]
	 * dd [1, Max]
	 * </pre>
	 * @param yyyyMMdd �磺19741015��20090101�Ϸ���20090230���Ϸ�
	 * @return
	 */
	public static boolean isDate(String yyyyMMdd) {

		if (yyyyMMdd==null || yyyyMMdd.length()!=8 || !Numberx.isDigit(yyyyMMdd))
			return false;

		int yyyy = Numberx.toInt(yyyyMMdd.substring(0,4), 0);
		int MM = Numberx.toInt(yyyyMMdd.substring(4, 6), 0);
		int dd = Numberx.toInt(yyyyMMdd.substring(6, 8), 0);

		if (MM<1 || MM>12) return false;
		if (dd <0 || dd > Datetime.lastDay(yyyy, MM)) return false;

		return true;
	}

	/**
	 * ������ת��, ���� Lunar
	 */
	public static String lunar(int y, int m, int d) {

		return new Lunar(y, m, d).toString();
	}

	/**
	 * ȡ�������ڼ�����1��1�����������գ�7
	 * @return
	 */
	public static int weekDay() {

		return weekDay(today());
	}

	/**
	 * ȡ����(1/.../7), �ѡ�������1��6��7��ת��Ϊ����һ��1��������7��
	 * �磺weekDay(calendar(2010,2,13)) -> 6
	 * @param cal
	 * @return
	 */
	public static int weekDay(Calendar cal) {

		int d0 = cal.get(Calendar.DAY_OF_WEEK);
		int d1 = d0 - 1;
		if (d1==0) d1 = 7;
		return d1;
	}

	/**
	 * ������"yyyy-MM-dd"��"yy��MM��dd��"�����ַ�������ΪDate��ע�⣺�ɺ��Ի��߲�����ʱ����
	 * @param dateString
	 * @return �����ʽ�����򷵻�null
	 */
	public static Calendar parse(String dateString, String format) {

		if (Stringx.nullity(dateString)) return null;
		try {
			Date date = new SimpleDateFormat(format).parse(dateString);
			return asCalendar(date);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * ������"yyyy-MM-dd"�����ַ�������ΪDate. ע�⣺����ʱ���롣
	 * @param dateString
	 * @return
	 */
	public static Calendar parse(String dateString) {

		return parse(dateString, "yyyy-MM-dd");
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {

		System.out.println(datetime(1974,10,15));
		System.out.println(datetime());
		System.out.println(datetime(add(0,0,280)));

		System.out.println(date(parse("2010-02-13")));
		System.out.println(lunar(2010,2,13));
		System.out.printf("Today - (2012-7-22) = %s ��\n", days(calendar(2012,7,22), today()));
		
		System.out.println(datetime(parse("2011-5-4 12:03:15", "yyyy-MM-dd HH:mm:ss")));
		System.out.println(date(parse("2011-5-4 12:03:15", "yyyy-MM-dd HH:mm:ss")));
		System.out.println(date(parse("2011-5-04")));
		System.out.println(date(parse("2011-05-04")));

	}

}
