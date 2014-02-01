package at.workflow.webdesk.po.update;

import org.apache.cocoon.configuration.Settings;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

/**
 * This is the base class for updatescripts written in java. this class gives some
 * basic infrastructure (access to applicationcontext) and access to session handling
 * if required.
 * 
 * @author ggruber
 * @author fritzberger 2012-12-03 added isMySql() method.
 */
public abstract class PoAbstractUpgradeScript {

	protected final Logger logger = Logger.getLogger(this.getClass());
	private ApplicationContext appCtx;

	/**
	 * implement your update procedure here!
	 */
	public abstract void execute();

	public void startScript() {

	}

	public boolean useTransaction() {
		return true;
	}

	public void setApplicationContext(ApplicationContext appCtx) {
		this.appCtx = appCtx;
	}

	/** @return the named bean from application context. */
	protected final Object getBean(String name)	{
		return appCtx.getBean(name);
	}
	
	/** @return true if the named bean is in application context. */
	protected final boolean containsBean(String name)	{
		return appCtx.containsBean(name);
	}
	
	/**
	 * Identifies the target database product from webdesk.properties settings (Hibernate SQL dialect).
	 * Currently 2012-11-30 Webdesk runs only on MySQL and Microsoft-SQL-Server.
	 * @return true if the webdesk.properties setting point to a MySQL database, else false.
	 */
	protected final boolean isMySql()	{
		final Settings webdeskProperties = (Settings) getBean(Settings.ROLE);
		final String databaseDialect = webdeskProperties.getProperty("hibernate.dialect");
		if (StringUtils.isEmpty(databaseDialect))
			throw new IllegalStateException("Unknown database: "+webdeskProperties);
		
		return databaseDialect.toUpperCase().contains("MYSQL");
	}

}
