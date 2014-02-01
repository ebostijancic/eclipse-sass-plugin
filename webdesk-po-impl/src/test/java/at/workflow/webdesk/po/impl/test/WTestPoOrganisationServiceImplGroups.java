package at.workflow.webdesk.po.impl.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;

import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoParentGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoPersonGroup;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

/**
 * @author ggruber
 * @author hentner
 */
public class WTestPoOrganisationServiceImplGroups extends AbstractTransactionalSpringHsqlDbTestCase {
    
    final static String MY_CLIENT_NAME = "Testmandant";
    final static String MY_STRUCTURE_NAME = "Kostenstellen";
    
    private PoClient myClient;
    private PoGroup myGroup1;
    private PoGroup myGroup2;
    private PoGroup myGroup3;
    private PoGroup myGroup4;
    private PoGroup myGroup5;
    private PoPerson myPerson;
    private PoOrgStructure myStructure;

    private PoOrganisationService organisationService;
    
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {

		super.onSetUpBeforeDataGeneration();

		if (organisationService==null)
			this.organisationService = (PoOrganisationService) getBean("PoOrganisationService");

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
		
		// myGroup1 - gültig ab jetzt - bis zum INF-DATE
		myGroup1 = new PoGroup();
		myGroup1.setName("group1");
		myGroup1.setShortName("short1");
		// Link zum Mandant -> auf beiden Seiten
		myGroup1.setClient(myClient);
//		myClient.addGroup(myGroup1);
		// Link zur Struktur
		myGroup1.setOrgStructure(myStructure);
		myStructure.addGroup(myGroup1);
		Calendar now = new GregorianCalendar();
		now.add(Calendar.SECOND,-2);
		myGroup1.setValidfrom(now.getTime());
		myGroup1.setDescription("g1");
		organisationService.saveGroup(myGroup1);
		
		
		myPerson = new PoPerson();
		// Link auf beiden Seiten
		myPerson.setClient(myClient);
//		myClient.addPersons(myPerson);
		myPerson.setFirstName("Lisi");
		myPerson.setLastName("MÜLLER");
		myPerson.setUserName("user");
		myPerson.setTaID("1001");
		myPerson.setEmployeeId("00004711");
		myPerson.setValidfrom(new Date());
		myPerson.setValidto(PoConstants.getInfDate());
		organisationService.savePerson(myPerson, myGroup1);
		
		
		// myGroup2 - gültig ab jetzt bis INF-DATE
		
		myGroup2= new PoGroup();
		myGroup2.setName("group2");
		myGroup2.setShortName("short2");
		// Link zum Mandant -> auf beiden Seiten
		myGroup2.setClient(myClient);
//		myClient.addGroup(myGroup2);
		// Link zur Struktur
		myGroup2.setOrgStructure(myStructure);
		myStructure.addGroup(myGroup2);
		myGroup2.setValidfrom(DateTools.yesterday()); // otherwise it would be deleted in the delete test
		myGroup2.setDescription("g2");
		organisationService.saveGroup(myGroup2);
		
		
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
		organisationService.saveGroup(myGroup3);
		
		// gruppe 4 - gültig ab jetzt - infDate
		
		myGroup4= new PoGroup();
		myGroup4.setName("group4");
		myGroup4.setShortName("short4");
		// Link zum Mandant -> auf beiden Seiten
		myGroup4.setClient(myClient);
//		myClient.addGroup(myGroup4);
		// Link zur Struktur
		myGroup4.setOrgStructure(myStructure);
		myStructure.addGroup(myGroup4);
		myGroup4.setValidfrom(new Date());
		myGroup4.setDescription("g4");
		organisationService.saveGroup(myGroup4);
		
		
		// gruppe 5 - gültig ab jetzt bis infDate
		GregorianCalendar gc = new GregorianCalendar();
		gc.add(Calendar.MONTH,1);
		myGroup5= new PoGroup();
		myGroup5.setName("group5");
		myGroup5.setShortName("short5");
		// Link zum Mandant -> auf beiden Seiten
		myGroup5.setClient(myClient);
//		myClient.addGroup(myGroup5);
		// Link zur Struktur
		myGroup5.setOrgStructure(myStructure);
		myStructure.addGroup(myGroup5);
		myGroup5.setValidfrom(gc.getTime());
		myGroup5.setDescription("g5");
		organisationService.saveGroup(myGroup5);
		
		
		/*
		
		// myGroup1 ist Parent der tmpGroup (gültig in einem monat bis inf)
		// link ist gültig ab (heute +1Monat) bis infDate
		
		myuid = myGroup1.getUID();
		// 2. Gruppe - erst in einem Monat gültig !
		 * 
		PoGroup tmpGroup = new PoGroup();
		tmpGroup.setClient(myClient);
		tmpGroup.setName(TestPoGroupDAOImpl.MY_GROUP_NAME_2);
		tmpGroup.setOrgStructure(myStructure);
		now.add(Calendar.MONTH,1);
		tmpGroup.setValidfrom(now.getTime());
		poOrganisationService.saveGroup(tmpGroup);
		poOrganisationService.setParentGroup(tmpGroup,myGroup1,now.getTime(),null); // Zuordnung sollte auch erst in einem Monat gültig sein.
		
		
		// 3. Gruppe - in 1 Sekunde gültig
		// myGroup ist parent der tmpGroup (gültig ein einer sekunde bis infDate)
		now.add(Calendar.MONTH,-1);
		now.add(Calendar.SECOND,1);
		
		tmpGroup = new PoGroup();
		tmpGroup.setClient(myClient);
		tmpGroup.setName(TestPoGroupDAOImpl.MY_GROUP_NAME_3);
		tmpGroup.setOrgStructure(myStructure);
		tmpGroup.setValidfrom(now.getTime());
		poOrganisationService.saveGroup(tmpGroup);
		poOrganisationService.setParentGroup(tmpGroup,myGroup1,now.getTime(),null);
		
		
		// 4. Gruppe in 2 Sekunden gültig
		// tmpGroup ist parent der tmpGroup2 (link in zwei sekunden gültig)
		now.add(Calendar.SECOND,1);
		PoGroup tmpGroup2 = new PoGroup();
		tmpGroup2.setClient(myClient);
		tmpGroup2.setName(TestPoGroupDAOImpl.MY_GROUP_NAME_4);
		tmpGroup2.setOrgStructure(myStructure);
		tmpGroup2.setValidfrom(now.getTime());
		poOrganisationService.saveGroup(tmpGroup2);
		poOrganisationService.setParentGroup(tmpGroup2,tmpGroup, now.getTime(),null);
		*/
		
		/***********************************NEU**************************/
		// tmpGroup1 ist in einem Monat gültig.
		// myGroup1 ist parent der neuen tmpGroup
		/*
		Calendar hTime = new GregorianCalendar();
		hTime.setTime(new Date());
		hTime.add(Calendar.MONTH,+1);
		
		PoGroup tmpGroup1 = new PoGroup();
		tmpGroup1.setClient(myClient);
		tmpGroup1.setName("test");
		tmpGroup1.setOrgStructure(myStructure);
		tmpGroup1.setValidfrom(hTime.getTime());
		poOrganisationService.saveGroup(tmpGroup1);
		poOrganisationService.setParentGroup(tmpGroup1,myGroup1,hTime.getTime(),null);
		*/
		/*
		 * 		myGroup1 myGroup2 myGroup3 myGroup4
		 * 		|	|  |
		 * 		|   |  |			
		 *tmpgroup1 |tmpGroup(name=test, valid +1Monat)
		 *(+1M)		|
		 * 		tmpgroup2
		 * 			|
		 * 		tmpgroup
		 * 
		 * 
		 */
		
		
	}

