package at.workflow.webdesk.po.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.cocoon.configuration.Settings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import at.workflow.webdesk.po.ActionConfigRegistrationPostProcessor;
import at.workflow.webdesk.po.PoActionService;
import at.workflow.webdesk.po.PoConnectorService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoFileService;
import at.workflow.webdesk.po.PoGeneralDbService;
import at.workflow.webdesk.po.PoHelpMessageService;
import at.workflow.webdesk.po.PoJobService;
import at.workflow.webdesk.po.PoKeyValueService;
import at.workflow.webdesk.po.PoLanguageService;
import at.workflow.webdesk.po.PoModuleService;
import at.workflow.webdesk.po.PoModuleUpdateService;
import at.workflow.webdesk.po.PoRegistrationService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoConnector;
import at.workflow.webdesk.po.model.PoFile;
import at.workflow.webdesk.po.model.PoJob;
import at.workflow.webdesk.po.model.PoKeyValue;
import at.workflow.webdesk.po.model.PoKeyValueType;
import at.workflow.webdesk.po.model.PoLanguage;
import at.workflow.webdesk.po.model.PoModule;
import at.workflow.webdesk.po.model.PoRegistrationBean;
import at.workflow.webdesk.po.model.PoTextModule;
import at.workflow.tools.ResourceHelper;
import at.workflow.webdesk.WebdeskEnvironment;

/**
 * @author hentner, ggruber
 *
 *
 *
 * Depending on which property is set in webdesk.properties 
 * wether 
 * 
 * runRegistration
 * or 
 * runegistrationWithoutTextModule
 * 
 * is called. As the name implies, the first action registers textmodules while the 
 * second does not. 
 * 
 * The list that has to be passed as a parameter contains the different modules that should
 * be registered.
 * 
 * In the end the function runRegistrationOfPackage is called.  
 * 
 * This class is responsible to
 * <ul>
 * 	<li> register Actions </li>
 *  <li> register Configs </li>
 *  <li> register Textmodules </li>
 *  <li> register Jobs </li>
 *  <li> register Job-Configs </li>
 *  <li> register Update Scripts</li>	
 *  <li> register Module Flowscripts </li>
 *  
 *  </ul>
 *
 */
