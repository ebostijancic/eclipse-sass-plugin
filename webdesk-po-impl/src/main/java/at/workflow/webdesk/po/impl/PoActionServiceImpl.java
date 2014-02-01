package at.workflow.webdesk.po.impl;

import java.beans.Expression;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Document;

import at.workflow.tools.XMLTools;
import at.workflow.tools.XPathTools;
import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.PoActionPermissionService;
import at.workflow.webdesk.po.PoActionService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoFileService;
import at.workflow.webdesk.po.PoLanguageService;
import at.workflow.webdesk.po.PoModuleService;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.PoProcessParameterAccessor;
import at.workflow.webdesk.po.PoRoleService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.ProcessReferenceModuleHandler;
import at.workflow.webdesk.po.daos.PoActionCacheDAO;
import at.workflow.webdesk.po.daos.PoActionDAO;
import at.workflow.webdesk.po.daos.PoQueryUtils;
import at.workflow.webdesk.po.daos.PoTextModuleDAO;
import at.workflow.webdesk.po.impl.util.CocoonHelper;
import at.workflow.webdesk.po.model.PoAPermissionAdapter;
import at.workflow.webdesk.po.model.PoAPermissionBase;
import at.workflow.webdesk.po.model.PoAPermissionClient;
import at.workflow.webdesk.po.model.PoAPermissionGroup;
import at.workflow.webdesk.po.model.PoAPermissionPerson;
import at.workflow.webdesk.po.model.PoAPermissionRole;
import at.workflow.webdesk.po.model.PoAPermissionVisitor;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoActionCache;
import at.workflow.webdesk.po.model.PoActionParameter;
import at.workflow.webdesk.po.model.PoActionParameterBase;
import at.workflow.webdesk.po.model.PoActionUrlPattern;
import at.workflow.webdesk.po.model.PoContextParameter;
import at.workflow.webdesk.po.model.PoFile;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoLanguage;
import at.workflow.webdesk.po.model.PoModule;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoTextModule;
import at.workflow.webdesk.po.util.ActionDescriptorParser;
import at.workflow.webdesk.tools.NamingConventions;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.date.HistorizationHelper;

/**
 * 
 * IMPLEMENTATION OF PoActionServiceImpl
 * 
 * Documentation can be found in the PoActionService interface.
 * 
 * Have a look at po.applicationContext.xml in order to see how the 
 * dependency injection works. Also the audit log interceptor is defined 
 * there.
 *
 * @author hentner, ggruber
 */
public class PoActionServiceImpl implements PoActionService, ApplicationContextAware {
	
	
	private static final String PO_RESOURCES_IMAGES = "classpath*:/at/workflow/webdesk/po/resources/images";
	private static final String PROCREF_POSTFIX = ".proc";
	private static final String ACTIONCONFIG_POSTFIX = ".cact";
	private static final String ACTION_POSTFIX = ".act";

	protected final Logger logger = Logger.getLogger(this.getClass().getName());
	
	/*----------------------------------- 
	 DAO's 
	 -----------------------------------*/
    private PoActionDAO actionDAO;
    private PoTextModuleDAO textModuleDAO;
    private PoActionCacheDAO actionCacheDAO;
    
	/*----------------------------------- 
	 Services 
	 -----------------------------------*/
    private PoActionPermissionService permissionService;
    private PoLanguageService languageService;
    private PoOrganisationService orgService;
    private PoRoleService roleService;
    private PoModuleService moduleService;
    private PoFileService fileService;
    
    
	/*----------------------------------- 
	 Util's 
	 -----------------------------------*/
    private CocoonHelper cocoonHelper;
    private PoQueryUtils queryUtils;
    private XPathTools xpathTools;
    

	/*----------------------------------- 
	 injected Variables (Bean Properties) 
	 -----------------------------------*/

    private List<String> softValues = new ArrayList<String>();
    
    /** spring appCtx */
    private ApplicationContext applicationContext;
    
    /**
     * @deprecated
     */
    private Map<String, List<String>> res16Images=new HashMap<String, List<String>>();
    
    
    
	public void setOrgService(PoOrganisationService orgService) {
		this.orgService = orgService;
	}

	
	/**
	 * @param res16Images
	 * @deprecated
	 */
	public void setRes16Images(Map<String, List<String>> res16Images) {
		this.res16Images = res16Images;
	}

	public void setRoleService(PoRoleService roleService) {
		this.roleService = roleService;
	}

	public void setTextModuleDAO(PoTextModuleDAO textModuleDAO) {
		this.textModuleDAO = textModuleDAO;
	}

	/** setter's for DAO's DI **/ 
    public void setActionDAO(PoActionDAO actionDAO) {
    	this.actionDAO = actionDAO;
    }
    
    
    
    /* ACTION METHODS ************************************************/
    
    @Override
	public void saveAction(PoAction action) {
    	if (action.getValidfrom()==null)
            action.setValidfrom(new Date());
        if (action.getValidto()==null)
            action.setValidto(new Date(DateTools.INFINITY_TIMEMILLIS));
        

        // set module for config!
        if (action.getParent()!=null) {
        	action.setModule(action.getParent().getModule());
        }
        
        if (action.getActionType() == PoConstants.ACTION_TYPE_PROCESS) {
        	// here we have to discover the controller..
        	String[] beanNames = applicationContext.getBeanNamesForType(ProcessReferenceModuleHandler.class);
        	if (beanNames.length == 1) {
        		ProcessReferenceModuleHandler handler = (ProcessReferenceModuleHandler) applicationContext.getBean(beanNames[0]);
        		action.setModule(handler.discoverModuleOfAction(action));;
        	}
        }
        
    	actionDAO.save(action);
    }

