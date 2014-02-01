package at.workflow.webdesk.po.impl.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import org.junit.Ignore;
import org.junit.Test;
import at.workflow.webdesk.po.CompetenceTarget;
import at.workflow.webdesk.po.PoActionPermissionService;
import at.workflow.webdesk.po.PoActionService;
import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.PoRoleService;
import at.workflow.webdesk.po.model.PoAPermissionBase;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoHistorization;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;

/**
 * @author hentner
 * 
 * TODO fri_23-02-2011 split this test in several smaller ones, grouped around the services they use.
 */
public class WTestPoActionServiceImplPermissions extends AbstractTransactionalSpringHsqlDbTestCase {
	
	 private PoClient myClient;

	 private PoPerson myPerson;
	 private PoPerson myPerson2;
	 private PoPerson myPerson3;
	 private PoPerson myPerson4;
	 private PoPerson expiredPerson;

     
     private PoClient myClient2;
     private PoOrgStructure myStructure2;
     private PoGroup myGroupC2;
     private PoPerson myPersonC2;
     
	 private PoGroup myGroup;
	 private PoGroup myGroup2;
	 private PoGroup myGroup3;
	 
	 private PoGroup sub_myGroup;
	 private PoGroup sub_sub_myGroup;
	 private PoGroup sub_sub_sub_myGroup;
	 
	 private PoOrgStructure myStructure;

	 private PoRole myRole;
	 private PoRole myRole2;
	 private PoRole myRole3;
	 
	 private PoAction myAction;
	 private PoAction myAction2;
	 
	 private Calendar future;
	 
	 int countRoles;
	 int countRoleHolders;
     
     int countClients;

	private PoOrganisationService orgService;
	private PoActionService actionService;
	private PoActionPermissionService permissionService;
	private PoRoleService roleService;

	
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		
    	if (this.orgService==null) {
    		this.orgService = (PoOrganisationService) this.applicationContext.getBean("PoOrganisationService");
    		this.actionService = (PoActionService) this.applicationContext.getBean("PoActionService");
    		this.permissionService = (PoActionPermissionService) this.applicationContext.getBean("PoActionPermissionService");
    		this.roleService = (PoRoleService) this.applicationContext.getBean("PoRoleService");
    	}

		super.onSetUpAfterDataGeneration();
		
        // Mandant anlegen
		countClients = this.orgService.loadAllClients().size();
        myClient = new PoClient();
		myClient.setName("myClient");
		myClient.setDescription("myClient");
		orgService.saveClient(myClient);
        
        myClient2 = new PoClient();
        myClient2.setName("myClient2");
        myClient2.setDescription("myClient2");
        orgService.saveClient(myClient2);
		// Person anlegen
		// ####################
		
//		 OrgStructure anlegen
		// ####################		
		myStructure = new PoOrgStructure();
		myStructure.setName("Organigramm");
		myStructure.setHierarchy(true);
		myStructure.setAllowOnlySingleGroupMembership(true);
		myStructure.setOrgType(PoOrgStructure.STRUCTURE_TYPE_ORGANISATION_HIERARCHY);
		myStructure.setClient(myClient);
		orgService.saveOrgStructure(myStructure);
        
        myStructure2 = new PoOrgStructure();
        myStructure2.setName("Organigramm");
        myStructure2.setHierarchy(true);
		myStructure2.setAllowOnlySingleGroupMembership(true);
        myStructure2.setOrgType(PoOrgStructure.STRUCTURE_TYPE_ORGANISATION_HIERARCHY);
        myStructure2.setClient(myClient2);
        orgService.saveOrgStructure(myStructure2);
        
        // Gruppe anlegen
		// ####################		

		myGroup = new PoGroup();
		myGroup.setClient(myClient);
		myGroup.setOrgStructure(myStructure);
		myGroup.setShortName("myGroup");
		myGroup.setName("myGroup");
		myGroup.setDescription("myGroup");
		myGroup.setValidfrom(DateTools.yesterday());
		orgService.saveGroup(myGroup);
        
        myGroupC2 = new PoGroup();
        myGroupC2.setClient(myClient2);
        myGroupC2.setOrgStructure(myStructure2);
        myGroupC2.setShortName("myGroupC2");
        myGroupC2.setName("myGroupC2");
        myGroupC2.setDescription("myGroupC2");
		myGroupC2.setValidfrom(DateTools.yesterday());
        orgService.saveGroup(myGroupC2);
        
        myPersonC2 = new PoPerson();
        // Link auf beiden Seiten
        myPersonC2.setClient(myClient2);
        myPersonC2.setFirstName("C2 p");
        myPersonC2.setLastName("C2 p");
        myPersonC2.setEmployeeId("00006589");
        myPersonC2.setUserName("c2_user");
        orgService.savePerson(myPersonC2,myGroupC2);
		
		myPerson = new PoPerson();
		// Link auf beiden Seiten
		myPerson.setClient(myClient);
		myPerson.setFirstName("myPerson");
		myPerson.setLastName("myPerson");
		myPerson.setEmployeeId("00004711");
		myPerson.setUserName("mul");
		orgService.savePerson(myPerson,myGroup);
		
		myPerson2 = new PoPerson();
		// Link auf beiden Seiten
		myPerson2.setClient(myClient);
		myPerson2.setFirstName("myPerson2");
		myPerson2.setLastName("myPerson2");
		myPerson2.setEmployeeId("00004712");
		myPerson2.setUserName("enh");
		orgService.savePerson(myPerson2,myGroup);
		

		
		// Gruppe anlegen
		// ####################		
		// mygroup existiert bereits (weiter oben) wird benötigt.
		
		myGroup2 = new PoGroup();
		myGroup2.setClient(myClient);
		myGroup2.setOrgStructure(myStructure);
		myGroup2.setShortName("myGroup2");
		myGroup2.setName("myGroup2");
		myGroup2.setDescription("desc");
		myGroup2.setValidfrom(DateTools.yesterday());
		orgService.saveGroup(myGroup2);
		
		myGroup3 = new PoGroup();
		myGroup3.setClient(myClient);
		myGroup3.setOrgStructure(myStructure);
		myGroup3.setShortName("myGroup3");
		myGroup3.setName("myGroup3");
		myGroup3.setDescription("myGroup3");
		myGroup3.setValidfrom(DateTools.yesterday());
		orgService.saveGroup(myGroup3);
		
		sub_myGroup = new PoGroup();
		sub_myGroup.setClient(myClient);
		sub_myGroup.setOrgStructure(myStructure);
		sub_myGroup.setShortName("sub_myGroup");
		sub_myGroup.setName("sub_myGroup");
		sub_myGroup.setDescription("sub_myGroup");
		sub_myGroup.setValidfrom(DateTools.yesterday());
		orgService.saveGroup(sub_myGroup);
		
		sub_sub_myGroup = new PoGroup();
		sub_sub_myGroup.setClient(myClient);
		sub_sub_myGroup.setOrgStructure(myStructure);
		sub_sub_myGroup.setShortName("sub_sub_myGroup");
		sub_sub_myGroup.setName("sub_sub_myGroup");
		sub_sub_myGroup.setDescription("desc");
		sub_sub_myGroup.setValidfrom(DateTools.yesterday());
		orgService.saveGroup(sub_sub_myGroup);

        myPerson3 = new PoPerson();
        myPerson3.setClient(myClient);
        myPerson3.setFirstName("myPerson3");
        myPerson3.setLastName("myPerson3");
        myPerson3.setEmployeeId("00004713");
        myPerson3.setUserName("mum");
        orgService.savePerson(myPerson3,sub_sub_myGroup);
        
		sub_sub_sub_myGroup = new PoGroup();
		sub_sub_sub_myGroup.setClient(myClient);
		sub_sub_sub_myGroup.setOrgStructure(myStructure);
		sub_sub_sub_myGroup.setShortName("sub_sub_sub_myGroup");
		sub_sub_sub_myGroup.setName("sub_sub_sub_myGroup");
		sub_sub_sub_myGroup.setDescription("sub_sub_sub_myGroup");
		sub_sub_sub_myGroup.setValidfrom(DateTools.yesterday());
		orgService.saveGroup(sub_sub_sub_myGroup);
		
		
        myPerson4 = new PoPerson();
        myPerson4.setClient(myClient);
        myPerson4.setFirstName("myPerson4");
        myPerson4.setLastName("myPerson4");
        myPerson4.setEmployeeId("00004714");
        myPerson4.setUserName("mum4");
        orgService.savePerson(myPerson4,sub_sub_sub_myGroup);
        
