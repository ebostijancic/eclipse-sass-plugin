package at.workflow.webdesk.po;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.model.OrgTree;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.po.model.PoParentGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoPersonGroup;
import at.workflow.webdesk.po.model.PoPersonImages;
import at.workflow.webdesk.tools.api.UserLookupService;
import at.workflow.webdesk.tools.api.UserPasswordService;

/**
 * <p>
 * 	From an information representation point of view, this service provides functionality 
 *  to handle an organisation. There are several objects involved, the most important objects are
 *  <ul>
 *  	<li>PoClient</li> can be assigned to several
 *  	<li>PoPerson</li> objects. These can be assigned to several
 *  	<li>PoGroup</li> objects. Person and Groups are linked via a 
 *  	<li>PoPersonGroup</li> objects. here is also the historisation captured.
 *  	<li>PoOrganisationStructure</li> objects are linked with groups.
 *  	<li>PoAction</li> and 
 *  	<li>PoRole</li> objects are handled in other Services, but are mentioned for the sake 
 *  	of completeness.
 *  </ul>
 * The service provides functionality to store, update, delete and link these objects. Read through
 * the javadocs to get an idea of what can be done. 
 * </p>
 * 
 * @see at.workflow.webdesk.po.PoActionService
 * @see at.workflow.webdesk.po.PoOrganisationService
 * @see at.workflow.webdesk.po.PoRegistrationService
 * @see at.workflow.webdesk.po.PoModuleService
 * @see at.workflow.webdesk.po.PoFileService
 * @see at.workflow.webdesk.po.PoLogService
 * @see at.workflow.webdesk.po.PoLanguageService
 * @see at.workflow.webdesk.po.PoBeanPropertyService
 * @see at.workflow.webdesk.po.PoJobService
 * @see at.workflow.webdesk.po.PoActionService
 * @see at.workflow.webdesk.po.PoRegistrationService
 * @see at.workflow.webdesk.po.PoModuleService
 * @see at.workflow.webdesk.po.PoFileService
 * @see at.workflow.webdesk.po.PoLogService
 * @see at.workflow.webdesk.po.PoLanguageService
 * @see at.workflow.webdesk.po.PoBeanPropertyService
 * @see at.workflow.webdesk.po.PoJobService
 *
 * Created on 01.07.2005
 * @author hentner (Harald Entner)
 * @author ggruber
 */
public interface PoOrganisationService extends UserLookupService, UserPasswordService {
	
	
/*---------------------------------------------------------------------------------------------------------------------------------------------------
 * Clients
 * 
 * 
 *---------------------------------------------------------------------------------------------------------------------------------------------------
 */
	
	/**
	 * @param uid Unique ID of the client.
	 * @return corresponding client with ID=uid
	 */
	public PoClient getClient(String uid);
	
	
	/**
	 * @param client PoClient object.
	 * 
	 * saves the client
	 */
	public void saveClient(PoClient client);
	/**
	 * @return list of all clients
	 */
	public List<PoClient> loadAllClients();
	/**
	 * @param name String with the name of the client.
	 * @return list of clients with given name
	 */
	public PoClient findClientByName(String name);
	
    
    /**
	 * 
	 * Deletes the corresponding client (validto is set to now.)
     * 
     * not implemented so far, because clients have no validfrom/to
	 * 
	 * @param client PoClient object that should be deleted.
	 * @throws PoException
	 */
	public void deleteClient(PoClient client);
	
    
    /**
     * 
     * Deletes the corresponding client from the database.
     * 
     * @param client PoClient object that should be deleted.
     * @throws PoException
     */
    public void deleteAndFlushClient(PoClient client);
    
    
	
	/**
	 * @param name
	 * @return true if a client with the given name exists.
	 */
	public boolean isClientExistent(String name);
		
	
	
    /**
     * Checks if it is valid to store the given person in the database.
     * Throws a PoRuntimeException if a unique constraint is violated.
     * 
     * @param person
     */
    public void checkUser(PoPerson person);
	
    /**
     * Checks if it is valid to store the given group in the database.
     * Throws a PoRuntimeException if a unique constraint is violated.
     * 
     * @param group
     */
    public void checkGroup(PoGroup group);

    /**
	 * @param name
	 * @return true if a OrgStructure with the given name exists.
	 */
	public boolean isOrgStructureExistent(String name, String client_uid);

	
	/**
	 * @param client
	 * @param date
	 * @return a list of <code>PoPerson</code> objects that are assigned to the 
	 * given <code>client</code>. The assignment has to be valid now or in future,
	 * regarding the given <code>date</code>. 
	 */
	public List<PoPerson> findPersonsOfClientF(PoClient client, Date date);
		
	/**
	 * @param client
	 * @param date
	 * @return a list of <code>PoGroup</code> objects that are assigned to the 
	 * given <code>client</code>. The assignment has to be valid now or in future,
	 * regarding the given <code>date</code>. 
	 */
	public List<PoGroup> findGroupsFromClientF(PoClient client, Date date);
    
    
    /**
     * @param orgStructure a PoOrgStructure object
     * @param date 
     * @return all groups that have no parent assigned and corresponds to the given <code>orgStructure</code>.
     * The groups have to be valid at the given date.
     */
    public List<PoGroup> findGroupsWithoutParent(PoOrgStructure orgStructure, Date date);
    
	

    /*---------------------------------------------------------------------------------------------------------------------------------------------------
     * OrgStructures
     * 
     * 
     *---------------------------------------------------------------------------------------------------------------------------------------------------
     */	/**
	 * 
	 * 
	 * @param uid Unique ID of the PoOrgStructure.
	 * @return a PoOrgStructure object with the given uid.
	 */
	public PoOrgStructure getOrgStructure(String uid);
	/**
	 * 
	 * @param orgStructure the PoOrgStructure that will be saved. 
	 */
	public void saveOrgStructure(PoOrgStructure orgStructure);
	/**
	 * @return a list of all OrgStructures. 
	 */
	public List<PoOrgStructure> loadAllOrgStructures();
	/**
	 * @param client PoClientObject that should be assigned to the OrgStructures.
	 * @return a list of OrgStructures with the assigned client.
	 */
	public List<PoOrgStructure> loadAllOrgStructures(PoClient client);
	
