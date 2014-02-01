package at.workflow.webdesk.po.impl.test;

import java.util.Date;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.workflow.tools.XMLTools;
import at.workflow.tools.XPathTools;
import at.workflow.webdesk.po.PoActionPermissionService;
import at.workflow.webdesk.po.PoActionService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoGeneralDbService;
import at.workflow.webdesk.po.PoLanguageService;
import at.workflow.webdesk.po.PoMenuService;
import at.workflow.webdesk.po.PoModuleService;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.impl.helper.PoDataGenerator;
import at.workflow.webdesk.po.model.PoAPermissionClient;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoLanguage;
import at.workflow.webdesk.po.model.PoMenuItem;
import at.workflow.webdesk.po.model.PoModule;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRegistrationBean;
import at.workflow.webdesk.po.model.PoTextModule;
import at.workflow.webdesk.tools.WebdeskConstants;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;
import at.workflow.webdesk.tools.testing.DataGenerator;


/**
 * Unit-Tests for the PoMenuService
 * 
 * @author sdzuban, ggruber, fritzberger
 */
public class WTestPoMenuService extends AbstractTransactionalSpringHsqlDbTestCase {

	public static final String CLIENT = "MyTestClient";
	public static final String ACTION_1 = "Aktion 1";
	public static final String ACTION_2 = "Aktion 2";
	public static final String ACTION_3 = "Aktion 3";
	public static final String ACTION_4 = "Aktion 4";
	
	private static PoMenuService  menuService;
	private static PoOrganisationService  organisationService;
	private static PoActionService  actionService;
	private static PoActionPermissionService  permissionService;
	private static PoLanguageService languageService;
	private static PoModuleService moduleService;
	private static XPathTools xpathTools;
	
	private PoClient client;
	private PoAction action1, action2, action3, action4;
	private PoModule module;
	
	private PoMenuItem item1, item2;
	
	
	@Override
	protected final DataGenerator[] getDataGenerators() {
		logger.info("getDataGenerators()");
		return new DataGenerator[] { new PoDataGenerator("at/workflow/webdesk/po/impl/test/data/MenuTestData.xml", true) };
	}

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		logger.info("onSetUpAfterDataGeneration()");
		
		((PoLanguageService) getBean("PoLanguageService")).init();
		
		menuService = (PoMenuService) getBean("PoMenuService");
		organisationService = (PoOrganisationService) getBean("PoOrganisationService");
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
		
		
		if (client == null) {
			client = organisationService.findClientByName("Workflex");
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
		
		if (item1 == null)
			item1 = new PoMenuItem();
		item1.setAction(action1);
		item1.setClient(client);
		item1.setDescription("description1");
		item1.setIconPath("iconPath1");
		item1.setName("TEST1");
		item1.setRanking(2);
		item1.setTemplateId(null);
		item1.setTextModuleKey("textModuleKey1");
		item1.setValidfrom(new Date());
		item1.setValidto(new Date(DateTools.INFINITY_TIMEMILLIS));
		
		menuService.saveMenuItem(item1);
		assertNotNull("UID not supplied", item1.getUID());
		
		if (item2 == null)
			item2 = new PoMenuItem();
		item2.setAction(action2);
		item2.setClient(client);
		item2.setDescription("description2");
		item2.setIconPath("iconPath2");
		item2.setName("TEST2");
		item2.setRanking(1);
		item2.setTemplateId(null);
		item2.setTextModuleKey("textModuleKey2");
		item2.setValidfrom(new Date());
		item2.setValidto(new Date(DateTools.INFINITY_TIMEMILLIS));
		
		menuService.saveMenuItem(item2);
		assertNotNull("UID not supplied", item2.getUID());
		
		menuService.clearMenuCache();
	}
	
