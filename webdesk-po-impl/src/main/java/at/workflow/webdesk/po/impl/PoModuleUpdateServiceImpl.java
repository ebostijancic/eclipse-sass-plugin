package at.workflow.webdesk.po.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import at.workflow.tools.DatabaseConstants;
import at.workflow.tools.XMLTools;
import at.workflow.webdesk.po.PoModuleService;
import at.workflow.webdesk.po.PoModuleUpdateService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.ProcessReferenceDetachHandler;
import at.workflow.webdesk.po.UpgradeFailedException;
import at.workflow.webdesk.po.daos.PoQueryUtils;
import at.workflow.webdesk.po.impl.update.RenameUpgradeScriptFactory;
import at.workflow.webdesk.po.model.PoModule;
import at.workflow.webdesk.po.model.PoRegistrationBean;
import at.workflow.webdesk.po.update.PoAbstractUpgradeScript;
import at.workflow.webdesk.po.update.RenameUpgradeScript;
import at.workflow.webdesk.tools.DaoJdbcUtil;
import at.workflow.webdesk.tools.JSTools;
import at.workflow.webdesk.tools.cache.CacheHibernateUtils;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.WebdeskApplicationContext.ApplicationContextGetBeanAccessor;

public class PoModuleUpdateServiceImpl implements PoModuleUpdateService, ApplicationContextAware {

	protected final Logger logger = Logger.getLogger(getClass().getName());

	private List<String> modules;
	private PoModuleService moduleService;
	private Map<String, PoRegistrationBean> registrationBeanMap;
	private DaoJdbcUtil daoJdbcUtil;
	private ApplicationContext appCtx;
	private PoQueryUtils queryUtils;

	
	@SuppressWarnings("unchecked")
	public void init() {
		// fillup modules variables for backward compatibility
		modules = new ArrayList<String>();
		registrationBeanMap = appCtx.getBeansOfType(PoRegistrationBean.class);
		for (String key : registrationBeanMap.keySet()) {
			PoRegistrationBean regBean = registrationBeanMap.get(key);
			modules.add(regBean.getFolderOfPackage());
		}

	}
	

	/**
	 * Does an incremental update.
	 * If any of the incremental update steps fails, keeps latest successful version of module.
	 * {@inheritDoc}
	 * 
	 * FIXME: we need a unit test for this!
	 */
	@Override
	public void upgrade(PoModule module, int targetVersion, String implFolder) throws UpgradeFailedException {
		if (targetVersion == 0)	// TODO explain what version 0 means and where it occurs
			return;
		
		if (module.getVersionNumber() == targetVersion)	{
			logger.info("Module "+module.getName()+" is up-to-date: v"+targetVersion);
			return;	// nothing to do
		}
		
		upgrade(module, targetVersion, implFolder, "prepare.xml", false);
		upgrade(module, targetVersion, implFolder, "upgrade.xml", true);
	}
	
	private void upgrade(PoModule module, int targetVersion, String implFolder, String xmlFileName, boolean saveModuleVersion) throws UpgradeFailedException	{
		logger.info("Searching all "+xmlFileName+" of module "+module.getName()+" from v"+module.getVersionNumber()+" to v"+targetVersion);

		int moduleVersion = module.getVersionNumber();
		boolean saveNecessary = saveModuleVersion;	// necessary in case there are only prepare.xml and no upgrade.xml scripts
		
		while (moduleVersion < targetVersion) {
			moduleVersion++;	// increment version BEFORE upgrade, to find the correct upgrade directory
			
			final String path = "/at/workflow/webdesk/"+module.getName()+"/"+implFolder+"/update/v"+moduleVersion+"/";
			final ClassPathResource classPathResource = new ClassPathResource(path+xmlFileName);
			
			if (classPathResource.exists())	{
				saveNecessary = false;	// saveModuleUpgradeVersion() will run when no exception is thrown
				
				logger.info("Executing "+classPathResource.getPath());
				final SAXBuilder builder = XMLTools.getNonValidatingWhitespaceIgnoringSaxBuilder();
				
				try {
					final Document doc = builder.build(classPathResource.getInputStream());	// parse XML
					final Element rootElement = doc.getRootElement();
	
					for (Object rootChildElement : rootElement.getChildren()) {
						cleanupBeforeScriptExecution(module);
						executeXmlElement(path, (Element) rootChildElement);
						cleanupAfterScriptExecution();
					}
					
					if (saveModuleVersion)
						saveModuleUpgradeVersion(module, moduleVersion);
				}
				catch (Exception e) {
					final String message = "Couldn't upgrade module "+module.getName()+" v"+module.getVersionNumber()+" to v"+targetVersion+": "+e.getMessage();
					logger.error(message, e);
					throw new UpgradeFailedException(message, e);
				}
			}
		}
		
		if (saveNecessary)	{
			saveModuleUpgradeVersion(module, moduleVersion);
		}
	}


