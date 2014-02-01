package at.workflow.webdesk.po;

import java.util.List;

/**
 * This is general interface for editable table 
 * to which rows and columns can be added
 * at any place and can also be deleted.
 * 
 * @param HEADER type of object in column's header
 * @param ROWOBJECT type of object at the rows beginning
 * @param CELLCONTENT type of objects in the cells of the table
 * 
 * @author sdzuban 07.04.2013
 */
public interface EditableTable<HEADER, ROWOBJECT, CELLCONTENT> {

	/** this interface represents the row as an object with list of associated values */
	public interface Row<ROWOBJECT, CELLCONTENT> {
		
		/** @return object associated with the row (the -1st column) */
		ROWOBJECT getRowObject();
		
		/** object associated with the row (the -1st column) */
		void setRowObject(ROWOBJECT rowObject);
		
		/** @return content of the cells of the row */
		List<CELLCONTENT> getRowContent();
		
		/** content of the cells of the row */
		void setRowContent(List<CELLCONTENT> rowContent);
	}

	/** adds new empty column as last column 
	 * @return index of added column */
    int addColumn();

    /** adds new empty column at the position defined by columnIdx. Existing columns are shifted right. */
    void addColumn(int columnIdx);

    /** removes column at columnIdx */
    void removeColumn(int columnIdx);

    /** adds new empty row as last row 
     * @return index of added row */
    int addRow();

    /** adds new empty row at the position defined by rowIdx. Existing rows are shifted down. */
    void addRow(int rowIdx);

    /** removes row at rowIdx */
    void removeRow(int rowIdx);

    /** @return header of the column at given index */
    HEADER getHeader(int columnIdx);

    /** sets header of the column at given index */
    void setHeader(HEADER header, int columnIdx);

    /** @return rowobject of the row at given index */
    ROWOBJECT getRowObject(int rowIdx);

    /** sets rowobject of the row at given index */
    void setRowObject(ROWOBJECT rowobject, int rowIdx);

    /** @return content of the cell at given indexes */
    CELLCONTENT getContent(int rowIdx, int columnIdx);

    /** sets content of the cell at given indexes */
    void setContent(CELLCONTENT content, int rowIdx, int columnIdx);
 
    /** adds row as last row 
     * @return index of added row */
    int addRow(Row<ROWOBJECT, CELLCONTENT> row);
    
    /** adds row at the position defined by rowIdx. Existing rows are shifted down. 
     * @return index of added row */
    void addRow(Row<ROWOBJECT, CELLCONTENT> row, int rowIdx);
    
    /** returns row at given index */
    Row<ROWOBJECT, CELLCONTENT> getRow(int rowIdx);
    
    int getRowCount();
    
    int getColumnCount();
}