	/**
	 * @param client PoClient object.
	 * @param key name of the OrgStructure
	 * @return a list of OrgStructures with the assigned client and the given key.
	 */
	public PoOrgStructure findOrgStructureByName(PoClient client, String key);
	
	/**
	 * TODO documentation is missing!
	 * @param client PoClientObject.
	 * @return a PoOrgStructure object of the given client with orgType organisation hierarchy
	 */
	PoOrgStructure getOrgHierarchy(PoClient client);
	
	/**
	 * @param client
	 * @return clients PoOrgStructure with orgType = Locations
	 */
    PoOrgStructure getOrgLocations(PoClient client);

    /**
     * @param client
     * @return clients PoOrgStructure with orgType = cost centers
     */
    PoOrgStructure getOrgCostCenters(PoClient client);
    	
	/**
	 * @param orgStructure PoOrgStructure object. 
	 * @throws PoException Exception that is thrown if an error occurs. 
	 * 
	 * Deletes the OrgStructure (internally only validTo is set to now).
	 */
	public void deleteOrgStructure(PoOrgStructure orgStructure);
	

    /**
     * Deletes the OrgStructure from the database.
     * 
     * @param orgStructure
     */
    public void deleteAndFlushOrgStructure(PoOrgStructure orgStructure);
	
	/**
	 * @param group
	 * @param index
	 * @param date
	 * @return a PoGroup object if a parent group is found that is the group
	 * which is reached after "index" times calling getParentGroup(group)
	 */
	public PoGroup getParentGroupByIndex(PoGroup group, int index, Date date);
	
	
	/**
	 * @param orgStructure
	 * @return the maximum amount of groups on the path from 
	 * the root node (topLevelGroup) to the lowest leaf 
	 */
	public int getMaxDepthOfOrgStructure(PoOrgStructure orgStructure);
	

	/*---------------------------------------------------------------------------------------------------------------------------------------------------
	 * Groups
	 * 
	 * 
	 *---------------------------------------------------------------------------------------------------------------------------------------------------
	 */
    /**
     * @param uid Unique key of the group.
     * @return a PoGroup object with the assigend uid.
     */
    public PoGroup getGroup(String uid);
    
    /**
     * @param uid
     * @return the PoParentGroup object with the given uid
     */
    public PoParentGroup getParentGroup(String uid);
    

    /**
     * Note: It is not guaranteed that all <code>PoGroup</code> objects are valid at the current date, presumably many are not.
     * 
     * @return a List of <code>PoGroup</code> objects.
     */
    public List<PoGroup> loadAllGroups();
    
    
    /**
     *  Note: It is not guaranteed that all <code>PoParentGroup</code> obejcts are valid at the current date, presumably many are not.
     * 
     * @return a List of <code>PoParentGroup</code> objects.
     */
    public List<PoParentGroup> loadAllParentGroups();
    
	/**
	 * Save or update the <code>PoGroup</code> object.
	 * 
	 * @param group <code>PoGroup </code>object. If the uid
	 * is set only a update is done.
	 * 
	 * 
	 */
	public void saveGroup(PoGroup group);
	
	/**
	 * <p>Persists the given <code>PoParentGroup</code>.
	 * 
	 * @param pg <code>PoParentGroup</code>.
	 */
	public void saveParentGroup(PoParentGroup pg);
	
	/**
	 * Deletes the group (internally only validTo is set to now.)
	 * 
	 * @param group PoGroup object that should be deleted.
	 * 
	 */
	public void deleteGroup(PoGroup group);
	
	
	/**
	 * Deletes the group (group is removed from database).
	 * 
	 * @param group PoGroup object that should be deleted.
	 * 
	 */
	public void deleteAndFlushGroup(PoGroup group);
	
	/**
	 * In other words the link between group and parentgroup will be perma-
	 * nently deleted
	 * 
	 * @param pg deletes a PoParentGroup object without any contraints.
	 */
	public void deletePGWithoutConstraints(PoParentGroup pg);
	
	/**
	 * result is an object if a valid link at the given date is found, null otherwise.
	 * 
	 * @param childgroup
	 * @param parentgroup
	 * @param validAt
	 * @return a PoParentGroup object that link the childGroup to the parentGroup. The 
	 */
	public PoParentGroup getParentGroupLink(PoGroup childGroup, PoGroup parentGroup, Date validAt);
	
	/**
	 * @param parentGroup
	 * 
	 * Sets the validTo field of the PoParentGroup object to the
	 * current date.
	 */
	public void removeGroupFromParentGroup(PoParentGroup parentGroup); //löschen falls aktuell
	
	
	/**
	 * @param parentGroup
	 * @param validFrom
	 * @param validTo
	 * 
	 * changes the validTo or the validFrom field of the given 
	 * PoParentGroup object. In Addition, other objects may change
	 * according to the database constraints.
	 */
	public void changeValidityParentGroupLink(PoParentGroup parentGroup, Date validFrom, Date validTo);
	
	
	
	/**
	 * @param key String of the name that is used to find Groups.
	 * @return a list of groups with a name equal to the value in "key".
	 */
	public List<PoGroup> findGroupByName(String key);
	
	
	/**
	 * @param rhpersonlink
	 * @param validfrom
	 * @param validto
	 * 
	 * changes validfrom and validto dates of a persongroup link, if
	 * corrsponding group belongs to a structure which is not hierarchical
	 * Change of validfrom only possible, if link's current validity starts 
	 * in future and new value for validfrom also is in future.
	 * Change of validto only possible, if link is actual or starts in future AND
	 * validto is in future and new value of validto is in future.
	 * if any of those restrictions is not met -> throw an exception
	 */
	public void changeValidityPersonGroupLink(PoPersonGroup rhpersonlink, Date validfrom, Date validto);
	
