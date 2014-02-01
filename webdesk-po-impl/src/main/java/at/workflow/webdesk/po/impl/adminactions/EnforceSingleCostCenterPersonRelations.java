package at.workflow.webdesk.po.impl.adminactions;

import java.util.ArrayList;
import java.util.List;

import at.workflow.webdesk.po.PoBeanPropertyService;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.impl.util.OrgAdminHelper;
import at.workflow.webdesk.po.model.PoBeanProperty;
import at.workflow.webdesk.po.model.PoBeanPropertyValue;

public class EnforceSingleCostCenterPersonRelations extends AbstractAdminAction {

	private static final String ALLOW_ONLY_SINGLE_COST_CENTER_ASSIGNMENT = "allowOnlySingleCostCenterAssignment";
	
	PoOrganisationService organisationService;
	PoBeanPropertyService beanPropertyService;
	
	@Override
	public void run() {
		PoBeanProperty bp = this.beanPropertyService.findBeanPropertyByKey("PoOrganisationServiceTarget", ALLOW_ONLY_SINGLE_COST_CENTER_ASSIGNMENT);
		if (bp!=null) {
			PoBeanPropertyValue value = (PoBeanPropertyValue) bp.getEntries().toArray()[0];
			value.setProperty("true");
			this.beanPropertyService.saveBeanProperty(bp);
			List<PoBeanProperty> bpToInject = new ArrayList<PoBeanProperty>();
			bpToInject.add(bp);
			this.beanPropertyService.injectAll(bpToInject);
		}
		
		
		OrgAdminHelper orgAdminHelper = new OrgAdminHelper();
		orgAdminHelper.setOrganisationService(this.organisationService);
		orgAdminHelper.enforceConsistentCostCenterGroupsForAll();
	}

	public void setOrganisationService(PoOrganisationService organisationService) {
		this.organisationService = organisationService;
	}

	public void setBeanPropertyService(PoBeanPropertyService beanPropertyService) {
		this.beanPropertyService = beanPropertyService;
	}
	

}
