/*
 * Created on 20.06.2005
 * @author hentner (Harald Entner)
 * 
 **/
package at.workflow.webdesk.po.daos;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoLanguage;
import at.workflow.webdesk.po.model.PoTextModule;

/**
 * @author hentner
 *
 */
public interface PoTextModuleDAO extends GenericDAO<PoTextModule> {
	
	/**
	 * @param parent
	 * @return a list of text modules that are directly linked with the parent text module.
	 * Only childs that are valid at the given date will be included.
	 */
	public List<PoTextModule> findChildTextModules(PoTextModule parent,Date date);
	
	
	/**
	 * @param child
	 * @return the parent of the text module. Both modules should be valid
	 * at the given date.
	 */
	public PoTextModule getParentTextModule(PoTextModule child,Date date);
	
	
	/**
	 * finds All valid textmodule for the given language. Will not returned textModules
	 * of a detached module.
	 * 
	 * @param language
	 * @return List of valid PoTextModules
	 */
	public List<PoTextModule> findTextModules(PoLanguage language);

	/**
	 * @param name
	 * @return the corresponding textModule, whereas the name
	 * is used as the key. 
	 */
	public List<PoTextModule> findTextModuleByName(String name);
	
	
	/**
	 * @param action
	 * @return a list of PoTextModule objects with the givne <code>action</code> <br>
	 * assigned and the given <code>languageCode</code>.
	 */
	public List<PoTextModule> findTextModulesForAction(PoAction action,String languageCode);
	

	/**
	 * @param name the key of the <code>PoTextModule</code>
	 * @param lang the <code>PoLanguage</code> object wich is linked with the <code>PoTextModule</code> 
	 * @return a <code>PoTextModule</code> or null if none was found
	 */
    public PoTextModule findTextModuleByNameAndLanguage(String name, PoLanguage lang);
    
    /**
     * gets PoTextModule for given Name and Language, returns null if none was found.
     * @param name -> key of Textmodule to be found
     * @param lang -> PoLanguage Object of corresponding Language
     * @return
     */
    public List<PoTextModule> findTextModuleLikeNameAndLanguage(String actionFolder, String name, PoLanguage lang);
    
    /**
     * finds all common textmodules (not linked to an action)of a specified language, 
     * which are currently valid (meaning they are not linked to a detached module)
     * 
     * @param language
     * @return List of PoTextModule Objects
     */
    public List<PoTextModule> findCommonTextModules(PoLanguage language);
    
    
    /**
     * @param textModule
     * @return the Parent PoTextModule of the given PoTextModule 
     */
    public PoTextModule findParentTextModule(PoTextModule textModule);

	/**
	 * @param name the key of the <code>PoTextModule</code>
	 * @param uid the <code>uid</code> of the <code>PoLanguage</code> object which is linked with the <code>PoTextModule</code> 
	 * @return a <code>PoTextModule</code> or null if none was found
	 */
	public PoTextModule findTextModuleByNameAndLanguage(String name, String uid);
	
    
    /**
     * returns list of all Textmodules for the current standard (=default)
     * language, which are currently valid and not belonging to a detached module.
     * 
     * @return List of all Textmodules (PoTextModule)
     */
    public List<PoTextModule> findStandardTextModules();
    
    /**
     * sets Flag allowUpdateOnVersionChange on every PoTextModule to true
     */
    public void allowUpdateOnVersionChangeForAllTextModules();
    
    /**
     * sets Flag allowUpdateOnVersionChange on every PoTextModule to false
     */
    public void disallowUpdateOnVersionChangeForAllTextModules();

	/**
	 * @param myLang the <code>PoLanguage</code> of the <code>PoTextModule</code>'s.
	 * @param dateOfCreation the <code>Date</code> after which the <code>PoTextModules</code> should have been created.
	 * 
	 * @return a <code>List</code> of <code>PoTextModule</code> objects which has been 
	 * created after the <code>dateOfCreation</code>. Also returns textModules linked
	 * to a detached module <Code>PoModule</code>
	 */
	public List<PoTextModule> findTextModules(PoLanguage myLang, Date dateOfCreation);
	
}
