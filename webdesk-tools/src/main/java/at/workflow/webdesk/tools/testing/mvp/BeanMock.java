package at.workflow.webdesk.tools.testing.mvp;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.ObjectUtils;

/**
 * This implementation refers to following MVP contract for "passive views":
 * <ul>
 * 	<li>Views contain no presentation logic except<ul>
 * 		<li>view-internal data type conversion (e.g. no Date widget exists)</li>
 * 		<li>property change event firing</li>
 * 		<li>support for locations (like index, path or unique keys)
 * 			which occur in adding or removing locations,
 * 			and as parameter in any getter or setter (see class <code>Location</code>)</li>
 * 		</ul></li>
 * 	<li>Parameters and return-types for view setters/getters should <b>not</b> be
 * 		the <i>domain object</i> but its <i>properties</i>, whereby<ul>
 * 		<li>enums must be converted to string by the Presenter
 * 			<font size="-2">(enable generic view implementations without aggregating any enum type)</font></li>
 * 		<li>lists of properties of multiple references must be tagged with locations
 * 			<font size="-2">(when multiple references have properties with same name, the View must distinguish them by the location)</font></li>
 * 		</ul></li>
 * </ul>
 * Action event firing is not supported. This can be tested by calling the adequate Presenter
 * method which must be public because it is also called by the View event mechanism.
 * 
 * <h3>1. Getter and setter symmetry support</h3>
 * 
 * This class mocks <b>setters and getters</b>, i.e. when calling a setXxx(x) Method on this object,
 * the retrieving getXxx() call will deliver the object x that was set by setter before ("symmetry").
 * <p/>
 * When calling a method that returns a class or interface <font size="-2">(except primitives, enum, String,
 * Date and aggregations like Collection or Map)</font>, a new BeanMock proxy will be returned
 * (recursively). It will be the same for any subsequent call.
 * <pre>
	interface PassiveView
	{
		String getWidgetSet();
		void setWidgetSet(String widgetSetName);
		
		Date getCreationDate();
		void setCreationDate(Date creationDate);
	}
 * </pre>
 * 
 * <h3>2. Listener support</h3>
 * 
 * The only supported <b>listener</b> type is <code>PropertyChangeListener</code>. A bean like
 * <pre>
	interface PassiveView
	{
		// listen to the change of any property
		void addPropertyChangeListener(PropertyChangeListener listener);
		void removePropertyChangeListener(PropertyChangeListener listener);
	}
 * </pre>
 * would fire a PropertyChangeEvent when an any property gets changed.
 * <p/>
 * On the other hand following bean would fire a PropertyChangeEvent when a specific property
 * gets changed. There are two ways to add listeners that listen to just one property (see comments):
 * <pre>
	interface PassiveView
	{
		String getXxx();
		void setXxx(String widgetSetName);
		
		// 1: Xxx is the name of observed property
		void addXxxListener(PropertyChangeListener listener);
		void removeXxxListener(PropertyChangeListener listener);
		
		// 2: propertyName contains the name of observed property
		void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);
		void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);
	}
 * </pre>
 * Mind that it is <code>addXxxListener</code>, not <code>addXxxChangeListener</code>,
 * it MUST be <code>"add"+propertyName+"Listener"</code>, and the listener's interface MUST be
 * <code>PropertyChangeListener</code>!
 * 
 * <h3>3. Mocking complex return types recursively</h3>
 * 
 * When the return from a getter is a class or interface other than String, Date, enum or
 * an aggregation-type (Collection, array, Map), BeanMock would return a <b>mock</b> from that
 * method, and remember that instance for the next call
 * (once allocated, the returned mock can be changed only by the related setter).
 * <pre>
	interface PassiveView
	{
		SubView getBody();	// on first call will return a SubView mock object, not null
		void setBody(SubView body);	// this could prepare the getter return, or change it afterwards
	}
 * </pre>
 * 
 * <h3>4. Location support</h3>
 * 
 * Additionally this mock supports <b>locations</b>.
 * Locations are indexes, pathes, unique keys or anything similar that points to a specified location
 * in the view, e.g. a table row (-> index) or a tree node (-> path).
 * There ist the possibility to call following hardcoded methods (which must be contained in the
 * mocked interface to make sense):
 * <pre>
	interface PassiveView
	{
		...
		
		Integer [] getLocations();
		void addLocations(Integer location);
		void removeLocations(Integer location);
	}
 * </pre>
 * 
 * @author fritzberger 31.01.2013
 * @author fritzberger 15.02.2013
 */
