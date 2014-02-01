package at.workflow.webdesk.po.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.util.CollectionUtils;

import at.workflow.webdesk.po.HistorizationTimelineHelper;
import at.workflow.webdesk.po.HistorizationTimelineRepairer;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.model.HistorizationComparator;
import at.workflow.webdesk.po.timeline.HistorizationCleanerAndMerger;
import at.workflow.webdesk.po.timeline.HistorizationTimelineUtils;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.date.DateTools.DatePrecision;
import at.workflow.webdesk.tools.date.HistorizationHelper;
import at.workflow.webdesk.tools.date.Interval;

/**
 * Repairs gaps in timeline by:
 * <ul><li>deleted link:moving validto of link before gap to just before validfrom of link after gap</li>
 * <li>changed link: moving neighboring links to complementary date</li></ul>
 * Repairs overlaps in timeline by:
 * <ul><li>skipping overshadowed links</li>
 * <li>adapting validto or validfrom to the validity of neighboring link</li>
 * <li>splitting link to encopass overshadowed link</li></ul> 
 * 
 * @author sdzuban 16.05.2013
 */
public class HistorizationTimelineRepairerImpl implements HistorizationTimelineRepairer {
	
	private class LinkedHistory {
		
		private Historization historization;
		private LinkedHistory previous, next;
		
		public LinkedHistory(Historization historization) {
			super();
			this.historization = historization;
		}

		public Historization getHistorization() {
			return historization;
		}
		public LinkedHistory getPrevious() {
			return previous;
		}
		public void setPrevious(LinkedHistory previous) {
			this.previous = previous;
		}
		public LinkedHistory getNext() {
			return next;
		}
		public void setNext(LinkedHistory next) {
			this.next = next;
		}
	}

	private HistorizationComparator timelineComparator = new HistorizationComparator();
	private HistorizationTimelineHelper helper;
	private Collection<? extends Historization> links;
	private List<? extends Historization> timeline;
	// following map linkUid -> link is necessary for back and forth traversing when skipping historizations
	private Map<String, LinkedHistory> linkedHistoryMap = new HashMap<String, LinkedHistory>(); 
	// map linkUid -> link
	private Map<String, Historization> fromDBMap = new HashMap<String, Historization>();
	
	public void setHelper(HistorizationTimelineHelper helper) {
		this.helper = helper;
	}

	@Override
	public Set<? extends Historization> repairOverlapsAndTimeline(List<? extends Historization> fromDB, Collection<? extends Historization> links, Interval validity, Date timelineBegin) {
		
		if (timelineBegin == null || ! timelineBegin.after(validity.getFrom()))
			return repairTimeline(fromDB, links, validity);
		if ( ! timelineBegin.before(validity.getTo()))
			return repairOverlaps(fromDB, links, validity);
		
		if (CollectionUtils.isEmpty(links) && validity.getTo().after(timelineBegin)) // no links to fill the timeline part
			throw new RuntimeException("Timeline is empty");
		
		List<? extends Historization> historizations = HistorizationTimelineUtils.getSortedHistorizationList(links);
		Date firstValidFrom = historizations.get(0).getValidfrom();
		Date begin = timelineBegin.after(validity.getFrom()) ? timelineBegin : validity.getFrom(); 
		
		Set<Historization> removed = new HashSet<Historization>();
		removed.addAll(repair(fromDB, links, timelineBegin));
		removed.addAll(setBegin(links, validity.getFrom(), false, true)); // trim to validfrom
		removed.addAll(setBegin(links, begin, begin.before(firstValidFrom), false)); // expand to begin
		removed.addAll(setEnd(links, validity.getTo()));
		return removed;
	}
	
	@Override
	public Set<? extends Historization> repairTimeline(List<? extends Historization> fromDB, Collection<? extends Historization> links, Interval validity) {
	
		if (CollectionUtils.isEmpty(links) && validity.isPositive()) // no links to fill the timeline
			throw new RuntimeException("Timeline is empty");
		
		Set<Historization> removed = new HashSet<Historization>();
		removed.addAll(repair(fromDB, links, validity.getFrom()));
		removed.addAll(setBegin(links, validity.getFrom()));
		removed.addAll(setEnd(links, validity.getTo()));
		return removed;
	}
	
