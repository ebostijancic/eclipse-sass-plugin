package at.workflow.webdesk.tools.api;

import java.util.Date;

import at.workflow.webdesk.tools.date.Interval;

/**
 * A <code>Historization</code> object has a certain validity time range.
 * This interface defines a set of methods that can be used to 
 * access the start and end date of the time range the object is valid.
 * <p/>
 * Mind that in Webdesk there are two implementations of this interface:
 * PoHistorization historicizes with millisecond precision, PoDayHistorization historicizes with day precision.
 * 
 * @author hentner 
 * @author fritzberger
 */
public interface Historization extends PersistentObject {
	
	/** Name of the property denoting the valid-from date. */
	public static final String VALID_FROM = "validfrom";
	
	/** Name of the property denoting the valid-from date. */
	public static final String VALID_TO = "validto";
	
	/**
	 * Sets the start date/time to <code>validfrom</code>.
	 * dep. use PoHistorization when needing the setter!
	 * cannot be deprecated - HistorizationTimelineHelper and similar classes
	 * are defined using Historization and need definitely to set validfrom and validto
	 * and definitively cannot use the indirect way of PoHistorization via
	 * generateUsefulValidfrom/to() unless all of them will be refactored.
	 */
	void setValidfrom(Date validfrom);
	
	/**
	 * @return <code>Date</code> representing the date on which the object becomes valid.
	 */
	Date getValidfrom();
	
	/**
	 * Sets the date on which the object becomes invalid to <code>validto</code>. 
	 * dep. use PoHistorization when needing the setter!
	 * cannot be deprecated - HistorizationTimelineHelper and similar classes
	 * are defined using Historization and need definitely to set validfrom and validto
	 * and definitively cannot use the indirect way of PoHistorization via
	 * generateUsefulValidfrom/to() unless all of them will be refactored.
	 */
	void setValidto(Date validto);
	
	/**
	 * @return the <code>Date</code> on which the object becomes invalid.
	 */
	Date getValidto();
	
	/** @return date interval containing both the validfrom and validto dates, 
	 * 	returns null if validto is not after validfrom */
	Interval getValidity();

	/**
	 * Historicizes the entity as of the current moment
	 */
	void historicize();
}