	@Override
	public void onSetUp() throws Exception {
		logger.info("onSetUp()");
		
		super.onSetUp();
		


	}
	
	
	public void testMenuItemCRUD() {
		logger.info("testMenuItemCRUD()");
		
		item1.setTemplateId(new Integer(1));
		menuService.saveMenuItem(item1);
		item2.setTemplateId(new Integer(2));
		menuService.saveMenuItem(item2);

		// saved in set up method
		assertNotNull("UID not supplied", item1.getUID());
		
		String uid = item1.getUID();
		
		PoMenuItem item2 = menuService.getMenuItem(uid);
		
		assertEquals(item1.getAction(), item2.getAction());
		assertEquals(item1.getClient(), item2.getClient());
		assertEquals(item1.getDescription(), item2.getDescription());
		assertEquals(item1.getIconPath(), item2.getIconPath());
		assertEquals(item1.getName(), item2.getName());
		assertEquals(item1.getRanking(), item2.getRanking());
		assertEquals(item1.getTemplateId(), item2.getTemplateId());
		assertEquals(item1.getTextModuleKey(), item2.getTextModuleKey());
		assertEquals(item1.getValidfrom(), item2.getValidfrom());
		assertEquals(item1.getValidto(), item2.getValidto());
		
		menuService.deleteMenuItem(item1);
		try {
			item2 = menuService.getMenuItem(uid);
			fail("retrieved deleted menu item");
		} catch (Exception e) {
			
		}
    }
	
    public void testTemplateId() {
		logger.info("testTemplateId()");
    	
    	item1.setTemplateId(new Integer(1));
    	item1.setClient(null);
    	menuService.saveMenuItem(item1);
    	item2.setTemplateId(new Integer(2));
    	item2.setClient(null);
    	menuService.saveMenuItem(item2);
    	
		List<Object[]> tIds = menuService.getAllTemplateIds();
		assertEquals(2, tIds.size());
		assertEquals(new Integer(1), tIds.get(0)[0]);
		assertEquals(new Integer(2), tIds.get(1)[0]);
		
		assertEquals(2, menuService.getMaxTemplateId());
		
		List<PoMenuItem> templItems = menuService.findTemplateLinks(1);
		assertEquals(0, templItems.size());
		
		List<PoMenuItem> items = menuService.findTemplateMenuItemsByTemplateId(1);
		assertNotNull(items);
		assertEquals(1, items.size());
		assertEquals("TEST1", items.get(0).getName());
		
		item1.setClient(client);
		menuService.saveMenuItem(item1);
		
		templItems = menuService.findTemplateLinks(1);
		assertEquals(1, templItems.size());
		assertEquals("TEST1", templItems.get(0).getName());
		
    }
    
    public void testFindMenuItemsByClient() {
		logger.info("testFindMenuItemsByClient()");
    	
    	List<PoMenuItem> items = menuService.findMenuItemsByClient(client);
    	
    	assertNotNull(items);
    	assertEquals(2, items.size());
    }
    
    public void testNoMenu() {
		logger.info("testNoMenu()");
    	
    	// first with items with no permissions
    	
    	Document menu = menuService.getMenuTreeOfPerson("wef");
    	
    	assertNotNull(menu);
    	NodeList nodes = xpathTools.getNodesetWithXPath(menu, "/tree");
    	assertTrue(nodes.getLength() == 1);
    	assertTrue(nodes.item(0).hasChildNodes());
    	
    	nodes = xpathTools.getNodesetWithXPath(menu, "/tree/tree");
    	assertTrue(nodes.getLength() == 1);
    	Node node = nodes.item(0);
    	assertFalse(node.hasChildNodes());
    	assertEquals("Logout", xpathTools.getNodeValue(node, "@text"));
    	assertEquals("logout", xpathTools.getNodeValue(node, "@action"));
    	assertEquals("./images/nuvola/16x16/actions/exit.png", xpathTools.getNodeValue(node, "@image"));
    	
    	// now no items at all
    	
    	menuService.deleteMenuItem(item1);
    	menuService.deleteMenuItem(item2);
    	
    	menu = menuService.getMenuTreeOfPerson("wef");
    	
    	assertNotNull(menu);
    	nodes = xpathTools.getNodesetWithXPath(menu, "/tree");
    	assertTrue(nodes.getLength() == 1);
    	assertTrue(nodes.item(0).hasChildNodes());
    	
    	nodes = xpathTools.getNodesetWithXPath(menu, "/tree/tree");
    	assertTrue(nodes.getLength() == 1);
    	node = nodes.item(0);
    	assertFalse(node.hasChildNodes());
    	assertEquals("Logout", xpathTools.getNodeValue(node, "@text"));
        assertEquals("logout", xpathTools.getNodeValue(node, "@action"));
        assertEquals("./images/nuvola/16x16/actions/exit.png", xpathTools.getNodeValue(node, "@image"));
    	
    }
    
