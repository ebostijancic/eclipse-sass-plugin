package at.workflow.webdesk.po.impl.test.sharedTimelineHandler;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;

import at.workflow.webdesk.po.SharedTimelineHandler;
import at.workflow.webdesk.po.SharedTimelineTable;
import at.workflow.webdesk.po.timeline.SharedTimelineTableImpl;
import at.workflow.webdesk.tools.date.EditableDateInterval;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.date.HistorizationHelper;

/**
 * @author sdzuban 07.04.2013
 */
public class WTestTimelineTableProcessing {

    private Date from = DateTools.now();
    private Date between = DateTools.toDate(2500, 6, 15);
    private Date to = DateTools.INFINITY;

    private SharedTimelineHandler<MyGroup, MyGroupCostCenter, MyCostCenter, Float> handler = 
    		new MyGroupCostCenterHandler();

    private MyCostCenter rowObject = new MyCostCenter();
    private MyCostCenter rowObject2 = new MyCostCenter();

    private MyGroup assignee = new MyGroup();

    @Test
    public void testNullAndEmpty() {
        // must check 100 per cent coverage all time

    	SharedTimelineTable<MyCostCenter, Float> table = null; 
        try {
            handler.assign(assignee, table);
            fail("Accepted null table");
        } catch (Exception e) {}

        table = new SharedTimelineTableImpl<MyCostCenter, Float>();
        try {
            handler.assign(assignee, table);
            fail("Accepted empty table");
        } catch (Exception e) { }
    }

    @Test
    public void testCheckOne() {
    	
        assignee.setValidfrom(from);
        assignee.setValidto(to);

    	SharedTimelineTable<MyCostCenter, Float> table;
    	
        //table.setRowObject(rowObject, 0);
    	table = new SharedTimelineTableImpl<MyCostCenter, Float>();
        try {
            handler.assign(assignee, table);
            fail("Accepted table without header");
        } catch (Exception e) { }
        
    	table = new SharedTimelineTableImpl<MyCostCenter, Float>();
        table.addColumn();	// adds to header
        try {
            handler.assign(assignee, table);
            fail("Accepted empty table");
        } catch (Exception e) { }
        
    	table = new SharedTimelineTableImpl<MyCostCenter, Float>();
        table.addRow();
        try {
            handler.assign(assignee, table);
            fail("Accepted empty table");
        } catch (Exception e) { }
        
    	table = new SharedTimelineTableImpl<MyCostCenter, Float>(1, 1);
        table.setContent(100.0f, 0, 0);
        try {
            handler.assign(assignee, table);
            fail("Accepted table without rowobject and without header");
        } catch (Exception e) { }
        
    	table = new SharedTimelineTableImpl<MyCostCenter, Float>();
        table.addRow();
        table.addColumn();	// adds to header
        table.setRowObject(null, 0);
        table.setHeader(new EditableDateInterval(), 0);
        try {
            handler.assign(assignee, table);
            fail("Accepted table with empty rowObject");
        } catch (Exception e) { }
        
    	table = new SharedTimelineTableImpl<MyCostCenter, Float>(1, 1);
        table.setRowObject(rowObject, 0);
        table.setHeader(new EditableDateInterval(), 0);
        try {
            handler.assign(assignee, table);
            fail("Accepted table with empty header");
        } catch (Exception e) { }
        
    	table = new SharedTimelineTableImpl<MyCostCenter, Float>(1, 1);
        table.setHeader(new EditableDateInterval(between), 0);
        try {
            handler.assign(assignee, table);
            fail("Accepted table starting after the validfrom of assignee");
        } catch (Exception e) { }
        
    	table = new SharedTimelineTableImpl<MyCostCenter, Float>(1, 1);
        table.setRowObject(rowObject, 0);
        table.setHeader(new EditableDateInterval(from), 0);
        table.setContent(100f, 0, 0);
        try {
            handler.assign(assignee, table);
        } catch (Exception e) {
            fail("Rejected valid table");
        }
        
    	table = new SharedTimelineTableImpl<MyCostCenter, Float>(1, 1);
        table.setRowObject(rowObject, 0);
        table.setHeader(new EditableDateInterval(from), 0);
        table.setContent(99.9f, 0, 0);
        try {
            handler.assign(assignee, table);
            fail("Accepted table with not enough share");
        } catch (Exception e) { }
        
    	table = new SharedTimelineTableImpl<MyCostCenter, Float>(1, 1);
        table.setContent(100.01f, 0, 0);
        try {
            handler.assign(assignee, table);
            fail("Accepted table with too much share");
        } catch (Exception e) { }
    }


