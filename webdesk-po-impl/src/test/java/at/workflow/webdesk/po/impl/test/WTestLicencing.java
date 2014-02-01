package at.workflow.webdesk.po.impl.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.CacheManager;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import at.workflow.tools.mail.Mail;
import at.workflow.tools.mail.MailServerConfiguration;
import at.workflow.tools.mail.MailService;
import at.workflow.webdesk.po.PoActionPermissionService;
import at.workflow.webdesk.po.PoActionService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoLanguageService;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.UpgradeFailedException;
import at.workflow.webdesk.po.impl.PoLicenceInterceptor;
import at.workflow.webdesk.po.impl.PoModuleUpdateServiceImpl;
import at.workflow.webdesk.po.impl.PoRegistrationServiceImpl;
import at.workflow.webdesk.po.impl.helper.PoDataGenerator;
import at.workflow.webdesk.po.impl.licence.LicenceReaderHelper;
import at.workflow.webdesk.po.impl.licence.LicenceReaderImpl;
import at.workflow.webdesk.po.impl.test.helper.RegistrationTestHelper;
import at.workflow.webdesk.po.licence.LicenceHelper;
import at.workflow.webdesk.po.licence.LicenceViolationException;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoPersonGroup;
import at.workflow.webdesk.po.model.PoRegistrationBean;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;
import at.workflow.webdesk.tools.testing.DataGenerator;
import at.workflow.webdesk.tools.testing.TestingHelper;

public class WTestLicencing extends AbstractTransactionalSpringHsqlDbTestCase {
	
	public class MailServiceMock implements MailService {
		
		private Logger logger = Logger.getLogger(this.getClass());

		public int noOfMailsSent = 0;
		public Mail lastMailSent;
		
		public void sendMail(Mail mail, MailServerConfiguration configuration) {
			logger.info("send Mail to " + mail.getTo());
			lastMailSent = mail;
			noOfMailsSent++;
		}

		public void sendMail(Mail mail) {
			logger.info("send Mail to " + mail.getTo());
			lastMailSent = mail;
			noOfMailsSent++;
		}

		public void setConfiguration(MailServerConfiguration configuration) {
			
		}
	}

	//Services
	private PoOrganisationService orgService;
	private PoActionPermissionService permissionService;
	private PoActionService actionService;
	
	private MailServiceMock mailService;

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
		actionService = (PoActionService) getBean("PoActionService");
		