    @Override
	public PoAction getAction(String uid)  {
    	return actionDAO.get(uid);
    }

    

	@Override
	public void deleteAction(PoAction action, Date date) {
		if (action == null)
			throw new IllegalArgumentException("Can not deleteAction with null action param: "+action);
		
		Collection<PoAPermissionBase> permissions = action.getPermissions();
        // delete all permissions
        for (PoAPermissionBase ap : permissions) {
            
            PoAPermissionVisitor apa = new PoAPermissionAdapter();
            ap.accept(apa);
            if (apa.getWhichClassVisited().equals("AR")) {
            	PoAPermissionRole apr = (PoAPermissionRole) ap;
            	
            	// only delete role, if it
            	// is a dummyrole!!!
            	if (apr.getRole().getRoleType() == PoRole.DUMMY_ROLE) { 
	            	apr.getRole().setValidto(new Date());
	            	
	            	// TODO ev. there are role holders as well  
	            	roleService.saveRole(apr.getRole());
            	}
            }
            
            ap.setValidto(date);
            actionDAO.saveAPermission(ap);
        }
        
        if (action.getChilds() != null && action.getChilds().size() != 0) {
            // action is a configuration, delete childs
            Collection<PoAction> childs = action.getChilds();
            for (PoAction aChild : childs) {
                aChild.setValidto(new Date());
                actionDAO.save(aChild);
            }
        }
        // TODO textmodules ? 
        
        // delete Action
        action.setValidto(new Date());
        actionDAO.save(action);
	}

	@Override
	public void deleteAndFlushAction(PoAction action) {
		
		Collection<PoAPermissionBase> permissions = action.getPermissions();
        if (permissions != null) {
            // delete all permissions
            for (PoAPermissionBase ap : permissions) {
                permissionService.deleteAPermission(ap);
            }
        }
        
        // delete all textModules
        Collection<PoTextModule> tms = action.getTextModules();
        if (tms != null) {
            for (PoTextModule textModule : tms) {
                textModuleDAO.delete(textModule);
            }
        }
        actionDAO.delete(action);
	}


	@Override
	public List<PoAction> findConfigsFromAction(PoAction action) {
	
		return actionDAO.findConfigsFromAction(action);
	}
	

   public String getActionType(PoAPermissionBase ab) {
       if (ab instanceof PoAPermissionRole)
           return "AR";
       if (ab instanceof PoAPermissionClient)
           return "AC";
       if (ab instanceof PoAPermissionPerson)
           return "AP";
       if (ab instanceof PoAPermissionGroup)
           return "AG";
       return null;
   }

   @Override
   public List<PoAction> loadAllActions() {    	
    	return actionDAO.loadAll();
    }

    /* this is static at the moment. When additional image sets are added, this has to be added here as well!
     */
    @Override
	public List<String> loadAllImageSets(){
        return Arrays.asList( new String[] { "nuvola", "crystalIcons" } );
    }

    private boolean isActionAssignedToGroup(PoGroup group, PoAction action, Date date) {
        boolean itc = true; // inherit to child always true
        if (actionDAO.isDirectlyLinkedActionConfigs(group,action, date))
            return true;
        
        while (orgService.getParentGroup(group, date) != null && itc) {
            group = orgService.getParentGroup(group, date).getParentGroup();
            if (actionDAO.isDirectlyLinkedActionConfigParent(group,action,date))
                return true;
        }
        return false;
    }
    
    
    public void deriveAction(PoAction parentAction, PoAction childAction,
            Date date) {
        if (childAction.getFiles() == null)
            childAction.setFiles(parentAction.getFiles());
        if (childAction.getImage().equals(""))
            childAction.setImage(parentAction.getImage());
        if (childAction.getImageSet().equals(""))
            childAction.setImageSet(parentAction.getImageSet());
        if (childAction.getName().equals(""))
            childAction.setName(parentAction.getName());
        if (childAction.getRanking() != 0)
            childAction.setRanking(parentAction.getRanking());
        if (childAction.getDefaultViewPermissionType() != -1)
            childAction.setDefaultViewPermissionType(parentAction
                    .getDefaultViewPermissionType());

        if (childAction.getTextModules() == null
                || childAction.getTextModules().size() == 0)
            childAction.setTextModules(parentAction.getTextModules());
    }
    
    

    public List<PoAction> findActionChilds(at.workflow.webdesk.po.model.PoAction parent,
            Date date) {
        List<PoAction> c = new ArrayList<PoAction>();
        findActionChildsRec(parent, date, c);
        return c;

    }

    private void findActionChildsRec(PoAction parent, Date date, List<PoAction> res) {

    	Collection<PoAction> childs = parent.getChilds();
        for (PoAction action : childs) {
            res.add(action);
            findActionChildsRec(action, date, res);
        }
    }
    
