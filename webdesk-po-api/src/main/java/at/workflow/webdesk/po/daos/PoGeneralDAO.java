package at.workflow.webdesk.po.daos;

import java.util.List;

import at.workflow.webdesk.tools.NamedQuery;
import at.workflow.webdesk.tools.PaginableQuery;
import at.workflow.webdesk.tools.PositionalQuery;
import at.workflow.webdesk.tools.api.PersistentObject;

/**
 * <p>
 * General DAO functions (which do not belong to 
 * one of the existing DAO's) should take place here.
 * </p>
 * Created at:       23.01.2008
 * @author DI Harald Entner  logged in as: hentner<br><br>
 */
@SuppressWarnings("rawtypes")
public interface PoGeneralDAO {

	/** 
	 * This is the universal generic method for all three currently defined Query classes.
	 * It processes queries without parameters, with positional parameters, with named parameters.
	 * @return without SELECT clause List of returned Objects, otherwise Object[].
	 */
	public List getListOfPOJOsOrArrays(PaginableQuery query);
	
	/**
	 * This is a universal generic method where you define all parameters in the 
	 * signature including the firstResult, maxResults and cacheable.
	 * If no parameters are supplied pass NULL values for names and values.
	 * If you want to use positional parameters, pass NULL for names and the
	 * actual Parameter objects in the values Array.
	 * @return List of Objects or list of Object[]
	 */
	public List find(String hqlOrNamedQuery, String[] names, Object[] values, int firstResultStartingWithZero, int maxResults, boolean cacheable);
	
	/**
	 * These methods take one of following arguments:
	 *  - PositionalQuery object
	 *  - NamedQuery object
	 *  Thus any additional parameters (like firstResult, maxResults)
	 *  can be conveyed to the DAO.
	 */
	public List getElementsAsList(PositionalQuery query);
	
	/** TODO: please document this! */
	public List findByNamedParam(NamedQuery query);
	
	/**
	 * Executes the given <code>query</code> with the given <code>keys</code>.
	 * 
	 * @param query a <code>String</code> representing a hql query.
	 * @param keys the keys used to query the db.
	 * @return a <code>List</code> of <code>Objects</code>. The kind of 
	 * Objects depends on the <code>query</code> that is used. 
	 */
	public List getElementsAsList(String query, Object[] keys);

	/** @deprecated	use findByNamedParam(NamedQuery query). */
	public List findByNamedParam(String query, String[] paramNames,	Object[] values);
	
	/** @return the domain object of passed class (database table) with unique identifier (primary database key). */
	public PersistentObject getObject(Class<?> clazz, String uid);
	
	/** TODO: please comment what this does. */
	public Object getOriginalObject(Object object);

	/** Deletes the given database object. */
	public void deleteObject(PersistentObject object);
	
	/** fri_2011-09-27: Evict means remove from Hibernate second level cache. */
	public void evictObject(Object object);
	
	public void saveObject(Object object);
	
	/**
	 * fri_2011-09-27: TODO: this might not return what expected but much more
	 * when the queried class has a CASCADE option on read operation.
	 * Then a join will performed on CASCADED entity-type, but only the objects
	 * of the given class will be returned, and these will not be unique then!
	 * 
	 * @param clazz the class to retrieve all instances from persistence.
	 * @return a list of record instances of the passed type (class).
	 */
	public List loadAllObjectsOfType(Class<?> clazz);

}
