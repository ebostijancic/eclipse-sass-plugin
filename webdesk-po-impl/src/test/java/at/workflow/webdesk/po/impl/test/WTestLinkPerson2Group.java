package at.workflow.webdesk.po.impl.test;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoPersonGroup;
import at.workflow.webdesk.po.timeline.HistorizationTimelineUtils;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

public class WTestLinkPerson2Group extends AbstractTransactionalSpringHsqlDbTestCase {

	private class PoPersonGroupComparator implements Comparator<PoPersonGroup> {

		@Override
		public int compare(PoPersonGroup pg1, PoPersonGroup pg2) {
			return pg1.getValidfrom().compareTo(pg2.getValidto());
		}
	}
	
	final static String MY_CLIENT_NAME="Testmandant";
	final static String MY_STRUCTURE_NAME="Kostenstellen";
	final static String MY_GROUP_NAME_1=  "group1";
	final static String MY_GROUP_NAME_2 = "group2";
	final static String MY_GROUP_NAME_3 = "group3";
	final static String MY_GROUP_NAME_4 = "group4";
	final static String MY_GROUP_NAME_5 = "group5";

	private PoClient myClient;
	private PoGroup myGroup1;
	private PoGroup myGroup2;
	private PoGroup myGroup3;
	private PoGroup myGroup4;
	private PoGroup myGroup5;

	private PoPerson myPerson;

	private PoOrganisationService organisationService;
	private PoOrgStructure myStructure;

	private Date now = DateTools.dateOnly(new Date());
	private Date infDate = new Date(DateTools.INFINITY_TIMEMILLIS);

	private Calendar gcFrom = GregorianCalendar.getInstance();
	private Calendar gcTo = GregorianCalendar.getInstance();



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
		// ####################		
		myStructure = new PoOrgStructure();
		myStructure.setName(MY_STRUCTURE_NAME);
		myStructure.setHierarchy(true);
		myStructure.setAllowOnlySingleGroupMembership(true);
		myStructure.setOrgType(PoOrgStructure.STRUCTURE_TYPE_ORGANISATION_HIERARCHY);
		myStructure.setClient(myClient);
		organisationService.saveOrgStructure(myStructure);
		myClient.addOrgStructure(myStructure);

		// Gruppe anlegen
		// ####################	
		// myGroup1 - gültig ab jetzt - bis zum INF-DATE
		myGroup1 = new PoGroup();
		myGroup1.setClient(myClient);
		myGroup1.setOrgStructure(myStructure);
		myGroup1.setName("g1");
		myGroup1.setShortName("g1");
		myGroup1.setValidfrom(now);
		organisationService.saveGroup(myGroup1);


		myPerson = new PoPerson();
		// Link auf beiden Seiten
		myPerson.setClient(myClient);
		myPerson.setFirstName("Lisi");
		myPerson.setLastName("MÜLLER");
		myPerson.setTaID("1001");
		myPerson.setEmployeeId("00004711");
		myPerson.setValidfrom(now);
		myPerson.setValidto(infDate);
		myPerson.setUserName("lisimüller");
		organisationService.savePerson(myPerson, myGroup1);


		// myGroup2 - gültig ab jetzt bis INF-DATE

		myGroup2= new PoGroup();
		myGroup2.setOrgStructure(myStructure);
		myGroup2.setName("g2");
		myGroup2.setShortName("g2");
		myGroup2.setClient(myClient);
		myGroup2.setValidfrom(now);
		organisationService.saveGroup(myGroup2);


		// Gruppe 3 - gültig ab jetzt bis inf-date
		myGroup3= new PoGroup();
		// Link zum Mandant -> auf beiden Seiten
		myGroup3.setClient(myClient);
		// Link zur Struktur
		myGroup3.setOrgStructure(myStructure);
		myGroup3.setName("g3");
		myGroup3.setShortName("g3");
		myGroup3.setValidfrom(now);
		organisationService.saveGroup(myGroup3);

		// gruppe 4 - gültig ab jetzt - infDate

		myGroup4= new PoGroup();
		// Link zum Mandant -> auf beiden Seiten
		myGroup4.setClient(myClient);
		myGroup4.setOrgStructure(myStructure);
		myGroup4.setName("g4");
		myGroup4.setShortName("g4");
		myGroup4.setValidfrom(now);
		organisationService.saveGroup(myGroup4);


