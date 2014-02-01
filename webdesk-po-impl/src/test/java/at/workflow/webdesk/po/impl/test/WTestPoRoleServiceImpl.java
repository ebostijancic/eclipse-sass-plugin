package at.workflow.webdesk.po.impl.test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.PoRoleService;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleCompetenceAll;
import at.workflow.webdesk.po.model.PoRoleHolderGroup;
import at.workflow.webdesk.po.model.PoRoleHolderPerson;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

public class WTestPoRoleServiceImpl extends AbstractTransactionalSpringHsqlDbTestCase {

	private PoClient myClient;
	private PoPerson myPerson;
	private PoPerson myPerson2;
	private PoPerson myPerson3;
	private PoGroup myGroup;
	private PoGroup myGroup2;
	private PoGroup myGroup3;

	private PoGroup sub_myGroup;
	private PoGroup sub_sub_myGroup;
	private PoGroup sub_sub_sub_myGroup;

	private PoOrgStructure myStructure;
	private PoRole myRole;
	private PoRole myRole2;
	private Calendar future;

	int countRoles;
	int countRoleHolders;

	private PoOrganisationService poOrganisationService;
	private PoRoleService poRoleService;

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {

		super.onSetUpAfterDataGeneration();

		if (poRoleService == null)
			this.poRoleService = (PoRoleService) getBean("PoRoleService");

		if (poOrganisationService == null)
			this.poOrganisationService = (PoOrganisationService) getBean("PoOrganisationService");

		// Mandant anlegen
		myClient = new PoClient();
		myClient.setName("TestClient");
		myClient.setDescription("mydescription");
		poOrganisationService.saveClient(myClient);

		// OrgStructure anlegen
		// ####################		
		myStructure = new PoOrgStructure();
		myStructure.setName("Organigramm");
		myStructure.setHierarchy(true);
		myStructure.setAllowOnlySingleGroupMembership(true);
		myStructure.setOrgType(PoConstants.STRUCTURE_TYPE_ORGANISATION_HIERARCHY);
		myStructure.setClient(myClient);
		poOrganisationService.saveOrgStructure(myStructure);
		// Gruppe anlegen
		// ####################		

		myGroup = new PoGroup();
		myGroup.setClient(myClient);
		myGroup.setOrgStructure(myStructure);
		myGroup.setShortName("Abt4711");
		myGroup.setName("Abt4711");
		myGroup.setValidfrom(new Date());
		myGroup.setDescription("Marketing und Vertriebsunterstützung");
		poOrganisationService.saveGroup(myGroup);

		// Person anlegen
		// ####################
		myPerson = new PoPerson();
		// Link auf beiden Seiten
		myPerson.setClient(myClient);
		myPerson.setFirstName("Lisi");
		myPerson.setLastName("MÜLLER");
		myPerson.setEmployeeId("00004711");
		myPerson.setUserName("mul");
		poOrganisationService.savePerson(myPerson, myGroup);

		myPerson2 = new PoPerson();
		// Link auf beiden Seiten
		myPerson2.setClient(myClient);
		myPerson2.setFirstName("Harald");
		myPerson2.setLastName("Entner");
		myPerson2.setEmployeeId("00004712");
		myPerson2.setUserName("enh");
		poOrganisationService.savePerson(myPerson2, myGroup);

		myPerson3 = new PoPerson();
		// Link auf beiden Seiten
		myPerson3.setClient(myClient);
		myPerson3.setFirstName("Max");
		myPerson3.setLastName("Mustermann");
		myPerson3.setEmployeeId("00004713");
		myPerson3.setUserName("mum");
		poOrganisationService.savePerson(myPerson3, myGroup);

		myGroup2 = new PoGroup();
		myGroup2.setClient(myClient);
		myGroup2.setOrgStructure(myStructure);
		myGroup2.setShortName("testGroup2");
		myGroup2.setName("testGroup2");
		myGroup2.setValidfrom(new Date());
		poOrganisationService.saveGroup(myGroup2);

		myGroup3 = new PoGroup();
		myGroup3.setClient(myClient);
		myGroup3.setOrgStructure(myStructure);
		myGroup3.setName("testGroup3");
		myGroup3.setShortName("testGroup3");
		myGroup3.setValidfrom(new Date());
		poOrganisationService.saveGroup(myGroup3);

		sub_myGroup = new PoGroup();
		sub_myGroup.setClient(myClient);
		sub_myGroup.setOrgStructure(myStructure);
		sub_myGroup.setShortName("sub_myGroup");
		sub_myGroup.setName("sub_myGroup");
		sub_myGroup.setValidfrom(new Date());
		poOrganisationService.saveGroup(sub_myGroup);

		sub_sub_myGroup = new PoGroup();
		sub_sub_myGroup.setClient(myClient);
		sub_sub_myGroup.setOrgStructure(myStructure);
		sub_sub_myGroup.setShortName("sub_sub_myGroup");
		sub_sub_myGroup.setName("sub_sub_myGroup");
		sub_sub_myGroup.setValidfrom(new Date());
		poOrganisationService.saveGroup(sub_sub_myGroup);

		sub_sub_sub_myGroup = new PoGroup();
		sub_sub_sub_myGroup.setClient(myClient);
		sub_sub_sub_myGroup.setOrgStructure(myStructure);
		sub_sub_sub_myGroup.setShortName("sub_sub_sub_myGroup");
		sub_sub_sub_myGroup.setName("sub_sub_sub_myGroup");
		sub_sub_sub_myGroup.setValidfrom(new Date());
		sub_sub_sub_myGroup.setDescription("desc");
		poOrganisationService.saveGroup(sub_sub_sub_myGroup);

		poOrganisationService.setParentGroup(sub_myGroup, myGroup);
		poOrganisationService.setParentGroup(sub_sub_myGroup, sub_myGroup);
		poOrganisationService.setParentGroup(sub_sub_sub_myGroup, sub_sub_myGroup);

		countRoles = poRoleService.loadAllRoles().size();
		myRole = new PoRole();
		myRole.setClient(myClient);
		myRole.setDescription("My Role Description");
		myRole.setName("my role name");
		myRole.setOrgType(new Integer(myStructure.getOrgType()));
		myRole.setParticipantId("myRolePId1");
		myRole.setLevelsToSearch(99);
		myRole.setDirectionOfInheritance(PoConstants.SEARCH_DIRECTION_UP);
		poRoleService.saveRole(myRole);

		myRole2 = new PoRole();
		myRole2.setClient(myClient);
		myRole2.setDescription("My Role Description 2");
		myRole2.setName("my role name future");
		myRole2.setOrgType(new Integer(myStructure.getOrgType()));

		future = new GregorianCalendar();
		future.add(Calendar.MONTH, 1);
		myRole2.setValidfrom(future.getTime());
		myRole2.setValidto(new Date(DateTools.INFINITY_TIMEMILLIS));
		myRole.setParticipantId("myRolePId2");
		poRoleService.saveRole(myRole2);
	}

