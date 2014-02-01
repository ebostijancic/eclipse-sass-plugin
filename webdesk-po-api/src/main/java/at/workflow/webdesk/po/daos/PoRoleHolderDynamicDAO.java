package at.workflow.webdesk.po.daos;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleHolderDynamic;

public interface PoRoleHolderDynamicDAO extends GenericDAO<PoRoleHolderDynamic>{

	/**
     * @param role
     * @param date
     * @return a <code>List</code> of <code>PoRoleHolderDynamic</code> objects that
     * are valid at the <code>date</code> or will become valid after that.
     */
    public List<PoRoleHolderDynamic> findRoleHolderDynamicF(PoRole role, Date date);
    

	/**
	 * @param controlledGroup the <code>PoGroup</code> that is the
	 * competence of the linked <code>PoRoleCompetenceBase</code> object.
	 * @param date the use <code>Date</code>
	 */
	public List<PoRoleHolderDynamic> findRoleHolderDynamicWithCompetence4Group(
			PoGroup controlledGroup, Date date);

	/**
	 * @param controlledPerson the <code>PoPerson</code> that is the
	 * competence of the linked <code>PoRoleCompetenceBase</code> object.
	 * @param date
	 */
	public List<PoRoleHolderDynamic> findRoleHolderDynamicWithCompetence4Person(
			PoPerson controlledPerson, Date date);
	
	
	/**
	 * @param date
	 */
	public List<PoRoleHolderDynamic> findRoleHolderDynamicWithCompetence4All(Date date);

	/**
	 * @param role
	 * @param controlledPerson
	 * @param date
	 * @return a <code>List</code> of <code>PoRoleHolderDynamic</code> objects 
	 * with a direct competence assignment to the given <code>controlledPerson</code>.
	 * The assignment has to be valid at the given <code>date</code>. 
	 */
	public List<PoRoleHolderDynamic> findRoleHolderDynamicWithCompetence4Person(PoRole role,
			PoPerson controlledPerson, Date date);
	
	/**
	 * @param role
	 * @param controlledGroup
	 * @param date
	 * @return a <code>List</code> of <code>PoRoleHolderDynamic</code> objects 
	 * with a direct competence assignment to the given <code>controlledGroup</code>.
	 * The assignment has to be valid at the given <code>date</code>. 
	 */
	public List<PoRoleHolderDynamic> findRoleHolderDynamicWithCompetence4Group(PoRole role,
			PoGroup controlledGroup, Date date);
	
	
	/**
	 * @param role
	 * @param controlledGroup
	 * @param date
	 * @return a <code>List</code> of <code>PoRoleHolderDynamic</code> objects 
	 * with competence assignment for all.
	 * The assignment has to be valid at the given <code>date</code>. 
	 */
	public List<PoRoleHolderDynamic> findRoleHolderDynamicWithCompetence4All(PoRole role,
			Date date);
	
	
	/**
	 * @param date
	 * @return a <code>List</code> of <code>PoRoleHolderDynamic</code> 
	 * objects. 
	 * 
	 */
	public List<PoRoleHolderDynamic> findRoleHolderDynamic(Date date);

}