public class BeanMock implements InvocationHandler
{
	/** Hardcoded name of the method that lists all Table/Tree rows: <code>getLocations()</code>. */
	private static final String GET_LOCATIONS_METHODNAME = "getLocations";
	
	/** Hardcoded name of the method that adds a Table/Tree row: <code>addLocation(location)</code>. */
	private static final String ADD_LOCATION_METHODNAME = "addLocation";
	
	/** Hardcoded name of the method that removes a Table/Tree row <code>removeLocation(location)</code>. */
	private static final String REMOVE_LOCATION_METHODNAME = "removeLocation";


	/**
	 * Creates a mock object for given interface.
	 * @param interfaceToMock the interface that should be mocked by the returned object.
	 */
	public static <T> T mock(Class<?> interfaceToMock) {
		return mock(interfaceToMock, new Class<?> [] { interfaceToMock });
	}
	
	/**
	 * Creates a mock object for given interfaces, using the class loader of given type.
	 * @param classLoaderHolder holder of the ClassLoader to be used by Proxy.newProxyInstance() call.
	 * @param interfacesToMock interface that should be mocked by the returned object.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T mock(Class<?> classLoaderHolder, Class<?> [] interfacesToMock) {
		final BeanMock beanMock = new BeanMock();
		return (T) Proxy.newProxyInstance(
				classLoaderHolder.getClassLoader(),
				interfacesToMock != null ? interfacesToMock : new Class<?> [] { classLoaderHolder },
				beanMock);
	}
	
	

	private Map<String, Object> propertyValues = new HashMap<String, Object>();
	private Map<Object, Map<String, Object>> locatedPropertyValues = new LinkedHashMap<Object, Map<String, Object>>();	// retain order of calls!
	
	private Map<String, List<Object>> listenerLists = new HashMap<String, List<Object>>();

	
	@Override
	public Object invoke(Object proxy, Method method, Object[] parameters) throws Throwable {
		// test for Object base method calls like equals(), hashCode() or toString()
		final Object o = objectMethodInvokes(proxy, method, parameters);
		if (o != null)
			return o;
		
		// test for hardcoded location-method calls
		final Object h = locationMethodInvokes(method, parameters);
		if (h != null)
			return h;
		
		// check for getter, setter or listener calls
		final String beanProperty = getBeanProperty(method);
		final String locatedBeanProperty = (beanProperty == null) ? getLocatedBeanProperty(method) : null;
		final String listenerProperty = (beanProperty == null && locatedBeanProperty == null) ? getListenerProperty(method) : null;
		
		// prepare parameters
		final Object firstParameter  = (parameters != null && parameters.length > 0) ? parameters[0] : null;
		final Object secondParameter = (parameters != null && parameters.length > 1) ? parameters[1] : null;

		// look what we've got
		if (beanProperty != null) { // is a getter or setter
			return processGetterOrSetter(proxy, method, firstParameter, beanProperty, propertyValues, null);
		}
		else if (locatedBeanProperty != null)	{ // is a getter or setter for specified location
			return processGetterOrSetter(proxy, method, secondParameter, locatedBeanProperty, null, firstParameter);
		}
		else if (listenerProperty != null) { // is an addXxxListener method
			processListener(method, firstParameter, secondParameter, listenerProperty);
		}
		// else: not a property, not a listener, ignore this

		return null;
	}

	
	// equals(), hashCode(), toString() support
	
	private Object objectMethodInvokes(Object proxy, Method method, Object[] parameters) {
		// imitate important Object methods
		final String name = method.getName();
		if ("equals".equals(name)) {
			return proxy == parameters[0]; // must throw IndexOutOfBoundsException
			// return proxy.equals(parameters[0]);	// can not use this, would be recursive when parameter is a Proxy too
		}
		else if ("hashCode".equals(name)) {
			return System.identityHashCode(proxy);
		}
		else if ("toString".equals(name)) {
			return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy)) + " mocked by " + this;
		}
		return null;
	}

	// locations support
	
	private Object locationMethodInvokes(Method method, Object [] parameters) {
		if (method.getName().equals(GET_LOCATIONS_METHODNAME))
			return getLocations(method.getReturnType());
		
		if (method.getName().equals(ADD_LOCATION_METHODNAME))
			return addLocation(parameters[0]);	// must throw IndexOutOfBoundsException when missing required parameter
		
		if (method.getName().equals(REMOVE_LOCATION_METHODNAME))
			return removeLocation(parameters[0]);	// must throw IndexOutOfBoundsException when missing required parameter
		
		return null;
	}

	private Object getLocations(Class<?> returnType) {
		Class<?> locationType = returnType.getComponentType();
		final Object locations = Array.newInstance(locationType, locatedPropertyValues.size());
		int i = 0;
		for (Object location : locatedPropertyValues.keySet())	{
			Array.set(locations, i, location);
			i++;
		}
		return locations;
	}

	private Object addLocation(Object location) {
		locatedPropertyValues.put(location, new HashMap<String, Object>());
		return null;
	}

	private Object removeLocation(Object location) {
		locatedPropertyValues.remove(location);
		return null;
	}

	
	// getter and setter support
	
	private Object processGetterOrSetter(Object proxy, Method method, Object newValue, String beanProperty, Map<String, Object> propertyValues, Object location)
		throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
	{
		final Object value = getPropertyValueNullSafe(propertyValues, beanProperty, location);

		if (isGetter(method.getName()))
			return processGetter(method, beanProperty, value, propertyValues, location);

		return processSetter(proxy, beanProperty, value, newValue, propertyValues, location);	// must be setter
	}

	private Object processGetter(Method method, String beanProperty, Object returnValue, Map<String, Object> propertyValues, Object location) {
		final Class<?> type = method.getReturnType();
		if (returnValue != null || isPrimitive(type) || type.isEnum() || isAggregation(type) || type.isInterface() == false)
			return returnValue; // do not mock primitive (Number, String, Date), enum, array, Collection, Map, or interfaces

		// mock anything else recursively
		final Object newMock = BeanMock.mock(type);
		ensureProperties(propertyValues, location, true).put(beanProperty, newMock);
		
		return newMock;
	}

	private Object processSetter(Object proxy, String beanProperty, Object oldValue, Object newValue, Map<String, Object> propertyValues, Object location)
		throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
	{
		ensureProperties(propertyValues, location, true).put(beanProperty, newValue); // remember the value for any subsequent getter call
		firePropertyChange(proxy, beanProperty, oldValue, newValue);
		return null;
	}
	
	private Object getPropertyValueNullSafe(Map<String,Object> properties, String beanProperty, Object location)	{
		return ensureProperties(properties, location, false).get(beanProperty);
	}
	
	private Map<String,Object> ensureProperties(Map<String,Object> properties, Object location, boolean storeToLocatedPropertyValues)	{
		if (properties == null && location != null)	{
			properties = locatedPropertyValues.get(location);
			if (properties == null)	{
				properties = new HashMap<String,Object>();
				if (storeToLocatedPropertyValues)
					locatedPropertyValues.put(location, properties);
			}
		}
		assert properties != null : "Need non-null properties, location = "+location;
		return properties;
	}

	private void processListener(Method method, Object firstParameter, Object secondParameter, final String listenerProperty) {
		assert firstParameter != null : "Either listener- or propertyName-parameter is null: "+method;
		
		final String listenerMapKey;
		final Object listener;
		if (method.getParameterTypes().length == 2)	{	// this is for addPropertyChangeListener(propertyName, listener)
			listenerMapKey = firstCharUpper((String) firstParameter);
			assert secondParameter != null : "Listener-parameter is null: "+method;
			listener = secondParameter;
		}
		else	{	// this is for addPropertyChangeListener(listener)
			listenerMapKey = listenerProperty;
			listener = firstParameter;
		}

		List<Object> listeners = listenerLists.get(listenerMapKey);
		if (isListenerAdder(method.getName())) {
			if (listeners == null)
				listenerLists.put(listenerMapKey, listeners = new ArrayList<Object>());

			listeners.add(listener);
		}
		else if (listeners != null) { // else: must be remover, listeners were added
			listeners.remove(listener);
		}
	}

	
	// firing events
	
	private void firePropertyChange(Object proxy, String beanProperty, Object oldValue, Object newValue)
		throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
	{
		if (ObjectUtils.nullSafeEquals(oldValue, newValue))
			return;

		final List<?> listeners = listenerLists.get(beanProperty); // listeners for just one property
		if (listeners != null)
			firePropertyChange(listeners, proxy, beanProperty, oldValue, newValue);
		
		final List<?> propertyChangeListeners = (beanProperty.equals("PropertyChange")) ? null : listenerLists.get("PropertyChange"); // add "generic" addPropertyChangeListener(listener) when exists
		if (propertyChangeListeners != null)
			firePropertyChange(propertyChangeListeners, proxy, beanProperty, oldValue, newValue);
	}

	private void firePropertyChange(List<?> listeners, Object proxy, String beanProperty, Object oldValue, Object newValue)
		throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
	{
		for (Object listener : listeners) {
			final Method listenerMethod = listener.getClass().getMethod("propertyChange", new Class [] { PropertyChangeEvent.class });
			listenerMethod.setAccessible(true);	// local or anonymous classes would not work else
			listenerMethod.invoke(listener, new Object[] { new PropertyChangeEvent(proxy, beanProperty, oldValue, newValue) });
		}
	}
	

	// utilities
	
	/** @return "Foo" for a method named "getFoo()" without parameters, or "setFoo(foo)" with exactly one parameter. */
	private String getBeanProperty(Method method) {
		return getBeanProperty(method, 0, 1);
	}

