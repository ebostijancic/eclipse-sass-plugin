package at.workflow.webdesk.tools.comparator;

import java.util.List;

import at.workflow.webdesk.tools.BeanReflectUtil;
import at.workflow.webdesk.tools.BeanReflectUtil.BeanProperty;

/**
 * Shallow bean comparator.
 * 
 * Compares values of
 * <ul><li>all bean properties.</li>
 * <li>provided bean properties.</li>
 * <li>all except specified bean properties.</li></ul>
 * 
 * @author sdzuban 29.08.2013
 */
public class PropertiesComparator {

	/**
	 * Determines whether all the defined beanProperties 
	 * have equal or null values.
	 * @param bean1 first bean for comparison
	 * @param bean2 second bean for comparison
	 * @return true if all the specified properties have both null or equal value
	 */
	public static boolean isEqual(Object bean1, Object bean2) {
		
		return isEqual(bean1, bean2, new String[] {});
	}
		
	/**
	 * Determines whether any of the defined beanProperties 
	 * have not equal or different values.
	 * @param bean1 first bean for comparison
	 * @param bean2 second bean for comparison
	 * @return true if any of the specified properties have different or not equal value
	 */
	public static boolean isNotEqual(Object bean1, Object bean2) {
		
		return ! isEqual(bean1, bean2, new String[] {});
	}
	
	/**
	 * Determines whether any of the defined beanProperties 
	 * with exception of propertyNamesToBeExcluded 
	 * have not equal or different values.
	 * @param bean1 first bean for comparison
	 * @param bean2 second bean for comparison
	 * @param propertyNamesToBeExcluded names of properties to be excluded from comparison
	 * @return true if any of the specified properties have different or not equal value
	 */
	public static boolean isNotEqual(Object bean1, Object bean2, String... propertyNamesToBeExcluded) {
		
		return ! isEqual(bean1, bean2, propertyNamesToBeExcluded);
	}
		
	/**
	 * Determines whether all the defined beanProperties 
	 * with exception of propertyNamesToBeExcluded 
	 * have equal or null values.
	 * @param bean1 first bean for comparison
	 * @param bean2 second bean for comparison
	 * @param propertyNamesToBeExcluded names of properties to be excluded from comparison
	 * @return true if all the specified properties have both null or equal value
	 */
	public static boolean isEqual(Object bean1, Object bean2, String... propertyNamesToBeExcluded) {
		
		if (bean1 == null && bean2 == null)
			return true;
		
		if (bean1 == null || bean2 == null)
			return false;
		
		Class<?> beanClass = bean1.getClass();
		propertyNamesToBeExcluded = propertyNamesToBeExcluded == null ? new String[] {} : propertyNamesToBeExcluded;

		List<BeanProperty> beanProperties = BeanReflectUtil.properties(beanClass, propertyNamesToBeExcluded, new String[] {});
		
		if (beanProperties == null)
			return true; // no properties to compare

		return isEqual(bean1, bean2, beanProperties);
	}


	/**
	 * Determines whether the specified beanProperties 
	 * have equal or null values.
	 * @param bean1 first bean for comparison
	 * @param bean2 second bean for comparison
	 * @param beanProperties properties to be compared
	 * @return true if all the specified properties have both null or equal value
	 */
	public static boolean isEqual(Object bean1, Object bean2, List<BeanProperty> beanProperties) {
		
		if (bean1 == null && bean2 == null)
			return true;
		
		if (bean1 == null || bean2 == null)
			return false;
		
		for (BeanProperty property : beanProperties) {
			try	{
				Object value1 = property.getter.invoke(bean1);
				Object value2 = property.getter.invoke(bean2);
				if (value1 == null && value2 != null)
					return false;
				if (value1 != null && value2 == null)
					return false;
				if (value1 != null && !value1.equals(value2))
					return false;
				if (value2 != null && !value2.equals(value1))
					return false;
			}
			catch (Exception e)	{
				throw new RuntimeException(e);
			}
		}
		return true;
	}
	
}