    public void testDeleteEmptyFolders() {
		logger.info("testDeleteEmptyFolders()");
    	
    	item1.setAction(null);
    	item1.setTemplateId(null);
    	menuService.saveMenuItem(item1);
    	item2.setAction(null);
    	item2.setTemplateId(null);
    	menuService.saveMenuItem(item2);
    	
    	Document menu = menuService.getMenuTreeOfPerson("wef");
    	
    	assertNotNull(menu);
    	NodeList nodes = xpathTools.getNodesetWithXPath(menu, "/tree");
    	assertEquals(1, nodes.getLength());
    	assertTrue(nodes.item(0).hasChildNodes());
    	
    	nodes = xpathTools.getNodesetWithXPath(menu, "/tree/tree");
    	assertEquals(1, nodes.getLength());
    	Node node = nodes.item(0);
    	assertFalse(node.hasChildNodes());
    	assertEquals("Logout", xpathTools.getNodeValue(node, "@text"));
        assertEquals("logout", xpathTools.getNodeValue(node, "@action"));
    }
    
    public void testUniversallyAllowedAction() {
		logger.info("testUniversallyAllowedAction()");
    	
    	action1.setUniversallyAllowed(true);
    	actionService.saveAction(action1);
    	
    	Document menu = menuService.getMenuTreeOfPerson("wef");
    	
    	assertNotNull(menu);
    	NodeList nodes = xpathTools.getNodesetWithXPath(menu, "/tree");
    	assertEquals(1, nodes.getLength());
    	assertTrue(nodes.item(0).hasChildNodes());
    	
    	nodes = xpathTools.getNodesetWithXPath(menu, "/tree/tree");
    	assertEquals(2, nodes.getLength());
    	
    	assertActionNode(nodes.item(0), ACTION_1, true);

    	Node node = nodes.item(1);
    	assertFalse(node.hasChildNodes());
    	assertEquals("Logout", xpathTools.getNodeValue(node, "@text"));
    	assertEquals("logout", xpathTools.getNodeValue(node, "@action"));
    }

    public void testPermissions() {
		logger.info("testPermissions()");
    
    	PoAPermissionClient permission = new PoAPermissionClient();
    	permission.setClient(client);
    	permission.setAction(action2);
    	permissionService.saveAPermission(permission);
    	
    	Document menu = menuService.getMenuTreeOfPerson("wef");
    	
    	assertNotNull(menu);
    	NodeList nodes = xpathTools.getNodesetWithXPath(menu, "/tree");
    	assertEquals(1, nodes.getLength());
    	assertTrue(nodes.item(0).hasChildNodes());
    	
    	nodes = xpathTools.getNodesetWithXPath(menu, "/tree/tree");
    	assertEquals(2, nodes.getLength());
    	
    	Node node = nodes.item(0);
    	assertFalse(node.hasChildNodes());
    	assertEquals(ACTION_2 + ".do_action_caption", xpathTools.getNodeValue(node, "@text"));
    	assertEquals(ACTION_2 + ".do", xpathTools.getNodeValue(node, "@action"));

    	node = nodes.item(1);
    	assertFalse(node.hasChildNodes());
    	assertEquals("Logout", xpathTools.getNodeValue(node, "@text"));
    	assertEquals("logout", xpathTools.getNodeValue(node, "@action"));
    }   
    