    @Test
    public void testTwoTwo() {

        assignee.setValidfrom(from);
        assignee.setValidto(to);

    	SharedTimelineTable<MyCostCenter, Float> table = new SharedTimelineTableImpl<MyCostCenter, Float>(2, 2);
        table.setHeader(new EditableDateInterval(from), 0);
        table.setHeader(new EditableDateInterval(between, to), 1);
        table.setRowObject(rowObject, 0);
        table.setRowObject(rowObject2, 1);
        table.setContent(60.0f, 0, 0);
        table.setContent(40.0f, 1, 0);
        table.setContent(33.33f, 0, 1);
        table.setContent(66.67f, 1, 1);

        try {
            handler.assign(assignee, table);
        } catch (Exception e) {
            fail("Rejected valid table " + e);
        }

        table.setContent(61.0f, 0, 0);
        try {
            handler.assign(assignee, table);
        	fail("Accepted table with invalid share sum");
        } catch (Exception e) { }
        try {
            handler.assign(assignee, table);
            fail("Accepted table with invalid share sum");
        } catch (Exception e) { }
        table.setContent(38.0f, 1, 0);
        try {
            handler.assign(assignee, table);
            fail("Accepted table with invalid share sum");
        } catch (Exception e) { }
        table.setContent(33.0f, 0, 1);
        try {
            handler.assign(assignee, table);
            fail("Accepted table with invalid share sum");
        } catch (Exception e) { }
        table.setContent(67.67f, 1, 1);
        try {
            handler.assign(assignee, table);
            fail("Accepted table with invalid share sum");
        } catch (Exception e) { }
    }

    
    @Test
    public void testAssignOne() {
    	
        assignee.setValidfrom(from);
        assignee.setValidto(to);
        rowObject.setUID("rowObj1");

    	SharedTimelineTable<MyCostCenter, Float> table = new SharedTimelineTableImpl<MyCostCenter, Float>(1, 1);
        table.setHeader(new EditableDateInterval(from), 0);
        table.setRowObject(rowObject, 0);
        table.setContent(100.0f, 0, 0);
        
        assertEquals(0, assignee.getGroupCostCenters().size());
        handler.assign(assignee, table);
        assertEquals(1, assignee.getGroupCostCenters().size());
        MyGroupCostCenter assignment = assignee.getGroupCostCenters().iterator().next();
        assertEquals(rowObject, assignment.getCostCenter());
        assertEquals(assignee, assignment.getGroup());
        assertEquals(100.0f, assignment.getShare(), 0.001f);
        assertEquals(getFrom(from), assignment.getValidfrom());
        assertEquals(getTo(to), assignment.getValidto());
    }

