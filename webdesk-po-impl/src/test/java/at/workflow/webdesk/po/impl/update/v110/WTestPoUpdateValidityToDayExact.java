package at.workflow.webdesk.po.impl.update.v110;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;

import at.workflow.webdesk.po.model.PoDayHistorization;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoParentGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoPersonGroup;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.date.DateInterval;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.date.Interval;


/**
 * 
 * @author sdzuban 24.01.2014
 *
 */
public class WTestPoUpdateValidityToDayExact extends
		AbstractUpdateV110TestCase {

	/** class allowing any datetime */
	public static class TestHistorization implements Historization {

		private Date validfrom, validto;
		
		@Override
		public String getUID() {
			return null;
		}
		@Override
		public void setValidfrom(Date validfrom) {
			this.validfrom = validfrom;
		}
		@Override
		public Date getValidfrom() {
			return validfrom;
		}
		@Override
		public void setValidto(Date validto) {
			this.validto = validto;
		}
		@Override
		public Date getValidto() {
			return validto;
		}
		@Override
		public Interval getValidity() {
			return new DateInterval(validfrom, validto);
		}
		@Override
		public void historicize() {
			// not used
		}

	}
	
	private PoUpdateValidityToDayExact updateScript;
	
	
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		super.onSetUpAfterDataGeneration();
		
		if (updateScript == null) {
			updateScript = new PoUpdateValidityToDayExact();
			updateScript.setApplicationContext(getApplicationContext());
		}
	}

	public void testUpdateCorrectValidity() {
		
		PoGroup orgUnit = getOrgUnit(0, true);
		assertEquals(DateTools.today(), orgUnit.getValidfrom());
		assertEquals(DateTools.INFINITY, orgUnit.getValidto());
		
		assertFalse(updateScript.updateValidity(orgUnit, ""));
		assertEquals(DateTools.today(), orgUnit.getValidfrom());
		assertEquals(DateTools.INFINITY, orgUnit.getValidto());
		
		Date validto = DateTools.lastMomentOfDay(DateTools.today());
		orgUnit.setValidto(validto);
		assertEquals(validto, orgUnit.getValidto());
		assertFalse(updateScript.updateValidity(orgUnit, ""));
		assertEquals(DateTools.today(), orgUnit.getValidfrom());
		assertEquals(validto, orgUnit.getValidto());
		
	}
	
	public void testUpdateIncorrerctValidity() {
		
		TestHistorization test = new TestHistorization();
		
		Date from = new Date();
		Date to = DateTools.INFINITY;
		
		test.setValidfrom(from);
		test.setValidto(to);
		assertEquals(from, test.getValidfrom());
		assertEquals(to, test.getValidto());
		
		assertTrue(updateScript.updateValidity(test, ""));
		assertEquals(DateTools.today(), test.getValidfrom());
		assertEquals(to, test.getValidto());
		
		from = DateTools.yesterday();
		to = new Date();

		test.setValidfrom(from);
		test.setValidto(to);
		assertEquals(from, test.getValidfrom());
		assertEquals(to, test.getValidto());
		
		assertTrue(updateScript.updateValidity(test, ""));
		assertEquals(from, test.getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(from), test.getValidto());
		
	}
	
	public void testEmptyRun() {
		
		try {
			updateScript.execute();
		} catch (Exception e) {
			fail("Empty run ended with exception");
		}
		
		assertEquals(0, updateScript.getFailedGroupsMessagesMap().size());
		assertEquals(0, updateScript.getFailedPersonsMessagesMap().size());
		assertEquals(0, updateScript.getGroupsToBeDeleted().size());
		assertEquals(0, updateScript.getPersonsToBeDeleted().size());
		assertEquals(0, updateScript.getGroupChanged().size());
		assertEquals(0, updateScript.getPersonChanged().size());
		assertEquals(0, updateScript.getParentGroupChanged().size());
		assertEquals(0, updateScript.getPersonGroupChanged().size());
	}
	
	public void testPerson() {
		
		getPerson(0, getOrgUnit(0, true));
		
		updateScript.execute();
		
		assertOKPerson();
	}

	public void testHistoricizedPerson() {
		
		PoPerson person = getPerson(0, getOrgUnit(0, true));
		person.historicize();
		
		updateScript.execute();
		
		assertEquals(0, updateScript.getFailedGroupsMessagesMap().size());
		assertEquals(0, updateScript.getFailedPersonsMessagesMap().size());
		assertEquals(0, updateScript.getGroupsToBeDeleted().size());
		assertEquals(1, updateScript.getPersonsToBeDeleted().size());
		assertAllFalse(updateScript.getGroupChanged().values());
		assertAllFalse(updateScript.getPersonChanged().values());
		assertEquals(0, updateScript.getParentGroupChanged().size());
		assertAllFalse(updateScript.getPersonGroupChanged().values());
	}
	
	public void testPersonWithLocationAndCostCenter() {
		
		PoPerson person = getPerson(0, getOrgUnit(0, true));
		getOrgService().linkPerson2Group(person, getLocation(0));
		getOrgService().linkPerson2Group(person, getCostCenter(0));
		
		updateScript.execute();
		
		assertOKPerson();
	}
	
	public void testPersonWithTwoConsecutiveOrgUnitsLocationsAndCostCenters() {

		PoPerson person = getPerson(0, getOrgUnit(0, true));
		getOrgService().linkPerson2Group(person, getOrgUnit(1, true), DateTools.tomorrow(), DateTools.INFINITY);
		getOrgService().linkPerson2Group(person, getLocation(0), DateTools.today(), DateTools.today());
		getOrgService().linkPerson2Group(person, getLocation(1), DateTools.tomorrow(), DateTools.INFINITY);
		getOrgService().linkPerson2Group(person, getCostCenter(0), DateTools.today(), DateTools.today());
		getOrgService().linkPerson2Group(person, getCostCenter(1), DateTools.tomorrow(), DateTools.INFINITY);
		
		updateScript.execute();
		
		assertOKPerson();
	}

	public void testPersonWithOrgUnitAssignedNotWholeValidity() {
		
		PoPerson person = getPerson(0, getOrgUnit(0, true));
		PoPersonGroup pg = person.getMemberOfGroups().iterator().next();
		pg.setValidfrom(DateTools.tomorrow());
		getOrgService().savePersonGroup(pg);
		
		try {
			updateScript.execute();
			fail("Accepted org unit not assigned at the beginning");
		} catch (Exception e) { }
		
		assertOneFailedPerson();

		pg.setValidfrom(DateTools.today());
		pg.setValidto(DateTools.tomorrow());
		getOrgService().savePersonGroup(pg);
		
		try {
			updateScript.execute();
			fail("Accepted org unit not assigned at the end");
		} catch (Exception e) { }
		
		assertOneFailedPerson();
	}
	
	public void testPersonWithGapBetweenOrgUnits() {
		
		PoPerson person = getPerson(0, getOrgUnit(0, true));
		PoPersonGroup pg0 = person.getMemberOfGroups().iterator().next();
		getOrgService().linkPerson2Group(person, getOrgUnit(1, true), DateUtils.addDays(DateTools.tomorrow(), 1), DateTools.INFINITY);
		pg0.setValidto(DateTools.today());
		getOrgService().savePersonGroup(pg0);
		
		try {
			updateScript.execute();
			fail("Accepted org unit timeline with gap");
		} catch (Exception e) { }
		
		assertOneFailedPerson();
	}
	
	public void testPersonWithOverlapOfOrgUnits() {
		
		PoPerson person = getPerson(0, getOrgUnit(0, true));
		PoPersonGroup pg0 = person.getMemberOfGroups().iterator().next();
		getOrgService().linkPerson2Group(person, getOrgUnit(1, true), DateTools.tomorrow(), DateTools.INFINITY);
		pg0.setValidto(DateTools.tomorrow());
		getOrgService().savePersonGroup(pg0);
		
		try {
			updateScript.execute();
			fail("Accepted org unit timeline with overlap");
		} catch (Exception e) { }
		
		assertOneFailedPerson();
	}
	
	public void testPersonWithOverlappingLocations() {
	
		Date afterTomorrow = DateUtils.addDays(DateTools.tomorrow(), 1);
		PoPerson person = getPerson(0, getOrgUnit(0, true));
		getOrgService().linkPerson2Group(person, getLocation(0), DateTools.today(), DateTools.tomorrow());
		List<PoPersonGroup> locationPGs = getOrgService().filterPersonGroups(person.getMemberOfGroups(), getOrgLocations());
		PoPersonGroup pg0 = locationPGs.get(0);
		getOrgService().linkPerson2Group(person, getLocation(1), afterTomorrow, DateTools.INFINITY);
		pg0.setValidto(afterTomorrow);
		getOrgService().savePersonGroup(pg0);
		
		try {
			updateScript.execute();
			fail("Accepted locations with overlap");
		} catch (Exception e) { }
		
		assertOneFailedPerson();
	}
	
	public void testPersonWithOverlappingCostCenters() {
		
		Date afterTomorrow = DateUtils.addDays(DateTools.tomorrow(), 1);
		PoPerson person = getPerson(0, getOrgUnit(0, true));
		getOrgService().linkPerson2Group(person, getCostCenter(0), DateTools.today(), DateTools.tomorrow());
		List<PoPersonGroup> costCenterPGs = getOrgService().filterPersonGroups(person.getMemberOfGroups(), getOrgCostCenters());
		PoPersonGroup pg0 = costCenterPGs.get(0);
		getOrgService().linkPerson2Group(person, getCostCenter(1), afterTomorrow, DateTools.INFINITY);
		pg0.setValidto(afterTomorrow);
		getOrgService().savePersonGroup(pg0);
		
		try {
			updateScript.execute();
			fail("Accepted cost centers with overlap");
		} catch (Exception e) { }
		
		assertOneFailedPerson();
	}

	public void testGroup() {
		
		getOrgUnit(0, true);
		
		updateScript.execute();
		
		assertEquals(0, updateScript.getFailedGroupsMessagesMap().size());
		assertEquals(0, updateScript.getFailedPersonsMessagesMap().size());
		assertEquals(0, updateScript.getGroupsToBeDeleted().size());
		assertEquals(0, updateScript.getPersonsToBeDeleted().size());
		assertAllFalse(updateScript.getGroupChanged().values());
		assertEquals(0, updateScript.getPersonChanged().size());
		assertEquals(0, updateScript.getParentGroupChanged().size());
		assertEquals(0, updateScript.getPersonGroupChanged().size());
	}
	
	public void testHistoricizedGroup() {
		
		PoGroup group = getOrgUnit(0, true);
		group.historicize();
		
		updateScript.execute();
		
		assertEquals(0, updateScript.getFailedGroupsMessagesMap().size());
		assertEquals(0, updateScript.getFailedPersonsMessagesMap().size());
		assertEquals(1, updateScript.getGroupsToBeDeleted().size());
		assertEquals(0, updateScript.getPersonsToBeDeleted().size());
		assertAllFalse(updateScript.getGroupChanged().values());
		assertEquals(0, updateScript.getPersonChanged().size());
		assertEquals(0, updateScript.getParentGroupChanged().size());
		assertEquals(0, updateScript.getPersonGroupChanged().size());
	}
	
	public void testGroupWithParentGroup() {
		
		PoGroup parent = getOrgUnit(0, true);
		PoGroup child = getOrgUnit(1, false);
		getOrgService().setParentGroup(child, parent);
		
		updateScript.execute();
		
		assertOkGroup();
	}

	public void testGroupWithNonOverlappingParentGroups() {
		
		PoGroup parent0 = getOrgUnit(0, true);
		PoGroup parent1 = getOrgUnit(1, false);
		PoGroup child = getOrgUnit(2, false);
		getOrgService().setParentGroup(child, parent0, DateTools.today(), DateTools.tomorrow());
		getOrgService().setParentGroup(child, parent1, DateUtils.addDays(DateTools.tomorrow(), 1), DateTools.INFINITY);
		
		updateScript.execute();
		
		assertOkGroup();
	}
	
	public void testGroupWithOverlappingParentGroups() {
		
		PoGroup parent0 = getOrgUnit(0, true);
		PoGroup parent1 = getOrgUnit(1, false);
		PoGroup child = getOrgUnit(2, false);
		getOrgService().setParentGroup(child, parent0, DateTools.today(), DateTools.today());
		PoParentGroup parentGroup0 = child.getParentGroups().iterator().next();
		getOrgService().setParentGroup(child, parent1, DateTools.tomorrow(), DateTools.INFINITY);
		// make overlap
		parentGroup0.setValidto(DateTools.tomorrow());
		getOrgService().saveParentGroup(parentGroup0);
		
		try {
			updateScript.execute();
			fail("Script did not detect overlap");
		} catch (Exception e) {
		}
		
		assertEquals(1, updateScript.getFailedGroupsMessagesMap().size());
		System.out.println(updateScript.getFailedGroupsMessagesMap().values().iterator().next());
		assertEquals(0, updateScript.getFailedPersonsMessagesMap().size());
		assertEquals(0, updateScript.getGroupsToBeDeleted().size());
		assertEquals(0, updateScript.getPersonsToBeDeleted().size());
		assertAllFalse(updateScript.getGroupChanged().values());
		assertEquals(0, updateScript.getPersonChanged().size());
		assertAllFalse(updateScript.getParentGroupChanged().values());
		assertEquals(0, updateScript.getPersonGroupChanged().size());
	}
	
	
