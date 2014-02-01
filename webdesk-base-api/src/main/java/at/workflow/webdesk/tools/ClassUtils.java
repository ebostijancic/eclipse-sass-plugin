package at.workflow.webdesk.tools;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

/**
 * This models groups of data types:
 * <ul>
 * 	<li>primitive (contains String)</li>
 * 	<li>standard (contains primitives and Date)</li>
 * 	<li>collection</li>
 * 	<li>bean-references (neither standard nor collection)</li>
 * </ul>
 * This groups are mainly put on PersistentObject (domain objects) reflection.
 * 
 * @author ggruber
 * @author fritzberger refactoring 2012-03-10
 */
public class ClassUtils {

	private static Class<?> [] standardTypes = {
		String.class,
		Number.class,
		Date.class,
		java.sql.Date.class,
		java.sql.Time.class,
		java.sql.Timestamp.class,
		Boolean.class,
		Integer.class,
		Double.class,
		Float.class,
		Long.class,
		Short.class,
		Character.class,
		Byte.class,
		BigInteger.class,
		BigDecimal.class,
	};

	/**
	 * Standard types are considered to be <b>primitives</b> OR :
	 * <pre>
		String.class,
		Number.class,
		Date.class,
		java.sql.Date.class,
		java.sql.Time.class,
		java.sql.Timestamp.class,
		Boolean.class,
		Integer.class,
		Double.class,
		Float.class,
		Long.class,
		Short.class,
		Character.class,
		Byte.class,
		BigInteger.class,
		BigDecimal.class,
	 * </pre>
	 */
	public static boolean isStandardType(Class<?> type)	{
		return isPrimitive(type) || ArrayUtils.contains(standardTypes, type);
	}
	
	/** @return true when give type is either primitive (int, char, boolean, ...) or class <b>String</b>. */
	public static boolean isPrimitive(Class<?> type)	{
		return type.isPrimitive() || type.equals(String.class);
	}

	/** @return true when give type is an array or assignable to Collection or Map class. */
	public static boolean isCollection(Class<?> type)	{
		return Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type);
	}

	/** @return true when give type is neither StandardType nor Collection. */
	public static boolean isBeanReference(Class<?> type) {
		// TODO should check for PersistentObject interface, but this is not importable here
		return isStandardType(type) == false && isCollection(type) == false && type.isArray() == false && type.isEnum() == false;
	}

	/** @return true when give type is a Java enum type. */
	public static boolean isEnumeration(Class<?> type) {
		return type.isEnum();
	}

	/**
	 * Converts a given type to an Object type when it is priimitive ().
	 * @param type the type to convert to Object class when primitive.
	 * @return the given type when not primitive, or a Class according to given primitive type.
	 */
	public static Class<?> toNonPrimitive(Class<?> type) {
		if (type.isPrimitive()) {
			if (type.equals(Boolean.TYPE))
				type = Boolean.class;
			else if (type.equals(Integer.TYPE))
				type = Integer.class;
			else if (type.equals(Long.TYPE))
				type = Long.class;
			else if (type.equals(Double.TYPE))
				type = Double.class;
			else if (type.equals(Float.TYPE))
				type = Float.class;
			else if (type.equals(Byte.TYPE))
				type = Byte.class;
			else if (type.equals(Character.TYPE))
				type = Character.class;
			else if (type.equals(Short.TYPE))
				type = Short.class;
		}
		return type;
	}

	/** @return true when <code>class1</code> could be assigned from <code>class2</code>, or vice versa. */ 
	public static boolean isCompatible(Class<?> class1, Class<?> class2) {
		class1 = ClassUtils.toNonPrimitive(class1);
		class2 = ClassUtils.toNonPrimitive(class2);
		return class1.isAssignableFrom(class2) || class2.isAssignableFrom(class1);
	}
	
	/**
	 * This handles primitive classes correctly.
	 * @return true when <code>toBeAssigned</code> could be assigned from <code>assigner</code>, NOT vice versa.
	 */
	public static boolean isAssignableFrom(Class<?> toBeAssigned, Class<?> assigner) {
		toBeAssigned = ClassUtils.toNonPrimitive(toBeAssigned);
		assigner = ClassUtils.toNonPrimitive(assigner);
		return toBeAssigned.isAssignableFrom(assigner);
	}

	/**
	 * Can resolve a CGLib-enhanced class to its real class.
	 * @param proxyOrRealObject the object that might be a CGLib-enhanced class.
	 * @return the class of the given object if it is not a CGLib-enhanced class, else the real class.
	 */
	public static Class<?> resolveProxyClass(Object proxyOrRealObject) {
		if (org.springframework.util.ClassUtils.isCglibProxy(proxyOrRealObject))
			return proxyOrRealObject.getClass().getSuperclass();	// CGLib proxy is a sub-class of the real class
		
		return proxyOrRealObject.getClass();
	}

}