		// gruppe 5 - gültig ab jetzt bis infDate
		GregorianCalendar gc = new GregorianCalendar();
		gc.add(Calendar.MONTH,1);
		myGroup5= new PoGroup();
		// Link zum Mandant -> auf beiden Seiten
		myGroup5.setClient(myClient);
		myGroup5.setOrgStructure(myStructure);
		myGroup5.setName("g5");
		myGroup5.setShortName("g5");
		myGroup5.setValidfrom(now);
		organisationService.saveGroup(myGroup5);

	}

	public void testLinkPerson2GroupBasicAssignment() {

		List<PoPersonGroup> poGroups = organisationService.getPersonGroups(myPerson , myStructure, now);

		assertNotNull(poGroups);
		assertEquals(1, poGroups.size());
		assertEquals(myGroup1, poGroups.get(0).getGroup());
		assertEquals(myPerson, poGroups.get(0).getPerson());
		assertTrue(equalsTo(setTime000000(gcFrom), poGroups.get(0).getValidfrom()));
		assertEquals(infDate, poGroups.get(0).getValidto());
		
		HistorizationTimelineUtils.checkTimeline(poGroups, myPerson.getValidity());
	}
	
	public void testLinkPerson2GroupSameBeginningSameEnd() {

		//  1.  |========================================|
		//	2.  |========================================| 
		
		gcFrom.setTime(now);
		gcTo.setTimeInMillis(DateTools.INFINITY_TIMEMILLIS);

		organisationService.linkPerson2Group(myPerson, myGroup2, gcFrom.getTime() ,gcTo.getTime());
		List<PoPersonGroup> poGroups = organisationService.findPersonGroupsF(myPerson, now, myStructure);

		assertReplacedGroup(poGroups);

		poGroups = organisationService.findPersonGroupsAll(myPerson, myGroup1.getOrgStructure());
		
		assertReplacedGroup(poGroups);
		
		HistorizationTimelineUtils.checkTimeline(poGroups, myPerson.getValidity());
	}

	private void assertReplacedGroup(List<PoPersonGroup> poGroups) {
		
		assertNotNull(poGroups);
		assertEquals(1, poGroups.size());
		PoPersonGroup poPersonGroup = poGroups.get(0);
		assertEquals(myGroup2, poPersonGroup.getGroup());
		assertEquals(myPerson, poPersonGroup.getPerson());
		assertEquals(setTime000000(gcFrom), poPersonGroup.getValidfrom());
		assertTrue(equalsTo(gcTo.getTime(), poPersonGroup.getValidto()));
	}
	
	public void testLinkPerson2GroupSameBeginning() {

		//  1.  |========================================| 
		//	2.  |=================|
		
		gcFrom.setTime(now);
		gcTo.setTime(now);
		gcTo.add(Calendar.DAY_OF_MONTH, 20);
		Set<PoGroup> groups = new HashSet<PoGroup>();
		groups.add(myGroup1);
		groups.add(myGroup2);

		organisationService.linkPerson2Group(myPerson, myGroup2, gcFrom.getTime(), gcTo.getTime());
		List<PoPersonGroup> poGroups = organisationService.findPersonGroupsF(myPerson, now, myStructure);

		assertNotNull(poGroups);
		assertEquals(2, poGroups.size());
		assertTrue(groups.contains(poGroups.get(0).getGroup()));
		assertEquals(myPerson, poGroups.get(0).getPerson());
		assertTrue(groups.contains(poGroups.get(1).getGroup()));
		assertEquals(myPerson, poGroups.get(1).getPerson());

		PoPersonGroup oldPg = null, newPg = null;
		if (poGroups.get(0).getGroup().equals(myGroup2)) {
			oldPg = poGroups.get(1);
			newPg = poGroups.get(0);
		} else {
			oldPg = poGroups.get(0);
			newPg = poGroups.get(1);
		}
		assertEquals(gcFrom.getTime(), newPg.getValidfrom());
		assertTrue(equalsTo(setTime235959(gcTo), newPg.getValidto()));
		assertEquals(setTime000000NextDay(gcTo), oldPg.getValidfrom());
		assertEquals(infDate, oldPg.getValidto());
		
		HistorizationTimelineUtils.checkTimeline(poGroups, myPerson.getValidity());

	}

	public void testLinkPerson2GroupSameEnd() {

		//  1. |========================================|
		//	2.                        |=================|
		
		gcFrom.setTime(now);
		gcFrom.add(Calendar.DAY_OF_MONTH, 20);
		gcTo.setTime(infDate);
		Set<PoGroup> groups = new HashSet<PoGroup>();
		groups.add(myGroup1);
		groups.add(myGroup2);

		organisationService.linkPerson2Group(myPerson, myGroup2, gcFrom.getTime() ,gcTo.getTime());
		List<PoPersonGroup> poGroups = organisationService.findPersonGroupsF(myPerson, now, myStructure);

		assert2PersonGroupsSameEnd(groups, poGroups);

		poGroups = organisationService.findPersonGroupsAll(myPerson, myStructure);
		
		assert2PersonGroupsSameEnd(groups, poGroups);
		
		HistorizationTimelineUtils.checkTimeline(poGroups, myPerson.getValidity());
		
	}

	private void assert2PersonGroupsSameEnd(Set<PoGroup> groups, List<PoPersonGroup> poGroups) {
		
		assertNotNull(poGroups);
		assertEquals(2, poGroups.size());
		assertTrue(groups.contains(poGroups.get(0).getGroup()));
		assertEquals(myPerson, poGroups.get(0).getPerson());
		assertTrue(groups.contains(poGroups.get(1).getGroup()));
		assertEquals(myPerson, poGroups.get(1).getPerson());

		PoPersonGroup oldPg = null, newPg = null;
		if (poGroups.get(0).getGroup().equals(myGroup1)) {
			oldPg = poGroups.get(0);
			newPg = poGroups.get(1);
		} else {
			oldPg = poGroups.get(1);
			newPg = poGroups.get(0);
		}
		assertEquals(gcFrom.getTime(), newPg.getValidfrom());
		assertEquals(infDate, newPg.getValidto());
		assertEquals(now, oldPg.getValidfrom());
		assertTrue(equalsTo(setTime235959PreviousDay(gcFrom), oldPg.getValidto()));
	}
	
	public void testLinPerson2GroupAfterOverlap() {
		
		//   1.    |=================|
		//   2. 		 |================================|

		gcFrom.setTime(now);
		gcFrom.add(Calendar.DAY_OF_MONTH, 20);
		gcTo.setTime(infDate);
		gcTo.add(Calendar.DAY_OF_MONTH, 20);
		Set<PoGroup> groups = new HashSet<PoGroup>();
		groups.add(myGroup1);
		groups.add(myGroup2);

		organisationService.linkPerson2Group(myPerson, myGroup2, gcFrom.getTime() ,gcTo.getTime());
		List<PoPersonGroup> poGroups = organisationService.findPersonGroupsF(myPerson, now, myStructure);

		assertNotNull(poGroups);
		assertEquals(2, poGroups.size());
		assertTrue(groups.contains(poGroups.get(0).getGroup()));
		assertEquals(myPerson, poGroups.get(0).getPerson());
		assertTrue(groups.contains(poGroups.get(1).getGroup()));
		assertEquals(myPerson, poGroups.get(1).getPerson());

		PoPersonGroup oldPg = null, newPg = null;
		if (poGroups.get(0).getGroup().equals(myGroup1)) {
			oldPg = poGroups.get(0);
			newPg = poGroups.get(1);
		} else {
			oldPg = poGroups.get(1);
			newPg = poGroups.get(0);
		}
		assertEquals(gcFrom.getTime(), newPg.getValidfrom());
		assertTrue(equalsTo(DateTools.lastMomentOfDay(gcTo.getTime()), newPg.getValidto()));
		assertEquals(now, oldPg.getValidfrom());
		assertTrue(equalsTo(setTime235959PreviousDay(gcFrom), oldPg.getValidto()));
		
		HistorizationTimelineUtils.checkTimeline(poGroups, myPerson.getValidity());
		
	}

	// the first assignment serves only as background 
	//so that the scenario can be played out in future
	public void testLinkPerson2GroupBothsideOut() {
		
		//   1. |=====================================|
		//   2.         |=================|
		//   3.    |============================|

		gcFrom.setTime(now);
		gcFrom.add(Calendar.DAY_OF_MONTH, 100);
		gcTo.setTimeInMillis(DateTools.INFINITY_TIMEMILLIS);
		gcTo.add(Calendar.DAY_OF_MONTH, -100);

		organisationService.linkPerson2Group(myPerson, myGroup2, gcFrom.getTime() ,gcTo.getTime());
		List<PoPersonGroup> poGroups = organisationService.findPersonGroupsF(myPerson, now, myStructure);

		gcFrom.setTime(now);
		gcFrom.add(Calendar.DAY_OF_MONTH, 10);
		gcTo.setTimeInMillis(DateTools.INFINITY_TIMEMILLIS);
		gcTo.add(Calendar.DAY_OF_MONTH, -10);
		
		organisationService.linkPerson2Group(myPerson, myGroup3, gcFrom.getTime() ,gcTo.getTime());
		poGroups = organisationService.findPersonGroupsF(myPerson, now, myStructure);
		
		assertNotNull(poGroups);
		assertEquals(3, poGroups.size());
		Collections.sort(poGroups, new PoPersonGroupComparator());
		PoPersonGroup poPersonGroup1 = poGroups.get(0);
		PoPersonGroup poPersonGroup2 = poGroups.get(1);
		PoPersonGroup poPersonGroup3 = poGroups.get(2);
		assertEquals(myPerson, poPersonGroup1.getPerson());
		assertEquals(myPerson, poPersonGroup2.getPerson());
		assertEquals(myPerson, poPersonGroup3.getPerson());
		assertEquals(myGroup1, poPersonGroup1.getGroup());
		assertEquals(myGroup3, poPersonGroup2.getGroup());
		assertEquals(myGroup1, poPersonGroup3.getGroup());
		
		assertEquals(now, poPersonGroup1.getValidfrom());
		assertTrue(equalsTo(setTime235959PreviousDay(gcFrom), poPersonGroup1.getValidto()));
		assertEquals(setTime000000(gcFrom), poPersonGroup2.getValidfrom());
		assertTrue(equalsTo(setTime235959(gcTo), poPersonGroup2.getValidto()));
		assertEquals(setTime000000NextDay(gcTo), poPersonGroup3.getValidfrom());
		assertTrue(equalsTo(infDate, poPersonGroup3.getValidto()));
		
		HistorizationTimelineUtils.checkTimeline(poGroups, myPerson.getValidity());

	}
	
	// the first assignment serves only as background 
	//so that the scenario can be played out in future
	public void testLinkPerson2GroupNewLongerForward() {
		
		//   1.  |================================|
		//   2.  |============|
		//   3.	 |================================|
		
		gcFrom.setTime(now);
		gcTo.setTime(now);
		gcTo.add(Calendar.DAY_OF_MONTH, 10);

		organisationService.linkPerson2Group(myPerson, myGroup2, gcFrom.getTime() ,gcTo.getTime());

		gcTo.setTime(infDate);

		organisationService.linkPerson2Group(myPerson, myGroup3, gcFrom.getTime() ,gcTo.getTime());
		List<PoPersonGroup> poGroups = organisationService.findPersonGroupsF(myPerson, now, myStructure);

		assertNotNull(poGroups);
		assertEquals(1, poGroups.size());
		PoPersonGroup poPersonGroup = poGroups.get(0);
		assertEquals(myGroup3, poPersonGroup.getGroup());
		assertEquals(myPerson, poPersonGroup.getPerson());
		assertTrue(equalsTo(setTime000000(gcFrom), poPersonGroup.getValidfrom()));
		assertTrue(equalsTo(gcTo.getTime(), poPersonGroup.getValidto()));
		
		HistorizationTimelineUtils.checkTimeline(poGroups, myPerson.getValidity());

	}
	
	// the first assignment serves only as background 
	//so that the scenario can be played out in future
	public void testLinkPerson2GroupNewLongerBack() {
		
		//   1.  |================================|
		//   2.                      |============|
		//   3.	 |================================|
		
		gcFrom.setTime(infDate);
		gcFrom.add(Calendar.DAY_OF_MONTH, -100);
		gcTo.setTime(infDate);
		
		organisationService.linkPerson2Group(myPerson, myGroup2, gcFrom.getTime() ,gcTo.getTime());
		
		gcFrom.setTime(now);
		
		organisationService.linkPerson2Group(myPerson, myGroup3, gcFrom.getTime() ,gcTo.getTime());
		List<PoPersonGroup> poGroups = organisationService.findPersonGroupsF(myPerson, now, myStructure);
		
		assertNotNull(poGroups);
		assertEquals(1, poGroups.size());
		PoPersonGroup poPersonGroup = poGroups.get(0);
		assertEquals(myGroup3, poPersonGroup.getGroup());
		assertEquals(myPerson, poPersonGroup.getPerson());
		assertTrue(equalsTo(setTime000000(gcFrom), poPersonGroup.getValidfrom()));
		assertTrue(equalsTo(gcTo.getTime(), poPersonGroup.getValidto()));
		
		HistorizationTimelineUtils.checkTimeline(poGroups, myPerson.getValidity());
		
	}
	
	// the first assignment serves only as background 
	//so that the scenario can be played out in future
	public void testLinkPerson2GroupShiftLeft() {
		
		//	 1.  |========================================|
		//   2.                         |=================|
		//   3.  |================================|
		
		gcFrom.setTime(infDate);
		gcFrom.add(Calendar.DAY_OF_MONTH, -20);
		gcTo.setTime(infDate);
		Set<PoGroup> groups = new HashSet<PoGroup>();
		groups.add(myGroup2);
		groups.add(myGroup3);

		organisationService.linkPerson2Group(myPerson, myGroup2, gcFrom.getTime(), gcTo.getTime());
		
		gcFrom.setTime(now);
		gcTo.setTime(infDate);
		gcTo.add(Calendar.DAY_OF_MONTH, -10);
		organisationService.linkPerson2Group(myPerson, myGroup3, gcFrom.getTime(), gcTo.getTime());
		List<PoPersonGroup> poGroups = organisationService.findPersonGroupsF(myPerson, now, myStructure);

		assertNotNull(poGroups);
		assertEquals(2, poGroups.size());
		assertTrue(groups.contains(poGroups.get(0).getGroup()));
		assertEquals(myPerson, poGroups.get(0).getPerson());
		assertTrue(groups.contains(poGroups.get(1).getGroup()));
		assertEquals(myPerson, poGroups.get(1).getPerson());

		PoPersonGroup oldPg = null, newPg = null;
		if (poGroups.get(0).getGroup().equals(myGroup2)) {
			oldPg = poGroups.get(0);
			newPg = poGroups.get(1);
		} else {
			oldPg = poGroups.get(1);
			newPg = poGroups.get(0);
		}
		assertEquals(gcFrom.getTime(), newPg.getValidfrom());
		assertTrue(equalsTo(setTime235959(gcTo), newPg.getValidto()));
		assertEquals(setTime000000NextDay(gcTo), oldPg.getValidfrom());
		assertEquals(infDate, oldPg.getValidto());
		
		HistorizationTimelineUtils.checkTimeline(poGroups, myPerson.getValidity());
	}
	
	public void testLinkPerson2GroupBothSidesIn() {
		
		//   1.  |======================================|
		//   2.		 |================================|

		gcFrom.setTime(now);
		gcFrom.add(Calendar.DAY_OF_MONTH, 100);
		gcTo.setTimeInMillis(DateTools.INFINITY_TIMEMILLIS);
		gcTo.add(Calendar.DAY_OF_MONTH, -100);

		organisationService.linkPerson2Group(myPerson, myGroup2, gcFrom.getTime() ,gcTo.getTime());
		List<PoPersonGroup> poGroups = organisationService.findPersonGroupsF(myPerson, now, myStructure);

		assertNotNull(poGroups);
		assertEquals(3, poGroups.size());
		Collections.sort(poGroups, new PoPersonGroupComparator());
		PoPersonGroup poPersonGroup1 = poGroups.get(0);
		PoPersonGroup poPersonGroup2 = poGroups.get(1);
		PoPersonGroup poPersonGroup3 = poGroups.get(2);
		assertEquals(myPerson, poPersonGroup1.getPerson());
		assertEquals(myPerson, poPersonGroup2.getPerson());
		assertEquals(myPerson, poPersonGroup3.getPerson());
		assertEquals(myGroup1, poPersonGroup1.getGroup());
		assertEquals(myGroup2, poPersonGroup2.getGroup());
		assertEquals(myGroup1, poPersonGroup3.getGroup());
		
		assertEquals(now, poPersonGroup1.getValidfrom());
		assertTrue(equalsTo(setTime235959PreviousDay(gcFrom), poPersonGroup1.getValidto()));
		assertEquals(setTime000000(gcFrom), poPersonGroup2.getValidfrom());
		assertTrue(equalsTo(setTime235959(gcTo), poPersonGroup2.getValidto()));
		assertEquals(setTime000000NextDay(gcTo), poPersonGroup3.getValidfrom());
		assertTrue(equalsTo(infDate, poPersonGroup3.getValidto()));
		
		HistorizationTimelineUtils.checkTimeline(poGroups, myPerson.getValidity());

	}
	
	public void testLinkPerson2GroupDoubleAssignmentError() {
		
		//  1. |==============================|
		//  2. |==|
		//  3.    |===========================|
		
		gcFrom.setTime(now);
		Date from2 = gcFrom.getTime();
		gcTo.setTime(now);
		gcTo.add(Calendar.DAY_OF_MONTH, 1);
		Date to2 = gcTo.getTime();
		Set<PoGroup> groups = new HashSet<PoGroup>();
		groups.add(myGroup1);
		groups.add(myGroup2);
	
		organisationService.linkPerson2Group(myPerson, myGroup2, from2, to2);
		List<PoPersonGroup> poGroups = organisationService.findPersonGroupsF(myPerson, now, myStructure);

		assertNotNull(poGroups);
		assertEquals(2, poGroups.size());
		PoPersonGroup poPersonGroup1 = poGroups.get(0);
		assertTrue(groups.contains(poPersonGroup1.getGroup()));
		assertEquals(myPerson, poPersonGroup1.getPerson());
		PoPersonGroup poPersonGroup2 = poGroups.get(1);

		Date from3 = to2;
		groups.add(myGroup3);
		
		organisationService.linkPerson2Group(myPerson, myGroup3, from3, infDate);
		poGroups = organisationService.findPersonGroupsF(myPerson, now, myStructure);
		
		assertNotNull(poGroups);
		assertEquals(2, poGroups.size());
		poPersonGroup1 = poGroups.get(0);
		poPersonGroup2 = poGroups.get(1);
		assertTrue(groups.contains(poPersonGroup1.getGroup()));
		assertEquals(myPerson, poPersonGroup1.getPerson());
		assertTrue(groups.contains(poPersonGroup2.getGroup()));
		assertEquals(myPerson, poPersonGroup2.getPerson());
		
		PoPersonGroup first = null, second = null;
		if (myGroup2.equals(poGroups.get(0).getGroup())) {
			first = poGroups.get(0);
			second = poGroups.get(1);
		} else {
			first = poGroups.get(1);
			second = poGroups.get(0);
		}

		assertEquals(from2, first.getValidfrom());
		assertTrue(equalsTo(setTime235959PreviousDay(from3), first.getValidto()));
		assertEquals(DateTools.dateOnly(from3), second.getValidfrom());
		assertEquals(infDate, second.getValidto());
		
		HistorizationTimelineUtils.checkTimeline(poGroups, myPerson.getValidity());
	}

	/**
	 * 
	 * Helper functions
	 * 
	 * 
	 */


	private Date setTime000000(Calendar cal) {

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTime();
	}

	private Date setTime000000NextDay(Calendar calendar) {

		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(calendar.getTime());
		cal.add(Calendar.DAY_OF_YEAR, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTime();
	}

	private Date setTime235959PreviousDay(Date date) {
		
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(date);
		return setTime235959PreviousDay(cal);
	}

	private Date setTime235959(Calendar cal) {
			
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		return cal.getTime();
	}

	private Date setTime235959PreviousDay(Calendar calendar) {
		
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(calendar.getTime());
		cal.add(Calendar.DAY_OF_YEAR, -1);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		return cal.getTime();
	}
	
	private boolean equalsTo(Date date1, Date date2) {

		Calendar cal1 = GregorianCalendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = GregorianCalendar.getInstance();
		cal2.setTime(date2);

		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
		&& cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
		&& cal1.get(Calendar.HOUR_OF_DAY) == cal2.get(Calendar.HOUR_OF_DAY)
		&& cal1.get(Calendar.MINUTE) == cal2.get(Calendar.MINUTE)
		&& cal1.get(Calendar.SECOND) == cal2.get(Calendar.SECOND);
	}
}
