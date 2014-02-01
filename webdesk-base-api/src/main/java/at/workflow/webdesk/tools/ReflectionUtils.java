package at.workflow.webdesk.tools;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanUtils;

import at.workflow.webdesk.tools.api.PersistentObject;

/**
 * helper class to quickly discover the methodnames of a given class
 * 
 * @author ggruber
 */
public class ReflectionUtils {

	/**
	 * Collects all the interfaces implemented either by the class directly or by its superclasses
	 * @param className
	 * @return interfaces implemented by the class directly or by its superclasses
	 */
	@SuppressWarnings("rawtypes")
	public static List<Class> getAllImplementedInterfaces(String className) {
		// set to prevent repetition of any interface
		Set<Class> interfaces = new HashSet<Class>();
		try {
			Class<?> myClass = Class.forName(className);
			interfaces.addAll(Arrays.asList(myClass.getInterfaces()));
			Class<?> superClass = myClass.getSuperclass();
			while (superClass != null) {
				interfaces.addAll(Arrays.asList(superClass.getInterfaces()));
				superClass = superClass.getSuperclass();
			}
			return new ArrayList<Class>(interfaces);
		}
		catch (Exception e) {
			throw new RuntimeException("Exception while getting all interfaces of class " + className + "; " + e, e);
		}
	}

	/**
	 * Calls <code>clazz.getMethods()</code> which returns also methods of all super-classes.
	 * This is used in <code>JavaActionHandlerAdapter.js JavaScript only.</code>.
	 * Mind that this also returns all wait() implementations,
	 * and toString(), hashCode(), equals(), getClass(), notify(), notifyAll().
	 */
	public static List<String> getAllMethodNames(Class<?> clazz) {
		List<String> ret = new ArrayList<String>();
		Method[] methods = clazz.getMethods();
		for (int i = 0; i < methods.length; i++) {
			ret.add(methods[i].getName());
		}
		return ret;
	}

	/** Scans for getXXX or isXXX methods that return "primitive types" or Strings, and returns their property names. */
	public static List<String> getAllPrimitiveAndStringProperties(Class<?> clazz) {
		return getAllStringAndPrimitiveProperties(clazz, true);
	}

	/** Scans for getXXX methods that return Strings, and returns their property names. */
	public static List<String> getAllStringProperties(Class<?> clazz) {
		return getAllStringAndPrimitiveProperties(clazz, false);
	}

	private static List<String> getAllStringAndPrimitiveProperties(Class<?> clazz, boolean alsoPrimitives) {
		List<String> ret = new ArrayList<String>();
		Method[] methods = clazz.getMethods();

		for (int i = 0; i < methods.length; i++) {

			boolean isTypeOK = alsoPrimitives ?
					ClassUtils.isPrimitive(methods[i].getReturnType()) :
					String.class == methods[i].getReturnType();
			if (methods[i].getParameterTypes().length == 0 &&
					isGetter(methods[i].getName()) && isTypeOK)
			{
				String propName = removeGetOrIsPrefix(methods[i]);
				ret.add(getFirstCharLowerCasePropertyName(propName));
			}
		}
		return ret;
	}

	/** @return the name of the passed method, after removing any leading "get" or "is" prefix when one was found. */
	public static String removeGetOrIsPrefix(Method method) {
		final String BaseName = getterBasenameFirstCharUpperCase(method.getName());
		return (BaseName != null) ? BaseName : method.getName();
	}

	/** @return the BaseName of the given method when it starts with "get" or "is" ("getFirstName" -> "FirstName"), else null. */
	public static String getterBasenameFirstCharUpperCase(String methodName) {
		if (methodName.startsWith("get"))
			return methodName.substring("get".length());

		if (methodName.startsWith("is"))
			return methodName.substring("is".length());

		return null;
	}

	/** @return the BaseName of the given method when it starts with "get" or "is" ("getFirstName" -> "FirstName"), else null. */
	public static String getterBasenameFirstCharLowerCase(String methodName) {
		final String BaseName = getterBasenameFirstCharUpperCase(methodName);
		return (BaseName != null) ? getFirstCharLowerCasePropertyName(BaseName) : methodName;
	}

	/**
	 * Scans for getXXX or isXXX methods that return "collections" (see ClassUtils.isCollection).
	 */
	public static List<String> getAllCollectionProperties(Class<?> clazz) {
		return doGetAllProperties(clazz, ChooseMode.COLLECTIONS, Arrays.asList(new Class<?>[] {}));
	}

