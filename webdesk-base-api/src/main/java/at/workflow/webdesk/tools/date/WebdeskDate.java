package at.workflow.webdesk.tools.date;

import java.text.SimpleDateFormat;

/**
 * Contains utilities in relation with Date concerning the Webdesk application (conveniences, standards).
 * 
 * @author fritzberger 08.05.2013
 */
public final class WebdeskDate {
	
	public static final String DATESHORT_MASK = "dd.MM.yy";
	public static final SimpleDateFormat DAYSHORT_FORMAT = new SimpleDateFormat(DATESHORT_MASK);
	
	public static final String DATE_MASK = "dd.MM.yyyy";
	public static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat(DATE_MASK);
	
	public static final String TIME_MASK = "HH:mm";
	public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat(TIME_MASK);

	public static final String TIMESECONDS_MASK = TIME_MASK + ":ss";
	public static final SimpleDateFormat TIMESECONDS_FORMAT = new SimpleDateFormat(TIMESECONDS_MASK);
	
	public static final String DATETIME_MASK = DATE_MASK+" "+TIME_MASK;
	public static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat(DATETIME_MASK);
	
	public static final String DATETIMESECONDS_MASK = DATE_MASK+" "+TIME_MASK+":ss";
	public static final SimpleDateFormat DATETIMESECONDS_FORMAT = new SimpleDateFormat(DATETIMESECONDS_MASK);
	
	public static final String DATETIMEFULL_MASK = "EEE MMM dd "+ TIMESECONDS_MASK + " zzz yyyy";
	public static final SimpleDateFormat DAYTIMEFULL_FORMAT = new SimpleDateFormat(DATETIMEFULL_MASK);

	
	public WebdeskDate() {}	// do not instantiate
}
