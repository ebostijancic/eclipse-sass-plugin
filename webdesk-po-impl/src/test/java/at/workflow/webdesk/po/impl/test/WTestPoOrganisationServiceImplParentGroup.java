package at.workflow.webdesk.po.impl.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;

import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoParentGroup;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

/**
 * Copy from HrPositionJob javadoc:
 *  * This entity models a historized ManyToOne Relationship between Position and Job. It means that
 *  * at any time, the position can be connected with max. 1 job.
 *  * At any time many positions can be connected to the same job.
 * 
 * @author sdzuban 22.03.2013
 */
public class WTestPoOrganisationServiceImplParentGroup extends AbstractTransactionalSpringHsqlDbTestCase {

	protected class TimelineComparator implements Comparator<Historization> {
		/** {@inheritDoc} */
		@Override
		public int compare(Historization o1, Historization o2) {
			return o1.getValidfrom().compareTo(o2.getValidfrom());
		}
	}
	
	private static final Date DATE_0101_2100 = DateTools.toDate(2100, 1, 1, 1, 1);
	private static final Date DATE_1501_2100 = DateTools.toDate(2100, 1, 15, 2, 15);
	private static final Date DATE_1503_2100 = DateTools.toDate(2100, 3, 15, 4, 15);
	private static final Date DATE_1506_2100 = DateTools.toDate(2100, 6, 15, 7, 15);
	private static final Date DATE_1509_2100 = DateTools.toDate(2100, 9, 15, 8, 15);
	private static final Date DATE_3112_2100 = DateTools.toDate(2100, 12, 31, 13, 15);

	private static final Date DATE_1503_2200 = DateTools.toDate(2200, 3, 15, 16, 15);
	private static final Date DATE_1506_2200 = DateTools.toDate(2200, 6, 15, 18, 15);
	private static final Date DATE_1509_2200 = DateTools.toDate(2200, 9, 15, 21, 15);
	private static final Date DATE_3112_2200 = DateTools.toDate(2200, 12, 31, 23, 30);

	private PoGroup parent1, parent2;
	private PoGroup group1, group2;
	private PoClient client;
	private PoOrgStructure ouStruct;
	private PoOrganisationService service;

	
	/** {@inheritDoc} */
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		super.onSetUpAfterDataGeneration();
		
		if (service == null) {
			service = (PoOrganisationService) getBean("PoOrganisationService");
		}
		
		client = new PoClient();
		client.setName("client");
		service.saveClient(client);
		
		ouStruct = new PoOrgStructure();
		ouStruct.setClient(client);
		ouStruct.setHierarchy(true);
		ouStruct.setAllowOnlySingleGroupMembership(true);
		ouStruct.setOrgType(PoConstants.STRUCTURE_TYPE_ORGANISATION_HIERARCHY);
		service.saveOrgStructure(ouStruct);

		parent1 = new PoGroup();
		parent1.setClient(client);
		parent1.setOrgStructure(ouStruct);
		parent1.setName("parent1");
		parent1.setShortName("p1");
		service.saveGroup(parent1);

		parent2 = new PoGroup();
		parent2.setClient(client);
		parent2.setOrgStructure(ouStruct);
		parent2.setName("parent2");
		parent2.setShortName("p2");
		service.saveGroup(parent2);

		group1 = new PoGroup();
		group1.setClient(client);
		group1.setOrgStructure(ouStruct);
		group1.setName("group1");
		group1.setShortName("g1");
		service.saveGroup(group1);

		group2 = new PoGroup();
		group2.setClient(client);
		group2.setOrgStructure(ouStruct);
		group2.setName("group2");
		group2.setShortName("g2");
		service.saveGroup(group2);
	}
	
//	-------------------------- NULLS ---------------------------------
	
	public void testNulls() {
		
		try {
			service.setParentGroup(null, null);
			fail("Accepted null group and parent");
		} catch (Exception e) { }
		
		try {
			service.setParentGroup(null, parent1);
			fail("Accepted null group");
		} catch (Exception e) { }
		
		try {
			service.setParentGroup(group1, null);
			fail("Accepted null parent");
		} catch (Exception e) { }
	}
	
