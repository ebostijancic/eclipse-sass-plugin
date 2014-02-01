package at.workflow.webdesk.po.impl.adminactions;

import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.impl.util.OrgAdminHelper;

public class EnforceConsistentPersonGroupOrgRelations extends AbstractAdminAction { 
	
	PoOrganisationService organisationService;
	
	@Override
	public void run() {
		OrgAdminHelper orgAdminHelper = new OrgAdminHelper();
		orgAdminHelper.setOrganisationService(organisationService);
		orgAdminHelper.enforceConsistentHierarchicalGroupsForAll();
	}

	public void setOrganisationService(PoOrganisationService organisationService) {
		this.organisationService = organisationService;
	}
	

}