	/**
	 * Tests various methods
	 * (SaveGroup, findGroupByName, ...)
	 * This test case could fail if more than one group with
	 * the same name exists.
	 */
	
	public void testSaveGroup() {
		
		PoGroup newGroup = new PoGroup();
		newGroup.setName("group6");
		newGroup.setShortName("short6");
		newGroup.setDescription("CC Lotus Notes/E-commerce");
		newGroup.setOrgStructure(myStructure);
		newGroup.setClient(myClient);
		myStructure.addGroup(newGroup);
		organisationService.saveGroup(newGroup);
		
		List<PoGroup> ret = new ArrayList<PoGroup>();
		ret = organisationService.findGroupByName("group6");
		if (ret.size()>=1){
			try {
			    organisationService.deleteAndFlushGroup(newGroup);
			} catch (Exception e) {
				fail(e.getMessage());
			}		    
		} else {
		    organisationService.deleteAndFlushGroup(newGroup);
			fail("saved Group could not be retrieved again from database");
		}
	}
	
//	ggruber 29072012 - this test should be verified with fritz ritzberger!
	
//	public void testSaveGroupAndTestConstraints() {
//		
//		PoGroup newGroup = new PoGroup();
//		newGroup.setName("group7");
//		newGroup.setShortName("short7");
//		newGroup.setOrgStructure(myStructure);
//		newGroup.setClient(myClient);
//		myStructure.addGroup(newGroup);
//		organisationService.saveGroup(newGroup);
//		
//		PoGroup otherGroup = organisationService.getGroup(newGroup.getUID());
//		otherGroup.setName("group7_1");
//		organisationService.saveGroup(otherGroup);
//
//	}
	