    public void testRanking() {
		logger.info("testRanking()");
    	
    	PoAPermissionClient permission = new PoAPermissionClient();
    	permission.setClient(client);
    	permission.setAction(action1);
    	permissionService.saveAPermission(permission);
    	
    	permission = new PoAPermissionClient();
    	permission.setClient(client);
    	permission.setAction(action2);
    	permissionService.saveAPermission(permission);
    	
    	Document menu = menuService.getMenuTreeOfPerson("wef");
    	
    	assertNotNull(menu);
    	NodeList nodes = xpathTools.getNodesetWithXPath(menu, "/tree");
    	assertEquals(1, nodes.getLength());
    	assertTrue(nodes.item(0).hasChildNodes());
    	
    	nodes = xpathTools.getNodesetWithXPath(menu, "/tree/tree");
    	assertEquals(3, nodes.getLength());
    	
    	Node node = nodes.item(0);
    	assertFalse(node.hasChildNodes());
    	assertEquals(ACTION_2 + ".do_action_caption", xpathTools.getNodeValue(node, "@text"));
    	assertEquals(ACTION_2 + ".do", xpathTools.getNodeValue(node, "@action"));

    	node = nodes.item(1);
    	assertFalse(node.hasChildNodes());
    	assertEquals(ACTION_1 + ".act_action_caption", xpathTools.getNodeValue(node, "@text"));
    	assertEquals(ACTION_1 + ".act", xpathTools.getNodeValue(node, "@action"));
    	
    	node = nodes.item(2);
    	assertFalse(node.hasChildNodes());
    	assertEquals("Logout", xpathTools.getNodeValue(node, "@text"));
    	assertEquals("logout", xpathTools.getNodeValue(node, "@action"));
    }
    