	/** @return "Foo" for a method named "getFoo(location)" with exactly one parameter, or "setFoo(location, foo)" with exactly two parameters. */
	private String getLocatedBeanProperty(Method method) {
		return getBeanProperty(method, 1, 2);
	}

	private String getBeanProperty(Method method, int numberOfGetterParams, int numberOfSetterParams) {
		final String name = method.getName();
		final boolean isGetter = isGetter(name);
		final boolean isBooleanGetter = (isGetter == false) && isBooleanGetter(name);

		if ((isGetter || isBooleanGetter) && method.getParameterTypes().length == numberOfGetterParams) {
			return name.substring(isGetter ? "get".length() : "is".length());
		}
		else if (isSetter(name) && method.getParameterTypes().length == numberOfSetterParams) {
			return name.substring("set".length());
		}
		return null;
	}

	private boolean isSetter(String name) {
		return name.startsWith("set");
	}

	private boolean isGetter(String name) {
		return name.startsWith("get");
	}

	private boolean isBooleanGetter(String name) {
		return name.startsWith("is");
	}

	private String getListenerProperty(Method method) {
		final String name = method.getName();
		if (name.endsWith("Listener") == false)
			return null;

		final boolean isAdder = isListenerAdder(name);
		final boolean isRemover = (isAdder == false) && isListenerRemover(name);

		if (isAdder == false && isRemover == false)
			return null;

		final Class<?> [] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length > 2 || parameterTypes.length < 1)
			return null;

