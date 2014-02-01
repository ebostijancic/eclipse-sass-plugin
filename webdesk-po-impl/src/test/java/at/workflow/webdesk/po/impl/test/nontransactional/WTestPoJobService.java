package at.workflow.webdesk.po.impl.test.nontransactional;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoJobService;
import at.workflow.webdesk.po.PoLogService;
import at.workflow.webdesk.po.PoModuleService;
import at.workflow.webdesk.po.PoRegistrationService;
import at.workflow.webdesk.po.jobs.testJob.TestInterruptableJob;
import at.workflow.webdesk.po.jobs.testJob.TestJob;
import at.workflow.webdesk.po.jobs.testJob.TestLongLastingJob;
import at.workflow.webdesk.po.jobs.testJob2.TestJobMultipleRuns;
import at.workflow.webdesk.po.model.PoJob;
import at.workflow.webdesk.po.model.PoJobStateInfo;
import at.workflow.webdesk.po.model.PoJobTrigger;
import at.workflow.webdesk.po.model.PoLog;
import at.workflow.webdesk.po.model.PoModule;
import at.workflow.webdesk.tools.DaoJdbcUtil;
import at.workflow.webdesk.tools.testing.AbstractAutoCommitSpringHsqlDbTestCase;
import at.workflow.webdesk.tools.testing.TestingHelper;

/**
 * Tests starting a Job and its writing to the database Logs.
 * runs *WITHOUT* transaction -> everything will be committed to database! 
 * 
 * fri_2012-06-01: TODO: Ich weiss nicht, was der Test WTestPoJobService macht,
 * dass er so instabil ist, aber ich weiss, dass es ich diesen Unit-Test am öftesten "failed"
 * antreffe, wenn ich morgens Hudson checke, d.h. er verursacht ständig sinnlose Aufwände!

 * @author ggruber
 * @author sdzuban
 */
public class WTestPoJobService extends AbstractAutoCommitSpringHsqlDbTestCase {

	private PoJobService jobService;
	private PoLogService logService;
	private PoModuleService moduleService;
	private PoRegistrationService registrationService;
	
	/** @return true to enable lazy loading of references to other persistent objects. */
	@Override
	protected boolean createAndKeepOpenPersistenceSession() {
		return true;
	}
	
    @Override
    protected void onSetUp() throws Exception {
    	
    	super.onSetUp();
    	
    	TestingHelper.configureLogging("at/workflow/webdesk/po/impl/test/log4jDb.xml");	// fri_2014-01-20: had a dead-lock with running job here, occurring in testDeleteJob()
    	
    	registrationService= (PoRegistrationService) getBean("PoRegistrationService");
    	jobService = (PoJobService) getBean("PoJobService");
    	logService = (PoLogService) getBean("PoLogService");
    	moduleService = (PoModuleService) getBean("PoModuleService");
    	
		// Start Scheduler
    	jobService.init();
    }
	
	
    @Override
	protected void clearUsedTables() {
		DaoJdbcUtil daoJdbcUtil = (DaoJdbcUtil) this.applicationContext.getBean("DaoJdbcUtil");
		daoJdbcUtil.execute("delete from PoJobTrigger", DaoJdbcUtil.DATASOURCE_WEBDESK);
		daoJdbcUtil.execute("delete from PoJob", DaoJdbcUtil.DATASOURCE_WEBDESK);
		daoJdbcUtil.execute("delete from PoModule", DaoJdbcUtil.DATASOURCE_WEBDESK);
//		daoJdbcUtil.execute("delete from PoLogDetail", DaoJdbcUtil.DATASOURCE_WEBDESK);
//		daoJdbcUtil.execute("delete from PoLog", DaoJdbcUtil.DATASOURCE_WEBDESK);
	}
	
