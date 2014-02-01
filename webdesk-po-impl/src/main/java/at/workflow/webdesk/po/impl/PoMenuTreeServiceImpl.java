package at.workflow.webdesk.po.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.output.DOMOutputter;

import at.workflow.webdesk.po.PoActionPermissionService;
import at.workflow.webdesk.po.PoActionService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoMenuTreeService;
import at.workflow.webdesk.po.daos.PoPersonDAO;
import at.workflow.webdesk.po.impl.daos.PoMenuItemActionLinkDAOImpl;
import at.workflow.webdesk.po.impl.daos.PoMenuItemBaseDAOImpl;
import at.workflow.webdesk.po.impl.daos.PoMenuItemFolderDAOImpl;
import at.workflow.webdesk.po.impl.daos.PoMenuItemTemplateLinkDAOImpl;
import at.workflow.webdesk.po.impl.daos.PoMenuTreeBaseDAOImpl;
import at.workflow.webdesk.po.impl.daos.PoMenuTreeClientDAOImpl;
import at.workflow.webdesk.po.impl.daos.PoMenuTreeTemplateDAOImpl;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoMenuItemActionLink;
import at.workflow.webdesk.po.model.PoMenuItemBase;
import at.workflow.webdesk.po.model.PoMenuItemFolder;
import at.workflow.webdesk.po.model.PoMenuItemTemplateLink;
import at.workflow.webdesk.po.model.PoMenuTreeBase;
import at.workflow.webdesk.po.model.PoMenuTreeClient;
import at.workflow.webdesk.po.model.PoMenuTreeTemplate;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.tools.date.DateTools;

public class PoMenuTreeServiceImpl implements PoMenuTreeService {

	protected final Logger logger = Logger.getLogger(this.getClass());

	private PoMenuItemBaseDAOImpl menuItemBaseDAO;
	private PoMenuItemActionLinkDAOImpl menuItemActionLinkDAO;
	private PoMenuItemFolderDAOImpl menuItemFolderDAO;
	private PoMenuItemTemplateLinkDAOImpl menuItemTemplateLinkDAO;

	private PoMenuTreeBaseDAOImpl menuTreeBaseDAO;
	private PoMenuTreeClientDAOImpl menuTreeClientDAO;
	private PoMenuTreeTemplateDAOImpl menuTreeTemplateDAO;

	private PoPersonDAO personDAO;
	private PoActionPermissionService permissionService;
	private PoActionService actionService;
	private CacheManager cacheManager;

	public static final String ELEMENT_NAME = "tree";

