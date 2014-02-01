package at.workflow.webdesk.po.impl.test.sharedTimelineHandler;

import java.util.Collection;
import java.util.Collections;

import at.workflow.webdesk.po.util.PoLinkingUtils;
import at.workflow.webdesk.tools.api.BusinessMessages;

/**
 * This test is in impl only because of dependency on HistorizationTImelineImpl.
 * 
 * @author sdzuban 04.04.2013
 */
public class WTestSharedTimelineSumOverLimit extends AbstractSharedTimelineHandlerTest {

	public void testINullIsSumOverLimit() {
		
		Collection<MyGroupCostCenter> assignments = null;
		try {
			timelineHandler.isSumOverLimit(assignments, null, null);
			fail("Accepted null timeline, assignee and limit");
		} catch (Exception e) {}
		
		try {
			timelineHandler.isSumOverLimit(assignments, new MyGroup(), 100f);
			fail("Accepted null timeline.");
		} catch (Exception e) {}
		
		try {
			timelineHandler.isSumOverLimit(Collections.<MyGroupCostCenter>emptyList(), null, 100f);
			fail("Accepted null assignee");
		} catch (Exception e) {}
		
		try {
			timelineHandler.isSumOverLimit(Collections.<MyGroupCostCenter>emptyList(), new MyGroup(), null);
			fail("Accepted null limit");
		} catch (Exception e) {}
	}
	
	public void testEmptyTimelineIsSumOverLimit() {
		
		assertFalse(timelineHandler.isSumOverLimit(Collections.<MyGroupCostCenter>emptyList(), new MyGroup(), 100f));
	}
	
	public void testOneCostCenterIsSumOverLimit() {
		
		MyGroup group = new MyGroup();
		MyCostCenter cc = new MyCostCenter();
		MyGroupCostCenter link = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc);
		link.setShare(99.99);
		assertFalse(timelineHandler.isSumOverLimit(group.getGroupCostCenters(), group, 100.0));
		link.setShare(100.00);
		assertFalse(timelineHandler.isSumOverLimit(group.getGroupCostCenters(), group, 100.0));
		link.setShare(100.01);
		assertTrue(timelineHandler.isSumOverLimit(group.getGroupCostCenters(), group, 100.0));
	}

	public void testTwoCostCentersIsSumOverLimit() {
		
		MyGroup group = new MyGroup();
		group.setValidfrom(today);
		group.setValidto(infinity);
		MyCostCenter cc1 = new MyCostCenter();
		MyCostCenter cc2 = new MyCostCenter();
		MyGroupCostCenter link1 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc1, today, infinity);
		link1.setShare(50.0);
		MyGroupCostCenter link2 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc2, today, infinity);
		link2.setShare(49.99);
		assertFalse(timelineHandler.isSumOverLimit(group.getGroupCostCenters(), group, 100.0));
		link2.setShare(50.0);
		assertFalse(timelineHandler.isSumOverLimit(group.getGroupCostCenters(), group, 100.0));
		link2.setShare(50.011); 
		assertTrue(timelineHandler.isSumOverLimit(group.getGroupCostCenters(), group, 100.0));
	}
	
	public void testINullCheckSumOverLimit() {
		
		Collection<MyGroupCostCenter> assignments = null;
		MyGroup assignee = null;
		try {
			timelineHandler.checkSumOverLimit(assignments, assignee, null);
			fail("Accepted null timeline, assignee and limit");
		} catch (Exception e) {}
		
		try {
			timelineHandler.checkSumOverLimit(assignments, new MyGroup(), 100f);
			fail("Accepted null timeline.");
		} catch (Exception e) {}
		
		try {
			timelineHandler.checkSumOverLimit(Collections.<MyGroupCostCenter>emptyList(), null, 100f);
			fail("Accepted null assignee");
		} catch (Exception e) {}
		
		try {
			timelineHandler.checkSumOverLimit(Collections.<MyGroupCostCenter>emptyList(), new MyGroup(), null);
			fail("Accepted null limit");
		} catch (Exception e) {}
	}
	
	public void testEmptyTimelineCheckSumOverLimit() throws Exception {
		
		assertTrue(timelineHandler.checkSumOverLimit(Collections.<MyGroupCostCenter>emptyList(), new MyGroup(), 100f).isEmpty());
	}
	
	public void testOneCostCenterCheckSumOverLimit() throws Exception {
		
		MyGroup group = new MyGroup();
		MyCostCenter cc = new MyCostCenter();
		MyGroupCostCenter link = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc);
		link.setShare(99.99);
		assertTrue(timelineHandler.checkSumOverLimit(group.getGroupCostCenters(), group, 100.0).isEmpty());
		link.setShare(100.00);
		assertTrue(timelineHandler.checkSumOverLimit(group.getGroupCostCenters(), group, 100.0).isEmpty());
		link.setShare(100.01);
		BusinessMessages messages = timelineHandler.checkSumOverLimit(group.getGroupCostCenters(), group, 100.0);
		assertFalse(messages.isEmpty());
		assertEquals(0, messages.getInfoMessages().size());
		assertEquals(1, messages.getWarnings().size());
		System.out.println(messages.getWarnings().iterator().next());
	}
	
	public void testTwoCostCentersCheckSumOverLimit() throws Exception {
		
		MyGroup group = new MyGroup();
		group.setValidfrom(today);
		group.setValidto(infinity);
		MyCostCenter cc1 = new MyCostCenter();
		MyCostCenter cc2 = new MyCostCenter();
		MyGroupCostCenter link1 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc1, today, infinity);
		link1.setShare(50.0);
		MyGroupCostCenter link2 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc2, today, infinity);
		link2.setShare(49.99);
		assertTrue(timelineHandler.checkSumOverLimit(group.getGroupCostCenters(), group, 100.0).isEmpty());
		link2.setShare(50.0);
		assertTrue(timelineHandler.checkSumOverLimit(group.getGroupCostCenters(), group, 100.0).isEmpty());
		link2.setShare(50.011); 
		BusinessMessages messages = timelineHandler.checkSumOverLimit(group.getGroupCostCenters(), group, 100.0);
		assertFalse(messages.isEmpty());
		assertEquals(0, messages.getInfoMessages().size());
		assertEquals(1, messages.getWarnings().size());
		System.out.println(messages.getWarnings().iterator().next());
	}
	
}
