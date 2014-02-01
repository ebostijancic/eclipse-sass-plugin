package at.workflow.webdesk.po.impl.daos;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.daos.PoGeneralDAO;
import at.workflow.webdesk.po.model.PoBase;
import at.workflow.webdesk.tools.NamedQuery;
import at.workflow.webdesk.tools.PaginableQuery;
import at.workflow.webdesk.tools.PositionalQuery;
import at.workflow.webdesk.tools.api.PersistentObject;

/**
 * This is a general DAO for Hibernate Access.
 * 
 * @author sdzuban
 * @author ggruber
 *
 */
@SuppressWarnings("rawtypes")
public class PoGeneralDAOImpl extends HibernateDaoSupport implements PoGeneralDAO	{

	/** {@inheritDoc} */
	@Override
	public List getListOfPOJOsOrArrays(PaginableQuery query) {
	
		String hql = query.getQueryText();
		String[] names = null;
		Object[] values = null;
		
		if (query instanceof NamedQuery) {
			NamedQuery q = (NamedQuery) query;
			names = q.getParamNames();
			values = q.getParamValues();
		} else if (query instanceof PositionalQuery) {
			values = ((PositionalQuery) query).getParamValues();
		} 
		return find(hql, names, values, query.getFirstResult(), query.getMaxResults(), false);
	}
	
	/** {@inheritDoc} */
	@Override
	public List getElementsAsList(PositionalQuery query) {
		Object[] keys = query.getParamValues();
		String hql = query.getQueryText();
		return find(hql, null, keys, query.getFirstResult(), query.getMaxResults(), false);
	}
	
	
	@Override
	public List find(final String hqlOrQueryName, final String[] names, final Object[] values, final int firstResultStartingWithZero, final int maxResults, final boolean cacheable) {
		
		return getHibernateTemplate().execute( new HibernateCallback<List<?>>() {

			@Override
			public List<?> doInHibernate(Session session) throws HibernateException, SQLException {
				
				final Query query;
				if (hqlOrQueryName.contains("from "))
					query = session.createQuery( hqlOrQueryName );
				else 
					query = session.getNamedQuery( hqlOrQueryName );
				
				if (firstResultStartingWithZero > 0)
					query.setFirstResult(firstResultStartingWithZero);
				if (maxResults > 0)
					query.setMaxResults(maxResults);
				
				query.setCacheable(cacheable);
				
				int i = 0;
				if (values != null && names!=null) {
					while (i < values.length) {
						if (names[i] != null && query.getQueryString().indexOf(":" + names[i]) > -1) {
							applyNamedParameterToQuery(query, names[i], values[i]);
						}
						i++;
					}
				} else if (values!=null && names==null) {
					while (i < values.length) {
						query.setParameter(i, values[i]);
						i++;
					}
					
				}
				return query.list();
			}
		});
		
	}
	
	static void applyNamedParameterToQuery(Query queryObject, String paramName, Object value)
			throws HibernateException {

		if (value instanceof Collection) {
			Collection<?> collectionValue = (Collection<?>) value;
			if (collectionValue.size()==0)
				queryObject.setParameterList(paramName, Arrays.asList(new String[] { "" } ));
			else
				queryObject.setParameterList(paramName, collectionValue);  
		}
		else if (value instanceof Object[]) {
			Object[] objArray = (Object[]) value;
			if (objArray.length==0)
				queryObject.setParameterList(paramName, new String[] { "" } );
			else
				queryObject.setParameterList(paramName, objArray);
		}
		else {
			queryObject.setParameter(paramName, value);
		}
	}

	
	

	/** {@inheritDoc} */
	@Override
	public List findByNamedParam(NamedQuery query) {
		return find(query.getQueryText(), query.getParamNames(), query.getParamValues(), query.getFirstResult(), query.getMaxResults(), false);
	}

	
	// ------- method working with query string and keys -----------
	
	@Override
	public List getElementsAsList(String query, Object[] keys) {
		if (keys == null || keys.length == 0)
			return getHibernateTemplate().find(query);
		return getHibernateTemplate().find(query, keys);
	}

	@Override
	public void deleteObject(PersistentObject object) {
		getHibernateTemplate().delete(object);
	}

	@Override
	public PersistentObject getObject(Class<?> clazz, String uid) {
		return (PersistentObject) getHibernateTemplate().get(clazz,uid);
	}

	@Override
	public List loadAllObjectsOfType(Class<?> clazz) {
		return getHibernateTemplate().loadAll(clazz);
	}

	@Override
	public void saveObject(Object object) {
		getHibernateTemplate().saveOrUpdate(object);
	}

	@Override
	public List findByNamedParam(String query, String[] paramNames,	Object[] values) {
		if (paramNames == null && values == null)
		   return getHibernateTemplate().find(query);
		return getHibernateTemplate().findByNamedParam(query, paramNames, values);
	}

	@Override
	public void evictObject(Object object) {
		getHibernateTemplate().evict(object);
	}

	@Override
	public Object getOriginalObject(Object object) {
		Session s = null;
		if ( ! (object instanceof PoBase) )
			throw new RuntimeException("Only Subclasses of PoBase can be processed...");
		
	    String uid = ((PoBase)object).getUID();
	    Object ret = null;
	    try {
			s = getHibernateTemplate().getSessionFactory().openSession();
			// TODO: WD-24 with new HSQLDB Version this command hangs!  
			ret = s.get(object.getClass(), uid);
	    } catch (HibernateException e) {
			this.logger.error(e);
			throw new PoRuntimeException("problems getting original object..", e);
		}
		finally {
	    	try {
	    		if (s!= null) { 
	    			s.close();
	    		}
			} catch (HibernateException e1) {
				e1.printStackTrace();
			}	    	
		}
		return ret;
	}
}
