package at.workflow.webdesk.po.impl.test;

import java.util.ArrayList;
import java.util.Arrays;
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
 * Test of Role Service around person competence.
 * 
 * @author sdzuban 08.08.2012
 */
public class WTestPoRoleServiceImplPersonCompetence extends AbstractPoRoleServiceAuthorityTest {

	
	// TODO deputy functionality must be tested yet.
	
	
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
		weiss = createPerson("weiss", "Florian", "Weiss", "fweiss", subDepartment);
		
		maier = createPerson("maier", "Tim", "Maier", "tmaier", subDepartment);
		mader = createPerson("mader", "Martin", "Mader", "mmader", subDepartment);

		// Role assignments
		roleService.assignRoleWithPersonCompetence(role, mueller, mader, null, null, 1);
		roleService.assignRoleWithPersonCompetence(role, mueller, weiss, DateTools.tomorrow(), null, 1);
		roleService.assignRoleWithPersonCompetence(role, topDepartment, maier, DateTools.tomorrow(), null, 1);
	}

	public void testRoleCompetencePerson() {
		
		PoRoleCompetenceClient rcClient = new PoRoleCompetenceClient();
		PoRoleCompetenceGroup rcGroup = new PoRoleCompetenceGroup();
		PoRoleCompetencePerson rcPerson = new PoRoleCompetencePerson();
		
		assertFalse(roleService.isRoleCompetencePerson(rcClient));
		assertFalse(roleService.isRoleCompetencePerson(rcGroup));
		assertTrue(roleService.isRoleCompetencePerson(rcPerson));
	}
	
	public void testFindRoleCompetencePerson() {
		
		List<PoRoleCompetencePerson> rcPersons = roleService.findRoleCompetencePerson(role); 
		assertNotNull(rcPersons);
		assertEquals(3, rcPersons.size()); // 1 for mader, 1 for weiss, 1 for maier
		List<PoPerson> persons = Arrays.asList(mader, weiss, maier);
		assertRoleCompetencePersons(rcPersons, persons);
	}
	
	public void testFindAuthorityForPerson() {
		
		List<PoPerson> result = roleService.findAuthority(maier, role);
		assertEquals(0, result.size());
		
		result = roleService.findAuthority(mader, role);
		assertEquals(1, result.size());
		assertTrue(result.contains(mueller));
		
		result = roleService.findAuthority(weiss, role, DateTools.tomorrow(), -1);
		assertEquals(1, result.size());
		assertTrue(result.contains(mueller));
	}
	
	public void testDeleteAndFlushRole() {

		roleService.deleteAndFlushRole(role);
		List<PoPerson> result = roleService.findAuthority(maier, role);
		assertEquals(0, result.size());
		
		result = roleService.findAuthority(weiss, role);
		assertEquals(0, result.size());
	}
	
	public void testFindCompetencePersonsOfGroup() {
		
		List<PoPerson> persons = roleService.findCompetencePersonsOfGroup(topDepartment, role, DateTools.now());
		assertNotNull(persons);
		assertEquals(0, persons.size());
		
		persons = roleService.findCompetencePersonsOfGroup(topDepartment, role, DateTools.tomorrow());
		assertNotNull(persons);
		assertEquals(1, persons.size());
		assertEquals(maier, persons.get(0));
		
	}
	
	public void testFindCompetencePersonsOfPerson() {
		
		List<PoPerson> persons = roleService.findCompetencePersonsOfPerson(maier, role, DateTools.now());
		assertNotNull(persons);
		assertEquals(0, persons.size());
		
		persons = roleService.findCompetencePersonsOfPerson(mueller, role, DateTools.now());
		assertNotNull(persons);
		assertEquals(1, persons.size());
		assertEquals(mader, persons.get(0));
		
		persons = roleService.findCompetencePersonsOfPerson(mueller, role, DateTools.tomorrow());
		assertNotNull(persons);
		assertEquals(2, persons.size());
		
	}
	
	public void testFindRoleCompetences() {

		// PoRoleCompetenceGroup is created with immediate validity 
		
		List<PoRoleCompetenceBase> result = roleService.findRoleCompetence(role);
		assertNotNull(result);
		assertEquals(3, result.size()); 
		List<PoPerson> persons = Arrays.asList(mader, weiss, maier);
		assertRoleCompetencePersons(result, persons);
		
		result = roleService.findRoleCompetences(role, DateTools.tomorrow());
		assertNotNull(result);
		assertEquals(3, result.size());
		assertRoleCompetencePersons(result, persons);
	}
	
	private void assertRoleCompetencePersons(List<? extends PoRoleCompetenceBase> competences, List<PoPerson> persons) {
		List<PoPerson> competencePersons = new ArrayList<PoPerson>();
		for (PoRoleCompetenceBase competence : competences) {
			assertTrue(roleService.isRoleCompetencePerson(competence));
			competencePersons.add(((PoRoleCompetencePerson) competence).getCompetence4Person());
		}
		assertEquals(persons.size(), competencePersons.size());
		for (PoPerson person : persons)
			assertTrue(competencePersons.contains(person));
	}
		
	public void testFindRoleHolderGroupWithCompetence4PersonF() {
		
		List<PoRoleHolderGroup> roleHolderGroups = roleService.findRoleHolderGroupWithCompetence4PersonF(role, topDepartment, weiss, DateTools.now());
		assertNotNull(roleHolderGroups);
		assertEquals(0, roleHolderGroups.size());
		
		roleHolderGroups = roleService.findRoleHolderGroupWithCompetence4PersonF(role, topDepartment, maier, DateTools.now());
		assertNotNull(roleHolderGroups);
		assertEquals(1, roleHolderGroups.size());
		assertEquals(topDepartment, roleHolderGroups.get(0).getGroup());
		
		roleHolderGroups = roleService.findRoleHolderGroupWithCompetence4PersonF(role, topDepartment, maier, DateTools.tomorrow());
		assertNotNull(roleHolderGroups);
		assertEquals(1, roleHolderGroups.size());
		assertEquals(topDepartment, roleHolderGroups.get(0).getGroup());
		
		roleHolderGroups = roleService.findRoleHolderGroupWithCompetence4PersonF(role, subDepartment, maier, DateTools.tomorrow());
		assertNotNull(roleHolderGroups);
		assertEquals(0, roleHolderGroups.size());
		
	}
	
	public void testFindRoleHolderPersonWithCompetence4Person() {
		
		List<PoRoleHolderPerson> roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4Person(role, maier, DateTools.now());
		assertNotNull(roleHolderPersons);
		assertEquals(0, roleHolderPersons.size());
		
		roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4Person(role, maier, DateTools.tomorrow());
		assertNotNull(roleHolderPersons);
		assertEquals(0, roleHolderPersons.size());
		
		roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4Person(role, mader, DateTools.now());
		assertNotNull(roleHolderPersons);
		assertEquals(1, roleHolderPersons.size());
		assertEquals(mueller, roleHolderPersons.get(0).getPerson());
		
		roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4Person(role, weiss, DateTools.now());
		assertNotNull(roleHolderPersons);
		assertEquals(0, roleHolderPersons.size());
		
		roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4Person(role, weiss, DateTools.tomorrow());
		assertNotNull(roleHolderPersons);
		assertEquals(1, roleHolderPersons.size());
		assertEquals(mueller, roleHolderPersons.get(0).getPerson());
		
	}
	
	public void testFindRoleHolderWithCompetenceForPerson() {
		
		List<PoRoleHolderLink> rhLinks = roleService.findRoleHolderWithCompetenceForPerson(maier, DateTools.now());
		assertNotNull(rhLinks);
		assertEquals(0, rhLinks.size());
		
		rhLinks = roleService.findRoleHolderWithCompetenceForPerson(maier, DateTools.tomorrow());
		assertNotNull(rhLinks);
		assertEquals(1, rhLinks.size());
		assertTrue(rhLinks.get(0) instanceof PoRoleHolderGroup);
		assertEquals(topDepartment, ((PoRoleHolderGroup) rhLinks.get(0)).getGroup());
		
		rhLinks = roleService.findRoleHolderWithCompetenceForPerson(mader, DateTools.now());
		assertNotNull(rhLinks);
		assertEquals(1, rhLinks.size());
		assertTrue(rhLinks.get(0) instanceof PoRoleHolderPerson);
		assertEquals(mueller, ((PoRoleHolderPerson) rhLinks.get(0)).getPerson());
		
		rhLinks = roleService.findRoleHolderWithCompetenceForPerson(weiss, DateTools.now());
		assertNotNull(rhLinks);
		assertEquals(0, rhLinks.size());
		
		rhLinks = roleService.findRoleHolderWithCompetenceForPerson(weiss, DateTools.tomorrow());
		assertNotNull(rhLinks);
		assertEquals(1, rhLinks.size());
		assertTrue(rhLinks.get(0) instanceof PoRoleHolderPerson);
		assertEquals(mueller, ((PoRoleHolderPerson) rhLinks.get(0)).getPerson());
		
	}
	
	public void testFindRoleHolderPersonWithCompetence4PersonF() {
		
		List<PoRoleHolderPerson> roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4PersonF(role, mueller, maier, DateTools.now());
		assertNotNull(roleHolderPersons);
		assertEquals(0, roleHolderPersons.size());
		
		roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4PersonF(role, mueller, mader, DateTools.now());
		assertNotNull(roleHolderPersons);
		assertEquals(1, roleHolderPersons.size());
		assertEquals(mueller, roleHolderPersons.get(0).getPerson());
		
		roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4PersonF(role, mueller, mader, DateTools.tomorrow());
		assertNotNull(roleHolderPersons);
		assertEquals(1, roleHolderPersons.size());
		assertEquals(mueller, roleHolderPersons.get(0).getPerson());
		
		roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4PersonF(role, mueller, weiss, DateTools.now());
		assertNotNull(roleHolderPersons);
		assertEquals(1, roleHolderPersons.size());
		
		roleHolderPersons = roleService.findRoleHolderPersonWithCompetence4PersonF(role, mueller, weiss, DateTools.tomorrow());
		assertNotNull(roleHolderPersons);
		assertEquals(1, roleHolderPersons.size());
		assertEquals(mueller, roleHolderPersons.get(0).getPerson());
		
	}
	
}
