package at.workflow.webdesk.po.timeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.date.Interval;

/**
 * This class serves extraction of LINKs according to validity.
 * <p/>
 * It can extract:
 * <ul><li>actual link, i.e. the one currently valid</li>
 * <li>past links, i.e. those valid no more</li>
 * <li>future links, i.e. thowe that will become valid in the future</li></ul>
 * <p/>
 * Past and future links are sorted as timeline from past to future.
 * 
 * @author sdzuban 05.09.2013
 * 
 * TODO: please share as much logic as possible with ActualLinkExtractorWithDiscriminator, at least Date comparison logic
 * TODO: this implements similar things as HistorizationHelper.getValidOrInvalidObjects(), we should try to keep this once-and-only-once!
 */
public class LinkExtractor<LINK extends Historization> {
	
	/**
	 * @param links unsorted collection of lazy loaded links
	 * @return refreshed current link
	 */
	public LINK getActualLink(Collection<LINK> links) {
		final Date now = DateTools.now();
		return getActualLink(links, now);
	}
	
	/**
	 * @param links unsorted collection of lazy loaded links
	 * @param date reference date
	 * @return refreshed link actual at date
	 */
	public LINK getActualLink(Collection<LINK> links, Date date) {
		for (LINK link : links) {
			if (link.getValidity().isValid(date))
				return link;
		}
		return null;
	}
	
	/**
	 * @param links unsorted collection of lazy loaded links
	 * @return sorted past links from past until present
	 */
	public List<LINK> getPastLinks(Collection<LINK> links) {
		final Date now = DateTools.now();
		return getPastLinks(links, now);
	}
	/**
	 * @param links unsorted collection of lazy loaded links
	 * @param date reference date
	 * @return sorted past links from past until date
	 */
	public List<LINK> getPastLinks(Collection<LINK> links, Date date) {

		List<LINK> sortedLinks = HistorizationTimelineUtils.getSortedLinkList(links);
		List<LINK> result = new ArrayList<LINK>();
		for (LINK link : sortedLinks) {
			if (link.getValidto().before(date))
				result.add(link);
		}
		return result;
	}

	/**
	 * @param links unsorted collection of lazy loaded links
	 * @return sorted future links after now to infinity
	 */
	public List<LINK> getFutureLinks(Collection<LINK> links) {
		final Date now = DateTools.now();
		return getFutureLinks(links, now);
	}
		
	/**
	 * @param links unsorted collection of lazy loaded links
	 * @param date reference date
	 * @return sorted future links after date to infinity
	 */
	public List<LINK> getFutureLinks(Collection<LINK> links, Date date) {
		
		List<LINK> sortedLinks = HistorizationTimelineUtils.getSortedLinkList(links);
		List<LINK> result = new ArrayList<LINK>();
		for (LINK link : sortedLinks) {
			if (link.getValidfrom().after(date))
				result.add(link);
		}
		return result;
	}
	
	/**
	 * @param links unsorted collection of lazy loaded links
	 * @param validity reference date interval
	 * @return sorted links valid (at least partly) in the validity interval
	 */
	public List<LINK> getLinksValidInInterval(Collection<LINK> links, Interval dateInterval) {
		
		if (dateInterval == null)
			throw new IllegalArgumentException("Date interval must be non null");
		
		List<LINK> sortedLinks = HistorizationTimelineUtils.getSortedLinkList(links);
		List<LINK> result = new ArrayList<LINK>();
		for (LINK link : sortedLinks) {
			Interval crossSection = dateInterval.crossSection(link.getValidity());
			if (crossSection.isPositive())
				result.add(link);
		}
		return result;
	}
}
