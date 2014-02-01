package at.workflow.webdesk.po.impl.test;

import java.util.Arrays;
import java.util.Set;

import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.date.DateInterval;

/**
 * @author sdzuban 16.05.2013
 */
public class WTestHistorizationTimelineRepairerOverlapsOnly extends AbstractHistorizationTimelineRepairerTestCase {
	
	private Set<? extends Historization> result;
	
	public void testNoOverlap() {
		
		fromDB.clear();
		
		result = getRepairer().repairOverlaps(fromDB, links);
		
		assertNotNull(result);
		assertEquals(0, result.size());
		
		assertEquals(4, links.size());
		assertDates(links, Arrays.asList(date0101, date3103, date0104, date3006, date0107, date3009, date0110, date3112));
	}
	
	public void testSame() {
		
		result = getRepairer().repairOverlaps(fromDB, links);
		
		assertNotNull(result);
		assertEquals(0, result.size());
		
		assertEquals(4, links.size());
		assertDates(links, Arrays.asList(date0101, date3103, date0104, date3006, date0107, date3009, date0110, date3112));
	}

	public void testSameWithTrimming() {
		
		result = getRepairer().repairOverlaps(fromDB, links, new DateInterval(date0203, date3110));
		
		assertNotNull(result);
		assertEquals(0, result.size());
		
		assertEquals(4, links.size());
		assertDates(links, Arrays.asList(date0203, date3103, date0104, date3006, date0107, date3009, date0110, date3110));
	}
	
	public void testSameWithExpandingAttempt() {
		
		links.remove(0);
		links.add(0, new TestLink("one", date0203, date3103, "target1"));
		links.remove(2);
		links.add(2, new TestLink("four", date0110, date3110, "target4"));
		
		result = getRepairer().repairOverlaps(fromDB, links, new DateInterval(date0101, date3112));
		
		assertNotNull(result);
		assertEquals(0, result.size());
		
		assertEquals(4, links.size());
		assertDates(links, 
				Arrays.asList(date0203, date3103, date0104, date3006, date0107, date3009, date0110, date3110));
	}
	
	public void testExactOverlap() {

		fromDB.remove(3);
		fromDB.remove(2);
		links.clear();
		links.add(new TestLink("two", date0101, date3103, "target2"));
		links.add(new TestLink("three", date0107, date3009, "target3"));
		
		result = getRepairer().repairOverlaps(fromDB, links);
		
		assertNotNull(result);
		assertEquals(0, result.size());
		
		assertEquals(2, links.size());
		assertDates(links, Arrays.asList(date0101, date3103, date0107, date3009));
	}
	
	public void testCompleteOverlap() {
		
		links.remove(3);
		links.add(3, new TestLink("two", date0101, date3006, "target2"));
		links.remove(1);
		links.add(1, new TestLink("three", date0107, date3112, "target3"));
		
		result = getRepairer().repairOverlaps(fromDB, links);
		
		assertNotNull(result);
		assertEquals(0, result.size());
		
		assertEquals(2, links.size());
		assertDates(links, Arrays.asList(date0101, date3006, date0107, date3112));
	}
	
	public void testValidtoChangedToEarlier() {
		
		links.remove(0);
		links.add(0, new TestLink("one", date0101, date0203, "target1"));
		
		result = getRepairer().repairOverlaps(fromDB, links);
		
		assertNotNull(result);
		assertEquals(0, result.size());
		
		assertEquals(4, links.size());
		assertDates(links, 
				Arrays.asList(date0101, date0203, date0104, date3006, date0107, date3009, date0110, date3112));
	}
	
	public void testValidtoChangedToLater() {
		
		links.remove(0);
		links.add(0, new TestLink("one", date0101, date3005, "target1"));
		
		result = getRepairer().repairOverlaps(fromDB, links);
		
		assertNotNull(result);
		assertEquals(0, result.size());
		
		assertEquals(4, links.size());
		assertDates(links, 
				Arrays.asList(date0101, date3005, date3105, date3006, date0107, date3009, date0110, date3112));
	}
	
	public void testValidfromChangedToEarlier() {
		
		links.remove(3);
		links.add(3, new TestLink("two", date0303, date3006, "target2"));
		
		result = getRepairer().repairOverlaps(fromDB, links);
		
		assertNotNull(result);
		assertEquals(0, result.size());
		
		assertEquals(4, links.size());
		assertDates(links, 
				Arrays.asList(date0101, date0203, date0303, date3006, date0107, date3009, date0110, date3112));
	}
	
	public void testValidfromChangedToLater() {
		
		links.remove(3);
		links.add(3, new TestLink("two", date3105, date3006, "target2"));
		
		result = getRepairer().repairOverlaps(fromDB, links);
		
		assertNotNull(result);
		assertEquals(0, result.size());
		
		assertEquals(4, links.size());
		assertDates(links, 
				Arrays.asList(date0101, date3103, date3105, date3006, date0107, date3009, date0110, date3112));
	}
	
