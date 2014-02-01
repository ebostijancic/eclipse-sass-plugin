package at.workflow.webdesk.tools.cache;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import at.workflow.webdesk.tools.ClassUtils;
import at.workflow.webdesk.tools.api.PersistentObject;

/**
 * This is a utility class to re-associate Hibernate Objects to the current session
 * in case they were returned from the cache.
 * 
 * CAUTION: the name of this Spring bean is "PoCacheHibernateUtils", not "CacheHibernateUtils"!!!
 * 
 * @author ggruber
 */
public class CacheHibernateUtils {
	
	private SessionFactory sessionFactory;
	
	/**
	 * Freshly reads the persistent object from database (ID-cached via Hibernate),
	 * without checking if it is contained in current database session.
	 */
	public Object reloadObject(Object anObject) {
		return reloadObject(anObject, getSession());
	}
	
	/**
	 * Freshly reads the persistent object from database (ID-cached via Hibernate)
	 * when the given object is not contained in current database session.
	 */
	public Object reassociate(Object anObject) throws Exception {
		return reassociate(anObject, getSession());
	}
	
	public void clearSession()	{
		getSession().clear();
	}
	
	private Object reloadObject(Object object, Session session) {
		return session.get( ClassUtils.resolveProxyClass(object), ((PersistentObject) object).getUID());
	}
	
	
	/**
	 * Re-associates an object with a session so lazy loaded collections will still work.
	 * We do not use <code>session.lock(object)</code> because:
	 * <pre>
	 * This method might cause Hibernate exceptions like
	 * 'Illegal attempt to associate a collection with two open sessions'
	 * caused by 2 threads fighting for the same cached object and trying
	 * to associate it with their (different) Hibernate sessions.
	 * </pre> 
	 * Furthermore <code>lock()</code> does not work on Spring AOP proxy objects:
	 * <pre>
	 * AopUtils.isAopProxy(anObject) || anObject.getClass().getName().indexOf("$$") > 0
	 * </pre>
	 */
	private Object reassociate(Object anObject, Session aSession) throws Exception {
		if (aSession.contains(anObject) == false)
			anObject = reloadObject(anObject, aSession);
		return anObject;
	}

	private Session getSession() {
		return SessionFactoryUtils.getSession(sessionFactory, null, null);
	}
	
	
	/** Spring dependency-injection setter, do not use. */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public List<Object> reassociateList(final List<?> cachedList) throws Exception {
		Session session = getSession();
		// always return a clone of the POJO List, to *NOT* let callers modify the original one - this is important!
		final List<Object> newList = new ArrayList<Object>();
		for (Object o : cachedList)	{
			if (o instanceof PersistentObject)	{
				o = reassociate(o, session);
				if (o != null)	// fri_2012-11-08: detected nulls in cached POJO lists when data were changed without releasing caches!
					newList.add(o);
			}
			else	{
				newList.add(o);
			}
		}
		return newList;
	}

}
