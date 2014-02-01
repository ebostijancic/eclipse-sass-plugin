package at.workflow.webdesk.po.impl.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;

import at.workflow.webdesk.po.PoActionPermissionService;
import at.workflow.webdesk.po.PoActionService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.impl.helper.PoDataGenerator;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoBase;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoParentGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoPersonGroup;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.date.HistorizationHelper;
import at.workflow.webdesk.tools.date.HistoricizableValidFromComparator;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;
import at.workflow.webdesk.tools.testing.DataGenerator;
import at.workflow.webdesk.tools.api.Historization;

public class WTestPoOrganisationServiceImpl extends AbstractTransactionalSpringHsqlDbTestCase {

	//Services
	private PoOrganisationService orgService;
	private PoActionPermissionService permissionService;

	/**
	 * use this to provide custom Datagenerators
	 * @return
	 */
	@Override
	protected final DataGenerator[] getDataGenerators() {
		return new DataGenerator[] { new PoDataGenerator("at/workflow/webdesk/po/impl/test/data/MinTestData.xml", false) };
	}

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		orgService = (PoOrganisationService) getBean("PoOrganisationService");
		permissionService = (PoActionPermissionService) getBean("PoActionPermissionService");
	}

	public void testLoadAllPersons() {
		assertTrue("there should be at least one person!", orgService.loadAllPersons().size() > 0);
	}

	public void testLoadAllGroups() {
		assertTrue("there should be at least one group!", orgService.loadAllGroups().size() > 0);
	}

	public void testLoadAllClients() {
		assertTrue("there should be at least one client!", orgService.loadAllClients().size() > 0);
	}

	public void testResolveGroupToPersons() {
		List<PoPerson> personList = orgService.resolveGroupToPersons("G01_1", true, new Date());
		assertTrue("The number of persons in G01_1+ should be exactly 6, but was " + personList.size(), personList.size() == 6);

		personList = orgService.resolveGroupToPersons("G01_1", false, new Date());
		assertTrue("The number of persons in G01_1 should be exactly 4, but was " + personList.size(), personList.size() == 4);

		personList = orgService.resolveGroupToPersons("G01", true, new Date());
		assertTrue("The number of persons in G01+ should be exactly 10, but was " + personList.size(), personList.size() == 10);
	}

	public void testSetParentGroup() {
		Date now = new Date();

		// move G01_1_1_1 to G01_2
		PoGroup groupToMove = orgService.findGroupByShortName("G01_1_1_1");
		PoGroup newParentGroup = orgService.findGroupByShortName("G01_2");

		orgService.setParentGroup(groupToMove, newParentGroup, now, DateTools.INFINITY);

		// now groupToMove should have exactly one parent in the future
		PoGroup myGroup = orgService.findGroupByShortName("G01_1_1_1");
		int no = getNumberOfHistorizationObjectsFromDate(myGroup.getParentGroups(), now);
		assertEquals("G01_1_1_1 should only have one parent in the future", 1, no);

		PoGroup newParentGroup2 = orgService.findGroupByShortName("G01_3");
		Date today = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
		Date startDate = DateUtils.addDays(today, 10);
		Date endDate = DateUtils.addDays(today, 20);
		orgService.setParentGroup(groupToMove, newParentGroup2, startDate, endDate);

		// now groupToMove should have exactly 3 parent in the future
		myGroup = orgService.findGroupByShortName("G01_1_1_1");
		no = getNumberOfHistorizationObjectsFromDate(myGroup.getParentGroups(), now);
		assertTrue("G01_1_1_1 should only have 3 parents in the future, but has " + no, no == 3);

		PoGroup newParentGroup4 = orgService.findGroupByShortName("G01_4");

		// now set a new parentgroup until INFDATE
		orgService.setParentGroup(groupToMove, newParentGroup4, now, PoConstants.getInfDate());

		myGroup = orgService.findGroupByShortName("G01_1_1_1");
		no = getNumberOfHistorizationObjectsFromDate(myGroup.getParentGroups(), now);
		assertTrue("G01_1_1_1 should only have one parent in the future, but has " + no, no == 1);

		// now set a SAME parentgroup again
		// we should NOT get any extra ParentGroup!
		orgService.setParentGroup(groupToMove, newParentGroup4, now, PoConstants.getInfDate());

		myGroup = orgService.findGroupByShortName("G01_1_1_1");
		no = getNumberOfHistorizationObjectsFromDate(myGroup.getParentGroups(), now);
		assertTrue("G01_1_1_1 should only have one parent in the future, but has " + no, no == 1);

		// now set SAME parentgroup beginning with tomorrow
		// we should NOT get any extra ParentGroup!
		orgService.setParentGroup(groupToMove, newParentGroup4, DateUtils.addDays(now, 1), PoConstants.getInfDate());

		myGroup = orgService.findGroupByShortName("G01_1_1_1");
		no = getNumberOfHistorizationObjectsFromDate(myGroup.getParentGroups(), now);
		assertTrue("G01_1_1_1 should only have one parent in the future, but has " + no, no == 1);

		// now set SAME parentgroup beginning in 10 days and ending after 20
		// we should NOT get any extra ParentGroup!
		orgService.setParentGroup(groupToMove, newParentGroup4, DateUtils.addDays(now, 10), DateUtils.addDays(now, 20));

		myGroup = orgService.findGroupByShortName("G01_1_1_1");
		no = getNumberOfHistorizationObjectsFromDate(myGroup.getParentGroups(), now);
		assertTrue("G01_1_1_1 should only have one parent in the future, but has " + no, no == 1);
	}

	public void testParentGroups2() {
		// move an existing child one layer higher
		PoGroup g01_1_1 = orgService.findGroupByShortName("G01_1_1");
		PoGroup g01 = orgService.findGroupByShortName("G01");
		orgService.setParentGroup(g01_1_1, g01);
		assertTrue(containsChild(g01, g01_1_1));

		// assign a child of g01_1_1 to g01 in the future
		Calendar gc = new GregorianCalendar();
		gc.add(Calendar.MONTH, 3);

		PoGroup g01_1_1_1 = orgService.findGroupByShortName("G01_1_1_1");
		orgService.setParentGroup(g01_1_1_1, g01,
				gc.getTime(), new Date(DateTools.INFINITY_TIMEMILLIS));

		// at the current time there should be no assignment
		assertFalse("g01_1_1_1 should not be a child of g01 at the moment",
				containsChild(g01, g01_1_1_1));
		assertTrue("g01_1_1_1 should be a child of g01 at the " + gc.getTime(),
				containsChild(g01, g01_1_1_1, gc.getTime()));
		// we should check if the child relation between g01_1_1 and g01_1_1_1 expires 
		printChilds(g01);
		printChilds(g01_1_1);
		assertTrue(checkExpireTime(g01_1_1, g01_1_1_1,
				makeToDate(HistorizationHelper.generateUsefulValidFrom(gc.getTime()))));
		int amountOfParentGroups = orgService.findChildGroupsF(g01, new Date()).size();

		// make the same assignment as before 
		orgService.setParentGroup(g01_1_1_1, g01,
				gc.getTime(), new Date(DateTools.INFINITY_TIMEMILLIS));
		// make the same tests as before
		assertFalse("g01_1_1_1 should not be a child of g01 at the moment",
				containsChild(g01, g01_1_1_1));
		assertTrue("g01_1_1_1 should be a child of g01 at the " + gc.getTime(),
				containsChild(g01, g01_1_1_1, gc.getTime()));
		// check if the assignment is made only once
		assertEquals("It seems that assigning the same parent twice results in two assignments",
				amountOfParentGroups, orgService.findChildGroupsF(g01, new Date()).size());
		System.out.println("After second assignment");
		printChilds(g01);
		printChilds(g01_1_1);
	}

	private int getNumberOfHistorizationObjectsFromDate(Collection<? extends Historization> histList, Date date) {
		System.out.println(" ... output links ....");

		List<Historization> histListSorted = new ArrayList<Historization>();
		histListSorted.addAll(histList);
		Collections.sort(histListSorted, new HistoricizableValidFromComparator());

		int ret = 0;
		Iterator<Historization> itr = histListSorted.iterator();
		while (itr.hasNext()) {
			Historization histObj = itr.next();
			if (histObj.getValidto().after(date)) {
				ret += 1;
				System.out.println("valid Link: " + histObj.getValidfrom() + " - " + histObj.getValidto() + ", UID=" + ((PoBase) histObj).getUID());
			}
		}
		return ret;
	}

	public void testLinkPerson2Group() {
		PoPerson person = orgService.findPersonByUserName("wef");
		List<PoPersonGroup> existingEntries = orgService.findPersonGroups(person, new Date(), PoOrgStructure.STRUCTURE_TYPE_ORGANISATION_HIERARCHY);
		System.out.println("existingEntries: " + existingEntries);
		orgService.findGroupByShortName("G01_1");

		TestServiceAdapter personGroupAdapter =
				new TestPoPersonGroupServiceAdapter(person);
		TestHistorizationHelper histHelper = new TestHistorizationHelper();
		histHelper.run(personGroupAdapter);

	}

	public void testCRUDPerson() {

		Date tomorow = DateUtils.addDays(new Date(), +1);

		final String clientName = getClass().getSimpleName();	// avoid potentially missing unit test isolation
		PoClient client = orgService.findClientByName(clientName);
		if (client == null)	{
			client = new PoClient();
			client.setName(clientName);
			client.setShortName("TC");
			orgService.saveClient(client);
		}
		PoPerson person = new PoPerson();
		person.setFirstName("Gabriel");
		person.setLastName("Gruber");
		person.setUserName("ggruber");
		person.setEmployeeId("4711");
		person.setValidfrom(tomorow); // so that the first group link will not be historicized
		person.setClient(client);

		PoGroup group = orgService.findGroupByShortName("G01");
		orgService.savePerson(person, group);
		List<PoPersonGroup> personGroups = orgService.findPersonGroupsAll(person);
		assertNotNull(personGroups);
		assertEquals(1, personGroups.size());
		assertEquals(group, personGroups.get(0).getGroup());
		
		// update - replace G01 with G01_01 
		PoGroup group1 = orgService.findGroupByShortName("G01_1");
		person.getMemberOfGroups().iterator().next().setGroup(group1);
		orgService.updatePerson(person);
		personGroups = orgService.findPersonGroupsAll(person);
		assertNotNull(personGroups);
		assertEquals(1, personGroups.size());
		assertEquals(group1, personGroups.get(0).getGroup());
		
		// update - back to G01 via new assignment
		person.getMemberOfGroups().clear();
		PoPersonGroup pg = new PoPersonGroup();
		group.addPersonGroup(pg);
		person.addMemberOfGroup(pg);
		pg.setValidfrom(person.getValidfrom());
		orgService.updatePerson(person);
		personGroups = orgService.findPersonGroupsAll(person);
		assertNotNull(personGroups);
		assertEquals(1, personGroups.size()); 
		assertEquals(group, personGroups.get(0).getGroup());

	}

	public void testAssignPermissionsPerson() {
		
		Date now = new Date();
		Date yesterday = DateTools.dateOnly(now);
		yesterday = DateUtils.addDays(yesterday, -1);
		Date tomorow = DateUtils.addDays(yesterday, +2);
		Date dayAfterTomorow = DateUtils.addDays(tomorow, +1);
		Date lastMonth = DateUtils.addMonths(now, -1);
		Date lastWeek = DateUtils.addWeeks(now, -1);
		
		final String clientName = getClass().getSimpleName();	// avoid potentially missing unit test isolation
		PoClient client = orgService.findClientByName(clientName);
		if (client == null)	{
			client = new PoClient();
			client.setName(clientName);
			client.setShortName("TC");
			orgService.saveClient(client);
		}
		PoGroup group = orgService.findGroupByShortName("G01");
		group.setValidfrom(yesterday);
		orgService.saveGroup(group);

		PoPerson person = new PoPerson();
		person.setFirstName("Gabriel");
		person.setLastName("Gruber");
		person.setUserName("ggruber");
		person.setEmployeeId("4711");
		person.setClient(client);
		person.setValidfrom(yesterday);
		orgService.savePerson(person, group);
		
		// now add a permission which started yesterday
		PoActionService actionService = (PoActionService) applicationContext.getBean("PoActionService");
		PoAction myAction = actionService.findActionByNameAndType("Arztgang", PoConstants.ACTION_TYPE_PROCESS);
		
		permissionService.assignPermission(myAction, person, lastMonth, lastWeek, PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON);
		permissionService.assignPermission(myAction, person, yesterday, tomorow, PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON);
		permissionService.assignPermission(myAction, person, tomorow, dayAfterTomorow, PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON);
		
		orgService.deletePerson(person);
		
		int count = 0;
		count = countValidHistorizationObjects(person.getPermissions(), new Date());
		assertEquals("There should be NO valid permission object", 0, count);
		
		count = countValidHistorizationObjects(person.getPermissions(), DateUtils.addHours(tomorow, 1));
		assertEquals("There should be ONE valid permission object with referenceDate=tommorow+1hour", 1, count);
		
		count = countValidHistorizationObjects(person.getPermissions(), DateUtils.addHours(lastMonth, 1));
		assertEquals("There should be ONE valid permission object with referenceDate=lastMonth+1hour", 1, count);
	}
	
	public void testFindPersonsWithFilter() {

		Calendar cal = GregorianCalendar.getInstance();
		Date today = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		Date tomorrow = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		Date afterTomorrow = cal.getTime();

		PoPerson wef = orgService.findPersonByUserName("wef");
		PoPerson ham = orgService.findPersonByUserName("ham");
		PoPerson duc = orgService.findPersonByUserName("duc");

		wef.setValidfrom(tomorrow);
		wef.setValidto(tomorrow);
		orgService.updatePerson(wef);

		ham.setValidfrom(today);
		ham.setValidto(today);
		orgService.updatePerson(ham);

		duc.setValidfrom(afterTomorrow);
		duc.setValidto(afterTomorrow);
		orgService.updatePerson(duc);

		List<String> uidList = new ArrayList<String>();
		uidList.add(wef.getUID());
		uidList.add(ham.getUID());
		uidList.add(duc.getUID());

		List<String> searchList = new ArrayList<String>();

		List<PoPerson> result = orgService.findPersonsWithFilter(uidList, searchList, tomorrow);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(wef, result.get(0));

		searchList.add("eis");

		result = orgService.findPersonsWithFilter(uidList, searchList, tomorrow);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(wef, result.get(0));

		searchList.set(0, "mar");

		result = orgService.findPersonsWithFilter(uidList, searchList, tomorrow);
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	public void testFindPersonsWithFilterFromTo() {

		Calendar cal = GregorianCalendar.getInstance();
		Date today = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		Date tomorrow = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		Date afterTomorrow = cal.getTime();

		PoPerson wef = orgService.findPersonByUserName("wef");
		PoPerson ham = orgService.findPersonByUserName("ham");
		PoPerson duc = orgService.findPersonByUserName("duc");

		wef.setValidfrom(tomorrow);
		wef.setValidto(tomorrow);
		orgService.updatePerson(wef);

		ham.setValidfrom(today);
		ham.setValidto(today);
		orgService.updatePerson(ham);

		duc.setValidfrom(afterTomorrow);
		duc.setValidto(afterTomorrow);
		orgService.updatePerson(duc);

		List<String> uidList = new ArrayList<String>();
		uidList.add(wef.getUID());
		uidList.add(ham.getUID());
		uidList.add(duc.getUID());

		List<String> searchList = new ArrayList<String>();

		List<PoPerson> result = orgService.findPersonsWithFilter(uidList, searchList, today, afterTomorrow);
		assertNotNull(result);
		assertEquals(3, result.size());
		assertTrue(result.contains(wef));
		assertTrue(result.contains(ham));
		assertTrue(result.contains(duc));

		searchList.add("eis");
		result = orgService.findPersonsWithFilter(uidList, searchList, today, afterTomorrow);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(wef, result.get(0));

		searchList.set(0, "du");
		result = orgService.findPersonsWithFilter(uidList, searchList, today, afterTomorrow);
		assertNotNull(result);
		assertEquals(0, result.size());

		searchList.clear();
		result = orgService.findPersonsWithFilter(uidList, searchList, tomorrow, tomorrow);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(wef, result.get(0));

		result = orgService.findPersonsWithFilter(uidList, searchList, today, today);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(ham, result.get(0));

		result = orgService.findPersonsWithFilter(uidList, searchList, afterTomorrow, afterTomorrow);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(duc, result.get(0));

	}

	public void testFindPersonsWithViewPermission() {

		Calendar cal = GregorianCalendar.getInstance();
		Date today = cal.getTime();
		PoGroup g01 = orgService.findGroupByShortName("G01");
		PoClient client = g01.getClient();
		PoPerson wef = orgService.findPersonByUserName("wef");
		PoPerson ham = orgService.findPersonByUserName("ham");

		List<String> uidList = new ArrayList<String>();
		uidList.add(client.getUID());
		Map<String, List<String>> viewPermissions = new HashMap<String, List<String>>();
		viewPermissions.put(PoActionPermissionService.CLIENTS, uidList);

		List<String> searchList = new ArrayList<String>();

		List<PoPerson> result = orgService.findPersonsWithViewPermission(viewPermissions, searchList, today);
		assertNotNull(result);
		assertEquals(10, result.size());

		searchList.add("eis");
		result = orgService.findPersonsWithViewPermission(viewPermissions, searchList, today);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(wef, result.get(0));

		searchList.set(0, "edd");
		result = orgService.findPersonsWithViewPermission(viewPermissions, searchList, today);
		assertNotNull(result);
		assertEquals(0, result.size());

		uidList.set(0, g01.getUID());
		viewPermissions.clear();
		viewPermissions.put(PoActionPermissionService.GROUPS, uidList);
		searchList.clear();
		result = orgService.findPersonsWithViewPermission(viewPermissions, searchList, today);
		assertNotNull(result);
		assertEquals(2, result.size());
		assertTrue(result.contains(wef));
		assertTrue(result.contains(ham));

		searchList.add("eis");
		result = orgService.findPersonsWithViewPermission(viewPermissions, searchList, today);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(wef, result.get(0));

		searchList.set(0, "edd");
		result = orgService.findPersonsWithViewPermission(viewPermissions, searchList, today);
		assertNotNull(result);
		assertEquals(0, result.size());

		uidList.set(0, wef.getUID());
		uidList.add(ham.getUID());
		viewPermissions.clear();
		viewPermissions.put(PoActionPermissionService.PERSONS, uidList);
		searchList.clear();
		result = orgService.findPersonsWithViewPermission(viewPermissions, searchList, today);
		assertNotNull(result);
		assertEquals(2, result.size());
		assertTrue(result.contains(wef));
		assertTrue(result.contains(ham));

		searchList.add("eis");
		result = orgService.findPersonsWithViewPermission(viewPermissions, searchList, today);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(wef, result.get(0));

		searchList.set(0, "edd");
		result = orgService.findPersonsWithViewPermission(viewPermissions, searchList, today);
		assertNotNull(result);
		assertEquals(0, result.size());

	}

	public void testFindPersonsWithViewPermissionFromTo() {

		Calendar cal = GregorianCalendar.getInstance();
		Date today = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Date yesterday = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, 2);
		Date tomorrow = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		Date afterTomorrow = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		Date after2Tomorrow = cal.getTime();

		PoGroup g01 = orgService.findGroupByShortName("G01");
		PoGroup g011 = orgService.findGroupByShortName("G01_1");
		PoClient client = g01.getClient();
		PoPerson wef = orgService.findPersonByUserName("wef");
		PoPerson ham = orgService.findPersonByUserName("ham");
		PoPerson duc = orgService.findPersonByUserName("duc");

		ham.setValidfrom(today);
		ham.setValidto(today);
		orgService.updatePerson(ham);

		duc.setValidfrom(after2Tomorrow);
		duc.setValidto(after2Tomorrow);
		orgService.updatePerson(duc);

		List<String> uidList = new ArrayList<String>();
		uidList.add(client.getUID());
		Map<String, List<String>> viewPermissions = new HashMap<String, List<String>>();
		viewPermissions.put(PoActionPermissionService.CLIENTS, uidList);

		List<String> searchList = new ArrayList<String>();

		List<PoPerson> result = orgService.findPersonsWithViewPermission(viewPermissions, searchList, tomorrow, afterTomorrow);
		assertNotNull(result);
		assertEquals(8, result.size());
		assertTrue(!result.contains(ham));
		assertTrue(!result.contains(duc));

		searchList.add("eis");
		result = orgService.findPersonsWithViewPermission(viewPermissions, searchList, tomorrow, afterTomorrow);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(wef, result.get(0));

		searchList.set(0, "edd");
		result = orgService.findPersonsWithViewPermission(viewPermissions, searchList, tomorrow, afterTomorrow);
		assertNotNull(result);
		assertEquals(0, result.size());

		uidList.set(0, g01.getUID());
		uidList.add(g011.getUID());
		viewPermissions.clear();
		viewPermissions.put(PoActionPermissionService.GROUPS, uidList);
		searchList.clear();
		result = orgService.findPersonsWithViewPermission(viewPermissions, searchList, tomorrow, afterTomorrow);
		assertNotNull(result);
		assertEquals(4, result.size());
		assertTrue(result.contains(wef));
		assertTrue(!result.contains(ham));
		assertTrue(!result.contains(duc));

		searchList.add("eis");
		result = orgService.findPersonsWithViewPermission(viewPermissions, searchList, tomorrow, afterTomorrow);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(wef, result.get(0));

		searchList.set(0, "edd");
		result = orgService.findPersonsWithViewPermission(viewPermissions, searchList, tomorrow, afterTomorrow);
		assertNotNull(result);
		assertEquals(0, result.size());

		uidList.set(0, wef.getUID());
		uidList.add(ham.getUID());
		uidList.add(duc.getUID());
		viewPermissions.clear();
		viewPermissions.put(PoActionPermissionService.PERSONS, uidList);
		searchList.clear();
		result = orgService.findPersonsWithViewPermission(viewPermissions, searchList, tomorrow, afterTomorrow);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertTrue(result.contains(wef));
		assertTrue(!result.contains(ham));
		assertTrue(!result.contains(duc));

		searchList.add("eis");
		result = orgService.findPersonsWithViewPermission(viewPermissions, searchList, tomorrow, afterTomorrow);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(wef, result.get(0));

		searchList.set(0, "edd");
		result = orgService.findPersonsWithViewPermission(viewPermissions, searchList, tomorrow, afterTomorrow);
		assertNotNull(result);
		assertEquals(0, result.size());

	}

	private int countValidHistorizationObjects(Collection<? extends Historization> histObjs, Date referenceDate) {
		int count = 0;
		Iterator<? extends Historization> itr = histObjs.iterator();
		while (itr.hasNext()) {
			Historization histObj = itr.next();
			if (HistorizationHelper.isValid(histObj, referenceDate)) {
				count++;
			}
		}
		return count;
	}

	private boolean checkExpireTime(PoGroup parent, PoGroup child,
			Date to) {

		List<PoParentGroup> childGroups = orgService.findChildGroupsF(parent, new Date());
		for (Iterator<PoParentGroup> i = childGroups.iterator(); i.hasNext();) {
			PoParentGroup pg = i.next();
			if (pg.getChildGroup().equals(child)) {
				if (pg.getValidto().equals(to))
					;
				return true;
			}
		}
		return false;
	}

	private Date makeToDate(Date to) {
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(to);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Date toDate = DateTools.lastMomentOfDay(cal.getTime());
		return toDate;
	}

	private void printChilds(PoGroup group) {
		List<PoParentGroup> childGroups = orgService.findChildGroupsF(group, new Date());
		int counter = 0;
		System.out.println("Parent: " + group.getShortName());
		for (Iterator<PoParentGroup> i = childGroups.iterator(); i.hasNext();) {
			PoParentGroup pg = i.next();
			System.out.println("Child " + counter + " : " + pg.getChildGroup().getShortName() +
					" " + pg.getValidfrom() + " - " + pg.getValidto());
			counter++;
		}
	}

	private boolean containsChild(PoGroup parent, PoGroup child) {
		return containsChild(parent, child, new Date());
	}

	private boolean containsChild(PoGroup parent, PoGroup child, Date date) {
		List<PoParentGroup> childGroups = orgService.findChildGroups(parent, date);
		for (Iterator<PoParentGroup> i = childGroups.iterator(); i.hasNext();) {
			PoParentGroup pg = i.next();
			if (pg.getChildGroup().equals(child))
				return true;
		}
		return false;
	}

}
