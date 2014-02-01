package at.workflow.webdesk.po.impl.test.sharedTimelineHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import at.workflow.webdesk.po.EditableTable;
import at.workflow.webdesk.po.HistorizationWithShare;
import at.workflow.webdesk.po.SharedTimelineHandler;
import at.workflow.webdesk.po.EditableTable.Row;
import at.workflow.webdesk.po.timeline.TimelineFragment;
import at.workflow.webdesk.tools.date.EditableDateInterval;
import at.workflow.webdesk.tools.date.DateTools;

/**
 * @author sdzuban 04.04.2013
 */
public abstract class AbstractSharedTimelineHandlerTest extends TestCase {

	protected SharedTimelineHandler<MyGroup, MyGroupCostCenter, MyCostCenter, Float> timelineHandler;
	protected Date today;
	protected Date infinity = DateTools.INFINITY;
	protected MyGroup group;
	protected Date date1, date2, date3;
	protected MyGroupCostCenter link;
	protected MyCostCenter cc;
	
	/** {@inheritDoc} */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		timelineHandler = new MyGroupCostCenterHandler();
			
		today = DateTools.today();
		
		date1 = DateTools.toDate(2100, 6, 15);
		date2 = DateTools.toDate(2200, 6, 15);
		date3 = DateTools.toDate(2300, 6, 15);
		
		group = new MyGroup();
		group.setValidfrom(today);
		group.setValidto(infinity);
		cc = new MyCostCenter();
		cc.setUID("test1"); // as if persisted
		// first assignment, full share, full validity
		timelineHandler.assign(group, cc, today, infinity);
		link = group.getGroupCostCenters().iterator().next();
	}
	

	protected void printFragments(List<TimelineFragment<MyCostCenter, MyGroupCostCenter>> fragments) {
		
		System.out.println("================================================================================");
		
		for (TimelineFragment<MyCostCenter, MyGroupCostCenter> fragment : fragments) {
			System.out.println(fragment.getDateInterval().getIntervalAsString("dd.MM.yyyy HH:mm:ss"));
			for (HistorizationWithShare assignment : fragment.getValidAssignments().values())
				System.out.println(((MyGroupCostCenter) assignment).getCostCenter().getUID() + 
						": " + assignment.getShare().floatValue() + " " + 
				assignment.getValidfrom() + " - " + assignment.getValidto());
		}
	}
	
	protected void printTimelineTable(EditableTable<EditableDateInterval, MyCostCenter, Float> table) {
		
		System.out.println("-------------------------------------------------------------");
		for (int idx = 0; idx < table.getColumnCount(); idx++) {
			EditableDateInterval intval = table.getHeader(idx);
			System.out.print(intval.getIntervalAsString("dd.MM.yyyy HH") + " | ");
		}
		System.out.println();
		for (int idx = 0; idx < table.getRowCount(); idx++) {
			Row<MyCostCenter, Float> row = table.getRow(idx);
			System.out.print(row.getRowObject().getUID() + "      |      ");
			
			for (Float share : row.getRowContent())
				System.out.print((share == null ? "null" : share.floatValue()) + "      |      ");
			
			System.out.println();
		}
	}

	protected List<Float> getRowContent(EditableTable<EditableDateInterval, MyCostCenter, Float> table, int rowIdx) {
		
		List<Float> result = new ArrayList<Float>();
		for (int idx = 0; idx < table.getColumnCount(); idx++)
			result.add(table.getContent(rowIdx, idx));
		return result;
	}
	
}
