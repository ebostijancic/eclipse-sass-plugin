/*
 * Created on 19.05.2005
 *
 * @author hentner (Harald Entner) 
 */
package at.workflow.webdesk.po.impl.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;

import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.daos.PoGroupDAO;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

/**
 * @author ggruber
 * @author hentner
 */
public class WTestPoGroupDAOImpl extends AbstractTransactionalSpringHsqlDbTestCase {
    
	private final static String MY_CLIENT_NAME = "Testmandant";
	private final static String MY_STRUCTURE_NAME = "Kostenstellen";
    
	private Date past, beforeStart, start, afterStart, beforeEnd, end, afterEnd, future;
	
    private PoClient myClient;
    private PoGroup myGroup, myGroup1, myGroup2, myGroup3, myGroup4, myGroup5;
    private List<String> groupUIDs1234 = new ArrayList<String>();
    private PoOrgStructure myStructure;

    private PoOrganisationService organisationService;
    private PoGroupDAO groupDao;
    
    private Date infinity = new Date(DateTools.INFINITY_TIMEMILLIS);
    
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {

		super.onSetUpBeforeDataGeneration();

		if (organisationService==null)
			this.organisationService = (PoOrganisationService) getBean("PoOrganisationService");

		if (groupDao == null)
			this.groupDao = (PoGroupDAO) getBean("PoGroupDAO");

		Calendar cal = GregorianCalendar.getInstance();
		past = cal.getTime();
		cal.add(Calendar.MONTH, 1);
		beforeStart = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		start = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		afterStart = cal.getTime();
		cal.add(Calendar.MONTH, 1);
		beforeEnd = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		end = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		afterEnd = cal.getTime();
		cal.add(Calendar.MONTH, 1);
		future = cal.getTime();
		
        // Mandant anlegen
        myClient = new PoClient();
		myClient.setName(MY_CLIENT_NAME);
		myClient.setDescription("mydescription");
		organisationService.saveClient(myClient);

		// OrgStructure anlegen
		myStructure = new PoOrgStructure();
		myStructure.setName(MY_STRUCTURE_NAME);
		myStructure.setHierarchy(true);
		myStructure.setAllowOnlySingleGroupMembership(true);
		myStructure.setOrgType(PoOrgStructure.STRUCTURE_TYPE_ORGANISATION_HIERARCHY);
		myStructure.setClient(myClient);
		organisationService.saveOrgStructure(myStructure);
		myClient.addOrgStructure(myStructure);
		
		// Gruppen anlegen
		
		myGroup = new PoGroup();
		myGroup.setName("group");
		myGroup.setShortName("short");
		myGroup.setClient(myClient);
		myGroup.setOrgStructure(myStructure);
		myStructure.addGroup(myGroup);
		myGroup.setValidfrom(past);
		myGroup.setValidto(future);
		myGroup.setDescription("g");
		groupDao.save(myGroup);

		// myGroup1 - gültig ab jetzt - bis zum INF-DATE
		myGroup1 = new PoGroup();
		myGroup1.setName("group1");
		myGroup1.setShortName("short1");
		// Link zum Mandant -> auf beiden Seiten
		myGroup1.setClient(myClient);
		// Link zur Struktur
		myGroup1.setOrgStructure(myStructure);
		myStructure.addGroup(myGroup1);
		Calendar time = GregorianCalendar.getInstance();
		time.add(Calendar.MONTH, 1);
		myGroup1.setValidfrom(time.getTime()); // now + 1 Month
		myGroup1.setDescription("g1");
		groupDao.save(myGroup1);
		groupUIDs1234.add(myGroup1.getUID());
		
		// myGroup2 - gültig ab jetzt bis INF-DATE
		
		myGroup2= new PoGroup();
		myGroup2.setName("group2");
		myGroup2.setShortName("short2");
		// Link zum Mandant -> auf beiden Seiten
		myGroup2.setClient(myClient);
		// Link zur Struktur
		myGroup2.setOrgStructure(myStructure);
		myStructure.addGroup(myGroup2);
		myGroup2.setValidfrom(new Date());
		time.add(Calendar.MONTH, 1);
		myGroup2.setValidto(time.getTime()); // now + 2 Months
		myGroup2.setDescription("g2");
		groupDao.save(myGroup2);
		groupUIDs1234.add(myGroup2.getUID());
		
		// Gruppe 3 - gültig ab jetzt bis inf-date
		myGroup3= new PoGroup();
		myGroup3.setName("group3");
		myGroup3.setShortName("short3");
		// Link zum Mandant -> auf beiden Seiten
		myGroup3.setClient(myClient);
//		myClient.addGroup(myGroup3);
		// Link zur Struktur
		myGroup3.setOrgStructure(myStructure);
		myStructure.addGroup(myGroup3);
		myGroup3.setValidfrom(new Date());
		myGroup3.setDescription("g3");
		groupDao.save(myGroup3);
		groupUIDs1234.add(myGroup3.getUID());
		
		// gruppe 4 - gültig ab jetzt - infDate
		myGroup4= new PoGroup();
		myGroup4.setName("myGroup4");
		myGroup4.setShortName("myShort4");
		// Link zum Mandant -> auf beiden Seiten
		myGroup4.setClient(myClient);
//		myClient.addGroup(myGroup4);
		// Link zur Struktur
		myGroup4.setOrgStructure(myStructure);
		myStructure.addGroup(myGroup4);
		myGroup4.setValidfrom(new Date());
		myGroup4.setDescription("g4");
		groupDao.save(myGroup4);
		groupUIDs1234.add(myGroup4.getUID());
		
		
		// gruppe 5 - gültig ab jetzt bis infDate
		GregorianCalendar gc = new GregorianCalendar();
		gc.add(Calendar.MONTH,1);
		myGroup5= new PoGroup();
		myGroup5.setName("myGroup5");
		myGroup5.setShortName("myShort5");
		// Link zum Mandant -> auf beiden Seiten
		myGroup5.setClient(myClient);
//		myClient.addGroup(myGroup5);
		// Link zur Struktur
		myGroup5.setOrgStructure(myStructure);
		myStructure.addGroup(myGroup5);
		myGroup5.setValidfrom(new Date());
		myGroup5.setDescription("g5");
		groupDao.save(myGroup5);
	}
	
