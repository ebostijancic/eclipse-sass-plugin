package at.workflow.webdesk.po.link;

import at.workflow.webdesk.tools.api.Historization;

/**
 * This interface provides a generic removal method for
 * relation-references from related entities.
 * 
 * @author sdzuban 09.07.2013
 */
public interface LinkRemover {

	/**
	 * Example PoPersonGroup:
	 * <pre>
	 * ((PoPersonGroup) relation).getPerson().getMemberOfGroups().remove(relation);
	 * ((PoPersonGroup) relation).getGroup().getPersonGroups().remove(relation);
	 * </pre>
	 * 
	 * @param relation the relation-object that should be removed from the Collections of its related entities.
	 */
	void remove(Historization relation);

}
