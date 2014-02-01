package at.workflow.webdesk.po.impl.test;

import java.util.List;

import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleCompetenceBase;
import at.workflow.webdesk.po.model.PoRoleCompetenceClient;
import at.workflow.webdesk.po.model.PoRoleCompetenceGroup;
import at.workflow.webdesk.po.model.PoRoleCompetencePerson;
import at.workflow.webdesk.po.model.PoRoleHolderGroup;
import at.workflow.webdesk.po.model.PoRoleHolderLink;
import at.workflow.webdesk.po.model.PoRoleHolderPerson;
import at.workflow.webdesk.tools.date.DateTools;

/**
 * Test of Role Service around client competence.
 * 
 * @author sdzuban 08.08.2012
 */
public class WTestPoRoleServiceImplClientCompetence extends AbstractPoRoleServiceAuthorityTest {

	private PoClient client, otherClient;
	private PoRole role;
	private PoGroup subDepartment;
	private PoPerson maier;
	private PoPerson mader;
	private PoPerson weiss;
	private PoPerson huber;
	 
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
    	super.onSetUpAfterDataGeneration();
	    
    	client = topDepartment.getClient();
    	
    	otherClient = new PoClient();
    	otherClient.setName("otherClient");
    	organisationService.saveClient(otherClient);
    	
		role = new PoRole();
		role.setName("role");
		role.setOrgType( PoOrgStructure.STRUCTURE_TYPE_ORGANISATION_HIERARCHY );
		roleService.saveRole(role);

		// Gruppe anlegen
		subDepartment = createGroup("sub-department");
		
		// Personen anlegen
		weiss = createPerson("weiss", "Florian", "Weiss", "fweiss", topDepartment);
		
		maier = createPerson("maier", "Tim", "Maier", "tmaier", subDepartment);
		mader = createPerson("mader", "Martin", "Mader", "mmader", subDepartment);
		
		huber = createPerson("huber", "Hansi", "Huber", "hhuber", topDepartment);

