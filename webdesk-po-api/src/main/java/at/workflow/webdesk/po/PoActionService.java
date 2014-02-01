/*
 * Created on 01.07.2005
 * @author hentner (Harald Entner)
 * 
 **/
package at.workflow.webdesk.po;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoActionCache;
import at.workflow.webdesk.po.model.PoActionParameter;
import at.workflow.webdesk.po.model.PoContextParameter;
import at.workflow.webdesk.po.model.PoFile;
import at.workflow.webdesk.po.model.PoLanguage;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoTextModule;


/**
 *<p>
 * This class provides the Interface to the Business Logic 
 * of <code>PoAction</code> objects.  
 * </p>
 * <p>In more detail the following functionallity has to be provided when
 * implementing this interface.</p>
 * <ul>
 * 	<li>action itself</li>
 * 	<li>action caches</li>
 * 	<li>registration of actions</li>
 * </ul>
 * 
 * 
 * @see at.workflow.webdesk.po.PoOrganisationService
 * @see at.workflow.webdesk.po.PoRegistrationService
 * @see at.workflow.webdesk.po.PoModuleService
 * @see at.workflow.webdesk.po.PoFileService
 * @see at.workflow.webdesk.po.PoLogService
 * @see at.workflow.webdesk.po.PoLanguageService
 * @see at.workflow.webdesk.po.PoBeanPropertyService
 * @see at.workflow.webdesk.po.PoJobService
 * 
 * @author hentner, ggruber
 */
public interface PoActionService {
	
	
	/**
	 * @param uid
	 * @return the PoAction with the given UID, null otherwise
	 */
	public PoAction getAction(String uid);
	  
    /**
     * @param action
     *  
     * Persists the given <code>PoAction</code>
     */
    public void saveAction(PoAction action); 
    
    
    /**
     * @param fullName the name of the action. eg. 'po_showPersons.act'
     * @return a <code>PoAction</code> or <code>null</code>.
     */
    public PoAction findActionWithFullName(String fullName);
    
    /**
     * @param action
     * @param date
     * 
     * deletes the action at the passed date (internally only the 
     * validto field is set to the date. Data still remains in the 
     * database
     */
    public void deleteAction(PoAction action, Date date);
    
    
    /**
     * @param action
     * 
     * deletes the action from the database as well as all
     * relations that depends on the PoAction object.
     */
    public void deleteAndFlushAction(PoAction action);
    
    /**
     * tries to 'estimate' the right configuration for the given person
     * and the given action. Iterates over all configs and checks
     * for permissions of the current person for the different configs.
     * A special order while searching is taken (see below!)
     * 
     * @param person
     * @param action
     * @return the correct configured action.<br><br>
     * 
     * Search Order:<br>
     * <br>
     * 1 ... Configs from him-/her-self.<br>
     * 2 ... Configs from his/her group/parentgroup.<br>
     * 3 ... Configs from his/her role.<br>
     * 4 ... Configs from his/her client.<br> 
     */
    public PoAction getConfigFromAction(PoPerson person, PoAction action);

    
    /**
     * Find the best Config for the specified Action, which should be called
     * by the given Person for the given targetPersonUid (meaning the 
     * User whose Data is called).
     * Can actually only return a Config where the person is allowed to "edit" 
     * (retrieve personal data of)
     * the Person with the targetUserUid and the "owner" of the information itself
     * has access to the data. (meaning the correct config would be returned!)
     * 
     * @param person: current User
     * @param action: action to be called
     * @param targetUserUid: Uid of the Person (or group) whose data should called 
     * @return the correct configured action.<br><br>
     * 
     * Order:<br>
     * <br>
     * 1 ... Configs from him-/her-self.<br>
     * 2 ... Configs from his/her group/parentgroup.<br>
     * 3 ... Configs from his/her role.<br>
     * 4 ... Configs from his/her client.<br> 
     * 
     */
    public PoAction getConfigFromAction(PoPerson person, PoAction action, String targetUserUid);
    
