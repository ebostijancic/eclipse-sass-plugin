package at.workflow.webdesk.po.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.w3c.dom.Document;

import at.workflow.webdesk.po.PoConnectorInterface;
import at.workflow.webdesk.tools.WebdeskLoggerFactory;

/**
 * Created on 10.04.2006
 * @author hentner (Harald Entner)
 * @author fritzberger 23.02.2012 refactoring.
 */
public abstract class PoAbstractConnector implements PoConnectorInterface {

	protected final Logger logger = Logger.getLogger(getClass().getName(), new WebdeskLoggerFactory("connectors"));
	
	/**
	 * As HibernateDaoSupport is an abstract class, this is needed to instantiate it.
	 */
	private class HibernateDaoSupportWrapper extends HibernateDaoSupport
	{
	}

	private HibernateDaoSupport hibernateDaoWrapper;
	private Document configurationFile;
	private ApplicationContext applicationContext;
	private boolean fieldNamesInitialized;
	private List<String> fieldNames;
	private List<String> fieldNamesToExclude = new ArrayList<String>();

	
	protected PoAbstractConnector() {
		this.hibernateDaoWrapper = new HibernateDaoSupportWrapper();
	}


	/** Does nothing. Override when needed to allocate services. */
	@Override
	public void init() {
	}

	@Override
	public final List<String> getFieldNames()	{
		if (fieldNamesInitialized == false)	{
			fieldNamesInitialized = true;
			initializeFieldNames();
			
			// avoid NullPointerException
			if(fieldNames == null)
				fieldNames = new ArrayList<String>();
			
			Collections.sort(fieldNames);
		}
		return Collections.unmodifiableList(fieldNames);
	}
	
	/** Sub-classes MUST implement this by calling addFieldName(). */
	protected abstract void initializeFieldNames();


	/** Called by PoConnectorService. */
	@Override
	public void setConfigurationFile(Document config) {
		this.configurationFile = config;
	}

	/** Called by PoConnectorService. */
	@Override
	public void setApplicationContext(ApplicationContext appCtx) {
		this.applicationContext = appCtx;
		
		if (hibernateDaoWrapper.getSessionFactory() == null && applicationContext.containsBean("sessionFactory"))
			setSessionFactory((SessionFactory) getBean("sessionFactory"));
	}
	
	/**
	 * This is called by Spring when the "sessionFactory" property was
	 * defined in application-context of a connector derivate.
	 * Can also be used to set a SessionFactory from unit tests.
	 */
	public void setSessionFactory(SessionFactory sf) {
		hibernateDaoWrapper.setSessionFactory(sf);
	}

	
	/** Adds the given name to filedNames, without checking for uniqueness. Asserts that it is not null. */
	protected void addFieldName(String fieldName)	{
		assert fieldName != null;
		if (fieldNames == null)
			fieldNames = new ArrayList<String>();
		fieldNames.add(fieldName);
	}

//	/** @return true if current fieldNames contain the passed name, false if no fieldNames present or they do not contain it. */
//	protected boolean containsFieldName(String fieldName)	{
//		return (fieldNames != null) ? fieldNames.contains(fieldName) : false;
//	}
	
	/**
	 * Use this to exclude persistent properties from being enumerated.
	 * Bean references to exclude are given by their normal name, not "$"+referenceName!
	 */
	protected void excludeFieldNames(String [] lowerFirstCharPropertyNames)	{
		for (String s : lowerFirstCharPropertyNames)
			fieldNamesToExclude.add(s);
	}
	
	/** @return the currently excluded persistent properties. */
	protected String [] excludedFieldNames()	{
		return fieldNamesToExclude.toArray(new String[fieldNamesToExclude.size()]);
	}
	
	/**
	 * this is done to provide the hibernateTemplate for all connectors which need them
	 * however we don't want to override log settings from HibernateDaoSupport!
	 */
	protected HibernateTemplate getHibernateTemplate() {
		return hibernateDaoWrapper.getHibernateTemplate();
	}

	/** @return the configuration file of this connector. */
	protected Document getConfigurationFile() {
		return configurationFile;
	}

	/** @return the named bean from Spring application context. */
	protected Object getBean(String beanName)	{
		return applicationContext.getBean(beanName);
	}

}