    public void testGetMenuTreeOfPerson() {
		logger.info("testGetMenuTreeOfPerson()");
    	
    	PoMenuItem item3 = new PoMenuItem();
		item3.setAction(null);
		item3.setClient(client);
		item3.setDescription("description3");
		item3.setIconPath("iconPath3");
		item3.setName("TEST3");
		item3.setRanking(1);
		item3.setTemplateId(null);
		item3.setTextModuleKey("textModuleKey3");
		item3.setValidfrom(new Date());
		item3.setValidto(new Date(DateTools.INFINITY_TIMEMILLIS));
		item3.addChild(item1);
		item3.addChild(item2);
		
		menuService.saveMenuItem(item3);
		
    	PoAPermissionClient permission = new PoAPermissionClient();
    	permission.setClient(client);
    	permission.setAction(action1);
    	permissionService.saveAPermission(permission);
    	
    	permission = new PoAPermissionClient();
    	permission.setClient(client);
    	permission.setAction(action2);
    	permissionService.saveAPermission(permission);
    	
    	Document menu = menuService.getMenuTreeOfPerson("wef");
    	
    	assertNotNull(menu);
    	NodeList nodes = xpathTools.getNodesetWithXPath(menu, "/tree");
    	assertEquals(1, nodes.getLength());
    	assertTrue(nodes.item(0).hasChildNodes());
    	
    	nodes = xpathTools.getNodesetWithXPath(menu, "/tree/tree");
    	assertEquals(2, nodes.getLength());
    	
    	Node node = nodes.item(0);
    	assertTrue(node.hasChildNodes());
    	assertEquals("textModuleKey3", xpathTools.getNodeValue(node, "@text"));
    	assertNull(xpathTools.getNodeValue(node, "@action"));
    	
    	NodeList childs = xpathTools.getNodesetWithXPath(node, "tree");
    	assertEquals(2, childs.getLength());
    	node = childs.item(0);
    	assertFalse(node.hasChildNodes());
    	assertEquals(ACTION_2 + ".do_action_caption", xpathTools.getNodeValue(node, "@text"));
    	assertEquals(ACTION_2 + ".do", xpathTools.getNodeValue(node, "@action"));

    	node = childs.item(1);
    	assertFalse(node.hasChildNodes());
    	assertEquals(ACTION_1 + ".act_action_caption", xpathTools.getNodeValue(node, "@text"));
    	assertEquals(ACTION_1 + ".act", xpathTools.getNodeValue(node, "@action"));
    	
    	node = nodes.item(1);
    	assertFalse(node.hasChildNodes());
    	assertEquals("Logout", xpathTools.getNodeValue(node, "@text"));
    	assertEquals("logout", xpathTools.getNodeValue(node, "@action"));
    }
    
    
    public void testTemplateMenuItemInclusion() {
		logger.info("testTemplateMenuItemInclusion()");
    	
    	PoAPermissionClient permission = new PoAPermissionClient();
    	permission.setClient(client);
    	permission.setAction(action1);
    	permissionService.saveAPermission(permission);
    	
    	permission = new PoAPermissionClient();
    	permission.setClient(client);
    	permission.setAction(action2);
    	permissionService.saveAPermission(permission);
    	
    	permission = new PoAPermissionClient();
    	permission.setClient(client);
    	permission.setAction(action3);
    	permissionService.saveAPermission(permission);
    	
    	permission = new PoAPermissionClient();
    	permission.setClient(client);
    	permission.setAction(action4);
    	permissionService.saveAPermission(permission);
    	
    	Integer templateId = new Integer(5);
    	
    	PoMenuItem template = new PoMenuItem();
    	template.setName("template1");
    	template.setDescription("template1");
    	template.setClient(null);
    	template.setTemplateId( templateId );

    	menuService.saveMenuItem( template );
    	
    	
    	PoMenuItem item4 = new PoMenuItem();
    	item4.setAction(action3);
    	item4.setClient(null);
    	item4.setRanking(3);
    	item4.setTemplateId( templateId );
    	template.addChild(item4);
    	
    	menuService.saveMenuItem(item4);
    	
    	PoMenuItem item5 = new PoMenuItem();
    	item5.setAction(action4);
    	item5.setClient(null);
    	item5.setRanking(4);
    	item5.setTemplateId( templateId );
    	template.addChild(item5);
    	
    	menuService.saveMenuItem(item5);
    	

    	// reference to template
    	PoMenuItem item3 = new PoMenuItem();
		item3.setAction(null);
		item3.setClient(client);
		item3.setName("TEST3");
		item3.setRanking(4);
		item3.setTemplateId( templateId );
		item3.setTextModuleKey("textModuleKey3");

		
		menuService.saveMenuItem(item3);
		
    	Document menu = menuService.getMenuTreeOfPerson("wef");
    	
    	logger.info("menue=" + XMLTools.createStringFromW3cDoc(menu, true));
    	
    	assertNotNull(menu);
    	NodeList nodes = xpathTools.getNodesetWithXPath(menu, "/tree");
    	assertEquals(1, nodes.getLength());
    	assertTrue(nodes.item(0).hasChildNodes());
    	
    	nodes = xpathTools.getNodesetWithXPath(menu, "/tree/tree");
    	assertEquals(4, nodes.getLength());
    	
    	Node node = nodes.item(0);
    	assertTrue(node.hasChildNodes()==false);
    	
    	NodeList childs = xpathTools.getNodesetWithXPath(nodes.item(2), "tree");
    	assertEquals(2, childs.getLength());
    	
    	assertActionNode(childs.item(0), ACTION_3);
    	
    	assertActionNode(childs.item(1), ACTION_4);
    	
    	node = nodes.item(3);
    	assertFalse(node.hasChildNodes());
    	assertEquals("Logout", xpathTools.getNodeValue(node, "@text"));
    	assertEquals("logout", xpathTools.getNodeValue(node, "@action"));
    }
    
