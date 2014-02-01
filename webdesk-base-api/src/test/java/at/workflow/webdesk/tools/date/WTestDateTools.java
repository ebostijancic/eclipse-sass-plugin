package at.workflow.webdesk.tools.date;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import org.apache.commons.lang.time.DateUtils;

import at.workflow.webdesk.tools.date.DateTools.DatePrecision;
import at.workflow.webdesk.tools.date.DateTools.DateType;

/**
 * Note: fritzberger started this unit test, but did not write class DateTools.
 * So this unit test is incomplete, everyone is invited to add tests here for
 * the methods he coded!
 * 
 * @author fritzberger 09.11.2010, ebostijancic 21.11.2012
 */
public class WTestDateTools extends TestCase {

	private static final String[] ISO_DATETIME_PARSE_PATTERN = new String[] {"yyyy-MM-dd HH:mm:ss"};

	public void testRoundToSeconds()	{
		assertRoundToSeconds(new Date());
		
		Date date;
		
		date = new Date(DateTools.toDate(1992, 8, 9, 12, 5, 0).getTime());
		assertRoundToSeconds(date);
		
		date = new Date(DateTools.toDate(1992, 8, 9, 12, 5, 0).getTime() + 1);
		assertRoundToSeconds(date);
		
		date = new Date(DateTools.toDate(1992, 8, 9, 12, 5, 0).getTime() + 499);
		assertRoundToSeconds(date);
		
		date = new Date(DateTools.toDate(1992, 8, 9, 12, 5, 0).getTime() + 500);
		assertRoundToSeconds(date);
		
		date = new Date(DateTools.toDate(1992, 8, 9, 12, 5, 0).getTime() + 501);
		assertRoundToSeconds(date);
		
		date = new Date(DateTools.toDate(1992, 8, 9, 12, 5, 0).getTime() + 999);
		assertRoundToSeconds(date);
	}

	private void assertRoundToSeconds(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		final int seconds1 = c.get(Calendar.SECOND);
		final int millis1 = c.get(Calendar.MILLISECOND);
		
		final Date secondsPrecise = DateTools.roundToSeconds(date);
		c.setTime(secondsPrecise);
		final int seconds2 = c.get(Calendar.SECOND);
		final int millis2 = c.get(Calendar.MILLISECOND);
		
		assertEquals(0, millis2);
		assertTrue(
				"Either seconds ("+seconds1+", "+seconds2+") must be equal or millis ("+millis1+") be greater equal 500",
				seconds1 == seconds2 || millis1 >= 500);
	}
	
	public void testRoundToMinute()	{
		assertRoundToMinute(new Date());
		Date date;
		
		date = DateTools.toDate(1992, 8, 9, 12, 5, 0);
		assertRoundToMinute(date);
		
		date = DateTools.toDate(1992, 8, 9, 12, 5, 1);
		assertRoundToMinute(date);
		
		date = DateTools.toDate(1992, 8, 9, 12, 5, 29);
		assertRoundToMinute(date);
		
		date = DateTools.toDate(1992, 8, 9, 12, 5, 30);
		assertRoundToMinute(date);
		
		date = DateTools.toDate(1992, 8, 9, 12, 5, 31);
		assertRoundToMinute(date);
		
		date = DateTools.toDate(1992, 8, 9, 12, 5, 59);
		assertRoundToMinute(date);
	}

	private void assertRoundToMinute(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		final int minute1 = c.get(Calendar.MINUTE);
		final int seconds1 = c.get(Calendar.SECOND);
		
		final Date minutePrecise = DateTools.roundToMinute(date);
		c.setTime(minutePrecise);
		final int minute2 = c.get(Calendar.MINUTE);
		final int seconds2 = c.get(Calendar.SECOND);
		
		assertEquals(0, seconds2);
		assertTrue(
				"Either minutes ("+minute1+", "+minute2+") must be equal or seconds ("+seconds1+") be greater equal 30",
				minute1 == minute2 || seconds1 >= 30);
	}
	
	/**
	 * See also
	 * http://stackoverflow.com/questions/4608470/why-dec-31-2010-returns-1-as-week-of-year
	 */
	public void testYearOfWeek()	{
		Date date;
		
		date = DateTools.toDate(2012, 12, 31);
		assertEquals(2013, DateTools.getYearOfWeek(date, Locale.GERMAN));
		assertEquals(2013, DateTools.getYearOfWeek(date, Locale.US));
		
		date = DateTools.toDate(2010, 12, 31);
		assertEquals(2010, DateTools.getYearOfWeek(date, Locale.GERMAN));
		assertEquals(2011, DateTools.getYearOfWeek(date, Locale.US));
	}
	
