package at.workflow.webdesk.po.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import at.workflow.webdesk.po.PoActionService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoLanguageService;
import at.workflow.webdesk.po.PoModuleService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.daos.PoLanguageDAO;
import at.workflow.webdesk.po.daos.PoTextModuleDAO;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoLanguage;
import at.workflow.webdesk.po.model.PoModule;
import at.workflow.webdesk.po.model.PoTextModule;
import at.workflow.webdesk.tools.IfDate;

/**
 * Hibernate Implementation of the <code>PoLanguageService</code> interface.
 * 
 * @author: hentner, ggruber
 * 
 */
public class PoLanguageServiceImpl implements PoLanguageService, ApplicationContextAware {
	
    private static final Log logger = LogFactory.getLog(PoLanguageServiceImpl.class);

    private static final String TEXTMODULELANG_CACHE_ID = "webdesk_textmodulecache";
    
    /*
	 * DAO's
	 */
	private PoLanguageDAO languageDAO;
	private PoTextModuleDAO textModuleDAO;
	
	
	/*
	 * Services
	 */
    private PoModuleService moduleService;
    
    /*
	 * Variables
	 */
    private String standardDefaultLangCode;	// Spring-initialized in po.applicationCOntext.xml
    
    private Cache tmCache;
    
    private ApplicationContext applicationContext;
    private CacheManager cacheManager;
    
    
	/**
	 * Allocates a tmpCache when not done, and ensures that hardcocded standard languages are in database.
	 * This is called by Spring.
	 */
    @Override
	public void init() {
    	if (tmCache == null) {
	    	tmCache = new Cache(TEXTMODULELANG_CACHE_ID, 30000, false, false, 5000, 2000, false, 5000);
	    	if (cacheManager.getCache(TEXTMODULELANG_CACHE_ID) == null) 
	    		cacheManager.addCache(tmCache);
    	}
    	
    	// ensure that hardcoded standard languages exist in database
    	Iterator<String> itr = PoConstants.getStandardLanguagesMap().keySet().iterator();
    	while(itr.hasNext()) {
    		String code = itr.next();
    		
    		if (findLanguageByCode(code) == null) {
    			PoLanguage newLang = new PoLanguage();
    			newLang.setCode(code);
    			newLang.setName(PoConstants.getStandardLanguagesMap().get(code));
    			newLang.setDefaultLanguage(code.equals(standardDefaultLangCode));
    			
    			saveLanguage(newLang);
    			logger.info("added standard language "+newLang);
    		}
    	}
    }
	
	public void setLanguageDAO(PoLanguageDAO languageDAO) {
		this.languageDAO = languageDAO;
	}
	
	public void setTextModuleDAO(PoTextModuleDAO textModuleDAO) {
		this.textModuleDAO = textModuleDAO;
	}

	@Override
	public List<PoLanguage> findAllLanguages() {
		return this.languageDAO.loadAll();
	}

	private void checkUniqueness(PoLanguage lang) {
		PoLanguage langWithSameCode = this.languageDAO.findLanguageByCode(lang.getCode());
		if (langWithSameCode!=null && (lang.getUID()==null || !lang.getUID().equals(langWithSameCode.getUID()))) {
			throw new PoRuntimeException("2 Langugages with same code '" + lang.getCode() + "' are not allowed!");
		}
		PoLanguage langWithSameName = this.languageDAO.findLanguageByName(lang.getName());
		if (langWithSameName!=null && (lang.getUID()==null || !lang.getUID().equals(langWithSameName.getUID()))) {
			throw new PoRuntimeException("2 Langugages with same name '" + lang.getName() + "' are not allowed!");
		}
	}
	

	@Override
	public void saveLanguage(PoLanguage language) {
        checkUniqueness(language);
        
        boolean isNewLanguage = (language.getUID() == null || language.getUID().equals(""));
        if (isNewLanguage) {
            List<PoTextModule> modulesWithParent = copyTextModulesFromDefaultLanguage(language);
            copyTextModulesWithParentFromDefaultLanguage(language, modulesWithParent);
            logger.info("Copied text modules from default language for new language "+language.toString());
        }
        
        languageDAO.save(language);
	}