	/**
	 * @param key String of the name that is used to find Groups.
	 * @param client PoClient object that is used to find the Groups.
	 * @return a list of groups with a name equals to the value in "key"
	 * and the client equals the passed PoClient.
	 */
	public PoGroup findGroupByName(String key, PoClient client);
	
	
	/**
	 * @param key String of the name that is used to find Groups.
	 * @param referenceDate
	 * @return a list of groups with a name equal to key and 
	 * the reference date in between validFrom and validTo
	 */
	public List<PoGroup> findGroupByName(String key, Date referenceDate);
	/**
	 * @param key String of the name that is used to find Groups.
	 * @param client PoClient object that is used to find the Groups.
	 * @param referenceDate
	 * @return a list of groups with a name equal to key and 
	 * the reference date in between validFrom and validTo and 
	 * the client equals the passed PoClient. 
	 */
	public PoGroup findGroupByName(String key, PoClient client, Date referenceDate);
    
    /**
     * @param name
     * @return
     */
    public PoGroup findGroupByShortName(String name);
    
    /**
     * @param name The <code>shortName</code> of the <code>PoGroup</code> object.
     * @param client The <code>client</code> of the given <code>PoGroup</code> object.
     * @return a <code>PoGroup</code> object, if one with the given
     * <code>shortName</code> and the given <code>client</code> exists.
     * Otherwise null is returned. The <code>PoGroup</code> object has
     * to be valid at the current date. 
     * 
     */
    public PoGroup findGroupByShortName(String name, PoClient client);
    
    /**
     * @param name
     * @param referenceDate
     * @return
     * 
     * @deprecated
     */
    public PoGroup findGroupByShortName(String name, Date referenceDate);
    
    /**
     * @param name The <code>shortName</code> of the <code>PoGroup</code> object.
     * @param client The <code>client</code> of the given <code>PoGroup</code> object.
     * @param referenceDate The date at which the <code>PoGroup</code> object have to be valid.
     * @return a <code>PoGroup</code> object, if one with the given
     * <code>shortName</code> and the given <code>client</code> exists.
     * Otherwise null is returned. The <code>PoGroup</code> object has
     * to be valid at the current date. 
     * 
     */
    public PoGroup findGroupByShortName(String name, PoClient client, Date referenceDate);
    
    /**
     * @param uids	list of UIDs of searched groups
     * @param names	list of search names for groups name or short name
     * @param date	due date
     * 
     * @return list of found groups
     */
	public List<PoGroup> findGroupsWithFilter(List<String> uids, List<String> names, Date date);

    /**
     * @param uids	list of UIDs of searched groups
     * @param names	list of search names for groups name or short name
     * @param from	from date
     * @param to	to date
     * 
     * @return list of found groups
     */
	public List<PoGroup> findGroupsWithFilter(List<String> uids, List<String> names, Date from, Date to);
    
	/**
	 * @param group
	 * @return a linear list of all child groups of the parent node group.
	 * The child groups must be valid at the current Date. 
	 **/
	public List<PoGroup> findAllChildGroupsFlat(PoGroup group);
	
	/**
	 * @param group
	 * @param effectiveDate
	 * @return a linear list of all <code>PoGroup</code> objects.
	 * 
	 * The child groups must be valid at the passed Date.
	 */
	public List<PoGroup> findAllChildGroupsFlat(PoGroup group, Date effectiveDate);
	
	
	/**
	 * @param group
	 * @return a list of <code>PoParentGroup</code> objects that are valid at the given <code>effectiveDate</code> 
	 */
	public List<PoParentGroup> findChildGroups(PoGroup group);
	
	/**
	 * @param group
	 * @param effectiveDate
	 * @return a list of <code>PoParentGroup</code> objects that are valid at the given <code>effectiveDate</code> 
	 */
	public List<PoParentGroup> findChildGroups(PoGroup group, Date effectiveDate);
	
	/** Same as findChildGroups(), but only UIDs get returned. */
    public List<String> findChildGroupUids(String groupUid, Date effectiveDate);


	/**
	 * @param group
	 * @param effectiveDate
	 * @return a linear list of directly linked childgroups. 
	 * @return a list of <code>PoParentGroup</code> objects that are valid at the given <code>effectiveDate</code> 
	 * or will become valid in the future..
	 **/
	public List<PoParentGroup> findChildGroupsF(PoGroup group, Date effectiveDate);
	
	/**
	 * Retrieves all the subgroups, past, current and future
	 * @param group
	 * @return
	 */
	public List<PoParentGroup> findChildGroupsAll(PoGroup group);
	
	
	/**
	 * @param group
	 * @return a list of groups that can be assigned as childs
	 */
	public List<PoGroup> findAvailableChildGroups(PoGroup group);

	/**
	 * @param group
	 * @return a list of groups that can be assigned as parent.
	 */
	public List<PoGroup> findAvailableParentGroups(PoGroup group);
	
	
	/**
	 * @param group
	 * @return the parent group of the passed group. 
	 * The parent group has to be valid at the current date.
	 */
	public PoParentGroup getParentGroup(PoGroup group);
	/**
	 * Returns a <code>PoParentGroup</code> object if a parent to the given
	 * <code>group</code> exists, null otherwise.
	 * 
	 * The link between group and parentGroup has to be valid at the given
	 * <code>referenceDate</code>.
	 * 
	 * 
	 * @param group
	 * @param referenceDate
	 * @return the <code>PoParentGroup</code> object of the 
	 * passed <code>group</code>.  
	 * 
	 */
	public PoParentGroup getParentGroup(PoGroup group, Date referenceDate);	
	
	/**
	 * @param group
	 * @param parent
	 * 
	 * Sets the parent group of the group "group" to "parent".
	 * The relation becomes immediately valid.
	 */
	public void setParentGroup(PoGroup group, PoGroup parent);
	
