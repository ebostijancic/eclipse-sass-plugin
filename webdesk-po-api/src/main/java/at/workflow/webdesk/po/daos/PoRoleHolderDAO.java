package at.workflow.webdesk.po.daos;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleCompetenceBase;
import at.workflow.webdesk.po.model.PoRoleHolderLink;

public interface PoRoleHolderDAO {

	public boolean hasPersonRoleAssigned(PoPerson p, PoRole r, Date date);

	public boolean hasPersonRoleAssignedWithCompetence4All(PoPerson p,
			PoRole r, Date date);

	
	/**
	 * This function returns a list of PoRoleHolderLink Objects
	 * (either PoRoleHolderPerson or PoRoleHolderGroup Objects)
	 * which have competence over the passed group at the supplied 
	 * date
	 * 
	 * @param controlledGroup
	 * @param date
	 * @return a list of <code>PoRoleHolderLink</code> objects.
	 */
	public List<PoRoleHolderLink> findRoleHolderWithCompetence4Group(PoGroup controlledGroup, Date date);
	
	/**
	 * This function returns a list of PoRoleHolderLink Objects
	 * (either PoRoleHolderPerson or PoRoleHolderGroup Objects)
	 * which have competence over the passed client at the supplied 
	 * date
	 * 
	 * @param client
	 * @param date
	 * @return a list of <code>PoRoleHolderLink</code> objects.
	 */
	public List<PoRoleHolderLink> findRoleHolderWithCompetence4Client(PoClient client, Date date);
	
	/**
	 * This function returns a List of <code>PoRoleHolderLink</code> Objects, 
	 * (<code>PoRoleHolderPerson</code> or <code>PoRoleHolderGroup</code>)
	 * for the supplied <code>PoRole</code> object, which have competence 
	 * over the given <code>PoGroup</code>.
	 * 
	 * @param role: the Role for which RoleHolders are beeing searched for
	 * @param controlledGroup: the Group over which the Roleholders have competence
	 * @param date: referenceDate
	 * @return: List of <code>PoRoleHolderLink</code> Objects
	 */
	public List<PoRoleHolderLink> findRoleHolderWithCompetence4Group(PoRole role, PoGroup controlledGroup, Date date);

	
	/**
	 * This function returns a list of <code>PoRoleHolderPerson</code> and 
	 * 	<code>PoRoleHolderGroup</code> objects, that are assigned to a 
	 * <code>PoRoleComptence</code> object with rights for the
	 * given <code>controlledPerson</code>.
	 * 
	 * 
	 * @param controlledPerson
	 * @param date
	 * @return a list of <code>PoRoleHolderPerson</code> and 
	 * 	<code>PoRoleHolderGroup</code> objects.
	 */
	public List<PoRoleHolderLink> findRoleHolderWithCompetence4Person(PoPerson controlledPerson, Date date);
	
	
	/**
	 * This function returns a List of <code>PoRoleHolderLink</code> Objects, 
	 * (<code>PoRoleHolderPerson</code> or <code>PoRoleHolderGroup</code>)
	 * for the supplied <code>PoRole</code> object, which have competence 
	 * over the given <code>controlledPerson</code>.
	 * 
	 * @param role: the Role for which RoleHolders are beeing searched for
	 * @param controlledPerson: the Person over which the Roleholders have competence
	 * @param date: referenceDate
	 * @return: List of <code>PoRoleHolderLink</code> Objects
	 */
	public List<PoRoleHolderLink> findRoleHolderWithCompetence4Person(PoRole role, PoPerson controlledPerson, Date date);
	
	/**
	 * This function returns a list of <code>PoRoleHolderPerson</code> and 
	 * 	<code>PoRoleHolderGroup</code> objects, that are assigned to a 
	 * <code>PoRoleComptence</code> object with rights for all.
	 * 
	 * 
	 * @param client
	 * @param date
	 * @return a list of <code>PoRoleHolderPerson</code> and 
	 * 	<code>PoRoleHolderGroup</code> objects.
	 */
	public List<PoRoleHolderLink> findRoleHolderWithCompetence4All(PoClient client, Date date);
	
	/**
	 * @param role
	 * @param date
	 * @return a <code>List</code> of <code>PoRoleHolderPerson</code>,
	 * <code>PoROleHolderGroup</code>, and <code>PoRoleHolderDynamic</code>
	 * objects. All these objects implements the <code>PoRoleHolderLink</code>
	 * Interface which provides a function to get the <code>PoRoleCompetenceBase
	 * </code> object. 
	 * 
	 */
	public List<PoRoleCompetenceBase> findRoleHolders(PoRole role, Date date);
	
}