	@Override
	public org.w3c.dom.Document getMenuTreeOfPerson(String userId) {
		PoPerson person = personDAO.findPersonByUserName(userId);
		PoClient personsClient = person.getClient();
		if (personsClient != null) {
			final PoMenuTreeClient clientsTree = menuTreeClientDAO.findMenuTreeForClient(personsClient);
			final List<PoAction> actions = permissionService.findAllActionsOfPerson(person, DateTools.nowWithHourPrecision());
			try {
				return getXMLDocument(clientsTree, actions);
			}
			catch (JDOMException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	private org.w3c.dom.Document getXMLDocument(PoMenuTreeClient clientTree, List<PoAction> actions) throws JDOMException {
		Element root = new Element(ELEMENT_NAME);
		Document doc = new Document(root);

		// get all top level items and add them to the document.
		for (PoMenuItemBase item : clientTree.getTopLevelItems()) {
			Element elementFromItem = getElementFromItem(item, actions);
			if (elementFromItem != null) {
				root.addContent(getElementFromItem(item, actions));
			}
		}

		/**
		 * this is always appended to the end of the tree, 
		 * as it's should be always possible to logout
		 */
		Element logoutElement = new Element(ELEMENT_NAME);
		root.addContent(logoutElement);
		logoutElement.setAttribute("text", "Logout");
		logoutElement.setAttribute("action", "logout");
		logoutElement.setAttribute("image", "./images/nuvola/16x16/actions/exit.png");

		DOMOutputter outputter = new DOMOutputter();

		return outputter.output(doc);
	}

	private Element getElementFromMenuItemActionLink(PoMenuItemActionLink actionLink, List<PoAction> permittedActions) {
		PoAction action = actionLink.getAction();

		if (permittedActions.contains(action) == false) {
			return null;
		}

		Element actionElement = new Element(ELEMENT_NAME);
		String postFix = actionService.getActionPostfix(action);

		if (action.getDescription() != null) {
			actionElement.setAttribute("title", action.getName() + postFix + "_action_description");
		}

		actionElement.setAttribute("id", actionLink.getAction().getUID());
		actionElement.setAttribute("action", actionLink.getAction().getName());
		actionElement.setAttribute("text", action.getName() + postFix + "_action_caption");
		actionElement.setAttribute("newWindow", action.isAlwaysOpenInNewWindow() ? "true" : "false");
		actionElement.setAttribute("layout", action.getLayoutTemplateToUse() == null ? "" : action.getLayoutTemplateToUse());
		actionElement.setAttribute("attr", "text", Namespace.getNamespace("i18n", "http://apache.org/cocoon/i18n/2.1"));

		if (actionLink.getAction().getImage() != null) {
			actionElement.setAttribute("image", buildImagePath(action, "16x16"));
		}
		return actionElement;
	}

	/**
	 * method used to build the image path.
	 * 
	 * @param action action to build the image path
	 * @param size size in pixels 16x16, 32x32, ..
	 * @return string of the path
	 */
	private String buildImagePath(PoAction action, String size) {

		return "./images" + action.getImageSet() + "/" + size + "/" + action.getImage();
	}

	private Element getElementFromMenuItemFolder(PoMenuItemFolder folder) {
		Element folderElement = new Element(ELEMENT_NAME);
		int folderCount = 0;
		folderElement.setAttribute("id", folder.getUID());

		if (folder.getI18nKey() != null) {
			folderElement.setAttribute("text", folder.getI18nKey());
			folderElement.setAttribute("attr", "text", Namespace.getNamespace("i18n", "http://apache.org/cocoon/i18n/2.1"));
		}

		for (PoMenuItemBase item : folder.getChilds()) {
			Element elementFromItem = getElementFromItem(item, new ArrayList<PoAction>());
			if (elementFromItem != null) {
				folderElement.addContent(elementFromItem);
			}

			if (item instanceof PoMenuItemFolder) {
				folderCount++;
			}
		}

		folderElement.setAttribute("foldercount", new Integer(folderCount).toString());
		return folderElement;
	}

	private Element getElementFromItem(PoMenuItemBase item, List<PoAction> permittedActions) {
		if (item instanceof PoMenuItemActionLink) {
			return getElementFromMenuItemActionLink((PoMenuItemActionLink) item, permittedActions);
		} else if (item instanceof PoMenuItemFolder) {
			return getElementFromMenuItemFolder((PoMenuItemFolder) item);
		} else if (item instanceof PoMenuItemTemplateLink) {
			return getElementFromMenuItemTemplateLink((PoMenuItemTemplateLink) item, permittedActions);
		} else {
			throw new IllegalStateException("found unknow item with class: " + item.getClass());
		}
	}

	private Element getElementFromMenuItemTemplateLink(PoMenuItemTemplateLink link, List<PoAction> permittedActions) {
		Element linkElement = new Element("tree");
		PoMenuTreeTemplate template = link.getTreeTemplate();
		for (PoMenuItemBase topLevelItem : template.getTopLevelItems()) {
			linkElement.addContent(getElementFromItem(topLevelItem, permittedActions));
		}
		return linkElement;
	}

	@Override
	public List<PoMenuTreeTemplate> loadAllTreeTemplates() {
		return menuTreeTemplateDAO.loadAll();
	}

	@Override
	public PoMenuTreeTemplate getTreeTemplate(String treeTemplateUid) {
		return menuTreeTemplateDAO.get(treeTemplateUid);
	}

	@Override
	public List<PoMenuItemTemplateLink> findTemplateLinks(PoMenuTreeTemplate treeTemplate) {
		return menuItemTemplateLinkDAO.findAllMenuItemTemplateLinks(treeTemplate);
	}

	@Override
	public void clearMenuCache() {
		Cache cache = cacheManager.getCache(PoConstants.MENUCACHE);
		try {
			cache.removeAll();
		}
		catch (Exception e) {
			this.logger.warn("Illgegal State Exception while clearing Menu Cache.");
		}
	}

	@Override
	public void saveMenuItem(PoMenuItemBase menuItem) {
		menuItemBaseDAO.save(menuItem);
	}

	@Override
	public void deleteMenuItem(PoMenuItemBase menuItem) {
		if (menuItem instanceof PoMenuItemActionLink) {
			menuItemActionLinkDAO.delete((PoMenuItemActionLink) menuItem);
		} else if (menuItem instanceof PoMenuItemFolder) {
			menuItemFolderDAO.delete((PoMenuItemFolder) menuItem);
		} else if (menuItem instanceof PoMenuItemTemplateLink) {
			menuItemTemplateLinkDAO.delete((PoMenuItemTemplateLink) menuItem);
		} else {
			throw new IllegalArgumentException("Cannot delete item as it's class: " + menuItem.getClass() + " not compatible with: " + PoMenuItemBase.class);
		}
	}

	@Override
	public void saveMenuTree(PoMenuTreeBase tree) {
		menuTreeBaseDAO.save(tree);
	}

	@Override
	public void deleteMenuTree(PoMenuTreeBase tree) {
		if (tree instanceof PoMenuTreeTemplate) {
			List<PoMenuItemTemplateLink> templateLinks = menuItemTemplateLinkDAO.findAllMenuItemTemplateLinks((PoMenuTreeTemplate) tree);
			for (PoMenuItemTemplateLink link : templateLinks) {
				menuItemTemplateLinkDAO.delete(link);
			}
		}
		menuTreeBaseDAO.delete(tree);
	}

	@Override
	public PoMenuItemActionLink findMenuItemActionLinkByActionName(PoMenuTreeBase tree, String fullActionName) {
		return null;
	}

	@Override
	public void refreshTextModulesOfFolders(PoClient client) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getMenuFolderTextModuleKey(PoMenuItemFolder folder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PoMenuItemActionLink getMenuItemActionLink(String uid) {
		return menuItemActionLinkDAO.get(uid);
	}

	@Override
	public PoMenuItemFolder getMenuItemFolder(String uid) {
		return menuItemFolderDAO.get(uid);
	}

	@Override
	public PoMenuItemBase getMenuItem(String uid) {
		return menuItemBaseDAO.get(uid);
	}

	@Override
	public PoMenuItemTemplateLink getMenuItemTemplateLink(String uid) {
		return menuItemTemplateLinkDAO.get(uid);
	}

	@Override
	public PoMenuTreeClient findMenuTreeByClient(PoClient client) {
		return menuTreeClientDAO.findMenuTreeForClient(client);
	}

	/** spring setter */
	public void setMenuItemActionLinkDAO(PoMenuItemActionLinkDAOImpl menuItemActionLinkDAO) {
		this.menuItemActionLinkDAO = menuItemActionLinkDAO;
	}

	/** spring setter */
	public void setMenuItemFolderDAO(PoMenuItemFolderDAOImpl menuItemFolderDAO) {
		this.menuItemFolderDAO = menuItemFolderDAO;
	}

	/** spring setter */
	public void setMenuItemTemplateLinkDAO(PoMenuItemTemplateLinkDAOImpl menuItemTemplateLinkDAO) {
		this.menuItemTemplateLinkDAO = menuItemTemplateLinkDAO;
	}

	/** spring setter */
	public void setMenuTreeClientDAO(PoMenuTreeClientDAOImpl menuTreeClientDAO) {
		this.menuTreeClientDAO = menuTreeClientDAO;
	}

	/** spring setter */
	public void setMenuTreeTemplateDAO(PoMenuTreeTemplateDAOImpl menuTreeTemplateDAO) {
		this.menuTreeTemplateDAO = menuTreeTemplateDAO;
	}

	/** spring setter */
	public void setMenuItemBaseDAO(PoMenuItemBaseDAOImpl menuItemBaseDAO) {
		this.menuItemBaseDAO = menuItemBaseDAO;
	}

	/** spring setter */
	public void setMenuTreeBaseDAO(PoMenuTreeBaseDAOImpl menuTreeBaseDAO) {
		this.menuTreeBaseDAO = menuTreeBaseDAO;
	}

	/**
	 * @param personDAO the personDAO to set
	 */
	public void setPersonDAO(PoPersonDAO personDAO) {
		this.personDAO = personDAO;
	}

	/**
	 * @param permissionService the permissionService to set
	 */
	public void setPermissionService(PoActionPermissionService permissionService) {
		this.permissionService = permissionService;
	}

	/**
	 * @param actionService the actionService to set
	 */
	public void setActionService(PoActionService actionService) {
		this.actionService = actionService;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	/**
	 * implementation of the moveDown service method used to move an item
	 * one position down in the tree. @see moveDown for further information.
	 */
	@Override
	public void moveUp(PoMenuItemFolder folder, PoMenuItemBase item) {
		if (item.getParent() != null && folder != null) {
			List<PoMenuItemBase> children = folder.getChilds();
			int oldPosition = children.indexOf(item);
			int newPosition = oldPosition - 1;

			// check if the item is already at the top.
			if (newPosition >= 0) {
				Collections.swap(children, oldPosition, newPosition);
				updateRanking(children);
			} else {
				moveToTop(folder, item);
			}
		} else if (folder == null) {
			moveToPosition(folder, item, item.getRanking() - 1);
		}
	}

	/**
	 * implementation of the moveDown service method used to move an item
	 * one position down in the tree. Using swap method from java.util.Collections
	 * @see http://docs.oracle.com/javase/6/docs/api/java/util/Collections.html#swap(java.util.List, int, int)
	 */
	@Override
	public void moveDown(PoMenuItemFolder folder, PoMenuItemBase item) {
		if (item.getParent() != null && folder != null) {
			List<PoMenuItemBase> children = folder.getChilds();
			int oldPosition = children.indexOf(item);
			int newPosition = oldPosition + 1;

			// check if the item is already at the bottom.
			if (newPosition <= children.size() - 1) {
				Collections.swap(children, oldPosition, newPosition);
				updateRanking(children);
			}
		} else if (folder == null) {
			moveToPosition(folder, item, item.getRanking() + 1);
		}
	}

	@Override
	public void moveToTop(PoMenuItemFolder folder, PoMenuItemBase item) {
		moveToPosition(folder, item, 0);
	}

	/** 
	 * implementation of the service method which is used to move an item inside a tree to the bottom of the tree.
	 */
	@Override
	public void moveToBottom(PoMenuItemFolder folder, PoMenuItemBase item) {
		moveToPosition(folder, item, Integer.MAX_VALUE);
	}

	@Override
	public void moveAfter(PoMenuItemBase item, PoMenuItemBase after) {
		moveToPosition(item.getParent(), item, after.getRanking() + 1);
	}

	@Override
	public void moveBefore(PoMenuItemBase item, PoMenuItemBase before) {
		moveToPosition(item.getParent(), item, before.getRanking() - 1);
	}

	/**
	 * generic method used to move objects inside a tree. Depending on the target and actual position, a sublist
	 * of the tree will be rotated. 
	 * @see http://docs.oracle.com/javase/6/docs/api/java/util/Collections.html#rotate(java.util.List, int) for
	 * more information on the implementation.
	 * 
	 * @param folder is a folder where the position of item should be changed.
	 * @param item the item whom position should be changed.
	 */
	private void moveToPosition(PoMenuItemFolder folder, PoMenuItemBase item, int targetPosition) {
		int lastPosition = targetPosition;
		List<PoMenuItemBase> items = new ArrayList<PoMenuItemBase>();

		if (folder != null) {
			items = folder.getChilds();
		} else {
			items = item.getMenuTree().getTopLevelItems();
		}
		lastPosition = items.size() - 1;

		if (targetPosition >= lastPosition) {
			moveItemToPosition(item, lastPosition, items);
		} else if (targetPosition <= 0) {
			moveItemToPosition(item, 0, items);
		} else {
			moveItemToPosition(item, targetPosition, items);
		}
	}

	private void moveItemToPosition(PoMenuItemBase item, int targetPosition, List<PoMenuItemBase> children) {
		int oldPosition = children.indexOf(item);

		if (oldPosition == targetPosition)
			return;

		PoMenuItemBase removed = children.remove(oldPosition);
		children.add(targetPosition, removed);

		// any change of the tree should update the childs itself.
		updateRanking(children);
	}

	/** method to synchronize ranking property with index inside the list */
	private void updateRanking(List<PoMenuItemBase> list) {
		for (PoMenuItemBase child : list) {
			child.setRanking(list.indexOf(child));
		}
	}
}