	public void testValidtoSkipsValidfrom() {
		
		links.remove(1);
		links.add(1, new TestLink("three", date0107, date0203, "target3"));

		result = getRepairer().repairOverlaps(fromDB, links);
		
		assertNotNull(result);
		assertEquals(1, result.size()); // new link removed due to being negative
		
		assertEquals(3, links.size());
		assertDates(links, 
				Arrays.asList(date0101, date3103, date0104, date3006, date0110, date3112));
	}
	
	public void testValidtoSkipsNeighbor() {
		
		links.remove(3);
		links.add(3, new TestLink("two", date0104, date3010, "target2"));
		
		result = getRepairer().repairOverlaps(fromDB, links);
		
		assertNotNull(result);
		assertEquals(0, result.size());
		
		assertEquals(3, links.size());
		assertDates(links, 
				Arrays.asList(date0101, date3103, date0104, date3010, date3110, date3112));
	}
	
	public void testValidfromSkipsNeighbor() {

		// this is impossible to test because of sorting according to validfrom 
		// which prevents any skipping constellation
	}
	
	public void testValidfromSkipsValidto() {
		
		links.remove(1);
		links.add(1, new TestLink("three", date3010, date3009, "target3"));
		
		result = getRepairer().repairOverlaps(fromDB, links);
		
		assertNotNull(result);
		assertEquals(1, result.size()); // new link removed due to being negative

		assertEquals(3, links.size());
		assertDates(links, 
				Arrays.asList(date0101, date3103, date0104, date3006, date0110, date3112));
	}
	
	public void testValidtoChangedToEarlierNew() {
		
		links.remove(0);
		links.add(0, new TestLink("one", date0101, date0203, "target1"));
		fromDB.clear();
		
		result = getRepairer().repairOverlaps(fromDB, links);
		
		assertNotNull(result);
		assertEquals(0, result.size());
		
		assertEquals(4, links.size());
		// different than changed because when both dates are changed valid from is taken as fix
		assertDates(links, 
				Arrays.asList(date0101, date0203, date0104, date3006, date0107, date3009, date0110, date3112));
	}
	
	public void testValidtoChangedToLaterNew() {
		
		links.remove(0);
		links.add(0, new TestLink("one", date0101, date3005, "target1"));
		fromDB.clear();
		
		result = getRepairer().repairOverlaps(fromDB, links);
		
		assertNotNull(result);
		assertEquals(0, result.size());
		
		assertEquals(4, links.size());
		// different than changed because when both dates are changed valid from is taken as fix
		assertDates(links, 
				Arrays.asList(date0101, date3103, date0104, date3006, date0107, date3009, date0110, date3112));
	}
	
	public void testValidfromChangedToEarlierNew() {
		
		links.remove(3);
		links.add(3, new TestLink("two", date0303, date3006, "target2"));
		fromDB.clear();
		
		result = getRepairer().repairOverlaps(fromDB, links);
		
		assertNotNull(result);
		assertEquals(0, result.size());
		
		assertEquals(4, links.size());
		assertDates(links, 
				Arrays.asList(date0101, date0203, date0303, date3006, date0107, date3009, date0110, date3112));
	}
	
	public void testValidfromChangedToLaterNew() {
		
		links.remove(3);
		links.add(3, new TestLink("two", date3105, date3006, "target2"));
		fromDB.clear();
		
		result = getRepairer().repairOverlaps(fromDB, links);
		
		assertNotNull(result);
		assertEquals(0, result.size());
		
		assertEquals(4, links.size());
		assertDates(links, 
				Arrays.asList(date0101, date3103, date3105, date3006, date0107, date3009, date0110, date3112));
	}
	
	public void testValidtoSkipsValidfromNew() {
		
		links.remove(1);
		links.add(1, new TestLink("three", date0107, date0203, "target3"));
		fromDB.clear();
		
		result = getRepairer().repairOverlaps(fromDB, links);

		assertNotNull(result);
		assertEquals(1, result.size()); // new link removed due to being negative
		
		assertEquals(3, links.size());
		assertDates(links, 
				Arrays.asList(date0101, date3103, date0104, date3006, date0110, date3112));
	}
	
	public void testValidtoSkipsNeighborNew() {
		
		links.remove(3);
		links.add(3, new TestLink("two", date0104, date3010, "target2"));
		fromDB.clear();
		
		result = getRepairer().repairOverlaps(fromDB, links);
		
		assertNotNull(result);
		assertEquals(0, result.size());
		
		// different than changed because when both dates are changed valid from is taken as fix
		assertEquals(5, links.size());
		assertDates(links, 
				Arrays.asList(date0101, date3103, date0104, date3006, date0107, date3009, date0110, date3010, date3110, date3112));
	}
	
	public void testValidfromSkipsNeighborNew() {
		
		// this is impossible to test because of sorting according to validfrom 
		// which prevents any skipping constellation
	}
	
	public void testValidfromSkipsValidtoNew() {
		
		links.remove(1);
		links.add(1, new TestLink("three", date3010, date3009, "target3"));
		fromDB.clear();
		
		result = getRepairer().repairOverlaps(fromDB, links);
		
		assertNotNull(result);
		assertEquals(1, result.size()); // new link removed due to being negative

		assertEquals(3, links.size());
		assertDates(links, 
				Arrays.asList(date0101, date3103, date0104, date3006, date0110, date3112));
	}
	
}
