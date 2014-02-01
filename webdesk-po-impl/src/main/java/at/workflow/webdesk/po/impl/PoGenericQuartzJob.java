package at.workflow.webdesk.po.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.MDC;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import at.workflow.tools.mail.Mail;
import at.workflow.tools.mail.MailService;
import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.PoJobService;
import at.workflow.webdesk.po.PoLogService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.impl.PoJobServiceImpl.JobDataHandler;
import at.workflow.webdesk.po.jobs.WebdeskJob;
import at.workflow.webdesk.po.model.PoJob;
import at.workflow.webdesk.po.model.PoLog;
import at.workflow.webdesk.po.model.PoLogDetail;
import at.workflow.webdesk.po.model.PoLogDetailThrowable;
import at.workflow.webdesk.po.model.PoOptions;
import at.workflow.webdesk.tools.date.DateTools;

/**
 * The generic Quartz job which is called for all invocations of of WebdeskJob
 * which implement the WebdeskJob interface. It shields the webdesk jobs from
 * implementation dependencies to Quartz. 
 * 
 * @author ggruber
 */
public class PoGenericQuartzJob implements Job {
	
	private PoJob job;
	private PoLogService logService;
	private PoLog jobLog = null;
	MailService mailService;
	private PoOptions options;
	
	private WebdeskJob wdJob;
	
	private static Map<String, Integer> logLevels=new HashMap<String, Integer>(); 
	
	static {
		logLevels.put("ALL", new Integer(0));
		logLevels.put("DEBUG", new Integer(1));
		logLevels.put("INFO", new Integer(2));
		logLevels.put("WARN", new Integer(3));
		logLevels.put("ERROR", new Integer(4));
		logLevels.put("FATAL", new Integer(5));
	}
	
	public WebdeskJob getRunningJob() {
		return wdJob;
	}
	
