package at.workflow.webdesk.tools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;

import at.workflow.webdesk.tools.date.DateTools;

/**
 * <p>
 * Created on 10.10.2005
 * 
 * @author DI hentner (Harald Entner)
 * 
 * Project: webdesk3
 * changed at: 11.11.2005
 * package: at.workflow.webdesk.ta.model
 * compilation unit: IfDate.java
 * </p><p>
 * fri_2010-12-06: this class originally was made for IF-6020 communication, but meanwhile it ...
 * <ul>
 * 	<li>... simplifies the date to minutes, meaning it ignores SECONDs</li>
 * 	<li>... uses the MILLISECOND field for ordering bookings that happen in same minute</li>
 * 	<li>... holds a flag for the fact whether this represents a 24:00 time or not (Java Calendar does not know 24:00)</li>
 * 	<li>... considers that flag on all implementations like equals(), before(), after() and compareTo()</li>
 * 	<li>... provides convenience methods taken from Calendar, which are also provided by DateTools or apache DateUtils.
 * </ul>
 * <p>
 * TODO fri_2010-12-15 After refactoring there is still a problem with IfDate and Date:
 * 		due to the isHour24 flag there will be IfDate objects that do not equal with ANY Date object!
 * </p><p>
 * TODO fri_2010-12-09 The usage of MILLISECOND field as a sort order tag should be replaced by
 * 		a field (member variable) that holds an explicit sort order tag, which must be
 * 		used by the compareTo(Date) implementation.
 * </p><p>
 * TODO fri_2010-12-09 Refactor IfDate to be used only in IF-6020 RMI module.
 * 		Push IfDate functionality to DateTools.
 * </p>
 */
public class IfDate extends Date {
	
	private static final Logger log = Logger.getLogger(IfDate.class);

	private static final long serialVersionUID = -693245337269681995L;

	/**
	 * corresponds to this pattern 'dd.MM.yyyy'
	 */
	public static final int IFDATEFORMATCH = 0;

	public static final String IFDATEFORMAT = "dd.MM.yyyy";

	/**
	 * corresponds to this pattern 'dd.MM.yy'
	 */
	public static final int IFDATEFORMATSHORTCH = 5;

	public static final String IFDATEFORMATSHORT = "dd.MM.yy";

	/**
	 * corresponds to this pattern 'dd.MM.yyyy HH:mm'
	 */
	public static final int IFDATETIMEFORMATCH = 2;

	public static final String IFDATETIMEFORMAT = "dd.MM.yyyy HH:mm";

	/**
	 * corresponds to this pattern 'yyyyMMdd'
	 */
	public static final int IFSQLDATEFORMATCH = 3;

	public static final String IFSQLDATEFORMAT = "yyyyMMdd";

	/**
	 * corresponds to this pattern 'HH:mm'. The date will be today.
	 */
	public static final int IFSQLTIMEFORMATCH = 4;

	public static final String IFSQLTIMEFORMAT = "HH:mm";

	/**
	 * corresponds to this pattern 'dd.MM.yyyy HH:mm:ss:SSSS'
	 */
	public static final int IFLOGFORMATCH = 1;

	public static final String IFLOGFORMAT = "dd.MM.yyyy HH:mm:ss:SSSS";

	public static final String SPECIALTIMEFORMAT = "HHmmss";

	private static final String JSDATEFORMAT = "yyyy,MM,dd";

	
	private static final int TIME_CHAR_LOCATION = IfDate.IFDATETIMEFORMAT.indexOf("HH:mm");	// is 11
	
	
	private boolean isHour24 = false;
	

	/** IfDate construction for "now". */
	public IfDate() {
		setSecondToZero();
	}

	/** IfDate construction from <code>Date.getTime()</code>. */
	public IfDate(long timeMillis) {
		super(timeMillis);
		setSecondToZero();
	}

	public IfDate(Calendar calendarToClone) {
		this(calendarToClone == null ? (Date) null : calendarToClone.getTime());
	}

	/**
	 * When passed date is null, new Date() is used, else this will be a clone of passed date.<br/>
	 */
	public IfDate(Date dateToClone) {
		this(dateToClone == null ? new Date().getTime() : dateToClone.getTime());
		
		if (dateToClone == null)	{
			// fri_2011-01-20 a clone of null can not be the current date, IllegalArgumentException should be thrown
			log.warn("IfDate has been constructed with null - we don't know if this is a bug or a feature!");
			Thread.dumpStack();
		}
	}

