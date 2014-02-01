package at.workflow.webdesk;

import java.io.Serializable;
import java.util.List;

/**
 * Hides the persistence implementation (database, Hibernate) from application.
 * In case of database persistence each table is wrapped by exactly one DAO.
 * 
 * @param <E> the Java type that is stored the wrapped table.
 */
public interface GenericDAO<E> {
	/**
	 * Reads the object with passed UID from persistence.
	 * This method does NOT throw an exception when the identifier was not found, it returns null then.
	 * @return the persistent object with given unique identifier, or null if not found.
	 */
	public E get(Serializable uid);
	
	/**
	 * Saves the passed object into persistence.
	 * @param entity the object to save to persistence.
	 */
	public void save(E entity);
	
	/**
	 * Deletes the passed object from persistence store.
	 * @param entity the object to delete from persistence store.
	 */
	public void delete(E entity);
	
	/**
	 * Loads all objects of the wrapped persistence store (database table)
	 * into a List and returns it.
	 * @return a List of all objects in wrapped persistence store.
	 */
	public List<E> loadAll();
	
	/**
	 * Removes the passed object from any cache.
	 * This will enforce fresh data for any subsequent get() call.
	 * @param entity the object to evict.
	 */
	public void evict(E entity);
}
