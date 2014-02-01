package at.workflow.webdesk.po.daos;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleHolderPerson;

public interface PoRoleHolderPersonDAO extends GenericDAO<PoRoleHolderPerson> {
		
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4Client(PoRole role,
			PoClient client, Date date);

	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4Group(PoRole role,
			PoGroup controlledGroup, Date date);
    
	/**
     * @param role the role object the person is linked to 
     * @param person the person that controls
     * @param controlledPerson the person that is controlled
     * @param referenceDate at which the PoRoleHolderPerson object has to be valid
     * @deputy set to <code>true</code> if the returned element should have assigned a 
     * <code>PoRoleDeputy</code> object, <code>false</code> otherwise. Most likely will be false.
     * @return a PoRoleHolderPerson object, if the given person 
     * has competences over the given controlledPerson. The PoRoleHolderPerson link has to be 
     * valid at the given date.
     * If no link exists, null is returned.
     */
	public PoRoleHolderPerson findRoleHolderPersonWithCompetence4Person(
			PoRole role, PoPerson person, PoPerson controlledPerson, Date date,
			boolean deputy);

	/**
	 * @param role
	 * @param person
	 * @param date
	 * @return a <code>List</code> of <code>PoRoleHolderPerson</code> objects 
	 * which have a <code>PoRoleCompetencePerson</code> object assigned where  
	 * <code>competence4Person</code> is the given <code>person</code>.
	 */
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4Person(PoRole role,
			PoPerson person, Date date);

	
	
    /**
     * @param role
     * @param person
     * @param controlledGroup
     * @param referenceDate
     * @deputy set to <code>true</code> if the returned element should have assigned a 
     * <code>PoRoleDeputy</code> object, <code>false</code> otherwise. Most likely will be false.
     * @return a PoRoleHolderPerson object, if the given person 
     * has competences over the given controlledGroup. The PoRoleHolderPerson link has to be 
     * valid at the given date.
     * If no link exists, null is returned.
     */
	public PoRoleHolderPerson findRoleHolderPersonWithCompetence4Group(
			PoRole role, PoPerson person, PoGroup controlledGroup, Date date,
			boolean deputy);

	public PoRoleHolderPerson findRoleHolderPersonWithCompetence4Client(
			PoRole role, PoPerson person, PoClient client, Date date);
	
	public PoRoleHolderPerson findRoleHolderPersonWithCompetence4All(
			PoRole role, PoPerson person, Date date, boolean deputy);

	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4AllF(PoRole role,
			PoPerson person, Date date);

	/**
	 * <p>This function returns a <code>List </code> of <code>PoRoleHolderPerson</code>
	 * objects that are valid now or will become valid in the future.
	 * </p>
	 * <p>The assigned <code>PoRoleCompetenceBase</code> object is of type 
	 * <code>PoRoleCompetencePerson</code>.
	 * </p>
	 * 
	 * @param role
	 * @param officeHolder a <code>PoPerson</code> that is the <code>officeHolder</code> of the 
	 * <code>PoRole</code>.
	 * @param validFrom
	 * @return a <code>List</code> of <code>PoRoleHolderPerson</code> objects
	 * 
	 */
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4PersonF(PoRole role, 
			PoPerson officeHolder, Date validFrom);

	
	

	/**
	 * <p>This function returns a <code>List </code> of <code>PoRoleHolderPerson</code>
	 * objects that are valid now or will become valid in the future.
	 * </p>
	 * <p>The assigned <code>PoRoleCompetenceBase</code> object is of type 
	 * <code>PoRoleCompetenceGroup</code>.
	 * </p>
	 * 
	 * @param role
	 * @param officeHolder a <code>PoPerson</code> that is the <code>officeHolder</code> of the 
	 * <code>PoRole</code>.
	 * @param validFrom
	 * @return a <code>List</code> of <code>PoRoleHolderPerson</code> objects
	 * 
	 */
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4GroupF(PoRole role, 
			PoPerson officeHolder, Date validFrom);
	

	/**
	 * Use this function to get a <code>List</code> of all <code>PoRoleHolderPerson</code>'s,
	 * with a <code>PoRoleCompetencePerson</code> assigned with competence for the 
	 * <code>target</code> object. The <code>PoRoleHolderPerson</code>'s have to be valid 
	 * at the given <code>date</code> or become valid afterwards.
	 * 
	 * 
	 * @param role
	 * @param officeHolder
	 * @param target
	 * @param date
	 * @return a <code>List</code> of <code>PoRoleHolderPerson</code> objects.
	 */
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4PersonF(PoRole role, PoPerson officeHolder, PoPerson target, Date date);


