/*
 * Created on 04.08.2005
 * @author hentner (Harald Entner)
 * 
 **/
package at.workflow.webdesk.po.daos;

import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoMenuItem;



/**
 * @author DI Harald Entner <br>
 *   	   logged in as: hentner<br><br>
 * 
 * Project:          webdesk3<br>
 * refactored at:    14.05.2007<br>
 * package:          at.workflow.webdesk.po.daos<br>
 * compilation unit: PoMenuDAO.java<br><br>
 *
 * Interface of the Menu Data Access Object. Use this in order
 * to retrieve Menu(-tree) data from the database. 
 */
public interface PoMenuDAO extends GenericDAO<PoMenuItem> {

    /**
     * @param name
     * @return
     */
    public PoMenuItem findMenuItemByName(String name);

    /**
     * @param client
     * @return
     */
    public List<PoMenuItem> findMenuItemsByClient(PoClient client);

    /**
     * @return
     */
    public List<PoMenuItem> findMenuItemsForAll();
    
    /**
     * @return
     */
    public List<PoMenuItem> findAllCurrentMenuItems();
    
    /**
     * Former hashasMenuTree(String clientId).
     * @param clientId
     * @return true if the PoClient object with clientId has a menutree attached.
     */
    public boolean hasMenuTree(String clientId);

    /**
     * @param sourceClientId
     * @return a List of PoMenuItems that are assigned to the given client
     * and have no parent. 
     */
    public List<PoMenuItem> findMenuRootOfClient(String sourceClientId);
    
	/**
	 * Returns a list of MenuItems with the given
	 * action assigned.
	 * 
	 * 
	 * @param myAction
	 * @return a List of PoMenuItem objects.
	 */
	public List<PoMenuItem> findMenuItemsWithAction(PoAction myAction);
	
	
	/**
	 * @param templateId
	 * @return a list of PoMenuItems objects which client-uids = null (=template-entries) wicht the given templateId
	 */
	public List<PoMenuItem> findTemplateMenuItemsByTemplateId(Integer templateId);
	
	
	/**
	 * @return as List of templateIds (group by over all template-menu-entries)
	 */
	public List<Object[]> findAllTemplateIds();
	
        

	/**
	 * @return the highest templateId or 0
	 */
	public int getMaxTemplateId();
	
	
	
	/**
	 * @param templateId
	 * @return a list of PoMenuItem objects-links to the given templateId  
	 */
	public List<PoMenuItem> findTemplateLinks(Integer templateId);
	
}