		if (checkAddListenerParameterTypes(parameterTypes) == false)
			return null;

		final String baseName = name.substring(isAdder ? "add".length() : "remove".length());
		return baseName.substring(0, baseName.length() - "Listener".length());
	}

	private boolean checkAddListenerParameterTypes(Class<?> [] parameterTypes) {
		if (parameterTypes.length == 1)	// addPropertyChangeListener(PropertyChangeListener listener)
			return implementsInterface(parameterTypes[0], PropertyChangeListener.class);
		// else: parameterTypes.length == 2, addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
		return parameterTypes[0].equals(String.class) && implementsInterface(parameterTypes[1], PropertyChangeListener.class);
	}

	private boolean implementsInterface(Class<?> type, Class<?> interfaceToImplement) {
		if (type.equals(interfaceToImplement))
			return true;
		
		for (Class<?> interFace : type.getInterfaces())
			if (interFace.equals(interfaceToImplement))
				return true;
		
		return false;
	}

	private boolean isListenerAdder(String name) {
		return name.startsWith("add");
	}

	private boolean isListenerRemover(String name) {
		return name.startsWith("remove");
	}

	private boolean isPrimitive(Class<?> type) {
		return type.isPrimitive() || type.equals(String.class) || Date.class.isAssignableFrom(type);
	}

	private boolean isAggregation(Class<?> type) {
		return type.isArray() || Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type);
	}

	private String firstCharUpper(String s) {
		return s.substring(0, 1).toUpperCase()+s.substring(1);
	}

}
