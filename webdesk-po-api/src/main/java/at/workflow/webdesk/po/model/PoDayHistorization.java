package at.workflow.webdesk.po.model;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.date.HistorizationHelper;

/**
 * Base-class for all day-exact historized persistent objects within Webdesk.
 * PoHistorization historicizes with day-precision. See
 * {@link at.workflow.webdesk.tools.date.HistorizationHelper}.
 * It uses <code>HistorizationHelper.generateUsefulValidXxxDay()</code>
 * to customize arguments passed to setValidfrom() or setValidto().
 * 
 * @author sdzuban 11.12.2013
 */
public abstract class PoDayHistorization extends PoHistorization 
{

	/**
	 * As day historized object must live at least 1 day,
	 * we perform historization of objects with a time point that is
	 * "now - 1 day". This constant gives the amount in seconds.
	 */
	public static final int MINIMAL_LIFETIME_SECONDS = 24 * 3600;
	
	/**
	 * Minimal object lifetime in milliseconds.
	 * Do not use for sleep in unit tests - it is 24 hours + 10 milliseconds.
	 * See MINIMAL_OBJECT_LIFETIME_SECONDS. This adds ten millisecond.
	 */
	@Deprecated
	public static final int RECOMMENDED_MINIMAL_LIFETIME_MILLIS =
			MINIMAL_LIFETIME_SECONDS * 1000 + 10;
			// 10 millis for minimal lifetime, remainder for inexact time measurement on Hudson
	
	@Override
	protected Date getDefaultValidFrom() {
		return DateTools.today();
	}
	
	/**
	 * This delegates to <code>HistorizationHelper.generateUsefulValidFromDay(validFrom)</code>.
	 * Please read that JavaDoc to avoid misunderstandings about what happens here.
	 * @see at.workflow.webdesk.tools.date.HistorizationHelper#generateUsefulValidFromDay(java.util.Date)
	 */
	@Override
	public void setValidfrom(Date validFrom) {
		super.setValidfrom(HistorizationHelper.generateUsefulValidFromDay(validFrom));
	}

	/**
	 * This delegates to <code>HistorizationHelper.generateUsefulValidToDay(validTo)</code>.
	 * Please read that JavaDoc to avoid misunderstandings about what happens here.
	 * @see at.workflow.webdesk.tools.date.HistorizationHelper#generateUsefulValidToDay(java.util.Date)
	 */
	@Override
	public void setValidto(Date validTo) {
		super.setValidto(HistorizationHelper.generateUsefulValidToDay(validTo));
	}

	@Override
	public void historicize() {
		setValidto(DateUtils.addDays(DateTools.now(), -1));
	}
}