	public void testFindGroupsWithFilterDateTime() {
	
		List<PoGroup> result = groupDao.findGroupsWithFilter(groupUIDs1234, null, null);
		assertNotNull(result);
		assertTrue(result.isEmpty());
		result = groupDao.findGroupsWithFilter(groupUIDs1234, null, new Date());
		assertNotNull(result);
		assertEquals(3, result.size());
		
		Calendar cal = GregorianCalendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		result = groupDao.findGroupsWithFilter(groupUIDs1234, null, cal.getTime());
		assertNotNull(result);
		assertEquals(4, result.size());
		
		cal.add(Calendar.MONTH, 1);
		result = groupDao.findGroupsWithFilter(groupUIDs1234, null, DateTools.lastMomentOfDay(cal.getTime()));
		assertNotNull(result);
		assertEquals(3, result.size());
		
	}
	
	public void testFindGroupsWithFilterDateGroups() {
		
		List<String> names = new ArrayList<String>();
		List<PoGroup> result = groupDao.findGroupsWithFilter(groupUIDs1234, names, new Date());
		assertNotNull(result);
		assertEquals(3, result.size());

		names.add("roup");
		result = groupDao.findGroupsWithFilter(groupUIDs1234, names, new Date());
		assertNotNull(result);
		assertEquals(3, result.size());
		
		names.add("grou");
		names.add("Grou");
		result = groupDao.findGroupsWithFilter(groupUIDs1234, names, new Date());
		assertNotNull(result);
		assertEquals(3, result.size());
		
		names.clear();
		names.add("xyz");
		result = groupDao.findGroupsWithFilter(groupUIDs1234, names, new Date());
		assertNotNull(result);
		assertEquals(0, result.size());
		
		names.clear();
		names.add("yGroup");
		result = groupDao.findGroupsWithFilter(groupUIDs1234, names, new Date());
		assertNotNull(result);
		assertEquals(1, result.size());
		
		names.clear();
		names.add("group");
		result = groupDao.findGroupsWithFilter(groupUIDs1234, names, new Date());
		assertNotNull(result);
		assertEquals(2, result.size());
		
		names.clear();
		names.add("group3");
		result = groupDao.findGroupsWithFilter(groupUIDs1234, names, new Date());
		assertNotNull(result);
		assertEquals(1, result.size());
		
		names.clear();
		names.add("shor");
		result = groupDao.findGroupsWithFilter(groupUIDs1234, names, new Date());
		assertNotNull(result);
		assertEquals(2, result.size());
		
		names.clear();
		names.add("yShor");
		result = groupDao.findGroupsWithFilter(groupUIDs1234, names, new Date());
		assertNotNull(result);
		assertEquals(1, result.size());
		
	}
	
