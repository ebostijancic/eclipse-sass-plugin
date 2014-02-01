package at.workflow.webdesk.po.impl;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.InterruptableJob;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoFileService;
import at.workflow.webdesk.po.PoJobService;
import at.workflow.webdesk.po.PoModuleService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.daos.PoJobDAO;
import at.workflow.webdesk.po.daos.PoJobTriggerDAO;
import at.workflow.webdesk.po.jobs.WebdeskInterruptableJob;
import at.workflow.webdesk.po.jobs.WebdeskJob;
import at.workflow.webdesk.po.model.PoFile;
import at.workflow.webdesk.po.model.PoJob;
import at.workflow.webdesk.po.model.PoJobStateInfo;
import at.workflow.webdesk.po.model.PoJobTrigger;
import at.workflow.webdesk.po.model.PoOptions;
import at.workflow.webdesk.tools.IfDate;

/**
 * Implementation of the PoJobService which uses Quartz from OpenSymphony internally.
 * Each PoJobTrigger model component maps to a JobTrigger in Quartz. 
 * 
 * In order to retrieve jobs from the quartz scheduler itself, use
 * name (x) of the PoJob object and function "y=getTriggerGroupName(trigger)".
 * -> s.(un)scheduleJob(x,y)
 * 
 * Quartz api see:
 * http://www.quartz-scheduler.org/docs/api/1.8.1/index.html
 * 
 * @author hentner, ggruber, sdzuban
 * 
 */
public class PoJobServiceImpl implements PoJobService, ApplicationContextAware {


	private static final Class<PoGenericInterruptableQuartzJob> GENERIC_QUARTZINTERRUPTABLEJOB_CLASS = PoGenericInterruptableQuartzJob.class;
	private static final Class<PoGenericQuartzJob> GENERIC_QUARTZJOB_CLASS = PoGenericQuartzJob.class;

	private static final Log logger = LogFactory.getLog(PoJobServiceImpl.class);

	private PoJobDAO jobDAO;                            // Zugriffsklasse für Jobs
	private PoJobTriggerDAO jobTriggerDAO;      // Zugriffsklasse für Jobdefinition

	private PoFileService fileService;
	private PoModuleService moduleService;
	private PoOptions poOptions;

	private int noOfSchedulerThreads;
	private boolean active = true;
	
	private Scheduler quartzScheduler;
	private ApplicationContext applicationContext;

	private Map<String,String> jobLogUidMap = new HashMap<String,String>();
	
	/** 
	 * Helper class to write and read the Quartz JobDetail Map
	 */
	static class JobDataHandler {
		
		static final String JOB_DATAMAP_ATTR_JOBMETADATA = "job";
		static final String JOB_DATAMAP_ATTR_JOBCLASS = "webdesk-job-class";
		static final String JOB_DATAMAP_ATTR_APPCTX = "appCtx";
		static final String JOB_DATAMAP_ATTR_CLUSTERNODE = "clusterNode";
		
		static PoJob getMetaData(JobExecutionContext jec) {
			return (PoJob) jec.getJobDetail().getJobDataMap().get(JOB_DATAMAP_ATTR_JOBMETADATA);
		}
		static void setMetaData(JobDetail jd, PoJob job) {
			jd.getJobDataMap().put(JOB_DATAMAP_ATTR_JOBMETADATA, job);
		}
		@SuppressWarnings("unchecked")
		static Class<? extends WebdeskJob> getWebdeskJobClass(JobExecutionContext jec) {
			return (Class<? extends WebdeskJob>) jec.getJobDetail().getJobDataMap().get( JOB_DATAMAP_ATTR_JOBCLASS );
		}
		static void setWebdeskJobClass(JobDetail jd, Class<? extends WebdeskJob> wdJobClass) {
			jd.getJobDataMap().put( JOB_DATAMAP_ATTR_JOBCLASS, wdJobClass);
		}
		static String getClusterNode(JobExecutionContext jec) {
			return (String) jec.getJobDetail().getJobDataMap().get( JOB_DATAMAP_ATTR_CLUSTERNODE);
		}
		static void setClusterNode(JobDetail jd, String clusterNode) {
			jd.getJobDataMap().put(JOB_DATAMAP_ATTR_CLUSTERNODE, clusterNode);
		}
		static ApplicationContext getApplicationContext(JobExecutionContext jec) {
			return (ApplicationContext) jec.getJobDetail().getJobDataMap().get( JOB_DATAMAP_ATTR_APPCTX);
		}
		static void setApplicationContext(JobDetail jd, ApplicationContext appCtx) {
			jd.getJobDataMap().put(JOB_DATAMAP_ATTR_APPCTX, appCtx);
		}
		
	}


