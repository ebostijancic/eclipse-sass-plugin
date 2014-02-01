package at.workflow.webdesk.po.timeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.util.CollectionUtils;

import at.workflow.webdesk.po.HistorizationTimelineHelper;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.comparator.PropertiesComparator;
import at.workflow.webdesk.tools.date.DateInterval;
import at.workflow.webdesk.tools.date.HistoricizableValidityComparator;
import at.workflow.webdesk.tools.date.HistorizationHelper;
import at.workflow.webdesk.tools.date.Interval;
import at.workflow.webdesk.tools.date.IntervalOrderComparator;

/**
 * This class provides methods for cleaning up and simplifying the timeline:
 * <ol><li>replacement of null dates</li>
 * <li>removal of assignments that have not positive validity</li>
 * <li>merge of same simultaneous assignments</li>
 * <li>merge of same consecutive assignments</li></ol> 
 * 
 * TODO: when merging check the validity of the assigned object
 * 
 * @author sdzuban 07.11.2013
 */
public class HistorizationCleanerAndMerger {
	
	private HistorizationTimelineHelper helper;
	
	/**
	 * Replaces null validfrom and validto dates with values generated by HistorizationHelper. 
	 * @param assignments are modified in place
	 */
	public void replaceNullDates(Collection<? extends Historization> assignments) {
		
		if (CollectionUtils.isEmpty(assignments)) // nothing to do
			return;
		
		for (Historization assignment : assignments) {
			if (assignment.getValidfrom() == null)
				assignment.setValidfrom(HistorizationHelper.generateUsefulValidFrom(null));
			if (assignment.getValidto() == null)
				assignment.setValidto(HistorizationHelper.generateUsefulValidTo(null));
		}
	}

	/**
	 * Removes assignments that have not positive validity.
	 * @param assignments are modified in place
	 * @return removed assignments
	 */
	public Set<? extends Historization> removeInvalidAssignments(Collection<? extends Historization> assignments) {
		
		if (CollectionUtils.isEmpty(assignments)) // nothing to do
			return Collections.emptySet();
		
		Set<Historization> toRemove = new HashSet<Historization>();
		for (Historization assignment : assignments)
			if ( ! assignment.getValidity().isPositive())
				toRemove.add(assignment);
		
		assignments.removeAll(toRemove);
		return toRemove;
	}

	/**
	 * Merges overlapping assignment to same entity.
	 * If assignment contains other properties like "percent" 
	 * only assignments to the same entity with same such values are merged. 
	 * @param assignments are modified in place
	 * @return removed assignments
	 */
	public Set<? extends Historization> mergeMultipleSimultaneousAssignments(Collection<? extends Historization> assignments) {
		
		if (CollectionUtils.isEmpty(assignments) || assignments.size() < 2) // nothing to do
			return Collections.emptySet();
		
		SortedSet<Date> timelineDates = getTimelineDates(assignments);
		SortedSet<Interval> timelineIntervals = getTimelineIntervals(timelineDates);
		
		SortedMap<Interval, List<Historization>> sortedTimelineFragments =
				collectFragmentAssignments(assignments, timelineIntervals);

		if ( ! containsMultipleSimultaneousAssignments(sortedTimelineFragments)) // nothing to merge
			return Collections.emptySet();
		
		return mergeMultipleSimultaneousAssignments(assignments, timelineIntervals);
	}
	
	/**
	 * Merges non-overlapping assignment to same entity that are consecutive.
	 * If assignment contains other properties like "percent" 
	 * only assignments to the same entity with same such values are merged.
	 * It shall be applied only when there are no more simultaneous assignments. 
	 * @param assignments are modified in place
	 * @return removed assignments
	 */
	public Set<? extends Historization> mergeConsecutiveAssignments(Collection<? extends Historization> assignments) {
		
		if (CollectionUtils.isEmpty(assignments) || assignments.size() < 2) // nothing to do
			return Collections.emptySet();
		
		/** list of lists containing assignments differing only in UID, validfrom and validto */
		List<List<Historization>> targetAssignmentsList = getTargetAssignments(assignments);
		Set<Historization> removed = new HashSet<Historization>();
		for (List<? extends Historization> targetAssignments : targetAssignmentsList)
			for (Historization merged : mergeConsecutiveAssignmentsOfOneTarget(targetAssignments, assignments))
				removed.add(merged);
		
		return removed;
	}
	
	/**
	 * For unknown reason cleaning and merging must be performed 2 or 3 times on really bad data.
	 * This method does cleaning and merging as long as the number of assignments decreases.
	 * @param assignments are modified in place
	 * @return removed assignments
	 */
	public Set<? extends Historization> cleanAndMergeAssignments(Collection<? extends Historization> assignments) {
		
		if (CollectionUtils.isEmpty(assignments)) // nothing to do
			return Collections.emptySet();
		
		Set<Historization> removed = new HashSet<Historization>();
		
		removed.addAll(removeInvalidAssignments(assignments));
		
		int countBefore, countAfter;
		do {
			countBefore = assignments.size();
			removed.addAll(mergeMultipleSimultaneousAssignments(assignments));
			removed.addAll(mergeConsecutiveAssignments(assignments));
			removed.addAll(removeInvalidAssignments(assignments));
			countAfter = assignments.size();
		}
		while (countAfter < countBefore);
		
		return removed;
	}
	
//	-------------------------------------- PRIVATE METHODS ----------------------------------------
	
	
	private SortedSet<Date> getTimelineDates(Collection<? extends Historization> assignments) {
		
		SortedSet<Date> timelineDates = new TreeSet<Date>();
		for (Historization assignment : assignments) {
			if (assignment.getValidfrom() == null || assignment.getValidto() == null)
				throw new IllegalArgumentException("Validfrom or valid to is null " +  assignment.toString());
			timelineDates.add(assignment.getValidfrom());
			timelineDates.add(assignment.getValidto());
		}
		return timelineDates;
	}
	
