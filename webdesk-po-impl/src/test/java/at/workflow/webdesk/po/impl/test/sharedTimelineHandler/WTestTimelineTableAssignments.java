package at.workflow.webdesk.po.impl.test.sharedTimelineHandler;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;

import at.workflow.webdesk.po.SharedTimelineHandler;
import at.workflow.webdesk.po.SharedTimelineTable;
import at.workflow.webdesk.po.timeline.SharedTimelineTableImpl;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.date.EditableDateInterval;
import at.workflow.webdesk.tools.date.HistorizationHelper;

public class WTestTimelineTableAssignments {

     private final Date date2000 = DateTools.toDate(2000, 6, 15, 13, 33);
     private final Date date2010 = DateTools.toDate(2010, 6, 15, 13, 33);
     private final Date date2012 = DateTools.toDate(2012, 6, 15, 13, 33);
     private final Date date2100 = DateTools.toDate(2100, 6, 15, 13, 33);
     private final Date date2200 = DateTools.toDate(2200, 6, 15, 13, 33);
     private final Date date2300 = DateTools.toDate(2300, 6, 15, 13, 33);
     private final Date date2400 = DateTools.toDate(2400, 6, 15, 13, 33);
     private final Date date2500 = DateTools.toDate(2500, 6, 15, 13, 33);

     private MyGroup group;
     private MyCostCenter cc1, cc2, cc3, cc4;
     private SharedTimelineTable<MyCostCenter, Float> assignments;

     private SharedTimelineHandler<MyGroup, MyGroupCostCenter, MyCostCenter, Float> handler = 
     		new MyGroupCostCenterHandler();


     @Test
     public void testTwoSameSinglesEnclosing() {
    	 //exist.      |--- 100 % ----|
    	 // new    |-------100% ----------| 

         setGroupWithCostCenter(date2010, date2400);

         setOneAssignment(cc1, date2000, date2500);
         try {
        	 handler.assign(group, assignments);
        	 fail("Accepted longer assignment than assignee");
         } catch (Exception e) { }
     }

     @Test
     public void testTwoSameSinglesSameTime() {
    	 //exist.      |--- 100 % ----|
    	 // new        |--- 100 % ----|

         setGroupWithCostCenter(date2010, date2400);

         setOneAssignment(cc1, date2010, date2400);
         handler.assign(group, assignments);
         Map<MyCostCenter, List<MyGroupCostCenter>> result = handler.getSortedTimelines(group.getGroupCostCenters());
         // assert single assignment with cc1
         assertEquals(1, result.size());
         assertNotNull(result.get(cc1));
         assertEquals(1, result.get(cc1).size());
         assertAssignment(result.get(cc1).get(0), cc1, date2010, date2400, 100.0f);
     }

     @Test
     public void testTwoSameSinglesInsideSincePast() {
    	 //exist.      |--- 100 % ----|
    	 //new           |- 100 % --|

         setGroupWithCostCenter(date2010, date2400);

         setOneAssignment(cc1, date2012, date2300);
         handler.assign(group, assignments);
         Map<MyCostCenter, List<MyGroupCostCenter>> result = handler.getSortedTimelines(group.getGroupCostCenters());
         // assert single assignment with cc1
         assertEquals(1, result.size());
         assertNotNull(result.get(cc1));
         assertEquals(1, result.get(cc1).size());
         assertAssignment(result.get(cc1).get(0), cc1, date2010, date2400, 100.0f);
     }

     @Test
     public void testTwoSameSinglesInsideInFuture() {
    	 //exist.      |--- 100 % ----|
    	 //new           |- 100 % ----|

         setGroupWithCostCenter(date2010, date2400);

         setOneAssignment(cc1, date2100, date2400);
         handler.assign(group, assignments);
         Map<MyCostCenter, List<MyGroupCostCenter>> result = handler.getSortedTimelines(group.getGroupCostCenters());
         // assert single assignment with cc1
         assertEquals(1, result.size());
         assertNotNull(result.get(cc1));
         assertEquals(1, result.get(cc1).size());
         assertAssignment(result.get(cc1).get(0), cc1, date2010, date2400, 100.0f);
     }

