package at.workflow.webdesk.po;

import at.workflow.webdesk.po.model.PoClient;

/**
 * This interface defines methods for handling multi client specific functionality.
 * 
 * @author sdzuban 03.09.2012
 */
public interface PoMultiClientService {
	
	/**
	 * Updates prefix of short name of all groups of client from oldPrefix to newPrefix 
	 */
	void updateGroupShortNamePrefix(PoClient client, String oldPrefix, String newPrefix);

	/**
	 * Updates prefix of user name of all persons of client from oldPrefix to newPrefix 
	 */
	void updatePersonUserNamePrefix(PoClient client, String oldPrefix, String newPrefix);
	
	/**
	 * Updates prefix of employee id of all persons of client from oldPrefix to newPrefix 
	 */
	void updatePersonEmployeeIdPrefix(PoClient client, String oldPrefix, String newPrefix);
	
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
