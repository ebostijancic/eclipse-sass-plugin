package at.workflow.webdesk.po.impl.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.CompetenceTarget;
import at.workflow.webdesk.po.PoActionPermissionService;
import at.workflow.webdesk.po.PoActionService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.impl.helper.PoDataGenerator;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoActionParameter;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.testing.DataGenerator;

public class WTestPoActionServiceImpl extends AbstractPoTestCase {
	
    //Services
    private PoOrganisationService orgService;
    private PoActionService actionService;
    private PoActionPermissionService permissionService;
    
    
    @Override
    protected void onSetUpAfterDataGeneration() throws Exception {
    	
    	if (this.orgService==null) {
    		this.orgService = (PoOrganisationService) this.applicationContext.getBean("PoOrganisationService");
    		this.actionService = (PoActionService) this.applicationContext.getBean("PoActionService");
    		this.permissionService = (PoActionPermissionService) this.applicationContext.getBean("PoActionPermissionService");
    	}
    	
    	// provide custom data (may not interfere with data from TestData.xml!!!!
    	DataGenerator minDataGen = new PoDataGenerator("at/workflow/webdesk/po/impl/test/data/WTestPoActionServiceImplTestData.xml");
    	minDataGen.create(applicationContext);
    }
    
	public void testAddPersonToCompetenceList() {
		
		PoAction action = actionService.findActionByNameAndType("XArztgang", 
			PoConstants.ACTION_TYPE_PROCESS);
		PoPerson person = orgService.findPersonByUserName("xwef");
		
		PoGroup group = orgService.findGroupByShortName("XG01");
		PoGroup nodeGroup = orgService.findGroupByShortName("XG01_1_1");
		List<CompetenceTarget> cl = new ArrayList<CompetenceTarget>();
		cl.add(group);
		
		permissionService.assignPermissionWithCompetenceTargets(
				action, person, new Date(), new Date(DateTools.INFINITY_TIMEMILLIS), 
				cl, 1, false);
		
		Map<String, List<String>> m = permissionService.findViewPermissions(person, action, new Date());
		
		List<String> groupList = m.get(PoActionPermissionService.GROUPS);
		assertTrue("XG01 is not contained in list.", groupList.contains(group.getUID()));
		
		assertTrue("XG01_1_1 should not be contained in list.", 
				!groupList.contains(nodeGroup.getUID()));
		
		// now set view inherit to childs to true 
		permissionService.assignPermissionWithCompetenceTargets(
				action, person, new Date(), new Date(DateTools.INFINITY_TIMEMILLIS), 
				cl, 1, true);
		
		m = permissionService.findViewPermissions(person, action, new Date());
		groupList = m.get(PoActionPermissionService.GROUPS);
		
		assertTrue("XG01_1_1 is not contained in list.", 
				groupList.contains(nodeGroup.getUID()));
		
	}
	
	public void testAddGroupToCompetenceList() {
		
		PoAction action = actionService.findActionByNameAndType("XArztgang", PoConstants.ACTION_TYPE_PROCESS);
		PoPerson ham = orgService.findPersonByUserName("xham");
		
		PoGroup group = orgService.findGroupByShortName("XG01");
		PoGroup nodeGroup = orgService.findGroupByShortName("XG01_1_1");
		List<CompetenceTarget> cl = new ArrayList<CompetenceTarget>();
		cl.add(group);
		
		permissionService.assignPermissionWithCompetenceTargets(
				action, group, new Date(), new Date(DateTools.INFINITY_TIMEMILLIS), 
				cl, 1, false, false);
		
		Map<String, List<String>> permissionMap = permissionService.findViewPermissions(ham, action, new Date());
		
		List<String> groupList = permissionMap.get(PoActionPermissionService.GROUPS);
		assertNotNull(groupList);
		
		assertTrue("G01 is not contained in list.", groupList.contains(group.getUID()));
		assertTrue("G01_1_1 should not be contained in list.", ! groupList.contains(nodeGroup.getUID()));
		
		// now set view inherit to childs to true 
		permissionService.assignPermissionWithCompetenceTargets(
				action, group, new Date(), new Date(DateTools.INFINITY_TIMEMILLIS), 
				cl, 1, true, true);
		
		permissionMap = permissionService.findViewPermissions(ham, action, new Date());
		groupList = permissionMap.get(PoActionPermissionService.GROUPS);
		assertNotNull(groupList);
		
		assertTrue("G01_1_1 is not contained in list.", groupList.contains(nodeGroup.getUID()));
		
		PoPerson wuh = orgService.findPersonByUserName("xwuh");
		permissionMap = permissionService.findViewPermissions(wuh, action, new Date());
		groupList = permissionMap.get(PoActionPermissionService.GROUPS);
		assertNotNull(groupList);
		// because inheritToChilds is set to true, wuh also receives the permissions
		assertTrue("G01 is not contained in list.", groupList.contains(group.getUID()));
	}
	