	/** Tests the DateTools.doesIntervalsOverlap() method. */
	public void testOverlappingIntervals()	{
		final int YEAR = 1987;
		final int MONTH1TO12 = 11;
		final int DAY = 5;
		final int HOUR24 = 12;
		final int MINUTE = 30;
		
		final Date dateFrom1 = DateTools.toDate(YEAR, MONTH1TO12, DAY, HOUR24, MINUTE, 15);
		final Date dateTo1 = DateTools.toDate(YEAR, MONTH1TO12, DAY, HOUR24, MINUTE, 20);
		
		Date dateFrom2, dateTo2;
		
		// assert that same intervals are overlapping
		dateFrom2 = DateTools.toDate(YEAR, MONTH1TO12, DAY, HOUR24, MINUTE, 15);
		dateTo2 = DateTools.toDate(YEAR, MONTH1TO12, DAY, HOUR24, MINUTE, 20);
		assertTrue(DateTools.doesIntervalsOverlap(dateFrom1, dateTo1, dateFrom2, dateTo2));
		
		// assert that an overlapping interval is overlapping
		dateFrom2 = DateTools.toDate(YEAR, MONTH1TO12, DAY, HOUR24, MINUTE, 19);
		dateTo2 = DateTools.toDate(YEAR, MONTH1TO12, DAY, HOUR24, MINUTE, 25);
		assertTrue(DateTools.doesIntervalsOverlap(dateFrom1, dateTo1, dateFrom2, dateTo2));
		
		// assert that a touching interval is NOT overlapping
		dateFrom2 = DateTools.toDate(YEAR, MONTH1TO12, DAY, HOUR24, MINUTE, 20);
		dateTo2 = DateTools.toDate(YEAR, MONTH1TO12, DAY, HOUR24, MINUTE, 25);
		assertFalse(DateTools.doesIntervalsOverlap(dateFrom1, dateTo1, dateFrom2, dateTo2));
		
		dateFrom2 = DateTools.toDate(YEAR, MONTH1TO12, DAY, HOUR24, MINUTE, 10);
		dateTo2 = DateTools.toDate(YEAR, MONTH1TO12, DAY, HOUR24, MINUTE, 15);
		assertFalse(DateTools.doesIntervalsOverlap(dateFrom1, dateTo1, dateFrom2, dateTo2));
	}
	
	/** Tests the DateTools.isOnSameDay() method. */
	public void testIsOnSameDay()	{
		Date today = DateTools.today();
		Date yesterday = DateUtils.addDays(today, -1);
		assertFalse(DateTools.isOnSameDay(today, yesterday));
		
		yesterday = DateTools.yesterday();
		assertFalse(DateTools.isOnSameDay(today, yesterday));
	}
	
	public void testInfinityDate()	{
		final int YEAR = 3000;
		final int MONTH1TO12 = 1;
		final int DAY = 1;
		
		// 1st test
		Date infinite = DateTools.toDate(YEAR, MONTH1TO12, DAY);
		assertEquals(DateTools.INFINITY_TIMEMILLIS, infinite.getTime());
		
		// 2nd test
		final Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, YEAR);
		c.set(Calendar.MONTH, MONTH1TO12 - 1);
		c.set(Calendar.DAY_OF_MONTH, DAY);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		assertEquals(DateTools.INFINITY_TIMEMILLIS, c.getTimeInMillis());
		