	public void testFindGroupsWithFilterFromToTime() {
		
		Date now = new Date();
		
		List<PoGroup> result = groupDao.findGroupsWithFilter(groupUIDs1234, null, null, null);
		assertNotNull(result);
		assertTrue(result.isEmpty());
		result = groupDao.findGroupsWithFilter(groupUIDs1234, null, now, infinity);
		assertNotNull(result);
		assertEquals(4, result.size()); // TODO check: it should be only 2
		
		Calendar cal = GregorianCalendar.getInstance();
		cal.add(Calendar.MONTH, 1); // now + 1 Month
		result = groupDao.findGroupsWithFilter(groupUIDs1234, null, now, cal.getTime());
		assertNotNull(result);
		assertEquals(4, result.size());
		cal.add(Calendar.DAY_OF_MONTH, -1); // yesterday + 1 Month
		result = groupDao.findGroupsWithFilter(groupUIDs1234, null, now, cal.getTime());
		assertNotNull(result);
		assertEquals(3, result.size());
		
		cal.add(Calendar.DAY_OF_MONTH, 1); // now + 1 Month
		result = groupDao.findGroupsWithFilter(groupUIDs1234, null, cal.getTime(), infinity);
		assertNotNull(result);
		assertEquals(4, result.size()); // TODO check: this should be only 3
		
		cal.add(Calendar.MONTH, 1); // now + 2 Months
		result = groupDao.findGroupsWithFilter(groupUIDs1234, null, cal.getTime(), infinity);
		assertNotNull(result);
		assertEquals(4, result.size());
		cal.add(Calendar.DAY_OF_MONTH, 1); // tomorrow + 2 Months
		result = groupDao.findGroupsWithFilter(groupUIDs1234, null, cal.getTime(), infinity);
		assertNotNull(result);
		assertEquals(3, result.size()); // TODO check: this should be only 2
		
	}
	
