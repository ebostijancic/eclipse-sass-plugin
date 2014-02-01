package at.workflow.webdesk.po.timeline;

import java.util.Date;

import at.workflow.webdesk.po.AssignableHistorizationServiceAdapter;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.date.Interval;

/**
 * This implementation of {@link at.workflow.webdesk.po.AssignableHistorizationServiceAdapter}
 * provides following two extra functionalities:
 * <li>
 * processing of dates by {@link at.workflow.webdesk.tools.date.HistorizationHelper} methods
 * for getting useful from and to dates. The reason for doing this here 
 * is that when the linking entities are stored by cascading the 
 * {@link at.workflow.webdesk.tools.HistoricizingDAOImpl} that
 * usually performs these transformations is not called.
 * </li>
 * <li>
 * checking of validity of both assignee and assigned for the whole duration of assignment
 * to prevent inconsistent links to be generated and saved
 * </li>
 *
 * @author sdzuban 09.04.2013
 */
public abstract class AbstractHistorizationAdapter<ASSIGNEE, ASSIGNED> 
	implements AssignableHistorizationServiceAdapter<ASSIGNEE, ASSIGNED> {

	private ASSIGNEE assignee;
	private ASSIGNED assigned;
	
	@Override
	public void setAssignee(ASSIGNEE assignee) {
		this.assignee = assignee;
	}
	
	@Override
	public void setAssigned(ASSIGNED assigned) {
		this.assigned = assigned;
	}
	
	@Override
	public ASSIGNEE getAssignee() {
		return assignee;
	}
	
	@Override
	public ASSIGNED getAssigned() {
		return assigned;
	}
	
	/**
	 * Replaces the original saveObject(assignment), 
	 * does the real saving either by persisting or by 
	 * adding to assigned's and assignee's list of assignments
	 * @param assignment the assignment
	 */
	protected abstract void saveAssignment(Historization assignment); 
	
	/**
	 * Replaces the original generateEmptyObject(from, to) method,
	 * generates the assignment for from and to dates 
	 * @param assignment the assignment
	 * @return generated assignment
	 */
	protected abstract Historization generateEmptyAssignment(Date from, Date to);
	
	
	/** {@inheritDoc} */
	@Override
	public final void saveObject(Historization assignment) {
		
		HistorizationTimelineUtils.checkValidity(getAssignee(), getAssigned(), assignment.getValidity());
		saveAssignment(assignment);
	}

	/** {@inheritDoc} */
	@Override
	public final Historization generateEmptyObject(Date from, Date to) {

		Interval assignmentDates = 
				HistorizationTimelineUtils.prepareAssignment(getAssignee(), getAssigned(), from, to);
		
		HistorizationTimelineUtils.checkValidity(getAssignee(), getAssigned(), 
							assignmentDates.getFrom(), assignmentDates.getTo());
		
		return generateEmptyAssignment(assignmentDates.getFrom(), assignmentDates.getTo());
	}

}
