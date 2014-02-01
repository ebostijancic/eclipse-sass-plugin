package at.workflow.webdesk.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import at.workflow.webdesk.HistoricizingDAO;
import at.workflow.webdesk.tools.api.Historization;

/**
 * Filters out deleted linking objects so that they can be historicized.
 * 
 * @author sdzuban 13.06.2013
 */
public class HistoricizingFilter<LINK extends Historization> {
	
	private HistoricizingDAO<LINK> dao;
	
	public HistoricizingFilter(HistoricizingDAO<LINK> dao) {
		this.dao = dao;
	}

	/**
	 * Filters out deleted linking objects and historicizes them.
	 * @param prototype links as stored in the database
	 * @param links links of current domain object
	 */
	public void historicizeDeleted(Collection<LINK> prototypes, Collection<LINK> links) {
		
		for (LINK deleted : extractDeleted(prototypes, links))
			dao.historicize(deleted);
	}

	
	/**
	 * Filters out deleted linking objects so that they can be historicized.
	 * @param prototype links as stored in the database
	 * @param links links of current domain object
	 * @return links that were deleted
	 */
	private List<LINK> extractDeleted(Collection<LINK> prototypes, Collection<LINK> links) {
		
		List<LINK> deleted = new ArrayList<LINK>();
		List<String> linkUids = new ArrayList<String>();
		
		if (prototypes.size() > 0 || links.size() > 0) {
			
			for (Historization link : links)
				if (StringUtils.isNotBlank(link.getUID()))
					linkUids.add(link.getUID());
			
			for (LINK prototype : prototypes)
				if (!linkUids.contains(prototype.getUID()))
					deleted.add(prototype);
		}		
		return deleted;
	}

}
