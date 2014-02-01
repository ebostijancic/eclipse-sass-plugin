package at.workflow.webdesk.po;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleCompetenceAll;
import at.workflow.webdesk.po.model.PoRoleCompetenceBase;
import at.workflow.webdesk.po.model.PoRoleCompetenceClient;
import at.workflow.webdesk.po.model.PoRoleCompetenceGroup;
import at.workflow.webdesk.po.model.PoRoleCompetencePerson;
import at.workflow.webdesk.po.model.PoRoleDeputy;
import at.workflow.webdesk.po.model.PoRoleHolderDynamic;
import at.workflow.webdesk.po.model.PoRoleHolderGroup;
import at.workflow.webdesk.po.model.PoRoleHolderLink;
import at.workflow.webdesk.po.model.PoRoleHolderPerson;

/**
 * <p>This service can handle <code>PoRole</code> objects.
 * Additionally following objects are handled:
 * <ul>
 * 	<li>PoRoleCompetenceBase (PoRoleCompetenceAll, PoRoleCompetenceClient, PoRoleCompetenceGroup, PoRoleCompetencePerson)
 *  <li>PoRoleHolderPerson, PoRoleHolderGroup, PoRoleHolderDynamic
 *  <li>PoRoleDeputy
 * </ul>
 * </p><p>
 * Additonally, it is possible to find <code>PoPerson</code> objects that are
 * assigned to a given <code>PoRole</code> object, which can handle <code>PoAction</code>
 * assignments and view permissions.
 * </p>
 * 
 * Created on 01.07.2005
 * @author hentner (Harald Entner)
 * @author ggruber
 * @author sdzuban (client stuff)
 */
public interface PoRoleService {
	
	/** Constant for the PoRoleHolderDynamic object. */
	public static final String DYNAMIC_TARGET_ALL = "ALL";
	/** Constant for the PoRoleHolderDynamic object. */
	public static final String DYNAMIC_TARGET_PERSON = "Person";
	/** Constant for the PoRoleHolderDynamic object. */
	public static final String DYNAMIC_TARGET_GROUP = "Group";

	/** TODO document me. */
	public static final int DYNAMIC_TYPE_OWN_HIERARCHY 		= 0;
	/** TODO document me. */
	public static final int DYNAMIC_TYPE_OWN_HIERARCHYPLUS  = 1;
	/** TODO document me. */
	public static final int DYNAMIC_TYPE_OWN_CLIENT 		= 2;
	/** TODO document me. */
	public static final int DYNAMIC_TYPE_ALL_CLIENTS 		= 3;
	
	/** TODO document me. */
	public static final int ROLEOWNER_TYPE_PERSON = 0;
	/** TODO document me. */
	public static final int ROLEOWNER_TYPE_GROUP = 1;
	/** TODO document me. */
	public static final int ROLEOWNER_TYPE_DYNAMIC = 2;


    /**
     * With this function a role can be loaded from database, if the <code>uid</code> is known. 
     * <pre>
     * Example:
     * PoRoleService rs = new PoRoleService();
     * PoRole loadedRole = rs.getRole(uid);
     * </pre>
     * @param uid the uid of the role that should be loaded.
     * @return the role with the given uid.
     */
    public PoRole getRole(String uid);

    /**
     * @return a <code>PoRoleHolderDynamic</code> if one with the given
     * <code>uid</code> was found, <code>null</code> otherwise.
     */
    public PoRoleHolderDynamic getRoleHolderDynamic(String uid);
    
    public PoRoleCompetenceBase getRoleCompetenceBase(String uid);
    
    /**
     * Deletes the given <code>PoRoleHolderDynamic</code> object from the database.
     */
    public void deleteAndFlushRoleHolderDynamic(PoRoleHolderDynamic rhd);
    
    /**
     * Assigns the given <code>role</code> to the given <code>person</code>.
     * Because no competence target is defined, the person has rights
     * for everyone.<br/>
     * The ranking determines which person is higher in the hierarchy.<br/> 
     * The link is immediately valid and valid until the infinite Date is reached.
     */
    public void assignRole(PoRole role, PoPerson person, int ranking);
    
    /**
     * Assigns the given group the given role. The ranking determines which 
     * person is higher in the hierarchy. The link is immediately valid and 
     * valid until the infinite Date is reached. 
     */
    public void assignRole(PoRole role, PoGroup group, int ranking);
    
    /**
     * This function can be used to generate a dynamic role holder. The <code>dynamicType</code> determines the role 
     * holder (own group, own client, ...) which is in charge of the <code>target</code>. The <code>target</code> can 
     * be <code>ALL</code>, <code>Person</code>, or <code>Group</code>. If one of the latter two is used as <code>target</code>
     * the the <code>targetId</code> must not be <code>null</code>. Otherwise it can be <code>null</code>. 
     * <p>
     * <code>validFrom</code> and <code>validTo</code> determine the validity of the assignment. 
     * 
     * @param dynamicType an <code>int</code> value (TODO this can be seen, but what does it mean?)
     * @param target one of [ALL, Person, Group]
     * @param targetId the id of the <code>PoPerson</code> or <code>PoGroup</code>.
     * 		If <code>target</code> is null, this field is not considered and thus can be <code>null</code>.
     */
    public void assignDynamicRoleHolder(int dynamicType, PoRole role, String target, String targetId, Date validFrom, Date validTo);
    