    /**
     * @param imageSet
     * @param size
     * @return
     * @deprecated
     * TODO move to other package 
     */
    private List<String> findImagesWithSize(String imageSet, String size) {
        
        // new code results out of relocating images to package structure!
        PathMatchingResourcePatternResolver pmResolver = new PathMatchingResourcePatternResolver();
        List<String> ret = new ArrayList<String>();
        try {
            Resource[] ress = pmResolver.getResources(PO_RESOURCES_IMAGES + "/" + imageSet + "/" + size + "/*/*.png");
            
            for (int i=0; i<ress.length; i++) {
                String path = ress[i].getURL().toString();
                String str = new String(path.substring(path.indexOf(size)+6,path.length()));
                str = str.replace('\\','/');
                ret.add(str);
            }
        } catch (IOException e) {
            logger.error(e,e);
        }
        
        return ret;
        
    }
    
    /**
     * @deprecated 
     * TODO move to other package 
     */
    @Override
	public List<String> loadImagesFromImageSet(String imageSet) {
    	// if not cached then get the Images
    	if(!res16Images.containsKey(imageSet)) {
	    	List<String> res16=new ArrayList<String>();
	        if(res16.size()==0) {
	        	List<String> res22=new ArrayList<String>();
	        	res16 = findImagesWithSize(imageSet, "16x16");
	        	res22 = findImagesWithSize(imageSet, "22x22");    
	        	try {
	        		res16.retainAll(res22);
	        	} catch (Exception e) {
	        		e.printStackTrace();
	        	}
	    	}
	        res16Images.put(imageSet,res16);
	    }
        return res16Images.get(imageSet);
    }


    @Override
	public PoAction findActionByNameAndType(String name,int type) {
        return actionDAO.findActionByNameAndType(name,type);
    }
    
    @Override
	public List<PoAction> findActionsOfPerson(PoPerson person, Date date) {
        return actionDAO.findActionsOfPerson(person,date);
    }

    @Override
	public List<PoAction> findActionsOfRole(PoRole role, Date date) {
        return actionDAO.findActionsOfRole(role,date);
    }

    @Override
	public String getLocationOfJs(String functionName) {
        return cocoonHelper.getLocationOfJs(functionName);
    }
    
    @Override
	public List<PoAction> findActionByProcessDefId(String procDefId){
    		return actionDAO.findActionByProcessDefId(procDefId);
    }
    
    @Override
	public List<PoAction> loadAllCurrentActions() {
        return actionDAO.loadAllCurrentActions();
    }

	@Override
	public List<PoAction> findAllActions(Date date) {
		return actionDAO.findAllActions(date);
	}

    @Override
	public List<PoAction> findConfigs() {
        return actionDAO.findConfigs();
    }
    
    @Override
	public List<PoAction> findAllCurrentConfigs() {
    	return actionDAO.findAllCurrentConfigs();
    }
    
    
    private void createAndRefreshPrimaryTextModulesOfAction(PoAction action, boolean allowUpdateOnVersionChange, boolean refreshTextModules) {
    	List<PoLanguage> languages = languageService.loadAllLanguages();
    	for (PoLanguage lng : languages) {
    		// this also refreshes the textmodules in the i18n file now!
    		List<PoTextModule> modules = new ArrayList<PoTextModule>();
    		modules.add(getPrimaryTextModuleOfAction(action,"caption",lng, allowUpdateOnVersionChange));
    		modules.add(getPrimaryTextModuleOfAction(action,"description",lng, allowUpdateOnVersionChange));
    		//refreshing of files is gone now    		
    	}
    }
    
    @Override
	public void createPrimaryTextModulesOfAction(PoAction action, boolean allowUpdateOnVersionChange) {
    	createAndRefreshPrimaryTextModulesOfAction(action, allowUpdateOnVersionChange, true);
    }
    
    @Override
	public PoTextModule getPrimaryTextModuleOfAction(PoAction action, String attribute, PoLanguage language) {
    	return getPrimaryTextModuleOfAction(action, attribute, language, false);
    }

	@Override
	public Collection<PoActionParameter> getActionParameters(PoAction action) {
		
		Collection<PoActionParameter> parameters = null;
		if (action.getActionType() == PoConstants.ACTION_TYPE_PROCESS ) {
			PoProcessParameterAccessor parameterHelper = (PoProcessParameterAccessor) WebdeskApplicationContext.getBean("PoProcessParameterAccessor");
			parameters = parameterHelper.getProcessActionParameters(action.getProcessDefId());
		} else if (action.getParent() != null) {
			parameters = action.getParent().getParameters();
		} else {
			parameters = action.getParameters();
		}
		// For sorting the PersistentSet must be changed to List
		List<PoActionParameter> result = new ArrayList<PoActionParameter>();
		for (PoActionParameter parameter : parameters) {
			result.add(parameter);
		}
		Collections.sort(result);
		return result;
	}

	@Override
	public Collection<PoContextParameter> getActionContextParameter(PoAction action) {
		
		Collection<PoContextParameter> parameters = null;
		if (action.getActionType() == PoConstants.ACTION_TYPE_PROCESS ) {
			PoProcessParameterAccessor parameterHelper = (PoProcessParameterAccessor) WebdeskApplicationContext.getBean("PoProcessParameterAccessor");
			parameters = parameterHelper.getProcessContextParameters(action.getProcessDefId());
		} else if (action.getParent() != null) {
			parameters = action.getParent().getContextParameters();
		} else {
			parameters = action.getContextParameters();
		}
		// For sorting the PersistentSet must be changed to List
		List<PoContextParameter> result = new ArrayList<PoContextParameter>();
		for (PoContextParameter parameter : parameters) {
			result.add(parameter);
		}
		Collections.sort(result);
		return result;
	}
	
	
    
