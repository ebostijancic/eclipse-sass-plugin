package at.workflow.webdesk.po.impl.test.sharedTimelineHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import at.workflow.webdesk.po.model.PoHistorization;
import at.workflow.webdesk.po.util.PoLinkingUtils;
import at.workflow.webdesk.tools.api.BusinessMessages;
import at.workflow.webdesk.tools.date.DateTools;

/**
 * This test is in impl only because of dependency on HistorizationTImelineImpl.
 * 
 * @author sdzuban 04.04.2013
 */
public class WTestSharedTimelineSumOverLimitOrDeviatesFromTarget extends AbstractSharedTimelineHandlerTest {
	
	public static class TargetSetter extends PoHistorization {

		private String UID;
		private float target = 100f;
		
		@Override
		public String getUID() {
			return UID;
		}
		@Override
		public void setUID(String uid) {
			UID = uid;
		}
		public float getTarget() {
			return target;
		}
	}

	public void testINullCheckSumOverLimitOrDeviatesFromTarget() throws Exception {
		
		Collection<TargetSetter> targetSetters = null;
		Collection<MyGroupCostCenter> assignments = null;
		try {
			timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, null, DateTools.today());
			fail("Accepted null targetSetters, timeline and target getter");
		} catch (Exception e) {}
		
		Method getTarget = TargetSetter.class.getDeclaredMethod("getTarget");
		assignments = new ArrayList<MyGroupCostCenter>();