	/**
	 * TODO: rename this to getAllBeanReferenceProperties().
	 * Scans for getXXX or isXXX methods that return "bean references" (see ClassUtils.isBeanReference) or primitive types.
	 */
	public static List<String> getAllBeanProperties(Class<?> clazz) {
		return doGetAllProperties(clazz, ChooseMode.BEANS, Arrays.asList(new Class<?>[] {}));
	}

	/**
	 * Scans for getXXX or isXXX methods that return "standard types" (see ClassUtils.isStandardType) or primitive types.
	 */
	public static List<String> getAllStandardProperties(Class<?> clazz) {
		return getAllStandardProperties(clazz, Arrays.asList(new Class<?>[] {}));
	}

	/**
	 * Scans for getXXX or isXXX methods that return "standard types" (see ClassUtils.isStandardType),
	 * "custom types" (list can be passed as parameter) or primitive types.
	 */
	public static List<String> getAllStandardProperties(Class<?> clazz, List<Class<?>> customTypes) {
		return doGetAllProperties(clazz, ChooseMode.STANDARD, customTypes);
	}

	private enum ChooseMode {
		/** choose only standard properties (primitives and their Object Wrappers and String, Date) */
		STANDARD,
		/** choose only Bean references (excluding Collection types) */
		BEANS,
		COLLECTIONS
	}

	/**
	 * Scans for getXXX or isXXX methods that return "standard types" (ClassUtils.isStandardType),
	 * "custom types" (list can be passed as parameter) or primitive types.
	 */
	private static List<String> doGetAllProperties(Class<?> clazz, ChooseMode mode, List<Class<?>> customTypes) {
		List<String> ret = new ArrayList<String>();

		for (Method method : clazz.getMethods()) {
			String name = method.getName();
			Class<?>[] parameterClasses = method.getParameterTypes();
			Class<?> returnClass = method.getReturnType();

			if (parameterClasses.length == 0 && isGetter(name) && (
					(mode == ChooseMode.STANDARD && (ClassUtils.isStandardType(returnClass) || customTypes.contains(returnClass))) ||
							(mode == ChooseMode.BEANS && (ClassUtils.isBeanReference(returnClass) || customTypes.contains(returnClass))) ||
					(mode == ChooseMode.COLLECTIONS && (ClassUtils.isCollection(returnClass) || customTypes.contains(returnClass)))
					))
			{
				String propName = removeGetOrIsPrefix(method);
				ret.add(getFirstCharLowerCasePropertyName(propName));
			}
		}

		return ret;
	}

	/** @return true if the given method starts with "get" or "is". */
	public static boolean isGetter(String methodName) {
		return methodName.startsWith("get") || methodName.startsWith("is");
	}

	/**
	 * TODO comment this method.
	 */
	public static Class<?> findServiceInterface(Object service) {

		if (!AopUtils.isAopProxy(service) && !AopUtils.isCglibProxy(service) && service.getClass().getName().indexOf("$$") == -1)
			return service.getClass();

		for (Class<?> ifClass : AopProxyUtils.proxiedUserInterfaces(service)) {
			if (ifClass.isInterface() && ifClass.getName().endsWith("Service"))
				return ifClass;
		}
		return null;
	}

	/**
	 * @param params list of Objects to retrieve Class for, none may be null.
	 * @return an array of Class that contains the classes of all Objects in given array.
	 */
	public static Class<?>[] convertToParamTypes(Object[] params) {
		Class<?>[] clazzes = new Class[params.length];
		for (int i = 0; i < params.length; i++) {
			clazzes[i] = params[i].getClass();
		}
		return clazzes;
	}

	/**
	 * Delegates to <b>apache-commons</b> <code>PropertyUtils.describe(bean)</code>.
	 * This returns all properties (and their values) that have a <b>public getter</b> method (setter not required).
	 * References to other Java objects are resolved by providing the original Object the property
	 * holds (not another Map of properties like this method returns).
	 * The class of the given bean must be public, no package-visible or inner private classes can be scanned.
	 * <p/>
	 * A property name is defined as the base-name of the getter-method, with first character lower-case,
	 * e.g. "count" for "getCount()" (even when the according field was named "itemCounter").
	 * @return a Map where
	 * 		Map key is property name (e.g. "name" for "getName()") and
	 * 		Map value is the value the bean holds for that property.
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getPropertyValues(Object bean) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		return PropertyUtils.describe(bean);
	}

	/**
	 * Mind that, for this method, a getter is enough to make up a "property", and Transient annotations will not be detected.
	 * @param bean the object to read property values from.
	 * @return a property - value Map with all primitive or String properties of given object.
	 * @throws Exception when reflection fails.
	 */
	public static Map<String, Object> getStandardPropertyValues(Object bean) throws Exception {
		Map<String, Object> simplePropertyValues = new HashMap<String, Object>();
		for (String property : getAllStandardProperties(bean.getClass())) {
			simplePropertyValues.put(property, invokeGetter(bean, property));
		}
		return simplePropertyValues;
	}

