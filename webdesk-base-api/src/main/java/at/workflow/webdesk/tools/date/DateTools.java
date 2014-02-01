package at.workflow.webdesk.tools.date;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import at.workflow.webdesk.tools.api.Historization;

/**
 * <p>This tool-class provides useful methods regarding manipulation and comparison of dates.
 * </p><p>
 * Some of the functions are a beware of issues that occurred with used open source frameworks.
 * E.g. the <code>lastMomentOfDay</code> function returns the <code>23:59:0900</code>
 * instead of <code>23:59:999</code>.</p>
 * 
 * TODO: take out all methods that work with DateType into another class.
 * 
 * @author hentner
 * @author sdzuban
 * @author fritzberger
 */
public class DateTools {

	private static final Logger log = Logger.getLogger(DateTools.class);
	
	/**
	 * The 1.1.3000 00:00:00 in milliseconds.
	 * Consider using PoHistorization.isValidtoNull() instead of this constant.
	 */
	public static final long INFINITY_TIMEMILLIS = DateTools.toDate(3000, 1, 1).getTime();
	
	/**
	 * The 1.1.3000 00:00:00 as Date.
	 */
	public static final Date INFINITY = new Date(INFINITY_TIMEMILLIS);
	
	/**
	 * The 1.1.1970 00:00:00 as Date.
	 * http://en.wikipedia.org/wiki/Epoch_(reference_date)#Computing
	 */
	public static final Date EPOCH = new Date(0L);
	
	/**
	 * Compatibility constant for old webdesk versions and customer java-scripts.
	 */
	@Deprecated
	public static final long INFDATE = INFINITY_TIMEMILLIS;

	/** The number of milliseconds in a 24-hour day. */
	public static final long DAY_MILLIS = 24L * 60L * 60L * 1000L;
	
	/** The number of milliseconds in an average year (365.25 days). */
	public static final long YEAR_MILLIS = Math.round(365.25 * DAY_MILLIS);
	
	/** The number of milliseconds in an average month (YEAR_MILLIS / 12). */
	public static final long MONTH_MILLIS = YEAR_MILLIS / 12L;
	
	/** specifies precision of date comparison */
	public enum DatePrecision {
		YEAR, MONTH, DAY, HOUR, MINUTE, SECOND
	}
	
	/**
	 * Common types of dates, relating to days, month and years.
	 * Designates either a day or a moment (milliseconds).
	 * This was made to make it possible to read such types from
	 * persistence and convert it back to an enum value.
	 */
	public enum DateType {
		/** Now, or current day start. */
		DATE,
		
		/** today 00:00:00 */
		TODAY_BEGINNING,
		
		/** today last moment of day */
		TODAY_END,
		
		/** tomorrow 00:00:00 */
		TOMORROW_BEGINNING,
		
		/** tomorrow last moment of days */
		TOMORROW_END,
		
		/** First moment of first day of current month. */
		MONTH_FIRST,
		
		/** Last day start (or last moment) of current month. */
		MONTH_LAST,
		
		/** First moment of day of previous month. */
		PREV_MONTH_FIRST,
		
		/** Last day start (or last moment) of previous month. */
		PREV_MONTH_LAST,
		
		/** First moment of first day of next month. */
		NEXT_MONTH_FIRST,
		
		/** First moment of first day of current quarter. */
		QUARTER_FIRST,
		
		/** Last day start (or last moment) of current quarter. */
		QUARTER_LAST, 
		
		/** First moment of first day of previous quarter. */
		PREV_QUARTER_FIRST,
		
		/** Last day start (or last moment) of previous quarter. */
		PREV_QUARTER_LAST,
		
		/** First moment of first day of current year. */
		YEAR_FIRST,
		
		/** Last day start (or last moment) of current year. */
		YEAR_LAST,
		
		/** First moment of first day of previous year. */
		PREV_YEAR_FIRST,
		
		/** Last day start (or last moment) of previous year. */
		PREV_YEAR_LAST,
	}
	
	
	/**
	 * This is a workaround for the fact that Timestamp.equals() always returns false when it receives a Date instance.
	 * Here equals will be called only when both arguments are instanceof Timestamp, else time millis will be compared.
	 * @param date1 the date that tests for equality (caller of equals).
	 * @param date2 the date that is tested for equality (parameter for equals). 
	 * @return true if both are equal, considering the more precise time information of Timestamp,
	 * 		but ignoring it when one of the arguments is a Date.
	 */
	public static boolean equals(Date date1, Date date2)	{
		if (date1 instanceof Timestamp && date2 instanceof Timestamp)
			return date1.equals(date2);
		
		return date1.getTime() == date2.getTime();	// this is a code duplication of Date.equals()
	}
	