	/**
	 * @param group
	 * @param parent
	 * @param now
	 * 
	 * Sets the parent group of the group "group" to parent. The relation becomes
	 * valid at the passed date.
	 */
	public void setParentGroup(PoGroup group, PoGroup parent, Date validFrom, Date validTo);
	
	/**
	 * Retrieves all the parent groups, past, present and future
	 * @param group
	 * @return
	 */
	public List<PoParentGroup> findParentGroupsAll(PoGroup group);
	
    // Persons --------------------------------------------------------------------------------------------------------------------------------------------------
	
	
	/**
	 * @param uid
	 * @return a PoPerson object.
	 */
	public PoPerson getPerson(String uid);
	
	
	/**
	 * @param uid
	 * @return the PoPersonGroup object with the given uid.
	 */
	public PoPersonGroup getPersonGroup(String uid);
	
	/**
	 * @param person
	 * @param orgStructure
	 * @param validAt
	 * @return a list of PoPersonGroup objects that are assigned to the given person and to the 
	 * given orgStructure. If a hierarchical orgStructure is used, only one list element 
	 * will be returned (only one hierarchical group is allowed at a given moment)
	 * 
	 */
	public List<PoPersonGroup> getPersonGroups(PoPerson person, PoOrgStructure orgStructure, Date validAt);
	
	
	/**
	 * @param person
	 * @param groupUid
	 * @param validAt
	 * @return the PoPersonGroup object that links the person to the given group. 
	 * The PoPersonGroup object has to be valid at the given date.
	 */
	public PoPersonGroup getPersonGroupLink(PoPerson person, PoGroup group,Date validAt);
	
	/**
	 * @param group
	 * @param validAt
	 * @return the Depth of the given Group in the OrgStructure. Root has depth 0.
	 */
	public int getDepthOfGroup(PoGroup group, Date validAt);
	
	/**
	 * @param person
	 * @return a PoOrgStructure object which is assigned to
	 * the given person.
	 */
	public PoOrgStructure getOrgStructure(PoPerson person);
	/**
	 * @return a list with all persons.
	 * Every entry has to be cast to PoPerson.
	 */
	public List<PoPerson> loadAllPersons();
	
	
	/**
	 * @param date
	 * @return a list of all persons that are valid at the given date.
	 */
	public List<PoPerson> findAllPersons(Date date);
	
	/**
	 * @param date
	 * @return a list of all persons that are valid at the time of the query.
	 */
	public List<PoPerson> findAllCurrentPersons();
	
	/**
	 * @param date
	 * @param activeUser
	 * @return a list of all persons that are valid at the given date 
	 * and have required activation state.
	 */
	public List<PoPerson> findAllPersons(Date date, boolean activeUser);
	
	/**
	 * @param date
	 * @return a list of all active persons that are valid at the time of the query.
	 */
	public List<PoPerson> findAllCurrentActivePersons();
	
	/**
	 * @param person
	
	 * Stores the person in the database.
	 *  */
	public void savePerson(PoPerson person, PoGroup group);
	//public List findPersonByName(String key);
	
	public void updatePerson(PoPerson person);
	
	/**
	 * Save bank accounts of a person as timeline per usage code 
	 * @param person
	 */
	void saveBankAccounts(PoPerson person);

	/**
	 * Terminates still valid accounts.
	 * @param person
	 */
	void historicizeBankAccounts(PoPerson person);
    
    /**
     * re-attaches detached hibernate object to the session.
     * uses lock-mode NONE -> can be called on any detached object which
     * is stored for instance inside the user-session.
     * 
     * @param obj (hibernate detached object)
     */
    public void refresh(Object obj);

    /**
	 * @param employeeId the <code>employeeId</code> of the <code>PoPerson</code>.
	 * @return a <code>PoPerson</code> if one with the given <code>employeeId</code> was found, 
	 * <code>null</code> otherwise.
	 */
	public PoPerson findPersonByEmployeeId(String employeeId);

    
	/**
	 * @param employeeId the <code>employeeId</code> of the <code>PoPerson</code>.
	 * @param date the date on which the <code>PoPerson</code> has to be valid.
	 * @return a <code>PoPerson</code> if one with the given <code>employeeId</code> was found, 
	 * <code>null</code> otherwise.
	 */
	public PoPerson findPersonByEmployeeId(String employeeId, Date date);
	
	
	/**
	 * @param key
	 * @param from
	 * @param to
	 * @return a PoPerson object if a person with TaId=key is found.
	 */
	public PoPerson findPersonByTaId(String key, Date from, Date to);
	
	/**
	 * @param key
	 * @return a PoPerson object if a person with TaId=key is found.
	 */
	public PoPerson findPersonByTaId(String key, Date date);
	
    /**
     * @param key
     * @return a PoPerson object if a person with TaId=key is found.
     */
    public PoPerson findPersonByTaId(String key);
    
    
	/**
	 * finds PoPerson given the UserName of the Person
	 * throws an Exception if more than one Persons match
	 * returns null if no match was found.
	 * 
	 * @param username the username of the person to find.
	 * @return a PoPerson Object, which matches the Username
	 */
	public PoPerson findPersonByUserName(String username);
	
	public PoPerson findPersonByWorkflowId(String workflowId);
	
	/**
	 * @param email the mail address of the person to find.
	 * @return the person with passed mail address, or null if none found.
	 */
	public PoPerson findPersonByEmail(String email);
	
