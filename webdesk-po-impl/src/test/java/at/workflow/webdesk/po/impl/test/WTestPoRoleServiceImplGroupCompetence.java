package at.workflow.webdesk.po.impl.test;

import java.util.List;

import at.workflow.webdesk.po.model.PoGroup;
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
 * Test of Role Service around group competence.
 * 
 * @author sdzuban 08.08.2012
 */
public class WTestPoRoleServiceImplGroupCompetence extends AbstractPoRoleServiceAuthorityTest {

	
	// TODO deputy functionality and special hierarchy group properties must be tested yet.
	
	
	private PoRole role;
	private PoGroup subDepartment;
	private PoPerson maier;
	private PoPerson weiss;
	private PoPerson mader;
	 
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
    	super.onSetUpAfterDataGeneration();
	    
		role = new PoRole();
		role.setName("role");
		roleService.saveRole(role);

		// Gruppe anlegen
		subDepartment = createGroup("sub-department");
		
		// Personen anlegen
		weiss = createPerson("weiss", "Florian", "Weiss", "fweiss", topDepartment);
		
		maier = createPerson("maier", "Tim", "Maier", "tmaier", subDepartment);
		mader = createPerson("mader", "Martin", "Mader", "mmader", topDepartment);

		// Role assignments
		roleService.assignRoleWithGroupCompetence(role, mueller, subDepartment, null, null, 1);
		roleService.assignRoleWithGroupCompetence(role, topDepartment, subDepartment, DateTools.tomorrow(), null, 1);
		roleService.assignRoleWithGroupCompetence(role, weiss, topDepartment, DateTools.tomorrow(), null, 1);
	}

	public void testRoleCompetenceGroup() {
		
		PoRoleCompetenceClient rcClient = new PoRoleCompetenceClient();
		PoRoleCompetenceGroup rcGroup = new PoRoleCompetenceGroup();
		PoRoleCompetencePerson rcPerson = new PoRoleCompetencePerson();
		
		assertFalse(roleService.isRoleCompetenceGroup(rcClient));
		assertTrue(roleService.isRoleCompetenceGroup(rcGroup));
		assertFalse(roleService.isRoleCompetenceGroup(rcPerson));
	}
	
	public void testFindRoleCompetenceGroup() {
		
		List<PoRoleCompetenceGroup> rcGroups = roleService.findRoleCompetenceGroup(role); 
		assertNotNull(rcGroups);
		assertEquals(2, rcGroups.size()); // 1 for topDepartment, 1 for subDepartment
	}
	
	public void testFindAuthorityForGroup() {
		
		List<PoPerson> result = roleService.findAuthority(subDepartment, role);
		assertEquals(1, result.size());
		assertTrue(result.contains(mueller));
		
		result = roleService.findAuthority(topDepartment, role);
		assertEquals(0, result.size());
		
		result = roleService.findAuthority(subDepartment, role, DateTools.tomorrow(), -1);
		assertEquals(3, result.size());
		assertTrue(result.contains(mueller));
		assertTrue(result.contains(mader));
		assertTrue(result.contains(weiss));
		
		result = roleService.findAuthority(topDepartment, role, DateTools.tomorrow(), -1);
		assertEquals(1, result.size());
		assertTrue(result.contains(weiss));
	}
	
	public void testDeleteAndFlushRole() {

		roleService.deleteAndFlushRole(role);
		List<PoPerson> result = roleService.findAuthority(subDepartment, role);
		assertEquals(0, result.size());
	}
	
	public void testFindCompetenceGroupsOfGroup() {
		
		List<PoGroup> groups = roleService.findCompetenceGroupsOfGroup(topDepartment, role, DateTools.now());
		assertNotNull(groups);
		assertEquals(0, groups.size());
		
		groups = roleService.findCompetenceGroupsOfGroup(topDepartment, role, DateTools.tomorrow());
		assertNotNull(groups);
		assertEquals(1, groups.size());
		assertEquals(subDepartment, groups.get(0));
		
	}
	
	public void testFindCompetenceGroupsOfPerson() {
		
		List<PoGroup> groups = roleService.findCompetenceGroupsOfPerson(mueller, role, DateTools.now());
		assertNotNull(groups);
		assertEquals(1, groups.size());
		assertEquals(subDepartment, groups.get(0));
		
		groups = roleService.findCompetenceGroupsOfPerson(weiss, role, DateTools.now());
		assertNotNull(groups);
		assertEquals(0, groups.size());
		
		groups = roleService.findCompetenceGroupsOfPerson(weiss, role, DateTools.tomorrow());
		assertNotNull(groups);
		assertEquals(1, groups.size());
		assertEquals(topDepartment, groups.get(0));
		
	}
	
	public void testFindRoleCompetences() {

		// PoRoleCompetenceGroup is created with immediate validity 
		
		List<PoRoleCompetenceBase> result = roleService.findRoleCompetence(role);
		assertNotNull(result);
		assertEquals(2, result.size()); 
		assertTrue(roleService.isRoleCompetenceGroup(result.get(0)));
		assertEquals(subDepartment, ((PoRoleCompetenceGroup) result.get(0)).getCompetence4Group());
		assertTrue(roleService.isRoleCompetenceGroup(result.get(1)));
		assertEquals(topDepartment, ((PoRoleCompetenceGroup) result.get(1)).getCompetence4Group());
		
		result = roleService.findRoleCompetences(role, DateTools.tomorrow());
		assertNotNull(result);
		assertEquals(2, result.size());
		assertTrue(roleService.isRoleCompetenceGroup(result.get(0)));
		assertEquals(subDepartment, ((PoRoleCompetenceGroup) result.get(0)).getCompetence4Group());
		assertTrue(roleService.isRoleCompetenceGroup(result.get(1)));
		assertEquals(topDepartment, ((PoRoleCompetenceGroup) result.get(1)).getCompetence4Group());
	}
		
	public void testFindRoleHolderGroupWithCompetence4GroupF() {
		
		List<PoRoleHolderGroup> roleHolderGroups = roleService.findRoleHolderGroupWithCompetence4GroupF(role, topDepartment, subDepartment, DateTools.now());
		assertNotNull(roleHolderGroups);
		assertEquals(1, roleHolderGroups.size());
		assertEquals(topDepartment, roleHolderGroups.get(0).getGroup());
		
		roleHolderGroups = roleService.findRoleHolderGroupWithCompetence4GroupF(role, topDepartment, subDepartment, DateTools.tomorrow());
		assertNotNull(roleHolderGroups);
		assertEquals(1, roleHolderGroups.size());
		assertEquals(topDepartment, roleHolderGroups.get(0).getGroup());
		
	}
	
	public void testFindRoleHolderPersonWithCompetence4Group() {
		
		List<PoRoleHolderPerson> roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4Group(role, subDepartment, DateTools.now());
		assertNotNull(roleHolderPersons);
		assertEquals(1, roleHolderPersons.size());
		assertEquals(mueller, roleHolderPersons.get(0).getPerson());
		
		roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4Group(role, subDepartment, DateTools.tomorrow());
		assertNotNull(roleHolderPersons);
		assertEquals(1, roleHolderPersons.size());
		assertEquals(mueller, roleHolderPersons.get(0).getPerson());
		
		roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4Group(role, topDepartment, DateTools.now());
		assertNotNull(roleHolderPersons);
		assertEquals(0, roleHolderPersons.size());
		
		roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4Group(role, topDepartment, DateTools.tomorrow());
		assertNotNull(roleHolderPersons);
		assertEquals(1, roleHolderPersons.size());
		assertEquals(weiss, roleHolderPersons.get(0).getPerson());
		
	}
	
	public void testFindRoleHolderWithCompetenceForGroup() {
		
		List<PoRoleHolderLink> rhLinks = roleService.findRoleHolderWithCompetenceForGroup(subDepartment, DateTools.now());
		assertNotNull(rhLinks);
		assertEquals(1, rhLinks.size());
		assertTrue(rhLinks.get(0) instanceof PoRoleHolderPerson);
		
		rhLinks = roleService.findRoleHolderWithCompetenceForGroup(subDepartment, DateTools.tomorrow());
		assertNotNull(rhLinks);
		assertEquals(2, rhLinks.size());
	}
	
	public void testFindRoleHolderPersonWithCompetence4GroupF() {
		
		List<PoRoleHolderPerson> roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4GroupF(role, mueller, subDepartment, DateTools.now());
		assertNotNull(roleHolderPersons);
		assertEquals(1, roleHolderPersons.size());
		assertEquals(mueller, roleHolderPersons.get(0).getPerson());
		
		roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4GroupF(role, weiss, topDepartment, DateTools.now());
		assertNotNull(roleHolderPersons);
		assertEquals(1, roleHolderPersons.size());
		assertEquals(weiss, roleHolderPersons.get(0).getPerson());
		
		roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4GroupF(role, maier, topDepartment, DateTools.now());
		assertNotNull(roleHolderPersons);
		assertEquals(0, roleHolderPersons.size());
		
		roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4GroupF(role, maier, subDepartment, DateTools.now());
		assertNotNull(roleHolderPersons);
		assertEquals(0, roleHolderPersons.size());
		
	}
	
	public void testFindRoleHolderGroupWithCompetence4Group() {
		
		List<PoRoleHolderGroup> roleHolderGroups = roleService.findRoleHolderGroupWithCompetence4Group(role, subDepartment, DateTools.now());
		assertNotNull(roleHolderGroups);
		assertEquals(0, roleHolderGroups.size());
		
		roleHolderGroups = roleService.findRoleHolderGroupWithCompetence4Group(role, subDepartment, DateTools.tomorrow());
		assertNotNull(roleHolderGroups);
		assertEquals(1, roleHolderGroups.size());
		assertEquals(topDepartment, roleHolderGroups.get(0).getGroup());
	}
}
