package at.workflow.webdesk.tools.testing;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Random;

import org.apache.log4j.Logger;

import at.workflow.webdesk.tools.BeanReflectUtil.BeanProperty;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.dbmetadata.PersistenceMetadata;
import at.workflow.webdesk.tools.dbmetadata.PersistenceMetadataHibernate;

/**
 * The basic unit test for DAO tests, generating test data itself.
 * When data generation is insufficient, overrides can be used to specially treat single fields.
 * Generates values for all fields of a bean class and prepares them as test values.
 * 
 * Leaves out nullable references to other objects.
 * This behaviour is overridable by shouldLeaveOutWhenNullable().
 * When not nullable, creates the reference and also references of that (recursive).
 * Does not fill backlinks. saves the reference first, then the referencer.
 * 
 * @author sdzuban
 * @author fritzberger
 * 
 * @param <B> the database POJO to test.
 */
public abstract class AbstractRandomDataOnlyProvidingTestCase<B extends PersistentObject> extends AbstractCrudTestCase<B> {

	private static final Logger logger = Logger.getLogger(AbstractRandomDataOnlyProvidingTestCase.class);
	
	private Random random = new Random();
	protected final PersistenceMetadata metadata = new PersistenceMetadataHibernate();

	/**
	 * Implements data providing by generating random values for all class properties
	 * that have getter/setters. This even creates relation objects, but only the first level.
	 * If more levels are needed, <code>provideValueForField</code> must be overridden.
	 */
	@Override
	protected final void provideInsertData(B bean)	{
		provideData(bean);
	}
	
	/**
	 * This default implementation delegates to <code>provideInsertData(bean)</code>,
	 * because that method will create random data that differ each time it is called.
	 * Override for specific update-data.
	 */
	@Override
	protected final void provideUpdateData(B bean)	{
		provideData(bean);
	}
	
	/**
	 * Calling this method will return a string that is not longer than the according database field.
	 * @param parentType the Class of the persistence class.
	 * @param propertyName the name of the field in persistence class.
	 * @return a random instance value of the length the property of passed persistence class demands.
	 */
	protected final Object getRandomStringValue(Class<? extends PersistentObject> parentType, String propertyName) {
		return getRandomValue(parentType, propertyName, String.class);
	}
	
	/**
	 * Callers cast the returned object to the type they intended by <code>type</code> parameter.
	 * @param type the Class of the value to generate.
	 * @return a random instance value of the passed class.
	 */
	protected final Object getRandomValue(Class<?> type) {
		return getRandomValue(null, null, type);
	}
	
	/**
	 * Fills passed bean with random values. Does not save the bean.
	 * Override <code>getRandomReference()</code> to create references to other beans. 
	 * @param bean the bean to fill with data.
	 */
	protected final void provideData(PersistentObject bean)	{
		Class<? extends PersistentObject> persistentClass = bean.getClass();
		logger.debug("Generating data for an instance of "+persistentClass);
		
		// add this to cache if cycles occur
		cache(persistentClass, bean);
		
		for (BeanProperty property : properties(persistentClass))	{
			boolean ignore = false;
			if (metadata.isMapped(persistentClass, property.propertyName) == false)	{
				ignore = true;
			}
			else if (isIgnored(property.propertyName))	{
				if (metadata.isNullable(persistentClass, property.propertyName) == false)
					throw new IllegalArgumentException("Can not ignore the not-null property "+property.propertyName+" in "+persistentClass);
				ignore = true;
			}
			
			if (ignore == false)	{
				final Object testValue = getRandomValue(persistentClass, property.propertyName, property.getter.getReturnType());
				if (testValue == null)
					continue;
				
				try	{
					logger.debug("  Setting field '"+property.propertyName+"' to generated value >"+testValue+"< in "+bean);
					property.setter.invoke(bean, new Object [] { testValue });
				}
				catch (IllegalArgumentException e)	{
					handleReflectionException(e);
				}
				catch (InvocationTargetException e)	{
					handleReflectionException(e);
				}
				catch (IllegalAccessException e)	{
					handleReflectionException(e);
				}
			}
		}
	}
	