    private PoTextModule getPrimaryTextModuleOfAction(PoAction action, String attribute, PoLanguage language, boolean allowUpdateOnVersionChange) {
    	
    	PoLanguage defLang = languageService.findDefaultLanguage();
    	String postFix = getActionPostfix(action);
    	String tmKey = action.getName()+postFix+"_action_"+attribute;
        PoTextModule oldtm = languageService.findTextModuleByNameAndLanguage(tmKey,language);
    	//po_editPerson.act_action_caption
    	
    	if (oldtm == null || 
    			((allowUpdateOnVersionChange || (oldtm.getValue()!=null && oldtm.getValue().startsWith("["))) 
    					&& language.equals(defLang))) {
    		
    		// case where no textmodule is yet available
    		// or textmodule should be updated. 
    		// or textmodule begins with [ while being a default language tm
            PoTextModule tm = new PoTextModule();
	    	tm.setAction(action);
	    	tm.setLanguage(language);
	    	PoModule module = moduleService.getModuleByName(action.getActionFolder());
	    	tm.setModule(module);
	    	tm.setName(tmKey);
	    	if (language.equals(defLang)) {
	    		if (attribute.equals("caption"))
	    			tm.setValue(action.getCaption());
	    		else
	    			tm.setValue(action.getDescription());
	    		
	    		// be sure that next syncTextModules (as part of registration)
	    		// ** DOES NOT ** restore captions/descriptions from 
	    		// i18n file!
	    		tm.setAllowUpdateOnVersionChange(true);
	    		
	    	} else {
	    		// make automatic textmodules for other languages
	    		if (attribute.equals("caption"))
	    			tm.setValue("["+language.getCode()+"] "+action.getCaption());
	    		else
	    			tm.setValue("["+language.getCode()+"] "+action.getDescription());
	    		
	    		// be sure that next syncTextModules (as part of registration)
	    		// **DOES** restore captions/descriptions from 
	    		// i18n file, if those exist!
	    		tm.setAllowUpdateOnVersionChange(true);
	    	}

           
	    	if (oldtm!=null) {
	    		
	    		if (allowUpdateOnVersionChange) {
	    			// update existing entry
	    			queryUtils.evictObject(oldtm);
	    			tm.setUID(oldtm.getUID());
	    			languageService.saveTextModule(tm);
	    		} else {
	    			queryUtils.evictObject(tm);
	    			return oldtm;
	    		}
            } else {
                // insert new entry
                languageService.saveTextModule(tm);
            }
            return tm;
    	} else
            return oldtm;
    }
    
    @Override
	public void createPrimaryTextModulesOfAction(PoAction action) {
    	createPrimaryTextModulesOfAction(action, false);
    }
    
	public void setCocoonHelper(CocoonHelper cocoonHelper) {
		this.cocoonHelper = cocoonHelper;
	}

    @Override
	public void checkAction(PoAction action) {
        actionDAO.checkAction(action);
        
    }
    
    @Override
	public void registerAction(Object actionDescr, String path, String folderOfPackage) {
    	registerAction(actionDescr, path, folderOfPackage, new HashMap<String, String>());
    }
    
    @Override
	public void registerAction(Object actionDescr, String path, String folderOfPackage, Map<String, String> actionCache) {
    	
    	
        PoAction newActionDTO = parseActionInfosFromDescriptor(actionDescr, folderOfPackage);
        String actionName= getActionNameByPath(path,folderOfPackage);
        newActionDTO.setName(actionName);
        
        PoAction currentAction = getActionFromDbOrCache(actionCache, actionName);
        
        restoreSoftValuesFromCurrentAction(folderOfPackage, newActionDTO, currentAction);
        
        boolean updateNeeded = true;
        boolean newActionFlag = false;
        	
        if (currentAction==null) {
        	// new
        	newActionFlag = true;
        	currentAction = newActionDTO;
        } else {
        	// update
        	queryUtils.evictObject(newActionDTO);
        	String[] ignoreProps = new String[] { "uid", "UID", "validfrom", "validto", "files", "helpMessages", "actionCaches", "permissions", "textModules", "allowedByActions", "childs", "detached", "attributes", "parameters", "urlPatterns", "contextParameters"};
        	
        	// only save action if something changed.
        	if (!EqualsBuilder.reflectionEquals(currentAction, newActionDTO, ignoreProps) ||
        			!parameterCollectionEquals(currentAction.getParameters(), newActionDTO.getParameters()) ||
        			!parameterCollectionEquals(currentAction.getUrlPatterns(), newActionDTO.getUrlPatterns()) ||
        			!parameterCollectionEquals(currentAction.getContextParameters(), newActionDTO.getContextParameters()) ||
        			!currentAction.getAttributes().equals(newActionDTO.getAttributes())) {
        		
        		BeanUtils.copyProperties(newActionDTO, currentAction, ignoreProps);
        		copyCascadingCollectionOrMap(newActionDTO, currentAction, "parameters", PoActionParameter.class, false, "setAction");
        		copyCascadingCollectionOrMap(newActionDTO, currentAction, "urlPatterns", PoActionUrlPattern.class, false, "setAction");
        		copyCascadingCollectionOrMap(newActionDTO, currentAction, "contextParameters", PoContextParameter.class, false, "setAction");
        		copyCascadingCollectionOrMap(newActionDTO, currentAction, "attributes", PoActionParameter.class, true, null);
        	} else if (!currentAction.isDetached()){
        		updateNeeded = false;
        	}
        }
        
        
        if (updateNeeded)  {
	        try {
	        	currentAction.reactivate();
        		saveAction(currentAction);
        		
        		if (logger.isDebugEnabled()) {
	        		if (newActionFlag) {
	        			logger.debug("Saved new Action " + currentAction.getName());
	        		} else {
	        			logger.debug("Updated Action " + currentAction.getName());
	        		}
        		}
        		
    			createAndRefreshPrimaryTextModulesOfAction(currentAction, currentAction.isAllowUpdateOnVersionChange(), false);
        		
        		reattachChildActions(currentAction);
		    
	        } catch (Exception e) {
	        	logger.error("problems registering Action with Name = " + newActionDTO.getName() + " while trying to save...");
	        	throw new RuntimeException(e);
	        }
    	}
    }


