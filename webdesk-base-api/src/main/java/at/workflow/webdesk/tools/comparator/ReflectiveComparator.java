package at.workflow.webdesk.tools.comparator;

import java.lang.reflect.Method;
import java.util.Comparator;

import org.apache.commons.lang.StringUtils;

/**
 * Use this comparator to compare 2 objects with one sortfield.
 * 
 * The sortfield type must implement Comparable interface or be a number.
 * 
 * sfeichter 03.11.2010 / sdzuban 14.09.2012
 * 
 * TODO: fri_2013-02-06: documentation of this class is insufficient!
 */
public class ReflectiveComparator implements Comparator<Object> {
	
	private final String sortField;
	private final boolean sameClassObjects;
	
	private Method method1, method2;	// buffering getters for performance tuning
	
	public ReflectiveComparator(String sortField) {
		this(sortField, false);
	}

	public ReflectiveComparator(String sortField, boolean sameClassObjects) {
		if (StringUtils.isBlank(sortField))
			throw new IllegalArgumentException("Blank sort field is not allowed in a Comparator: "+sortField);
		
		this.sortField = sortField.substring(0, 1).toUpperCase()+sortField.substring(1);	// TODO this has been coded in BeanReflectUtil
		this.sameClassObjects = sameClassObjects;
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public int compare(Object o1, Object o2) {
		
		final int testForNull = CompareUtil.testForNulls(o1, o2);
		if (testForNull != CompareUtil.BOTH_ARE_NOT_NULL_AND_NOT_IDENTICAL)
			return testForNull;
		
		assert o1.getClass().isAssignableFrom(o2.getClass()) || o2.getClass().isAssignableFrom(o1.getClass()): "Cannot compare different type of objects";
		
		if (method1 == null || !sameClassObjects)
			method1 = getMethod(o1);

		if (method2 == null || !sameClassObjects)
			method2 = getMethod(o2);
		
		final Object value1 = getValue(o1, method1);
		final Object value2 = getValue(o2, method2);
		
		final int testForNullValues = CompareUtil.testForNulls(value1, value2);
		if (testForNullValues != CompareUtil.BOTH_ARE_NOT_NULL_AND_NOT_IDENTICAL)
			return testForNullValues;
		
		if (value1 instanceof String && value2 instanceof String)
			return ((String) value1).compareToIgnoreCase((String) value2);
		
		if (value1 instanceof Comparable)
			return ((Comparable) value1).compareTo(value2);
		
		if (value1 instanceof Number && value2 instanceof Number) {
			final double diff = ((Number) value1).doubleValue() - ((Number) value2).doubleValue();
			return (diff > 0) ? +1 : (diff < 0) ? -1 : 0;
		}

		throw new RuntimeException("Data types not implemented: value1 = "+value1.getClass()+", value2 = "+value2.getClass());
	}

	private Method getMethod(Object o) {
		try {
			return o.getClass().getMethod("get"+sortField);
		} catch (Exception e) {
			try {
				return o.getClass().getMethod("is"+sortField);
			} catch (Exception e1) {
				throw new RuntimeException("Exceptions while getting getter for field " + sortField + ": " + e1, e1);
			}
		}
	}

	private Object getValue(Object o, Method method) {
		
		try {
			return method.invoke(o);
		} catch (Exception e) {
			throw new RuntimeException("Exceptions while invoking getter for field " + sortField + " on object " + o.toString() + ": " + e, e);
		}
	}

}
