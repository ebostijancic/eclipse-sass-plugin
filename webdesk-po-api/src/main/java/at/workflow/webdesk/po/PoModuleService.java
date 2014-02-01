/*
 * Created on 01.02.2006
 * @author hentner (Harald Entner)
 * 
 **/
package at.workflow.webdesk.po;

import java.util.List;

import at.workflow.webdesk.po.model.PoModule;

/**
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          webdesk3<br>
 * created at:       16.08.2006<br>
 * package:          at.workflow.webdesk.po<br>
 * compilation unit: PoModuleService.java<br><br>
 *
 *<p>
 * Can read and store <code>PoModule</code> objects.
 *</p>
 *
 *
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


 */
public interface PoModuleService {

	
	/**
	 * load Module
	 * @return PoModule
	 */
	public PoModule getModule(String uid);
	
  
    /**
     * @param moduleName  the name of the module
     * @return a PoModule with the given name, null otherwise.
     */
    public PoModule getModuleByName(String moduleName);
    
    /**
     * Persists the given PoModule object.
     * 
     * @param module
     */
    public void saveModule(PoModule module);
    
	/**
	 * This function can be used determine a corresponding 
	 * <code>PoModule</code>, which is returned  
	 * if it is possible to extract the module key from 
	 * <code>className</code>, which should have the following 
	 * form: "*\/at/workflow/webdesk/PCKNAME/*", where * can be anything
	 * 
	 * @param className
	 * @return a <code>PoModule</code> if it was able to determine it 
	 * 
	 */
    public PoModule tryToExtractModuleFromClassName(String className);
    
    /**
     * This function can be used determine a corresponding 
     * <code>PoModule</code>, which is returned  
     * if it is possible to extract the module key from 
     * <code>beanName</code>, which should have the following 
     * form: "<ModulePrefix>*", where * can be anything
     * 
     * @param className
     * @return a <code>PoModule</code> if it was able to determine it 
     * 
     */
    public PoModule tryToExtractModuleFromBeanName(String beanName);
    
    /**
     * returns a List of all PoModule, including ones which
     * are not active anylonger...
     */
    public List<PoModule>loadAllModules();

    /**
     * returns a list of the currently active Modules
     */
    public List<PoModule>loadActiveModules();
    
    

}