	public void testAddClientToPersonCompetenceList() {
		
		PoAction action = actionService.findActionByNameAndType("XArztgang", PoConstants.ACTION_TYPE_PROCESS);
		PoPerson ham = orgService.findPersonByUserName("xham");
		
		PoClient client2 = new PoClient();
		client2.setName("client2");
		orgService.saveClient(client2);
		List<CompetenceTarget> cl = new ArrayList<CompetenceTarget>();
		cl.add(client2);
		
		permissionService.assignPermissionWithCompetenceTargets(
				action, ham, new Date(), new Date(DateTools.INFINITY_TIMEMILLIS), 
				cl, 1, false);
		
		Map<String, List<String>> permissionMap = permissionService.findViewPermissions(ham, action, new Date());
		
		List<String> clientList = permissionMap.get(PoActionPermissionService.CLIENTS);
		assertNotNull(clientList);
		assertEquals(1, clientList.size());
		assertTrue("client2 is not contained in list.", clientList.contains(client2.getUID()));
		
		PoPerson xwuh = orgService.findPersonByUserName("xwuh");
		permissionMap = permissionService.findViewPermissions(xwuh, action, new Date());
		clientList = permissionMap.get(PoActionPermissionService.CLIENTS);
		assertNull(clientList);
	}
	
	public void testAddClientToGroupCompetenceList() {
		
		PoAction action = actionService.findActionByNameAndType("XArztgang", PoConstants.ACTION_TYPE_PROCESS);
		PoGroup group = orgService.findGroupByShortName("XG01");
		PoPerson ham = orgService.findPersonByUserName("xham");
		
		PoClient client2 = new PoClient();
		client2.setName("client2");
		orgService.saveClient(client2);
		List<CompetenceTarget> cl = new ArrayList<CompetenceTarget>();
		cl.add(client2);
		
		permissionService.assignPermissionWithCompetenceTargets(
				action, group, new Date(), new Date(DateTools.INFINITY_TIMEMILLIS), 
				cl, 1, false, false);
		
		Map<String, List<String>> permissionMap = permissionService.findViewPermissions(ham, action, new Date());
		
		List<String> clientList = permissionMap.get(PoActionPermissionService.CLIENTS);
		assertNotNull(clientList);
		
		assertTrue("client2 is not contained in list.", clientList.contains(client2.getUID()));
		
		PoPerson xwuh = orgService.findPersonByUserName("xwuh");
		permissionMap = permissionService.findViewPermissions(xwuh, action, new Date());
		clientList = permissionMap.get(PoActionPermissionService.CLIENTS);
		assertNull(clientList);
	}
	
	public void testGetBestConfigFromAction() {
		
		PoPerson weiss = orgService.findPersonByUserName("xwef");
		PoPerson haider = orgService.findPersonByUserName("xham");
		PoPerson wurzer = orgService.findPersonByUserName("xwuh");
		
		PoAction action = actionService.findActionByNameAndType("getPersonalDataConfig1", PoConstants.ACTION_TYPE_CONFIG);
		
		List<CompetenceTarget> compTargets= new ArrayList<CompetenceTarget>();
		compTargets.add(haider);
		
		permissionService.assignPermissionWithCompetenceTargets(action, weiss, new Date(), new Date(DateTools.INFINITY_TIMEMILLIS), compTargets, 1, false);
		permissionService.assignPermission(action, haider, new Date(), new Date(DateTools.INFINITY_TIMEMILLIS), PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON);
		
		action = actionService.findActionByNameAndType("getPersonalDataConfig2", PoConstants.ACTION_TYPE_CONFIG);
		compTargets.clear();
		compTargets.add(orgService.findPersonByUserName("xwuh"));
		permissionService.assignPermissionWithCompetenceTargets(action, weiss, new Date(), new Date(DateTools.INFINITY_TIMEMILLIS), compTargets, 1, false);
		permissionService.assignPermission(action, wurzer, new Date(), new Date(DateTools.INFINITY_TIMEMILLIS), PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON);
		
		PoAction parentAction = actionService.getAction(action.getParent().getUID());
		
		PoAction returnedAction = actionService.getConfigFromAction(weiss, parentAction, wurzer.getUID());
		assertNotNull(returnedAction);
		assertEquals("The returned Action should be getPersonalDataConfig2, but was " + returnedAction.getName(), "getPersonalDataConfig2", returnedAction.getName());
		returnedAction = actionService.getConfigFromAction(weiss, parentAction, haider.getUID());
		assertNotNull(returnedAction);
		assertEquals("The returned Actoin should be getPersonalDataConfig1, but was " + returnedAction.getName(), "getPersonalDataConfig1", returnedAction.getName());
		
	}
	
	public void testSaveActionWithParameters() {
		PoAction action = new PoAction();
		action.setActionType(PoConstants.ACTION_TYPE_ACTION);
		action.setActionFolder("po");
		action.setName("po_testActionParameter");
		action.setConfigurable(false);
		
		PoActionParameter aParameter = new PoActionParameter();
		aParameter.setName("time");
		aParameter.setPattern("dd.MM.yyyy");
		aParameter.setType("something");
		action.addParameter(aParameter);
		
		this.actionService.saveAction(action);
		
		PoAction newAction = this.actionService.findActionByURL("po_testActionParameter.act");
		assertTrue(newAction!=null);
		assertTrue(newAction.getParameters().size()==1);
	}


}
