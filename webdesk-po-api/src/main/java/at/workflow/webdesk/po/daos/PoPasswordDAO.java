package at.workflow.webdesk.po.daos;

import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoPassword;
import at.workflow.webdesk.po.model.PoPerson;

/**
 * DAO interface for passwords of persons.
 * 
 * @author fritzberger 21.10.2010
 */
public interface PoPasswordDAO extends GenericDAO<PoPassword>	{
	
	/**
	 * @return the currently valid password of the passed person, might be null.
	 */
	PoPassword findCurrentPassword(PoPerson person);

	/**
	 * @return all passwords of the passed person, will never be null.
	 */
	List<PoPassword> findAllPasswords(PoPerson person);

	/**
	 * @return the latest n passwords of the passed person that are expired, will never be null.
	 */
	List<PoPassword> findNewestPasswords(PoPerson person, int numberOfPasswords);
	
}