	@Override
	public Set<? extends Historization> repairOverlaps(List<? extends Historization> fromDB, Collection<? extends Historization> links) {
		return repair(fromDB, links, null);
	}
	
	@Override
	public Set<? extends Historization> repairOverlaps(List<? extends Historization> fromDB, Collection<? extends Historization> links, Interval validity) {
		Set<Historization> removed = new HashSet<Historization>();
		removed.addAll(repair(fromDB, links, null));
		removed.addAll(setBegin(links, validity.getFrom(), false, true));
		removed.addAll(setEnd(links, validity.getTo(), false, true));
		return removed;
	}

	@SuppressWarnings("unchecked")
	private Set<? extends Historization> repair(List<? extends Historization> fromDB, Collection<? extends Historization> links, Date timelineBegin) {
		
		HistorizationCleanerAndMerger cleanerAndMerger = new HistorizationCleanerAndMerger();
		cleanerAndMerger.setHelper(helper);
		// first the assignments are roughly cleaned and merged, as applicable
		cleanerAndMerger.replaceNullDates((Collection<Historization>) links);
		Set<? extends Historization> removed = cleanerAndMerger.cleanAndMergeAssignments((Collection<Historization>) links);

		// now the timeline will be repaired
		if (links.size() < 2)
			return removed;
		
		initialize(fromDB, links);
		
		List<Historization> additionalLinks = new ArrayList<Historization>();
		List<Historization> toRemoveLinks = new ArrayList<Historization>();
		Historization first = null;
		boolean firstTime = true;
		for (Historization link : timeline) {
			if (firstTime) {
				firstTime = false;
				first = link; 
			} else if (first.getValidity().isPositive() && link.getValidity().isPositive()) {
				Historization second = link;
				if (first.getValidity().enclosing(second.getValidity()) &&
						hasChangedValidfrom(second)) { 
					// second is inside first
					if (splitLink(first, second, additionalLinks))
						continue;
				} else if (second.getValidity().enclosing(first.getValidity()) &&
						hasChangedValidto(first)) { 
					// first is inside second
					if (splitLink(second, first, additionalLinks)) {
						first = second;
						continue;
					}
				}  
				if (isTimeline(first, second, timelineBegin) && !helper.areDatesConsecutive(first.getValidto(), second.getValidfrom()) ||
					!isTimeline(first, second, timelineBegin) && isOverlap(first, second))
					repair(first, second);
				
				first = second;
			} else if (first.getValidity().isPositive()) {
				// skip link with null validity
				toRemoveLinks.add(link);
				continue; 
			} else if (link.getValidity().isPositive()) { 
				// skip first with null validity
				toRemoveLinks.add(first);
				first = link; 
			} else {
				//
				toRemoveLinks.add(link);
				toRemoveLinks.add(first);
				first = link; 
			}
		}
		// add clones
		((Collection<Historization>) links).addAll(additionalLinks);
		((Collection<Historization>) links).removeAll(toRemoveLinks);
		// toRemoveLinks are not added to removed because they have to be deleted or historicized as appropriate
		// whereas the removed links are redundant links that have to be deleted physically
		return removed;
	}

	private boolean isTimeline(Historization first, Historization second, Date timelineBegin) {
		
		if (timelineBegin == null)
			return false;
		Date firstTo = first.getValidto();
		Date secondFrom = second.getValidfrom();
		// both are on or after ref. date
		if ( ! firstTo.before(timelineBegin) && ! secondFrom.before(timelineBegin))
			return true;
		// both are before ref. date
		if (firstTo.before(timelineBegin) && secondFrom.before(timelineBegin))
			return false;
		if (firstTo.before(timelineBegin) && ! secondFrom.before(timelineBegin)) {
			second.setValidfrom(timelineBegin); // to start of the timeline
			return false;
		}
		// first is on or after ref. date, second before -> overlap	
		return true;
	}