	public void testAssignRoles() {
		poRoleService.assignRole(myRole, myPerson, 1);
		poRoleService.assignRole(myRole, myPerson2, 2);
		PoRoleCompetenceAll rh = (PoRoleCompetenceAll) poRoleService.findRoleCompetence(myRole).get(0);
		assertTrue(poRoleService.findRoleCompetence(myRole).size() == 1 &&
					rh.getRoleHolderPersons().size() == 2); // unabhängig von bisher vorhandenen RoleHolders da Role neu angelegt
	}

	public void testLoadAllRolesWithClient() {
		assertEquals(poRoleService.loadAllRoles(myClient).size(), 1);
	}

	/* already tested at setUp
	public void testSaveRole() {
	}
	 */

	/** possible error despite working api would be two roles with the same name */
	public void testFindRoleByName() {
		assertEquals(poRoleService.findRoleByName("my role name").size(), 1);
	}

	public void testFindRoleByNameWithDate() {
		assertEquals(poRoleService.findRoleByName("my role name future", future.getTime()).size(), 1);
	}

	/** TODO implement this, will probably work! */
	public void testFindRoleByNameWithDateAndClient() {
	}

	public void testDeleteRole() {
		poRoleService.assignRole(myRole, myPerson, 1);
		poRoleService.assignRole(myRole, myPerson2, 2);

		poRoleService.deleteRole(myRole, true);
		assertEquals(poRoleService.findRoleByName("my role name").size(), 0);
	}

	// doesn't work - attempted to delete null, in tearDown function
	/*
	public void testDeleteRoleWithException() {
		poRoleService.assignRole(myRole,myPerson,1);
		//poRoleService.assignRole(myRole,myPerson2,2);
		try {
			poRoleService.deleteRole(myRole,false);
			fail("An Exception should be thrown, because Role Holder should exist!");
		} catch (PoRuntimeException re) {
			assertTrue(true);
		}
	}
	 */

