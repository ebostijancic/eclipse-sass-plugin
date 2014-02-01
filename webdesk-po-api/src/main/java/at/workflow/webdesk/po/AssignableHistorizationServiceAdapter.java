package at.workflow.webdesk.po;


/**
 * This extension of the {@link at.workflow.webdesk.po.HistorizationServiceAdapter}
 * specifies methods necessary for handling of timeline, i.e. relationship where assignee
 * must have an assignment to other entity (assigned) all the time.
 * 
 * @author sdzuban 09.04.2013
 */
public interface AssignableHistorizationServiceAdapter<ASSIGNEE, ASSIGNED> extends HistorizationServiceAdapter {

	/** sets the entity which must always have relationship */
	void setAssignee(ASSIGNEE assignee);
	
	ASSIGNEE getAssignee();

	/** sets the other side of the relationship */
	void setAssigned(ASSIGNED assigned);
	
	ASSIGNED getAssigned();

}
