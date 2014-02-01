package at.workflow.webdesk.po.impl.test.sharedTimelineHandler;

import at.workflow.webdesk.po.HistorizationWithShare;
import at.workflow.webdesk.po.model.PoDayHistorization;
import at.workflow.webdesk.po.util.ToStringHelper;

/**
 * @author sdzuban 04.04.2013
 */
public class MyGroupCostCenter extends PoDayHistorization implements HistorizationWithShare {

	private String uid;
	private MyGroup group;
	private MyCostCenter costCenter;
	private float share;

	@Override
	public void setUID(String uid) {
		this.uid = uid;
	}

	@Override
	public String getUID() {
		return uid;
	}

	@Override
	public Float getShare() {
		return new Float(share);
	}

	@Override
	public void setShare(Number share) {
		this.share = share.floatValue();
	}

	public MyGroup getGroup() {
		return group;
	}

	public void setGroup(MyGroup group) {
		this.group = group;
	}

	public MyCostCenter getCostCenter() {
		return costCenter;
	}

	public void setCostCenter(MyCostCenter costCenter) {
		this.costCenter = costCenter;
	}

	@Override
	public String toString() {
		return ToStringHelper.toString(this);
	}
}
