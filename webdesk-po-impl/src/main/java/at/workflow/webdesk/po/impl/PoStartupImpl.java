package at.workflow.webdesk.po.impl;

import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.distribution.RMICacheManagerPeerListener;

import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.spring.configurator.impl.RunningModeHelper;
import org.apache.log4j.Logger;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import at.workflow.tools.DatabaseConstants;
import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.PoActionService;
import at.workflow.webdesk.po.PoBeanPropertyService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoDataSourceService;
import at.workflow.webdesk.po.PoJobService;
import at.workflow.webdesk.po.PoLanguageService;
import at.workflow.webdesk.po.PoModuleUpdateService;
import at.workflow.webdesk.po.PoRegistrationService;
import at.workflow.webdesk.po.PoSearchService;
import at.workflow.webdesk.po.PoRegistrationService.RunRegistrationValue;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.UpgradeFailedException;
import at.workflow.webdesk.po.impl.util.MySqlCharsetChecker;
import at.workflow.webdesk.po.licence.LicenceReader;
import at.workflow.webdesk.po.licence.LicenceViolationException;
import at.workflow.webdesk.po.model.LicenceDefinition;
import at.workflow.webdesk.tools.api.ClusterConfig;
import at.workflow.webdesk.tools.cache.CacheCommunicationListener;
import at.workflow.webdesk.tools.cache.CustomEhCacheFactoryBean;
import at.workflow.webdesk.tools.config.PropertiesUtils;
import at.workflow.webdesk.tools.config.StartupPropertyProvider;
import at.workflow.webdesk.tools.config.WebdeskStartup;

/**
 * This the main Startup Class of the Po Module. It implements some custom startup code
 * of the Portal & Organisation Module, which can not be done inside the init methods of
 * the services themselves.
 * 
 * @author ggruber
 */
public class PoStartupImpl implements WebdeskStartup, ApplicationContextAware {

	private static final Logger logger = Logger.getLogger(PoStartupImpl.class);
	
	private boolean licenceCheckEnabled = true;
	private ApplicationContext appCtx;
	private PoModuleUpdateService moduleUpdateService;
	private PoBeanPropertyService beanPropertyService;
	private PoBeanPropertyCollector beanPropertyCollector;
	private PoRegistrationService registrationService;
	private PoLanguageService languageService;
	private PoActionService actionService;
	private CacheManager cacheManager;
	private Settings settings;
	private PoDataSourceService dataSourceService;
	private MySqlCharsetChecker mySqlCharsetChecker;
	private PoSearchService searchIndexService; // TODO call the first indexing.