	private void createWebdeskJobInstance(Class<? extends WebdeskJob> wdJobClass) {
		try {
			wdJob = wdJobClass.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void execute(JobExecutionContext jec) throws JobExecutionException {
		
		Class<? extends WebdeskJob> wdJobClass = JobDataHandler.getWebdeskJobClass(jec);
		createWebdeskJobInstance(wdJobClass);
		
        job = JobDataHandler.getMetaData(jec);
        
        ApplicationContext appCtx = JobDataHandler.getApplicationContext(jec);
        
        logService = (PoLogService)appCtx.getBean("PoLogService");
        mailService = (MailService) appCtx.getBean("PoMailService");
        options = (PoOptions) appCtx.getBean("PoOptions");
        
            
        try {
        	String clusterNodeToRun = JobDataHandler.getClusterNode(jec);
        	
            // log starttime
            jobLog = new PoLog();
            jobLog.setBeginTime(new Date());
            jobLog.setReferenceUID(job.getUID());
            jobLog.setUserName("SYSTEM");
            jobLog.setActionName(job.getName()+".job");
            jobLog.setClusterNode(clusterNodeToRun);
            logService.saveLog(jobLog);   
            
            if (options.runningInWorkingCluster() && ! (options.getClusterNode().equals(clusterNodeToRun)) ) {
            	// this case should actually never happen, but problems have been reported
            	// by customers where jobs are running on the wrong clusternode
            	// so we throw an exception here...
            	
            	throw new PoRuntimeException("This Job was configured to run on " + clusterNodeToRun + ", but the current node [" + options.getClusterNode() +"] tries to run it by mistake..." );
            }

            PoJobService jobService = (PoJobService)appCtx.getBean("PoJobService");
            job = jobService.getJob(job.getUID());
            
            
            // set log uid for Log4J Reference
            MDC.put("uid", jobLog.getUID());
            String triggerId = jec.getTrigger().getGroup();
            jobService.informAboutLogUidOfJob(triggerId, jobLog.getUID());
            
            // before calling anything on the job, give it a context
            wdJob.setApplicationContext(appCtx);
            
            // get my Config Xml if job is a config
            // if job is no config -> configxml is set to null!
            wdJob.setConfigXml(jobService.getConfigXmlOfJob(this.job));
            
            // call custom handler
            wdJob.onBeforeRun();
            
            if (wdJob.useTransaction()) {
	            // create transaction around job
	            PlatformTransactionManager transactionManger = (PlatformTransactionManager) appCtx.getBean("TransactionManager");
	            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManger);
	            
	            final PoLog jl2 = jobLog;
	            final WebdeskJob execJob = wdJob;
	            
	            transactionTemplate.execute(new TransactionCallback() {
	                @Override
					public Object doInTransaction(TransactionStatus status) {
	                    try {
	                    	execJob.run();
	                        finish(jl2);
	                    } catch (Exception e) {
	                        //logger.error(e,e);
	                        throw new PoRuntimeException(e);
	                    }
	                    return null;
	                }
	            });
            }  else {
            	// run without transaction
            	// but with an open hibernate session using the session factory!

            	Session session=null;
            	SessionFactory sf = (SessionFactory) appCtx.getBean("sessionFactory");
            	
            	try {
            		// bindResource must be in try / finally so that any binding exception is followed by unbindResource.
            		// Otherwise the job can not be run again until the resource is somehow unbound.
	            	if (wdJob.bindHibernateSession()) {
		            	// get session and bind it to the local thread
		                session = SessionFactoryUtils.getSession(sf, true); 
		                session.setFlushMode(FlushMode.MANUAL);
		                TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
	            	}
                	wdJob.run();
                	finish(jobLog);
                } catch(Exception e) {
                	wdJob.getJobLogger().error(e,e);
                	throw e;
                } finally {
	            	// unbind and close it!
                	if (wdJob.bindHibernateSession()) {
		        	    TransactionSynchronizationManager.unbindResource(sf);
		        	    session.close();
                	}
                }
            }
            
            wdJob.getJobLogger().info("ended job with name " + job.getName() + " at " + new Date());
		} catch (Exception e) {
			wdJob.getJobLogger().error("terminated job with name " + job.getName() + " abnormally at " + new Date(), e);
		} finally {
            if (logService!=null && jobLog!=null) {
    		    // -> this causes problems, as the uid is not given at this time
            	// -> the LogService uses a different transaction manager jobLog = logDAO.getLog(jobLog.getUID());
            	jobLog = logService.getLog(jobLog.getUID());
    		    jobLog.setOk(true);
    		    jobLog.setEndTime(new Date());
    		    jobLog.setDuration( new java.lang.Double((jobLog.getEndTime().getTime() - jobLog.getBeginTime().getTime())/1000) );
    		    logService.saveLog(jobLog);
            }
        }
	}
	
	private void finish(PoLog jobLog) {
    	
    	// eventually we have to inform someone about what happened inside the job 
        if (job.isMailForwardingEnabled()) {
        	sendMailsIfNeccessary(job, jobLog);        	
        }
	}
	
	/**
	 * @param job
	 * 
	 * <p>
	 * Send mails to defined email-adresses in the job.
	 * 
	 */
	public static void sendMailsIfNeccessary(PoJob job, PoLog log) {
		
		boolean found = false;
		StringBuffer buffer = new StringBuffer();
		StringBuffer content = new StringBuffer();
		List<String> searchEntries = null;
		if (job.getSearchCriteria()!=null)
			searchEntries = Arrays.asList(job.getSearchCriteria().split(" "));
		buffer.append("<p><b>" + job.getName().toUpperCase() + "</b> Log Details</p>");
		
		if (job.getSearchCriteria()!=null) {
			buffer.append("<p>The search criteria " + job.getSearchCriteria() +
					" was fulfilled.</p>");
		} else 
			buffer.append("The Loglevel " + job.getLogLevel() + " occured during " +
					"the time of execution of " + job.getName() +".</p>");
		buffer.append("<p><ul>");
		buffer.append("<li>Job startet " + DateTools.toDateTimeFormat(log.getBeginTime())+"</li>");
		buffer.append("<li>actual time is " + DateTools.toDateTimeFormat(new Date()) + "</li>");
		buffer.append("<li>User " + log.getUserName()+"</li>");
		buffer.append("</ul></p>");
		
		content.append("<h2>Entries</h2>");
		
		content.append("<table border='1'>");
		
		// the log level is not considered as a search criteria exists
		PoLogService logService = (PoLogService) WebdeskApplicationContext.getBean("PoLogService");
		Iterator<PoLogDetail> details = logService.findLogDetails(log).iterator();
		while (details.hasNext()) {
			PoLogDetail detail = details.next();
			content.append("<tr><td>");
			content.append(detail.getLogLevel());
			content.append("</td><td>");
			content.append(detail.getMessage());
			content.append("</td></tr>");
			if (job.getSearchCriteria()!=null && !"".equals(job.getSearchCriteria().trim())) {
				// search by criteria -> ignore Loglevels!
				if (containsSearchCriteria(detail, searchEntries, buffer)) {
					found=true;
				}
			} else {
				// search by Loglevels
				if ( logLevels.get(detail.getLogLevel()) >= getJobLogLevel(job) ) {
					// an entry with the given log level was found
					found=true;
				}
			}
			// if a link is sufficient, we don't have to search further 
			if (found && job.isLinkToLogs())
				break;
		}
		content.append("</table>");
		if (found) {
			//logger.info("Going to send log detail information.");
			Mail mail = new Mail();
			mail.setSendTo(job.getEmailAdressesAsList());
			mail.setSubject(job.getName() +" Log Details");
			
			if (job.isLinkToLogs()) {
				content = new StringBuffer();
				content.append("<a href=\"");
				content.append(job.getBasicUrl()+"po_showLogDetails.act?uid=" + log.getUID());
				content.append("\">Link to Logs</a>");
				mail.setInnerHtmlMessage(buffer.toString() + content.toString());
			} else { 
				StringBuffer mailContent = new StringBuffer();
				mailContent.append(buffer.toString() + content.toString());
				mail.setInnerHtmlMessage(mailContent.toString());
			}
			MailService mailService = (MailService) WebdeskApplicationContext.getBean("PoMailService");
			mailService.sendMail(mail);
		} else {
			// logger.debug("Logs are not sent to " + job.getEmailAdressesAsList());
		}
	}
	
	private static Integer getJobLogLevel(PoJob job) {
		if (job.getLogLevel() == null)
			return logLevels.get("FATAL");
		
		return logLevels.get(job.getLogLevel().toUpperCase());
	}

	private static boolean containsSearchCriteria(PoLogDetail jd, List<String> searchEntries, StringBuffer buffer) {
		boolean found = false;
		if (contains(jd.getMessage(), searchEntries))
			found = true;
		
		Iterator<PoLogDetailThrowable> i = jd.getLogDetailThrowables().iterator();
		while (i.hasNext()) {
			PoLogDetailThrowable ldt = i.next();
			if (StringUtils.isBlank(ldt.getMessage())==false && contains(ldt.getMessage(),searchEntries))
				found = true;
			buffer.append("STACKTRACE");
			buffer.insert(15, ldt.getMessage());
		}
		return found;
	}

	private static boolean contains(String message, List<String> searchEntries) {
		boolean found = false;
		Iterator<String> sI = searchEntries.iterator();
		while (sI.hasNext()) {
			String entry = sI.next();
			if (message.toLowerCase().indexOf(entry.toLowerCase())>-1)
				found = true;
		}
		return found;
	}

}