    /**
     * @return a <code>List</code> of <code>PoRoleHolderDynamic</code> objects that
     * 		are valid at the <code>date</code> or will become valid after that.
     */
    public List<PoRoleHolderDynamic> findRoleHolderDynamicF(PoRole role, Date date);
    
    /**
     * Assigns the given <code>role</code>to the given <code>person</code>. 
     * The ranking determines which <code>person</code> is higher in the hierarchy.
     * The link is valid from the Date <code>validFrom</code>.  If <code>validTo</code> is <code>null</code>,
     * the infinite Date is used.  
     */
    public void assignRole(PoRole role, PoPerson person, Date validFrom, Date validTo, int ranking);
    
    /**
     * Assigns the given <code>role</code> to the given <code>group</code>. 
     * The ranking determines which <code>group</code> is higher in the hierarchy.
     * The link is valid from the Date <code>validFrom</code>. If <code>validTo</code> is null, 
     * the infinite Date is used.  
     */
    public void assignRole(PoRole role, PoGroup group, Date validFrom, Date validTo, int ranking);
    
    /**
     * Assigns the given <code>role</code> to the given <code>person</code>. 
     * The ranking determines which <code>person</code> is higher in the hierarchy.
     * The <code>person</code> has the competence over the given <code>group</code>.
     * The link is valid from the Date </code>validFrom</code>. If <code>validTo</code> is null, 
     * the infinite Date is used. 
     * 
     * @param person PoPersonObject, determines the person that has the competence over the given client.
     * @param client the client that is allowed to be controlled.
     * @param validFrom The date from which the action is valid and recognized. 
     * @param validTo The date until the action is valid. 
     */
    public void assignRoleWithClientCompetence(PoRole role, PoPerson person, PoClient client, Date validFrom, Date validTo, int ranking);
    
    /**
     * Assigns the given role to the given group. The ranking determines which group is higher in the hierarchy.
     * The group (competenceGroup) has the competence over the given client (controlledClient).
     * The link is valid from the Date "validFrom". If "validTo" is null, the infinite Date is used. 
     * 
     * @param group PoGroup object, determines the group that has competences.
     * @param client PoClient object, determines the client that is controlled.
     * @param validFrom Date, from which the link is true.
     * @param validTo Date, to which the link is true
     */
    public void assignRoleWithClientCompetence(PoRole role, PoGroup group, PoClient client, Date validFrom, Date validTo, int ranking);
    
    /**
     * Assigns the given <code>role</code> to the given <code>person</code>. 
     * The ranking determines which <code>person</code> is higher in the hierarchy.
     * The <code>person</code> has the competence over the given <code>group</code>.
     * The link is valid from the Date </code>validFrom</code>. If <code>validTo</code> is null, 
     * the infinite Date is used. 
     * 
     * @param person PoPersonObject, determines the person that has the competence over the given group.
     * @param group the group that is allowed to be controlled.
     * @param validFrom The date from which the action is valid and recognized. 
     * @param validTo The date until the action is valid. 
     */
    public void assignRoleWithGroupCompetence(PoRole role, PoPerson person, PoGroup group, Date validFrom, Date validTo, int ranking);
	
	/**
     * Assigns the given role to the given group. The ranking determines which group is higher in the hierarchy.
     * The group (competenceGroup) has the competence over the given group (controlledGroup).
     * The link is valid from the Date "validFrom". If "validTo" is null, the infinite Date is used. 
     * 
	 * @param competenceGroup PoGroup object, determines the group that has competences.
	 * @param controlledGroup PoGroup object, determines the group that is controlled.
	 * @param validFrom Date, from which the link is true.
	 * @param validTo Date, to which the link is true
	 */
    public void assignRoleWithGroupCompetence(PoRole role, PoGroup competenceGroup, PoGroup controlledGroup, Date validFrom, Date validTo, int ranking);
	
    /**
     * @return a <code>List</code> of all active <code>PoRole</code> objects.
     */
    public List<PoRole> findAllActiveRoles();
    
	/**
	 * <p>Use this function to assign the <code>PoRole</code> <code>role</code>
	 * to the <code>competencePerson</code>. The generated <code>PoRoleCompetencePerson</code>
	 * object will be linked with the <code>PoRoleHolderPerson</code> object. 
	 * </p><p>
	 * The <code>PoRoleCompetencePerson</code> object is not necessarily generated, 
	 * if one with competence for the given <code>competencePerson</code> exists,
	 * it is reused. 
	 * </p><p>
	 * The validity is handled via the <code>PoRoleHolderPerson</code> object.</p>
	 * 
	 * @param competencePerson the officeHolder of the <code>PoRole</code>.
	 * @param ranking the ranking defines the ordering when <code>findAuthority</code> is called.
	 */
	public void assignRoleWithPersonCompetence(PoRole role, PoPerson competencePerson, PoPerson controlledPerson, Date validFrom, Date validTo, int ranking);
	
