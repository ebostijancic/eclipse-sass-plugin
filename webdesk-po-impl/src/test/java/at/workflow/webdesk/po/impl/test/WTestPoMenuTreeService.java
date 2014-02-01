package at.workflow.webdesk.po.impl.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.ehcache.CacheManager;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.workflow.tools.XMLTools;
import at.workflow.tools.XPathTools;
import at.workflow.webdesk.po.PoActionPermissionService;
import at.workflow.webdesk.po.PoActionService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoLanguageService;
import at.workflow.webdesk.po.PoMenuTreeService;
import at.workflow.webdesk.po.PoModuleService;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.PoRoleService;
import at.workflow.webdesk.po.impl.helper.PoDataGenerator;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoMenuItemActionLink;
import at.workflow.webdesk.po.model.PoMenuItemBase;
import at.workflow.webdesk.po.model.PoMenuItemFolder;
import at.workflow.webdesk.po.model.PoMenuItemTemplateLink;
import at.workflow.webdesk.po.model.PoMenuTreeBase;
import at.workflow.webdesk.po.model.PoMenuTreeClient;
import at.workflow.webdesk.po.model.PoMenuTreeTemplate;
import at.workflow.webdesk.po.model.PoModule;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRegistrationBean;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;
import at.workflow.webdesk.tools.testing.DataGenerator;

/**
 * Unit-Tests for the PoMenuTreeService
 * 
 * @author ebostijancic 29.08.2012
 */
public class WTestPoMenuTreeService extends AbstractTransactionalSpringHsqlDbTestCase {

	public static final String CLIENT = "MyTestClient";
	public static final String ACTION_1 = "Aktion 1";
	public static final String ACTION_2 = "Aktion 2";
	public static final String ACTION_3 = "Aktion 3";
	public static final String ACTION_4 = "Aktion 4";

	private static PoMenuTreeService menuTreeService;
	private static PoOrganisationService organisationService;
	private static PoRoleService roleService;
	private static PoActionService actionService;
	private static PoActionPermissionService permissionService;
	private static PoLanguageService languageService;
	private static PoModuleService moduleService;
	private static XPathTools xpathTools;

	private PoClient client1, client2;
	private PoAction action1, action2, action3, action4;
	private PoPerson person1, person2;
	private PoGroup group1, group2;
	private PoRole role1, role2;
	private PoModule module;

	private PoMenuItemActionLink actionLink1, actionLink2, actionLink3, actionLink4;
	private PoMenuItemFolder folder1, folder2, folder3, folder4, folder5, folder6;
	private PoMenuTreeTemplate menuTreeTemplate1, menuTreeTemplate2;
	private PoMenuItemTemplateLink templateLink1, templateLink2, templateLink3, templateLink4;
	private PoMenuTreeClient menuTreeClient1, menuTreeClient2;

	@Override
	protected void onSetUpBeforeDataGeneration() throws Exception {
		((PoLanguageService) getBean("PoLanguageService")).init();
	}

	@Override
	protected final DataGenerator[] getDataGenerators() {
		logger.info("getDataGenerators()");
		return new DataGenerator[] { new PoDataGenerator("at/workflow/webdesk/po/impl/test/data/MenuTestData.xml", true) };
	}

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		logger.info("onSetUpAfterDataGeneration()");

		super.onSetUpAfterDataGeneration();

		menuTreeService = (PoMenuTreeService) getBean("PoMenuTreeService");
		organisationService = (PoOrganisationService) getBean("PoOrganisationService");
		roleService = (PoRoleService) getBean("PoRoleService");
		actionService = (PoActionService) getBean("PoActionService");
		permissionService = (PoActionPermissionService) getBean("PoActionPermissionService");
		languageService = (PoLanguageService) getBean("PoLanguageService");
		moduleService = (PoModuleService) getBean("PoModuleService");

		if (xpathTools == null)
			xpathTools = new XPathTools();

		// ensure that also the test-admin file is loaded!
		PoRegistrationBean poRegBean = (PoRegistrationBean) getBean("PoRegistrationBean_po");
		List<String> regList = poRegBean.getRegisterMenuTree();
		String newEntry = "classpath*:/at/workflow/webdesk/po/actions/*.test-admin.xml";
		if (!regList.contains(newEntry)) {
			regList.add(newEntry);
			poRegBean.setRegisterMenuTree(regList);
		}

		if (client1 == null) {
			client1 = organisationService.findClientByName("Workflex");
		}