	public void testRunTestJob() throws Exception {
        Resource[] ress = { new ClassPathResource("at/workflow/webdesk/po/jobs/testJob/job-descr.xml") };
        registrationService.registerJobs(ress, "xx");
		
		PoJob myJob = jobService.findPoJobByName("Test Job");
		
		// the job writes logdetails (at first run one more!)
		String uid = jobService.runJobOnce(myJob);
		Thread.sleep(100);	// fri_2013-03-08
		assertNotNull(uid);	// fri_2011-04-07 added this as I got an exception from logService.getLog(uid), local test execution
		// fri_2012-12-13: AssertionFailedError
		// fri_2014-01-20: got this executing local
		
		assertTrue(TestJob.messageWritten != null);	// fri_2012-10-23: AssertionFailedError, fri_2013-02-28: AssertionFailedError, fri_2013-03-08: AssertionFailedError
		Thread.sleep(100);	// fri_2010-12-01
		assertTrue(TestJob.messageWritten.before(new Date()));
		assertTrue(TestJob.message != null);
		assertTrue(TestJob.message.endsWith(TestJob.messageWritten.toString()));
		
		PoLog jobLog = logService.getLog(uid);
		assertEquals(4, logService.findLogDetails(jobLog).size());
		
		// the second time the job returns only 2 logdetail 
		String uid2 = jobService.runJobOnce(myJob);
    	Thread.sleep(1000);	// // fri_2013-03-25: trying to fix this notoriously failing test, not reproducible locally
		PoLog jobLog2 = logService.getLog(uid2);	// fri_2012-06-01: on Hudson only: IllegalArgumentException: id to load is required for loading 
		assertEquals(3, logService.findLogDetails(jobLog2).size());	// fri_2013-03-21: Hudson fail: expected:<3> but was:<0> // fri_2013-03-22: Hudson fail: expected:<3> but was:<1>	// fri_2013-03-25: Hudson fail: expected:<3> but was:<2>

		// delete trigger and job
		jobService.deleteJob(myJob);
	}
	
	public void testCheckUniqueJobInstances() throws Exception {
        Resource[] ress = { new ClassPathResource("at/workflow/webdesk/po/jobs/testJob2/job-descr.xml") };
        registrationService.registerJobs(ress, "xx");
        
        PoModule module = new PoModule();
        module.setName("xx");
        moduleService.saveModule(module);
        
        PoJob myJob = jobService.findPoJobByName("Test Job2");
        myJob.setActive(true);
        // fri_2014-01-20: NullPointerException, on Hudson and locally - missing resource testJob2/job-descr.xml ?
        myJob.setModule(module);
        jobService.saveOrUpdateJob(myJob);
        
        TestJobMultipleRuns.clearHashCodesOfInstances();
        
		// add a CRON Trigger, which runs every second
        PoJobTrigger jt = new PoJobTrigger();
        jt.setActive(true);
        jt.setCronExpression("* * * * * ?");
        jt.setName("testtrigger");
        jt.setScheduleType(PoConstants.CRON_TRIGGER);
        jt.setJob(myJob);
        jobService.saveOrUpdateJobTrigger(jt);
        
        // wait 5 seconds
        Thread.sleep(5 * 1000);

        // there should have been exactly 4 runs!
        assertTrue( "There should have been at least 4 different Job Instances", TestJobMultipleRuns.getHashCodesOfInstances().size()>3 );
	}

	public void testSaveJobTrigger() {
		
		final String jobName= "JobDefinition 1";
		
    	// add a job
		PoJob j = new PoJob();
		j.setName(jobName);
		j.setDescription("Beschreibung");
        jobService.saveOrUpdateJob(j);
        
        j = jobService.getJob(j.getUID());
        
		// add a jobtrigger
        PoJobTrigger jt = new PoJobTrigger();
        jt.setActive(true);
        jt.setCronExpression("");
        jt.setName("testtrigger");
        jt.setScheduleType(PoConstants.DAILY_TRIGGER);
        jt.setJob(j);
        jobService.saveOrUpdateJobTrigger(jt);
        jt = jobService.getJobTrigger(jt.getUID());
        
        assertTrue(jt.isActive());
        assertNotNull(jt.getUID());
        assertNotNull(jt.getName());
        assertEquals("testtrigger", jt.getName());
        assertEquals(jobName, jt.getJob().getName());
        
        jt.setActive(false);
        jt.setName("name");
        jobService.saveOrUpdateJobTrigger(jt);
        jt = jobService.getJobTrigger(jt.getUID());
        
        assertFalse(jt.isActive());
        assertNotNull(jt.getUID());
        assertNotNull(jt.getName());
        assertEquals("name", jt.getName());
        
		// delete trigger and job
		jobService.deleteJobTrigger(jt);
		jobService.deleteJob(j);
    }