	/**
	 * Use this function to get a <code>List</code> of all <code>PoRoleHolderPerson</code>'s,
	 * with a <code>PoRoleCompetenceGroup</code> assigned with competence for the 
	 * <code>target</code> object. The <code>PoRoleHolderPerson</code>'s have to be valid 
	 * at the given <code>date</code> or become valid afterwards.
	 * 
	 * 
	 * @param role
	 * @param officeHolder
	 * @param target
	 * @param date
	 * @return a <code>List</code> of <code>PoRoleHolderPerson</code> objects.
	 */
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4GroupF(PoRole role, PoPerson officeHolder, PoGroup target, Date date);
	
	
	/**
	 * Use this function to get a <code>List</code> of all <code>PoRoleHolderPerson</code>'s,
	 * with a <code>PoRoleCompetenceClient</code> assigned with competence for the 
	 * <code>target</code> object. The <code>PoRoleHolderPerson</code>'s have to be valid 
	 * at the given <code>date</code> or become valid afterwards.
	 * 
	 * 
	 * @param role
	 * @param officeHolder
	 * @param target
	 * @param date
	 * @return a <code>List</code> of <code>PoRoleHolderPerson</code> objects.
	 */
	public List<PoRoleHolderPerson> findRoleHolderPersonWithCompetence4ClientF(PoRole role, PoPerson officeHolder, PoClient target, Date date);
	
	
	/**
	 * @param person
	 * @param date
	 * @return a <code>List</code> of <code>PoRoleHolderPerson</code> objects, which have to be valid
	 * at the given date. Only "normal" roles are returned. <code>Dummy roles</code> are not considered.
	 * The given <code>PoPerson</code> should be the same as the person linked with the <code>PoRoleHolderPerson</code>. 
	 */
	public List<PoRoleHolderPerson> findRoleHolderPerson(PoPerson person, Date date);

	/**
	 * @param person
	 * @param date
	 * @return a <code>List</code> of <code>PoRoleHolderPerson</code> objects, which have to be valid
	 * at the given date or become active afterwards. Only "normal" roles are returned. <code>Dummy roles</code> are not considered.
	 * The given <code>PoPerson</code> should be the same as the person linked with the <code>PoRoleHolderPerson</code>. 
	 */
	public List<PoRoleHolderPerson> findRoleHolderPersonF(PoPerson person, Date date);

	/**
	 * @param person
	 * @return a <code>List</code> of <code>PoRoleHolderPerson</code> objects
	 * Only "normal" roles are returned. <code>Dummy roles</code> are not considered.
	 * The given <code>PoPerson</code> should be the same as the person linked with the <code>PoRoleHolderPerson</code>. 
	 */
	public List<PoRoleHolderPerson> findRoleHolderPersonAll(PoPerson person);
	
	/**
	 * @param person a <code>PoPerson</code>
	 * @param date a <code>java.util.Date</code>
	 * @return a <code>List</code> of <code>PoRoleHolderPerson</code> objects, which have to be valid
	 * at the given date or become active afterwards. Only "normal" roles are returned. <code>Dummy roles</code> are not considered.
	 * The given <code>PoRole</code> should be the same as the <code>role</code> of the <code>PoRoleHolder</code> 
	 * which is linked with the <code>PoRoleHolderPerson</code>. 
	 * 
	 */
	public List<PoRoleHolderPerson> findRoleHolderPersonF(PoRole role, Date date);

	/**
	 * @param person a <code>PoPerson</code>
	 * @param date a <code>java.util.Date</code>
	 * @return a <code>List</code> of <code>PoRoleHolderPerson</code> objects, which have to be valid
	 * at the given date. Only "normal" roles are returned. <code>Dummy roles</code> are not considered.
	 * The given <code>PoRole</code> should be the same as the <code>role</code> of the <code>PoRoleHolder</code> 
	 * which is linked with the <code>PoRoleHolderPerson</code>. 
	 * 
	 */
	public List<PoRoleHolderPerson> findRoleHolderPerson(PoRole role, Date date);
	

	public List<PoRoleHolderPerson> findRoleHolderPersonsWithRoleF(PoRole role, Date date);
	

}