	public void testAssignRole() {
		poRoleService.assignRole(myRole, myPerson, 1);
		PoRoleCompetenceAll rh = (PoRoleCompetenceAll) poRoleService.findRoleCompetence(myRole).get(0);
		assertNotNull(rh);
		assertEquals(myRole, rh.getRole());
		List<PoRoleHolderPerson> roleHolderPersons = rh.getRoleHolderPersons();
		assertNotNull(roleHolderPersons);
		assertEquals(1, roleHolderPersons.size());
		assertEquals(myPerson, roleHolderPersons.get(0).getPerson());
	}

	public void testDeleteRoleHolder() {
		poRoleService.assignRole(myRole, myPerson, 1);
		poRoleService.assignRole(myRole, myPerson2, 2);

		PoRoleCompetenceAll rh = (PoRoleCompetenceAll) poRoleService.findRoleCompetence(myRole).get(0);
		poRoleService.deleteRoleCompetence(rh);
		assertEquals(poRoleService.findRoleCompetence(myRole).size(), 0);
	}

	public void testAssignRoleToGroup() {
		poRoleService.assignRole(myRole, myGroup, 1);
		assertEquals(poRoleService.findRoleCompetence(myRole).size(), 1);
	}

	/*
	public void testAssignRoleToGroupDuplicate() {
		try {
			poRoleService.assignRole(myRole,myGroup,1);
			poRoleService.assignRole(myRole,myGroup,1);
			fail("An Exception should be thrown");
		} catch (PoRuntimeException e) {
			//exception = true;
			e.printStackTrace();
			assertTrue(true);
			
		}
	}
	*/

	public void testAssignRoleToGroup2() {
		poRoleService.assignRole(myRole, myGroup, 1);
		poRoleService.assignRole(myRole, myGroup2, 1);
		PoRoleCompetenceAll rh = (PoRoleCompetenceAll) poRoleService.findRoleCompetence(myRole).get(0);
		assertEquals(rh.getRoleHolderGroups().size(), 2);
	}

	public void testAssignRoleWithGroupCompetence() {
		poRoleService.assignRoleWithGroupCompetence(myRole, myPerson, myGroup, new Date(), null, 1);
		poRoleService.assignRoleWithGroupCompetence(myRole, myPerson2, myGroup, new Date(), null, 2);
		poRoleService.assignRoleWithGroupCompetence(myRole2, myPerson, myGroup, new Date(), null, 3);
		assertEquals(poRoleService.findRoleCompetenceGroup(myRole).size(), 1);
		assertEquals(poRoleService.findRoleCompetenceGroup(myRole2).size(), 1);
	}

	public void testAssignRolesWithGroupCompetence2GroupsPerRole() {
		poRoleService.assignRoleWithGroupCompetence(myRole, myPerson, myGroup, new Date(), null, 1);
		poRoleService.assignRoleWithGroupCompetence(myRole, myPerson, myGroup2, new Date(), null, 2);
		assertEquals(poRoleService.findRoleCompetenceGroup(myRole).size(), 2);
	}

	public void testAssignRolesWithGroupCompetenceGroup2Group() {
		poRoleService.assignRoleWithGroupCompetence(myRole, myGroup, myGroup3, new Date(), null, 1);
		poRoleService.assignRoleWithGroupCompetence(myRole, myGroup2, myGroup3, new Date(), null, 2);
		assertEquals(poRoleService.findRoleCompetenceGroup(myRole).size(), 1);
	}

	public void testAssignRoleWithPersonCompetence() {
		poRoleService.assignRoleWithPersonCompetence(myRole, myPerson, myPerson3, new Date(), null, 1);
		poRoleService.assignRoleWithPersonCompetence(myRole, myPerson2, myPerson3, new Date(), null, 2);
		assertEquals(poRoleService.findRoleCompetencePerson(myRole).size(), 1);
	}

	public void testAssignRoleWithPersonCompetencePerson2Person() {
		poRoleService.assignRoleWithPersonCompetence(myRole, myPerson, myPerson3, new Date(), null, 1);
		poRoleService.assignRoleWithPersonCompetence(myRole, myPerson2, myPerson3, new Date(), null, 2);
		assertEquals(poRoleService.findRoleCompetencePerson(myRole).size(), 1);
	}

