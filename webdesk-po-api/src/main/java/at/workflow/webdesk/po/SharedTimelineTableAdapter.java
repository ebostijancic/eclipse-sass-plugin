package at.workflow.webdesk.po;

import java.util.Date;

import at.workflow.webdesk.tools.api.PersistentObject;

/**
 * This is interface to be implemented by adapter between
 * {@link at.workflow.webdesk.po.SharedTimelineHandler}
 * and the {@link at.workflow.webdesk.po.timeline.SharedTimelineTableImpl}.
 * 
 * @author sdzuban 12.04.2013
 */
public interface SharedTimelineTableAdapter<ASSIGNEE extends PersistentObject, 
							ASSIGNMENT extends HistorizationWithShare, 
							ASSIGNED extends PersistentObject, 
							SHARE extends Number> {

	/** 
	 * Assigns the stored assignee with the rowObject from the identified row 
	 * and value from identified cell for the specified time. 
	 * If the table manages more than value via sub classing all the values shall be set.
	 * @param rowIdx
	 * @param columnIdx
	 * @param from
	 * @param to
	 * @return created assignment
	 * @throw runtime exception if the assignment is not possible
	 */
	ASSIGNMENT assignFromTable(int rowIdx, int columnIdx, Date from, Date to);
	
	
	/**
	 * Fills content stored from identified cell to the assignment. 
	 * If the table manages more than one value via sub classing
	 * all the values shall be set.
	 * @param assignment
	 * @param rowIdx
	 * @param columnIdx
	 */
	void fillContentFromTable(ASSIGNMENT assignment, int rowIdx, int columnIdx);
	
	
	/**
	 * Compares cell content at given two position with the defined tolerance.
	 * If the table manages more than one value via sub classing
	 * all the values shall be compared to their pendants..
	 * @param rowIdx
	 * @param columnIdx1
	 * @param columnIdx2
	 * @return true if the two cell positions contain same data
	 */
	boolean isEqual(int rowIdx, int columnIdx1, int columnIdx2);


	/**
	 * Checks whether the specified content is zero share
	 * @param rowIdx
	 * @param columnIdx
	 * @return
	 */
	boolean isZeroShare(int rowIdx, int columnIdx);


	/**
	 * Sets the contents at specified coordinates to zero
	 * @param rowIdx
	 * @param columnIdx
	 */
	void setZeroShare(int rowIdx, int columnIdx);
	
	
	/**
	 * Saves the assignment either in the DB 
	 * or links the assignment lists of 
	 * both assignee and assigned
	 * to the assignment
	 * if it is not already linked.
	 * Can also call delete if the assignment
	 * is not worth storing like the share is 0 etc.
	 * @param assignment
	 */
	void save(ASSIGNMENT assignment);
	
	/**
	 * Either deletes assignment from DB,
	 * historizes it or deletes it from
	 * the lists of assignments of both
	 * the assignee and assigned.
	 * @param assignment
	 */
	void delete(ASSIGNMENT assignment);
	

	void setTable(SharedTimelineTable<ASSIGNED, SHARE> table);

	SharedTimelineTable<ASSIGNED, SHARE> getTable();
	
	void setAssignee(ASSIGNEE assignee);
	
	ASSIGNEE getAssignee();
	
	void setTolerance(SHARE tolerance);


	/**
	 * copies all additional values from sourceTable to destinationTable
	 */
	void copyAdditionalValues(SharedTimelineTable<ASSIGNED, SHARE> sourceTable, int srcRowIdx, int srcColIdx,
			SharedTimelineTable<ASSIGNED, SHARE> destinationTable, int dstRowIdx, int dstColIdx);
	
}
