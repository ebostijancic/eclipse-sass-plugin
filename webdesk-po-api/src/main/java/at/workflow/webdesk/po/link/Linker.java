package at.workflow.webdesk.po.link;

import at.workflow.webdesk.tools.api.Historization;

/**
 * This interface provides a generic linking method for
 * relation-references from related entities.
 * 
 * @author sdzuban 15.07.2013
 */
public interface Linker {

	/**
	 * Example PoPersonGroup:
	 * <pre>
	 * if (((PoPersonGroup) relation).getPerson().getMemberOfGroups().add(relation))
	 * 	((PoPersonGroup) relation).getPerson().getMemberOfGroups().add(relation);
	 * if (((PoPersonGroup) relation).getGroup().getPersonGroups().add(relation))
	 * 	((PoPersonGroup) relation).getGroup().getPersonGroups().add(relation);
	 * </pre>
	 * 
	 * @param relation the relation-object that should be added to the Collections of its related entities.
	 */
	void addLinks(Historization relation);

}