	/** Implements WebdeskStartup. */
	@Override
	public void start() {
		
		// redirecting System.err logging to root-logger with ERROR level (such will be visible in log files)
		// See http://intranet/intern/ifwd_mgm.nsf/0/FA4DD29DBCCF28A0C12579EB0056B67E?OpenDocument notes://asterix/intern/ifwd_mgm.nsf/0/FA4DD29DBCCF28A0C12579EB0056B67E?EditDocument
		//Logger rootLogger = Logger.getRootLogger();
		//System.setErr( new PrintStream( new LoggingOutputStream( rootLogger, Level.ERROR), true) );
		// No need to do that:
		// neither Jetty nor Tomcat ignore System.err, it is just the FlowScript layer that
		// swallows Error (NoSuchMethodError), and we hope to get rid of that layer soon.
		
		// Set parameters for Xalan...
		// disable DTD checking
		SAXBuilder myBuilder = new SAXBuilder(false);
		// do not load dtds !!!
		// only valid for xalan (not for crimson)
		myBuilder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		myBuilder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		// Initialize the caches
		for (Object o : appCtx.getBeansOfType(CustomEhCacheFactoryBean.class).entrySet()) {
			@SuppressWarnings("rawtypes")
			Map.Entry e = (Map.Entry) o;
			@SuppressWarnings("unused")
			String key = (String) e.getKey();
			CustomEhCacheFactoryBean factory = (CustomEhCacheFactoryBean) e.getValue();
			factory.setCacheManager(cacheManager);
		}

		// set system property for cachedir
		// FIXME: this is *NOT* save if several webdesk instances run within the same tomcat!!!
		System.setProperty("webdesk.cachedir", settings.getCacheDirectory());
		
		// Sanity checks of persistence
		if ( isMySQL() ) {
			mySqlCharsetChecker.checkCharsetSanity();
		}
		

		// set ApplicationContext for 3rd party components which
		// do not have access to the Spring applicationContext
		WebdeskApplicationContext.setApplicationContext(appCtx);

		// init languageService
		languageService.init();

		// a little security hole but nice for developing...
		// property: licenceCheckDisabled=true
		licenceCheckEnabled = ! Boolean.valueOf(settings.getProperty(StartupPropertyProvider.WEBDESK_LICENCE_CHECK_DISABLED, "false")).booleanValue();

		// =========== load licence file
		LicenceReader lr = null;
		PoLicenceInterceptor licenceInterceptor = null;
		try {
			if (licenceCheckEnabled) {
				lr = (LicenceReader) appCtx.getBean("LicenceReader");
				// ============ Read Licence Definition Files and inject into
				List<LicenceDefinition> licDefs = lr.getLicenceDefinitions(); // parses the xml structure of the
																				// previously loaded files
				licenceInterceptor = (PoLicenceInterceptor) appCtx.getBean("PoLicenceInterceptor");
				licenceInterceptor.setLicenceDefinitions(licDefs);
				licenceInterceptor.initAllowedActions();
			}
		}
		catch (Exception e) {
			logger.error("Licence problem: "+e.getMessage(), e);
			throw new LicenceViolationException(e.getMessage());
		}

		// =========== Start Update Process: upgrade/downgrade if necessary
		try	{
			moduleUpdateService.installModules();
		}
		catch (UpgradeFailedException e)	{
			final String ignoreFailedUpgrade = settings.getProperty("ignoreFailedUpgrade");
			if (ignoreFailedUpgrade == null || ignoreFailedUpgrade.toLowerCase().equals("true") == false)
				throw new PoRuntimeException(e);
		}

		try {
			// inject live instances of Spring beans with data from database
			beanPropertyService.registerBeanProperties(beanPropertyCollector.getCollectedBeanProperties());
			
			// if setting the System or webdesk property 'webdesk.resetBeanPropertiesToDefault' to true
			// we can completly reset the existing beanproperties in the database.
			boolean ignoreDatabaseValues = "true".equals(settings.getProperty("webdesk.resetBeanPropertiesToDefault", "false"));
			
			beanPropertyService.writeBeanPropertiesToDb( ignoreDatabaseValues );
			beanPropertyService.injectAll();
		}
		catch (Exception e) {
			logger.error("Problems occured while injecting values into 'live' Spring bean instances ...", e);
		}

		
		// Possibility to turn OFF running the registration in parallel by setting as webdesk/system property
		// webdesk.runRegistrationInThread=false
		boolean runRegistrationInThread = "true".equals(settings.getProperty("webdesk.runRegistrationInThread", "true"));
		/*
		 * Maybe we should run the registration
		 * 0 = never run registration
		 * 1 = run once
		 * 2 = run always
		 * 3 = run always but without textmodules sync
		 */
		RunRegistrationValue runRegistrationCommand;
		
		if (languageService.findCommonTextModules(languageService.findDefaultLanguage()).size() == 0)
			// if no common textmodules exist -> register anyway!
			runRegistrationCommand = RunRegistrationValue.ONCE_THEN_RESET;
		else
			// try to load the property, using ONCE_THEN_RESET as default
			runRegistrationCommand = RunRegistrationValue.byCode(settings.getProperty(PoRegistrationService.WEBDESK_RUN_REGISTRATION_PROPERTY, RunRegistrationValue.ONCE_THEN_RESET.toString()));

		assert runRegistrationCommand != null : "runRegistrationCommand is assumed to have a default here!";
		
		if ( ! runRegistrationCommand.equals(RunRegistrationValue.NEVER) ) {
			try { // start Registration and Jobs in a Thread
				class RunRegistrationThread extends Thread
				{
					private final Logger logger = Logger.getLogger(getClass().getName());
					private RunRegistrationValue runRegistration = RunRegistrationValue.NEVER;

					public RunRegistrationThread(RunRegistrationValue runRegistration) {
						this.runRegistration = runRegistration;
					}

					@Override
					public void run() {
						if (logger.isDebugEnabled())
							logger.debug("call refreshCache to load ProcessDefinitions");

						if (runRegistration.equals(RunRegistrationValue.ALWAYS_EXCLUDE_TEXTMODULES))	{
							registrationService.runRegistrationWithOutTextModules(moduleUpdateService.getModules());
						}
						else if (runRegistration.equals(RunRegistrationValue.ONCE_THEN_RESET)) {
							// IGNORE ini file cache!!!!
							registrationService.runRegistration(moduleUpdateService.getModules(), true);
							// reset registration flag to never
							PropertiesUtils.saveWebdeskProperty(PoRegistrationService.WEBDESK_RUN_REGISTRATION_PROPERTY, RunRegistrationValue.NEVER.toString());
						}
						else {
							registrationService.runRegistration(moduleUpdateService.getModules(), false);
						}
					}
				}

				Thread t = new RunRegistrationThread(runRegistrationCommand);
				
				if (runRegistrationInThread)
					t.start();
				else
					t.run();
			}
			catch (Exception e) {	// thread would end here as well
				logger.error(e.getMessage(), e);
			}
		}

		// init licence interceptor now, because otherwise newly registered actions can not be used !
		if (licenceCheckEnabled)
			licenceInterceptor.initAllowedActions();

		// startup distributed caches
		try {
			if (settings.getProperty("isDistributed") != null && settings.getProperty("isDistributed").equals("true")) {
				CacheManager cm = (CacheManager) appCtx.getBean("CacheManager");
				RMICacheManagerPeerListener cl = (RMICacheManagerPeerListener) cm.getCachePeerListener("RMI");
				cl.init();

				Cache communicationCache = cm.getCache(PoConstants.COMMUNICATIONCACHE);
				CacheCommunicationListener ccl = (CacheCommunicationListener) appCtx.getBean("CacheCommunicationListener");
				communicationCache.getCacheEventNotificationService().registerListener(ccl);
			}
		}
		catch (Exception e) {
			logger.error("Failed to start Distribution System", e);
		}

		// =============== start Job Service
		System.out.println("starting Job Service");
		try {
			PoJobService jobService = (PoJobService) appCtx.getBean("PoJobService");
			jobService.scheduleAll();
		}
		catch (Exception e) {
			logger.error("Failed to schedule jobs!", e);
		}

		ClusterConfig config = (ClusterConfig) appCtx.getBean("PoOptions");

		if (config.isDistributed()) {
			System.out.println("Webdesk started on ClusterNode=" + config.getClusterNode());
		}
		else {
			System.out.println("WebdeskStartup PoStartupImpl.start() finished ...");
		}
		
		// =================== preload Image set
		actionService.loadAllImageSets();
		
		// TODO: init Lucene search index when not already done
		// write a class SearchIndexService as Service
		// add it as bean in Spring applicationContext
		// inject a SessionFactory property into it
		// add a method reCreateSearchIndex()
		// use SessionFactory to create a Session and then do:
		searchIndexService.refreshSearchIndex();
//		SessionFactory sessionFactory;
//		Session session = sessionFactory.openSession();
//		FullTextSession fullTextSession = Search.getFullTextSession(session);
//		fullTextSession.createIndexer().startAndWait();
		
	}