     @Test
     public void testTwoPlusSameOneSameTime() {
    	 //exist.   |-- 100 % -|-- 100% --|
    	 //new      |-------- 100 % ------|

         setGroupWith2CostCenters(date2010, null, date2200, date2400);

         setOneAssignment(cc1, date2010, date2400);
         handler.assign(group, assignments);
         Map<MyCostCenter, List<MyGroupCostCenter>> result = handler.getSortedTimelines(group.getGroupCostCenters());
         // assert single assignment with cc1
         assertEquals(1, result.size());
         assertNotNull(result.get(cc1));
         assertEquals(1, result.get(cc1).size());
         assertAssignment(result.get(cc1).get(0), cc1, date2010, date2400, 100.0f);
     }

     @Test
     public void testTwoPlusSameOneInsideFuture() {
    	 //exist.   |-- 100 % -|-- 100% --|
    	 //new         |----- 100 % ---|

         setGroupWith2CostCenters(date2010, previousDay(date2200), date2200, date2400);

         setOneAssignment(cc1, date2100, date2300);
         Collection<MyGroupCostCenter> ccs = group.getGroupCostCenters();
         handler.assign(group, assignments);
         ccs = group.getGroupCostCenters();
         Map<MyCostCenter, List<MyGroupCostCenter>> result = handler.getSortedTimelines(ccs);
         // assert single assignment with cc1
         assertEquals(2, result.size());
         assertNotNull(result.get(cc1));
         assertEquals(1, result.get(cc1).size());
         assertAssignment(result.get(cc1).get(0), cc1, date2010, date2300, 100.0f);
         assertNotNull(result.get(cc2));
         assertEquals(1, result.get(cc2).size());
         assertAssignment(result.get(cc2).get(0), cc2, nextDay(date2300), date2400, 100.0f);
     }

     @Test
     public void testTwoParalellPlusSameOneSameTime() {
    	 //exist.   |--------  60 % ------|
    	 //exist.   |--------  40 % ------|
    	 //new      |-------- 100 % ------|

         setGroupWith2CostCenters(date2010, date2400, 60.0f, 40.0f);

         setOneAssignment(cc1, date2010, date2400);
         handler.assign(group, assignments);
         Map<MyCostCenter, List<MyGroupCostCenter>> result = handler.getSortedTimelines(group.getGroupCostCenters());
         // assert single assignment with cc1
         assertEquals(1, result.size());
         assertNotNull(result.get(cc1));
         assertEquals(1, result.get(cc1).size());
         assertAssignment(result.get(cc1).get(0), cc1, date2010, date2400, 100.0f);
     }

     @Test
     public void testTwoParalellPlustSameOneInsideFuture() {
    	 //exist.   |--------  60 % ------|
    	 //exist.   |--------  40 % ------|
    	 //new      |-------- 100 % ------|

         setGroupWith2CostCenters(date2010, date2400, 60.0f, 40.0f);

         setOneAssignment(cc1, date2100, date2300);
         handler.assign(group, assignments);
         Map<MyCostCenter, List<MyGroupCostCenter>> result = handler.getSortedTimelines(group.getGroupCostCenters());
         // assert 5 assignment with cc1+cc2, cc1, cc1+cc2
         assertEquals(2, result.size());
         assertNotNull(result.get(cc1));
         assertEquals(3, result.get(cc1).size());
         assertAssignment(result.get(cc1).get(0), cc1, date2010, previousDay(date2100), 60.0f);
         assertAssignment(result.get(cc1).get(1), cc1, date2100, date2300, 100.0f);
         assertAssignment(result.get(cc1).get(2), cc1, nextDay(date2300), date2400, 60.0f);
     }

//    ============================ END OF SAME ONE COST CENTER ================================

     @Test
     public void testTwoSinglesEnclosing() {
    	 //exist.     |------ 100 % ----|
    	 //new      |-------- 100 % ------|

         setGroupWithCostCenter(date2010, date2400);

         cc2 = new MyCostCenter();
         cc2.setUID("test2");
         setOneAssignment(cc2, date2000, date2500);
         try {
        	 handler.assign(group, assignments);
        	 fail("Accepted longer assignment than assignees validity");
         } catch (Exception e) { }
     }

