package at.workflow.webdesk.tools.cache;

import java.beans.PropertyEditor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.CacheManager;

import org.apache.log4j.Logger;
import org.springmodules.cache.CacheException;
import org.springmodules.cache.CachingModel;
import org.springmodules.cache.FlushingModel;
import org.springmodules.cache.provider.CacheModelValidator;
import org.springmodules.cache.provider.CacheProviderFacade;
import org.springmodules.cache.provider.ehcache.EhCacheFacade;

import at.workflow.webdesk.tools.api.PersistentObject;

/**
 * Custom EhCache Facade to avoid LazyInitializationException.
 * This should also refresh the objects' data contents.
 * 
 * @author ggruber
 */
public class CustomEhCacheFacade implements CacheProviderFacade {
	
	private static final Logger logger = Logger.getLogger(CustomEhCacheFacade.class); 

	private EhCacheFacade ehCacheFacade;
	private CacheHibernateUtils cacheHibernateUtils;

	
	public CustomEhCacheFacade() {
		ehCacheFacade = new EhCacheFacade();
	}

	/**
	 * Implemented to detect Lists containing (stateful) Hibernate POJOs,
	 * and re-attach them to current Session. This is to avoid LazyInitializationException.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "rawtypes" })
	public Object getFromCache(Serializable cacheKey, CachingModel cachingModel) throws CacheException {
		
		Object cachedObject = ehCacheFacade.getFromCache(cacheKey, cachingModel);
		if (cacheHibernateUtils == null || isPersistentObject(cachedObject) == false)	// not yet initialized, or nothing to do
			return cachedObject;

		try {
			// re-attach any cached Hibernate POJO to current session
			if (cachedObject instanceof List) {	// TODO: shouldn't we extend this to Collection to also cover Sets?
				final List cachedList = (List) cachedObject;
				cachedObject = cacheHibernateUtils.reassociateList(cachedList);

			} else {
				cachedObject = cacheHibernateUtils.reassociate(cachedObject);
			}
		} catch (Exception e) {
			logger.error("Couldn't reassociate a session with this Hibernate POJO: " + cachedObject, e);
		}

		
		return cachedObject;
	}

	
	private boolean isPersistentObject(Object cachedObject) {
		if (cachedObject == null)
			return false;

		if (cachedObject instanceof PersistentObject)
			return true;

		if (cachedObject instanceof List<?>)	{
			List<?> list = (List<?>) cachedObject;
			for (Object o : list)
				if (o instanceof PersistentObject)
					return true;
		}
		return false;
	}

	
	public void setCacheManager(CacheManager newCacheManager) {
		ehCacheFacade.setCacheManager(newCacheManager);
	}

	/** just wrappers **/
	@Override
	public void cancelCacheUpdate(Serializable arg0) throws CacheException {
		ehCacheFacade.cancelCacheUpdate(arg0);
	}

	@Override
	public void flushCache(FlushingModel arg0) throws CacheException {
		ehCacheFacade.flushCache(arg0);
	}

	@Override
	public PropertyEditor getCachingModelEditor() {
		return ehCacheFacade.getCachingModelEditor();
	}

	@Override
	public PropertyEditor getFlushingModelEditor() {
		return ehCacheFacade.getFlushingModelEditor();
	}

	@Override
	public boolean isFailQuietlyEnabled() {
		return ehCacheFacade.isFailQuietlyEnabled();
	}

	@Override
	public CacheModelValidator modelValidator() {
		return ehCacheFacade.modelValidator();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void putInCache(Serializable cacheKey, CachingModel cachingModel, Object value) throws CacheException {
		if (value instanceof List && isPersistentObject(value))	{
			// always store a clone of the original List (that will be returned to caller, and possibly will be modified then)
			value = new ArrayList((List) value);
		}
		ehCacheFacade.putInCache(cacheKey, cachingModel, value);
	}

	@Override
	public void removeFromCache(Serializable arg0, CachingModel arg1) throws CacheException {
		ehCacheFacade.removeFromCache(arg0, arg1);
	}

	/** TODO this is from InitializingBean, but we do not implement this. */
	public void afterPropertiesSet() throws Exception {
		ehCacheFacade.afterPropertiesSet();
	}

	
	/** Spring dependency injection method. */
	public void setCacheHibernateUtils(CacheHibernateUtils cacheHibernateUtils) {
		this.cacheHibernateUtils = cacheHibernateUtils;
	}

}
