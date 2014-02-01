package at.workflow.webdesk.po;

import java.util.Date;
import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.model.PoAPermissionBase;
import at.workflow.webdesk.po.model.PoAPermissionClient;
import at.workflow.webdesk.po.model.PoAPermissionGroup;
import at.workflow.webdesk.po.model.PoAPermissionPerson;
import at.workflow.webdesk.po.model.PoAPermissionRole;
import at.workflow.webdesk.po.model.PoAPermissionUniversal;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.tools.NamedQuery;

public interface PoActionPermissionService {

	public static final String PERSONS = "persons";
	public static final String GROUPS =  "groups";
	public static final String CLIENTS = "clients";
	
	/**
	 * This is necessary due to Spring being enable to handle both-way dependency.
	 * It intended to be set by action service itself
	 */
	public void setActionService(PoActionService actionService);
	
    /**
     * @param permission
     *  
     * saves the passed action permission to the database.
     */
    public void saveAPermission(PoAPermissionBase permission); 

    /**
     * @param uid
     * @return a PoActionPermission object with the given uid.
     */
    public PoAPermissionBase getAPermission(String uid);
    
    /**
     * @param uid
     * @return a PoActionPermission object with the given uid.
     */
    public PoAPermissionRole getAPermissionRole(String uid);
    
    /**
     * Checks whether the given user (may be switched)
     * has permission to execute the specified action.
     * 
     * @param user - the current Username
     * @param switchedUser - the userName of the original User, if current user is switched
     * @param action - the Action which needs to be executed
     * @return true/false
     */
    public boolean hasPersonPermissionForAction(String userName, String switchedUserName, PoAction action);
    
    /**
     * @param user
     * @param action
     * @return true if the user has permission to execute the given action,
     * false otherwise.
     */
    public boolean hasPersonPermissionForAction(String user, PoAction action);
    
    
    
    /**
     * 
     * This function deletes the permission cache for an Action, if 
     * an action with the given name is found.
     * 
     * @param actionName the name of the action
     */
    public void deletePermissionCacheForAction(String actionName);

    /**
     * 
     * Returns true if the given person is allowed to execute the given action<br>
     * which will in return change the values of an object which is identified<br>
     * by its id. 
     * 
     * @param uid the uid of the instance that will be edited.
     * @param userName the userName of the PoPerson object.
     * @param action
     * @return true if the given person has rights to edit an object with <br>
     * the given <code>uid</code>
     */
    public boolean hasPersonPermissionToEditObjectWithId(String uid,String userName,PoAction action,Date date);
    
    /**
     * @deprecated 
     * replaced with assignPermission
     */
    public void addClientToAction(PoAction action, PoClient client, Date validFrom, Date validTo,int viewPermissionType);
    
    /**
     * @deprecated
     * replaced with assignPermission
     */
    public void addPersonToAction(PoAction action, PoPerson person, Date validFrom, Date validTo,int viewPermissionType);
    
    
    /**
     * <p>
     * This function creates a dummy <code>PoRole</code> object with the following name convention: 
     * "action.getName() + "_" + person.getFirstName()+ "_" + person.getLastName()"
     * FIXME This is probably going to change.
     * <p>
     * It creates a <code>PoRoleCompetenceBase</code> object for each element passed inside the
     * <code>competenceList</code> which can consists of <code>PoPerson</code> and <code>PoGroup</code>
     * objects. The assignments are valid within the given timerange.
     * <p>
     * If viewInheritToChild is set to <code>true</code>, then also the subgroups of any <code>PoGroup</code>
     * object contained in the <code>competenceList</code> is included when view permissions are resolved. 
     * 
     * @deprecated
     * replaced by assignPermissionWithCompetenceTargets 
     */
    public void addPersonToActionWithCompetenceList(PoAction action, PoPerson person, Date validFrom, Date validTo, 
    		List<CompetenceTarget> competenceList, int ranking, boolean viewInheritToChilds);
    
    /**
     * @deprecated
     * replaced by assignPermission 
     */
    public void addGroupToAction(PoAction action, PoGroup group, Date validFrom, Date validTo, boolean inheritToChild, int viewPermissionType); 

