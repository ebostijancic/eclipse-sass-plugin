package at.workflow.webdesk.tools.hibernate;

import java.io.Serializable;
import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.tools.api.PersistentObject;

/**
 * Generic data access object (DAO) implementation for the Hibernate persistence layer,
 * providing CRUD methods (create/read/update/delete).
 * 
 * @author ggruber
 * @author fritzberger (refactorings and extensions).
 * 
 * @param <E> the POJO class read from persistence layer (i.e. the class of a database record)
 */
public abstract class GenericHibernateDAOImpl<E> extends GenericHibernateDaoSupport implements GenericDAO<E> {

	/** Read method, see GenericDAO. */
	@Override
	public /*final*/ E get(Serializable uid) {
		E ret = getHibernateTemplate().get(getEntityClass(), uid);
		afterGet(ret);
		return ret;
	}

	/** Bulk Read method, see GenericDAO. */
	@Override
	public /*final*/ List<E> loadAll() {
		return getHibernateTemplate().loadAll(getEntityClass());
	}

	/** Delete method, see GenericDAO. */
	@Override
	public /*final*/ void delete(E entity) {
		beforeDelete(entity);
		getHibernateTemplate().delete(entity);
	}

	/** CREATE/UPDATE method, see GenericDAO. */
	@Override
	public /*final*/ void save(E entity) {
		beforeSave(entity);
		getHibernateTemplate().saveOrUpdate(entity);
	}
	
	/**
	 * Called after an entity has been read. This implementation does nothing.
	 * Override for additional actions on the entity.
	 * @param e the entity that just has been read from persistence.
	 */
	@SuppressWarnings("unused")	// parameter e -> afterGet was made for overriding!
	protected void afterGet(E e)	{
	}
	
	/**
	 * Called before save(entity), does nothing.
	 * Override this to throw some RuntimeException when data are not ready to be saved,
	 * or to complete data contained in entity.
	 * @param e the entity about to be saved.
	 * @exception RuntimeExcpetion when data are not valid.
	 */
	@SuppressWarnings("unused")	// parameter e -> beforeSave was made for overriding!
	protected void beforeSave(E e)	{
	}
	
	/**
	 * Called before delete(entity), does nothing.
	 * Override this to do things before deletion happens.
	 * @param e the entity about to be deleted.
	 */
	@SuppressWarnings("unused")	// parameter e -> beforeDelete was made for overriding!
	protected void beforeDelete(E e)	{
	}
	
	@Override
	public final void evict(E entity) {
		getHibernateTemplate().evict(entity);
	}

	@Override
	protected abstract Class<E> getEntityClass();
	
	@Override
	protected PersistentObject getPersistentObject(String entityUid) {
		return (PersistentObject) get(entityUid);
	}

	/**
	 * Generic find implementation that delivers the passed criterion.
	 * @param filter the filter to apply to query.
	 * @return the List of found entities.
	 */
	protected List<E> find(Filter filter)	{
		return find(new Filter [] { filter });
	}
	
	/**
	 * Generic find implementation that AND's all passed criteria.
	 * @param filters the filters to apply to query.
	 * @return the List of found entities.
	 */
	protected List<E> find(Filter [] filters)	{
		return find(filters, null);
	}
	
	/**
	 * Generic find implementation that AND's all passed criteria.
	 * @param filters the filters to apply to query.
	 * @param orderBy the ORDER BY clause text without leading "oder by", can be null.
	 * @return the List of found entities.
	 */
	protected List<E> find(Filter [] filters, String orderBy)	{
		return find(null, filters, orderBy);
	}
	
	/**
	 * Generic find implementation that AND's all passed criteria.
	 * IN-clauses are described by passing a List as attributeValue in Filter.
	 * NULL values are handled via <code>Filter.IGNORE_IN_QUERY</code>.
	 * @param queryStart the SELECT or FROM clause to use,
	 * 		can be null, then <code>from()</code> is called. Must not contain "where"!
	 * @param filters the filters to apply to query, all AND'ed.
	 * @param orderBy the ORDER BY clause text, without leading "oder by", can be null.
	 * @return the List of found entities.
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected List<E> find(final String queryStart, final Filter [] filters, final String orderBy)	{
		return super.find(queryStart, filters, orderBy);
	}

}