    /**
     * compares 2 collections with PoActionParameter by comparing all internal props except UID, action
     * @return
     */
	private boolean parameterCollectionEquals(Collection<? extends PoActionParameterBase> oldParameters, 
			Collection<? extends PoActionParameterBase> newParameters) {
		
		if (oldParameters==null && newParameters==null)
			return true;
		
		if (oldParameters==null || newParameters==null)
			return false;
		
		if (oldParameters.size() != newParameters.size())
			return false;
		
		for (PoActionParameterBase newParam : newParameters) {
			if (findParameterByName(oldParameters, newParam.getName())==null)
				return false;
			
			if (!parameterEquals(findParameterByName(oldParameters, newParam.getName()),newParam))
				return false;
		}
		
		for (PoActionParameterBase oldParam : oldParameters) {
			if (findParameterByName(newParameters, oldParam.getName())==null)
				return false;
			
			if (!parameterEquals(findParameterByName(newParameters, oldParam.getName()),oldParam))
				return false;
		}
		
		return true;
	}
	
	private boolean parameterEquals(PoActionParameterBase oldP, PoActionParameterBase newP) {
		return EqualsBuilder.reflectionEquals(oldP, newP, new String[] { "uid", "UID", "action" });
	}

	private PoActionParameterBase findParameterByName(Collection<? extends PoActionParameterBase> oldParameters, String name) {
		
		for (PoActionParameterBase param : oldParameters) {
			if (param.getName().equals(name))
				return param;
		}
		return null;
	}


	
	@SuppressWarnings("unchecked")
	private void copyCascadingCollectionOrMap(Object newActionDTO, Object currentAction, String collectionName, Class<?> elementClass, boolean isMap, String setInverseReferenceMethodName) {
		
		try {
			Field field = org.springframework.util.ReflectionUtils.findField(PoAction.class, collectionName);
			field.setAccessible(true);
			
			if (isMap) {
				Map<String, Object> mapOriginal = (Map<String, Object>) field.get(currentAction);
				Map<String, Object> mapNew = (Map<String, Object>) field.get(newActionDTO);
				mapOriginal.clear();
				mapOriginal.putAll(mapNew);
			} else {
				Collection<Object> collOriginal = (Collection<Object>) field.get(currentAction);
				Collection<Object> collNew = (Collection<Object>) field.get(newActionDTO);
				collOriginal.clear();
				collOriginal.addAll(collNew);
				
				Method method = org.springframework.util.ReflectionUtils.findMethod(elementClass, setInverseReferenceMethodName, new Class[] { PoAction.class });
				for (Object elem : collOriginal) {
					method.invoke(elem, new Object[] { currentAction });
				}
				
			}
		} catch (Exception e) {
			throw new PoRuntimeException("Problems copying all-delete-orphan collection",e);
		}
	}
		


	private void reattachChildActions(PoAction currentAction) {
		if (currentAction.getChilds().size()>0) {
			for(PoAction child : currentAction.getChilds()) {
				if (child.isDetached()) {
					child.setDetached(false);
					saveAction(child);
				}
			}
		}
	}


	private void restoreSoftValuesFromCurrentAction(String folderOfPackage,
			PoAction newActionDTO, PoAction currentAction) {
		if (currentAction!=null) {
            newActionDTO.setValidfrom(currentAction.getValidfrom());
            
            if (!currentAction.isAllowUpdateOnVersionChange()) {
                // if UpdateOnVersionChange should not happen
                // restore SOFT VALUES from old Action
            	for (String sv : softValues) {
                    try {
                        String functionName= sv.substring(0,1).toUpperCase();
                        functionName += sv.substring(1,sv.length());
                        Expression ex;
                        try {
                            ex = new Expression(currentAction,"get"+functionName,null);
                            // try to access the value -> exception is not thrown, until
                            // value is actually accessed!
                            ex.getValue();
                            // if an error is thrown, try isMethod...
                        } catch (Exception exc) {
                            // try with boolean
                            ex = new Expression(currentAction,"is"+functionName,null);
                        }
                        Expression set = new Expression(newActionDTO,"set"+functionName,new Object[]{ex.getValue()});
                        set.execute();
                    } catch (Exception e1) {
                        logger.error(e1,e1);
                        e1.printStackTrace();
                    }
                }
            }
            
            // newer versions of the action
            // may only *ENABLE* universallyallowed
            // but are not allowed to reset the permission later on again!
            if (currentAction.isUniversallyAllowed() && !newActionDTO.isUniversallyAllowed()) {
            	newActionDTO.setUniversallyAllowed(true);
            }
            
            // newer versions of the action are not able to change
            // the defaultviewpermissiontype!!!! So if you want to change it, you
            // have to write an update script!
            newActionDTO.setDefaultViewPermissionType(currentAction.getDefaultViewPermissionType());
            newActionDTO.setActionFolder(folderOfPackage);
        }
	}