	@Override
	public void informAboutLogUidOfJob(String jobTriggerUid, String logUid) {
		synchronized (this.jobLogUidMap) {
			this.jobLogUidMap.put(jobTriggerUid, logUid);
		}
	}

	@Override
	public List<PoJobTrigger> loadAllJobTriggers() {
		return this.jobTriggerDAO.loadAll();
	}

	@Override
	public void saveOrUpdateJobTrigger(PoJobTrigger trigger) {
		
		final String name = trigger.getName();
		if (name == null || "".equals(name)) {
			trigger.setName("temporary name");
			this.jobTriggerDAO.save(trigger);      
			trigger.setName(trigger.getUID());
		}
		this.jobTriggerDAO.save(trigger);
		Collection<PoJobTrigger> triggers = trigger.getJob().getJobTriggers();
		if (triggers.contains(trigger) == false) {
			triggers.add(trigger);
			saveOrUpdateJob(trigger.getJob());
		} else {
			unscheduleTrigger(trigger);
			scheduleTrigger(trigger);
		}
	}

	/**
	 * Schedules the job of the given trigger.
	 * @param trigger
	 */
	private void scheduleTrigger(PoJobTrigger trigger) {
		this.scheduleQuartzJob(trigger.getJob(), trigger,false);
	}

	/**
	 * Unschedules the job of the given trigger.
	 * @param trigger
	 */
	private void unscheduleTrigger(PoJobTrigger trigger) {

		if (trigger.getJob()!=null) {
			logger.info("Going to unschedule Job trigger "+ trigger.getName() + " of job " + trigger.getJob().getName());
			PoJob job = getJob(trigger.getJob().getUID());
			if (job.getJobTriggers().size() <= 1) {
				// unschedule job
				unscheduleQuartzJob(job);
			} else {
				// unschedule trigger
				try {
					quartzScheduler.unscheduleJob(trigger.getUID(), trigger.getJob().getName()); // triggerName, groupName
				} catch (SchedulerException e) {
					logger.error("Unable to unschedule Job trigger "+ trigger.getName() + " of job " + trigger.getJob().getName());
				}
			}
		}
	}

	@Override
	public List<PoJob> loadAllJobs(boolean includeConfigurableJobs) {
		return this.jobDAO.loadAllJobs(includeConfigurableJobs);
	}

	@Override
	public PoJob getJob(String uid) {
		return this.jobDAO.get(uid);
	}

	@Override
	public PoJob findPoJobByName(String jobName) {
		if (this.jobDAO.findJobByNameAndType(jobName, PoConstants.JOB)!=null) 
			return this.jobDAO.findJobByNameAndType(jobName, PoConstants.JOB);
		return this.jobDAO.findJobByNameAndType(jobName, PoConstants.JOB_CONFIG);
	}

	@Override
	public PoJobTrigger getJobTrigger(String uid) {
		return this.jobTriggerDAO.get(uid);
	}

	@Override
	public void saveOrUpdateJob(PoJob job) {
		jobDAO.save(job);
		rescheduleJob(job);
	}
	
	private void rescheduleJob(PoJob job) {
		unscheduleQuartzJob(job);
		scheduleJob(job);
	}

	@Override
	public void deleteJob(PoJob job) {

		// first unschedule
		this.unscheduleQuartzJob(job);

		// delete triggers
		if (job.getJobTriggers()!=null) {
			for (PoJobTrigger trigger : job.getJobTriggers()) {
				jobTriggerDAO.delete(trigger);
			}
		}

		// delete derived jobs
		if (job.getChilds()!=null) {
			for(PoJob tempJob : job.getChilds()) {
				this.deleteJob(tempJob);
			}
		}

		// delete config files
		for (PoFile file : job.getConfigFiles()) {
			this.fileService.deleteFile(file);
		}

		// at last delete job itself
		jobDAO.delete(job);
	}

	@Override
	public void deleteJobTrigger(PoJobTrigger jobTrigger) {

		this.unscheduleTrigger(jobTrigger);
		
		// remove link to be sure we have no strange effects!
		PoJob job = getJob(jobTrigger.getJob().getUID());
		job.getJobTriggers().remove(jobTrigger);
		
		this.jobTriggerDAO.delete(jobTrigger);        
	}

	public void setJobDAO(PoJobDAO poJobDAO) {
		this.jobDAO = poJobDAO;
	}

	public void setJobTriggerDAO(PoJobTriggerDAO poJobTriggerDAO) {
		this.jobTriggerDAO = poJobTriggerDAO;
	}


	@Override
	public List<PoJobTrigger> findActiveTriggersOfJob(PoJob job) {
		return this.jobTriggerDAO.findActiveTriggersOfJob(job);
	}

