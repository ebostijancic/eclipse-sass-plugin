package at.workflow.webdesk.po;

import java.io.InputStream;
import java.util.List;

import org.w3c.dom.Document;

import at.workflow.webdesk.po.model.PoJob;
import at.workflow.webdesk.po.model.PoJobStateInfo;
import at.workflow.webdesk.po.model.PoJobTrigger;

/**
 *<p> 
 * The JobService class offers functions to 
 * save, update and delete PoJob and PoJobTrigger objects.
 *</p> 
 *<p> 
 * Additionally it does all the needed tasks to schedule a Job,
 * with one or more triggers. 
 * Just set a job and its trigger to active 
 * and store them, the service will do the rest. 
 * </p>
 * Created on 13.10.2005
 * @author ggruber, hentner
 * 
 * @see at.workflow.webdesk.po.PoActionService
 * @see at.workflow.webdesk.po.PoOrganisationService
 * @see at.workflow.webdesk.po.PoRegistrationService
 * @see at.workflow.webdesk.po.PoModuleService
 * @see at.workflow.webdesk.po.PoFileService
 * @see at.workflow.webdesk.po.PoLogService
 * @see at.workflow.webdesk.po.PoLanguageService
 * @see at.workflow.webdesk.po.PoBeanPropertyService
 * @see at.workflow.webdesk.po.model.PoJob
 */
public interface PoJobService {

	/**
	 * is the job scheduled on this cluster node?
	 */
	public boolean isJobScheduled(PoJob job);
	
	/**
	 * is the given job running on the current cluster node?
	 */
	public boolean isJobRunning(PoJob job);
	
	/**
     * 
     * Returns all Triggers.
     * 
     * 
	 * @return a list of PoJob triggers.
	 */
	public List<PoJobTrigger> loadAllJobTriggers();
	
	/**
     * Saves (null) or updates (uid valid) 
     * a JobTrigger (depends on uid).
     * 
     * If the trigger is active then its <a href="PoJob.html">job</a> 
     * (if its active)is scheduled.
     * 
     * 
	 * @param trigger the job trigger
	 */
	public void saveOrUpdateJobTrigger(PoJobTrigger trigger);
	
	/**
     * Deletes the PoJobTrigger object. If the trigger has an 
     * assosiated <a href="PoJob.html">job</a> it is unscheduled.
     * 
	 * @param trigger
	 */
	public void deleteJobTrigger(PoJobTrigger trigger);
	
	
    /**
     * @return a list of all configurable <a href="PoJob.html">jobs</a> .
     */
    public List<PoJob> findAllConfigurableJobs();
    
    /**
     * Tries to find a PoJob by its Name. Returns NULL if no PoJob could be found! 
     * @param name
     * @return PoJob
     */
    public PoJob findPoJobByName(String name);
    
	/**
     * @param includeConfigurableJobs defines wether to include configurable jobs or not
	 * @return a list of all PoJob objects.
	 */
	public List<PoJob> loadAllJobs(boolean includeConfigurableJobs);
	
	/**
     * Returns a PoJob object or null.
     * 
     * @param uid
	 * @return a PoJob object if one with the given uid exists, null otherwise.
	 */
	public PoJob getJob(String uid);
	
	/**
	 * @param uid
	 * @return a PoJobTrigger object if one with the given uid exists, null otherwise.
	 */
	public PoJobTrigger getJobTrigger(String uid);
	
	/**
     * 
     * Saves (null) or updates (uid valid) a Job (depends on uid).
     * If the job is active and has one or more associated triggers, 
     * then the job is scheduled with each active trigger.
     * @param job
	 */
	public void saveOrUpdateJob(PoJob job);
	
	/**
     * 
     * Deletes the PoJob object. If the job has one or more 
     * associated trigger(s) all triggers will be
     * unscheduled.
	 * @param job
	 */
	public void deleteJob(PoJob job);
	
	/**
     * 
     * Runs the given job exactly once.
     * 
	 * @param job
	 * @return uid of LogDetail for the started job
	 */
	public String runJobOnce(PoJob job);
    
    
    /**
     * Use this function to find active jobs of a trigger.
     * 
     * @param job
     * @return a list of PoJobTrigger objects that correspond to the given job.
     */
    public List<PoJobTrigger> findActiveTriggersOfJob(PoJob job);

    
    /**
     * returns assigned Config XML of Job. 
     * 
     * @param job
     * @return
     */
    public Document getConfigXmlOfJob(PoJob job);
    
    
    /**
     * 
     * @param inputStream
     * @return a PoJob object
     */
    public PoJob getJobFromConfigFile(InputStream inputStream);
    
    
    /**
     * 
     * Registers a PoJob and maybe a PoJobTrigger object if the given inputStream
     * is a valid job descriptor.
     * 
     * @param inputStream
     * @param path
     * @param module
     */
    public void registerPoJobs(InputStream inputStream, String path, String module);
    
    
    /**
     * This function is used to schedule all 
     * jobs. (though the schedule depends on the configuration of
     * the job and its trigger. In other words both trigger and job
     * has to be valid and set to active).
     */
    public void scheduleAll();
    
    
    /**
     * initialises the scheduler
     */
    public void init();
    
    
    /**
     * This function returns a List filled with information about
     * every trigger that is scheduled inside the quartz scheduler.
     * It extracts information like groupname, name, running, ...
     * 
     * The List contains java.util.Map objects, with key - value pairs
     * 
     * 
     * 
     * @return a java.util.List object
     */
    public List<PoJobStateInfo> getCurrentStatus();
    
    
    /**
     * Interrupts Job with given name and group. The job has to implement InterruptableJob in order to be interruptable.
     * 
     * 
     * @param jobName the name of the real job, not the PoJob
     * @param jobGroup the name of the real jobgroup
     * @return true if the job was interrupted, false otherwise
     */
    public boolean interrupt(String realJobName, String realJobGroup);
    
    
    /**
     * @param trigger
     * @return the details of the passed trigger
     */
    public String getTriggerDetails(PoJobTrigger trigger);
    
    /**
     * this method is called by jobs started by the job engine
     * to return the actual log uid 
     * 
     * @param jobTriggerUid
     * @param logUid
     */
    public void informAboutLogUidOfJob(String jobTriggerUid, String logUid);
    
    /**
     * checks whether the passed String contains a valid cron expression
     * 
     * @param cronExpression
     * @return true, if expression is valid
     */
    public boolean isCronExpressionValid(String cronExpression);
    
    /**
     * @return no of currently active threads
     */
    public int getNoOfSchedulerThreads();
    
    /**
     * Set the Number active threads. Needs reinitialization afterwards in
     * order to get active!
     */
    public void setNoOfSchedulerThreads(int noOfSchedulerThreads);
    
    /**
     * Returns boolean, if Job-Engine is running on current server.
     */
    public boolean isJobEngineRunning();
    

}
