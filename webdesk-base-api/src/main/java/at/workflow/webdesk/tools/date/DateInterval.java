package at.workflow.webdesk.tools.date;

import java.util.Date;

/**
 * This is simple, immutable data transfer object for "from" and "to" dates of an interval.
 * Negative intervals (from after to) can occur when a future validity is historiziced, then "now" is put
 * into field "to", but "from" is still in future.
 * 
 * @author ggruber
 */
public class DateInterval extends IntervalFormats {

	private Date from;
	private Date to;

	/**
	 * Default constructor.
	 * @param from normally before or equal to "to", but can be after when interval is negative.
	 * @param to normally after or equal to "from", but can be before when interval is negative.
	 * @throws RuntimeException when one arguments is null.
	 */
	public DateInterval(Date from, Date to) throws RuntimeException {
		if (from == null)
			throw new RuntimeException("first parameter \"from\" must not be null");
		if (to == null)
			throw new RuntimeException("second parameter \"to\" must not be null");
		
		this.from = from;
		this.to = to;
	}

	@Override
	public Date getFrom() {
		return from;
	}

	@Override
	public Date getTo() {
		return to;
	}

	@Override
	protected Interval newInterval(Date from, Date to) {
		return new DateInterval(from, to);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return super.getDatesAsString();
	}
}