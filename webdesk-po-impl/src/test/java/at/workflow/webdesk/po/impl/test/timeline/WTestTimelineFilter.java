package at.workflow.webdesk.po.impl.test.timeline;

import java.util.ArrayList;
import java.util.List;

import at.workflow.webdesk.po.timeline.TimelineFilter;
import at.workflow.webdesk.po.timeline.TimelineFilter.TimelineParts;
import at.workflow.webdesk.tools.HistoricizingDAOImpl;

/**
 * @author sdzuban 16.05.2013
 */
public class WTestTimelineFilter extends AbstractTimelineBase {
	
	
	private List<TestLink> fromDB = new ArrayList<TestLink>();
	private List<TestLink> tested = new ArrayList<TestLink>();
	private TimelineParts<TestLink> result;
	private TimelineFilter<TestLink> filter;
	private TestLink link1, link2;

	/** {@inheritDoc} */
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		super.onSetUpAfterDataGeneration();
		
		fromDB.clear();
		tested.clear();
		@SuppressWarnings("unchecked")
		HistoricizingDAOImpl<TestLink> dao = (HistoricizingDAOImpl<TestLink>) getBean("PoGroupDAO");
		filter = new TimelineFilter<TestLink>(dao);
		link1 = new TestLink(new TestEntity(), "name1");
		link2 = new TestLink(new TestEntity(), "name2");
	}
	
	public void testEmpties() {
		
		result = filter.filter(fromDB, tested);
		
		assertEquals(0, result.getNewChangedOrDeleted().size());
		assertEquals(0, result.getNewOnes().size());
		assertEquals(0, result.getDeleted().size());
		assertEquals(0, result.getChanged().size());
		assertEquals(0, result.getChangedWithOriginal().size());
	}

	public void testNew() {
		
		tested.add(link1);
		result = filter.filter(fromDB, tested);
		
		assertEquals(1, result.getNewChangedOrDeleted().size());
		assertEquals(1, result.getNewOnes().size());
		assertEquals(0, result.getDeleted().size());
		assertEquals(0, result.getChanged().size());
		assertEquals(0, result.getChangedWithOriginal().size());
		
		assertEquals(link1, result.getNewOnes().get(0));
	}
	
	public void testUnchanged() {
		
		link1.setUID("uid1");
		fromDB.add(link1);
		tested.add(link1);
		
		result = filter.filter(fromDB, tested);
		
		assertEquals(0, result.getNewChangedOrDeleted().size());
		assertEquals(0, result.getNewOnes().size());
		assertEquals(0, result.getDeleted().size());
		assertEquals(0, result.getChanged().size());
		assertEquals(0, result.getChangedWithOriginal().size());
	}
	
	public void testChanged() {
		
		link1.setUID("uid1");
		fromDB.add(link1);
		link2.setUID("uid1");
		tested.add(link2);
		
		result = filter.filter(fromDB, tested);
		
		assertEquals(1, result.getNewChangedOrDeleted().size());
		assertEquals(0, result.getNewOnes().size());
		assertEquals(0, result.getDeleted().size());
		assertEquals(1, result.getChanged().size());
		assertEquals(1, result.getChangedWithOriginal().size());
		
		assertEquals("name2", result.getChanged().get(0).getName());
	}
	
	public void testDeleted() {
		
		link1.setUID("uid1");
		fromDB.add(link1);
		
		result = filter.filter(fromDB, tested);
		
		assertEquals(1, result.getNewChangedOrDeleted().size());
		assertEquals(0, result.getNewOnes().size());
		assertEquals(1, result.getDeleted().size());
		assertEquals(0, result.getChanged().size());
		assertEquals(0, result.getChangedWithOriginal().size());
		
		assertEquals(link1, result.getDeleted().get(0));
	}
	
}
