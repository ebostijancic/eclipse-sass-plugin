package at.workflow.webdesk.po.impl.test;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.PoRoleService;
import at.workflow.webdesk.po.impl.PoRoleServiceImpl;
import at.workflow.webdesk.po.impl.test.helper.PoDeputyTestMergeHelper;
import at.workflow.webdesk.po.impl.test.helper.PoHelper;
import at.workflow.webdesk.po.impl.test.helper.PoRoleHolderGroupCompetenceAllHelper;
import at.workflow.webdesk.po.impl.test.helper.PoRoleHolderGroupCompetenceGroupHelper;
import at.workflow.webdesk.po.impl.test.helper.PoRoleHolderGroupCompetencePersonHelper;
import at.workflow.webdesk.po.impl.test.helper.PoRoleHolderPersonCompetenceAllHelper;
import at.workflow.webdesk.po.impl.test.helper.PoRoleHolderPersonCompetenceGroupHelper;
import at.workflow.webdesk.po.impl.test.helper.PoRoleHolderPersonCompetencePersonHelper;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleDeputy;
import at.workflow.webdesk.po.model.PoRoleHolderLink;
import at.workflow.webdesk.po.model.PoRoleHolderPerson;
import at.workflow.webdesk.tools.IfDate;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

/**
 * <p>
 * These Testcases perform a validity check of following
 * parts of the <code>PoRoleService</code>:
 * <ul>
 * 	<li>Assignment of a <code>PoRole</code> instances on different dates with 
 * 		different or same <code>PoRoleCompetenceBase</code> objects. Much emphasis was placed on 
 * 		the validation check of side effects that occurs when an assignment is 
 * 		commited. For instance, when an newly inserted and not unusual more 
 * 		than one <code>PoRoleHolderPerson</code> objects overlap, the system tries
 * 		to merge these, if and only if the same <code>PoRoleCompetenceBase</code>
 * 		object is linked with the <code>PoRoleHolderPerson</code> objects.
 * 	<li>Assignments of <code>PoRoleDeputy</code> objects. But not only the assignment, 
 * 		also the generation of <code>PoRoleHolderPerson</code> objects. 
 * 	<li>As a side effect, various <code>PoRoleService</code> and <code>PoRoleDAO</code>
 * 		functions are validated, worth to mention: <code>findAuthority(..)</code>
 * </ul>
 * </p>
 * <b>Execute this SQL script when deletion does not work:</b>
 * <pre>
	delete from poroleholderPerson
	delete from poroleholder
	delete from poroledeputy
	delete from porole
	delete from popersongroup
	delete from poperson
	delete from pogroup
	delete from poorgstructure
	delete from PoClient
 * </pre>
 * 
 * Created on 19.05.2005
 * created at:       04.03.2008<br>
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 */
public class WTestPoRoleDeputy extends AbstractTransactionalSpringHsqlDbTestCase {

	private PoClient client;
	private PoOrgStructure orgStructure;
	private PoGroup tlGroup;
	private PoGroup tlGroup2;
	private PoRole role;

	private PoPerson tlPerson;
	private PoPerson tlPerson1;

	private PoPerson slPerson;
	private PoPerson slPerson2;

	private PoOrganisationService poOrganisationService;
	private PoRoleService poRoleService;

	private IfDate validto = new IfDate().getLastMomentOfDay();

	private IfDate vf1 = new IfDate().getDateOnly();
	private IfDate vt1 = new IfDate().getLastMomentOfDay();

	private IfDate vf2 = new IfDate().getDateOnly();
	private IfDate vt2 = new IfDate().getLastMomentOfDay();

	private IfDate vf3 = new IfDate().getDateOnly();
	private IfDate vt3 = new IfDate().getLastMomentOfDay();

	private IfDate vf4 = new IfDate().getDateOnly();
	private IfDate vt4 = new IfDate().getLastMomentOfDay();

	private IfDate vf5 = new IfDate().getDateOnly();
	private IfDate vt5 = new IfDate().getLastMomentOfDay();

	private IfDate firstDate = new IfDate().getDateOnly();
	private IfDate lastDate = new IfDate().getLastMomentOfDay();

	private IfDate inside1vf = new IfDate();
	private IfDate inside1vt = new IfDate();

	private IfDate inside2vf = new IfDate();
	private IfDate inside2vt = new IfDate();
	private IfDate inside3vf = new IfDate();
	private IfDate inside3vt = new IfDate();

	public static void main(String[] args) {
		junit.textui.TestRunner.run(WTestPoRoleDeputy.class);
	}

