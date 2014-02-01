package at.workflow.webdesk.po.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import at.workflow.tools.ResourceHelper;
import at.workflow.tools.XMLTools;
import at.workflow.webdesk.po.PoActionPermissionService;
import at.workflow.webdesk.po.PoActionService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoLanguageService;
import at.workflow.webdesk.po.PoMenuService;
import at.workflow.webdesk.po.PoModuleService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.daos.PoMenuDAO;
import at.workflow.webdesk.po.daos.PoPersonDAO;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoLanguage;
import at.workflow.webdesk.po.model.PoMenuItem;
import at.workflow.webdesk.po.model.PoModule;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRegistrationBean;
import at.workflow.webdesk.po.model.PoTextModule;
import at.workflow.webdesk.tools.api.SysAdminUserInfo;
import at.workflow.webdesk.tools.comparator.ReflectiveComparator;
import at.workflow.webdesk.tools.date.DateTools;

public class PoMenuServiceImpl implements PoMenuService {

	protected final Logger logger = Logger.getLogger(this.getClass());

	private PoMenuDAO menuDAO;
	private PoPersonDAO personDAO;

	private PoLanguageService languageService;
	private PoModuleService moduleService;
	private PoActionService actionService;
	private PoActionPermissionService permissionService;

	private CacheManager cacheManager;

	private Map<String, PoRegistrationBean> registrationBeanMap;
	private SysAdminUserInfo sysAdminUserInfo;

	private PoModule module;
	private PoLanguage defLanguage;
	private List<PoLanguage> languages;

	private List<PoTextModule> changedTextModules;
	private List<PoTextModule> allMenuFolderTextModules;

	private PermissionExtractor permissionExtractor;

	private boolean extractPermissionBeforeHand;
	
	private boolean mergeMenuFoldersWithSameName = false;

	public void setMergeMenuFoldersWithSameName(boolean mergeMenuFoldersWithSameName) {
		this.mergeMenuFoldersWithSameName = mergeMenuFoldersWithSameName;
	}

	public void init() {
		permissionExtractor = new PermissionExtractor();
		permissionExtractor.setName("PoMenuServiceImpl.PermissionExtractor");
		permissionExtractor.setActionPermissionService(this.permissionService);
		permissionExtractor.setActionService(this.actionService);
		permissionExtractor.start();
	}

	@Override
	public int getMaxTemplateId() {
		return this.menuDAO.getMaxTemplateId();
	}

	@Override
	public List<PoMenuItem> findMenuItemsByClient(PoClient client) {
		return this.menuDAO.findMenuItemsByClient(client);
	}

	@Override
	public List<PoMenuItem> findTemplateMenuItemsByTemplateId(Integer TemplateId) {
		return this.menuDAO.findTemplateMenuItemsByTemplateId(TemplateId);
	}

	@Override
	public List<PoMenuItem> findTemplateLinks(Integer templateId) {
		return this.menuDAO.findTemplateLinks(templateId);
	}

	@Override
	public List<Object[]> getAllTemplateIds() {
		List<Object[]> templateIds = this.menuDAO.findAllTemplateIds();
		return templateIds;
	}

	@Override
	public PoMenuItem getMenuItem(String uid) {
		return this.menuDAO.get(uid);
	}

	@Override
	public PoMenuItem findMenuItemByName(String name) {
		return this.menuDAO.findMenuItemByName(name);
	}

	/* (non-Javadoc)
	 * @see at.workflow.webdesk.po.impl.PoMenoDAO#saveMenuItem(at.workflow.webdesk.po.model.PoMenuItem)
	 */
	@Override
	public void saveMenuItem(PoMenuItem mi) {

		if (mi.getValidfrom() == null)
			mi.setValidfrom(new Date());
		if (mi.getValidto() == null)
			mi.setValidto(new Date(DateTools.INFINITY_TIMEMILLIS));
		menuDAO.save(mi);
	}

