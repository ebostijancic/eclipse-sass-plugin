/**
 * 
 */
package at.workflow.webdesk.po.model;

/**
 * @author fpovysil
 *
 */
public class DependendLock {

	
	private String lockUid;
	private int lockNumber;
	private String addInfo;
	private String key;
	
	public DependendLock(String lockUid, int lockNumber, String addInfo) {
		this.lockUid = lockUid;
		this.lockNumber = lockNumber;
		this.addInfo = addInfo;
		this.key = lockUid + "_" + new Integer(lockNumber).toString();
	}

	public int getLockNumber() {
		return lockNumber;
	}

	public void setLockNumber(int lockNumber) {
		this.lockNumber = lockNumber;
	}

	public String getLockUid() {
		return lockUid;
	}

	public void setLockUid(String lockUid) {
		this.lockUid = lockUid;
	}

	public String getAddInfo() {
		return addInfo;
	}

	public void setAddInfo(String addInfo) {
		this.addInfo = addInfo;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}


	
	
	
}