	private boolean splitLink(Historization encompassing, Historization encompassed, 
			List<Historization> additionalLinks) {
		
		Date newValidfrom = helper.getDateFrom(encompassed.getValidto());
		
		if (!newValidfrom.before(encompassing.getValidto()))
			return false;
			
		Date newValidto = helper.getDateTo(encompassed.getValidfrom());
		
		if (encompassing.getValidfrom().before(newValidto)) {
			Historization clone = null;
			try {
				clone = (Historization) BeanUtils.cloneBean(encompassing);
				BeanUtils.setProperty(clone, "UID", null);
			} catch (Exception e) {
				throw new RuntimeException("Could not clone bean " + encompassing + e);
			}
			clone.setValidto(newValidto);
			// add new link to collections of links of linked entities
			// otherwise its only database and visible only after page reload
			// this is not necessary - UI does full reload after save 
			// linker.addLinks(clone);
			additionalLinks.add(clone);
		}
		encompassing.setValidfrom(newValidfrom);
		
		return true;
	}
	
	private boolean isOverlap(Historization first, Historization second) {
		return first.getValidto().after(second.getValidfrom()) ||
				first.getValidfrom().after(first.getValidto()) ||
				second.getValidfrom().after(second.getValidto());
	}

	private void repair(Historization first, Historization second) {
		
		if (!first.getValidfrom().before(first.getValidto()))
			throw new PoRuntimeException("Validity is negativ, thus incorrect in this object: " + first);
				
		if (!second.getValidfrom().before(second.getValidto()))
			throw new PoRuntimeException("Validity is negativ, thus incorrect in this object: " + second);
		
		boolean firstChanged = hasChangedValidto(first);
		boolean secondChanged = hasChangedValidfrom(second);
		
		if (secondChanged || !firstChanged && !secondChanged) {
			if (second.getValidfrom().after(first.getValidfrom())) {
				// adapt first to the second
				first.setValidto(helper.getDateTo(second.getValidfrom()));
			} else {
				// delete links on left until second.getValidfrom() is inside one
				deleteSkippedLinksLeft(second);
			}
		} else { // only firstChanged
			if (first.getValidto().before(second.getValidto())) {
				// adapt second to the first
				second.setValidfrom(helper.getDateFrom(first.getValidto()));
			} else {
				// delete links on the right until first.getValidto() is inside one
				deleteSkippedLinksRight(first);
			}
		}
	}

	private void deleteSkippedLinksLeft(Historization second) {
		
		Date validfrom = second.getValidfrom();
		LinkedHistory linkHistory = linkedHistoryMap.get(second.getUID());
		LinkedHistory previous = linkHistory == null ? null : linkHistory.getPrevious();
		while (previous != null && !validfrom.after(previous.getHistorization().getValidfrom())) {
			links.remove(previous.getHistorization());
			linkedHistoryMap.remove(previous.getHistorization().getUID());
			if (previous.getPrevious() != null)
				previous.getPrevious().setNext(previous.getNext());
			previous.getNext().setPrevious(previous.getPrevious());
			previous = previous.getPrevious();
		}
		if (previous != null)
			previous.getHistorization().setValidto(helper.getDateTo(validfrom));
	}

	private void deleteSkippedLinksRight(Historization first) {
		
		Date validto = first.getValidto();
		LinkedHistory linkHistory = linkedHistoryMap.get(first.getUID());
		if (linkHistory == null)
			return;
		LinkedHistory next = linkHistory.getNext();
		while (next != null && !validto.before(next.getHistorization().getValidto())) {
			links.remove(next.getHistorization());
			linkedHistoryMap.remove(next.getHistorization().getUID());
			next.getPrevious().setNext(next.getNext());
			if (next.getNext() != null)
				next.getNext().setPrevious(next.getPrevious());
			next = next.getNext();
		}
		if (next != null)
			next.getHistorization().setValidfrom(helper.getDateFrom(validto));
	}
	
	private boolean hasChangedValidfrom(Historization hist) {
		
		if (hist.getUID() == null)
			return true;
		Historization histDB = fromDBMap.get(hist.getUID());
		if (histDB == null)
			return true;
		Date date1 = hist.getValidfrom();
		Date date2 = histDB.getValidfrom();
		return !date1.equals(date2);
	}

