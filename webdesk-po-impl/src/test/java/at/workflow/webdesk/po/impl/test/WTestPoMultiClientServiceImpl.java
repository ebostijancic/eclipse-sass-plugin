package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.PoMultiClientService;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.impl.helper.PoDataGenerator;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;
import at.workflow.webdesk.tools.testing.DataGenerator;

/**
 * @author sdzuban 03.09.2012
 */
public class WTestPoMultiClientServiceImpl extends AbstractTransactionalSpringHsqlDbTestCase {
	
	private PoOrganisationService orgService;
	private PoMultiClientService service;
	private PoClient workflex;

	@Override
	protected final DataGenerator[] getDataGenerators() {
		return new DataGenerator[] { new PoDataGenerator("at/workflow/webdesk/po/impl/test/data/MinTestData.xml", false) };
	}

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		service = (PoMultiClientService) getBean("PoMultiClientService");
		orgService = (PoOrganisationService) getBean("PoOrganisationService");
		workflex = orgService.findClientByName("Workflex");
	}
	
	public void testNullParameters() {
		
		service.updateGroupShortNamePrefix(null, null, null);
		
		service.updateGroupShortNamePrefix(workflex, null, null);
	}
	
	public void testFinderByPrefix() {
		
		PoClient newClient = new PoClient();
		newClient.setName("newClient");
		newClient.setGroupShortNamePrefix("gsn");
		newClient.setPersonUserNamePrefix("pun");
		newClient.setPersonEmployeeIdPrefix("pei");
		
		orgService.saveClient(newClient);
		
		assertNull(service.findClientByGroupShortNamePrefix("test"));
		assertNull(service.findClientByPersonUserNamePrefix("test"));
		assertNull(service.findClientByPersonEmployeeIdPrefix("test"));
		
		assertNotNull(service.findClientByGroupShortNamePrefix("gsn"));
		assertNotNull(service.findClientByPersonUserNamePrefix("pun"));
		assertNotNull(service.findClientByPersonEmployeeIdPrefix("pei"));
		
		assertEquals(newClient, service.findClientByGroupShortNamePrefix("gsn"));
		assertEquals(newClient, service.findClientByPersonUserNamePrefix("pun"));
		assertEquals(newClient, service.findClientByPersonEmployeeIdPrefix("pei"));
		
		// test updater duplication prevention 
		try {
			service.updateGroupShortNamePrefix(workflex, null, "gsn");
			fail("accepted assigned group short name prefix");
		} catch (Exception e) {}
		
		try {
			service.updatePersonUserNamePrefix(workflex, null, "pun");
			fail("accepted assigned person user name prefix");
		} catch (Exception e) {}
		
		try {
			service.updatePersonEmployeeIdPrefix(workflex, null, "pei");
			fail("accepted assigned person employee idprefix");
		} catch (Exception e) {}
		
		// exception in next calls would mean failure
		service.updateGroupShortNamePrefix(newClient, null, "gsn");
		service.updatePersonUserNamePrefix(newClient, null, "pun");
		service.updatePersonEmployeeIdPrefix(newClient, null, "pei");
	}
	
	public void testPrefixReplacement() {
		
		PoGroup g01 = orgService.findGroupByShortName("G01");
		PoGroup g011 = orgService.findGroupByShortName("G01_1");
		PoGroup g012 = orgService.findGroupByShortName("G01_2");
		
		PoClient client = orgService.findClientByName("Workflex");
		service.updateGroupShortNamePrefix(client, "G0", "abc");
		
		PoGroup abc1 = orgService.findGroupByShortName("abc1");
		PoGroup abc11 = orgService.findGroupByShortName("abc1_1");
		PoGroup abc12 = orgService.findGroupByShortName("abc1_2");

		assertEquals(g01, abc1);
		assertEquals(g011, abc11);
		assertEquals(g012, abc12);
		
		client = orgService.findClientByName("Workflex");
		service.updateGroupShortNamePrefix(client, "ab", null);
		
		PoGroup c1 = orgService.findGroupByShortName("c1");
		PoGroup c11 = orgService.findGroupByShortName("c1_1");
		PoGroup c12 = orgService.findGroupByShortName("c1_2");
		
		assertEquals(g01, c1);
		assertEquals(g011, c11);
		assertEquals(g012, c12);
	}
	
	public void testUserNameReplacement() {
		
		PoPerson p1 = orgService.findPersonByUserName("wef");
		PoPerson p2 = orgService.findPersonByUserName("ham");
		PoPerson p3 = orgService.findPersonByUserName("duc");
		
		PoClient client = orgService.findClientByName("Workflex");
		service.updatePersonUserNamePrefix(client, null, "abc");
		
		PoPerson abc1 = orgService.findPersonByUserName("abcwef");
		PoPerson abc2 = orgService.findPersonByUserName("abcham");
		PoPerson abc3 = orgService.findPersonByUserName("abcduc");
		
		assertEquals(p1, abc1);
		assertEquals(p2, abc2);
		assertEquals(p3, abc3);
		
		client = orgService.findClientByName("Workflex");
		service.updatePersonUserNamePrefix(client, "AB", null);
		
		PoPerson c1 = orgService.findPersonByUserName("abcwef");
		PoPerson c11 = orgService.findPersonByUserName("abcham");
		PoPerson c12 = orgService.findPersonByUserName("abcduc");
		
		assertEquals(p1, c1);
		assertEquals(p2, c11);
		assertEquals(p3, c12);
	}
	
	
	public void testEmployeeIdReplacement() {
		
		PoPerson p1 = orgService.findPersonByEmployeeId("0096");
		PoPerson p2 = orgService.findPersonByEmployeeId("0044");
		PoPerson p3 = orgService.findPersonByEmployeeId("0028");
		
		PoClient client = orgService.findClientByName("Workflex");
		service.updatePersonEmployeeIdPrefix(client, "00", "ab");
		
		PoPerson abc1 = orgService.findPersonByEmployeeId("ab96");
		PoPerson abc2 = orgService.findPersonByEmployeeId("ab44");
		PoPerson abc3 = orgService.findPersonByEmployeeId("ab28");
		
		assertEquals(p1, abc1);
		assertEquals(p2, abc2);
		assertEquals(p3, abc3);
		
		client = orgService.findClientByName("Workflex");
		service.updatePersonEmployeeIdPrefix(client, "ab", "000");
		
		PoPerson c1 = orgService.findPersonByEmployeeId("00096");
		PoPerson c11 = orgService.findPersonByEmployeeId("00044");
		PoPerson c12 = orgService.findPersonByEmployeeId("00028");
		
		assertEquals(p1, c1);
		assertEquals(p2, c11);
		assertEquals(p3, c12);
	}
	
}