public class PoRegistrationServiceImpl implements PoRegistrationService,
        ApplicationContextAware {

    protected final Log logger = LogFactory.getLog(this.getClass().getName());

    private static int C_REGISTRATIONMODE_FULL = 0;
    private static int C_REGISTRATIONMODE_WITHOUT_TEXTMODULES = 1;
    private static int C_REGISTRATIONMODE_ACTIONS_AND_FLOWS_ONLY = 2;
    
    private PathMatchingResourcePatternResolver pmResolver = new PathMatchingResourcePatternResolver();
    
    // services 
    private PoActionService actionService;
    private PoFileService fileService;
    private PoLanguageService languageService;
    private PoJobService jobService;
    private PoKeyValueService keyValueService;
    private PoConnectorService connectorService;
    private PoModuleService moduleService;
    private PoModuleUpdateService moduleUpdateService;
    private PoHelpMessageService helpMessageService;
    
    // variables
    private ApplicationContext appCtx;

    
    private Map<String, String> unResolvedTextModuleParents = new HashMap<String, String>();
    private List<Resource> adminMenusCps = new ArrayList<Resource>();

    public PoRegistrationServiceImpl() {
        super();
    }

    
    @Override
	public void runRegistration(List<String> l, boolean ignoreIniCache) {
    	ignoreIniCache = getIgnoreIniCache(ignoreIniCache);
    	runRegistrationIfPossible(l, C_REGISTRATIONMODE_FULL, ignoreIniCache, null);
    }
    
    public void runRegistration(List<String> l, boolean ignoreIniCache, Map<String, PoRegistrationBean> registrationBeanMap) {
    	ignoreIniCache = getIgnoreIniCache(ignoreIniCache);
    	runRegistrationIfPossible(l, C_REGISTRATIONMODE_FULL, ignoreIniCache, registrationBeanMap);
    }
    
    /**
     * simple heuristic to determine if we should trust the iniCache or 
     * not. if either actions or textmodules are not present -> register all of them 
     * if the noOfActions*2 (+5% confidence) is bigger than the number of textmodules
     * for the default language, than something is wrong and we should better
     * ignore the ini files!
     * 
     * @param ignoreIniCache
     * @return
     */
    private boolean getIgnoreIniCache(boolean ignoreIniCache) {
    	return ignoreIniCache;
    }
    
    private void runRegistrationIfPossible(List<String> l, int registrationMode, boolean ignoreIniCache, Map<String, PoRegistrationBean> registrationBeanMap) {
    	
    	ignoreIniCache = getIgnoreIniCache(ignoreIniCache);
    	
    	orderModuleListForRegistration(l);
    	unResolvedTextModuleParents = Collections.synchronizedMap(new HashMap<String, String>());
    	
        logger.info("-----------------------------------------------");
        logger.info("|  REGISTRATION STARTS                        |");
        logger.info("-----------------------------------------------");
        logger.info("|                                              ");
        logger.info("|                                              ");
        long time = System.currentTimeMillis();
       
    	boolean syncAdminMenu = moduleUpdateService.getModules().size()==l.size()?true:false;
    	// clear the admin menu (otherwise the sorting could be wrong, when only one 
    	// module was synchronized before
    	if (syncAdminMenu) 
    		adminMenusCps.clear();
    	
    	logger.info("create textmodule cache");
    	Map<String, String> textModuleCache = createTextModuleCache();
    	logger.info("finished creating textmodule cache");
    	
        Iterator<String> i = l.iterator();
        while (i.hasNext()) {
            String pckg = i.next();
            logger.info("-----------------------------------------------");
            logger.info("|  Try to register " + pckg
                    + " package.                |");
            logger.info("-----------------------------------------------");
            try {
            	String key = "PoRegistrationBean_" + pckg;
            	PoRegistrationBean rb = null;
            	if (registrationBeanMap==null || registrationBeanMap.size()==0 || !registrationBeanMap.containsKey(key)) {
            		rb = (PoRegistrationBean) appCtx.getBean(key);
            	} else {
            		rb = registrationBeanMap.get(key);
            	}
                runRegistrationOfPackage(rb, registrationMode, ignoreIniCache, textModuleCache);
            } catch (Exception e) {
            	logger.error(e,e);
            	logger.error("-----------------------------------------------");
                logger.error("|  Registration of " + pckg
                        + " package failed.         |");
                logger.error("-----------------------------------------------");
            }
        }
        
        //	try it 3 times....
        // that means inheritence is checked for max 3 levels....
        if (unResolvedTextModuleParents.size()>0)  {
        	processUnresolvedTextModuleParents();
        }
        if (unResolvedTextModuleParents.size()>0)  {
        	processUnresolvedTextModuleParents();
        }
        if (unResolvedTextModuleParents.size()>0)  {
        	processUnresolvedTextModuleParents();
        }
        
        unResolvedTextModuleParents.clear();
        checkActionsForAvailability();
        
        logger.info("|                                              ");
        logger.info("-----------------------------------------------");
        logger.info("|  REGISTRATION HAS FINISHED AFTER " +(System.currentTimeMillis()-time) + " ms!");
        logger.info("-----------------------------------------------");
        
    }
    
    private void checkActionsForAvailability() {
    	
    	List<PoAction> actions = actionService.loadAllCurrentActions();
    	
    	for (PoAction action : actions) {
    		if (!action.isDetached() &&
    				action.getModule()==null && !"custom".equals(action.getActionFolder())
    				&& (action.getActionType()!=PoConstants.ACTION_TYPE_PROCESS)
    				&& (action.getActionType()!=PoConstants.ACTION_TYPE_CONFIG)) {
    			
    			logger.info("Action " + action.getName() + " has no module and is not custom, probably a dead action => going to detach it!");
    			action.setDetached(true);
    			actionService.saveAction(action);
    		}
    	}
    	
    }
    
    
    private void processUnresolvedTextModuleParents() {
    	// unresolvedTExtModuleParents is a Map
    	// where the key is the UID of the PoTextModule where the parent could not be found
    	// and the value is the name of the parent textmodule
    	Iterator<String> itr = unResolvedTextModuleParents.keySet().iterator();
    	while (itr.hasNext()) {
    		String moduleUID = itr.next();
    		PoTextModule myModule = languageService.getTextModule(moduleUID);
    		if (myModule!=null) {
	    		// find parent
	    		String parentKey = unResolvedTextModuleParents.get(myModule.getUID());
	    		if (parentKey!=null) {
	    			
		    		PoTextModule parent = languageService.findTextModuleByNameAndLanguage(parentKey, myModule.getLanguage());
		    		if (parent!=null) {
		    			myModule.setParent(parent);
		    			languageService.saveTextModule(myModule);
		    			// take out textmodule from Map of unresolved textmodules
		    			unResolvedTextModuleParents.remove(myModule);
		    		}
	    		} else {
	    			logger.warn("Textmodule '" + parentKey + "', referenced as parent from " + myModule.getName() + " was not found!");
	    		}
    		}
    	}
    }

    /**
     * as modules have dependencies to each other this
     * ordering ensures that a dependency tree is traversed
     * that dependent modules are registered after the module
     * was registered this module depends on...
     * 
     * @param l List of Modules (string)
     */
    private void orderModuleListForRegistration(List<String> moduleList) {
    	
    	// simple implementation to ensure
    	// po is allways registered first...
    	if (moduleList.contains("po")) {
    		moduleList.remove("po");
    		moduleList.add(0, "po");
    	}
    	
	}

    @Override
	public void registerActions(Resource[] actionDescriptors,
            String folderOfPackage) {
    	registerActions(actionDescriptors, folderOfPackage, false);
    }
    
    private boolean isRunningModeDev() {
    	Settings settings = (Settings) appCtx.getBean("org.apache.cocoon.configuration.Settings");
    	return settings.getRunningMode().equals("dev");
    }
    
    private void registerActions(Resource[] actionDescriptors,
            String folderOfPackage, boolean ignoreIniFileCache) {
    	try {
    		
	    	File f = createIniFileCacheDir("actions.ini", ignoreIniFileCache);
	    	
	    	Properties props = new Properties();
			props.load(new FileInputStream(f));
			
			Map<String, String> actionCache = new HashMap<String, String>();
			
			// only use actioncache in dev mode
			// as it produces problems in production and the ini
			// cache should avoid the performance problem anyway...
			if (isRunningModeDev()) {
				logger.info("create action cache");
				actionCache = createActionCache(folderOfPackage);
				logger.info("finished creating action cache");
			}
			
	        for (int i = 0; i < actionDescriptors.length; i++) {
	            try {
	                Resource r = actionDescriptors[i];
	                Object o = props.get(r.getURL().getPath());
	                long changedAt  = getChangedDate(r);
	                if (o==null || new Long(o.toString()).longValue()<changedAt || isRunningModeDev()) {
	                	
	                	props.put(r.getURL().getPath(), new Long(changedAt).toString());
		                
	                	if (r.getURL().getPath().indexOf("!") > -1)
		                    actionService.registerAction(r.getInputStream(), r.getURL()
		                            .getPath(), getActionFolderFromFullPath(r.getURL()
		                            .getPath()), actionCache);
		                else
		                    actionService.registerAction(r.getFile(), r.getURL().getPath(),
		                            getActionFolderFromFullPath(r.getURL().getPath()), actionCache);
		                
	            	}
	            } catch (Exception e) {
	                logger.error("Action Registration failed!", e);
	            }
	        } // for all resources
	        
	        saveProperties(f, props);
	        
    	} catch (FileNotFoundException e1) {
    		e1.printStackTrace();
    	} catch (IOException e1) {
    		e1.printStackTrace();
    	}
    	
    }

	private void saveProperties(File f, Properties props) throws FileNotFoundException, IOException {
		FileOutputStream os = new FileOutputStream(f);
		try {
			props.store(os,null);
		} finally {
			os.close();
		}
	}
    
    private File createIniFileCacheDir(String iniFileName, boolean ignoreIniFileCache) throws IOException {
    	File f = new File (getIniFileCacheDir()+"/inifiles/" + iniFileName);
    	if (!f.exists() || ignoreIniFileCache) {
    		
    		if (f.exists())
    			f.delete();
    		
    		File dir = new File(getIniFileCacheDir()+"/inifiles");
    		dir.mkdirs();
    		f.createNewFile();
    	}
    	return f;
    }

    /** 
     * take Cache dir from cocoon if running inside webapplication or
     * the system-tmp dir if running in a testcase.
     * 
     * @return String with path for directory to put ini files in.
     */
    private String getIniFileCacheDir() {
    	
    	Settings settings = (Settings) appCtx.getBean("org.apache.cocoon.configuration.Settings");
    	
    	String cacheDirSettings = settings.getCacheDirectory();
    	if (cacheDirSettings!=null && !"".equals(cacheDirSettings)) {
    		return cacheDirSettings;
    	}
    	
		String dir = System.getProperty("java.io.tmpdir");
        dir = dir.replaceAll("\\\\", "/");
        
		return dir;
	}


    

    /**
     * creates PoActions for every Config found if Config is already in
     * database, checks if Property UpdateOnVersionChange==true if yes -> do
     * update, if no -> do nothing in case of update: check first, if referenced
     * parent Action is found, if not, no config will be registered do not
     * overwrite certain Properties (softvalues in PoActionDAOImpl)
     * 
     * Also writes the corresponding config-File into the database.
     * 
     * @param configs
     */
    @Override
	public void registerConfigs(Resource[] configs) {

        for (int i = 0; i < configs.length; i++) {
        	
        	Resource r = configs[i];
        	String path = null;
        	
            try {
            	path = ResourceHelper.getClassPathOfResource( r );
                PoFile file = fileService.getFilePerPath(path);

                PoAction newConfig = actionService.getActionFromConfigFile(r.getInputStream(), path);
                PoAction oldConfig =  actionService.findActionByNameAndType(newConfig.getName(),PoConstants.ACTION_TYPE_CONFIG);
                
                if (oldConfig!= null) {
                    // Existing Action Config
                    if (oldConfig.isAllowUpdateOnVersionChange()) {
                        // in this case the old config
                    	// is COMPLETLY replaced by the config read from the 
                    	// action-config descriptor file!
                    	// ALSO the softvalues are overwritten!!!!
                    	
                    	actionService.replaceAction(oldConfig, newConfig);
                    	
                        if (newConfig != null && newConfig.getName() != null) {
                            if (file==null) {
                                file = new PoFile();
                                file.setPath(path);
                                file.setTimeStamp(new Date());
                                file.setType(PoConstants.CONFIG);
                                file.setAction(newConfig);
                                file.setMimeType( "text/xml" );
                            }
                            file.setContent(fileService.readFileFromDisk(r.getInputStream()));
                            fileService.saveFile(file, false);
                            
                            if (logger.isDebugEnabled())
                            	logger.debug("Config Action & file (" + newConfig.getName() + ") updated. ");
                        } else
                            logger.warn("File seems not to be a config: " + path);
                        
                        actionService.createPrimaryTextModulesOfAction(newConfig, oldConfig.isAllowUpdateOnVersionChange());
                        callPostProcessors(newConfig);
                    } else {
                    	// nothing is done
                    	// in case the oldconfig has isAllowUpdateOnVersionChange set to false!!!!!!
                    	// so the registration does NOT overwrite the old config!!!!!
                    }
                } else {
                	
                	// New Action Config to be saved
                	
                    PoFile f = new PoFile();
                    f.setPath(path);
                    f.setContent(fileService.readFileFromDisk(r.getInputStream()));
                    f.setTimeStamp(new Date());
                    
                    newConfig.setActionFolder(newConfig.getParent().getActionFolder());
                    
                    // NEW ... automatically set
                    // allowUpdateOnVersionChange = false
                    newConfig.setAllowUpdateOnVersionChange(Boolean.FALSE);
                    
                    actionService.saveAction(newConfig);
                    f.setAction(newConfig);
                    fileService.saveFile(f, false);
                    
                    if (logger.isDebugEnabled())
                    	logger.debug("Config Action & file ("+ newConfig.getName()+ ") saved to DB. ");
                    
                	actionService.createPrimaryTextModulesOfAction(newConfig);
                	callPostProcessors(newConfig);
                }
            } catch (Exception e) {
                logger.error("Registration of Action Config at path " + path + "  failed.", e);
            }
        }

    }

    private void callPostProcessors(PoAction newConfig) {
    	
    	Map<String, ActionConfigRegistrationPostProcessor> postProcessors = appCtx.getBeansOfType( ActionConfigRegistrationPostProcessor.class );
    	for (ActionConfigRegistrationPostProcessor postProcessor : postProcessors.values()) {
    		if (newConfig.getParent().getName().equals( postProcessor.appliesToAction()))
    			postProcessor.afterRegistration( newConfig );
    	}
	}


	/**
     * creates PoJobs for every Config found if Config is already in database,
     * checks if Property UpdateOnVersionChange==true if yes -> do update, if no ->
     * do nothing in case of update: check first, if referenced parent Action is
     * found, if not, no config will be registered
     * 
     * Also writes the corresponding config-File into the database.
     * 
     * @param configs
     */
    @Override
	public void registerJobConfigs(Resource[] configs) {
        try {

            for (int i = 0; i < configs.length; i++) {
                Resource r = configs[i];

                String path = r.getURL().getPath().replaceAll("\\\\", "/");
                if (path.indexOf("!") > -1)
                    path = "classpath:/"
                            + path.substring(path.indexOf("!") + 2);
                else
                    path = path.substring(WebdeskEnvironment.getRealPath().length());
                PoFile file = fileService.getFilePerPath(path);

                PoJob newConfig = jobService.getJobFromConfigFile(r.getInputStream());
                if (file != null) {
                    PoJob oldConfig = file.getJob();
                    if (oldConfig.isAllowUpdateOnVersionChange()) {
                        if (newConfig != null && newConfig.getName() != null) {
                            file.setContent(fileService.readFileFromDisk(r
                                    .getInputStream()));
                            fileService.saveFile(file, false);
                            oldConfig.setConfigurable(true);
                            oldConfig.setParent(newConfig.getParent());
                            jobService.saveOrUpdateJob(oldConfig);
                            logger.debug("Config Action & file ("
                                    + oldConfig.getName() + ") updated. ");
                        } else
                            logger.warn("File seems not to be a config: "
                                    + path);
                    }
                } else {
                    if (newConfig != null) { // indicates that a parent
                                                // action exists
                        PoFile f = new PoFile();
                        f.setPath(path);
                        f.setContent(fileService.readFileFromDisk(r
                                .getInputStream()));
                        f.setTimeStamp(new Date());
                        if (newConfig != null) {
                            if (newConfig.getName() != null) {
                                // NEW ... automatically set
                                // allowUpdateOnVersionChange = false

                                newConfig
                                        .setAllowUpdateOnVersionChange(Boolean.FALSE);
                                newConfig.setConfigurable(true);
                                jobService.saveOrUpdateJob(newConfig);
                                f.setJob(newConfig);
                                fileService.saveFile(f, false);
                                logger.debug("Config Job & file ("
                                        + newConfig.getName()
                                        + ") saved to DB. ");
                            } else
                                logger.warn("File seems not to be a config: "
                                        + path);
                        }
                    } else {
                        logger.warn("Config was not saved!");
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Registration of Configs failed.");
            e.printStackTrace();
        }
    }

    public long capture(long prevTime,String label) {
    	long res = System.currentTimeMillis() - prevTime;
    	if (res > 20)
    		logger.info("duration:" +res+ " : " + label);
    	
    	prevTime = System.currentTimeMillis();
    	return prevTime;
    }
    
    
    /*
     * Synchronises textModule files (/actions/???/i18n.xml) with DB
     * 
     */
    @Override
	public void syncTextModules(Resource[] textModules) {
    	syncTextModules(textModules, false, new HashMap<String, String>());
    }
 
    @SuppressWarnings("unchecked")
	private Map<String, String> createTextModuleCache() {
    	Map<String, String> ret = new HashMap<String, String>();
    	PoGeneralDbService generalDbService = (PoGeneralDbService) appCtx.getBean("PoGeneralDbService");    		
    	for (PoLanguage lang : languageService.findAllLanguages()) {
    		List<Object[]> rows = generalDbService.getElementsAsList("select name, language.code as code, UID from PoTextModule where language=?", new Object[] { lang } );
    		for (Object[] row : rows) {
    			String key = row[0] + "_" + row[1];
    			ret.put(key, (String) row[2]);
    		}
    	}
    	return ret;
    }
    
    @SuppressWarnings("unchecked")
	private Map<String, String> createActionCache(String moduleName) {
    	Map<String, String> ret = new HashMap<String, String>();
    	
    	PoModule myModule = null;
    	if (moduleName != null && !"".equals(moduleName)) {
    		myModule = moduleService.getModuleByName(moduleName);
    	}
    	
    	PoGeneralDbService generalDbService = (PoGeneralDbService) appCtx.getBean("PoGeneralDbService");
    	Collection<PoAction> actions = generalDbService.getElementsAsList("from PoAction where module=? and validfrom<current_timestamp() and validto>current_timestamp()", new Object[] { myModule });
    	
    	Iterator<PoAction> actItr = actions.iterator();
    	while (actItr.hasNext()) {
    		PoAction act = actItr.next();
    		ret.put(actionService.getActionURL(act), act.getUID());
    	}
    	
    	return ret;
    }
 

	private void syncTextModules(Resource[] textModules, boolean ignoreIniFileCache, Map<String, String> textModuleCache) {
        try {
	        File f = createIniFileCacheDir("textmodules.ini", ignoreIniFileCache);
	    	Properties props = new Properties();
			props.load(new FileInputStream(f));
			
	        for (int i = 0; i < textModules.length; i++) {
	        	Resource textModule = textModules[i];
	        	String cacheKey = textModule.getURL().getPath();
	        	
	            try {	// to parse TextModule
	            	Object lastModifiedFileTime = props.get(cacheKey);
	            	// if it has been changed since last caching it
	                if (lastModifiedFileTime == null || Long.valueOf(lastModifiedFileTime.toString()).longValue() < getChangedDate(textModule)) {
	                	props.put(cacheKey, new Long( getChangedDate(textModule)).toString());
	                	languageService.syncTextModule(textModule, unResolvedTextModuleParents, textModuleCache);
	                }
                }
	            catch (Exception e) {
	                throw new PoRuntimeException(e);
	            }
            }	// end for all textmodules
	        
	        saveProperties(f, props);
	        
        } catch (Exception e) {
        	logger.error(e,e);
        }
	}
    
    private long getChangedDate(Resource resource) {
		try {
			String path = resource.getURL().getPath();
			
			if (path.indexOf("!") > -1) {	// we are in a JAR file
				if (path.startsWith("file:/"))
					path = path.substring(6);
				
				String OS = System.getProperty("os.name").toLowerCase();
				String jarFile =path.substring(0,path.indexOf("!"));
		        if (!(OS.indexOf("windows") > -1 || OS.indexOf("nt") > -1))
		        	jarFile = "/" + jarFile;
		        
		        jarFile = jarFile.replaceAll("%20", " ");
		        	
				JarFile jf = new JarFile(jarFile);
				JarEntry je = (JarEntry) jf.getEntry(path.substring(path.indexOf("!")+2));
				return je.getTime();
			}
			
			// we are in file-system
			return resource.getFile().lastModified();
		}
		catch (IOException e) {
			logger.error(e,e);
		}
		return 0;
	}    

    private String getActionFolderFromFullPath(String path) {
    	// FIXME: does not work with custom actions in %WEBCONTAINER%/webapps/webdesk3/./custom/actions/helloWorld/act-descr.xml
    	
    	//if (path.endsWith("/act-descr.xml"))
    	//	path = path.substring(0, path.lastIndexOf("/act-descr.xml"));
    	
        String helper = path.substring(0, path.lastIndexOf('/')); 
        String helper2 = helper.subSequence(0, helper.lastIndexOf('/'))
                .toString(); // po
        String helper3 = helper2.subSequence(0, helper2.lastIndexOf('/'))
                .toString(); // webdesk

        if (helper3.endsWith("webdesk"))
            return (helper2.subSequence(helper2.lastIndexOf('/') + 1, helper2
                    .length()).toString());
        else {
            if (helper3.equals("configs") || helper3.equals("actions"))
                return ("custom");
            else
                return (helper3.subSequence(helper3.lastIndexOf('/') + 1,
                        helper3.length()).toString());
        }
    }

    

    /**
     * @param languageDAO
     *            The languageDAO to set.
     */
    public void setLanguageService(PoLanguageService languageService) {
        this.languageService= languageService;
    }


    @Override
	public void registerJobs(Resource[] jobDescriptors, String folderOfPackage) {
        for (int i = 0; i < jobDescriptors.length; i++) {
            try {
                Resource r = jobDescriptors[i];
                jobService.registerPoJobs(r.getInputStream(), r.getURL().getPath(),
                        getActionFolderFromFullPath(r.getURL().getPath()));
            } catch (Exception e) {
                logger.error("Job Registration failed!");
                e.printStackTrace();
            }
        } // for all resources
    }

    
    /*
     * 
     * Runs the registration of the given package (defined in the registration bean).
     * 
     * The PoRegistrationbean holds all neccessary information (paths to register). 
     * 
     * 
     */
    private void runRegistrationOfPackage(PoRegistrationBean rb, int registrationMode, boolean ignoreIniCache, Map<String, String> textModuleCache) throws IOException {
        
        
        long time2 = System.currentTimeMillis();
        long time = System.currentTimeMillis();
        
        
        try {
            
            // ACTION REGISTRATION
            logger.info("***********************************************");
            logger.info("** Registering Actions of "+rb.getFolderOfPackage() + " package ...     **");
            logger.info("***********************************************");
            logger.info("                                               ");
            
            registerActions(rb, ignoreIniCache);
            
            logger.info("** Action Registration of "+rb.getFolderOfPackage()+" package finished after " + (System.currentTimeMillis() - time )+ " ms!");
            logger.info("***********************************************");
            logger.info("                                               ");
            time = System.currentTimeMillis();
            // HelpMessage REGISTRATION
            logger.info("***********************************************");
            logger.info("** Registering Help Messages of "+rb.getFolderOfPackage() + " package ...     **");
            logger.info("***********************************************");
            logger.info("                                               ");
            
            registerHelpMessages(rb);
            logger.info("** Help Message Registration of "+rb.getFolderOfPackage()+" package finished after " + (System.currentTimeMillis() - time )+ " ms!");
            logger.info("***********************************************");
            logger.info("                                               ");
            
            
            String pattern = "";
            time = System.currentTimeMillis();
            // KEYVALUETYPE REGISTRATION
            Iterator<String> i = rb.getRegisterKeyValueTypes().iterator();
            logger.info("***********************************************");
            logger.info("** Registering KeyValueTypes of "+rb.getFolderOfPackage() + " package ...     **");
            logger.info("***********************************************");
            logger.info(rb.getRegisterKeyValueTypes().size()+ " patterns defined");
            while(i.hasNext()) {
                try {
                    Resource[] ress = null;
                    pattern = i.next();
                    ress = pmResolver.getResources(appendRealPathIfNecessary(pattern));
                    logger.info("found " + ress.length + " keyValueType files with pattern: " + appendRealPathIfNecessary(pattern));
                    registerKeyValueTypes(ress);
                } catch (Exception e) {
                    logger.warn("no File found at pattern: " + pattern);
                }
            }
            logger.info("** KeyValueType Registration of "+rb.getFolderOfPackage()+" package finished  after " + (System.currentTimeMillis() - time )+ " ms!");
            logger.info("***********************************************");
            logger.info("                                               ");
            
            
            // CONFIG REGISTRATION
            time = System.currentTimeMillis();
            logger.info("***********************************************");
            logger.info("** Registering Configs ...                   **");
            logger.info("***********************************************");
            registerConfigs(rb);
            logger.info("** Config Registration finished  after " + (System.currentTimeMillis() - time )+ " ms!");
            logger.info("***********************************************");
            logger.info("                                               ");
            
            if (registrationMode != C_REGISTRATIONMODE_ACTIONS_AND_FLOWS_ONLY) {
            	// JOB REGISTRATION
            	time = System.currentTimeMillis();
                
            	logger.info("***********************************************");
            	logger.info("** Registering Jobs of "+rb.getFolderOfPackage()+" package ...     **");
            	logger.info("***********************************************");
            	registerJobs(rb);
            	logger.info("** Job Registration of "+rb.getFolderOfPackage()+" package finished  after " + (System.currentTimeMillis() - time )+ " ms!");
            	logger.info("***********************************************");
                logger.info("                                               ");
                
            	// CONFIG REGISTRATION
                time = System.currentTimeMillis();
                logger.info("***********************************************");
            	logger.info("** Registering Job Configs ...                   **");
            	logger.info("***********************************************");
            	registerJobConfigs(rb);
            	logger.info("** Job Config Registration finished after " + (System.currentTimeMillis() - time )+ " ms!");
            	logger.info("***********************************************");
                logger.info("                                               ");
                
            }
        
            // Connector Registration
            
            time = System.currentTimeMillis();
            logger.info("***************************************************");
            logger.info("** Registering Connectors of " + rb.getFolderOfPackage() + " package   ");
            logger.info("***************************************************");
            logger.info("** " + rb.getRegisterConnectors().size()+ " patterns defined ");
            i = rb.getRegisterConnectors().iterator();
            while (i.hasNext()) {
                try{
                    Resource[] ress = null;
                    pattern = i.next();
                    ress = pmResolver.getResources(appendRealPathIfNecessary(pattern));
                    
                    logger.info("found " + ress.length + " connector-descriptor files with pattern: " + appendRealPathIfNecessary(pattern));
                    
                    registerConnectors(ress,moduleService.getModuleByName(rb.getFolderOfPackage()));
                } catch (Exception e) {
                	logger.error(e,e);
                	logger.warn("no File found at pattern: " + pattern);
                }            
            }   
            logger.info("** Connector Synchronisation finished  after " + (System.currentTimeMillis() - time )+ " ms!");
        	logger.info("***********************************************");
            logger.info("                                               ");
        
            
            
            
            time = System.currentTimeMillis();
            if (registrationMode == C_REGISTRATIONMODE_FULL) {
	            logger.info("***************************************************");
	            logger.info("** Synchronise i18n files of " + rb.getFolderOfPackage() + " package");
	            logger.info("***************************************************");
	            // TEXT MODULE SYNCHRONISATION
	            i = rb.getSyncTextModules().iterator();
	            while (i.hasNext()) {
	                try {
	                    Resource[] ress = null;
	                    pattern = i.next();                
	                    ress = pmResolver.getResources(appendRealPathIfNecessary(pattern));
	                    logger.info("found " + ress.length + " i18n files with pattern: " + appendRealPathIfNecessary(pattern));
	                    syncTextModules(ress, ignoreIniCache, textModuleCache);
	                    
	                } catch (Exception e) {
	                    logger.warn("No File found at pattern: " + pattern);
	                }            
	            }
            }
            
            logger.info("** I18n File Synchronisation finished after " + (System.currentTimeMillis() - time )+ " ms!");
        	logger.info("***********************************************");
            logger.info("                                               ");
            
            logger.info("* Appending admin menus to list");
            logger.info("***********************************************");
            logger.info("                                               ");
            
            // Iterate over all defined classpath resource patterns
            Iterator<String> resources = rb.getRegisterMenuTree().iterator();
            while (resources.hasNext()) {
            	ResourcePatternResolver rpr = new PathMatchingResourcePatternResolver();
                Resource[] ress = rpr.getResources(resources.next());
                // add them to the adminMenuCps List if not contained already 
                for (int j=0; j<ress.length; j++) {
                	
                	String cpRessource = ResourceHelper.getClassPathOfResource(ress[j]);
                	
                	Resource adminRess = new ClassPathResource(cpRessource);
                	
                	if (!adminMenusCps.contains(adminRess))
                		adminMenusCps.add(adminRess);
                }
            }
            logger.info("------------------------------------------------------------------------");
            logger.info("         Synchronisation of "+rb.getFolderOfPackage() + " package ended after " + (System.currentTimeMillis()-time2) + " ms.");
            logger.info("------------------------------------------------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    private void registerHelpMessages(PoRegistrationBean rb) {
		
    	// Register Actions in DB
        Iterator<String> i = rb.getRegisterHelpMessages().iterator();
        String pattern ="";
        while (i.hasNext()) {
            try {
                Resource[] ress = null;
                pattern = i.next();
                ress = pmResolver.getResources(appendRealPathIfNecessary(pattern));
                logger.info("found " + ress.length + " helpMessage files with pattern: " + appendRealPathIfNecessary(pattern));
                helpMessageService.registerHelpMessages(ress,rb.getFolderOfPackage());
            } catch (Exception e) {
            	logger.warn("no File found at pattern: " + pattern + " exception: " , e);
            }   
        }
	}

	@Override
	public void registerConnectors(Resource[] ress, PoModule module) {
		
    	SAXBuilder builder = new SAXBuilder();
        Document doc;
        for (int i=0; i<ress.length; i++) {
    		try {
				doc = builder.build(ress[i].getInputStream());
				XPath xpath = XPath.newInstance("//connector");
                @SuppressWarnings("unchecked")
				Iterator<Element> results = xpath.selectNodes(doc).iterator();
                while (results.hasNext()) {
                	Element node = results.next();
                	PoConnector connector = connectorService.findConnectorByNameAndModule(node.getChildText("name"), module);
                	if (connector==null)
                		connector = new PoConnector();
                	connector.setName(node.getChildText("name"));
                	
                	if (connector.getUID()==null || connector.isUpdateOnVersionChange()) {
                		
                		String className = node.getChildText("className");
	                	connector.setClassName( className );
	                	connector.setConfigurable(Boolean.valueOf(node.getChildText("configurable")).booleanValue());
	                	connector.setModule(module);
	                	connector.setUpdateOnVersionChange(Boolean.valueOf(node.getChildText("updateOnVersionChange")).booleanValue());
	                	connector.setFlowDirection( connectorService.getFlowDirectionByInspection(connector));
                	}
                	connectorService.saveConnector(connector);
                }
			} catch (JDOMException e) {
				logger.error(e,e);
			} catch (IOException e) {
				logger.error(e,e);
			}
    		
    	}
		
	}



	private void registerActions(PoRegistrationBean rb, boolean ignoreIniCache) {
        // Register Actions in DB
        Iterator<String> i = rb.getRegisterActions().iterator();
        String pattern ="";
        while (i.hasNext()) {
            try {
                Resource[] ress = null;
                pattern = i.next();
                ress = pmResolver.getResources(appendRealPathIfNecessary(pattern));
                logger.info("found " + ress.length + " act-descr files with pattern: " + appendRealPathIfNecessary(pattern));
                registerActions(ress,rb.getFolderOfPackage(), ignoreIniCache);
            } catch (Exception e) {
            	logger.warn("no File found at pattern: " + pattern + ". Enable debug mode to see the exception.");
            	logger.debug(e,e);
            }   
        }
    }
    
    public void registerJobs(PoRegistrationBean rb) throws IOException {
        // Register Actions in DB
        Iterator<String> i = rb.getRegisterJobs().iterator();
        String pattern ="";
        while (i.hasNext()) {
            try {
                Resource[] ress = null;
                pattern = i.next();
                ress = pmResolver.getResources(appendRealPathIfNecessary(pattern));
                logger.info("found " + ress.length + " job-descriptor files with pattern: " + appendRealPathIfNecessary(pattern));
                registerJobs(ress,rb.getFolderOfPackage());
            } catch (Exception e) {
                logger.warn("no File found at pattern: " + pattern);
            }   
        }
    }
    
    
    
    public void registerConfigs(PoRegistrationBean rb) {
        // Register Configs in DB
        Iterator<String> i = rb.getRegisterConfigs().iterator();
        String pattern ="";
        while (i.hasNext()) {
            Resource[] ress = null;
            try {
                pattern = i.next();
                ress = pmResolver.getResources(appendRealPathIfNecessary(pattern));
                logger.info("found " + ress.length + " config files with pattern: " + appendRealPathIfNecessary(pattern));
                registerConfigs(ress);
            } catch (Exception e) {
                logger.warn("no File found at pattern: " + pattern);
            }
        }         
    }
    
    
    public void registerJobConfigs(PoRegistrationBean rb) throws IOException {
        // Register Configs in DB
        Iterator<String> i = rb.getRegisterJobConfigs().iterator();
        String pattern ="";
        while (i.hasNext()) {
            Resource[] ress = null;
            try {
                pattern = i.next();
                ress = pmResolver.getResources(appendRealPathIfNecessary(pattern));
                logger.info("found " + ress.length + " jobconfig files with pattern: " + appendRealPathIfNecessary(pattern));
                registerJobConfigs(ress);
            } catch (Exception e) {
                logger.warn("no File found at pattern: " + pattern);
            }
        }         
    }
    
    
    @Override
	public String appendRealPathIfNecessary(String pattern) {
        if (pattern.startsWith(".")) 
            return("file:" + WebdeskEnvironment.getRealPath() + pattern);
        return pattern;
    }
    

    public void setJobService(PoJobService jobService) {
        this.jobService = jobService;
    }

    @Override
	public void setApplicationContext(ApplicationContext arg0)
            throws BeansException {
        this.appCtx = arg0;
    }


	@Override
	public void runRegistrationWithOutTextModules(List<String> modules) {
		runRegistrationIfPossible(modules, C_REGISTRATIONMODE_WITHOUT_TEXTMODULES, false, null);
	}

	@Override
	public void runRegistrationOfActionsAndFlowscripts(List<String> modules) {
		this.runRegistrationIfPossible(modules, C_REGISTRATIONMODE_ACTIONS_AND_FLOWS_ONLY, false, null);
		
	}

	public void registerKeyValueTypes(Resource[] resources) {
		SAXBuilder builder = new SAXBuilder();
		for (int i = 0; i < resources.length; i++) {
			Resource myResource = resources[i];
			
	        try {
	            Document myDoc = builder.build(myResource.getInputStream());
	            
	            @SuppressWarnings("unchecked")
				List<Element> keyValueTypeList = XPath.selectNodes(myDoc, "/keyvaluetypes/keyvaluetype");
	            Iterator<Element> kvItr = keyValueTypeList.iterator();
	            while (kvItr.hasNext()) {
	            	Element kvtElement = kvItr.next();
	            	
	            	String myKvtName=kvtElement.getAttributeValue("id");
	            	
	            	PoKeyValueType kvt = keyValueService.findKeyValueType(myKvtName);
	            	if (kvt!=null) {
	            		checkExistingKeyValues(kvt, kvtElement);
	            	} else {
	            		createNewKeyValues(kvtElement);
	            	}
	            }
	        } catch (Exception e) {
	        	this.logger.warn("Problems while trying to register KeyValueType " + myResource.getFilename(), e);
	        	
	        }
		}
	}

     
	private String cutToLength(String s, int length) {
		if (s==null) return null;
		if (s.length()>length) 
			return s.substring(0,length);
		else
			return s;
	}

	private void createNewKeyValues(Element kvtElement) {
		String defLangCode = languageService.findDefaultLanguage().getCode();
		
		PoKeyValueType kvt = new PoKeyValueType();
		kvt.setName(cutToLength(kvtElement.getAttributeValue("id"),50));
		kvt.setDescription(kvtElement.getChildText("description"));
		
		@SuppressWarnings("unchecked")
		List<Element> kvList = kvtElement.getChildren("keyvalue");
		Iterator<Element> kvItr = kvList.iterator();
		// iterate over all child <keyvalue> entries in the xml file
		// of the actual <keyvaluetype> node
		while (kvItr.hasNext()) {
			Element kvElem = kvItr.next();
			
			PoKeyValue kv = generateKeyValueAndTextModules(kvElem, defLangCode, kvt.getName());
			
			// add keyvalue to keyvaluetype
			// will be saved on cascade through hibernate
			kvt.addKeyValue(kv);
		}
		
		keyValueService.saveKeyValueType(kvt);
	}

	@SuppressWarnings("unchecked")
	private PoKeyValue generateKeyValueAndTextModules(Element kvElem, String defLangCode, String keyValueTypeName) {
		PoKeyValue kv = new PoKeyValue();
		kv.setKey(cutToLength(kvElem.getAttributeValue("id"),50));
		
		if (kvElem.getAttribute("filter")!=null)
			kv.setFilter( cutToLength(kvElem.getAttributeValue("filter"),255) );
		
		
		// loop through all child keyvalues
		// which are actual language specific
		// f.i.  <de>heute</de>
		//       <en>today</en>
		// 
		List<Element> langKvList = kvElem.getChildren();
		Iterator<Element> langKvItr = langKvList.iterator();
		while (langKvItr.hasNext()) {
			Element langKv = langKvItr.next();
			String lang = langKv.getName();
			// find language object
			PoLanguage language = languageService.findLanguageByCode(lang);
			
			if (language!=null) {
				// lookup textmodule
				PoTextModule myTextModule= languageService.findTextModuleByNameAndLanguage(
						PoConstants.SELECTIONLIST_TEXTMODULE_PREFIX + keyValueTypeName + "_" + kv.getKey(), 
						language);
				
				if (myTextModule==null) {
					// if not existent create it!
					myTextModule = new PoTextModule();
					myTextModule.setLanguage(language);
					myTextModule.setName(PoConstants.SELECTIONLIST_TEXTMODULE_PREFIX + keyValueTypeName + "_" + kv.getKey());
					myTextModule.setAllowUpdateOnVersionChange(Boolean.TRUE);
				}
				myTextModule.setValue(langKv.getText());
				
				if (lang.equals(defLangCode)) {
					kv.setTextModule(myTextModule);
					// will be saved later by saving the KeyValue!!!
				} else {
					// save it ourselves
					languageService.saveTextModule(myTextModule);
				}
			}
		}
		return kv;
	}
	private void checkExistingKeyValues(PoKeyValueType kvt, Element kvtElement) {
		
		if (kvt.isAllowUpdateOnVersionChange()==false) return;
		
		String defLangCode = languageService.findDefaultLanguage().getCode();
		
		kvt.setDescription(kvtElement.getChildText("description"));
		
		Set<String> keys = new HashSet<String>(); 
		
		@SuppressWarnings("unchecked")
		List<Element> kvList = kvtElement.getChildren("keyvalue");
		Iterator<Element> kvItr = kvList.iterator();
		// iterate over all child <keyvalue> entries in the xml file
		// of the actual <keyvaluetype> node
		while (kvItr.hasNext()) {
			Element kvElem = kvItr.next();
			
			String keyName = cutToLength(kvElem.getAttributeValue("id"),15);
			keys.add(keyName);
			
			if (!containsKeyValue(kvt, keyName)) {
				// create KeyValue
				PoKeyValue kv = generateKeyValueAndTextModules(kvElem, defLangCode, kvt.getName());
				kvt.addKeyValue(kv);
			} else {
				// update existing value!
				updateKeyValueAndTextModules(kvt, kvElem, defLangCode);
			}
		}
		
		// historice key value which have been removed from the XML
		for (PoKeyValue keyValue : kvt.getKeyValues()) {
			if (keys.contains(keyValue.getKey())==false) {
				keyValue.historicize();
			}
		}
		
		keyValueService.saveKeyValueType(kvt);
		
	}
	
	private void updateKeyValueAndTextModules(PoKeyValueType kvt, Element kvElem, String defLangCode) {
		
		String keyValueName = cutToLength(kvElem.getAttributeValue("id"),15);
		PoKeyValue kv = findKeyValue(kvt, keyValueName);
		
		String filter =  cutToLength(kvElem.getAttributeValue("filter"),255);
		kv.setFilter(filter);
		
		@SuppressWarnings("unchecked")
		List<Element> langKvList = kvElem.getChildren();
		Iterator<Element> langKvItr = langKvList.iterator();
		while (langKvItr.hasNext()) {
			Element langKv = langKvItr.next();
			String lang = langKv.getName();
			// find language object
			PoLanguage language = languageService.findLanguageByCode(lang);
			
			if (language!=null) {
				// lookup textmodule
				PoTextModule myTextModule= languageService.findTextModuleByNameAndLanguage(
						PoConstants.SELECTIONLIST_TEXTMODULE_PREFIX + kvt.getName() + "_" + kv.getKey(), 
						language);
				
				if (myTextModule==null) {
					// if not existent create it!
					myTextModule = new PoTextModule();
					myTextModule.setLanguage(language);
					myTextModule.setName(PoConstants.SELECTIONLIST_TEXTMODULE_PREFIX + kvt.getName() + "_" + kv.getKey());
					myTextModule.setAllowUpdateOnVersionChange(Boolean.TRUE);
				}
				myTextModule.setValue(langKv.getText());
				
				if (lang.equals(defLangCode)) {
					kv.setTextModule(myTextModule);
					// will be saved later by saving the KeyValue!!!
				} else {
					// save it ourselves
					languageService.saveTextModule(myTextModule);
				}
			}
		}
	}
	
	
	private boolean containsKeyValue(PoKeyValueType keyValueType, String key) {
		return (findKeyValue(keyValueType,key)!=null);
	}
	
	
	private PoKeyValue findKeyValue(PoKeyValueType keyValueType, String key) {
		for (PoKeyValue keyValue : keyValueType.getKeyValues()) {
			if (keyValue.getKey().equals(key))
				return keyValue;
		}
		return null;
	}

	public void setKeyValueService(PoKeyValueService keyValueService) {
		this.keyValueService = keyValueService;
	}

	public void setActionService(PoActionService actionService) {
		this.actionService = actionService;
	}

	public void setFileService(PoFileService fileService) {
		this.fileService = fileService;
	}

	public void setModuleService(PoModuleService moduleService) {
		this.moduleService = moduleService;
	}

	public void setConnectorService(PoConnectorService connectorService) {
		this.connectorService = connectorService;
	}

	public void setHelpMessageService(PoHelpMessageService helpMessageService) {
		this.helpMessageService = helpMessageService;
	}

	public void setModuleUpdateService(PoModuleUpdateService moduleUpdateService) {
		this.moduleUpdateService = moduleUpdateService;
	}
}
