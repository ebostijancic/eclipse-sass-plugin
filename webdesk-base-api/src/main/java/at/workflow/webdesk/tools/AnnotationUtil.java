package at.workflow.webdesk.tools;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Methods related to annotation processing.
 * 
 * @author fritzberger 10.04.2013
 */
public final class AnnotationUtil
{
	/**
	 * Finds a method that is annotated with given annotation class,
	 * or throws NoAnnotatedMethodFoundException when no such method found.
	 * Searches from given class up through all its super classes and thus
	 * always finds the most specific annotated method.
	 * @param objectClass the class to search an annotated method within.
	 * @param annotationClass the annotation to find.
	 * @return the most specific method annotated with given annotation.
	 * @throws Exception
	 */
	public static Method findAnnotatedMethod(Class<?> objectClass, Class<? extends Annotation> annotationClass) throws Exception {
		while (objectClass.equals(Object.class) == false) {
			for (final Method method : objectClass.getDeclaredMethods()) {
				final boolean shouldBeInspected =
						Modifier.isPublic(method.getModifiers()) ||
								Modifier.isProtected(method.getModifiers());

				if (shouldBeInspected && method.isAnnotationPresent(annotationClass)) {
					return method;
				}
			}
			objectClass = objectClass.getSuperclass();
		}
		throw new NoAnnotatedMethodFoundException("No method of was annotated with " + annotationClass.getName() + " in " + objectClass);
	}

	/**
	 * Exception type to be caught when default behavior is
	 * needed and no method with given annotation was not found.
	 */
	public static class NoAnnotatedMethodFoundException extends Exception
	{
		private NoAnnotatedMethodFoundException(String message) {
			super(message);
		}
	}

	
	/**
	 * Finds all fields that are annotated with given annotation class,
	 * searching given class and all its super-classes.
	 * @param objectClass the class containing the fields that should be inspected.
	 * @param annotationClass the annotation to be searched on fields.
	 * @return all Fields that are marked with given annotation, never null, but can be empty.
	 */
	public static Field [] findAnnotatedFields(Class<?> objectClass, Class<? extends Annotation> annotationClass)	{
		final List<Field> fields = new ArrayList<Field>();
		// search ALL fields in class hierarchy for annotated fields
		while (objectClass != null && objectClass.equals(Object.class) == false)	{
			for (final Field field : objectClass.getDeclaredFields())	{
				if (field.getAnnotation(annotationClass) != null)	{
					fields.add(field);
				}
			}
			objectClass = objectClass.getSuperclass();
		}
		return fields.toArray(new Field[fields.size()]);
	}
	
	
	
	private AnnotationUtil() {
	} // do not instantiate

}
