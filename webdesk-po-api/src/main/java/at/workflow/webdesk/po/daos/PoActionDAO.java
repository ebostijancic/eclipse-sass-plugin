package at.workflow.webdesk.po.daos;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoAPermissionBase;
import at.workflow.webdesk.po.model.PoAPermissionClient;
import at.workflow.webdesk.po.model.PoAPermissionGroup;
import at.workflow.webdesk.po.model.PoAPermissionPerson;
import at.workflow.webdesk.po.model.PoAPermissionRole;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;

/**
 * Created on 05.01.2005
 * @author hentner
 * @author ggruber
 * @author sdzuban
 *
 * This is only a <b>data access object</b>, more functions concerning 
 * actions are found in the classes implementing the {@link at.workflow.webdesk.po.PoActionService} 
 * interface.
 * 
 * @see at.workflow.webdesk.po.PoActionService
 */
public interface PoActionDAO extends GenericDAO<PoAction> {
	
	/**
	 * Creates a text that contains comma-separated UID values.
	 * The text will be '' when null or an empty list is passed.
	 * @param uids List of uid values to convert to a comma-separated string, can be null or empty.
	 * @param isString when true, each item of the list will be enclosed into single 'quotes'.
	 * @return the empty string '' when list null or empty,
	 * 		else 'uid1','uid2',... when isString == true, uid1,uid2,... when isString == false.
	 */
	public String generateCommaList(List<String> uids, boolean isString);

    /*************************FIND ACTIONS***************/
    
    /**
     * @param person
     * @param date
     * 
     * @return returns all actions that are assigned to the given person
     * via a PoAPermission 
     */
    public List<PoAction> findActionsOfPerson(PoPerson person, Date date);
    
    /**
     * @param role
     * @param date
     * @return a list of actions, that are or become valid after the given date 
     * and that are assigned to the given role.
     */
    public List<PoAction> findActionsOfRole(PoRole role, Date date);
    
    
    /**
     * Returns a list with the names of all <code>PoAction</code> objects.
     * Only actions of type PoConstants.ACTION_TYPE_ACTION are returned.
     * 
     * 
     * @param module
     * @return a List of <code>String</code> objects.
     */
    public List<String> findActionNamesOfModule(String module);
    
    /*************************FIND ACTION PERMISSIONS ***********/
    
    /**
     * @param client
     * @param date
     * @return returns list of PoAPermissionClient objects, that are valid at the given date
     */
    public List<PoAPermissionClient> findActionPermissionsOfClient(PoClient client, Date date);
    
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
    

	public PoAction findActionByNameAndType(String name, int type); 
	
	
	/**
	 * @param actionPermission
	 * 
	 * Deletes the PoAPermissionBase object. 
	 */
	public void deleteAPermission(PoAPermissionBase actionPermission);
	
	public PoAPermissionBase getAPermission(String uid);
	
	public PoAPermissionRole getAPermissionRole(String uid);
	
    /**
     * @param apr
     * @param validFrom
     * @param validTo
     * @return a list of <code>PoAPermissionRole</code> objects.
     * The returned objects have the same role and action as the 
     * PoAPermissionRole object. 
     */
    public List<PoAPermissionRole> findActionPermissionRoleWithActionAndRole(PoAPermissionRole apr,
    		Date validFrom, Date validTo);
    
    /**
     * @param apg
     * @param validFrom
     * @returna list of <code>PoAPermissionGroup</code> objects.
     * The returned objects have the same group and action as the 
     * PoAPermissionRole object. 
     */
    public List<PoAPermissionGroup> findActionPermissionGroupWithActionAndGroup(PoAPermissionGroup apg, 
    		Date validFrom);
    
    
    public List<PoAPermissionGroup> findDirectlyLinkedActionConfigPermissions(PoGroup group, PoAction action, Date date);
    
    public List<PoAPermissionGroup> findDirectlyLinkedActionPermissions(PoGroup group, Date date);
    
    /**
     * retrieves directly referenced PoAPermissionGroup Objects for specified group
     * from given date into future.
     * 
     * @param group: PoGroup object to search for
     * @param date:  from the given date into the future
     * @return  List of PoAPermissionGroup Objects
     */
    public List<PoAPermissionGroup> findDirectlyLinkedActionPermissionsF(PoGroup group, Date date);
    
    
    public boolean isActionAssignedToPerson(PoPerson person, Date date, PoAction action);
    
    public boolean isActionAssignedToRole(PoRole role, PoAction action, Date date);
    
    public boolean isActionAssignedToClient(PoClient client,PoAction action, Date date);
    
    public boolean isDirectlyLinkedActionConfigParent(PoGroup group, PoAction action, Date date);
    
	
	 /**
	 * This function returns a list of configurated actions, which are <br>
	 * inherited from the given <code>action</code> 
	 * 
	 * @param action
	 * @return a list of configurated actions inherited from the given <code>action</code>.
	 */
	public List<PoAction> findConfigsFromAction(PoAction action);
	 
    /**
     * @return a list of configs
     */
    public List<PoAction> findConfigs();
    
    /**
     * loads all current active Configs
     * @return List of PoActions
     */
    public List<PoAction> findAllCurrentConfigs();
    
	 /**
	  * This function returns a list of <code>PoAPermissionBase</code> objects, that are <br>
	  * assigned to the given <code>action</code>. The assignment has to be valid<br>
	  * at the given <code>date</code>. 
	  * 
	 * @param action
	 * @param date
	 * @return a list of PoAPermissionBase objects
	 */
	public List<PoAPermissionBase> findAllPermissionsForActionF(PoAction action, Date date);
	    
	    
	
