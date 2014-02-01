package at.workflow.webdesk.tools.dbmetadata;

import java.util.ArrayList;
import java.util.List;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.tools.BeanReflectUtil;
import at.workflow.webdesk.tools.ClassUtils;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.dbmetadata.PersistenceMetadata;

/**
 * Global utilities in relation with <code>PersistenceMetadata</code>
 * (data about data that are persisted through the JPA layer, i.e. Hibernate).
 * 
 * @author fritzberger 05.03.2012
 */
public class PersistenceMetadataUtil {

	/**
	 * Delivers persistent properties of given domain object (POJO) class, optionally filtered.
	 * Collections and object references are contained, too.
	 * @param clazz the domain object (POJO) class to scan for properties.
	 * @param ignoredPropertyNames optional names of properties that should not be returned, can be null or empty.
	 * @return List of persistent properties with all necessary information about that property, for instance the getter's return type
	 * 		or the setter's parameter type.
	 * Utility that retrieves spring bean from running application context cannot be used in test
	 */
	public static List<BeanReflectUtil.BeanProperty> getPersistentProperties(Class<? extends PersistentObject> clazz, String [] ignoredPropertyNames)	{
		PersistenceMetadata metaData = (PersistenceMetadata) WebdeskApplicationContext.getApplicationContext().getBean("PersistenceMetadata");
		return getPersistentProperties(clazz, ignoredPropertyNames, metaData);
	}
	
	/**
	 * Delivers persistent properties of given domain object (POJO) class, optionally filtered.
	 * Collections and object references are contained, too. Primary key (UID) is not contained.
	 * @param clazz the domain object (POJO) class to scan for properties.
	 * @param ignoredPropertyNames optional names of properties that should not be returned, can be null or empty.
	 * @param metaData result of getApplicationContext().getBean("PersistenceMetadata").
	 * @return List of persistent properties with all necessary information about that property, for instance the getter's return type
	 * 		or the setter's parameter type.
	 */
	public static List<BeanReflectUtil.BeanProperty> getPersistentProperties(Class<? extends PersistentObject> clazz, String [] ignoredPropertyNames, PersistenceMetadata metaData)	{
		return getPersistentProperties(clazz, ignoredPropertyNames, metaData, false);	// false: as of 2012-03-14 we do not export the UID
	}
	
	/**
	 * Delivers persistent properties of given domain object (POJO) class, optionally filtered.
	 * Collections and object references are contained, too. Primary key (UID) is contained when includePrimaryKey is true.
	 * @param clazz the domain object (POJO) class to scan for properties.
	 * @param ignoredPropertyNames optional names of properties that should not be returned, can be null or empty.
	 * @param metaData result of getApplicationContext().getBean("PersistenceMetadata").
	 * @param includePrimaryKey when true, UID will also be contained.
	 * @return List of persistent properties with all necessary information about that property, for instance the getter's return type
	 * 		or the setter's parameter type.
	 */
	public static List<BeanReflectUtil.BeanProperty> getPersistentProperties(Class<? extends PersistentObject> clazz, String [] ignoredPropertyNames, PersistenceMetadata metaData, boolean includePrimaryKey)	{
		List<String> persistentProperties = metaData.getPropertyNames(clazz);
		if (includePrimaryKey)
			persistentProperties.add("UID");	// TODO: 241 occurrences of "UID" in Webdesk project, 2013-03-14
		return BeanReflectUtil.properties(clazz, ignoredPropertyNames, persistentProperties.toArray(new String[persistentProperties.size()]));
	}
	
	
	
	
	/**
	 * Standard properties are primitives and their Java wrapper classes, and String and Date.
	 * @return a new List containing standard properties, taken from a List retrieved via <code>getPersistentProperties()</code>.
	 */
	public static List<BeanReflectUtil.BeanProperty> getStandardProperties(List<BeanReflectUtil.BeanProperty> persistentProperties)	{
		List<BeanReflectUtil.BeanProperty> filtered = new ArrayList<BeanReflectUtil.BeanProperty>();
		for (BeanReflectUtil.BeanProperty property : persistentProperties)
			if (ClassUtils.isStandardType(property.getter.getReturnType()))
				filtered.add(property);
		return filtered;
	}

	/**
	 * Bean references are pointers to other domain objects (1:n relations).
	 * @return a new List containing bean-reference properties, taken from a List retrieved via <code>getPersistentProperties()</code>.
	 */
	public static List<BeanReflectUtil.BeanProperty> getBeanReferenceProperties(List<BeanReflectUtil.BeanProperty> persistentProperties)	{
		List<BeanReflectUtil.BeanProperty> filtered = new ArrayList<BeanReflectUtil.BeanProperty>();
		for (BeanReflectUtil.BeanProperty property : persistentProperties)
			if (ClassUtils.isBeanReference(property.getter.getReturnType()))
				filtered.add(property);
		return filtered;
	}

	/**
	 * Collections are <code>Map</code> or <code>Collection</code> implementations, wrapping any type.
	 * @return a new List containing collection properties, taken from a List retrieved via <code>getPersistentProperties()</code>.
	 */
	public static List<BeanReflectUtil.BeanProperty> getCollectionProperties(List<BeanReflectUtil.BeanProperty> persistentProperties)	{
		List<BeanReflectUtil.BeanProperty> filtered = new ArrayList<BeanReflectUtil.BeanProperty>();
		for (BeanReflectUtil.BeanProperty property : persistentProperties)
			if (ClassUtils.isCollection(property.getter.getReturnType()))
				filtered.add(property);
		return filtered;
	}

	/**
	 * Collections are <code>Map</code> or <code>Collection</code> implementations, wrapping any type.
	 * @return a new List containing non-collection properties (standard and references), taken from a List retrieved via <code>getPersistentProperties()</code>.
	 */
	public static List<BeanReflectUtil.BeanProperty> getNonCollectionProperties(List<BeanReflectUtil.BeanProperty> persistentProperties)	{
		List<BeanReflectUtil.BeanProperty> filtered = new ArrayList<BeanReflectUtil.BeanProperty>();
		for (BeanReflectUtil.BeanProperty property : persistentProperties)
			if (ClassUtils.isCollection(property.getter.getReturnType()) == false)
				filtered.add(property);
		return filtered;
	}

}
