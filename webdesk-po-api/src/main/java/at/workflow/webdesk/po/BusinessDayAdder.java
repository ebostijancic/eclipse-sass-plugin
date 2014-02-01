package at.workflow.webdesk.po;

import java.util.Date;

public interface BusinessDayAdder {
	/**
	 * 
	 * @return business day that lies businessDays before from-day in any region
	 */
	Date addBusinessDays(Date date, int businessDays);
	
	/** 
	 * @return next business day after from-day in any region
	 */
	Date getNextBusinessDay(Date from);
}