    /**
     * same as method getConfigFromAction(PoPerson person, PoAction action, String targetUserUid)
     * but with the difference that no check is done, if the target Person has actually 
     * permission to open a potential derived config.
     * 
     * @return a matching config (PoAction object)
     */
    public PoAction getConfigFromActionWithoutTargetPermCheck(PoPerson person, PoAction action, String targetPersonUid);
	

	/**
	 * @param name
	 * @return a <code>PoAction</code> object, if an action with the given <code>name</code> and 
	 * <code>type</code> was found. <code>null</code> otherwise.
	 */
    public PoAction findActionByNameAndType(String name, int type); 
	
    /**
     * @param url the name of the action, something like "po_showPersons.act", a resources package name.
     * @return a <code>PoAction</code> if one with the given name exists.
     */
    public PoAction findActionByURL(String url);
    
    
    /**
     * @param person
     * @param date
     * 
     * @return returns all actions that are assigned DIRECTLY to the given person
     * via a PoAPermissionPerson 
     */
    public List<PoAction> findActionsOfPerson(PoPerson person, Date date);
    
    /**
     * @param role
     * @param date
     * @return a list of actions, that are or become valid after the given date 
     * and that are assigned to the given role.
     */
    public List<PoAction> findActionsOfRole(PoRole role, Date date);
    
    
    
    
	 /**
	 * @param action
	 * @return a list of configurated actions inherited from the given <code>action</code>.
	 */
	public List<PoAction> findConfigsFromAction(PoAction action);
	
    
    /**
     * @return a list of PoAction Configs
     */
    public List<PoAction> findConfigs();
	
    /**
     * loads all current active Configs
     * @return List of PoActions
     */
    public List<PoAction> findAllCurrentConfigs();
	
	/**
	 * @return list of all Actions
	 */
	public List<PoAction> loadAllActions();
	
	/**
	 * @param date
	 * @return all PoAction objects that are valid at the given date.
	 */
	public List<PoAction> findAllActions(Date date);
	
	/**
	 * @return list of all ImageSets
	 */
	public List<String> loadAllImageSets();
	
	/**
	 * @TODO outsource to webclient-helper
	 * 
	 * @param imageSet
	 * @return list of Images from the given ImageSet
	 */
	public List<String> loadImagesFromImageSet(String imageSet);
	
	

    /**
     * @param action
     * @return a list of PoFile objects, that are assigned to the given action.
     */
    public List<PoFile> findFilesFromAction(PoAction action);
        
    
    /**
     * This function is used to load the flowscript files from within the 
     * classpath by the thin Flowscript layer in ExecActions.js
     * 
     * 
     * @param functionName
     * @return the location of the js file inside the classpath.
     */
    public String getLocationOfJs(String functionName);
    
    
    /**
     * @param procDefId
     * @return a list of Actions with corresponding procDefId
     */
    public List<PoAction> findActionByProcessDefId(String procDefId);
    
    
    /**
     * loads all current active Actions
     * @return List of PoActions
     */
    public List<PoAction> loadAllCurrentActions();

    /**
     * Creates 2 TextModules (Caption and Description) for each Language with i18n-key 
     * module_actionName_action_caption and module_actionName_action_description.
     * If a TextModule alredy exists, it has no effect. 
     * @param action
     */
    public void createPrimaryTextModulesOfAction(PoAction action);
    
    /**
     * Creates 2 TextModules (Caption and Description) for each Language with i18n-key 
     * module_actionName_action_caption and module_actionName_action_description.
     * If a TextModule alredy exists and updateOnVersionChange is false, it has no effect.
     * if updateOnVersionChange is true the creation is enforced! 
     * @param action
     * @param updateOnVersionChange  
     */
	public void createPrimaryTextModulesOfAction(PoAction newConfig, boolean updateOnVersionChange);
    
    
    /**
     * Checks if it is valid to store the given action in the database.
     * Throws a PoRuntimeException if a unique constraint is violated.
     * 
     * @param action
     */
    public void checkAction(PoAction action);
    
    
    /**
     * @param action
     * @param attribute
     * @param language
     * @return a PoTextModule that exists for every Action. The Parameter attribute can be 'caption' or 'description'.
     */
    public PoTextModule getPrimaryTextModuleOfAction(PoAction action, String attribute, PoLanguage language);
    
