package at.workflow.webdesk.po.impl.test;

import java.util.Calendar;
import java.util.Date;
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

public class WTestActiveUser extends AbstractTransactionalSpringHsqlDbTestCase {

	private PoRoleService roleService;
	private PoOrganisationService organisationService;
	
	private PoClient client;
	private PoOrgStructure orgStructure;
	private PoGroup group;
	private PoPerson person;
	private String personUID;
	private PoRole role;
	
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		
		organisationService = (PoOrganisationService) getBean("PoOrganisationService");
		roleService = (PoRoleService) getBean("PoRoleService");
		
		createTestData();
	}

	private void createTestData() {
		Calendar validto = Calendar.getInstance();
		validto.set(Calendar.YEAR, 2060);
		validto.set(Calendar.MONTH, 4);
		validto.set(Calendar.DAY_OF_MONTH, 9);

		try {
			client = new PoClient();
			client.setName("TestClient");
			client.setShortName("TC");
			organisationService.saveClient(client);
			
			orgStructure = new PoOrgStructure();
			orgStructure.setClient(client);
			orgStructure.setHierarchy(true);
			orgStructure.setAllowOnlySingleGroupMembership(true);
			orgStructure.setName("TestHierarchy");
			orgStructure.setOrgType(PoConstants.STRUCTURE_TYPE_ORGANISATION_HIERARCHY);
			organisationService.saveOrgStructure(orgStructure);
			
			group = new PoGroup();
			group.setShortName("TestTL");
			group.setName("Test Top Level Group");
			group.setOrgStructure(orgStructure);
			group.setClient(client);
			organisationService.saveGroup(group);
			
			
			role = new PoRole();
			role.setClient(client);
			role.setDirectionOfInheritance(PoConstants.SEARCH_DIRECTION_NONE);
			role.setName("TestRole");
			role.setOrgType(new Integer(1));
			role.setParticipantId("TestRole");
			role.setValidfrom(new Date());
			role.setValidto(validto.getTime());
			roleService.saveRole(role);
			
			person = new PoPerson();
			person.setClient(client);
			person.setFirstName("Top");
			person.setLastName("Level");
			person.setEmployeeId("1");
			person.setUserName("toplevel");
			organisationService.savePerson(person, group);
			personUID = person.getUID();
		
			roleService.assignRole(role, person, new Date(), validto.getTime(), 1);
			
		} catch (Exception e) {
			fail("Exception " + e);
		}
	}
	
	
	public void testActiveUser() throws Exception {
		person = organisationService.getPerson(personUID);
		
		assertNotNull(person);
		assertTrue(person.isActiveUser());
		
		List<PoPerson> persons = roleService.findAuthority(person, role, new Date(), 0);
		assertNotNull(persons);
		assertTrue(persons.contains(person));
	}
	
	public void testInactiveUser() throws Exception {
		person = organisationService.getPerson(personUID);
		person.setActiveUser(false);
		organisationService.updatePerson(person);
		
		person = organisationService.getPerson(personUID);
		
		assertNotNull(person);
		assertFalse(person.isActiveUser());
		
		List<PoPerson> persons = roleService.findAuthority(person, role, new Date(), 0);
		assertNotNull(persons);
		assertFalse(persons.contains(person));
	}

}
