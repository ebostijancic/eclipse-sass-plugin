/*
 * Created on 22.06.2005
 * @author hentner (Harald Entner)
 * 
 **/
package at.workflow.webdesk.po.model;

import java.util.Date;

/**
 * FIXME: Unused sofar
 * @author hentner
 */

@SuppressWarnings("unused")
public class PoAuditLogDefinition {

	/**
	 *  
	 * 
	 * 
	 */
	private String uid;

	/**
	 *  
	 * @hibernate.id column="AUDITLOG_UID" generator-class="uuid.hex" length="32" type="string"
	 */
	public String getUID() {
		return uid;
	}

	/**
	 *  
	 * 
	 */
	public void setUID(String uid) {
		this.uid = uid;
	}

	/**
	 *  
	 * 
	 */
	private String objectName;

	/**
	 *  
	 * @hibernate.property 
	 */
	public String getObjectName() {
		return objectName;
	}

	/**
	 *  
	 * 
	 */
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	/**
	 *  
	 * 
	 */
	private String attributeName;

	/**
	 *  
	 * @hibernate.property length="30"
	 * 
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 *  
	 * 
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	/**
	 *  
	 * 
	 */
	private String dataType;

	/**
	 *  
	 * @hibernate.property length="30"
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 *  
	 * 
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	
	
}
