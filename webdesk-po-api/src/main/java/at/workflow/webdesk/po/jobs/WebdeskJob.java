package at.workflow.webdesk.po.jobs;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContextAware;
import org.w3c.dom.Document;

/**
 * <p>
 * A Job is something that can be executed deferred, periodically or only once.
 * Like UNIX cron jobs, or WINDOWS task scheduler.
 * </p><p>
 * Order of initialization is:
 * <ul>
 * 	<li>setConfigXml()</li>
 * 	<li>setApplicationContext()</li>
 * 	<li>onBeforeRun()</li>
 * 	<li>useTransaction()</li>
 * 	<li><i>when useTransaction() returned true:</i> bindHibernateSession()</li>
 * 	<li>run()</li>
 * 	<li><i>when useTransaction() returned true:</i> bindHibernateSession()</li>
 * </ul>
 * </p>
 */
public interface WebdeskJob extends Runnable, ApplicationContextAware {

	/**
	 * Sets the XML configuration for this job.
	 * @param configXml the w3c DOM containing configuration.
	 */
	public void setConfigXml(Document configXml);

	/**
	 * Called before <code>run()</code> is called.
	 */
	public void onBeforeRun();
	
	/**
	 * When this implementation returns true, the job runs with its own transaction.
	 * @return true when all persistence statements of this job should be committed at once
	 * 	and not with the transaction logic of business methods (one call one transaction).
	 */
	public boolean useTransaction();
	
	/**
	 * Called only when <code>useTransaction()</code> returned <b>false</b>,
	 * then this job runs without transaction but with an newly created HibernateSession.
	 * When this method returns true, the Session's flush-mode is set to <code>NEVER</code>, and
	 * <code>TransactionSynchronizationManager.bindResource</code> is called, passing in the Session and its Factory.
	 * At job termination <code>TransactionSynchronizationManager.unbindResource</code> is called,
	 * and the Session is closed.
	 */
	public boolean bindHibernateSession();
	
	/**
	 * Executes the job. To be implemented specifically by any subclass.
	 */
	@Override
	public abstract void run();
	
	/**
	 * Use this method to get access to the Joblogger associated. Is used by the calling JobService to log additional information
	 * not produced by the job itself...
	 * 
	 * @return Log4j Logger connected to the particular Job.
	 */
	public Logger getJobLogger();
	
}