//	---------------------------- SIMPLE ASSIGNMENTS ------------------------------
	
	public void testSameRelationshipAssignments () {
		
		service.setParentGroup(group1, parent1, DATE_1503_2100, DATE_1509_2100);
		PoParentGroup parentGroup = group1.getParentGroups().iterator().next();
		assertEquals(DateTools.dateOnly(DATE_1503_2100), parentGroup.getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(DATE_1509_2100), parentGroup.getValidto());

		// repeated assignment
		service.setParentGroup(group1, parent1, DATE_1503_2100, DATE_1509_2100);
		assertEquals(1, group1.getParentGroups().size());
		assertEquals(1, parent1.getChildGroups().size());
		parentGroup = group1.getParentGroups().iterator().next();
		assertEquals(DateTools.dateOnly(DATE_1503_2100), parentGroup.getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(DATE_1509_2100), parentGroup.getValidto());

		// overlapping assignment, same position and job, different overlapping time
		service.setParentGroup(group1, parent1, DATE_1501_2100, DATE_1506_2100);
		assertEquals(2, group1.getParentGroups().size());
		assertEquals(2, parent1.getChildGroups().size());
		
		// non conflicting assignment, same position and job, different non-overlapping time
		service.setParentGroup(group1, parent1, DATE_1503_2200, DATE_1509_2200);
		assertEquals(3, group1.getParentGroups().size());
		assertEquals(3, parent1.getChildGroups().size());
		
		// overlapping assignment, same position and job, different overlapping time
		service.setParentGroup(group1, parent1, DATE_1506_2200, DATE_3112_2200);
		assertEquals(3, group1.getParentGroups().size());
		assertEquals(3, parent1.getChildGroups().size());
		
		// all-overlapping assignment, same position and job, different double-overlapping time
		service.setParentGroup(group1, parent1, DATE_0101_2100, DATE_3112_2200);
		assertEquals(1, group1.getParentGroups().size());
		assertEquals(1, parent1.getChildGroups().size());
		assertEquals(DateTools.dateOnly(DATE_0101_2100), group1.getParentGroups().iterator().next().getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(DATE_3112_2200), group1.getParentGroups().iterator().next().getValidto());

	}
	
	public void testDifferentRelationshipAssignments () {
		
		service.setParentGroup(group1, parent1, DATE_1503_2100, DATE_1509_2100);
		service.setParentGroup(group2, parent2, DATE_1503_2100, DATE_1509_2100);
		
		assertEquals(1, group1.getParentGroups().size());
		assertEquals(1, parent1.getChildGroups().size());
		assertEquals(1, group2.getParentGroups().size());
		assertEquals(1, parent2.getChildGroups().size());

	}
	
