package at.workflow.webdesk.tools.testing;

import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.tools.api.PersistentObject;

/**
 * This derivation additionally provides random-generated reference objects.
 * Normally it leaves out nullable references to other objects, but this behaviour
 * is overridable via <code>shouldLeaveOutWhenNullable()</code>.
 * When a reference is not nullable, this class creates a referenced bean and also the
 * references of that created bean (recursively). Does not fill <i>backlink</i> Collections.
 * Every object is saved separately. Saves the reference first, then the referencer.
 * Does NOT save if <code>shouldSaveReference()</code> is overwritten and returns false
 * (needed for CASCADE tests).
 * 
 * @author fritzberger 2011-10-04
 * 
 * @param <B> the database POJO to test.
 */
public abstract class AbstractRandomDataAndReferencesProvidingTestCase<B extends PersistentObject> extends AbstractRandomDataOnlyProvidingTestCase<B> {

	private static final Logger logger = Logger.getLogger(AbstractRandomDataAndReferencesProvidingTestCase.class);

	private Map<String, PersistentObject> cache = new Hashtable<String, PersistentObject>();

	/**
	 * Overridden to also fill references with random values.
	 * This also covers self-references.
	 */
	@Override
	protected final Object getRandomReference(Class<? extends PersistentObject> parentClass, String propertyName, Class<?> propertyClass)	{
		try {
			// avoid long work by leaving out nullable properties
			if (metadata.isNullable(parentClass, propertyName) && shouldLeaveOutWhenNullable(parentClass, propertyName))
				return null;

			PersistentObject o = (PersistentObject) propertyClass.newInstance();
			Object cached = cache(propertyClass, o);
			// re-use entities cached per persistenceClass name, this also avoids cyclic references
			if (reuseCachedPropertyObjects() && cached != o)
				return cached;	// there was a cached object of that class

			logger.info("    Instantiated object for reference '" + propertyName + "' in " + parentClass + ", the field is " + propertyClass);

			provideData(o);
			logger.info("    Provided test values for reference '" + propertyName + "': " + o);

			if (shouldSaveReference(o))	{
				save(o);
				logger.info("    Saved target object for reference '" + propertyName + "'");
			}

			return o;
		}
		catch (InstantiationException e)	{
			handleReflectionException(e);
		}
		catch (IllegalAccessException e) {
			handleReflectionException(e);
		}
		return null;
	}

	/** Can the referenced object be non-unique? */
	protected boolean reuseCachedPropertyObjects() {
		return true;
	}

	/**
	 * This implementation always returns true. Override to avoid saving of reference objects.
	 * @param toSave the object about to be saved.
	 * @return true when any created reference object also should be saved to persistence transaction.
	 */
	protected boolean shouldSaveReference(Object toSave) {
		return true;
	}

	/**
	 * Saves the passed bean to persistence using DAO naming conventions
	 * (when this is not sufficient please override <i>getDaoName</i>).
	 * This is accessible (<code>protected</code>) for any subclass that creates data itself.
	 * @param bean the bean to save.
	 */
	@SuppressWarnings("unchecked")
	protected final void save(Object bean)	{
		assert bean != null;
		@SuppressWarnings("rawtypes")
		GenericDAO dao = getDao(bean.getClass());
		dao.save(bean);
	}

	/** @return the DAO belonging to passed persistence class (POJO). */
	@SuppressWarnings("rawtypes")
	protected final GenericDAO getDao(Class<?> beanClass) {
		GenericDAO dao = (GenericDAO) applicationContext.getBean(getDaoName(beanClass));
		return dao;
	}
	
	/**
	 * Returns whether the passed nullable property should NOT be filled with data,
	 * so a return of true would let it be null. This is to be overridden for
	 * properties that are nullable but the implementation demands a non-null value.
	 * @param parentType the class of the parent object that holds the named property.
	 * @param propertyName the name of the property to fill with data.
	 * @return true, to be overridden.
	 */
	protected boolean shouldLeaveOutWhenNullable(Class<? extends PersistentObject> parentType, String propertyName) {
		return true;
	}

	/**
	 * As this default implementation returns true, all generated PersistentObject
	 * instances are cached via class-name. If this should not happen for a certain
	 * class this can be prevented here by returning false.
	 * Caching instances prevents endless recursion loops and saves memory by reusing
	 * test instances.
	 * @param clazz the class about to be used as cache key.
	 * @return true when instances of passed class should be cached, else false.
	 */
	protected boolean shouldCache(Class<?> clazz) {
		return true;
	}
	
	/**
	 * Caches and returns the new object when not found, else returns the cached one.
	 * Implements a simple by-classname caching.
	 * This means every referenced top-level POJO class will have just ONE instance!
	 */
	@Override
	protected final Object cache(Class<?> clazz, Object o)	{
		if (shouldCache(clazz) == false)
			return o;
		
		final String cacheKey = clazz.getName();
		Object cached = cache.get(cacheKey);
		if (cached != null)
			return cached;
		
		cache.put(cacheKey, (PersistentObject) o);
		return o;
	}

}
