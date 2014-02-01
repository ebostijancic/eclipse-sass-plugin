package at.workflow.webdesk.po;

import java.util.List;

import at.workflow.webdesk.tools.api.PersistentObject;

/***
 * Services for the search in the indexed entities in
 * the database for improving the search.
 * @author iaranibar 10.12.2013
 */
public interface PoSearchService {

	/**
	 * (Re)-index the files index of Lucene.
	 */
	void refreshSearchIndex();

	/***
	 * Clean the files index of Lucene.
	 */
	void cleanSearchIndex();

	/**
	 * Searches the Lucene index with the passed searchPhrase in the passed fields.
	 * For this to work correctly, you have to annotate the used entity-type as @Indexed,
	 * and each of its search properties with @Field or @IndexedEmbedded.
	 * By the settings of this implementation, * or ? are allowed as wildcard-characters,
	 * but note that this can produce very slow queries on big indexes.
	 * <p/>
	 * Warning: Before using this method for several classes solve 
	 * WDHREXPERT-559.
	 * <p/>
	 * @param searchPhrase is the string which was supplied by the user to search.
	 * @param entityClasses the set of database tables to search.
	 * @return objects that conform to given searchPhrase.
	 */
	List<? extends PersistentObject> search(String searchPhrase, Class<? extends PersistentObject>[] entityClasses);

}
