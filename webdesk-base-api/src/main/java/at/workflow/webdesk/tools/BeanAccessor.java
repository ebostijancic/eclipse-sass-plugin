package at.workflow.webdesk.tools;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import at.workflow.webdesk.tools.BeanReflectUtil.BeanProperty;

/**
 * Generic access to properties of a Java class and property values in a Java object of that class.
 * A property has both getter and setter, and both must be public (see BeanReflectUtil docs).
 * Nested properties are addressed using dotted names, e.g. "person.address.city".
 * Indexed nested beans are addressed using an index in brackets, e.g. "person.addresses[3]" or "addresses[3].email".
 * Wildcards in property-expressions are not supported.
 * <p/>
 * Most important methods are <code>getValue()</code> and <code>setValue()</code>.
 * Both accept a property expression that can contain "." (nested access) and "[999]" (indexed access).
 * For convenience a bean can be set into this accessor, but this will be ignored when another bean is passed as parameter.
 * 
 * @author Fritz Ritzberger, May 11, 2013
 */
public class BeanAccessor
{
	/** The token that opens an index like e.g. persons[3]". */
	public static final String INDEX_BEGIN = "[";
	
	/** The token that closes an index like e.g. persons[3]". */
	public static final String INDEX_END = "]";
	
	/** The token that opens an index like e.g. persons[3]". */
	public static final String NESTING = ".";
	
	/**
	 * @param propertyExpression the property to append an index to.
	 * @param index the index to append to given property.
	 * @return the ready-made "xxx[999]".
	 */
	public static String buildIndexedExpression(String propertyExpression, int index)	{
		return propertyExpression+INDEX_BEGIN+index+INDEX_END;
	}
	
	/**
	 * @param propertyExpression the property to append a nested property to.
	 * @param nestedProperty the property to nest.
	 * @return the ready-made "xxx.yyy".
	 */
	public static String buildNestedExpression(String propertyExpression, String nestedProperty)	{
		return propertyExpression+NESTING+nestedProperty;
	}
	
	/**
	 * For "person.groups[3].name" this would return "person.groups", for "person.groups" null.
	 * @param propertyExpression the property expression to inspect for indexes.
	 * @return the first part before the index bracket when an index is contained, else null.
	 */
	public static String containsIndexing(String propertyExpression)	{
		final int openBracketIndex = propertyExpression.indexOf(INDEX_BEGIN);
		if (openBracketIndex < 0)
			return null;
		
		if (openBracketIndex == 0)
			throw new IllegalArgumentException("Illegal property expression starting with bracket: '"+propertyExpression+"'");
		
		return propertyExpression.substring(0, openBracketIndex);
	}
	
	
	/**
	 * For "person.groups[3].name" this would return [["person", null], "groups[3].name"], for "groups[3]" or "groups" null.
	 * For "groups[3].name" this would return [["groups", 3], "name"].
	 * @param propertyExpression the property expression to inspect for nestings.
	 * @return the first part before the first nesting dot when a dot is contained, else null.
	 */
	public static NameWithIndex containsNesting(String propertyExpression)	{
		final int dotIndex = propertyExpression.indexOf(NESTING);
		if (dotIndex == 0)
			throw new IllegalArgumentException("Property expression starting with dot: '"+propertyExpression+"'");
		
		if (dotIndex < 0)	// is not a dotted expression
			return null;
		
		return NameWithIndex.parse(propertyExpression.substring(0, dotIndex));
	}
	
	
	/** The class this accessor can access. */
	private final Class<?> beanClass;
	
	/** Optional bean for concrete value reading, must be of class beanClass. */
	private Object bean;

	/** Cached associations between (plain) property names and read/write accessors for that property. */
	private final Map<String,PropertyAccessor> propertyAccessors = new Hashtable<String,PropertyAccessor>();
	
	
	public BeanAccessor(Object bean) {
		this(bean, null, null);
	}
	
	public BeanAccessor(Object bean, String[] excludedPropertyNames, String[] includedPropertyNames) {
		this(bean.getClass(), excludedPropertyNames, includedPropertyNames);
		setBean(bean);
	}
	
