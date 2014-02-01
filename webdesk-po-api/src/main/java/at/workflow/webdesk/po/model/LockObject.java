/**
 * 
 */
package at.workflow.webdesk.po.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author fpovysil
 *
 */
@SuppressWarnings("serial")
public class LockObject implements Serializable {

	 private String objectUID;			// uid of the object to lock
	 private String lockGroup;			// additional part to specify the lock
	 private String cacheKey;			// key of the lock: objectUID + "_" + lockGroup
	 private String user;				// the user, whos locked the object
	 private String addInfo;			// add. information of the lock
	 private long refreshedAt;			// datetime last refreshed
	 private long created;				// datetime of creation
	 
	 /**
	  * default constructor
	  */
	 public LockObject() {
	 }

	 public LockObject(String objectUID, String user, String addInfo) {
		 this(objectUID, "1", user, addInfo);
	 }
	 
	 /**
	  * preffered constructor
	  * @param objectUID
	  * @param lockGroup
	  * @param user
	  * @param addInfo
	  */
	 public LockObject(String objectUID, String lockGroup, String user, String addInfo) {
		 this.objectUID = objectUID;
		 if ( lockGroup == null || lockGroup.equals(""))
			 this.lockGroup = "1";
		 else
			 this.lockGroup = lockGroup;
		 this.user = user;
		 this.addInfo = addInfo;
		 this.cacheKey = objectUID + "_" + this.lockGroup;
		 this.refreshedAt = new Date().getTime();
	 }

	public String getAddInfo() {
		return addInfo;
	}

	public void setAddInfo(String addInfo) {
		this.addInfo = addInfo;
	}

	public String getCacheKey() {
		return cacheKey;
	}

	public void setCacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
	}

	public String getLockGroup() {
		return lockGroup;
	}

	public void setLockGroup(String lockGroup) {
		this.lockGroup = lockGroup;
	}

	public String getObjectUID() {
		return objectUID;
	}

	public void setObjectUID(String objectUID) {
		this.objectUID = objectUID;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public long getRefreshedAt() {
		return refreshedAt;
	}

	public void setRefreshedAt(long refreshedAt) {
		this.refreshedAt = refreshedAt;
	}

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}
	
	
	
}