	public void testGetGroup() {
	    PoGroup tmpGroup = organisationService.getGroup(myGroup1.getUID());
	    assertEquals(tmpGroup,myGroup1);
	}

	/**
	 * Tests if the delete method works. In reality the Group is not 
	 * really deleted from the database, but its Historization fields 
	 * (validFrom, validTo) are set to the past.
	 */
	public void testDeleteGroup() {
	    organisationService.deleteGroup(myGroup2);	  
	    // wenn es das Objekt nach wie vor gibt
	    assertNotNull(organisationService.getGroup(myGroup2.getUID()));
	}

	/**
	 * Tests if the groups are really deleted from database.
	 */
	public void testDeleteAndFlushGroup() {
	    // lösche eine Abteilung ohne Childs
		PoGroup newGroup = new PoGroup();
		newGroup.setName("g6");
		newGroup.setShortName("short6");
		newGroup.setDescription("CC Lotus Notes/E-commerce");
		newGroup.setOrgStructure(myStructure);
		newGroup.setClient(myClient);
		myStructure.addGroup(newGroup);
		organisationService.saveGroup(newGroup);
		
	    List<PoGroup> ret = organisationService.findGroupByName("g6");
	    int size = organisationService.loadAllGroups().size();
	    organisationService.deleteAndFlushGroup(ret.get(0));
	    assertEquals(size - 1, organisationService.loadAllGroups().size());
	}

	/**
	 *  Tests if the findGroupByName function works.
	 */
	public void testFindGroupByNameString() {
	    List<PoGroup> ret = organisationService.findGroupByName("group1");
	    assertNotNull(ret);
	    assertFalse(ret.isEmpty());
	}

	/**
	 * Tests if the findGroupByName function works with an 
	 * additional client restrictions.
	 */
	public void testFindGroupByNameStringPoClient() {	    
	    PoGroup group = organisationService.findGroupByName("group1",myClient);
	    assertNotNull(group);
	}

	/**
	 *  Tests if the findGroupByName function works with an additional 
	 * restriction of a given date.
	 */
	public void testFindGroupByNameStringDate() {
	    List<PoGroup> ret = organisationService.findGroupByName("group2",new Date());
	    assertEquals(ret.get(0),myGroup2);
	}


	/**
	 *  Tests if the findGroupByName function works, this time with client and 
	 *  date restrictions.
	 */
	public void testFindGroupByNameStringPoClientDate() {
	    Date referenceDate = new Date();
	    PoGroup group = organisationService.findGroupByName("group2",myClient,referenceDate);
	    assertEquals(group,myGroup2);
	}

	
	/**
	 * Tests if the getAllChildGroupsFlat method works.
	 * 
	 */
	
	public void testGetAllChildGroupsFlatPoGroup() {
		organisationService.setParentGroup(myGroup2,myGroup1);
		organisationService.setParentGroup(myGroup3,myGroup1);
		organisationService.setParentGroup(myGroup4,myGroup1);
		organisationService.setParentGroup(myGroup3,myGroup1);
		
	    List<PoGroup> ret = organisationService.findAllChildGroupsFlat(myGroup1);
	    
	    // group1 hat HEUTE 3 unterabteilungen
	    assertEquals(ret.size(),3);
	    assertTrue(ret.contains(myGroup2));
	    assertTrue(ret.contains(myGroup3));
	    assertTrue(ret.contains(myGroup4));
	}


