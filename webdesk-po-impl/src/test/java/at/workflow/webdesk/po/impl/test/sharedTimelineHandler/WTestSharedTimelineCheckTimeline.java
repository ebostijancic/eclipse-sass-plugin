package at.workflow.webdesk.po.impl.test.sharedTimelineHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import at.workflow.webdesk.po.util.PoLinkingUtils;
import at.workflow.webdesk.tools.date.DateTools;

/**
 * This test is in impl only because of dependency on HistorizationTImelineImpl.
 * 
 * @author sdzuban 04.04.2013
 */
public class WTestSharedTimelineCheckTimeline extends AbstractSharedTimelineHandlerTest {

	public void testNull() {
		
		Collection<MyGroupCostCenter> assignments = null;
		try {
			timelineHandler.checkTimeline(assignments, null);
			fail("Accepted null timeline and assignee");
		} catch (Exception e) {}
		
		try {
			timelineHandler.checkTimeline(assignments, new MyGroup());
			fail("Accepted null timeline.");
		} catch (Exception e) {}
		
		try {
			timelineHandler.checkTimeline(Collections.<MyGroupCostCenter>emptyList(), null);
			fail("Accepted null assignee");
		} catch (Exception e) {}
	}
	
	public void testEmptyTimeline() {
		
		try {
			timelineHandler.checkTimeline(Collections.<MyGroupCostCenter>emptyList(), new MyGroup());
			fail("Accepted empty timeline.");
		} catch (Exception e) {
		}
	}
	
