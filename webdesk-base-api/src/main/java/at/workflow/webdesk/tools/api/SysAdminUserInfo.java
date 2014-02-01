package at.workflow.webdesk.tools.api;

/**
 * This interface's implementation returns the
 * customizable name of the Full System Administrator. 
 * 
 * @author ggruber
 */
public interface SysAdminUserInfo {

	/** returns the Username of the Full System Administrator */
	public String getSysAdminUser();

	/** returns the User object corresponding the Full System Administrator */
	public User getSysAdminPerson();
}