	private boolean isMySQL() {
		return dataSourceService.getDatabaseVendor( dataSourceService.getDataSource( PoDataSourceService.WEBDESK)).equals( DatabaseConstants.MYSQL);
	}

	/** Implements ApplicationContextAware. */
	@Override
	public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
		this.appCtx = appCtx;
	}

	/** Spring setter. */
	public void setModuleUpdateService(PoModuleUpdateService moduleUpdateService) {
		this.moduleUpdateService = moduleUpdateService;
	}

	/** Spring setter. */
	public void setBeanPropertyService(PoBeanPropertyService beanPropertyService) {
		this.beanPropertyService = beanPropertyService;
	}

	/** Spring setter. */
	public void setRegistrationService(PoRegistrationService registrationService) {
		this.registrationService = registrationService;
	}

	/** Spring setter. */
	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	/** Spring setter. */
	public void setBeanPropertyCollector(PoBeanPropertyCollector beanPropertyCollector) {
		this.beanPropertyCollector = beanPropertyCollector;
	}

	/** Spring setter. */
	public void setLanguageService(PoLanguageService languageService) {
		this.languageService = languageService;
	}

	/** Spring setter. */
	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	/** Spring setter. */
	public void setActionService(PoActionService actionService) {
		this.actionService = actionService;
	}

	/** Spring setter. */
	public void setDataSourceService(PoDataSourceService dataSourceService) {
		this.dataSourceService = dataSourceService;
	}

	/** Spring setter. */
	public void setMySqlCharsetChecker(MySqlCharsetChecker mySqlCharSetChecker) {
		this.mySqlCharsetChecker = mySqlCharSetChecker;
	}

	/** Spring setter. */
	public void setSearchIndexService(PoSearchService searchIndexService) {
		this.searchIndexService = searchIndexService;
	}

}