     @Test
     public void testTwoSinglesSameTime() {
    	 //exist.   |-------- 100 % ------|
    	 //new      |-------- 100 % ------|

         setGroupWithCostCenter(date2010, date2400);

         cc2 = new MyCostCenter();
         cc2.setUID("test2");
         setOneAssignment(cc2, date2010, date2400);
         handler.assign(group, assignments);
         Map<MyCostCenter, List<MyGroupCostCenter>> result = handler.getSortedTimelines(group.getGroupCostCenters());
         // assert single assignment with cc2
         assertEquals(1, result.size());
         assertNotNull(result.get(cc2));
         assertEquals(1, result.get(cc2).size());
         assertAssignment(result.get(cc2).get(0), cc2, date2010, date2400, 100.0f);
     }

     @Test
     public void testTwoSinglesInsideSincePast() {
    	 //exist.   |-------- 100 % ------|
    	 //new        |------ 100 % ----|

         setGroupWithCostCenter(date2010, date2400);

         cc2 = new MyCostCenter();
         cc2.setUID("test2");
         setOneAssignment(cc2, date2012, date2300);
         handler.assign(group, assignments);
         Map<MyCostCenter, List<MyGroupCostCenter>> result = handler.getSortedTimelines(group.getGroupCostCenters());
         // assert three assignment with cc1, cc2, cc1
         assertEquals(2, result.size());
         assertNotNull(result.get(cc1));
         assertEquals(2, result.get(cc1).size());
         assertAssignment(result.get(cc1).get(0), cc1, date2010, previousDay(date2012), 100.0f);
         assertAssignment(result.get(cc1).get(1), cc1, nextDay(date2300), date2400, 100.0f);
         assertNotNull(result.get(cc2));
         assertEquals(1, result.get(cc2).size());
         assertAssignment(result.get(cc2).get(0), cc2, date2012, date2300, 100.0f);
     }

     @Test
     public void testTwoSinglesInsideInFuture() {
    	 //exist.   |-------- 100 % ------|
    	 //new        |------ 100 % ----|

         setGroupWithCostCenter(date2010, date2400);

         cc2 = new MyCostCenter();
         cc2.setUID("test2");
         setOneAssignment(cc2, date2100, date2300);
         handler.assign(group, assignments);
         Map<MyCostCenter, List<MyGroupCostCenter>> result = handler.getSortedTimelines(group.getGroupCostCenters());
         // assert three assignment with cc1, cc2, cc1
         assertEquals(2, result.size());
         assertNotNull(result.get(cc1));
         assertEquals(2, result.get(cc1).size());
         assertAssignment(result.get(cc1).get(0), cc1, date2010, previousDay(date2100), 100.0f);
         assertAssignment(result.get(cc1).get(1), cc1, nextDay(date2300), date2400, 100.0f);
         assertNotNull(result.get(cc2));
         assertEquals(1, result.get(cc2).size());
         assertAssignment(result.get(cc2).get(0), cc2, date2100, date2300, 100.0f);
     }

     @Test
     public void testTwoPlusOneSameTime() {
    	 //exist.   |-- 100 % -|-- 100% --|
    	 //new      |-------- 100 % ------|

         setGroupWith2CostCenters(date2010, null, date2200, date2400);

         cc3 = new MyCostCenter();
         cc3.setUID("test3");
         setOneAssignment(cc3, date2010, date2400);
         handler.assign(group, assignments);
         Map<MyCostCenter, List<MyGroupCostCenter>> result = handler.getSortedTimelines(group.getGroupCostCenters());
         // assert single assignment with cc3
         assertEquals(1, result.size());
         assertNotNull(result.get(cc3));
         assertEquals(1, result.get(cc3).size());
         assertAssignment(result.get(cc3).get(0), cc3, date2010, date2400, 100.0f);
     }

