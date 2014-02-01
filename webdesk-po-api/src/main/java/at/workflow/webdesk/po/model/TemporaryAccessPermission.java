package at.workflow.webdesk.po.model;

import java.io.Serializable;

/**
 * This class represents a temporary Access Permission to access a specific action
 * for a specific user, only if the link of the action was clicked from another 
 * specific sourceaction.
 * 
 * furthermore it is only valid within the current session
 * 
 * @author ggruber
 *
 */
@SuppressWarnings("serial")
public class TemporaryAccessPermission implements Serializable {

	private String securityToken;
	/**
	 * the URL of the target Action to be accessed. f.i. po_editClients.act
	 */
	private String targetActionURL;
	/**
	 * the URL of the source Action where the link/clock comes from po_showClients.act
	 */
	private String sourceActionUrl;
	
	/**
	 * username (PoPerson.username) for which the permission is valid
	 */
	private String userName;
	private int hits=0;
	
	
	public String getSecurityToken() {
		return securityToken;
	}
	public void setSecurityToken(String id) {
		this.securityToken = id;
	}
	public String getTargetActionURL() {
		return targetActionURL;
	}
	public void setTargetActionURL(String targetActionURL) {
		this.targetActionURL = targetActionURL;
	}
	public String getSourceActionUrl() {
		return sourceActionUrl;
	}
	public void setSourceActionUrl(String sourceActionUrl) {
		this.sourceActionUrl = sourceActionUrl;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public int getHits() {
		return hits;
	}
	public void setHits(int hits) {
		this.hits = hits;
	}
	
}
