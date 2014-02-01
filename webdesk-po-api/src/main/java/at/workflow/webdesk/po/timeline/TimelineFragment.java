package at.workflow.webdesk.po.timeline;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import at.workflow.webdesk.po.HistorizationWithShare;
import at.workflow.webdesk.tools.NamingConventionI18n;
import at.workflow.webdesk.tools.api.BusinessLogicException;
import at.workflow.webdesk.tools.api.I18nMessage;
import at.workflow.webdesk.tools.api.I18nRuntimeException;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.date.EditableDateInterval;

/** 
 * This class wraps all the assignments valid in the specified interval 
 * together with the from and to dates.
 * 
 * The comparator orders on the from-date.
 * 
 * The ASSIGNED element was introduced to eliminate the need 
 * for getter when processing assignments.
 *  
 * @author sdzuban 04.04.2013
 */
public class TimelineFragment<ASSIGNED extends PersistentObject, ASSIGNMENT extends HistorizationWithShare> 
		implements Comparable<TimelineFragment<ASSIGNED, ASSIGNMENT>> {
	
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,s");
		
	private EditableDateInterval interval;
	private Map<ASSIGNED, ASSIGNMENT> validAssignments = new HashMap<ASSIGNED, ASSIGNMENT>();
	
	public TimelineFragment(Date from, Date to) {
		interval = new EditableDateInterval(from, to);
	}
	
	/** date interval of this fragment */
	public EditableDateInterval getDateInterval() {
		return interval;
	}
	/** beginning of the fragment */
	public Date getDateFrom() {
		return interval.getFrom();
	}
	/** end of the fragment */
	public Date getDateTo() {
		return interval.getTo();
	}
	/**
	 * Stores assignment valid in the fragments date interval of assigned entity
	 * @param assigned assigned entity
	 * @param assignment
	 * @throws I18nRuntimeException when there is already another assignment of assigned entity
	 * valid in the date interval of this fragment 
	 */
	public void putValidAssignment(ASSIGNED assigned, ASSIGNMENT assignment) {
		
		if (assigned == null)
			throw new IllegalArgumentException("Assigned object must be non-null");
		
		if (validAssignments.get(assigned) != null) {
			String[] params = {NamingConventionI18n.getI18nKey(assigned.getClass()),
					assigned.toString(), 
					interval.getDatesAsString()};
			throw new BusinessLogicException(
					new I18nMessage("po_timelineFragment.putValidAssignment_{0}_{1}_assigned_multiple_times_{2}", 
							params,
							new Boolean[] {true, false, false}));
		}
		validAssignments.put(assigned, assignment);
	}
	/** @return assignment of the assigned entity valid in this fragments date interval */
	public ASSIGNMENT getValidAssignment(ASSIGNED assigned) {
		return validAssignments.get(assigned);
	}
	/** @return all assignments that are valid in this fragments date interval */
	public Map<ASSIGNED, ASSIGNMENT> getValidAssignments() {
		return validAssignments;
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(TimelineFragment<ASSIGNED, ASSIGNMENT> other) {
		return getDateFrom().compareTo(other.getDateFrom());
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[from " + sdf.format(getDateFrom()) + 
				" to " + sdf.format(getDateTo()) + " " + validAssignments.size() + " assignments]";
	}
	
}