     @Test
     public void testTwoPlusOneInsideFuture() {
    	 //exist.   |-- 100 % -|-- 100% --|
    	 //new         |----- 100 % ---|

         setGroupWith2CostCenters(date2010, previousDay(date2200), date2200, date2400);

         cc3 = new MyCostCenter();
         cc3.setUID("test3");
         setOneAssignment(cc3, date2100, date2300);
         handler.assign(group, assignments);
         Map<MyCostCenter, List<MyGroupCostCenter>> result = handler.getSortedTimelines(group.getGroupCostCenters());
         // assert three assignment with cc1, cc3, cc2
         assertEquals(3, result.size());
         assertNotNull(result.get(cc1));
         assertEquals(1, result.get(cc1).size());
         assertAssignment(result.get(cc1).get(0), cc1, date2010, previousDay(date2100), 100.0f);
         assertNotNull(result.get(cc3));
         assertEquals(1, result.get(cc3).size());
         assertAssignment(result.get(cc3).get(0), cc3, date2100, date2300, 100.0f);
         assertNotNull(result.get(cc2));
         assertEquals(1, result.get(cc2).size());
         assertAssignment(result.get(cc2).get(0), cc2, nextDay(date2300), date2400, 100.0f);
     }


     @Test
     public void testThreePlusOneFirstToThird() {
    	 //exist.   |- 100 % -|- 100% -|- 100% --|
    	 //new         |-------- 100 % ------|

         setGroupWith3CostCenters(date2010, null, date2200, null, date2300, date2500);

         cc4 = new MyCostCenter();
         cc4.setUID("test4");
         setOneAssignment(cc4, date2100, date2400);
         handler.assign(group, assignments);
         Map<MyCostCenter, List<MyGroupCostCenter>> result = handler.getSortedTimelines(group.getGroupCostCenters());
         // assert 3 assignment with cc1, cc4, cc3
         assertEquals(3, result.size());
         assertNotNull(result.get(cc1));
         assertEquals(1, result.get(cc1).size());
         assertAssignment(result.get(cc1).get(0), cc1, date2010, previousDay(date2100), 100.0f);
         assertNotNull(result.get(cc4));
         assertEquals(1, result.get(cc4).size());
         assertAssignment(result.get(cc4).get(0), cc4, date2100, date2400, 100.0f);
         assertNotNull(result.get(cc3));
         assertEquals(1, result.get(cc3).size());
         assertAssignment(result.get(cc3).get(0), cc3, nextDay(date2400), date2500, 100.0f);
     }


     @Test
     public void testThreePlusOneSecond() {
    	 //exist.   |- 100 % -|- 100% -|- 100% --|
    	 //new                |- 100% -|

         setGroupWith3CostCenters(date2010, previousDay(date2200), date2200, previousDay(date2300), date2300, date2500);

         cc4 = new MyCostCenter();
 		 cc4.setUID("test4"); // as if persisted
         setOneAssignment(cc4, date2200, previousDay(date2300));
         handler.assign(group, assignments);
         Map<MyCostCenter, List<MyGroupCostCenter>> result = handler.getSortedTimelines(group.getGroupCostCenters());
         // assert 3 assignment with cc1, cc4, cc3
         assertEquals(3, result.size());
         assertNotNull(result.get(cc1));
         assertEquals(1, result.get(cc1).size());
         assertAssignment(result.get(cc1).get(0), cc1, date2010, previousDay(date2200), 100.0f);
         assertNotNull(result.get(cc4));
         assertEquals(1, result.get(cc4).size());
         assertAssignment(result.get(cc4).get(0), cc4, date2200, previousDay(date2300), 100.0f);
         assertNotNull(result.get(cc3));
         assertEquals(1, result.get(cc3).size());
         assertAssignment(result.get(cc3).get(0), cc3, date2300, date2500, 100.0f);
     }


