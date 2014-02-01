package at.workflow.webdesk;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.tools.api.Historization;

/**
 * Hides the persistence implementation (database, Hibernate) from application
 * for historicized entities.
 * In case of database persistence each table is wrapped by exactly one DAO.
 * 
 * @author sdzuban 14.06.2013
 */
public interface HistoricizingDAO<E extends Historization> extends GenericDAO<E> {

	/** Calls loadAllValid(now). */
	List<E> loadAllValid();

	/** Calls loadAllValid(validityDate, validityDate). */
	List<E> loadAllValid(Date validityDate);

	/**
	 * Provides the loading of all records minus the invalid ones.
	 * @return a list of entities that have their validity date according to <code>isValid()</code> implementation.
	 */
	List<E> loadAllValid(Date validFrom, Date validTo);

	/**
	 * Historicizes the given entity when it ever has become valid, else it deletes the entity physically
	 * (data volume optimization - do not historicize entities that never were valid).
	 * This method should be used generally instead of delete(physically).
	 * <p/>
	 * Note: for future use it would be useful to pass a LinkRemover as second parameter.
	 * That LinkRemover could cleanup links when physical deletion happens.
	 * 
	 * @param pojo the entity to historicize or delete.
	 * @return true when given POJO was deleted physically
	 * 		(happens when validFrom in future -> must cleanup relations then!),
	 * 		false when it was just historicized.
	 */
	boolean historicize(E pojo);

}