		if (client2 == null) {
			client2 = organisationService.findClientByName("Workflow");
		}

		if (role1 == null) {
			role1 = roleService.findRoleByName("R01", DateTools.now()).get(0);
		}

		if (role2 == null) {
			role2 = roleService.findRoleByName("R02", DateTools.now()).get(0);
		}

		if (person1 == null) {
			person1 = organisationService.findPersonByUserName("wef");
		}

		if (person2 == null) {
			person2 = organisationService.findPersonByUserName("mmu");
		}

		if (group1 == null) {
			group1 = organisationService.findGroupByShortName("G01", client1);
		}

		if (group2 == null) {
			group2 = organisationService.findGroupByShortName("G02", client1);
		}

		if (module == null) {
			module = new PoModule();
			module.setName("mymodule");
			moduleService.saveModule(module);
		}

		if (action1 == null) {
			action1 = new PoAction();
			action1.setName(ACTION_1);
			action1.setActionType(1);
			action1.setModule(module);
			actionService.saveAction(action1);
		}
		
		if (action2 == null) {
			action2 = new PoAction();
			action2.setName(ACTION_2);
			action2.setModule(module);
			actionService.saveAction(action2);
		}
		
		if (action3 == null) {
			action3 = new PoAction();
			action3.setName(ACTION_3);
			action3.setModule(module);
			actionService.saveAction(action3);
		}
		
		if (action4 == null) {
			action4 = new PoAction();
			action4.setModule(module);
			action4.setName(ACTION_4);
			actionService.saveAction(action4);
		}
		
		permissionService.assignPermission(action1, person1, DateTools.yesterday(), DateTools.INFINITY, PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON);
		permissionService.assignPermission(action2, person1, DateTools.yesterday(), DateTools.INFINITY, PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON);
		permissionService.assignPermission(action3, person1, DateTools.yesterday(), DateTools.INFINITY, PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON);
		permissionService.assignPermission(action4, person1, DateTools.yesterday(), DateTools.INFINITY, PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON);