	@Ignore("see WD-118")
	@Test
    public void dotestTemplateSubmenuInclusion() {
		logger.info("testTemplateSubmenuInclusion()");
    	
    	PoAPermissionClient permission = new PoAPermissionClient();
    	permission.setClient(client);
    	permission.setAction(action1);
    	permissionService.saveAPermission(permission);
    	
    	permission = new PoAPermissionClient();
    	permission.setClient(client);
    	permission.setAction(action2);
    	permissionService.saveAPermission(permission);
    	
    	permission = new PoAPermissionClient();
    	permission.setClient(client);
    	permission.setAction(action3);
    	permissionService.saveAPermission(permission);
    	
    	permission = new PoAPermissionClient();
    	permission.setClient(client);
    	permission.setAction(action4);
    	permissionService.saveAPermission(permission);
    	
    	
    	// Build Template
    	
    	// item
    	PoMenuItem item4 = new PoMenuItem();
    	item4.setAction(action3);
    	item4.setClient(null);
    	item4.setRanking(3);
    	item4.setTemplateId(new Integer(5));
    	
    	menuService.saveMenuItem(item4);
    	
    	// item
    	PoMenuItem item5 = new PoMenuItem();
    	item5.setAction(action4);
    	item5.setClient(null);
    	item5.setRanking(4);
    	item5.setTemplateId(new Integer(5));
    	
    	menuService.saveMenuItem(item5);
    	
    	// the folder inside template
    	PoMenuItem item6 = new PoMenuItem();
    	item6.setAction(null);
    	item6.setClient(null);
    	item6.setRanking(3);
    	item6.setName("TemplateFolder");
    	item6.setTemplateId(new Integer(5));
    	item6.addChild(item4);
    	item6.addChild(item5);
    	
    	menuService.saveMenuItem(item6);
    	
    	// a sibling to the folder
    	PoMenuItem item7 = new PoMenuItem();
    	item7.setAction(action4);
    	item7.setClient(null);
    	item7.setRanking(4);
    	item7.setTemplateId(new Integer(5));
    	
    	menuService.saveMenuItem(item7);
    	
    	// the template reference
    	PoMenuItem item3 = new PoMenuItem();
    	item3.setAction(null);
    	item3.setClient(client);
    	item3.setName("TEST3");
    	item3.setRanking(1);
    	item3.setTemplateId(new Integer(5));
    	item3.setTextModuleKey("textModuleKey3");

    	
    	menuService.saveMenuItem(item3);
    	
    	Document menu = menuService.getMenuTreeOfPerson("wef");
    	
        logger.info(XMLTools.createStringFromW3cDoc(menu,true));
    	
    	assertNotNull(menu);
    	NodeList nodes = xpathTools.getNodesetWithXPath(menu, "/tree");
    	assertEquals(1, nodes.getLength());
    	assertTrue(nodes.item(0).hasChildNodes());
    	
    	nodes = xpathTools.getNodesetWithXPath(menu, "/tree/tree");
    	assertEquals(5, nodes.getLength());
    	
    	// TemplateFolder
    	Node node = nodes.item(0);
    	assertTrue(node.hasChildNodes());
    	assertFolder(node, "TemplateFolder");
    	
    	NodeList childs = xpathTools.getNodesetWithXPath(node, "tree");
    	assertEquals(2, childs.getLength());
    	assertActionNode(childs.item(0), ACTION_3);
    	assertActionNode(childs.item(1), ACTION_4);
    	
    	// ----------
    	
    	NodeList rootItems = xpathTools.getNodesetWithXPath(menu, "/tree/tree");

    	assertActionNode(rootItems.item(1), ACTION_4);
    	
    	assertActionNode(rootItems.item(2), ACTION_2);
    	
    	assertActionNode(rootItems.item(3), ACTION_1, true);
    	
    	node = rootItems.item(4);
    	
    	assertFalse(node.hasChildNodes());
    	assertEquals("Logout", xpathTools.getNodeValue(node, "@text"));
    	assertEquals("logout", xpathTools.getNodeValue(node, "@action"));
    }

	private void assertFolder(Node node, String templateName) {
		assertTrue(xpathTools.getNodeValue(node, "@text").startsWith(templateName));
    	assertNull(xpathTools.getNodeValue(node, "@action"));
	}

	private void assertActionNode(Node node, String actionNameWithoutPostfix) {
		assertActionNode(node, actionNameWithoutPostfix, false);
	}
	
