package at.workflow.webdesk.tools.date;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import at.workflow.webdesk.tools.IfDate;

/**
 * Standard functionality in conjunction with time intervals.
 * 
 * @author fritzberger 25.07.2011
 */
public class TimeIntervalUtil {
	
	/**
	 * Minutes-precise "Plus-one-day" logic. JavaDoc see peer method using Date.
	 */
	public static IfDate adjustToDate(IfDate from, IfDate to) {
		// add one day to "to" when "from" is after "to"
		return willAdjustToDate(from, to) ? new IfDate(DateUtils.addDays(to, 1)) : to;
	}
	
	/**
	 * Seconds-precise "Plus-one-day" logic.
	 * When from-time is after (or equal to) to-time, one day is added to to-time.
	 * E.g. for from-date/time 20.4.2011 23:00 and to-date/time 20.4.2011 1:00
	 * the adjusted to-date/time 21.4.2011 1:00 would be returned.
	 * 
	 * @param from the start date/time of the interval. Can be null, then "to" remains unchanged.
	 * @param to the end date/time of the interval. Can be null, then "to" remains unchanged.
	 * @return the adjusted or unchanged to-time.
	 */
	public static Date adjustToDate(Date from, Date to) {
		// add one day to "to" when "from" is after "to"
		return willAdjustToDate(from, to) ? new IfDate(DateUtils.addDays(to, 1)) : to;
	}

	/**
	 * "Plus-one-day" logic.
	 * When from-time is after (or equal to) to-time, one day is added to to-time.
	 * E.g. for from-date/time 20.4.2011 23:00 and to-date/time 20.4.2011 1:00
	 * the adjusted to-date/time 21.4.2011 1:00 would be returned. 
	 * 
	 * CAUTION: IfDate works with minutes, simply cutting off seconds! 
	 * 
	 * @param from the start date/time of the interval. Can be null.
	 * @param to the end date/time of the interval. Can be null.
	 * @return true when the passed to-time will be adjusted, else false.
	 */
	public static boolean willAdjustToDate(IfDate from, IfDate to) {
		return from != null && to != null && from.afterEqualsSoft(to);
	}

	/** Second-precise "Plus-one-day" logic. JavaDoc see peer method using IfDate. */
	public static boolean willAdjustToDate(Date from, Date to) {
		// add one day to "to" when "from" is after "to"
		return from != null && to != null && (from.after(to) || from.equals(to));
	}
}