	/**
	 * Mind that, for this method, a getter is enough to make up a "property", and Transient annotations will not be detected.
	 * @param bean the object to read property values from.
	 * @return a property - value Map with all reference properties of given object.
	 * @throws Exception when reflection fails.
	 */
	public static Map<String, Object> getBeanPropertyValues(Object bean) throws Exception {
		Map<String, Object> beanPropertyValues = new HashMap<String, Object>();
		for (String property : getAllBeanProperties(bean.getClass())) {
			beanPropertyValues.put(property, invokeGetter(bean, property));
		}
		return beanPropertyValues;
	}

	/**
	 * Mind that, for this method, a getter is enough to make up a "property", and Transient annotations will not be detected.
	 * @param bean the object to read property values from.
	 * @return a property - value Map with all Collection properties of given object.
	 * @throws Exception when reflection fails.
	 */
	public static Map<String, Object> getCollectionPropertyValues(Object bean) throws Exception {
		Map<String, Object> collectionPropertyValues = new HashMap<String, Object>();
		for (String property : getAllCollectionProperties(bean.getClass())) {
			collectionPropertyValues.put(property, invokeGetter(bean, property));
		}
		return collectionPropertyValues;
	}

	/**
	 * Mind that, for this method, a getter is enough to make up a "property", and Transient annotations will not be detected.
	 */
	public static Map<String, Object> getStandardAndBeanPropertyValues(Object bean) throws Exception {
		Map<String, Object> simplePropertyValues = new HashMap<String, Object>();

		List<String> properties = getAllStandardProperties(bean.getClass());
		properties.addAll(getAllBeanProperties(bean.getClass()));

		for (String property : properties) {
			simplePropertyValues.put(property, invokeGetter(bean, property));
		}
		return simplePropertyValues;
	}

	/** Uses PropertyUtils.getPropertyDescriptors() to find out all property names and their data types. */
	public static Map<String, Class<?>> getPropertyClasses(Class<?> beanClass) {
		PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(beanClass);
		Map<String, Class<?>> result = new HashMap<String, Class<?>>();
		for (PropertyDescriptor descriptor : descriptors) {
			result.put(descriptor.getName(), descriptor.getPropertyType());
		}
		return result;
	}

	/** @return any method (public, private, protected or package-visibly) of given methodName, and set it accessible. */
	public static Method getAnyMethodAndMakeItAccessable(Class<?> clazz, String methodName) {
		Method[] declaredMethods = clazz.getDeclaredMethods();
		Method retMethod = null;
		for (int i = 0; i < declaredMethods.length; i++) {
			Method method = declaredMethods[i];
			if (methodName.equals(method.getName())) {
				retMethod = method;
				break;
			}
		}

		if (retMethod == null)
			return null;

		retMethod.setAccessible(true);
		return retMethod;
	}

	/**
	 * Copies property values, exclusive ignoreProperties, from source to target.
	 * Delegates to apache commons BeanUtils.copyProperties().
	 * @deprecated this is not used anywhere!
	 */
	public static void copyProperties(Object source, Object target, String[] ignoreProperties) {
		BeanUtils.copyProperties(source, target, ignoreProperties);
	}

	/**
	 * Invokes the getter method of given property on targetObject and returns its value.
	 */
	public static Object invokeGetter(Object targetObject, String firstCharLowerCasePropertyName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		final String methodNameBase = getFirstCharUpperCasePropertyName(firstCharLowerCasePropertyName);
		try {
			Method method = targetObject.getClass().getMethod("get" + methodNameBase, new Class[0]);
			return method.invoke(targetObject, new Object[0]);
		}
		catch (NoSuchMethodException e) {
			try {
				Method method = targetObject.getClass().getMethod("is" + methodNameBase, new Class[0]);
				return method.invoke(targetObject, new Object[0]);
			}
			catch (NoSuchMethodException e2) {
				throw e; // throw the "get" variant error
			}
		}
	}

