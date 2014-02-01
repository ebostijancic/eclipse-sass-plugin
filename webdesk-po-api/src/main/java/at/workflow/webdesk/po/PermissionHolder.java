package at.workflow.webdesk.po;

import at.workflow.webdesk.tools.api.PersistentObject;

/** 
 * Marker interface to identify Po Model objects which are able to hold
 * permissions on actions. 
 * 
 * @author ggruber
 */
public interface PermissionHolder extends PersistentObject {

	/** @return a human-readable name of the permission holder, appropriate for UI labels. */
	String getLabel();

}