	private void copyTextModulesWithParentFromDefaultLanguage(PoLanguage language, List<PoTextModule> modulesWithParent) {
		for (PoTextModule moduleToCopy : modulesWithParent) {
		    language.addTextModule(cloneTextModule(moduleToCopy, language));
		}
	}

	private List<PoTextModule> copyTextModulesFromDefaultLanguage(PoLanguage language) {
		List<PoTextModule> myModules = this.findTextModules(this.findDefaultLanguage());
		List<PoTextModule> modulesWithParent = new ArrayList<PoTextModule>();
		logger.info("Copy Textmodules without Parent from default Language");
		for (PoTextModule moduleToCopy : myModules) {
		    if (moduleToCopy.getParent()==null) {
		        // no parent
		        language.addTextModule(cloneTextModule(moduleToCopy,language));
		    } else { 
		        modulesWithParent.add(moduleToCopy);
			}        
		}
		return modulesWithParent;
	}
    
    /**
	 * clone PoTextModule to new TextModule for a new Language (destLanguage)
	 * 
	 * @param moduleToCopy:
	 *            PoTextModule Object to clone
	 * @param destLanguage:
	 *            PoLanguage Object
	 * @return new PoTextModule
	 */
    private PoTextModule cloneTextModule(PoTextModule moduleToCopy, PoLanguage destLanguage) {
        PoTextModule myModule = new PoTextModule();
        myModule.setAction(moduleToCopy.getAction());
        myModule.setAllowUpdateOnVersionChange( moduleToCopy.isAllowUpdateOnVersionChange() );
        myModule.setLanguage(destLanguage);
        myModule.setModule(moduleToCopy.getModule());
        myModule.setName(moduleToCopy.getName());
        myModule.setLastModified(new Date());
        if (moduleToCopy.getParent()==null) {
            myModule.setValue("[" + destLanguage.getCode() + "] " + moduleToCopy.getValue());
        } else {
            PoTextModule newParent = this.findTextModuleByNameAndLanguage(moduleToCopy.getParent().getName(), destLanguage);
            if (newParent!=null && newParent.getUID()!=null) {
                myModule.setParent(newParent);
            }
        }
        
        return myModule;
    }

    @Override
	public List<PoTextModule> findTextModulesForAction(PoAction action,String languageKey) {
        return this.textModuleDAO.findTextModulesForAction(action, languageKey);
    }

	@Override
	public void saveNewDefaultLanguage(PoLanguage language) {
		PoLanguage oldStandard = this.findDefaultLanguage();
		if (oldStandard != null) {
			oldStandard.setDefaultLanguage(false);
			this.saveLanguage(oldStandard);
		}
		language.setDefaultLanguage(true);
		this.saveLanguage(language);
	}