    /**
     * <p>
     * This function creates a dummy <code>PoRole</code> object with the following name convention: 
     * "action.getName() + "_" + group.getShortName()+ "_" + timestamp."
     * FIXME This is probably going to change.
     * <p>
     * It creates a <code>PoRoleCompetenceBase</code> object for each element passed inside the
     * <code>competenceList</code> which can consists of <code>PoPerson</code> and <code>PoGroup</code>
     * objects. The assignments are valid within the given timerange.
     * <p>
     * If viewInheritToChild is set to <code>true</code>, then also the subgroups of any <code>PoGroup</code>
     * object contained in the <code>competenceList</code> is included when view permissions are resolved. 
     * <p>
     * If <code>inheritToChilds</code> is set to <code>true</code>, also the child groups of the 
     * <code>group</code> has the permission to execute the given <code>action</code>. 
     * 
     * @deprecated 
     * replaced with assignPermissionWithCompetenceTargets
     */
    public void addGroupToActionWithCompetenceList(PoAction action, PoGroup group, Date validFrom, 
    		Date validTo, List<CompetenceTarget> competenceList, int ranking, boolean inheritToChilds, boolean viewInheritToChilds);
    
    /**
     * <p>
     * This function allows to execute the <code>action</code> for roleholders of <code>role</code>.
     * The <code>viewPermissionType</code> defines the viewpermission. When the role holder of the 
     * <code>role</code> is a <code>PoGroup</code> then <code>inheritToChilds</code> determines 
     * whether the permission is inherited to its childgroups or not. 
     * <p>
     * <code>viewInheritToChilds</code> determines if the view of a parent group is inherited to 
     * its childs.
     * 
     * @deprecated 
     * replaced with assignPermission 
     */
    public void addRoleToAction(PoAction action, PoRole role,  Date validFrom, Date validTo,
    		int viewPermissionType, boolean inheritToChilds, boolean viewInheritToChilds);
    
    /**
     * 
     * 	Following methods replace deprecated methods for adding Client/Group/Person/role to action
     * 
     */
    
    /**
     * Assign the client permission to the action. 
     * The relationship is valid from validfrom date or from now.
     * 
     * @param myaction
     * @param client
     * @param validfrom
     * @param validto
     * @param vpt
     */
	public void assignPermission(PoAction myaction, PoClient client, Date validfrom, Date validto, int viewpermissiontype);

	/**
     * Assign the group permission to the action. 
     * The relationship is valid from validfrom date or from now.
     * 
	 * @param myaction
	 * @param group
	 * @param validfrom
	 * @param validto
	 * @param inherittochilds
	 * @param viewpermissiontype
	 */
	public void assignPermission(PoAction myaction, PoGroup group, Date validfrom, Date validto, 
			boolean inherittochilds, int viewpermissiontype);
	
	/**
	 * Assign the person permission to the passed action. 
	 * The relationship is valid from the validfrom date or from now.
	 * 
	 * @param myaction
	 * @param person
	 * @param validfrom
	 * @param validto
	 * @param viewpermissiontype
	 */
	public void assignPermission(PoAction myaction, PoPerson person, Date validfrom, Date validto, 
			int viewpermissiontype);
	
	/**
	 * Assign the role permission to the passed action. 
	 * The relationship is valid from the validfrom date or from now.
	 * 
	 * @param action
	 * @param role
	 * @param validFrom
	 * @param validTo
	 * @param viewPermissionType
	 * @param viewInheritToChilds
	 */
    public void assignPermission(PoAction action, PoRole role,  Date validFrom, Date validTo,
    		int viewPermissionType, boolean inheritToChilds, boolean viewInheritToChilds);
    
	/**
	 * Creates dummy PoRole based on the group and assigns it to all the competence targets.
	 * 
	 * @param myaction
	 * @param group
	 * @param validfrom
	 * @param validto
	 * @param ctlist
	 * @param ranking
	 * @param inherittochilds
	 * @param view_inherittochilds
	 */
	public void assignPermissionWithCompetenceTargets(PoAction myaction, PoGroup group, Date validfrom, 
			Date validto, List<CompetenceTarget> ctlist, int ranking, boolean inherittochilds, boolean viewinherittochilds);
	
