package at.workflow.webdesk.po.model;

/**
 * maps an action to a client for a specific timeframe. if an action is mapped this 
 * way, it reflects, that all members of this client (persons and groups) are allowed
 * to use the linked action. (PoAction Object)
 * 
 * Created on 16.06.2005
 * @author Gabriel Gruber, Harald Entner
 */
public class PoAPermissionClient extends PoAPermissionBase {

	private static final long serialVersionUID = 1L;

    private PoClient client;

	@Override
	public void accept(PoAPermissionVisitor visitor) {
        visitor.visit(this);
    }
    
    /** @return mapped client (PoClient object) who is permitted to use the referenced Action. */
    public PoClient getClient() {
        return client;
    }

    public void setClient(PoClient client) {
        this.client = client;
    }

}