	public void testAssignRoleWithPersonCompetenceGroupControlled() {
		poRoleService.assignRoleWithPersonCompetence(myRole, myGroup, myPerson, new Date(), null, 1);
		poRoleService.assignRoleWithPersonCompetence(myRole, myGroup2, myPerson, new Date(), null, 2);
		assertEquals(poRoleService.findRoleCompetencePerson(myRole).size(), 1);
	}

	public void testAssignRoleWithPersonCompetenceAndWithout() {
		poRoleService.assignRoleWithPersonCompetence(myRole, myPerson, myPerson2, new Date(), null, 1);
		poRoleService.assignRoleWithPersonCompetence(myRole, myPerson, myPerson3, new Date(), null, 2);
		poRoleService.assignRole(myRole, myPerson, 1);
		poRoleService.assignRole(myRole, myPerson2, 2);

		assertEquals(poRoleService.findRoleCompetence(myRole).size(), 3);
		assertEquals(poRoleService.findRoleCompetencePerson(myRole).size(), 2);
	}

	public void testFindRHPersonWithCompetence4Group() {
		System.out.println("test");
		poRoleService.assignRoleWithGroupCompetence(myRole, myPerson, myGroup, new Date(), null, 1);
		List<PoRoleHolderPerson> l = poRoleService.findRoleHolderPersonWithCompetence4Group(myRole, myGroup, new Date());
		PoRoleHolderPerson p = l.get(0);
		assertEquals(p.getPerson(), myPerson);
		assertEquals(l.size(), 1);
	}