	private PoAction getActionFromDbOrCache(Map<String, String> actionCache, String actionName) {
		PoAction currentAction;
		if (actionCache.containsKey(actionName + getActionPostfix(PoConstants.ACTION_TYPE_ACTION))) {
        	String oldActionUid = actionCache.get(actionName + getActionPostfix(PoConstants.ACTION_TYPE_ACTION));
        	currentAction = getAction(oldActionUid);
        	
        } else {
        	currentAction = findActionByNameAndType(actionName, PoConstants.ACTION_TYPE_ACTION);
        }
		return currentAction;
	}
    
       
    private String getActionNameByPath(String path,String folderOfPackage) {
        String actionName ="";
        if (path!=null && !path.equals("")) {
            path = path.replaceAll("\\\\","/");
            actionName = path.substring(0,path.lastIndexOf("/"));
            actionName = actionName.substring(actionName.lastIndexOf("/")+1,actionName.length());
            actionName = folderOfPackage+"_"+actionName;
        } else 
            throw new PoRuntimeException("Synchronisation failed.!");
        return actionName;
    }
    
    @Override
	public PoAction setSoftValuesOfAction(PoAction newConfig, PoAction oldConfig) {
        try {
        	for (String field : softValues) {
            	// transfers softvalues from newConfig to oldConfig
                Object result = null;
                String functionName= field.substring(0,1).toUpperCase();
                functionName += field.substring(1,field.length());
                Expression e = new Expression(result,oldConfig,"get"+functionName,null);
                e.execute();
                e.getValue();
                Expression set = new Expression(newConfig,"set"+functionName,new Object[]{result});
                set.execute();
            }
            return oldConfig;
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn(e,e);
            return null;
        }
    }
    
	public void setSoftValues(List<String> softValues) {
		this.softValues = softValues;
	}

    
    @Override
	public PoAction getActionFromConfigFile(InputStream is, String path) {
    	
    	Document configXml = XMLTools.createW3cDocFromStream(is);
    	String parentActionName = xpathTools.getXPathStringValue(configXml, "/action-config/sitemap/action");
    	
        PoAction pa = null;
        PoAction newAction = new PoAction();
        newAction.setActionType(PoConstants.ACTION_TYPE_CONFIG);
                
        pa = findActionByNameAndType(parentActionName,PoConstants.ACTION_TYPE_ACTION);
        if (pa==null) 
            throw new IllegalStateException("Config has no corresponding registered Action " + parentActionName);
        
        if (pa.isDetached()) 
        	throw new IllegalStateException("Config at path " + path + " depends on detached parent action with name=" + parentActionName);
           
        
        String nameOfConfig = extractConfigName(path, configXml);
        if (StringUtils.isBlank( nameOfConfig ))
        	throw new IllegalArgumentException("For the Action Config in file " + path + ", no useful action name could be extracted!" );
        
        String actionCaption = xpathTools.getXPathStringValue(configXml, "/action-config/layout/caption");
        String actionDescription = xpathTools.getXPathStringValue(configXml, "/action-config/layout/description");
        String actionImageset = xpathTools.getXPathStringValue(configXml, "/action-config/layout/imageset");
        String actionImage = xpathTools.getXPathStringValue(configXml, "/action-config/layout/image");
        boolean actionAllowUpdateOnVersionChange = xpathTools.getXPathBooleanValue(configXml, "/action-config/layout/allowupdateonversionchange", false);
        
        newAction.setCaption( StringUtils.isBlank(actionCaption)==false ? actionCaption : nameOfConfig );
        newAction.setDescription( actionDescription );
        
        if (StringUtils.isBlank(actionImageset) || StringUtils.isBlank(actionImage)) {
        	// take images from parent
        	newAction.setImageSet( pa.getImageSet() );
        	newAction.setImage( pa.getImage() );
        } else {
        	newAction.setImageSet( actionImageset );
        	newAction.setImage( actionImage );
        }
        newAction.setParent( pa );
        
        newAction.setName( nameOfConfig );
        newAction.setAllowUpdateOnVersionChange( actionAllowUpdateOnVersionChange );
        
        // set module of path, not the module of the parent module!
        // f.i. we could define a action config within the TM module, which references the wf_getOrgProcessInstances.act action.
        newAction.setModule( extractModuleFromPathOfConfigFileName(path) );
        
		return newAction;
    }


