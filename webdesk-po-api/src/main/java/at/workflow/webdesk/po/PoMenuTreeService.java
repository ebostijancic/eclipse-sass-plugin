package at.workflow.webdesk.po;

import java.util.List;

import org.w3c.dom.Document;

import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoMenuItemActionLink;
import at.workflow.webdesk.po.model.PoMenuItemBase;
import at.workflow.webdesk.po.model.PoMenuItemFolder;
import at.workflow.webdesk.po.model.PoMenuItemTemplateLink;
import at.workflow.webdesk.po.model.PoMenuTreeBase;
import at.workflow.webdesk.po.model.PoMenuTreeClient;
import at.workflow.webdesk.po.model.PoMenuTreeTemplate;

/**
 * This service replaces the PoMenuService. It provides a cleaner model for interacting
 * with Trees, items and templates. Most methods are a semantic copy of the original PoMenuService.
 * 
 * @author ebostijancic
 * 
 */
public interface PoMenuTreeService {

	/** retrieves the displayed webdesk menutree for the specified user as XML document. 
	 * The structure of the XML document is a tree of 'tree' tags containing all information to
	 * build actionlinks or folders. */
	public Document getMenuTreeOfPerson(String userId);

	public PoMenuItemBase getMenuItem(String uid);

	public PoMenuItemActionLink getMenuItemActionLink(String uid);

	public PoMenuItemFolder getMenuItemFolder(String uid);

	public PoMenuItemTemplateLink getMenuItemTemplateLink(String uid);

	public List<PoMenuTreeTemplate> loadAllTreeTemplates();

	public PoMenuTreeClient findMenuTreeByClient(PoClient client);

	public PoMenuTreeTemplate getTreeTemplate(String treeTemplateUid);

	public List<PoMenuItemTemplateLink> findTemplateLinks(PoMenuTreeTemplate treeTemplate);

	public void clearMenuCache();

	/** saves a new menu item, which can be of type PoMenuItemActionLink or PoMenuItemFolder */
	public void saveMenuItem(PoMenuItemBase menuItem);

	/** deletes a menu item which can be of type @{link PoMenuItemActionLink} or {@link PoMenuItemFolder} */
	public void deleteMenuItem(PoMenuItemBase menuItem);

	/** saves a new menu tree which can be of type {@link PoMenuTreeClient} or {@link PoMenuTreeTemplate} */
	public void saveMenuTree(PoMenuTreeBase tree);

	/** delete a menu tree which can be of type {@link PoMenuTreeClient} or {@link PoMenuTreeTemplate} */
	public void deleteMenuTree(PoMenuTreeBase tree);

	/** moves an item inside a list one position up */
	public void moveUp(PoMenuItemFolder folder, PoMenuItemBase item);

	/** moves an item inside a list one position down */
	public void moveDown(PoMenuItemFolder folder, PoMenuItemBase item);

	/** moves an item to the top of the list (first in list) */
	public void moveToTop(PoMenuItemFolder folder, PoMenuItemBase item);

	/** moves an item to the bottom of the list (last in list) */
	public void moveToBottom(PoMenuItemFolder folder, PoMenuItemBase item);

	/**
	 * moves item after another item in the position.
	 * @param item Item to move.
	 * @param after The item to be placed after.
	 */
	public void moveAfter(PoMenuItemBase item, PoMenuItemBase after);

	/**
	 * see moveAfter but item is placed before.
	 * @param item
	 * @param before
	 */
	public void moveBefore(PoMenuItemBase item, PoMenuItemBase before);

	/** finds the first MenuActionLink corresponding to the passed fullActionName (f.i. po_showClients.act),
	 * @returns found MenuItemActionLink or null if none could be found */
	public PoMenuItemActionLink findMenuItemActionLinkByActionName(PoMenuTreeBase tree, String fullActionName);

	public void refreshTextModulesOfFolders(PoClient client);

	public String getMenuFolderTextModuleKey(PoMenuItemFolder folder);
}