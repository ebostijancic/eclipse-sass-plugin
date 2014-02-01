/*
 * Created on 22.06.2005
 * @author hentner (Harald Entner)
 * 
 **/
package at.workflow.webdesk.po.model;

import java.util.Date;

/**
 * @author hentner
 * 
 * @hibernate.class
 * table="PoAuditLog"
 * @hibernate.cache usage="read-write"
 */
public class PoAuditLog {
	
	private String uid;
	private String objectName;
	private String attributeName;
	private String objectUid;
	private String modifiedBy;
	private Date lastModified;
	private String fieldValue;
	
	
	/**
	 * @return Returns the uID.
	 * @hibernate.id column="AUDITLOG_UID" generator-class="uuid.hex" length="32" type="string"
	 */
	public String getUID() {
		return uid;
	}
	/**
	 * @param uid The uID to set.
	 */
	public void setUID(String uid) {
		this.uid = uid;
	}
	/**
	 * @return Returns the attributeName.
	 * @hibernate.property length="30"
	 */
	public String getAttributeName() {
		return attributeName;
	}
	/**
	 * @param attributeName The attributeName to set.
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	/**
	 * @return Returns the lastModified.
	 * @hibernate.property type="timestamp"
	 */
	public Date getLastModified() {
		return lastModified;
	}
	/**
	 * @param lastModified The lastModified to set.
	 */
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	/**
	 * @return Returns the modifiedBy.
	 * @hibernate.property length="30"
	 */
	public String getModifiedBy() {
		return modifiedBy;
	}
	/**
	 * @param modifiedBy The modifiedBy to set.
	 */
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	/**
	 * @return Returns the objectUid.
	 * 
	 * @hibernate.property length="32"
	 */
	public String getObjectUid() {
		return objectUid;
	}
	/**
	 * @param objectUid The objectUid to set.
	 */
	public void setObjectUid(String objectUid) {
		this.objectUid = objectUid;
	}
	/**
	 * @return Returns the tableName.
	 * @hibernate.property 
	 *  
	 * 256 chars allowed
	 **/
	public String getObjectName() {
		return objectName;
	}
	/**
	 * @param tableName The tableName to set.
	 */
	public void setObjectName(String tableName) {
		this.objectName = tableName;
	}


	/**
	 * @return Returns the value.
	 * @hibernate.property length="200"
	 */
	public String getFieldValue() {
		return fieldValue;
	}
	/**
	 * @param value The value to set.
	 */
	public void setFieldValue(String value) {
		this.fieldValue = value;
	}
}