	@Override
	public void saveTextModule(PoTextModule textModule) {
		IfDate lm = new IfDate();
		lm.add(Calendar.MINUTE, -1);
		textModule.setLastModified(lm);
		
		// ensure we have a module set!
		if (textModule.getAction()!=null && textModule.getModule()==null)
			textModule.setModule(textModule.getAction().getModule());
		
		if (StringUtils.isBlank(textModule.getName()))
			throw new IllegalArgumentException("The passed PoTextModule has not a valid name: " + textModule);
		
		this.textModuleDAO.save(textModule);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see at.workflow.webdesk.po.PoLanguageService#saveAndRefreshTextModule(at.workflow.webdesk.po.model.PoTextModule)
	 */
	@Override
	public void saveAndRefreshTextModule(PoTextModule textModule) {
		this.saveTextModule(textModule);
		// no refreshing is necessary anylonger...
	}

	@Override
	public List<PoTextModule> findStandardTextModules() {
		return this.textModuleDAO.findStandardTextModules();
	}

	
	@Override
	public List<PoTextModule> findTextModuleByName(String name) {
		return this.textModuleDAO.findTextModuleByName(name);
	}
	
	@Override
	public List<PoTextModule> findTextModules(PoLanguage language) {
		return this.textModuleDAO.findTextModules(language);
	}

	@Override
	public void inheritTextModule(PoTextModule father, PoTextModule child) {
		if (child==null)
			child = new PoTextModule();
		child.setAction(father.getAction());
		child.setAllowUpdateOnVersionChange( father.isAllowUpdateOnVersionChange() );
		child.setLanguage(father.getLanguage());
		child.setName(father.getName());
		child.setParent(father);
		father.addChild(child);
		this.textModuleDAO.save(child);
	}

	@Override
	public PoLanguage getLanguage(String uid) {
		return this.languageDAO.get(uid);
	}
	
	@Override
	public void deleteAndFlushTextModule(PoTextModule textModule) {
		this.textModuleDAO.delete(textModule);
	}

    @Override
	public PoLanguage findLanguageByCode(String langCode) {
        return this.languageDAO.findLanguageByCode(langCode);
    }

    @Override
	public List<PoLanguage> loadAllLanguages() {
        return this.languageDAO.loadAll();
    }

    @Override
	public PoTextModule getTextModule(String UID) {
        return this.textModuleDAO.get(UID);
    }

    @Override
	public List<PoTextModule> findCommonTextModules(PoLanguage language) {
        return this.textModuleDAO.findCommonTextModules(language);
    }
    
    private Element fillDocument(Element root, List<PoTextModule> modules, PoLanguage myLang) {
    	//prepare HashMap
    	Map<String, PoTextModule> modulesMap = new HashMap<String, PoTextModule>(modules.size());
    	for (PoTextModule module : modules) {
    		modulesMap.put(module.getName(), module);
    	}
    	
    	for (PoTextModule textModule : modules) {
             if (textModule.getName()!=null) {
                Element myElem = new Element("message");
                myElem.setAttribute("key", textModule.getName());
                
                // handles multiple inheritances
                List<String> alreadyProcessed = new ArrayList<String>();
                while(textModule.getParent()!=null && !alreadyProcessed.contains(textModule.getName())) {
                	alreadyProcessed.add(textModule.getName());
                	textModule=textModule.getParent();
                }
                myElem.setText(replaceContainingReferences(textModule.getValue(),modulesMap, null));
                root.addContent(myElem);
             }
         }
    	return root;
    }
    
    @Override
	public List<PoTextModule> findTextModules(PoLanguage myLang, Date dateOfCreation) {
		return this.textModuleDAO.findTextModules(myLang, dateOfCreation);
	}
    
    @Override
	public PoLanguage findDefaultLanguage() {
    	
    	PoLanguage lang = this.languageDAO.findDefaultLanguage(); 
    	if (lang==null) {
    		// try de
    		lang = findLanguageByCode("de");
    	}

    	if (lang==null) {
    		// try first one
    		List<PoLanguage> langs = loadAllLanguages();
    		if (langs.size()>0)
    			lang = langs.get(0);
    	}

    	return lang;
    }
    
    
    private PoTextModule findTextModuleByNameAndLanguage(String name, PoLanguage lang, Map<String, String> textModuleCache) {
    	String key = name + "_" + (lang==null?"null":lang.getCode());
    	if (textModuleCache!=null && textModuleCache.containsKey(key) ) {
    		if (logger.isDebugEnabled())
    			logger.debug("need no db access for textmodule with key = " + key);
    		
    		String uid = textModuleCache.get(key);
    		return this.getTextModule(uid);
    	} else {
    		if (logger.isDebugEnabled())
    			logger.debug("need db access for textmodule with key=" + key);
    		
    		PoTextModule textModule = this.textModuleDAO.findTextModuleByNameAndLanguage(name, lang);
    		if (textModuleCache!=null && textModule!=null)
    			textModuleCache.put(key, textModule.getUID());
    		return textModule;
    	}
    		
    }

    @Override
	public PoTextModule findTextModuleByNameAndLanguage(String name, PoLanguage lang) {
    	return findTextModuleByNameAndLanguage(name, lang, new HashMap<String, String>());
    }

    @Override
	public void setUpdateOnVersionChangeTrueForAllTextModules() {
        this.textModuleDAO.allowUpdateOnVersionChangeForAllTextModules();
    }

    @Override
	public void setUpdateOnVersionChangeFalseForAllTextModules() {
        this.textModuleDAO.disallowUpdateOnVersionChangeForAllTextModules();
    }
	
    @Override
	public PoTextModule findParentTextModule(PoTextModule textModule) {
    	return this.textModuleDAO.findParentTextModule(textModule);
    }

    @Override
	public String insertParams(String source, List<String> params) {
        try {
	    	
	    	if (source !=null && params!=null)
	            for (int i=0; i<params.size(); i++) {
	            	if (params.get(i)!=null) 
	            		source = source.replaceAll("\\{"+i+"\\}", cut( ( params.get(i)).replaceAll("\\$", ""), 50));
	            	else
	            		source = source.replaceAll("\\{"+i+"\\}", "[err: null was passed]");
	            
	            }
	        return source; 
        } catch (Exception e) {
        	logger.error("An error occured while trying to insert params." + source + " " + params +". Change to debug to see the stacktrace.");
        	logger.debug(e,e);
        	return source;
        }
    }
    
    private String cut(String string, int i) {
		if (string!=null && string.length()>i) {
			return string.substring(0,i) +"..";
		}
		return string;
	}

    public String insertParams(String source, List<String> params, boolean[] keys) {
        for (int i=0; i<params.size(); i++) {
            source = source.replaceFirst("{"+i+"}", params.get(i));
        }
        return source;
    }

    @Override
	public List<String> replaceTextModulesInDependenceList(List<String> params, List<Boolean> keys, String langKey) {
        
        if (params!=null)
            for (int i=0; i<params.size(); i++) {
                try {
                    if (keys.get(i).booleanValue()) {
                        PoTextModule tm = this.findTextModuleByNameAndLanguage(params.get(i), this.findLanguageByCode(langKey));
                        if (tm!=null && tm.getValue()!=null) 
                            params.set(i,tm.getValue());
                        else
                        	if (tm!=null && tm.getParent()!=null)
                        		if (tm.getParent().getValue()!=null)
                        			params.set(i,tm.getParent().getValue());
                    }
                } catch (Exception e) {
                	// TODO cover only specific types of exceptions, let the rest pass!
                	e.printStackTrace();
                    logger.debug("couldn't replace params ["+params.size()+"]. keys ["+keys.size()+"]; Using source value: " + params.get(i));
                }
            }
        return params;
    }
    

    @Override
	public String replaceContainingReferences(String value,PoLanguage language) {
        return replaceContainingReferences(value, null, language);
    }
    
    /**
     * This method is optimized version of its public counterpart. It takes Map of PoTextModules instead of
     * query database for them.
     */
    private String replaceContainingReferences(String value,Map<String, PoTextModule> textModules, PoLanguage language) {
    	
        if (value!=null && value.indexOf("${")!=-1) {
        	
        	if (textModules == null && language == null)
        		throw new IllegalArgumentException("Either a complete Map of textmodules or the reference language has to be supplied to replace containing References!");
        	
            // it could be possible that more than one i18ns are contained
            try {
                String toProcess = value;
                String result = "";
                int safetyCounter = 0;
                while (toProcess.indexOf("${")!=-1 && safetyCounter < 30) {
                    result += toProcess.substring(0,toProcess.indexOf("${"));
                    toProcess = toProcess.substring(toProcess.indexOf("${")+2);
                    String key = toProcess.substring(0,toProcess.indexOf("}"));
                    
                    PoTextModule tm;
                    if (textModules!=null) {
                    	tm = textModules.get(key);
                    } else {
                    	tm = this.findTextModuleByNameAndLanguage(key, language);
                    }
                    
                    String replacement="";
                    if (tm!=null) {
                        if (tm.getParent()==null)
                            replacement = tm.getValue();
                        else
                            replacement = tm.getParent().getValue();
                        
                        result += replacement;
                       
                    } else {
                        result += ("${" + toProcess.substring(0,toProcess.indexOf("}")+1));
                    }
                    toProcess = toProcess.substring(toProcess.indexOf("}")+1);
                    safetyCounter++;
                }
                result +=toProcess;
                value = result;
            } catch (Exception e) {
                logger.warn("Couldn't write textmodule correctly. Using original value:"+value,e);
            }
        }
        return value;
    }
    
    
	@Override
	public void deleteAndFlushLanguage(PoLanguage language) {
		if (false == language.getDefaultLanguage()) {
			// changed to cascade="all-delete-orphan" -> textmodules are deleted automatically
			language.getTextModules().clear();
			language.getHelpMessages().clear();
			languageDAO.save(language);
			languageDAO.delete(language);
			logger.info("deleted language "+language);
		} else
			throw new PoRuntimeException(PoRuntimeException.ERROR_TRY_TO_DELETE_DEFAULT_LANGUAGE);
	}
	


	public void setStandardDefaultLangCode(String standardDefaultLangCode) {
		this.standardDefaultLangCode = standardDefaultLangCode;
	}
	
	
	// START OF DUPLICATE CODE (ALSO CONTAINED IN POACTIONSERVICEIMPL)
	
	private String getActionNameFromFullPath(String path) {
	        String helper = path.substring(0, path.lastIndexOf('/'));
	        return getActionFolderFromFullPath(path)
	                + "_"
	                + helper.subSequence(helper.lastIndexOf('/') + 1,
	                        helper.length()).toString();
	}
	 
	private String getActionFolderFromFullPath(String path) {
	        String helper = path.substring(0, path.lastIndexOf('/')); // actions
	        String helper2 = helper.subSequence(0, helper.lastIndexOf('/'))
	                .toString(); // po
	        String helper3 = helper2.subSequence(0, helper2.lastIndexOf('/'))
	                .toString(); // webdesk

	        if (helper3.endsWith("webdesk") || helper3.endsWith("regdata"))
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

	// END OF DUPLICATE CODE (ALSO CONTAINED IN POACTIONSERVICEIMPL)

	
	@Override
	public void syncTextModule(Resource r, Map<String, String> unResolvedTextModuleParents, Map<String, String> textModuleCache) {
		
		// TODO: refactor this big method, make better names for local variables, put local variables to where they are needed
		
		// don't use IOC this time
		// to avoid circuar IOC references between PoActionService and PoLanguageService
	    PoActionService actionService = (PoActionService) this.applicationContext.getBean("PoActionService");
		
		int counter = 0;
        org.jdom.Document myDoc = new Document();
        SAXBuilder saxBuilder = new SAXBuilder();
        String actionName;
        String textModuleName;
        PoAction action;
        PoTextModule textModule;
        PoModule module;
        int actionType;	
        boolean unresolvedParent = false;
        HashMap<String, PoLanguage> languageMap = new HashMap<String, PoLanguage>();
        
        for (PoLanguage poLanguage : loadAllLanguages()) {
            languageMap.put(poLanguage.getCode(), poLanguage);
        }
        
        try {
			myDoc = saxBuilder.build(r.getInputStream());
	        String fullPathClean = r.getURL().getPath()
	                .replaceAll("\\\\", "/");
	        
	        actionName = getActionNameFromFullPath(fullPathClean);
	
	        if (r.getURL().getPath().indexOf("configs") != -1)
	            actionType = PoConstants.ACTION_TYPE_CONFIG;
	        else
	            actionType = PoConstants.ACTION_TYPE_ACTION;
	        
	        action = actionService.findActionByNameAndType(actionName,actionType);
	        module = this.moduleService.getModuleByName(getActionFolderFromFullPath(fullPathClean));
	        
	            // loop through textmodules
	            @SuppressWarnings("unchecked")
				Iterator<Element> myItr = XPath.selectNodes(myDoc, "//key").iterator();
	            String refKey=null;
	            while (myItr.hasNext()) {
	            	if (counter%100 ==0)
	            		languageDAO.flush();
	            	counter++;
	                Element element = myItr.next();
	                textModuleName = element.getAttributeValue("value");
	                @SuppressWarnings("unchecked")
					Iterator<Element> languageElems = element.getChildren().iterator();
	                while (languageElems.hasNext() && textModuleName != null) {
	                    Element langElem = languageElems.next();
	                    PoLanguage poLang = languageMap.get(langElem.getName()); 
	                    if (poLang == null) {
	                        poLang = createLanguageSilently(languageMap, langElem);
	                    }
	                    unresolvedParent=false;
	                    
	                    textModule = findTextModuleByNameAndLanguage(
	                                    textModuleName, poLang, textModuleCache);
	                    if (textModule == null) {
	                        // not found --> create
	                        textModule = new PoTextModule();
	                        textModule.setAction(action);
	                        textModule.setModule(module);
	                        textModule.setLanguage(poLang);
	                        textModule.setValue(langElem.getTextTrim());
	                        // a newly generated textmodule can be updated! 
	                        textModule.setAllowUpdateOnVersionChange(Boolean.TRUE);
	                        textModule.setName(textModuleName);
	                        refKey = langElem.getAttributeValue("ref-key");
	                        if (refKey != null && !refKey.equals("")) {
	                            PoTextModule tm = findTextModuleByNameAndLanguage(
	                                            refKey, poLang, textModuleCache);
	                            if (tm != null) {
	                                textModule.setParent(tm);
	                            } else {
	                                // parent textmodule not found -->
	                                // set Update Flag to true
	                                // maybe next registration can solve the
									// problem!
	                                textModule.setAllowUpdateOnVersionChange(Boolean.TRUE);
	                                unresolvedParent = true;
	                            }
	                            
	                        }
	                        if (textModule.getParent() == null)
	                        	if (logger.isDebugEnabled())
	                            logger.debug("Saved TextModule "
	                                    + textModule.getName()
	                                    + " value: "
	                                    + textModule.getValue());
	                        else
	                        	if (logger.isDebugEnabled())
	                            logger.debug("Saved TextModule "
	                                    + textModule.getName()
	                                    + " value [from Parent]: "
	                                    + textModule.getParent()
	                                            .getValue());
	
	                        if ( StringUtils.isBlank(textModule.getName()) ) {
	                        	logger.warn("The textmodule=" + textModule + " is not saved, because it has not a valid name!");
	                        } else {
	                        	saveTextModule(textModule);
	                        	// for later retrial...
	                        	if (unresolvedParent && refKey!=null)
	                        		unResolvedTextModuleParents.put(textModule.getUID(), refKey);
	                        }
	                    } else {
	                        // allready here -> check if update is allowed
	                        if (textModule.isAllowUpdateOnVersionChange()) {
	                            textModule.setAction(action);
	                            textModule.setModule(module);
	                            textModule.setLanguage(poLang);
	                            textModule.setValue(langElem.getTextTrim());
	                            textModule.setName(textModuleName);
	                            refKey = langElem.getAttributeValue("ref-key");
	                            if (refKey != null && !refKey.equals("")) {
	                                PoTextModule tm = findTextModuleByNameAndLanguage(
	                                                refKey, poLang);
	                                if (tm != null && !tm.getUID().equals(textModule.getUID())) {
	                                	textModule.setParent(tm);
	                                	textModule.setAllowUpdateOnVersionChange( false );
	                                }
	                                else if(tm!=null)
	                                	logger.warn("Parent of Textmodule references to itself!\ntextmodule:" + textModule.toString() + "\nparent:" + tm.toString());
	                                
	                                
	                            } else
	                                textModule.setParent(null);
	                            if (textModule.getParent() == null)
	                                logger.debug("Synchronised TextModule "
	                                        + textModule.getName()
	                                        + " value: "
	                                        + textModule.getValue());
	                            else
	                                logger.debug("Synchronised TextModule "
	                                        + textModule.getName()
	                                        + " value [from Parent]: "
	                                        + textModule.getParent()
	                                                .getValue());
	                            saveTextModule(textModule);
	                        }
	                    }
	                }
	            }
        } catch (Exception e) {
        	String path = r.toString();
        	try { path = r.getURL().getPath(); } catch (Exception ex) {}
        	String message = "Problems while reading/parsing the XML resource "+path;
        	logger.error(message, e);
        	throw new PoRuntimeException(message, e);
        }
	}

	private PoLanguage createLanguageSilently(HashMap<String, PoLanguage> languageMap, Element langElem) {
		PoLanguage poLang = new PoLanguage();
		poLang.setCode(langElem.getName());
		poLang.setDefaultLanguage(false);
		
		if (PoConstants.getStandardLanguagesMap().containsKey(langElem.getName()))
			poLang.setName(PoConstants.getStandardLanguagesMap().get(langElem.getName()));
		else 
			poLang.setName(langElem.getName());
		
		saveLanguage(poLang);
		languageMap.put(poLang.getCode(), poLang);
		
		return poLang;
	}
	
	@Override
	public void syncTextModule(Resource r, Map<String, String> unResolvedTextModuleParents) {
		syncTextModule(r, unResolvedTextModuleParents, null);
	}


	public void setModuleService(PoModuleService moduleService) {
		this.moduleService = moduleService;
	}

	
	/** 
	 * @param locale the required language.
	 * @param i18nKey the language-neutral identifier for wanted text.
	 * @return translation of given key for given language.
	 */
	@Override
	public String translate(Locale locale, String i18nKey) {
		return translate(i18nKey, locale.getLanguage(), null, null);
	}

	/** 
	 * @param localeStr the <code>locale.getLanguage()</code> string of the required language.
	 * @param i18nKey the language-neutral identifier for wanted text.
	 * @return translation of given key for given language.
	 */
	@Override
	public String translate(String localeStr, String i18nKey) {
		return translate(i18nKey, localeStr, null, null);
	}

	/** 
	 * @param i18nKey the language-neutral identifier for wanted text.
	 * @return translation of given key for default language.
	 */
	@Override
	public String translate(String i18nKey) {
		return translate(i18nKey, (String) null, null, null);
	}
	
	@Override
	public String translate(String i18nKey, Locale locale, List<String> params, List<Boolean> doParamsI18n) {
		return translate(i18nKey, locale.getLanguage(), params, doParamsI18n);
	}

	/**
	 * This is the "OnceAndOnlyOnce" implementation of the translation.
	 * @see at.workflow.webdesk.po.PoLanguageService#translate(java.lang.String, java.util.Locale, java.util.List<String>, java.util.List<Boolean>)
	 */
	private String translate(final String i18nKey, final String localeStr, final List<String> params, final List<Boolean> doParamsI18n) {
		PoLanguage lang = localeStr == null ? findDefaultLanguage() : findLanguageByCode(localeStr);
		if (lang == null && localeStr!=null && localeStr.indexOf("_") > 0)
			lang = findLanguageByCode(localeStr.substring(0, localeStr.indexOf("_")));
		if (lang == null)
			lang = findDefaultLanguage();
		
		if (lang != null) {
			boolean doReplace = params != null && doParamsI18n != null && params.size() == doParamsI18n.size();
			List<String> newParams = doReplace
				? replaceTextModulesInDependenceList(params, doParamsI18n, lang.getCode())
				: params;

			PoTextModule tm = findTextModuleByNameAndLanguage(i18nKey, lang);
			while (tm != null && tm.getParent() != null)
				tm = tm.getParent();

			if (tm != null && tm.getValue() != null)
				return insertParams(replaceContainingReferences(tm.getValue(), lang), newParams);

			return i18nKey;
		}
		
		return "notlanguageset:" + i18nKey + " " + localeStr;
		// TODO: throw an exception from here!
	}
	
	
	
	/**
	 * The code for this method was taken from {@link #writeTextModuleFile(PoLanguage, boolean)}.
	 * @param poLanguage
	 * @param outputStream
	 * @param appendLanguageAttribute
	 * @throws IOException
	 */
	@Override
	public void writeLanguageXMLToOutputStream(PoLanguage poLanguage, OutputStream outputStream, boolean appendLanguageAttribute) throws IOException {
		long st = System.currentTimeMillis();
		Element root=null;
		Document myDoc = null;
		myDoc = new Document();
		root = new Element("catalogue");
		if (appendLanguageAttribute)
			root.setAttribute("lang", poLanguage.getCode());
		myDoc.setRootElement(root);
		List<PoTextModule> modules = findTextModules(poLanguage);
		root = fillDocument(root, modules, poLanguage);
		
		XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat());
		outputter.output(myDoc, outputStream);
		
		long generationTime = (System.currentTimeMillis()-st);
		logger.debug("Generation of xml file took " + generationTime);
	}

	public String getStandardDefaultLangCode() {
		return standardDefaultLangCode;
	}

	@Override
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		this.applicationContext = arg0;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
}