	public void testAssigneeValidfromValidtoNull() {
		
		MyGroup group = new MyGroup();
		MyCostCenter cc = new MyCostCenter();
		PoLinkingUtils.link(MyGroupCostCenter.class, group, cc);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
			fail("Accepted assignee with null validfrom and validto.");
		} catch (Exception e) {}
		group.setValidfrom(DateTools.now());
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
			fail("Accepted assignee with null validto.");
		} catch (Exception e) {}
		group.setValidfrom(null);
		group.setValidto(DateTools.INFINITY);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
			fail("Accepted assignee with null validfrom.");
		} catch (Exception e) {}
	}
	
	public void testOneCostCenterWrongShare() {
		
		MyGroup group = new MyGroup();
		MyCostCenter cc = new MyCostCenter();
		MyGroupCostCenter link = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
			fail("Accepted uncovered timeline.");
		} catch (Exception e) {}
		link.setShare(99.98);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
			fail("Accepted partially covered timeline.");
		} catch (Exception e) {}
		link.setShare(100.01);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
			fail("Accepted overccovered timeline.");
		} catch (Exception e) {}
	}

	public void testOneCostCenterRightShare() {
		
		MyGroup group = new MyGroup();
		group.setValidfrom(today);
		group.setValidto(infinity);
		MyCostCenter cc = new MyCostCenter();
		MyGroupCostCenter link = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc, today, infinity);
		link.setShare(99.991);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
		} catch (Exception e) {
			fail("Rejected lower tolerance timeline " + e);
		}
		link.setShare(100.00);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
		} catch (Exception e) {
			fail("Rejected exact timeline " + e);
		}
		link.setShare(100.009);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
		} catch (Exception e) {
			fail("Rejected upper tolerance timeline " + e);
		}
	}
	
	public void testTwoCostCentersWrongShare() {
		
		MyGroup group = new MyGroup();
		group.setValidfrom(today);
		group.setValidto(infinity);
		MyCostCenter cc1 = new MyCostCenter();
		MyCostCenter cc2 = new MyCostCenter();
		MyGroupCostCenter link1 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc1, today, infinity);
		link1.setShare(40.0);
		MyGroupCostCenter link2 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc2, today, infinity);
		link2.setShare(40.0);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
			fail("Accepted partially covered timeline.");
		} catch (Exception e) {}
		link1.setShare(70.0);
		link2.setShare(70.0);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
			fail("Accepted overcovered timeline.");
		} catch (Exception e) {}
	}
	
	public void testTwoCostCentersRightShare() {
		
		MyGroup group = new MyGroup();
		group.setValidfrom(today);
		group.setValidto(infinity);
		MyCostCenter cc1 = new MyCostCenter();
		MyCostCenter cc2 = new MyCostCenter();
		MyGroupCostCenter link1 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc1, today, infinity);
		link1.setShare(40.0);
		MyGroupCostCenter link2 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc2, today, infinity);
		link2.setShare(59.991);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
		} catch (Exception e) {
			fail("Rejected lower tolerance timeline " + e);
		}
		link1.setShare(40.0);
		link2.setShare(60.0);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
		} catch (Exception e) {
			fail("Rejected exact timeline " + e);
		}
		link1.setShare(40.0);
		link2.setShare(60.009);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
		} catch (Exception e) {
			fail("Rejected upper tolerance timeline " + e);
		}
	}
	
	public void testIncompleteCoverage() {
		
		MyCostCenter cc = new MyCostCenter();
		MyGroup group = new MyGroup();
		group.setValidfrom(today);
		group.setValidto(infinity);
		MyGroupCostCenter link3 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc, DateTools.toDate(2100, 1, 1), DateTools.toDate(2200, 12, 31));
		link3.setShare(100.0);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
			fail("Accepted incomplete coverage of both beginning and end.");
		} catch (Exception e) { }
		
		group = new MyGroup();
		group.setValidfrom(today);
		group.setValidto(infinity);
		MyGroupCostCenter link1 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc, DateTools.toDate(2100, 1, 1), infinity);
		link1.setShare(100.0);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
			fail("Accepted incomplete coverage of beginning.");
		} catch (Exception e) { }

		group = new MyGroup();
		group.setValidfrom(today);
		group.setValidto(infinity);
		MyGroupCostCenter link2 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc, today, DateTools.toDate(2200, 12, 31));
		link2.setShare(100.0);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
			fail("Accepted incomplete coverage of end.");
		} catch (Exception e) { }
	}
	
	public void testTwoConsecutiveCostCentersGap() {
		
		MyGroup group = new MyGroup();
		group.setValidfrom(today);
		group.setValidto(infinity);
		MyCostCenter cc1 = new MyCostCenter();
		MyCostCenter cc2 = new MyCostCenter();
		MyGroupCostCenter link1 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc1, today, DateTools.toDate(2100, 1, 1));
		link1.setShare(100.0);
		MyGroupCostCenter link2 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc2, DateTools.toDate(2200, 1, 1), infinity);
		link2.setShare(100.0);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
			fail("Accepted timeline with gap");
		} catch (Exception e) {}
	}
	
	public void testTwoConsecutiveCostCentersOverlap() {
		
		MyGroup group = new MyGroup();
		group.setValidfrom(today);
		group.setValidto(infinity);
		MyCostCenter cc1 = new MyCostCenter();
		MyCostCenter cc2 = new MyCostCenter();
		MyGroupCostCenter link1 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc1, today, DateTools.toDate(2200, 1, 1));
		link1.setShare(100.0);
		MyGroupCostCenter link2 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc2, DateTools.toDate(2100, 1, 1), infinity);
		link2.setShare(100.0);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
			fail("Accepted timeline with overlap");
		} catch (Exception e) {}
	}
	
	public void testTwoConsecutiveCostCentersCorrect() {
		
		MyGroup group = new MyGroup();
		group.setValidfrom(today);
		group.setValidto(infinity);
		MyCostCenter cc1 = new MyCostCenter();
		MyCostCenter cc2 = new MyCostCenter();
		MyGroupCostCenter link1 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc1, today, DateTools.lastMomentOfDay(DateTools.toDate(2100, 6, 15)));
		link1.setShare(100.0);
		MyGroupCostCenter link2 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc2, DateTools.dateOnly(DateTools.toDate(2100, 6, 16)), infinity);
		link2.setShare(100.0);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
		} catch (Exception e) {
			fail("Rejected correrct timeline " + e);
		}
	}
	
	public void testThreeCostCentersCorrect() {
		
		Date to1 = DateTools.lastMomentOfDay(DateTools.toDate(2100, 6, 15));
		Date from1 = DateTools.dateOnly(DateTools.toDate(2100, 6, 16));
		
		MyGroup group = new MyGroup();
		group.setValidfrom(today);
		group.setValidto(infinity);
		MyCostCenter cc1 = new MyCostCenter();
		MyCostCenter cc2 = new MyCostCenter();
		MyGroupCostCenter link1 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc1, today, to1);
		link1.setShare(60.0);
		MyGroupCostCenter link2 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc2, today, to1);
		link2.setShare(40.0);
		MyGroupCostCenter link3 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc2, from1, infinity);
		link3.setShare(100.0);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
		} catch (Exception e) {
			fail("Rejected correrct timeline " + e);
		}
	}
	
	public void testThreeCostCentersIncomplete() {
		
		Date to1 = DateTools.lastMomentOfDay(DateTools.toDate(2100, 6, 15));
		Date from1 = DateTools.dateOnly(DateTools.toDate(2100, 6, 16));
		
		MyGroup group = new MyGroup();
		group.setValidfrom(today);
		group.setValidto(infinity);
		MyCostCenter cc1 = new MyCostCenter();
		MyCostCenter cc2 = new MyCostCenter();
		MyGroupCostCenter link1 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc1, DateTools.toDate(2090, 6, 15), to1);
		link1.setShare(60.0);
		MyGroupCostCenter link2 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc2, today, to1);
		link2.setShare(40.0);
		MyGroupCostCenter link3 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc2, from1, infinity);
		link3.setShare(100.0);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
			fail("Rejected correrct timeline");
		} catch (Exception e) {}

		link1.setValidfrom(today);
		link2.setValidto(DateTools.toDate(2200, 6, 15));
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
			fail("Accepted incomplete three centers timeline");
		} catch (Exception e) {}
	}
	
	public void testThreeCostCentersOverlap() {
		
		Date to1 = DateTools.lastMomentOfDay(DateTools.toDate(2100, 6, 30));
		Date from1 = DateTools.dateOnly(DateTools.toDate(2100, 6, 16));
		
		MyGroup group = new MyGroup();
		group.setValidfrom(today);
		group.setValidto(infinity);
		MyCostCenter cc1 = new MyCostCenter();
		MyCostCenter cc2 = new MyCostCenter();
		MyGroupCostCenter link1 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc1, today, to1);
		link1.setShare(60.0);
		MyGroupCostCenter link2 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc2, today, to1);
		link2.setShare(40.0);
		MyGroupCostCenter link3 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc2, from1, infinity);
		link3.setShare(100.0);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
			fail("Accepted three centers overlap timeline");
		} catch (Exception e) {}
	}
	
	public void testThreeCostCentersGap() {
		
		Date to1 = DateTools.lastMomentOfDay(DateTools.toDate(2100, 6, 1));
		Date from1 = DateTools.dateOnly(DateTools.toDate(2100, 6, 16));
		
		MyGroup group = new MyGroup();
		group.setValidfrom(today);
		group.setValidto(infinity);
		MyCostCenter cc1 = new MyCostCenter();
		MyCostCenter cc2 = new MyCostCenter();
		MyGroupCostCenter link1 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc1, today, to1);
		link1.setShare(60.0);
		MyGroupCostCenter link2 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc2, today, to1);
		link2.setShare(40.0);
		MyGroupCostCenter link3 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc2, from1, infinity);
		link3.setShare(100.0);
		try {
			timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
			fail("Accepted three centers timeline with gap");
		} catch (Exception e) {}
	}
	
}
