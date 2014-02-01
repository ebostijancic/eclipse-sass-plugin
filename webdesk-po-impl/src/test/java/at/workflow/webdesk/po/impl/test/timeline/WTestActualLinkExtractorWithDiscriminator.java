package at.workflow.webdesk.po.impl.test.timeline;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.timeline.ActualLinkExtractorWithDiscriminator;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

/**
 * @author sdzuban 05.09.2013
 */
public class WTestActualLinkExtractorWithDiscriminator extends AbstractTransactionalSpringHsqlDbTestCase {

	private final Date DATE1 = DateTools.toDate(2000, 6, 15);
	private final Date DATE2 = DateTools.toDate(2010, 6, 15);
	private final Date DATE3 = DateTools.toDate(2100, 6, 15);
	private final Date DATE4 = DateTools.toDate(2110, 6, 15);
	
	private PoOrganisationService orgService;
	private ActualLinkExtractorWithDiscriminator<PoGroup> extractor;
	
	private PoClient client;
	private PoOrgStructure orgStructure;
	private PoGroup groupPastA, groupPresentA, groupFutureA, groupPastB, groupPresentB, groupFutureB;
	private List<PoGroup> groups, result;
	
	/** {@inheritDoc} */
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		super.onSetUpAfterDataGeneration();
		
		if (orgService == null) {
			orgService = (PoOrganisationService) getBean("PoOrganisationService");
			Method getDiscriminatorValue = PoGroup.class.getDeclaredMethod("getDescription");
			extractor = new ActualLinkExtractorWithDiscriminator<PoGroup>(getDiscriminatorValue);
			
			client = new PoClient();
			client.setName("Client");
			orgService.saveClient(client);
			
			orgStructure = new PoOrgStructure();
			orgStructure.setAllowOnlySingleGroupMembership(true);
			orgStructure.setClient(client);
			orgStructure.setHierarchy(true);
			orgStructure.setOrgType(PoOrgStructure.STRUCTURE_TYPE_ORGANISATION_HIERARCHY);
			orgService.saveOrgStructure(orgStructure);

			groupPastA = new PoGroup();
			groupPastA.setClient(client);
			groupPastA.setName("past");
			groupPastA.setShortName("histA");
			groupPastA.setDescription("A");
			groupPastA.setOrgStructure(orgStructure);
			groupPastA.setValidfrom(DATE1);
			groupPastA.setValidto(DATE2);
			orgService.saveGroup(groupPastA);
			
			groupPresentA = new PoGroup();
			groupPresentA.setClient(client);
			groupPresentA.setName("present");
			groupPresentA.setShortName("currA");
			groupPresentA.setDescription("A");
			groupPresentA.setOrgStructure(orgStructure);
			groupPresentA.setValidfrom(DATE2);
			groupPresentA.setValidto(DATE3);
			orgService.saveGroup(groupPresentA);
			
			groupFutureA = new PoGroup();
			groupFutureA.setClient(client);
			groupFutureA.setName("Future");
			groupFutureA.setShortName("futA");
			groupFutureA.setDescription("A");
			groupFutureA.setOrgStructure(orgStructure);
			groupFutureA.setValidfrom(DATE3);
			groupFutureA.setValidto(DATE4);
			orgService.saveGroup(groupFutureA);
			
			
			groupPastB = new PoGroup();
			groupPastB.setClient(client);
			groupPastB.setName("past");
			groupPastB.setShortName("histB");
			groupPastB.setDescription("B");
			groupPastB.setOrgStructure(orgStructure);
			groupPastB.setValidfrom(DATE1);
			groupPastB.setValidto(DATE2);
			orgService.saveGroup(groupPastB);
			
			groupPresentB = new PoGroup();
			groupPresentB.setClient(client);
			groupPresentB.setName("present");
			groupPresentB.setShortName("currB");
			groupPresentB.setDescription("B");
			groupPresentB.setOrgStructure(orgStructure);
			groupPresentB.setValidfrom(DATE2);
			groupPresentB.setValidto(DATE3);
			orgService.saveGroup(groupPresentB);
			
			groupFutureB = new PoGroup();
			groupFutureB.setClient(client);
			groupFutureB.setName("Future");
			groupFutureB.setShortName("futB");
			groupFutureB.setDescription("B");
			groupFutureB.setOrgStructure(orgStructure);
			groupFutureB.setValidfrom(DATE3);
			groupFutureB.setValidto(DATE4);
			orgService.saveGroup(groupFutureB);
			
		}
		groups = new ArrayList<PoGroup>();
	}
	
	public void testNullsExtraction() {
		
		try {
			extractor.getActualLink(null, null);
			fail("Accepted null links");
		} catch (Exception e) { }
		
		try {
			extractor.getPastLinks(null, null);
			fail("Accepted null links");
		} catch (Exception e) { }
		
		try {
			extractor.getFutureLinks(null, null);
			fail("Accepted null links");
		} catch (Exception e) { }
	}
	
	public void testEmptyListExtraction() {
		
		try {
			assertNull(extractor.getActualLink(groups, "A"));
		} catch (Exception e) {
			fail("Rejected null links");
		}
		
		try {
			result = extractor.getPastLinks(groups, "A");
			assertNotNull(result);
			assertEquals(0, result.size());
		} catch (Exception e) { 
			fail("Rejected null links");
		}
		
		try {
			result = extractor.getFutureLinks(groups, "A");
			assertNotNull(result);
			assertEquals(0, result.size());
		} catch (Exception e) { 
			fail("Accepted null links");
		}
	}
	
	public void test3ElementListExtraction() {
		
		groups.add(groupPastA);
		groups.add(groupPresentB);
		groups.add(groupFutureA);
		groups.add(groupPastB);
		groups.add(groupPresentA);
		groups.add(groupFutureB);
		
		assertNotNull(extractor.getActualLink(groups, "A"));
		assertEquals(groupPresentA, extractor.getActualLink(groups, "A"));
		
		result = extractor.getPastLinks(groups, "B");
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(groupPastB, result.get(0));
		
		result = extractor.getFutureLinks(groups, "A");
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(groupFutureA, result.get(0));
	}
	
	
}
