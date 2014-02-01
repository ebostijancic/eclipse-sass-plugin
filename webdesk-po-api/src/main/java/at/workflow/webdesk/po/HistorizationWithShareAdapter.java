package at.workflow.webdesk.po;


/**
 * This extension of the {@link at.workflow.webdesk.po.AssignableHistorizationServiceAdapter}
 * specifies methods necessary for handling of timelines with share, i.e. timelines where 
 * sum of shares of individual assignments must always be some predefined value, e.g. 100 per cent.
 * This can be set via bean property.
 * 
 * The assignment entities themselves must implement {@link HistorizationWithShare} interface.
 * 
 * Share of each and every assignment must be set as number.
 * For Integer and Long the sum will always be exact number 
 * calculated using long buffer.
 * For Float and Double the sum can differ slightly of the real sum
 * and is calculated using double buffer.
 * 
 * @author sdzuban 02.04.2013
 */
public interface HistorizationWithShareAdapter<ASSIGNEE, ASSIGNED> 
	extends AssignableHistorizationServiceAdapter<ASSIGNEE, ASSIGNED>, ShareAware {

}
