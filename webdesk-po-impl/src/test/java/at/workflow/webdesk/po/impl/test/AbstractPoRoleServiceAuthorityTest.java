package at.workflow.webdesk.po.impl.test;

import java.util.List;

import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.PoRoleService;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

/**
 * Provides base data for authority search tests:
 * Mueller is chief in the top-department.
 * 
 * See Lotus ticket:
 * http://intranet/intern/ifwd_mgm.nsf/0/B0E4208EFFC1A41FC125788E0025D5E0?OpenDocument 
 * notes://Miraculix/intern/ifwd_mgm.nsf/0/B0E4208EFFC1A41FC125788E0025D5E0?EditDocument
 * 
 * @author ggruber 12.05.2011
 * @author fritzberger 12.05.2011
 */
public class AbstractPoRoleServiceAuthorityTest extends AbstractTransactionalSpringHsqlDbTestCase {
	
	protected static final String TEST_CLIENT = "UnitTestClient";
	
	// services
	protected PoOrganisationService organisationService;
	protected PoRoleService roleService;
	
	// data
	
	private PoClient client;
	private PoOrgStructure structure;
	
	protected PoPerson mueller;
	
	protected PoGroup topDepartment;
	
	protected PoRole chiefRole;
	
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
    	super.onSetUpAfterDataGeneration();
	    
	    roleService = (PoRoleService) getBean("PoRoleService");
	    
	    organisationService = (PoOrganisationService) getBean("PoOrganisationService");
	    
       // Mandant anlegen
		client = new PoClient();
		client.setName(TEST_CLIENT);
		client.setDescription("test client description");
		organisationService.saveClient(client);
		
		// OrgStructure anlegen
		structure = new PoOrgStructure();
		structure.setName("Organigramm");
		structure.setHierarchy(true);
		structure.setAllowOnlySingleGroupMembership(true);
		structure.setOrgType(PoConstants.STRUCTURE_TYPE_ORGANISATION_HIERARCHY);
		structure.setClient(client);
		organisationService.saveOrgStructure(structure);
		
		// Gruppe anlegen
		topDepartment = createGroup("top-department");
		
		// Rolle anlegen
		chiefRole = createHierarchicalRole("chief", client);
		
		// Person anlegen
		mueller = createPerson("mueller", "Heinz", "Müller", "hmueller", topDepartment);
		
		// Rolle zuordnen
		roleService.assignRoleWithGroupCompetence(chiefRole, mueller, topDepartment, null, null, 1);
	}


	 /**
	  * @param name name of person.
	  * @param client the client of person.
	  * @param doNotAllowSelfApproval can not approve itself.
	  * @param doNotAllowApprovalByDeputy deputy can not approve candidate.
	  * @return created person.
	  */
	protected PoRole createHierarchicalRole(String name, PoClient client) {
		PoRole role = new PoRole();
		role.setClient(client);
		role.setName(name);
		role.setDoNotAllowSelfApproval(true);
		role.setDoNotAllowApprovalByDeputy(true);
		role.setDirectionOfInheritance(PoConstants.SEARCH_DIRECTION_UP);
		role.setLevelsToSearch(Integer.MAX_VALUE);
		role.setOrgType(PoOrgStructure.STRUCTURE_TYPE_ORGANISATION_HIERARCHY);
		roleService.saveRole(role);
		return role;
	}

	protected PoGroup createGroup(String name) {
		PoGroup group = new PoGroup();
		group.setClient(client);
		group.setOrgStructure(structure);
		group.setShortName(name);
		group.setName(name);
		organisationService.saveGroup(group);
		return group;
	}
	
	protected PoPerson createPerson(String employeeId, String firstName, String lastName, String userName, PoGroup group) {
		PoPerson person = new PoPerson();
		// Link auf beiden Seiten
		person.setClient(group.getClient());
		person.setFirstName(firstName);
		person.setLastName(lastName);
		person.setEmployeeId(employeeId);
		person.setUserName(userName);
		organisationService.savePerson(person,group);
		return person;
	}
	
	public void testFindAuthorityForPersonMueller() {
		List<PoPerson> ret = roleService.findAuthority(mueller, chiefRole);
		assertTrue(ret.size() == 0);
	}
	
}
