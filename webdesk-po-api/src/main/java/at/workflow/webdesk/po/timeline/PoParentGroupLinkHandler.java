package at.workflow.webdesk.po.timeline;

import at.workflow.webdesk.po.link.LinkHandler;
import at.workflow.webdesk.po.model.PoParentGroup;
import at.workflow.webdesk.tools.api.Historization;

/**
 * This is class capable of removal of parentGroups 
 * from collections of related groups.
 * 
 * @author sdzuban 10.07.2013
 */
public class PoParentGroupLinkHandler implements LinkHandler {
	
	/** {@inheritDoc} */
	@Override
	public void remove(Historization link) {
		
		((PoParentGroup) link).getParentGroup().getChildGroups().remove(link);
		((PoParentGroup) link).getChildGroup().getParentGroups().remove(link);
	}
	
	/** {@inheritDoc} */
	@Override
	public void addLinks(Historization relation) {
		 
		PoParentGroup personGroup = (PoParentGroup) relation;
		if (!personGroup.getParentGroup().getChildGroups().contains(relation))
			personGroup.getParentGroup().getChildGroups().add(personGroup);
		
		if (!personGroup.getChildGroup().getParentGroups().contains(relation))
			personGroup.getChildGroup().getParentGroups().add(personGroup);
	}

}
