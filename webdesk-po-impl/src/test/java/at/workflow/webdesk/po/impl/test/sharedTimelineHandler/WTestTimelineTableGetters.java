package at.workflow.webdesk.po.impl.test.sharedTimelineHandler;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import at.workflow.webdesk.po.EditableTable;
import at.workflow.webdesk.po.SharedTimelineTable;
import at.workflow.webdesk.po.timeline.SharedTimelineTableImpl;
import at.workflow.webdesk.po.timeline.TimelineFragment;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.date.EditableDateInterval;
import at.workflow.webdesk.tools.date.Interval;

/**
 * This test is in impl only because of dependency on HistorizationTImelineImpl.
 * 
 * @author sdzuban 04.04.2013
 */
public class WTestTimelineTableGetters extends AbstractSharedTimelineHandlerTest {

	private Date from = DateTools.toDate(2100, 6, 15);
	private Date to = DateTools.lastMomentOfDay(DateTools.toDate(2200, 6, 15));
	
	
	public void testGetRequiredSumNumber() {

		assertEquals(100.0f, timelineHandler.getShareSumTarget().floatValue());
	}
	
	public void testGetSortedTimelineFragments() {

		try {
			timelineHandler.getSortedTimelineFragments(null);
			fail("Accepted null timeline");
		} catch (Exception e) {}

		List<TimelineFragment<MyCostCenter, MyGroupCostCenter>> result = 
				timelineHandler.getSortedTimelineFragments(Collections.<MyGroupCostCenter>emptyList());
		assertNotNull(result);
		assertEquals(0, result.size());

		result = timelineHandler.getSortedTimelineFragments(group.getGroupCostCenters());
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(group.getValidfrom(), result.get(0).getDateFrom());
		assertEquals(group.getValidto(), result.get(0).getDateTo());
		assertEquals(group.getGroupCostCenters().iterator().next(), result.get(0).getValidAssignments().values().iterator().next());

		// cc1 |----100----|---80---|-----100-------|
		// cc2             |---20---|

		MyCostCenter cc2 = new MyCostCenter();
		cc2.setUID("test2"); // as if persisted
		timelineHandler.assign(group, getTimelineTable(cc2, from, to));

		timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
		
		List<TimelineFragment<MyCostCenter, MyGroupCostCenter>> fragments = timelineHandler.getSortedTimelineFragments(group.getGroupCostCenters());
		assertNotNull(fragments);
		assertEquals(3, fragments.size());
		assertEquals(1, fragments.get(0).getValidAssignments().size());
		assertEquals(2, fragments.get(1).getValidAssignments().size());
		assertEquals(1, fragments.get(2).getValidAssignments().size());
	}

	public void testGetTimelineTable() {

		try {
			timelineHandler.getTimelineTable(null);
			fail("Accepted null timeline");
		} catch (Exception e) {}

		SharedTimelineTable<MyCostCenter, Float> result = timelineHandler.getTimelineTable(Collections.<MyGroupCostCenter>emptyList());
		assertNotNull(result);
		assertEquals(0, result.getColumnCount());
		assertEquals(0, result.getRowCount());

		result = timelineHandler.getTimelineTable(group.getGroupCostCenters());
		assertNotNull(result);
		assertEquals(1, result.getColumnCount());
		Interval header0 = result.getHeader(0);
		assertEquals(today, header0.getFrom());
		assertEquals(infinity, header0.getTo());

		assertEquals(1, result.getRowCount());
		PersistentObject key = result.getRowObject(0);
		assertNotNull(key);
		assertEquals(link.getCostCenter(), key);
		assertEquals(group.getGroupCostCenters().iterator().next().getShare(), result.getContent(0, 0));


		// cc1 |----100----|---80---|-----100-------|
		// cc2             |---20---|

		MyCostCenter cc2 = new MyCostCenter();
		cc2.setUID("test2"); // as if persisted
		timelineHandler.assign(group, getTimelineTable(cc2, from, to));

		timelineHandler.checkTimeline(group.getGroupCostCenters(), group);
		
		List<TimelineFragment<MyCostCenter, MyGroupCostCenter>> fragments = timelineHandler.getSortedTimelineFragments(group.getGroupCostCenters());
		
		printFragments(fragments);
		
		assertNotNull(fragments);
		assertEquals(3, fragments.size());
		assertEquals(1, fragments.get(0).getValidAssignments().size());
		assertEquals(2, fragments.get(1).getValidAssignments().size());
		assertEquals(1, fragments.get(2).getValidAssignments().size());
		
		EditableTable<EditableDateInterval, MyCostCenter, Float> table = timelineHandler.getTimelineTable(group.getGroupCostCenters());
		
		printTimelineTable(table);
		
		assertNotNull(table);
		assertEquals(3, table.getColumnCount());
		assertEquals(2, table.getRowCount()); // 2 rows
		List<Float> row0 = getRowContent(table, 0);
		List<Float> row1 = getRowContent(table, 1);
		assertListContent(row0);
		assertListContent(row1);

	}

	//	-------------------- PRIVATE METHODS ----------------------

	private void assertListContent(List<Float> row) {

		if (row.get(0) == null) {
			assertEquals(20.0f, row.get(1));
			assertNull(row.get(2));
		} else {
			assertEquals(100.0f, row.get(0));
			assertEquals(80.0f, row.get(1));
			assertEquals(100.0f, row.get(2));
		}
	}

	private SharedTimelineTable<MyCostCenter, Float> getTimelineTable(MyCostCenter cc2, Date fromDate, Date toDate) {
		
		SharedTimelineTable<MyCostCenter, Float> table = new SharedTimelineTableImpl<MyCostCenter, Float>(2, 1);
		table.setHeader(new EditableDateInterval(fromDate, DateTools.lastMomentOfDay(toDate)), 0);
		table.setRowObject(cc, 0);
		table.setContent(80.0f, 0, 0);
		table.setRowObject(cc2, 1);
		table.setContent(20.0f, 1, 0);
		return table;
	}
	
}
