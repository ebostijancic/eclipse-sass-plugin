package at.workflow.webdesk.po;

import java.util.List;

import org.w3c.dom.Document;

import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoLanguage;
import at.workflow.webdesk.po.model.PoMenuItem;

/**
 * <p>
 * This service provides functions to handle a <code>MenuTree</code> stored in
 * <code>PoMenuStructure</code> objects. Add, remove, update, copy, find, just read
 * through the javadocs to get more information on what can be done. 
 * </p>

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
 */
public interface PoMenuService {

	/**
	 * same as {{@link #getMenuTreeOfPerson(String, PoLanguage)} but without need to specify language
	 */
	public Document getMenuTreeOfPerson( String userId);
	
	/**
	 * Retrieve the menu tree of the person in XML format containing
	 * nested 'tree' elements. 
	 * @param userId is the username of the person
	 * @param language is the current set Language, if null the default language will be used.
	 * @return a DOM document containing the menutree as XML.
	 */
    public Document getMenuTreeOfPerson( String userId, PoLanguage language);

    /**
     * @param uid
     * @return a <code>PoMenuItem</code> object.
     */
    public PoMenuItem getMenuItem(String uid);
    
    /**
     * @return a list of <code>Integer</code>-objects templateIds (for each template one templatId will be returned = grouping over all templateIds)  
     */
    public List<Object[]> getAllTemplateIds();

    /**
     * @return the highest templatedId from all templates 
     */
    public int getMaxTemplateId();
    
    /**
     * @param client
     * @return list of PoMenuItems for the given client sorted by ranking
     */
    public List<PoMenuItem> findMenuItemsByClient(PoClient client);
    
    /**
     * @param TemplateId
     * @return a list of PoMenuItem for the given template (templateId)
     */
    public List<PoMenuItem> findTemplateMenuItemsByTemplateId(Integer TemplateId);
    
    /**
     * @param templateId
     * @return a list of PoMenuItems then links to the given template (templateId) 
     */
    public List<PoMenuItem> findTemplateLinks(Integer templateId);
    
    public void clearMenuCache();

    /**
     * Deletes the given MenuItem from the database.
     */
    public void deleteMenuItem(PoMenuItem mi);
    
    /**
     * Saves the menu item
     */
    public void saveMenuItem(PoMenuItem mi);
    

    public PoMenuItem findMenuItemByName(String name);

	/**
	 * refreshes folder textmodules for the given client
	 */
	public void refreshTextModulesOfFolders(PoClient client);
	
	/**
	 * returns the i18n-key used for the passed MenuItem
	 * This key can be persisted as attribute inside the item itself
	 * or will be newly created by using the items linked client or
	 * FolderTemplate Id
	 * 
	 * @param item
	 * @return String
	 */
	public String getMenuFolderTextModuleKey(PoMenuItem item);

}