		PoLanguageService langService = (PoLanguageService) getBean("PoLanguageService");
		langService.init();
	}
	
	@Override
	protected void onTearDownAfterTransaction() {
		deactivateLicencing();
		RegistrationTestHelper.clearRegistrationTables(applicationContext);
		TestingHelper.clearAllCaches((CacheManager) this.applicationContext.getBean("CacheManager"));
	}
	
	@Override
	protected void onSetUpBeforeDataGeneration() throws Exception {
		simulateLicence("basic=10");
		PoLicenceInterceptor licenceInterceptor = (PoLicenceInterceptor) getBean("PoLicenceInterceptor");
		mailService = new MailServiceMock();
		licenceInterceptor.setMailService(mailService);
		licenceInterceptor.setLicenceManagerEmail("customer@customer.com");
	}

	private void deactivateLicencing() {
		LicenceReaderHelper lrh = new LicenceReaderHelper();
		lrh.deactivateLicenceCheck((PoLicenceInterceptor) getBean("PoLicenceInterceptor"));
	}
	
	
	private void simulateLicence(String licence) {
		LicenceReaderHelper lrh = new LicenceReaderHelper();
		lrh.simulateLicence(licence, (LicenceReaderImpl) getBean("LicenceReader"), (PoLicenceInterceptor) getBean("PoLicenceInterceptor"));
	}
	
	public void testCreateUsers() {
		
		simulateLicence("basic=100");
		mailService.noOfMailsSent = 0;
		
		// create 90 persons
		PoGroup group = this.orgService.findGroupByShortName("G01");
		for (int i=10; i<100; i++) {
			createPerson(i, group, true);
		}
		
		logger.info("No of Persons=" + this.orgService.findAllCurrentPersons().size());
		logger.info("No of Users=" + this.orgService.findAllCurrentActivePersons().size());
		
		try {
			createPerson(101,group, true);
			fail("this should fail with a licenceviolation...");
		} catch (LicenceViolationException e) {
			logger.info("caught Licence-Exception: " + e.getMessage());
		}
		
		System.out.println("mail:");
		System.out.println(mailService.lastMailSent.getMessage());
		assertTrue(mailService.noOfMailsSent == 2);
		
		
	}
	
	public void testCreateUsersAndEmployees() {
		
		simulateLicence("employee=200,basic=100");
		
		// create 90 persons
		PoGroup group = this.orgService.findGroupByShortName("G01");
		for (int i=10; i<100; i++) {
			createPerson(i, group, true);
		}
		
		logger.info("No of Persons=" + this.orgService.findAllCurrentPersons().size());
		logger.info("No of Users=" + this.orgService.findAllCurrentActivePersons().size());

		// create 100 additional employees
		for (int i=100; i<200; i++) {
			createPerson(i, group, false);
		}
		
		logger.info("No of Persons=" + this.orgService.findAllCurrentPersons().size());
		logger.info("No of Users=" + this.orgService.findAllCurrentActivePersons().size());

		// try to create an additional person
		try {
			createPerson(201,group, true);
			fail("this should fail with a licenceviolation...");
		} catch (LicenceViolationException e) {
			logger.info("caught Licence-Exception: " + e.getMessage());
			
		}
	}
	
	
	public void testCreateAdminUsersAndAssignActions() throws UpgradeFailedException {
		registerModuleXX();
    	
    	// set new licence
    	simulateLicence("basic=100");
    	
    	// try to assign ham xx_action1 -> this should fail
    	PoPerson ham = this.orgService.findPersonByUserName("ham");
    	
    	try {
    		assignPermission("xx_action1.act", ham);
    		fail("it should not be allowed to assign permission for xx_action1.act as system has no licence!");
    	} catch (Exception e) {
    		
    	}
    	
    	// set new licence
    	// sysadmin-users allows to use action xx_action1 and xx_action2
    	simulateLicence("basic=100,sysadmin-users=2");
    	assignPermission("xx_action1.act", ham);
    	assignPermission("xx_action1.act", personByUserName("wef"));
    	
    	try {
    		assignPermission("xx_action1.act", personByUserName("duc"));
    		fail("it should not be allowed to assign permission for xx_action1.act as system has no licence!");
    	} catch (Exception e) {
    		
    	}
    	
		System.out.println("mail:");
		System.out.println(mailService.lastMailSent.getMessage());
		
	}
	
	public void testCreateAdminFullUsersAndAssignActions() throws UpgradeFailedException {
		registerModuleXXandXY();
    	
    	// set new licence
    	simulateLicence("basic=100");
    	
    	// try to assign ham xx_action1 -> this should fail
    	PoPerson ham = this.orgService.findPersonByUserName("ham");
    	
    	try {
    		assignPermission("xy_action1.act", ham);
    		fail("it should not be allowed to assign permission for xy_action1.act as system has no licence!");
    	} catch (Exception e) {
    		
    	}
    	
    	// set new licence
    	// sysadmin-full allows to use action xx_action1 and xx_action2
    	simulateLicence("basic=100,sysadmin-full=2");
    	assignPermission("xx_action1.act", ham);
    	assignPermission("xy_action1.act", ham);
    	
    	try {
    		assignPermission("xx_action2.act", personByUserName("ham"));
    		fail("it should not be allowed to assign permission for xx_action2.act as system has no licence!");
    	} catch (Exception e) {
    		
    	}
    	
		System.out.println("mail:");
		System.out.println(mailService.lastMailSent.getMessage());
		
	}
	
	@Ignore("see WD-118")
	@Test
	public void dotestCreateAdminUsersAndAssignActionsByGroup() throws UpgradeFailedException {
		registerModuleXX();
    	
    	// set new licence
    	// sysadmin-users allows to use action xx_action1 and xx_action2
    	simulateLicence("basic=10,sysadmin-users=2");
    	assignPermission("xx_action1.act", groupByName("G01"), false);
    	
    	System.out.println("G01 has " + orgService.findPersonsOfGroup(groupByName("G01"),null).size() + " members");
    	
    	try {
    		PoPerson person = personByUserName("keg");
    		orgService.linkPerson2Group(person, groupByName("G01"));
    		
//    		PoPersonGroup pg = new PoPersonGroup();
//    		pg.setGroup( groupByName("G01") );
//    		person.addMemberOfGroup( pg );
//    		orgService.updatePerson(person);
    		
    		fail("It should not be allowed to move another person to G01, as it would violate usage of xx_action1");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

    	// reassigning should work
    	assignPermission("xx_action1.act", groupByName("G01"), false);
    	
    	// try reassigning persmission to G01 -> but with inheritence to subgroups
    	try {
    		assignPermission("xx_action1.act", groupByName("G01"), true);
    		fail("It should not be allowed to assign permission for xx_action1 to G01 with inheritence!");
    	} catch (Exception e) {
    		
    	}
	}
    	
    	
	public void testCreateEmployeesWhenSpecialLicencePresent() throws UpgradeFailedException {
		registerModuleXX();
    	
    	// set new licence
    	// sysadmin-users allows to use action xx_action1 and xx_action2
    	simulateLicence("basic=10,sysadmin-users=2,employee=20");
    	
		// create 10 employees
		PoGroup group = this.orgService.findGroupByShortName("G01");
		for (int i=10; i<20; i++) {
			createPerson(i, group, false);
		}
		
		assertTrue(this.orgService.findAllCurrentActivePersons().size()<=10);
		assertTrue(this.orgService.findAllCurrentPersons().size()<=20);
		
		assignPermission("xx_action1.act", groupByName("G01"), false);

		
	}
	
	public void testAssignNegativePermissions() throws UpgradeFailedException {
		registerModuleXX();
		
		final String actionName = "xx_action1.act"; 
		
    	// set new licence
    	// sysadmin-users allows to use action xx_action1 and xx_action2
    	simulateLicence("basic=10,sysadmin-users=10");
		assignPermission(actionName, groupByName("G01"), true);
		
		assignNegativePermission(actionName, personByUserName("wef"));
		
		PoAction action = actionService.findActionByURL(actionName);
		assertTrue( getLicenceHelper().getNoOfUsersWithPermissionForAction(action) == 9);
	}
	
	private LicenceHelper getLicenceHelper() {
		return (LicenceHelper) getBean("PoLicenceHelper");
	}
	
	public void testAssignClientPermission() throws UpgradeFailedException {
		registerModuleXX();
		
		final String actionName = "xx_action1.act"; 
		
    	// set new licence
    	// sysadmin-users allows to use action xx_action1 and xx_action2
    	simulateLicence("basic=10,sysadmin-users=8");
    	
    	// assign Permission for complete client
    	try {
    		assignPermission(actionName, groupByName("G01").getClient());
    		fail("This should fail with a licence violation, as client has 10 users!");
    	} catch (Exception e) {
    		// this should fail with a Licence Violation
    	}
	}
	public void testAssignGeneralPermission() throws UpgradeFailedException {
		registerModuleXX();
		
		final String actionName = "xx_action1.act"; 
		
    	// set new licence
    	// sysadmin-users allows to use action xx_action1 and xx_action2
    	simulateLicence("basic=10,sysadmin-users=8");
    	
    	// assign Permission for complete client
    	try {
    		assignGeneralPermission(actionName);
    		fail("This should fail with a licence violation, as complete system (all clients) has 10 users!");
    	} catch (Exception e) {
    		// this should fail with a Licence Violation
    	}
		
	}
	
	private PoAction createAndSaveConfig(PoAction action, String configName) {
		PoAction config = new PoAction();
		config.setName( configName );
		config.setActionType(PoConstants.ACTION_TYPE_CONFIG);
		action.addChild(config);
		actionService.saveAction( config );
		return config;
	}
	
	public void testAssignNegativePermissionsToActionVariants() throws UpgradeFailedException {
		
		registerModuleXXandXY();
		
		// set new licence
		// sysadmin-full allows to use action xy_actions
		simulateLicence("basic=10,sysadmin-full=8");
		
		final String actionName = "xy_action3.act";
		
		PoAction action = actionService.findActionByURL(actionName);
		
		PoAction config1 = createAndSaveConfig(action, "xy_config1");
		PoAction config2 = createAndSaveConfig(action, "xy_config2");
		
		String config1Name = config1.getName()+".cact";
		assignPermission( config1Name , personByUserName("wef"));
		assignPermission( config1Name , personByUserName("ham"));
		
		String config2Name = config2.getName()+".cact";
		assignPermission( config2Name , personByUserName("wuh"));
		assignPermission( config2Name , personByUserName("scr"));
		assignPermission( config2Name , personByUserName("wef"));
		
		// we should get 4 persons using the action, 2 from config1 and 2 from config2
		// as wef is already counted in config1 it will not be counted in config2!
		assertTrue( getLicenceHelper().getNoOfUsersWithPermissionForActionAndChildConfigs(action) == 4);
		
		assignNegativePermission(actionName, groupByName("G01"));
		assertTrue( getLicenceHelper().getNoOfUsersWithPermissionForActionAndChildConfigs(action) == 4);
		
		// now we should have only 2 persons which are allowed for the action
		// as the config 'config2' has a negative permission for all persons
		assignNegativePermission(config2Name, groupByName("G01"));
		assertTrue( getLicenceHelper().getNoOfUsersWithPermissionForActionAndChildConfigs(action) == 2);
		
	}
	

	private PoGroup groupByName(String name) {
		return orgService.findGroupByShortName(name);
	}

	private void registerModuleXX() throws UpgradeFailedException {
		// register new module xx
		// containing 2 new actions: xx_action1 and xx_action2
		
		PoModuleUpdateServiceImpl moduleUpdateService = RegistrationTestHelper.createModuleUpdateServiceManually(this.applicationContext);
		PoRegistrationServiceImpl registrationService = (PoRegistrationServiceImpl) getBean("PoRegistrationServiceTarget");
		
		Map<String, PoRegistrationBean> registrationBeanMap = new HashMap<String, PoRegistrationBean>();
		List<String> modules = new ArrayList<String>();
		
		addModule("xx", registrationBeanMap, modules);

    	moduleUpdateService.installModules(registrationBeanMap);
    	registrationService.runRegistration(modules, true, registrationBeanMap);
	}
	
	private void registerModuleXXandXY() throws UpgradeFailedException {
		
		PoModuleUpdateServiceImpl moduleUpdateService = RegistrationTestHelper.createModuleUpdateServiceManually(this.applicationContext);
		PoRegistrationServiceImpl registrationService = (PoRegistrationServiceImpl) getBean("PoRegistrationServiceTarget");
		
		Map<String, PoRegistrationBean> registrationBeanMap = new HashMap<String, PoRegistrationBean>();
		List<String> modules = new ArrayList<String>();
		
		addModule("xx", registrationBeanMap, modules);
    	addModule("xy", registrationBeanMap, modules);

    	moduleUpdateService.installModules(registrationBeanMap);
    	registrationService.runRegistration(modules, true, registrationBeanMap);
	}
	
	private PoPerson personByUserName(String userName) {
		 return orgService.findPersonByUserName(userName);
	}
	
	private void assignNegativePermission(String actionName, PoPerson person) {
		PoAction action = actionService.findActionByURL(actionName);
		permissionService.assignNegativePermission(action, person, new Date(), new Date(DateTools.INFINITY_TIMEMILLIS));
	}
	
	private void assignNegativePermission(String actionName, PoGroup group) {
		PoAction action = actionService.findActionByURL(actionName);
		permissionService.assignNegativePermission(action, group, new Date(), new Date(DateTools.INFINITY_TIMEMILLIS), true);
	}
	
	private void assignPermission(String actionName, PoPerson person) {
		PoAction action = actionService.findActionByURL(actionName);
		permissionService.assignPermission(action, person, new Date(), new Date(DateTools.INFINITY_TIMEMILLIS), PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT);
	}
	
	private void assignPermission(String actionName, PoGroup group, boolean inheritToChilds) {
		PoAction action = actionService.findActionByURL(actionName);
		permissionService.assignPermission(action, group, new Date(), new Date(DateTools.INFINITY_TIMEMILLIS), inheritToChilds, PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT);
	}
	
	private void assignGeneralPermission(String actionName) {
		PoAction action = actionService.findActionByURL(actionName);
		action.setUniversallyAllowed(true);
		actionService.saveAction(action);
	}
	
	private void assignPermission(String actionName, PoClient client) {
		PoAction action = actionService.findActionByURL(actionName);
		permissionService.assignPermission(action, client, new Date(), new Date(DateTools.INFINITY_TIMEMILLIS), PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT);
	}
	

	private void addModule(String moduleName, Map<String, PoRegistrationBean> registrationBeanMap, List<String> modules) {
		registrationBeanMap.put("PoRegistrationBean_" + moduleName, RegistrationTestHelper.createRegistrationBean(moduleName));
    	modules.add(moduleName);
	}

	private void createPerson(int i, PoGroup group, boolean active) {
		final String clientName = getClass().getSimpleName();	// avoid potentially missing unit test isolation
		PoClient client = orgService.findClientByName(clientName);
		if (client == null)	{
			client = new PoClient();
			client.setName(clientName);
			client.setShortName("TC");
			orgService.saveClient(client);
		}

		PoPerson person = new PoPerson();
		person.setFirstName("firstname" + i);
		person.setLastName("lastname" + i);
		person.setUserName("username" + i);
		person.setEmployeeId("employeeid" + i);
		person.setActiveUser(active);
		person.setClient(client);
		
		this.orgService.savePerson(person, group);
	}



}
