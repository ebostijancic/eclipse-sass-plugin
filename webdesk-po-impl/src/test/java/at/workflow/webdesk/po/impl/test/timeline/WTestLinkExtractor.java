package at.workflow.webdesk.po.impl.test.timeline;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.timeline.LinkExtractor;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

/**
 * @author sdzuban 05.09.2013
 */
public class WTestLinkExtractor extends AbstractTransactionalSpringHsqlDbTestCase {

	private final Date DATE1 = DateTools.toDate(2000, 6, 15);
	private final Date DATE2 = DateTools.toDate(2010, 6, 15);
	private final Date DATE3 = DateTools.toDate(2100, 6, 15);
	private final Date DATE4 = DateTools.toDate(2110, 6, 15);
	
	private PoOrganisationService orgService;
	private LinkExtractor<PoGroup> extractor;
	
	private PoClient client;
	private PoOrgStructure orgStructure;
	private PoGroup groupPast, groupPresent, groupFuture;
	private List<PoGroup> groups, result;
	
	/** {@inheritDoc} */
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		super.onSetUpAfterDataGeneration();
		
		if (orgService == null) {
			orgService = (PoOrganisationService) getBean("PoOrganisationService");
			extractor = new LinkExtractor<PoGroup>();
			
			client = new PoClient();
			client.setName("Client");
			orgService.saveClient(client);
			
			orgStructure = new PoOrgStructure();
			orgStructure.setAllowOnlySingleGroupMembership(true);
			orgStructure.setClient(client);
			orgStructure.setHierarchy(true);
			orgStructure.setOrgType(PoOrgStructure.STRUCTURE_TYPE_ORGANISATION_HIERARCHY);
			orgService.saveOrgStructure(orgStructure);

			groupPast = new PoGroup();
			groupPast.setClient(client);
			groupPast.setName("past");
			groupPast.setShortName("hist");
			groupPast.setOrgStructure(orgStructure);
			groupPast.setValidfrom(DATE1);
			groupPast.setValidto(DATE2);
			orgService.saveGroup(groupPast);
			
			groupPresent = new PoGroup();
			groupPresent.setClient(client);
			groupPresent.setName("present");
			groupPresent.setShortName("curr");
			groupPresent.setOrgStructure(orgStructure);
			groupPresent.setValidfrom(DATE2);
			groupPresent.setValidto(DATE3);
			orgService.saveGroup(groupPresent);
			
			groupFuture = new PoGroup();
			groupFuture.setClient(client);
			groupFuture.setName("Future");
			groupFuture.setShortName("hist");
			groupFuture.setOrgStructure(orgStructure);
			groupFuture.setValidfrom(DATE3);
			groupFuture.setValidto(DATE4);
			orgService.saveGroup(groupFuture);
			
		}
		groups = new ArrayList<PoGroup>();
	}
	
	public void testNullsExtraction() {
		
		try {
			extractor.getActualLink(null);
			fail("Accepted null links");
		} catch (Exception e) { }
		
		try {
			extractor.getPastLinks(null);
			fail("Accepted null links");
		} catch (Exception e) { }
		
		try {
			extractor.getFutureLinks(null);
			fail("Accepted null links");
		} catch (Exception e) { }
	}
	
	public void testEmptyListExtraction() {
		
		try {
			assertNull(extractor.getActualLink(groups));
		} catch (Exception e) {
			fail("Rejected null links");
		}
		
		try {
			result = extractor.getPastLinks(groups);
			assertNotNull(result);
			assertEquals(0, result.size());
		} catch (Exception e) { 
			fail("Rejected null links");
		}
		
		try {
			result = extractor.getFutureLinks(groups);
			assertNotNull(result);
			assertEquals(0, result.size());
		} catch (Exception e) { 
			fail("Accepted null links");
		}
	}
	
	public void test3ElementListExtraction() {
		
		groups.add(groupPast);
		groups.add(groupPresent);
		groups.add(groupFuture);
		
		assertNotNull(extractor.getActualLink(groups));
		assertEquals(groupPresent, extractor.getActualLink(groups));
		
		result = extractor.getPastLinks(groups);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(groupPast, result.get(0));
		
		result = extractor.getFutureLinks(groups);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(groupFuture, result.get(0));
	}
	
	
}
