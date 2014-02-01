package at.workflow.webdesk.po.impl.test;

import java.util.Arrays;
import java.util.Set;

import at.workflow.webdesk.po.timeline.HistorizationTimelineUtils;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.date.DateInterval;

/**
 * @author sdzuban 16.05.2013
 */
public class WTestHistorizationTimelineRepairerTimelineBegin extends AbstractHistorizationTimelineRepairerTestCase {
	
	private Set<? extends Historization> result;
	
	public void testSameWithTrimming() {
		
		result = getRepairer().repairOverlapsAndTimeline(fromDB, links, new DateInterval(date0203, date3110), date0203);
		
		assertNotNull(result);
		assertEquals(0, result.size());
		
		assertEquals(4, links.size());
		assertDates(HistorizationTimelineUtils.getSortedHistorizationList(links), 
				Arrays.asList(date0203, date3103, date0104, date3006, date0107, date3009, date0110, date3110));

		result = getRepairer().repairOverlapsAndTimeline(fromDB, links, new DateInterval(date0203, date3110), date3110);
		
		assertNotNull(result);
		assertEquals(0, result.size());
		
		assertEquals(4, links.size());
		assertDates(HistorizationTimelineUtils.getSortedHistorizationList(links), 
				Arrays.asList(date0203, date3103, date0104, date3006, date0107, date3009, date0110, date3110));
	}
	
	public void testGapLinkOverlapLinkGapLinkCompleteTimeline() {
		
		fromDB.clear();
		links.clear();
		links.add(new TestLink("one", date0203, date3105, "target1"));
		links.add(new TestLink("two", date0104, date3006, "target2"));
		links.add(new TestLink("three", date0110, date3010, "target3"));
		
		result = getRepairer().repairOverlapsAndTimeline(fromDB, links, new DateInterval(date0101, date3112), date0101);
		
		assertNotNull(result);
		assertEquals(0, result.size());
		
		assertEquals(3, links.size());
		assertDates(HistorizationTimelineUtils.getSortedHistorizationList(links), 
				Arrays.asList(date0101, date3103, date0104, date3009, date0110, date3112));
	}
	
	public void testGapLinkOverlapLinkGapLinkPartialTimeline() {
		
		fromDB.clear();
		links.clear();
		links.add(new TestLink("one", date0203, date3105, "target1"));
		links.add(new TestLink("two", date0104, date3006, "target2"));
		links.add(new TestLink("three", date0110, date3010, "target3"));
		
		result = getRepairer().repairOverlapsAndTimeline(fromDB, links, new DateInterval(date0101, date3112), date0107);
		
		assertNotNull(result);
		assertEquals(0, result.size());
		
		assertEquals(3, links.size());
		assertDates(HistorizationTimelineUtils.getSortedHistorizationList(links), 
				Arrays.asList(date0203, date3103, date0104, date3006, date0107, date3112));
	}
	
	public void testGapLinkOverlapLinkGapLinkNoTimeline() {
		
		fromDB.clear();
		links.clear();
		links.add(new TestLink("one", date0203, date3105, "target1"));
		links.add(new TestLink("two", date0104, date3006, "target2"));
		links.add(new TestLink("three", date0110, date3010, "target3"));
		
		result = getRepairer().repairOverlapsAndTimeline(fromDB, links, new DateInterval(date0101, date3112), date3112);
		
		assertNotNull(result);
		assertEquals(0, result.size());
		
		assertEquals(3, links.size());
		assertDates(HistorizationTimelineUtils.getSortedHistorizationList(links), 
				Arrays.asList(date0203, date3103, date0104, date3006, date0110, date3010));
	}
	
}