    /**
     * Retrieves action parameters of action.
     * For actions the registered parameters declared in act-descr.xml are retrieved.
     * For processes the processvariables are get.
     * @param action
     * @return collection of PoActionParameters
     */
	public Collection<PoActionParameter> getActionParameters(PoAction action);
	
	/**
     * Retrieves context parameters offered by action.
     * For actions the registered parameters declared in act-descr.xml are retrieved.
     * For processes the processvariables are get.
     * @param action
     * @return collection of PoActionParameters
	 */
	public Collection<PoContextParameter> getActionContextParameter(PoAction action);
    
    /**
     * This function registers (saves to db) the corresponding function of the <br>
     * given action-descriptor. If the action is already present, the softvalues <br>
     * defined in the po-applicationContext.xml file are only overwritten if <br>
     * allowUpdateOnVersionChange is set to true.<br>
     * 
     * @param actionDescr act-desr.xml (Description of the given action)<br>
     * Allowed object types are java.util.File or InputStream
     * @param path the path to the given resource
     * @param folderOfPackage the folder of the package (eg. ta, po, wf, ...) 
     */
    public void registerAction(Object actionDescr, String path, String folderOfPackage);
    
    public void registerAction(Object actionDescr, String path, String folderOfPackage, Map<String, String> actionCache);
    
    /**
     * transfers the Properties of the PoAction described as softvalues in 
     * the applicationContext from the newConfig to the oldConfig
     * 
     * @param newConfig PoAction Object where softvalues are extracted
     * @param oldConfig PoAction Object where softvalues should be set
     * @return oldConfig PoAction filled with the softvalues of the newConfig 
     */
    public PoAction setSoftValuesOfAction(PoAction newConfig, PoAction oldConfig);
    
    /**
     * @param path
     * @param folderOfPackage
     * @return a PoAction object, if an action with the given path inside the corresponding
     * folder is found.
     */
    public PoAction getActionByPath(String path,String folderOfPackage);
    
    /**
     * @param is InputStream of the config file.
     * @param path is the path of the config file
     * @return an PoAction Object created of the ActionDescriptor supplied
     * in the Inputstream given as parameter
     */
    public PoAction getActionFromConfigFile(InputStream is, String path);
    
    /**
     * replaces old PoAction Object with new one and saves new Object to database
     * must be used, when both objects are present and calling evict is 
     * necessary due to hibernate limitations
     * 
     * @param oldConfig
     * @param newConfig
     */
    public void replaceAction(PoAction oldConfig, PoAction newConfig);
    
    public List<String> findActionNamesOfModule(String name);
    
    
    /**
     * Returns the PostFix of the action.
     * <br><br>
     * e.g. ".act", ".cact", ".proc", ...
     * 
     * @param action
     * @return the postfix of the action
     */
    public String getActionPostfix(PoAction action);
    
 
    /**
     * @param uid the uid of the <code>PoActionCache</code> object.
     * @return a <code>PoActionCache</code> object
     */
    public PoActionCache getActionCache(String uid);
    
    /**
     * @param action a <code>PoAction</code> object which is linked with the returned <code>PoActionCache</code>.
     * @param person a <code>PoPerson</code> object which is linked with the returned <code>PoActionCache</code>.
     * @return a <code>PoActionCache</code> object
     * if one was found, null otherwise 
     */
    public PoActionCache findActionCache(PoAction action, PoPerson person);
    
    /**
     * Persists the given <code>PoActionCache</code> object.
     * @param actionCache
     */
    public void saveActionCache(PoActionCache actionCache);
    
    
    /**
     * @return a <code>List</code> of <code>String</code>'s
     */
    public List<String> findInUseProcessDefinitions();
    
    
    /**
     * @param action
     * @return the <code>URL</code> of the given <code>action</code>. 
     */
    public String getActionURL(PoAction action);
}