	public void testSaveJob() {
		
		final String jobName= "JobDefinition 1";
		
		// add a job
		PoJob j = new PoJob();
		j.setName(jobName);
		j.setDescription("Beschreibung");
		jobService.saveOrUpdateJob(j);
		
		j = jobService.getJob(j.getUID());
		
		// add a jobtrigger
		PoJobTrigger jt = new PoJobTrigger();
		jt.setActive(true);
		jt.setCronExpression("");
		jt.setScheduleType(PoConstants.DAILY_TRIGGER);
		jt.setJob(j);
		jobService.saveOrUpdateJobTrigger(jt);
		jt = jobService.getJobTrigger(jt.getUID());
		
		assertTrue(jt.isActive());
		assertNotNull(jt.getUID());
		assertNotNull(jt.getName());
		assertEquals(jt.getUID(), jt.getName());
		
		// find active triggers 
		List<PoJobTrigger> l = jobService.findActiveTriggersOfJob(j);
		assertEquals(1, l.size());
		assertEquals(jt, l.get(0));
		
		jt.setActive(false);
		jt.setName("testtrigger");
		jobService.saveOrUpdateJobTrigger(jt);
		jt = jobService.getJobTrigger(jt.getUID());
		
		assertFalse(jt.isActive());
		assertEquals("testtrigger", jt.getName());
		
		assertTrue(jobService.loadAllJobs(true).size()>=1);
		
		// find active triggers 
		l = jobService.findActiveTriggersOfJob(j);
		assertEquals(0, l.size());
		
		// delete trigger and job
		jobService.deleteJobTrigger(jt);
		jobService.deleteJob(j);
	}
	
	
	public void testJobOrder() {
		
		final String jobName1 = "A JobDefinition";
		final String jobName2 = "B JobDefinition";
		
		// add a job
		PoJob j = new PoJob();
		j.setActive(true);
		j.setName(jobName1);
		j.setDescription("Beschreibung");
    	j.setJobClass("at.workflow.webdesk.po.jobs.testJob.TestJob");
		jobService.saveOrUpdateJob(j);
		
		j = jobService.getJob(j.getUID());
		
		// add a jobtrigger
		PoJobTrigger jt = new PoJobTrigger();
		jt.setActive(true);
		jt.setCronExpression("");
		jt.setName("nameZ");
		jt.setScheduleType(PoConstants.DAILY_TRIGGER);
		jt.setJob(j);
		jobService.saveOrUpdateJobTrigger(jt);
		jt = jobService.getJobTrigger(jt.getUID());
		
		// add a job
		PoJob j2 = new PoJob();
		j2.setActive(true);
		j2.setName(jobName2);
		j2.setDescription("Beschreibung");
		j2.setJobClass("at.workflow.webdesk.po.jobs.testJob.TestJob");
		jobService.saveOrUpdateJob(j2);
		
		j2 = jobService.getJob(j2.getUID());
		
		// add a jobtrigger
		PoJobTrigger jt2 = new PoJobTrigger();
		jt2.setActive(true);
		jt2.setCronExpression("");
		jt2.setName("nameA");
		jt2.setScheduleType(PoConstants.DAILY_TRIGGER);
		jt2.setJob(j2);
		jobService.saveOrUpdateJobTrigger(jt2);
		jt2 = jobService.getJobTrigger(jt2.getUID());
		
		assertFalse(jt2.equals(jt));
		
		List<PoJobStateInfo> result = jobService.getCurrentStatus();
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(jobName1, result.get(0).getJobName());
		assertEquals(jobName2, result.get(1).getJobName());

		// delete trigger and job
		jobService.deleteJobTrigger(jt);
		jobService.deleteJobTrigger(jt2);
		jobService.deleteJob(j);
	}
	
