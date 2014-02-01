package at.workflow.webdesk.po;

import java.util.List;
import java.util.Map;

import at.workflow.webdesk.tools.NamedQuery;
import at.workflow.webdesk.tools.PaginableQuery;
import at.workflow.webdesk.tools.PositionalQuery;
import at.workflow.webdesk.tools.api.PersistentObject;

/**
 * This service provides convenient methods to access model instances from
 * the database directly without going through special DAOs.
 * 
 * this can be used in a scripting environment or in situations where no
 * DAOs and special services are available (for instance for rapid prototyping
 * where only Hibernate domain classes (and mappings) are present).
 * 
 * @author ggruber
 */
public interface PoGeneralDbService {

	public PersistentObject getObject(Class<? extends PersistentObject> clazz, String uid);
	
	public void deleteObject(PersistentObject object);
	
	public void saveObject(PersistentObject object);
	
	public List<? extends PersistentObject> loadAllObjectsOfType(Class<? extends PersistentObject> clazz);
	
	/** 
	 * This is the universal generic method for all three currently defined Query classes.
	 * It processes queries without parameters, with positional parameters, with named parameters.
	 * It supports full pagination emulation.
	 * @return without explicit SELECT clause this returns a List of persistence Objects,
	 * 		else a List of arrays where one array represents all fields in SELECT clause, in order as given in query.
	 */
	public List selectObjectsOrArrays(PaginableQuery query);

	/**
	 * Executes all the 3 query types, see findByQuery.
	 * The query MUST contain SELECT clause with at least two terms. Otherwise an exception is thrown.
	 * It supports full pagination emulation.
	 * @return List of Maps (one Map stands for one database-record) where key is 
	 * the expression or the alias from SELECT clause, and value is the retrieved value.
	 */
	public List<Map<String, ?>> select(PaginableQuery query);
	
	/**
	 * Executes the hql query with the positional parameter values from query object parameter.
	 * It supports full pagination emulation.
	 * @param query object with the query string and positional parameter values
	 * @return a <code>List</code> of <code>Objects</code>. The kind of 
	 * Objects depends on the <code>query</code> that is used. 
	 */
	public List getElementsAsList(PositionalQuery query);
	
	/**
	 * Executes the hql query with positional parameter values from query object parameter
	 * The query MUST contain SELECT clause with at least two terms. Otherwise an exception is thrown.
	 * It supports full pagination emulation.
	 * @param query object with the query string and positional parameter values
	 * @return List of Maps (one Map stands for one database-record) where key is 
	 * the expression or the alias from SELECT clause, and value is the retrieved value.
	 */
	public List<Map<String, ?>> getElementsAsListOfNamedMaps(PositionalQuery query);
	
	/**
	 * Executes the hql query with the named parameter values from query object parameter.
	 * It supports full pagination emulation.
	 * @param query object with the query string, parameter names and values.
	 * @return a <code>List</code> of <code>Objects</code>. The kind of 
	 * Objects depends on the <code>hql query</code> that is used. 
	 */
	public List getElementsAsList(NamedQuery query); 
	
	/**
	 * Executes the hql query with named parameter values from query object parameter
	 * The query MUST contain SELECT clause with at least two terms. Otherwise an exception is thrown.
	 * It supports full pagination emulation.
	 * @param query object with the query string, parameter names and values.
	 * @return List of Maps (one Map stands for one database-record) where key is 
	 * the expression or the alias from SELECT clause, and value is the retrieved value.
	 */
	public List<Map<String, ?>> getElementsAsListOfNamedMaps(NamedQuery query);
	
	
	
	/**
	 * Executes the given <code>query</code> with the given <code>keys</code>.
	 * 
	 * @param query a <code>String</code> representing a hql query.
	 * @param keys the keys used to query the db.
	 * @return a <code>List</code> of <code>Objects</code>. The kind of 
	 * Objects depends on the <code>query</code> that is used. 
	 */
	public List getElementsAsList(String query, Object[] keys);
	
	/**
	 * Executes the query with keys.
	 * Returns List of Maps (one Map stands for one database-record) where key is a "columnX" String 
	 * (with X being the index of the value in the query result), and value is the retrieved value.
	 * 
	 */
	public List<Map<String, ?>> getElementsAsListOfMaps(String query, Object[] keys);

	/**
	 * Executes the query with keys
	 * @param query
	 * @param keys
	 * @return List of Maps (one Map stands for one database-record) where key is 
	 * the expression or the alias from SELECT clause, and value is the retrieved value.
	 * If no SELECT clause is found it works the same as getElementsAsListOfMaps()
	 */
	public List<Map<String, ?>> getElementsAsListOfNamedMaps(String query, Object[] keys);
	
	/**
	 * converts an existing result of a hql query, which is an array of objects and
	 * the corresponding original hql-query to a list of named maps where the key
	 * is the expression or the alias from the select Clause.
	 * 
	 * @param listOfArrays retruned from a HQL query
	 * @param query - the hql query which was used.
	 */
	public List<Map<String, ?>> convertResultToListOfNamedMaps(List<Object[]> listOfArrays, String query);
	
	/**
	 * Executes the given <code>query</code> with the named <code>values</code>.
	 * 
	 * @param query a <code>String</code> representing a hql query.
	 * @param paramNames the names used as placeholder in the query.
	 * @param paramValues the names used as placeholder in the query.
	 * @return a <code>List</code> of <code>Objects</code>. The kind of 
	 * Objects depends on the <code>query</code> that is used. 
	 */
	public List findByNamedParam(String query, String[] paramNames,	Object[] paramValues);
	
	/**
	 * @param o the object that should be evicted (removed from session)
	 */
	public void evictObject(Object o);
	
}
