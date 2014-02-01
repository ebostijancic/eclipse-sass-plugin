package at.workflow.webdesk.po.timeline;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.BeanUtils;

import at.workflow.webdesk.HistoricizingDAO;
import at.workflow.webdesk.po.link.LinkRemover;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.timeline.TimelineFilter.ChangedWithOriginal;
import at.workflow.webdesk.po.timeline.TimelineFilter.TimelineParts;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.date.HistorizationHelper;

/**
 * This class serves saving, historicizing and deleting of links
 * via HistoricizingDAOImpl.
 * 
 * @author sdzuban 04.09.2013
 */
public class TimelineProcessor<LINK extends Historization> {

	private HistoricizingDAO<LINK> dao;
	private LinkRemover linkRemover;
	private TimelineParts<LINK> parts;

	/**
	 * @param dao
	 * @param linkRemover
	 */
	public TimelineProcessor(HistoricizingDAO<LINK> dao, LinkRemover linkRemover) {
		this.dao = dao;
		this.linkRemover = linkRemover;
	}

	/** 
	 * filters links according to their state
	 * @param fromDB
	 * @param links
	 * @return
	 */
	public TimelineParts<LINK> processLinks(List<LINK> fromDB, Collection<LINK> links) {
		
		TimelineFilter<LINK> filter = new TimelineFilter<LINK>(dao);
		parts = filter.filter(fromDB, links);
		return parts;
	}

	/**
	 * Filters links according to their state and saves new (contained in links but not in fromDB) 
	 * and changed ones (contained both in links and in fromDB but differing) and 
	 * historizes those deleted (present in fromDB but not in links).
	 * @param fromDB assignments read from database
	 * @param links assignments from domain object
	 */
	public void processAndSaveLinks(List<LINK> fromDB, Collection<LINK> links) {
		processAndSaveLinks(fromDB, links, Collections.<LINK>emptySet());
	}
		
	/**
	 * Filters links according to their state and saves new (contained in links but not in fromDB) 
	 * and changed ones (contained both in links and in fromDB but differing),
	 * deletes those in deletePhysically and 
	 * historizes those deleted (present in fromDB but not in links and not present in deletePhysically).
	 * @param fromDB assignments read from database
	 * @param links assignments from domain object
	 * @param deletePhysically
	 */
	public void processAndSaveLinks(List<LINK> fromDB, Collection<LINK> links, Set<LINK> deletePhysically) {
			
		parts = processLinks(fromDB, links);
		
		for (LINK link : parts.getNewOnes())
			dao.save(link);
		for (ChangedWithOriginal<LINK> changedAndOriginal : parts.getChangedWithOriginal()) {
			BeanUtils.copyProperties(changedAndOriginal.getChanged(), changedAndOriginal.getOriginal());
			dao.save(changedAndOriginal.getOriginal());
		}
		for (LINK link : parts.getDeleted()) {
			
			if (deletePhysically != null && deletePhysically.contains(link)) {
				
				link = dao.get(link.getUID()); // refresh necessary for remove() operation	// TODO: why is this necessary?
				linkRemover.remove(link);
				dao.delete(link);
				
			} else  if (HistorizationHelper.isValidOrFuture(link, DateTools.now())) {
				// reload the link
				// otherwise historicizing in the future
				// will produce an exception when deleting the link
				// while it is still in the collections of the partners
				link = dao.get(link.getUID());
				
				final boolean wasDeletedPhysically = dao.historicize(link);
				
				// unlink the link from partners
				// like personGroup.getGroup().getPersonGroups().remove(personGroup);
				if (wasDeletedPhysically) 
					linkRemover.remove(link);
			}
		}
	}

	/** @return parts containing new, changed and deleted links */
	public TimelineParts<LINK> getParts() {
		return parts;
	}

	/** @return the PoPerson that were changed, a set without duplicates. */
	public Set<PoPerson> getPoPersonsWithChanges() {
		return getEntitiesWithChanges("getPerson", new HashSet<PoPerson>());
	}

	/** @return unique objects that can be retrieved from entities by applying method with getterMethodName */
	protected <E extends PersistentObject> Set<E> getEntitiesWithChanges(String getterMethodName, Set<E> entities) {
		assert parts != null : "processLinks() must be called before this!";
		
		Method getter = null;
		for (LINK link : parts.getNewChangedOrDeleted()) {
			try {
				if (getter == null)
					getter = link.getClass().getMethod(getterMethodName);
	
				@SuppressWarnings("unchecked")
				final E entity = (E) getter.invoke(link);
				
				entities.add(entity);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return entities;
	}

}