	public void testTwoTriggers() {
		
		final String jobName= "JobDefinition 1";
		
		// add a job
		PoJob j = new PoJob();
		j.setActive(true);
		j.setName(jobName);
		j.setDescription("Beschreibung");
		j.setJobClass("at.workflow.webdesk.po.jobs.testJob.TestJob");
		jobService.saveOrUpdateJob(j);
		
		j = jobService.getJob(j.getUID());
		
		// add a jobtrigger
		PoJobTrigger jt = new PoJobTrigger();
		jt.setActive(true);
		jt.setCronExpression("");
		jt.setName("nameA");
		jt.setScheduleType(PoConstants.DAILY_TRIGGER);
		jt.setJob(j);
		jobService.saveOrUpdateJobTrigger(jt);
		jt = jobService.getJobTrigger(jt.getUID());
		
		// add a jobtrigger
		PoJobTrigger jt2 = new PoJobTrigger();
		jt2.setActive(true);
		jt2.setCronExpression("");
		jt2.setName("nameZ");
		jt2.setScheduleType(PoConstants.DAILY_TRIGGER);
		jt2.setJob(j);
		jobService.saveOrUpdateJobTrigger(jt2);
		jt2 = jobService.getJobTrigger(jt2.getUID());
		
		assertFalse(jt2.equals(jt));
		
		// find active triggers 
		List<PoJobTrigger> l = jobService.findActiveTriggersOfJob(j);
		assertEquals(2, l.size());
		assertTrue(l.contains(jt));
		assertTrue(l.contains(jt2));
		
		List<PoJobStateInfo> result = jobService.getCurrentStatus();
		assertNotNull(result);
		assertEquals(2, result.size());
		
		// delete trigger and job
		jobService.deleteJobTrigger(jt);
		jobService.deleteJobTrigger(jt2);
		jobService.deleteJob(j);
	}
	
	public void testGetAndFindJob() {
		
		final String jobName= "Get and Find Job";
		
		// add a job
		PoJob j = new PoJob();
		j.setName(jobName);
		j.setDescription("Beschreibung");
		j.setType(PoConstants.JOB); // THIS is crucial for findJobByName
		jobService.saveOrUpdateJob(j);
		
		j = jobService.getJob(j.getUID());
		
		assertNotNull(j);
		assertEquals(jobName, j.getName());
		assertEquals("Beschreibung", j.getDescription());
		assertEquals(PoConstants.JOB, j.getType());
		
		PoJob j2 = jobService.findPoJobByName(jobName);
		
		assertNotNull(j2);
		assertEquals(j, j2);
		
	}
	
