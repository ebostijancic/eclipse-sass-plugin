package at.workflow.webdesk.po.timeline;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import at.workflow.webdesk.po.EditableTable.Row;
import at.workflow.webdesk.po.HistorizationWithShare;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.ShareAware;
import at.workflow.webdesk.po.SharedTimelineHandler;
import at.workflow.webdesk.po.SharedTimelineTable;
import at.workflow.webdesk.po.SharedTimelineTableAdapter;
import at.workflow.webdesk.po.timeline.TimelineDate.DateType;
import at.workflow.webdesk.tools.NamingConventionI18n;
import at.workflow.webdesk.tools.api.BusinessLogicException;
import at.workflow.webdesk.tools.api.BusinessMessages;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.api.I18nMessage;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.date.EditableDateInterval;
import at.workflow.webdesk.tools.date.HistorizationHelper;
import at.workflow.webdesk.tools.date.Interval;
import at.workflow.webdesk.tools.numbers.NumberComparator;
import at.workflow.webdesk.tools.numbers.NumberSumCalculator;

/**
 * @author sdzuban 02.04.2013
 */
public abstract class AbstractSharedTimelineHandler<ASSIGNEE extends Historization, 
												ASSIGNMENT extends HistorizationWithShare, 
												ASSIGNED extends PersistentObject, 
												SHARE extends Number> 
											implements SharedTimelineHandler<ASSIGNEE,ASSIGNMENT,ASSIGNED,SHARE> {
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
	
    private static final Logger logger = Logger.getLogger(AbstractSharedTimelineHandler.class);
    
    private class AbstractAssignment {
    	Date validfrom;
    	Date validto;
    	int rowIdx;
    	int columnIdx;
    	@Override
    	public String toString() {
    		return this.getClass().getSimpleName() + "[row: "+rowIdx+", col:"+columnIdx+
    			", from "+validfrom+", to "+validto+"]";
    	}
    }
    
    private class ColumnExact {
    	int colIdx;
    	boolean exact;
    	@Override
    	public String toString() {
    		return this.getClass().getSimpleName() + "[colIdx: "+colIdx+", exact:"+exact+"]";
    	}
    }
    
	private NumberComparator comparator;
	private NumberSumCalculator calculator;
		
//	--------------------- METHODS TO OVERRIDE ------------------------------	
	
	/** type of the share - Integer, Long, Float, Double */
	protected abstract Class<SHARE> getShareType();
	/** tolerance of the real sum of the shares with regard to targeted one, e.g. 0.01 for share sum 100.0 */
	protected abstract SHARE getSumTolerance();

	/** returns adapter corresponding to the configuration of classes */
	protected abstract SharedTimelineTableAdapter<ASSIGNEE, ASSIGNMENT, ASSIGNED, SHARE> getAdapter();
	
	/** 
	 * This method must always deliver current assignments of assignee. 
	 * No caching possible because after every one share change processed share
	 * will effect changes in the assignment collection of assignee 
	 * @param assignee e.g. position
	 * @return current assignments
	 */
	protected abstract Collection<ASSIGNMENT> getAssignments(ASSIGNEE assignee);

	/**
	 * This method must deliver the assigned entity.
	 * @param assignment e.g. position-to-costCenter assignment
	 * @return assigned entity e.g. costCenter
	 */
	protected abstract ASSIGNED getAssigned(ASSIGNMENT assignment);

	/**
	 * @return i18nKey of the share property, used for assembly of I18nMessages
	 * e.g. NamingConventionI18n.getI18nKey(assignmentClass, sharePropertyName) 
	 */
	protected abstract String getSharePropertyI18nKey();
	
	/** @return name table of the proper type */
	protected abstract SharedTimelineTable<ASSIGNED, SHARE> getNewTable();
	
//	--------------------- END METHODS TO OVERRIDE ------------------------------	
	
	/** must be called in the constructor of the derived handler */
	public void init() {
		
		calculator = new NumberSumCalculator(getShareType());
		comparator = new NumberComparator(getShareType(), getSumTolerance());
	}

	
	/** {@inheritDoc} */
	@Override
	public void assign(ASSIGNEE assignee, ASSIGNED assigned, Date from, Date to) {
		
		if (assignee == null)
			throw new IllegalArgumentException("Assignee must not be null");
		
		Interval useful = HistorizationTimelineUtils.prepareAssignment(assignee, assigned, from, to);
		
        SharedTimelineTable<ASSIGNED, SHARE> table = new SharedTimelineTableImpl<ASSIGNED, SHARE>(1, 1);
        table.setHeader(new EditableDateInterval(useful.getFrom(), useful.getTo()), 0);
        table.setRowObject(assigned, 0);
        table.setContent(getShareSumTarget(), 0, 0);
		
		assign(assignee, table);
	}
	
	
	/** {@inheritDoc} */
	@Override
	public void assign(ASSIGNEE assignee, SharedTimelineTable<ASSIGNED, SHARE> timelineTable) {
	
		logger.debug("Going to assign " + assignee + " to assigned entities");
		
		Date afrom = assignee.getValidfrom();
		Date ato = assignee.getValidto();
		Date tfrom = timelineTable.getHeader(0).getFrom();
		Date tto = timelineTable.getHeader(timelineTable.getColumnCount() - 1).getTo();
		
		// no beginning -> validfrom() of the assignee
		if (tfrom == null)
			timelineTable.getHeader(0).setFrom(afrom);
		// no end -> validto() of the assignee
		if (tto == null)
			timelineTable.getHeader(0).setTo(ato);
		
		trimFromDatesToDateOnly(timelineTable);
//		trimToDatesToLastMomentOfDay(timelineTable);
		
		Date ausefulfrom = HistorizationHelper.generateUsefulValidFromDay(afrom);
		Date ausefulto = HistorizationHelper.generateUsefulValidToDay(ato);
		Date tusefulfrom = HistorizationHelper.generateUsefulValidFromDay(tfrom);
		Date tusefulto = HistorizationHelper.generateUsefulValidToDay(tto);
		
		if (tusefulfrom.before(ausefulfrom)) {
			String[] params = {sdf.format(tusefulfrom), 
					NamingConventionI18n.getI18nKey(assignee.getClass()), 
					sdf.format(ausefulfrom)};
			throw new BusinessLogicException(
					new I18nMessage("po_abstractSharedTimelineHandler.assign_assignments_from_{0}_before_{1}_from_{2}", 
							params,
							new Boolean[] {false, true, false}));
		}
		
		if (ausefulto.before(tusefulto)) {
			String[] params = {NamingConventionI18n.getI18nKey(assignee.getClass()),
					sdf.format(ausefulto),
					sdf.format(tusefulto)};
			throw new BusinessLogicException(
					new I18nMessage("po_abstractSharedTimelineHandler.assign_{0}_to_{1}_before_assignments_to_{2}", 
							params,
							new Boolean[] {true, false, false}));
		}
		
		// table can restrict the interval to smaller one
		if (ausefulfrom.getTime() >= tusefulfrom.getTime())
			timelineTable.getHeader(0).setFrom(ausefulfrom);
		else
			timelineTable.getHeader(0).setFrom(tusefulfrom);
		
		if (tusefulto.getTime() >= ausefulto.getTime())
			timelineTable.getHeader(timelineTable.getColumnCount() - 1).setTo(ausefulto);
		else
			timelineTable.getHeader(timelineTable.getColumnCount() - 1).setTo(tusefulto);
		
		checkRowObjectsForUniqueness(timelineTable);

		assignTable(assignee, timelineTable);
	}

	
	/** {@inheritDoc} */
	@Override
	public void checkSumOverLimitAndTimeline(Collection<ASSIGNMENT> timeline, ASSIGNEE assignee, Date timelineBegin) {
		
		if (assignee == null)
			throw new IllegalArgumentException("Assignee must be non-null.");
		if (timeline == null)
			throw new IllegalArgumentException("Timeline must be non-null.");
		if (timeline.isEmpty())
			throw new IllegalArgumentException("Timeline must not be empty.");
		if (assignee.getValidfrom() == null || assignee.getValidto() == null)
			throwNullDateException(assignee);
		
		if (timelineBegin == null)
			timelineBegin = DateTools.today();
		
		// the timeline is chopped to fragments and fragments are then individually checked for the obedience of the rules 
		List<ASSIGNMENT> assignments = new ArrayList<ASSIGNMENT>(timeline);
		List<TimelineFragment<ASSIGNED, ASSIGNMENT>> timelineFragments = 
				getSortedAndCheckedTimelineFragments(assignments, assignee, timelineBegin);
		
		List<TimelineFragment<ASSIGNED, ASSIGNMENT>> timelineFragmentsBefore = new ArrayList<TimelineFragment<ASSIGNED, ASSIGNMENT>>();
		List<TimelineFragment<ASSIGNED, ASSIGNMENT>> timelineFragmentsOnOrAfter = new ArrayList<TimelineFragment<ASSIGNED, ASSIGNMENT>>();
		
		for (TimelineFragment<ASSIGNED, ASSIGNMENT> fragment : timelineFragments)
			if (fragment.getDateTo().after(timelineBegin))
				timelineFragmentsOnOrAfter.add(fragment);
			else
				timelineFragmentsBefore.add(fragment);

		checkSumOverLimit(timelineFragmentsBefore, 100F);
		checkTargetSum(timelineFragmentsOnOrAfter);
	}
	
	
	/** {@inheritDoc} */
	@Override
	public void checkTimeline(Collection<ASSIGNMENT> timeline, ASSIGNEE assignee) {
		
		if (assignee == null)
			throw new IllegalArgumentException("Assignee must be non-null.");
		if (timeline == null)
			throw new IllegalArgumentException("Timeline must be non-null.");
		if (timeline.isEmpty())
			throw new IllegalArgumentException("Timeline must not be empty.");
		if (assignee.getValidfrom() == null || assignee.getValidto() == null)
			throwNullDateException(assignee);

		// the timeline is chopped to fragments and fragments are then individually checked for the obedience of the rules 
		List<ASSIGNMENT> assignments = new ArrayList<ASSIGNMENT>(timeline);
		List<TimelineFragment<ASSIGNED, ASSIGNMENT>> timelineFragments = 
				getSortedAndCheckedTimelineFragments(assignments, assignee, null);

		checkTargetSum(timelineFragments);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isSumOverLimit(Collection<ASSIGNMENT> timeline, ASSIGNEE assignee, Number limit) {
		
		if (assignee == null)
			throw new IllegalArgumentException("Assignee must be non-null.");
		if (timeline == null)
			throw new IllegalArgumentException("Timeline must be non-null.");
		if (limit == null)
			throw new IllegalArgumentException("Limit must be non-null.");
		if (timeline.isEmpty())
			return false;
		if (assignee.getValidfrom() == null || assignee.getValidto() == null)
			throwNullDateException(assignee);
		
		// the timeline is chopped to fragments and fragments are then individually checked for the obeyance of the rules 
		List<ASSIGNMENT> assignments = new ArrayList<ASSIGNMENT>(timeline);
		List<TimelineFragment<ASSIGNED, ASSIGNMENT>> timelineFragments = 
				getSortedAndCheckedTimelineFragments(assignments, assignee, null);
		
		return isSumOverLimit(timelineFragments, limit);
	}
	
	
	/** {@inheritDoc} */
	@Override
	public BusinessMessages checkSumOverLimit(Collection<ASSIGNMENT> timeline, ASSIGNEE assignee, Number limit) {
		
		if (assignee == null)
			throw new IllegalArgumentException("Assignee must be non-null.");
		if (timeline == null)
			throw new IllegalArgumentException("Timeline must be non-null.");
		if (limit == null)
			throw new IllegalArgumentException("Limit must be non-null.");
		if (timeline.isEmpty())
			return new BusinessMessages();
		if (assignee.getValidfrom() == null || assignee.getValidto() == null)
			throwNullDateException(assignee);
		
		// the timeline is chopped to fragments and fragments are then individually checked for the obeyance of the rules 
		List<ASSIGNMENT> assignments = new ArrayList<ASSIGNMENT>(timeline);
		List<TimelineFragment<ASSIGNED, ASSIGNMENT>> timelineFragments = 
				getSortedAndCheckedTimelineFragments(assignments, assignee, null);
		
		return getSumOverLimitMessages(timelineFragments, limit, null);
	}
	
	/** {@inheritDoc} */
	@Override
	public BusinessMessages checkSumDeviatesFromTarget(Collection<? extends Historization> timelineDefiningTarget, 
			Collection<ASSIGNMENT> timelineWithShare, Method getTarget) {
		
		if (timelineDefiningTarget == null)
			throw new IllegalArgumentException("Target defining timeline shall not be null");
		if (timelineWithShare == null)
			throw new IllegalArgumentException("Timeline with share shall not be null");
		if (getTarget == null)
			throw new IllegalArgumentException("Method for target value extraction must be non-null.");
		
		LinkExtractor<ASSIGNMENT> linkExtractor = new LinkExtractor<ASSIGNMENT>();
		
		BusinessMessages result = new BusinessMessages();
		
		for (Historization historizationDefiningTarget : timelineDefiningTarget) {
			
			Interval validity = historizationDefiningTarget.getValidity();
			if (validity.isPositive()) {
				
				try {
					Number target = (Number) getTarget.invoke(historizationDefiningTarget);
					Collection<ASSIGNMENT> subtimelineWithShare = 
							linkExtractor.getLinksValidInInterval(timelineWithShare, validity);
					List<TimelineFragment<ASSIGNED, ASSIGNMENT>> timelineFragments = 
							getSortedTimelineFragments(subtimelineWithShare);
					result.addAll(getSumDeviatesFromTargetMessages(timelineFragments, target, validity));
					
				} catch (Exception e) {
					throw new RuntimeException("Error while checking timeline share sum vs. target: " + e, e);
				}
			}
		}
		return result;
	}
	
	/** {@inheritDoc} */
	@Override
	public BusinessMessages checkSumOverLimit(Collection<? extends Historization> timelineDefiningTarget, 
			Collection<ASSIGNMENT> timelineWithShare, Method getTarget) {
		
		return checkSumOverLimitOrDeviatesFromTarget(timelineDefiningTarget, timelineWithShare, getTarget, null);
	}
	
	/** {@inheritDoc} */
	@Override
	public BusinessMessages checkSumOverLimitOrDeviatesFromTarget(Collection<? extends Historization> timelineDefiningTarget, 
			Collection<ASSIGNMENT> timelineWithShare, Method getTarget, Date timelineBeginning) {
		
		return checkSumOverLimitOrDeviatesFromTarget(timelineDefiningTarget, timelineWithShare, getTarget, timelineBeginning, null);
	}
	
	/** {@inheritDoc} */
	@Override
	public BusinessMessages checkSumOverLimitOrDeviatesFromTarget(Collection<? extends Historization> timelineDefiningTarget, 
			Collection<ASSIGNMENT> timelineWithShare, Method getTarget, Date timelineBeginning, Interval timeRange) {
		
		if (timelineDefiningTarget == null)
			throw new IllegalArgumentException("Target defining timeline shall not be null");
		if (timelineWithShare == null)
			throw new IllegalArgumentException("Timeline with share shall not be null");
		if (getTarget == null)
			throw new IllegalArgumentException("Method for target value extraction must be non-null.");
		
		LinkExtractor<ASSIGNMENT> linkExtractor = new LinkExtractor<ASSIGNMENT>();
		
		BusinessMessages result = new BusinessMessages();
		
		for (Historization historizationDefiningTarget : timelineDefiningTarget) {
			
			Interval validity = historizationDefiningTarget.getValidity();
			if (timeRange != null)
				validity = validity.crossSection(timeRange);
			
			if (validity.isPositive()) {
				
				try {
					Number target = (Number) getTarget.invoke(historizationDefiningTarget);
					Collection<ASSIGNMENT> subtimelineWithShare = 
							linkExtractor.getLinksValidInInterval(timelineWithShare, validity);
					List<TimelineFragment<ASSIGNED, ASSIGNMENT>> timelineFragments = 
							getSortedTimelineFragments(subtimelineWithShare);
					if (timelineBeginning == null || validity.getTo().before(timelineBeginning))
						result.addAll(getSumOverLimitMessages(timelineFragments, target, validity));
					else
						result.addAll(getSumDeviatesFromTargetMessages(timelineFragments, target, validity));
					
				} catch (Exception e) {
					throw new RuntimeException("Error while checking timeline share sum vs. target: " + e, e);
				}
			}
		}
		return result;
	}
	
	/** {@inheritDoc} */
	@Override
	public List<TimelineFragment<ASSIGNED, ASSIGNMENT>> getSortedTimelineFragments(Collection<ASSIGNMENT> assignments) {
		
		SortedSet<TimelineDate> timelineDates = getTimelineDates(assignments);
		
		if (timelineDates.isEmpty())
			return Collections.<TimelineFragment<ASSIGNED, ASSIGNMENT>>emptyList();

		return collectTimelineFragments(assignments, timelineDates);
	}

	
	/** {@inheritDoc} */
	@Override
	public Map<ASSIGNED, List<ASSIGNMENT>> getSortedTimelines(Collection<ASSIGNMENT> assignments) {
		
		try {
			Map<ASSIGNED, List<ASSIGNMENT>> result = new HashMap<ASSIGNED, List<ASSIGNMENT>>(); 
			for (Historization historization : HistorizationTimelineUtils.getSortedHistorizationList(assignments)) {
				@SuppressWarnings("unchecked")
				ASSIGNMENT assignment = (ASSIGNMENT) historization;
				ASSIGNED assigned = getAssigned(assignment);
				List<ASSIGNMENT> ass = result.get(assigned);
				if (ass == null) {
					ass = new ArrayList<ASSIGNMENT>();
					result.put(assigned, ass);
				}
				ass.add(assignment);
			}
			return result;
		} catch (Exception e) {
			throw new PoRuntimeException("Could not process existing assignments " + e, e);
		}
	}
	
	
	/** {@inheritDoc} */
	@Override
	public SharedTimelineTable<ASSIGNED, SHARE> getTimelineTable(Collection<ASSIGNMENT> assignments) {
		List<TimelineFragment<ASSIGNED, ASSIGNMENT>> fragments = getSortedTimelineFragments(assignments);
		SharedTimelineTable<ASSIGNED, SHARE> table = getNewTable();
		table.fillTableFromFragments(fragments);
		return table;
	}
	
	/** {@inheritDoc} */
	@Override
	public Set<ASSIGNED> getAllAssignedObjects(Collection<ASSIGNMENT> assignments) {
		
		Set<ASSIGNED> result = new HashSet<ASSIGNED>();
		
		for (ASSIGNMENT assignment : assignments)
			try {
				result.add(getAssigned(assignment));
			} catch (Exception e) {
				throw new PoRuntimeException("Could not get assigned object of " + assignment);
			}
		return result;
	}


//	-------------------------------------- PRIVATE METHODS ----------------------------------------

	private void assignTable(ASSIGNEE assignee, SharedTimelineTable<ASSIGNED, SHARE> table) {
		
		Date from = table.getHeader(0).getFrom();
		Date to = table.getHeader(table.getColumnCount() - 1).getTo();
		
		logger.debug("Going to assign " + assignee + " to assigned entities from " + from + " to " + to);
		// checks also parameters
		if (from.after(assignee.getValidfrom()) || to.before(assignee.getValidto())) {
			SharedTimelineTable<ASSIGNED, SHARE> tableFromDB = getTimelineTable(getAssignments(assignee));
			mergeTable(tableFromDB, table);
		}

		checkTimeline(table, assignee);
		
		SharedTimelineTableAdapter<ASSIGNEE, ASSIGNMENT, ASSIGNED, SHARE> adapter = getAdapter();
		adapter.setAssignee(assignee);
		adapter.setTable(table);
		adapter.setTolerance(getSumTolerance());
		
		// sort timelineTable according to linked entity
		Map<ASSIGNED, List<AbstractAssignment>> requiredLinks = getRequiredAssignments(adapter);
		
		// sort existing links according to linked entity
		Map<ASSIGNED, List<ASSIGNMENT>> existingLinks = getSortedTimelines(getAssignments(assignee));
		
		for (Map.Entry<ASSIGNED, List<AbstractAssignment>> requiredEntry : requiredLinks.entrySet()) {
			// process required entries while reusing existing ones and creating new only on demand
			processAssignments(adapter, requiredEntry.getValue(), existingLinks.get(requiredEntry.getKey()));
			existingLinks.remove(requiredEntry.getKey());
		}
		
		deleteOrNullify(adapter, existingLinks);

		logger.info("Assigned entities to assignee according to assignment table");
			
	}

	/**
	 * Merges second table (extract from all assignments) into the first table (all assignments).
	 * Rows of merged to table are reduced to correspond to those of the merged table.
	 * Columns of the table are replaced by columns of merged table
	 * 
	 * Public is this method only for the testing.
	 *  
	 * @param tableFromDB table containing all the assignments from assignees validfrom to assignees validto
	 * @param tableToSave table containing new assignments for any time interval up to validity of the assignee
	 */
	public void mergeTable(SharedTimelineTable<ASSIGNED, SHARE> tableFromDB, 
						SharedTimelineTable<ASSIGNED, SHARE> tableToSave) {
		
		if (tableFromDB.getColumnCount() == 0 || tableFromDB.getRowCount() == 0)
			return; // nothing to merge
		
		Date fromDB = tableFromDB.getHeader(0).getFrom();
		Date toDB = tableFromDB.getHeader(tableFromDB.getColumnCount() - 1).getTo();
		
		Date from = tableToSave.getHeader(0).getFrom();
		Date to = tableToSave.getHeader(tableToSave.getColumnCount() - 1).getTo();
		
		if (fromDB.compareTo(from) >= 0 && toDB.compareTo(to) <= 0)
			return;  // full replacement
		
		// determine until when and from when the old table must be copied into new one
		ColumnExact toCol = getIndexForFromDate(tableFromDB, from);
		ColumnExact fromCol = getIndexForToDate(tableFromDB, to);

		copyColumnsLeft(tableFromDB, tableToSave, toCol);
		copyColumnsRight(tableFromDB, tableToSave, fromCol);
	}
	
	/**
	 * Checks that the timeline as represented by the EditableTable object is
	 *  - complete, i.e. it covers all the time of validity of the assignee
	 *  - consistent, i.e. the sum of all the assignment's shares is prescribed amount all the time
	 * 
	 * @param timelineTable Table representing distribution of shares to assigned entities in relation to time
	 * for the whole assignees validity 
	 * @param assignee entity that must have e.g. 100 percent coverage of costs all the time
	 * @throws BusinessLogicException if the timeline is not complete or the sum of shares is not as required all the time
	 */
	private void checkTimeline(SharedTimelineTable<ASSIGNED, SHARE> table, ASSIGNEE assignee) {

		if (table == null || assignee == null)
			throw new IllegalArgumentException("Assignee and timeline must be not null");
		
		if (!isWellFormed(table))
			throw new IllegalArgumentException("The table is not well formed. Either header or body is null or not all rows are as long as header.");
			
		checkHeadersForNulls(table, assignee);
		trimFromDatesToDateOnly(table);
		fillToDatesAsLastMomentOfDay(table, assignee.getValidto());
		checkTimeline(getTimelineDates(table), assignee, null);

		checkRowObjectsForNull(table);
		checkRowObjectsForUniqueness(table);
		
		SharedTimelineTableAdapter<ASSIGNEE, ASSIGNMENT, ASSIGNED, SHARE> adapter = getAdapter();
		adapter.setAssignee(assignee);
		adapter.setTable(table);
		adapter.setTolerance(getSumTolerance());
		
		fillEmptyCellsWithZero(adapter);
		checkTargetSum(table);
	}
	
	private void checkHeadersForNulls(SharedTimelineTable<ASSIGNED, SHARE> table, ASSIGNEE assignee) {
		for (int idx = 0; idx < table.getColumnCount(); idx++) {
			Interval header = table.getHeader(idx);
				if (header == null || header.getFrom() == null)
					throw new IllegalArgumentException("Empty header or header date at index " + idx);
		}
		if (table.getHeader(0).getFrom().after(assignee.getValidfrom())) {
			String[] params = {NamingConventionI18n.getI18nKey(table.getRowObject(0).getClass()),
					sdf.format(table.getHeader(0).getFrom()),
					NamingConventionI18n.getI18nKey(assignee.getClass()),
					sdf.format(assignee.getValidfrom())};
			throw new BusinessLogicException(
					new I18nMessage("po_abstractSharedTimelineHandler.checkHeadersForNulls_{0}_assignments_start_{1}_after_{2}_starts_{3}",
							params,
							new Boolean[] {true, false, true, false}));
		}
	}
	
	private void trimFromDatesToDateOnly(SharedTimelineTable<ASSIGNED, SHARE> table) {
		// beginning day can be 'now' and shall not be tampered with
		for (int idx = 1; idx < table.getColumnCount(); idx++) {
			EditableDateInterval header = table.getHeader(idx);
			header.setFrom(HistorizationHelper.generateUsefulValidFrom(header.getFrom()));
		}
	}
	
	@SuppressWarnings("unused")
	private void trimToDatesToLastMomentOfDay(SharedTimelineTable<ASSIGNED, SHARE> table) {
		// end day can be 'now' and shall not be tampered with
		for (int idx = 1; idx < table.getColumnCount(); idx++) {
			EditableDateInterval header = table.getHeader(idx);
			header.setTo(HistorizationHelper.generateUsefulValidTo(header.getTo()));
		}
	}
	
	private void checkRowObjectsForNull(SharedTimelineTable<ASSIGNED, SHARE> table) {
		for (int idx = 0; idx < table.getRowCount(); idx++) {
			ASSIGNED rowObject = table.getRowObject(idx);
			if (rowObject == null)
				throw new BusinessLogicException(
						new I18nMessage("po_abstractSharedTimelineHandler.checkRowObjectsForNull_no_entity_assigned_in_row_{0}",
								(idx + 1 ) +""));
		}
	}
	
	private void checkRowObjectsForUniqueness(SharedTimelineTable<ASSIGNED, SHARE> table) {
		Set<ASSIGNED> rowObjects = new HashSet<ASSIGNED>();
		ASSIGNED tresspassor = null;
		for (int idx = 0; idx < table.getRowCount(); idx++) {
			if (!rowObjects.add(table.getRowObject(idx)))
				tresspassor = table.getRowObject(idx);
		}
		if (table.getRowCount() != rowObjects.size())
			throw new BusinessLogicException(
					new I18nMessage("po_abstractSharedTimelineHandler.checkRowObjectsForUniqueness_{0}_{1}_assigned_to_multiple_rows", 
							new String[] {NamingConventionI18n.getI18nKey(tresspassor.getClass()),
								tresspassor.toString()},
							new Boolean[] {true, false}));
	}
	
	private void fillEmptyCellsWithZero(SharedTimelineTableAdapter<ASSIGNEE, ASSIGNMENT, ASSIGNED, SHARE> adapter) {
		SharedTimelineTable<ASSIGNED, SHARE> table = adapter.getTable();
		for (int rowIdx = 0; rowIdx < table.getRowCount(); rowIdx++) {
			for (int columnIdx = 0; columnIdx < table.getColumnCount(); columnIdx++)
				if (table.getContent(rowIdx, columnIdx) == null)
					adapter.setZeroShare(rowIdx, columnIdx);
		}
	}
	
	@SuppressWarnings("unused")
	private List<ASSIGNED> getRowObjects(SharedTimelineTable<ASSIGNED, SHARE> table) {
		List<ASSIGNED> result = new ArrayList<ASSIGNED>();
		for (int i = 0; i < table.getRowCount(); i++)
			result.add(table.getRowObject(i));
		return result;
	}
	
	private Map<ASSIGNED, Integer> getRowObjectMapping(SharedTimelineTable<ASSIGNED, SHARE> table) {
		Map<ASSIGNED, Integer> result = new HashMap<ASSIGNED, Integer>();
		for (int rowIdx = 0; rowIdx < table.getRowCount(); rowIdx++)
			result.put(table.getRowObject(rowIdx), rowIdx);
		return result;
	}
	
	private void copyColumnsLeft(SharedTimelineTable<ASSIGNED, SHARE> tableFromDB, 
			SharedTimelineTable<ASSIGNED, SHARE> tableNew, ColumnExact toColumn) {
		
		int toColumnIdx = toColumn.colIdx;
		Map<ASSIGNED, Integer> rowObjectMappingDB = getRowObjectMapping(tableFromDB);
		Map<ASSIGNED, Integer> rowObjectMappingNew = getRowObjectMapping(tableNew);
		for(int colIdx = 0; colIdx <= toColumnIdx; colIdx++) {
			if (colIdx == toColumnIdx && toColumn.exact)
				break;
			else if (colIdx == toColumnIdx && !toColumn.exact) { 
				// last column of insert and no beginning alignment - adapt to new table
				tableNew.addColumn(colIdx);
				Date from = tableFromDB.getHeader(colIdx).getFrom();
				Date to = makeUsefulToDate(tableNew.getHeader(toColumnIdx + 1).getFrom()); // 1 col inserted -> 1
				EditableDateInterval header = new EditableDateInterval(from, to);
				tableNew.setHeader(header, colIdx);
			} else { 
				tableNew.addColumn(colIdx);
				tableNew.setHeader(tableFromDB.getHeader(colIdx), colIdx);
			}

			// copy shares and additional data for rowObjects contained in the tableNew
			for (int rowIdx = 0; rowIdx < tableNew.getRowCount(); rowIdx++) {
				ASSIGNED rowObject = tableNew.getRowObject(rowIdx);
				Integer rowIdxDB = rowObjectMappingDB.get(rowObject);
				if (rowIdxDB != null) {
					tableNew.setContent(tableFromDB.getContent(rowIdxDB, colIdx), rowIdx, colIdx);
					getAdapter().copyAdditionalValues(tableFromDB, rowIdxDB, colIdx, tableNew, rowIdx, colIdx);
				}
			}
			if (colIdx == 0)
				// copy shares and additional data for rowObjects not contained in the tableNew
				for (int rowIdx = 0; rowIdx < tableFromDB.getRowCount(); rowIdx++) {
					ASSIGNED rowObject = tableFromDB.getRowObject(rowIdx);
					Integer rowIdxNew = rowObjectMappingNew.get(rowObject);
					if (rowIdxNew == null) { // not in new table
						int newRowIdx = tableNew.addRow();
						tableNew.setRowObject(tableFromDB.getRowObject(rowIdx), newRowIdx);
						tableNew.setContent(tableFromDB.getContent(rowIdx, colIdx), newRowIdx, colIdx);
						getAdapter().copyAdditionalValues(tableFromDB, rowIdx, colIdx, tableNew, newRowIdx, colIdx);
					}
				}
		}
	}

	private void copyColumnsRight(SharedTimelineTable<ASSIGNED, SHARE> tableFromDB, 
			SharedTimelineTable<ASSIGNED, SHARE> tableNew, ColumnExact fromColumn) {

		int fromColumnIdx = fromColumn.colIdx;
		Map<ASSIGNED, Integer> rowObjectMappingDB = getRowObjectMapping(tableFromDB);
		int toColumnIdx = tableFromDB.getColumnCount() - 1;
		int newColumnIdx = -1;
		for(int colIdx = fromColumnIdx; colIdx <= toColumnIdx; colIdx++) {
			if (colIdx == fromColumnIdx && fromColumn.exact)
				continue;
			else if (colIdx == fromColumnIdx && !fromColumn.exact) {
				// insert after the new table, first column of the insert - adapt to new Table
				newColumnIdx = tableNew.addColumn();
				Date from = makeUsefulFromDate(tableNew.getHeader(tableNew.getColumnCount() - 2).getTo());
				Date to = tableFromDB.getHeader(colIdx).getTo();
				EditableDateInterval header = new EditableDateInterval(from, to);
				tableNew.setHeader(header, newColumnIdx);
			} else { 
				newColumnIdx = tableNew.addColumn();
				tableNew.setHeader(tableFromDB.getHeader(colIdx), newColumnIdx);
			}
			
			// copy shares for rowObjects contained in the tableNew
			for (int rowIdx = 0; rowIdx < tableNew.getRowCount(); rowIdx++) {
				ASSIGNED rowObject = tableNew.getRowObject(rowIdx);
				Integer rowIdxDB = rowObjectMappingDB.get(rowObject);
				if (rowIdxDB != null) {
					tableNew.setContent(tableFromDB.getContent(rowIdxDB, colIdx), rowIdx, newColumnIdx);
					getAdapter().copyAdditionalValues(tableFromDB, rowIdxDB, colIdx, tableNew, rowIdx, newColumnIdx);
				}
			}
		}
	}
	
	private Date makeUsefulFromDate(Date to) {
		if (DateTools.lastMomentOfDay(to).compareTo(to) == 0)
			return DateTools.dateOnly(DateUtils.addDays(to, 1));
		return to;
	}

	private Date makeUsefulToDate(Date from) {
		if (DateTools.dateOnly(from).compareTo(from) == 0)
			return DateTools.lastMomentOfDay(DateUtils.addDays(from, -1));
		return from;
	}
	
	/** fills to dates so that they are just the last moment of the last day before next from dates */
	private void fillToDatesAsLastMomentOfDay(SharedTimelineTable<ASSIGNED, SHARE> table, Date lastTo) {
		
		for (int idx = 0; idx < table.getColumnCount() - 1; idx++) {
			EditableDateInterval current = table.getHeader(idx);
			Interval next = table.getHeader(idx + 1);
			current.setTo(makeUsefulToDate(next.getFrom()));
		}
		table.getHeader(table.getColumnCount() - 1).setTo(lastTo);
	}
	
	/** if exact flag is set the date is exactly a from date of the indicated column */
	private ColumnExact getIndexForFromDate(SharedTimelineTable<ASSIGNED, SHARE> table, Date fromDate) {
		
		for (int colIdx = 0; colIdx < table.getColumnCount(); colIdx++) {
			EditableDateInterval header = table.getHeader(colIdx);
			if (fromDate.compareTo(header.getFrom()) >= 0 && fromDate.compareTo(header.getTo()) < 0) {
				ColumnExact result = new ColumnExact();
				result.colIdx = colIdx;
				result.exact = fromDate.compareTo(header.getFrom()) == 0;
				return result;
			}
		}
		throw new BusinessLogicException(
				new I18nMessage("po_abstractSharedTimelineHandler.getIndexForFromDate_no_{0}_assignment_beginning_{1}",
						new String[] {NamingConventionI18n.getI18nKey(table.getRowObject(0).getClass()),
							sdf.format(fromDate)},
						new Boolean[] {true, false}));
	}
	
	/** if exact flag is set the date is exactly a to date of the indicated column */
	private ColumnExact getIndexForToDate(SharedTimelineTable<ASSIGNED, SHARE> table, Date toDate) {
		
		for (int colIdx = 0; colIdx < table.getColumnCount(); colIdx++) {
			EditableDateInterval header = table.getHeader(colIdx);
			if (toDate.compareTo(header.getFrom()) > 0 && toDate.compareTo(header.getTo()) <= 0) {
				ColumnExact result = new ColumnExact();
				result.colIdx = colIdx;
				result.exact = toDate.compareTo(header.getTo()) == 0;
				return result;
			}
		}
		throw new BusinessLogicException(
				new I18nMessage("po_abstractSharedTimelineHandler.getIndexForToDate_no_{0}_assignment_ending_{1}",
					new String[] {NamingConventionI18n.getI18nKey(table.getRowObject(0).getClass()),
						sdf.format(toDate)},
					new Boolean[] {true, false}));
	}
	
	/** transforms headers to TimelineDates */
	private SortedSet<TimelineDate> getTimelineDates(SharedTimelineTable<ASSIGNED, SHARE> table) {
		
		SortedSet<TimelineDate> timelineDates = new TreeSet<TimelineDate>();
		for (int idx = 0; idx < table.getColumnCount(); idx++) {
			EditableDateInterval interval = table.getHeader(idx);
			if (interval.getFrom() == null || interval.getTo() == null) {
				String[] params = {idx + "", interval.getIntervalAsString()};
				throw new BusinessLogicException(
						new I18nMessage("po_abstractSharedTimelineHandler.getTimelineDates_from_or_to_of_column_{0}_null_{1}", params));
			}
			timelineDates.add(new TimelineDate(interval.getFrom(), TimelineDate.DateType.FROM));
			timelineDates.add(new TimelineDate(interval.getTo(), TimelineDate.DateType.TO));
		}
		return timelineDates;
	}

	/**
	 * Checks whether:
	 *  - header is not null
	 *  - body is not null
	 *  - all rows are exactly as long as the header
	 * @return true if yes, false otherwise
	 */
	private boolean isWellFormed(SharedTimelineTable<ASSIGNED, SHARE> table) {
		
		if (table == null || table.getColumnCount() == 0 || table.getRowCount() == 0)
			return false;
		
		int size = table.getColumnCount();
		for (int idx = 0; idx < table.getRowCount(); idx++) {
			Row<ASSIGNED, SHARE> row = table.getRow(idx);
			if (row.getRowContent() == null || row.getRowContent().size() != size)
				return false;
		}
		return true;
	}

	private void checkTargetSum(SharedTimelineTable<ASSIGNED, SHARE> table) {
		
		String msg = "";
		Class<?> rowObjectClass = null;
		for (int idx = 0; idx < table.getColumnCount(); idx++) {
			calculator.reset();
			for (SHARE share : getWholeColumn(table, idx))
				calculator.add(share);
			if (comparator.compare(calculator.getResult(), getShareSumTarget()) != 0) {
				rowObjectClass = table.getRowObject(0).getClass();
				msg += table.getHeader(idx).getIntervalAsString() + ": sum = " + calculator.getResult() + ". ";
			}
		}
		if (StringUtils.isNotBlank(msg)) {
			throw new BusinessLogicException(
					new I18nMessage("po_abstractSharedTimelineHandler.checkTargetSum_sum_{0}_of_{1}_deviates_from_{2}_{3}", 
							new String[] {getSharePropertyI18nKey(),
							NamingConventionI18n.getI18nKey(rowObjectClass),
							getShareSumTarget().doubleValue() + "",
							msg},
							new Boolean[] {true, true, false, false}));
		}
	}
	
	private List<SHARE> getWholeColumn(SharedTimelineTable<ASSIGNED, SHARE> table, int columnIdx) {
		
		List<SHARE> result = new ArrayList<SHARE>();
		for (int idx = 0; idx < table.getRowCount(); idx++)
			result.add(table.getContent(idx, columnIdx));
		return result;
	}
	
	private Map<ASSIGNED, List<AbstractAssignment>> getRequiredAssignments(SharedTimelineTableAdapter<ASSIGNEE, ASSIGNMENT, ASSIGNED, SHARE> adapter) {
		
		SharedTimelineTable<ASSIGNED, SHARE> table = adapter.getTable();
		
		Map<ASSIGNED, List<AbstractAssignment>> result = new HashMap<ASSIGNED, List<AbstractAssignment>> ();
		
		for (int rowIdx = 0; rowIdx < table.getRowCount(); rowIdx++) {
			List<AbstractAssignment> oneRow = new ArrayList<AbstractAssignment>();
			
			for (int columnIdx = 0; columnIdx < table.getColumnCount(); columnIdx++) {
				AbstractAssignment assignment = new AbstractAssignment();
				assignment.rowIdx = rowIdx;
				assignment.columnIdx = columnIdx;
				assignment.validfrom = table.getHeader(columnIdx).getFrom();
				assignment.validto = table.getHeader(columnIdx).getTo();
				oneRow.add(assignment);
			}
			ASSIGNED assigned = table.getRowObject(rowIdx);
			result.put(assigned, optimizeCount(adapter, rowIdx, oneRow));
		}
		
		return result;
	}

	/** merges neighboring AbstractAssignments with same share */
	private List<AbstractAssignment> optimizeCount(SharedTimelineTableAdapter<ASSIGNEE, ASSIGNMENT, ASSIGNED, SHARE> adapter, 
			int rowIdx, List<AbstractAssignment> oneRow) {
		
		List<AbstractAssignment> result = new ArrayList<AbstractAssignment>();  
		if (oneRow.size() < 2)
			return oneRow;
		
		int firstIdx = 0;
		boolean storeFirst = false;
		boolean storeSecond = false;
		AbstractAssignment first = null;
		AbstractAssignment second = null;
		for (int secondIdx = 1; secondIdx < oneRow.size(); secondIdx++) {
			first = oneRow.get(firstIdx);
			second = oneRow.get(secondIdx);
			if (adapter.isEqual(rowIdx, firstIdx, secondIdx)) {
				first.validto = second.validto;
				storeFirst = true;
				storeSecond = false;
				// forget the second, it is swallowed by the first
			} else {
				result.add(first);
				firstIdx = secondIdx;
				storeFirst = false;
				storeSecond = true;
			}
		}
		if (storeFirst)
			result.add(first);
		if (storeSecond)
			result.add(second);
		return result;
	}
	
	/** generates required assignments under reuse of existing ones */
	private void processAssignments(SharedTimelineTableAdapter<ASSIGNEE, ASSIGNMENT, ASSIGNED, SHARE> adapter, 
			List<AbstractAssignment> required, List<ASSIGNMENT> existing) {
		
		int skipped = 0;
		int rounds = Math.max(required == null ? 0 : required.size(), existing == null ? 0 : existing.size());
		for (int idx = 0; idx < rounds; idx++) {
			AbstractAssignment req = null;
			if (required != null && idx < required.size())
				req = required.get(idx);
			ASSIGNMENT exist = null;
			if (existing != null && idx - skipped < existing.size())
				exist = existing.get(idx - skipped);
			
			if (req != null && adapter.isZeroShare(req.rowIdx, req.columnIdx)) {
				skipped++; // skip one more required assignments
				continue;
			}
			
			if (req != null && exist != null) {
				// transfer required info to existing assignment
				adapter.fillContentFromTable(exist, req.rowIdx, req.columnIdx);
				exist.setValidfrom(req.validfrom);
				exist.setValidto(req.validto);
			} else if (req != null && exist == null) {
				// new link must be created
				ASSIGNMENT link = adapter.assignFromTable(req.rowIdx, req.columnIdx, req.validfrom, req.validto);
				adapter.save(link);
			} else if (req == null && exist != null) {
				// existing link must be deleted if table defines the whole lifetime
				// it simply is not needed
				adapter.delete(exist);
			}
		}
		
		// postprocess skipped number of existing objects
		// because they were processed only until Math.abs(...) - skipped
		if (existing != null && existing.size() > 0)
			for (int i = skipped; rounds - i < existing.size(); i--) {
				ASSIGNMENT exist = existing.get(rounds - i);
				adapter.delete(exist);
			}
	}

	private void deleteOrNullify(SharedTimelineTableAdapter<ASSIGNEE, ASSIGNMENT, ASSIGNED, SHARE> adapter,
			Map<ASSIGNED, List<ASSIGNMENT>> unusedLinks) {
		
		// because these are outside of the complete table of relations they have to be deleted 
		for (List<ASSIGNMENT> existings : unusedLinks.values()) {
			for (ASSIGNMENT assignment : existings) {
				adapter.delete(assignment);
			}
		}
	}
	
	private void checkTargetSum(Collection<TimelineFragment<ASSIGNED, ASSIGNMENT>> timelineFragments) {
		
		String msg = "";
		Class<?> assignedClass = null;
		for (TimelineFragment<ASSIGNED, ASSIGNMENT> fragment : timelineFragments) {
			calculator.reset();
			for (ASSIGNMENT assignment : fragment.getValidAssignments().values())
				calculator.add(((ShareAware) assignment).getShare());
			if (comparator.compare(calculator.getResult(), getShareSumTarget()) != 0) {
				assignedClass = getAssignedClass(fragment);
				msg += sdf.format(fragment.getDateFrom()) + " - " + sdf.format(fragment.getDateTo()) + 
						": sum = " + calculator.getResult() + ". ";
			}
		}
		if (StringUtils.isNotBlank(msg))
			throw new BusinessLogicException(
					new I18nMessage("po_abstractSharedTimelineHandler.checkTargetSum_sum_{0}_of_{1}_deviates_from_{2}_{3}", 
							new String[] {getSharePropertyI18nKey(),
							NamingConventionI18n.getI18nKey(assignedClass),
							getShareSumTarget().doubleValue() + "",
							msg},
							new Boolean[] {true, true, false, false}));
	}
	
	private void checkSumOverLimit(Collection<TimelineFragment<ASSIGNED, ASSIGNMENT>> timelineFragments, Number limit) {
		
		String msg = "";
		Class<?> assignedClass = null;
		for (TimelineFragment<ASSIGNED, ASSIGNMENT> fragment : timelineFragments) {
			calculator.reset();
			for (ASSIGNMENT assignment : fragment.getValidAssignments().values())
				calculator.add(((ShareAware) assignment).getShare());
			if (comparator.compare(calculator.getResult(), limit) > 0) {
				assignedClass = getAssignedClass(fragment);
				msg += sdf.format(fragment.getDateFrom()) + " - " + sdf.format(fragment.getDateTo()) + 
						": sum = " + calculator.getResult() + ". ";
			}
		}
		if (StringUtils.isNotBlank(msg))
			throw new BusinessLogicException(
					new I18nMessage("po_abstractSharedTimelineHandler.checkTargetSum_sum_{0}_of_{1}_deviates_from_{2}_{3}", 
							new String[] {getSharePropertyI18nKey(),
							NamingConventionI18n.getI18nKey(assignedClass),
							getShareSumTarget().doubleValue() + "",
							msg},
							new Boolean[] {true, true, false, false}));
	}
	
	private boolean isSumOverLimit(Collection<TimelineFragment<ASSIGNED, ASSIGNMENT>> timelineFragments, Number limit) {
		
		for (TimelineFragment<ASSIGNED, ASSIGNMENT> fragment : timelineFragments) {
			calculator.reset();
			for (ASSIGNMENT assignment : fragment.getValidAssignments().values())
				calculator.add(((ShareAware) assignment).getShare());
			if (comparator.compare(calculator.getResult(), limit) > 0)
				return true;
		}
		return false;
	}
	
	private BusinessMessages getSumOverLimitMessages(Collection<TimelineFragment<ASSIGNED, ASSIGNMENT>> timelineFragments, Number limit, Interval validity) {

		BusinessMessages messages = new BusinessMessages();
		for (TimelineFragment<ASSIGNED, ASSIGNMENT> fragment : timelineFragments) {
			calculator.reset();
			for (ASSIGNMENT assignment : fragment.getValidAssignments().values())
				calculator.add(((ShareAware) assignment).getShare());
			if (comparator.compare(calculator.getResult(), limit) > 0)
				messages.addWarning(
						new I18nMessage("po_abstractSharedTimelineHandler.checkTargetSum_sum_{0}_of_{1}_of_{2}_over_{3}_from_{4}_to_{5}",
								new String[] {calculator.getResult().doubleValue() + "",
									getSharePropertyI18nKey(),
									NamingConventionI18n.getI18nKey(getAssignedClass(fragment)),
									limit.doubleValue() + "",
									sdf.format(validity == null ? fragment.getDateFrom() : fragment.getDateInterval().crossSection(validity).getFrom()), 
									sdf.format(validity == null ? fragment.getDateTo() : fragment.getDateInterval().crossSection(validity).getTo())},
								new Boolean[] {false, true, true, false, false, false})); 
		}
		return messages;
	}
	
	private BusinessMessages getSumDeviatesFromTargetMessages(
			Collection<TimelineFragment<ASSIGNED, ASSIGNMENT>> timelineFragments, Number target, Interval validity) {
		
		BusinessMessages messages = new BusinessMessages();
		for (TimelineFragment<ASSIGNED, ASSIGNMENT> fragment : timelineFragments) {
			calculator.reset();
			for (ASSIGNMENT assignment : fragment.getValidAssignments().values())
				calculator.add(((ShareAware) assignment).getShare());
			if (comparator.compare(calculator.getResult(), target) != 0)
				messages.addWarning(
						new I18nMessage("po_abstractSharedTimelineHandler.checkTargetSum_sum_{0}_of_{1}_of_{2}_deviatesFrom_{3}_from_{4}_to_{5}",
								new String[] {calculator.getResult().doubleValue() + "",
								getSharePropertyI18nKey(),
								NamingConventionI18n.getI18nKey(getAssignedClass(fragment)),
								target.doubleValue() + "",
								sdf.format(fragment.getDateInterval().crossSection(validity).getFrom()), 
								sdf.format(fragment.getDateInterval().crossSection(validity).getTo())},
								new Boolean[] {false, true, true, false, false, false})); 
		}
		return messages;
	}
	
	private List<TimelineFragment<ASSIGNED, ASSIGNMENT>> getSortedAndCheckedTimelineFragments(
									Collection<ASSIGNMENT> assignments, ASSIGNEE assignee, Date timelineBegin) {
		
		SortedSet<TimelineDate> timelineDates = getTimelineDates(assignments);
		
		// due to the fact, that in consolidated all-time-100% timeline 
		// every time a new assignment is made some must be ended just before the new validfrom date
		// to provide room for the new share value
		
		checkTimeline(timelineDates, assignee, timelineBegin);
		
		if (timelineDates.isEmpty())
			return Collections.<TimelineFragment<ASSIGNED, ASSIGNMENT>>emptyList();
		
		return collectTimelineFragments(assignments, timelineDates);
	}
	
	private void checkTimeline(SortedSet<TimelineDate> timelineDates, ASSIGNEE assignee, Date timelineBegin) {
	
		if (timelineDates.isEmpty())
			return;
		
		if (timelineBegin == null)
			timelineBegin = assignee.getValidfrom(); // beginning of the validity
		
		checkForCompletness(timelineDates, assignee, timelineBegin);
		
		checkForOverlaps(timelineDates);
		
		checkForGaps(timelineDates, timelineBegin);
	}
	
	private SortedSet<TimelineDate> getTimelineDates(Collection<ASSIGNMENT> assignments) {
		
		SortedSet<TimelineDate> timelineDates = new TreeSet<TimelineDate>();
		for (ASSIGNMENT assignment : assignments) {
			if (assignment.getValidfrom() == null || assignment.getValidto() == null)
				throw new IllegalArgumentException("Validfrom or valid to is null " +  assignment.toString());
			timelineDates.add(new TimelineDate(assignment.getValidfrom(), TimelineDate.DateType.FROM));
			timelineDates.add(new TimelineDate(assignment.getValidto(), TimelineDate.DateType.TO));
		}
		return timelineDates;
	}

	private void checkForCompletness(SortedSet<TimelineDate> timelineDates, ASSIGNEE assignee, Date timelineBegin) {
		
		Date begin = timelineBegin.before(assignee.getValidfrom()) ? assignee.getValidfrom() : timelineBegin;
		
		if (timelineDates.first().getDate().after(begin) 
				||
				assignee.getValidto().after(timelineBegin) &&
				timelineDates.last().getDate().before(assignee.getValidto())) {
			String[] params = 
				{sdf.format(timelineDates.first().getDate()), 
					sdf.format(timelineDates.last().getDate()), 
					NamingConventionI18n.getI18nKey(assignee.getClass()),
					sdf.format(begin),
					sdf.format(assignee.getValidto())};
			throw new BusinessLogicException(
					new I18nMessage("po_abstractSharedTimelineHandler.checkForCompletness_assignments_from_{0}_to_{1}_do_not_cover_validity_of_{2}_from_{3}_to_{4}", 
							params,
							new Boolean[] {false, false, true, false, false}));  
		}
	}

	/* check where are at least two consecutive FROM or two consecutive TO TimelineDates meet */
	private void checkForOverlaps(SortedSet<TimelineDate> timelineDates) {
		
		Iterator<TimelineDate> itr = timelineDates.iterator();
		TimelineDate reference = itr.next();
		while (itr.hasNext()) {
			TimelineDate test = itr.next();
			if (reference.getDateType() == test.getDateType()) {
				String[] params = 
					{reference.getDateType().toString(), sdf.format(reference.getDate()),
					test.getDateType().toString(), sdf.format(test.getDate())};
				throw new BusinessLogicException(
						new I18nMessage("po_abstractSharedTimelineHandler.checkForOverlaps_overlapping_dates_{0}_{1}_and_{2}_{3}", 
								params)); 
			}
			reference = test;
		}
	}

	private void checkForGaps(SortedSet<TimelineDate> timelineDates, Date timelineBegin) {

		Iterator<TimelineDate> itr;
		itr = timelineDates.iterator();
		itr.next(); // drop the first from
		while (itr.hasNext()) {
			TimelineDate tTo = itr.next();
			if (itr.hasNext()) {
				TimelineDate tFrom = itr.next();
				if ( tFrom.getDate().after(timelineBegin)) { // only gaps after timeline begin are error 
					// allowed is maximum the difference between dateOnly and lastMomentOfDay of previous day on DB without milliseconds (23:59:59)
					if (Math.abs(tFrom.getDate().getTime() - tTo.getDate().getTime()) > 1000) {
						String[] params = {sdf.format(tTo.getDate()), sdf.format(tFrom.getDate())};
						throw new BusinessLogicException(
								new I18nMessage("po_abstractSharedTimelineHandler.checkForGaps_gap_found_from_{0}_to_{1}", params));
					}
				}
			}
		}
	}

	private List<TimelineFragment<ASSIGNED, ASSIGNMENT>> collectTimelineFragments(Collection<ASSIGNMENT> assignments, 
					SortedSet<TimelineDate> timelineDates) {
		
		List<TimelineFragment<ASSIGNED, ASSIGNMENT>> result = new ArrayList<TimelineFragment<ASSIGNED, ASSIGNMENT>>();
		Iterator<TimelineDate> itr;
		itr = timelineDates.iterator();
		if (itr.hasNext()) {
			Date dateFrom = itr.next().getDate();
			while (itr.hasNext()) {
				TimelineDate tDate = itr.next();
				Date dateTo = null; 
				if (tDate.getDateType() == DateType.TO)
					dateTo = tDate.getDate();
				else // DateType.FROM
					dateTo = DateUtils.addDays(tDate.getDate(), -1);
				TimelineFragment<ASSIGNED, ASSIGNMENT> fragment = new TimelineFragment<ASSIGNED, ASSIGNMENT>(dateFrom, dateTo);
				result.add(fragment);
				// collect assignments valid in the fragment
				for (ASSIGNMENT assignment : assignments) {
					if (HistorizationTimelineUtils.isComplete(assignment, dateFrom, dateTo))
						fragment.putValidAssignment(getAssigned(assignment), assignment);
				}
				if (tDate.getDateType() == DateType.TO && itr.hasNext())
					dateFrom = itr.next().getDate();
				else if (tDate.getDateType() == DateType.FROM)
					dateFrom = tDate.getDate();
			}
		}
		return result;
	}
	
	private void throwNullDateException(ASSIGNEE assignee) {
		throw new BusinessLogicException(
				new I18nMessage("po_abstractSharedTimelineHandler_{0}_has_null_validfrom_or_validto", 
						new String[] {NamingConventionI18n.getI18nKey(assignee.getClass())},
						new Boolean[] {true}));
	}
	private Class<? extends PersistentObject> getAssignedClass(
			TimelineFragment<ASSIGNED, ASSIGNMENT> fragment) {
		return fragment.getValidAssignments().keySet().iterator().next().getClass();
	}
	
}