	/**
	 * Creates dummy PoRole based on the person and assigns it to all the competence targets.
	 * 
	 * @param myaction
	 * @param person
	 * @param validfrom
	 * @param validto
	 * @param ctlist
	 * @param ranking
	 * @param viewinherittochilds
	 */
	public void assignPermissionWithCompetenceTargets(PoAction myaction, PoPerson person, Date validfrom, 
			Date validto, List<CompetenceTarget> ctlist, int ranking, boolean viewinherittochilds);
	
	/**
     * Assign negative client permission to the action. 
     * The relationship is valid from validfrom date or from now.
     * 
     * @param action
     * @param client
     * @param validFrom
     * @param validTo
     */
    public void assignNegativePermission(PoAction action, PoClient client, Date validFrom, Date validTo);
    
    /**
     * Assign negative group permission to the action. 
     * The relationship is valid from validfrom date or from now.
     * 
     * @param action
     * @param group
     * @param validFrom
     * @param validTo
     * @param inheritToChild
     */
    public void assignNegativePermission(PoAction action, PoGroup group, Date validFrom, Date validTo, boolean inheritToChild);
    
    /**
	 * Assign negative person permission to the passed action. 
	 * The relationship is valid from the validfrom date or from now.
	 * 
     * @param action
     * @param person
     * @param validFrom
     * @param validTo
     */
    public void assignNegativePermission(PoAction action, PoPerson person, Date validFrom, Date validTo);
    
    /**
	 * Assign negative role permission to the passed action. 
	 * The relationship is valid from the validfrom date or from now.
	 * 
     * @param action
     * @param role
     * @param validFrom
     * @param validTo
     */
    public void assignNegativePermission(PoAction action, PoRole role, Date validFrom, Date validTo, boolean inheritToChild);
    

    /**
     * @param action
     * 
     * deletes the permission of a linkable object
     * (can be a person, group, role or client) 
     * to an action from the database. 
     * throws an exception, if validfrom is in the past
     */
    public void deleteAndFlushAPermission(PoAPermissionBase aPermissionObject, Date date);
    
    /**
     * @param aPermissionObject a <code>PoAPermissionBase</code> object.
     * @param validfrom start of the validtity
     * @param validto validity ends at this date (and thus the action assignment)
     * @param inheritToChild determines wheter the permission is inherited to childs or not. 
     * @param viewInheritToChilds determine whether the view permissions is inherited or not. 
     * (TODO provide better documentation for this, especially for <code>viewInheritToChilds</code>.
     */
    public void changeValidityAPermission(PoAPermissionBase aPermissionObject, Date validfrom, 
    		Date validto, boolean inheritToChild, boolean viewInheritToChilds);
    
	/**
	 * @param actionPermission
	 * @param date
	 * 
	 * deletes the actionPermission at the given date. (internally
	 * only the validto field is set to the passed date)
	 */
	public void deleteAPermission(PoAPermissionBase actionPermission);
	

    /*************************FIND ACTION PERMISSIONS ****************
     * 
     */
    
    
    /**
     * @param client
     * @param date
     * @return returns list of PoAPermissionClient objects, that are valid at the given date
     */
    public List<PoAPermissionClient> findActionPermissionsOfClient(PoClient client, Date date);
    
    
    /**
     * @return a <code>List</code> of <code>PoAPermissionUniversal</code> 
     * objects.
     */
    public List<PoAPermissionUniversal> findUniversallyAllowedActions();
    
    /**
     * @param client
     * @param date
     * @return a list of PoAPermissionClient objects, that are at least valid at <br>
     * the given date or become valid after the given date.
     */
    public List<PoAPermissionClient> findActionPermissionsOfClientF(PoClient client, Date date);
    
    /**
     * @param person
     * @param date 
     * @return returns list of PoAPermissionPerson objects, that are valid at the given date
     *
     **/
    public List<PoAPermissionPerson> findActionPermissionsOfPerson(PoPerson person, Date date);
    
    /**
     * @param person
     * @param date
     * @return a list of PoAPermissionPerosn objects that are at least valid at the given date <br>
     * or will become valid in the future.
     */
    public List<PoAPermissionPerson> findActionPermissionsOfPersonF(PoPerson person, Date date);

    /**
     * @param group
     * @param date
     * @return a list of PoAPermissionGroup objects that are valid at the given date. <br>
     * 
     */
    public List<PoAPermissionGroup> findActionPermissionsOfGroup(PoGroup group, Date date);
    
