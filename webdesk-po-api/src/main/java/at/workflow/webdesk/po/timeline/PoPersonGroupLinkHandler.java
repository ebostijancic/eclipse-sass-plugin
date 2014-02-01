package at.workflow.webdesk.po.timeline;

import at.workflow.webdesk.po.link.LinkHandler;
import at.workflow.webdesk.po.model.PoPersonGroup;
import at.workflow.webdesk.tools.api.Historization;

/**
 * This is class capable of removal of personGroups 
 * from collections of related person and group.
 * 
 * @author sdzuban 10.07.2013
 */
public class PoPersonGroupLinkHandler implements LinkHandler {
	
	/** {@inheritDoc} */
	@Override
	public void remove(Historization link) {
		
		((PoPersonGroup) link).getPerson().getMemberOfGroups().remove(link);
		((PoPersonGroup) link).getGroup().getPersonGroups().remove(link);
	}
	
	/** {@inheritDoc} */
	@Override
	public void addLinks(Historization relation) {
		 
		PoPersonGroup personGroup = (PoPersonGroup) relation;
		if (!personGroup.getPerson().getMemberOfGroups().contains(relation))
			personGroup.getPerson().getMemberOfGroups().add(personGroup);
		
		if (!personGroup.getGroup().getPersonGroups().contains(relation))
			personGroup.getGroup().getPersonGroups().add(personGroup);
	}

}