//	-------------------------- ONE POSITION, TWO JOBS -------------------------------------
	
	public void testDifferentParentAssignmentsInDifferentTimes() {
		
		service.setParentGroup(group1, parent1, DATE_1503_2100, DATE_1509_2100);
		service.setParentGroup(group1, parent2, DATE_1503_2200, DATE_1509_2200);

		assertEquals(2, group1.getParentGroups().size());
		assertEquals(1, parent1.getChildGroups().size());
		assertEquals(1, parent2.getChildGroups().size());
	}
	
	public void testOverlappingDifferentParentAssignmentLeft() {
		
		service.setParentGroup(group1, parent1, DATE_1503_2100, DATE_1509_2100);
		service.setParentGroup(group1, parent2, DATE_0101_2100, DATE_1506_2100);

		assertEquals(2, group1.getParentGroups().size());
		assertEquals(1, parent1.getChildGroups().size());
		assertEquals(1, parent2.getChildGroups().size());
		assertEquals(DateTools.dateOnly(DATE_0101_2100), parent2.getChildGroups().iterator().next().getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(DATE_1506_2100), parent2.getChildGroups().iterator().next().getValidto());
		assertEquals(DateTools.dateOnly(DateUtils.addDays(DATE_1506_2100, 1)) , parent1.getChildGroups().iterator().next().getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(DATE_1509_2100), parent1.getChildGroups().iterator().next().getValidto());
	}

	public void testOverlappingDifferentParentAssignmentRight() {
		
		service.setParentGroup(group1, parent1, DATE_1503_2100, DATE_1509_2100);
		service.setParentGroup(group1, parent2, DATE_1506_2100, DATE_3112_2100);

		assertEquals(2, group1.getParentGroups().size());
		assertEquals(1, parent1.getChildGroups().size());
		assertEquals(1, parent2.getChildGroups().size());
		assertEquals(DateTools.dateOnly(DATE_1503_2100), parent1.getChildGroups().iterator().next().getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(DateUtils.addDays(DATE_1506_2100, -1)), parent1.getChildGroups().iterator().next().getValidto());
		assertEquals(DateTools.dateOnly(DATE_1506_2100), parent2.getChildGroups().iterator().next().getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(DATE_3112_2100), parent2.getChildGroups().iterator().next().getValidto());
	}
	
	public void testOverlappingDifferentParentAssignmentInside() {
		
		service.setParentGroup(group1, parent1, DATE_0101_2100, DATE_3112_2100);
		service.setParentGroup(group1, parent2, DATE_1503_2100, DATE_1509_2100);
		
		assertEquals(3, group1.getParentGroups().size());
		assertEquals(2, parent1.getChildGroups().size());
		assertEquals(1, parent2.getChildGroups().size());
		List<PoParentGroup> links = new ArrayList<PoParentGroup>(group1.getParentGroups());
		Collections.sort(links, new TimelineComparator());
		assertEquals(DateTools.dateOnly(DATE_0101_2100), links.get(0).getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(DateUtils.addDays(DATE_1503_2100, -1)), links.get(0).getValidto());
		assertEquals(parent1, links.get(0).getParentGroup());
		assertEquals(DateTools.dateOnly(DATE_1503_2100), links.get(1).getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(DATE_1509_2100), links.get(1).getValidto());
		assertEquals(parent2, links.get(1).getParentGroup());
		assertEquals(DateTools.dateOnly(DateUtils.addDays(DATE_1509_2100, 1)), links.get(2).getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(DATE_3112_2100), links.get(2).getValidto());
		assertEquals(parent1, links.get(2).getParentGroup());
	}
	
	public void testOverlappingDifferentParentAssignmentOutside() {

		// seemingly reverse order of parents is here intended
		service.setParentGroup(group1, parent2, DATE_1503_2100, DATE_1509_2100);
		service.setParentGroup(group1, parent1, DATE_0101_2100, DATE_3112_2100);
		
		assertEquals(1, group1.getParentGroups().size());
		assertEquals(1, parent1.getChildGroups().size());
		assertEquals(0, parent2.getChildGroups().size());
		assertEquals(DateTools.dateOnly(DATE_0101_2100), parent1.getChildGroups().iterator().next().getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(DATE_3112_2100), parent1.getChildGroups().iterator().next().getValidto());
	}
	
	public void testOverlappingDifferentParentAssignmentOverwriting() {
		
		service.setParentGroup(group1, parent1, DATE_1503_2100, DATE_1509_2100);
		service.setParentGroup(group1, parent2, DATE_0101_2100, DATE_1506_2100);
		
		assertEquals(2, group1.getParentGroups().size());
		assertEquals(1, parent1.getChildGroups().size());
		assertEquals(1, parent2.getChildGroups().size());
		assertEquals(DateTools.dateOnly(DATE_0101_2100), parent2.getChildGroups().iterator().next().getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(DATE_1506_2100), parent2.getChildGroups().iterator().next().getValidto());
		assertEquals(DateTools.dateOnly(DateUtils.addDays(DATE_1506_2100, 1)), parent1.getChildGroups().iterator().next().getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(DATE_1509_2100), parent1.getChildGroups().iterator().next().getValidto());
	}
	
