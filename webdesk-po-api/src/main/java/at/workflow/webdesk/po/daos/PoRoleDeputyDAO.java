package at.workflow.webdesk.po.daos;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleDeputy;

public interface PoRoleDeputyDAO extends GenericDAO<PoRoleDeputy>{
	
	/**
	 * 
	 * <p>A <code>List</code> of <code>PoRoleDeputy</code> objects, if 
	 * a <code>roleDeputy</code> for the <code>role</code> and <code>person</code>
	 * exists. 
	 * </p>
	 * 
	 * 
	 * @param person
	 * @param role
	 * @param date
	 * @return a <code>List</code> of <code>PoRoleDeputy</code> objects.
	 */
	public List<PoRoleDeputy> findRoleDeputiesOfPerson(PoPerson person, PoRole role, Date date);
	
	/**
	 * 
	 * <p>A <code>List</code> of <code>PoRoleDeputy</code> objects, if 
	 * a <code>roleDeputy</code> for the <code>role</code> and <code>person</code>
	 * exists. The only constraint is that the <code>PoRoleDeputy</code> has to be valid or will 
	 * become valid after <code>validfrom</code> 
	 * </p>
	 * 
	 * 
	 * @param person <code>PoPerson</code>
	 * @param role <code>PoRole</code>
	 * @param date <code>java.util.Date</code>
	 * @return a <code>List</code> of <code>PoRoleDeputy</code> objects.
	 */	
	public List<PoRoleDeputy> findRoleDeputiesOfPersonF(PoPerson person, PoRole role, Date validfrom);
	

	

    /**
     * <p>Returns a <code>LIst</code> of <code>PoRoleDeputy</code> objects that
     * are valid at the given <code>date</code> or will become valid afterwards.
     * 
     * Use this function to determine all deputies of a person.
     * All deputies that are or will become valid are returned.
     * 
     * @param person a <code>PoPerson </code> object
     * @param date the <code>Date</code> on which the assignment should be valid or afterwards
     * @return a <code>List</code> of <code>PoRoleDeputies</code> assigned to <code>person</code>
     */
	public List<PoRoleDeputy> findRoleDeputiesOfPersonF(PoPerson person, Date date);
	
	
    /**
     * <p>
     * Use this function to determine the deputies of a person.
     * All deputies that are or will become valid are returned.
     * <p>
     * Pay attention: in comparison to other methods, this method
     * behaves diffently. The difference in question is the 
     * comparison of the <code>validto</code> property. Normally,
     * when a <code>date</code> is passed, the <code>validto</code>
     * field must be after this <code>date</code>, otherwise it is 
     * considered as deleted. And here's the difference: this function
     * includes the <code>PoRoleDeputy</code> when the <code>validto</code>
     * property is after or <b>equal</b> the given <code>date</code>.
     * 
     * @param person the office holder 
     * @return a <code>List</code> of <code>PoRoleDeputy</code> objects.
     */
	public List<PoRoleDeputy> findRoleDeputiesOfPerson(PoPerson person, Date date);

}
