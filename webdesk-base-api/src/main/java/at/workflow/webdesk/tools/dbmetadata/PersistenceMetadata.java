package at.workflow.webdesk.tools.dbmetadata;

import java.util.List;
import java.util.Map;

import at.workflow.webdesk.tools.api.PersistentObject;

/**
 * Provides database meta-information e.g. name of a property, maximum length of a column etc.
 * 
 * @author sfeichter 26.01.2011
 * @author fritzberger 2011, 2012 refactorings, extensions
 */
public interface PersistenceMetadata {

	/**
	 * The maximum length of a database column.
	 * @param persistentClass the POJO persistence class the passed field is in.
	 * @param propertyName the name of the field whose length should be returned.
	 * @return the maximum length of the according database attribute.
	 * @exception MappingException when passed property is non-mapped.
	 */
	int getMaximumLength(Class<? extends PersistentObject> persistentClass, String propertyName);

	/**
	 * The maximum length of a database column.
	 * @param className the name of the POJO persistence class the passed field is in.
	 * @param propertyName the name of the field whose length should be returned.
	 * @return the maximum length of the according database attribute.
	 * @exception MappingException when passed property is non-mapped.
	 */
	public int getMaximumLength(String className, String propertyName);
	
	/**
	 * True when the database column according to passed Java field is nullable.
	 * @param persistentClass the POJO persistence class the passed field is in.
	 * @param propertyName the name of the field whose nullability should be returned.
	 * @return true when the according database attribute is nullable.
	 * @exception MappingException when passed property is non-mapped.
	 */
	boolean isNullable(Class<? extends PersistentObject> persistentClass, String propertyName);
	
	/**
	 * True when the database column according to passed Java field is nullable.
	 * @param className the name of the POJO persistence class the passed field is in.
	 * @param propertyName the name of the field whose nullability should be returned.
	 * @return true when the according database attribute is nullable.
	 * @exception MappingException when passed property is non-mapped.
	 */
	boolean isNullable(String className, String propertyName);

	/**
	 * @param persistentClass the POJO persistence class the passed field is in.
	 * @param propertyName the name of the field to be checked.
	 * @return true when there is no according database attribute for passed property,
	 * 		or it is a collection (e.g. back-references).
	 */
	public boolean isMapped(Class<? extends PersistentObject> persistentClass, String propertyName);

	/**
	 * @param className the name of the class for which property names should be retrieved.
	 * @return all property names of given class, and its super-classes, excluding primary key (UID).
	 */
	List<String> getPropertyNames(String className);
	
	/**
	 * @param domainObjectClass class of which property names should be retrieved.
	 * @return all property names of given class, excluding primary key (UID).
	 */
	List<String> getPropertyNames(Class<? extends PersistentObject> domainObjectClass);

	
	/**
	 * @return a list of concrete and abstract classes that are mapped to database tables.
	 */
	List<Class<? extends PersistentObject>> getMappedClasses();

	/**
	 * @return the database table name of given mapped class.
	 */
	String getTableName(Class<? extends PersistentObject> domainObjectClass);
	
	/**
	 * @return the database attribute name for given mapped class and property.
	 * @throws IllegalArgumentException when the given property is not among properties of given class.
	 */
	String getAttributeName(Class<? extends PersistentObject> domainObjectClass, String propertyName);
	
	/**
	 * @return the database attribute name for given mapped class and property.
	 * @throws IllegalArgumentException when the given column-name is not among columns of given class.
	 */
	String getPropertyName(Class<? extends PersistentObject> domainObjectClass, String attributeName);
	
	/**
	 * @return the name of the primary key constraint.
	 */
	String getPrimaryKeyConstraintName(Class<? extends PersistentObject> domainObjectClass);
	
	/**
	 * @return a list of database foreign key constraint names of given mapped class.
	 */
	List<String> getForeignKeyConstraintNames(Class<? extends PersistentObject> domainObjectClass);

	/**
	 * @return a list of database index names of given mapped class.
	 */
	List<String> getIndexNames(Class<? extends PersistentObject> domainObjectClass);

	/**
	 * Finds foreign key relations, except mandatory one-to-one relations (where primary key is foreign key).
	 * @return the key = column-name (not property-name!), value = foreign entity class-name.
	 * 		This will work only for foreign keys that use exactly one column.
	 * 		Mind that the column-name is NOT the Java property-name but the database attribute-name!
	 */
	Map<String,String> getForeignEntityNames(Class<? extends PersistentObject> domainObjectClass);

	/**
	 * @param domainObjectClass the class to inspect for one-to-one relations.
	 * @return a Map with key = property-name, value = class-name of foreign entity-type. 
	 */
	Map<String,String> getCascadingOneToOneForeignEntities(Class<? extends PersistentObject> domainObjectClass);

	/**
	 * @param propertyName the Java property-name for given mapped class and property.
	 * @return the class-name of the data type of the given property.
	 * @throws IllegalArgumentException when the given column-name is not among properties of given class.
	 */
	String getPropertyType(Class<? extends PersistentObject> domainObjectClass, String propertyName);

	/**
	 * ClassUtils.isCollection(clazz) would tell you if a property is a Collection.
	 * @return the fully qualified Java class name of the <b>elements</b> of the Collection the given property represents.
	 */
	String getCollectionType(Class<? extends PersistentObject> domainObjectClass, String propertyName);

	/**
	 * Retrieves the name written in "mappedBy" annotation or XML attribute.
	 * @param beanClass the class to inspect, containing given propertyName.
	 * @param propertyName the name of the property to search "mappedBy" for.
	 * @return the name written in "mappedBy", either from XML or from annotation, null when not found.
	 */
	String getMappedByPropertyName(Class<? extends PersistentObject> beanClass, String propertyName);

	/**
	 * @param propertyName the name of the property to check (Java property, maybe not identical with database attribute name).
	 * @return true when given property has a unique constraint defined.
	 */
	boolean isUniqueProperty(Class<? extends PersistentObject> beanClass, String propertyName);
	
}