	public BeanAccessor(Class<?> beanClass) {
		this(beanClass, null, null);
	}
	
	public BeanAccessor(Class<?> beanClass, String[] excludedPropertyNames, String[] includedPropertyNames) {
		if (beanClass == null)
			throw new IllegalArgumentException("Bean class must not be null!");
		
		this.beanClass = beanClass;
		introspect(excludedPropertyNames, includedPropertyNames);
	}
	
	/** @return the obtained bean class, never null. */
	public Class<?> getBeanClass()	{
		return beanClass;
	}
	
	/** @return the currently obtained bean, could be null. */
	public Object getBean()	{
		return bean;
	}
	
	/** Convenience to wrap a bean, or to use more than one bean with one BeanAccessor. */
	public final void setBean(Object bean)	{
		this.bean = bean;
	}

	/**
	 * @param propertyExpression the property to retrieve the type for, possibly containing dots.
	 * @return the type of the property at the end of given dotted expression.
	 */
	public Class<?> getType(String propertyExpression)	{
		return getMethodAccessorAndBean(null, propertyExpression).getType();
	}

	/**
	 * Mind that you must call setBean(bean) before calling this.
	 * @param propertyExpression the property to retrieve the value from, possibly containing dots.
	 * @return the value of the given property expression (possibly containing dots).
	 */
	public Object getValue(String propertyExpression)	{
		if (bean == null)
			throw new IllegalStateException("Bean to access is null, use getValue(bean, propertyExpression) or call setBean(bean) before");
		
		return getValue(getBean(), propertyExpression);
	}

	/**
	 * Mind that you must call setBean(bean) before calling this.
	 * Sets the value in the obtained bean.
	 * @param propertyExpression the property to retrieve the value from, possibly containing dots.
	 */
	public void setValue(String propertyExpression, Object value)	{
		if (bean == null)
			throw new IllegalStateException("Bean to access is null, use setValue(bean, propertyExpression) or call setBean(bean) before");
		
		setValue(getBean(), propertyExpression, value);
	}

	/**
	 * @param bean the bean to retrieve the property value from.
	 * @param propertyExpression the property to retrieve the value from, possibly containing dots.
	 * @return the value of the given property expression (possibly containing dots) in given bean.
	 */
	public Object getValue(Object bean, String propertyExpression)	{
		return getMethodAccessorAndBean(bean, propertyExpression).getValue();
	}

	/**
	 * Sets the value in the given bean.
	 * @param bean the bean to set the property value into.
	 * @param propertyExpression the property to set the value for, possibly containing dots.
	 */
	public void setValue(Object bean, String propertyExpression, Object value)	{
		getMethodAccessorAndBean(bean, propertyExpression).setValue(value);
	}
	
	/**
	 * Tests of the given property exists.
	 * @param propertyExpression the property to set the value for, possibly containing dots.
	 * @return true when property exists, else false.
	 */
	public boolean existsProperty(String propertyExpression)	{
		try	{
			getType(propertyExpression);
			return true;
		}
		catch (PropertyNotFoundException e)	{
			return false;
		}
	}

	/** @return all property names. */
	public List<String> getPropertyNames()	{
		return getPropertiesAssignableToTypes(null, true);
	}

	/**
	 * @param type the type that must be the class of the property, or one of its super-classes.
	 * @param positive when true, all properties assignable to the given type will be returned,
	 * 		when false, all properties NOT assignable to the given type will be returned.
	 * @return names of properties that are assignable to given type, never returns null.
	 */
	public List<String> getPropertiesAssignableToType(Class<?> type, boolean positive)	{
		assert type != null;
		return getPropertiesAssignableToTypes(new Class<?> [] { type }, positive);
	}

