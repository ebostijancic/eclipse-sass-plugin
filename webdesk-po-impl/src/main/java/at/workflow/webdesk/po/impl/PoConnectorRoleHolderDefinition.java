package at.workflow.webdesk.po.impl;

import at.workflow.webdesk.po.model.PoRole;

public class PoConnectorRoleHolderDefinition {
	
	static public final String KEY_EMPLOYEEID="employeeId";
	static public final String KEY_TAID="taId";
	static public final String KEY_USERNAME="userName";
	
	private PoRole	role; 
	private int maximumRanking;
	private String key;
	private boolean deleteCurrentRoleHolderWhenPersonNotFound;
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public int getMaximumRanking() {
		return maximumRanking;
	}
	public void setMaximumRanking(int maximumRanking) {
		this.maximumRanking = maximumRanking;
	}
	public PoRole getRole() {
		return role;
	}
	public void setRole(PoRole role) {
		this.role = role;
	}
	public boolean isDeleteCurrentRoleHolderWhenPersonNotFound() {
		return deleteCurrentRoleHolderWhenPersonNotFound;
	}
	public void setDeleteCurrentRoleHolderWhenPersonNotFound(
			boolean deleteCurrentRoleHolderWhenPersonNotFound) {
		this.deleteCurrentRoleHolderWhenPersonNotFound = deleteCurrentRoleHolderWhenPersonNotFound;
	}

}