     @Test
     public void testThreePlusOneInSecond() {
    	 //exist.   |- 100 % -|- 100% -|- 100% --|
    	 //new                 | 100% |

         setGroupWith3CostCenters(date2010, previousDay(date2100), date2100, previousDay(date2400), date2400, date2500);

         cc4 = new MyCostCenter();
 		 cc4.setUID("test4"); // as if persisted
         setOneAssignment(cc4, date2200, date2300);
         handler.assign(group, assignments);
         Map<MyCostCenter, List<MyGroupCostCenter>> result = handler.getSortedTimelines(group.getGroupCostCenters());
         // assert 3 assignment with cc1, c2, cc4, c2, cc3
         assertEquals(4, result.size());
         assertNotNull(result.get(cc1));
         assertEquals(1, result.get(cc1).size());
         assertAssignment(result.get(cc1).get(0), cc1, date2010, previousDay(date2100), 100.0f);
         assertNotNull(result.get(cc2));
         assertEquals(2, result.get(cc2).size());
         assertAssignment(result.get(cc2).get(0), cc2, date2100, previousDay(date2200), 100.0f);
         assertAssignment(result.get(cc2).get(1), cc2, nextDay(date2300), previousDay(date2400), 100.0f);
         assertNotNull(result.get(cc4));
         assertEquals(1, result.get(cc4).size());
         assertAssignment(result.get(cc4).get(0), cc4, date2200, date2300, 100.0f);
         assertNotNull(result.get(cc3));
         assertEquals(1, result.get(cc3).size());
         assertAssignment(result.get(cc3).get(0), cc3, date2400, date2500, 100.0f);
     }

     @Test
     public void testTwoParalellPlusOneSameTime() {
    	 //exist.   |--------  60 % ------|
    	 //exist.   |--------  40 % ------|
    	 //new      |-------- 100 % ------|

         setGroupWith2CostCenters(date2010, date2400, 60.0f, 40.0f);

         cc3 = new MyCostCenter();
 		 cc3.setUID("test3"); // as if persisted
         setOneAssignment(cc3, date2010, date2400);
         handler.assign(group, assignments);
         Map<MyCostCenter, List<MyGroupCostCenter>> result = handler.getSortedTimelines(group.getGroupCostCenters());
         // assert single assignment with cc3
         assertEquals(1, result.size());
         assertNotNull(result.get(cc3));
         assertEquals(1, result.get(cc3).size());
         assertAssignment(result.get(cc3).get(0), cc3, date2010, date2400, 100.0f);
     }

     @Test
     public void testTwoParalellPlustOneInsideFuture() {
    	 //exist.   |--------  60 % ------|
    	 //exist.   |--------  40 % ------|
    	 //new         |----- 100 % ---|

         setGroupWith2CostCenters(date2010, date2400, 60.0f, 40.0f);

         cc3 = new MyCostCenter();
 		 cc3.setUID("test3"); // as if persisted
         setOneAssignment(cc3, date2100, date2300);
         handler.assign(group, assignments);
         Map<MyCostCenter, List<MyGroupCostCenter>> result = handler.getSortedTimelines(group.getGroupCostCenters());
         // assert 5 assignment with cc1+cc2, cc3, cc1+cc2
         assertEquals(3, result.size());
         assertNotNull(result.get(cc1));
         assertEquals(2, result.get(cc1).size());
         assertAssignment(result.get(cc1).get(0), cc1, date2010, previousDay(date2100), 60.0f);
         assertAssignment(result.get(cc1).get(1), cc1, nextDay(date2300), date2400, 60.0f);
         assertNotNull(result.get(cc2));
         assertEquals(2, result.get(cc2).size());
         assertAssignment(result.get(cc2).get(0), cc2, date2010, previousDay(date2100), 40.0f);
         assertAssignment(result.get(cc2).get(1), cc2, nextDay(date2300), date2400, 40.0f);
         assertNotNull(result.get(cc3));
         assertEquals(1, result.get(cc3).size());
         assertAssignment(result.get(cc3).get(0), cc3, date2100, date2300, 100.0f);
     }