	/**
     * Assigns the given role to the given group. The ranking determines which group is higher in the hierarchie.
     * The group has the competence over the given person.
     * The link is valid from the Date "validFrom". If "validTo" is null, the infinite Date is used. 
     * 
	 * @param competenceGroup PoGroup object, determines the group that has competences.
	 * @param controlledPerson PoPerson object, determines the person that is controlled.
	 */
	public void assignRoleWithPersonCompetence(PoRole role, PoGroup competenceGroup, PoPerson controlledPerson, Date validFrom, Date validTo, int ranking);
	
    /**
     * Loads all roles (including 'dummy roles')
     * @return a list of all roles that are actually valid.
     */
    public List<PoRole> loadAllRoles();
    
	/**
     * Loads all 'normal' roles for the specified client
	 * @return a list of valid roles, with the given client assigned.
	 */
	public List<PoRole> loadAllRoles(PoClient client);
	
    /**
     *	Saves the given role in database.
     */
    public void saveRole(PoRole role);
    
    /**
     * @return a list of roles which names are equal to the given key.
     */
    public List<PoRole> findRoleByName(String key);
    
    /**
	 * Returns a role with given name for the specified client.
	 * NOTE: does not return a role which can be used by *ALL* clients.
     *
     * @param key the name of the <code>PoRole</code>, must not be null.
     * @param client the <code>PoClient</code> of the <code>PoRole</code>, must not be null.
     * @return a list of roles which names are equal to the given key, and the client equal to the given client.
     */
    public PoRole findRoleByName(String key, PoClient client);
    
    /**
	 * Returns a role with given name for the specified client, or no client (client is null).
	 * NOTE: might return a role which can be used by *ALL* clients.
     *
     * @param roleName the name of the <code>PoRole</code>, must not be null.
     * @param client the <code>PoClient</code> of the <code>PoRole</code>, must not be null.
     * @return the role with given name and client either null or equal to the given client.
     */
    public PoRole findRoleByNameAndGivenOrNullClient(String roleName, PoClient client);
    
    /**
	 * Returns true when given person has a role with given name that is either
	 * associated with its client, or has no client at all (client is null).
	 * (So this implements a defaulting mechanism for role ownership of persons.)
     *
     * @param person the person to search the role for, must not be null.
     * @param roleName the name of the <code>PoRole</code>, must not be null or empty.
     * @return true when role with given name and a client, either equal to the person's client or being null, exists.
     */
    public boolean hasPersonRole(PoPerson person, String roleName);
    
	/**
	 * @return a list of roles  which names are equal to the given key and
	 * 		the reference Date is valid from the reference Date.
	 */
	public List<PoRole> findRoleByName(String key, Date referenceDate);
	
	/**
	 * Returns a role with given name for the specified client.
	 * NOTE: does not return a role which can be used by *ALL* clients.
	 * 
	 * @param key the name of the <code>PoRole</code>.
	 * @param client the <code>PoClient</code> of the <code>PoRole</code>.
	 * @param referenceDate the <code>Date</code> on which the <code>PoRole</code> has to be active.
	 * @return a <code>PoRole</code>
	 */
	public PoRole findRoleByName(String key, PoClient client, Date referenceDate);
	
	/**
	 * given the workflow participant id (from xpdl) finds the corresponding
	 * PoRole object and returns it. if not found returns null.
	 * throws an error, if more than one match exists.
	 * 
	 * @return PoRole Object of role to be found
	 */
	public PoRole findRoleByParticipantId(String key);
	
	/**
	 * given the workflow participant id (from xpdl) and a
	 * referencedate, finds the corresponding
	 * PoRole object which is valid at that date and returns it. 
	 * if not found returns null.
	 * throws an error, if more than one match exists.
	 * 
	 * @param effectiveDate: Date at which Role to be search should be valid
	 * @return PoRole Object of role to be found
	 */
	public PoRole findRoleByParticipantId(String key, Date effectiveDate);
	
	/**
	 * given the workflow participant id (from xpdl) and a
	 * a poClient object, finds the corresponding
	 * PoRole object and returns it. 
	 * if not found returns null.
	 * throws an error, if more than one match exists.
	 * 
	 * @param client: PoClient object for which Role ist searched
	 * @return PoRole Object of role to be found
	 */
	public PoRole findRoleByParticipantId(String key, PoClient client);
	
	/**
	 * given the workflow participant id (from xpdl), 
	 * a poClient object and a referenceDate, finds the corresponding
	 * PoRole object, which is valid at the Date and for the client
	 * and returns it. 
	 * if not found returns null.
	 * throws an error, if more than one match exists.
	 * 
	 * @param client: PoClient object for which Role ist searched
	 * @return PoRole Object of role to be found
	 */
	public PoRole findRoleByParticipantId(String key, PoClient client, Date effectiveDate);
	
    /**
     * If force is set to true, the role is deleted even if role holders exists. Otherwise an exception is thrown. If 
     * no role holders exists, deleteRole(role,false) is equal to deleteRole(role,true). Everything is still in the database, 
     * although nothing is valid any more.
     */
    public void deleteRole(PoRole role,boolean force);
    
    /**
     * Deletes the role from the database. (Including all corresponding role holders.)
     */
    public void deleteAndFlushRole(PoRole role);
    