    /**
     * @param group
     * @param date
     * @return a list of PoAPermissionGroup objects that are at least valid at the given date <br>
     * or will become valid in the future
     */
    public List<PoAPermissionGroup> findActionPermissionsOfGroupF(PoGroup group, Date date);
    
    /**
     * @param role
     * @param date
     * @return a list of PoAPermissionsRole objects, that are valid at the given date 
     * and assigned to the given role.
     */
    public List<PoAPermissionRole> findActionPermissionsOfRole(PoRole role, Date date);
    
    /**
     * @param role
     * @param date
     * @return a list of PoAPermissionsRole objects, that are or become valid after the given date 
     * and that are assigned to the given role.
     */
    public List<PoAPermissionRole> findActionPermissionsOfRoleF(PoRole role, Date date);
    
	/**
	 * @param person
	 * @param date
	 * @return a list of Actions that are assigned to the passed 
	 * person and that are valid at the passed date.
	 */
	public List<PoAPermissionBase> findAllActionPermissionsOfPerson(PoPerson person, Date date);
	
	/**
	 * @param person
	 * @param date
	 * @return a list of Actions that are assigned to the passed 
	 * person and that are valid at the passed date. future is included
	 */
	public List<PoAPermissionBase> findAllActionPermissionsOfPersonF(PoPerson person, Date date);
	
	
	/**
	 * @param group
	 * @param date
	 * @return a list of Actions that are assigned to the passed 
	 * group and that are valid at the passed date. future is included
	 */
	public List<PoAPermissionBase> findAllActionPermissionsOfGroupF(PoGroup group, Date date);
	
	/**
	 * @param person
	 * @param date
	 * @return a list of Actions that are assigned to the passed 
	 * role and that are valid at the passed date. future is included
	 */
	public List<PoAPermissionRole> findAllActionPermissionsOfRoleF(PoRole role, Date date);
	
	
    
    public List<String> findAssignedPersonsOfAction(PoAPermissionBase apb);
    
	/**
	 * @param person
	 * @param aPermissionBase
	 * @param date
	 * @return a list of competence targets for the given person and the given aPermission. The actionpermission 
	 * has to be valid at the given date.
	 */
	public List<CompetenceTarget> findActionCompetenceTargets(PoPerson person, PoAPermissionBase aPermissionBase, Date date);
	
	/**
	 * @param group
	 * @param aPermissionBase
	 * @param date
	 * @return a list of competence targets for the given group and the given aPermission. The actionpermission 
	 * has to be valid at the given date.
	 */
	public List<CompetenceTarget> findActionCompetenceTargets(PoGroup group, PoAPermissionBase aPermissionBase, Date date);
	
    /**
     * @param action
     * @param date
     * @return a list of PoAPermissionBase objects that are assigned to the given action and 
     * are valid at the given date.
     */
    public List<PoAPermissionBase> findAllPermissionsForActionF(PoAction action, Date date);
         
    /**
     * Retrieves all actions available to person according to positive and negative permissions.
     * 
     * @param person
     * @param date
     * @return
     */
    public List<PoAction> findAllActionsOfPerson(PoPerson person, Date date);
    

    /**
     * 
     * Returns three Lists of corresponding client, person and group UID's which are kept in a HashMap. <br>
     * The lists can be retrieved using the keys <code>PoActionPermissionService.CLIENTS</code>,
     * <code>PoActionPermissionService.GROUPS</code>, and <code>PoActionPermissionService.PERSONS</code>.
     * 
     * @param person the person that executes the action.
     * @param action the action that is executed. 
     * @param date at which the action is executed.
     * @return a Hashmap containing different UID's
     */
    public Map<String, List<String>> findViewPermissions(PoPerson person, PoAction action, Date date);
    
    /**
     * same as above method but with additional parameter of an action, where
     * the view permission should be added. It is actually a priority hint for 
     * the inheritance graph produced by the 'allowsAction' property, as there can occur
     * inheritance situations which are not desired.
     * 
     * @param person
     * @param action
     * @param date
     * @param actionToInheritViewPermissionFrom
     * @return
     */
    public Map<String, List<String>> findViewPermissions(PoPerson person, PoAction action, Date date, PoAction actionToInheritViewPermissionFrom);
    
    
	/**
	 * checks if Person has Permission for the specified Action
	 * returns true/false
	 * 
	 * @param person
	 * @param action
	 * @return
	 */
	public boolean hasPersonPermissionForAction(PoPerson person, PoAction action);
	