//	-------------------------- ONE JOB, TWO POSITIONS -------------------------------

	
	public void testDifferentChildAssignmentsInDifferentTimes() {
		
		service.setParentGroup(group1, parent1, DATE_1503_2100, DATE_1509_2100);
		service.setParentGroup(group2, parent1, DATE_1503_2200, DATE_1509_2200);

		assertEquals(2, parent1.getChildGroups().size());
		assertEquals(1, group1.getParentGroups().size());
		assertEquals(1, group2.getParentGroups().size());
	}
	
	public void testOverlappingDifferentChildAssignmentLeft() {
		
		service.setParentGroup(group1, parent1, DATE_1503_2100, DATE_1509_2100);
		service.setParentGroup(group2, parent1, DATE_0101_2100, DATE_1506_2100);

		assertEquals(2, parent1.getChildGroups().size());
		assertEquals(1, group1.getParentGroups().size());
		assertEquals(1, group2.getParentGroups().size());
		assertEquals(DateTools.dateOnly(DATE_1503_2100), group1.getParentGroups().iterator().next().getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(DATE_1509_2100), group1.getParentGroups().iterator().next().getValidto());
		assertEquals(DateTools.dateOnly(DATE_0101_2100), group2.getParentGroups().iterator().next().getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(DATE_1506_2100), group2.getParentGroups().iterator().next().getValidto());
	}

	public void testOverlappingDifferentChildAssignmentRight() {
		
		service.setParentGroup(group1, parent1, DATE_1503_2100, DATE_1509_2100);
		service.setParentGroup(group2, parent1, DATE_1506_2100, DATE_3112_2100);

		assertEquals(2, parent1.getChildGroups().size());
		assertEquals(1, group1.getParentGroups().size());
		assertEquals(1, group2.getParentGroups().size());
		assertEquals(DateTools.dateOnly(DATE_1503_2100), group1.getParentGroups().iterator().next().getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(DATE_1509_2100), group1.getParentGroups().iterator().next().getValidto());
		assertEquals(DateTools.dateOnly(DATE_1506_2100), group2.getParentGroups().iterator().next().getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(DATE_3112_2100), group2.getParentGroups().iterator().next().getValidto());
	}
	
	public void testOverlappingDifferentChildAssignmentInside() {
		
		service.setParentGroup(group1, parent1, DATE_0101_2100, DATE_3112_2100);
		service.setParentGroup(group2, parent1, DATE_1503_2100, DATE_1509_2100);
		
		assertEquals(2, parent1.getChildGroups().size());
		assertEquals(1, group1.getParentGroups().size());
		assertEquals(1, group2.getParentGroups().size());
		assertEquals(DateTools.dateOnly(DATE_0101_2100), group1.getParentGroups().iterator().next().getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(DATE_3112_2100), group1.getParentGroups().iterator().next().getValidto());
		assertEquals(DateTools.dateOnly(DATE_1503_2100), group2.getParentGroups().iterator().next().getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(DATE_1509_2100), group2.getParentGroups().iterator().next().getValidto());
	}
	
	public void testOverlappingDifferentChildAssignmentOutside() {
		
		service.setParentGroup(group1, parent1, DATE_1503_2100, DATE_1509_2100);
		service.setParentGroup(group2, parent1, DATE_0101_2100, DATE_3112_2100);
		
		assertEquals(2, parent1.getChildGroups().size());
		assertEquals(1, group2.getParentGroups().size());
		assertEquals(1, group1.getParentGroups().size());
		assertEquals(DateTools.dateOnly(DATE_1503_2100), group1.getParentGroups().iterator().next().getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(DATE_1509_2100), group1.getParentGroups().iterator().next().getValidto());
		assertEquals(DateTools.dateOnly(DATE_0101_2100), group2.getParentGroups().iterator().next().getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(DATE_3112_2100), group2.getParentGroups().iterator().next().getValidto());
	}

}