    @Test
    public void testAssignTwo() {
    	
        assignee.setValidfrom(from);
        assignee.setValidto(to);
        assignee.setUID("test");

    	SharedTimelineTable<MyCostCenter, Float> table = new SharedTimelineTableImpl<MyCostCenter, Float>(2, 2);
        table.setHeader(new EditableDateInterval(from), 0);
        table.setHeader(new EditableDateInterval(between, to), 1);
        table.setRowObject(rowObject, 0);
        table.setRowObject(rowObject2, 1);
        table.setContent(100.0f, 0, 0);
        table.setContent(0.0f, 1, 0);
        table.setContent(0.0f, 0, 1);
        table.setContent(100.0f, 1, 1);

        assertEquals(0, assignee.getGroupCostCenters().size());
        handler.assign(assignee, table);
        assertEquals(2, assignee.getGroupCostCenters().size());

        Map<MyCostCenter, List<MyGroupCostCenter>> assignmentsMap = handler.getSortedTimelines(assignee.getGroupCostCenters());
        
        List<MyGroupCostCenter> assignments = assignmentsMap.get(rowObject);
        assertEquals(1, assignments.size());
        MyGroupCostCenter assignment = assignments.get(0);
        assertEquals(rowObject, assignment.getCostCenter());
        assertEquals(assignee, assignment.getGroup());
        assertEquals(100.0f, assignment.getShare(), 0.001f);
        assertEquals(getFrom(from), assignment.getValidfrom());
        assertEquals(getTo(previousDay(between)), assignment.getValidto());
        
        assignments = assignmentsMap.get(rowObject2);
        assertEquals(1, assignments.size());
        assignment = assignments.get(0);
        assertEquals(rowObject2, assignment.getCostCenter());
        assertEquals(assignee, assignment.getGroup());
        assertEquals(100.0f, assignment.getShare(), 0.001f);
        assertEquals(getFrom(between), assignment.getValidfrom());
        assertEquals(getTo(to), assignment.getValidto());
    }
    
    @Test
    public void testMergeTwoColumns() {
    	
    	assignee.setValidfrom(from);
    	assignee.setValidto(to);
    	assignee.setUID("test");
    	
    	SharedTimelineTable<MyCostCenter, Float> table = new SharedTimelineTableImpl<MyCostCenter, Float>(1, 2);
    	table.setHeader(new EditableDateInterval(from), 0);
    	table.setHeader(new EditableDateInterval(between, to), 1);
    	table.setRowObject(rowObject, 0);
    	table.setContent(100.0f, 0, 0);
    	table.setContent(100.0f, 0, 1);
    	
    	assertEquals(0, assignee.getGroupCostCenters().size());
    	handler.assign(assignee, table);
    	assertEquals(1, assignee.getGroupCostCenters().size());
    	
    	Map<MyCostCenter, List<MyGroupCostCenter>> assignmentsMap = handler.getSortedTimelines(assignee.getGroupCostCenters());
    	
    	List<MyGroupCostCenter> assignments = assignmentsMap.get(rowObject);
    	assertEquals(1, assignments.size());
    	MyGroupCostCenter assignment = assignments.get(0);
    	assertEquals(rowObject, assignment.getCostCenter());
    	assertEquals(assignee, assignment.getGroup());
    	assertEquals(100.0f, assignment.getShare(), 0.001f);
    	assertEquals(getFrom(from), assignment.getValidfrom());
    	assertEquals(getTo(to), assignment.getValidto());
    	
    }
    
