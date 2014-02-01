package at.workflow.webdesk.tools.date;

import java.util.Comparator;

/**
 * @author fritzberger 20.06.2013
 */
/** compares from - dates */
public class FromDateComparator implements Comparator<Interval> {
	/** {@inheritDoc} */
	@Override
	public int compare(Interval i1, Interval i2) {
		return i1.getFrom().compareTo(i2.getFrom());
	}
}