	/**
	 * Job with two triggers.
	 * When one is deleted the job and the other must remain
	 * When the second trigger is deleted job is retained.
	 */
	public void testDeleteJobTrigger() {

		final String jobName = "Delete Trigger Job";
		
		// Start Scheduler
    	jobService.init();
		
    	PoJob job = new PoJob();
    	job.setName(jobName);
    	job.setActive(true);
    	job.setJobClass("at.workflow.webdesk.po.jobs.testJob.TestJob");
    	jobService.saveOrUpdateJob(job);

    	assertFalse(jobService.isJobScheduled(job));
    	
    	PoJobTrigger t1 = new PoJobTrigger();
    	t1.setJob(job);
    	t1.setActive(true);
    	jobService.saveOrUpdateJobTrigger(t1);
    	
    	assertTrue(jobService.isJobScheduled(job));
    	
    	List<PoJob> jobs = jobService.loadAllJobs(false);
    	assertNotNull(jobs);
    	assertEquals(1, jobs.size());
    	assertEquals(jobName, jobs.get(0).getName());
    	
    	List<PoJobTrigger> triggers = jobService.loadAllJobTriggers();
    	assertNotNull(triggers);
    	assertEquals(1, triggers.size());
    	String t1UID = triggers.get(0).getUID();
    	// UID is used as triggers name
    	assertEquals(t1UID, triggers.get(0).getName());
    	
    	PoJobTrigger t2 = new PoJobTrigger();
    	t2.setJob(job);
    	t2.setActive(true);
    	jobService.saveOrUpdateJobTrigger(t2);
    	
    	jobs = jobService.loadAllJobs(false);
    	assertNotNull(jobs);
    	assertEquals(1, jobs.size());
    	assertEquals(jobName, jobs.get(0).getName());
    	
    	triggers = jobService.loadAllJobTriggers();
    	assertNotNull(triggers);
    	assertEquals(2, triggers.size());
  
    	jobService.deleteJobTrigger(t2);

    	jobs = jobService.loadAllJobs(false);
    	assertNotNull(jobs);
    	assertEquals(1, jobs.size());
    	assertEquals(jobName, jobs.get(0).getName());
    	assertTrue(job.isActive());
    	assertTrue(jobService.isJobScheduled(jobs.get(0)));
    	
    	triggers = jobService.loadAllJobTriggers();
    	assertNotNull(triggers);
    	assertEquals(1, triggers.size());
    	// UID is used as triggers name
    	assertEquals(t1UID, triggers.get(0).getName());

    	jobService.deleteJobTrigger(t1);
    	
    	jobs = jobService.loadAllJobs(false);
    	assertNotNull(jobs);
    	assertEquals(1, jobs.size());
    	
    	triggers = jobService.loadAllJobTriggers();
    	assertNotNull(triggers);
    	assertEquals(0, triggers.size());
	}
	
	
	// fri_2014-01-21: this test method very frequently generates a dead-lock in onSetup().
	// 		When generating data, it locks with a job-thread on a logger object.
	//		Had this dead-lock yesterday and today, locally.
	//		Gabriel: this is a consequence of the failing testCheckUniqueJobInstances() method, which soon will be fixed.
	public void testDeleteJob() {
		
		final String jobName = "Test delete Job";
		
		PoJob job = new PoJob();
		job.setName(jobName);
		job.setActive(true);
		job.setJobClass("at.workflow.webdesk.po.jobs.testJob.TestJob");
		jobService.saveOrUpdateJob(job);
		
		PoJobTrigger t1 = new PoJobTrigger();
		t1.setJob(job);
		t1.setActive(true);
		jobService.saveOrUpdateJobTrigger(t1);
		
		List<PoJob> jobs = jobService.loadAllJobs(false);
		assertNotNull(jobs);
		assertEquals(1, jobs.size());
		assertEquals(jobName, jobs.get(0).getName());
		assertEquals(1, jobs.get(0).getJobTriggers().size());
		
		List<PoJobTrigger> triggers = jobService.loadAllJobTriggers();
		assertNotNull(triggers);
		assertEquals(1, triggers.size());
		job = jobService.getJob(job.getUID());
		assertEquals(1, job.getJobTriggers().size());
		
		jobService.deleteJob(job);
		
		jobs = jobService.loadAllJobs(false);
		assertNotNull(jobs);
		assertEquals(0, jobs.size());
		
		triggers = jobService.loadAllJobTriggers();
		assertNotNull(triggers);
		assertEquals(0, triggers.size());
	}
	