	public List<PoJob> findAllActiveJobs() {
		return this.jobDAO.findAllActiveJobs();
	}

	@Override
	public List<PoJob> findAllConfigurableJobs() {
		return this.jobDAO.findAllConfigurableJobs();
	}

	@Override
	public org.w3c.dom.Document getConfigXmlOfJob(PoJob job) {

		// make sure job is loaded correctly with a new session
		PoJob myJob = this.getJob(job.getUID());

		if (myJob.getConfigFiles().size()>0) {
			String fileId = ((PoFile)(myJob.getConfigFiles().toArray()[0])).getFileId();
			PoFile myFile = this.fileService.getFileWithHighestVersion(fileId);

			// getch xml by UID
			try {
				return fileService.getFileAsXML(myFile.getUID());
			} catch (Exception e) {
				logger.error(e,e);
				throw new PoRuntimeException(e);
			}
		}
		return null;
	}

	@Override
	public void registerPoJobs(InputStream inputStream, String path, String module) {
		PoJob job = this.registerPoJobFromDescriptor(inputStream,module);
		
		// some logger info
		if (job != null && job.getUID() != null && ! job.getUID().equals("")) {
			logger.info("Job " + job.getName() + " saved.");

			for(PoJobTrigger jt :  this.findActiveTriggersOfJob(job)) {
				logger.info(job.getName() + " starts its execution [" + jt.getName() + "] at: " + jt.getStartDate() + ", schedule type is " + jt.getScheduleType() + ".");
			}
		}
	}

	/**
	 * @param inputStream of the job descriptor xml
	 * @param module: name of module
	 * @return a fully qualified and persisted job. If the descriptor contains 
	 * a trigger (or more), it is also saved in the db and associated with the new PoJob 
	 * object.
	 */
	@SuppressWarnings("unchecked")
	private PoJob registerPoJobFromDescriptor(InputStream inputStream, String module) {
		PoJob job = null;
		SAXBuilder builder = new SAXBuilder();
		org.jdom.Document doc;
		try {
			doc = builder.build(inputStream);
			Element root = doc.getRootElement();
			String jobName = root.getChild("name").getText();
			PoJob oldJob = this.jobDAO.findJobByNameAndType(jobName,PoConstants.JOB);

			if (oldJob == null || oldJob.isAllowUpdateOnVersionChange()) {
				// job does not exist OR job exits but is allowed to be updated 
				
				if (oldJob!=null && oldJob.isAllowUpdateOnVersionChange()) {
					// delete job and connections to trigger
					job = oldJob;
					logger.info("Update Job " + oldJob.getName());
					
				} else {
					// new Job
					job = new PoJob();
					job.setName(jobName);
				}
				
				// create new job or update job 
				createJobFromXmlSnippet(module, job, root);
				jobDAO.save(job);

				// register jobtrigger
				if ( jobConfigContainsTriggers(root) ) {

					Iterator<Element> triggerElemItr = ((Element)root.getChildren("jobTriggers").get(0)).getChildren("trigger").iterator();
					while (triggerElemItr.hasNext()) {
						Element triggerElem = triggerElemItr.next();
						// find jobtrigger
						PoJobTrigger jt = this.jobTriggerDAO.findJobTriggerByNameAndJob(triggerElem.getChildText("name"), job);
						if (jt!=null){
							// modify existing named jobtrigger (will be updated!)
							jt = createJobTriggerFromXmlSnippet(triggerElem, jt);
						} else {
							// add new jobtrigger
							jt = new PoJobTrigger();
							jt = createJobTriggerFromXmlSnippet(triggerElem, jt);
						}
						jt.setJob(job);
						this.jobTriggerDAO.save(jt);
					} 
				}
				// now reschedule it!
				rescheduleJob(job);
			} 
		} catch (JDOMException e) {
			job = null;
			logger.error(e,e);
		} catch (IOException e) {
			logger.error(e,e);
			job= null;
		}
		return job;
	}

	private boolean jobConfigContainsTriggers(Element root) {
		return root.getChild("jobTriggers")!=null && 
				((Element)root.getChildren("jobTriggers").get(0)).getChildren("trigger")!=null &&
				((Element)root.getChildren("jobTriggers").get(0)).getChildren("trigger").size()>0;
	}

	private void createJobFromXmlSnippet(String module, PoJob job, Element root) {
		job.setDescription(root.getChildText("description"));
		job.setConfigurable( Boolean.valueOf(root.getChildText("isConfigurable")) );
		job.setJobClass(root.getChildText("jobClass"));
		if (job.getUID()==null) {
			// modify active only if it is a new job!
			job.setActive( Boolean.valueOf(root.getChildText("active")) );
		}
		job.setAllowUpdateOnVersionChange( Boolean.valueOf(root.getChildText("updateOnVersionChange")) );
		job.setType(new Integer(PoConstants.JOB));
		job.setModule(this.moduleService.getModuleByName(module));
	}