	public WTestPoRoleDeputy() {
		firstDate.add(Calendar.DAY_OF_MONTH, 1);

		vf1.add(Calendar.DAY_OF_MONTH, 5);
		inside1vf.add(Calendar.DAY_OF_MONTH, 6);
		inside1vf = inside1vf.getDateOnly();
		inside1vt.add(Calendar.DAY_OF_MONTH, 6);
		inside1vt = inside1vt.getLastMomentOfDay();
		vt1.add(Calendar.DAY_OF_MONTH, 7);

		vf2.add(Calendar.DAY_OF_MONTH, 10);
		inside2vf.add(Calendar.DAY_OF_MONTH, 14);
		inside2vf = inside2vf.getDateOnly();
		inside2vt.add(Calendar.DAY_OF_MONTH, 14);
		inside2vt = inside2vt.getLastMomentOfDay();
		vt2.add(Calendar.DAY_OF_MONTH, 17);

		vf3.add(Calendar.DAY_OF_MONTH, 20);
		inside3vf.add(Calendar.DAY_OF_MONTH, 24);
		inside3vf = inside3vf.getDateOnly();
		inside3vt.add(Calendar.DAY_OF_MONTH, 24);
		inside3vt = inside3vt.getLastMomentOfDay();
		vt3.add(Calendar.DAY_OF_MONTH, 27);

		vf4.add(Calendar.DAY_OF_MONTH, 30);
		vt4.add(Calendar.DAY_OF_MONTH, 37);

		vf5.add(Calendar.DAY_OF_MONTH, 40);
		vt5.add(Calendar.DAY_OF_MONTH, 47);

		lastDate.add(Calendar.DAY_OF_MONTH, 100);
	}
	
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		validto.set(Calendar.YEAR, 2060);
		validto.set(Calendar.MONTH, 4);
		validto.set(Calendar.DAY_OF_MONTH, 9);
		
		poRoleService = (PoRoleService) getBean("PoRoleService");
		poOrganisationService = (PoOrganisationService) getBean("PoOrganisationService");
		