	private boolean hasChangedValidto(Historization hist) {

		if (hist.getUID() == null)
			return true;
		Historization histDB = fromDBMap.get(hist.getUID());
		if (histDB == null)
			return true;
		Date date1 = hist.getValidto();
		Date date2 = histDB.getValidto();
		return !date1.equals(date2);
	}
	
	private void initialize(List<? extends Historization> fromDB, Collection<? extends Historization> links) {
		
		linkedHistoryMap = new HashMap<String, LinkedHistory>();
		fromDBMap = new HashMap<String, Historization>();
		
		this.links = links;
		timeline = new ArrayList<Historization>(links);
		Collections.sort(timeline, timelineComparator);
		
		fillLinkedHistoryMap();
		
		for (Historization link : fromDB)
			fromDBMap.put(link.getUID(), link);
	}

	private void fillLinkedHistoryMap() {
		
		LinkedHistory prev, current;
		prev = new LinkedHistory(timeline.get(0));
		linkedHistoryMap.put(prev.getHistorization().getUID(), prev);
		
		for (int idx = 1; idx < timeline.size(); idx++) {
			current = new LinkedHistory(timeline.get(idx));
			prev.setNext(current);
			current.setPrevious(prev);
			linkedHistoryMap.put(current.getHistorization().getUID(), current);
			prev = current;
		}
	}

	/** {@inheritDoc} 
	 * @return */
	@Override
	public Set<? extends Historization> setBegin(Collection<? extends Historization> links, Date date) {
		return setBegin(links, date, true, true);
	}
	
	private Set<? extends Historization> setBegin(Collection<? extends Historization> links, Date date, boolean expand, boolean trimm) {
		
		if (CollectionUtils.isEmpty(links))
			return Collections.emptySet();
		
		Date validfrom = HistorizationHelper.generateUsefulValidFrom(date);
		
		timeline = new ArrayList<Historization>(links);
		Collections.sort(timeline, timelineComparator);
		
		Set<Historization> removed = new HashSet<Historization>();
		
		if (DateTools.datesAreEqual(validfrom, timeline.get(0).getValidfrom(), DatePrecision.SECOND)) { 
			return removed;
		} else if (DateTools.datesCompare(validfrom, timeline.get(0).getValidfrom(), DatePrecision.SECOND) < 0) { 
			if (expand)
				timeline.get(0).setValidfrom(validfrom);
		} else {
			if (trimm) {
				for (int i = 0; i < timeline.size(); i++) {
					Historization link = timeline.get(i);
					if (!validfrom.after(link.getValidto())) {
						link.setValidfrom(validfrom);
						return removed;
					}
					removed.add(link);
					links.remove(link);
				}
			}
		}
		return removed;
	}

	/** {@inheritDoc} 
	 * @return */
	@Override
	public Set<? extends Historization> setEnd(Collection<? extends Historization> links, Date date) {
		return setEnd(links, date, true, true);
	}
	
	private Set<? extends Historization> setEnd(Collection<? extends Historization> links, Date date, boolean expand, boolean trimm) {

		if (CollectionUtils.isEmpty(links))
			return Collections.emptySet();
		
		Date validto = HistorizationHelper.generateUsefulValidTo(date);
		
		timeline = new ArrayList<Historization>(links);
		Collections.sort(timeline, timelineComparator);
		
		Set<Historization> removed = new HashSet<Historization>();
		
		int size = timeline.size();
		if (validto.equals(timeline.get(size-1).getValidto())) {
			return removed;
		} else if (validto.after(timeline.get(size-1).getValidto())) {
			if (expand)
				timeline.get(size-1).setValidto(validto);
		} else {
			if (trimm) {
				for (int i = timeline.size() - 1; i >= 0; i--) {
					Historization link = timeline.get(i);
					if (!validto.before(link.getValidfrom())) {
						link.setValidto(validto);
						return removed;
					}
					removed.add(link);
					links.remove(link);
				}
			}
		}
		return removed;
	}
}
