package at.workflow.webdesk.tools;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reflection utilities in conjunction with bean properties that
 * <ul>
 * 	<li>have both getters and setters</li>
 * 	<li>which are both public</li>
 * </ul>
 * That means, a property would not be found if it has just a getter and no setter,
 * or one of them is not public. This does not scan fields (uses only method access).
 * <p/>
 * Mind that the check for both public getter and setter is important to ignore
 * transient getters in Hibernate POJOs, e.g. PoPerson.getFullName()!
 * 
 * @author fritzberger 14.10.2010
 */
public final class BeanReflectUtil {

	/**
	 * Generic Java property. Does not contain or support dotted names.
	 */
	public static class BeanProperty
	{
		/** Name of the property, with lower-case first letter, ready to be used for property hash-maps. */
		public final String propertyName;
		/** The getter method of the bean property. */
		public final Method getter;
		/** The setter method of the bean property. */
		public final Method setter;
		/** The class containing this property. */
		public final Class<?> containingClass;
		/** The class of this property. */
		public final Class<?> propertyClass;
		
		private BeanProperty(Method getter, Method setter, String BaseName, Class<?> clazz, Class<?> propertyClass) {
			this.getter = getter;
			this.setter = setter;
			this.propertyName = isAllUpperCase(BaseName) ? BaseName : ReflectionUtils.getFirstCharLowerCasePropertyName(BaseName);
			this.containingClass = clazz;
			this.propertyClass = propertyClass;
			
			setter.setAccessible(true);
		}
		
		@Override
		public boolean equals(Object o) {
			BeanProperty other = (BeanProperty) o;
			return containingClass.equals(other.containingClass) && propertyName.equals(other.propertyName);
		}
		
		@Override
		public int hashCode() {
			return propertyName.hashCode() + containingClass.hashCode();
		}
		
		@Override
		public String toString()	{
			return containingClass.getSimpleName()+"."+propertyName;
		}
		
		private boolean isAllUpperCase(String s)	{
			if (s.length() == 1)
				return false;	// do not take "B" as property "B", let it be "b"
			
			for (int i = 0; i < s.length(); i++)
				if (Character.isLowerCase(s.charAt(i)))
					return false;
			
			return true;	// do not take "UID" as "uID", let it be "UID"
		}
	}
	
	
	/**
	 * Returns the list of "properties" (see class comment) of the bean class.
	 * @param beanClass the class to search for properties.
	 * @return list of properties of the bean class.
	 */
	public static List<BeanProperty> properties(Class<?> beanClass)	{
		return properties(beanClass, null, null);
	}
	
	/**
	 * @return the property with given name in given class, or null when no such property.
	 */
	public static BeanProperty property(String propertyName, Class<?> beanClass)	{
		if (propertyName.contains("."))
			throw new IllegalArgumentException("Can not resolve dotted property names: "+propertyName);
		
		final List<BeanProperty> propertiesOfThatName = properties(beanClass, null, new String [] { propertyName });
		assert propertiesOfThatName.size() <= 1 : "Having more than one property of name '"+propertyName+"': "+propertiesOfThatName;
		
		if (propertiesOfThatName.size() <= 0)
			return null;
		
		return propertiesOfThatName.get(0);
	}
	
	/**
	 * Returns the list of "properties" (see class comment) of the bean class.
	 * @param beanClass the class to search for properties.
	 * @param excludedPropertyNames properties that should not be contained in returned list.
	 * @param includedPropertyNames of properties that should be exclusively contained in returned list.
	 * @return list of all BeanProperties of the given class.
	 */
	public static List<BeanProperty> properties(Class<?> beanClass, String [] excludedPropertyNames, String [] includedPropertyNames)	{
		List<BeanProperty> list = new ArrayList<BeanProperty>();
		excludedPropertyNames = makeFirstCharLowerCase(excludedPropertyNames);
		includedPropertyNames = makeFirstCharLowerCase(includedPropertyNames);
		
		// seek all getters
		for (Method method : beanClass.getMethods())	{
			final String methodName = method.getName();
			final Class<?> [] parameterClasses = method.getParameterTypes();
			final Class<?> returnClass = method.getReturnType();
			final String BaseName = ReflectionUtils.getterBasenameFirstCharUpperCase(methodName);
			
			// when it is a legal getter
			if (BaseName != null &&
					(excludedPropertyNames == null || isAmong(excludedPropertyNames, BaseName) == false) &&
					(includedPropertyNames == null || isAmong(includedPropertyNames, BaseName) == true) &&
					parameterClasses.length == 0 &&
					Void.TYPE.equals(returnClass) == false &&
					(method.getModifiers() & Modifier.PUBLIC) != 0)
			{
				// seek the setter
				final Method setter = findMethod(beanClass, "set"+BaseName, new Class<?> [] { returnClass });
				if (setter != null)	// when found, add this property
					list.add(new BeanProperty(method, setter, BaseName, beanClass, returnClass));
			}
		}
		
		return list;
	}

