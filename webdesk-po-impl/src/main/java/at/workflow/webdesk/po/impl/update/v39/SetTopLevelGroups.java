package at.workflow.webdesk.po.impl.update.v39;

import java.util.Date;
import java.util.Iterator;

import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.daos.PoGeneralDAO;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;

/**
 * This update script sets the topLevel flag of all top level groups that have active childs.
 *
 * @author DI Harald Entner (hentner) 16.04.2008
 */
public class SetTopLevelGroups extends PoAbstractUpgradeScript {

	@Override
	public void execute() {
		PoOrganisationService orgService = (PoOrganisationService) getBean("PoOrganisationService");
		
		PoGeneralDAO gd = (PoGeneralDAO) getBean("PoGeneralDAO");
		
		
		String query ="select distinct pg.parentGroup from PoParentGroup as pg " +
				" where pg.parentGroup.validfrom<=? and pg.parentGroup.validto>? and " +
				"pg.parentGroup.orgStructure=? " +
				" and pg.parentGroup.UID not in " + 
				" (select pg2.childGroup.UID from PoParentGroup pg2 where pg2.validfrom<=? " +
				"and pg2.validto>? and pg.parentGroup.orgStructure=?)";
		
		for (PoOrgStructure os : orgService.loadAllOrgStructures()) {
			if (os.isHierarchy()) {
				// the toplevel flag is not set yet!
				Object[] keys = {new Date(), new Date(), os, new Date(), new Date(), os};
				Iterator tlGroupsI = gd.getElementsAsList(query, keys).iterator();
				while (tlGroupsI.hasNext()) {
					PoGroup tlGroup = (PoGroup) tlGroupsI.next();
					tlGroup.setTopLevel(true);
					orgService.saveGroup(tlGroup);
					logger.info("Set topLevel Flag of group " + tlGroup.getShortName() + "! [os=" + tlGroup.getOrgStructure().getName() +"]");
				}
			}
		}
	}
}
