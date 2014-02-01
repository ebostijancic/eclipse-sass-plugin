package at.workflow.webdesk.tools.date;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Base class for date intervals offering lot of useful methods.
 * <p/>
 * This interval implementation defines an interval that includes
 * both start- and end-point of the interval, meaning the to-time
 * also belongs to the interval. So two intervals are overlapping
 * when the second interval starts at the end-point of the first.
 * 
 * @author sdzuban 10.04.2013
 * @author fritzberger 20.06.2013 refactoring
 */
public abstract class Interval
{
	/** @return the interval start, never null (fri_2013-08-12: TODO: EditableDateInterval does not conform to that). */
	public abstract Date getFrom();

	/** @return the interval end, never null (fri_2013-08-12: TODO: EditableDateInterval does not conform to that). */
	public abstract Date getTo();

	/** @return a new interval. */
	protected abstract Interval newInterval(Date from, Date to);
	

	private Date from() {
		Date d = getFrom();
		assert d != null;
		return d;
	}

	private Date to() {
		Date d = getTo();
		assert d != null;
		return d;
	}
	
	/** @return is this interval is enclosing the other interval, inclusive boundaries. */
	public boolean enclosing(Interval interval) {
		return isNegative() == false && interval.isNegative() == false &&
				! interval.from().before(from()) && ! interval.to().after(to());
	}

	/** @return is this interval is inside the other interval, inclusive boundaries. */
	public boolean within(Interval interval) {
		return isNegative() == false && interval.isNegative() == false &&
				! from().before(interval.from()) && ! to().after(interval.to());
	}

	/** @return true if this interval overlaps with the given Interval, or they touch each other at some end. */
	public boolean overlaps(Interval interval) {
		return isNegative() == false && interval.isNegative() == false &&
				from().before(interval.to()) && to().after(interval.from());
	}

	/** @return true if this Interval is completely after the given Interval (do not overlap, not even touch). */
	public boolean after(Interval interval) {
		return isNegative() == false && interval.isNegative() == false && from().after(interval.to());
	}

	/** @return true if this Interval is completely before the given Interval (do not overlap, not even touch). */
	public boolean before(Interval interval) {
		return isNegative() == false && interval.isNegative() == false && to().before(interval.from());
	}

	/**
	 * @return interval that is common to both interval1 and interval2 (intersection),
	 * 		can return a negative interval.
	 */
	public Interval crossSection(Interval other) {
		assert other != null;
		
		if (isNegative())
			return this;
		
		if (other.isNegative())
			return other;
		
		Date from = other.from().before(from()) ? from() : other.from();
		Date to = other.to().before(to()) ? other.to() : to();
		
		return newInterval(from, to);
	}

	/**
	 * @return interval that is union of both interval1 and interval2,
	 * 		can return a negative interval when both are negative.
	 * @throw runtime exception if intervals are disjunct and union would be uncontinuous.
	 */
	public Interval union(Interval other) {
		assert other != null;
		
		if (isNegative())
			return other;
		
		if (other.isNegative())
			return this;
		
		Date from = other.from().after(from()) ? from() : other.from();
		Date to = other.to().after(to()) ? other.to() : to();
		if (other.before(this) && other.to().before(from()) || before(other) && to().before(other.from()))
			// this method is widely used (46 matches in workspace)
			// returning negative interval instead of throwing exception should not be done!
			throw new RuntimeException("Union of non-overlapping intervals is not possible.");
		
		return newInterval(from, to);
	}
	
	/**
	 * @return true if the given date is contained in this interval,
	 * 		meaning it is either the start-point, or the end-point,
	 * 		or in between. Mind that the end-point of the interval
	 * 		also belongs to the interval, other than the implementation
	 * 		in HistorizationHelper.isValid()!
	 */
	public boolean containsDate(Date date) {
		return ! date.before(getFrom()) && ! date.after(getTo());
		// this logic comes from tm-module
	}
	
	/**
	 * @return true if the given date is inside this interval but neither its start- nor its end-point.
	 */
	public boolean isDateInside(Date date) {
		return date.after(getFrom()) && date.before(getTo());
	}

	/**
	 * @return true when the given date is either the start-point of this interval,
	 * 		or any point until the end-point, but not the end-point itself.
	 */
	public boolean isValid(Date date)	{
		return ! isFuture(date) && isValidOrFuture(date);
	}
	
	/**
	 * @return true when the given date is either the start-point of this interval,
	 * 		or any point until the end-point, but not the end-point itself.
	 */
	public boolean isValidOrFuture(Date date)	{
		return isNegative() == false && date.before(getTo());
	}
	
	/**
	 * @return true when the given date is either the start-point of this interval,
	 * 		or any point until the end-point, but not the end-point itself.
	 */
	public boolean isFuture(Date date)	{
		return isNegative() == false && date.before(getFrom());
	}
	
	
	/** @return true if "to" is before "from" (not equal), else false. See Jira issue WD-6. */
	public boolean isNegative()	{
		return getTo().before(getFrom());
	}
	
	/** @return true if "to" is after "from" (not equal), else false. See Jira issue WD-6. */
	public boolean isPositive()	{
		return getTo().after(getFrom());
	}
	
	
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object o) {
		if (o == null || o instanceof Interval == false)
			return false;
		
		Interval other = (Interval) o;
		return from().equals(other.from()) && to().equals(other.to());
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return from().hashCode() + 17 * to().hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return toString("dd.MM.yyyy HH:mm:ss");
	}

	public String toString(String dateFormatPattern) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatPattern);
		return (getFrom() != null ? dateFormat.format(getFrom()) : "")+" - "+(getTo() != null ? dateFormat.format(getTo()) : "");
	}
}