	private SortedSet<Interval> getTimelineIntervals(SortedSet<Date> timelineDates) {
		
		SortedSet<Interval> result = new TreeSet<Interval>(new IntervalOrderComparator());
		Iterator<Date> itr;
		itr = timelineDates.iterator();
		Date t1 = null;
		if (itr.hasNext())
			t1 = itr.next();
		while (itr.hasNext()) {
			Date t2 = itr.next();
			Interval interval = new DateInterval(t1, t2);
			result.add(interval);
			t1 = t2;
		}
		return result;
	}

	private SortedMap<Interval, List<Historization>> collectFragmentAssignments(Collection<? extends Historization> assignments, 
					SortedSet<Interval> timelineIntervals) {
		
		SortedMap<Interval, List<Historization>> result = new TreeMap<Interval, List<Historization>>(new IntervalOrderComparator());
		for (Interval interval : timelineIntervals)
			result.put(interval, getValidAssignments(assignments, interval));

		return result;
	}


	private List<Historization> getValidAssignments(
			Collection<? extends Historization> assignments, Interval interval) {
		
		List<Historization> result = new ArrayList<Historization>();
		for (Historization assignment : assignments)
			if (assignment == null)
				continue;
			else if (HistorizationTimelineUtils.isComplete(assignment, interval.getFrom(), interval.getTo()))
				result.add(assignment);
		return result;
	}

	private boolean containsMultipleSimultaneousAssignments(
			SortedMap<Interval, List<Historization>> sortedTimelineFragments) {
		
		for (List<? extends Historization> simultaneousAssignments : sortedTimelineFragments.values()) {
			if (CollectionUtils.isEmpty(simultaneousAssignments) || simultaneousAssignments.size() < 2)
				continue; // nothing to merge
			for (int i = 0; i < simultaneousAssignments.size() - 1; i++) {
				Historization firstAssignment = simultaneousAssignments.get(i);
				for (int k = i + 1; k < simultaneousAssignments.size(); k++) {
					Historization secondAssignment = simultaneousAssignments.get(k);
					if (PropertiesComparator.isEqual(firstAssignment, secondAssignment, "UID", "validfrom", "validto"))
						return true;
				}
			}
		}
		return false;
	}

	private Set<? extends Historization>  mergeMultipleSimultaneousAssignments(Collection<? extends Historization> assignments, 
			SortedSet<Interval> timelineIntervals) {

		Set<Historization> removed = new HashSet<Historization>();
		for (Interval interval : timelineIntervals) {
			List<? extends Historization> simultaneousAssignments = getValidAssignments(assignments, interval);
			if (simultaneousAssignments.size() < 2)
				continue;
			
			List<Historization> toRemove = new ArrayList<Historization>();
			for (int i = 0; i < simultaneousAssignments.size() - 1; i++) {
				Historization firstAssignment = simultaneousAssignments.get(i);
				if (toRemove.contains(firstAssignment))
					continue;
				for (int k = i + 1; k < simultaneousAssignments.size(); k++) {
					Historization secondAssignment = simultaneousAssignments.get(k);
					if (toRemove.contains(secondAssignment))
						continue;
					if (PropertiesComparator.isEqual(firstAssignment, secondAssignment, "UID", "validfrom", "validto")) {
						if (firstAssignment.getValidity().enclosing(secondAssignment.getValidity())) {
							; // nothing to do, the second will be deleted
						} else { // stretch the first assignment to cover union of intervals
							Interval union = firstAssignment.getValidity().union(secondAssignment.getValidity()); 
							firstAssignment.setValidfrom(union.getFrom());
							firstAssignment.setValidto(union.getTo());
						}		
						toRemove.add(secondAssignment);
					}
				}
			}
			removed.addAll(toRemove);
			assignments.removeAll(toRemove);
		}
		return removed;
	}
	
	private List<List<Historization>> getTargetAssignments(Collection<? extends Historization> assignments) {
		
		List<List<Historization>> result = new ArrayList<List<Historization>>();
		for (Historization assignment : assignments) {
			boolean added = false;
			for (List<Historization> assignmentList : result) {
				if (PropertiesComparator.isEqual(assignment, assignmentList.get(0), "UID", "validfrom", "validto")) { 
					assignmentList.add(assignment);
					added = true;
					break;
				}
			}
			if ( ! added) {
				List<Historization> newList = new ArrayList<Historization>();
				newList.add(assignment);
				result.add(newList);
			}
		}
		
		return result;
	}

	private Set<? extends Historization> mergeConsecutiveAssignmentsOfOneTarget(List<? extends Historization> sameTargetAssignments, Collection<? extends Historization> assignments) {
		
		Collections.sort(sameTargetAssignments, new HistoricizableValidityComparator());
		
		Set<Historization> removed = new HashSet<Historization>();
		
		Iterator<? extends Historization> it = sameTargetAssignments.iterator();
		Historization first = null;
		if (it.hasNext())
			first = it.next();
		while (it.hasNext()) {
			Historization second = it.next();
			if (helper.areDatesConsecutive(first.getValidto(), second.getValidfrom())) {
				// merge consecutive assignments
				first.setValidto(second.getValidto());
				removed.add(second);
				assignments.remove(second);
			} else
				first = second;
		}
		
		return removed;
	}

	public void setHelper(HistorizationTimelineHelper helper) {
		this.helper = helper;
	}
	
}