		// Role assignments
		roleService.assignRoleWithClientCompetence(role, mueller, client, null, null, 1);
		roleService.assignRoleWithClientCompetence(role, subDepartment, client, null, null, 1);
		roleService.assignRoleWithClientCompetence(role, mueller, otherClient, DateTools.tomorrow(), null, 2);
		roleService.assignRoleWithClientCompetence(role, subDepartment, otherClient, DateTools.tomorrow(), null, 2);
	}

	public void testRoleCompetenceClient() {
		
		PoRoleCompetenceClient rcClient = new PoRoleCompetenceClient();
		PoRoleCompetenceGroup rcGroup = new PoRoleCompetenceGroup();
		PoRoleCompetencePerson rcPerson = new PoRoleCompetencePerson();
		
		assertTrue(roleService.isRoleCompetenceClient(rcClient));
		assertFalse(roleService.isRoleCompetenceClient(rcGroup));
		assertFalse(roleService.isRoleCompetenceClient(rcPerson));
	}
	
	public void testFindRoleCompetenceClient() {
		
		List<PoRoleCompetenceClient> rcClients = roleService.findRoleCompetenceClient(role); 
		assertNotNull(rcClients);
		assertEquals(2, rcClients.size());
	}
	
	public void testFindAuthorityForPersonWithFallBackToClient() {
		
		List<PoPerson> roleHolders = roleService.findAuthority(huber, role);
		assertTrue(roleHolders.size()==3);
		assertTrue(roleHolders.contains(maier));
		assertTrue(roleHolders.contains(mader));
		assertTrue(roleHolders.contains(mueller));
		
		roleService.assignRoleWithGroupCompetence(role, weiss, topDepartment, DateTools.today(), null, 1);
		roleHolders = roleService.findAuthority(huber, role);
		assertEquals(4, roleHolders.size());
		assertTrue(roleHolders.contains(weiss));
		
	}
	
	public void testFindAuthorityForClient() {
		
		List<PoPerson> result = roleService.findAuthority(client, role);
		assertEquals(3, result.size());
		assertTrue(result.contains(mueller));
		assertTrue(result.contains(maier));
		assertTrue(result.contains(mader));
		
		result = roleService.findAuthority(otherClient, role);
		assertEquals(0, result.size());
		
		result = roleService.findAuthority(client, role, DateTools.tomorrow(), -1);
		assertEquals(3, result.size());
		assertTrue(result.contains(mueller));
		assertTrue(result.contains(maier));
		assertTrue(result.contains(mader));
		
		result = roleService.findAuthority(otherClient, role, DateTools.tomorrow(), -1);
		assertEquals(3, result.size());
		assertTrue(result.contains(mueller));
		assertTrue(result.contains(maier));
		assertTrue(result.contains(mader));
	}
	
	public void testDeleteAndFlushRole() {

		roleService.deleteAndFlushRole(role);
		List<PoPerson> result = roleService.findAuthority(client, role);
		assertEquals(0, result.size());
	}
	
	public void testFindCompetenceClientsOfGroup() {
		
		List<PoClient> clients = roleService.findCompetenceClientsOfGroup(subDepartment, role, DateTools.now());
		assertNotNull(clients);
		assertEquals(1, clients.size());
		assertEquals(client, clients.get(0));
		
		clients = roleService.findCompetenceClientsOfGroup(subDepartment, role, DateTools.tomorrow());
		assertNotNull(clients);
		assertEquals(2, clients.size());
		assertTrue(clients.contains(client));
		assertTrue(clients.contains(otherClient));
		
		clients = roleService.findCompetenceClientsOfGroup(topDepartment, role, DateTools.now());
		assertNotNull(clients);
		assertEquals(0, clients.size());
		
		clients = roleService.findCompetenceClientsOfGroup(topDepartment, role, DateTools.tomorrow());
		assertNotNull(clients);
		assertEquals(0, clients.size());
		
	}
	
	public void testFindCompetenceClientsOfPerson() {
		
		List<PoClient> clients = roleService.findCompetenceClientsOfPerson(mueller, role, DateTools.now());
		assertNotNull(clients);
		assertEquals(1, clients.size());
		assertEquals(client, clients.get(0));
		
		clients = roleService.findCompetenceClientsOfPerson(mueller, role, DateTools.tomorrow());
		assertNotNull(clients);
		assertEquals(2, clients.size());
		assertTrue(clients.contains(client));
		assertTrue(clients.contains(otherClient));
		
		clients = roleService.findCompetenceClientsOfPerson(maier, role, DateTools.now());
		assertNotNull(clients);
		assertEquals(0, clients.size());
		
		clients = roleService.findCompetenceClientsOfPerson(maier, role, DateTools.tomorrow());
		assertNotNull(clients);
		assertEquals(0, clients.size());
		
	}
	
	public void testFindRoleCompetences() {

		// PoRoleCompetenceClient is created with immediate validity 
		
		List<PoRoleCompetenceBase> result = roleService.findRoleCompetence(role);
		assertNotNull(result);
		assertEquals(2, result.size()); // only 2 because it is today
		assertTrue(roleService.isRoleCompetenceClient(result.get(0)));
		assertEquals(client, ((PoRoleCompetenceClient) result.get(0)).getCompetence4Client());
		assertTrue(roleService.isRoleCompetenceClient(result.get(1)));
		assertEquals(otherClient, ((PoRoleCompetenceClient) result.get(1)).getCompetence4Client());
		
		result = roleService.findRoleCompetences(role, DateTools.tomorrow());
		assertNotNull(result);
		assertEquals(2, result.size());
		assertTrue(roleService.isRoleCompetenceClient(result.get(0)));
		assertEquals(client, ((PoRoleCompetenceClient) result.get(0)).getCompetence4Client());
		assertTrue(roleService.isRoleCompetenceClient(result.get(1)));
		assertEquals(otherClient, ((PoRoleCompetenceClient) result.get(1)).getCompetence4Client());
	}
	
	public void testFindRoleHolderGroups() {
		
		List<PoRoleHolderGroup> roleHolderGroups = roleService.findRoleHolderGroupWithCompetence4ClientF(role, subDepartment, client, DateTools.now());
		assertNotNull(roleHolderGroups);
		assertEquals(1, roleHolderGroups.size());
		assertEquals(subDepartment, roleHolderGroups.get(0).getGroup());
		
		roleHolderGroups = roleService.findRoleHolderGroupWithCompetence4ClientF(role, subDepartment, client, DateTools.tomorrow());
		assertNotNull(roleHolderGroups);
		assertEquals(1, roleHolderGroups.size());
		assertEquals(subDepartment, roleHolderGroups.get(0).getGroup());
		
		roleHolderGroups = roleService.findRoleHolderGroupWithCompetence4ClientF(role, subDepartment, otherClient, DateTools.now());
		assertNotNull(roleHolderGroups);
		assertEquals(1, roleHolderGroups.size());
		assertEquals(subDepartment, roleHolderGroups.get(0).getGroup());
		
		roleHolderGroups = roleService.findRoleHolderGroupWithCompetence4ClientF(role, subDepartment, otherClient, DateTools.tomorrow());
		assertNotNull(roleHolderGroups);
		assertEquals(1, roleHolderGroups.size());
		assertEquals(subDepartment, roleHolderGroups.get(0).getGroup());
		
	}
	
	public void testFindRoleHolderPersons() {
		
		List<PoRoleHolderPerson> roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4Client(role, client, DateTools.now());
		assertNotNull(roleHolderPersons);
		assertEquals(1, roleHolderPersons.size());
		assertEquals(mueller, roleHolderPersons.get(0).getPerson());
		
		roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4Client(role, client, DateTools.tomorrow());
		assertNotNull(roleHolderPersons);
		assertEquals(1, roleHolderPersons.size());
		assertEquals(mueller, roleHolderPersons.get(0).getPerson());
		
		roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4Client(role, otherClient, DateTools.now());
		assertNotNull(roleHolderPersons);
		assertEquals(0, roleHolderPersons.size());
		
		roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4Client(role, otherClient, DateTools.tomorrow());
		assertNotNull(roleHolderPersons);
		assertEquals(1, roleHolderPersons.size());
		assertEquals(mueller, roleHolderPersons.get(0).getPerson());
		
	}
	
	public void testFindRoleHolderWithCompetenceForClient() {
		
		List<PoRoleHolderLink> rhLinks = roleService.findRoleHolderWithCompetenceForClient(client, DateTools.now());
		assertNotNull(rhLinks);
		assertEquals(2, rhLinks.size());
		
		rhLinks = roleService.findRoleHolderWithCompetenceForClient(otherClient, DateTools.now());
		assertNotNull(rhLinks);
		assertEquals(0, rhLinks.size());
		
		rhLinks = roleService.findRoleHolderWithCompetenceForClient(otherClient, DateTools.tomorrow());
		assertNotNull(rhLinks);
		assertEquals(2, rhLinks.size());
	}
	
	public void testFindRoleHolderGroupWithCompetence4ClientF() {
		
		List<PoRoleHolderPerson> roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4ClientF(role, mueller, client, DateTools.now());
		assertNotNull(roleHolderPersons);
		assertEquals(1, roleHolderPersons.size());
		assertEquals(mueller, roleHolderPersons.get(0).getPerson());
		
		roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4ClientF(role, weiss, client, DateTools.now());
		assertNotNull(roleHolderPersons);
		assertEquals(0, roleHolderPersons.size());
		
		roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4ClientF(role, mueller, otherClient, DateTools.now());
		assertNotNull(roleHolderPersons);
		assertEquals(1, roleHolderPersons.size());
		assertEquals(mueller, roleHolderPersons.get(0).getPerson());
		
		roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4ClientF(role, weiss, otherClient, DateTools.now());
		assertNotNull(roleHolderPersons);
		assertEquals(0, roleHolderPersons.size());
		
	}
	
	public void testFindRoleHolderPersonWithCompetence4ClientF() {
		
		List<PoRoleHolderGroup> roleHolderGroups = roleService.findRoleHolderGroupWithCompetence4ClientF(role, subDepartment, client, DateTools.now());
		assertNotNull(roleHolderGroups);
		assertEquals(1, roleHolderGroups.size());
		assertEquals(subDepartment, roleHolderGroups.get(0).getGroup());
		
		roleHolderGroups = roleService.findRoleHolderGroupWithCompetence4ClientF(role, subDepartment, client, DateTools.tomorrow());
		assertNotNull(roleHolderGroups);
		assertEquals(1, roleHolderGroups.size());
		assertEquals(subDepartment, roleHolderGroups.get(0).getGroup());
		
		roleHolderGroups = roleService.findRoleHolderGroupWithCompetence4ClientF(role, subDepartment, otherClient, DateTools.now());
		assertNotNull(roleHolderGroups);
		assertEquals(1, roleHolderGroups.size());
		
		roleHolderGroups = roleService.findRoleHolderGroupWithCompetence4ClientF(role, subDepartment, otherClient, DateTools.tomorrow());
		assertNotNull(roleHolderGroups);
		assertEquals(1, roleHolderGroups.size());
		assertEquals(subDepartment, roleHolderGroups.get(0).getGroup());
		
	}
}
