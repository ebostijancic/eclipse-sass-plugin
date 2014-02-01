package at.workflow.webdesk.po.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import at.workflow.webdesk.po.HistorizationServiceAdapter;
import at.workflow.webdesk.po.HistorizationTimelineHelper;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.date.DateTools.DatePrecision;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.lang.time.DateUtils;

/**
 * A {@link at.workflow.webdesk.po.HistorizationTimelineHelper} Implementation
 * that is able to handle <code>PoParentGroup</code> objects.
 * <p>
 * 
 * See also {@link at.workflow.webdesk.po.HistorizationServiceAdapter}
 * 
 * 
 * @author hentner
 *
 */
public class HistorizationTimelineHelperImpl implements HistorizationTimelineHelper {
	
	
	/* (non-Javadoc)
	 * @see at.workflow.webdesk.po.HistorizationTimelineHelper#generateHistorizationObject(java.util.List, at.workflow.webdesk.po.HistorizationServiceAdapter, java.util.Date, java.util.Date)
	 */
	@Override
	public Historization generateHistorizationObject(List<? extends Historization> existingObjects, 
			HistorizationServiceAdapter adapter, Date from, Date to) {
		
		if (existingObjects == null)
			throw new IllegalArgumentException("Non-null existing object list must be provided");
		
		if (adapter == null)
			throw new IllegalArgumentException("Non-null adapter must be provided");
		
		Historization newObject = adapter.generateEmptyObject(from, to);
		
		
		Iterator<?> histObjectsI = existingObjects.iterator();
        if (!histObjectsI.hasNext()) {
         		// no relations exists
         		newObject.setValidfrom(from);
         		newObject.setValidto(to);
        } else {
        	// existing relations 
        	while (histObjectsI.hasNext()) {
        		Historization existingHistObject = (Historization) histObjectsI.next();
        		
        		if (existingHistObject.getValidfrom().equals(from) && equalsTo(existingHistObject.getValidto(), to)) {
        			//   intervalls are the same
        			//   new  		 |================================|
        			//   exist       x================================x
        			//
        			// we delete the old entry and generate a new one 
        			//	-> historization is lost 
        			//	-> formerly e.g. only the parentGroup changed, the historization was lost in that case too
        			
        			
        			if (!adapter.isStructurallyEqual(existingHistObject, newObject)) {
        				// delete the old entry and generate a new one (structurally correct)
        				newObject.setValidfrom(from);
            			newObject.setValidto(to);
            			adapter.deleteObject(existingHistObject);
//            			adapter.saveObject(newObject);  // sd_2013 not necessary, saved at the end
        			} else 
        				// we do nothing as they are structurally and timely the same
        				newObject=null;
        			
        			break;
        			
        		} else if (existingHistObject.getValidfrom().before(from) && beforeTo(from, existingHistObject.getValidto()) 
        				&& beforeTo(existingHistObject.getValidto(), to)) {
        			//   start before and END within
        			//   new  		 |================================|
        			//   exist    |=================|
        			//
        			if (adapter.isStructurallyEqual(existingHistObject, newObject)) {
        				// we extend the lifecycle of the existingHistObject 
        				existingHistObject.setValidto(to);
        				newObject=null;
        			} else {
        				// we shorten the lifecycle of the existingHistObject, as they are structurally not equal 
        				existingHistObject.setValidto(getDateTo(from));
        			}
        			adapter.saveObject(existingHistObject);
        			
        		} else if (existingHistObject.getValidfrom().after(from) &&  beforeTo(existingHistObject.getValidto(), to)) {
        			//   start after and END within
        			//   new  		 |================================|
        			//   exist            |=================|
        			//
        			//   ===> delete
        			//
        			adapter.deleteObject(existingHistObject);
        			
            	} else if (existingHistObject.getValidfrom().after(from) && equalsTo(existingHistObject.getValidto(), to)) {
            		//   start after and END at same point
            		//   new  		 |================================|
            		//   exist                           |============x
            		// 
        			//   ===> delete
        			//
        			adapter.deleteObject(existingHistObject);
        			
            	} else if (existingHistObject.getValidfrom().equals(from) && beforeTo(existingHistObject.getValidto(), to)) {
            		//   start at same point and END after 
            		//   new  		 |================================|
            		//   exist       x============|
            		// 
            		//   ===> delete
            		//
            		adapter.deleteObject(existingHistObject);
            	} else if (existingHistObject.getValidfrom().after(from) && beforeTo(existingHistObject.getValidfrom(), to) &&
            			beforeTo(to, existingHistObject.getValidto())) {
        			//   start after and END afterwards
        			//   new  		 |================================|
        			//   exist                           |=================|
            		// 
            		//  ==> start with old link at validto of new link
            		existingHistObject.setValidfrom(getDateFrom(to));
        			adapter.saveObject(existingHistObject);
            	} else if (existingHistObject.getValidfrom().before(from) && beforeTo(to, existingHistObject.getValidto())) {
        			//   existing overlapps new one
        			//   new  		 |================================|
        			//   exist     |======================================|

            		if (adapter.isStructurallyEqual(existingHistObject, newObject)) {
            			// do nothing
            			newObject = null;
            			break;
            		}
            		
					Historization newLink = adapter.copyHistObject(existingHistObject);
					
					newLink.setValidto(existingHistObject.getValidto());
					newLink.setValidfrom(getDateFrom(to));
					
					existingHistObject.setValidto(getDateTo(from));
					adapter.saveObject(existingHistObject);
					adapter.saveObject(newLink);
					
            	} else if (existingHistObject.getValidfrom().equals(from) && beforeTo(to, existingHistObject.getValidto())) {

            		//   start at same point and END before 
            		//   new  		 |================================|
            		//   exist       x=====================================|
            		// 
            		//   ===> overwrite existing with new, append old afterwards 
            		//
            		if (adapter.isStructurallyEqual(existingHistObject, newObject)) {
            			newObject = null;
            			// existing must be retained to prevent group-free time at the end
            			
            		} else {
            			// ==> restart existing one after new one
            			newObject.setValidfrom(from);
            			newObject.setValidto(to);
            			existingHistObject.setValidfrom(getDateFrom(to));
            			adapter.saveObject(existingHistObject);
            		}

            	} else if (existingHistObject.getValidfrom().before(from) && equalsTo(existingHistObject.getValidto(), to)) {
        			//   existing overlapps ends with same point that new one
        			//   new  		 |================================|
        			//   exist     |==================================x

            		if (adapter.isStructurallyEqual(existingHistObject, newObject)) {
            			// nothing to do
            			newObject = null;
            			break;
            		}
            		
					// ==> end existing one
					existingHistObject.setValidto(getDateTo(from));
					adapter.saveObject(existingHistObject);
            	}
        	}
        }
        // WDHREXPERT-531 if the newObject is PoDayHistorization object it can gain negative validity
        // when from and to dates are on the same day
        if (newObject != null && ! newObject.getValidity().isPositive())
        	newObject = null;
        
        if (newObject != null) {
            // Insert a new relation between parent and child, but check for previous inserted 
            // changes (is not needed until a relation with a previous given end date can be inserted
            adapter.saveObject(newObject);
            // FIXME the adaption of the lists of currently loaded objects should maybe take place in the adapter as well 
            //child.getParentGroups().add(newObject);
            //parent.getChildGroups().add(newObject);
            // the fix must be done in specific adapters because here different timelines can be handled, not only parent groups
        }
        // at the moment, this can be null! 
		return newObject; 
	}
	
