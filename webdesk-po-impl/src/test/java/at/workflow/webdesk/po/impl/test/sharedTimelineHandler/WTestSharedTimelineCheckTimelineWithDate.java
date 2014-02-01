package at.workflow.webdesk.po.impl.test.sharedTimelineHandler;

import java.util.Collection;
import java.util.Collections;

import at.workflow.webdesk.po.util.PoLinkingUtils;
import at.workflow.webdesk.tools.api.BusinessLogicException;
import at.workflow.webdesk.tools.date.DateTools;

/**
 * This test is in impl only because of dependency on HistorizationTImelineImpl.
 * 
 * @author sdzuban 04.04.2013
 */
public class WTestSharedTimelineCheckTimelineWithDate extends AbstractSharedTimelineHandlerTest {

	public void testNull() {
		
		Collection<MyGroupCostCenter> assignments = null;
		try {
			timelineHandler.checkSumOverLimitAndTimeline(assignments, null, null);
			fail("Accepted null timeline and assignee");
		} catch (Exception e) {}
		
		try {
			timelineHandler.checkSumOverLimitAndTimeline(assignments, new MyGroup(), null);
			fail("Accepted null timeline.");
		} catch (Exception e) {}
		
		try {
			timelineHandler.checkSumOverLimitAndTimeline(Collections.<MyGroupCostCenter>emptyList(), null, null);
			fail("Accepted null assignee");
		} catch (Exception e) {}
	}
	
	public void testEmptyTimeline() {
		
		try {
			timelineHandler.checkSumOverLimitAndTimeline(Collections.<MyGroupCostCenter>emptyList(), new MyGroup(), null);
			fail("Accepted empty timeline.");
		} catch (Exception e) {
		}
	}
	
	public void testAssigneeValidfromValidtoNull() {
		
		MyGroup group = new MyGroup();
		MyCostCenter cc = new MyCostCenter();
		PoLinkingUtils.link(MyGroupCostCenter.class, group, cc);
		try {
			timelineHandler.checkSumOverLimitAndTimeline(group.getGroupCostCenters(), group, null);
			fail("Accepted assignee with null validfrom and validto.");
		} catch (Exception e) {}
		group.setValidfrom(DateTools.now());
		try {
			timelineHandler.checkSumOverLimitAndTimeline(group.getGroupCostCenters(), group, null);
			fail("Accepted assignee with null validto.");
		} catch (Exception e) {}
		group.setValidfrom(null);
		group.setValidto(DateTools.INFINITY);
		try {
			timelineHandler.checkSumOverLimitAndTimeline(group.getGroupCostCenters(), group, null);
			fail("Accepted assignee with null validfrom.");
		} catch (Exception e) {}
	}
	
	public void testOneCostCenterWrongShare() {
		
		MyGroup group = new MyGroup();
		MyCostCenter cc = new MyCostCenter();
		MyGroupCostCenter link = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc);
		try {
			timelineHandler.checkSumOverLimitAndTimeline(group.getGroupCostCenters(), group, null);
			fail("Accepted uncovered timeline.");
		} catch (BusinessLogicException e) {}
		link.setShare(99.98);
		try {
			timelineHandler.checkSumOverLimitAndTimeline(group.getGroupCostCenters(), group, null);
			fail("Accepted partially covered timeline.");
		} catch (BusinessLogicException e) {}
		link.setShare(100.01);
		try {
			timelineHandler.checkSumOverLimitAndTimeline(group.getGroupCostCenters(), group, null);
			fail("Accepted overccovered timeline.");
		} catch (BusinessLogicException e) {}
	}

	public void testOneCostCenterRightShare() {
		
		MyGroup group = new MyGroup();
		group.setValidfrom(today);
		group.setValidto(infinity);
		MyCostCenter cc = new MyCostCenter();
		MyGroupCostCenter link = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc, today, infinity);
		link.setShare(99.991);
		try {
			timelineHandler.checkSumOverLimitAndTimeline(group.getGroupCostCenters(), group, null);
		} catch (BusinessLogicException e) {
			fail("Rejected lower tolerance timeline " + e);
		}
		link.setShare(100.00);
		try {
			timelineHandler.checkSumOverLimitAndTimeline(group.getGroupCostCenters(), group, null);
		} catch (BusinessLogicException e) {
			fail("Rejected exact timeline " + e);
		}
		link.setShare(100.009);
		try {
			timelineHandler.checkSumOverLimitAndTimeline(group.getGroupCostCenters(), group, null);
		} catch (BusinessLogicException e) {
			fail("Rejected upper tolerance timeline " + e);
		}
	}
	
	public void testTwoConsecutiveCostCentersGap() {
		
		MyGroup group = new MyGroup();
		group.setValidfrom(today);
		group.setValidto(infinity);
		MyCostCenter cc1 = new MyCostCenter();
		MyCostCenter cc2 = new MyCostCenter();
		MyGroupCostCenter link1 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc1, today, DateTools.toDate(2099, 12, 31));
		link1.setShare(70.0);
		MyGroupCostCenter link2 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc2, DateTools.toDate(2200, 1, 1), infinity);
		link2.setShare(100.0);
		try {
			timelineHandler.checkSumOverLimitAndTimeline(group.getGroupCostCenters(), group, null);
			fail("Accepted timeline with gap");
		} catch (BusinessLogicException e) {}
		try {
			timelineHandler.checkSumOverLimitAndTimeline(group.getGroupCostCenters(), group, DateTools.toDate(2200, 1, 1));
		} catch (BusinessLogicException e) {
			fail("Rejected correct timeline " + e);
		}
	}
	
	public void testTwoConsecutiveCostCentersOverlap() {
		
		MyGroup group = new MyGroup();
		group.setValidfrom(today);
		group.setValidto(infinity);
		MyCostCenter cc1 = new MyCostCenter();
		MyCostCenter cc2 = new MyCostCenter();
		MyGroupCostCenter link1 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc1, today, DateTools.toDate(2199, 12, 31));
		link1.setShare(100.0);
		MyGroupCostCenter link2 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc2, DateTools.toDate(2100, 1, 1), infinity);
		link2.setShare(100.0);
		try {
			timelineHandler.checkSumOverLimitAndTimeline(group.getGroupCostCenters(), group, null);
			fail("Accepted timeline with overlap");
		} catch (BusinessLogicException e) {}
		try {
			timelineHandler.checkSumOverLimitAndTimeline(group.getGroupCostCenters(), group, DateTools.toDate(2100, 1, 1));
			fail("Accepted timeline with overlap");
		} catch (BusinessLogicException e) {}
	}
	
}