	private void assertActionNode(Node node, String actionNameWithoutPostfix, boolean isAct) {
		String postfix = isAct==false?".do":".act";
		assertFalse(node.hasChildNodes());
		assertEquals(actionNameWithoutPostfix + postfix + "_action_caption", xpathTools.getNodeValue(node, "@text"));
    	assertEquals(actionNameWithoutPostfix + postfix, xpathTools.getNodeValue(node, "@action"));
	}
    
	public void testGetMenuTreeOfPersonAdmin() {
		logger.info("testGetMenuTreeOfPersonAdmin()");
		
		Document menu = menuService.getMenuTreeOfPerson(WebdeskConstants.SADMIN);
		assertNotNull(menu);
		
		NodeList nodes = xpathTools.getNodesetWithXPath(menu, "/tree");
		assertNotNull(nodes);
		assertTrue(nodes.item(0).hasChildNodes());
		nodes = xpathTools.getNodesetWithXPath(menu, "/tree/tree");
		
		
		// be aware that only those folders will appear
		// for which actions exist
		// as we have *NO* dependency on webclient modules
		// only those actions will appear which are explicitly 
		// registered manually with MenuTestData.xml
		
		assertEquals(3, nodes.getLength());  
		NodeList items = xpathTools.getNodesetWithXPath(menu, "/tree/tree/tree");
		assertEquals(4, items.getLength());
		
		assertEquals("po_masterdata", xpathTools.getNodeValue(nodes.item(0), "@text"));
		assertEquals("po_menu_and_actions", xpathTools.getNodeValue(nodes.item(1), "@text"));
		assertEquals("po_logout", xpathTools.getNodeValue(nodes.item(2), "@text"));
		
		items = xpathTools.getNodesetWithXPath(nodes.item(0), "tree");
		assertEquals(3, items.getLength());

		Node node = items.item(0);
		assertActionNode(node, "po_showClients", true);
		
		node = items.item(1);
		assertActionNode(node, "po_showOrgStructures", true);
		
		node = items.item(2);
		assertActionNode(node, "po_showRoles", true);
		
		
		items = xpathTools.getNodesetWithXPath(nodes.item(1), "tree");
		assertEquals(1, items.getLength());

		node = items.item(0);
		assertActionNode(node, "po_editMenuTree", true);
		
	}
	
	public void testRefreshTextModulesOfFolders() {
		item1.setAction(null);
		item1.setTemplateId(null);
		item1.addChild(item2);
		
		menuService.saveMenuItem(item1);
		
		PoMenuItem item3 = new PoMenuItem();
		item3.setAction(action3);
		
		menuService.saveMenuItem(item3);
		
		item2.setAction(null);
		item2.setTemplateId(null);
		item2.addChild(item3);
		
		menuService.saveMenuItem(item2);
		
		// assert that "en" and "de" are already created by Spring languageService.init() call
		List<PoLanguage> langs = languageService.loadAllLanguages();
		assertNotNull(langs);	// must exist
		assertEquals(2, langs.size());
		
		PoLanguage de = languageService.findLanguageByCode("de");
		assertNotNull(de);
		assertTrue(de.getDefaultLanguage());
		PoLanguage en = languageService.findLanguageByCode("en");
		assertNotNull(en);
		assertFalse(en.getDefaultLanguage());

		PoTextModule tm = languageService.findTextModuleByNameAndLanguage(item1.getTextModuleKey(), de);
		assertNull(tm);
		tm = languageService.findTextModuleByNameAndLanguage(item1.getTextModuleKey(), en);
		assertNull(tm);
		
		tm = languageService.findTextModuleByNameAndLanguage(item2.getTextModuleKey(), de);
		assertNull(tm);
		tm = languageService.findTextModuleByNameAndLanguage(item2.getTextModuleKey(), en);
		assertNull(tm);
		
		menuService.refreshTextModulesOfFolders(client);
		
		tm = languageService.findTextModuleByNameAndLanguage(item1.getTextModuleKey(), de);
		assertNotNull(tm);
		assertEquals("TEST1", tm.getValue());
		tm = languageService.findTextModuleByNameAndLanguage(item1.getTextModuleKey(), en);
		assertNotNull(tm);
		assertEquals("[en] TEST1", tm.getValue());

		tm = languageService.findTextModuleByNameAndLanguage(item2.getTextModuleKey(), de);
		assertNotNull(tm);
		assertEquals("TEST2", tm.getValue());
		tm = languageService.findTextModuleByNameAndLanguage(item2.getTextModuleKey(), en);
		assertNotNull(tm);
		assertEquals("[en] TEST2", tm.getValue());
	}

