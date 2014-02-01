package at.workflow.webdesk.po.impl.test.timeline;

import java.util.Arrays;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;

import at.workflow.webdesk.po.impl.test.AbstractHistorizationTimelineRepairerTestCase;
import at.workflow.webdesk.po.timeline.HistorizationCleanerAndMerger;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.date.DateTools;

/**
 * @author sdzuban 07.11.2013
 */
public class WTestHistorizationCleanerAndMerger extends AbstractHistorizationTimelineRepairerTestCase {
	
	private HistorizationCleanerAndMerger cleanerAndMerger;
	
	private Set<? extends Historization> result;
	
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		super.onSetUpAfterDataGeneration();
		
		if (cleanerAndMerger == null) {
			cleanerAndMerger = (HistorizationCleanerAndMerger) getBean("HistorizationCleanerAndMerger");
		}
	}
	
	public void testNulls() {
		
		try {
			cleanerAndMerger.replaceNullDates(null);
		} catch (Exception e) {
			fail("Rejected null");
		}
		
		try {
			result = cleanerAndMerger.removeInvalidAssignments(null);
		} catch (Exception e) {
			fail("Rejected null");
		}
		assertNotNull(result);
		assertEquals(0, result.size());
		
		try {
			result = cleanerAndMerger.mergeMultipleSimultaneousAssignments(null);
		} catch (Exception e) {
			fail("Rejected null");
		}
		assertNotNull(result);
		assertEquals(0, result.size());
		
		try {
			result = cleanerAndMerger.cleanAndMergeAssignments(null);
		} catch (Exception e) {
			fail("Rejected null");
		}
		assertNotNull(result);
		assertEquals(0, result.size());
		
	}
	
	public void testReplaceNullDates() {
		
		links.clear();
		links.add(new TestLink(null, null, null, "target1"));
		
		cleanerAndMerger.replaceNullDates(links);
		assertFalse(links.get(0).getValidfrom().after(DateTools.now()));
		assertFalse(links.get(0).getValidfrom().before(DateUtils.addSeconds(DateTools.now(), -5)));
		assertEquals(DateTools.INFINITY, links.get(0).getValidto());
	}
	
	public void testRemoveInvalidAssignments() {
		
		TestLink link1 = new TestLink(null, date3005, date0101, "target1");
		TestLink link2 = new TestLink(null, date0101, date3006, "target2");

		links.clear();
		links.add(link1);
		links.add(link2);

		result = cleanerAndMerger.removeInvalidAssignments(links);
		
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(link1, result.iterator().next());
		
		assertEquals(1, links.size());
		assertDates(links, Arrays.asList(date0101, date3006));
		assertNull(links.get(0).getUID());
		assertEquals("target2", ((TestLink) links.get(0)).getTarget());
	}
	
	public void testMergeMultipleSimultaneousAssignmentsToOneTarget() {

		TestLink link1 = new TestLink(null, date0101, date3005, "target1");
		TestLink link2 = new TestLink(null, date0101, date3006, "target1");
		TestLink link3 = new TestLink(null, date0101, date3009, "target1");
		
		links.clear();
		links.add(link1);
		links.add(link2);
		links.add(link3);

		result = cleanerAndMerger.mergeMultipleSimultaneousAssignments(links);
		
		assertNotNull(result);
		assertEquals(2, result.size());
		assertTrue(result.contains(link2));
		assertTrue(result.contains(link3));
		
		assertEquals(1, links.size());
		assertDates(links, Arrays.asList(date0101, date3009));
		assertNull(links.get(0).getUID());
		assertEquals("target1", ((TestLink) links.get(0)).getTarget());
	}
	
	public void testMergeMultipleSimultaneousAssignmentsToTwoTargets() {
		
		TestLink link11 = new TestLink(null, date0101, date3005, "target1");
		TestLink link12 = new TestLink(null, date0101, date3006, "target1");
		TestLink link13 = new TestLink(null, date0101, date3009, "target1");
		TestLink link21 = new TestLink(null, date0101, date3010, "target2");

		links.clear();
		links.add(link11);
		links.add(link12);
		links.add(link13);
		links.add(link21);
		
		result = cleanerAndMerger.mergeMultipleSimultaneousAssignments(links);
		
		assertNotNull(result);
		assertEquals(2, result.size());
		assertTrue(result.contains(link12));
		assertTrue(result.contains(link13));
		
		assertEquals(2, links.size());
		assertDates(links, Arrays.asList(date0101, date3009, date0101, date3010));
		assertNull(links.get(0).getUID());
		assertEquals("target1", ((TestLink) links.get(0)).getTarget());
		assertNull(links.get(1).getUID());
		assertEquals("target2", ((TestLink) links.get(1)).getTarget());
	}
	
	public void testMergeConsecutiveAssignmentsToOneTarget1() {
		
		TestLink link1 = new TestLink(null, date0101, date0203, "target1");
		TestLink link2 = new TestLink(null, date0303, date3009, "target1");
		TestLink link3 = new TestLink(null, date3110, date3112, "target1");
		
		links.clear();
		links.add(link1);
		links.add(link2);
		links.add(link3);

		result = cleanerAndMerger.mergeConsecutiveAssignments(links);
		
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(link2, result.iterator().next());
		
		assertEquals(2, links.size());
		assertDates(links, Arrays.asList(date0101, date3009, date3110, date3112));
		assertNull(links.get(0).getUID());
		assertEquals("target1", ((TestLink) links.get(0)).getTarget());
		assertNull(links.get(1).getUID());
		assertEquals("target1", ((TestLink) links.get(1)).getTarget());
	}
	
	public void testMergeConsecutiveAssignmentsToOneTarget2() {
		
		TestLink link1 = new TestLink(null, date0101, date0203, "target1");
		TestLink link2 = new TestLink(null, date3005, date3010, "target1");
		TestLink link3 = new TestLink(null, date3110, date3112, "target1");
		
		links.clear();
		links.add(link1);
		links.add(link2);
		links.add(link3);
		
		result = cleanerAndMerger.mergeConsecutiveAssignments(links);
		
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(link3, result.iterator().next());
		
		assertEquals(2, links.size());
		assertDates(links, Arrays.asList(date0101, date0203, date3005, date3112));
		assertNull(links.get(0).getUID());
		assertEquals("target1", ((TestLink) links.get(0)).getTarget());
		assertNull(links.get(1).getUID());
		assertEquals("target1", ((TestLink) links.get(1)).getTarget());
	}
	
	public void testMergeConsecutiveAssignmentsToTwoTargets() {
		
		TestLink link11 = new TestLink(null, date0101, date0203, "target1");
		TestLink link12 = new TestLink(null, date0303, date3010, "target1");
		TestLink link21 = new TestLink(null, date3110, date3112, "target2");
		
		links.clear();
		links.add(link11);
		links.add(link12);
		links.add(link21);
		
		result = cleanerAndMerger.mergeConsecutiveAssignments(links);
		
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(link12, result.iterator().next());
		
		assertEquals(2, links.size());
		assertDates(links, Arrays.asList(date0101, date3010, date3110, date3112));
		assertNull(links.get(0).getUID());
		assertEquals("target1", ((TestLink) links.get(0)).getTarget());
		assertNull(links.get(1).getUID());
		assertEquals("target2", ((TestLink) links.get(1)).getTarget());
	}

	public void testCleanAndMergeAssignmentsToOneTarget() {
		
		links.clear();
		links.add(new TestLink(null, date0101, date0203, "target1"));
		links.add(new TestLink(null, date0303, date3009, "target1"));
		links.add(new TestLink(null, date3110, date3112, "target1"));
		links.add(new TestLink(null, date0101, date0203, "target1"));
		links.add(new TestLink(null, date3005, date3010, "target1"));
		links.add(new TestLink(null, date3110, date3112, "target1"));

		result = cleanerAndMerger.cleanAndMergeAssignments(links);
		
		assertNotNull(result);
		assertEquals(5, result.size());
		
		assertEquals(1, links.size());
		assertDates(links, Arrays.asList(date0101, date3112));
		assertNull(links.get(0).getUID());
		assertEquals("target1", ((TestLink) links.get(0)).getTarget());
	}
	
	public void testCleanAndMergeAssignmentsToTwoTargets() {
		
		links.clear();
		links.add(new TestLink(null, date0101, date3005, "target1"));
		links.add(new TestLink(null, date0101, date3006, "target1"));
		links.add(new TestLink(null, date0101, date3009, "target1"));
		links.add(new TestLink(null, date0101, date0203, "target1"));
		links.add(new TestLink(null, date0303, date3010, "target1"));
		links.add(new TestLink(null, date3110, date3112, "target2"));
		
		result = cleanerAndMerger.cleanAndMergeAssignments(links);
		
		assertNotNull(result);
		assertEquals(4, result.size());
		
		assertEquals(2, links.size());
		assertDates(links, Arrays.asList(date0101, date3010, date3110, date3112));
		assertNull(links.get(0).getUID());
		assertEquals("target1", ((TestLink) links.get(0)).getTarget());
		assertNull(links.get(1).getUID());
		assertEquals("target2", ((TestLink) links.get(1)).getTarget());
		
	}
	
}