	/**
	 * @param types the types that must be the class of the property, or one of its super-classes.
	 * @param positive when true, all properties assignable to one of the given types will be returned,
	 * 		when false, all properties NOT assignable to any of the given types will be returned.
	 * @return names of properties that are assignable to given types, all properties when types are null or empty, never returns null.
	 */
	public List<String> getPropertiesAssignableToTypes(Class<?> [] types, boolean positive)	{
		final List<String> propertyNames = new ArrayList<String>();
		
		for (Map.Entry<String,PropertyAccessor> property : propertyAccessors.entrySet())	{
			final String propertyName = property.getKey();
			final PropertyAccessor methodAccessor = property.getValue();
			
			if (types == null || types.length <= 0)	{
				propertyNames.add(propertyName);
			}
			else	{
				boolean isAssignableToAtLeastOne = false;
				for (Class<?> type : types)
					if (ClassUtils.isAssignableFrom(type, methodAccessor.getter.getReturnType()))
						isAssignableToAtLeastOne = true;
			
				if (isAssignableToAtLeastOne == positive)
					propertyNames.add(propertyName);
			}
		}
		return propertyNames;
	}

	
	/** Scans properties of this bean class. */
	private void introspect(String[] excludedPropertyNames, String[] includedPropertyNames) {
		for (BeanProperty property : BeanReflectUtil.properties(beanClass, excludedPropertyNames, includedPropertyNames))	{
			propertyAccessors.put(property.propertyName, new PropertyAccessor(property.getter, property.setter));
		}
	}

	
	/** Resolves expressions like "person.address[3].city". */
	private BeanPropertyAccessor getMethodAccessorAndBean(final Object bean, final String propertyExpression) {
		assertArguments(bean, propertyExpression);
		
		final NameWithIndexAndNestings nameWithIndexAndNestings = NameWithIndexAndNestings.parse(propertyExpression);	// text before first "."
		final PropertyAccessor accessor = propertyAccessors.get(nameWithIndexAndNestings.nameWithIndex.name);
		if (accessor == null)
			throw new PropertyNotFoundException("Can not find PropertyAccessor for '"+nameWithIndexAndNestings.nameWithIndex.name+"', contained by "+beanClass);
		
		if (nameWithIndexAndNestings.nestings == null)	// is not a dotted expression
			return new BeanPropertyAccessor(accessor, bean, nameWithIndexAndNestings.nameWithIndex.index);
		
		// is a dotted expression
		final Object propertyValue;
		final Class<?> propertyClass;
		if (nameWithIndexAndNestings.nameWithIndex.index != null)	{	// must apply index
			if (bean == null)
				throw new IllegalStateException("Can not determine the class of Collection elements when no bean instance is present!");
			
			final BeanPropertyAccessor beanPropertyAccessor = new BeanPropertyAccessor(accessor, bean, nameWithIndexAndNestings.nameWithIndex.index);
			propertyValue = beanPropertyAccessor.getValue();
			propertyClass = propertyValue.getClass();
		}
		else	{
			propertyValue = (bean != null) ? accessor.getValue(bean) : null;
			propertyClass = accessor.getType();
		}
		
		return new BeanAccessor(propertyClass).getMethodAccessorAndBean(propertyValue, nameWithIndexAndNestings.nestings);
	}
	
	
	private void assertArguments(Object bean, String propertyExpression) {
		if (bean != null && ClassUtils.isAssignableFrom(beanClass, bean.getClass()) == false)
			throw new IllegalArgumentException("The given bean of "+bean.getClass()+" is not compatible with obtained "+beanClass);
		
		if (StringUtils.isBlank(propertyExpression))
			throw new IllegalArgumentException("Empty or null property expression!");
	}
	

	
	/**
	 * Calls reflection getters and setters,
	 * throwing RuntimeExceptions when exception is thrown from that.
	 */
	private static class PropertyAccessor
	{
		private final Method getter;
		private final Method setter;
		
		public PropertyAccessor(Method getter, Method setter)	{
			assert getter != null && setter != null;
			
			this.getter = getter;
			this.setter = setter;
		}
		
		public Class<?> getType()	{
			return getter.getReturnType();
		}
		