	private PoJobTrigger createJobTriggerFromXmlSnippet(Element triggerElem, PoJobTrigger jt) {
		jt.setActive( Boolean.valueOf(triggerElem.getChildText("active")) );
		jt.setCronExpression(triggerElem.getChildText("cronExpression"));

		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern(PoConstants.DEFAULT_DATE_PATTERN);
		try {
			if (triggerElem.getChildText("startDate")!=null && !"".equals(triggerElem.getChildText("startDate"))) {
				jt.setStartDate(sdf.parse(triggerElem.getChildText("startDate")));
			} else {
				jt.setStartDate(new Date());
			}
			if (triggerElem.getChildText("endDate")!=null && !"".equals(triggerElem.getChildText("endDate"))) {
				jt.setEndDate(sdf.parse(triggerElem.getChildText("endDate")));
			} else {
				jt.setEndDate(PoConstants.getInfDate());
			}
		} catch (ParseException e) {
			logger.error(e,e);
			jt.setStartDate(new Date());
			jt.setEndDate(new Date());
		}

		try {
			jt.setIntervalMinutes(new Integer(triggerElem.getChildText("intervalMinutes")).intValue());
		} catch (Exception e) { 
			jt.setIntervalMinutes(0);}
		jt.setName(triggerElem.getChildText("name"));
		jt.setScheduleType(new Integer(triggerElem.getChildText("scheduleType")).intValue());
		return jt;
	}



	/**
	 * Pauses (deactivate) the given trigger 
	 * @param t
	 */
	public void deactivateTrigger(PoJobTrigger t) {
		t.setActive(false);
		this.saveOrUpdateJobTrigger(t);
	}

	public void activateTrigger(PoJobTrigger t) {
		t.setActive(true);
		this.saveOrUpdateJobTrigger(t);
	}

	public void deactivateJob(PoJob job) {
		job.setActive(false);
		this.saveOrUpdateJob(job);
	}

	public void activateJob(PoJob job) {
		job.setActive(true);
		this.saveOrUpdateJob(job);
	}
	
	/** needed for test cases */
	@Override
	public void init() {
		
		if (noOfSchedulerThreads==0 || active == false) {
			logger.info("Scheduler is turned off by setting noOfSchedulerThreads to 0 or setting active to false!");
			logger.info("Exiting Scheduler initialization....");
			return;
		}
		
		logger.info("Scheduler initializes ... ");
		
		if (runningInCluster())
			logger.info("on Clusternode: " + this.poOptions.getClusterNode());
		
		// set No of Threads from PoSchedulerBean
		Resource quartzPropertiesFile = new ClassPathResource("org/quartz/quartz.properties");
		Properties myProps = new Properties();
		try {
			myProps.load(quartzPropertiesFile.getInputStream());
			myProps.setProperty("org.quartz.threadPool.threadCount",new Integer(this.noOfSchedulerThreads).toString());
			SchedulerFactory  sf = new StdSchedulerFactory(myProps);

			if (quartzScheduler!=null)  // shutdown scheduler if it is already running!
				quartzScheduler.shutdown();

			quartzScheduler = sf.getScheduler();
			quartzScheduler.start();
			if (runningInCluster()) {
				logger.info("Scheduler was started sucessfully on clusternode: " + this.poOptions.getClusterNode() + ".");
			} else {
				logger.info("Scheduler was started sucessfully.");
			}
			
		} catch (Exception e) {
			logger.error(e,e);
			throw new PoRuntimeException(e);
		}
	}

	@Override
	public void scheduleAll()
	{
		init();
		for (PoJob job : findAllActiveJobs()) {
			try {
				for (PoJobTrigger trigger :  job.getJobTriggers()) {
					scheduleQuartzJob(job,trigger,false);
				}
			} catch (Exception e) {
				logger.error(e,e);
			}
		}
	}

	@Override
	public String runJobOnce(PoJob job) {
		
		
		PoJobTrigger jt = new PoJobTrigger();
		jt.setScheduleType(-1);
		try {
			scheduleQuartzJob(job, jt,true);
		} catch (Exception e) {
			throw new PoRuntimeException("Could not schedule job", e);
		}
		
		// wait for 20 seconds 
		final int MAX_TRIES = 20;

		// to retrieve the log uid of the job started
		String uid = null;
		int i = 0;

		try {
			uid = this.jobLogUidMap.get(jt.getName());

			while ( uid == null && i < MAX_TRIES )
			{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					logger.warn(e1);
				}
				uid = this.jobLogUidMap.get(jt.getName());
				i++;
			}
		} catch (Exception e) {
			logger.warn("could not receive uid from jobDatamap",e);
		}

