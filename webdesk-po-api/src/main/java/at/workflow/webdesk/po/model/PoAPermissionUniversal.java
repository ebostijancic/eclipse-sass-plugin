package at.workflow.webdesk.po.model;

import at.workflow.webdesk.po.ActionPermission;

@SuppressWarnings("serial")
public class PoAPermissionUniversal extends PoHistorization implements ActionPermission {
	
	private PoAction action;
	
	@Override
	public PoAction getAction() {
		return action;
	}

	public void setAction(PoAction action) {
		this.action = action;
	}

	@Override
	public String getUID() {
		return null;
	}

	@Override
	public void setUID(String uid) {
		// nothing to do
	}
	
	

}