	public void testTrimmedMenuNotStored() {
		logger.info("testTrimmedMenuNotStored()");
		
    	PoAPermissionClient permission = new PoAPermissionClient();
    	permission.setClient(client);
    	permission.setAction(action2);
    	permissionService.saveAPermission(permission);
    	
    	PoMenuItem item3 = new PoMenuItem();
    	item3.setAction(action3);
    	item3.setClient(client);
    	
    	menuService.saveMenuItem(item3);
    	
    	item1.setAction(null);
    	item1.setTemplateId(null);
    	item1.addChild(item2);
    	item1.addChild(item3);
    	
    	menuService.saveMenuItem(item1);
    	
    	item2.setTemplateId(null);
    	
    	menuService.saveMenuItem(item2);
    	
    	Document menu = menuService.getMenuTreeOfPerson("wef");
    	
    	assertNotNull(menu);
    	NodeList nodes = xpathTools.getNodesetWithXPath(menu, "/tree");
    	assertEquals(1, nodes.getLength());
    	assertTrue(nodes.item(0).hasChildNodes());
    	
    	nodes = xpathTools.getNodesetWithXPath(menu, "/tree/tree");
    	assertEquals(2, nodes.getLength());
    	
    	Node node = nodes.item(0);
    	assertTrue(node.hasChildNodes());
    	assertEquals("textModuleKey1", xpathTools.getNodeValue(node, "@text"));
    	
    	NodeList children = xpathTools.getNodesetWithXPath(node, "tree");
    	assertEquals(1, children.getLength());
    	
    	node = children.item(0);
    	assertFalse(node.hasChildNodes());
    	assertEquals(ACTION_2 + ".do_action_caption", xpathTools.getNodeValue(node, "@text"));
    	assertEquals(ACTION_2 + ".do", xpathTools.getNodeValue(node, "@action"));

    	node = nodes.item(1);
    	assertFalse(node.hasChildNodes());
    	assertEquals("Logout", xpathTools.getNodeValue(node, "@text"));
    	assertEquals("logout", xpathTools.getNodeValue(node, "@action"));
		
    	List<PoGroup> groups = organisationService.findGroupsFromClientF(client, new Date());
    	PoGroup g01 = groups.get(0);
    	
    	PoPerson person = new PoPerson();
    	person.setFirstName("first");
    	person.setLastName("last");
    	person.setUserName("uname");
    	person.setClient(client);

    	organisationService.savePerson(person, g01);
    	
    	PoMenuItem i = menuService.getMenuItem(item1.getUID());
    	assertNotNull(i);
    	i = menuService.getMenuItem(item2.getUID());
    	assertNotNull(i);
    	i = menuService.getMenuItem(item3.getUID());
    	assertNotNull(i);
	}
	
    @Override
    protected void onTearDownAfterTransaction() throws Exception {
    	PoGeneralDbService generalDbService = (PoGeneralDbService) getBean("PoGeneralDbService");
    	
    	assertTrue(actionService.findActionByNameAndType(ACTION_1, PoConstants.ACTION_TYPE_ACTION) == null);
    	assertTrue(actionService.findActionByNameAndType(ACTION_2, PoConstants.ACTION_TYPE_ACTION) == null);
    	assertTrue(actionService.findActionByNameAndType(ACTION_3, PoConstants.ACTION_TYPE_ACTION) == null);
    	assertTrue(actionService.findActionByNameAndType(ACTION_4, PoConstants.ACTION_TYPE_ACTION) == null);
    	
    	assertTrue(generalDbService.loadAllObjectsOfType(PoMenuItem.class).size()==0);
    }

}