	/*
	 *  Test if the getAllChildGroupsFlat method works.
	*/
	public void testGetAllChildGroupsFlatPoGroupDate() {
		
		organisationService.setParentGroup(myGroup2,myGroup1);
		organisationService.setParentGroup(myGroup3,myGroup1);

	    Calendar nextMonth = new GregorianCalendar();
	    nextMonth.add(Calendar.MONTH,1);
	    nextMonth.add(Calendar.MINUTE,4);	    
	    List<PoGroup> ret = organisationService.findAllChildGroupsFlat(myGroup1, nextMonth.getTime());	    
	    // abt 4711 hat in einem Monat 3 unterabteilungen (2 direkte, 1 indirekte)
	    assertEquals(ret.size(),2);
	    assertTrue(ret.contains(myGroup2));
	    assertTrue(ret.contains(myGroup3));
	}

	public void testGetParentGroup() {
	    
	   organisationService.setParentGroup(myGroup4,myGroup1); 
	    // 3. Gruppe ist Childgroup von 1. Gruppe (in myGroup gespeichert)
	    
	    boolean found = false;
	    Iterator<PoParentGroup> iter = myGroup1.getChildGroups().iterator();
	    while (iter.hasNext()) {
	    	PoParentGroup pg = iter.next();
	    	if (pg == organisationService.getParentGroup(myGroup4)) 
	    		found = true;
	    }
	    assertTrue(found);
	}

	public void testLoadAllGroups() {
	    List<PoGroup> ret = organisationService.loadAllGroups();
	    assertTrue("couldn't load groups out of database...", ret.size() >= 1);    
	}	
	
	public void testSetParentGroupPoGroupPoGroup() {
	    // Gruppe 4 hängt nach Setup() auf Gruppe 3
	    // hänge die Gruppe 4 auch auf die Gruppe 1
	    // nun mus die Verbindung zu Gruppe 1 historisiert werden
	    // und eine 2. Verbindung eröffnet werden.
	    organisationService.setParentGroup( myGroup4,myGroup1);
	    PoParentGroup parent = organisationService.getParentGroup(myGroup4);
	    assertNotNull(parent);
	    assertEquals(myGroup1, parent.getParentGroup());
	    assertEquals(myGroup4, parent.getChildGroup());
	}	
	
	public void testSetParentGroupPoGroupPoGroupDate() {
	    // Gruppe 4 hängt nach Setup() auf Gruppe 3
	    // hänge nun die Gruppe per Stichtag heute + 1 Monat auf die Gruppe 1
	    Calendar nextMonth = new GregorianCalendar();
	    nextMonth.add(Calendar.MONTH,2);
	    Calendar hTime = new GregorianCalendar();
	    hTime.setTime(nextMonth.getTime());
	    hTime.add(Calendar.HOUR,+1);
	    // setze ParentGroup per Heute + 1 Monat
	    organisationService.setParentGroup(myGroup4, myGroup1, nextMonth.getTime(),null);
	    PoParentGroup parent = organisationService.getParentGroup(myGroup4, hTime.getTime());
	    assertEquals(myGroup1, parent.getParentGroup());
	}
	
	public void testFindChildGroups() {
		 
		Calendar hTime = new GregorianCalendar();
		 hTime.setTime(new Date());
		 hTime.add(Calendar.MONTH,+6);
		 Date secondDate = hTime.getTime();
		 organisationService.setParentGroup(myGroup3,myGroup1,hTime.getTime(),null);
		 hTime.add(Calendar.MONTH,-3);
		 Date firstDate = hTime.getTime();
		 organisationService.setParentGroup(myGroup4,myGroup1,hTime.getTime(),null);
		 
		 assertEquals(0, organisationService.findChildGroups(myGroup1).size());
		 assertEquals(0, organisationService.findChildGroups(myGroup1, new Date()).size());
		 assertEquals(1, organisationService.findChildGroups(myGroup1, firstDate).size());
		 assertEquals(2, organisationService.findChildGroups(myGroup1, secondDate).size());
		 
		 assertEquals(2, organisationService.findChildGroupsF(myGroup1, new Date()).size());
	}	
	
	/*
	 * Gibt die untergeordneten Childgroups zurück (nur eine Ebene)
	 * gültig für das aktuelle Datum von den Parentgroups (Tabelle)
	*/
	public void testGetChildGroupsPoGroup() {
		organisationService.setParentGroup(myGroup2,myGroup1);
		Collection<PoParentGroup> l = organisationService.findChildGroups(myGroup1);
		assertEquals(l.size(),1);	    
	}
	
