package at.workflow.webdesk.po;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.workflow.webdesk.po.timeline.TimelineFragment;
import at.workflow.webdesk.tools.api.BusinessMessages;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.date.Interval;

/**
 * This interface defines methods for checking and maintaining 
 * day exact timeline with prescribed constant sum of shares.
 * 
 * The task is to maintain e.g. relationship to cost centers in such a way 
 * that at any time there is at least one cost center assigned 
 * to the assignee for its whole validity time and that the sum 
 * of the shares of all the assigned cost centers equals 100 percent at any moment
 * for the whole assignee's validity time.
 * 
 * 
 * @author sdzuban 02.04.2013
 */
public interface SharedTimelineHandler<ASSIGNEE extends Historization, ASSIGNMENT extends HistorizationWithShare, 
		ASSIGNED extends PersistentObject, SHARE extends Number> {


	/**
	 * Assignes assigned entity to the assignee for 100% in the from / to interval.
	 * @param assignee entity that must have e.g. 100 percent coverage of costs all the time
	 * @param assigned entity providing 100 per cent coverage 
	 * @param from 
	 * @param to 
	 * @throws runtime exception if the assignment cannot be carried through
	 */
	void assign(ASSIGNEE assignee, ASSIGNED assigned, Date from, Date to);
	
	
	/**
	 * This method enables complete one-step definition of the whole shared time line
	 * for the whole assignee validity (first from == null, last to  == null),
	 * as well as for the time interval defined by first from and last to in the header
	 * of the table. 
	 * 
	 * E.g. code for entering following table
	 * <table>
	 * <header><tr><th>CostCenter</th><th>1.1.2014</th><th>1.7.2014</th></tr></header>
	 * <body>
	 * <tr><td>CostCenter1</td><td>60.0%</td><td>33.33%</td></tr>
	 * <tr><td>CostCenter2</td><td>40.0%</td><td>66.67%</td></tr>
	 * </body>
	 * </table>
	 * 
	 * looks like this:
	 * <pre>
        SharedTimelineTable<MyCostCenter, Float> table = new SharedTimelineTableImpl<MyCostCenter, Float>(2, 2);
        table.setHeader(new EditableDateInterval(DateTools.toDate(2014, 1, 1)), 0);
        table.setHeader(new EditableDateInterval(DateTools.toDate(2014, 7, 1)), 1);
        table.setRowObject(costCenter1, 0);
        table.setRowObject(costCenter2, 1);
        table.setContent(60.0f, 0, 0);
        table.setContent(40.0f, 1, 0);
        table.setContent(33.33f, 0, 1);
        table.setContent(66.67f, 1, 1);
	 * </pre>  
	 * 
	 * For the definition of TimelineTable see {@link at.workflow.webdesk.po.timeline.SharedTimelineTableImpl}
	 * 
	 * Zeros need not be entered, just non-zero values shall be.
	 * 
	 * @param assignee entity that must have e.g. 100 percent coverage of costs all the time
	 * @param timelineTable Table representing distribution of shares to assigned entities in relation to time 
	 * for the whole assignees validity 
	 * @throws runtime exception if the assignment cannot be carried through
	 */
	void assign(ASSIGNEE assignee, SharedTimelineTable<ASSIGNED, SHARE> timelineTable);
	
	
	/**
	 * Checks that the timeline as represented by the HistorizationWithShare objects is
	 *  - complete, i.e. it covers all the time of validity of the assignee
	 *  - consistent, i.e. the sum of all the assignment's shares is prescribed amount all the time
	 * 
	 * @param timeline timeline with share
	 * @param assignee entity that must have e.g. 100 percent coverage of costs all the time
	 * @throws I18nRuntimeException if 
	 * <li><ul>the timeline is not complete</ul>
	 * <ul>there is more than one assignment of the same assigned entity at some time</ul>
	 * <ul>the sum of shares is not as required all the time</ul></li>
	 * IllegalArgumentException if timeline or assignee is null
	 * 
	 */
	void checkTimeline(Collection<ASSIGNMENT> timeline, ASSIGNEE assignee);

	
	/**
	 * Checks that the timeline as represented by the HistorizationWithShare objects is
	 *  - does not exceed 100 percent before timelineBegin
	 *  - complete, i.e. it covers all the time of validity of the assignee after timelineBegin
	 *  - consistent, i.e. the sum of all the assignment's shares is prescribed amount all the time after timelineBegin
	 * 
	 * @param timeline timeline with share
	 * @param assignee entity that must have e.g. 100 percent coverage of costs all the time
	 * @param timelineBegin beginning of the timeline, wenn null DateTools.today() will be taken
	 * @throws I18nRuntimeException if 
	 * <li><ul>the sum of shares exceeds 100 percent before the timelineBegin</ul>
	 * <ul>the timeline is not complete after the timelineBegin</ul>
	 * <ul>there is more than one assignment of the same assigned entity at some time</ul>
	 * <ul>the sum of shares is not as required all the time after timelineBegin</ul></li>
	 * IllegalArgumentException if timeline or assignee is null
	 * 
	 */
	void checkSumOverLimitAndTimeline(Collection<ASSIGNMENT> timeline, ASSIGNEE assignee, Date timelineBegin);
	
	
	/**
	 * @param timeline timeline with share
	 * @param assignee target of the timeline
	 * @param limit
	 * @return true if at any moment the sum of shares is over the limit
	 * @throws BusinessLogicException if there multiple assignments of the assigned entity to the assignee
	 * at the same time
	 */
	boolean isSumOverLimit(Collection<ASSIGNMENT> timeline, ASSIGNEE assignee, Number limit);
	
	/**
	 * @param timeline timeline with share
	 * @param assignee target of the timeline
	 * @param limit
	 * @return BusinessMessages if at any time the sum of shares is over limit 
	 * @throws BusinessLogicException if there multiple assignments of the assigned entity to the assignee
	 * at the same time 
	 */
	BusinessMessages checkSumOverLimit(Collection<ASSIGNMENT> timeline, ASSIGNEE assignee, Number limit);
	
	/**
	 * Checks that the sum of shares as defined by timelineWithShare equals at all times the target sum
	 * defined by timelineDefiningTarget.
	 * @param timelineDefiningTarget timeline defining target sums. It can have gaps but it shall not have overlaps.
	 * @param timelineWithShare timeline defining shares
	 * @param getTarget method to read the target sum from the historization defining target
	 * @return BusinessMessages when at any time the sum of the shares deviates from the target 
	 * more than tolerance allows
	 */
	BusinessMessages checkSumDeviatesFromTarget(Collection<? extends Historization> timelineDefiningTarget, 
			Collection<ASSIGNMENT> timelineWithShare, Method getTarget);
	
	/**
	 * Checks that the sum of shares as defined by timelineWithShare is at all times less or equal 
	 * the target sum defined by timelineDefiningTarget.
	 * @param timelineDefiningTarget timeline defining target sums. It can have gaps but it shall not have overlaps.
	 * @param timelineWithShare timeline defining shares
	 * @param getTarget method to read the target sum from the historization defining target
	 * @return BusinessMessages when at any time the sum of the shares deviates from the target 
	 * more than tolerance allows
	 */
	BusinessMessages checkSumOverLimit(Collection<? extends Historization> timelineDefiningTarget, 
			Collection<ASSIGNMENT> timelineWithShare, Method getTarget);
	
	/**
	 * Checks that 
	 * <ul><li>before timelineBeginning the sum of shares as defined by timelineWithShare is at all times less or equal 
	 * the target sum defined by timelineDefiningTarget</li>
	 * <li>from timelineBeginning the sum of shares as defined by timelineWithShare equals at all times the target sum
	 * defined by timelineDefiningTarget.</li></ul>
	 * @param timelineDefiningTarget timeline defining target sums. It can have gaps but it shall not have overlaps.
	 * @param timelineWithShare timeline defining shares
	 * @param getTarget method to read the target sum from the historization defining target
	 * @param timelineBeginning beginning of the strict timeline (no overlaps, no gaps) 
	 * null means no strict timeline at all
	 * @return BusinessMessages when at any time the sum of the shares deviates from the target 
	 * more than tolerance allows
	 */
	BusinessMessages checkSumOverLimitOrDeviatesFromTarget(Collection<? extends Historization> timelineDefiningTarget, 
			Collection<ASSIGNMENT> timelineWithShare, Method getTarget, Date timelineBeginning);
	
	/**
	 * Checks that 
	 * <ul><li>before timelineBeginning the sum of shares as defined by timelineWithShare is at all times less or equal 
	 * the target sum defined by timelineDefiningTarget</li>
	 * <li>from timelineBeginning the sum of shares as defined by timelineWithShare equals at all times the target sum
	 * defined by timelineDefiningTarget.</li></ul>
	 * @param timelineDefiningTarget timeline defining target sums. It can have gaps but it shall not have overlaps.
	 * @param timelineWithShare timeline defining shares
	 * @param getTarget method to read the target sum from the historization defining target
	 * @param timelineBeginning beginning of the strict timeline (no overlaps, no gaps) 
	 * null means no strict timeline at all
	 * @param timeRange restricts the investigated time range
	 * @return BusinessMessages when at any time the sum of the shares deviates from the target 
	 * more than tolerance allows
	 */
	BusinessMessages checkSumOverLimitOrDeviatesFromTarget(Collection<? extends Historization> timelineDefiningTarget, 
			Collection<ASSIGNMENT> timelineWithShare, Method getTarget, Date timelineBeginning, Interval timeRange);
	
	/**
	 * {@link at.workflow.webdesk.po.timeline.TimelineFragment}
	 * @param assignments list of historicized links with share
	 * @return timelineFragments of all the links
	 * @throw IllegalArgumentException(TimelineFragment.DUPLICATE_ASSIGNMENT:assigned.toString()) when
	 * there is duplicate assignment of assigned entity at some time
	 */
	List<TimelineFragment<ASSIGNED, ASSIGNMENT>> getSortedTimelineFragments(Collection<ASSIGNMENT> assignments);

	/**
	 * Sorts assignments according to assigned objects and to validfrom date.
	 * @param assignments
	 * @return sorted timelines, key = assigned object, value = timeline with this assigned object
	 */
	Map<ASSIGNED, List<ASSIGNMENT>> getSortedTimelines(Collection<ASSIGNMENT> assignments);
	 
	/**
	 * {@link at.workflow.webdesk.po.timeline.SharedTimelineTableImpl}
	 * @param assignments
	 * @return timelineTable of all the assignments
	 */
	SharedTimelineTable<ASSIGNED, SHARE> getTimelineTable(Collection<ASSIGNMENT> assignments);
	
	/** @return all distinct objects that are assigned via assignments */ 
	Set<ASSIGNED> getAllAssignedObjects(Collection<ASSIGNMENT> assignments);
	
	/** sum of the shares required at any moment, e.g. 100 for percent */
	SHARE getShareSumTarget();
	
}