		public Object getValue(Object bean)	{
			try {
				return getter.invoke(bean, new Object[0]);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		void setValue(Object bean, Object value)	{
			try {
				setter.invoke(bean, new Object [] { value });
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	
	/**
	 * Can apply a given PropertyAyccessor to a given bean.
	 * Will resolve an indexed property to a Collection element when given index is not null.
	 */
	private static class BeanPropertyAccessor
	{
		public final PropertyAccessor propertyAccessor;
		public final Object bean;
		public final Integer index;
		
		/** When index is not null, the propertyAccessor must point to a Collection in bean. */
		public BeanPropertyAccessor(PropertyAccessor propertyAccessor, Object bean, Integer index) {
			assert propertyAccessor != null;
			assert index == null || index.intValue() >= 0 : "Invalid BeanAccessor index: "+index;
			
			this.propertyAccessor = propertyAccessor;
			this.bean = bean;	// could be null
			this.index = index;	// could be null
		}
		
		public Class<?> getType()	{
			return propertyAccessor.getType();
		}
		
		public Object getValue()	{
			final Object value = propertyAccessor.getValue(bean);
			if (index == null)
				return value;
			
			final Collection<?> collection = (Collection<?>) value;	// must throw ClassCastExpetion when indexing something that is not a Collection
			final Iterator<?> iterator = collection.iterator();
			for (int i = 0; i < index.intValue(); i++)
				iterator.next();
			
			return iterator.next();	// must throw NoSuchElementException when index was wrong
		}
		
		public void setValue(Object value)	{
			if (index == null)	{
				propertyAccessor.setValue(bean, value);
			}
			else	{
				throw new UnsupportedOperationException("Setting Collection elements is not supported: "+propertyAccessor.setter);
			}
		}
	}
	
	
	/**
	 * Encapsulates "persons[0].group.name": name will be ["persons", 0], nestings will be "group.name".
	 * When nestings is null, there was no dot in property-expression.
	 */
	public static class NameWithIndexAndNestings
	{
		/** Reads index between "[" and "]" when contained. */
		public static NameWithIndexAndNestings parse(String propertyExpression)	{
			final NameWithIndex firstNestingPart = containsNesting(propertyExpression);
			if (firstNestingPart == null)	// no brackets contained in expression
				return new NameWithIndexAndNestings(NameWithIndex.parse(propertyExpression), null);
			
			final String nestings = propertyExpression.substring(propertyExpression.indexOf(NESTING) + 1);
			return new NameWithIndexAndNestings(firstNestingPart, nestings);
		}
		
		public final NameWithIndex nameWithIndex;
		public final String nestings;
		
		public NameWithIndexAndNestings(NameWithIndex nameWithIndex, String nestings) {
			assert nameWithIndex != null && StringUtils.isBlank(nameWithIndex.name) == false;
			
			this.nameWithIndex = nameWithIndex;
			this.nestings = nestings;
		}
	}
	
	
	/**
	 * Encapsulates "group.persons[4]": name will be "group.persons", index will be 4.
	 * When index is null, there was no bracket in property-expression.
	 */
	public static class NameWithIndex
	{
		/**
		 * For "person.groups[3].name" this would return ["person.groups", 3].
		 */
		public static NameWithIndex parse(String propertyExpression)	{
			final String firstIndexingPart = containsIndexing(propertyExpression);
			if (firstIndexingPart == null)	// no brackets contained in expression
				return new NameWithIndex(propertyExpression, null);
			
			final String indexString = propertyExpression.substring(firstIndexingPart.length() + INDEX_BEGIN.length()).trim();
			final int closeBracketIndex = indexString.indexOf(INDEX_END);
			if (closeBracketIndex < 0)
				throw new IllegalArgumentException("Illegal property expression without closing bracket: '"+propertyExpression+"'");
			if (closeBracketIndex == 0)
				throw new IllegalArgumentException("Illegal property expression without index: '"+propertyExpression+"'");
			
			final Integer index = Integer.valueOf(indexString.substring(0, closeBracketIndex));	// must throw NumberFormatException when not numeric
			return new NameWithIndex(firstIndexingPart, index);
		}
		
		public final String name;
		public final Integer index;
		
		public NameWithIndex(String name, Integer index) {
			this.name = name;
			this.index = index;
		}
	}
	
	
	
	private static class PropertyNotFoundException extends IllegalArgumentException
	{
		public PropertyNotFoundException(String message) {
			super(message);
		}
	}
}