    /**
     * @param uid the <code>uid</code> of the <code>PoRoleCompetenceAll</code> object to return.
     * @return a <code>PoRoleCompetenceAll</code> object or null if none with the given <code>uid</code> was found.
     */
    public PoRoleCompetenceAll getRoleCompetenceAll(String uid);
    
    /**
     * @return the role holder person with the corresponding rhp uid
     */
    public PoRoleHolderPerson getRoleHolderPerson(String uid);
    
    /**
     * @return rhe role holder group with the corresponding rhg uid
     */
    public PoRoleHolderGroup getRoleHolderGroup(String uid);
    
    /**
     * @return true if the given Object is of type PoRoleCompetencePerson
     */
    public boolean isRoleCompetencePerson(PoRoleCompetenceBase rhb);
    
	/**
	 * @return true if the given Object is of type PoRoleCompetenceGroup
	 */
	public boolean isRoleCompetenceGroup(PoRoleCompetenceBase rhb);
    
	/**
	 * @return true if the given Object is of type PoRoleCompetenceClient
	 */
	public boolean isRoleCompetenceClient(PoRoleCompetenceBase rhb);
	
	/**
	 * @return true if the given Object is of type PoRoleCompetenceAll
	 */
	public boolean isRoleCompetenceAll(PoRoleCompetenceBase rhb);
    
    /**
     * Stores the given roleHolder in the database.
     */
    public void saveRoleCompetence(PoRoleCompetenceBase roleCompetence);

    /**
     * @return a list of <code>PoClient</code> objects which are under control of the 
     * given person which is assigned to the given role.
     */
    public List<PoClient> findCompetenceClientsOfPerson(PoPerson person, PoRole role, Date date);
    
    /**
     * @return a list of <code>PoGroup</code> objects which are under control of the 
     * given person which is assigned to the given role. The PoRoleHolderPerson objects
     * as well as the PoRoleCompetenceGroup objects have to be valid at the
     * given date.
     */
    public List<PoGroup> findCompetenceGroupsOfPerson(PoPerson person, PoRole role, Date date);
	
    /**
     * @return a list of <code>PoPerson</code> objects, which are under control of the 
     * given <code>person</code> which is assigned to the given <code>role</code>. The <code>PoRoleHolderPerson </code>objects 
     * as well as the <code>PoRoleCompetencePerson </code>objects have to be valid at the 
     * given <code>date</code>. 
     */
    public List<PoPerson> findCompetencePersonsOfPerson(PoPerson person, PoRole role, Date date);
    
    /**
     * @return a list of PoClient objects which are under control of the given group
     * which is assigned to the given role.
     */
    public List<PoClient> findCompetenceClientsOfGroup(PoGroup group,PoRole role,Date date);
    
    /**
     * @return a list of PoPerson objects which are under control of the given group
     * which is assigned to the given role. The PoRoleHolderGroup objets, as well as 
     * the PoRoleCompetencePerson objects have to be valid at the given date.
     */
    public List<PoPerson> findCompetencePersonsOfGroup(PoGroup group,PoRole role,Date date);
    
	/**
	 * In order to be contained in the list, the assigned <code>PoRoleCompetencePerson</code> object should be linked
	 * with the specified <code>PoRole</code>object. Additionally it should supply the user with rights 
	 * for the given <code>PoPerson</code>object (as the competenceTarget). 
	 * As always, all links have to be valid.
	 */
	public List<PoRoleHolderGroup> findDistinctRoleHolderGroupsWithCompetence4Person(PoRole role, PoPerson controlledPerson, Date date);
	
    /**
     * @return a list of PoGroup objects which are under control of the given group
     * which is assigned to the given role. The PoRoleHolderGroup objets, as well as 
     * the PoRoleCompetencePerson objects have to be valid at the given date.
     */
    public List<PoGroup> findCompetenceGroupsOfGroup(PoGroup group, PoRole role, Date date);
    
    /**
     * The returned roles are roles associated either directly to the person,
     * or roles associated to the groups the person is in.
     * Consider that all even dummy roles are returned. TODO: what does that mean?
     * When needing roles that are associated to just the person (without groups),
     * use <code>findRoles(person)</code>.
     * @param person the person to find roles for.
     */
    public List<PoRole> findRolesOfPerson(PoPerson person);

    /**
     * The returned roles are roles associated either directly to the person,
     * or roles associated to the groups the person is in.
     * Consider that all even dummy roles are returned. TODO: what does that mean? 
     * When needing roles that are associated to just the person (without groups),
     * use <code>findRoles(person)</code>.
     * @param person the person to find roles for.
     * @param date the <code>date</code>on which the assignments have to valid.   
     * 		If <code>null</code> is passed, the current date is used.
     */
    public List<PoRole> findRolesOfPerson(PoPerson person, Date date);
    
    /**
     * @return a list of role holders (PoRoleCompetenceBase objects) , 
     * 		with its corresponding role equal to the given role object.
     */
    public List<PoRoleCompetenceBase> findRoleCompetence(PoRole role);
    
	/**
	 * F = Future.
	 * @return a list of <code>PoRoleHolderPerson<code> objects, that are or will become 
	 * valid at or after the given <code>date</code>. Expired <code>PoRoleHolderPerson</code> objects
	 * that have been valid in the past are not included.
	 */
	public List<PoRoleHolderPerson> findRoleHolderPersonF(PoPerson person, Date date);
	
