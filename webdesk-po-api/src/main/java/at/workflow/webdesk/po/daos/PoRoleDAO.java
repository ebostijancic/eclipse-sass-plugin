package at.workflow.webdesk.po.daos;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;

/**
 * DAO for the objects PoRole, PoRoleHolderPerson, PoRoleholderGroup, PoRoleCompetenceGroup, PoRoleCompetencePerson
 * PoRolholderDynamic, ...
 * 
 * @TODO: refactor into more DAOS
 * 
 * @author DI Harald Entner <br>
 *
 */
public interface PoRoleDAO extends GenericDAO<PoRole>{

	
	/**
	 * @param group
	 * @param date
	 * @return a list of <code>PoRole</code> object that are assigned 
	 * to the given <code>person</code> and are valid 
	 * at the given <code>date</code>.
	 */
	public List<PoRole> findRoles(PoPerson person, Date date);
	

	/**
	 * @param group
	 * @param date
	 * @return a list of <code>PoRole</code> object that are assigned 
	 * to the given <code>group</code> and are valid 
	 * at the given <code>date</code>.
	 */
	public List<PoRole> findRoles(PoGroup group, Date date);
	
	/**
	 * @param group
	 * @param date
	 * @return a list of <code>PoRole</code> object that are assigned 
	 * to the given <code>group</code> and are or will become valid 
	 * at the given <code>date</code>.
	 */
	public List<PoRole> findRolesF(PoGroup group, Date date);

	/**
	 * @param referenceDate
	 * @return a list of <code>PoRole</code> objects that are valid at the <code>referenceDate</code>
	 */
	public List<PoRole> loadAllRoles(Date referenceDate);

	/**
	 * @param client
	 * @param referenceDate
	 * @return a list of <code>PoRole</code> objects that have the given
	 * <code>client</code> assigned and that are valid at the given
	 * <code>referenceDate</code>.
	 */
	public List<PoRole> loadAllRoles(PoClient client, Date referenceDate);


	/**
	 * @param name the name of the <code>PoRole</code>'s.
	 * @param referenceDate the <code>Date</code> at which the <code>PoRole</code>'s have to be valid.
	 * @return a list of <code>PoRole</code> objects.
	 */
	public List<PoRole> findRoleByName(String name, Date referenceDate);

	/**
	 * @param name the name of the <code>PoRole</code>
	 * @param client the <code>PoClient</code> of the <code>PoRole</code>
	 * @param referenceDate
	 * @return a <code>PoRole</code>
	 */
	public PoRole findRoleByName(String name, PoClient client, Date referenceDate);

	

	/**
	 * @param role
	 * @param controlledGroup
	 * @param date
	 * @return a list of persons with competences over the given Group only a
	 *         direct connection is covered, in other words it is not searched
	 *         for dependencies in parent groups. Use findAuthority(...)
	 *         instead.
	 */
	public List<PoPerson> findPersonsWithCompetence4Group(PoRole role,
			PoGroup controlledGroup, Date date);

	public List<PoPerson> findPersonsWithCompetence4Person(PoRole role,
			PoPerson controlledPerson, Date date);

	public List<PoPerson> findPersonsWithCompetence4Client(PoRole role,
			PoClient client, Date date);
	
	/**
	 * Use this function to get a list of <code>PoPerson</code> objects, that
	 * have the given <code>PoRole</code> object via a <code>PoRoleCompetenceAll</code>
	 * object assigned. The assignment(as always) has to be valid at the given <code>date</code>.
	 * 
	 * @param role
	 * @param date
	 * @return a list of <code>PoPerson</code> objects.
	 */
	public List<PoPerson> findPersonsWithRoleAndCompetence4All(PoRole role, Date date);

	/**
	 * Use this function to get a list of <code>PoGroup</code> objects, that
	 * have the given <code>PoRole</code> object via a <code>PoRoleCompetenceAll</code>
	 * object assigned. The assignment(as always) has to be valid at the given <code>date</code>.
	 * 
	 */
	public List<PoGroup> findGroupsWithRoleAndCompetence4All(PoRole role, Date date);
		
	public List<PoGroup> findGroupsWithCompetence4Group(PoRole role,
			PoGroup controlledGroup, Date date);

	public List<PoGroup> findGroupsWithCompetence4Client(PoRole role, PoClient client, Date date);
	

	/**
	 * @param person
	 * @param date
	 * @return a list of PoRole objects that are assigned to this person and are
	 *         valid at the given date. It is generally assumed that a role can
	 *         be only directly connected to the person or directly connected to
	 *         a group. For the latter case the person has to be directly
	 *         connected to the group in order to occupy the role.
	 */
	public List<PoRole> findRolesOfPerson(PoPerson person, Date date);

	public List<PoPerson> findCompetencePersonsOfPerson(PoPerson person, PoRole role,
			Date date);

	public List<PoGroup> findCompetenceGroupsOfPerson(PoPerson person, PoRole role,
			Date date);

	public List<PoGroup> findCompetenceGroupsOfGroup(PoGroup group, PoRole role,
			Date date);

	public List<PoPerson> findCompetencePersonsOfGroup(PoGroup group, PoRole role,
			Date date);

	public List<PoClient> findCompetenceClientsOfPerson(PoPerson person, PoRole role, Date date);
	
	public List<PoClient> findCompetenceClientsOfGroup(PoGroup group, PoRole role, Date date);
	

	public PoRole findRoleByParticipantId(String key, Date effectiveDate);

	public PoRole findRoleByParticipantId(String key, PoClient client, Date effectiveDate);

	public Object findPerformerOfDummyRole(PoRole role, Date date);

	
	/**
	 * Find all Roles to be used in a workflow process for a client, 
	 * meaning that this returns a List of all valid PoRole objects
	 * within this client. This includes Roles which are general (not connected to any client) and
	 * local Roles (connected to the specific Client) but EXCLUDES roles which are connected
	 * to other clients!
	 * 
	 * @param client a <code>PoClient</code> object
	 * @param date the date on which the roles have to be valid. 
	 * @return a <code>List</code> of valid <code>PoRole</code> objects to be used in a workflow
	 * for users of this client
	 */
	public List<PoRole> findRolesForClient(PoClient client, Date date);

	/**
	 * <p>This function returns a <code>List</code> of <code>PoRole</code>
	 * objects that are directly linked to the given <code>PoPerson</code>.
	 * </p> 
	 * <p>The assignment has to be valid at the given <code>date</code>.</p>
	 * 
	 * @param person
	 * @param date 
	 * @return a <code>List</code> of <code>PoRole</code> objects.
	 */
	public List<PoRole> findDirectlyLinkedRolesOfPerson(PoPerson person, Date date);


	/**
	 * @return a <code>List</code> of all active <code>PoRole</code> objects.
	 */
	public List<PoRole> findAllActiveRoles();


	/** @return the role with given name and no client (null). */
	public PoRole findRoleByNameAndNullClient(String roleName);
	
}