	private void cleanupBeforeScriptExecution(PoModule module) {
		queryUtils.evictObject(module);	// take module object out of session. TODO: why is this necessary?
	}

	private void cleanupAfterScriptExecution() {
		CacheManager cacheManager = (CacheManager) appCtx.getBean("CacheManager");
		// fri_2012-11-30 it was not possible to share this code with ClearCacheActionListener
		String[] cacheNames = cacheManager.getCacheNames();
		for (int i = 0; i < cacheNames.length; i++) {
			Cache myCache = cacheManager.getCache(cacheNames[i]);
			myCache.removeAll();
		}
		
		CacheHibernateUtils cacheHibernateUtils = (CacheHibernateUtils) appCtx.getBean("PoCacheHibernateUtils");
		cacheHibernateUtils.clearSession();
	}

	private void saveModuleUpgradeVersion(PoModule module, int moduleVersion) {
		// re-attach module to session, this is needed in case a script loaded the module
		module = moduleService.getModule(module.getUID());
		
		module.setVersionNumber(moduleVersion);
		moduleService.saveModule(module);

		logger.info("Module "+module.getName()+" upgraded to v"+moduleVersion);
	}

	private void executeXmlElement(final String path, final Element xmlElement)
		throws Exception
	{
		final String updateScriptType = xmlElement.getName().toLowerCase();
		final String strFailOnError = xmlElement.getAttributeValue("failOnError");
		final boolean failOnError = (strFailOnError != null && strFailOnError.equals("true")); 
		
		try	{
			if (updateScriptType.equals("sql")) {	// SQL statement found
				if (isIntendedDatabaseVendor(xmlElement, daoJdbcUtil.getDatabaseVendorAsString()))
					executeSql(xmlElement);
				else
					logger.debug("Upgrade SQL was not executed, as it was not intended for the current database: "+daoJdbcUtil.getDatabaseVendorAsString());
			}
			else if (updateScriptType.equals("js")) {	// JavaScript found
				final ClassPathResource jsFile = new ClassPathResource(path+xmlElement.getText());
				try	{
					executeJavascript(jsFile);
				}
				catch (NullPointerException e)	{
					throw new PoRuntimeException("Java script not found: "+jsFile.getPath()+", exception: "+e);
				}
			}
			else if (updateScriptType.equals("java")) {	// Java fully qualified class name
				final Class<?> updateScriptClass = Class.forName(xmlElement.getText());
				final PoAbstractUpgradeScript updateScript = (PoAbstractUpgradeScript) updateScriptClass.newInstance();
				executeJava(updateScript);
			}
			else if (updateScriptType.equals("rename")) {	// bean property rename found
				final RenameUpgradeScript renameScript = RenameUpgradeScriptFactory.create(xmlElement.getAttribute("type").getValue());
				renameScript.setXmlContext(xmlElement);
				executeJava(renameScript);
			}
			else {
				throw new PoRuntimeException("Unknown type of upgrade script: "+updateScriptType);
			}
		}
		catch (Exception e) {
			if (failOnError == false) {
				logger.warn("Upgrade script couldn't be executed normally! (Doesn't necessarily have to be a problem.) Change to DEBUG log-level to see the stacktrace.");
				final String failedSql = "Failed "+updateScriptType+" command is: "+xmlElement.getText();
				if (logger.isDebugEnabled())	{
					logger.error(failedSql, e);	// error to see the stack trace
				}
				else	{
					logger.info(failedSql);
					logger.info("Exception is: "+e);
				}
			}
			else	{
				throw e;
			}
		}
	}