	/**
	 * Invokes the setter method of given property on targetObject.
	 * @param targetObject the object on which to call the setter-method representing the given property.
	 * @param firstCharLowerCasePropertyName the name of the property to use for the setter, e.g. "count" for <code>setCount()</code>.
	 * @param value the value to pass to the setter method.
	 * @param valueClass the class of the value, this is for the case that the value is null or different from method argument class.
	 */
	public static void invokeSetter(Object targetObject, String firstCharLowerCasePropertyName, Object value, Class<?> valueClass) throws NoSuchMethodException, InvocationTargetException,
			IllegalAccessException {
		final String methodNameBase = getFirstCharUpperCasePropertyName(firstCharLowerCasePropertyName);
		Method method = targetObject.getClass().getMethod("set" + methodNameBase, new Class[] { valueClass });
		method.invoke(targetObject, new Object[] { value });
	}

	/** @return given name with first character converted to upper-case. */
	public static String getFirstCharUpperCasePropertyName(String propName) {
		if (propName == null)
			return null;
		if (propName.length() > 1)
			return propName.toUpperCase().substring(0, 1) + propName.substring(1);
		return propName.toUpperCase();
	}

	/** @return given name with first character converted to lower-case. */
	public static String getFirstCharLowerCasePropertyName(String BaseName) {
		if (BaseName == null)
			return null;
		if (BaseName.length() > 1)
			return BaseName.toLowerCase().substring(0, 1) + BaseName.substring(1);
		return BaseName.toLowerCase();
	}

	/**
	 * @param collectionProperty the name of the collection property, in plural. e.g. "groups" from setGroups().
	 * @param clazz class that contains given collectionProperty.
	 * @return the method from given class that adds a collection element, e.g. addGroup(group).
	 */
	public static Method getAdderMethod(Class<?> clazz, String collectionProperty, Class<?> parameterType) {
		try {
			return clazz.getMethod(getAdderMethodName(collectionProperty), new Class<?>[] { parameterType });
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param collectionProperty the name of the collection property, in plural. e.g. "groups" from setGroups(),
	 * 		dotted names are also processed correctly, e.g. "person.memberOfGroups" would return "addMemberOfGroups".
	 * @return the name of the method from given class that adds a collection element, e.g. addGroup(group).
	 */
	public static String getAdderMethodName(String collectionProperty) {
		final int i = collectionProperty.lastIndexOf(".");
		if (i >= 0)
			collectionProperty = collectionProperty.substring(i + 1);

		final String propertyNameSingular = TextUtils.getSingularPropertyName(collectionProperty);
		return "add" + ReflectionUtils.getFirstCharUpperCasePropertyName(propertyNameSingular);
	}

	/**
	 * Finds all field names in given class and embedded classes when they are indexed.
	 * @param clazz the class to inspect for search-indexed fields.
	 * @return the unsorted names of the fields that have been annotated to be indexed,
	 * 			e.g. <code>[ "shortName", "name" ]</code>,
	 * 		but result could also be dotted names when @IndexedEmbedded properties were found,
	 * 			e.g. <code>[ "person.lastName", "person.firstName" ]</code>.
	 */
	public static String[] getIndexedFieldNames(Class<? extends PersistentObject> clazz) {
		assert isIndexedForTextSearch(clazz) : "To be suitable for a Lucene-search, the entity-type should contain the "+Indexed.class.getSimpleName()+" annotation, failed entity is "+clazz;
		
		final Collection<String> indexedFieldNames = new ArrayList<String>();

		for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(Field.class)) {
				indexedFieldNames.add(field.getName());
			}
			
			// if an embedded object is indexed, we have to extract those fields also
			if (field.isAnnotationPresent(IndexedEmbedded.class)) {
				@SuppressWarnings("unchecked")
				final String[] embeddedFields = getIndexedFieldNames((Class<? extends PersistentObject>) field.getType());
				
				for (String embeddedField : embeddedFields)
					indexedFieldNames.add(field.getName()+"."+embeddedField);
			}
		}

		return indexedFieldNames.toArray(new String[indexedFieldNames.size()]);
	}

	/**
	 * @return true when given class has been annotated for search-indexing. 
	 */
	public static boolean isIndexedForTextSearch(Class<? extends PersistentObject> clazz) {
		return clazz.isAnnotationPresent(Indexed.class);
	}

}
