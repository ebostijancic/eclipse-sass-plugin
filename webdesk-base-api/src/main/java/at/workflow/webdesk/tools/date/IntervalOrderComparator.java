package at.workflow.webdesk.tools.date;

import java.util.Comparator;

/**
 * Compares interval based on their "from" from left/past to right/future.
 * 
 * @author sdzuban 09.09.2013
 */
public class IntervalOrderComparator implements Comparator<Interval> {


	/**
	 * This uses the valid-from field to estimate sort order.
	 * If comparison of valid-from field yields 0, valid-to fields are compared. 
	 * This ensures deterministic ordering of entities starting at the same time.
	 */
	@Override
	public int compare(Interval i1, Interval i2) {
		
		final int fromResult = i1.getFrom().compareTo(i2.getFrom());
		if (fromResult == 0)
			return i1.getTo().compareTo(i2.getTo());
		
		return fromResult;
	}

}