	private void executeJava(PoAbstractUpgradeScript javaClassName) {
		final UpdateScriptRunner runner = new UpdateScriptRunner(javaClassName);
		runner.setApplicationContext(appCtx);
		runner.run();
	}

	private void executeSql(Element child) {
		String statement = child.getText();
		String schema = child.getAttributeValue("dataSource");
		daoJdbcUtil.execute(statement, schema);
		logger.info("Successfully executed SQL statement: " + statement);
	}

	private String executeJavascript(Resource jsFile) throws IOException, NullPointerException {
		Map<String,Object> parameterValues = new HashMap<String,Object>();
		parameterValues.put("appCtx", new ApplicationContextGetBeanAccessor(appCtx));
		parameterValues.put("logger", logger);
		String script = IOUtils.toString(jsFile.getInputStream());
		
		JSTools.executeJS(script, parameterValues);
		return script;
	}

	private boolean isIntendedDatabaseVendor(Element child, String currentVendor) {
		List<String> excludes = getCommaSeparatedValues(child.getAttributeValue("excludes"));
		if (isExcluded(currentVendor, excludes))
			return false;

		List<String> includes = getCommaSeparatedValues(child.getAttributeValue("applies"));
		List<String> vendors = null;
		if (includes != null && includes.size() > 0)
			if (includes.contains("*"))	// only vendors supported by webdesk
				vendors = supportedVendors();
			else	// specific vendors, given by the upgrade programmer
				vendors = includes;
		// else: the upgrade programmer specified nothing

		return vendors == null || vendors.contains(currentVendor);
	}

	private boolean isExcluded(String currentVendor, List<String> excludes) {
		if (excludes == null)
			return false;
		
		if (excludes.contains(currentVendor))
			return true;
		
		if (currentVendor.equals(DatabaseConstants.MSSQL))	// resolve ambiguities on MS-SQL
			for (String exclude : excludes)
				if (exclude.equals(DatabaseConstants.MSQL))	// was given as "msql"
					return true;
		
		return false;
	}

	private List<String> supportedVendors() {
		List<String> vendors = new ArrayList<String>();
		vendors.add(DatabaseConstants.DB2);
		vendors.add(DatabaseConstants.MYSQL);
		vendors.add(DatabaseConstants.MSQL);
		vendors.add(DatabaseConstants.MSSQL);
		vendors.add(DatabaseConstants.ORACLE);
		vendors.add(DatabaseConstants.HSQL);
		return vendors;
	}

	private List<String> getCommaSeparatedValues(String text) {
		List<String> values = new ArrayList<String>();
		if (text != null) {
			StringTokenizer tokenizer = new StringTokenizer(text, ",");
			while (tokenizer.hasMoreTokens())
				values.add(tokenizer.nextToken().trim());
		}
		return values;
	}

	@Override
	public List<String> getModules() {
		return modules;
	}

	public void setModules(List<String> modules) {
		this.modules = modules;
	}

	public void setModuleService(PoModuleService moduleService) {
		this.moduleService = moduleService;
	}

	public void setDaoJdbcUtil(DaoJdbcUtil daoJdbcUtil) {
		this.daoJdbcUtil = daoJdbcUtil;
	}

	@Override
	public void setApplicationContext(ApplicationContext arg0) throws BeansException {
		this.appCtx = arg0;
	}

	public void setQueryUtils(PoQueryUtils queryUtils) {
		this.queryUtils = queryUtils;
	}

