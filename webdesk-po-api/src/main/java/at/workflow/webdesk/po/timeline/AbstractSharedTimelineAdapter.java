package at.workflow.webdesk.po.timeline;

import at.workflow.webdesk.po.HistorizationWithShareAdapter;

/**
 * {@link at.workflow.webdesk.po.timeline.AbstractHistorizationAdapter}
 * @author sdzuban 09.04.2013
 */
@Deprecated
public abstract class AbstractSharedTimelineAdapter<ASSIGNEE, ASSIGNED> 
	extends AbstractHistorizationAdapter<ASSIGNEE, ASSIGNED> implements 	
	HistorizationWithShareAdapter<ASSIGNEE, ASSIGNED> {
	
	private Number share;
	
	@Override
	public void setShare(Number share) {
		this.share = share;
	}
	@Override
	public Number getShare() {
		return share;
	}
}