	public void testFindGroupsWithFilterFromToGroups() {
		
		List<String> names = new ArrayList<String>();
		List<PoGroup> result = groupDao.findGroupsWithFilter(groupUIDs1234, names, new Date(), infinity);
		assertNotNull(result);
		assertEquals(4, result.size());
		
		names.add("roup");
		result = groupDao.findGroupsWithFilter(groupUIDs1234, names, new Date(), infinity);
		assertNotNull(result);
		assertEquals(4, result.size());
		
		names.add("grou");
		names.add("Grou");
		result = groupDao.findGroupsWithFilter(groupUIDs1234, names, new Date(), infinity);
		assertNotNull(result);
		assertEquals(4, result.size());
		
		names.clear();
		names.add("xyz");
		result = groupDao.findGroupsWithFilter(groupUIDs1234, names, new Date(), infinity);
		assertNotNull(result);
		assertEquals(0, result.size());
		
		names.clear();
		names.add("yGroup");
		result = groupDao.findGroupsWithFilter(groupUIDs1234, names, new Date(), infinity);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		names.clear();
		names.add("group");
		result = groupDao.findGroupsWithFilter(groupUIDs1234, names, new Date(), infinity);
		assertNotNull(result);
		assertEquals(3, result.size());
		
		names.clear();
		names.add("group3");
		result = groupDao.findGroupsWithFilter(groupUIDs1234, names, new Date(), infinity);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		names.clear();
		names.add("shor");
		result = groupDao.findGroupsWithFilter(groupUIDs1234, names, new Date(), infinity);
		assertNotNull(result);
		assertEquals(3, result.size());
		
		names.clear();
		names.add("yShor");
		result = groupDao.findGroupsWithFilter(groupUIDs1234, names, new Date(), infinity);
		assertNotNull(result);
		assertEquals(1, result.size());
		
	}
	
	public void testFindPersonsOfGroup() {
		
		PoPerson testPerson = createTestPerson(start, end); // so that it is valid until end 23:59:59

		organisationService.savePerson(testPerson, myGroup);
		
		List<PoPerson> result = groupDao.findPersonsOfGroup(myGroup1, beforeStart);
		assertNotNull(result);
		assertEquals(0, result.size());
		
		result = groupDao.findPersonsOfGroup(myGroup, start);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = groupDao.findPersonsOfGroup(myGroup, afterStart);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = groupDao.findPersonsOfGroup(myGroup, beforeEnd);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = groupDao.findPersonsOfGroup(myGroup, end);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = groupDao.findPersonsOfGroup(myGroup, afterEnd);
		assertNotNull(result);
		assertEquals(0, result.size());
	}
	
	
	public void testFindPersonsOfGroupWithInterval() {
		
		PoPerson testPerson = createTestPerson(start, end); // so that it is valid until end 23:59:59

		organisationService.savePerson(testPerson, myGroup);
		
		List<PoPerson> result = groupDao.findPersonsOfGroup(myGroup, past, beforeStart);
		assertNotNull(result);
		assertEquals(0, result.size());
		
		result = groupDao.findPersonsOfGroup(myGroup, past, beforeStart);
		assertNotNull(result);
		assertEquals(0, result.size());
		
		result = groupDao.findPersonsOfGroup(myGroup, past, start);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = groupDao.findPersonsOfGroup(myGroup, past, afterStart);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = groupDao.findPersonsOfGroup(myGroup, start, end);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = groupDao.findPersonsOfGroup(myGroup, afterStart, beforeEnd);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = groupDao.findPersonsOfGroup(myGroup, past, future);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = groupDao.findPersonsOfGroup(myGroup, beforeEnd, future);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = groupDao.findPersonsOfGroup(myGroup, end, future);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = groupDao.findPersonsOfGroup(myGroup, afterEnd, future);
		assertNotNull(result);
		assertEquals(0, result.size());
	}
	
	public void testFindPersonsOfGroupWithIntervalDistinct() {
		PoPerson testPerson = createTestPerson(beforeStart, afterEnd);
		
		organisationService.savePerson(testPerson, myGroup);

		// reassign to other group to produce 2 separate associations to myGroup
		organisationService.linkPerson2Group(testPerson, myGroup1, afterStart, beforeEnd);
		
		List<PoPerson> result = groupDao.findPersonsOfGroup(myGroup, beforeStart, afterEnd);
		assertNotNull(result);
		assertEquals(1, result.size());
		
	}

	private PoPerson createTestPerson(Date start, Date end) {
		PoPerson testPerson = new PoPerson();
		testPerson.setClient(myClient);
		testPerson.setFirstName("Test");
		testPerson.setLastName("Person");
		testPerson.setUserName("tperson");
		testPerson.setEmployeeId("4711");
		testPerson.setValidfrom(start);
		testPerson.setValidto(end);
		return testPerson;
	}
}