		// clear chache is important as getMenuTreeOfPerson will fail, while using data from PoMenuService.
		CacheManager cacheManger = (CacheManager) getBean("CacheManager");
		cacheManger.clearAll();

	}

	@Override
	public void onSetUp() throws Exception {
		logger.info("onSetUp()");

		super.onSetUp();

		generateMenuItemActionLinks();

		generateMenuItemFolders();

		generateClientTrees();

		generateTreeTemplates();

		generateTemplateLinks();

		menuTreeService.clearMenuCache();
	}

	private void generateTreeTemplates() {
		/*======================================*/
		/*          Generate Tree Templates     */
		/*======================================*/
		if (menuTreeTemplate1 == null)
			menuTreeTemplate1 = new PoMenuTreeTemplate();
		menuTreeTemplate1.setName("Tree Template1");
		menuTreeTemplate1.setTopLevelItems(new ArrayList<PoMenuItemBase>(Arrays.asList(new PoMenuItemBase[] { folder1, folder2, (PoMenuItemBase) templateLink1 })));

		if (menuTreeTemplate2 == null)
			menuTreeTemplate2 = new PoMenuTreeTemplate();
		menuTreeTemplate2.setName("Tree Template2");
		menuTreeTemplate2.setTopLevelItems(new ArrayList<PoMenuItemBase>(Arrays.asList(new PoMenuItemBase[] { folder3, folder4, (PoMenuItemBase) templateLink2 })));

		menuTreeService.saveMenuTree(menuTreeTemplate1);
		assertNotNull("UID not supplied for menuTreeTemplate1", menuTreeTemplate1.getUID());

		menuTreeService.saveMenuTree(menuTreeTemplate2);
		assertNotNull("UID not supplied for menuTreeTemplate2", menuTreeTemplate2.getUID());
	}

	private void generateTemplateLinks() {

		/*======================================*/
		/*          Generate Template Links     */
		/*======================================*/

		if (templateLink1 == null) {
			templateLink1 = new PoMenuItemTemplateLink();
		}

		templateLink1.setTreeTemplate(menuTreeTemplate1);
		menuTreeService.saveMenuItem(templateLink1);

		if (templateLink2 == null) {
			templateLink2 = new PoMenuItemTemplateLink();
		}

		templateLink2.setTreeTemplate(menuTreeTemplate1);
		menuTreeService.saveMenuItem(templateLink2);

		if (templateLink3 == null) {
			templateLink3 = new PoMenuItemTemplateLink();
		}

		templateLink3.setTreeTemplate(menuTreeTemplate2);
		menuTreeService.saveMenuItem(templateLink3);

		if (templateLink4 == null) {
			templateLink4 = new PoMenuItemTemplateLink();
		}

		templateLink4.setTreeTemplate(menuTreeTemplate2);
		menuTreeService.saveMenuItem(templateLink4);
	}

	private void generateMenuItemActionLinks() {
		/*======================================*/
		/*          Generate Action Links       */
		/*======================================*/
		if (actionLink1 == null) {
			actionLink1 = new PoMenuItemActionLink();
		}
		actionLink1.setAction(action1);

		menuTreeService.saveMenuItem(actionLink1);
		assertNotNull("UID not supplied", actionLink1.getUID());

		if (actionLink2 == null) {
			actionLink2 = new PoMenuItemActionLink();
		}
		actionLink2.setAction(action2);

		menuTreeService.saveMenuItem(actionLink2);
		assertNotNull("UID not supplied", actionLink2.getUID());

		if (actionLink3 == null) {
			actionLink3 = new PoMenuItemActionLink();
		}
		actionLink3.setAction(action3);

		menuTreeService.saveMenuItem(actionLink3);
		assertNotNull("UID not supplied", actionLink3.getUID());

		if (actionLink4 == null) {
			actionLink4 = new PoMenuItemActionLink();
		}
		actionLink4.setAction(action4);
		actionLink4.setRanking(4);

		menuTreeService.saveMenuItem(actionLink4);
		assertNotNull("UID not supplied", actionLink4.getUID());

	}

	private void generateMenuItemFolders() {
		/*======================================*/
		/*          Generate Folders            */
		/*======================================*/
		if (folder1 == null) {
			folder1 = new PoMenuItemFolder();
		}
		folder1.setI18nKey("Key1");
		folder1.addChild(actionLink1);
		folder1.addChild(actionLink2);

		if (folder2 == null) {
			folder2 = new PoMenuItemFolder();
		}
		folder2.setI18nKey("Key2");
		folder2.addChild(actionLink3);
		folder2.addChild(actionLink4);

		if (folder3 == null) {
			folder3 = new PoMenuItemFolder();
		}
		folder3.setI18nKey("Key3");

		if (folder4 == null) {
			folder4 = new PoMenuItemFolder();
		}
		folder4.setI18nKey("Key4");

		if (folder5 == null) {
			folder5 = new PoMenuItemFolder();
		}
		folder5.setI18nKey("Key5");

		if (folder6 == null) {
			folder6 = new PoMenuItemFolder();
		}
		folder6.setI18nKey("Key6");

		menuTreeService.saveMenuItem(folder1);
		assertNotNull("UID not supplied for folder1", folder1.getUID());

		menuTreeService.saveMenuItem(folder2);
		assertNotNull("UID not supplied for folder2", folder2.getUID());

		menuTreeService.saveMenuItem(folder3);
		assertNotNull("UID not supplied for folder3", folder3.getUID());

		menuTreeService.saveMenuItem(folder4);
		assertNotNull("UID not supplied for folder4", folder4.getUID());

		menuTreeService.saveMenuItem(folder5);
		assertNotNull("UID not supplied for folder5", folder5.getUID());

		menuTreeService.saveMenuItem(folder6);
		assertNotNull("UID not supplied for folder6", folder6.getUID());
	}

	private void generateClientTrees() {
		/*======================================*/
		/*          Generate Client Tree        */
		/*======================================*/

		if (menuTreeClient1 == null) {
			menuTreeClient1 = new PoMenuTreeClient();
		}

		if (menuTreeClient2 == null) {
			menuTreeClient2 = new PoMenuTreeClient();
		}

		menuTreeClient1.setClient(client1);

		menuTreeClient1.addTopLevelItem(actionLink1);
		menuTreeClient1.addTopLevelItem(actionLink2);
		menuTreeClient1.addTopLevelItem(folder1);
		menuTreeClient1.addTopLevelItem(folder2);

		menuTreeClient2.setClient(client2);
		menuTreeClient2.addTopLevelItem(actionLink3);
		menuTreeClient2.addTopLevelItem(actionLink4);
		menuTreeClient2.addTopLevelItem(folder3);
		menuTreeClient2.addTopLevelItem(folder4);

		menuTreeService.saveMenuTree(menuTreeClient1);
		assertNotNull("UID not supplied for menuTreeClient1", menuTreeClient1.getUID());

		menuTreeService.saveMenuTree(menuTreeClient2);
		assertNotNull("UID not supplied for menuTreeClient2", menuTreeClient1.getUID());
	}

	public void testMenuItemActionLinkCRUD() {
		logger.info("testMenuItemCRUD()");

		// change actionLink1 and save it
		String uid1 = actionLink1.getUID();
		int ranking = actionLink1.getRanking() - 1;
		actionLink1.setRanking(ranking);
		menuTreeService.saveMenuItem(actionLink1);

		// check if actionLink1 was updated;
		assertEquals(actionLink1.getUID(), uid1);
		assertEquals(actionLink1.getRanking(), ranking);

		menuTreeService.deleteMenuItem(actionLink1);

		String uid2 = actionLink2.getUID();
		actionLink2.setAction(action3);

		menuTreeService.saveMenuItem(actionLink2);
		assertEquals(actionLink2.getUID(), uid2);

		menuTreeService.deleteMenuItem(actionLink2);

		// deleted actionLink should not exist.
		actionLink2 = menuTreeService.getMenuItemActionLink(uid1);
		assertNull(actionLink2);

		actionLink1 = menuTreeService.getMenuItemActionLink(uid2);
		assertNull(actionLink1);
	}

	private List<String> getChildUids(PoMenuItemFolder folder) {
		List<String> uids = new ArrayList<String>();
		for (PoMenuItemBase child : folder.getChilds()) {
			if (child instanceof PoMenuItemFolder) {
				uids.addAll(getChildUids((PoMenuItemFolder) child));
			}
			uids.add(child.getUID());
		}
		return uids;
	}

	/**
	 * test ensures that creating nested folders is correct.
	 * Folders should be threaten in a special way as they can
	 * have children, and to ensure that there are no infinite
	 * loops one has to check possible constellation.
	 */
	public void testCreatingMenuItemNestedFolders() {
		logger.info("testMenuItemNestedFolder()");

		folder1.addChild(folder2);
		menuTreeService.saveMenuItem(folder1);
		folder1 = menuTreeService.getMenuItemFolder(folder1.getUID());
		assertEquals(folder1.getChilds().contains(folder2), true);

		try {
			/*
			 * folder1
			 *   |
			 *   + folder2 -> try to add folder1
			 *   
			 *  add one level up parent as child, should throw exception.
			 */
			folder2.addChild(folder1);
			fail();
		}
		catch (IllegalArgumentException e) {

		}

		folder2.addChild(folder3);
		menuTreeService.saveMenuItem(folder2);
		folder2 = menuTreeService.getMenuItemFolder(folder2.getUID());
		assertEquals(folder2.getChilds().contains(folder3), true);
		try {
			/* add two level up parent as child, should throw exception.
			 * folder1 is parent of folder2 which is parent of folder3
			 *
			 * folder1
			 *   |
			 *   + folder2
			 *       |
			 *       + folder3 -> try to add folder1
			 */
			folder3.addChild(folder1);
			fail();
		}
		catch (IllegalArgumentException e) {

		}

		// add itself as child, should throw exception.
		try {
			folder1.addChild(folder1);
			fail();
		}
		catch (IllegalArgumentException e) {

		}

		// try to add null as child, should throw exception.
		try {
			folder1.addChild(null);
			fail();
		}
		catch (IllegalArgumentException e) {

		}
	}

	public void testMenuItemFolderCRUD() {
		logger.info("testMenuItemFolderCRUD()");

		// get child UIDs inside the folder
		folder1.addChild(folder2);
		int numberOfChilds = folder1.getChilds().size() + folder2.getChilds().size();
		List<String> childs1_uids = getChildUids(folder1);
		assertEquals(childs1_uids.size(), numberOfChilds);

		String uid1 = folder1.getUID();

		// delete folder
		menuTreeService.deleteMenuItem(folder1);
		folder1 = menuTreeService.getMenuItemFolder(uid1);
		assertNull(folder1);

		// check if all children were deleted too.
		for (String uid : childs1_uids) {
			assertNull(menuTreeService.getMenuItem(uid));
		}

		// folder2 is nested inside folder1 and should be deleted together with folder1
		assertNull(menuTreeService.getMenuItemFolder(folder2.getUID()));

		List<String> childs2_uids = getChildUids(folder2);
		String uid2 = folder2.getUID();
		menuTreeService.deleteMenuItem(folder2);
		folder2 = menuTreeService.getMenuItemFolder(uid2);
		assertNull(folder2);
		for (String uid : childs2_uids) {
			assertNull(menuTreeService.getMenuItem(uid));
		}
	}

	public void testMenuItemTemplateLinkCRUD() {
		logger.info("testMenuItemTemplateLink()");
		folder1.addChild(templateLink1);
		assertEquals(folder1.getChilds().contains(templateLink1), true);

		folder1.getChilds().remove(templateLink1);
		assertEquals(folder1.getChilds().contains(templateLink1), false);

		String folder1Uid = folder1.getUID();
		String templateLink1Uid = templateLink1.getUID();
		menuTreeService.deleteMenuItem(folder1);
		folder1 = menuTreeService.getMenuItemFolder(folder1Uid);
		templateLink1 = menuTreeService.getMenuItemTemplateLink(templateLink1Uid);
		assertNull(folder1);
		assertNotNull(templateLink1);
	}

	public void testMenuTreeClientCRUD() {
		logger.info("testMenuTreeClientCRUD()");
		PoClient client1 = menuTreeClient1.getClient();
		int numberOfTopLevelItems = menuTreeClient1.getTopLevelItems().size();

		menuTreeClient1 = null;
		menuTreeClient1 = menuTreeService.findMenuTreeByClient(client1);
		assertNotNull(menuTreeClient1);
		assertEquals(menuTreeClient1.getTopLevelItems().size(), numberOfTopLevelItems);

		menuTreeClient1 = menuTreeService.findMenuTreeByClient(null);
		assertNull(menuTreeClient1);
	}

	public void testMenuTreeTemplateCRUD() {
		logger.info("testMenuTreeTemplateCRUD()");
		String uid = menuTreeTemplate1.getUID();
		List<PoMenuItemTemplateLink> templateLinks = menuTreeService.findTemplateLinks(menuTreeTemplate1);

		menuTreeService.deleteMenuTree(menuTreeTemplate1);
		menuTreeTemplate1 = menuTreeService.getTreeTemplate(uid);
		assertNull(menuTreeTemplate1);

		for (PoMenuItemTemplateLink link : templateLinks) {
			PoMenuItemTemplateLink linkInDb = menuTreeService.getMenuItemTemplateLink(link.getUID());
			assertNull(linkInDb);
		}
	}

	public void testLoadAllTemplates() {
		logger.info("testLoadAllTemplates()");
		List<PoMenuTreeTemplate> templates = menuTreeService.loadAllTreeTemplates();

		// menuTreeTemplate1 and menuTreeTemplate2 were saved.
		assertEquals(templates.size(), 2);

		menuTreeService.deleteMenuTree(menuTreeTemplate1);
		templates = menuTreeService.loadAllTreeTemplates();
		assertEquals(templates.size(), 1);
	}

	public void testNoMenu() {
		logger.info("testNoMenu()");
		// TODO implement test
	}

	public void testDeleteEmptyFolders() {
		logger.info("testDeleteEmptyFolders()");
		// TODO implement test
	}

	public void testUniversallyAllowedAction() {
		logger.info("testUniversallyAllowedAction()");
		// TODO implement test
	}

	public void testPermissions() {
		logger.info("testPermissions()");

		assertNotNull(role1);
		assertNotNull(role2);
		assertNotNull(group1);
		assertNotNull(group2);
		assertNotNull(person1);
		assertNotNull(person2);

		/*
		 * folder1
		 *  |
		 *  + actionLink1
		 *  |
		 *  + actionLink2
		 *  |
		 *  + actionLink3
		 *  |
		 *  + actionLink4		
		 */

		folder1.addChild(actionLink3);
		folder1.addChild(actionLink4);

		menuTreeService.saveMenuTree(menuTreeClient1);

		// assign permission for action1 and action2 to role1
		permissionService.assignPermission(action1, role1, DateTools.yesterday(), DateTools.INFINITY,
				PoConstants.VIEW_PERMISSION_TYPE_ROLE_COMPETENCE, false, false);
		permissionService.assignPermission(action2, role1, DateTools.yesterday(), DateTools.INFINITY,
				PoConstants.VIEW_PERMISSION_TYPE_ROLE_COMPETENCE, false, false);

		roleService.assignRole(role1, person1, 0);
		roleService.assignRole(role2, group2, 0);

		final Document treeOfPerson = menuTreeService.getMenuTreeOfPerson("wef");
		assertNotNull(treeOfPerson);

		final NodeList nodeList = xpathTools.getNodesetWithXPath(treeOfPerson, "/tree");
		final NodeList rootNodeList = nodeList.item(0).getChildNodes();

		assertTrue(nodeList.item(0).hasChildNodes());

		assertTrue(checkPermissions(rootNodeList));
	}

	/**
	 * checks each node for action permissions.
	 * @param rootNodeList
	 * @return true if user has permission for each node in the
	 * tree. This should be always the case.
	 */
	private boolean checkPermissions(final NodeList rootNodeList) {
		boolean permissionsCorrect = true;
		for (int index = 0; index < rootNodeList.getLength(); index++) {
			final Node node = rootNodeList.item(index);

			final NamedNodeMap attributes = node.getAttributes();

			if (attributes.getNamedItem("action") != null && attributes.getNamedItem("id") != null) {
				final boolean hasPersonPermission = personHasPermissionForAction(attributes);
				permissionsCorrect &= hasPersonPermission;
			} else if (attributes.getNamedItem("foldercount") != null && node.hasChildNodes()) {
				permissionsCorrect &= checkPermissions(node.getChildNodes());
			}
		}

		return permissionsCorrect;
	}

	private boolean personHasPermissionForAction(final NamedNodeMap attributes) {
		final String actionId = attributes.getNamedItem("id").getNodeValue();
		final PoAction action = actionService.getAction(actionId);
		final boolean hasPersonPermission = permissionService.hasPersonPermissionForAction(person1, action);
		return hasPersonPermission;
	}

	public void testRanking() {
		logger.info("testRanking()");
		int oldRanking = actionLink1.getRanking();
		int newRanking = oldRanking - 1;
		actionLink1.setRanking(newRanking);
		final PoMenuItemFolder parentFolder = (PoMenuItemFolder) actionLink1.getParent();

		if (parentFolder != null) {
			final List<PoMenuItemBase> childs = parentFolder.getChilds();
			for (PoMenuItemBase child : childs) {
				System.out.println(child.getRanking());
			}
		}

		// TODO finish implementation
	}

	public void testGetMenuTreeOfPerson() {
		logger.info("testGetMenuTreeOfPerson()");
		org.w3c.dom.Document menu = menuTreeService.getMenuTreeOfPerson("wef");

		System.out.println(XMLTools.createStringFromW3cDoc(menu, true));

		NodeList nodes = xpathTools.getNodesetWithXPath(menu, "/tree");

		PoMenuTreeClient clientTree = menuTreeService.findMenuTreeByClient(client1);
		// there is a menu existing
		assertNotNull(menu);

		// only one tree root element.
		assertEquals(1, nodes.getLength());

		// menu has action links or folders...
		assertTrue(nodes.item(0).hasChildNodes());

		NodeList treeNodes = nodes.item(0).getChildNodes();

		for (int index = 0; index < treeNodes.getLength() - 1; index++) {
			Node node = treeNodes.item(index);

			// an action node must not have a foldercount and must not have children 
			if (isAction(node) != null) {
				checkAction(clientTree, node, index);
			} else if (isFolder(node) != null) {
				String folderUid = node.getAttributes().getNamedItem("id").getNodeValue();
				assertNotNull(folderUid);
				PoMenuItemFolder folder = menuTreeService.getMenuItemFolder(folderUid);
				assertNotNull(folder);
				assertEquals(folder.getRanking(), index);
				checkFolder(clientTree, node, index);
			}
		}

		// logout should always be the last action in the tree
		Node logoutNode = nodes.item(0).getLastChild();
		assertEquals("Logout", xpathTools.getNodeValue(logoutNode, "@text"));
		assertEquals("logout", xpathTools.getNodeValue(logoutNode, "@action"));

		//fail("Test scenario not implemented completely yet.");
	}

	private Node isFolder(Node node) {
		return node.getAttributes().getNamedItem("foldercount");
	}

	private Node isAction(Node node) {
		return node.getAttributes().getNamedItem("action");
	}

	/**
	 * this function is used to check for a valid folder tree node.
	 * @param node
	 */
	private void checkFolder(PoMenuTreeClient client, Node node, int parentIndex) {
		assertNull(isAction(node));
		PoMenuItemFolder folder = menuTreeService.getMenuItemFolder(node.getAttributes().getNamedItem("id").getNodeValue());
		assertNotNull(folder);
		assertEquals(folder.getRanking(), parentIndex);

		// folder can be nested and can also contain actions so check recursively.
		for (int index = 0; index < node.getChildNodes().getLength(); index++) {
			Node nestedNode = node.getChildNodes().item(index);

			if (isAction(nestedNode) != null) {
				checkAction(client, nestedNode, index);
			}

			if (isFolder(nestedNode) != null) {
				String folderUid = node.getAttributes().getNamedItem("id").getNodeValue();
				PoMenuItemFolder nestedFolder = menuTreeService.getMenuItemFolder(folderUid);
				assertEquals(nestedFolder.getRanking(), index);
				checkFolder(client, nestedNode, index);
			}
		}
	}

	/**
	 * this function checks for a valid action tree node in the XML document.
	 * 
	 * @param node check for valid (it should exist in the database).
	 */
	private void checkAction(PoMenuTreeClient clientTree, Node node, int parentIndex) {
		assertNull(isFolder(node));
		assertFalse(node.hasChildNodes());

		// except for logout, every action should have an id
		if (isAction(node).getNodeValue().equalsIgnoreCase("logout") == false) {
			PoAction action = actionService.getAction(node.getAttributes().getNamedItem("id").getNodeValue());
			assertNotNull(action);

			PoMenuTreeBase tree = menuTreeService.findMenuTreeByClient(clientTree.getClient());

			assertNotNull(tree);
			PoMenuItemBase itemInTree = tree.getTopLevelItems().get(parentIndex);
			assertEquals(itemInTree.getRanking(), parentIndex);
		}
	}

	public void testTemplateMenuItemInclusion() {
		logger.info("testTemplateMenuItemInclusion()");
		// TODO implement test
	}

	public void testTemplateSubmenuInclusion() {
		logger.info("testTemplateSubmenuInclusion()");
		// TODO implement test
	}

	public void testMoveUp() {
		logger.info("testMoveUp()");

		// add more actionLinks to folder
		folder1.addChild(actionLink3);
		folder1.addChild(actionLink4);

		/*
		 * after new children (actionLink3 and actionLink4) were added the structure of the tree looks like:
		 * 
		 * folder1
		 *   |
		 *   + actionLink1
		 *   |
		 *   + actionLink2
		 *   |
		 *   + actionLink3
		 *   |
		 *   + actionLink4 
		 */

		// moving actionLink1 one position up should not change the structure as it's already at the top.
		int actualPosition = folder1.getChilds().indexOf(actionLink1);
		menuTreeService.moveUp(folder1, actionLink1);
		assertEquals(actualPosition, folder1.getChilds().indexOf(actionLink1));

		assertEquals(4, folder1.getChilds().size());

		menuTreeService.moveUp(folder1, actionLink2);
		menuTreeService.saveMenuItem(actionLink2);
		menuTreeService.saveMenuTree(folder1.getMenuTree());
		assertEquals(0, actionLink2.getRanking());
		assertEquals(0, folder1.getChilds().indexOf(actionLink2));
	}

	public void testMoveDown() {
		logger.info("testMoveDown()");

		folder1.addChild(actionLink3);
		folder1.addChild(actionLink4);

		/*
		 * after new children (actionLink3 and actionLink4) were added the structure of the tree looks like:
		 * 
		 * folder1
		 *   |
		 *   + actionLink1
		 *   |
		 *   + actionLink2
		 *   |
		 *   + actionLink3
		 *   |
		 *   + actionLink4 
		 */

		// moving actionLink4 one position down, should not change the structure as it's already at the bottom.
		int actualPosition = folder1.getChilds().indexOf(actionLink4);
		menuTreeService.moveDown(folder1, actionLink4);
		assertEquals(actualPosition, folder1.getChilds().indexOf(actionLink4));

		assertEquals(4, folder1.getChilds().size());
		assertTrue(folder1.getChilds().indexOf(actionLink3) < folder1.getChilds().indexOf(actionLink4));
		assertTrue(folder1.getChilds().indexOf(actionLink4) > folder1.getChilds().indexOf(actionLink3));

		menuTreeService.moveDown(folder1, actionLink2);
		/*
		 * after moving actionLink2 one position down the new structure looks like:
		 * 
		 * folder1
		 *   |
		 *   + actionLink1
		 *   |
		 *   + actionLink3
		 *   |
		 *   + actionLink2
		 *   |
		 *   + actionLink4 
		 * 
		 */
		assertTrue(actionLink2.getRanking() > actionLink3.getRanking());
	}

	public void testMoveMenuItemBaseToTopOfTree() {
		logger.info("testMoveMenuItemBaseToTopOfTree()");
		folder1.addChild(actionLink3);
		folder1.addChild(actionLink4);
		folder1.addChild(folder2);
		folder1.addChild(folder3);

		/* 
		 * folder1
		 *   |
		 *   + actionLink1
		 *   |
		 *   + actionLink3
		 *   |
		 *   + actionLink2
		 *   |
		 *   + actionLink4 
		 *   |
		 *   + folder2
		 *   |
		 *   + folder3
		 *   
		 *   move folder2 to the top
		 */
		menuTreeService.moveToTop(folder1, folder2);

		/*
		 * after moving folder2 to the top the new structure should look like:
		 * 
		 * folder1
		 *   |
		 *   + folder2
		 *   |
		 *   + actionLink1
		 *   |
		 *   + actionLink3
		 *   |
		 *   + actionLink2
		 *   |
		 *   + actionLink4 
		 *   |
		 *   + folder3
		 * 
		 */
		// check if folder2 is at the top
		assertEquals(folder1.getChilds().indexOf(folder2), 0);
		// check if actionLink1 is on second position
		assertEquals(folder1.getChilds().indexOf(actionLink1), 1);
	}

	public void testMoveTopLevelItemDown() {
		logger.info("testMoveTopLevelItemDown");

		//generateSimpleThreeItemsTree();

		PoMenuTreeClient treeByClient = menuTreeService.findMenuTreeByClient(client2);
		assertEquals(treeByClient.getTopLevelItems().size(), 4);

		PoMenuItemBase firstElement = treeByClient.getTopLevelItems().get(0);

		assertEquals(firstElement.getRanking(), 0);

		menuTreeService.moveDown(null, firstElement);

		assertEquals(firstElement.getRanking(), 1);

		PoMenuItemBase lastElement = treeByClient.getTopLevelItems().get(3);
		assertEquals(lastElement.getRanking(), 3);
		menuTreeService.moveDown(null, lastElement);
		assertEquals(lastElement.getRanking(), 3);
	}

	public void testMoveTopLevelItemUp() {

		PoMenuTreeClient treeByClient = menuTreeService.findMenuTreeByClient(client2);
		assertEquals(treeByClient.getTopLevelItems().size(), 4);

		PoMenuItemBase firstElement = treeByClient.getTopLevelItems().get(0);
		assertEquals(firstElement.getRanking(), 0);

		menuTreeService.moveUp(null, firstElement);

		assertEquals(firstElement.getRanking(), 0);

		PoMenuItemBase lastElement = treeByClient.getTopLevelItems().get(3);
		assertEquals(lastElement.getRanking(), 3);
		menuTreeService.moveUp(null, lastElement);
		assertEquals(lastElement.getRanking(), 2);
	}

	public void testMoveMenuItemToBottomOfTree() {
		logger.info("testMoveMenuItemToBottomOfTree()");
		folder1.addChild(actionLink3);
		folder1.addChild(actionLink4);
		folder1.addChild(folder2);
		folder1.addChild(folder3);

		/* 
		 * folder1
		 *   |
		 *   + actionLink1
		 *   |
		 *   + actionLink3
		 *   |
		 *   + actionLink2
		 *   |
		 *   + actionLink4 
		 *   |
		 *   + folder2
		 *   |
		 *   + folder3
		 *   
		 *   move actionLink3 to bottom
		 */
		int actionLink3Index = folder1.getChilds().indexOf(actionLink3);
		menuTreeService.moveToBottom(folder1, actionLink3);

		/*
		 * after moving actionLink3 to the bottom the new structure should look like:
		 * 
		 * folder1
		 *   |
		 *   + actionLink1
		 *   |
		 *   + actionLink2
		 *   |
		 *   + actionLink4
		 *   |
		 *   + folder2 
		 *   |
		 *   + folder3
		 *   |
		 *   + actionLink3
		 * 
		 */

		// check if folder2 is at the bottom
		assertEquals(folder1.getChilds().indexOf(actionLink3), folder1.getChilds().size() - 1);

		// check if actionLink3 is on second position
		assertEquals(folder1.getChilds().indexOf(actionLink4), actionLink3Index);
	}

	@Override
	protected void onTearDownAfterTransaction() throws Exception {
		super.onTearDownAfterTransaction();

		logger.info("After Rollback: Number of languages=" + languageService.loadAllLanguages().size());
	}

}
