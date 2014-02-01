package at.workflow.webdesk.po.impl.test.nontransactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import at.workflow.webdesk.po.PoActionService;
import at.workflow.webdesk.po.PoJobService;
import at.workflow.webdesk.po.PoLanguageService;
import at.workflow.webdesk.po.UpgradeFailedException;
import at.workflow.webdesk.po.impl.PoModuleUpdateServiceImpl;
import at.workflow.webdesk.po.impl.PoRegistrationServiceImpl;
import at.workflow.webdesk.po.impl.test.helper.RegistrationTestHelper;
import at.workflow.webdesk.po.model.PoJob;
import at.workflow.webdesk.po.model.PoRegistrationBean;
import at.workflow.webdesk.tools.testing.AbstractAutoCommitSpringHsqlDbTestCase;

/**
 * Tests the registration process in PoRegistration.
 * runs *WITHOUT* transaction -> everything will be committed to database! 
 * @author ggruber
 */
public class WTestPoRegistrationServiceImpl extends AbstractAutoCommitSpringHsqlDbTestCase {
	
    //Services
    private PoLanguageService languageService;
    private PoActionService actionService;
    
    // take implementations here to have access to
    // method variants which are not public
    private PoRegistrationServiceImpl registrationService;
    private PoModuleUpdateServiceImpl moduleUpdateService;
    
	private List<String> modules = new ArrayList<String>();
	private Map<String, PoRegistrationBean> registrationBeanMap = new HashMap<String, PoRegistrationBean>();
    
    
    @Override
    protected void onSetUp() throws Exception {
    	
    	super.onSetUp();
    	
		System.out.println(" ------ get services and clear tables ------ ");
		this.registrationService = (PoRegistrationServiceImpl) this.applicationContext.getBean("PoRegistrationServiceTarget");
		this.languageService = (PoLanguageService) this.applicationContext.getBean("PoLanguageService");
		this.actionService = (PoActionService) this.applicationContext.getBean("PoActionService");
		this.moduleUpdateService = RegistrationTestHelper.createModuleUpdateServiceManually(this.applicationContext);
    }
    
    @Override
	protected void clearUsedTables() {
    	RegistrationTestHelper.clearRegistrationTables(applicationContext);
	}

    
	@Ignore("see WD-118")
	@Test
    public void dotestRegistrationAndDeRegistrationOfModulesActionsAndTextModules() throws UpgradeFailedException {
    	
    	// we define 2 modules
    	// xx has 2 actions and 9 textmodules
    	// xy has 3 actions and 8 textmodules
    	
    	addModule("xx");
    	
    	this.moduleUpdateService.installModules(registrationBeanMap);
    	this.registrationService.runRegistration(modules, true, registrationBeanMap);
    	assertEquals(2, actionService.loadAllCurrentActions().size());
    	// fri_2013-12-13: failed 2013-12-09, 2013-12-11, 2013-12-13
    	
    	this.moduleUpdateService.installModules(registrationBeanMap);
    	this.registrationService.runRegistration(modules, true, registrationBeanMap);
    	assertEquals(2, actionService.loadAllCurrentActions().size());
    	
    	int countTextModules = this.languageService.findTextModules(this.languageService.findDefaultLanguage()).size();
    	assertTrue("We should have 9 textmodules, but got " + countTextModules, countTextModules==9);
    	
    	// add another module
    	addModule("xy");

    	this.moduleUpdateService.installModules(registrationBeanMap);
    	this.registrationService.runRegistration(modules, true, registrationBeanMap);
    	assertTrue(this.actionService.loadAllCurrentActions().size() == 5);
    	assertTrue(this.languageService.findTextModules(this.languageService.findDefaultLanguage()).size()==17);
    	
    	// remove first module
    	removeModule("xx");

    	this.moduleUpdateService.installModules(registrationBeanMap);
    	this.registrationService.runRegistration(modules, true, registrationBeanMap);
    	
    	int countActions = this.actionService.loadAllCurrentActions().size();
    	assertTrue("we should have 4 actions, but system has now " + countActions + " actions...", this.actionService.loadAllCurrentActions().size() == 3);
    	countTextModules = this.languageService.findTextModules(this.languageService.findDefaultLanguage()).size();
    	assertTrue("we should have 8 textmodules, but system has now " + countTextModules + " textModules...",countTextModules==8);

    	// read module and see if actions/textmodules get reactivated
    	addModule("xx");

    	this.moduleUpdateService.installModules(registrationBeanMap);
    	this.registrationService.runRegistration(modules, true, registrationBeanMap);
    	assertTrue(this.actionService.loadAllCurrentActions().size() == 5);
    	assertTrue(this.languageService.findTextModules(this.languageService.findDefaultLanguage()).size()==17);

    }
    
	public void testRegisterJobWithTriggerMultipleTimes() {
		
		PoJobService jobService = (PoJobService) getBean("PoJobService");
		jobService.init();
		
        Resource[] ress = { new ClassPathResource("at/workflow/webdesk/po/impl/test/regdata/xx/jobs/regJob/job-descr.xml") };
        registrationService.registerJobs(ress, "xx");
        
        PoJob job = jobService.findPoJobByName("TestRegistration Job");
        assertTrue(job!=null);
        assertTrue(jobService.findActiveTriggersOfJob(job).size()==1);
        assertTrue(jobService.isJobScheduled(job));
        
        // register AND check again
        registrationService.registerJobs(ress, "xx");
        job = jobService.findPoJobByName("TestRegistration Job");
        assertTrue(jobService.findActiveTriggersOfJob(job).size()==1);
        assertTrue(jobService.isJobScheduled(job));
	}

	private void addModule(String moduleName) {
		registrationBeanMap.put("PoRegistrationBean_" + moduleName, RegistrationTestHelper.createRegistrationBean(moduleName));
    	modules.add(moduleName);
	}
	
	private void removeModule(String moduleName) {
		registrationBeanMap.remove("PoRegistrationBean_" + moduleName);
		modules.remove(moduleName);
	}


}
