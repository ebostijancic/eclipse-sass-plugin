package at.workflow.webdesk.po.jobs;

import org.w3c.dom.Document;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import at.workflow.webdesk.tools.WebdeskLoggerFactory;

/**
 * Default implementations for WebdeskJob.
 */
public abstract class AbstractWebdeskJob implements WebdeskJob  {
	
	protected final Logger logger = Logger.getLogger(getClass().getName(), new WebdeskLoggerFactory("jobs"));
	
	private ApplicationContext appCtx;
	protected Document configXml;
	
	/** Interface method. This is here just to enable the Override annotation in subclasses for Java 1.5. */
	@Override
	public abstract void run();

	/** This WebdeskJob default implementation always returns false. TODO document what happens when returning true, and what when false. */
	@Override
	public boolean useTransaction() {
		return false;
	}

	/** This WebdeskJob default implementation always returns true. */
	@Override
	public boolean bindHibernateSession() {
		return true;
	}
	
	/** This WebdeskJob default implementation does nothing. */
	@Override
	public void onBeforeRun() {
	}
	
	/** Receives the Spring application-context and stores it to field <code>appCtx</code>. */
	@Override
	public void setApplicationContext(ApplicationContext appCtx) {
		this.appCtx = appCtx;
	}
	
	/** @return the ApplicationContext. */
	protected final ApplicationContext getApplicationContext()	{
		return appCtx;
	}

	/**
	 * Receives the XML configuration and stores it to field <code>configXml</code>.
	 * Override this to immediately store configured contents to some member fields.
	 * The ApplicationContext has already been set into this Job.
	 */
	@Override
	public void setConfigXml(Document configXml) {
		this.configXml = configXml;
	}

	/** @return the Job Logger */
	@Override
	public Logger getJobLogger() {
		return logger;
	}
	
	/** @return the named bean from appCtx. */
	protected final Object getBean(String beanId)	{
		assert appCtx != null : "Spring application context was not set!";
		return appCtx.getBean(beanId);
	}
	
}