//	----------------------------- ASSERTIONS ----------------------------------
	
	private void assertOKPerson() {
		
		assertEquals(0, updateScript.getFailedGroupsMessagesMap().size());
		assertEquals(0, updateScript.getFailedPersonsMessagesMap().size());
		assertEquals(0, updateScript.getGroupsToBeDeleted().size());
		assertEquals(0, updateScript.getPersonsToBeDeleted().size());
		assertAllFalse(updateScript.getGroupChanged().values());
		assertAllFalse(updateScript.getPersonChanged().values());
		assertEquals(0, updateScript.getParentGroupChanged().size());
		assertAllFalse(updateScript.getPersonGroupChanged().values());
	}
	
	private void assertOneFailedPerson() {
		
		assertEquals(0, updateScript.getFailedGroupsMessagesMap().size());
		Map<PoPerson, List<String>> failedMessages = updateScript.getFailedPersonsMessagesMap();
		assertEquals(1, failedMessages.size());
		for (String message : failedMessages.values().iterator().next())
			System.out.println(message);
		assertEquals(0, updateScript.getGroupsToBeDeleted().size());
		assertEquals(0, updateScript.getPersonsToBeDeleted().size());
		assertAllFalse(updateScript.getGroupChanged().values());
		assertAllFalse(updateScript.getPersonChanged().values());
		assertEquals(0, updateScript.getParentGroupChanged().size());
		assertAllFalse(updateScript.getPersonGroupChanged().values());
	}
	
	private void assertOkGroup() {
		
		assertEquals(0, updateScript.getFailedGroupsMessagesMap().size());
		assertEquals(0, updateScript.getFailedPersonsMessagesMap().size());
		assertEquals(0, updateScript.getGroupsToBeDeleted().size());
		assertEquals(0, updateScript.getPersonsToBeDeleted().size());
		assertAllFalse(updateScript.getGroupChanged().values());
		assertEquals(0, updateScript.getPersonChanged().size());
		assertAllFalse(updateScript.getParentGroupChanged().values());
		assertEquals(0, updateScript.getPersonGroupChanged().size());
	}
	
	
//	---------------------------- PRIVATE METHODS ---------------------------------
	
	/** this method sets the date directly to the property without calling setter to preserve date exactly 
	 * THIS DOES NOT WORK - IllegalAccessException IS THROWN
	 * @throws Exception */
	@SuppressWarnings("unused")
	private void setValidfrom(PoDayHistorization entity, Date date) throws Exception {
		Field validfrom = entity.getClass().getSuperclass().getSuperclass().getDeclaredField("validfrom");
		validfrom.set(entity, date);
	}
	
	/** this method sets the date directly to the property without calling setter to preserve date exactly 
	 * THIS DOES NOT WORK - IllegalAccessException IS THROWN
	 * @throws Exception */
	@SuppressWarnings("unused")
	private void setValidto(PoDayHistorization entity, Date date) throws Exception {
		Field validto = entity.getClass().getSuperclass().getSuperclass().getDeclaredField("validto");
		validto.set(entity, date);
	}
	
}
