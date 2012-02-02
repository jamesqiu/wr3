package test.wr3.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static wr3.util.Datetime.add;
import static wr3.util.Datetime.calendar;
import static wr3.util.Datetime.date;
import static wr3.util.Datetime.datetime;
import static wr3.util.Datetime.days;
import static wr3.util.Datetime.isDate;
import static wr3.util.Datetime.lastDay;
import static wr3.util.Datetime.month;
import static wr3.util.Datetime.time;
import static wr3.util.Datetime.year;

import java.util.Calendar;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import wr3.util.Datetime;

/**
 * @author jamesqiu 2008-11-24
 *
 */
public class DatetimeTest {

	@SuppressWarnings("unused")
	private void print(Object ... args) {
		for (Object object : args) {
			System.out.println("\t[DatetimeTest] " + object);
		}
	}
	
	@Test
	public void dateTest() {
//		print(	"today is: " + date(),
//				"2008-1-1: " + date(calendar(2008,1,1)),
//				"2008-12-12: " + date(calendar(2008,12,12)),
//				"2008-2-29: " + date(calendar(2008,2,29)));
		assertTrue(date().length() >= "2008-1-1".length());
		assertEquals("2008-1-1", date(calendar(2008,1,1)));
		assertEquals("2008-12-12", date(calendar(2008,12,12)));
		assertEquals("2008-2-29", date(calendar(2008,2,29)));
	}

	@Test
	public void timeTest() {
		String time = Datetime.time();
		assertTrue(time.length() <= "10:15:59".length()); // 9:28:02
		assertTrue(time.indexOf(':')==1 || time.indexOf(':')==2);
		assertTrue(time.lastIndexOf(':')==4 || time.lastIndexOf(':')==5);
//		print("time is: " + time);
	}

	@Test
	public void datetimeTest() {
//		print("now: " + datetime());
		assertEquals(datetime(), date() + " " + time());
	}

	@Test
	public void yearTest() {
//		print(	"this year: " + year(),
//				"+10 year: " + year(add(10,0,0)),
//				"-10 year: " + year(add(-10,0,0)));
		assertEquals(year()+10, year(add(10,0,0)));
		assertEquals(year()-10, year(add(-10,0,0)));
		
	}

	@Test
	public void monthTest() {
//		print(	"this month: " + month(),
//				"+1 month: " + month(add(0,1,0)),
//				"-4 month: " + month(add(0,-4,0)));
		int m_current = month();
		int m_next = month(add(0,1,0));
		if (m_current==12) {
			assertEquals(1, m_next);
		} else {
			assertEquals(m_next, m_current+1);
		}
		assertEquals(month(), month(add(0,12,0)));
	}

	@Test
	public void dayTest() {
//		print(	"this day: " + day(),
//				"+30 day: " + day(add(0,0,30)),
//				"-30 day: " + day(add(0,0,-30)));
	}

	@Test
	public void lastDayTest() {
//		print(	"month days: " + lastDay(),
//				"next month days: " + lastDay(month()+1),
//				"last month days: " + lastDay(month()-1),
//				"2nd month days: " + lastDay(2),
//				"2008-2 days: " + lastDay(2008, 2),
//				"2009-2 days: " + lastDay(2009, 2));
		assertEquals(29, lastDay(2008,2));
		assertEquals(28, lastDay(2009,2));
	}
	
	@Test
	public void testIsDate() {
		
		assertTrue(isDate("20090101"));
		assertTrue(isDate("19741015"));
		assertTrue(isDate("20091231"));
		
		assertFalse(isDate("2009303"));
		assertFalse(isDate("2009-1-1"));
		assertFalse(isDate("20090230"));
	}
	
	@Test
	public void testDays() {
		
		Calendar c0 = calendar(2010, 2, 13);
		Calendar c1 = calendar(2010, 2, 14);
		Calendar c2 = calendar(2010, 6, 13);
		assertEquals(1, days(c0, c1));
		assertEquals(120, days(c0, c2));		
	}

	// ---------------------- main() ----------------------
	public static void main(String[] args) {

		JUnitCore.main(DatetimeTest.class.getName());
	}
}