	/**
	 * TODO 
	 */
	public List<PoRoleHolderPerson> findRoleHolderPersonAll(PoPerson person);
	
	/**
	 * @return a List of PoRoleHolderPerson objects that are valid at the given date. 
	 */
	public List<PoRoleHolderPerson> findRoleHolderPerson(PoRole role, Date date);
	
	/**
     * @return a List of PoRoleHolderPerson objects, that are or become 
     * valid from the given date. (past is not included)
     */
	public List<PoRoleHolderPerson> findRoleHolderPersonF(PoRole role, Date date);
	
	/**
	 * @return a list of <code>PoRoleHolderGroup</code> objects, that are or will become 
	 * valid regarding the the given <code>date</code>. Expired entries are not included.
	 */
	public List<PoRoleHolderGroup> findRoleHolderGroupF(PoPerson person, Date date);
	
	/**
	 * TODO
	 */
	public List<PoRoleHolderGroup> findRoleHolderGroupAll(PoPerson person);
	
	/**
	 * @return a list of <code>PoRoleHolderGroup</code> objects, with the given role assigned.
	 */
	public List<PoRoleHolderGroup> findRoleHolderGroupF(PoRole role, Date date);
    
    /**
     * @return a list of PoRoleHolderGroup objects, that are or become 
     * valid from the given date. (past is not included)
     * and the given Group is a actual member of the referenced
     * roles. 
     */
    public List<PoRoleHolderGroup> findRoleHolderGroupF(PoGroup group, Date date);
    
    /**
     * Retrieves all role holders of group
     */
	public List<PoRoleHolderGroup> findRoleHolderGroupAll(PoGroup group);
    
    /**
     * @return a list of PoRoleCompetenceClient objects which are assigned to the given role
     * meaning all role assignments, where clients are the competence target!
     */
    public List<PoRoleCompetenceClient> findRoleCompetenceClient(PoRole role);
    
    /**
     * @return a list of PoRoleCompetenceGroup objects which are assigned to the given role
     * meaning all role assignments, where groups are the competence target!
     */
    public List<PoRoleCompetenceGroup> findRoleCompetenceGroup(PoRole role);
    
    /**
     * @return a list of PoRoleCompetencePerson objects which are assigned to the given role
     * meaning all role assignments, where persons are the direct competence targets
     */
    public List<PoRoleCompetencePerson> findRoleCompetencePerson(PoRole role);
    
    /**
     * Deletes the given role Holder. Internally only validto is set to now.
     * @param roleholder PoRoleCompetenceBase Object (can be PoRoleholder, PoRoleholderCompetenceGroup or PoRoleholderCompetencePerson)
     */
    public void deleteRoleCompetence(PoRoleCompetenceBase roleholder);
    
    /**
     * delets the roleholderperson link from the database
     * if the following conditions are met:
     * th roleholderperson links validity starts in the future
     * otherwise throws an exception
     */
    public void deleteAndFlushRoleHolderPersonLink(PoRoleHolderPerson rhpersonlink);
    
    /**
     * Tries to delete the roleHolderGroup link. This is only possible if 
     * the rhGroupLink.validFrom field is after the current date. Otherwise
     * a PoRuntimeException is thrown.
     */
    public void deleteAndFlushRoleHolderGroupLink(PoRoleHolderGroup rhGroupLink);
	
    /**
     * Removes the assignment of the role to the group. In other words (
     * which is more correctly) the roleHolderGroup.validTo field is set
     * to the current date.
     */
    public void removeGroupFromRole(PoRoleHolderGroup roleHolderGroup);
    
    /**
     * changes validfrom and validto dates of a roleholderperson link
	 * Change of validfrom only possible, if link's current validity starts 
	 * in future and new value for validfrom also is in future.
	 * Change of validto only possible, if link is actual or starts in future AND
	 * validto is in future and new value of validto is in future.
	 * if any of those restrictions is not met -> throw an exception
     * @param validfrom can be null.
     * @param validto can be null. 
     */
    public void changeValidityRHPersonLink(PoRoleHolderPerson rhpersonlink, Date validfrom, Date validto);
    
    /**
     * changes the validity of the given rhgrouplink. (PoRoleHolderGroup object) 
     */
    public void changeValidityRHGroupLink(PoRoleHolderGroup rhgrouplink, Date validfrom, Date validto);
    
    
    /**
     * @param controlledPerson a <code>PoPerson</code> object.
     * @param role <code>PoRole</code> object
     * @return a <code>List</code> of <code>PoPerson</code>'s, which are 
     * officeholders for the <code>PoRole</code>. The assignment has to be valid 
     * at the current date. All <code>PoPerson</code> objects are returned. 
     * Use {@link findAuthority(PoPerson, PoRole, Date, int)} if you need more
     * constraints. 
     */
    public List<PoPerson> findAuthority(PoPerson controlledPerson, PoRole role);
    
    /**
     * @param group controlled group
     * @param date the date up to which the authority was valid.
     * @return a <code>List</code> of all <code>PoPerson</code>s which are 
     * 		officeholders of the given <code>PoRole</code>. 
     */
    public List<PoPerson> findAuthority(PoGroup group, PoRole role);

