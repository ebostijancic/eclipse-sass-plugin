package at.workflow.webdesk.po.impl.licence;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoPerson;

public class PoLicenceCheckHeuristic {
	
	public boolean fullLicenceCheckNeeded(PoAction action) {
		return action.isUniversallyAllowed()==true;
	}
	
	public boolean fullLicenceCheckNeeded(PoPerson person) {
		PoOrganisationService orgService = (PoOrganisationService) WebdeskApplicationContext.getBean("PoOrganisationService");
		return orgService.checkNeededForUpdatePerson(person);
	}
	

}
