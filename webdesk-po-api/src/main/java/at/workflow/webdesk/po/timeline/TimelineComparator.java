package at.workflow.webdesk.po.timeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.comparator.PropertiesComparator;
import at.workflow.webdesk.tools.date.DateInterval;
import at.workflow.webdesk.tools.date.Interval;

/**
 * Compares two timelines for equality.
 * Second timeline is expected to be exactly the same as the complete first one.
 * 
 * First timeline does not need to be complete 
 * In this case only non-empty portions of timeline one are compared.
 * Second timeline is than expected to be exactly the same as first one 
 * only where the first one is defined. It can be anything else in between.
 * 
 * @author sdzuban 29.08.2013
 */
public class TimelineComparator<LINK extends Historization>  {
	
	/**
	 * Compares two timelines for non-equality. UID is not considered.
	 * <p/>
	 * It can operate in strict mode and in non-strict mode.
	 * <ul><li>In strict mode slight difference in timelines, 
	 * validfrom and validto dates included, is enough for non-equality.</li>
	 * <li>In non-strict mode timeline two is expected to differ from timeline one 
	 * where timeline one is defined.
	 * Validfrom and validto difference do not mean non-equality 
	 * as long as links2 cover whole links1.</li>
	 * 
	 * Timelines do not need to be continuous or of same time length. 
	 * 
	 * @param links1 links representing timeline one
	 * @param links2 links representing timeline two
	 * @param strict In strict mode slight difference in timelines means non-equality.
	 * In non-strict mode second timeline differring from timeline one where timeline one is defined means non-equality
	 */
	public boolean areNotEqual(Collection<LINK> links1, Collection<LINK> links2, boolean strict) {
		return ! areEqual(links1, links2, strict);
	}
	
	/**
	 * Compares two timelines for equality. UID is not considered.
	 * <p/>
	 * It can operate in strict mode and in non-strict mode.
	 * <ul><li>In strict mode timelines are expected to be exactly the same, 
	 * validfrom and validto dates included.</li>
	 * <li>In non-strict mode timeline two is expected to be exactly the same as the timeline one 
	 * where timeline one is defined and anything else where timeline one is not defined.
	 * Validfrom and validto date do not need to be same 
	 * as long as links2 cover whole links1.</li>
	 * 
	 * Timelines do not need to be continuous or of same time length. 
	 * 
	 * @param links1 links representing timeline one
	 * @param links2 links representing timeline two
	 * @param strict In strict mode second timeline is expected to be exactly the same as the complete first one.
	 * In non-strict mode second timeline must be the same as timeline one where timeline one is defined
	 */
	public boolean areEqual(Collection<LINK> links1, Collection<LINK> links2, boolean strict) {
		
		boolean noLinks1 = links1 == null || links1.isEmpty();
		boolean noLinks2 = links2 == null || links2.isEmpty();
		
		if (noLinks1 && noLinks2 || noLinks1)
			return true;
		
		if (noLinks2)
			return false;
		
		// sort links to timelines
		List<LINK> timeline1 = HistorizationTimelineUtils.<LINK>getSortedLinkList(links1);
		List<LINK> timeline2 = HistorizationTimelineUtils.<LINK>getSortedLinkList(links2);
		
		// link counts
		int linkCount1 = timeline1.size();
		int linkCount2 = timeline1.size();
		
		// determine validity
		Interval validity1 = new DateInterval(timeline1.get(0).getValidfrom(), timeline1.get(linkCount1 - 1).getValidto());
		Interval validity2 = new DateInterval(timeline2.get(0).getValidfrom(), timeline1.get(linkCount2 - 1).getValidto());
		
		// see if timelines are continuous
		boolean isFirstContinuous = HistorizationTimelineUtils.isTimelineContinuous(timeline1);
		boolean isSecondContinuous = HistorizationTimelineUtils.isTimelineContinuous(timeline2);
		
		if (strict) {

			if (linkCount1 != linkCount2 || !validity1.equals(validity2) || isFirstContinuous != isSecondContinuous)
				return false;
	
			for (int i = 0; i < linkCount1; i++) {
				LINK link1 = timeline1.get(i);
				LINK link2 = timeline2.get(i);
				if (PropertiesComparator.isNotEqual(link1, link2, new String[] {"UID"}))
					return false;
			}
			return true;
		}

		for(Entry<LINK, List<LINK>> entry : getCorrelatedLinks(timeline1, timeline2).entrySet()) {
			LINK link1 = entry.getKey();
			List<LINK> links12 = entry.getValue();
			
			Interval valid2 = getCompoundValidity(links12);
			if (valid2 == null || ! link1.getValidity().within(valid2))
				return false;
			for (LINK link2 : links12)
				if (PropertiesComparator.isNotEqual(link1, link2, new String[] {"UID","validfrom","validto"}))
					return false;
		}
		return true;
	}
		
	/** collects correlated links */
	private Map<LINK, List<LINK>> getCorrelatedLinks(Collection<LINK> links1, Collection<LINK> links2) {

		Map<LINK, List<LINK>> result = new HashMap<LINK, List<LINK>>();
		
		for (LINK link1 : links1) {
			Interval validity1 = link1.getValidity();
			if (validity1 == null)
				continue;
			for (LINK link2 : links2) {
				Interval validity2 = link2.getValidity();
				if (validity2 == null)
					continue;
				if (validity2.overlaps(validity1)) {
					List<LINK> links12 = result.get(link1);
					if (links12 == null) {
						links12 = new ArrayList<LINK>();
						result.put(link1, links12);
					}
					links12.add(link2);
				}
			}
		}
		return result;
	}
	
	/** links must be sorted according to validfrom */
	private Interval getCompoundValidity(Collection<LINK> links) {
		
		if (links == null || links.isEmpty())
			return null;
		
		Interval result = links.iterator().next().getValidity();
		for (LINK link : links)
			try {
				result = result.union(link.getValidity());
			} catch (Exception e) {
				return null;
			}
		return result;
	}
	
}
