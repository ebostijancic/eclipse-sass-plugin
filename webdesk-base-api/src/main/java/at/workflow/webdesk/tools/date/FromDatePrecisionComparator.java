package at.workflow.webdesk.tools.date;

import java.util.Comparator;

import at.workflow.webdesk.tools.date.DateTools.DatePrecision;

/**
 * @author fritzberger 20.06.2013
 */
/** compares from - dates of DateIntervals with given precision */
public class FromDatePrecisionComparator implements Comparator<Interval> {

	private DatePrecision precision;

	public FromDatePrecisionComparator(DatePrecision precision) {
		this.precision = precision;
	}

	/** {@inheritDoc} */
	@Override
	public int compare(Interval i1, Interval i2) {
		return DateTools.datesCompare(i1.getFrom(), i2.getFrom(), precision);
	}
}