		// 3rd test
		assertEquals(DateTools.INFINITY_TIMEMILLIS, new GregorianCalendar(YEAR, MONTH1TO12 - 1, DAY).getTimeInMillis());
	}
	
	public void testYearMonthDayMillis()	{
		final long DAY_MILLIS = 86400000;	// 24L * 60L * 60L * 1000L;
		final long YEAR_MILLIS = 31557600000L;	//Math.round(DAY_MILLIS * 365.25);
		final long MONTH_MILLIS = 2629800000L;	//YEAR_MILLIS / 12L;
		
		assertEquals(DAY_MILLIS, DateTools.DAY_MILLIS);
		assertEquals(MONTH_MILLIS, DateTools.MONTH_MILLIS);
		assertEquals(YEAR_MILLIS, DateTools.YEAR_MILLIS);
	}
	
	public void testToDate()	{
		final int YEAR = 1987;
		final int MONTH1TO12 = 11;
		final int DAY = 5;
		final Calendar c = Calendar.getInstance();
		
		Date date = DateTools.toDate(YEAR, MONTH1TO12, DAY);
		c.setTime(date);
		
		assertEquals(YEAR, c.get(Calendar.YEAR));
		assertEquals(MONTH1TO12, c.get(Calendar.MONTH) + 1);
		assertEquals(DAY, c.get(Calendar.DAY_OF_MONTH));

		final int HOUR24 = 21;
		final int MINUTE = 0;
		final int SECOND = 59;
		
		Date date2 = DateTools.toDate(YEAR, MONTH1TO12, DAY, HOUR24, MINUTE, SECOND);
		c.setTime(date2);
		
		assertEquals(YEAR, c.get(Calendar.YEAR));
		assertEquals(MONTH1TO12, c.get(Calendar.MONTH) + 1);
		assertEquals(DAY, c.get(Calendar.DAY_OF_MONTH));
		assertEquals(HOUR24, c.get(Calendar.HOUR_OF_DAY));
		assertEquals(MINUTE, c.get(Calendar.MINUTE));
		assertEquals(SECOND, c.get(Calendar.SECOND));
	}
	
	public void testDayDifference()	{
		final int YEAR_1 = 1987;
		final int MONTH1TO12_1 = 11;
		final int DAY_1 = 5;
		final Date start = DateTools.toDate(YEAR_1, MONTH1TO12_1, DAY_1, 2, 0);	// 5.11.1987, 02:00
		final int YEAR_2 = 1987;
		final int MONTH1TO12_2 = 11;
		final int DAY_2 = 8;
		final Date end = DateTools.toDate(YEAR_2, MONTH1TO12_2, DAY_2, 0, 59);	// 8.11.1987, 00:59
		
		// following call removes time and leaves just the day
		assertEquals(3, DateTools.getNumberOfDays(start, end));
		
		// following call calculates with millis and rounds
		assertEquals(3, DateTools.dayDifference(start, end));
	}
	
	public void testYearDates() {
		
		Date test = DateTools.toDate(2222, 5, 23);
		Calendar cal = Calendar.getInstance();
		
		Date firstOfYear = DateTools.getFirstDayOfYear(test);
		assertNotNull(firstOfYear);
		cal.setTime(firstOfYear);
		assertEquals(1, cal.get(Calendar.DAY_OF_YEAR));
		assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(0, cal.get(Calendar.MONTH));
		assertEquals(2222, cal.get(Calendar.YEAR));
		
		assertBeginningOfDay(cal);
		
		Date lastOfYear = DateTools.getLastDayOfYear(test);
		assertNotNull(lastOfYear);
		cal.setTime(lastOfYear);
		assertEquals(365, cal.get(Calendar.DAY_OF_YEAR));
		assertEquals(31, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(11, cal.get(Calendar.MONTH));
		assertEquals(2222, cal.get(Calendar.YEAR));
		
		assertBeginningOfDay(cal);

		lastOfYear = DateTools.getLastMomentOfYear(test);
		assertNotNull(lastOfYear);
		cal.setTime(lastOfYear);
		assertEquals(365, cal.get(Calendar.DAY_OF_YEAR));
		assertEquals(31, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(11, cal.get(Calendar.MONTH));
		assertEquals(2222, cal.get(Calendar.YEAR));
		
		assertEndOfDay(cal);
		
		firstOfYear = DateTools.getFirstDayOfPreviousYear(test);
		assertNotNull(firstOfYear);
		cal.setTime(firstOfYear);
		assertEquals(1, cal.get(Calendar.DAY_OF_YEAR));
		assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(0, cal.get(Calendar.MONTH));
		assertEquals(2221, cal.get(Calendar.YEAR));
		
		assertBeginningOfDay(cal);
		
		lastOfYear = DateTools.getLastDayOfPreviousYear(test);
		assertNotNull(lastOfYear);
		cal.setTime(lastOfYear);
		assertEquals(365, cal.get(Calendar.DAY_OF_YEAR));
		assertEquals(31, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(11, cal.get(Calendar.MONTH));
		assertEquals(2221, cal.get(Calendar.YEAR));
		
		assertBeginningOfDay(cal);

		lastOfYear = DateTools.getLastMomentOfPreviousYear(test);
		assertNotNull(lastOfYear);
		cal.setTime(lastOfYear);
		assertEquals(365, cal.get(Calendar.DAY_OF_YEAR));
		assertEquals(31, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(11, cal.get(Calendar.MONTH));
		assertEquals(2221, cal.get(Calendar.YEAR));
		
		assertEndOfDay(cal);
	}

	public void testQuarterDates() {
		
		Date test = DateTools.toDate(2222, 5, 23);
		Calendar cal = Calendar.getInstance();
		
		Date firstOfQuarter = DateTools.getFirstDayOfQuarter(test);
		assertNotNull(firstOfQuarter);
		cal.setTime(firstOfQuarter);
		assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(3, cal.get(Calendar.MONTH));
		assertEquals(2222, cal.get(Calendar.YEAR));

		assertBeginningOfDay(cal);
		
		Date lastOfQuarter = DateTools.getLastDayOfQuarter(test);
		assertNotNull(lastOfQuarter);
		cal.setTime(lastOfQuarter);
		assertEquals(5, cal.get(Calendar.MONTH));
		assertEquals(30, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(2222, cal.get(Calendar.YEAR));
		
		assertBeginningOfDay(cal);

		lastOfQuarter = DateTools.getLastMomentOfQuarter(test);
		assertNotNull(lastOfQuarter);
		cal.setTime(lastOfQuarter);
		assertEquals(5, cal.get(Calendar.MONTH));
		assertEquals(30, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(2222, cal.get(Calendar.YEAR));
		
		assertEndOfDay(cal);
		
		firstOfQuarter = DateTools.getFirstDayOfPreviousQuarter(test);
		assertNotNull(firstOfQuarter);
		cal.setTime(firstOfQuarter);
		assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(0, cal.get(Calendar.MONTH));
		assertEquals(2222, cal.get(Calendar.YEAR));
		
		assertBeginningOfDay(cal);
		
		lastOfQuarter = DateTools.getLastDayOfPreviousQuarter(test);
		assertNotNull(lastOfQuarter);
		cal.setTime(lastOfQuarter);
		assertEquals(2, cal.get(Calendar.MONTH));
		assertEquals(31, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(2222, cal.get(Calendar.YEAR));
		
		assertBeginningOfDay(cal);
		
		lastOfQuarter = DateTools.getLastMomentOfPreviousQuarter(test);
		assertNotNull(lastOfQuarter);
		cal.setTime(lastOfQuarter);
		assertEquals(2, cal.get(Calendar.MONTH));
		assertEquals(31, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(2222, cal.get(Calendar.YEAR));
		
		assertEndOfDay(cal);
	}
	
	public void testMonthsDates() {
		
		Date test = DateTools.toDate(2222, 5, 23);
		Calendar cal = Calendar.getInstance();
		
		Date firstOfMonth = DateTools.getFirstDayOfMonth(test);
		assertNotNull(firstOfMonth);
		cal.setTime(firstOfMonth);
		assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(4, cal.get(Calendar.MONTH));
		assertEquals(2222, cal.get(Calendar.YEAR));
		
		assertBeginningOfDay(cal);
		
		Date lastOfMonth = DateTools.getLastDayOfMonth(test);
		assertNotNull(lastOfMonth);
		cal.setTime(lastOfMonth);
		assertEquals(31, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(4, cal.get(Calendar.MONTH));
		assertEquals(2222, cal.get(Calendar.YEAR));
		
		assertBeginningOfDay(cal);
		
		lastOfMonth = DateTools.getLastMomentOfMonth(test);
		assertNotNull(lastOfMonth);
		cal.setTime(lastOfMonth);
		assertEquals(31, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(4, cal.get(Calendar.MONTH));
		assertEquals(2222, cal.get(Calendar.YEAR));
		
		assertEndOfDay(cal);
		
		firstOfMonth = DateTools.getFirstDayOfPreviousMonth(test);
		assertNotNull(firstOfMonth);
		cal.setTime(firstOfMonth);
		assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(3, cal.get(Calendar.MONTH));
		assertEquals(2222, cal.get(Calendar.YEAR));
		
		assertBeginningOfDay(cal);
		
		lastOfMonth = DateTools.getLastDayOfPreviousMonth(test);
		assertNotNull(lastOfMonth);
		cal.setTime(lastOfMonth);
		assertEquals(30, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(3, cal.get(Calendar.MONTH));
		assertEquals(2222, cal.get(Calendar.YEAR));
		
		assertBeginningOfDay(cal);
		
		lastOfMonth = DateTools.getLastMomentOfPreviousMonth(test);
		assertNotNull(lastOfMonth);
		cal.setTime(lastOfMonth);
		assertEquals(30, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(3, cal.get(Calendar.MONTH));
		assertEquals(2222, cal.get(Calendar.YEAR));
		
		assertEndOfDay(cal);
		
	}
	
	public void testGetMoment() {
		
		Date firstOfYear = DateTools.getFirstDayOfYear(DateTools.now());
		Date fromDateType = DateTools.getMoment(DateType.YEAR_FIRST);
		assertEquals(firstOfYear, fromDateType);
		
		Date lastOfYear = DateTools.getLastMomentOfYear(DateTools.now());
		fromDateType = DateTools.getMoment(DateType.YEAR_LAST);
		assertEquals(lastOfYear, fromDateType);
		
		firstOfYear = DateTools.getFirstDayOfPreviousYear(DateTools.now());
		fromDateType = DateTools.getMoment(DateType.PREV_YEAR_FIRST);
		assertEquals(firstOfYear, fromDateType);
		
		lastOfYear = DateTools.getLastMomentOfPreviousYear(DateTools.now());
		fromDateType = DateTools.getMoment(DateType.PREV_YEAR_LAST);
		assertEquals(lastOfYear, fromDateType);
		
		Date firstOfQuarter = DateTools.getFirstDayOfQuarter(DateTools.now());
		fromDateType = DateTools.getMoment(DateType.QUARTER_FIRST);
		assertEquals(firstOfQuarter, fromDateType);
		
		Date lastOfQuarter = DateTools.getLastMomentOfQuarter(DateTools.now());
		fromDateType = DateTools.getMoment(DateType.QUARTER_LAST);
		assertEquals(lastOfQuarter, fromDateType);
		
		firstOfQuarter = DateTools.getFirstDayOfPreviousQuarter(DateTools.now());
		fromDateType = DateTools.getMoment(DateType.PREV_QUARTER_FIRST);
		assertEquals(firstOfQuarter, fromDateType);
		
		lastOfQuarter = DateTools.getLastMomentOfPreviousQuarter(DateTools.now());
		fromDateType = DateTools.getMoment(DateType.PREV_QUARTER_LAST);
		assertEquals(lastOfQuarter, fromDateType);
		
		Date firstOfMonth = DateTools.getFirstDayOfMonth(DateTools.now());
		fromDateType = DateTools.getMoment(DateType.MONTH_FIRST);
		assertEquals(firstOfMonth, fromDateType);
		
		Date lastOfMonth = DateTools.getLastMomentOfMonth(DateTools.now());
		fromDateType = DateTools.getMoment(DateType.MONTH_LAST);
		assertEquals(lastOfMonth, fromDateType);
		
		firstOfMonth = DateTools.getFirstDayOfPreviousMonth(DateTools.now());
		fromDateType = DateTools.getMoment(DateType.PREV_MONTH_FIRST);
		assertEquals(firstOfMonth, fromDateType);
		
		lastOfMonth = DateTools.getLastMomentOfPreviousMonth(DateTools.now());
		fromDateType = DateTools.getMoment(DateType.PREV_MONTH_LAST);
		assertEquals(lastOfMonth, fromDateType);
	}
	
	public void testGetDay() {
		
		Date firstOfYear = DateTools.getFirstDayOfYear(DateTools.now());
		Date fromDateType = DateTools.getDay(DateType.YEAR_FIRST);
		assertEquals(firstOfYear, fromDateType);
		
		Date lastOfYear = DateTools.getLastDayOfYear(DateTools.now());
		fromDateType = DateTools.getDay(DateType.YEAR_LAST);
		assertEquals(lastOfYear, fromDateType);
		
		firstOfYear = DateTools.getFirstDayOfPreviousYear(DateTools.now());
		fromDateType = DateTools.getDay(DateType.PREV_YEAR_FIRST);
		assertEquals(firstOfYear, fromDateType);
		
		lastOfYear = DateTools.getLastDayOfPreviousYear(DateTools.now());
		fromDateType = DateTools.getDay(DateType.PREV_YEAR_LAST);
		assertEquals(lastOfYear, fromDateType);
		
		Date firstOfQuarter = DateTools.getFirstDayOfQuarter(DateTools.now());
		fromDateType = DateTools.getDay(DateType.QUARTER_FIRST);
		assertEquals(firstOfQuarter, fromDateType);
		
		Date lastOfQuarter = DateTools.getLastDayOfQuarter(DateTools.now());
		fromDateType = DateTools.getDay(DateType.QUARTER_LAST);
		assertEquals(lastOfQuarter, fromDateType);
		
		firstOfQuarter = DateTools.getFirstDayOfPreviousQuarter(DateTools.now());
		fromDateType = DateTools.getDay(DateType.PREV_QUARTER_FIRST);
		assertEquals(firstOfQuarter, fromDateType);
		
		lastOfQuarter = DateTools.getLastDayOfPreviousQuarter(DateTools.now());
		fromDateType = DateTools.getDay(DateType.PREV_QUARTER_LAST);
		assertEquals(lastOfQuarter, fromDateType);
		
		Date firstOfMonth = DateTools.getFirstDayOfMonth(DateTools.now());
		fromDateType = DateTools.getDay(DateType.MONTH_FIRST);
		assertEquals(firstOfMonth, fromDateType);
		
		Date lastOfMonth = DateTools.getLastDayOfMonth(DateTools.now());
		fromDateType = DateTools.getDay(DateType.MONTH_LAST);
		assertEquals(lastOfMonth, fromDateType);
		
		firstOfMonth = DateTools.getFirstDayOfPreviousMonth(DateTools.now());
		fromDateType = DateTools.getDay(DateType.PREV_MONTH_FIRST);
		assertEquals(firstOfMonth, fromDateType);
		
		lastOfMonth = DateTools.getLastDayOfPreviousMonth(DateTools.now());
		fromDateType = DateTools.getDay(DateType.PREV_MONTH_LAST);
		assertEquals(lastOfMonth, fromDateType);
	}
	
	public void testWeekDates() {
		
		Date test = DateTools.toDate(2012, 3, 14);
		Calendar cal = Calendar.getInstance(new Locale("de", "AT"));
		cal.setTime(test);
		
		Date firstOfWeek = DateTools.getFirstDayOfWeek(2012, 11);
		assertNotNull(firstOfWeek);
		cal.setTime(firstOfWeek);
		assertEquals(11, cal.get(Calendar.WEEK_OF_YEAR));
		assertEquals(12, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(2, cal.get(Calendar.MONTH));
		assertEquals(2012, cal.get(Calendar.YEAR));
		
		Date lastOfWeek = DateTools.getLastDayOfWeek(2012, 11);
		assertNotNull(lastOfWeek);
		cal.setTime(lastOfWeek);
		assertEquals(11, cal.get(Calendar.WEEK_OF_YEAR));
		assertEquals(18, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(2, cal.get(Calendar.MONTH));
		assertEquals(2012, cal.get(Calendar.YEAR));
		
	}
	
	public void testMinutePrecision()	{
		final int MINUTE = 10;
		final Date date0 = DateTools.toDate(2012, 6, 28, 12, MINUTE, 0);
		final Date date29 = DateTools.toDate(2012, 6, 28, 12, MINUTE, 29);
		final Date date30 = DateTools.toDate(2012, 6, 28, 12, MINUTE, 30);
		final Date date59 = DateTools.toDate(2012, 6, 28, 12, MINUTE, 59);
		assertEquals(MINUTE, DateTools.getDateTimeField(DateTools.minutesOnly(date0), Calendar.MINUTE));
		assertEquals(MINUTE, DateTools.getDateTimeField(DateTools.minutesOnly(date29), Calendar.MINUTE));
		assertEquals(MINUTE, DateTools.getDateTimeField(DateTools.minutesOnly(date30), Calendar.MINUTE));
		assertEquals(MINUTE, DateTools.getDateTimeField(DateTools.minutesOnly(date59), Calendar.MINUTE));
	}
	
	private void assertBeginningOfDay(Calendar cal) {
		assertEquals(0, cal.get(Calendar.HOUR));
		assertEquals(0, cal.get(Calendar.MINUTE));
		assertEquals(0, cal.get(Calendar.SECOND));
		assertEquals(0, cal.get(Calendar.MILLISECOND));
	}
	
	private void assertEndOfDay(Calendar cal) {
		assertEquals(23, cal.get(Calendar.HOUR_OF_DAY));
		assertEquals(59, cal.get(Calendar.MINUTE));
		assertEquals(59, cal.get(Calendar.SECOND));
		assertEquals(900, cal.get(Calendar.MILLISECOND));
	}
	
	public void testTimeOnly() {
		final Date date1 = DateTools.toDate(2012, 1, 24, 23, 49, 24);
		final Date date2 = DateTools.toDate(1940, 4, 12, 23, 49, 24);
		Date date3 = new Date(date1.getTime());
		Date date4 = new Date(date2.getTime());
		assertEquals(DateTools.timeOnly(date1).getTime(), DateTools.timeOnly(date2).getTime());
		for(int i=0;i<10;i++) {
			date3 = timeOnlyRecursive(date3);
			date4 = timeOnlyRecursive(date4);
		}
		assertEquals(date3.getTime(), date4.getTime());
	}
	
	private Date timeOnlyRecursive(Date date) {
		return DateTools.timeOnly(date);
	}
	
	public void testDateOnly() {
		Date date1 = DateTools.toDate(2012, 1, 30, 12, 43, 45);
		Date date2 = DateTools.toDate(2012, 1, 30, 2, 12, 00);
		
		assertEquals(DateTools.dateOnly(date1).getTime(), DateTools.dateOnly(date2).getTime());
		
		for(int i=0;i<10;i++) {
			date1 = dateOnlyRecursive(date1);
			date2 = dateOnlyRecursive(date2);
		}
		
		assertEquals(date1.getTime(), date2.getTime());
	}
	
	public void testThisMonth()	{
		final Calendar calendar1 = Calendar.getInstance();
		calendar1.set(Calendar.DAY_OF_MONTH, 1);
		final int year1 = calendar1.get(Calendar.YEAR);
		final int month1 = calendar1.get(Calendar.MONTH);
		final int day1 = calendar1.get(Calendar.DAY_OF_MONTH);
		
		final Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(DateTools.thisMonth());
		final int year2 = calendar1.get(Calendar.YEAR);
		final int month2 = calendar1.get(Calendar.MONTH);
		final int day2 = calendar1.get(Calendar.DAY_OF_MONTH);
		
		assertEquals(year1, year2);
		assertEquals(month1, month2);
		assertEquals(day1, day2);
	}
	
	public void testPreviousMonth()	{
		final Calendar calendar1 = Calendar.getInstance();
		calendar1.set(Calendar.DAY_OF_MONTH, 1);
		calendar1.set(Calendar.MONTH, calendar1.get(Calendar.MONTH) - 1);
		final int year1 = calendar1.get(Calendar.YEAR);
		final int month1 = calendar1.get(Calendar.MONTH);
		final int day1 = calendar1.get(Calendar.DAY_OF_MONTH);
		
		final Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(DateTools.previousMonth());
		final int year2 = calendar1.get(Calendar.YEAR);
		final int month2 = calendar1.get(Calendar.MONTH);
		final int day2 = calendar1.get(Calendar.DAY_OF_MONTH);
		
		assertEquals(year1, year2);
		assertEquals(month1, month2);
		assertEquals(day1, day2);
	}
	
	public void testDatesAreEqual() {
		
		assertTrue(DateTools.datesAreEqual(null, null, null));
		assertFalse(DateTools.datesAreEqual(new Date(), null, null));
		assertFalse(DateTools.datesAreEqual(null, new Date(), null));
		
		Date now = new Date();
		assertTrue(DateTools.datesAreEqual(now, now, null));
		
		Date date1 = DateTools.toDate(2012, 9, 28, 17, 4, 55);
		Date date2 = DateTools.toDate(2012, 9, 28, 17, 4, 55);
		assertTrue(DateTools.datesAreEqual(date1, date2, DatePrecision.SECOND));

		date2 = DateTools.toDate(2012, 9, 28, 17, 4, 11);
		assertFalse(DateTools.datesAreEqual(date1, date2, DatePrecision.SECOND));
		assertTrue(DateTools.datesAreEqual(date1, date2, DatePrecision.MINUTE));
		
		date2 = DateTools.toDate(2012, 9, 28, 17, 9, 11);
		assertFalse(DateTools.datesAreEqual(date1, date2, DatePrecision.MINUTE));
		assertTrue(DateTools.datesAreEqual(date1, date2, DatePrecision.HOUR));
		
		date2 = DateTools.toDate(2012, 9, 28, 2, 9, 11);
		assertFalse(DateTools.datesAreEqual(date1, date2, DatePrecision.HOUR));
		assertTrue(DateTools.datesAreEqual(date1, date2, DatePrecision.DAY));
		
		date2 = DateTools.toDate(2012, 9, 2, 2, 9, 11);
		assertFalse(DateTools.datesAreEqual(date1, date2, DatePrecision.DAY));
		assertTrue(DateTools.datesAreEqual(date1, date2, DatePrecision.MONTH));
		
		date2 = DateTools.toDate(2012, 5, 2, 2, 9, 11);
		assertFalse(DateTools.datesAreEqual(date1, date2, DatePrecision.MONTH));
		assertTrue(DateTools.datesAreEqual(date1, date2, DatePrecision.YEAR));
		
		date2 = DateTools.toDate(2013, 5, 2, 2, 9, 11);
		assertFalse(DateTools.datesAreEqual(date1, date2, DatePrecision.YEAR));
	}

	public void testGetDaysInInterval() {
		
		Date start = DateTools.toDate(2012, 11, 21);
		Date end = DateTools.toDate(2012, 11, 30);
		
		assertEquals(10, DateTools.getDaysInInterval(start, end, true, true).size());
		assertEquals(8, DateTools.getDaysInInterval(start, end, false, false).size());
		assertEquals(9, DateTools.getDaysInInterval(start, end, true, false).size());
		assertEquals(9, DateTools.getDaysInInterval(start, end, false, true).size());
		
		List<Date> days = DateTools.getDaysInInterval(start, end, true, true);
		assertTrue(days.contains(DateTools.dateOnly(DateTools.toDate(2012, 11, 21))));
		assertTrue(days.contains(DateTools.dateOnly(DateTools.toDate(2012, 11, 22))));
		assertTrue(days.contains(DateTools.dateOnly(DateTools.toDate(2012, 11, 23))));
		assertTrue(days.contains(DateTools.dateOnly(DateTools.toDate(2012, 11, 24))));
		assertTrue(days.contains(DateTools.dateOnly(DateTools.toDate(2012, 11, 25))));
		assertTrue(days.contains(DateTools.dateOnly(DateTools.toDate(2012, 11, 26))));
		assertTrue(days.contains(DateTools.dateOnly(DateTools.toDate(2012, 11, 27))));
		assertTrue(days.contains(DateTools.dateOnly(DateTools.toDate(2012, 11, 28))));
		assertTrue(days.contains(DateTools.dateOnly(DateTools.toDate(2012, 11, 29))));
		assertTrue(days.contains(DateTools.dateOnly(DateTools.toDate(2012, 11, 30))));
		
		days = DateTools.getDaysInInterval(start, end, true, false);
		assertTrue(days.contains(DateTools.dateOnly(DateTools.toDate(2012, 11, 21))));
		assertFalse(days.contains(DateTools.dateOnly(DateTools.toDate(2012, 11, 30))));
		
		days = DateTools.getDaysInInterval(start, end, false, true);
		assertTrue(days.contains(DateTools.dateOnly(DateTools.toDate(2012, 11, 30))));
		assertFalse(days.contains(DateTools.dateOnly(DateTools.toDate(2012, 11, 21))));
		
		days = DateTools.getDaysInInterval(start, end, false, false);
		assertFalse(days.contains(DateTools.dateOnly(DateTools.toDate(2012, 11, 30))));
		assertFalse(days.contains(DateTools.dateOnly(DateTools.toDate(2012, 11, 21))));
		
		// null values always return empty list to avoid npe.
		days = DateTools.getDaysInInterval(start, null, true, true);
		assertTrue(days.isEmpty());	
		
		days = DateTools.getDaysInInterval(null, null, true, true);
		assertTrue(days.isEmpty());
		
		
		// check next year transition.
		start = DateTools.toDate(2012, 12, 25);
		end = DateTools.toDate(2013, 1, 5);
		
		assertEquals(12, DateTools.getDaysInInterval(start, end, true, true).size());
		
		// check leap-year
		start = DateTools.toDate(2012, 2, 27);
		end = DateTools.toDate(2012, 3, 1);
		Date feb29th = DateTools.toDate(2012, 2, 29);
		assertTrue(DateTools.getDaysInInterval(start, end, true, true).contains(feb29th));
		assertEquals(4, DateTools.getDaysInInterval(start, end, true, true).size());
		
		// check start after end constellation
		start = DateTools.toDate(2012, 1, 29);
		end = DateTools.toDate(2012, 1, 30);
		
		assertEquals(start, DateTools.getDaysInInterval(end, start, true, true).get(0));
	}
	
	public void testDatesCompare() {
		
		assertEquals(0, DateTools.datesCompare(null, null, null));
		assertTrue(0 < DateTools.datesCompare(new Date(), null, null));
		assertTrue(0 > DateTools.datesCompare(null, new Date(), null));
		
		Date now = new Date();
		assertEquals(0, DateTools.datesCompare(now, now, null));
		
		Date date1 = DateTools.toDate(2012, 9, 28, 17, 4, 55);
		Date date2 = DateTools.toDate(2012, 9, 28, 17, 4, 55);
		assertEquals(0, DateTools.datesCompare(date1, date2, DatePrecision.SECOND));
		
		date2 = DateTools.toDate(2012, 9, 28, 17, 4, 11);
		assertTrue(0 > DateTools.datesCompare(date2, date1, DatePrecision.SECOND));
		assertTrue(0 < DateTools.datesCompare(date1, date2, DatePrecision.SECOND));
		assertEquals(0, DateTools.datesCompare(date1, date2, DatePrecision.MINUTE));
		
		date2 = DateTools.toDate(2012, 9, 28, 17, 9, 11);
		assertTrue(0 < DateTools.datesCompare(date2, date1, DatePrecision.MINUTE));
		assertTrue(0 > DateTools.datesCompare(date1, date2, DatePrecision.MINUTE));
		assertEquals(0, DateTools.datesCompare(date1, date2, DatePrecision.HOUR));
		
		date2 = DateTools.toDate(2012, 9, 28, 2, 9, 11);
		assertTrue(0 > DateTools.datesCompare(date2, date1, DatePrecision.HOUR));
		assertTrue(0 < DateTools.datesCompare(date1, date2, DatePrecision.HOUR));
		assertEquals(0, DateTools.datesCompare(date1, date2, DatePrecision.DAY));
		
		date2 = DateTools.toDate(2012, 9, 2, 2, 9, 11);
		assertTrue(0 > DateTools.datesCompare(date2, date1, DatePrecision.DAY));
		assertTrue(0 < DateTools.datesCompare(date1, date2, DatePrecision.DAY));
		assertEquals(0, DateTools.datesCompare(date1, date2, DatePrecision.MONTH));
		
		date2 = DateTools.toDate(2012, 5, 2, 2, 9, 11);
		assertTrue(0 > DateTools.datesCompare(date2, date1, DatePrecision.MONTH));
		assertTrue(0 < DateTools.datesCompare(date1, date2, DatePrecision.MONTH));
		assertEquals(0, DateTools.datesCompare(date1, date2, DatePrecision.YEAR));
		
		date2 = DateTools.toDate(2013, 5, 2, 2, 9, 11);
		assertTrue(0 > DateTools.datesCompare(date1, date2, DatePrecision.YEAR));
	}
	
	private Date dateOnlyRecursive(Date date) {
		return DateTools.dateOnly(date);
	}
	
	public void testLastWorkingDay() throws ParseException {
		
		// Wednesday 30.01.2013
		Date date1 = DateUtils.parseDate("2013-01-30 00:00:00", ISO_DATETIME_PARSE_PATTERN);
		Date predicted1 = DateUtils.parseDate("2013-01-29 00:00:00", ISO_DATETIME_PARSE_PATTERN);
		
		Date lastWorkingDay = DateTools.getLastWorkingDay(date1);

		assertEquals(DateTools.dateOnly(lastWorkingDay), DateTools.dateOnly(predicted1));
		
		// Monday 25.03.2013
		Date date2 = DateUtils.parseDate("2013-03-25 00:00:00", ISO_DATETIME_PARSE_PATTERN);
		Date predicted2 = DateUtils.parseDate("2013-03-22 00:00:00", ISO_DATETIME_PARSE_PATTERN);
		
		lastWorkingDay = DateTools.getLastWorkingDay(date2);
		
		assertEquals(DateTools.dateOnly(lastWorkingDay), DateTools.dateOnly(predicted2));
	}
	
	public void testNextWorkingDay() throws ParseException {
		// Wednesday 30.01.2013
		Date date1 = DateUtils.parseDate("2013-01-30 00:00:00", ISO_DATETIME_PARSE_PATTERN);
		Date predicted1 = DateUtils.parseDate("2013-01-31 00:00:00", ISO_DATETIME_PARSE_PATTERN);

		assertEquals(DateTools.dateOnly(DateTools.getNextWorkingDay(date1)), DateTools.dateOnly(predicted1));
		
		// Monday 25.03.2013
		Date date2 = DateUtils.parseDate("2013-01-04 00:00:00", ISO_DATETIME_PARSE_PATTERN);
		Date predicted2 = DateUtils.parseDate("2013-01-07 00:00:00", ISO_DATETIME_PARSE_PATTERN);
		
		assertEquals(DateTools.dateOnly(DateTools.getNextWorkingDay(date2)), DateTools.dateOnly(predicted2));
		
		Date date3 = DateUtils.parseDate("2013-01-20 00:00:00", ISO_DATETIME_PARSE_PATTERN);
		Date predicted3 = DateUtils.parseDate("2013-01-21 00:00:00", ISO_DATETIME_PARSE_PATTERN);
		
		assertEquals(DateTools.dateOnly(DateTools.getNextWorkingDay(date3)), predicted3);
	}

	
	public void testGetAge() {

		assertNull(DateTools.getAge(null));
		
		assertEquals(0, (int) DateTools.getAge(new Date()));
		
		Calendar cal = Calendar.getInstance();
		// just before first birthday
		cal.add(Calendar.YEAR, -1);
		cal.add(Calendar.DAY_OF_MONTH, 1);
		assertEquals(0, (int) DateTools.getAge(cal.getTime()));
		// first birthday
		cal.add(Calendar.DAY_OF_MONTH, -1);
		assertEquals(1, (int) DateTools.getAge(cal.getTime()));
		// just after first birthday
		cal.add(Calendar.DAY_OF_MONTH, -1);
		assertEquals(1, (int) DateTools.getAge(cal.getTime()));
		
		cal = Calendar.getInstance();
		// just before second birthday
		cal.add(Calendar.YEAR, -2);
		cal.add(Calendar.DAY_OF_MONTH, 1);
		assertEquals(1, (int) DateTools.getAge(cal.getTime()));
		// second birthday
		cal.add(Calendar.DAY_OF_MONTH, -1);
		assertEquals(2, (int) DateTools.getAge(cal.getTime()));
		// just after second birthday
		cal.add(Calendar.DAY_OF_MONTH, -1);
		assertEquals(2, (int) DateTools.getAge(cal.getTime()));
		
		// test for leap years
		Date dateOfBirth = DateTools.toDate(1960, 2, 29);
		assertTrue(DateTools.getAge(dateOfBirth) > 52);
	}

	public void testGetBirthdayDate() {
		
		assertNull(DateTools.getBirthdayDate(null));
		
		Date now = new Date();
		assertEquals(now, DateTools.getBirthdayDate(now));
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -1);
		assertEquals(now, DateTools.getBirthdayDate(cal.getTime()));
	}
	
}