	/**
	 * Resolves any dot (".") contained in propertyName. 
	 * @param beanClass the class that owns the given property.
	 * @param propertyExpression the property expression to resolve, can contain dots (".") to address referenced objects, e.g. "person.lastName".
	 * @return the class that really owns the given propertyName expression, for example
	 * 		<code>getOwnerBeanClass(HrPerson.class, "person.lastName")</code> would be resolved to <code>PoPerson.class</code>.
	 */
	public static Class<?> getOwnerBeanClass(Class<?> beanClass, String propertyExpression)	{
		final String [] firstAndRemainder = TextUtils.getFirstAndNextPart(propertyExpression);
		if (firstAndRemainder.length <= 1 || firstAndRemainder[1] == null)
			return beanClass;
		
		final BeanProperty property = BeanReflectUtil.property(firstAndRemainder[0], beanClass);
		return getOwnerBeanClass(property.propertyClass, firstAndRemainder[1]);
	}

	/**
	 * Resolves any dot (".") contained in propertyName. 
	 * @param bean the object that holds the given property.
	 * @param propertyExpression the property expression to resolve, can contain dots (".") to address referenced objects, e.g. "person.lastName".
	 * @return the object that really owns the given propertyName expression, for example
	 * 		<code>getOwnerBeanObject(hrPerson, "person.lastName")</code> would resolve to <code>hrPerson.getPerson()</code>.
	 */
	public static Object getOwnerBeanObject(Object bean, String propertyExpression)	{
		final String [] firstAndRemainder = TextUtils.getFirstAndNextPart(propertyExpression);
		if (firstAndRemainder.length <= 1 || firstAndRemainder[1] == null)
			return bean;
		
		final BeanProperty property = BeanReflectUtil.property(firstAndRemainder[0], bean.getClass());
		Object value;
		try {
			value = property.getter.invoke(bean, new Object[0]);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return getOwnerBeanObject(value, firstAndRemainder[1]);
	}

	/**
	 * Resolves any dot (".") contained in propertyName. 
	 * @param bean the object that holds the given property.
	 * @param propertyExpression the property expression to resolve, can contain dots (".") to address referenced objects, e.g. "person.lastName".
	 * @return the value of given property.
	 */
	public static Object getOwnerBeanPropertyValue(Object bean, String propertyExpression)	{
		final Object ownerBean = getOwnerBeanObject(bean, propertyExpression);
		final String rightMostPropertyName = TextUtils.getRightmostPropertyName(propertyExpression);
		final Class<?> beanClass = ClassUtils.resolveProxyClass(ownerBean);
		final BeanProperty property = BeanReflectUtil.property(rightMostPropertyName, beanClass);
		try {
			return property.getter.invoke(ownerBean, new Object[0]);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return the field with given name in given class, searching in all super-classes,
	 * 		or null if no such field has been found.
	 * 		Property names like "person.lastName" are allowed.
	 */
	public static Field getField(Class<?> clazz, String propertyExpression)	{
		clazz = getOwnerBeanClass(clazz, propertyExpression);
		final String propertyName = TextUtils.getRightmostPropertyName(propertyExpression);
		
		// search ALL fields in class hierarchy for annotated fields
		while (clazz != null && clazz.equals(Object.class) == false)	{
			try	{
				return clazz.getDeclaredField(propertyName);
			}
			catch (NoSuchFieldException e)	{
				clazz = clazz.getSuperclass();
			}
		}
		return null;
	}
	
	
	
	private static String[] makeFirstCharLowerCase(String[] propertyNames) {
		if (propertyNames == null || propertyNames.length <= 0)
			return null;
		
		String [] firstCharLowerCase = new String[propertyNames.length];
		for (int i = 0; i < propertyNames.length; i++)
			firstCharLowerCase[i] = ReflectionUtils.getFirstCharLowerCasePropertyName(propertyNames[i]);
		
		return firstCharLowerCase;
	}

	/** Considers first upper case char of given name and does an adequate comparison. */
	private static boolean isAmong(String [] propertyNames, final String BaseName) {
		String baseName = ReflectionUtils.getFirstCharLowerCasePropertyName(BaseName);
		return Arrays.asList(propertyNames).contains(baseName);
	}
	
	private static Method findMethod(Class<?> beanClass, String methodName, Class<?> [] parameterClasses)	{
		for (Method method : beanClass.getMethods())	{
			final String name = method.getName();
			final Class<?> [] thisParameterClasses = method.getParameterTypes();
			
			if (name.equals(methodName) &&
					(method.getModifiers() & Modifier.PUBLIC) != 0 &&
					Void.TYPE.equals(method.getReturnType()) &&
					areCompatibleParameters(parameterClasses, thisParameterClasses))
				return method;
		}
		return null;
	}

	private static boolean areCompatibleParameters(Class<?>[] parameterClasses1, Class<?>[] parameterClasses2) {
		if (parameterClasses1 == parameterClasses2)
			return true;
		
		if (parameterClasses1 == null || parameterClasses2 == null || parameterClasses1.length != parameterClasses2.length)
			return false;
		
		for (int i = 0; i < parameterClasses1.length; i++)
			if (ClassUtils.isCompatible(parameterClasses1[i], parameterClasses2[i]) == false)
				return false;
		
		return true;
	}

	
	private BeanReflectUtil()	{}	// do not instantiate this class
}