		createTestData();
	}


	private void createTestData() {
		try {
			client = new PoClient();
			client.setName("TestClient");
			client.setShortName("TC");
			poOrganisationService.saveClient(client);

			orgStructure = new PoOrgStructure();
			orgStructure.setClient(client);
			orgStructure.setHierarchy(true);
			orgStructure.setAllowOnlySingleGroupMembership(true);
			orgStructure.setName("TestHierarchy");
			orgStructure.setOrgType(PoConstants.STRUCTURE_TYPE_ORGANISATION_HIERARCHY);
			poOrganisationService.saveOrgStructure(orgStructure);

			tlGroup = new PoGroup();
			tlGroup.setShortName("TestTL");
			tlGroup.setName("Test Top Level Group");
			tlGroup.setOrgStructure(orgStructure);
			tlGroup.setClient(client);
			poOrganisationService.saveGroup(tlGroup);

			tlGroup2 = new PoGroup();
			tlGroup2.setShortName("TestTL2");
			tlGroup2.setName("Test Top Level Group2");
			tlGroup2.setOrgStructure(orgStructure);
			tlGroup2.setClient(client);
			poOrganisationService.saveGroup(tlGroup2);

			role = new PoRole();
			role.setClient(client);
			role.setDirectionOfInheritance(PoConstants.SEARCH_DIRECTION_NONE);
			role.setName("TestRole");
			role.setOrgType(new Integer(1));

			role.setParticipantId("TestRole");
			role.setValidfrom(new Date());

			role.setValidto(validto);
			poRoleService.saveRole(role);

			tlPerson = new PoPerson();
			tlPerson.setClient(client);
			tlPerson.setFirstName("Top");
			tlPerson.setLastName("Level");
			tlPerson.setEmployeeId("1");
			//tlPerson.setPassword("pw");
			tlPerson.setUserName("toplevel");
			poOrganisationService.savePerson(tlPerson, tlGroup);

			tlPerson1 = new PoPerson();
			tlPerson1.setClient(client);
			tlPerson1.setFirstName("Top1");
			tlPerson1.setLastName("Level1");
			tlPerson1.setEmployeeId("4");
			//tlPerson1.setPassword("pw1");
			tlPerson1.setUserName("toplevel1");
			poOrganisationService.savePerson(tlPerson1, tlGroup);

			slPerson = new PoPerson();
			slPerson.setClient(client);
			slPerson.setFirstName("Sub");
			slPerson.setLastName("Level");
			slPerson.setEmployeeId("2");
			//slPerson.setPassword("pw");
			slPerson.setUserName("sublevel");
			poOrganisationService.savePerson(slPerson, tlGroup);

			slPerson2 = new PoPerson();
			slPerson2.setClient(client);
			slPerson2.setFirstName("Sub2");
			slPerson2.setLastName("Level2");
			slPerson2.setEmployeeId("3");
			//slPerson2.setPassword("pw2");
			slPerson2.setUserName("sublevel2");
			poOrganisationService.savePerson(slPerson2, tlGroup);

			// assign the role to the top level person 
			poRoleService.assignRoleWithPersonCompetence(role, tlPerson, slPerson, new Date(), validto, 1);
			// tlPerson1 has rights for all 
			poRoleService.assignRole(role, tlPerson1, new Date(), validto, 1);

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * simple test if parts of the generation worked. 
	 */
	public void testGeneration() {
		assertEquals(poRoleService.findRoleHolderPersonWithCompetence4Person(role, slPerson, new Date()).size(), 1);
	}

	/**
	 * Simple test if generation of deputy worked. 
	 */
	public void testDeputyMultipleMerge() {

		// assert 
		List<PoPerson> l = poRoleService.findAuthority(slPerson2, role);
		// check if the list does not contains the deputy
		assertFalse(l.contains(tlPerson));

		// add the role to slperson
		/*
		 * 
		 * orig:   +-------------o
		 * dep:  +------o
		 * 
		 * new 
		 * dep        +---------------o
		 * 
		 * -> bug:
		 * 
		 * the result was that no deputy was generated and the dep was deleted
		 * 
		 */
		//poRoleService.assignRole(role, slPerson, vf1, vt2,1);
		poRoleService.assignRoleWithPersonCompetence(role, slPerson, slPerson2, vf1, vt2, 1);

		// assert 
		l = poRoleService.findAuthority(slPerson2, role, vf1, -1);
		// check if the list contains the deputy
		assertFalse(l.contains(tlPerson));

		poRoleService.generateDeputy(role, slPerson, tlPerson, firstDate, inside1vt);

		// assert 

		l = poRoleService.findAuthority(slPerson2, role, vf1, -1);
		// check if the list contains the deputy
		assertTrue(l.contains(tlPerson));

		poRoleService.generateDeputy(role, slPerson, tlPerson, inside1vf, vt3);

		// assert 
		l = poRoleService.findAuthority(slPerson2, role, vf1, -1);
		// check if the list contains the deputy
		assertTrue(l.contains(tlPerson));

		List<PoRoleHolderPerson> lrhp = poRoleService.findRoleHolderPersonWithCompetence4PersonF(role, tlPerson, slPerson2, vf1);
		PoRoleHolderPerson rhp = lrhp.get(0);

		assertEquals(rhp.getValidfrom().toString(), vf1.toString());
		assertEquals(rhp.getValidto().toString(), vt2.toString());

	}

	/**
	 * Test if removePersonFromRole works.
	 */
	public void testDeputyRemove() {

		// tlPerson has rights (direct) for slPerson

		// make tlPerson1 the deputy of tlPerson -> tlPerson1 get rights for slPerson
		poRoleService.generateDeputy(role, tlPerson, slPerson2, new Date(), validto);
		System.out.println("Generated deputy '" + tlPerson1.getFullName() + "' for person '" + tlPerson.getFullName() + "'.");

		// assert 
		List<PoPerson> l = poRoleService.findAuthority(slPerson, role);
		// check if the list contains the deputy
		assertTrue(l.contains(slPerson2));

		List<PoRoleHolderPerson> roleHolderPersons = poRoleService.findRoleHolderPersonWithCompetence4Person(role, slPerson, new Date());

		Iterator<PoRoleHolderPerson> i = roleHolderPersons.iterator();

		while (i.hasNext()) {
			PoRoleHolderPerson rhp = i.next();
			if (rhp.getPerson().equals(tlPerson))
				poRoleService.removePersonFromRole(rhp);
		}

		// assert 
		l = poRoleService.findAuthority(slPerson, role);
		// check if the list contains the deputy
		assertFalse(l.contains(slPerson2));
	}

	/**
	 * Checks if the deputy is correctly updated.
	 * 
	 * At first a deputy is generated. 
	 * 
	 * more precicely, update of: assignRoleWithPersonCompetence(role, person, person, date, date, ranking) is checked.
	 */
	public void testDeputyLateAssignment() {
		poRoleService.generateDeputy(role, tlPerson, slPerson2, new Date(), validto);
		System.out.println("Generated deputy '" + slPerson.getFullName() + "' for person '" + tlPerson.getFullName() + "'.");

		// assert 
		List<PoPerson> l = poRoleService.findAuthority(slPerson, role);
		// check if the list contains the deputy
		assertTrue(l.contains(slPerson2));

		//		 assert 
		l = poRoleService.findAuthority(tlPerson1, role);
		// check if the list contains the deputy
		assertFalse(l.contains(slPerson));

		// now add a second assignment
		poRoleService.assignRoleWithPersonCompetence(role, tlPerson, tlPerson1, new Date(), validto, 1);

		// assert -> the slPerson should be contained inside the authority 
		l = poRoleService.findAuthority(tlPerson1, role);
		// check if the list contains the deputy
		assertTrue(l.contains(slPerson2));
	}

	/**
	 * Checks if the deputy is correctly updated.
	 * 
	 * more precicely: assignRoleWithGroupCompetence(role, person, group, date, date, ranking) is checked.
	 */
	public void testDeputyLateAssignment2() {
		poRoleService.generateDeputy(role, tlPerson, slPerson2, new Date(), validto);
		System.out.println("Generated deputy '" + slPerson.getFullName() + "' for person '" + tlPerson.getFullName() + "'.");

		// assert 
		List<PoPerson> l = poRoleService.findAuthority(slPerson, role);
		// check if the list contains the deputy
		assertTrue(l.contains(slPerson2));

		// now add a second assignment

		poRoleService.assignRoleWithGroupCompetence(role, tlPerson, tlGroup, new Date(), validto, 1);

		// assert -> the slPerson should be contained inside the authority 
		l = poRoleService.findAuthority(tlPerson1, role);
		// check if the list contains the deputy
		assertTrue(l.contains(slPerson2));
	}

	/**
	 * 
	 * Checks if the deputy is correctly generated 
	 * via saveDeputy
	 * 
	 */
	public void testDeputyDirectly() {

		PoRoleDeputy rd = new PoRoleDeputy();
		rd.setDeputy(slPerson2);
		rd.setOfficeHolder(tlPerson);
		rd.setRole(role);
		rd.setValidfrom(new Date());
		rd.setValidto(validto);

		poRoleService.saveDeputy(rd);
		System.out.println("Generated deputy '" + slPerson2.getFullName() + "' for person '" + tlPerson.getFullName() + "'.");
		// assert 
		List<PoPerson> l = poRoleService.findAuthority(slPerson, role);
		// check if the list contains the deputy
		assertTrue(l.contains(slPerson2));
	}

	/**
	 * This test checks if a roleHolder with comptence for all 
	 * is correctly found via findAuthority.
	 * 
	 * Then it generates a deputy and checks if the deputy 
	 * is also found. 
	 */
	public void testDeputyForAll() {
		List<PoPerson> l = poRoleService.findAuthority(tlPerson, role);
		assertTrue(l.contains(tlPerson1));

		PoRoleDeputy rd = new PoRoleDeputy();
		rd.setDeputy(tlPerson);
		rd.setOfficeHolder(tlPerson1);
		rd.setRole(role);
		rd.setValidfrom(new Date());
		rd.setValidto(validto);

		poRoleService.saveDeputy(rd);

		l = poRoleService.findAuthority(slPerson2, role);
		assertTrue(l.contains(tlPerson));
	}

	public void testDeputyChangeDate() {

		PoRoleDeputy rd = new PoRoleDeputy();
		rd.setDeputy(tlPerson);
		rd.setOfficeHolder(tlPerson1);
		rd.setRole(role);
		rd.setValidfrom(new Date());
		rd.setValidto(validto);

		rd = poRoleService.saveDeputy(rd);

		List<PoPerson> l = poRoleService.findAuthority(slPerson2, role);
		assertTrue(l.contains(tlPerson));

		IfDate newValidFrom = new IfDate().getDateOnly();
		newValidFrom.add(Calendar.DAY_OF_MONTH, 1);

		IfDate newValidTo = new IfDate().getLastMomentOfDay();
		newValidTo.add(Calendar.DAY_OF_MONTH, 5);

		rd.setValidfrom(newValidFrom);
		rd.setValidto(newValidTo);

		poRoleService.saveDeputy(rd);

		Iterator<PoRoleHolderPerson> i = rd.getRoleHolderPersons().iterator();
		while (i.hasNext()) {
			PoRoleHolderPerson rhp = i.next();
			if (rhp.getValidfrom().after(new Date())) {
				assertEquals(rhp.getValidfrom().toString(), newValidFrom.toString());
				assertEquals(rhp.getValidto().toString(), newValidTo.toString());
			}
		}
	}

	public void testDeputyChangeValidity() {
		poRoleService.generateDeputy(role, tlPerson, slPerson2, new Date(), validto);
		System.out.println("Generated deputy '" + slPerson2.getFullName() + "' for person '" + tlPerson.getFullName() + "'.");

		// assert 
		List<PoPerson> l = poRoleService.findAuthority(slPerson, role);
		// check if the list contains the deputy
		assertTrue(l.contains(slPerson2));

		List<PoRoleHolderPerson> roleHolderPersons = poRoleService.findRoleHolderPersonWithCompetence4Person(role, slPerson, new Date());

		Iterator<PoRoleHolderPerson> i = roleHolderPersons.iterator();

		IfDate newValidFrom = new IfDate();
		newValidFrom.add(Calendar.DAY_OF_MONTH, 10);

		IfDate newValidTo = new IfDate();
		newValidTo.add(Calendar.YEAR, 100); // timerange exceeds the base assignment! 

		while (i.hasNext()) {
			PoRoleHolderPerson rhp =i.next();
			if (rhp.getPerson().equals(tlPerson))
				poRoleService.changeValidityRHPersonLink(rhp, newValidFrom, newValidTo);
		}

		roleHolderPersons = poRoleService.findRoleHolderPersonWithCompetence4Person(role, slPerson, new Date());
		// the actual date was used to search, thus there should be no entry 
		assertTrue(roleHolderPersons.size() == 0);

		roleHolderPersons = poRoleService.findRoleHolderPersonWithCompetence4Person(role, slPerson, newValidFrom);
		// now we adapted the date, thus there should exist (i think two) entries
		assertTrue(roleHolderPersons.size() > 0);

		i = roleHolderPersons.iterator();

		while (i.hasNext()) {
			PoRoleHolderPerson rhp = i.next();
			if (rhp.getPerson().equals(slPerson)) {
				assertEquals(rhp.getValidfrom().toString(), newValidFrom.getDateOnly().toString());
				assertEquals(rhp.getValidto().toString(), validto.toString());
			}
		}

		// assert 
		l = poRoleService.findAuthority(slPerson, role);
		// check if the list contains the deputy
		assertFalse(l.contains(slPerson2));

		// assert 
		l = poRoleService.findAuthority(slPerson, role, newValidFrom, -1);
		// check if the list contains the deputy
		assertTrue(l.contains(slPerson2));
	}

	public void testDeputyTwiceWithdifferentDates() {
		IfDate newValidTo = new IfDate();
		newValidTo.add(Calendar.DAY_OF_MONTH, 5);

		PoRoleDeputy rd = new PoRoleDeputy();
		rd.setDeputy(tlPerson);
		rd.setOfficeHolder(tlPerson1);
		rd.setRole(role);
		rd.setValidfrom(new Date());
		rd.setValidto(newValidTo);

		rd = poRoleService.saveDeputy(rd);

		List<PoPerson> l = poRoleService.findAuthority(tlPerson1, role);
		assertTrue(l.contains(tlPerson));
		// check if the list contains the deputy

		PoRoleDeputy rd2 = new PoRoleDeputy();
		rd2.setDeputy(tlPerson);
		rd2.setOfficeHolder(tlPerson1);
		rd2.setRole(role);

		// generate a new deputy ten days after the previous deputy ends
		newValidTo.add(Calendar.DAY_OF_MONTH, 10);
		rd2.setValidfrom(newValidTo);

		IfDate validTo2 = new IfDate(newValidTo);
		validTo2.add(Calendar.DAY_OF_MONTH, 10);

		rd2.setValidto(validTo2);

		rd = poRoleService.saveDeputy(rd2);

		l = poRoleService.findAuthority(tlPerson1, role, newValidTo, 3);
		assertTrue(l.contains(tlPerson));
	}

	public void testDeputyTwiceWithOverlappingDates() {
		IfDate newValidTo = new IfDate();
		newValidTo.add(Calendar.DAY_OF_MONTH, 5);

		PoRoleDeputy rd = new PoRoleDeputy();
		rd.setDeputy(tlPerson);
		rd.setOfficeHolder(tlPerson1);
		rd.setRole(role);
		rd.setValidfrom(new Date());
		rd.setValidto(newValidTo);

		rd = poRoleService.saveDeputy(rd);

		List<PoPerson> l = poRoleService.findAuthority(tlPerson1, role);
		assertTrue(l.contains(tlPerson));
		// check if the list contains the deputy

		PoRoleDeputy rd2 = new PoRoleDeputy();
		rd2.setDeputy(tlPerson);
		rd2.setOfficeHolder(tlPerson1);
		rd2.setRole(role);
		newValidTo.add(Calendar.DAY_OF_MONTH, -3);
		rd2.setValidfrom(newValidTo);

		IfDate validTo2 = new IfDate(newValidTo);
		validTo2.add(Calendar.DAY_OF_MONTH, 10);

		rd2.setValidto(validTo2);

		rd = poRoleService.saveDeputy(rd2);

		l = poRoleService.findAuthority(tlPerson1, role, newValidTo, -1);
		assertTrue(l.contains(tlPerson));
	}

	/**
	 * <p>
	 * This Testcase checks for validity of Merging.
	 * </p>
	 * <p>
	 * Especially the function <code>assignRole(PoRole, PoPerson, Date, Date, int)</code>
	 * is checked.</p>
	 * <p>
	 * See {@link PoRoleServiceImpl#doesOverlap(PoRoleHolderPerson rhp, Date vf, Date vt)} 
	 * for the meaning of the digits in the source code. Actually, there are 
	 * eleven possible overlappings. The digits are references to one of these 
	 * cases.
	 * </p>
	 */
	public void testMergeCompetenceAll_Person() {
		runMergeTestWithHelper(new PoRoleHolderPersonCompetenceAllHelper(poRoleService, role, slPerson, 10));
	}

	/**
	 * <p>
	 * This Testcase checks for validity of Merging.
	 * </p>
	 * <p>
	 * Especially the function <code>assignRole(PoRole, PoGroup, Date, Date, int)</code>
	 * is checked.</p>
	 * <p>
	 * See {@link PoRoleServiceImpl#doesOverlap(PoRoleHolderPerson rhp, Date vf, Date vt)} 
	 * for the meaning of the digits in the source code. Actually, there are 
	 * eleven possible overlappings. The digits are references to one of these 
	 * cases.
	 * </p>
	 */
	public void testMergeCompetenceAll_Group() {
		runMergeTestWithHelper(new PoRoleHolderGroupCompetenceAllHelper(poRoleService, role, tlGroup, 10));
		// fri_2014-01-17: yielded "AssertionFailedError: expected:<1> but was:<2>"
	}

	/**
	 * <p>
	 * This Testcase checks for validity of Merging.
	 * </p>
	 * <p>
	 * Especially the function 
	 * <code>assignRoleWithCompetence4Person(PoRole, PoPerson, PoPerson, Date, Date, int)</code>
	 * is checked.</p>
	 * <p>
	 * See {@link PoRoleServiceImpl#doesOverlap(PoRoleHolderPerson rhp, Date vf, Date vt)} 
	 * for the meaning of the digits in the source code. Actually, there are 
	 * eleven possible overlappings. The digits are references to one of these 
	 * cases.
	 * </p>
	 */
	public void testMergeCompetencePerson_Person() {
		runMergeTestWithHelper(new PoRoleHolderPersonCompetencePersonHelper(poRoleService, role, slPerson, tlPerson, 10));
	}

	/**
	 * <p>
	 * This Testcase checks for validity of Merging.
	 * </p>
	 * <p>
	 * Especially the function 
	 * <code>assignRoleWithCompetenceForPerson(PoRole,PoGroup, PoPerson, Date, Date, int)</code>
	 * is checked.</p>
	 * <p>
	 * See {@link PoRoleServiceImpl#doesOverlap(PoRoleHolderPerson rhp, Date vf, Date vt)} 
	 * for the meaning of the digits in the source code. Actually, there are 
	 * eleven possible overlappings. The digits are references to one of these 
	 * cases.
	 * </p>
	 */
	public void testMergeCompetencePerson_Group() {
		runMergeTestWithHelper(new PoRoleHolderGroupCompetencePersonHelper(poRoleService, role, tlGroup, tlPerson, 10));
	}

	/**
	 * <p>
	 * This Testcase checks for validity of Merging.
	 * </p>
	 * <p>
	 * Especially the function 
	 * <code>assignRoleWithCompetence4Person(PoRole, PoPerson, PoPerson, Date, Date, int)</code>
	 * is checked.</p>
	 * <p>
	 * See {@link PoRoleServiceImpl#doesOverlap(PoRoleHolderPerson rhp, Date vf, Date vt)} 
	 * for the meaning of the digits in the source code. Actually, there are 
	 * eleven possible overlappings. The digits are references to one of these 
	 * cases.
	 * </p>
	 */
	public void testMergeCompetenceGroup_Person() {
		runMergeTestWithHelper(new PoRoleHolderPersonCompetenceGroupHelper(poRoleService, role, slPerson, tlGroup, 10));
	}

	/**
	 * <p>
	 * This Testcase checks for validity of Merging.
	 * </p>
	 * <p>
	 * Especially the function 
	 * <code>assignRoleWithCompetenceForPerson(PoRole,PoGroup, PoPerson, Date, Date, int)</code>
	 * is checked.</p>
	 * <p>
	 * See {@link PoRoleServiceImpl#doesOverlap(PoRoleHolderPerson rhp, Date vf, Date vt)} 
	 * for the meaning of the digits in the source code. Actually, there are 
	 * eleven possible overlappings. The digits are references to one of these 
	 * cases.
	 * </p>
	 */
	public void testMergeCompetenceGroup_Group() {
		runMergeTestWithHelper(new PoRoleHolderGroupCompetenceGroupHelper(poRoleService, role, tlGroup, tlGroup2, 10));
	}

	public void testDeputyWithHelper_GroupAssignment() {
		PoHelper genHelper = new PoRoleHolderPersonCompetenceGroupHelper(poRoleService, role, slPerson, tlGroup, 10);
		PoHelper searchHelper = new PoRoleHolderPersonCompetenceGroupHelper(poRoleService, role, tlPerson1, tlGroup, 10);

		PoRoleDeputy rd = new PoRoleDeputy();
		rd.setOfficeHolder(slPerson);
		rd.setDeputy(tlPerson1);
		rd.setRole(role);
		rd.setValidfrom(firstDate);
		rd.setValidto(lastDate);

		runMergeTestWithHelper(new PoDeputyTestMergeHelper(poRoleService, role, rd, genHelper, searchHelper));
	}

	public void testDeputyWithHelper_PersonAssignment() {
		PoHelper genHelper = new PoRoleHolderPersonCompetencePersonHelper(poRoleService, role, slPerson, tlPerson1, 10);
		PoHelper searchHelper = new PoRoleHolderPersonCompetencePersonHelper(poRoleService, role, tlPerson, tlPerson1, 10);

		PoRoleDeputy rd = new PoRoleDeputy();
		rd.setOfficeHolder(slPerson);
		rd.setDeputy(tlPerson);
		rd.setRole(role);
		rd.setValidfrom(firstDate);
		rd.setValidto(lastDate);

		runMergeTestWithHelper(new PoDeputyTestMergeHelper(poRoleService, role, rd, genHelper, searchHelper));
	}

	public void testDeputyWithHelper_All() {
		PoHelper genHelper = new PoRoleHolderPersonCompetenceAllHelper(poRoleService, role, tlPerson, 10);
		PoHelper searchHelper = new PoRoleHolderPersonCompetenceAllHelper(poRoleService, role, slPerson, 10);

		PoRoleDeputy rd = new PoRoleDeputy();
		rd.setOfficeHolder(tlPerson);
		rd.setDeputy(slPerson);
		rd.setRole(role);
		rd.setValidfrom(firstDate);
		rd.setValidto(lastDate);

		runMergeTestWithHelper(new PoDeputyTestMergeHelper(poRoleService, role, rd, genHelper, searchHelper));
	}

	/**
	 * <p>This function tests for all possible overlappings
	 * of <code>PoRoleHolderPerson</code> or <code>PoRoleHolder</code>
	 * group objects.
	 * </p>
	 * <p>With the aid of the <code>PoHelper</code> classes,
	 * it is rather easy to test all assignments of 
	 * <code>PoRoleCompetenceBase</code> objects.
	 * </p>
	 * 
	 * @param helper a <code>PoHelper</code> implementation
	 */
	private void runMergeTestWithHelper(PoHelper helper) {
		helper.doAssignment(vf1, vt1);
		helper.doAssignment(vf2, vt2);
		helper.doAssignment(vf3, vt3);

		List<? extends PoRoleHolderLink> list = helper.findRoleHolder(vf1);
		assertEquals(3, list.size());

		assertEquals(helper.getValidFrom(list, 0).toString(), vf1.toString());
		assertEquals(helper.getValidTo(list, 0).toString(), vt1.toString());

		assertEquals(helper.getValidFrom(list, 1).toString(), vf2.toString());
		assertEquals(helper.getValidTo(list, 1).toString(), vt2.toString());

		assertEquals(helper.getValidFrom(list, 2).toString(), vf3.toString());
		assertEquals(helper.getValidTo(list, 2).toString(), vt3.toString());

		// Test Merge multiple with (start equals,end outside)(3), (both outside)(1), (start outside, end equals) (8) 
		helper.doAssignment(vf1, vt3);
		list = helper.findRoleHolder(vf1);
		assertEquals(1, list.size());

		assertEquals(helper.getValidFrom(list, 0).toString(), vf1.toString());
		assertEquals(helper.getValidTo(list, 0).toString(), vt3.toString());

		helper.removeRoleHolderFromRole(list, 0);

		// Test Merge multiple with (start outside, end outside)(1), (both outside)(1), (start outside, end outside)(1)
		// this case was already considered above!
		helper.doAssignment(vf1, vt1);
		helper.doAssignment(vf2, vt2);
		helper.doAssignment(vf3, vt3);

		helper.doAssignment(firstDate, lastDate);
		list = helper.findRoleHolder(firstDate);

		assertEquals(helper.getValidFrom(list, 0).toString(), firstDate.toString());
		assertEquals(helper.getValidTo(list, 0).toString(), lastDate.toString());
		helper.removeRoleHolderFromRole(list, 0);

		// Test both inside (9)

		helper.doAssignment(vf1, vt2);
		helper.doAssignment(inside1vf, inside2vt);

		list = helper.findRoleHolder(vf1);

		assertEquals(list.size(), 1);
		assertEquals(helper.getValidFrom(list, 0).toString(), vf1.toString());
		assertEquals(helper.getValidTo(list, 0).toString(), vt2.toString());
		helper.removeRoleHolderFromRole(list, 0);

		// Test end equals start (10)
		helper.doAssignment(vf1, vt1);
		helper.doAssignment(vt1, vt2);

		list = helper.findRoleHolder(vf1);

		assertEquals(1, list.size());
		// fri_2013-10-08: failed on Hudson with "expected:<2> but was:<1>", not reproducible locally in Eclipse
		// fri_2013-11-18: still fails, not reproducible. Corrected the assert parameter order.
		assertEquals(helper.getValidFrom(list, 0).toString(), vf1.toString());
		assertEquals(helper.getValidTo(list, 0).toString(), vt2.toString());
		helper.removeRoleHolderFromRole(list, 0);

		// Test start equals end (11)
		helper.doAssignment(vf2, vt2);
		helper.doAssignment(vf1, vf2);

		list = helper.findRoleHolder(vf1);

		assertEquals(1, list.size());
		assertEquals(helper.getValidFrom(list, 0).toString(), vf1.toString());
		assertEquals(helper.getValidTo(list, 0).toString(), vt2.toString());
		helper.removeRoleHolderFromRole(list, 0);

		// Test start outside, end inside (2)

		helper.doAssignment(vf2, vt2);
		helper.doAssignment(vf1, inside2vt);

		list = helper.findRoleHolder(vf1);

		assertEquals(1, list.size());
		assertEquals(helper.getValidFrom(list, 0).toString(), vf1.toString());
		assertEquals(helper.getValidTo(list, 0).toString(), vt2.toString());
		helper.removeRoleHolderFromRole(list, 0);

		// Test start equals, end inside (4)

		helper.doAssignment(vf1, vt2);
		helper.doAssignment(vf1, inside2vt);

		list = helper.findRoleHolder(vf1);

		assertEquals(1, list.size());
		assertEquals(helper.getValidFrom(list, 0).toString(), vf1.toString());
		assertEquals(helper.getValidTo(list, 0).toString(), vt2.toString());
		helper.removeRoleHolderFromRole(list, 0);

		// Test both equals (5)

		helper.doAssignment(vf2, vt2);
		helper.doAssignment(vf2, vt2);

		list = helper.findRoleHolder(vf2);

		assertEquals(1, list.size());
		assertEquals(helper.getValidFrom(list, 0).toString(), vf2.toString());
		assertEquals(helper.getValidTo(list, 0).toString(), vt2.toString());
		helper.removeRoleHolderFromRole(list, 0);

		// Test start inside, end equals (6)

		helper.doAssignment(vf2, vt2);
		helper.doAssignment(inside2vf, vt2);

		list = helper.findRoleHolder(vf1);

		assertEquals(1, list.size());
		assertEquals(helper.getValidFrom(list, 0).toString(), vf2.toString());
		assertEquals(helper.getValidTo(list, 0).toString(), vt2.toString());
		helper.removeRoleHolderFromRole(list, 0);

		// Test (start inside end outside) (7)

		helper.doAssignment(vf1, inside2vt);
		helper.doAssignment(vf2, vt2);

		list = helper.findRoleHolder(vf1);

		assertEquals(1, list.size());
		assertEquals(helper.getValidFrom(list, 0).toString(), vf1.toString());
		assertEquals(helper.getValidTo(list, 0).toString(), vt2.toString());
		helper.removeRoleHolderFromRole(list, 0);
	}

}