    /**
     * @param person
     * @param group
     * 
     *  Links a person to a group (is stored in database)
     *  the link is immediately valid.
     */
    public void linkPerson2Group(PoPerson person, PoGroup group);
    
    
    /**
     * @param person
     * @param group
     * @param effectiveDate
    
     * Links a person to a group (is stored in database)
     * The link is valid with the given validity Dates. 
     * 
     * if the link refers to a hierarchical group then the current
     * link to a group of this hierachical structure will be terminated
     * with the given validFrom Date. If the given Validto Date is smaller
     * than the minimum of the validTo field of the person and the group, 
     * another link (with the old group) has to be created 
     * to fill up the timeline into the future. This is also true, even if a 
     * future link exists, but the validTo field of the new relation is smaller than its
     * 
     * 
     * TODO needs to be implemented.
     *  */
    public void linkPerson2Group(PoPerson person, PoGroup group, Date validFrom, Date validTo);
 
    
    /**
     * Removes given person from given group when a relation exists, else does nothing.
     * This is no physical remove but historization.
     * @throws PoRuntimeException.ERROR_TRY_TO_DELETE_HIERARCHICAL_GROUP when group.getOrgStructure().isHierarchy().
     */
    public void removePersonFromGroup(PoPerson person, PoGroup group);

    /**
     * Removes given person from given group when a relation for given effectiveDate exists, else does nothing.
     * This is no physical remove but historization.
     * @throws PoRuntimeException.ERROR_TRY_TO_DELETE_HIERARCHICAL_GROUP when group.getOrgStructure().isHierarchy().
     */
    public void removePersonFromGroup(PoPerson person, PoGroup group, Date effectiveDate);    
    
    
    /**
     * @param person
     * @param group
     * @return true if person is a member of group.
     */
    public boolean isPersonActualMemberOfGroup(PoPerson person, PoGroup group);    
    
    
    /**
     * The function first checks if a group with the given <b>ShortName</b> exists.
     * If not it returns false, if it does exists and the given person
     * is assigned to the found group the function returns true, otherwise 
     * false.
     * 
     * 
     * @param person
     * @param group
     * @return true if person is a member of the given group, false if not.
     */
    public boolean isPersonActualMemberOfGroup(PoPerson person, String group);
    
    
    /**
     * @param person
     * @return a list of groups that are linked to person.
     */
    public List<PoGroup> findPersonsLinkedGroups(PoPerson person);
    
    
    /**
     * @param person
     * @param orgType one of PoConstants.STRUCTURE_TYPE_*.
     * @return a list of groups that are linked to person and an orgStructure with 
     * the orgStructureType equal to the passed orgStructuretype.
     * The groups have to be valid at the current time.
     */
    public List<PoGroup> findPersonsLinkedGroups(PoPerson person, int orgType);
    
    /**
     * @param person
     * @param effectiveDate
     * @return a list of groups that are linked to person. The groups have to be valid
     * at the passed date.
     */
    public List<PoGroup> findPersonsLinkedGroups(PoPerson person, Date effectiveDate);
    
    /**
     * @param person
     * @param orgStructureType
     * @param date
     * @return a <code>List</code> containing <code>PoGroup</code> objects.
     * <p>
     * This function can be used to find a <code>List</code> of <code>PoGroup</code> objects, 
     * which have to be valid at the <code>date</code>. 
     * <p>
     * The <code>orgStructureType</code> defines, to which <code>PoOrgstructure</code> the 
     * <code>PoGroup</code> belongs. If it is <code>-1</code> it is ommitted. 
     */
    public List<PoGroup> findPersonsLinkedGroups(PoPerson person, int orgStructureType, Date date);
    
    /**
     * get the Department of the given Person at the specified date
     * department ist the hiearchical PoGroup of the Person
     * 
     * @param person: PoPerson Object
     * @param date: java.util.Date
     * @return PoGroup Object
     */
    public PoGroup getPersonsHierarchicalGroup(PoPerson person, Date date);

    /**
     * @return the current department of the given Person, whereby
     * 		"department" is the person's (one and only one) hierarchical PoGroup.
     */
    public PoGroup getPersonsHierarchicalGroup(PoPerson person);
    
    /**
     * @return the current cost-center of the given Person, whereby
     * 		"cost-center" is the person's non-hierarchical PoGroup of organization-type
     * 		PoConstants.STRUCTURE_TYPE_COSTCENTERS.
     */
    public PoGroup getPersonsCostCenterGroup(PoPerson person);
    
    /**
     * @param orgType one of PoConstants.STRUCTURE_TYPE_*.
     * @return a list of PoPersonGroup objects that are assigned to the passed person. 
     * The appropriate Groups or rather the PoPersonGroup objects must be valid at the passed date.
     * If an integer that is >=0 is passed as the poOrgStructure, the corresponding groups have to 
     * belong to this orgStructure, otherwise the orgStructure is omitted.
     * 
     */
    public List<PoPersonGroup> findPersonGroups(PoPerson person, Date date, int orgType);
    
    
	 /**
	 * @param group
	 * @param validAt
	 * @return a list of PoPerson objects that are assigned to the given group.
	 * The assignment has to be valid at the given date.
	 */
	public List<PoPerson> findPersonsOfGroup(PoGroup group, Date validAt);
	 
	 /**
	 * @param group
	 * @param validAt
	 * @return a list of PoPerson objects that are assigned to the given group 
	 * The assignment has to be valid at the given date.
	 */
	public List<PoPerson> findPersonsOfGroupF(PoGroup group, Date validAt);
    
	/**
	 * 
	 * @param group
	 * @param from
	 * @param to
	 * @return a list of PoPerson objects that are assigned to the given group;
	 * The assignment has to be valid between from and to dates.
	 */
	public List<PoPerson> findPersonsOfGroup(PoGroup group, Date from, Date to);
	
    
    /**
     * @param orgStructure
     * @return a List of Group objects that belong to the passed orgStructure,
     * which are valid now or in the future
     */
    public List<PoGroup> findGroupsOfOrgStructureF(PoOrgStructure orgStructure, Date date);
    
    public List<PoGroup> findGroupsOfOrgStructure(PoOrgStructure orgStructure, Date date);
    