     @Test
     public void testThreePlustOneInsideFutureLeft() {
    	 //exist.   |-- 60 % --|-- 100% --|
    	 //exist.   |-- 40 % --|
    	 //new         |----- 100 % --|

         setGroupWith3CostCenters(date2010, previousDay(date2200), date2200, date2400, true);

         cc3 = new MyCostCenter();
 		 cc3.setUID("test3"); // as if persisted
         setOneAssignment(cc3, date2100, date2300);
         handler.assign(group, assignments);
         Map<MyCostCenter, List<MyGroupCostCenter>> result = handler.getSortedTimelines(group.getGroupCostCenters());
         // assert 4 assignment with cc1+cc2, cc3, cc1
         assertEquals(3, result.size());
         assertNotNull(result.get(cc1));
         assertEquals(2, result.get(cc1).size());
         assertAssignment(result.get(cc1).get(0), cc1, date2010, previousDay(date2100), 60.0f);
         assertAssignment(result.get(cc1).get(1), cc1, nextDay(date2300), date2400, 100.0f);
         assertNotNull(result.get(cc2));
         assertEquals(1, result.get(cc2).size());
         assertAssignment(result.get(cc2).get(0), cc2, date2010, previousDay(date2100), 40.0f);
         assertNotNull(result.get(cc3));
         assertEquals(1, result.get(cc3).size());
         assertAssignment(result.get(cc3).get(0), cc3, date2100, date2300, 100.0f);
     }

     @Test
     public void testThreePlustOneInsideFutureRight() {
    	 //exist.   |-- 100% --|-- 60 % --|
    	 //exist.              |-- 40 % --|
    	 //new         |----- 100 % --|

         setGroupWith3CostCenters(date2010, previousDay(date2200), date2200, date2400, false);

         cc3 = new MyCostCenter();
 		 cc3.setUID("test3"); // as if persisted
         setOneAssignment(cc3, date2100, date2300);
         handler.assign(group, assignments);
         Map<MyCostCenter, List<MyGroupCostCenter>> result = handler.getSortedTimelines(group.getGroupCostCenters());
         // assert 4 assignment with cc1, cc3, cc1+cc2
         assertEquals(3, result.size());
         assertNotNull(result.get(cc1));
         assertEquals(2, result.get(cc1).size());
         assertAssignment(result.get(cc1).get(0), cc1, date2010, previousDay(date2100), 100.0f);
         assertAssignment(result.get(cc1).get(1), cc1, nextDay(date2300), date2400, 60.0f);
         assertNotNull(result.get(cc2));
         assertEquals(1, result.get(cc2).size());
         assertAssignment(result.get(cc2).get(0), cc2, nextDay(date2300), date2400, 40.0f);
         assertNotNull(result.get(cc3));
         assertEquals(1, result.get(cc3).size());
         assertAssignment(result.get(cc3).get(0), cc3, date2100, date2300, 100.0f);
     }


//    -------------------------- PRIVATE METHODS -------------------------------

     private void setOneAssignment(MyCostCenter cc, Date from, Date to) {
         setOneAssignment(cc, 100.0f, from, to);
     }

     private void setOneAssignment(MyCostCenter cc, float share, Date from, Date to) {

         assignments = new SharedTimelineTableImpl<MyCostCenter, Float>(1, 1);
         assignments.setHeader(new EditableDateInterval(from, getTo(to)), 0);
         assignments.setRowObject(cc, 0);
         assignments.setContent(share, 0, 0);
     }

     private void setGroupWithCostCenter(Date from, Date to) {

         group = new MyGroup();
         group.setValidfrom(from);
         group.setValidto(getTo(to));

         // fullAssign cc1
         cc1 = new MyCostCenter();
 		 cc1.setUID("test1"); // as if persisted
         handler.assign(group, cc1, group.getValidfrom(), group.getValidto());
     }

     private void setGroupWith2CostCenters(Date from1, Date to1, Date from2, Date to2) {

         group = new MyGroup();
         group.setValidfrom(from1);
         group.setValidto(getTo(to2));

         cc1 = new MyCostCenter();
 		 cc1.setUID("test1"); // as if persisted
         cc2 = new MyCostCenter();
 		 cc2.setUID("test2"); // as if persisted
         
         setOneAssignment(cc1, 100.0f, from1, to1);
         assignments.addColumn();
         assignments.addRow();
         assignments.setHeader(new EditableDateInterval(from2, getTo(to2)), 1);
         assignments.setRowObject(cc2, 1);
         assignments.setContent(100.0f, 1, 1);

         handler.assign(group, assignments);
     }

