package at.workflow.webdesk.tools.collections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.BeanUtils;

/**
 * This class collects methods that offer closure functionality
 * and will become deprecated in Java 8.
 * 
 * For further methods see also org.springframework.CollectionUtils
 * as well as org.apache.commons collection framework.
 * 
 * @author sdzuban 30.10.2012
 */
public class CollectionsUtils {
	
	
	/**
	 * This method filters collection of elements based on elements property. 
	 * Collection of same kind (e.g. HashSet) as the input collection is returned.
	 * WARNING: Wrap PersistentBag otherwise new unpersisted PersistentBag will be returned!  
	 * 
	 * @param collection
	 * @param propertyName
	 * @param requiredPropertyValue elements with property = property value are collected
	 * @return new collection of same kind with all conforming elements
	 * 
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public static <LISTELEMENT> Collection<LISTELEMENT> filterCollection(Collection<LISTELEMENT> collection, String propertyName, Object requiredPropertyValue) 
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {

		return filterCollection(collection, propertyName, requiredPropertyValue, true);
	}

	/**
	 * This method filters collection of elements based on elements property.
	 * Collection of same kind (e.g. HashSet) as the input collection is returned.
	 * WARNING: Wrap PersistentBag otherwise new unpersisted PersistentBag will be returned!  
	 * 
	 * @param collection
	 * @param propertyName
	 * @param requiredPropertyValue elements with property = property value are collected
	 * @return new collection of same kind with all conflicting elements
	 * 
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public static <LISTELEMENT> Collection<LISTELEMENT> filterCollectionNegative(Collection<LISTELEMENT> collection, String propertyName, Object requiredPropertyValue) 
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
		
		return filterCollection(collection, propertyName, requiredPropertyValue, false);
	}
	
	
	private static <LISTELEMENT> Collection<LISTELEMENT> filterCollection(Collection<LISTELEMENT> collection, String propertyName, Object propertyValue, boolean eqls) 
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
			
		if (collection == null)
			return null;
		
		Method propertyReader = null;
		
		@SuppressWarnings("unchecked")
		Collection<LISTELEMENT> result = collection.getClass().newInstance();
		if (collection.isEmpty())
			return result;
		
		for (LISTELEMENT element : collection) {
			if (propertyReader == null && element != null)
				propertyReader = BeanUtils.getPropertyDescriptor(element.getClass(), propertyName).getReadMethod();
			if (element != null) {
				Object value = propertyReader.invoke(element);
				boolean valueEquals = value == null && propertyValue == null || value != null && value.equals(propertyValue);
				if (eqls && valueEquals || !eqls && !valueEquals)
					result.add(element);
			}
		}
		
		return result;
	}
	
	/**
	 * Applies method with methodName to every member of the array and collects the results of the invocations in resulting List.
	 * Collection of same kind (e.g. HashSet) as the input collection is returned.
	 * WARNING: Wrap PersistentBag otherwise new unpersisted PersistentBag will be returned!  
	 * 
	 * @param array
	 * @param methodName
	 * @param arguments optional method arguments, 
	 * @return ArrayList of results of method invocation
	 * 
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public static <LISTELEMENT, VALUETYPE> Collection<VALUETYPE> applyMethod(LISTELEMENT[] array, String methodName, Object ... arguments) 
			throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	
		List<LISTELEMENT> inputList = new ArrayList<LISTELEMENT>();
		for (LISTELEMENT element : array)
			inputList.add(element);
		
		return applyMethod(inputList, methodName, arguments);
	}
		
	/**
	 * Applies method with methodName to every member of the input collection 
	 * and collects the results of the invocations in resulting collection of the same type as the input collection.
	 * Collection of same kind (e.g. HashSet) as the input collection is returned.
	 * WARNING: Wrap PersistentBag otherwise new unpersisted PersistentBag will be returned!  
	 * 
	 * @param array
	 * @param methodName
	 * @param arguments optional method arguments, 
	 * @return collection of results of method invocation
	 * 
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	public static <LISTELEMENT, VALUETYPE> Collection<VALUETYPE> applyMethod(Collection<LISTELEMENT> collection, String methodName, Object ... arguments) 
			throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		if (collection == null)
			return null;
		
		Method method = null;
		
		Class<?>[] argTypes = new Class<?>[arguments.length];
		for (int idx = 0; idx < arguments.length; idx++)
			argTypes[idx] = arguments[idx++].getClass();
		
		Collection<VALUETYPE> result = collection.getClass().newInstance();
		if (collection.isEmpty())
			return result;
		
		for (LISTELEMENT element : collection) {
			if (method == null && element != null)
				method = element.getClass().getMethod(methodName, argTypes);
			if (element != null) {
				result.add((VALUETYPE) method.invoke(element, arguments));
			}
		}
		return result;
	}
	
	/**
	 * Applies service method with methodName to every member of the array and collects the results of the invocations in resulting List.
	 * Collection of same kind (e.g. HashSet) as the input collection is returned.
	 * WARNING: Wrap PersistentBag otherwise new unpersisted PersistentBag will be returned!  
	 * 
	 * @param array
	 * @param service
	 * @param methodName
	 * @return ArrayList of results of method invocation
	 * 
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public static <LISTELEMENT, VALUETYPE> Collection<VALUETYPE> applyServiceMethod(LISTELEMENT[] array, Object service, String methodName) 
			throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		List<LISTELEMENT> inputList = new ArrayList<LISTELEMENT>();
		for (LISTELEMENT element : array)
			inputList.add(element);
		
		return applyServiceMethod(inputList, service, methodName);
	}
	
	/**
	 * Applies service method with methodName to every member of the input collection 
	 * and collects the results of the invocations in resulting collection of the same type as the input collection.
	 * Collection of same kind (e.g. HashSet) as the input collection is returned.
	 * WARNING: Wrap PersistentBag otherwise new unpersisted PersistentBag will be returned!  
	 * 
	 * @param collection
	 * @param service
	 * @param methodName
	 * @return collection of results of method invocation
	 * 
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	public static <ELEMENTTYPE, VALUETYPE> Collection<VALUETYPE> applyServiceMethod(Collection<ELEMENTTYPE> collection, Object service, String methodName) 
			throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		if (service == null)
			throw new IllegalArgumentException("Service shall not be null");
		
		if (collection == null)
			return null;
		
		Collection<VALUETYPE> result = collection.getClass().newInstance();
		if (collection.isEmpty())
			return result;
		
		ELEMENTTYPE firstNotNullElement = getFirstNotNullElement(collection);
		if (firstNotNullElement == null)
			return null;
		Method method = service.getClass().getMethod(methodName, firstNotNullElement.getClass());
		
		for (ELEMENTTYPE element : collection) {
			result.add((VALUETYPE) method.invoke(service, new Object[] {element}));
		}
		return result;
	}

	/**
	 * Utility method to get first not null element of the collection
	 * @param elements
	 * @return
	 */
	public static <ELEMTYPE> ELEMTYPE getFirstNotNullElement(Collection<ELEMTYPE> elements) {
		
		if (elements == null)
			return null;
		
		for (ELEMTYPE element : elements)
			if (element != null)
				return element;
		return null;
	}
	
	/**
	 * Utility method to get all the not null elements of the collection
	 * Collection of same kind (e.g. HashSet) as the input collection is returned.
	 * WARNING: Wrap PersistentBag otherwise new unpersisted PersistentBag will be returned!
	 *   
	 * @param elements
	 * @return
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static <ELEMTYPE> Collection<ELEMTYPE> getAllNotNullElements(Collection<ELEMTYPE> elements) throws InstantiationException, IllegalAccessException {
		
		if (elements == null)
			return null;
		
		@SuppressWarnings("unchecked")
		Collection<ELEMTYPE> result = elements.getClass().newInstance();
		if (elements.isEmpty())
			return result;
		
		for (ELEMTYPE element : elements) {
			if (element != null)
				result.add(element);
		}
		return result;
	}
}