    /**
     * same as {@link #getViewPermissionQuery(PoAction, String, Date)} but without having an initial query.
     */
     public NamedQuery getViewPermissionQuery(PoAction action, String user, Date date) ;    
     
     
    /**
     * The function replaces the placeholders <code>($GROUPUIDS$,$CLIENTUIDS$,$PERSONUIDS$)</code><br>
     * inside the query with the correct uid's for which the given <code>user</code> has rights (according to
     * his view permissions) using named parameters inside the returned NamedQuery object.
     * 
     * @param action the action that is going to be executed
     * @param query a NamedQuery object holding query string and named parameter/value pairs that is going to be executed 
     * @param user a string that holds the userName of the registered user.
     * @param date the date where the viewpermissions should be evaluated.
     * @return a new NamedQuery object holding the original name/value pairs
     * and new ones according to the view permissions. It is designated to use the query with<br>
     * a QueryPaginationCursor object, but it can be used also with the PoGeneralDbService.
     */
    public NamedQuery getViewPermissionQuery(PoAction action, String user, NamedQuery query, Date date);
    
    /**
     * The function replaces the placeholders <code>($GROUPUIDS$,$CLIENTUIDS$,$PERSONUIDS$)</code><br>
     * inside the query with the correct uid's for which the given <code>user</code> has rights (according to
     * his view permissions) using named parameters inside the returned NamedQuery object. When you want to
     * inherit the viewpermissions from some other action, pass that action as 5th parameter.
     * 
     * @param action the action that is called by the User
     * @param query a NamedQuery object holding query string and named parameter/value pairs that is going to be executed 
     * @param user a string that holds the userName of the registered user.
     * @param date the date where the viewpermissions should be evaluated.
     * @param actionToInheritViewPermissionFrom is the action where we want to inherit view permissions from. If not null, viewpermissions
     * will be evaluated using this additional action, instead of the original action (first parameter).
     * @return a new NamedQuery object holding the original name/value pairs
     * and new ones according to the view permissions. It is designated to use the query with<br>
     * a QueryPaginationCursor object, but it can be used also with the PoGeneralDbService.
     */
    public NamedQuery getViewPermissionQuery(PoAction action, String user, NamedQuery query, Date date, PoAction actionToInheritViewPermissionFrom );
     
     /**
      * clear query and permission cache.
      */
     public void clearPermissionCaches();
              
     /**
      * @param person
      * @param action
      * @param date
      * @return a List of Persons and Groups for which the person has
      * Access to view personal Data via the specified Action
      * if viewpermissiontype equals 1 returns person itself 
      */

     public List<CompetenceTarget> findCompetenceTargetForAction(PoPerson person, PoAction action, Date date);
     
     public List<CompetenceTarget> findCompetenceTargetForAction(PoGroup group, PoAction action, Date date);

     public List<PoAPermissionGroup> findDirectlyLinkedActionPermissionsF(PoGroup group, Date date);
     
     /**
      * @param apb a <code>PoAPermissionBase</code> object. These object is used to determine
      * the view permission.
      * @param group the group that corresponds to the given <code>PoAPermissionBase </code> object.
      * @param date the date on which the objects have to be valid.
      * @return a <code>String</code> representing the view permission of the given <code>PoAPermissionBase</code>
      * object. 
      */
     public String getViewPermissionAsString(PoAPermissionBase apb, PoGroup group, Date date);
     
     
     /**
      * @param apb a <code>PoAPermissionBase</code> object. These object is used to determine
      * the view permission.
      * @param person the person that corresponds to the given <code>PoAPermissionBase </code> object.
      * @param date the date on which the objects have to be valid.
      * @return a <code>String</code> representing the view permission of the given <code>PoAPermissionBase</code>
      * object. 
      */
     public String getViewPermissionAsString(PoAPermissionBase apb, PoPerson person, Date date);

     
     /**
      * @param apb a <code>PoAPermissionBase</code> object. These object is used to determine
      * the view permission.
      * @param client the client that corresponds to the given <code>PoAPermissionBase </code> object.
      * @param date the date on which the objects have to be valid.
      * @return a <code>String</code> representing the view permission of the given <code>PoAPermissionBase</code>
      * object. 
      */
     public String getViewPermissionAsString(PoAPermissionBase apb, PoClient client, Date date);
 	/**
 	 * @param query a <code>String</code>
 	 * @param hm a Map <code>M(x,y)</code> where x can be <code>$CLIENTUIDS$</code>, 
 	 * <code>$GROUPUIDS$</code> or <code>$PERSONUIDS$</code>, and y is a <code>List </code> of values.
 	 * @return a <code>String</code> where the occurances of <code>$CLIENTUIDS$</code>, 
 	 * <code>$GROUPUIDS$</code> and <code>$PERSONUIDS$</code>  
 	 * will be replaced with the corresponding values contained in the Map <code>hm</code>.
 	 * 
 	 */
 	public String replacePlaceHoldersInQuery(String query, Map<String,List<String>> hm);
 	