	public void testGetChildGroupsPoGroupDate() {
		Calendar hTime = new GregorianCalendar();
	    hTime.add(Calendar.MONTH,+1);
	    organisationService.setParentGroup(myGroup4,myGroup1);
		Collection<PoParentGroup> l = organisationService.findChildGroups(myGroup1,hTime.getTime());
		assertEquals(l.size(),1);
	}
	
	public void testModelGetChilds() {
		organisationService.setParentGroup(myGroup2,myGroup1);
		organisationService.setParentGroup(myGroup3,myGroup1);
		organisationService.setParentGroup(myGroup4,myGroup1);

		Collection<PoParentGroup>  l =  myGroup1.getChildGroups();
		assertEquals(l.size(),3);
	}
	
	public void testFindChildGroupsAll() {

		List<PoParentGroup> result = organisationService.findChildGroups(myGroup4);
		assertNotNull(result);
		assertEquals(0, result.size());
		
		result = organisationService.findChildGroupsAll(myGroup4);
		assertNotNull(result);
		assertEquals(0, result.size());
		
		organisationService.setParentGroup(myGroup1, myGroup4);
		organisationService.setParentGroup(myGroup2, myGroup4);

		result = organisationService.findChildGroups(myGroup4);
		assertNotNull(result);
		assertEquals(2, result.size());

		result = organisationService.findChildGroupsAll(myGroup4);
		assertNotNull(result);
		assertEquals(2, result.size());
		
		organisationService.changeValidityParentGroupLink(result.get(0), null, new Date());
		organisationService.changeValidityParentGroupLink(result.get(1), null, new Date());
		
		result = organisationService.findChildGroups(myGroup4, DateTools.tomorrow());
		assertNotNull(result);
		assertEquals(0, result.size());
		
		result = organisationService.findChildGroupsAll(myGroup4);
		assertNotNull(result);
		assertEquals(2, result.size());
		
		organisationService.setParentGroup(myGroup3, myGroup4, DateTools.tomorrow(), null);
		
		result = organisationService.findChildGroups(myGroup4, DateTools.tomorrow());
		assertNotNull(result);
		assertEquals(1, result.size());
		
		result = organisationService.findChildGroupsAll(myGroup4);
		assertNotNull(result);
		assertEquals(3, result.size());
		
		// test of ordering
		organisationService.setParentGroup(myGroup1, myGroup4, DateTools.tomorrow(), null);

		result = organisationService.findChildGroupsAll(myGroup4);
		assertNotNull(result);
		assertEquals(4, result.size());
		assertEquals(myGroup1, result.get(0).getChildGroup());
		assertEquals(myGroup1, result.get(1).getChildGroup());
		
		assertEquals(myGroup2, result.get(2).getChildGroup());
		assertEquals(myGroup3, result.get(3).getChildGroup());
	}
	
	public void testChangeValidityParentGroupLinkToLessThanDay () {
		
		PoParentGroup result = organisationService.getParentGroup(myGroup4);
		assertNull(result);
		
		List<PoParentGroup> list = organisationService.findParentGroupsAll(myGroup4);
		assertNotNull(list);
		assertEquals(0, list.size());
		
		organisationService.setParentGroup(myGroup4, myGroup1);
		
		list = organisationService.findParentGroupsAll(myGroup4);
		assertNotNull(list);
		assertEquals(1, list.size());
		
		result = organisationService.getParentGroup(myGroup4);
		assertNotNull(result);
		
		organisationService.changeValidityParentGroupLink(result, DateTools.today(), DateTools.yesterday());

		try {
			Thread.sleep(20);	// TODO please comment why this is done here
		} catch (InterruptedException e) {
		}
		
		result = organisationService.getParentGroup(myGroup4);
		assertNull(result);
		
		list = organisationService.findParentGroupsAll(myGroup4);
		assertNotNull(list);
		assertEquals(0, list.size());
		
	}
	
