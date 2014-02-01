package at.workflow.webdesk.po.base;

import java.util.List;

import org.apache.log4j.Logger;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.ClientDataService;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.ClientData;

/**
 * This service base provides basic methods for handling of module specific client data.
 * 
 * @author sdzuban 01.10.2012
 */
public abstract class ClientDataServiceBase<CLIENTDATA extends ClientData> 
	implements ClientDataService<CLIENTDATA> {

    private static final Logger logger = Logger.getLogger(ClientDataService.class);
	
	private GenericDAO<CLIENTDATA> clientDataDAO;
	
	protected GenericDAO<CLIENTDATA> getClientDataDAO() { return clientDataDAO; }
	
	/** This method is used for inclusion of module specific client data checks into saving process 
	 * @param data client data */
	protected boolean isClientDataOK(CLIENTDATA data) { return true; }
	
	/** {@inheritDoc} */
	@Override
	public final void saveClientData(CLIENTDATA data) {
		
		PoClient client = data.getClient();
		if(client == null) {
			String msg = "Client is missing in client data";
			logger.error(msg);
			throw new RuntimeException(msg);
		}
		
		CLIENTDATA other = findClientDataForClient(client);
		if (other != null && (data.getUID() == null || !data.equals(other))) {
			String msg = "There is already different client data stored for this client.";
			logger.error(msg);
			throw new RuntimeException(msg);
		}
		
		if (isClientDataOK(data))
			clientDataDAO.save(data);
	}

	/** {@inheritDoc} */
	@Override
	public CLIENTDATA getClientData(String uid) {
		return clientDataDAO.get(uid);
	}

	/** {@inheritDoc} */
	@Override
	public void deleteClientData(CLIENTDATA data) {
		clientDataDAO.delete(data);
	}

	/** {@inheritDoc} */
	@Override
	public List<CLIENTDATA> loadAllClientData() {
		return clientDataDAO.loadAll();
	}


	public void setClientDataDAO(GenericDAO<CLIENTDATA> clientDataDAO) {
		this.clientDataDAO = clientDataDAO;
	}

}