	/**
	 * Constructs either a clone of passed date, or 24:00 of the passed day.
	 * This is a special constructor for the artificial 24:00 for IF-6020, which is not identical with 00:00 on next day.
	 * @param date either the day associated to 24:00, or the date to clone.
	 * @param isHour24 when false the date is to be cloned, when true it is to be taken as day base for 24:00.
	 * @exception when day is null.
	 */
	public IfDate(Date date, boolean isHour24) {
		assert date != null : "Illegal value for IfDate constructor: null";
		
		setTime(date.getTime());
		if (isHour24)
			setHour24();
		setSecondToZero();
	}

	/** @param ifdateformat one of IF*CH constants. */
	public IfDate(String value, int ifdateformat) {
		
		switch (ifdateformat) {
		case IfDate.IFDATEFORMATCH:
			setTime( DateTools.parse(value, IfDate.IFDATEFORMAT).getTime());
			break;
			
		case IfDate.IFDATEFORMATSHORTCH:
			setTime(DateTools.parse(value, IfDate.IFDATEFORMATSHORT).getTime());
			break;
			
		case IfDate.IFDATETIMEFORMATCH:		
			// parsing must be first as calling code
			// might catch Parsing exceptions if a non valid string is passed!
            setTime(DateTools.parse(value, IfDate.IFDATETIMEFORMAT).getTime());
            
            final String timeValue = value.substring(TIME_CHAR_LOCATION);

            if (timeValue.equals("24:00")) {
				// when time is 24:00, set this to 23:59 and set D24 flag to true.
				final String dateValue = value.substring(0, TIME_CHAR_LOCATION);
				setTime(DateTools.parse(dateValue,IfDate.IFDATEFORMAT).getTime());
				setHour24();
            }
            break;
			
		case IfDate.IFLOGFORMATCH:
			setTime( DateTools.parse(value,IfDate.IFLOGFORMAT).getTime());
			break;
			
		case IfDate.IFSQLDATEFORMATCH:
			setTime( DateTools.parse(value,IfDate.IFSQLDATEFORMAT).getTime());
			break;
			
		case IfDate.IFSQLTIMEFORMATCH:
			setTime( DateTools.parse(value,IfDate.IFSQLTIMEFORMAT).getTime() );
			int hour = get(Calendar.HOUR_OF_DAY);
			int minute = get(Calendar.MINUTE);
			setTime(new IfDate().getTime());
			set(Calendar.HOUR_OF_DAY, hour);
			set(Calendar.MINUTE, minute);
			break;
			
		default:
			throw new IllegalArgumentException("Given Dateformat is not known: " + ifdateformat + ". Use IfDate values.");
		}
		
		setSecondToZero();
	}
	
	
	private void setHour24()	{
		isHour24 = true;
		set(Calendar.HOUR_OF_DAY, 23);
		set(Calendar.MINUTE, 59);
		set(Calendar.MILLISECOND, 980);
	}

	/**
	 * @return the date in format dd.MM.yyyy
	 */
	public String toDateFormat() {
		return toDateFormat(Locale.getDefault());
	}

	/**
	 * @return the date in format dd.MM.yyyy
	 */
	public String toDateFormat(Locale locale) {
		SimpleDateFormat sdf = new SimpleDateFormat(IfDate.IFDATEFORMAT, locale);
		return sdf.format(this);
	}

