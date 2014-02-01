package at.workflow.webdesk.po.impl.test.sharedTimelineHandler;

import java.util.Date;

import at.workflow.webdesk.po.HistorizationWithShare;
import at.workflow.webdesk.po.SharedTimelineTable;
import at.workflow.webdesk.po.SharedTimelineTableAdapter;
import at.workflow.webdesk.po.util.PoLinkingUtils;
import at.workflow.webdesk.po.util.ToStringHelper;
import at.workflow.webdesk.tools.numbers.NumberComparator;

/**
 * @author sdzuban 04.04.2013
 */
public class MyCostCentersTableAdapter implements SharedTimelineTableAdapter<MyGroup, MyGroupCostCenter, MyCostCenter, Float> {

	// this class is necessary because the test classes are in module 'po' but start with 'My'
	private static final Class<? extends HistorizationWithShare> linkClass = 
				at.workflow.webdesk.po.impl.test.sharedTimelineHandler.MyGroupCostCenter.class;
	
	private NumberComparator comparator;
	
	private SharedTimelineTable<MyCostCenter, Float> table;
	private MyGroup assignee;
	private final Float zero = 0.0f;
	
	
	/** {@inheritDoc} */
	@Override
	public void copyAdditionalValues(SharedTimelineTable<MyCostCenter, Float> sourceTable, int srcRowIdx, int srcColIdx,
			SharedTimelineTable<MyCostCenter, Float> destinationTable, int dstRowIdx, int dstColIdx) {
	}

	/** {@inheritDoc} */
	@Override
	public MyGroupCostCenter assignFromTable(int rowIdx, int columnIdx, Date from, Date to) {
		MyGroupCostCenter newLink = (MyGroupCostCenter) PoLinkingUtils.getLinkObject(linkClass, assignee, table.getRowObject(rowIdx), from, to);
		newLink.setShare(table.getContent(rowIdx, columnIdx));
		return newLink;
	}

	/** {@inheritDoc} */
	@Override
	public void fillContentFromTable(MyGroupCostCenter assignment, int rowIdx, int columnIdx) {
		assignment.setShare(table.getContent(rowIdx, columnIdx));
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEqual(int rowIdx, int columnIdx1, int columnIdx2) {
		return comparator.compare(table.getContent(rowIdx, columnIdx1), table.getContent(rowIdx, columnIdx2)) == 0;
	}
	

	/** {@inheritDoc} */
	@Override
	public void delete(MyGroupCostCenter assignment) {
		assignment.getGroup().getGroupCostCenters().remove(assignment);
		assignment.getCostCenter().getGroupCostCenters().remove(assignment);
	}

	/** {@inheritDoc} */
	@Override
	public void save(MyGroupCostCenter assignment) {
		if (comparator.compare(assignment.getShare(), zero) == 0) {
			delete(assignment);
			return;
		}
		if (!assignment.getGroup().getGroupCostCenters().contains(assignment))
			assignment.getGroup().addGroupCostCenter(assignment);
		if (!assignment.getCostCenter().getGroupCostCenters().contains(assignment))
			assignment.getCostCenter().addGroupCostCenter(assignment);
	}


	/** {@inheritDoc} */
	@Override
	public boolean isZeroShare(int rowIdx, int columnIdx) {
		return comparator.compare(table.getContent(rowIdx, columnIdx), zero) == 0;
	}

	
	/** {@inheritDoc} */
	@Override
	public void setZeroShare(int rowIdx, int columnIdx) {
		table.setContent(zero, rowIdx, columnIdx);
	}
	
	
	/** {@inheritDoc} */
	@Override
	public void setTable(SharedTimelineTable<MyCostCenter, Float> table) {
		this.table = table;
	}
	
	
	/** {@inheritDoc} */
	@Override
	public SharedTimelineTable<MyCostCenter, Float> getTable() {
		return table;
	}
	
	/** {@inheritDoc} */
	@Override
	public void setAssignee(MyGroup assignee) {
		this.assignee = assignee;
	}
	
	/** {@inheritDoc} */
	@Override
	public MyGroup getAssignee() {
		return assignee;
	}
	
	/** {@inheritDoc} */
	@Override
	public void setTolerance(Float tolerance) {
		comparator = new NumberComparator(Float.class, tolerance);
	}

	@Override
	public String toString() {
		return ToStringHelper.toString(this);
	}

}
