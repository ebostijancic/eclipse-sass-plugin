package at.workflow.webdesk.po.model;
/**
 * This class defines methods common for module specific client data.
 * 
 * @author sdzuban 01.10.2012
 */
@SuppressWarnings("serial")
public abstract class ClientData extends PoBase {

	/** client for which the data applies */
	public abstract PoClient getClient();

	/** client for which the data applies */
	public abstract void setClient(PoClient client);

}
