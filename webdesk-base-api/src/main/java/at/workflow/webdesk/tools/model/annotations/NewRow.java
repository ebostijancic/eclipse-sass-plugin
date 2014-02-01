package at.workflow.webdesk.tools.model.annotations;

/**
 * The strategy how to create a new relation object.
 *  
 * @author fritzberger 04.09.2013
 */
public enum NewRow
{
	/**
	 * When creating a new relation, copy property values from recently created relation item.
	 * Falls back to IS_EMPTY when there is no recently created item.
	 */
	COPIES_RECENT,
	
	/**
	 * When creating a new relation, do not set any value into it.
	 * This will keep the values the item has by construction.
	 */
	IS_EMPTY,

}