	private String extractConfigName(String path, Document configXml) {
		String nameOfConfig = xpathTools.getXPathStringValue(configXml, "/action-config/layout/url");
        if (nameOfConfig!=null && nameOfConfig.indexOf(ACTIONCONFIG_POSTFIX)>-1) 
            nameOfConfig = nameOfConfig.substring(0,nameOfConfig.indexOf(ACTIONCONFIG_POSTFIX));
        
        if (StringUtils.isBlank(nameOfConfig) && StringUtils.isBlank(path)==false) {
        	if (path.indexOf("/")>=0) {
        		nameOfConfig = path.substring(path.lastIndexOf("/")+1);
        		if (nameOfConfig.indexOf('.')>=0) {
        			nameOfConfig = nameOfConfig.substring(0, nameOfConfig.lastIndexOf('.'));
        		}
        	}
        }
		return nameOfConfig;
	}
    
    private PoModule extractModuleFromPathOfConfigFileName(String path) {
    	String moduleName = NamingConventions.getModuleNameOfResource(path);
		return moduleService.getModuleByName(moduleName);
	}


	private int getActionConfigLevel(PoAction actionConfig,PoPerson person,Date date) {
    	
        if (actionDAO.isActionAssignedToPerson(person,date,actionConfig))
            return 1;
        if (isActionAssignedToGroup(orgService.getPersonsHierarchicalGroup(person,date),actionConfig,date))
            return 2;
        

        List<PoGroup> notHierarchicalGroups = orgService.findNotHierarchicalGroupsOfPerson(person, date);
        for (PoGroup g : notHierarchicalGroups) {
            if (actionDAO.isDirectlyLinkedActionConfigs(g,actionConfig, date))
                 return 3;
        }
        
        List<PoRole> roles = roleService.findRolesOfPerson(person, date);
        for (PoRole role : roles) {
            if (actionDAO.isActionAssignedToRole(role, actionConfig, date))
                return 4;                
        }
        if (actionDAO.isActionAssignedToClient(person.getClient(), actionConfig, date))
            return 5;
        
        if (actionConfig.isUniversallyAllowed())
        	return 6;
        
        return -1;
    }
    
    
    @Override
	public PoAction getConfigFromAction(PoPerson person, PoAction action) {
    	return getConfigFromAction(person, action, HistorizationHelper.getNowWithHourPrecision(), null, false);
    }
    
    @Override
	public PoAction getConfigFromAction(PoPerson person, PoAction action, String targetPersonUid) {
    	return getConfigFromAction(person, action, HistorizationHelper.getNowWithHourPrecision(), targetPersonUid, false);
    }
    
    @Override
	public PoAction getConfigFromActionWithoutTargetPermCheck(PoPerson person, PoAction action, String targetPersonUid) {
    	return getConfigFromAction(person, action, HistorizationHelper.getNowWithHourPrecision(), targetPersonUid, true);
    }
    
    private PoAction getConfigFromAction(PoPerson person, PoAction action, Date date, String targetPersonUid, boolean omitTargetPersonCheck) {
    	
    	// double check that action object is initialized!
    	action = getAction(action.getUID());
    	
    	PoPerson targetPerson = null;
    	if (targetPersonUid != null) {
    		targetPerson = orgService.getPerson(targetPersonUid);
    	}
    	
    	Collection<PoAction> actionConfigs = action.getChilds();
    	int actLevel = 100;
    	PoAction resActionConfig = null;
    	for (PoAction actionConfig : actionConfigs) {
    		
    		// FIXME: we should also check if the person has permission for the actionConfig at all, if targetPersonUid is null!
    		if (actionConfig.getValidfrom().before(date) &&
    				actionConfig.getValidto().after(date) && 
    				(targetPersonUid == null || targetPerson == null || 
    						(permissionService.hasPersonPermissionToEditObjectWithId(targetPersonUid, person.getUserName(), actionConfig, date) &&
    								(omitTargetPersonCheck || permissionService.hasPersonPermissionForAction(targetPerson, actionConfig))))) {
    			
    			// check configlevel for person for which we call the data!
    			// or for the calling user if omitTargetPersonCheck is set
    			int level = getActionConfigLevel(actionConfig, (targetPerson != null && !omitTargetPersonCheck) ? targetPerson : person, date);
    			logger.debug("Action Config level is " + level + " [Action:" + actionConfig+"]");
    			if (level < actLevel && level !=-1) {
    				resActionConfig = actionConfig;
    				actLevel = level;
    				if (actLevel==1)
    					break;
    			}
    		}
    	}
    	logger.debug("returning " + resActionConfig);
    	return resActionConfig;
    }
    
    
    public List<PoAction> findActionsThatAllowAction(PoAction action, Date date) {
    	return actionDAO.findActionsThatAllowAction(action, date);
    }
    
    @Override
	public List<PoFile> findFilesFromAction(PoAction action) {
        return fileService.findFilesOfActionOrderByVersion(action);
    }
    