	/**
	 * @return the local time zone offset, measured in minutes,
	 *         relative to UTC that is appropriate for the time represented by
	 *         a new Instance of <code>java.util.Calendar</code>.
	 */
	public static long getTimeOffset() {
		Calendar c = Calendar.getInstance();
		return (c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET));
	}

	/**
	 * @param date
	 *            <code>Date</code> object, that represents the
	 *            <code>Date</code> of the returned <code>Date</code> object.
	 *            After all, this object represents the result.
	 * @param time
	 *            <code>Date</code> object that represents the
	 *            time of the returned <code>Date</code>.
	 * @return the a new instance of <code>Date</code> object, with its time set
	 *         to the time of the <code>time</code> object.
	 */
	public static Date mergeDateAndTime(Date date, Date time) {
		Calendar cDate = Calendar.getInstance();
		cDate.setTime(date);
		Calendar cTime = Calendar.getInstance();
		cTime.setTime(time);

		cDate.set(Calendar.HOUR_OF_DAY, cTime.get(Calendar.HOUR_OF_DAY));
		cDate.set(Calendar.MINUTE, cTime.get(Calendar.MINUTE));
		cDate.set(Calendar.SECOND, cTime.get(Calendar.SECOND));
		cDate.set(Calendar.MILLISECOND, cTime.get(Calendar.MILLISECOND));

		return cDate.getTime();
	}

	/**
	 * Remove the time information of the given <code>date</code>,
	 * i.e. it sets all of hour, minute, second, and millisecond to zero (midnight of the day).
	 * 
	 * @param date the date/time to create the new date from.
	 * @return a new date with time set to <code>00:00:0000</code>.
	 */
	public static Date dateOnly(Date argDate) {
		Calendar c = Calendar.getInstance();
		c.setTime(argDate);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	/**
	 * Due to the fact that some DBs (MySQL) does not store milliseconds
	 * this method is not reentrant.
	 * When it is applied to Date that already represents lastMomentOfDay
	 * the milliseconds will be 000.
	 * 
	 * @return <code>date</code>, but the time is set to <code>23:59:59:900</code>.
	 *         This is done for CForms UI, else their Date-Widgets would
	 *         consider the date to be on next day.
	 */
	public static Date lastMomentOfDay(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		c.set(Calendar.MILLISECOND, 900);	// TODO why 900 and not 999 ?

		// return the INFDATE if the calculated date is after it in a timely fashion
		if (c.getTime().getTime() > DateTools.INFINITY_TIMEMILLIS)
			return new Date(DateTools.INFINITY_TIMEMILLIS);

		return c.getTime();
	}
	
	/** 
	 * due to the fact, that some DB (MySQL) does not store milliseconds
	 * milliseconds cannot be compared here
	 * @param date
	 * @return
	 */
	public static boolean isLastMomentOfDay(Date date) {
		
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int hours = c.get(Calendar.HOUR_OF_DAY);
		int minutes = c.get(Calendar.MINUTE);
		int seconds = c.get(Calendar.SECOND);
		
		return hours == 23 && minutes == 59 && seconds == 59;
	}

	/** @return the date of yesterday one second before midnight. */
	public static Date yesterdayOneSecondBeforeMidnight()	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, -1);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
	
	/** @return the date of today one second before midnight. */
	public static Date todayOneSecondBeforeMidnight()	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
	
	/**
	 * TODO this method should throw NullPointerException when passed date is null!
	 * Getting a null date from somewhere and passing it on would cause current time!?
	 * 
	 * Returns the time-part of passed date, or current time when argument is null.
	 * @param date the date to retrieve time from (is NOT modified by this method), can be null for current time.
	 * @return a new Date object containing only the time portion, year/month/day all are set to zero.
	 */
	public static Date timeOnly(Date date) {
		Calendar timeCal = Calendar.getInstance();

		if (date != null)
			timeCal.setTime(date);

		timeCal.set(Calendar.DAY_OF_MONTH, 1);
		timeCal.set(Calendar.MONTH, 0);
		
		// changed to 1970 (epoch) as using year 1 has unpredictable behavior.
		timeCal.set(Calendar.YEAR, 1970);

		return timeCal.getTime();
	}

	/**
	 * @return true if the given intervals overlap,
	 * 		but having a start time being the same as another interval's end time,
	 * 		or having an end time being the same as another interval's start time,
	 * 		does not mean "overlap".
	 */
	public static boolean doesIntervalsOverlap(Date dateFrom1, Date dateTo1, Date dateFrom2, Date dateTo2) {
		return dateFrom1.before(dateTo2) && dateTo1.after(dateFrom2);
	}

	/**
	 * @return true if the interval (<b><code>dateFrom1</code></b>, <b> <code>dateTo1</code></b>)
	 * is surrounded by interval (<b><code>dateFrom2</code></b>, <b> <code>dateTo2</code></b>)
	 * in a way that start of interval 1 is equal or greater than start of interval 2,
	 * and end of interval 1 is equal or smaller than end of interval 2.
	 */
	public static boolean isInterval1InsideInterval2(Date dateFrom1, Date dateTo1, Date dateFrom2, Date dateTo2) {
		return ! dateFrom1.before(dateFrom2) && ! dateTo1.after(dateTo2);
	}

	/**
	 * @return true if the given date is greater equal dateFromInterval and less equal dateToInterval,
	 * 		i.e. it is within interval including start and end.
	 */
	public static boolean isDateNotOutsideInterval(Date date, Date dateFrom, Date dateTo) {
		return ! date.before(dateFrom) && ! date.after(dateTo);
	}

	/**
	 * @return true if the given date is greater than dateFromInterval and less than dateToInterval,
	 * 		i.e. it is within interval excluding start and end.
	 */
	public static boolean isDateInsideInterval(Date date, Date dateFromInterval, Date dateToInterval) {
		return date.after(dateFromInterval) && date.before(dateToInterval);
	}

	/** Delegates to doesOverlap(..., includingPast = false). */
	public static boolean doesOverlap(Date validFrom1, Date validTo1, Date validFrom2, Date validTo2) {
		return doesOverlap(validFrom1, validTo1, validFrom2, validTo2, false);
	}

	/** Delegates to doesOverlap(..., checkDayOnly = true). */
	public static boolean doesOverlap(Date validFrom1, Date validTo1, Date validFrom2, Date validTo2, boolean includingPast) {
		return doesOverlap(validFrom1, validTo1, validFrom2, validTo2, includingPast, true);
	}

	/**
	 * This function returns <code>true</code> if the timerange
	 * <code>validFrom1</code> - <code>validTo1</code> overlaps with the timerange
	 * <code>validFrom2</code> - <code>validTo2</code>.
	 * <p/>
	 * Possible overlappings:
	 * <pre>
	 *     	    +----------------------o        (the referenced interval: validFrom1, validTo1)
	 * 
	 * (1)  +------------------------------o 	(validFrom2 outside, validTo2 outside)
	 * (2)  +--------------o					(validFrom2 outside, validTo2 inside)
	 * (3)      +--------------------------o    (validFrom2 equals, validTo2 outside)
	 * (4)      +----------o					(validFrom2 equals, validTo2 inside)
	 * (5)      +----------------------o		(validFrom2 equals, validTo2 equals)
	 * (6)              +--------------o		(validFrom2 inside, validTo2 equals)
	 * (7)              +------------------o	(validFrom2 inside, validTo2 outside)
	 * (8)  +--------------------------o		(validFrom2 outside, validTo2 equals)
	 * (9)          +-----------o				(validFrom2 inside, validTo2 inside)
	 * (10) +---o								(validFrom2 outside, validTo2 equals start)
	 * (11)                            +---o	(validFrom2 equals end, validTo2 outside
	 * <pre>
	 * @param includingPast when true, then false is returned when one of the <code>validTo</code>
	 * 		fields is before the current date.
	 * @param checkDayOnly when true, then the time portion of passed parameters will be removed
	 * 		before comparisons, if false, this will return
	 * 		<code>doesIntervalsOverlap(validFrom1, validTo1, validFrom2, validTo2)</code>.
	 */
	public static boolean doesOverlap(Date validFrom1, Date validTo1, Date validFrom2, Date validTo2, boolean includingPast, boolean checkDayOnly) {

		if (includingPast == false)	{
			final Date now = now();
			
			if (validTo1.before(now) || validTo2.before(now))
				return false;
		}

		if (checkDayOnly) {
			validFrom1 = DateTools.dateOnly(validFrom1);
			validTo1 = DateTools.dateOnly(validTo1);
			validFrom2 = DateTools.dateOnly(validFrom2);
			validTo2 = DateTools.dateOnly(validTo2);
			
			return (beforeEquals(validFrom2, validFrom1) && afterEquals(validTo2, validTo1) // (1,3,5,8)
					|| beforeEquals(validFrom2, validFrom1) && beforeEquals(validTo2, validTo1) && validTo2.after(validFrom1) // (2,4,5,8)
					|| afterEquals(validFrom2, validFrom1) && afterEquals(validTo2, validTo1) && beforeEquals(validFrom2, validTo1) && ! validFrom2.equals(validTo2) // (6, 7)
					|| validFrom2.after(validFrom1) && validFrom2.before(validTo1) && validTo2.before(validTo1) // (9)
					|| validFrom2.before(validFrom1) && validTo2.equals(validFrom1) // (10)
					|| validFrom2.equals(validTo1) && validTo2.after(validTo1)); // (11)
		}
		
		// if one intervals starts on the same time as the other ends we do not regard it as overlapping!
		return (doesIntervalsOverlap(validFrom1, validTo1, validFrom2, validTo2));
	}

	/**
	 * @return <code>true</code> if <code>date1</code> is
	 *         after or equal <code>date2</code> in a timely fashion ,
	 *         <code>false</code> otherwise.
	 */
	private static boolean afterEquals(Date date1, Date date2) {
		return (date1.after(date2) || date1.equals(date2)) ? true : false;
	}

	/**
	 * @return <code>true</code> if <code>date1</code> is
	 *         before or equal <code>date2</code> in a timely fashion ,
	 *         <code>false</code> otherwise.
	 */
	private static boolean beforeEquals(Date date1, Date date2) {
		return (date1.before(date2) || date1.equals(date2)) ? true : false;
	}

	/** @return either the minimal valid-from date or the maximal valid-to date from given list. */
	public static Date getMinMaxDate(List<Historization> list, boolean min) {
		Iterator<Historization> i = list.iterator();
		Date date = null;
		while (i.hasNext()) {
			Historization hist = i.next();
			if (date == null
					|| (min && date.after(hist.getValidfrom()))
					|| (!min && date.before(hist.getValidto())))
				if (min)
					date = hist.getValidfrom();
				else
					date = hist.getValidto();
		}
		return date;
	}

	public static String toDateTimeFormat(Date beginDateTime) {
		DateFormat df = DateFormat.getDateTimeInstance();
		return df.format(beginDateTime);
	}

	public static String toDateFormat(Date beginDateTime) {
		DateFormat df = DateFormat.getDateInstance();
		return df.format(beginDateTime);
	}

	/**
	 * gets monday of this week. If today is monday returns today
	 * otherwise goes back to the last monday before.
	 */
	public static Calendar getMonday(Date currentDate) {
		Calendar res = Calendar.getInstance();
		res.setTime(currentDate);
		switch (res.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.MONDAY:
			return res;
		case Calendar.TUESDAY:
			res.add(Calendar.DAY_OF_MONTH, -1);
			break;
		case Calendar.WEDNESDAY:
			res.add(Calendar.DAY_OF_MONTH, -2);
			break;
		case Calendar.THURSDAY:
			res.add(Calendar.DAY_OF_MONTH, -3);
			break;
		case Calendar.FRIDAY:
			res.add(Calendar.DAY_OF_MONTH, -4);
			break;
		case Calendar.SATURDAY:
			res.add(Calendar.DAY_OF_MONTH, -5);
			break;
		case Calendar.SUNDAY:
			res.add(Calendar.DAY_OF_MONTH, -6);
			break;
		}
		return res;
	}

	/**
	 * returns Date with monday of last week
	 * (gets this weeks monday and subtracts one week)
	 * 
	 * @param date
	 * @return
	 */
	public static Date getMondayOfLastWeek(Date date) {
		Calendar c = getMonday(date);
		c.setTime(date);
		c.add(Calendar.WEEK_OF_YEAR, -1);
		date.setTime(c.getTimeInMillis());
		return date;
	}

	/**
	 * returns Date with monday of next week.
	 * (returns this weeks monday and adds one week)
	 * 
	 * @param date
	 *            : referenced date
	 * @return
	 */
	public static Date getMondayOfNextWeek(Date date) {
		Calendar c = getMonday(date);
		c.setTime(date);
		c.add(Calendar.WEEK_OF_YEAR, 1);
		date.setTime(c.getTimeInMillis());
		return date;
	}

	/**
	 * Creates a clone of the passed date object and sets
	 * the specifiedCalendar field to passed value.
	 * @param date the date to create a manipulated clone from.
	 * @param field one of Calendar.YEAR, Calendar.MONTH, ...
	 * @param value the value to set into specified field.
	 * @return a new Date object with the given setting.
	 */
	public static Date setDateTimeField(Date date, int field, int value) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(field, value);
		return c.getTime();
	}

	/**
	 * @param date the Date from which to retrieve.
	 * @param field the Calendar.* constant telling the field to retrieve.
	 * @return the specified field from given date.
	 */
	public static int getDateTimeField(Date date, int field) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(field);
	}
	
	/**
	 * Standard functionality to turn a text into a Date.
	 * Will return null when called with null or empty string.
	 * @param text the text to parse as date, can be null or empty.
	 * @param simpleDateFormatMask something like "dd.MM.yyyy" (according to Java SimpleDateFormat conventions).
	 * @return a Date parsed from passed text argument, or null when text was null or empty.
	 * @throws ParseException when text contains no Java-SimpleDateFormat-valid date.
	 */
	public static Date valueOf(String text, String simpleDateFormatMask) throws ParseException {
		if (text == null || text.trim().length() <= 0)
			return null;
		try	{
			return new SimpleDateFormat(simpleDateFormatMask).parse(text);
		}
		catch (ParseException e)	{
			log.error("Date parsing error offset for >"+text+"< is "+e.getErrorOffset()+", correct part is >"+text.substring(0, Math.min(e.getErrorOffset(), text.length()))+"<");
			throw e;
		}
	}

	/**
	 * @param year the year of the aimed date.
	 * @param month1To12 the month of the aimed date, 1-12.
	 * @param dayOfMonth the day of the aimed date.
	 * @return the Date constructed from passed parameters, hour set to 0.
	 */
	public static Date toDate(int year, int month1To12, int dayOfMonth)	{
		return toDate(year, month1To12, dayOfMonth, 0, 0, 0);
	}

	/**
	 * 
	 * @param year the year of the aimed date.
	 * @param month1To12 the month of the aimed date, 1-12.
	 * @param dayOfMonth the day of the aimed date.
	 * @param hour24 the hour, from 0-24.
	 * @param minute the minute, from 0-59.
	 * @return the Date constructed from passed parameters, second set to 0.
	 */
	public static Date toDate(int year, int month1To12, int dayOfMonth, int hour24, int minute)	{
		return toDate(year, month1To12, dayOfMonth, hour24, minute, 0);
	}

	/**
	 * @param hour24 the aimed hour, 0-24.
	 * @param minute the aimed minute, 0-60.
	 * @param second the aimed second, 0-60.
	 * @param rest see above.
	 * @return the Date constructed from passed parameters.
	 */
	public static Date toDate(int year, int month1To12, int dayOfMonth, int hour24, int minute, int second)	{
		assert month1To12 >= 1 && month1To12 <= 12 : "Month must be between 1 and 12";
		assert dayOfMonth >= 1 && dayOfMonth <= 31 : "Day must be between 1 and 31";
		assert hour24 >= 0 && hour24 <= 24 : "Hour must be between 0 and 24";
		assert minute >= 0 && minute <= 60 : "Minute must be between 0 and 60";
		assert second >= 0 && second <= 60 : "Second must be between 0 and 60";
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month1To12 - 1);
		c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		c.set(Calendar.HOUR_OF_DAY, hour24);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, second);
		c.set(Calendar.MILLISECOND, 0);
		
		return c.getTime();
	}

	/**
	 * @param d1 the one date to compare.
	 * @param d2 the other date to compare. 
	 * @return true when passed dates are in same minute and hour on same day, month and year, else false.
	 */
	public static boolean isInSameMinute(Date d1, Date d2) {
		assert d1 != null && d2 != null;
		
		Calendar c1 = Calendar.getInstance();
		c1.setTime(d1);
		Calendar c2 = Calendar.getInstance();
		c2.setTime(d2);
		return
			c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
			c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
			c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH) &&
			c1.get(Calendar.HOUR_OF_DAY) == c2.get(Calendar.HOUR_OF_DAY) &&
			c1.get(Calendar.MINUTE) == c2.get(Calendar.MINUTE);
	}

	/**
	 * @param d1 the one date to compare.
	 * @param d2 the other date to compare. 
	 * @return true when passed dates are on same day, month and year, else false.
	 */
	public static boolean isOnSameDay(Date d1, Date d2) {
		assert d1 != null && d2 != null;
		
		Calendar c1 = Calendar.getInstance();
		c1.setTime(d1);
		Calendar c2 = Calendar.getInstance();
		c2.setTime(d2);
		return
			c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
			c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
			c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * @param d1 the one date to compare.
	 * @param d2 the other date to compare. 
	 * @return true when passed dates are in same month and year, else false.
	 */
	public static boolean isInSameMonth(Date d1, Date d2) {
		Calendar c1 = Calendar.getInstance();
		c1.setTime(d1);
		Calendar c2 = Calendar.getInstance();
		c2.setTime(d2);
		return
			c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
			c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH);
	}

	/** @return new Date(). */
	public static Date now()	{
		return new Date();
	}

	/** @return now with time 00:00, delegating now() to dateOnly(). */
	public static Date today()	{
		return dateOnly(now());
	}

	/** @return now with time 00:00, delegating now() to dateOnly(). */
	public static Date today(int hour24, int minute)	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, hour24);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	/** @return tomorrow with time 00:00, delegating to dateOnly(). */
	public static Date tomorrow()	{
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, +1);
		return dateOnly(c.getTime());
	}

	/** @return yesterday with time 00:00, delegating to dateOnly(). */
	public static Date yesterday()	{
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, -1);
		return dateOnly(c.getTime());
	}

	/** @return the first moment of the first day of current month. */
	public static Date thisMonth()	{
		return getFirstDayOfMonth(today());
	}

	/** @return the last moment of the last day of current month. */
	public static Date thisMonthEnd()	{
		return getLastMomentOfMonth(today());
	}

	/** @return the first moment of the first day of previous month. */
	public static Date nextMonth()	{
		return getFirstDayOfNextMonth(today());
	}

	/** @return the first moment of the first day of previous month. */
	public static Date previousMonth()	{
		return getFirstDayOfPreviousMonth(today());
	}

	/** @return the first moment of the first day of previous month. */
	public static Date previousMonthEnd()	{
		return getLastDayOfPreviousMonth(today());
	}

	/** @return the number of days in month of passed Date. */
	public static int getNumberOfDaysInMonth(Date month)	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(month);
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}
	
	/**
	 * @param year year of the month
	 * @param month1to12 the 1-12 number of the month to retrieve days for.
	 * @return the number of days in passed month, 0-11 (Java month).
	 */
	public static int getNumberOfDaysInMonth(int year, int month1to12)	{
		assert month1to12 > 0 && month1to12 < 13;
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month1to12 - 1);
		return getNumberOfDaysInMonth(calendar.getTime());
	}
	
	/**
	 * @param month1to12 the 1-12 number of the month of the current year to retrieve days for.
	 * @return the number of days in passed month, 0-11 (Java month).
	 */
	public static int getNumberOfDaysInMonth(int month1to12)	{
		assert month1to12 > 0 && month1to12 < 13;
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, month1to12 - 1);
		return getNumberOfDaysInMonth(calendar.getTime());
	}

	/**
	 * @param interval
	 * @return the number of days between start and end of interval.
	 */
	public static int getNumberOfDays(Interval interval) {
		assert interval != null;
		
		return dayDifference(dateOnly(interval.getFrom()), dateOnly(interval.getTo()));
	}
	
	/**
	 * @param start the start date to count days from, will be simplified to dateOnly.
	 * @param end the end date to count days to, will be simplified to dateOnly.
	 * @return the number of days in given range.
	 */
	public static int getNumberOfDays(Date start, Date end) {
		assert start != null && end != null;
		
		return dayDifference(dateOnly(start), dateOnly(end));
	}

	/**
	 * @param date the date to check if it is on weekend.
	 * @return true when passed day is on weekend.
	 */
	public static boolean isWeekend(Date date) {
		int weekDay = getDayOfWeek(date);
		return weekDay == Calendar.SATURDAY || weekDay == Calendar.SUNDAY;
	}
	
	/**
	 * method used to calculate the last working day from the 
	 * param date. It's helpful to find out if one day before is
	 * a working day.
	 * 
	 * @param date
	 * @return last working day from date (param).
	 */
	public static Date getLastWorkingDay(Date date) {		
		Date lastDay = DateUtils.addDays(dateOnly(date), -1);
		if(isWeekend(lastDay)) {
			while(isWeekend(lastDay)) {
				lastDay = DateUtils.addDays(lastDay, -1);
			}
		}
		return lastDay;
	}
	
	public static Date getNextWorkingDay(Date date) {
		Date nextDay = DateUtils.addDays(date, 1);
		if(isWeekend(nextDay)) {
			while(isWeekend(nextDay)) {
				nextDay = DateUtils.addDays(nextDay, 1);
			}
		}
		
		return nextDay;
	}
	
	/**
	 * This returns the next (future) whole hour (CAUTION: does NOT round)!
	 * This is for creating a validity-time to select from historicizing types,
	 * in order for the result to be cached with that Date parameter for an hour (AOP layers).
	 */
	public static Date nowWithHourPrecision() {
		Date now = now();
		now = DateUtils.addHours(now, 1);
		now = DateUtils.truncate(now, Calendar.HOUR_OF_DAY);
		return now;
	}

	/**
	 * This returns the next (future) whole minute (CAUTION: does NOT round)!
	 * This is for creating a validity-time to select from historicizing types,
	 * in order for the result to be cached with this Date parameter for a minute (AOP layers).
	 * 
	 * TODO: not used yet, needed when caching must be minute-precise.
	 */
	public static Date nowWithMinutePrecision() {
		Date date = DateUtils.addMinutes(now(), 1);
		return minutesOnly(date);
	}

	/**
	 * @return the given Date rounded to minutes,
	 * 		16:24:30 will be 16:25:00, and
	 * 		16:24:29 will be 16:24:00.
	 */
	public static Date roundToMinute(Date date) {
		date = DateUtils.addSeconds(date, 30);
		return DateUtils.truncate(date, Calendar.MINUTE);
	}

	/**
	 * @return the given Date rounded to seconds,
	 * 		16:24:30:500 will be 16:24:31:000, and
	 * 		16:24:29:500 will be 16:24:29:000.
	 */
	public static Date roundToSeconds(Date date) {
		date = DateUtils.addMilliseconds(date, 500);
		return DateUtils.truncate(date, Calendar.SECOND);
	}

	/**
	 * @return the given date with minute precision (truncated seconds and millis).
	 * 		This does not round 30 seconds to the next minute, 16:24:30 will be 16:24:00, as 16:24:59 will be 16:24:00.
	 */
	public static Date minutesOnly(Date date) {
		return DateUtils.truncate(date, Calendar.MINUTE);
	}

	/**
	 * @param date1 the first point of the time range.
	 * @param date2 the second point of the time range.
	 * @return the span of the passed time range, rounded to whole days,
	 * 		positive when date1 was before date2.
	 */
	public static int dayDifference(Date date1, Date date2) {
		long diff = minuteDifference(date1, date2);
		return (int) Math.round((double) diff / (double) 60 / 24);
	}

	/**
	 * This rounds, meaning a difference of 30 seconds will be returned as 1 minute.
	 * @param date1 the start point of the time range.
	 * @param date2 the end point of the time range.
	 * @return the span of the passed time range, rounded to whole minutes (not industry minutes!),
	 * 		positive when date1 was before date2.
	 */
	public static int minuteDifference(Date date1, Date date2) {
		long diff = secondsDifference(date1, date2);
		return (int) Math.round((double) diff / (double) 60);
	}

	/**
	 * This rounds, meaning a difference of 500 millis will be returned as 1 second.
	 * @param date1 the start point of the time range.
	 * @param date2 the end point of the time range.
	 * @return the span of the passed time range, rounded to whole seconds,
	 * 		positive when date1 was before date2.
	 */
	public static long secondsDifference(Date date1, Date date2) {
		long diff = date2.getTime() - date1.getTime();
		return Math.round((double) diff / (double) 1000);
	}

	/**
	 * Calculates the Date back to which e.g. bookings are changeable.
	 * The returned date is not changeable anymore, but the day after it is still changeable.
	 * If e.g. <code>lastMonthsEntriesChangeableUpTo</code> is 15 and current date is the 16th, only this month's
	 * data are changeable. If <code>lastMonthsEntriesChangeableUpTo</code> is 15 and current date is the 14th,
	 * also last month's data are changeable (additionally to those of current month).
	 * @param lastMonthsEntriesChangeableUpTo the day up to which last month's entries are changeable.
	 * @return the date back to which entries are changeable, or null when argument is less equal zero.
	 */
	public static Date getBackwardLimit(int lastMonthsEntriesChangeableUpTo) {
		if (lastMonthsEntriesChangeableUpTo <= 0)
			return null;
		
		Date today = DateTools.now();
		int currentDayOfMonth = DateTools.getDateTimeField(today, Calendar.DAY_OF_MONTH);
		int daysBack = currentDayOfMonth;
		boolean includeLastMonth = (lastMonthsEntriesChangeableUpTo >= currentDayOfMonth);
		if (includeLastMonth)	{
			Date lastMonth = DateUtils.addMonths(today, -1);
			int daysOfLastMonth = DateTools.getNumberOfDaysInMonth(lastMonth);
			daysBack += daysOfLastMonth;
		}
		return dateOnly(DateUtils.addDays(today, -daysBack));
	}

	/** @return one of Calendar.SATURDAY, Calendar.SUNDAY, ... constants. */
	public static int getDayOfWeek(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_WEEK);
	}
	
	/**
	 * @return the year of the week the given date is in, considering language/country. This could differ from Calendar.YEAR!
	 * @see http://docs.oracle.com/javase/1.5.0/docs/api/java/util/Calendar.html#WEEK_OF_YEAR
	 * @see http://stackoverflow.com/questions/4608470/why-dec-31-2010-returns-1-as-week-of-year
	 */
	public static int getYearOfWeek(Date date, Locale locale)	{
		final Calendar calendar = Calendar.getInstance(locale);
		calendar.setTime(date);
		
		final int week = calendar.get(Calendar.WEEK_OF_YEAR);
		final int year = calendar.get(Calendar.YEAR);
		final int day = calendar.get(Calendar.DAY_OF_YEAR);
		
		// TODO there is already a built-in function in Calendar in JDK7 which offers 
		// the functionality http://docs.oracle.com/javase/7/docs/api/java/util/GregorianCalendar.html#getWeekYear()
		return (week <= 1 && day > 356)	// first day(s) of week still in old year
			? year + 1
			: year;
	}

	
	
	
	/** @return 1.1. of the year the given day is in, 00:00:00,0 */
	public static Date getFirstDayOfYear(Date day) {
		assert day != null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(day);
		cal.set(Calendar.DAY_OF_YEAR, 1);
		return dateOnly(cal.getTime());
	}
	
	/** @return 31.12. of the year the given day is in, 00:00:00,0 */
	public static Date getLastDayOfYear(Date day) {
		assert day != null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(day);
		cal.set(Calendar.MONTH, 11);
		cal.set(Calendar.DAY_OF_MONTH, 31);
		return dateOnly(cal.getTime());
	}
	
	/** @return 31.12. of the year the given day is in, 23:59:59,9 */
	public static Date getLastMomentOfYear(Date day) {
		assert day != null;
		return lastMomentOfDay(getLastDayOfYear(day));
	}
	
	/** @return first day of the quarter the given day is in, 00:00:00,0 */
	public static Date getFirstDayOfQuarter(Date day) {
		assert day != null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(day);
		int month = cal.get(Calendar.MONTH);
		cal.set(Calendar.MONTH, 3 * (month / 3));
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return dateOnly(cal.getTime());
	}
	
	/** @return last day of the quarter the given day is in, 00:00:00,0 */
	public static Date getLastDayOfQuarter(Date day) {
		assert day != null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(day);
		int month = cal.get(Calendar.MONTH);
		cal.set(Calendar.MONTH, 3 * (month / 3) + 2);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		return dateOnly(cal.getTime());
	}
	
	/** @return last day of the quarter the given day is in, 23:59:59,9 */
	public static Date getLastMomentOfQuarter(Date day) {
		assert day != null;
		return lastMomentOfDay(getLastDayOfQuarter(day));
	}
	
	/** @return first day of the month the given day is in, 00:00:00,0 */
	public static Date getFirstDayOfMonth(Date day) {
		assert day != null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(day);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return dateOnly(cal.getTime());
	}
	
	/** @return last day of the month the given day is in, 00:00:00,0 */
	public static Date getLastDayOfMonth(Date day) {
		assert day != null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(day);
		int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		cal.set(Calendar.DAY_OF_MONTH, lastDay);
		return dateOnly(cal.getTime());
	}
 	
	/** @return last moment of the month the given day is in, 23:59:59,9 */
	public static Date getLastMomentOfMonth(Date day) {
		assert day != null;
		return lastMomentOfDay(getLastDayOfMonth(day));
	}
	
	/** @return first day of the year previous to the year the given day is in, 00:00:00,0 */
	public static Date getFirstDayOfPreviousYear(Date day) {
		assert day != null;
		return getFirstDayOfYear(DateUtils.addYears(day, -1));
	}
	
	/** @return last day of the year previous to the year the given day is in, 00:00:00,0 */
	public static Date getLastDayOfPreviousYear(Date day) {
		assert day != null;
		return getLastDayOfYear(DateUtils.addYears(day, -1));
	}
	
	/** @return last moment of the year previous to the year the given day is in, 23:59:59,9 */
	public static Date getLastMomentOfPreviousYear(Date day) {
		assert day != null;
		return getLastMomentOfYear(DateUtils.addYears(day, -1));
	}
	
	/** @return first day of the quarter previous to the quarter the given day is in, 00:00:00,0 */
	public static Date getFirstDayOfPreviousQuarter(Date day) {
		assert day != null;
		return getFirstDayOfQuarter(DateUtils.addMonths(day, -3));
	}
	
	/** @return last day of the quarter previous to the quarter the given day is in, 00:00:00,0 */
	public static Date getLastDayOfPreviousQuarter(Date day) {
		assert day != null;
		return getLastDayOfQuarter(DateUtils.addMonths(day, -3));
	}
	
	/** @return last moment of the quarter previous to the quarter the given day is in, 23:59:59,9 */
	public static Date getLastMomentOfPreviousQuarter(Date day) {
		assert day != null;
		return getLastMomentOfQuarter(DateUtils.addMonths(day, -3));
	}
	
	/** @return first day of the month previous to the month the given day is in, 00:00:00,0 */
	public static Date getFirstDayOfPreviousMonth(Date day) {
		assert day != null;
		return getFirstDayOfMonth(DateUtils.addMonths(day, -1));
	}
	
	/** @return first day of the month next to the month the given day is in, 00:00:00,0 */
	public static Date getFirstDayOfNextMonth(Date day) {
		assert day != null;
		return getFirstDayOfMonth(DateUtils.addMonths(day, +1));
	}
	
	/** @return last day of the month previous to the month the given day is in, 00:00:00,0 */
	public static Date getLastDayOfPreviousMonth(Date day) {
		assert day != null;
		return getLastDayOfMonth(DateUtils.addMonths(day, -1));
	}
	
	/** @return last day of the month previous to the month the given day is in, 23:59:59,9 */
	public static Date getLastMomentOfPreviousMonth(Date day) {
		assert day != null;
		return getLastMomentOfMonth(DateUtils.addMonths(day, -1));
	}
	
	
	
	/**
	 * @param type which type of moment is required.
	 * @return either first moment of the first day of period,
	 * 		or last moment of last day of period,
	 * 		or now() when type is null.
	 */
	public static Date getMoment(DateType type) {
		switch (type)
		{
		case DATE: return now();
		case MONTH_FIRST: return getFirstDayOfMonth(now());
		case TODAY_BEGINNING: return today();
		case TODAY_END: return lastMomentOfDay(today());
		case TOMORROW_BEGINNING: return tomorrow();
		case TOMORROW_END: return lastMomentOfDay(tomorrow());
		case MONTH_LAST: return getLastMomentOfMonth(now());
		case PREV_MONTH_LAST: return getLastMomentOfPreviousMonth(now());
		case QUARTER_FIRST: return getFirstDayOfQuarter(now());
		case QUARTER_LAST: return getLastMomentOfQuarter(now());
		case PREV_MONTH_FIRST: return getFirstDayOfPreviousMonth(now());
		case PREV_QUARTER_FIRST: return getFirstDayOfPreviousQuarter(now());
		case PREV_QUARTER_LAST: return getLastMomentOfPreviousQuarter(now());
		case YEAR_FIRST: return getFirstDayOfYear(now());
		case YEAR_LAST: return getLastMomentOfYear(now());
		case PREV_YEAR_FIRST: return getFirstDayOfYear(DateUtils.addYears(now(), -1));
		case PREV_YEAR_LAST: return getLastMomentOfPreviousYear(now());
		default: throw new IllegalArgumentException("Unknown DateType enum value: "+type);
		}
	}

	/**
	 * @param type which type of day is required.
	 * @return either first moment of first day of the period,
	 * 		or first moment of last last day of period,
	 * 		or now() when type is null.
	 */
	public static Date getDay(DateType type) {
		switch (type)
		{
		case DATE: return dateOnly(now());
		case TODAY_BEGINNING: return today();
		case TODAY_END: return lastMomentOfDay(today());
		case TOMORROW_BEGINNING: return tomorrow();
		case TOMORROW_END: return lastMomentOfDay(tomorrow());
		case MONTH_LAST: return getLastDayOfMonth(now());
		case PREV_MONTH_LAST: return getLastDayOfPreviousMonth(now());
		case QUARTER_LAST: return getLastDayOfQuarter(now());
		case PREV_QUARTER_LAST: return getLastDayOfPreviousQuarter(now());
		case YEAR_LAST: return getLastDayOfYear(now());
		case PREV_YEAR_LAST: return getLastDayOfPreviousYear(now());
		default: return getMomentOrDay(type);
		}
	}
	
	private static Date getMomentOrDay(DateType type) {
		switch (type)
		{
		case MONTH_FIRST: return getFirstDayOfMonth(now());
		case PREV_MONTH_FIRST: return getFirstDayOfPreviousMonth(now());
		case NEXT_MONTH_FIRST: return getFirstDayOfNextMonth(now());
		case QUARTER_FIRST: return getFirstDayOfQuarter(now());
		case PREV_QUARTER_FIRST: return getFirstDayOfPreviousQuarter(now());
		case YEAR_FIRST: return getFirstDayOfYear(now());
		case PREV_YEAR_FIRST: return getFirstDayOfPreviousYear(now());
		default: throw new IllegalArgumentException("Unknown DateType enum value: "+type);
		}
	}
	
	public static Date getFirstDayOfWeek(int year, int week) {
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.WEEK_OF_YEAR, week);
		cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
		return cal.getTime();
	}
	
	public static Date getLastDayOfWeek(int year, int week) {
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.WEEK_OF_YEAR, week);
		if (cal.getFirstDayOfWeek() == Calendar.MONDAY)
			cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		else
			cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		return cal.getTime();
	}
	
	
	
	public static String getWeekDates(int year, int week) {
		
		Date monday = getFirstDayOfWeek(year, week);
		Date sunday = getLastDayOfWeek(year, week);
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.");
		
		return sdf.format(monday) + "-" + sdf.format(sunday);
	}
	
	/** parses a String to a date with the supplied pattern. Converts
	 * checked ParseExceptions to RuntimeExceptions if they occur!
	 * Essentially same as: SimpleDateFormat.parse() but without
	 * checked Exceptions. 
	 * */
	public static Date parse(String dateStr, String pattern) {
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		dateFormat.applyPattern(pattern);
		try {
			return dateFormat.parse(dateStr);
		}
		catch (ParseException e) {
			throw new RuntimeException("Problems parsing the String=" + dateStr + " to Date with pattern=" + pattern,e);
		}
	}
	
	/** tries to parse the passed String with the supplied pattern. Returns true if parseable
	 * or false if not */
	public static boolean isValidDate(String dateStr, String pattern) {
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		dateFormat.applyPattern(pattern);
		try {
			dateFormat.parse(dateStr);
			return true;
		}
		catch (ParseException e) {
			return false;
		}
	}
	
	/**
	 * Tests equality of dates 1 and 2 with given precision.
	 * Mind that this comparison truncates, so minute 11:59:999
	 * will not be equal to 12:00:000 in MINUTES precision!
	 * @param date1 the first date/time to compare, can be null.
	 * @param date2 the second date/time to compare, can be null.
	 * @param precision the precision of comparison, can be null,
	 * 		then delegates to DateTools.equals (milliseconds precision).
	 */
	public static boolean datesAreEqual(Date date1, Date date2, DatePrecision precision) {
		if (date1 == date2)	// identical, or both are null
			return true;
		
		if (date1 == null || date2 == null)
			return false;
		
		if (precision == null)
			return DateTools.equals(date1, date2);
		
		final Calendar calendar1 = Calendar.getInstance();
		calendar1.setTime(date1);
		final Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(date2);
		
		final boolean equalYear = calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
		if (equalYear == false || precision == DatePrecision.YEAR)
			return equalYear;
		
		final boolean equalMonth = calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH);
		if (equalMonth == false || precision == DatePrecision.MONTH)
			return equalMonth;
		
		final boolean equalDay = calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH);
		if (equalDay == false || precision == DatePrecision.DAY)
			return equalDay;
		
		final boolean equalHour = calendar1.get(Calendar.HOUR_OF_DAY) == calendar2.get(Calendar.HOUR_OF_DAY);
		if (equalHour == false || precision == DatePrecision.HOUR)
			return equalHour;
		
		final boolean equalMinute = calendar1.get(Calendar.MINUTE) == calendar2.get(Calendar.MINUTE);
		if (equalMinute == false || precision == DatePrecision.MINUTE)
			return equalMinute;
		
		if (precision != DatePrecision.SECOND)
			throw new IllegalArgumentException("Unknown Date Precision " + precision);
		
		return calendar1.get(Calendar.SECOND) == calendar2.get(Calendar.SECOND);
	}
	
	/**
	 * Compares dates 1 and 2 with given precision.
	 * If the precision is null milliseconds are taken.
	 * One or both dates can be null.
	 */
	public static int datesCompare(Date date1, Date date2, DatePrecision precision) {
		
		if (date1 == null && date2 == null)
			return 0;
		if (date1 == null)
			return -1;
		if (date2 == null)
			return 1;
		
		if (precision == null)
			return date1.compareTo(date2);
		
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		
		int result = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
		if (result != 0 || precision == DatePrecision.YEAR)
			return result;
		result = cal1.get(Calendar.MONTH) - cal2.get(Calendar.MONTH);
		if (result != 0 || precision == DatePrecision.MONTH)
			return result;
		result = cal1.get(Calendar.DAY_OF_MONTH) - cal2.get(Calendar.DAY_OF_MONTH);
		if (result != 0 || precision == DatePrecision.DAY)
			return result;
		result = cal1.get(Calendar.HOUR_OF_DAY) - cal2.get(Calendar.HOUR_OF_DAY);
		if (result != 0 || precision == DatePrecision.HOUR)
			return result;
		result = cal1.get(Calendar.MINUTE) - cal2.get(Calendar.MINUTE);
		if (result != 0 || precision == DatePrecision.MINUTE)
			return result;
		return cal1.get(Calendar.SECOND) - cal2.get(Calendar.SECOND);
	}
	
	/**
	 * just short cut to getDaysInInterval using interval.
	 * @param interval
	 * @return Days in interval including start and end.
	 */
	public static List<Date> getDaysInInterval(Interval interval) {
		return getDaysInInterval(interval.getFrom(), interval.getTo());
	}
	
	/**
	 * just short cut to getDaysInInterval including start and end date in interval.
	 * @param startOfInterval
	 * @param endOfInterval
	 * @return Days in interval including start and end.
	 */
	public static List<Date> getDaysInInterval(Date startOfInterval, Date endOfInterval) {
		return getDaysInInterval(startOfInterval, endOfInterval, true, true);
	}
	
	/**
	 * helper method used to get a list of days inside an interval. A list of Date object
	 * is returned. Depending on includeStart and includeEnd variables, the start and end
	 * date are included in the list.
	 * 
	 * @param startOfInterval Date of start of interval.
	 * @param endOfInterval Date of end of interval
	 * @param includeStart flag indicates if start date should be included in results.
	 * @param includeEndDate flag indicates if end date should be included in results.
	 * @return List of Date objects containing each day between start and end date.
	 */
	public static List<Date> getDaysInInterval(Date startOfInterval, Date endOfInterval, boolean includeStart, boolean includeEnd) {
		List<Date> daysInInterval = new ArrayList<Date>();
		
		// check for null values and return empty list to avoid npe.
		if(startOfInterval == null || endOfInterval == null) {
			return daysInInterval;
		}
		
		
		if(isOnSameDay(startOfInterval, endOfInterval) && (includeStart || includeEnd)) {
			daysInInterval.add(startOfInterval);
			return daysInInterval;
		} else if(isOnSameDay(startOfInterval, endOfInterval) && includeStart == false && includeEnd == false) {
			// this case is special, start and end are on the same day and both should be excluded -> empty list.
			return daysInInterval;
		}
		
		// work without time.
		startOfInterval = dateOnly(startOfInterval);
		endOfInterval = dateOnly(endOfInterval);
		
		// swap dates if start is after end.
		if(startOfInterval.after(endOfInterval)) {
			Date tempDate = endOfInterval;
			endOfInterval = startOfInterval;
			startOfInterval = tempDate;
		}
		
		Date dayInInterval = null;
		// include start if required.
		if(includeStart) {
			daysInInterval.add(startOfInterval);
			dayInInterval = dateOnly(DateUtils.addDays(startOfInterval, 1));
		} else {
			dayInInterval = dateOnly(DateUtils.addDays(startOfInterval, 1));
		}
		
		// iterate until get to end date.
		while(isOnSameDay(dayInInterval, endOfInterval) == false) {
			daysInInterval.add(dayInInterval);
			// next day
			dayInInterval = dateOnly(DateUtils.addDays(dayInInterval, 1));
		}
		
		// include end date if required.
		if(includeEnd) {
			daysInInterval.add(endOfInterval);
		}
		
		return daysInInterval;
	}
	
	
	/**
	 * Use this method to add weekdays to a given date. This method leaves the
	 * original date object unchanged and returns a new date object.
	 * 
	 * @param date where we want to add weekdays
	 * @param days is the number of weekdays to add/subtract (if negative)
	 * @return a new date object
	 */
	public static Date addWeekDays(Date date, int days) {
		Date newDate = (Date) date.clone();
		int workdays=0;
		
		int dayToAdd = (days >= 0 ? 1 : -1 );
		int daysLimit = (days>=0 ? days : -days);
		
		while(workdays < daysLimit) {
			newDate = DateUtils.addDays(newDate, dayToAdd);
			
			// jump over weekend
			if (DateTools.isWeekend(newDate))
				newDate = DateUtils.addDays(newDate, dayToAdd);
			if (DateTools.isWeekend(newDate))
				newDate = DateUtils.addDays(newDate, dayToAdd);
			
			workdays = workdays + 1;
		}
		return newDate;
	}
	

	// person age utils
	
	/** @return current age of the person born on dateOfBirth (29th February has birthday every year) */
	public static Integer getAge(Date dateOfBirth) {
		return getAge(dateOfBirth, new Date());
	}
		
	/** @return age of the person born on dateOfBirth (29th February has birthday every year) at referenceDate */
	public static Integer getAge(Date dateOfBirth, Date referenceDate) {
		
	    if (dateOfBirth == null)
	        return null;
	    
	    if (dateOfBirth.after(referenceDate))
	    	throw new IllegalArgumentException("Date after reference Date.");
	    
	    Calendar cal1 = Calendar.getInstance();
	    cal1.setTime(dateOfBirth);
	    cal1.add(Calendar.YEAR, 1);
	    Calendar cal2 = Calendar.getInstance();
	    cal2.setTime(referenceDate);
	    int i = 0;
	    while ( ! cal1.after(cal2)) {
	        cal1.add(Calendar.YEAR, 1);
	        i += 1;
	    }
	    return i;
 	}
	
	/** @return birthday date this year */
	public static Date getBirthdayDate(Date dateOfBirth) {
		
		if (dateOfBirth == null)
			return null;
		
		if (dateOfBirth.after(new Date()))
			throw new IllegalArgumentException("Date of birth is in future");
		
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		cal.setTime(dateOfBirth);
		cal.set(Calendar.YEAR, year);
		return cal.getTime();
	}

	/**
	 * @param year year for which the month shall be provided as interval
	 * @param month 1 .. 12 month for which the interval shall be provided
	 * @return specified month as interval with first day 00:00:00 and last day 23:59:59
	 */
	public static Interval getMonthAsInterval(int year, int month) {
		
		if (month < 1 || month > 12)
			throw new IllegalArgumentException("Month can be only between 1 and 12: " + month);
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Date from = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		Date to = cal.getTime();
		
		return new DateInterval(from, to);
	}
}