	@Override
	public void deleteMenuItem(PoMenuItem mi) {
		menuDAO.delete(mi);
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
	public org.w3c.dom.Document getMenuTreeOfPerson(String userId) {
		return getMenuTreeOfPerson(userId, null);
	}
	
	/**
	 * Menu Document provider
	 */
	@Override
	public org.w3c.dom.Document getMenuTreeOfPerson(String userId, PoLanguage language) {
		if (userId.equals(this.sysAdminUserInfo.getSysAdminUser())) {
			try {
				List<Resource> resources = getAdminMenusResources();
				PoMenuItem root = getAdminMenuItemRoot(resources);
				return XMLTools.convertToW3cDoc(convertMenuItemTreeToDocument(root));
			}
			catch (Exception e1) {
				throw new PoRuntimeException(e1);
			}
		}
		
		PoPerson person = this.personDAO.findPersonByUserName(userId);
		List<PoAction> actions = permissionService.findAllActionsOfPerson(person, new Date());
		List<PoMenuItem> menu = this.readMenuFromDatabase(person.getClient(), actions, userId);

		List<PoAction> actions2Process = new ArrayList<PoAction>();
		collectActionsFromMenuRec(menu, actions2Process);
		if (extractPermissionBeforeHand) {
			registerPermissionExtractor(person, actions2Process);
		}
		
		List<? extends PoMenuItem> mergedMenus = new ArrayList<PoMenuItem>(menu);
		
		
		if (mergeMenuFoldersWithSameName==true) {
			// do it 2 times, so that structures of depth 2 are merged.
			// fi. 2 folders exist:  a/b and a/b => those will be merged
			mergedMenus = mergeIdenticalMenuTreeFolders(mergedMenus, language);
			mergedMenus = mergeIdenticalMenuTreeFolders(mergedMenus, language);
		}
		
		return getXMLDocument( mergedMenus );
	}
	
	@SuppressWarnings("serial")
	private static class MenuItemWrapper extends PoMenuItem {
		public MenuItemWrapper(PoMenuItem item) {
			super();
			if (item instanceof MenuItemWrapper)
				this.item = ((MenuItemWrapper)item).getItem();
			else
				this.item = item;
		}

		PoMenuItem item;
		List<MenuItemWrapper> childs = new ArrayList<MenuItemWrapper>();
		
		public PoMenuItem getItem() {
			return item;
		}
		
		@Override
		public PoAction getAction() {
			return item.getAction();
		}
		@Override
		public String getName() {
			return item.getName();
		}
		@Override
		public String getIconPath() {
			return item.getIconPath();
		}
		@Override
		public Integer getTemplateId() {
			return item.getTemplateId();
		}
		@Override
		public List<PoMenuItem> getChilds() {
			List<PoMenuItem> items = new ArrayList<PoMenuItem>();
			items.addAll(childs);
			return items;
		}
		@Override
		public String getUID() {
			return item.getUID();
		}
		@Override
		public int getRanking() {
			return item.getRanking();
		}
		
		@Override
		public PoClient getClient() {
			return item.getClient();
		}
		
		@Override
		public String getDescription() {
			return item.getDescription();
		}
		
		@Override
		public String getTextModuleKey() {
			return item.getTextModuleKey();
		}
		
		@Override
		public boolean hasChilds() {
			return childs.size()>0;
		}
		
		public void addChilds(List<MenuItemWrapper> items) {
			childs.addAll(items);
		}
	}
	
	@SuppressWarnings("unchecked")
	private List<? extends MenuItemWrapper> mergeIdenticalMenuTreeFolders(List<? extends PoMenuItem>menu, PoLanguage language) {
		
		List<MenuItemWrapper> newItems = new ArrayList<MenuItemWrapper>();
		Map<String, MenuItemWrapper> caption2ItemWrapper = new HashMap<String,MenuItemWrapper>();
		
		for (PoMenuItem item : menu) {
			
			String caption = languageService.translate(language.getCode(), getTextModuleKey(item));
			
			List<MenuItemWrapper> childs = new ArrayList<MenuItemWrapper>();
			if (isItemFolder(item) && item.hasChilds()) {
				childs = (List<MenuItemWrapper>) mergeIdenticalMenuTreeFolders(item.getChilds(), language);
			}
			
			if (caption2ItemWrapper.containsKey(caption) && isItemFolder(item)) {
				caption2ItemWrapper.get(caption).addChilds(childs);
			} else {
				// bloody hack to remove broken entries
				//if (caption.equals(getTextModuleKey(item))==false) {
					MenuItemWrapper itemWrapper = new MenuItemWrapper(item);
					newItems.add(itemWrapper);
					itemWrapper.addChilds(childs);
					caption2ItemWrapper.put(caption, itemWrapper);
				//}
			}
		}
		
		return newItems;
	}
	
	private boolean isItemFolder(PoMenuItem item) {
		return item.getAction()==null;
	}

	/**
	 * walk through MenuItem Tree and collect all PoActions which are referenced
	 * as menutree leaves.
	 * 
	 * @param items: List of PoMenuItems
	 * @param actions: List of PoActions to be returned.
	 */
	private void collectActionsFromMenuRec(List<? extends PoMenuItem> items, List<PoAction> actions) {
		for (PoMenuItem item : items) {
			if (item.getAction() != null) {
				actions.add(item.getAction());
			}
			else {
				
				Collections.sort(item.getChilds(), new ReflectiveComparator("ranking"));
				
				collectActionsFromMenuRec(item.getChilds(), actions);
			}
		}
	}

	private void registerPermissionExtractor(PoPerson person, List<PoAction> actions) {
		this.permissionExtractor.addExtractionBean(new PermissionExtractionBean(person, actions));
	}

	private class PermissionExtractionBean {

		public PermissionExtractionBean(PoPerson person, List<PoAction> actions) {
			super();
			this.person = person;
			this.actions = actions;
		}

		/** the person whose menuactions are referenced here */
		private PoPerson person;
		
		/** list of actions which are referenced in persons menu */
		private List<PoAction> actions;

	}

	private class PermissionExtractor extends Thread {
		private List<PermissionExtractionBean> extractionBeans = Collections.synchronizedList(new ArrayList<PermissionExtractionBean>());
		private PoActionPermissionService actionPermissionService;
		private PoActionService actionService;

		@Override
		public void run() {
			while (true) {
				if (extractionBeans.size() > 0) {
					List<PermissionExtractionBean> toRemove = new ArrayList<PermissionExtractionBean>();
					for (int i = 0; i < extractionBeans.size(); i++) {
						PermissionExtractionBean bean = extractionBeans.get(0);
						for (PoAction action : bean.actions) {
							boolean hasPermission = actionPermissionService.hasPersonPermissionForAction(bean.person.getUserName(), action);
							if (!hasPermission) {
								actionService.getConfigFromAction(bean.person, action);
							}
						}
						toRemove.add(bean);
					}

					for (PermissionExtractionBean bean : toRemove) {
						extractionBeans.remove(bean);
					}
				}

				try {
					sleep(100);
				}
				catch (InterruptedException e) { /*can ignore this*/
				}
				yield();
			}
		}

		public void addExtractionBean(PermissionExtractionBean extractionBean) {
			this.extractionBeans.add(extractionBean);
		}

		public void setActionPermissionService(PoActionPermissionService actionPermissionService) {
			this.actionPermissionService = actionPermissionService;
		}

		public void setActionService(PoActionService actionService) {
			this.actionService = actionService;
		}
	}

	@Override
	public void refreshTextModulesOfFolders(PoClient client) {

		module = this.moduleService.getModuleByName("po");
		defLanguage = this.languageService.findDefaultLanguage();
		languages = this.languageService.findAllLanguages();

		List<PoMenuItem> menu = this.readMenuFromDatabase(client, null, null);

		if (this.logger.isDebugEnabled())
			this.logger.debug("start creating textmodules for menufolders...");

		changedTextModules = new ArrayList<PoTextModule>();
		allMenuFolderTextModules = new ArrayList<PoTextModule>();

		createTextModulesOfFoldersRec(menu);
	}

	public List<Resource> getAdminMenusResources() throws IOException {

		// Set is used to eliminate duplicates
		Set<String> adminResourcePaths = new LinkedHashSet<String>();
		for (String key : this.registrationBeanMap.keySet()) {
			PoRegistrationBean regBean = this.registrationBeanMap.get(key);
			adminResourcePaths.addAll(regBean.getRegisterMenuTree());
		}

		// Set is used to eliminate duplicates
		Set<Resource> adminMenuResources = new LinkedHashSet<Resource>();

		// Iterate over all defined classpath resource patterns
		for (String resourcePath : adminResourcePaths) {
			ResourcePatternResolver rpr = new PathMatchingResourcePatternResolver();
			Resource[] ress = rpr.getResources(resourcePath);
			// add them to the adminMenuRes Set 
			adminMenuResources.addAll(Arrays.asList(ress));
		}

		List<Resource> result = new ArrayList<Resource>();
		result.addAll(adminMenuResources);

		return result;
	}

	private int createDomRec(List<? extends PoMenuItem> items, Element e, int foldercount) {

		if (items == null || items.isEmpty())
			return foldercount;

		Collections.sort(items);

		boolean first = true;
		Iterator<? extends PoMenuItem> i = items.iterator();
		while (i.hasNext()) {
			PoMenuItem item = i.next();
			if (item.hasChilds() ||
					item.getName() != null && !item.getName().equals("") && item.getAction() == null) {
				
				if (item.getTemplateId() != null && item.getClient() != null) {
					// TEMPLATE
					foldercount = createDomRec(item.getChilds(), e, foldercount);
					
				} else {
					//FOLDER
					Element folder = generateElementForFolder(item, foldercount);
					e.addContent(folder);
					foldercount++;
					foldercount = createDomRec(item.getChilds(), folder, foldercount);
				}

			}
			else {
				//ACTION
				Element action = generateElementForAction(item);
				e.addContent(action);
			}
			if (first)
				first = false;
		}
		return foldercount;
	}

	private Element generateElementForAction(PoMenuItem item) {

		PoAction action = item.getAction();
		Element tree = new Element("tree");

		String postFix = actionService.getActionPostfix(action);

		if (action.getDescription() != null)
			tree.setAttribute("title", action.getName() + postFix + "_action_description");

		tree.setAttribute("action", action.getName() + postFix);
		tree.setAttribute("text", action.getName() + postFix + "_action_caption");
		tree.setAttribute("newWindow", action.isAlwaysOpenInNewWindow() ? "true" : "false");
		tree.setAttribute("layout", action.getLayoutTemplateToUse() == null ? "" : action.getLayoutTemplateToUse());
		tree.setAttribute("attr", "text", Namespace.getNamespace("i18n", "http://apache.org/cocoon/i18n/2.1"));

		// for every application usefull...
		if (action.getImage() != null)
			tree.setAttribute("image", "./images/" +
					action.getImageSet() + "/16x16/" + action.getImage());

		tree.setAttribute("id", item.getUID());
		return tree;
	}

	private Element generateElementForFolder(PoMenuItem item, int foldercount) {

		Element tree = new Element("tree");

		String textModuleKey = getMenuFolderTextModuleKey(item);

		if (textModuleKey != null) {
			tree.setAttribute("text", textModuleKey);
			tree.setAttribute("attr", "text", Namespace.getNamespace("i18n", "http://apache.org/cocoon/i18n/2.1"));
		}
		else {
			// falls TextmoduleKey leer sein sollte
			tree.setAttribute("text", item.getName());
		}
		tree.setAttribute("id", item.getUID());

		tree.setAttribute("foldercount", foldercount + "");

		return tree;
	}

	@Override
	public String getMenuFolderTextModuleKey(PoMenuItem item) {

		if (item.getClient() == null && item.getTemplateId() == null)
			return null;

		if (item.getTextModuleKey() != null)
			return item.getTextModuleKey();

		return generateFolderTextModuleKey(item);
	}

	private String generateFolderTextModuleKey(PoMenuItem item) {
		String keypart = item.getClient() != null ? item.getClient().getName() : "template_" + item.getTemplateId().toString();
		return "po_editMenuTree.act_menufolder_" + keypart + "_" + item.getUID();
	}

	/**
	 * @see at.workflow.webdesk.po.impl.PoMenuService#getXMLDocument(at.workflow.webdesk.po.model.PoMenuStructure, boolean)
	 */
	private org.w3c.dom.Document getXMLDocument(List<? extends PoMenuItem> items) {

		long startTime = System.currentTimeMillis();
		Element root = new Element("tree");
		Document doc = new Document(root);
		createDomRec(items, root, 0);

		Element tree = new Element("tree");
		root.addContent(tree);
		tree.setAttribute("text", "Logout");
		tree.setAttribute("action", "logout");
		tree.setAttribute("image", "./images/nuvola/16x16/actions/exit.png");

		try {
			DOMOutputter outputter = new DOMOutputter();
			logger.debug("getXMLDocument needed " + (System.currentTimeMillis() - startTime) + " ms");
			return outputter.output(doc);
		}
		catch (JDOMException jde) {
			jde.printStackTrace();
			return null;
		}
	}

	private List<PoMenuItem> readMenuFromDatabase(PoClient client, List<PoAction> actions, String userId) {

		if (this.logger.isDebugEnabled()) {
			logger.debug("entering readMenuFromDatabase with " + actions.size() + " actions.");
		}

		List<PoMenuItem> menuItems = null;
		if (client != null) {
			menuItems = menuDAO.findMenuItemsByClient(client);
		}
		if (menuItems == null || menuItems.isEmpty()) {
			menuItems = menuDAO.findMenuItemsForAll();
		}
		
		// ggruber 2013-01-10 quickfix try
		//List<PoMenuItem> templateLinks2Remove = new ArrayList<PoMenuItem>();

		List<PoMenuItem> itemsFromTemplates = new ArrayList<PoMenuItem>();
		for (PoMenuItem item : menuItems) {

            // ggruber 2013-01-10 quickfix try to fix embedded ranking
			//item.setRanking( item.getRanking()*100 );
			
			// resolve template-links
			if ( isTemplateLink(item)==true ) {
				List<PoMenuItem> templateItems = menuDAO.findTemplateMenuItemsByTemplateId(item.getTemplateId());
				if (templateItems == null || templateItems.isEmpty())
					continue;
				itemsFromTemplates.addAll(templateItems);
				
				for (PoMenuItem templateItem : templateItems) {
					if (templateItem.getParent() == null) {
						// ugly code which changes an model attribute temporarly
						// without persisting!
						item.addChild(templateItem);
						templateItem.setTemplateId(null);
						
						//ggruber 2013-01-10 quickfix try
						 
						//templateItem.setRanking( item.getRanking() + templateItem.getRanking() );
						
						// move Top level template item to parent of template link
						//if (item.getParent()!=null) {
						//	item.getParent().addChild(templateItem);
						//}
							
						//templateLinks2Remove.add(item);						
					}
				}
			}
		}
		
		// ggruber 2013-01-10 quickfix try
		
		// remove template links
		//for (PoMenuItem templateLink : templateLinks2Remove) {
		//	menuItems.remove(templateLink);
		//	if (templateLink.getParent()!=null)
		//		templateLink.getParent().getChilds().remove(templateLink);
		//}

		if (!itemsFromTemplates.isEmpty())
			menuItems.addAll(itemsFromTemplates);

		removeItemsWithoutPermission(menuItems, userId, actions);

		removeItemsWithDetachedActions(menuItems, userId);

		removeEmptyFolders(menuItems);

		if (logger.isDebugEnabled()) {
			logger.debug("leaving readMenuFromDatabase with " + actions.size() + " menutitems.");
		}

		return getTopLevelMenuItems(menuItems);
	}

	private void removeItemsWithoutPermission(List<PoMenuItem> items, String userId, List<PoAction> permittedActions) {

		if (logger.isDebugEnabled()) {
			logger.debug("now lets remove items without permissions...");
		}

		List<PoMenuItem> removeItems = new ArrayList<PoMenuItem>();
		for (PoMenuItem item : items) {
			if (userId != null && item.getAction() != null) {
				if (permittedActions == null || !permittedActions.contains(item.getAction())) {
					removeItemRec(item, removeItems);
				}
			}
		}
		items.removeAll(removeItems);

		if (this.logger.isDebugEnabled()) {
			this.logger.debug("removed " + removeItems.size() + " items with not permitted actions.");
		}
	}

	private void removeItemsWithDetachedActions(List<PoMenuItem> items, String userId) {

		if (logger.isDebugEnabled()) {
			logger.debug("now lets remove items with detached items..");
		}

		List<PoMenuItem> removeItems = new ArrayList<PoMenuItem>();
		for (PoMenuItem item : items) {
			if (userId != null && item.getAction() != null && item.getAction().isDetached()) {
				removeItemRec(item, removeItems);
			}
		}

		items.removeAll(removeItems);

		if (logger.isDebugEnabled()) {
			logger.debug("removed " + removeItems.size() + " items with detached actions.");
		}

	}

	private void removeEmptyFolders(List<PoMenuItem> items) {

		if (this.logger.isDebugEnabled()) {
			this.logger.debug("now lets remove items with empty folders..");
		}

		List<PoMenuItem> removeItems = new ArrayList<PoMenuItem>();
		for (PoMenuItem item : items) {
			if (item.getAction() == null || item.getAction().getUID().equals("")) {
				// Folder detected
				if (!item.hasChilds()) {
					removeItemRec(item, removeItems);
				}
			}
		}
		items.removeAll(removeItems);

		if (logger.isDebugEnabled()) {
			logger.debug("removed " + removeItems.size() + " items with empty folders.");
		}
	}

	private void removeItemRec(PoMenuItem item, List<PoMenuItem> removedItems) {

		if (logger.isDebugEnabled()) {
			logger.debug("remove menuitem=" + item);
		}

		removedItems.add(item);

		PoMenuItem parent = item.getParent();
		if (parent == null) { // root
			return;
		}
		parent.getChilds().remove(item);
		if (!parent.hasChilds()) {
			removeItemRec(parent, removedItems);
		}
	}

	private List<PoMenuItem> getTopLevelMenuItems(List<PoMenuItem> items) {

		List<PoMenuItem> roots = new ArrayList<PoMenuItem>();
		for (PoMenuItem item : items) {
			if ( item.getParent() == null 
					// ggruber 2013-01-10 quickfix try
					//&& (isTemplateLink(item)==false) 
					) {
				roots.add(item);
			}
		}

		Collections.sort(roots, new ReflectiveComparator("ranking"));
		return roots;
	}
	
	private boolean isTemplateLink(PoMenuItem item) {
		return (item.getTemplateId()!=null && item.getClient()!=null);
	}

	private PoMenuItem getAdminMenuItemRoot(List<Resource> adminMenusRes) {

		PoMenuItem root = new PoMenuItem();
		SAXBuilder builder = new SAXBuilder();
		Set<String> resourceClassPaths = new HashSet<String>();

		/**
		 * ensure that the resource containing the po modules
		 * is always on top (processed first!) 
		 * @author ggruber
		 */
		class AdminResourceComperator implements Comparator<Resource> {

			@Override
			public int compare(Resource r1, Resource r2) {
				if (r1.getFilename().endsWith("po.admin.xml"))
					return -1;
				if (r2.getFilename().endsWith("po.admin.xml"))
					return 1;
				return r1.getFilename().toLowerCase().compareTo(r2.getFilename().toLowerCase());
			}

		}

		Collections.sort(adminMenusRes, new AdminResourceComperator());

		for (Resource r : adminMenusRes) {

			if (r.exists() && !resourceClassPaths.contains(ResourceHelper.getClassPathOfResource(r))) {
				try {
					resourceClassPaths.add(ResourceHelper.getClassPathOfResource(r));
					Document doc = builder.build(r.getInputStream());
					Element rootElement = doc.getRootElement();
					generateAdminMenuRec(rootElement, root, root);
				}
				catch (JDOMException e) {
					logger.error("JdomException occured: " + e.getMessage() + ". Skipping file " + r.getFilename());
				}
				catch (IOException e) {
					logger.error("An IoException occured: " + e.getMessage() + ". Skipping file " + r.getFilename());
				}
			}
		}
		return root;
	}

	@SuppressWarnings("unchecked")	// untyped jdom Element child list
	private org.jdom.Document convertMenuItemTreeToDocument(PoMenuItem root) {

		Document doc = new Document();
		doc.setRootElement(new Element("tree"));
		writeAdminMenuRec(doc.getRootElement(), root, 0);

		// Add the logout button 
		Element logout = new Element("tree");
		logout.setAttribute(new Attribute("text", "po_logout"));
		logout.setAttribute(new Attribute("action", "logout"));
		logout.setAttribute(new Attribute("image", "./images/nuvola/16x16/actions/exit.png"));

		doc.getRootElement().getChildren().add(logout);

		return doc;
	}

	private int writeAdminMenuRec(Element rootElement, PoMenuItem root, int count) {

		if (root.getAction() == null && root.getName() == null) {
			// the root node, create an empty tree node.
			rootElement.setName("tree");
		}
		for (PoMenuItem mi : root.getChilds()) {

			if (mi.getName() != null) {
				// folder
				if (mi.getChilds().size() > 0) {
					// only add folder, if it has childs...
					Element folderNode = new Element("tree");
					folderNode.setAttribute(new Attribute("text", mi.getName()));
					folderNode.setAttribute(new Attribute("foldercount", new Integer(count).toString()));
					count++;
					count = writeAdminMenuRec(folderNode, mi, count);
					rootElement.addContent(folderNode);
				}
			}
			else {
				Element actionNode = new Element("tree");
				
				actionNode.setAttribute(new Attribute("text", getActionTextModuleKey(mi.getAction())));
				actionNode.setAttribute(new Attribute("action", getFullActionName(mi.getAction())));
				actionNode.setAttribute(new Attribute("newWindow", mi.getAction().isAlwaysOpenInNewWindow() ? "true" : "false"));
				String layoutTemplate = mi.getAction().getLayoutTemplateToUse();
				actionNode.setAttribute(new Attribute("layout", layoutTemplate == null ? "" : layoutTemplate));
				actionNode.setAttribute(new Attribute("image", "./images/" + mi.getAction().getImageSet() +
						"/16x16/" + mi.getAction().getImage()));
				rootElement.addContent(actionNode);
			}
		}
		return count;
	}
	
	
	private String getTextModuleKey(PoMenuItem item) {
		if (isItemFolder(item))
			return getMenuFolderTextModuleKey(item);
		else
			return getActionTextModuleKey(item.getAction());
	}
	
	private String getActionTextModuleKey(PoAction action) {
		return getFullActionName(action) + "_action_caption";
	}
	
	private String getFullActionName(PoAction action) {
		return action.getName() + actionService.getActionPostfix(action);
	}

	private void generateAdminMenuRec(Element e, PoMenuItem actItem, PoMenuItem root) {

		@SuppressWarnings("unchecked")
		List<Element> childs = e.getChildren();
		for (Element child : childs) {
			PoMenuItem mi = new PoMenuItem();
			if ("folder".equals(child.getName())) {
				// the child describes a folder 
				mi.setName(child.getAttributeValue("text"));
				if (child.getAttribute("extends") != null) {
					// we have to find the folder that will be extended.
					PoMenuItem newActItem = findFolderOrItemWithName(root, child.getAttributeValue("extends"));
					if (newActItem == null)
						logger.warn("No Item with name " + child.getAttributeValue("extends") + " was found. Skipping element.");
					else {
						newActItem.addChild(mi);
						generateAdminMenuRec(child, mi, root);
					}
				}
				else if (child.getAttribute("insert-after") != null) {
					insertAfter(actItem, mi, child.getAttributeValue("insert-after"));
					generateAdminMenuRec(child, mi, root);
				}
				else {
					actItem.addChild(mi);
					logger.debug("Added folder " + mi.getName());
					generateAdminMenuRec(child, mi, root);
				}
			}

			if (child.getName().equals("action")) {
				// the child describes an action
				PoAction action = this.actionService.findActionWithFullName(child.getAttributeValue("id"));
				if (action != null) {
					mi.setAction(action);
					if (child.getAttribute("insert-after") != null) {
						insertAfter(actItem, mi, child.getAttributeValue("insert-after"));
					}
					if (child.getAttribute("extends") != null) {
						PoMenuItem newActItem = findFolderOrItemWithName(root, child.getAttributeValue("extends"));
						newActItem.addChild(mi);
					}
					else {
						logger.debug("added action " + mi.getAction().getName());
						actItem.addChild(mi);
					}
				}
				else {
					logger.warn("Didn't find an action with name " + child.getAttributeValue("id") + ". Skipping entry ..");
				}
			}
		}
	}

	/**
	 * <p>
	 * This function tries to insert the <code>PoMenuItem</code> <code>mi</code> after 
	 * the child of <code>actItem</code> with <code>name</code> equal to <code>attributeValue</code>.
	 * If the no item with <code>name</code> was found, <code>mi</code> is inserted at the end. 
	 * 
	 * 
	 * @param actItem
	 * @param mi
	 * @param attributeValue
	 */
	private void insertAfter(PoMenuItem actItem, PoMenuItem mi, String attributeValue) {

		List<PoMenuItem> childs = actItem.getChilds();
		int index = 0;
		for (PoMenuItem item : childs) {

			if (item.getName() != null && item.getName().equals(attributeValue)) {
				// entry was found -> index is its position
				mi.setParent(actItem);
				actItem.getChilds().add(index + 1, mi);
				return;
			}
			index++;
		}

		// insert the item at the end
		logger.info("Was not able to find item with name " + attributeValue + ". Going to assign the elemt at the end of the actual entry.[" +
				actItem.getName() != null ? actItem.getName() : actItem.getAction().getName() + "]");
		actItem.addChild(mi);
	}

	private PoMenuItem findFolderOrItemWithName(PoMenuItem root, String attributeValue) {

		if (root.getName() != null && root.getName().equals(attributeValue))
			return root;

		List<PoMenuItem> childs = root.getChilds();
		for (PoMenuItem child : childs) {
			if (child.getName() != null) {
				if (child.getName().equals(attributeValue))
					return child;
				else if (child.hasChilds()) {
					PoMenuItem foundItem = findFolderOrItemWithName(child, attributeValue);
					if (foundItem != null)
						return foundItem;
				}
			} // else is an action
		}
		// this is only reached when no entry with the given name was found
		return null;
	}

	/**
	 * this method creates textmodules for all menufolders of the given menustructure for all languages defined.
	 * The actual name of the menuitem is taken as the value for the default language. the values of the other 
	 * languages are defined as "[langCode] defaultValue". so if a folder is called "Antr�ge" the value of the
	 * german textmodule will obviously be "Antr�ge", assuming that german is the default language.
	 * For English the generated value will be "[en] Antr�ge". 
	 * 
	 * @param ms PoMenuStructure object containing the complete menu
	 * @param ss SortedSet of PoMenuItem Objects
	 * @param changedTextModules  List of PoTextModule Objects which changed
	 */
	private void createTextModulesOfFoldersRec(List<PoMenuItem> items) {

		if (items == null || items.isEmpty())
			return;
		
		PoAction action = actionService.findActionByNameAndType("po_editMenuTree", PoConstants.ACTION_TYPE_ACTION);

		for (PoMenuItem mi : items) {
			// assure the menuitem is a folder and not an action!
			if (mi.hasChilds() || (mi.getName() != null && !mi.getName().equals("") && mi.getAction() == null)) {
				// is Folder

				// check that a corresponding textmodule is present, if not create one
				// does NOT overwrite existing ones!
				for (PoLanguage lng : languages) {
					boolean writeToDB = false;
					PoTextModule tm = this.languageService.findTextModuleByNameAndLanguage(mi.getTextModuleKey(), lng);

					// if textmodule is not existing -> create one
					if (tm == null) {
						// not existing yet
						tm = new PoTextModule();
						tm.setName(mi.getTextModuleKey());
						tm.setAction(action);
						tm.setLanguage(lng);
						tm.setModule(module);
						writeToDB = true;
					}

					if (lng.equals(defLanguage)) {
						// default language
						if (tm.getValue() == null || !tm.getValue().equals(mi.getName())) {
							// set Value if it changed
							tm.setValue(mi.getName());
							writeToDB = true;
						}
					}
					else {
						// any other language
						if (tm.getValue() == null) {
							// only overwrite if not existing!
							// DO not overwrite existing translations automatically!
							tm.setValue("[" + lng.getCode() + "] " + mi.getName());
							writeToDB = true;
						}
					}
					// only write to db if necessary!
					if (writeToDB) {
						this.languageService.saveTextModule(tm);
						changedTextModules.add(tm);
					}
					allMenuFolderTextModules.add(tm);
				}

				// if childs of the actual folder are present
				// go into those recursivly!
				createTextModulesOfFoldersRec(mi.getChilds());
			}
		}

	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public void setSysAdminUserInfo(SysAdminUserInfo sysAdminUserInfo) {
		this.sysAdminUserInfo = sysAdminUserInfo;
	}

	public void setLanguageService(PoLanguageService languageService) {
		this.languageService = languageService;
	}

	public void setMenuDAO(PoMenuDAO menuDAO) {
		this.menuDAO = menuDAO;
	}

	public void setModuleService(PoModuleService moduleService) {
		this.moduleService = moduleService;
	}

	public void setPersonDAO(PoPersonDAO personDAO) {
		this.personDAO = personDAO;
	}

	public void setActionService(PoActionService actionService) {
		this.actionService = actionService;
	}

	public void setRegistrationBeanMap(Map<String, PoRegistrationBean> registrationBeanMap) {
		this.registrationBeanMap = registrationBeanMap;
	}

	public void setPermissionService(PoActionPermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setExtractPermissionBeforeHand(boolean extractPermissionBeforeHand) {
		this.extractPermissionBeforeHand = extractPermissionBeforeHand;
	}

}
