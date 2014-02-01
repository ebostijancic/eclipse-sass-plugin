package at.workflow.webdesk.po.timeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.EditableTable;
import at.workflow.webdesk.po.HistorizationWithShare;
import at.workflow.webdesk.po.SharedTimelineTable;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.date.EditableDateInterval;

/**
 * This is data structure intended for definition of 
 * timeline assignments with share where sum of shares must 
 * be specified value all the time.
 * The column axis is time axis as represented by EditableDateIntervals in header.
 * The row axis is the axis of assigned objects as represented by rowObjects 
 * at the rows beginnings.
 * The cell content is the share, e.g. 20 per cent.
 * 
 * This is also linear structure for tabular presentation of 
 * {@link at.workflow.webdesk.po.timeline.TimelineFragment}
 * and of timeline information in general.
 * 
 * @author sdzuban 04.04.2013
 */
public class SharedTimelineTableImpl<ASSIGNED extends PersistentObject, SHARE extends Number> 
	implements SharedTimelineTable<ASSIGNED, SHARE> {

	private class RowImpl implements EditableTable.Row<ASSIGNED, SHARE> {

		private ASSIGNED rowObject;
		private List<SHARE> rowContent;

		public RowImpl() {
			rowContent = new ArrayList<SHARE>(header.size());
			for (int i = 0; i < header.size(); i++)
				rowContent.add(null);
		}

		@Override
		public ASSIGNED getRowObject() { return rowObject; }
		@Override
		public void setRowObject(ASSIGNED rowObject) { this.rowObject = rowObject; }
		@Override
		public List<SHARE> getRowContent() { return rowContent; }
		@Override
		public void setRowContent(List<SHARE> rowContent) { this.rowContent = rowContent; }

		/** {@inheritDoc} */
		@Override
		public String toString() {
			return "Row [" + (rowObject != null ? rowObject.toString() : "null") + rowContent + "]";
		}
	}

	private List<EditableDateInterval> header = new ArrayList<EditableDateInterval>();
	private List<Row<ASSIGNED, SHARE>> rows = new ArrayList<Row<ASSIGNED, SHARE>>();

    /** generates empty table without rows and without columns */
    public SharedTimelineTableImpl() { }

    /** generates empty table with defined numbers of rows and columns */
    public SharedTimelineTableImpl(int rowCount, int columnCount) {

        for (int idx = 0; idx < columnCount; idx++)
            addColumn();

        for (int idx = 0; idx < rowCount; idx++)
            addRow();
    }

	/** fills the table from list of TimelineFragments */
    @Override
	@SuppressWarnings("unchecked")
	public <ASSIGNMENT extends HistorizationWithShare> void 
		fillTableFromFragments(List<TimelineFragment<ASSIGNED, ASSIGNMENT>> fragments) {

		if (fragments == null || fragments.isEmpty())
			return;

		Map<ASSIGNED, Integer> rowIndexMap = new HashMap<ASSIGNED, Integer>();
		for (TimelineFragment<ASSIGNED, ASSIGNMENT> fragment : fragments) {
			
			// every time line fragment gets new column
			int columnIndex = addColumn();
			// collect header
			setHeader(fragment.getDateInterval(), columnIndex);

			for (Map.Entry<ASSIGNED, ASSIGNMENT> assignedAssignment : 
		    											fragment.getValidAssignments().entrySet()) {
		         Integer rowIndex = rowIndexMap.get(assignedAssignment.getKey());
		         if (rowIndex == null) {
		             rowIndex = addRow();
		             setRowObject(assignedAssignment.getKey(), rowIndex);
		             rowIndexMap.put(assignedAssignment.getKey(), rowIndex);
		         }
		         setContent((SHARE) assignedAssignment.getValue().getShare(), rowIndex, columnIndex);
		         setAdditionalValues(assignedAssignment.getValue(), rowIndex, columnIndex);
			}
		}
	}

	/**
	 * To be overridden if additional data is to be handled like primaryCostCenter etc.
	 * @param rowIdx
	 * @param columnIdx
	 * @param validAssignment
	 */
	protected void setAdditionalValues(HistorizationWithShare validAssignment, int rowIdx, int columnIdx) {
	}

	/** {@inheritDoc} */
	@Override
	public int addColumn() {

		header.add(new EditableDateInterval());
		for (Row<ASSIGNED, SHARE> row : rows)
			row.getRowContent().add(null);
		return header.size() - 1;
	}

	/** {@inheritDoc} */
	@Override
	public void addColumn(int columnIdx) {

		checkColumnIndex(columnIdx);
		header.add(columnIdx, new EditableDateInterval());
		for (Row<ASSIGNED, SHARE> row : rows)
			row.getRowContent().add(columnIdx, null);
	}

	/** {@inheritDoc} */
	@Override
	public void removeColumn(int columnIdx) {

		checkColumnIndex(columnIdx);
		header.remove(columnIdx);
		for (Row<ASSIGNED, SHARE> row : rows)
			row.getRowContent().remove(columnIdx);
	}

	/** {@inheritDoc} */
	@Override
	public int addRow() {

		rows.add(new RowImpl());
		return rows.size() - 1;
	}

	/** {@inheritDoc} */
	@Override
	public void addRow(int rowIdx) {

		checkRowIndex(rowIdx);
		rows.add(rowIdx, new RowImpl());
	}

	/** {@inheritDoc} */
	@Override
	public void removeRow(int rowIdx) {

		checkRowIndex(rowIdx);
		rows.remove(rowIdx);
	}

	/** {@inheritDoc} */
	@Override
	public EditableDateInterval getHeader(int columnIdx) {

		checkColumnIndex(columnIdx);
		return header.get(columnIdx);
	}

	/** {@inheritDoc} */
	@Override
	public void setHeader(EditableDateInterval header, int columnIdx) {

		checkColumnIndex(columnIdx);
		this.header.set(columnIdx, header);
	}

	/** {@inheritDoc} */
	@Override
	public ASSIGNED getRowObject(int rowIdx) {

		checkRowIndex(rowIdx);
		return rows.get(rowIdx).getRowObject();
	}

	@Override
	/** {@inheritDoc} */
	public void setRowObject(ASSIGNED rowObject, int rowIdx) {

		checkRowIndex(rowIdx);
		rows.get(rowIdx).setRowObject(rowObject);
	}

	/** {@inheritDoc} */
	@Override
	public SHARE getContent(int rowIdx, int columnIdx) {

		checkRowIndex(rowIdx);
		checkColumnIndex(columnIdx);
		return rows.get(rowIdx).getRowContent().get(columnIdx);
	}

	/** {@inheritDoc} */
	@Override
	public void setContent(SHARE content, int rowIdx, int columnIdx) {

		checkRowIndex(rowIdx);
		checkColumnIndex(columnIdx);
		rows.get(rowIdx).getRowContent().set(columnIdx, content);
	}

	/** {@inheritDoc} */
	@Override
	public int addRow(Row<ASSIGNED, SHARE> row) {
		rows.add(row);
		return rows.size() - 1;
	}

	/** {@inheritDoc} */
	@Override
	public void addRow(Row<ASSIGNED, SHARE> row, int rowIdx) {
		rows.add(rowIdx, row);
	}

	//   public void addRow(ASSIGNED assigned, List<SHARE> shares) {
	//	   addRow(new RowImpl(assigned, shares));
	//   }

	/** {@inheritDoc} */
	@Override
	public Row<ASSIGNED, SHARE> getRow(int rowIdx) {
		return rows.get(rowIdx);
	}

	/** {@inheritDoc} */
	@Override
	public int getRowCount() {
		return rows.size();
	}

	/** {@inheritDoc} */
	@Override
	public int getColumnCount() {
		return header.size();
	}


	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.getClass().getClass().getSimpleName() + "[" + 
				(header == null ? "null" : header.toString()) + ", [" +
				(rows == null ? "null" : rows.toString()) + "]]";
	}

	//	--------------------------- PROTECTED METHODS -------------------------------

	/** to be used also in derivations */
	protected void checkColumnIndex(int columnIdx) {

		if (columnIdx >= header.size())
			throw new IllegalArgumentException("Column index out of bounds");
	}

	/** to be used also in derivations */
	protected void checkRowIndex(int rowIdx) {

		if (rowIdx >= rows.size())
			throw new IllegalArgumentException("Row index out of bounds");
	}

}