		if (uid!=null) {
			this.jobLogUidMap.remove(jt.getName());
		}
		return uid;

	}

	@Override
	public boolean isCronExpressionValid(String cronExpression) {
		return CronExpression.isValidExpression(cronExpression);
	}

	/**
	 * Assigns the given job to the given JobTrigger and schedule the datamap 
	 * with the scheduler. The job starts only if the job is active or immediately is set.
	 * @param job
	 * @param jt
	 * @param immediately
	 */
	@SuppressWarnings("unchecked")
	private void scheduleQuartzJob(PoJob job, PoJobTrigger jt, boolean immediately) {
		
		if (quartzScheduler == null) {
			logger.info("Quartz Scheduler was not started, exiting...");
			return;
		}
		
		if (runningInCluster())
			logger.debug("current ClusterNode = " + this.poOptions.getClusterNode());
		
		boolean run = true;
		if (runningInCluster() &&!immediately) {
			if (jt.getRunInCluster()==null) { 
				run = false;
			} else {
				if (!jt.getRunInCluster().equals(poOptions.getClusterNode())) {
					run = false;
				}
			}
		}
		if (!run &&!immediately) {
			if (logger.isDebugEnabled())
				logger.debug("Job " + job.getName() + " was not started because the name of the cluster node [" + 
						poOptions.getClusterNode() + "] is not the same as the name the cluster node defined " +
				"in the assigned job trigger");
		}


		if (job!=null && jt!=null && run)
			if (job.isActive() || immediately) {
				Trigger t=null;
				int RUNONCE_SCHEDTYPE = -1;
				if (jt.getScheduleType()!= RUNONCE_SCHEDTYPE) {

					t = getQuartzTrigger(jt);
					t.setName(jt.getUID());
					t.setGroup(job.getName());
				} else {
					t = TriggerUtils.makeImmediateTrigger(job.getName() + " immediateTrigger at " + new Date(), 0, 0);           
					t.setName(PoConstants.IMMEDIATE_TRIGGER);
					t.setGroup("ImmediateTrigger_" + new Date().getTime());
					jt.setName("ImmediateTrigger_" + new Date().getTime());
				}
				try {
					Class<?> quartzJobClass = null;
					Class<? extends WebdeskJob> wdJobClass = null;
					if ( StringUtils.isBlank(job.getJobClass())==false ) {
						
						wdJobClass = (Class<? extends WebdeskJob>) Class.forName(job.getJobClass()); 
						quartzJobClass = getMatchingQuartzJobClassOrReturnOriginal(wdJobClass);

					} else { 
						if ( job.getParent()!=null && StringUtils.isBlank( job.getParent().getJobClass())==false ) {
							
							wdJobClass = (Class<? extends WebdeskJob>) Class.forName(job.getParent().getJobClass());  
							quartzJobClass = getMatchingQuartzJobClassOrReturnOriginal(wdJobClass);
							
						} else {
							throw new IllegalStateException("Neither PoJob or its parent have a Class defined: " + job);
						}
					}

					JobDetail jd = null;
					if (jt.getScheduleType()!=RUNONCE_SCHEDTYPE)
						jd = new JobDetail(jt.getUID(), job.getName(), quartzJobClass); // name, group, class
					else
						jd = new JobDetail(PoConstants.IMMEDIATE_TRIGGER, job.getName(), quartzJobClass); // name, group, class


					if (wdJobClass!=null) {
						JobDataHandler.setWebdeskJobClass(jd, wdJobClass);	
					}

					JobDataHandler.setApplicationContext(jd, applicationContext);
					JobDataHandler.setMetaData(jd, job);
					
					if (runningInCluster()) {
						JobDataHandler.setClusterNode(jd, poOptions.getClusterNode());
					}

					if (quartzScheduler.getJobDetail(jd.getName(), t.getGroup())==null) { // jobName, jobGroup
						quartzScheduler.scheduleJob(jd,t); // jobDetail, trigger
						logger.info("Scheduler: scheduled job " + job.getName());
					} else
						logger.info("Job with Name " + jd.getName() + " and Group " + t.getGroup() + " already scheduled" );



				} catch (ClassNotFoundException cne) {
					logger.warn("Job " + job.getName() + " could not be executed because " + job.getJobClass() + " was not found -> delete job!");
					this.deleteJob(job);

				} catch (Exception e) {
					logger.warn("Job " + job.getName() + " could not be executed because " + job.getJobClass() + " was not found.",e);
					throw new PoRuntimeException(e);
				}
			}
	}
	
	private Class<?> getMatchingQuartzJobClassOrReturnOriginal(Class<?> webdeskJobClass) {
		if (ClassUtils.isAssignable(WebdeskInterruptableJob.class, webdeskJobClass)) {
			return GENERIC_QUARTZINTERRUPTABLEJOB_CLASS;
		} else if (ClassUtils.isAssignable(WebdeskJob.class, webdeskJobClass)) {
			return GENERIC_QUARTZJOB_CLASS;
		} else {
			return webdeskJobClass;
		}
	}

	private boolean runningInCluster() {
		return poOptions.runningInWorkingCluster();
	}


	/**
	 * Unschedules the given job (and all of its triggers)
	 * @param poJob a PoJob object.
	 */
	private void unscheduleQuartzJob(PoJob poJob) {

		if (quartzScheduler==null)
			return;

		if (poJob.getJobTriggers()!=null && poJob.getJobTriggers().size()>0) {

			logger.info("try to unschedule " + poJob.getName());
			String name = "";
			try {
				name = "not set";
				String[] quartzJobNames = quartzScheduler.getJobNames(poJob.getName()); // groupName
				for (String quartzJobName : quartzJobNames) {
					name = quartzJobName;
					if (!quartzScheduler.deleteJob(quartzJobName, poJob.getName()))
						logger.warn("Could not delete job " + name + " " + poJob.getName());
				}
			} catch (Exception e) {
				logger.warn("Could not delete job " + name + " " + poJob.getName(),e);
			}
		}
	}


	public void scheduleJob(PoJob job) {
		// first of all unschedule all jobs
		if (job.isActive()) {
			if (job.getJobTriggers()!=null && job.getJobTriggers().size()>0) {

				String nodeLogText = "";
				String clusterNode = poOptions.getClusterNode(); 
				if (clusterNode != null && !"".equals(clusterNode))
					nodeLogText = " on cluster node: " + clusterNode;
				logger.info("try to schedule job " + job.getName() + nodeLogText);
				
				Iterator<PoJobTrigger> i = job.getJobTriggers().iterator();
				while (i.hasNext()) {
					PoJobTrigger jt = i.next();
					scheduleQuartzJob(job , jt,false);
				}
			}
		}
	}



	/**
	 * Returns a Quartz Trigger which then can be used
	 * to schedule a job with the quartz scheduler.
	 * 
	 * 
	 * @param jt a PoJobTrigger object that defines 
	 * how the returned trigger is configurated.
	 * @return a quartz trigger object
	 */
	private Trigger getQuartzTrigger(PoJobTrigger jt) {
		Trigger t = null;
		Calendar gc = new GregorianCalendar();
		if (jt.getStartDate()==null)
			jt.setStartDate(new Date());
		gc.setTime(jt.getStartDate());
		int dom = gc.get(Calendar.DAY_OF_MONTH); // trigger will not fire if the day of month doesn't exist
		int dow = gc.get(Calendar.DAY_OF_WEEK);
		int hour = gc.get(Calendar.HOUR_OF_DAY);
		int minute = gc.get(Calendar.MINUTE);    

		switch (jt.getScheduleType()) {
		case PoConstants.HOURLY_TRIGGER:
			t = TriggerUtils.makeHourlyTrigger(1);
			t.setStartTime(new Date());
			t.setEndTime(PoConstants.getInfDate());

			break;
		case PoConstants.DAILY_TRIGGER:
			t = TriggerUtils.makeDailyTrigger(hour,minute);
			break;
		case PoConstants.WEEKLY_TRIGGER:
			t = TriggerUtils.makeWeeklyTrigger(dow,hour,minute);
			break;
		case PoConstants.MONTHLY_TRIGGER:
			t = TriggerUtils.makeMonthlyTrigger(dom,hour,minute);
			break;
		case PoConstants.SIMPLE_TRIGGER:
			if (jt.getEndDate()==null)
				jt.setEndDate(PoConstants.getInfDate());
			if (jt.getIntervalMinutes()==0)
				jt.setIntervalMinutes(1);
			t = new SimpleTrigger(jt.getJob().getName(),jt.getJob().getModule().getName(), jt.getStartDate(), jt.getEndDate(),  jt.getRepeatCount(),1000L*60L* jt.getIntervalMinutes());
			break;
		case PoConstants.CRON_TRIGGER:
			try {
				t = new CronTrigger(jt.getJob().getName(), jt.getJob().getModule().getName(), jt.getCronExpression());
			} catch (ParseException e) {
				logger.warn(e,e);
				throw new PoRuntimeException("Cron Expression is not valid");
			}
			break;
		case PoConstants.MINUTELY_TRIGGER:
			t = TriggerUtils.makeMinutelyTrigger();
			break;
		default:
			throw new PoRuntimeException("Schedule Type is not defined. Returning Null!");
		}
		return t;
	}

	public Scheduler getScheduler()
	{
		return this.quartzScheduler;
	}

	public void shutdown() {
		try {
			quartzScheduler.shutdown();
		} catch (SchedulerException e) {
			logger.error(e);
			throw new PoRuntimeException(e);
		}
	}


	@Override
	public void setNoOfSchedulerThreads(int noOfSchedulerThreads) {
		this.noOfSchedulerThreads = noOfSchedulerThreads;
	}


	@Override
	@SuppressWarnings("unchecked")
	// ggruber 27.3.2012
	// this is dead code, as we are not using job-configs delivered inside the code
	public PoJob getJobFromConfigFile(InputStream inputStream) {
		try {
			SAXBuilder builder = new SAXBuilder();
			builder.setValidation(false);
			builder.setIgnoringElementContentWhitespace(true);
			org.jdom.Document doc;
			doc = builder.build(inputStream);
			XPath xpath = XPath.newInstance("//job-config/settings");
			Iterator<Element> results = xpath.selectNodes(doc).iterator();
			Element myElem = null;
			PoJob newJob = new PoJob();
			if (results.hasNext() && (myElem = results.next()) !=null) {
				String name= myElem.getChildText("job");
				PoJob parentJob = jobDAO.findJobByNameAndType(name,PoConstants.JOB);
				newJob.setName(myElem.getChildText("name"));
				newJob.setParent(parentJob);
				newJob.setType(new Integer(PoConstants.JOB_CONFIG));
				if (parentJob==null) 
					logger.warn("Config has no corresponding registered Job " + name);
			}
			return newJob;
		} catch (Exception e) {
			logger.error(e,e);
			return null;
		}
	}
	public void setModuleService(PoModuleService moduleService) {
		this.moduleService = moduleService;
	}


	@Override
	public List<PoJobStateInfo> getCurrentStatus() {
		List<PoJobStateInfo> res = new ArrayList<PoJobStateInfo>();
		Map<String, Object> currentlyExecuting = new HashMap<String, Object>();
		try {
			// extract executing jobs
			for (Object jecObj : quartzScheduler.getCurrentlyExecutingJobs()) {
				JobExecutionContext jec = (JobExecutionContext) jecObj;
				// put the name of the executing job as key and the 
				// group of the executing job as value

				currentlyExecuting.put(jec.getJobDetail().getName(), jec.getJobDetail().getGroup());
			}

			for (PoJob job : this.jobDAO.loadAllJobs(false)) {
				String jobName = job.getName();
				String[] jobTriggerUIDs = quartzScheduler.getJobNames(jobName); // groupName

				for (String jobTriggerUID : jobTriggerUIDs) {
					if (!jobTriggerUID.equals(PoConstants.IMMEDIATE_TRIGGER)) {
						PoJobStateInfo desc = new PoJobStateInfo();
						desc.setJobName(jobName);
						desc.setTriggerUID(jobTriggerUID);
						PoJobTrigger poTrigger = jobTriggerDAO.get(jobTriggerUID);
						desc.setTriggerName(poTrigger.getName());

						if (currentlyExecuting.containsKey(jobTriggerUID) &&
								currentlyExecuting.get(jobTriggerUID).equals(jobName))
							desc.setExecuting(true);
						else
							desc.setExecuting(false);

						if (quartzScheduler.getJobDetail(jobTriggerUID, jobName)==null) // jobName, jobGroup 
							desc.setActive(false);
						else {
							desc.setActive(true);
						}

						Trigger[] triggers = quartzScheduler.getTriggersOfJob(jobTriggerUID, jobName); // jobName, jobGroup
						Date nextRun=null;
						for (int i=0;i<triggers.length; i++) {
							if (nextRun==null || triggers[i].getNextFireTime().before(nextRun))
								nextRun = triggers[i].getNextFireTime(); 
						}
						desc.setNextFireTime(nextRun);
						res.add(desc);
					}
				}
			}
			
			for (Object jecObj : quartzScheduler.getCurrentlyExecutingJobs()) {
				JobExecutionContext jec = (JobExecutionContext) jecObj;

				PoJobStateInfo desc = new PoJobStateInfo();
				desc.setJobName(jec.getJobDetail().getGroup());
				desc.setTriggerUID(jec.getJobDetail().getName());
				desc.setExecuting(true);
				desc.setNextFireTime(jec.getTrigger().getNextFireTime());
				PoJob job = this.findPoJobByName(jec.getJobDetail().getGroup());
				if (job!=null && job.isActive()) 
					desc.setActive(true);
				else
					desc.setActive(false);
				
				if (!res.contains(desc))
					res.add(desc);
			}
			Collections.sort(res);
			
		} catch (SchedulerException e) {
			logger.error(e,e);
		}
		return res;
	}
	

	@Override
	public boolean interrupt(String quartzJobName, String quartzJobGroup) {
		
		boolean isInterruptable = false;
		try {
			JobDetail jobDetail = quartzScheduler.getJobDetail(quartzJobName, quartzJobGroup); // jobName, jobGroup
			if (jobDetail == null) {
				logger.warn("JobDetail for " + quartzJobName + ", " + quartzJobGroup + " not found; interrupt not possible");
			} else {
				Class<?>[] interfaces =  jobDetail.getJobClass().getInterfaces();
				for (Class<?> iface : interfaces) {
					if (iface.equals(InterruptableJob.class)) {
						isInterruptable = true;
						break;
					}
				}
			}
		} catch (Exception e) {
			logger.warn("Problems inspecting job for Interruptability..., interrupt not possible", e);
		}
		
		if (isInterruptable) {
			try {
				boolean result = quartzScheduler.interrupt(quartzJobName, quartzJobGroup); // jobName, groupName
				
				if (result)
					logger.info("Job with name '" + quartzJobName + "' and group '" + quartzJobGroup + "' interrupted!");
				else
					logger.info("Tried to interrupt job with name '" + quartzJobName + "' and group '" + quartzJobGroup + "'!");
				
				return result;
			} catch (Exception e) {
				logger.info("Could not interrupt Job with name '" + quartzJobName + "' and group '" + quartzJobGroup + "'!");
			}
		}
		return false;
	}


	private String format(int s) {
		if (s<10)
			return "0"+s;
		else
			return new Integer(s).toString();
	}


	@Override
	public String getTriggerDetails(PoJobTrigger trigger) {
		String res ="";
		IfDate sd = new IfDate(trigger.getStartDate());
		switch (trigger.getScheduleType()) {
			case PoConstants.HOURLY_TRIGGER: res += "Trigger fires hourly";
				break;
			case PoConstants.DAILY_TRIGGER: 
				res+="Trigger fires daily at " + format(sd.get(Calendar.HOUR_OF_DAY)) +":"+format(sd.get(Calendar.MINUTE));
				break;
			case PoConstants.WEEKLY_TRIGGER:
				res+="Trigger fires weekly at every " +sd.get(Calendar.DAY_OF_WEEK) + ". at "+ format(sd.get(Calendar.HOUR_OF_DAY)) +":"+format(sd.get(Calendar.MINUTE));
				break;
			case PoConstants.MONTHLY_TRIGGER:
				res+="Trigger fires monthly every " +sd.get(Calendar.DAY_OF_MONTH) +". at "+ format(sd.get(Calendar.HOUR_OF_DAY)) +":"+format(sd.get(Calendar.MINUTE));
				break;
			case PoConstants.CRON_TRIGGER:
				res+="Trigger is a cron Trigger: '" + trigger.getCronExpression()+"'";
				break;
			case PoConstants.MINUTELY_TRIGGER:
				res+="Trigger fires every minute";
				break;
			default:
				return "Schedule Type of trigger not known.";
		}
		
		if (runningInCluster() && trigger.getRunInCluster()!=null)
			res+=" on ClusterNode='" + trigger.getRunInCluster() +"'";
		return res;
	}

	public void setFileService(PoFileService fileService) {
		this.fileService = fileService;
	}

	@Override
	public int getNoOfSchedulerThreads() {
		return noOfSchedulerThreads;
	}

	public void setPoOptions(PoOptions poOptions) {
		this.poOptions = poOptions;
	}

	@Override
	public boolean isJobScheduled(PoJob job) {
		
		if (!job.isActive())
			return false;
		
		if (!runningInCluster()) {
			return findActiveTriggersOfJob(job).size()>0;
		} else {
			for(PoJobTrigger trigger : findActiveTriggersOfJob(job)) {
				if (trigger.getRunInCluster()!=null &&
						trigger.getRunInCluster().equals(poOptions.getClusterNode())) {
					return true;
				}
			}
			return false;
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean isJobRunning(PoJob job) {
		List<JobExecutionContext> executingJobContexts;
		try {
			executingJobContexts = quartzScheduler.getCurrentlyExecutingJobs();
		} catch (SchedulerException e) {
			throw new RuntimeException("Problems to query Quartz for currently running jobs...");
		}
		for (JobExecutionContext jobExecCtx : executingJobContexts) {
			PoJob jobOfDataMap = (PoJob) jobExecCtx.getJobDetail().getJobDataMap().get("job");
			if (jobOfDataMap!=null && job.equals(jobOfDataMap))
				return true;
		}
		return false;
		
	}

	/**  Spring Setter **/
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public boolean isJobEngineRunning() {
		return getScheduler()!=null;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
