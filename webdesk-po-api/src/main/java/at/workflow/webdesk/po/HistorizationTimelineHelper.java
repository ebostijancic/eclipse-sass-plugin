package at.workflow.webdesk.po;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.tools.api.Historization;

/**
 * The purpose of the <code>HistorizationTimelineHelper</code> 
 * interface is to provide an easy way to achieve a close 
 * time-line (of <code>Historization</code> objects of the same type)
 * <p>
 * See also See also {@link at.workflow.webdesk.po.HistorizationServiceAdapter}  
 * 
 * @author hentner
 *
 */
public interface HistorizationTimelineHelper {

	
	/**
	 * Generates a new {@link at.workflow.webdesk.tools.api.Historization} object, 
	 * or adapts an existing one. The goal of the function is to provide a close timeline 
	 * (without a gap). The result is in most cases an already stored 
	 * <code>Historization</code> object. Even this behaviour is not stringent (e.g. the adapter 
	 * could implement the {@link at.workflow.webdesk.po.HistorizationServiceAdapter#saveObject(Historization)}
	 * in such a way that it only collects <code>Historization</code> objects). 
	 * <p> 
	 * 
	 * @param existingObjects a <code>List</code> of <code>Historization</code> objects. Keep in mind
	 * that all objects should be of the same type, (e.g. <code>PoPersonGroup</code>, <code>PoParentGroup</code>, ..),
	 * unfortunately we are not allowed to use Generics here, due the fact that the implementation is extended and Generics 
	 * cannot handle that. 
	 * @param adapter a Service adapter to create, delete and compare concrete <code>Historization</code> objects. 
	 * @param from the <code>from</code> date of the generated <code>Historization</code> object 
	 * @param to the <code>to</code> date of the generated <code>Historization</code> object  
	 * @return the generated or adapted <code>Historization</code> object. This functionality is not 
	 * fully implemented so far, as the old code didn't supported it. FIXME 
	 */
	Historization generateHistorizationObject(List<? extends Historization> existingObjects, 
			HistorizationServiceAdapter adapter, Date from, Date to);
	
	/**
	 * Checks if the date is the first second of the day
	 * @param date
	 * @return
	 */
	boolean isFirstSecondOfDay(Date date); 
	
	/**
	 * checks if the date is the last second of the day
	 * @param date
	 * @return
	 */
	boolean isLastSecondOfDay(Date date);
	
	/**
	 * @param dateTo end of previous link
	 * @param dateFrom beginning of next link
	 * @return true if dateTo is same as dateFrom or if dateTo is last second of day and dateFrom is first second of day
	 */
	boolean areDatesConsecutive(Date dateTo, Date dateFrom);

	/**
	 * Creates dateFrom that follows dateTo or is same as it
	 * @param dateTo
	 * @return for last second of day the first second of the next day, otherwise the same date
	 */
	Date getDateFrom(Date dateTo);

	/**
	 * Creates dateTo that precedes dateFrom or is same as it
	 * @param dateFrom
	 * @return for firstSecond of the day the last second of previous day, otherwise same date
	 */
	Date getDateTo(Date dateFrom);
}
