package at.workflow.webdesk.po.timeline;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.date.DateTools;

/**
 * This class serves extraction of LINKs according to validity and to discriminator value.
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
 * TODO: please share as much logic as possible with ActualLinkExtractor, at least Date comparison logic
 */
public class ActualLinkExtractorWithDiscriminator<LINK extends Historization> {
	
	private Method getDiscriminatorProperty;
	
	/**
	 * 
	 * @param dao for refreshing the link
	 * @param getDiscriminatorProperty for extracting discriminator property value
	 */
	public ActualLinkExtractorWithDiscriminator(Method getDiscriminatorProperty) {
		this.getDiscriminatorProperty = getDiscriminatorProperty;
	}

	
	/**
	 * @param links unsorted collection of lazy loaded links
	 * @param value discriminating value, typically of key-value-type
	 * @return refreshed current link
	 */
	public LINK getActualLink(Collection<LINK> links, String value) {
		
		final Date now = DateTools.now();
		for (LINK link : links) {
			try {
				if (StringUtils.equals(value, (String) getDiscriminatorProperty.invoke(link)) &&
						link.getValidity().isValid(now))
					return link;
			} catch (Exception e) {
				throw new PoRuntimeException("Error extracting discriminator property value " + e, e);
			}
		}
		return null;
	}
	
	/**
	 * @param links unsorted collection of lazy loaded links
	 * @param value discriminating value, typically of key-value-type
	 * @return sorted past links from past to present
	 */
	public List<LINK> getPastLinks(Collection<LINK> links, String value) {
		
		final Date now = DateTools.now();
		List<LINK> sortedLinks = HistorizationTimelineUtils.getSortedLinkList(links);
		List<LINK> result = new ArrayList<LINK>();
		for (LINK link : sortedLinks) {
			try {
				if (StringUtils.equals(value, (String) getDiscriminatorProperty.invoke(link)) &&
						link.getValidto().before(now))
					result.add(link);
			} catch (Exception e) {
				throw new PoRuntimeException("Error extracting discriminator property value " + e, e);
			}
		}
		return result;
	}
	
	/**
	 * @param links unsorted collection of lazy loaded links
	 * @param value discriminating value, typically of key-value-type
	 * @return sorted future links from now to infinity
	 */
	public List<LINK> getFutureLinks(Collection<LINK> links, String value) {
		
		final Date now = DateTools.now();
		List<LINK> sortedLinks = HistorizationTimelineUtils.getSortedLinkList(links);
		List<LINK> result = new ArrayList<LINK>();
		for (LINK link : sortedLinks) {
			try {
				if (StringUtils.equals(value, (String) getDiscriminatorProperty.invoke(link)) &&
						link.getValidfrom().after(now))
					result.add(link);
			} catch (Exception e) {
				throw new PoRuntimeException("Error extracting discriminator property value " + e, e);
			}
		}
		return result;
	}
}