    /**
     * @param orgType one of PoConstants.STRUCTURE_TYPE_*.
     * @return a list of <code>PoPersonGroup</code> objects that are or become  valid after the given date..
     * If poOrgStructure is -1, the orgStructure is not taken into account.
     */
    public List<PoPersonGroup> findPersonGroupsF(PoPerson person, Date date, int orgType);
    	
    
    /**
     * @param person
     * @param date
     * @param poOrgStructure
     * @return a list of PoPersonGroup objects that are assigned to the passed person. 
     * The appropriate Groups or rather the PoPersonGroup objects must be valid at the passed date.
     * for the given orgstructure object     * 
     */
    public List<PoPersonGroup> findPersonGroups(PoPerson person, Date date, PoOrgStructure orgStructure);
    
    
    /**
     * @param person
     * @param date
     * @param poOrgStructure
     * @return a list of PoPersonGroup objects that are or become  valid after the given date..
     * for the given orgstructure object
     */
    public List<PoPersonGroup> findPersonGroupsF(PoPerson person, Date date, PoOrgStructure orgStructure);
    	
 
    /**
     * @param person
     * @param poOrgStructure
     * @return a list of all PoPersonGroup objects for the person
     * for the given orgstructure object
     */
    public List<PoPersonGroup> findPersonGroupsAll(PoPerson person, PoOrgStructure orgStructure);
    
    /**
     * @param person
     * @return a list of all PoPersonGroup objects for the person in past, present and future
     */
    public List<PoPersonGroup> findPersonGroupsAll(PoPerson person);
    
    
    /** Do not use this, it does not perform business logic, is for OrgAdminHelper only. */
    public void deletePersonGroupLink(PoPersonGroup pglink);
    
    
    
    /**
     * 
     * 
     * @param person
     * Sets the validto field to now. The change is stored in the database.
     * 
     */
    public void deletePerson(PoPerson person);
    /**
     * @param person
     * 
     * Deletes the person from database.
     */
    
    public void deleteAndFlushPerson(PoPerson person);
    
    
    /**
     * @param pg
     * 
     * Deletes the given <code>PoPersonGroup</code> link from the
     * database.
     * 
     * This happens for all person-group-links, except to hierarchical group
     * ones. It's crucial that the linked <code>PoPerson</code> object has a 
     * corresponding hierarchical group at any point in time (as long as 
     * the given person is valid).  
     * 
     */
    public void deleteAndFlushPersonGroupLink(PoPersonGroup pg);
    
    /**
     * @param person
     * @return the Values to the given person in a HashMap
     */
    public Map<String, Object> getPersonAsMap(PoPerson person);
    
    /**
     * get all Current PoGroups
     * @return List of PoGroups
     */
    public List<PoGroup> findCurrentGroups();
    
    public List<PoPerson> findPersonsOfClient(PoClient client, Date validAt);    

    /**
     * @param client
     * @param from
     * @param to
     * @return list of persons of client valid in the from / to interval
     */
    public List<PoPerson> findPersonsOfClient(PoClient client, Date from, Date to);    
    
    
    /**
     * @param group
     * @param date
     * @return a list of PoPersonGroup objects that are
     * assigned to the given group. The assignment has to be valid
     * at the given date or become valid in the future.
     */
    public List<PoPersonGroup> findPersonGroupsF(PoGroup group, Date date);
         
    /**
     * Retrieves all the PoPersonGroups that are, were or will be assigned to the group
     * @param group
     * @return
     */
    public List<PoPersonGroup> findPersonGroupsAll(PoGroup group);
    
    /**
     * Checks if the person has changed (in comparison to the person contained in the database)
     * (even this case is almost impossible, due to the person should be saved. otherwise 
     * update should be called)
     * 
     * @return true if a licence check is needed, false otherwise.
     */
    public Boolean checkNeededForSavePerson(PoPerson person, PoGroup group);
    
    /**
     * Checks if the person has changed (in comparison to the person contained in the database)
     * 
     * @return true if a licence check is needed, false otherwise.
     */
    public Boolean checkNeededForUpdatePerson(PoPerson person);
    
    /**
     * This function replaces the placeholder in headerText and returns the resulting text.
     * a placeholder is marked with a beginning and ending $ char. 
     * 
     * e.g. "$getFirstName()$ is my name". would result (i'm Harry) in 
     * "Harry is my name", taking into consideration that i'm logged in. 
     * 
     * The functions inside the $...$ signs, should be public for the 
     * PoPerson objects. Other functions are not considered so far. 
     * 
     * If the function does not exist, the function name is kept.
     * 
     * @return a String object
     */
    public String getResolvedHeaderText(PoPerson person);


	public List<PoGroup> findNotHierarchicalGroupsOfPerson(PoPerson person, Date date);
	


	public List<String> findUidsOfClients();


	/**
	 * Returns a list of <code>String</code> objects. These <code>String</code>'s are the <code>uid</code>'s 
	 * of <code>PoPerson</code>'s which are linked with the extracted<code>PoClient</code> objects. 
	 * Thus a list of <code>PoClient</code> uid's is required.
	 * 
	 * @param list a list of <code>String</code> objects. Each object should correspond to the 
	 * <code>uid</code> of a <code>PoClient</code>
	 * @param date a java.util.Date object
	 * @return a list of <code>String</code> objects. In other words the uid's of the 
	 * <code>PoPerson</code>'s.
	 *
	 */
	public List<String> findPersonIdsOfClients(List<String> clientUidList, Date date);
	
	/**
	 * Returns a list of <code>String</code> objects. These <code>String</code>'s are the <code>uid</code>'s 
	 * of <code>PoGroup</code>'s which are linked with the extracted<code>PoClient</code> objects. 
	 * Thus a list of <code>PoClient</code> uid's is required.
	 * 
	 * @param list a list of <code>String</code> objects. Each object should correspond to the 
	 * <code>uid</code> of a <code>PoClient</code>
	 * @param date a java.util.Date object
	 * @return a list of <code>String</code> objects. In other words the uid's of the 
	 * <code>PoGroup</code>'s.
	 *
	 */
	public List<String> findGroupIdsOfClients(List<String> clientUidList, Date date);