    /**
     * @param client controlled group
     * @param date the date up to which the authority was valid.
     * @return a <code>List</code> of all <code>PoPerson</code>s which are 
     * 		officeholders of the given <code>PoRole</code>. 
     */
    public List<PoPerson> findAuthority(PoClient client, PoRole role);
    
    /**
     * @param client the subject to find authorities for.
     * @param role the role the authorities should be in.
     * @param date the validity date for the query.
     * @param minAmount the minimum amount of returned entries in the returned <code>List</code>.
     * @return a <code>List</code> of <code>PoPerson</code>'s, which are 
     * 		officeholders of the <code>PoRole</code>. The assignment has to be valid 
     * 		at the current date. If <code>minAmount</code> is <code>-1</code> all 
     * 		existing found authority <code>PoPerson</code>s are returned, otherwise
     * 		at most <code>minAmount</code>.
     */
    public List<PoPerson> findAuthority(PoClient client, PoRole role, Date date, int minAmount);
    
    /**
     * @param group the subject to find authorities for.
     * @param role the role the authorities should be in.
     * @param date the validity date for the query.
     * @param minAmount the minimum amount of returned entries in the returned <code>List</code>.
     * @return a <code>List</code> of <code>PoPerson</code>'s, which are 
     * 		officeholders of the <code>PoRole</code>. The assignment has to be valid 
     * 		at the current date. If <code>minAmount</code> is <code>-1</code> all 
     * 		existing found authority <code>PoPerson</code>s are returned, otherwise
     * 		at most <code>minAmount</code>.
     */
    public List<PoPerson> findAuthority(PoGroup group, PoRole role, Date date, int minAmount);
    
    /**
     * @param controlledPerson the subject to find authorities for.
     * @param role the role authorities should be in.
     * @param date the validity date for the query.
     * @param minAmount the minimum amount of entries in the returned <code>List</code>.
     * @return a list of Persons, who belong to the given role and have competence for the given
     * 		PoPerson object, obeying that the role holder has to be valid at the passed date.
     */
    public List<PoPerson> findAuthority(PoPerson controlledPerson, PoRole role, Date date, int minAmount);

    /**
     * @return a list of PoRoleHolderPerson's, which belong to the given role and have competence for the given Client.
     * The PoRoleHolderPerson object has to be valid at the given date (referenceDate). 
     */
    public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4Client(PoRole role, PoClient client, Date referenceDate);
    
    /**
     * @return a list of PoRoleHolderPerson's, which belong to the given role and have competence for the given Group.
     * The PoRoleHolderPerson object has to be valid at the given date (referenceDate). 
     */
    public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4Group(PoRole role, PoGroup controlledGroup, Date referenceDate);
    
    /**
     * @return a list of PoRoleHolderLink objects which have competences 
     * for the given person. The PoRoleHolderLink objects have to be valid
     * at the given date.
     */
    public List<PoRoleHolderLink> findRoleHolderWithCompetenceForPerson(PoPerson controlledPerson, Date date);
    
    /**
     * There are several ways how a group or person can have rights over another person. 
     * The most trivial way is a person, group with a role assigned but no competences.
     * Then it has rights over all. The next level are the directly connected links, then a 
     * 
     * @return a list of PoRoleCompetenceAll objects with have comptences 
     * for the given group. The PoRoleCompetenceAll objects have to be valid
     * at the given date. 
     */
    public List<PoRoleHolderLink> findRoleHolderWithCompetenceForGroup(PoGroup controlledGroup, Date date);
	
    /**
     * There are several ways how a group or person can have rights over client. 
     * The most trivial way is a person, group with a role assigned but no competences.
     * Then it has rights over all. The next level are the directly connected links, then a 
     * 
     * @return a list of PoRoleCompetenceAll objects which have competences 
     * for the given client. The PoRoleCompetenceAll objects have to be valid
     * at the given date. 
     */
    public List<PoRoleHolderLink> findRoleHolderWithCompetenceForClient(PoClient client, Date date);
    
    /**
     * @param role the role object the person is linked to 
     * @param person the person that controls
     * @param referenceDate at which the PoRoleHolderPerson object has to be valid
     * @return a PoRoleHolderPerson object, if the given person 
     * 		has competences over at least one person. The PoRoleHolderPerson link has to be 
     * 		valid at the given date. If no link exists, null is returned.
     */
    public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4Person(PoRole role,PoPerson person,  Date date);
    
    /**
	 * This method is used by custom Java Jobs for the customer VKB!
     * @param role the role object the person is linked to 
     * @param person the person that controls
     * @param controlledPerson the person that is controlled
     * @param referenceDate at which the PoRoleHolderPerson object has to be valid
     * @deputy set to <code>true</code> if the returned element should have assigned a 
     * <code>PoRoleDeputy</code> object, <code>false</code> otherwise. Most likely will be false.
     * @return a PoRoleHolderPerson object, if the given person 
     * 		has competences over the given controlledPerson. The PoRoleHolderPerson link has to be 
     * 		valid at the given date. If no link exists, null is returned.
     */
    public PoRoleHolderPerson findRoleHolderPersonWithCompetence4Person(PoRole role, PoPerson person, 
    		PoPerson controlledPerson, Date referenceDate, boolean deputy);
    
    
    /**
     * @return a list (probably only one element, but not imperative.)
     * 		of PoRoleHolderPerson objects, with the given role and person assigned.
     * 		The objects have to be valid now or in the future.
     */
    public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4AllF(PoRole role, PoPerson person,  Date referenceDate);
    