    // Process only files under dir
    public static void visitAllFiles(File dir) {
    	
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                visitAllFiles(new File(dir, children[i]));
            }
        } else {
            // dir
        }
    }
    

	private PoAction parseActionInfosFromDescriptor(Object obj, String moduleName) {
    	PoModule actModule = moduleService.getModuleByName(moduleName);
        return ActionDescriptorParser.parseActionInfosFromDescriptor(this, obj, actModule);
    }

    @Override
	public PoAction getActionByPath(String path,String folderOfPackage) {
    	
        String actionName ="";
        if (path!=null && !path.equals("")) {
            path = path.replaceAll("\\\\","/");
            actionName = path.substring(0,path.lastIndexOf("/"));
            actionName = actionName.substring(actionName.lastIndexOf("/")+1,actionName.length());
            actionName = folderOfPackage+"_"+actionName;
        } else 
            throw new PoRuntimeException("No Action with given path found!");
        return findActionByNameAndType(actionName,PoConstants.ACTION_TYPE_ACTION);
    }
    
	@Override
	public void replaceAction(PoAction oldAction, PoAction newAction) {
		queryUtils.evictObject(oldAction);
		newAction.setUID(oldAction.getUID());
		saveAction(newAction);
	}


	public void setModuleService(PoModuleService moduleService) {
		this.moduleService = moduleService;
	}

	public void setQueryUtils(PoQueryUtils queryUtils) {
		this.queryUtils = queryUtils;
	}

	public void setLanguageService(PoLanguageService languageService) {
		this.languageService = languageService;
	}

	
    @Override
	public List<String> findActionNamesOfModule(String name) {
    	return actionDAO.findActionNamesOfModule(name);
    }
    
    @Override
	public String getActionPostfix(PoAction action) {
    	
    	if (action==null) return "";
    	
        return getActionPostfix(action.getActionType());
    }
    
    private String getActionPostfix(int actionType) {
    	
        if (actionType==PoConstants.ACTION_TYPE_ACTION || actionType==PoConstants.ACTION_TYPE_CUSTOM) {
            return ACTION_POSTFIX;
        } else if (actionType==PoConstants.ACTION_TYPE_CONFIG) {
            return ACTIONCONFIG_POSTFIX;
        } else if (actionType==PoConstants.ACTION_TYPE_PROCESS) {
            return PROCREF_POSTFIX;
        } else {
            return ".do";
        }
    }

    
    /**
     * @param postfix
     * @return the type of the action
     */
    private int getActionType(String postfix) {
    	if (postfix == null || postfix.equals(""))
    		return -1;
    	if (postfix.equals(ACTION_POSTFIX))
         return PoConstants.ACTION_TYPE_ACTION;

    	if (postfix.equals(ACTIONCONFIG_POSTFIX))
            return PoConstants.ACTION_TYPE_CONFIG;
    	if (postfix.equals(PROCREF_POSTFIX))
            return PoConstants.ACTION_TYPE_PROCESS;
       	return -1;   
    }

    
    @Override
	public PoAction findActionWithFullName(String fullName) {
    	try {
	    	String postfix = fullName.substring(fullName.indexOf("."), fullName.length());
	    	String actionName = fullName.substring(0, fullName.indexOf("."));
	    	return findActionByNameAndType(actionName, getActionType(postfix));
    	} catch (Exception e) {
    		logger.error("Could not determine action or type of action " + fullName, e);
    		return null;
    	}
    }
    
    

	public void setFileService(PoFileService fileService) {
		this.fileService = fileService;
	}

	@Override
	public PoAction findActionByURL(String url) {
		
		if (url.indexOf("?")>-1)
			url = url.substring(0,url.indexOf("?"));
		
		
		if (url.indexOf(".")>-1) {
			String postfix = url.substring(url.indexOf("."));
			String name = url.substring(0,url.indexOf("."));
			int actiontype = -1;
	    	if (postfix.equals( ACTION_POSTFIX ))
	    		actiontype = PoConstants.ACTION_TYPE_ACTION;
	    	if (postfix.equals( ACTIONCONFIG_POSTFIX ))
	    		actiontype = PoConstants.ACTION_TYPE_CONFIG;
	    	if (postfix.equals( PROCREF_POSTFIX ))
	            actiontype = PoConstants.ACTION_TYPE_PROCESS;

	    	
	    	if (actiontype==-1) {
	    		if (logger.isDebugEnabled())
	    			logger.debug("Can't find action with postfix: " + postfix);
	    		return null;
	    	}
	    	return findActionByNameAndType(name, actiontype);
		} else 
			return null;
	}
		
	// Action Cache handling
	@Override
	public PoActionCache findActionCache(PoAction action, PoPerson person) {
		return actionCacheDAO.findActionCache(action, person);
	}

	@Override
	public PoActionCache getActionCache(String uid) {
		return actionCacheDAO.get(uid);
	}

	@Override
	public void saveActionCache(PoActionCache actionCache) {
		actionCacheDAO.save(actionCache);
	}

	public void setActionCacheDAO(PoActionCacheDAO actionCacheDAO) {
		this.actionCacheDAO = actionCacheDAO;
	}

	@Override
	public List<String> findInUseProcessDefinitions() {
		return actionDAO.findInUseProcessDefinitions();
	}

	@Override
	public String getActionURL(PoAction action) {
		return action.getName() + getActionPostfix(action);
	}


	/**
	 * Due to both way dependency this service must set itself on PoActionPermissionService 
	 * Spring is unable to handle both-way dependencies
	 */
	public void setPermissionService(PoActionPermissionService permissionService) {
		this.permissionService = permissionService;
		this.permissionService.setActionService(this);
	}


	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}


	public void setXpathTools(XPathTools xpathTools) {
		this.xpathTools = xpathTools;
	}

}