		assignments.add(new MyGroupCostCenter());
		try {
			timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, DateTools.today());
			fail("Accepted null targetSetters.");
		} catch (Exception e) {}
		
		targetSetters = new ArrayList<TargetSetter>();
		try {
			timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, DateTools.today());
		} catch (Exception e) {
			fail("Rejected empty targetSetters.");
		}
		
		targetSetters.add(new TargetSetter());
		try {
			timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, null, DateTools.today());
			fail("Accepted null target getter");
		} catch (Exception e) {}
	}
	
	public void testEmptyTargetSetterTimelineCheckSumOverLimitOrDeviatesFromTarget() throws Exception {
		
		Collection<TargetSetter> targetSetters = new ArrayList<TargetSetter>();
		Collection<MyGroupCostCenter> assignments = new ArrayList<MyGroupCostCenter>();
		Method getTarget = TargetSetter.class.getDeclaredMethod("getTarget");
		MyGroupCostCenter assignment = new MyGroupCostCenter();
		assignment.setShare(10F);
		assignments.add(assignment);

		assertTrue(timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, DateTools.today()).isEmpty());
	}
	
	public void testPartialTimelineTargetSetterTimelineCheckSumOverLimitOrDeviatesFromTarget() throws Exception {
		
		Collection<TargetSetter> targetSetters = new ArrayList<TargetSetter>();
		Collection<MyGroupCostCenter> assignments = new ArrayList<MyGroupCostCenter>();
		Method getTarget = TargetSetter.class.getDeclaredMethod("getTarget");
		MyGroup group = new MyGroup();
		MyCostCenter cc = new MyCostCenter();
		MyGroupCostCenter link = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc);
		assignments.add(link);
		link.setShare(10f);
		TargetSetter targetSetter = new TargetSetter();
		targetSetter.setValidfrom(date1);
		targetSetter.setValidto(date2);
		targetSetters.add(targetSetter);
		
		assertTrue(timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, null).isEmpty());
		BusinessMessages messages = timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, DateTools.today());
		assertFalse(messages.isEmpty());
		assertEquals(0, messages.getInfoMessages().size());
		assertEquals(1, messages.getWarnings().size());
		System.out.println(messages.getWarnings().iterator().next());
	}
	
	public void testOneCostCenterCheckSumOverLimitOrDeviatesFromTarget() throws Exception {
		
		Collection<TargetSetter> targetSetters = new ArrayList<TargetSetter>();
		Collection<MyGroupCostCenter> assignments = new ArrayList<MyGroupCostCenter>();
		Method getTarget = TargetSetter.class.getDeclaredMethod("getTarget");

		TargetSetter targetSetter = new TargetSetter();
		targetSetters.add(targetSetter);
		
		MyGroup group = new MyGroup();
		MyCostCenter cc = new MyCostCenter();
		MyGroupCostCenter link = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc);
		assignments.add(link);
		
		
		link.setShare(99.989);
		assertTrue(timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, null).isEmpty());
		BusinessMessages  messages = timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, DateTools.today());
		assertEquals(1, messages.getWarnings().size());
		System.out.println(messages.getWarnings().iterator().next());
		messages = timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, date1);
		assertEquals(1, messages.getWarnings().size());
		System.out.println(messages.getWarnings().iterator().next());
		link.setShare(99.991);
		assertTrue(timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, null).isEmpty());
		assertTrue(timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, DateTools.today()).isEmpty());
		assertTrue(timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, date1).isEmpty());
		link.setShare(100.00);
		assertTrue(timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, null).isEmpty());
		assertTrue(timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, DateTools.today()).isEmpty());
		assertTrue(timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, date1).isEmpty());
		link.setShare(100.009);
		assertTrue(timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, null).isEmpty());
		assertTrue(timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, DateTools.today()).isEmpty());
		assertTrue(timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, date1).isEmpty());
		link.setShare(100.011);
		messages = timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, null);
		assertEquals(1, messages.getWarnings().size());
		System.out.println(messages.getWarnings().iterator().next());
		messages = timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, DateTools.today());
		assertEquals(1, messages.getWarnings().size());
		System.out.println(messages.getWarnings().iterator().next());
		messages = timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, date1);
		assertEquals(1, messages.getWarnings().size());
		System.out.println(messages.getWarnings().iterator().next());
	}
	
	public void testTwoAssignmentsCheckSumOverLimitOrDeviatesFromTarget() throws Exception {
		
		Collection<TargetSetter> targetSetters = new ArrayList<TargetSetter>();
		Collection<MyGroupCostCenter> assignments = new ArrayList<MyGroupCostCenter>();
		Method getTarget = TargetSetter.class.getDeclaredMethod("getTarget");

		TargetSetter targetSetter = new TargetSetter();
		targetSetters.add(targetSetter);
		
		MyGroup group = new MyGroup();
		group.setValidfrom(today);
		group.setValidto(infinity);
		MyCostCenter cc = new MyCostCenter();
		MyGroupCostCenter link = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc);
		assignments.add(link);
		
		MyCostCenter cc1 = new MyCostCenter();
		MyCostCenter cc2 = new MyCostCenter();
		MyGroupCostCenter link1 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc1, today, infinity);
		assignments.add(link1);
		link1.setShare(50.0);
		MyGroupCostCenter link2 = (MyGroupCostCenter) PoLinkingUtils.link(MyGroupCostCenter.class, group, cc2, today, infinity);
		assignments.add(link2);
		link2.setShare(49.989);
		assertTrue(timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, null).isEmpty());
		BusinessMessages  messages = timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, DateTools.today());
		assertEquals(1, messages.getWarnings().size());
		System.out.println(messages.getWarnings().iterator().next());
		messages = timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, date1);
		assertEquals(1, messages.getWarnings().size());
		System.out.println(messages.getWarnings().iterator().next());
		link2.setShare(49.99);
		assertTrue(timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, null).isEmpty());
		assertTrue(timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, DateTools.today()).isEmpty());
		assertTrue(timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, date1).isEmpty());
		link2.setShare(50.0);
		assertTrue(timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, null).isEmpty());
		assertTrue(timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, DateTools.today()).isEmpty());
		assertTrue(timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, date1).isEmpty());
		link2.setShare(50.009);
		assertTrue(timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, null).isEmpty());
		assertTrue(timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, DateTools.today()).isEmpty());
		assertTrue(timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, date1).isEmpty());
		link2.setShare(50.011); 
		messages = timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, null);
		assertEquals(1, messages.getWarnings().size());
		System.out.println(messages.getWarnings().iterator().next());
		messages = timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, DateTools.today());
		assertEquals(1, messages.getWarnings().size());
		System.out.println(messages.getWarnings().iterator().next());
		messages = timelineHandler.checkSumOverLimitOrDeviatesFromTarget(targetSetters, assignments, getTarget, date1);
		assertEquals(1, messages.getWarnings().size());
		System.out.println(messages.getWarnings().iterator().next());
	}
	
}