    @Test
    public void testAssignFour() {
    	
    	assignee.setValidfrom(from);
    	assignee.setValidto(to);
    	rowObject.setUID("uid1");
    	rowObject2.setUID("uid2");
    	
    	SharedTimelineTable<MyCostCenter, Float> table = new SharedTimelineTableImpl<MyCostCenter, Float>(2, 2);
    	table.setHeader(new EditableDateInterval(from), 0);
    	table.setHeader(new EditableDateInterval(between, to), 1);
    	table.setRowObject(rowObject, 0);
    	table.setRowObject(rowObject2, 1);
    	table.setContent(60.0f, 0, 0);
    	table.setContent(40.0f, 1, 0);
    	table.setContent(33.33f, 0, 1);
    	table.setContent(66.67f, 1, 1);
    	
    	assertEquals(0, assignee.getGroupCostCenters().size());
    	handler.assign(assignee, table);
    	assertEquals(4, assignee.getGroupCostCenters().size());
    	
        Map<MyCostCenter, List<MyGroupCostCenter>> assignmentsMap = handler.getSortedTimelines(assignee.getGroupCostCenters());
    	
        List<MyGroupCostCenter> assignments = assignmentsMap.get(rowObject);
        assertEquals(2, assignments.size());
        MyGroupCostCenter assignment = assignments.get(0);
        assertEquals(rowObject, assignment.getCostCenter());
        assertEquals(assignee, assignment.getGroup());
        assertEquals(60.0f, assignment.getShare(), 0.001f);
        assertEquals(getFrom(from), assignment.getValidfrom());
        assertEquals(getTo(previousDay(between)), assignment.getValidto());
        assignment = assignments.get(1);
        assertEquals(rowObject, assignment.getCostCenter());
        assertEquals(assignee, assignment.getGroup());
        assertEquals(33.33f, assignment.getShare(), 0.001f);
        assertEquals(getFrom(between), assignment.getValidfrom());
        assertEquals(getTo(to), assignment.getValidto());
        
        assignments = assignmentsMap.get(rowObject2);
        assertEquals(2, assignments.size());
        assignment = assignments.get(0);
        assertEquals(rowObject2, assignment.getCostCenter());
        assertEquals(assignee, assignment.getGroup());
        assertEquals(40.0f, assignment.getShare(), 0.001f);
        assertEquals(getFrom(from), assignment.getValidfrom());
        assertEquals(getTo(previousDay(between)), assignment.getValidto());
        assignment = assignments.get(1);
        assertEquals(rowObject2, assignment.getCostCenter());
        assertEquals(assignee, assignment.getGroup());
        assertEquals(66.67f, assignment.getShare(), 0.001f);
        assertEquals(getFrom(between), assignment.getValidfrom());
        assertEquals(getTo(to), assignment.getValidto());
        
    }
    
    @Test
    public void testAssignTwoTimesCheckDeletes() {
    	
    	assignee.setValidfrom(from);
    	assignee.setValidto(to);
    	rowObject.setUID("uid1");
    	rowObject2.setUID("uid2");
    	
    	SharedTimelineTable<MyCostCenter, Float> table = new SharedTimelineTableImpl<MyCostCenter, Float>(2, 2);
    	table.setHeader(new EditableDateInterval(from), 0);
    	table.setHeader(new EditableDateInterval(between, to), 1);
    	table.setRowObject(rowObject, 0);
    	table.setRowObject(rowObject2, 1);
    	table.setContent(100.0f, 0, 0);
    	table.setContent(0.0f, 1, 0);
    	table.setContent(30.0f, 0, 1);
    	table.setContent(70.0f, 1, 1);
    	
    	assertEquals(0, assignee.getGroupCostCenters().size());
    	handler.assign(assignee, table);
    	assertEquals(3, assignee.getGroupCostCenters().size());
    	
    	table.setContent(100.0f, 0, 1);
    	table.setContent(0.0f, 1, 1);
    	handler.assign(assignee, table);
    	// one merged, two deleted
    	assertEquals(1, assignee.getGroupCostCenters().size());
    	
    	Map<MyCostCenter, List<MyGroupCostCenter>> assignmentsMap = handler.getSortedTimelines(assignee.getGroupCostCenters());
    	
    	List<MyGroupCostCenter> assignments = assignmentsMap.get(rowObject);
    	assertEquals(1, assignments.size());
    	MyGroupCostCenter assignment = assignments.get(0);
    	assertEquals(rowObject, assignment.getCostCenter());
    	assertEquals(assignee, assignment.getGroup());
    	assertEquals(100.0f, assignment.getShare(), 0.001f);
    	assertEquals(getFrom(from), assignment.getValidfrom());
    	assertEquals(getTo(to), assignment.getValidto());
    	
    }
    
    private Date getFrom(Date date) {
    	return HistorizationHelper.generateUsefulValidFromDay(date);
    }
    
    private Date getTo(Date date) {
    	return HistorizationHelper.generateUsefulValidToDay(date);
    }
    

    private Date nextDay(Date day) {
   	 return DateUtils.addDays(day, 1);
    }
    
    private Date previousDay(Date day) {
   	 return DateUtils.addDays(day, -1);
    }
}