	/**
	 * Job with two triggers.
	 * When one is deleted the job and the other must remain
	 * When the second is deleted nothing remains
	 * @throws InterruptedException 
	 */
	public void testGetCurrrentStatus() throws InterruptedException {

		final String jobName = "Test job get status";
		
    	PoJob job = new PoJob();
    	job.setName(jobName);
    	job.setActive(true);
    	job.setJobClass("at.workflow.webdesk.po.jobs.testJob.TestJob");
    	jobService.saveOrUpdateJob(job);

    	List<PoJobStateInfo> jobDescriptors = jobService.getCurrentStatus();
    	assertNotNull(jobDescriptors);
    	assertEquals(0, jobDescriptors.size());

    	PoJobTrigger t1 = new PoJobTrigger();
    	t1.setJob(job);
    	t1.setActive(true);
    	jobService.saveOrUpdateJobTrigger(t1);
    	
    	Thread.sleep(250);
    	jobDescriptors = jobService.getCurrentStatus();
    	assertNotNull(jobDescriptors);
    	assertEquals(1, jobDescriptors.size());
    	
		String jobNameFromJobDescriptor = jobDescriptors.get(0).getJobName();
		assertNotNull(jobNameFromJobDescriptor);
		assertEquals(jobName, jobNameFromJobDescriptor);
		assertEquals(t1.getUID(), jobDescriptors.get(0).getTriggerUID());

    	
    	PoJobTrigger t2 = new PoJobTrigger();
    	t2.setJob(job);
    	t2.setActive(true);
    	jobService.saveOrUpdateJobTrigger(t2);
    	
    	Thread.sleep(250);
    	jobDescriptors = jobService.getCurrentStatus();
    	assertNotNull(jobDescriptors);
    	assertEquals(2, jobDescriptors.size());
    	
    	jobService.deleteJobTrigger(t2);

    	jobDescriptors = jobService.getCurrentStatus();
    	assertNotNull(jobDescriptors);
    	assertEquals(1, jobDescriptors.size());
    	
    	jobService.deleteJobTrigger(t1);
    	
    	jobDescriptors = jobService.getCurrentStatus();
    	assertNotNull(jobDescriptors);
    	assertEquals(0, jobDescriptors.size());
    	
	}
	
	/**
	 * Long lasting job with immediate start.
	 * @throws InterruptedException 
	 */
	public void testRunJobOnce() throws InterruptedException {

		final String jobName = "Test job run once";
		
		PoJob job = new PoJob();
		job.setName(jobName);
		job.setActive(true);
		job.setJobClass(TestLongLastingJob.class.getName());
		jobService.saveOrUpdateJob(job);
		
		jobService.runJobOnce(job);
		
		// first wait 500
		final int FIRST_WAIT_TIME = 500;
		Thread.sleep(FIRST_WAIT_TIME);
		assertTrue(jobService.isJobRunning(job));	// expected: job still running
		
		List<PoJobStateInfo> jobDescriptors = jobService.getCurrentStatus();
		assertNotNull(jobDescriptors);
		assertEquals(1, jobDescriptors.size());
		
		// then wait 1500
		// now we waited 2000, this is the time the job lasts
		Thread.sleep(TestLongLastingJob.WAIT_TIME - FIRST_WAIT_TIME + 500); // Job lasts 2000 ms
		assertFalse(jobService.isJobRunning(job));
		
		jobDescriptors = jobService.getCurrentStatus();
		assertNotNull(jobDescriptors);
		assertEquals(0, jobDescriptors.size());
		
	}

	/**
	 * Long lasting job with immediate start.
	 * @throws InterruptedException 
	 */
	public void testInterruptNoninterruptableJob() throws InterruptedException {
		
		final String jobName = "Test job non-interrupt";
		
		PoJob job = new PoJob();
		job.setName(jobName);
		job.setActive(true);
		job.setType(PoConstants.JOB); // THIS is crucial for findJobByName
		job.setConfigurable(false);
		job.setJobClass(TestLongLastingJob.class.getName());
		jobService.saveOrUpdateJob(job);

		assertNotNull(jobService.findPoJobByName(jobName));
		
		jobService.runJobOnce(job);
		
		Thread.sleep(500); 
		assertTrue(jobService.isJobRunning(job));
		List<PoJobStateInfo> jobDescriptors = jobService.getCurrentStatus();
		assertNotNull(jobDescriptors);
		assertEquals(1, jobDescriptors.size());
		
		assertNotNull(jobService.findPoJobByName(jobName));
		
		boolean interrupted = jobService.interrupt(PoConstants.IMMEDIATE_TRIGGER, jobName);
		assertFalse(interrupted);

		assertTrue(jobService.isJobRunning(job));
		jobDescriptors = jobService.getCurrentStatus();
		assertNotNull(jobDescriptors);
		assertEquals(1, jobDescriptors.size());
		
	}
	
