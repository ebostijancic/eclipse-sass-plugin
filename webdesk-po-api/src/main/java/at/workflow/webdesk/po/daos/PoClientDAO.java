package at.workflow.webdesk.po.daos;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoClient;

/**
 * DAO for PoClient
 * 
 * Created on 07.01.2005
 * @author ggruber
 */
public interface PoClientDAO extends GenericDAO<PoClient>{
    
	/**
     * 
     * This function returns a list of PoClient objects. <br>
     * Also objects that are not valid are returned.
     * 
	 * @param name
	 * @return a list of PoClient objects.
	 */
	PoClient findClientByName(String name);
	
    /**
     * This function returns true if a client with the given id exist.
     * 
     * @param name
     * @return true if a client with the given name exist.
     */
    boolean isClientExistent(String name);	

    
	/**
	 * Finds client using given prefix 
	 */
	PoClient findClientByGroupShortNamePrefix(String prefix);
	
	/**
	 * Finds client using given prefix 
	 */
	PoClient findClientByPersonUserNamePrefix(String prefix);
	
	/**
	 * Finds client using given prefix 
	 */
	PoClient findClientByPersonEmployeeIdPrefix(String prefix);
	
}