    /**
     * @return a list (probably only one element, but not imperative.)
     * 		of PoRoleHolderGroup objects, with the given role and group assigned.
     * 		The objects have to be valid now or in the future.
     */
    public List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4AllF(PoRole role, PoGroup group,  Date referenceDate);
    
    /**
	 * This method is used by custom Java Jobs for the customer VKB!
	 * 
	 * Use this function to get the <code>PoRoleHolderGroup</code> object with the given 
	 * <code>role</code> and <code>group</code> assigned and where the <code>group</code> is 
	 * in charge of the given <code>controlledPerson</code>.
	 */
	public PoRoleHolderGroup findRoleHolderGroupWithCompetence4Person(PoRole role, PoGroup group, PoPerson controlledPerson, Date referenceDate);
    
    
    /**
     * TODO all methods of an interface have to be documented.
     */
	public List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4Group(PoRole role, PoGroup group, Date referenceDate);
	
	/**
	 * finds corresponding PoRoleHolderPerson Link for the given Person and RoleHolder
	 * object and sets validto to now.
	 * if no object is found -> exception is thrown
	 */
	public void removePersonFromRole(PoRoleHolderPerson roleHolderPerson);
	
    /**
     * finds the Performer (Roleholder) of a specific Dummyrole
     * a Dummyrole has only ONE performer. this can be a group or person.
     * returnes a PoGroup or PoPerson or a PoRoleHolderDynamic object 
     * 
     * @return Object of Type PoGroup or PoPerson
     */
    public Object findPerformerOfDummyRole(PoRole role, Date effectiveDate);
    
    /**
     * @return a List of Role Holder persons that are valid at the passed date
     */
    public List<PoRoleCompetenceAll> findRoleCompetenceAll(PoRole role, Date date);
    
    /**
     * checks whether the given Person has the given Role assigned at the specified date
     */
    public boolean hasPersonRoleAssigned(PoPerson p, PoRole r, Date date);
    
    /**
     * checks whether the given Person has the given Role assigned at the specified date
     * with Competence for All
     */   
    public boolean hasPersonRoleAssignedWithCompetence4All(PoPerson p, PoRole r, Date date);

    /**
     * TODO all methods of an interface have to be documented.
     */
    public List<PoRoleHolderPerson> findRoleHolderPersonsWithRoleF(PoRole role, Date date);
    
	/**
	 * @param date the date on which the roles have to be valid. 
	 * @return a <code>List</code> of <code>PoRole</code> objects. These <code>PoRole</code> objects have 
	 * the given client or no client assigned.
	 */
    public List<PoRole> findRolesForClient(PoClient client, Date date);
    
    /**
     * Use this function to determine valid deputies of a person.
     * All deputies that are or will become valid are returned.
     * 
     * @param person the office holder 
     */
    public List<PoRoleDeputy> findRoleDeputiesOfPerson(PoPerson person);
    
	/**
	 * This function defines a <code>deputy</code> for a timerange 
	 * [<code>validFrom</code> - <code>validTo</code>]. To accomplish this 
	 * goal all necessary objects are copied 
	 * 
	 * @param officeHolder <code>PoPerson</code> which is the <code>officeHolder</code> of the <code>PoRole</code> 
	 * @param deputy <code>PoPerson</code> which becomes the <code>deputy</code> 
	 * @return the generated <code>PoRoleDeputy</code>. 
	 */
	public PoRoleDeputy generateDeputy(PoRole role, PoPerson officeHolder, PoPerson deputy, Date validFrom, Date validTo);
	
	/**
	 * This function updates the <code>PoRoleCompetenceBase</code> object
	 * assigned to the <code>roleDeputy</code>
	 */
	public void updateDeputy(PoRoleDeputy roleDeputy);
	
	/**
	 * If the <code>UID</code> of the <code>PoRoleDeputy</code>
	 * is <code>null</code> the <code>roleDeputy</code> will 
	 * is generated. See {@link generateDeputy(PoRole, PoPerson, PoPerson, Date, Date)}
	 * If it does already exist, {@link updateDeputy(PoRoleDeputy)} is called.
	 * @return the generated <code>PoRoleDeputy</code>
	 */
	public PoRoleDeputy saveDeputy(PoRoleDeputy roleDeputy);
	
	/**
	 * This function invalidates the <code>roleDeputy</code> and 
	 * its <code>roleHolderPerson</code> objects. The <code>PoRoleCompetenceBase</code> 
	 * objects stay the same. 
	 */
	public void deleteDeputy(PoRoleDeputy roleDeputy);
	
	/**
	 * <p>This function returns a <code>List</code> of <code>PoRole</code>
	 * objects that are directly linked to the given <code>PoPerson</code>.
	 * </p><p>
	 * The assignment has to be valid now.</p>
	 */
	public List<PoRole> findDirectlyLinkedRolesOfPerson(PoPerson person);
	