	@Override
	public void installModules() throws UpgradeFailedException {
		installModules(registrationBeanMap);
	}
	
	
	/** Public only for unit tests. Use installModules() instead. */
	public void installModules(Map<String,PoRegistrationBean> registrationBeans) throws UpgradeFailedException {
        final List<PoModule> registeredModules = new ArrayList<PoModule>();
        
        for (PoRegistrationBean registrationBean : sortedRegistrationBeans(registrationBeans.values())) {
            final PoModule module = getOrCreateModule(registrationBean);
            reactivateDetachedModule(module);
            
            registeredModules.add(module);

            upgrade(module, registrationBean.getVersionNumber(), registrationBean.getImplFolder());
            // executes (1) prepare.xml of all v* directories and (2) upgrade.xml of all v* directories
            // and thus updates every module fully (prepare + upgrade) before going to next module.
            // This anticipates that no _upgrade.xml_ of "po" expects any _prepare.xml_ of "gw" having been executed.
        }
        
        detachNonRegisteredModules(registeredModules);
	}

	/**
	 * In case of undiscovered module-cross-dependencies it is important that
	 * the sort order of modules on upgrade is the same at every Webdesk installation.
	 * So create a fixed sort order for all modules here.
	 */
	private List<PoRegistrationBean> sortedRegistrationBeans(Collection<PoRegistrationBean> registrationBeans) {
		List<PoRegistrationBean> sortedRegistrationBeans = new ArrayList<PoRegistrationBean>(registrationBeans);
        Collections.sort(sortedRegistrationBeans, new Comparator<PoRegistrationBean>()	{
            // "po" <- "wf" <- "ta", rest alfabetically
            private final List<String> moduleOrder = Arrays.asList(new String [] {
            		"po", "wf", "ta", 
            });
            
        	@Override
        	public int compare(PoRegistrationBean b1, PoRegistrationBean b2) {
        		int i1 = moduleOrder.indexOf(b1.getFolderOfPackage());
        		int i2 = moduleOrder.indexOf(b2.getFolderOfPackage());
        		if (i1 >= 0 && i2 >= 0)
        			return i1 - i2;
        		if (i1 >= 0)
        			return -1;
        		if (i2 >= 0)
        			return +1;
        		return b1.getFolderOfPackage().compareTo(b2.getFolderOfPackage());
        	}
        });
		return sortedRegistrationBeans;
	}
	
	private PoModule getOrCreateModule(PoRegistrationBean registrationBean) {
		PoModule module = moduleService.getModuleByName(registrationBean.getFolderOfPackage());
		if (module == null) {
		    module = new PoModule();
		    module.setName(registrationBean.getFolderOfPackage());
		    module.setCreatedAt(DateTools.now());
		    // set version number to version number of registration-bean, as module is installed NOW
		    module.setVersionNumber(registrationBean.getVersionNumber());
		    module.setDetached(false);
		    moduleService.saveModule(module);
		}
		return module;
	}

	private void reactivateDetachedModule(final PoModule module) {
		if (module.isDetached()) {
			logger.info("Module " + module.getName() + " is detached -> reactivate it...");
			
			module.reactivate();
			moduleService.saveModule(module);
		}
	}

	/** Iterates over all modules and sets "detached" flag on non-registered ones. */
	private void detachNonRegisteredModules(final List<PoModule> registeredModules) {
        for (PoModule module : moduleService.loadAllModules()) {
        	if (registeredModules.contains(module) == false && module.isDetached() == false) {
        		logger.info("Module " + module.getName() + " is not licensed any longer -> detaching it ...");
        		module.setDetached(true);
        		
        		moduleService.saveModule(module);
        		
        		if (getProcessReferenceDetachHandler() != null && "wf".equals(module.getName())) {
        			// detach also processes when detached module is the "wf" module
        			getProcessReferenceDetachHandler().detachProcessReferences();
        		}
        	}
        }
	}



	private ProcessReferenceDetachHandler getProcessReferenceDetachHandler() {
		String[] beanNames = appCtx.getBeanNamesForType(ProcessReferenceDetachHandler.class);
		if (beanNames.length > 0) {
			return (ProcessReferenceDetachHandler) appCtx.getBean(beanNames[0]);
		}
		
		return null;
	}

}
