package at.workflow.webdesk.po.timeline;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author sdzuban 06.04.2013
 */
public class TimelineDate implements Comparable<TimelineDate> {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,s");
	
	public enum DateType { FROM, TO };
	private Date date;
	private DateType type;

	public TimelineDate(Date date, DateType type) {
		this.date = date;
		this.type = type;
	}

	public Date getDate() {
		return date;
	}
	public DateType getDateType() {
		return type;
	}

	/**
	 * If date is same than FROM is after TO 
	 * (first something must end, than a new beginning is possible) 
	 * This is to cope with possible same validfrom and validto dates in past and in now(); 
	 */
	/** {@inheritDoc} */
	@Override
	public int compareTo(TimelineDate other) {
		int dateResult = date.compareTo(other.getDate());
		if (dateResult == 0) {
			if (type == other.getDateType())
				return 0;
			return type == DateType.TO ? -1 : 1;
		}
		return dateResult;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof TimelineDate && 0 == compareTo((TimelineDate) obj);
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[" + type + " " + sdf.format(date) + "]";
	}
}
