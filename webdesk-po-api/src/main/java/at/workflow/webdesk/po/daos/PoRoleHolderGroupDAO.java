package at.workflow.webdesk.po.daos;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleHolderGroup;

public interface PoRoleHolderGroupDAO extends GenericDAO<PoRoleHolderGroup> {
	
	List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4Person(PoRole role, PoPerson controlledPerson, Date date);

	List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4AllF(PoRole role, PoGroup group, Date date);

	PoRoleHolderGroup findRoleHolderGroupWithCompetence4Person(PoRole role, PoGroup group, PoPerson controlledPerson, Date date);
	
	/**
	 * @return a list of <code>PoRoleHolderGroup</code> objects, which have rights for 
	 * the given <code>group</code>.
	 */
	List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4Group(PoRole role, PoGroup group, Date date);
	
	/**
	 * Returns a list of <code>PoRoleHolderGroup</code> objects.<br>
	 * <br>
	 * In order to be contained in the list, the assigned <code>PoRoleCompetencePerson</code> object should be linked
	 * with the specified <code>PoRole</code>object. Additionally it should supply the user with rights 
	 * for the given <code>PoPerson</code>object (as the competenceTarget). 
	 * <br/><br/>
	 * As always, all links have to be valid.
	 */
	List<PoRoleHolderGroup> findDistinctRoleHolderGroupsWithCompetence4Person(PoRole role, PoPerson controlledPerson, Date date);
	
	/**
	 * Returns a list of <code>PoRoleHolderGroup</code> objects.<br>
	 * <br>
	 * In order to be contained in the list, the assigned <code>PoRoleCompetenceGroup</code> object should be linked
	 * with the specified <code>PoRole</code>object. Additionally it should supply the user with rights 
	 * for the given <code>PoGroup</code>object (as the competenceTarget). 
	 * <br/><br/>
	 * As always, all links have to be valid.
	 */
	List<PoRoleHolderGroup> findDistinctRoleHolderGroupsWithCompetence4Group(PoRole role, PoGroup group, Date date);

	/**
	 * Use this function to get a <code>List</code> of all <code>PoRoleHolderGroup</code>'s,
	 * with a <code>PoRoleCompetenceGroup</code> assigned with competence for the 
	 * <code>target</code> object. The <code>PoRoleHolderGroup</code>'s have to be valid 
	 * at the given <code>date</code> or become valid afterwards.
	 */
	List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4GroupF(PoRole role, PoGroup officeHolder, PoGroup target, Date date);
	

	/**
	 * Use this function to get a <code>List</code> of all <code>PoRoleHolderGroup</code>'s,
	 * with a <code>PoRoleCompetenceClient</code> assigned with competence for the 
	 * <code>target</code> object. The <code>PoRoleHolderGroup</code>'s have to be valid 
	 * at the given <code>date</code> or become valid afterwards.
	 */
	List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4ClientF(PoRole role, PoGroup officeHolder, PoClient target, Date date);
	
	
	/**
	 * Use this function to get a <code>List</code> of all <code>PoRoleHolderGroup</code>'s,
	 * with a <code>PoRoleCompetencePerson</code> assigned with competence for the 
	 * <code>target</code> object. The <code>PoRoleHolderGroup</code>'s have to be valid 
	 * at the given <code>date</code> or become valid afterwards.
	 */
	List<PoRoleHolderGroup> findRoleHolderGroupWithCompetence4PersonF(PoRole role, PoGroup officeHolder, PoPerson target, Date date);
	
	List<PoRoleHolderGroup> findRoleHolderGroupF(PoPerson person, Date date);

	List<PoRoleHolderGroup> findRoleHolderGroupAll(PoPerson person);
	
	//List<PoRoleHolderGroup> findRoleHolderGroup(PoRole role, Date validityDate);

	List<PoRoleHolderGroup> findRoleHolderGroupF(PoRole role, Date date);

	List<PoRoleHolderGroup> findRoleHolderGroupF(PoGroup group, Date date);
	
	/**
	 * Retrieves all role holders of group
	 */
	List<PoRoleHolderGroup> findRoleHolderGroupAll(PoGroup group);

	List<PoRoleHolderGroup> findRoleHolderGroup(PoRole role, Date validityDate);

}