	/**
	 * Returns a list of <code>String</code> objects. These <code>String</code>'s are the <code>uid</code>'s 
	 * of <code>PoPerson</code>'s which are linked with the extracted<code>PoGroup</code> objects. 
	 * Thus a list of <code>PoGroup</code> uid's is required.
	 * 
	 * 
	 * @param list a list of <code>String</code> objects. Each object should correspond to the 
	 * <code>uid</code> of a <code>PoGroup</code>
	 * @param date a java.util.Date object
	 * @return a list of <code>String</code> objects. In other words the uid's of the 
	 * <code>PoPerson</code>'s.
	 *
	 */
	public List<String> findPersonUidsOfGroups(List<String> groupUidList, Date date);
	
    
	/**
	 * @param client The <code>PoClient</code> of the <code>PoOrgstructure</code> with the given <code>name</code>
	 * @param name The <code>name</code> of the <code>PoOrgStructure</code>.
	 * @return a <code>PoOrgstructure</code> with the given <code>name</code> and <code>client</code> or null if none was found.
	 */
	public PoOrgStructure findOrgStructureByNameAndClient(PoClient client, String name);
	
	/**
     * A list of valid <code>PoGroup</code> objects concerning the given <code>date</code>.
     * The given <code>person</code> has to be contained in the returned group, as well as 
     * be assigned to the given <code>orgStructure</code>.
     *
     * 
     * @param person A person contained in the groups 
     * @param os The Organisation Structure of the groups
     * @param date The date at which the groups have to be valid.
     * @return a List of <code>PoGroup</code> objects. 
     */
    public List<PoGroup> findGroupWithOrgStructureAndPerson(PoPerson person, PoOrgStructure os, Date date);


	/**
	 * This function links the given <code>List</code> of <code>PoPerson</code> objects with the given
	 * <code>PoGroup</code>. Persons which have been in this group prior to this call but which are now 
	 * not contained within parameter 1 will be removed! 
	 * 
	 * The <code>PoGroup</code> object should not be a hierarchical group, otherwise
	 * the already contained persons will not be removed when not contained in the <code>persons</code> list. 
	 * 
	 * @param persons a <code>List</code> of <code>PoPerson</code> objects.
	 * @param myGroup a <code>PoGroup</code> object, to which the given persons should be linked. 
	 * @return a List of added persons.
	 */
	public List<PoPerson> linkPersons2Group(List<PoPerson> persons, PoGroup myGroup);


	/**
	 * @param group a <code>PoGroup</code> object. Only <code>PoPerson</code> objects that 
	 * are linked with this <code>group</code> are considered.   
	 * @param date the <code>Date</code> on which the Assignment has to be valid.
	 * @param names a <code>List</code> of <code>String</code> objects which 
	 * should rather match the <code>firstName</code> or the <code>lastName</code>
	 * property of one <code>PoPerson</code> object. If one matched, it will be 
	 * added to the returned <code>ArrayList</code>.
	 * @return a <code>List</code> of <code>PoPerson</code> objects.
	 */
	public List<PoPerson> findPersonsOfGroup(PoGroup group, List<String> searchList, Date date);
	

	/**
	 * @see at.workflow.webdesk.po.PoOrganisationService#findPersonsWithFilter(List uids, List names, Date date);
	 */
	public List<PoPerson> findPersonsWithFilter(List<String> uids, List<String> names);
	
	/**
	 * @param uids a <code>List</code> of <code>String</code> objects, representing
	 * the <code>UID</code> of a <code>PoPerson</code> object. 
	 * @param names a <code>List</code> of <code>String</code> objects which 
	 * should rather match the <code>firstName</code> or the <code>lastName</code>
	 * property of one <code>PoPerson</code> object. If one object has matched, it will be 
	 * added to the returned <code>List</code>.
	 * @param date the date on which the person has to be valid.
	 * @return a <code>List</code> of <code>PoPerson</code> objects.
	 * 
	 * 
	 * @see at.workflow.webdesk.po.dao.PoGroupDAO#findPersonsOfGroup(PoGroup group, List names, Date date );
	 */
	public List<PoPerson> findPersonsWithFilter(List<String> uids, List<String> names, Date date);
	
	
	/**
	 * @param uids a <code>List</code> of <code>String</code> objects, representing
	 * the <code>UID</code> of a <code>PoPerson</code> object. 
	 * @param names a <code>List</code> of <code>String</code> objects which 
	 * should rather match the <code>firstName</code> or the <code>lastName</code>
	 * property of one <code>PoPerson</code> object. If one object has matched, it will be 
	 * added to the returned <code>List</code>.
	 * @param from	begin
	 * @param to	end
	 * @return a <code>List</code> of <code>PoPerson</code> objects.
	 * 
	 * 
	 * @see at.workflow.webdesk.po.dao.PoGroupDAO#findPersonsOfGroup(PoGroup group, List names, Date date );
	 */
	public List<PoPerson> findPersonsWithFilter(List<String> uids, List<String> names, Date from, Date to);
	
	
	
	
	/**
	 * @param viewPermissions a <code>Map</code> M(x,y) where x is a <code>String</code>
	 * object [<code>clients</code>, <code>groups</code>, <code>persons</code> are actually
	 * considered] and y is a List of <code>uid</code>'s. 
	 * 
	 * 
	 * @param names a <code>List</code> of <code>String</code> objects which 
	 * should rather match the <code>firstName</code> or the <code>lastName</code>
	 * property of one <code>PoPerson</code> object. If one object has matched, it will be 
	 * added to the returned <code>List</code>. The <code>*</code> character will be replaced 
	 * with <code>%</code> in the hql query
	 * 
	 * @param date
	 * @return a <code>List</code> of <code>PoPerson</code> objects.
	 */
	public List<PoPerson> findPersonsWithViewPermission(Map<String, List<String>> viewPermissions, List<String> names, Date date);
	
