package at.workflow.webdesk.po.impl.test.sharedTimelineHandler;

import java.util.List;

import at.workflow.webdesk.po.timeline.HistorizationTimelineUtils;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.date.DateTools;

/**
 * This test is in impl only because of dependency on HistorizationTImelineImpl.
 * 
 * @author sdzuban 04.04.2013
 */
public class WTestTimelineTableFirstAssignment extends AbstractSharedTimelineHandlerTest {

	public void testNullAssignee() {
		
		MyGroup group = null;
		MyCostCenter cc = new MyCostCenter();
		cc.setUID("test");
		try {
			timelineHandler.assign(group, cc, null, null);
			fail("accepted null assignee.");
		} catch (Exception e) {}
	}
	
	
	@SuppressWarnings("cast")
	public void testWithExplicitTimesNowAndInfinity() {
		
		MyGroup group = new MyGroup();
		group.setValidfrom(today);
		group.setValidto(infinity);
		MyCostCenter cc = new MyCostCenter();
		cc.setUID("test");
		timelineHandler.assign(group, cc, group.getValidfrom(), group.getValidto());
		List<Historization> links = HistorizationTimelineUtils.getSortedHistorizationList(group.getGroupCostCenters());
		assertEquals(1, links.size());
		MyGroupCostCenter link = (MyGroupCostCenter) links.get(0);
		assertTrue(link instanceof MyGroupCostCenter);
		assertEquals(today, link.getValidfrom());
		assertEquals(infinity, link.getValidto());
		assertEquals(100.0, link.getShare().doubleValue(), 0.01);
		MyGroupCostCenter assignment = (MyGroupCostCenter) link;
		assertEquals(group, assignment.getGroup());
		assertEquals(cc, assignment.getCostCenter());
		assertEquals(assignment, group.getGroupCostCenters().iterator().next());
		assertEquals(assignment, cc.getGroupCostCenters().iterator().next());
		
	}
	
	@SuppressWarnings("cast")
	public void testWithExplicitTimes() {
		
		MyGroup group = new MyGroup();
		group.setValidfrom(DateTools.toDate(2100, 6, 15));
		group.setValidto(DateTools.toDate(2200, 6, 15));
		MyCostCenter cc = new MyCostCenter();
		cc.setUID("test");
		timelineHandler.assign(group, cc, group.getValidfrom(), group.getValidto());
		List<Historization> links = HistorizationTimelineUtils.getSortedHistorizationList(group.getGroupCostCenters());
		assertEquals(1, links.size());
		MyGroupCostCenter link = (MyGroupCostCenter) links.get(0);
		assertTrue(link instanceof MyGroupCostCenter);
		assertEquals(group.getValidfrom(), link.getValidfrom());
		assertEquals(group.getValidto(), link.getValidto());
		assertEquals(100.0, link.getShare().doubleValue(), 0.01);
		MyGroupCostCenter assignment = (MyGroupCostCenter) link;
		assertEquals(group, assignment.getGroup());
		assertEquals(cc, assignment.getCostCenter());
		assertEquals(assignment, group.getGroupCostCenters().iterator().next());
		assertEquals(assignment, cc.getGroupCostCenters().iterator().next());
		
	}
	
	public void testWithNullAssigneeDates() {
		
		MyGroup group = new MyGroup();
		MyCostCenter cc = new MyCostCenter();
		cc.setUID("test");
		try {
			timelineHandler.assign(group, cc, null, null);
		} catch (Exception e) {
			fail("Rejected null validity date(s) of assignee.");
		}
		
	}
	
}