	/**
	 * Long lasting job with immediate start, test interrupt.
	 * @throws InterruptedException 
	 */
	public void testInterruptInterruptableJob() throws InterruptedException {
		PoJob job = new PoJob();
		job.setName("Test Job interrupt");
		job.setActive(true);
		job.setType(PoConstants.JOB); // THIS is crucial for findJobByName
		job.setJobClass("at.workflow.webdesk.po.jobs.testJob.TestInterruptableJob");
		jobService.saveOrUpdateJob(job);
		
		jobService.runJobOnce(job);
		
		job = jobService.getJob(job.getUID());
		
		Thread.sleep(500); 
		assertEquals("running", TestInterruptableJob.message);
		List<PoJobStateInfo> triggerMaps = jobService.getCurrentStatus();
		assertNotNull(triggerMaps);
		assertEquals(1, triggerMaps.size());
		
		String jobName =  triggerMaps.get(0).getJobName();
		
		boolean interrupted = jobService.interrupt(PoConstants.IMMEDIATE_TRIGGER, jobName);
		assertTrue(interrupted);
		
		Thread.sleep(300);
		assertEquals("interrupted", TestInterruptableJob.message);
		triggerMaps = jobService.getCurrentStatus();
		assertNotNull(triggerMaps);
		assertEquals(0, triggerMaps.size());
		
	}
	
	
	public void testInterruptJobWithTrigger() throws InterruptedException {
		
		final String jobName= "Job with Trigger";
        final String triggerName = "testtrigger";
		
    	// add a job
		PoJob j = new PoJob();
		j.setName(jobName);
		j.setActive(true);
		j.setType(PoConstants.JOB); // THIS is crucial for findJobByName
		j.setJobClass("at.workflow.webdesk.po.jobs.testJob.TestInterruptableJob");
        jobService.saveOrUpdateJob(j);
        
        TestInterruptableJob.message = "idle";
        
        j = jobService.getJob(j.getUID());

        Calendar cal = GregorianCalendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        // stellen auf nächste volle minute
        cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.MINUTE, 1);
        Date startTime = cal.getTime();
        
		// add a jobtrigger
        PoJobTrigger jt = new PoJobTrigger();
        jt.setActive(true);
        jt.setCronExpression("");
		jt.setName(triggerName);
        jt.setScheduleType(PoConstants.DAILY_TRIGGER);
        jt.setStartTime(startTime);
        jt.setJob(j);
        jobService.saveOrUpdateJobTrigger(jt);
        jt = jobService.getJobTrigger(jt.getUID());

        List<PoJobStateInfo> triggerMaps = jobService.getCurrentStatus();
        assertEquals(1, triggerMaps.size());
        assertEquals(triggerName, triggerMaps.get(0).getTriggerName());
        
        assertEquals("idle", TestInterruptableJob.message);
        String jobTriggerUID = triggerMaps.get(0).getTriggerUID();
        
        // wait for job start
        int count = 150; // max 75 sec
        while(count >= 0 && "idle".equals(TestInterruptableJob.message)) {
        	Thread.sleep(500); 
        	count--;
        }
		
		assertEquals("running", TestInterruptableJob.message);
		
		boolean interrupted = jobService.interrupt(jobTriggerUID, jobName);
		assertTrue(interrupted);
		
		Thread.sleep(300);
		assertEquals("interrupted", TestInterruptableJob.message);
		triggerMaps = jobService.getCurrentStatus();
		assertNotNull(triggerMaps);
		assertEquals(1, triggerMaps.size());
		
    }

}