	/**
	 * compares 2 to-dates ignoring milliseconds
	 * @param date1
	 * @param date2
	 * @return
	 */
	private boolean equalsTo(Date date1, Date date2) {
		
// TODO	return DateTools.datesAreEqual(date1, date2, DatePrecision.SECOND);
		
		Calendar cal1 = GregorianCalendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = GregorianCalendar.getInstance();
		cal2.setTime(date2);
		
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
			&& cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
			&& cal1.get(Calendar.HOUR_OF_DAY) == cal2.get(Calendar.HOUR_OF_DAY)
			&& cal1.get(Calendar.MINUTE) == cal2.get(Calendar.MINUTE)
			&& cal1.get(Calendar.SECOND) == cal2.get(Calendar.SECOND);
	}
	
	/**
	 * determines whether date1 is prior date2, ignores milliseconds
	 * @param date1
	 * @param date2
	 * @return
	 */
	private boolean beforeTo(Date date1, Date date2) {
		
// TODO	return DateTools.datesCompare(date1, date2, DatePrecision.SECOND) < 0;
		
		Calendar cal1 = GregorianCalendar.getInstance();
		cal1.setTime(date1);
		cal1.set(Calendar.MILLISECOND, 0);
		Calendar cal2 = GregorianCalendar.getInstance();
		cal2.setTime(date2);
		cal2.set(Calendar.MILLISECOND, 0);
		
		return cal1.getTime().before(cal2.getTime());
	}
	
	/**
	 * checks if the date is the last second of the day
	 * @param date
	 * @return
	 */
	@Override
	public boolean isLastSecondOfDay(Date date) {
		
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(date);
		
		return cal.get(Calendar.HOUR_OF_DAY) == 23
			&& cal.get(Calendar.MINUTE) == 59
			&& cal.get(Calendar.SECOND) == 59;
	}
	
	/**
	 * Checks if the date is the first second of the day
	 * @param date
	 * @return
	 */
	@Override
	public boolean isFirstSecondOfDay(Date date) {
		
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(date);
		
		return cal.get(Calendar.HOUR_OF_DAY) == 0
		&& cal.get(Calendar.MINUTE) == 0
		&& cal.get(Calendar.SECOND) == 0;
	}
	
	/**
	 * changes last second of day to first second of the next day
	 * @param dateTo
	 * @return
	 */
	@Override
	public Date getDateFrom(Date dateTo) {
		
		if (isLastSecondOfDay(dateTo)) {

			Calendar cal = GregorianCalendar.getInstance();
			cal.setTime(dateTo);
			cal.add(Calendar.DAY_OF_YEAR, 1);
			return DateTools.dateOnly(cal.getTime());
		}
		
		return dateTo;
	}
	
	/**
	 * changes first second of the day 
	 * to the last second of the previous day
	 * @param dateFrom
	 * @return
	 */
	@Override
	public Date getDateTo(Date dateFrom) {
		
		if (isFirstSecondOfDay(dateFrom)) {

			Calendar cal = GregorianCalendar.getInstance();
			cal.setTime(dateFrom);
			cal.add(Calendar.DAY_OF_YEAR, -1);
			return DateTools.lastMomentOfDay(cal.getTime());
		}
		
		return dateFrom;
	}
	
	
	@Override
	public boolean areDatesConsecutive(Date dateTo, Date dateFrom) {
		
		if (dateTo.equals(dateFrom))
			return true;
		
		return isLastSecondOfDay(dateTo) && 
				isFirstSecondOfDay(dateFrom) &&
				DateTools.datesAreEqual(DateUtils.addDays(dateTo, 1), dateFrom, DatePrecision.DAY);
	}
}
