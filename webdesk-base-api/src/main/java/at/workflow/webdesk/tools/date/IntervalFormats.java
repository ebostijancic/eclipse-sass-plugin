package at.workflow.webdesk.tools.date;


/**
 * Adds String formattings to base class.
 * 
 * @author sdzuban 10.04.2013
 * @author fritzberger 20.04.2013
 */
public abstract class IntervalFormats extends Interval {

	/** @return interval as dd.MM.yyyy - dd.MM.yyyy */
	public String getDatesAsString() {
		return getIntervalAsString("dd.MM.yyyy");
	}

	/** @return interval as HH:mm:ss - HH:mm:ss */
	public String getTimesAsString() {
		return getIntervalAsString("HH:mm:ss");
	}

	/** @return interval as dd.MM.yyyy HH:mm:ss - dd.MM.yyyy HH:mm:ss */
	public String getIntervalAsString() {
		return getIntervalAsString("dd.MM.yyyy HH:mm:ss");
	}

	/** @return interval as pattern - pattern */
	public String getIntervalAsString(String pattern) {
		return toString(pattern);
	}

}