	/**
	 * <p>This function returns a <code>List</code> of <code>PoRole</code>
	 * objects that are directly linked to the given <code>PoPerson</code>.
	 * </p><p>
	 * The assignment has to be valid at the given <code>date</code>.</p>
	 */
	public List<PoRole> findDirectlyLinkedRolesOfPerson(PoPerson person, Date date);
	
	
	/**
	 * Use this function to get a <code>List</code> of all <code>PoRoleHolderPerson</code>'s,
	 * with a <code>PoRoleCompetencePerson</code> assigned with competence for the 
	 * <code>target</code> object. The <code>PoRoleHolderPerson</code>'s have to be valid 
	 * at the given <code>date</code> or become valid afterwards.
	 */
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4PersonF(PoRole role, PoPerson officeHolder, PoPerson target, Date date);

	/**
	 * Use this function to get a <code>List</code> of all <code>PoRoleHolderPerson</code>'s,
	 * with a <code>PoRoleCompetenceGroup</code> assigned with competence for the 
	 * <code>target</code> object. The <code>PoRoleHolderPerson</code>'s have to be valid 
	 * at the given <code>date</code> or become valid afterwards.
	 */
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4GroupF(PoRole role, PoPerson officeHolder, PoGroup target, Date date);

	/**
	 * Use this function to get a <code>List</code> of all <code>PoRoleHolderPerson</code>'s,
	 * with a <code>PoRoleCompetenceClient</code> assigned with competence for the 
	 * <code>target</code> object. The <code>PoRoleHolderPerson</code>'s have to be valid 
	 * at the given <code>date</code> or become valid afterwards.
	 */
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4ClientF(PoRole role, PoPerson officeHolder, PoClient target, Date date);
	
	/**
	 * Use this function to get a <code>List</code> of all <code>PoRoleHolderGroup</code>'s,
	 * with a <code>PoRoleCompetenceClient</code> assigned with competence for the 
	 * <code>target</code> object. The <code>PoRoleHolderGroup</code>'s have to be valid 
	 * at the given <code>date</code> or become valid afterwards.
	 */
	public List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4ClientF(PoRole role, PoGroup officeHolder, PoClient target, Date date);
	
	/**
	 * Use this function to get a <code>List</code> of all <code>PoRoleHolderGroup</code>'s,
	 * with a <code>PoRoleCompetenceGroup</code> assigned with competence for the 
	 * <code>target</code> object. The <code>PoRoleHolderGroup</code>'s have to be valid 
	 * at the given <code>date</code> or become valid afterwards.
	 */
	public List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4GroupF(PoRole role, PoGroup officeHolder, PoGroup target, Date date);
	
	/**
	 * Use this function to get a <code>List</code> of all <code>PoRoleHolderGroup</code>'s,
	 * with a <code>PoRoleCompetencePerson</code> assigned with competence for the 
	 * <code>target</code> object. The <code>PoRoleHolderGroup</code>'s have to be valid 
	 * at the given <code>date</code> or become valid afterwards.
	 */
	public List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4PersonF(PoRole role, PoGroup officeHolder, PoPerson target, Date date);
	
	/**
	 * Persists the given <code>PoRoleHolderPerson</code> object.
	 */
	public void saveRoleHolderPerson(PoRoleHolderPerson rhp);
	
	/**
	 * Persists the given <code>PoRoleHolderGroup</code> object.
	 */
	public void saveRoleHolderGroup(PoRoleHolderGroup rhg);
	
	/**
	 * Saves the given <code>PoRoleHolderDynamic</code> object.
	 */
	public void saveRoleHolderDynamic(PoRoleHolderDynamic rhd);
	
	/**
	 * @return a <code>List</code> of <code>PoRoleHolderPerson</code>,
	 * <code>PoROleHolderGroup</code>, and <code>PoRoleHolderDynamic</code>
	 * objects. All these objects implements the <code>PoRoleHolderLink</code>
	 * Interface which provides a function to get the <code>PoRoleCompetenceBase
	 * </code> object. 
	 */
	public List<PoRoleCompetenceBase> findRoleCompetences(PoRole role, Date date);

	/**
	 * @param role the role to search valid persons for.
	 * @return a list of persons that have given role, considering also the valid groups that have that role.
	 */
	public List<PoPerson> findRoleHolders(PoRole role);

	/**
	 * @param person the person to find roles for.
	 * @return a list of <code>PoRole</code> object that are assigned 
	 * 		to the given <code>person</code> and are valid 
	 * 		at the given <code>date</code>.
	 * 		This does <b>not</b> return roles that are associated to a group the person is in!
	 * 		Use findRolesOfPerson() when needing that.
	 */
	public List<PoRole> findRoles(PoPerson person, Date date);
	
	/**
	 * @return a list of <code>PoRole</code> object that are assigned 
	 * to the given <code>group</code> and are valid 
	 * at the given <code>date</code>.
	 */
	public List<PoRole> findRoles(PoGroup group, Date date);
	
	/**
	 * @return a list of <code>PoRole</code> object that are assigned 
	 * to the given <code>group</code> and are or will become valid 
	 * at the given <code>date</code>.
	 */
	public List<PoRole> findRolesF(PoGroup group, Date date);

}


