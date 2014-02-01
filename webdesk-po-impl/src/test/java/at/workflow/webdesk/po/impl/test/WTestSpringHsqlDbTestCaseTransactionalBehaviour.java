package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.daos.PoClientDAO;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

/**
 * Ensures that the unit test transaction concept works properly.
 * Shows how to derive AbstractTransactionalSpringHsqlDbTestCase correctly.
 * 
 * @author fritzberger 17.10.2011
 */
public class WTestSpringHsqlDbTestCaseTransactionalBehaviour extends AbstractTransactionalSpringHsqlDbTestCase {

	private static final String TESTMETHOD_LOCAL_SETUP_CLIENT = "Testmethod-local setup client";
	private static final String TESTMETHOD_LOCAL_CLIENT = "TestMethod local client";
	private static final String TESTCASE_GLOBAL_CLIENT = "TestCase global client";
	
	private PoOrganisationService orgService;
	
	/** Called BEFORE the transaction of following testMethod() starts. */
	@Override
	protected void onSetUpBeforeDataGeneration() throws Exception {
		orgService = (PoOrganisationService) getBean("PoOrganisationService");
		
		assertMethodLocalCientsDontExist();
		
		// create a client that exists for all testMethods().
		PoClient client = orgService.findClientByName(TESTCASE_GLOBAL_CLIENT);
		if (client == null)
			createClient(TESTCASE_GLOBAL_CLIENT, "from onSetUpBeforeDataGeneration");
	}
	
	/** Called AFTER the transaction of preceding testMethod() ended. */
	@Override
	protected void onTearDownAfterTransaction() throws Exception {
		PoClient globalClient = orgService.findClientByName(TESTCASE_GLOBAL_CLIENT);
		assertNotNull("Global client should exist!", globalClient);
		
		// as orgService.deleteClient() is not implemented and deleteAndFlushClient() throws
		// a lazy-loading exception, delete the client by means of DAO
		PoClientDAO clientDAO = (PoClientDAO) getBean("PoClientDAO");
		clientDAO.delete(globalClient);
		
		globalClient = orgService.findClientByName(TESTCASE_GLOBAL_CLIENT);
		assertNull("Global client should NOT exist anymore!", globalClient);
		
		assertMethodLocalCientsDontExist();
	}
	
	/** Called AFTER transaction for following testMethod() has started. */
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		PoClient globalClient = orgService.findClientByName(TESTCASE_GLOBAL_CLIENT);
		assertNotNull("Global client should exist!", globalClient);
		
		assertMethodLocalCientsDontExist();
		
		createClient(TESTMETHOD_LOCAL_SETUP_CLIENT, "from onSetUpAfterDataGeneration");
	}

	public void testMethod0()	{
		PoClient client = orgService.findClientByName(TESTMETHOD_LOCAL_SETUP_CLIENT);
		assertNotNull("Data created by onSetUpAfterDataGeneration must be available!", client);
	}
	
	public void testMethod1()	{
		PoClient client = orgService.findClientByName(TESTMETHOD_LOCAL_CLIENT);
		assertNull("As each test method has its own data, client MUST NOT exist!", client);
		createClient(TESTMETHOD_LOCAL_CLIENT, "from testMethod 1");
	}
	
	public void testMethod2()	{
		PoClient client = orgService.findClientByName(TESTMETHOD_LOCAL_CLIENT);
		assertNull("As each test method has its own data, client MUST NOT exist!", client);
		createClient(TESTMETHOD_LOCAL_CLIENT, "from testMethod 2");
	}


	private void createClient(String name, String shortName) {
		PoClient client;
		client = new PoClient();
		client.setName(name);
		client.setShortName(shortName);
		orgService.saveClient(client);
	}

	private void assertMethodLocalCientsDontExist() {
		PoClient client = orgService.findClientByName(TESTMETHOD_LOCAL_SETUP_CLIENT);
		assertNull("As each test method has its own data, client MUST NOT exist!", client);
		
		client = orgService.findClientByName(TESTMETHOD_LOCAL_CLIENT);
		assertNull("As each test method has its own data, client MUST NOT exist!", client);
	}
	
}