 	/**
 	 * resolves ViewPermissions for a particular User and action
 	 * to a List of PoPersons
 	 * 
 	 * @param person
 	 * @param action
 	 * @return List of PoPerson Objects
 	 */
 	public List<PoPerson> resolveViewPermissionsToListOfPersons(PoPerson person, PoAction action); 
     
 	/**
 	 * resolves ViewPermissions for a particular User and action
 	 * to a List of PoPersons
 	 * 
 	 * @param person
 	 * @param action
 	 * @param from
 	 * @param to
 	 * @return
 	 */
	public List<PoPerson> resolveViewPermissionsToListOfPersons(PoPerson person, PoAction action, Date from, Date to);
	
 	/**
 	 * resolves ViewPermissions for a particular User and action
 	 * to a List of PoGroups
 	 * 
 	 * @return List of PoGroup Objects
 	 */
 	public List<PoGroup> resolveViewPermissionsToListOfGroups(PoPerson person, PoAction action); 
 	
 	/**
 	 * resolves ViewPermissions for a particular User and action
 	 * to a Map PoOrgStructure -> list of PoGroups of the org structure
 	 * 
 	 * @return Map PoOrgStructure -> PoGroups 
 	 */
 	public Map<PoOrgStructure, List<PoGroup>> resolveViewPermissionsToGroupsMap(PoPerson person, PoAction action); 
 	
 	/**
 	 * resolves ViewPermissions for a particular User and action
 	 * to a List of PoClients
 	 * 
 	 * @return List of PoClient Objects
 	 */
 	public List<PoClient> resolveViewPermissionsToListOfClients(PoPerson person, PoAction action); 
 	
 	/**
 	 * determines whether the user has view permissions for all clients 
 	 * to a List of PoClients
 	 * 
 	 * @return List of PoClient Objects
 	 */
 	public boolean hasViewPermissionForAllClients(PoPerson person, PoAction action); 
 	
     /**
      * 
      * Returns true if the given person is allowed to execute the given action<br>
      * which will in return change the values of an object which is identified<br>
      * by its id. 
      * 
      * @param uid the uid of the instance that will be edited.
      * @param userName the userName of the PoPerson object.
      * @param action
      * @return true if the given person has rights to edit an object with <br>
      * the given <code>uid</code>
      */
     public boolean hasPersonPermissionToEditObjectWithId(String uid,
             String userName, PoAction action);

     
     /**
      * <p>
      * This function returns <code>true</code> if there exists a <code>PoPerson</code>
      * object, which <code>userName</code> equals the parameter <code>userName</code>,
      * which is allowed to execute the <code>action</code> and especially, to pass
      * the <code>uid</code> as parameter. 
      * <p>
      * The assignments have to be valid at the current date. 
      * 
      * 
      * 
      * @param uid <code>String</code> representing the <code>UID</code> of an arbitrary object.
      * @param userName <code>String</code> representing the <code>userName</code> of a <code>PoPerson</code>
      * @param action <code>PoAction</code>
      * @return <code>boolean</code>
      */
     public boolean hasPersonPermissionToEditWithIdConfig(String uid,
     		String userName, PoAction action);
     
 	/**
 	 * @param person
 	 * @param action
 	 * 
 	 * removes Permission on Action for specified person
 	 * (sets validto to now on corrsponding PoAPermissionPerson Object)
 	 */
 	public void removeActionFromObject(PoAPermissionBase object);

}