	public void testFindParentGroupsAll () {
		
		PoParentGroup result = organisationService.getParentGroup(myGroup4);
		assertNull(result);
		
		List<PoParentGroup> list = organisationService.findParentGroupsAll(myGroup4);
		assertNotNull(list);
		assertEquals(0, list.size());
		
		organisationService.setParentGroup(myGroup4, myGroup1);
		
		result = organisationService.getParentGroup(myGroup4);
		assertNotNull(result);
		
		list = organisationService.findParentGroupsAll(myGroup4);
		assertNotNull(list);
		assertEquals(1, list.size());
		
		organisationService.changeValidityParentGroupLink(result, null, new Date());

		try {
			Thread.sleep(20);	// TODO please comment why this is done here
		} catch (InterruptedException e) {
		}
		
		organisationService.setParentGroup(myGroup4, myGroup2, DateTools.tomorrow(), null);
		
		result = organisationService.getParentGroup(myGroup4);
		assertNotNull(result);
		
		list = organisationService.findParentGroupsAll(myGroup4);
		assertNotNull(list);
		assertEquals(2, list.size());
		assertEquals(myGroup2, list.get(0).getParentGroup());
		assertEquals(myGroup1, list.get(1).getParentGroup());
		
	}
	
	public void testFindPersonGroupsAll() throws Exception {

		List<PoPersonGroup> personToGroupRelations = organisationService.findPersonGroupsF(myGroup1, new Date());
		assertNotNull(personToGroupRelations);
		assertEquals(1, personToGroupRelations.size());
		
		personToGroupRelations = organisationService.findPersonGroupsAll(myGroup1);
		assertNotNull(personToGroupRelations);
		assertEquals(1, personToGroupRelations.size());
		
		// fri_2012-03-16: sleeping to make DAO perform a historice() and not a delete()
		// see comment about failing unit test below
		// sdz_2013-12-11: the problem was in the use of new Date(), reworked to specific days now
//		Thread.sleep(HistorizationHelper.RECOMMENDED_MINIMAL_OBJECT_LIFETIME_MILLIS);
		
		// remove myPerson from its current group myGroup1 and add it to myGroup2
		organisationService.linkPerson2Group(myPerson, myGroup2, DateTools.tomorrow(), null);	// removes myPerson from myGroup1 and adds it to myGroup2

		// check that person is not more member of myGroup1
		personToGroupRelations = organisationService.findPersonGroupsF(myGroup1, DateTools.tomorrow());
		assertNotNull(personToGroupRelations);
		assertEquals(0, personToGroupRelations.size());

		// check that the expired relation between myPerson and myGroup1 is still stored as history
		personToGroupRelations = organisationService.findPersonGroupsAll(myGroup1);	// finds ALL objects, not only valid ones
		assertNotNull(personToGroupRelations);
		assertEquals(1, personToGroupRelations.size());
		// fri_2012-03-14 this assert seems to be instable: "expected:<1> but was:<0>",
		// failed on build-server, succeeded on my machine in both Eclipse and Maven Konsole.
		// fri_2012-03-16 is now permanently failing on build-server! Still succeeds locally.
		// fri_2012-03-2 hopefully fixed by a sleep, see comment above! Reason was new historicize() implementation in HistoricizingDAOImpl.java.
		
		PoPerson myPerson2 = new PoPerson();
		// Link auf beiden Seiten
		myPerson2.setClient(myClient);
//		myClient.addPersons(myPerson);
		myPerson2.setFirstName("Peter");
		myPerson2.setLastName("Weiss");
		myPerson2.setUserName("user2");
		myPerson2.setTaID("1002");
		myPerson2.setEmployeeId("00004712");
		myPerson2.setValidfrom(new Date());
		myPerson2.setValidto(PoConstants.getInfDate());
		organisationService.savePerson(myPerson2, myGroup1);
		
		personToGroupRelations = organisationService.findPersonGroupsF(myGroup1, DateTools.tomorrow());
		assertNotNull(personToGroupRelations);
		assertEquals(1, personToGroupRelations.size());
		
		personToGroupRelations = organisationService.findPersonGroupsAll(myGroup1);
		assertNotNull(personToGroupRelations);
		assertEquals(2, personToGroupRelations.size());
		
		organisationService.linkPerson2Group(myPerson, myGroup1, DateUtils.addDays(DateTools.today(), 2), null);

		personToGroupRelations = organisationService.findPersonGroupsAll(myGroup1);
		assertNotNull(personToGroupRelations);
		assertEquals(3, personToGroupRelations.size());
		assertEquals(myPerson, personToGroupRelations.get(0).getPerson());
		assertEquals(myPerson, personToGroupRelations.get(1).getPerson());
		assertTrue(personToGroupRelations.get(0).getValidfrom().after(personToGroupRelations.get(1).getValidfrom()));
		assertEquals(myPerson2, personToGroupRelations.get(2).getPerson());
	}
	
}
