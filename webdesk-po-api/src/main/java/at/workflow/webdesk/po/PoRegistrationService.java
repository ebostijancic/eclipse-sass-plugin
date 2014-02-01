package at.workflow.webdesk.po;

import java.util.List;

import org.springframework.core.io.Resource;

import at.workflow.webdesk.po.model.PoModule;

/**
 * This class is responsible to
 * <ul>
 * 	<li> register Actions </li>
 *  <li> register Configs </li>
 *  <li> register Textmodules </li>
 *  <li> register Jobs </li>
 *  <li> register Job-Configs </li>
 *  <li> register Update Scripts</li>	
 *  <li> register Module Flowscripts </li>
 *  </ul>
 *  
 * Depending on which property is set in <code>webdesk.properties</code> this runs full registration
 * or registration without TextModules.
 * 
 * The list that has to be passed as a parameter contains the different modules that should
 * be registered.
 * 
 * At end the function runRegistrationOfPackage is called.  
 *
 * @see at.workflow.webdesk.po.PoActionService
 * @see at.workflow.webdesk.po.PoOrganisationService
 * @see at.workflow.webdesk.po.PoRegistrationService
 * @see at.workflow.webdesk.po.PoModuleService
 * @see at.workflow.webdesk.po.PoFileService
 * @see at.workflow.webdesk.po.PoLogService
 * @see at.workflow.webdesk.po.PoLanguageService
 * @see at.workflow.webdesk.po.PoBeanPropertyService
 * @see at.workflow.webdesk.po.PoJobService
 *
 * Project:          webdesk3<br>
 * created at:       28.03.2007<br>
 * @author DI Harald Entner (hentner)
 */
public interface PoRegistrationService {
    
	/** The name of the property in webdesk.properties that contains the run-registration criterion. */ 
	public static final String WEBDESK_RUN_REGISTRATION_PROPERTY = "webdesk.runRegistration";
	
	public enum RunRegistrationValue { 
		/** Possible value for run-registration that would not run registration ever. */
		NEVER(0),
		
		/** Possible value for run-registration that once runs registration and resets the flag to "0" then. */
		ONCE_THEN_RESET(1),
		
		/** Possible value for run-registration that would run full registration always. */
		ALWAYS(2),
		
		/** Possible value for run-registration that would run registration always, excluding text-modules. */
		ALWAYS_EXCLUDE_TEXTMODULES(3);
		
		/** @return the according enum for passed code, or ONCE_THEN_RESET (default) when not found. */
		public static RunRegistrationValue byCode(String code) {
			for (RunRegistrationValue regValue : RunRegistrationValue.values()) {
				if (code.equals(regValue.toString())) {
					return regValue;
				}
			}
			
			// no valid code is found
			return RunRegistrationValue.ONCE_THEN_RESET;
		}
		
	 	private int code;
	 	
	 	private RunRegistrationValue(int c) {
	 		this.code = c; 
	 	} 
		public int getCode() {   
			return code; 
		}
		@Override
		public String toString() {   
			return Integer.valueOf(code).toString(); 
		}
	}
	

    /**
     * creates PoActions in DB.
     * if PoAction is found in DB -->
     * do not overwrite certain Properties (softvalues in PoActionDAOImpl)
     * 
     * @param List of actionDescriptors (XML Files)
     * @param folderOfPackage the folder of the package (eg. po,ta,wf, ..) 
     */
    public void registerActions(Resource[] actionDescriptors, String folderOfPackage);
    
    
    /**
     * creates PoJob objects in DB.
     * if a corresponding trigger is found (identified by its name), the <br>
     * newly inserted Job is associated with this trigger. (More than one trigger <br>
     * can be associated with the job).
     * 
     * 
     * @param jobDescriptors a list of resources (job-descr.xml files)
     * @param folderOfPackage a string containing the package name (e.g. po,ta,wf,...)
     */
    public void registerJobs(Resource[] jobDescriptors, String folderOfPackage);
    
    
    /**
     * reads all textModule Files from the given Resources, reads the textmodules into memory and
     * registers every textmodule with the corresponding action and language as a PoTextModule Object
     * 
     * convention for filenames:
     * actionName/i18n_language.xml
     * 
     * from the directory the poaction can be extracted, the name of the file indicates the language
     * the real languagecode however is extracted from the xml file!
     * 
     * @param textModules
     */
    public void syncTextModules(Resource[] textModules);
    
    
    
    /**
     * creates PoActions for every Config found 
     * if Config is already in database, checks if Property UpdateOnVersionChange==true
     * if yes -> do update, if no -> do nothing
     * in case of update: check first, if referenced parent Action is found, 
     * if not, no config will be registered
     * do not overwrite certain Properties (softvalues in PoActionDAOImpl)
     * 
     * Also writes the corresponding config-File into the database.
     * 
     * @param configs
     */
    public void registerConfigs(Resource[] configs);
    
    /**
     * runs Registration for all passed Modules 
     * it is assumed, that for every module a PoRegistrationBean_xx Spring bean
     * exists in the applicationContext. this is then looked up and the init() 
     * method is called to register all the modules there!
     * 
     * @param modules: List of Strings which are the names of the modules to register
     */

    public void runRegistration(List<String> modules, boolean ignoreIniCache);
    
    /**
     * same as runRegistrationIfPossible, except that the sync of 
     * textmodules is skipped
     * 
     * @param modules: List of Strings which are the names of the modules to register
     */
    
    public void runRegistrationWithOutTextModules(List<String> modules);
    
    
    /**
     * runs Registration of Actions and Modules Flowscripts of passed List of
     * Modules. Does *NOT* sync the textmodules (neither jobs nor configs nor updatescripts)
     * 
     * @param modules: List of Strings which are the names of the modules to register
     */
    public void runRegistrationOfActionsAndFlowscripts(List<String> modules);
    /**
     * registers the given job configs.
     * 
     * @param ress
     */
    public void registerJobConfigs(Resource[] ress);
	
	/**
	 * @param pattern a path
	 * @return Eventually the path changes, if it is not a well-formed path.
	 */
	public String appendRealPathIfNecessary(String pattern);
	

    /**
     * <p>
     * For each <code>connector-descr.xml</code> file, a <code>PoConnector</code> object is created.
     * With the aid of the <code>connector-descr.xml</code> file, sufficient information is supplied in 
     * order to create such an instance.
     * </p>
     *<p>The <code>PoConnector</code> is not only created, but also persisted.
     *
     * </p>
     *
     * 
     * 
     * 
     * @param ress a <code>Array</code> of <code>Resource</code> objects. In other words the <code>connector-descr.xml</code> Files.
     * @param module a <code>PoModule</code> object.
     */
    public void registerConnectors(Resource[] ress, PoModule module);
}



