package at.workflow.webdesk.tools.model.annotations;

/**
 * Field edit-ability. A field can be editable at different times: on creation only, ...
 * 
 * @author fritzberger 19.09.2013
 */
public enum Editable
{
	/**
	 * The field annotated with this value should never be editable by the user.
	 */
	NEVER,
	
	/**
	 * The field annotated with this value should always be editable by the user.
	 */
	ALWAYS,
	
	/**
	 * The field annotated with this value should be editable by the user only when its record was created newly.
	 */
	ON_CREATE,

}
