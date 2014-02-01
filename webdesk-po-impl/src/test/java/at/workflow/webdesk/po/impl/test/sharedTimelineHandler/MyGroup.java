package at.workflow.webdesk.po.impl.test.sharedTimelineHandler;

import java.util.Collection;
import java.util.HashSet;

import at.workflow.webdesk.po.model.PoDayHistorization;

/**
 * Unit tests only.
 * 
 * @author sdzuban 04.04.2013
 */
public class MyGroup extends PoDayHistorization {

	private String uid;
	private Collection<MyGroupCostCenter> groupCostCenters = new HashSet<MyGroupCostCenter>();

	@Override
	public void setUID(String uid) {
		this.uid = uid;
	}

	@Override
	public String getUID() {
		return uid;
	}

	public Collection<MyGroupCostCenter> getGroupCostCenters() {
		return groupCostCenters;
	}

	public void setGroupCostCenters(Collection<MyGroupCostCenter> groupCostCenters) {
		this.groupCostCenters = groupCostCenters;
	}

	public void addGroupCostCenter(MyGroupCostCenter groupCostCenter) {
		groupCostCenters.add(groupCostCenter);
	}
}