        expiredPerson = new PoPerson();
        expiredPerson.setClient(myClient);
        expiredPerson.setFirstName("expiredPerson");
        expiredPerson.setLastName("expiredPerson");
        expiredPerson.setEmployeeId("00004715");
        expiredPerson.setValidfrom(DateTools.yesterday());
        expiredPerson.setValidto(DateTools.yesterday());
        expiredPerson.setUserName("exp");
        orgService.savePerson(expiredPerson,sub_sub_sub_myGroup);

        // link groups together to make a tree
		orgService.setParentGroup(sub_myGroup,myGroup);
		orgService.setParentGroup(sub_sub_myGroup,sub_myGroup);
		orgService.setParentGroup(sub_sub_sub_myGroup,sub_sub_myGroup);
		
		countRoles = roleService.loadAllRoles().size();
		myRole = new PoRole();
		myRole.setClient(myClient);
		myRole.setDescription("My Role Description");
		myRole.setName("my role name");
		myRole.setValidfrom(new Date());
		myRole.setValidto(new Date(DateTools.INFINITY_TIMEMILLIS));
		roleService.saveRole(myRole);
		
		myRole2 = new PoRole();
		myRole2.setClient(myClient);
		myRole2.setDescription("My Role Description 2");
		myRole2.setName("my role name future");
		future = new GregorianCalendar();
		future.add(Calendar.MONTH,1);
		myRole2.setValidfrom(future.getTime());
		myRole2.setValidto(new Date(DateTools.INFINITY_TIMEMILLIS));
		roleService.saveRole(myRole2);
		
		myRole3 = new PoRole();
		myRole3.setClient(myClient);
		myRole3.setDescription("My Role Description 3");
		myRole3.setName("my role name 3");
		myRole3.setValidfrom(new Date());
		myRole3.setValidto(new Date(DateTools.INFINITY_TIMEMILLIS));
		roleService.saveRole(myRole3);
		