	public void testGetAuthorityPersonRecursive2() {
		try {
			poRoleService.assignRoleWithGroupCompetence(myRole, myPerson, myGroup, new Date(), null, 1);
			poRoleService.assignRoleWithGroupCompetence(myRole, myGroup2, myGroup, new Date(), null, 2);
			poOrganisationService.linkPerson2Group(myPerson2, myGroup2);
			poOrganisationService.linkPerson2Group(myPerson3, sub_sub_sub_myGroup);
			assertEquals(poRoleService.findAuthority(myPerson3, myRole).size(), 2);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testGetAuthorityPersonRecursive3() {
		try {
			poRoleService.assignRoleWithGroupCompetence(myRole, myPerson, myGroup, new Date(), null, 1);
			//poRoleService.assignRoleWithGroupCompetence(myRole,myGroup2,myGroup, new Date(), null, 2);
			// link already exist poOrganisationService.linkPerson2Group(myPerson3,myGroup);
			assertEquals(poRoleService.findAuthority(myPerson3, myRole).size(), 1);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testGetAuthorityPersonRecursive4() {
		try {
			poRoleService.assignRole(myRole, myPerson, new Date(), null, 1);
			//poRoleService.assignRoleWithGroupCompetence(myRole,myGroup2,myGroup, new Date(), null, 2);
			// link already exist poOrganisationService.linkPerson2Group(myPerson3,myGroup);
			assertEquals(poRoleService.findAuthority(myPerson3, myRole).size(), 1);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testGetAuthorityPersonRecursive5() {
		try {
			poRoleService.assignRole(myRole, myPerson, new Date(), null, 1);
			poRoleService.assignRole(myRole, myGroup, new Date(), null, 1);
			//poRoleService.assignRoleWithGroupCompetence(myRole,myGroup2,myGroup, new Date(), null, 2);
			// link already exist poOrganisationService.linkPerson2Group(myPerson3,myGroup);
			assertEquals(poRoleService.findAuthority(myPerson3, myRole).size(), 3);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testRanking() {
		poRoleService.assignRoleWithGroupCompetence(myRole, myPerson, myGroup, new Date(), null, 1);
		poRoleService.assignRoleWithGroupCompetence(myRole, myPerson2, myGroup, new Date(), null, 2);
		List<PoRoleHolderPerson> l = poRoleService.findRoleHolderPersonWithCompetence4Group(myRole, myGroup, new Date());
		if (l.size() == 2) {
			assertEquals(l.get(0).getPerson(), myPerson);
			assertEquals(l.get(1).getPerson(), myPerson2);
		}
	}

	public void testfindCompetencePersonsOfPerson() {
		try {
			poRoleService.assignRoleWithPersonCompetence(myRole, myPerson, myPerson2, new Date(), null, 1);
			poRoleService.assignRoleWithPersonCompetence(myRole, myPerson, myPerson3, new Date(), null, 2);
			poRoleService.assignRole(myRole, myPerson, 1);
			poRoleService.assignRole(myRole, myPerson2, 2);
			assertEquals(poRoleService.findCompetencePersonsOfPerson(myPerson, myRole, new java.util.Date()).size(), 2);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testfindCompetenceGroupsOfPerson() {
		try {
			poRoleService.assignRoleWithGroupCompetence(myRole, myPerson, myGroup, new Date(), null, 1);
			poRoleService.assignRoleWithGroupCompetence(myRole, myPerson, myGroup2, new Date(), null, 2);
			poRoleService.assignRole(myRole, myPerson, 1);
			poRoleService.assignRole(myRole, myPerson2, 2);
			assertEquals(poRoleService.findCompetenceGroupsOfPerson(myPerson, myRole, new Date()).size(), 2);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test @Ignore("See WD-142")
	public void dotestFindRoleHolderGroupAll() throws Exception {
		final Date now = DateTools.now();
		
		List<PoRoleHolderGroup> roleHolders = poRoleService.findRoleHolderGroupF(myGroup, now);
		assertNotNull(roleHolders);
		assertEquals(0, roleHolders.size());

		roleHolders = poRoleService.findRoleHolderGroupAll(myGroup);
		assertNotNull(roleHolders);
		assertEquals(0, roleHolders.size());

		poRoleService.assignRole(myRole, myGroup, 1);

		roleHolders = poRoleService.findRoleHolderGroupF(myGroup, now);
		assertNotNull(roleHolders);
		assertEquals(1, roleHolders.size());
		assertEquals(myGroup, roleHolders.get(0).getGroup());
		assertEquals(myRole, roleHolders.get(0).getRoleCompetenceBase().getRole());

		roleHolders = poRoleService.findRoleHolderGroupAll(myGroup);
		assertNotNull(roleHolders);
		assertEquals(1, roleHolders.size());
		assertEquals(myGroup, roleHolders.get(0).getGroup());
		assertEquals(myRole, roleHolders.get(0).getRoleCompetenceBase().getRole());

		poRoleService.changeValidityRHGroupLink(roleHolders.get(0), null, now);

		roleHolders = poRoleService.findRoleHolderGroupF(myGroup, now);
		assertNotNull(roleHolders);
		assertEquals(0, roleHolders.size());

		roleHolders = poRoleService.findRoleHolderGroupAll(myGroup);
		assertNotNull(roleHolders);
		assertEquals(1, roleHolders.size());
		assertEquals(myGroup, roleHolders.get(0).getGroup());

		poRoleService.assignRole(myRole2, myGroup, 1);

		roleHolders = poRoleService.findRoleHolderGroupF(myGroup, now);
		assertNotNull(roleHolders);
		assertEquals(1, roleHolders.size());

		roleHolders = poRoleService.findRoleHolderGroupAll(myGroup);
		assertNotNull(roleHolders);
		assertEquals(2, roleHolders.size());

		poRoleService.assignRole(myRole, myGroup, null, DateTools.INFINITY, 2);

		// test of the ordering
		roleHolders = poRoleService.findRoleHolderGroupAll(myGroup);
		assertNotNull(roleHolders);
		assertEquals(3, roleHolders.size());
		// fri_2013-06-25: failed with "expected:<3> but was:<2>", fail was not reproducible on locale machine
		// fri_2013-07-02: failed again
		// fri_2013-07-04: failed again
		// fri_2013-07-05: failed again. Not reproducible in Eclipse.
		// fri_2013-07-08: observed again.
		// fri_2013-09-09: failed again
		// fri_2013-11-04: still fails
		// fri_2013-11-04: now its enough, see WD-142
		
		assertEquals(myRole, roleHolders.get(0).getRoleCompetenceBase().getRole());
		assertEquals(myRole, roleHolders.get(1).getRoleCompetenceBase().getRole());
		assertTrue(roleHolders.get(0).getValidfrom().after(roleHolders.get(1).getValidfrom()) || roleHolders.get(0).getValidfrom().equals(roleHolders.get(1).getValidfrom()));
		assertEquals(myRole2, roleHolders.get(2).getRoleCompetenceBase().getRole());
	}

}
