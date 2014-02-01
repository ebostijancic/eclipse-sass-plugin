package at.workflow.webdesk.po.impl.test.sharedTimelineHandler;

import java.util.Collection;

import at.workflow.webdesk.po.SharedTimelineTable;
import at.workflow.webdesk.po.SharedTimelineTableAdapter;
import at.workflow.webdesk.po.timeline.AbstractSharedTimelineHandler;
import at.workflow.webdesk.po.timeline.SharedTimelineTableImpl;
import at.workflow.webdesk.tools.NamingConventionI18n;

/**
 * @author sdzuban 05.04.2013
 */
public class MyGroupCostCenterHandler extends AbstractSharedTimelineHandler<MyGroup, MyGroupCostCenter, MyCostCenter, Float> {

	
	public MyGroupCostCenterHandler() {
		init();
	}
	
	/** {@inheritDoc} */
	@Override
	protected Class<Float> getShareType() {
		return Float.class;
	}

	/** {@inheritDoc} */
	@Override
	public Float getShareSumTarget() {
		return 100.0f;
	}

	/** {@inheritDoc} */
	@Override
	protected Float getSumTolerance() {
		return 0.01f;
	}

	/** {@inheritDoc} */
	@Override
	protected SharedTimelineTableAdapter<MyGroup, MyGroupCostCenter, MyCostCenter, Float> getAdapter() {
		return new MyCostCentersTableAdapter();
	}
	
	/** {@inheritDoc} */
	@Override
	protected Collection<MyGroupCostCenter> getAssignments(MyGroup assignee) {
		return assignee.getGroupCostCenters();
	}

	/** {@inheritDoc} */
	@Override
	protected MyCostCenter getAssigned(MyGroupCostCenter assignment) {
		return assignment.getCostCenter();
	}
	
	@Override
	protected String getSharePropertyI18nKey() {
		return NamingConventionI18n.getI18nKey(MyGroupCostCenter.class, "share");
	}

	/** {@inheritDoc} */
	@Override
	protected SharedTimelineTable<MyCostCenter, Float> getNewTable() {
		return new SharedTimelineTableImpl<MyCostCenter, Float>();
	}

}