	/**
	 * @return the date in format dd.MM.
	 */
	public String toDateFormatWithoutYear(Locale locale) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.", locale);
		return sdf.format(this);
	}

	/**
	 * @return the time of this Date, as HH:mm, with leading zeros.
	 * 		This is NOT the IF-6020 time format!
	 */
	public String toTimeFormat() {
		if (isHour24)
			return "24:00";
		
		SimpleDateFormat sdf = new SimpleDateFormat(IfDate.IFSQLTIMEFORMAT);
		return sdf.format(this);
	}

	/**
	 * @return the date as date + time, format: dd.MM.yyyy HH:mm
	 */
	public String toDateTimeFormat() {
		return toDateTimeFormat(Locale.getDefault());
	}

	/**
	 * @return the date as date + time, format: dd.MM.yyyy HH:mm
	 */
	public String toDateTimeFormat(Locale locale) {
		SimpleDateFormat sdf = new SimpleDateFormat(IfDate.IFDATETIMEFORMAT, locale);
		return sdf.format(this);
	}

	/**
	 * The name of this method refers to IF-6020 SQL, not database SQL.
	 * @return the date as date + time, format: yyyyMMdd
	 */
	public String toSqlDateFormat() {
		SimpleDateFormat sdf = new SimpleDateFormat(IfDate.IFSQLDATEFORMAT);
		return sdf.format(this);
	}

	/**
	 * @return the time as: HHmmss
	 */
	public String toSpecialTimeFormat() {
		SimpleDateFormat sdf = new SimpleDateFormat(IfDate.SPECIALTIMEFORMAT);
		return sdf.format(this);

	}

	/**
	 * TODO: JS = JavaScript?
	 * @return the time as: yyyy,MM,dd
	 */
	public String toJSDateFormat() {
		SimpleDateFormat sdf = new SimpleDateFormat(IfDate.JSDATEFORMAT);
		return sdf.format(this);
	}

	/**
	 * Most likely made for IF-6020 time format.
	 * @return the time, format: HH:mm, leading "00" is replaced by " 0", leading "01:" is replaced by " 1:".
	 */
	public String toSqlTimeFormat() {
		String res = toTimeFormat();
		
		if (res.indexOf("00") == 0)
			res = res.replaceFirst("00", " 0");
		else
		if (res.indexOf("0") == 0 && res.indexOf(":") == 2)
			res = " " + res.substring(1);
		
		return res;
	}

	public String toLogFormat() {
		SimpleDateFormat sdf = new SimpleDateFormat(IfDate.IFLOGFORMAT);
		return sdf.format(this);
	}

	/**
	 * @return a new IfDate containing 0 time and the year/month/day of this IfDate.
	 */
	public IfDate getDateOnly() {
		Calendar gc = Calendar.getInstance();
		gc.setTime(this);
		gc.set(Calendar.HOUR_OF_DAY, 0);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.MILLISECOND, 0);	// fri_2011-02-15: this is used to sort dates
		return new IfDate(gc.getTime());
	}

	public void add(int field, int i) {
		if (field == Calendar.SECOND)
			throw new IllegalArgumentException("Can not add to SECOND in IfDate");
		
		Calendar gc = Calendar.getInstance();
		gc.setTime(this);
		gc.add(field, i);
		setTime(gc.getTime().getTime());
	}

	/** 
	 * @param field one of Calendar.XXX field constants.
	 * @return the value of the named field in this Date object.
	 */
	public int get(int field) {
		Calendar gc = Calendar.getInstance();
		gc.setTime(this);
		return gc.get(field);
	}

	/** 
	 * Sets the time of this Date object.
	 * @param field one of Calendar.XXX field constants.
	 * @param value the value to set into the named field.
	 */
	public void set(int field, int value) {
		if (field == Calendar.SECOND)
			throw new IllegalArgumentException("Can not set SECOND in IfDate");
		
		internalSet(field, value);
	}

	private void setSecondToZero()	{
		// fri_2012-08-23: rounding 30 seconds to next minute not done by now, too dangerous, could create future dates
		//if (get(Calendar.SECOND) >= 30)	// 12:00:30 will be rounded to 12:01:00
		//	internalSet(Calendar.MINUTE, get(Calendar.MINUTE) + 1);
		
		internalSet(Calendar.SECOND, 0);
		
	}

	private void internalSet(int field, int value) {
		Calendar gc = Calendar.getInstance();
		gc.setTime(this);
		gc.set(field, value);
		setTime(gc.getTimeInMillis());
	}

	/**
	 * Sets the year/month/day of passed date into this one, but not its time.
	 */
	public void setDateOnly(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		set(Calendar.YEAR, c.get(Calendar.YEAR));
		set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
		set(Calendar.MONTH, c.get(Calendar.MONTH));
	}

	public IfDate getDateTimeWithoutMs() {
		Calendar gc = Calendar.getInstance();
		gc.setTime(this);
		gc.set(Calendar.MILLISECOND, 0);
		return new IfDate(gc.getTime());
	}

	/**
	 * @return true if passed date is equal to this, ignoring SECOND and MILLISECOND,
	 * 		but considering isHour24 flag.
	 */
	public boolean equalsSoft(IfDate date) {
		if (date == null)
			return false;

		if (get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
				get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
				get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH) &&
				get(Calendar.HOUR_OF_DAY) == date.get(Calendar.HOUR_OF_DAY) &&
				get(Calendar.MINUTE) == date.get(Calendar.MINUTE) &&
				isHour24 == date.isHour24)
			return true;
		
		return false;
	}

	/**
	 * Ignores the SECOND but not the MILLISECOND.
	 * NOTE: The MILLISECOND is currently used to express the order of bookings.
	 * For example see <code>TaBookingDAOImpl.loadLastBooking()</code>, or
	 * 		<code>IfDataBuchRecord.setBudatum</code>, search for ".set(Calendar.MILLISECOND"
	 */
	@Override
	public boolean equals(Object other) {
		if (other == null || other instanceof Date == false)
			return false;

		if (other instanceof IfDate == false)
			return super.equals(other);
		
		IfDate otherIfDate = (IfDate) other;
		return get(Calendar.YEAR) == otherIfDate.get(Calendar.YEAR) &&
				get(Calendar.MONTH) == otherIfDate.get(Calendar.MONTH) &&
				get(Calendar.DAY_OF_MONTH) == otherIfDate.get(Calendar.DAY_OF_MONTH) &&
				get(Calendar.HOUR_OF_DAY) == otherIfDate.get(Calendar.HOUR_OF_DAY) &&
				get(Calendar.MINUTE) == otherIfDate.get(Calendar.MINUTE) &&
				// leave out SECOND because 6020 does not know seconds
				get(Calendar.MILLISECOND) == otherIfDate.get(Calendar.MILLISECOND) &&
				isHour24 == otherIfDate.isHour24;
	}

	/** Not overridden because the super impl is sufficient, and further isHour24 flag will be very rare. */
	@Override
	public int hashCode()	{
		return super.hashCode();
	}
	
	/** Handles the special case with isHour24 flag. */
	@Override
	public int compareTo(Date other) {
		int result = super.compareTo(other);
		if (other instanceof IfDate == false)
			return result;
		
		IfDate otherIfDate = (IfDate) other;
		// check special case when one is normal and other isHour24 and on same day
		if (isHour24 == otherIfDate.isHour24 || getDateOnly().equals(otherIfDate.getDateOnly()) == false)
			return result;
		
		// "this is before" generates -1, equal time-millis generates 0, "this is after" generates +1
		return isHour24 ? +1 : -1;
	}
	
	/**
	 * @return true if this date is before (and not equalSoft to) given date.
	 */
	@Override
	public boolean before(Date date) {
		if (date instanceof IfDate == false)
			return super.before(date);
		
		IfDate ifDate = (IfDate) date;
		return false == equalsSoft(ifDate) && compareTo(ifDate) < 0;
	}

	/**
	 * @return true if this date is after (and not equalSoft to) given date.
	 */
	@Override
	public boolean after(Date date) {
		if (date instanceof IfDate == false)
			return super.after(date);
		
		IfDate ifDate = (IfDate) date;
		// fri_2010-12-10 avoiding cases where a date could be neither equal nor before nor after
		return false == equalsSoft(ifDate) && compareTo(ifDate) > 0;
	}

	/** @return true if this date is before or equalSoft to passed date. */
	public boolean beforeEqualsSoft(IfDate date) {
		if (equalsSoft(date))
			return true;
		
		return before(date);
	}

	/** @return true if this date is after or equalSoft to passed date. */
	public boolean afterEqualsSoft(IfDate date) {
		if (equalsSoft(date))
			return true;
		
		return after(date);
	}

	public long diff(Date date) {
		return diff(date, false);
	}

	/**
	 * <p>
	 * Calculates the difference between this <code>IfDate</code> object
	 * and the given <code>date</code>.
	 * <p>
	 * If the <code>dateParam</code> is after <code>this</code> in a timely
	 * fashion, the result will be positive and negative otherwise. If they
	 * 're equal, <code>0</code> will be returned.
	 * <p>
	 * Conversion
	 * <ul>
	 * <li>60 Hour
	 * <li>1440 Day
	 * <li>Pay attention on Month and year, as the minutes differ.
	 * </ul>
	 * 
	 * @param dateParam <code>java.util.Date</code> object
	 * @return the difference between the two dates in minutes.
	 */
	public long diff(Date dateParam, boolean removeTime) {
		IfDate date = new IfDate(dateParam);
		IfDate thisDate = new IfDate(this);
		if (removeTime) {
			date = date.getDateOnly();
			thisDate = thisDate.getDateOnly();
		}
		else {
			// very similar -> old code, but dangerous to change (ev.)// TODO
			int hour = date.get(Calendar.HOUR_OF_DAY);
			int minute = date.get(Calendar.MINUTE);
			date.setTime(getTime());
			date.set(Calendar.HOUR_OF_DAY, hour);
			date.set(Calendar.MINUTE, minute);
		}

		long dateWoMs = date.getTime() / (1000 * 60); // minutes
		long thisWoMs = thisDate.getTime() / (1000 * 60); // minutes

		return dateWoMs - thisWoMs;
	}

	public String toFormat(String string) {
		SimpleDateFormat sdf = new SimpleDateFormat(string);
		return sdf.format(this);
	}

	public IfDate getLastMomentOfDay() {
		setTime(DateTools.lastMomentOfDay(new Date(getTime())).getTime());
		return this;
	}
	
	
}
