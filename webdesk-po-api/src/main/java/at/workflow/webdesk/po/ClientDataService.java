package at.workflow.webdesk.po;

import java.util.List;

import at.workflow.webdesk.po.model.PoClient;


/**
 * This interface defines base methods as needed by any client data service. 
 * 
 * @author sdzuban 01.10.2012
 */
public interface ClientDataService<CLIENTDATA> {

	/** Saves client data. Must ensure, that client data is unique with regard to client */
	void saveClientData(CLIENTDATA data);
	
	CLIENTDATA getClientData(String uid);
	
	void deleteClientData(CLIENTDATA data);
	
	List<CLIENTDATA> loadAllClientData();
	
	/** @returns client data of specified client, null when called with null client */
	CLIENTDATA findClientDataForClient(PoClient client);
	
}