	/**
	 * @param viewPermissions a <code>Map</code> M(x,y) where x is a <code>String</code>
	 * object [<code>clients</code>, <code>groups</code>, <code>persons</code> are actually
	 * considered] and y is a List of <code>uid</code>'s. 
	 * 
	 * 
	 * @param names a <code>List</code> of <code>String</code> objects which 
	 * should rather match the <code>firstName</code> or the <code>lastName</code>
	 * property of one <code>PoPerson</code> object. If one object has matched, it will be 
	 * added to the returned <code>List</code>. The <code>*</code> character will be replaced 
	 * with <code>%</code> in the hql query
	 * 
	 * @param from	beginning
	 * @param to	end
	 * @return a <code>List</code> of <code>PoPerson</code> objects.
	 */
	public List<PoPerson> findPersonsWithViewPermission(Map<String, List<String>> viewPermissions, List<String> names, Date from, Date to);
	
	/**
	 @see at.workflow.webdesk.po.PoOrganisationService#findPersonsWithViewPermission(Map viewPermissions, List names, Date date)</code>
	 */
	public List<PoPerson> findPersonsWithViewPermission(Map<String, List<String>> viewPermissions, List<String> names);
	
	
    /**
     * <p>Returns an <code>OrgTree</code> object which represents 
     * the hierarchical structure of the underlying groups. This function
     * only makes sense for <code>PoOrgStructure</code> objects with 
     * property hierarchy set to true. 
     * </p> 
     * <p>
     * This function will be cached. Consider that 
     * the OrgTree has a <code>validTo</code> property, that 
     * states until when tree is valid. As the 
     * caching is declarative, we manually have to check this.
     * </p>
     * <p>The current date is used to query the database. If 
     * you need the orgModel in past or in future use 
     * getOrgModel(2) 
     * </p>
     * 
     * 
     * @param orgS
     * @return an <code>OrgTree</code>
     */
    public OrgTree getOrgModelCached(PoOrgStructure orgS);
    
    /**
     * <p>Returns an <code>OrgTree</code> object which represents 
     * the hierarchical structure of the underlying groups. This function
     * only makes sense for <code>PoOrgStructure</code> objects with 
     * property hierarchy set to true. 
     * </p> 
     * 
     * @param orgS
     * @param date
     * @return
     */
    public OrgTree getOrgModel(PoOrgStructure orgS, Date date);
	
    
    
	/**
	 * @param os
	 * @param date
	 * @return a <code>List </code> of <code>PoGroup</code> objects that are 
	 * at the top level of the given <code>PoOrgStructure</code>. The 
	 */
	public List<PoGroup> getTopLevelGroupsOfOrgStructureWithDate(PoOrgStructure os, Date date, boolean includeFlag);

	/**
	 * @param os
	 * @param includeFlag
	 * @return a <code>List </code> of <code>PoGroup</code> objects that are 
	 * at the top level of the given <code>PoOrgStructure</code>. The <code>toplevel</code>
	 * flag of the returned <code>PoGroup</code>'s is equal to <code>includeFlag</code>.
	 *   
	 */
	public List<PoGroup> getTopLevelGroupsOfOrgStructure(PoOrgStructure os, boolean includeFlag);

	/**
	 * <p>
	 * Saves or updates the <code>PoPersonGroup</code> instance, 
	 * depending on the <code>uid</code> of the object. 
	 * 
	 * 
	 * @param newPg a <code>PoPersonGroup</code>
	 * 
	 * 
	 */
	public void savePersonGroup(PoPersonGroup newPg);
	

	
	/**
	 * @param possibleChildGroup
	 * @param possibleParentGroup
	 * @return a <code>PoGroup</code> which is the parent of the <code>possibleChildGroup</code>, if
	 * and only if a <code>PoGroup</code> is found which is a child (or ev. the group itself) of 
	 * <code>possibleParentGroup</code>. If no childgroup is found <code>null</code> is returned.
	 */
	public PoGroup isGroupChildGroup(PoGroup possibleChildGroup, PoGroup possibleParentGroup);

	/**
	 * WDHREXPERT-414
	 * @param personsHierarchicalGroup
	 * @param referenceDate
	 * @return 2nd Level OE (from top) in organigram if organigram has only one root, otherwise the root-OE
	 */
	PoGroup getDepartment(PoGroup group, Date referenceDate);
	
	/**
	 * <p>
	 * Use this function to get a <code>List</code> of 
	 * <code>PoParentGroup</code> objects that connect 
	 * <code>parent</code> and <code>child</code>.
	 * 
	 * @param parent
	 * @param child
	 * @return a <code>List</code> of <code>PoParentGroup</code> objects 
	 * that have <code>parent</code> as parent and <code>child</code> as 
	 * child. 
	 */
	public List<PoParentGroup> findParentGroups(PoGroup parent, PoGroup child);


	/**
	 * <p>
	 * Permanently deletes the given <code>PoParentGroup</code>
	 * 
	 * @param delPg
	 * 
	 **/
	public void deleteAndFlushParentGroup(PoParentGroup delPg);
	
	/**
	 * resolves given Group (defined by its shortName) to a List of PoPerson objects
	 * 
	 * @param shortName of Group
	 * @param includeChildGroups: should we include the subGroups?
	 * @param date: date for which we should carry out the operation
	 * 
	 * @return List of PoPerson objects
	 */
	public List<PoPerson> resolveGroupToPersons(String shortName, boolean includeChildGroups, Date date);

	/**
	 * Getter for PoPersonImages
	 * @param uid
	 * @return PoPersonImages
	 */
	PoPersonImages getPersonImages(String uid);
	
	/** @return the sub-list of personGroups that belong to the given orgStructure. */
	List<PoPersonGroup> filterPersonGroups(Collection<PoPersonGroup> personGroups, PoOrgStructure structure);

}