	/**
	 * @param actionPermission
	 * @param date
	 * @return a list of PoGroup objects which are assigned to this action.
	 */
	public List<PoGroup> findGroupsFromAction(PoAction action, Date date);
	
	/**
	 * @param actionPermission
	 * @param date
	 * @return a list of PoPerson objects which are assigned to this action.
	 */
	public List<PoPerson> findPersonsFromAction(PoAction action, Date date);
	
	/**
	 * @param actionPermission
	 * @param date
	 * @return a list of PoRole objects which are assigned to this action.
	 */
	public List<PoRole> findRolesFromAction(PoAction action, Date date);
	
	
    /**
     * saves a PoAPermission Object (can be of Type PoAPermissionPerson, PoAPermissionGroup
     * PoAPermissionRole or PoAPermissionClient)
     * @param permission
     */
    public void saveAPermission(PoAPermissionBase permission);
    

    /**
     * @param procDefId
     * @return a list of Actions with corresponding procDefId
     */
    public List<PoAction> findActionByProcessDefId(String procDefId);
    
    /**
     * loads all current active Actions
     * @return List of PoActions
     */
    public List<PoAction> loadAllCurrentActions();

	/**
	 * @param date
	 * @return all actions that are valid at the given date.
	 */
	public List<PoAction> findAllActions(Date date);
	
	/**
	 * @param date
	 * @param actionType
	 * @return all actions that are valid at the given date.
	 */
	public List<PoAction> findAllActionsOfType(Date date, int actionType);

	/**
	 * @param date
	 * @return all actions that are universally allowed
	 * and valid at the given date.
	 */
	public List<PoAction> findAllUniversallyAllowedActions(Date date);
	
    public void checkAction(PoAction action);

	public List<PoAPermissionClient> findActionPermissionClientWithActionAndClientF(PoAction action, PoClient client, Date date);

	public List<PoAPermissionPerson> findActionPermissionPersonWithActionAndPersonF(PoAction action, PoPerson person, Date date);

	public List<PoAction> findDirectlyLinkedActions(PoGroup group, Date date);
	

	/**
	 * @param action a <code>PoAction</code>
     * @param group a <code>PoGroup</code>
     * @param date
     * @return a <code>List</code> of <code>PoAPermissionRole</code> objects of a <code>PoRole</code>
     * whose role holder is the passed <code>group</code>. The assignments have to be valid at the passed 
     * <code>date</code> and have the given <code>action</code> assigned.
     */
    public List<PoAPermissionRole> findAPRoleWithActionAndGroup(PoGroup group, Date date,PoAction action);
    
    public boolean isDirectlyLinkedActionConfigs(PoGroup group, PoAction action, Date date);

	public List<PoAPermissionClient>  findActionPermissionClientWithActionAndClient(PoAction action, PoClient client, Date date);

	public List<PoAPermissionPerson> findActionPermissionPersonWithActionAndPerson(PoAction action, PoPerson person, Date date);

	public List<PoAPermissionGroup> findActionPermissionGroupWithActionAndGroup(PoAction action, PoGroup hierarchicalGroup, Date date);

	public List<PoAPermissionGroup> findActionPermissionGroupWithActionAndGroupAndInheritToChilds(PoAction action, PoGroup group, Date date, boolean b);

	/**
	 * <p>
	 * This function returns a <code>List</code> of <code>PoAPermissionRole</code> objects with
	 * the given <code>action</code> assigned. The <code>group</code> is the owner. All links
	 * have to be valid at the given <code>date</code>.
	 * 
	 * 
	 * @param action
	 * @param group
	 * @param date
	 * @return a <code>List</code> of <code>PoAPermissionRole</code> objects.
	 * 
	 */
	public List<PoAPermissionRole> findActionPermissionRoleWithActionAndGroup(PoAction action, PoGroup group, Date date);
        
	/**
	 * @param person
	 * @param date
	 * @param action
	 * @return a <code>List</code> of <code>PoAPermissionRole</code> objects that are directly linked 
	 * with the given <code>person</code>. Only <i>normal</i> <code>PoRole</code>'s are considered. 
	 */
	public List<PoAPermissionRole> findAPRoleWithActionAndPerson(PoPerson person, Date date,PoAction action);

	public List<PoAPermissionRole> findActionPermissionRoleWithActionAndPersonAndRoleType(PoAction action, PoPerson person, Date date, int roleType);

	public List<PoAPermissionRole> findActionPermissionRoleWithActionAndGroupAndRoleType(PoAction action, PoGroup hierarchicalGroup, Date date, int dummy_role);

	public List<Object> findByQuery(String query);
	public List<Object> findByQueryAndNamedParameters(String query, String[] paramNames, Object[] values);

	/**
	 *  Returns a list of universally allowed actions.
	 */
	public List<PoAction> findActionsUniversallyAllowed();

	/**
	 * @return a <code>List</code> of <code>String</code>'s representing
	 * the <code>Processes</code>
	 */
	public List<String> findInUseProcessDefinitions();

	/**
	 * @param action
	 * @param date
	 * @return a <code>List</code> of <code>PoAction</code>'s that allow to execute 
	 * the given <code>action</code>.
	 */
	public List<PoAction> findActionsThatAllowAction(PoAction action, Date date);

	
}