     private void setGroupWith2CostCenters(Date from, Date to, float share1, float share2) {

         group = new MyGroup();
         group.setValidfrom(from);
         group.setValidto(getTo(to));

         cc1 = new MyCostCenter();
 		 cc1.setUID("test1"); // as if persisted
         cc2 = new MyCostCenter();
 		 cc2.setUID("test2"); // as if persisted
         
         setOneAssignment(cc1, share1, from, to);
         assignments.addRow();
         assignments.setRowObject(cc2, 1);
         assignments.setContent(share2, 1, 0);

         handler.assign(group, assignments);
     }


     private void setGroupWith3CostCenters(Date from1, Date to1, Date from2, Date to2, Date from3, Date to3) {

         group = new MyGroup();
         group.setValidfrom(from1);
         group.setValidto(getTo(to3));

         cc1 = new MyCostCenter();
 		 cc1.setUID("test1"); // as if persisted
         cc2 = new MyCostCenter();
 		 cc2.setUID("test2"); // as if persisted
 		 cc3 = new MyCostCenter();
 		 cc3.setUID("test3"); // as if persisted
         
         setOneAssignment(cc1, 100.0f, from1, getTo(to1));
         assignments.addColumn();
         assignments.addRow();
         assignments.setHeader(new EditableDateInterval(from2, getTo(to2)), 1);
         assignments.setRowObject(cc2, 1);
         assignments.setContent(100.0f, 1, 1);
         assignments.addColumn();
         assignments.addRow();
         assignments.setHeader(new EditableDateInterval(from3, getTo(to3)), 2);
         assignments.setRowObject(cc3, 2);
         assignments.setContent(100.0f, 2, 2);
         
         handler.assign(group, assignments);

     }

     private void setGroupWith3CostCenters(Date from1, Date to1, Date from2, Date to2, boolean left) {

         group = new MyGroup();
         group.setValidfrom(from1);
         group.setValidto(getTo(to2));

         cc1 = new MyCostCenter();
 		 cc1.setUID("test1"); // as if persisted
         cc2 = new MyCostCenter();
 		 cc2.setUID("test2"); // as if persisted
         
         setOneAssignment(cc1, 100.0f, from1, getTo(to1));
         assignments.addColumn();
         assignments.addRow();
         assignments.setHeader(new EditableDateInterval(from2, getTo(to2)), 1);
         assignments.setRowObject(cc2, 1);
         if (left) {
        	 assignments.setContent(60.0f, 0, 0);
        	 assignments.setContent(40.0f, 1, 0);
        	 assignments.setContent(100.0f, 0, 1);
        	 assignments.setContent(0.0f, 1, 1);
         } else {
        	 assignments.setContent(100.0f, 0, 0);
        	 assignments.setContent(0.0f, 1, 0);
        	 assignments.setContent(60.0f, 0, 1);
        	 assignments.setContent(40.0f, 1, 1);
         }
         
         handler.assign(group, assignments);
     }

     private void assertAssignment(MyGroupCostCenter assignment, MyCostCenter assigned, Date from, Date to, Float share) {
         assertEquals(assigned, assignment.getCostCenter());
       	 assertEquals(getFrom(from), assignment.getValidfrom());
       	 assertEquals(getTo(to), assignment.getValidto());
         assertEquals(share, assignment.getShare().floatValue(), 0.001f);
     }

     private Date getFrom(Date date) {
    	 return HistorizationHelper.generateUsefulValidFromDay(date);
     }
     
     private Date getTo(Date date) {
    	 if (date == null)
    		 return null;
   		 return HistorizationHelper.generateUsefulValidToDay(date);
     }

     private Date nextDay(Date day) {
    	 return DateUtils.addDays(day, 1);
     }
     
     private Date previousDay(Date day) {
    	 return DateUtils.addDays(day, -1);
     }
}

