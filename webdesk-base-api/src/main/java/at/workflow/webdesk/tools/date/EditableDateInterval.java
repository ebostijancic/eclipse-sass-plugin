package at.workflow.webdesk.tools.date;

import java.util.Date;

/**
 * This is simple, mutable data transfer object for from and to dates of an interval.
 * 
 * fri_2013-08-12: TODO: this class does not conform to the Interval contract that getFrom() and getTo() will never be null.
 * 
 * @author sdzuban 04.04.2013
 */
public class EditableDateInterval extends IntervalFormats {

	private Date from, to;

	public EditableDateInterval() {
	}
	
	public EditableDateInterval(Date from) {
		this(from, null);
	}
	
	public EditableDateInterval(Date from, Date to) {
		checkFromBeforeTo(from, to);
		
		this.from = from;
		this.to = to;
	}

	@Override
	public Date getFrom() {
		return from;
	}
	public void setFrom(Date date) {
		checkFromBeforeTo(date, to);
		from = date;
	}
	@Override
	public Date getTo() {
		return to;
	}
	public void setTo(Date date) {
		checkFromBeforeTo(from, date);
		this.to = date;
	}

	@Override
	protected Interval newInterval(Date from, Date to) {
		return new EditableDateInterval(from, to);
	}
	
//	---------------------- PRIVATE METHODS -----------------------------
	
	private void checkFromBeforeTo(Date from, Date to) {
		if (from != null && to != null && from.after(to))
			throw new IllegalArgumentException("From date " + from + " must be before or same as to date " + to + ".");
	}

}