		myAction = new PoAction();
		myAction.setAllowUpdateOnVersionChange(false);
		myAction.setChilds(null);
		myAction.setDescription("myAction");
		myAction.setFiles(null);
		myAction.setName("myAction");
		myAction.setParent(null);
		myAction.setActionFolder("myAction");
		myAction.setDefaultViewPermissionType(0);
		//myAction.setPermissions(null); würde in Nullpointerexception enden
		myAction.setRanking(1);
		myAction.setTextModules(null);
		actionService.saveAction(myAction);
		
		
		myAction2 = new PoAction();
		myAction2.setAllowUpdateOnVersionChange(false);
		myAction2.setChilds(null);
		myAction2.setDescription("myAction2");
		myAction2.setFiles(null);
		myAction2.setName("myAction2");
		myAction2.setParent(null);
		actionService.saveAction(myAction2);
		
	}

	
    /***************************************************************
     * ZUORDNUNG AKTION ZU EINER PERSON
     ***************************************************************
     *
     ***************************************************************/
    
    /*
     *  viewPermission ist 0 ---> Berechtigung für eigene Person
     * 
     */
    public void testAddPersonToAction_VP_0() {
    	
		permissionService.assignPermission(myAction,myPerson,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON);
		Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.PERSONS).size(),1) ;
        assertEquals(m.get(PoActionPermissionService.PERSONS).get(0),myPerson.getUID());
        assertNull(m.get(PoActionPermissionService.GROUPS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
    
    /*
     *  viewPermission ist 1 ---> Berechtigung für eigene hierarchische Gruppe
     * 
     */
    public void testAddPersonToAction_VP_1() {
    	
        permissionService.assignPermission(myAction,myPerson,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.GROUPS).size(),1);
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(this.orgService.getPersonsHierarchicalGroup(myPerson,new Date()).getUID()));
        assertNull(m.get(PoActionPermissionService.PERSONS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
    /*
     *  viewPermission ist 2 ---> Berechtigung für eigene hierarchische Gruppe + Untergruppen
     * 
     */
    public void testAddPersonToAction_VP_2() {
    	
        permissionService.assignPermission(myAction,myPerson,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.GROUPS).size(),4);
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(this.orgService.getPersonsHierarchicalGroup(myPerson,new Date()).getUID()));
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_myGroup.getUID()));
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_sub_myGroup.getUID()));
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_sub_sub_myGroup.getUID()));
        assertNull(m.get(PoActionPermissionService.PERSONS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
    /*
     *  viewPermission ist 3 ---> Berechtigung wird durch dummy-Role festgelegt
     *  viewInheritToChild = false
     */
    public void testAddPersonToAction_VP_3() {
    	
        List<CompetenceTarget> competenceList = new ArrayList<CompetenceTarget>();
        competenceList.add(myPerson3);
        competenceList.add(myPerson2);
        permissionService.assignPermissionWithCompetenceTargets(myAction,myPerson,new Date(),null,competenceList,1,false);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.PERSONS).size(),2);
        assertNull(m.get(PoActionPermissionService.CLIENTS));
        assertNull(m.get(PoActionPermissionService.GROUPS));
    }
    
    
    /*
     *  viewPermission ist 3 ---> Berechtigung wird durch dummy-Role festgelegt
     *  viewInheritToChild = true
     */
    public void testAddPersonToAction_VP_3_itc() {
    	
        List<CompetenceTarget> competenceList = new ArrayList<CompetenceTarget>();
        competenceList.add(myPerson3);
        competenceList.add(myGroup);
        competenceList.add(myClient);
        competenceList.add(myClient2);
        permissionService.assignPermissionWithCompetenceTargets(myAction,myPerson,new Date(),null,competenceList,1,true);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.PERSONS).size(),1);
        assertEquals(m.get(PoActionPermissionService.CLIENTS).size(),2);
        assertEquals(m.get(PoActionPermissionService.GROUPS).size(),4);
    }
    
    /*
     *  viewPermission ist 3 ---> Berechtigung wird durch dummy-Role festgelegt
     *  viewInheritToChild = true
     */
    public void testAddPersonToAction_VP_3_itc_2() {
    	
    	List<CompetenceTarget> competenceList = new ArrayList<CompetenceTarget>();
        competenceList.add(myPerson);
        competenceList.add(myGroup);
        competenceList.add(myClient);
        permissionService.assignPermissionWithCompetenceTargets(myAction, myPerson, new Date(),null,competenceList,1,true);
        
        // load CompTargets and see if group is contained
        // see if SubGroups are contained
        List<CompetenceTarget> compTargets = permissionService.findCompetenceTargetForAction(myPerson,myAction,new Date());
        assertTrue( compTargets.contains(myGroup) );
        assertTrue( compTargets.contains(sub_myGroup) );
        assertTrue( compTargets.contains(sub_sub_myGroup) );
        assertTrue( compTargets.contains(myClient) );
        
    }
    
    
    /*
     *  viewPermission ist 4 ---> Berechtigung für eigenen Klienten
     * 
     */
    public void testAddPersonToAction_VP_4() {
    	
        permissionService.assignPermission(myAction,myGroup,new Date(),null,false,PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.CLIENTS).size(),1);
        assertNull(m.get(PoActionPermissionService.PERSONS));
        assertNull(m.get(PoActionPermissionService.GROUPS));
    }
    
    
    /*
     *  viewPermission ist 5 ---> Berechtigung für eigenen Klienten
     * 
     */
    public void testAddPersonToAction_VP_5() {
        permissionService.assignPermission(myAction,myGroup,new Date(),null,false,PoConstants.VIEW_PERMISSION_TYPE_ALL_CLIENTS);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.CLIENTS).size(),countClients + 2);
        assertNull(m.get(PoActionPermissionService.PERSONS));
        assertNull(m.get(PoActionPermissionService.GROUPS));
    }
    

    /***************************************************************
     * ZUORDNUNG AKTION ZU EINER GRUPPE
     ***************************************************************
     *
     ***************************************************************/
    
    /*
     *  viewPermission ist 0 ---> Berechtigung für eigene Person
     * 
     * inherit to child is set to false
     *      
     */
    public void testAddGroupToAction_VP_0() {
    	
        permissionService.assignPermission(myAction,myGroup,new Date(),null,false,PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.PERSONS).size(),1);
        assertEquals(m.get(PoActionPermissionService.PERSONS).get(0),myPerson.getUID());
        assertNull(m.get(PoActionPermissionService.GROUPS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
    /*
     *  viewPermission ist 0 ---> Berechtigung für eigene Person
     * 
     * inherit to child is set to true
     * 
     * The action is assigned to "Abt4711" (myGroup), which is the topLevelGroup of the OrgHierarchy.
     * myPerson3 is assigned to sub_sub_myGroup, which is two levels below "Abt4711"    
     *      
     */
    public void testAddGroupToAction_VP_0_itc() {
    	
        permissionService.assignPermission(myAction,myGroup,new Date(),null,true,PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson3,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.PERSONS).size(),1);
        assertEquals(m.get(PoActionPermissionService.PERSONS).get(0),myPerson3.getUID());
        assertNull(m.get(PoActionPermissionService.GROUPS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }

    /*
     *  viewPermission ist 1 ---> Berechtigung für eigene hierarchische Gruppe
     * 
     */
    public void testAddGroupToAction_VP_1() {
    	
        permissionService.assignPermission(myAction,myGroup,new Date(),null,false,PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.GROUPS).size(),1);
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(this.orgService.getPersonsHierarchicalGroup(myPerson,new Date()).getUID()));
        assertNull(m.get(PoActionPermissionService.PERSONS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
    
    /*
     *  viewPermission ist 1 ---> Berechtigung für eigene hierarchische Gruppe
     * 
     */
    public void testAddGroupToAction_VP_1_itc() {
    	
        permissionService.assignPermission(myAction,myGroup,new Date(),null,true,PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson3,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.GROUPS).size(),1);
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_sub_myGroup.getUID()));
        assertNull(m.get(PoActionPermissionService.PERSONS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
    /*
     *  viewPermission ist 2 ---> Berechtigung für eigene hierarchische Gruppe + Untergruppen
     * 
     */
    public void testAddGroupToAction_VP_2() {
    	
        permissionService.assignPermission(myAction,myGroup,new Date(),null,false,PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.GROUPS).size(),4);
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(this.orgService.getPersonsHierarchicalGroup(myPerson,new Date()).getUID()));
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_myGroup.getUID()));
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_sub_myGroup.getUID()));
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_sub_sub_myGroup.getUID()));
        assertNull(m.get(PoActionPermissionService.PERSONS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
    /*
     *  viewPermission ist 2 ---> Berechtigung für eigene hierarchische Gruppe + Untergruppen
     *  inheritToChild=true (myGroup inherits action to myPerson3)
     * 
     */
    public void testAddGroupToAction_VP_2_itc() {
    	
        permissionService.assignPermission(myAction,myGroup,new Date(),null,true,PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson3,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.GROUPS).size(),2);
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_sub_myGroup.getUID())); // myPerson3 's hierarchical group
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_sub_sub_myGroup.getUID()));
        assertNull(m.get(PoActionPermissionService.PERSONS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
    /*
     *  viewPermission ist 3 ---> Berechtigung wird durch dummy-Role festgelegt
     *  inheritToChilds and viewInheritToChilds =false
     */
    public void testAddGroupsToAction_VP_3() {
    	
        List<CompetenceTarget> competenceList = new ArrayList<CompetenceTarget>();
        competenceList.add(myPerson3);
        competenceList.add(myPerson2);
        competenceList.add(sub_sub_sub_myGroup);
        competenceList.add(myClient);
        competenceList.add(myClient2);
        permissionService.assignPermissionWithCompetenceTargets(myAction,myGroup,new Date(),null,competenceList,1,false,false);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.PERSONS).size(),2);
        assertEquals(m.get(PoActionPermissionService.CLIENTS).size(),2);
        assertEquals(m.get(PoActionPermissionService.GROUPS).size(),1);
    }
    
    
    /*
     *  viewPermission ist 3 ---> Berechtigung wird durch dummy-Role festgelegt
     *  inheritToChilds = true, viewInheritToChilds =false
     */
    public void testAddGroupsToAction_VP_3_itc() {
    	
        List<CompetenceTarget> competenceList = new ArrayList<CompetenceTarget>();
        competenceList.add(myPerson2);
        competenceList.add(sub_sub_sub_myGroup);
        competenceList.add(myClient2);
        permissionService.assignPermissionWithCompetenceTargets(myAction,myGroup,new Date(),null,competenceList,1,true,false);
        // myPerson3 is in sub_sub_myGroup
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson3,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.PERSONS).size(),1);
        assertEquals(m.get(PoActionPermissionService.PERSONS).get(0),myPerson2.getUID());
        assertEquals(m.get(PoActionPermissionService.CLIENTS).size(),1);
        assertEquals(m.get(PoActionPermissionService.CLIENTS).get(0),myClient2.getUID());
        assertEquals(m.get(PoActionPermissionService.GROUPS).size(),1);
        assertEquals(m.get(PoActionPermissionService.GROUPS).get(0),sub_sub_sub_myGroup.getUID());
    }
    
    /*
     *  viewPermission ist 3 ---> Berechtigung wird durch dummy-Role festgelegt
     * 
     *  inheritToChilds = true, viewInheritToChilds =true
     * 
     */
    public void testAddGroupsToAction_VP_3_itc_2() { 
    	
        List<CompetenceTarget> competenceList = new ArrayList<CompetenceTarget>();
        competenceList.add(myPerson2);
        competenceList.add(sub_sub_myGroup);
        competenceList.add(myClient);
        permissionService.assignPermissionWithCompetenceTargets(myAction,sub_myGroup,new Date(),null,competenceList,1,true,true);
        // myPerson3 is in sub_sub_myGroup
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson3,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.PERSONS).size(),1);
        assertEquals(m.get(PoActionPermissionService.CLIENTS).size(),1);
        assertEquals(m.get(PoActionPermissionService.CLIENTS).get(0),myClient.getUID());
        assertEquals(m.get(PoActionPermissionService.GROUPS).size(),2);
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_sub_myGroup.getUID()));
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_sub_sub_myGroup.getUID()));
        
    }
    
    
    /*
     *  viewPermission ist 3 ---> Berechtigung wird durch dummy-Role festgelegt
     * 
     *  inheritToChilds = false, viewInheritToChilds =true
     * 
     */
    public void testAddGroupsToAction_VP_3_itc_3() {
    	
        List<CompetenceTarget> competenceList = new ArrayList<CompetenceTarget>();
        competenceList.add(myPerson2);
        competenceList.add(sub_sub_myGroup);
        competenceList.add(myClient2);
        permissionService.assignPermissionWithCompetenceTargets(myAction,sub_sub_myGroup,new Date(),null,competenceList,1,false,true);
        // myPerson3 is in sub_sub_myGroup
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson3,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.PERSONS).size(),1);
        assertEquals(m.get(PoActionPermissionService.CLIENTS).size(),1);
        assertEquals(m.get(PoActionPermissionService.CLIENTS).get(0),myClient2.getUID());
        assertEquals(m.get(PoActionPermissionService.GROUPS).size(),2);
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_sub_myGroup.getUID()));
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_sub_sub_myGroup.getUID()));
    }
    
    /*
     *  viewPermission ist 3 ---> Berechtigung wird durch dummy-Role festgelegt
     * 
     *  inheritToChilds = false, viewInheritToChilds =true
     * 
     */
    public void testAddGroupsToAction_VP_3_itc_4() {
    	
        List<CompetenceTarget> competenceList = new ArrayList<CompetenceTarget>();
        competenceList.add(myPerson);
        competenceList.add(myGroup);
        competenceList.add(myClient);
        permissionService.assignPermissionWithCompetenceTargets(myAction,myGroup,new Date(),null,competenceList,1,true,true);
        
        // load CompTargets and see if group is contained
        // see if SubGroup is contained
        List<CompetenceTarget> compTargets = permissionService.findCompetenceTargetForAction(myGroup,myAction,new Date());
        assertTrue( compTargets.contains(myClient) );
        assertTrue( compTargets.contains(myGroup) );
        assertTrue( compTargets.contains(sub_myGroup) );
        assertTrue( compTargets.contains(sub_sub_myGroup) );
        assertTrue( compTargets.contains(myPerson) );
        
    }

    
    /*
     *  viewPermission ist 4 ---> Berechtigung für eigenen Klienten
     * 
     */
    public void testAddGroupsToAction_VP_4() {
    	
        permissionService.assignPermission(myAction,myGroup,new Date(),null,false,PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.CLIENTS).size(),1);
        assertNull(m.get(PoActionPermissionService.PERSONS));
        assertNull(m.get(PoActionPermissionService.GROUPS));
    }
    /*
     *  viewPermission ist 4 ---> Berechtigung für eigenen Klienten
     * 
     */
    public void testAddGroupsToAction_VP_4_itc() {
    	
        permissionService.assignPermission(myAction,myGroup,new Date(),null,true,PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson3,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.CLIENTS).size(),1);
        assertNull(m.get(PoActionPermissionService.PERSONS));
        assertNull(m.get(PoActionPermissionService.GROUPS));
    }
    
    /*
     *  viewPermission ist 5 ---> Berechtigung für eigenen Klienten
     * 
     */
    public void testAddGroupsToAction_VP_5() {
    	
        permissionService.assignPermission(myAction,myGroup,new Date(),null,false,PoConstants.VIEW_PERMISSION_TYPE_ALL_CLIENTS);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.CLIENTS).size(), countClients + 2);
        assertNull(m.get(PoActionPermissionService.PERSONS));
        assertNull(m.get(PoActionPermissionService.GROUPS));
    }
    /*
     *  viewPermission ist 5 ---> Berechtigung für eigenen Klienten
     * 
     */
    public void testAddGroupsToAction_VP_5_itc() {
    	
        permissionService.assignPermission(myAction,myGroup,new Date(),null,true,PoConstants.VIEW_PERMISSION_TYPE_ALL_CLIENTS);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson3,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.CLIENTS).size(),countClients+2);
        assertNull(m.get(PoActionPermissionService.PERSONS));
        assertNull(m.get(PoActionPermissionService.GROUPS));
    } 
    
    
    
    /***************************************************************
     * ZUORDNUNG AKTION ZU EINER ROLLE
     ***************************************************************
     *
     ***************************************************************/
    
    
    public void testAddRoleToAction_assignedToPerson_VP_0_personIsControlled() {
    	
        roleService.assignRoleWithGroupCompetence(myRole,myPerson,sub_myGroup, new Date(),null,1);
        permissionService.assignPermission(myAction,myRole,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON,false,false);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.PERSONS).size(),1);
        assertEquals(m.get(PoActionPermissionService.PERSONS).get(0),myPerson.getUID());
        assertNull(m.get(PoActionPermissionService.GROUPS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
    /**
     * 
     *  itc = true oder vitc = true macht bei viewPermission=0 keinen Sinn 
     * 
     *  sollte im Backend eventuell auch noch abgefangen und gegebenenfalls vielleicht
     *  sogar mit einer Meldung bestätigt werden.
     * */
    
    public void testAddRoleToAction_assignedToPerson_VP_0_groupIsControlled() {
    	
        roleService.assignRoleWithPersonCompetence(myRole,myPerson,myPerson, new Date(),null,1);
        permissionService.assignPermission(myAction,myRole,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON,false,false);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.PERSONS).size(),1);
        assertEquals(m.get(PoActionPermissionService.PERSONS).get(0),myPerson.getUID());
        assertNull(m.get(PoActionPermissionService.GROUPS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
    public void testAddRoleToAction_assignedToPerson_VP_1_person() {
    	
        roleService.assignRoleWithGroupCompetence(myRole,myPerson,sub_myGroup, new Date(),null,1);
        permissionService.assignPermission(myAction,myRole,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT,false,false);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.GROUPS).size(),1);
        assertEquals(m.get(PoActionPermissionService.GROUPS).get(0),orgService.getPersonsHierarchicalGroup(myPerson).getUID());
        assertNull(m.get(PoActionPermissionService.PERSONS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
    
    public void testAddRoleToAction_assignedToPerson_VP_1_group() {
    	
        roleService.assignRoleWithPersonCompetence(myRole,myPerson,myPerson3, new Date(),null,1);
        permissionService.assignPermission(myAction,myRole,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT,false,false);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.GROUPS).size(),1);
        assertEquals(m.get(PoActionPermissionService.GROUPS).get(0),this.orgService.getPersonsHierarchicalGroup(myPerson).getUID());
        assertNull(m.get(PoActionPermissionService.PERSONS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
    
    public void testAddRoleToAction_assignedToPerson_VP_2_group() {
    	
        roleService.assignRoleWithGroupCompetence(myRole,myPerson,sub_myGroup, new Date(),null,1);
        permissionService.assignPermission(myAction,myRole,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS,false,false);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.GROUPS).size(),4);
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(myGroup.getUID()));
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_myGroup.getUID()));
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_sub_myGroup.getUID()));
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_sub_sub_myGroup.getUID()));
        assertNull(m.get(PoActionPermissionService.PERSONS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
    
    public void testAddRoleToAction_assignedToPerson_VP_2_person() {
    	
        roleService.assignRoleWithPersonCompetence(myRole,myPerson,myPerson3, new Date(),null,1);
        permissionService.assignPermission(myAction,myRole,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS,false,false);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.GROUPS).size(),4);
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(myGroup.getUID()));
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_myGroup.getUID()));
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_sub_myGroup.getUID()));
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_sub_sub_myGroup.getUID()));
        assertNull(m.get(PoActionPermissionService.PERSONS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
     
    public void testAddRoleToAction_assignedToPERSON_VP_6() {
    	
    	roleService.assignRoleWithClientCompetence(myRole,myPerson,myClient, new Date(),null,1);
    	roleService.assignRoleWithClientCompetence(myRole,myPerson,myClient2, new Date(),null,1);
    	permissionService.assignPermission(myAction,myRole,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_ROLE_COMPETENCE,false,false);
    	Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
    	assertEquals((m.get(PoActionPermissionService.CLIENTS)).size(),2);
    	assertTrue((m.get(PoActionPermissionService.CLIENTS)).contains(myClient.getUID()));
    	assertTrue((m.get(PoActionPermissionService.CLIENTS)).contains(myClient2.getUID()));
    	assertNull(m.get(PoActionPermissionService.GROUPS));
    	assertNull(m.get(PoActionPermissionService.PERSONS));
    }
    
    public void testAddRoleToAction_assignedToPerson__CTGROUP_VP_3() {
    	
        roleService.assignRoleWithGroupCompetence(myRole,myPerson,sub_myGroup, new Date(),null,1);
        permissionService.assignPermission(myAction,myRole,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_ROLE_COMPETENCE,false,false);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.GROUPS).size(),1);
        assertTrue((m.get(PoActionPermissionService.GROUPS)).contains(sub_myGroup.getUID()));
        assertNull(m.get(PoActionPermissionService.PERSONS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
    
    public void testAddRoleToAction_assignedToPerson__CTPERSON_VP_3() {
    	
        roleService.assignRoleWithPersonCompetence(myRole,myPerson3,myPerson, new Date(),null,1);
        permissionService.assignPermission(myAction,myRole,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_ROLE_COMPETENCE,false,false);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson3,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.PERSONS).size(),1);
        assertTrue((m.get(PoActionPermissionService.PERSONS)).contains(myPerson.getUID()));
        assertNull(m.get(PoActionPermissionService.GROUPS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
    
    public void testAddRoleToAction_assignedToPERSON_VP_4() {
    	
        roleService.assignRoleWithPersonCompetence(myRole,myPerson,myPerson, new Date(),null,1);
        permissionService.assignPermission(myAction,myRole,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT,false,false);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.CLIENTS).size(),1);
        assertTrue((m.get(PoActionPermissionService.CLIENTS)).contains(myPerson.getClient().getUID()));
        assertNull(m.get(PoActionPermissionService.GROUPS));
        assertNull(m.get(PoActionPermissionService.PERSONS));
    }
    
    public void testAddRoleToAction_assignedToPERSON_VP_5() {
    	
        roleService.assignRoleWithPersonCompetence(myRole,myPerson,myPerson, new Date(),null,1);
        permissionService.assignPermission(myAction,myRole,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_ALL_CLIENTS,false,false);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals((m.get(PoActionPermissionService.CLIENTS)).size(),countClients + 2);
        assertTrue((m.get(PoActionPermissionService.CLIENTS)).contains(myPerson.getClient().getUID()));
        assertNull(m.get(PoActionPermissionService.GROUPS));
        assertNull(m.get(PoActionPermissionService.PERSONS));
    }
    
    /* Role is assigned to a group */ 
    
    
    public void testAddRoleToAction_assignedToGroup_VP_0_personIsControlled() {
    	
        roleService.assignRoleWithGroupCompetence(myRole,myGroup,sub_myGroup, new Date(),null,1);
        permissionService.assignPermission(myAction,myRole,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON,false,false);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.PERSONS).size(),1);
        assertEquals(m.get(PoActionPermissionService.PERSONS).get(0),myPerson.getUID());
        assertNull(m.get(PoActionPermissionService.GROUPS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
    
    public void testAddRoleToAction_assignedToGroup_VP_0_group() {
    	
        roleService.assignRoleWithPersonCompetence(myRole,myGroup,myPerson, new Date(),null,1);
        permissionService.assignPermission(myAction,myRole,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON,false,false);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.PERSONS).size(),1);
        assertEquals(m.get(PoActionPermissionService.PERSONS).get(0),myPerson.getUID());
        assertNull(m.get(PoActionPermissionService.GROUPS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
    public void testAddRoleToAction_assignedToGroup_VP_1_person() {
    	
        roleService.assignRoleWithGroupCompetence(myRole,myGroup,sub_myGroup, new Date(),null,1);
        permissionService.assignPermission(myAction,myRole,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT,false,false);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.GROUPS).size(),1);
        assertEquals(m.get(PoActionPermissionService.GROUPS).get(0),orgService.getPersonsHierarchicalGroup(myPerson).getUID());
        assertNull(m.get(PoActionPermissionService.PERSONS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
    
    public void testAddRoleToAction_assignedToGroup_VP_1_group() {
    	
        roleService.assignRoleWithPersonCompetence(myRole,myGroup,myPerson, new Date(),null,1);
        permissionService.assignPermission(myAction,myRole,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT,false,false);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.GROUPS).size(),1);
        assertEquals(m.get(PoActionPermissionService.GROUPS).get(0),this.orgService.getPersonsHierarchicalGroup(myPerson).getUID());
        assertNull(m.get(PoActionPermissionService.PERSONS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
    
    public void testAddRoleToAction_assignedToGroup_VP_2_group() {
    	
        roleService.assignRoleWithGroupCompetence(myRole,myGroup,sub_myGroup, new Date(),null,1);
        permissionService.assignPermission(myAction,myRole,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS,false,false);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.GROUPS).size(),4);
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(myGroup.getUID()));
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_myGroup.getUID()));
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_sub_myGroup.getUID()));
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_sub_sub_myGroup.getUID()));
        assertNull(m.get(PoActionPermissionService.PERSONS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
    
    public void testAddRoleToAction_assignedToGroup_VP_2_person() {
    	
        roleService.assignRoleWithPersonCompetence(myRole,myGroup,myPerson, new Date(),null,1);
        permissionService.assignPermission(myAction,myRole,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS,false,false);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.GROUPS).size(),4);
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(myGroup.getUID()));
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_myGroup.getUID()));
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_sub_myGroup.getUID()));
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_sub_sub_myGroup.getUID()));
        assertNull(m.get("persons"));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
    
    public void testAddRoleToAction_assignedToGroup_CTCLIENT_VP_3() {
    	
    	roleService.assignRoleWithClientCompetence(myRole,myGroup,myClient, new Date(),null,1);
    	roleService.assignRoleWithClientCompetence(myRole,myGroup,myClient2, new Date(),null,1);
    	permissionService.assignPermission(myAction,myRole,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_ROLE_COMPETENCE,false,false);
    	Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
    	assertEquals(m.get(PoActionPermissionService.CLIENTS).size(),2);
    	assertTrue(m.get(PoActionPermissionService.CLIENTS).contains(myClient.getUID()));
    	assertTrue(m.get(PoActionPermissionService.CLIENTS).contains(myClient2.getUID()));
    	assertNull(m.get(PoActionPermissionService.PERSONS));
    	assertNull(m.get(PoActionPermissionService.GROUPS));
    }
    
    
    public void testAddRoleToAction_assignedToGroup_CTGROUP_VP_3() {
    	
        roleService.assignRoleWithGroupCompetence(myRole,myGroup,sub_myGroup, new Date(),null,1);
        permissionService.assignPermission(myAction,myRole,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_ROLE_COMPETENCE,false,false);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.GROUPS).size(),1);
        assertTrue(m.get(PoActionPermissionService.GROUPS).contains(sub_myGroup.getUID()));
        assertNull(m.get(PoActionPermissionService.PERSONS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    

    public void testAddRoleToAction_assignedToGroup_CTPERSON_VP_3() {
    	
        roleService.assignRoleWithPersonCompetence(myRole,myGroup,myPerson, new Date(),null,1);
        permissionService.assignPermission(myAction,myRole,new Date(),null,PoConstants.VIEW_PERMISSION_TYPE_ROLE_COMPETENCE,false,false);
        Map<String, List<String>> m = permissionService.findViewPermissions(myPerson,myAction,new Date());
        assertEquals(m.get(PoActionPermissionService.PERSONS).size(),1);
        assertTrue(m.get(PoActionPermissionService.PERSONS).contains(myPerson.getUID()));
        assertNull(m.get(PoActionPermissionService.GROUPS));
        assertNull(m.get(PoActionPermissionService.CLIENTS));
    }
    
    /**
     * 
     * NEGATIVE PERMISSIONS TESTS
     * 
     */
    
    public void testExcludedClient() {

    	permissionService.assignPermission(myAction, myClient, null, null, PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT);
    	permissionService.assignNegativePermission(myAction, myClient2, null, null);
    	assertTrue(permissionService.hasPersonPermissionForAction(myPerson, myAction));
    	assertFalse(permissionService.hasPersonPermissionForAction(myPersonC2, myAction));
    }
    
    public void testExcludedGroup() {
    	
    	permissionService.assignPermission(myAction, myGroup, null, null, false, PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT);
    	permissionService.assignNegativePermission(myAction, sub_sub_myGroup, null, null, false);
    	assertTrue(permissionService.hasPersonPermissionForAction(myPerson, myAction));
    	assertFalse(permissionService.hasPersonPermissionForAction(myPerson3, myAction));
    }
    
    public void testExcludedGroupWithSubgroups() {
    	
    	permissionService.assignPermission(myAction, myGroup, null, null, true, PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT);
    	assertTrue(permissionService.hasPersonPermissionForAction(myPerson, myAction)); // myGroup
    	assertTrue(permissionService.hasPersonPermissionForAction(myPerson3, myAction)); // sub_sub_myGroup
    	assertTrue(permissionService.hasPersonPermissionForAction(myPerson4, myAction)); // sub_sub_sub_myGroup
    	permissionService.clearPermissionCaches();
    	
    	permissionService.assignNegativePermission(myAction, sub_sub_myGroup, null, null, false);
    	assertTrue(permissionService.hasPersonPermissionForAction(myPerson, myAction));
    	assertFalse(permissionService.hasPersonPermissionForAction(myPerson3, myAction));
    	assertTrue(permissionService.hasPersonPermissionForAction(myPerson4, myAction));
    	permissionService.clearPermissionCaches();
    	
    	permissionService.assignNegativePermission(myAction, sub_sub_myGroup, null, null, true);
    	assertTrue(permissionService.hasPersonPermissionForAction(myPerson, myAction));
    	assertFalse(permissionService.hasPersonPermissionForAction(myPerson3, myAction));
    	assertFalse(permissionService.hasPersonPermissionForAction(myPerson4, myAction));
    }
    
    public void testExcludedPerson() {
    	
    	permissionService.assignPermission(myAction, myPerson, null, null, PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT);
    	permissionService.assignPermission(myAction, myPerson2, null, null, PoConstants.VIEW_PERMISSION_TYPE_ALL_CLIENTS);
    	assertTrue(permissionService.hasPersonPermissionForAction(myPerson, myAction));
    	assertTrue(permissionService.hasPersonPermissionForAction(myPerson2, myAction));
    	permissionService.clearPermissionCaches();
    	permissionService.assignNegativePermission(myAction, myPerson2, null, null);
    	assertTrue(permissionService.hasPersonPermissionForAction(myPerson, myAction));
    	assertFalse(permissionService.hasPersonPermissionForAction(myPerson2, myAction));
    }

    public void testExcludedRole() {
    	
    	permissionService.assignPermission(myAction, myRole, null, null, PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT, false, false);
    	permissionService.assignPermission(myAction, myRole3, null, null, PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT, false, false);
    	roleService.assignRole(myRole, myPerson, 1);
    	roleService.assignRole(myRole3, myPerson3, 1);
    	assertTrue(permissionService.hasPersonPermissionForAction(myPerson, myAction));
    	assertTrue(permissionService.hasPersonPermissionForAction(myPerson3, myAction));
    	permissionService.clearPermissionCaches();
    	permissionService.assignNegativePermission(myAction, myRole3, null, null, false);
    	assertTrue(permissionService.hasPersonPermissionForAction(myPerson, myAction));
    	assertFalse(permissionService.hasPersonPermissionForAction(myPerson3, myAction));
    }
    
    public void testExcludedAction() {
    	
    	permissionService.assignPermission(myAction, myPerson, null, null, PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT);
    	permissionService.assignPermission(myAction2, myPerson, null, null, PoConstants.VIEW_PERMISSION_TYPE_ALL_CLIENTS);
    	assertTrue(permissionService.hasPersonPermissionForAction(myPerson, myAction));
    	assertTrue(permissionService.hasPersonPermissionForAction(myPerson, myAction2));
    	permissionService.clearPermissionCaches();
    	permissionService.assignNegativePermission(myAction2, myPerson, null, null);
    	assertTrue(permissionService.hasPersonPermissionForAction(myPerson, myAction));
    	assertFalse(permissionService.hasPersonPermissionForAction(myPerson, myAction2));
    }
    
    /**
     * 
     * FIND ACTIONS OF ...
     * 
     */
    
    public void testFindActionsOfPerson() {
    	
    	permissionService.assignPermission(myAction, myPerson, null, null, PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT);
    	List<PoAction> actions = actionService.findActionsOfPerson(myPerson, new Date());
    	assertEquals(actions.size(), 1);
    	assertEquals(actions.get(0), myAction);
    	permissionService.clearPermissionCaches();
    	permissionService.assignNegativePermission(myAction, myPerson, null, null);
    	assertEquals(actionService.findActionsOfPerson(myPerson, new Date()).size(), 0);
    }
    
    public void testFindActionsOfRole() {
    	
    	permissionService.assignPermission(myAction, myRole, null, null, PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT, false, false);
    	List<PoAction> actions = actionService.findActionsOfRole(myRole, new Date());
    	assertEquals(actions.size(), 1);
    	assertEquals(actions.get(0), myAction);
    	permissionService.clearPermissionCaches();
    	permissionService.assignNegativePermission(myAction, myRole, null, null, false);
    	assertEquals(actionService.findActionsOfRole(myRole, new Date()).size(), 0);
    }

    
    
    /**
     * 
     * findAllAction...
     * 
     */

    public void testFindAllActionsOfPersonByPerson() {

    	permissionService.assignPermission(myAction, myPerson, null, null, PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT);
    	List<PoAction> actions = permissionService.findAllActionsOfPerson(myPerson, new Date());
    	assertEquals(actions.size(), 1);
    	assertEquals(actions.get(0), myAction);
    	permissionService.clearPermissionCaches();
    	permissionService.assignNegativePermission(myAction, myPerson, null, null);
    	actions = permissionService.findAllActionsOfPerson(myPerson, new Date());
    	assertEquals(actions.size(), 0);
    }
    
    public void testFindAllActionsOfPersonByGroup() {
    	
    	permissionService.assignPermission(myAction, myGroup, null, null, false, PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT);
    	List<PoAction> actions = permissionService.findAllActionsOfPerson(myPerson, new Date());
    	assertEquals(actions.size(), 1);
    	assertEquals(actions.get(0), myAction);
    	permissionService.clearPermissionCaches();
    	permissionService.assignNegativePermission(myAction, myGroup, null, null, false);
    	actions = permissionService.findAllActionsOfPerson(myPerson, new Date());
    	assertEquals(actions.size(), 0);
    }
    
    public void testFindAllActionsOfPersonByGroupWithInheritance() {
    	
    	permissionService.assignPermission(myAction, myGroup, null, null, true, PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT);
    	List<PoAction> actions = permissionService.findAllActionsOfPerson(myPerson4, new Date());
    	assertEquals(actions.size(), 1);
    	assertEquals(actions.get(0), myAction);
    	permissionService.clearPermissionCaches();
    	
    	permissionService.assignNegativePermission(myAction, sub_sub_myGroup, null, null, true);
    	actions = permissionService.findAllActionsOfPerson(myPerson4, new Date());
    	assertEquals(actions.size(), 0);
    }
    
    public void testFindAllActionsOfGroupWithInheritence() {
        List<CompetenceTarget> competenceList = new ArrayList<CompetenceTarget>();
        competenceList.add(myPerson2);
        competenceList.add(sub_sub_sub_myGroup);
        permissionService.assignPermissionWithCompetenceTargets(myAction,myGroup,new Date(),null,competenceList,1,true,false);
        
        // check whether sub_mygroup also has access to myAction
        List<PoAPermissionBase>  perms = permissionService.findAllActionPermissionsOfGroupF(sub_myGroup, new Date());
        boolean actionFound=false;
        for (PoAPermissionBase perm : perms) {
        	if (perm.getAction().equals(myAction)) {
        		actionFound = true;
        		break;
        	}
        }
        if (!actionFound)
        	fail("Action " + myAction + " should be assigned via inheritence to Group=" + sub_myGroup);
        
        // check wheter myPerson3 (member of sub_sub_mygroup) has assigned the aciton
        assertTrue("Person " + myPerson3 + " should have inherited access to action=" + myAction, permissionService.hasPersonPermissionForAction(myPerson3, myAction));
        
        // check wheter myPerson3 (member of sub_sub_mygroup) has assigned the aciton
        assertTrue("Person " + myPersonC2 + " should *NOT* have inherited access to action=" + myAction, !permissionService.hasPersonPermissionForAction(myPersonC2, myAction));
        
        
    }
    
    public void testFindAllActionsOfPersonByRole() {

    	permissionService.assignPermission(myAction, myRole, null, null, PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT, false, false);
    	roleService.assignRole(myRole, myPerson, 1);
    	List<PoAction> actions = permissionService.findAllActionsOfPerson(myPerson, new Date());
    	assertEquals(actions.size(), 1);
    	assertEquals(actions.get(0), myAction);
    	permissionService.clearPermissionCaches();
    	permissionService.assignNegativePermission(myAction, myRole, null, null, false);
    	actions = permissionService.findAllActionsOfPerson(myPerson, new Date());
    	assertEquals(actions.size(), 0);
    }
    
    public void testFindAllActionsOfPersonByClient() {
    	
    	permissionService.assignPermission(myAction, myClient, null, null, PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT);
    	List<PoAction> actions = permissionService.findAllActionsOfPerson(myPerson, new Date());
    	assertEquals(actions.size(), 1);
    	assertEquals(actions.get(0), myAction);
    	permissionService.clearPermissionCaches();
    	permissionService.assignNegativePermission(myAction, myClient, null, null);
    	actions = permissionService.findAllActionsOfPerson(myPerson4, new Date());
    	assertEquals(actions.size(), 0);
    }
    
    /**
     * 
     * VIEW PERMISSIONS AS STRING
     * 
     */
    
	public void testGetGroupViewPermissionAsString() {
		
		PoAPermissionBase apb = new PoAPermissionBase();

		apb.setNegative(true);
		String result = permissionService.getViewPermissionAsString(apb, myGroup, new Date());
		assertEquals("", result);

		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON);
		result = permissionService.getViewPermissionAsString(apb, myGroup, new Date());
		assertEquals("", result);

		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT);
		result = permissionService.getViewPermissionAsString(apb, myGroup, new Date());
		assertEquals("", result);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS);
		result = permissionService.getViewPermissionAsString(apb, myGroup, new Date());
		assertEquals("", result);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT);
		result = permissionService.getViewPermissionAsString(apb, myGroup, new Date());
		assertEquals("", result);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_ALL_CLIENTS);
		result = permissionService.getViewPermissionAsString(apb, myGroup, new Date());
		assertEquals("", result);
		
		apb.setNegative(false);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON);
		result = permissionService.getViewPermissionAsString(apb, myGroup, new Date());
		assertEquals("", result);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT);
		result = permissionService.getViewPermissionAsString(apb, myGroup, new Date());
		assertEquals("", result);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS);
		result = permissionService.getViewPermissionAsString(apb, myGroup, new Date());
		assertEquals("", result);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT);
		result = permissionService.getViewPermissionAsString(apb, myGroup, new Date());
		assertEquals(myClient.getName(), result);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_ALL_CLIENTS);
		result = permissionService.getViewPermissionAsString(apb, myGroup, new Date());
		assertEquals(myClient.getName() + " " + myClient2.getName() + " ", result);
		
	}
	
	public void testGetGroupViewPermissionAsStringWithCompetenceTarget() {
		
        List<CompetenceTarget> competenceList = new ArrayList<CompetenceTarget>();
        competenceList.add(myPerson3);
        competenceList.add(myGroup);
        competenceList.add(myClient);
        permissionService.assignPermissionWithCompetenceTargets(myAction,myGroup,new Date(),null,competenceList,1,true, true);
        List<PoAPermissionBase> perms = permissionService.findAllActionPermissionsOfGroupF(myGroup, new Date());
		String result = permissionService.getViewPermissionAsString(perms.get(0), myGroup, new Date());
		assertTrue(result.indexOf(myPerson3.getFullName()) >= 0);
		assertTrue(result.indexOf(", ") >= 0);
		assertTrue(result.indexOf(myGroup.getShortName() + "+") >= 0);
		assertTrue(result.indexOf(myClient.getName()) >= 0);
		
		PoAPermissionBase perm = perms.get(0);
		perm.setNegative(true);
		result = permissionService.getViewPermissionAsString(perm, myGroup, new Date());
		assertEquals("", result);
		
	}
	
	public void testGetClientViewPermissionAsString() {
		
		PoAPermissionBase apb = new PoAPermissionBase();
		
		apb.setNegative(true);
		String result = permissionService.getViewPermissionAsString(apb, myClient, new Date());
		assertEquals("", result);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON);
		result = permissionService.getViewPermissionAsString(apb, myClient, new Date());
		assertEquals("", result);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT);
		result = permissionService.getViewPermissionAsString(apb, myClient, new Date());
		assertEquals("", result);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS);
		result = permissionService.getViewPermissionAsString(apb, myClient, new Date());
		assertEquals("", result);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_ROLE_COMPETENCE);
		result = permissionService.getViewPermissionAsString(apb, myClient, new Date());
		assertEquals("", result);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT);
		result = permissionService.getViewPermissionAsString(apb, myClient, new Date());
		assertEquals("", result);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_ALL_CLIENTS);
		result = permissionService.getViewPermissionAsString(apb, myClient, new Date());
		assertEquals("", result);

		apb.setNegative(false);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON);
		result = permissionService.getViewPermissionAsString(apb, myClient, new Date());
		assertEquals("", result);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT);
		result = permissionService.getViewPermissionAsString(apb, myClient, new Date());
		assertEquals("", result);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS);
		result = permissionService.getViewPermissionAsString(apb, myClient, new Date());
		assertEquals("", result);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_ROLE_COMPETENCE);
		result = permissionService.getViewPermissionAsString(apb, myClient, new Date());
		assertEquals("", result);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT);
		result = permissionService.getViewPermissionAsString(apb, myClient, new Date());
		assertEquals(myClient.getName(), result);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_ALL_CLIENTS);
		result = permissionService.getViewPermissionAsString(apb, myClient, new Date());
		assertEquals(myClient.getName() + " " + myClient2.getName() + " ", result);
	}
	
	
	public void testGetViewPermissionAsString() {

		PoAPermissionBase apb = new PoAPermissionBase();
		
		apb.setNegative(true);
		String result = permissionService.getViewPermissionAsString(apb, myPerson, new Date());
		assertEquals("", result);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON);
		result = permissionService.getViewPermissionAsString(apb, myPerson, new Date());
		assertEquals("", result);
			
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT);
		result = permissionService.getViewPermissionAsString(apb, myPerson, new Date());
		assertEquals("", result);
			
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS);
		result = permissionService.getViewPermissionAsString(apb, myPerson, new Date());
		assertEquals("", result);
		

		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT);
		result = permissionService.getViewPermissionAsString(apb, myPerson, new Date());
		assertEquals("", result);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_ALL_CLIENTS);
		result = permissionService.getViewPermissionAsString(apb, myPerson, new Date());
		assertEquals("", result);

		apb.setNegative(false);
			
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_PERSON);
		result = permissionService.getViewPermissionAsString(apb, myPerson, new Date());
		assertEquals(myPerson.getFullName(), result);
			
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT);
		result = permissionService.getViewPermissionAsString(apb, myPerson, new Date());
		assertEquals(orgService.getPersonsHierarchicalGroup(myPerson, new Date()).getShortName(), result);
			
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_ORG_UNIT_WITH_SUB_GROUPS);
		result = permissionService.getViewPermissionAsString(apb, myPerson, new Date());
		assertEquals(orgService.getPersonsHierarchicalGroup(myPerson, new Date()).getShortName() + " + ", result);
		

		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_OWN_CLIENT);
		result = permissionService.getViewPermissionAsString(apb, myPerson, new Date());
		assertEquals(myClient.getName(), result);
		
		apb.setViewPermissionType(PoConstants.VIEW_PERMISSION_TYPE_ALL_CLIENTS);
		result = permissionService.getViewPermissionAsString(apb, myPerson, new Date());
		assertEquals(myClient.getName() + " " + myClient2.getName() + " ", result);

	}
	
	public void testGetPersonViewPermissionAsStringWithCompetenceTarget() {
		
        List<CompetenceTarget> competenceList = new ArrayList<CompetenceTarget>();
        competenceList.add(myPerson3);
        competenceList.add(myGroup);
        competenceList.add(myClient);
        permissionService.assignPermissionWithCompetenceTargets(myAction,myPerson3,new Date(),null,competenceList,1,true);
        List<PoAPermissionBase> perms = permissionService.findAllActionPermissionsOfPerson(myPerson3, new Date());
		String result = permissionService.getViewPermissionAsString(perms.get(0), myPerson3, new Date());
		assertTrue(result.indexOf(myPerson3.getFullName()) >= 0);
		assertTrue(result.indexOf(", ") >= 0);
		assertTrue(result.indexOf(myGroup.getShortName() + "+") >= 0);
		assertTrue(result.indexOf(myClient.getName()) >= 0);
		
		PoAPermissionBase perm = perms.get(0);
		perm.setNegative(true);
		result = permissionService.getViewPermissionAsString(perm, myPerson3, new Date());
		assertEquals("", result);
		
	}
	
    public void testResolveViewPermissionsOfPersons() {
    	
        List<CompetenceTarget> competenceList = new ArrayList<CompetenceTarget>();
        competenceList.add(myPerson2);
        competenceList.add(myPerson3);
        competenceList.add(myPerson4);
        competenceList.add(expiredPerson);
        permissionService.assignPermissionWithCompetenceTargets(myAction,myPerson,new Date(),null,competenceList,1,true);
        assertEquals(0, permissionService.resolveViewPermissionsToListOfClients(myPerson, myAction).size());
        assertEquals(0, permissionService.resolveViewPermissionsToListOfGroups(myPerson, myAction).size());
        List<PoPerson> listOfPersons = permissionService.resolveViewPermissionsToListOfPersons(myPerson, myAction);
        assertEquals(3, listOfPersons.size());
        assertTrue(listOfPersons.contains(myPerson2));
        assertTrue(listOfPersons.contains(myPerson3));
        assertTrue(listOfPersons.contains(myPerson4));
    }

    
    public void testResolveViewPermissionsOfPersonsViaClient() {
    	
    	List<CompetenceTarget> competenceList = new ArrayList<CompetenceTarget>();
        competenceList.add(myClient);
        permissionService.assignPermissionWithCompetenceTargets(myAction,myPerson,new Date(),null,competenceList,1,true);
        assertEquals(1, permissionService.resolveViewPermissionsToListOfClients(myPerson, myAction).size());
        assertEquals(6, permissionService.resolveViewPermissionsToListOfGroups(myPerson, myAction).size());
        List<PoPerson> listOfPersons = permissionService.resolveViewPermissionsToListOfPersons(myPerson, myAction);
        assertEquals(4, listOfPersons.size());
        assertTrue(listOfPersons.contains(myPerson));
        assertTrue(listOfPersons.contains(myPerson2));
        assertTrue(listOfPersons.contains(myPerson3));
        assertTrue(listOfPersons.contains(myPerson4));

    }
    
    public void testResolveViewPermissionsOfPersonsViaGroup() {
    	
        List<CompetenceTarget> competenceList = new ArrayList<CompetenceTarget>();
        competenceList.add(myGroup);
        permissionService.assignPermissionWithCompetenceTargets(myAction,myPerson,new Date(),null,competenceList,1,false);
        assertEquals(0, permissionService.resolveViewPermissionsToListOfClients(myPerson, myAction).size());
        assertEquals(1, permissionService.resolveViewPermissionsToListOfGroups(myPerson, myAction).size());
        List<PoPerson> listOfPersons = permissionService.resolveViewPermissionsToListOfPersons(myPerson, myAction);
        assertEquals(2, listOfPersons.size());
        assertTrue(listOfPersons.contains(myPerson));
        assertTrue(listOfPersons.contains(myPerson2));

    }
    
    public void testResolveViewPermissionsOfPersonsViaGroupWithInheritance() {
    	
    	List<CompetenceTarget> competenceList = new ArrayList<CompetenceTarget>();
    	competenceList.add(myGroup);
    	permissionService.assignPermissionWithCompetenceTargets(myAction,myPerson,new Date(),null,competenceList,1,true);
    	assertEquals(0, permissionService.resolveViewPermissionsToListOfClients(myPerson, myAction).size());
    	assertEquals(4, permissionService.resolveViewPermissionsToListOfGroups(myPerson, myAction).size());
    	List<PoPerson> listOfPersons = permissionService.resolveViewPermissionsToListOfPersons(myPerson, myAction);
    	assertEquals(4, listOfPersons.size());
    	assertTrue(listOfPersons.contains(myPerson));
    	assertTrue(listOfPersons.contains(myPerson2));
    	assertTrue(listOfPersons.contains(myPerson3));
    	assertTrue(listOfPersons.contains(myPerson4));
    	
    }
    
    public void testResolveViewPermissionsOfGroups() {
    
    	List<CompetenceTarget> competenceList = new ArrayList<CompetenceTarget>();
        competenceList.add(myGroup);
        competenceList.add(myGroup2);
        competenceList.add(myGroup3);
        permissionService.assignPermissionWithCompetenceTargets(myAction,myPerson,new Date(),null,competenceList,1,false);
        assertEquals(0, permissionService.resolveViewPermissionsToListOfClients(myPerson, myAction).size());
        List<PoGroup> listOfGroups = permissionService.resolveViewPermissionsToListOfGroups(myPerson, myAction);
        assertEquals(3, listOfGroups.size());
        assertEquals(2, permissionService.resolveViewPermissionsToListOfPersons(myPerson, myAction).size());
        assertTrue(listOfGroups.contains(myGroup));
        assertTrue(listOfGroups.contains(myGroup2));
        assertTrue(listOfGroups.contains(myGroup3));
    }
    
    public void testResolveViewPermissionsOfGroupsWithInheritance() {
    	List<CompetenceTarget> competenceList = new ArrayList<CompetenceTarget>();
    	competenceList.add(myGroup);
    	competenceList.add(myGroup2);
    	competenceList.add(myGroup3);
    	permissionService.assignPermissionWithCompetenceTargets(myAction,myPerson,new Date(),null,competenceList,1,true);
    	assertEquals(0, permissionService.resolveViewPermissionsToListOfClients(myPerson, myAction).size());
    	List<PoGroup> listOfGroups = permissionService.resolveViewPermissionsToListOfGroups(myPerson, myAction);
    	assertEquals(6, listOfGroups.size());
    	assertEquals(4, permissionService.resolveViewPermissionsToListOfPersons(myPerson, myAction).size());
    	assertTrue(listOfGroups.contains(myGroup));
    	assertTrue(listOfGroups.contains(myGroup2));
    	assertTrue(listOfGroups.contains(myGroup3));
    	assertTrue(listOfGroups.contains(sub_myGroup));
    	assertTrue(listOfGroups.contains(sub_sub_myGroup));
    	assertTrue(listOfGroups.contains(sub_sub_sub_myGroup));
    }
    
    public void testResolveViewPermissionsOfClients() throws Exception {
    	List<CompetenceTarget> competenceList = new ArrayList<CompetenceTarget>();
        competenceList.add(myClient);
        permissionService.assignPermissionWithCompetenceTargets(myAction,myPerson,new Date(),null,competenceList,1,false);
        List<PoClient> listOfClients = permissionService.resolveViewPermissionsToListOfClients(myPerson, myAction);
        assertEquals(1, listOfClients.size());
        assertEquals(6, permissionService.resolveViewPermissionsToListOfGroups(myPerson, myAction).size());
        assertEquals(4, permissionService.resolveViewPermissionsToListOfPersons(myPerson, myAction).size());
        assertTrue(listOfClients.contains(myClient));
        assertFalse(listOfClients.contains(myClient2));

        competenceList.add(myClient2);
        Thread.sleep(PoHistorization.RECOMMENDED_MINIMAL_LIFETIME_MILLIS);	// fri_2013-08-13: trying to fix unit test, see below
        permissionService.assignPermissionWithCompetenceTargets(myAction,myPerson,new Date(),null,competenceList,1,false);
        Thread.sleep(PoHistorization.RECOMMENDED_MINIMAL_LIFETIME_MILLIS);	// fri_2013-08-12: trying to fix unit test, see below
        listOfClients = permissionService.resolveViewPermissionsToListOfClients(myPerson, myAction);
        System.out.println("List of clients in unstable test is: "+listOfClients);
        assertEquals(2, listOfClients.size());
        // fri_2013-07-31: failed on Hudson: "expected:<2> but was:<1>", this is unstable
        // fri_2013-08-05: failed on Hudson: "expected:<2> but was:<1>", not reproducible on local machine
        // this is most likely because of the four AbstractAutoCommitSpringHsqlDbTestCase in this directory
        // fri_2013-08-05: moved all four AbstractAutoCommitSpringHsqlDbTestCase's in same directory to new sub-dir "nontransactional", for now this test succeeds
        // fri_2013-08-05: failed again despite of moved tests, println yielded "List of clients in unstable test is: [PoClient[shortName=null, name=myClient, description=myClient, uid=40288a2c405650e901405654cce81efe]]"
        assertEquals(7, permissionService.resolveViewPermissionsToListOfGroups(myPerson, myAction).size());
        assertEquals(5, permissionService.resolveViewPermissionsToListOfPersons(myPerson, myAction).size());
        assertTrue(listOfClients.contains(myClient));
        assertTrue(listOfClients.contains(myClient2));
    }
    
    @Test @Ignore("See WD-145")
    public void testHasPermissionForAllClients() throws Exception {
    	final Date now = DateTools.now();
    	boolean hasAllPermission = permissionService.hasViewPermissionForAllClients(myPerson, myAction);
    	assertFalse(hasAllPermission);

    	List<CompetenceTarget> competenceList = new ArrayList<CompetenceTarget>();
    	competenceList.add(myClient);
    	permissionService.assignPermissionWithCompetenceTargets(myAction, myPerson, now, null, competenceList, 1, false);
        Thread.sleep(PoHistorization.RECOMMENDED_MINIMAL_LIFETIME_MILLIS);	// fri_2013-08-13: trying to fix unit test, see below
    	hasAllPermission = permissionService.hasViewPermissionForAllClients(myPerson, myAction);
    	assertFalse(hasAllPermission);
    	
    	competenceList = new ArrayList<CompetenceTarget>();
    	competenceList.add(myClient2);
    	permissionService.assignPermissionWithCompetenceTargets(myAction, myPerson, now, null, competenceList, 1, false);
        Thread.sleep(PoHistorization.RECOMMENDED_MINIMAL_LIFETIME_MILLIS);	// fri_2013-08-12: trying to fix unit test, see below. fri_2013-08-12: increased sleep to 3 seconds, then to 5 seconds, neither helped
    	hasAllPermission = permissionService.hasViewPermissionForAllClients(myPerson, myAction);
    	assertTrue(hasAllPermission);
    	// fri_2013-07-04: failed on Hudson with AssertionFailedError, fail was not reproducible on locale machine
    	// fri_2013-07-10: failed on Hudson with AssertionFailedError
    	// fri_2013-07-12: failed on Hudson with AssertionFailedError, despite "now"
    	// fri_2013-07-31: failed on Hudson with AssertionFailedError, this is unstable
        // fri_2013-08-05: moved all four AbstractAutoCommitSpringHsqlDbTestCase's in same directory to new sub-dir "nontransactional", but this test failed after
    	// fri_2013-09-03: failed on Hudson with AssertionFailedError
    	// fri_2013-09-06: failed on Hudson with AssertionFailedError, even with 3 seconds sleep time
    	// fri_2013-09-11: failed on Hudson many times since, even with 5 seconds sleep time
    	// fri_2013-09-12: failed again, there seems to be a real problem, sent mail to Stanislav and Gabriel
    	// fri_2014-01-27: yielded "junit.framework.AssertionFailedError: null"
    }
    
}
