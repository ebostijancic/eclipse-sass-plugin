package at.workflow.webdesk.po.model;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.commons.lang.time.DateUtils;

import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.date.DateInterval;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.date.HistorizationHelper;
import at.workflow.webdesk.tools.date.Interval;

/**
 * Base-class for all historized persistent objects within Webdesk.
 * PoHistorization historicizes with day-precision in the future 
 * and with millisecond precision today and in the past. See
 * {@link at.workflow.webdesk.tools.date.HistorizationHelper}.
 * It uses <code>HistorizationHelper.generateUsefulValidXxx()</code>
 * to customize arguments passed to setValidfrom() or setValidto().
 * 
 * @author hentner, ggruber, ebostijancic, fritzberger
 */
@MappedSuperclass
public abstract class PoHistorization extends PoBase implements Historization
{
	/**
	 * As some databases do not support time precision below 1 second,
	 * we perform historization of objects with a time point that is
	 * "now - X seconds". This constant gives the amount of X in seconds.
	 */
	public static final int MINIMAL_LIFETIME_SECONDS = 1;
	
	/**
	 * Recommended sleep milliseconds after object creation in unit tests.
	 * See MINIMAL_OBJECT_LIFETIME_SECONDS. This adds one millisecond.
	 */
	public static final int RECOMMENDED_MINIMAL_LIFETIME_MILLIS =
			MINIMAL_LIFETIME_SECONDS * 1000 + 10;
			// 1 milli for minimal lifetime, remainder for inexact time measurement on Hudson
	
	@Column(nullable=false)
	private Date validfrom = getDefaultValidFrom();
	
	@Column(nullable=false)
	private Date validto = new Date(DateTools.INFINITY_TIMEMILLIS);

	@Transient
	protected Date getDefaultValidFrom() {
		return new Date();
	}
	
	@Override
	public Date getValidfrom() {
		return typedValidityDate(validfrom);
	}

	/**
	 * This delegates to <code>HistorizationHelper.generateUsefulValidFrom(validFrom)</code>.
	 * Please read that JavaDoc to avoid misunderstandings about what happens here.
	 * @see at.workflow.webdesk.tools.date.HistorizationHelper#generateUsefulValidFrom(java.util.Date)
	 */
	@Override
	public void setValidfrom(Date validFrom) {
		this.validfrom = HistorizationHelper.generateUsefulValidFrom(validFrom);
	}

	@Override
	public Date getValidto() {
		return typedValidityDate(validto);
	}

	/**
	 * This delegates to <code>HistorizationHelper.generateUsefulValidTo(validTo)</code>.
	 * Please read that JavaDoc to avoid misunderstandings about what happens here.
	 * @see at.workflow.webdesk.tools.date.HistorizationHelper#generateUsefulValidTo(java.util.Date)
	 */
	@Override
	public void setValidto(Date validTo) {
		this.validto = HistorizationHelper.generateUsefulValidTo(validTo);
	}

	
	/**
	 * @return interval containing both the validfrom and validto dates as references,
	 * 		returns null if validfrom is not before validto.
	 */
	@Override
	@Transient
	public Interval getValidity() {
		return new DateInterval(getValidfrom(), getValidto());	// TODO: do not return null, see WD-6
	}
	

	/** @return true when the validTo date is <code>DateTools.INFDATE</code>. */
	@Transient
	public boolean isValidtoNull()	{
		return validto.getTime() == DateTools.INFINITY_TIMEMILLIS;
	}

	/**
	 * Checks if given validityDate is a a Timestamp object. and converts it to a 
	 * Date object if so. As equals() fails when comparing date with Timestamp, this 
	 * method can be used to get equals() working.
	 * 
	 * @param validityDate - Date object (can be Timestamp too).
	 * @return always a Date object, and if validityDate is Timestamp then a new Date
	 * 		object with same milliseconds is returned.
	 */
	private Date typedValidityDate(Date validityDate) {
		if (validityDate instanceof Timestamp)
			return new Date(validityDate.getTime());
		return validityDate;
	}

	/** @return true if this object is valid "now". */
	@Transient
	public boolean isValid()	{
		return isValid(null);
	}
	
	/** @return true if this object is valid at given time point. */
	@Transient
	public boolean isValid(Date validityDate)	{
		if (validityDate == null)
			validityDate = DateTools.now();
		return HistorizationHelper.isValid(this, validityDate);
	}

	@Override
	@Transient
	public void historicize() {
		setValidto(DateUtils.addSeconds(DateTools.now(), -MINIMAL_LIFETIME_SECONDS));
	}
 }