	/**
	 * Creates a random data value for a given property.
	 * @param parentClass the class containing the field for which a value should be created. 
	 * @param propertyName the property's name in parentClass.
	 * @param propertyClass the class of the property, returned value must be an instance of that class.
	 * @return a random value for given member field of parentClass.
	 */
	protected Object getRandomValue(Class<? extends PersistentObject> parentClass, String propertyName, Class<?> propertyClass) {
		// when creating a Date value, take care that it is not the same as before
		if (Date.class.isAssignableFrom(propertyClass))	// all Dates and Timestamp go here
			try { Thread.sleep(1000); } catch (InterruptedException e1) {	}

		if (propertyClass.isArray())	{
			Class<?> componentType = propertyClass.getComponentType();
			final int LENGTH = 3;
			final Object array = Array.newInstance(componentType, LENGTH);
			for (int i = 0; i < LENGTH; i++)	{
				Object value = getRandomValue(null, null, componentType);
				Array.set(array, i, value);
			}
			return array;
		}
	
		// TODO this should be in ClassUtils
		if (propertyClass.equals(boolean.class) || propertyClass.equals(Boolean.class))	// works due to auto-boxing
			return Boolean.valueOf(random.nextBoolean());
		else if (propertyClass.equals(byte.class) || propertyClass.equals(Byte.class))
			return Byte.valueOf((byte) random.nextInt(255));
		else if (propertyClass.equals(char.class) || propertyClass.equals(Character.class))
			return Character.valueOf((char) random.nextInt(255));
		else if (propertyClass.equals(short.class) || propertyClass.equals(Short.class))
			return Short.valueOf((short) random.nextInt(60000));
		else if (propertyClass.equals(int.class) || propertyClass.equals(Integer.class))
			return Integer.valueOf(random.nextInt());
		else if (propertyClass.equals(long.class) || propertyClass.equals(Long.class))
			return Long.valueOf(random.nextLong());
		else if (propertyClass.equals(float.class) || propertyClass.equals(Float.class))
			return Float.valueOf(random.nextFloat());
		else if (propertyClass.equals(double.class) || propertyClass.equals(Double.class))
			return Double.valueOf(random.nextDouble());
		else if (propertyClass.equals(BigInteger.class))
			return BigInteger.valueOf(random.nextLong());
		else if (propertyClass.equals(BigDecimal.class))
			return BigDecimal.valueOf(random.nextDouble());
		else if (propertyClass.equals(String.class))
			return randomString(parentClass, propertyName);
		else if (propertyClass.equals(Timestamp.class))	// Date goes below with propertyClass.newInstance()
			return new Timestamp(new Date().getTime());
		else if (propertyClass.isEnum()) // is enum
			return getFirstEnumMember(propertyClass);
		else if (Collection.class.isAssignableFrom(propertyClass))
			throw new IllegalArgumentException("not implemented: Collection class (is this a back-reference?)");
		else	{	// Date and all non-primitive types go here
			try	{
				if (PersistentObject.class.isAssignableFrom(propertyClass) == false) // is NOT a relation object (e.g. Date)
					return propertyClass.newInstance();
				
				return getRandomReference(parentClass, propertyName, propertyClass);
			}
			catch (InstantiationException e)	{
				handleReflectionException(e);
			}
			catch (IllegalAccessException e) {
				handleReflectionException(e);
			}
		}
		return null;
	}


	/** This always returns null. To be overridden by reference-managing class. */
	@SuppressWarnings("unused")
	protected Object getRandomReference(Class<? extends PersistentObject> parentClass, String propertyName, Class<?> propertyClass)	{
		return null;
	}
	
	/** This implementation does not cache at all. To be overridden by reference-managing class. */
	@SuppressWarnings("unused")
	protected Object cache(Class<?> clazz, Object o)	{
		return o;
	}

	/** Simple exception management by e.printStackTrace(). */
	protected final void handleReflectionException(Exception e) {
		e.printStackTrace();
	}

	private Object randomString(Class<? extends PersistentObject> persistentClass, String propertyName) {
		String s = "s" + Math.abs(random.nextInt());
		if (persistentClass != null && propertyName != null)	{
			// check if it is longer than allowed
			int length = metadata.getMaximumLength(persistentClass, propertyName);
			if (length > 0 && s.length() > length)
				s = s.substring(0, length);
		}
		return s;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object getFirstEnumMember(Class<?> propertyClass) {
		final Field firstField = propertyClass.getFields()[0];
		final String value = firstField.getName();
		return Enum.valueOf((Class<Enum>) propertyClass, value);
	}

}
