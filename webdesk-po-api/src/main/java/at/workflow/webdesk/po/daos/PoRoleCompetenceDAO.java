package at.workflow.webdesk.po.daos;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleCompetenceAll;
import at.workflow.webdesk.po.model.PoRoleCompetenceBase;
import at.workflow.webdesk.po.model.PoRoleCompetenceClient;
import at.workflow.webdesk.po.model.PoRoleCompetenceGroup;
import at.workflow.webdesk.po.model.PoRoleCompetencePerson;

public interface PoRoleCompetenceDAO extends GenericDAO<PoRoleCompetenceBase> {
	
	/**
	 * @param uid
	 * @return a <code>PoRoleCompetenceAll</code> object.
	 */
	public PoRoleCompetenceAll getRoleCompetenceAll(String uid);

	/**
	 * @param role a <code>PoRole</code> 
	 * @return a <code>List</code> of <code>PoRoleCompetenceBase</code> objects.
	 */
	public List<PoRoleCompetenceBase> findRoleCompetence(PoRole role);

	/**
	 * @param role a <code>PoRole</code>.
	 * @param date the <code>date</code> at which the <code>PoRoleCompetenceBase</code> objects have to be valid or become valid after that  
	 * @return a <code>List</code> of <code>PoRoleCompetenceBase</code> objects.
	 */
	public List<PoRoleCompetenceBase> findRoleCompetenceF(PoRole role, Date date);

	/**
	 * @param role
	 * @param referenceDate
	 * @return a <code>List</code> of <code>PoRoleCompetenceAll</code> objects. 
	 */
	public List<PoRoleCompetenceAll> findRoleCompetenceAll(PoRole role, Date referenceDate);

	/**
	 * @param role
	 * @param validFrom
	 * @return a <code>List</code> of <code>PoRoleCompetenceAll</code> objects
	 * that are valid now or will become valid in the future.
	 */
	public List<PoRoleCompetenceAll> findRoleCompetenceAllF(PoRole role, Date validFrom);
	
	/**
	 * This function can be used to find <code>PoPerson</code> objects with competence over the given <code>PoPerson</code>. 
	 * It does not return <code>PoPerson</code> objects itself, but <code>PoRoleCompetencePerson</code> objects. Even it's 
	 * very easy to get the <code>PoPerson</code>'s from the returned <code>List</code>. (a method exist in the model class
	 * for this) 
	 * 
	 * 
	 * @param role the returned <code>PoRoleCompetencePerson</code> has to be linked with the given <code>role</code>.
	 * @param controlledPerson the <code>PoRoleCompetencePerson</code> has to define the competence over the <code>controlledPerson</code>
	 * @param referenceDate all links have to be valid at the given <code>date</code> or afterwards.
	 * @return a list of <code>PoRoleCompetencePerson</code>. 
	 */
	public List<PoRoleCompetencePerson> findRoleCompetencePersonWithCompetence4PersonF(PoRole role,
			PoPerson controlledPerson, Date referenceDate);

	/**
	 * @param role
	 * @param controlledGroup
	 * @param validFrom
	 * @return a <code>List</code> of <code>PoRoleCompetenceGroup</code> objects.
	 * If everything works fine, only one entry should be returned for the 
	 * given <code>PoGroup</code> and <code>PoRole</code>
	 * 
	 */
	public List<PoRoleCompetenceGroup> findRoleCompetenceGroupWithCompetence4GroupF(PoRole role, 
			PoGroup controlledGroup, Date validFrom);
	
	public List<PoRoleCompetenceClient> findRoleCompetenceClient(PoRole role);

	public List<PoRoleCompetenceGroup> findRoleCompetenceGroup(PoRole role);
	
	public List<PoRoleCompetencePerson> findRoleCompetencePerson(PoRole role);

	public List<PoRoleCompetenceClient> findRoleCompetenceClientWithCompetence4ClientF(PoRole role,
			PoClient client, Date referenceDate);

}
