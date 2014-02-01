package at.workflow.webdesk.po.timeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.ObjectUtils;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.HistorizationComparator;
import at.workflow.webdesk.tools.BeanReflectUtil;
import at.workflow.webdesk.tools.BeanReflectUtil.BeanProperty;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.comparator.PropertiesComparator;
import at.workflow.webdesk.tools.date.DateInterval;
import at.workflow.webdesk.tools.date.Interval;

/**
 * Filters linking objects into:
 * <ul><li>new ones</li>
 * <li>deleted ones</li>
 * <li>changed ones (any of the fields changed against version from DB)</li>
 * </ul>
 * 
 * @author sdzuban 15.05.2013
 */
public class TimelineFilter<LINK extends Historization> {
	
	/** DTO for transport of link with changed values together with same link with original values */
	public static class ChangedWithOriginal<LINK extends Historization> implements Comparable<ChangedWithOriginal<LINK>> {
		
		private LINK changed, original;

		public ChangedWithOriginal(LINK changed, LINK original) {
			assert changed != null && original != null && ObjectUtils.nullSafeEquals(original.getUID(), changed.getUID());
			this.changed = changed;
			this.original = original;
		}

		public LINK getChanged() {
			return changed;
		}
		public LINK getOriginal() {
			return original;
		}
		public Interval getOriginalValidity() {
			return new DateInterval(original.getValidfrom(), original.getValidto());
		}
		public Interval getChangedValidity() {
			return new DateInterval(changed.getValidfrom(), changed.getValidto());
		}

		/** {@inheritDoc} */
		@Override
		public int compareTo(ChangedWithOriginal<LINK> o) {
			return this.getOriginal().getValidfrom().compareTo(o.getOriginal().getValidfrom());
		}
	}
	
	public static class TimelineParts<LINK extends Historization > {
		
		private List<LINK> newOnes, changed, deleted, newChangedDeleted;
		private List<ChangedWithOriginal<LINK>> changedWithOriginals;

		/** DTO for transport of new links, changed links and deleted links */
		public TimelineParts(List<LINK> newOnes, List<LINK> deleted,
				List<ChangedWithOriginal<LINK>> changedWithOriginals) {
			super();
			this.newOnes = newOnes;
			this.deleted = deleted;
			this.changedWithOriginals = changedWithOriginals;
			changed = new ArrayList<LINK>();
			for (ChangedWithOriginal<LINK> changedWithOriginal : changedWithOriginals)
				changed.add(changedWithOriginal.getChanged());
			newChangedDeleted = new ArrayList<LINK>(newOnes);
			newChangedDeleted.addAll(changed);
			newChangedDeleted.addAll(deleted);
		}

		/** @return new links sorted according to validfrom */
		public List<LINK> getNewOnes() {
			return newOnes;
		}

		/** @return changed links sorted according to validfrom */
		public List<LINK> getChanged() {
			return changed;
		}

		/** @return deleted links sorted according to validfrom */
		public List<LINK> getDeleted() {
			return deleted;
		}

		/** @return new, changed and deleted links */
		public List<LINK> getNewChangedOrDeleted() {
			return newChangedDeleted;
		}

		/** @return tuples of original and of changed link */
		public List<ChangedWithOriginal<LINK>> getChangedWithOriginal() {
			return changedWithOriginals;
		}
		
	}

	private GenericDAO<LINK> dao;
	
	private List<LINK> newOnes = new ArrayList<LINK>();
	private List<LINK> deleted = new ArrayList<LINK>();
	/** changed need also original to show what has changed, how it changed */
	private List<ChangedWithOriginal<LINK>> changedWithOriginals = new ArrayList<ChangedWithOriginal<LINK>>();

	private Collection<LINK> prototypes;
	private Collection<LINK> links;
	
	// uid -> Historization
	private Map<String, LINK> prototypeMap = new HashMap<String, LINK>();
	private Map<String, LINK> linkMap = new HashMap<String, LINK>();
	
	/** dao is for evicting */
	public TimelineFilter(GenericDAO<LINK> dao) {
		this.dao = dao;
	}
	
	/**
	 * @param prototype links as stored in the database
	 * @param links links of current domain object
	 * @return links that are new, changed or deleted
	 */
	public TimelineParts<LINK> filter(Collection<LINK> prototypes, Collection<LINK> links) {
		
		this.prototypes = prototypes;
		this.links = links;

		prototypeMap = new HashMap<String, LINK>();
		linkMap = new HashMap<String, LINK>();
		
		if (prototypes.size() > 0 || links.size() > 0) {
			
			processCollections();
		
			extractChangedLinks();
			
			Comparator<Historization> comparator = new HistorizationComparator();
			Collections.sort(newOnes, comparator);
			Collections.sort(deleted, comparator);
			Collections.sort(changedWithOriginals);
		}		
		return new TimelineParts<LINK>(newOnes, deleted, changedWithOriginals);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "TimelineParts[newOnes:" + toString(newOnes) + ", changed:" + toString(changedWithOriginals) + ", deleted:" + toString(deleted)  + "]";
	}
	
	private String toString(Collection<?> c) {
		return (c == null ? "null" : c.size() + "");
	}

	private void extractChangedLinks() {
		
		List<BeanProperty> properties = getBeanProperties();
		
		if (properties == null)
			return;
		
		for (LINK link : linkMap.values()) {
			LINK prototype = prototypeMap.get(link.getUID());
			if ( ! PropertiesComparator.isEqual(link, prototype, properties)) {
				if (prototype != null)
					changedWithOriginals.add(new ChangedWithOriginal<LINK>(link, prototype));
				else // prototypes of new properties are null
					newOnes.add(link);
			}
		}
	}


	private List<BeanProperty> getBeanProperties() {
		
		Class<?> linkClass = null;
		if (links.size() > 0)
			linkClass = links.iterator().next().getClass();
		else if (prototypes.size() > 0)
			linkClass = prototypes.iterator().next().getClass();
		else
			return null;
		return BeanReflectUtil.properties(linkClass, new String[] { "UID" }, new String[] {});
	}

	
	private void processCollections() {
		
		for (LINK link : links)
			if (StringUtils.isNotBlank(link.getUID()))
				linkMap.put(link.getUID(), link);
			else
				newOnes.add(link);
		
		for (LINK prototype : prototypes)	{
			if (linkMap.containsKey(prototype.getUID())) {
				dao.evict(prototype);	// 8.8.2013: only objects read by finders, in this business call, will be evicted here 
				prototypeMap.put(prototype.getUID(), prototype);
			} else	{
				deleted.add(prototype);
			}
		}
	}

}
