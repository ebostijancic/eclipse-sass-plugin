package at.workflow.webdesk.po.impl.update.v110;

import java.util.Collection;

import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;


/**
 * 
 * @author sdzuban 27.01.2014
 *
 */
public class AbstractUpdateV110TestCase extends
		AbstractTransactionalSpringHsqlDbTestCase {

	private PoOrganisationService orgService;
	
	private PoClient client;
	private PoOrgStructure orgHierarchy;
	private PoOrgStructure orgLocations;
	private PoOrgStructure orgCostCenters;
	
	
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		super.onSetUpAfterDataGeneration();
		
		if (orgService == null) {
			orgService = (PoOrganisationService) getBean("PoOrganisationService");
			
			client = new PoClient();
			client.setName("client");
			orgService.saveClient(client);
			
			orgHierarchy = new PoOrgStructure();
			orgHierarchy.setClient(client);
			orgHierarchy.setAllowOnlySingleGroupMembership(true);
			orgHierarchy.setHierarchy(true);
			orgHierarchy.setName("orgHierarchy");
			orgHierarchy.setOrgType(PoOrgStructure.STRUCTURE_TYPE_ORGANISATION_HIERARCHY);
			orgService.saveOrgStructure(orgHierarchy);
			
			orgLocations = new PoOrgStructure();
			orgLocations.setClient(client);
			orgLocations.setAllowOnlySingleGroupMembership(true);
			orgLocations.setHierarchy(false);
			orgLocations.setName("orgLocations");
			orgLocations.setOrgType(PoOrgStructure.STRUCTURE_TYPE_LOCATIONS);
			orgService.saveOrgStructure(orgLocations);
			
			orgCostCenters = new PoOrgStructure();
			orgCostCenters.setClient(client);
			orgCostCenters.setAllowOnlySingleGroupMembership(true);
			orgCostCenters.setHierarchy(false);
			orgCostCenters.setName("orgCostCenters");
			orgCostCenters.setOrgType(PoOrgStructure.STRUCTURE_TYPE_COSTCENTERS);
			orgService.saveOrgStructure(orgCostCenters);

		}
	}

//	----------------------------- ASSERTIONS ----------------------------------
	
	protected void assertAllFalse(Collection<Boolean> values) {

		for (boolean value : values)
			assertFalse(value);
	}
	
//	---------------------------- PROTECTED METHODS ---------------------------------
	
	protected PoGroup getOrgUnit(int i, boolean topLevel) {
		
		PoGroup group = new PoGroup();
		group.setClient(client);
		group.setName("group" + i);
		group.setOrgStructure(orgHierarchy);
		group.setShortName("g" + i);
		group.setTopLevel(topLevel);
		orgService.saveGroup(group);
		return group;
	}

	protected PoGroup getLocation(int i) {
		
		PoGroup location = new PoGroup();
		location.setClient(client);
		location.setName("location" + i);
		location.setOrgStructure(orgLocations);
		location.setShortName("l" + i);
		orgService.saveGroup(location);
		return location;
	}

	protected PoGroup getCostCenter(int i) {
		
		PoGroup costCenter = new PoGroup();
		costCenter.setClient(client);
		costCenter.setName("costCenter" + i);
		costCenter.setOrgStructure(orgCostCenters);
		costCenter.setShortName("c" + i);
		orgService.saveGroup(costCenter);
		return costCenter;
	}
	
	protected PoPerson getPerson(int i, PoGroup orgUnit) {
		
		PoPerson person = new PoPerson();
		person.setClient(client);
		person.setFirstName("first" + i);
		person.setLastName("last" + i);
		person.setUserName("user" + i);
		orgService.savePerson(person, orgUnit);
		return person;
	}

	protected final PoOrganisationService getOrgService() {
		return orgService;
	}

	protected final PoClient getClient() {
		return client;
	}

	protected final PoOrgStructure getOrgHierarchy() {
		return orgHierarchy;
	}

	protected final PoOrgStructure getOrgLocations() {
		return orgLocations;
	}

	protected final PoOrgStructure getOrgCostCenters() {
		return orgCostCenters;
	}
	
}
