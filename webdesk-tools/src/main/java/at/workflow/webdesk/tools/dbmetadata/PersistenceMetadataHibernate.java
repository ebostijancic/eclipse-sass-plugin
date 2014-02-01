package at.workflow.webdesk.tools.dbmetadata;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.OneToMany;

import org.hibernate.MappingException;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.CascadeStyle;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.OneToOne;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Value;

import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.hibernate.ExtLocalSessionFactoryBean;

/**
 * Utility to read essential informations from database metadata, like
 * maximum length of a text column, or if a column is nullable.
 * 
 * @author fritzberger 20.10.2010
 */
public class PersistenceMetadataHibernate implements PersistenceMetadata {

	private String sessionFactoryBeanName = "webdesk-SessionFactory";

	/** {@inheritDoc} */
	@Override
	public int getMaximumLength(Class<? extends PersistentObject> persistentObject, String propertyName)	{
		PersistentClass persistenceClass = getPersistentClass(persistentObject);
		Column column = getDatabaseColumnSupportingLength(persistenceClass, propertyName);
		return column != null ? column.getLength() : 0;
	}
	
	/** {@inheritDoc} */
	@Override
	public int getMaximumLength(String className, String propertyName) {
		PersistentClass persistentClass = getPersistentClass(className);
		Column column = getDatabaseColumnSupportingLength(persistentClass, propertyName);
		return column != null ? column.getLength() : 0;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean isNullable(Class<? extends PersistentObject> persistentClass, String propertyName)	{
		PersistentClass persistenceClass = getPersistentClass(persistentClass);
		return isNullable(propertyName, persistenceClass);
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean isNullable(String className, String propertyName) {
		PersistentClass persistenceClass = getPersistentClass(className);
		return isNullable(propertyName, persistenceClass);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isMapped(Class<? extends PersistentObject> persistentClass, String propertyName)	{
		PersistentClass persistenceClass = getPersistentClass(persistentClass);
		if (isMapped(persistenceClass, propertyName))
			return true;
		
		do	{
			persistenceClass = persistenceClass.getSuperclass();
			if (persistenceClass != null)	{
				if (isMapped(persistenceClass, propertyName))
					return true;
				persistenceClass = persistenceClass.getSuperclass();
			}
		}
		while (persistenceClass != null);
		
		return false;
	}
	
	/** {@inheritDoc} */
	@Override
	public List<String> getPropertyNames(String className) {
		PersistentClass persistentClass = getPersistentClass(className);
		List<String> propertyNames = new ArrayList<String>();
		
		while (persistentClass != null)	{
			@SuppressWarnings("unchecked")
			Iterator<Property> propertyIterator = persistentClass.getPropertyIterator();
			
			while (propertyIterator.hasNext()){
				Property property = propertyIterator.next();
				String propertyName = property.getName();
				propertyNames.add(propertyName);
			}
			
			persistentClass = persistentClass.getSuperclass();
		}
		return propertyNames;
	}
	
	/** {@inheritDoc} */
	@Override
	public List<String> getPropertyNames(Class<? extends PersistentObject> domainObjectClass) {
		return getPropertyNames(domainObjectClass.getName());
	}

	/** {@inheritDoc} */
	@Override
	public List<Class<? extends PersistentObject>> getMappedClasses()	{
		final List<Class<? extends PersistentObject>> classes = new ArrayList<Class<? extends PersistentObject>>();
		Configuration configuration = getPersistenceLayerConfiguration();
		
		@SuppressWarnings("rawtypes")
		Iterator mappedClasses = configuration.getClassMappings();
		while (mappedClasses.hasNext())	{
			PersistentClass mappedClass = (PersistentClass) mappedClasses.next();
			String className = mappedClass.getClassName();
			
			try {
				@SuppressWarnings("unchecked")
				final Class<? extends PersistentObject> clazz = (Class<? extends PersistentObject>) Class.forName(className);
				classes.add(clazz);
			}
			catch (ClassNotFoundException e) {	// Not very likely to happen after successful Hibernate boot
				throw new RuntimeException(e);
			}
		}
		return classes;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getTableName(Class<? extends PersistentObject> domainObjectClass)	{
		PersistentClass persistentClass = getPersistentClass(domainObjectClass);
		return persistentClass.getTable().getName();
	}
	
	
	/** {@inheritDoc} */
	@Override
	public List<String> getForeignKeyConstraintNames(Class<? extends PersistentObject> domainObjectClass)	{
		return new ArrayList<String>(getForeignEntityInformation(domainObjectClass, true).values());
	}
	
	/** {@inheritDoc} */
	@Override
	public Map<String,String> getForeignEntityNames(Class<? extends PersistentObject> domainObjectClass)	{
		return getForeignEntityInformation(domainObjectClass, false);
	}
	
	private Map<String,String> getForeignEntityInformation(Class<? extends PersistentObject> domainObjectClass, boolean retrieveForeignKeyConstraintNames)	{
		final Map<String,String> foreignKeyInfo = new HashMap<String,String>();
		final PersistentClass persistentClass = getPersistentClass(domainObjectClass);
		
		@SuppressWarnings("rawtypes")
		final Iterator foreignKeys = persistentClass.getTable().getForeignKeyIterator();
		while (foreignKeys.hasNext())	{
			final ForeignKey fk = (ForeignKey) foreignKeys.next();
			final String columnName = fk.getColumn(0).getName();	// must throw NPE when there is no column at all
			foreignKeyInfo.put(columnName, retrieveForeignKeyConstraintNames ? fk.getName() : fk.getReferencedEntityName());
		}
		return foreignKeyInfo;
	}
	
	
	/** {@inheritDoc} */
	@Override
	public Map<String,String> getCascadingOneToOneForeignEntities(Class<? extends PersistentObject> domainObjectClass)	{
		final PersistentClass persistentClass = getPersistentClass(domainObjectClass);
		final Map<String,String> oneToOneRelations = new HashMap<String,String>();
		
		@SuppressWarnings("rawtypes")
		final Iterator properties = persistentClass.getPropertyIterator();
		while (properties.hasNext())	{
			final Property property = (Property) properties.next();
			final Value value = property.getValue();
			final boolean oneToOne = (value != null && value.getClass().equals(OneToOne.class));
			// TODO: OneToOne.class is Hibernate-specific, solve this by Visitor
			
			if (oneToOne)	{
				final CascadeStyle cascadeStyle = property.getCascadeStyle();
				if (cascadeStyle != null && cascadeStyle != CascadeStyle.NONE)	{
					final String propertyName = property.getName();
					final String className = getPropertyType(domainObjectClass, propertyName);
					oneToOneRelations.put(propertyName, className);
				}
			}
		}
		return oneToOneRelations;
	}
	

	/** {@inheritDoc} */
	@Override
	public List<String> getIndexNames(Class<? extends PersistentObject> domainObjectClass)	{
		final List<String> indexNames = new ArrayList<String>();
		PersistentClass persistentClass = getPersistentClass(domainObjectClass);
		
		@SuppressWarnings("rawtypes")
		Iterator indexes = persistentClass.getTable().getIndexIterator();
		while (indexes.hasNext())	{
			Index idx = (Index) indexes.next();
			indexNames.add(idx.getName());
		}
		return indexNames;
	}
	
	/**
	 * {@inheritDoc}
	 * @throws IllegalArgumentException when the given property is not in properties of given class.
	 */
	@Override
	public String getAttributeName(Class<? extends PersistentObject> domainObjectClass, String propertyName) {
		final PersistentClass persistentClass = getPersistentClass(domainObjectClass);
		
		@SuppressWarnings("unchecked")
		Property property = findProperty(persistentClass.getPropertyIterator(), propertyName);
		
		if (property == null && persistentClass.getIdentifierProperty().getName().equals(propertyName))	// look if it is the primary key
			property = persistentClass.getIdentifierProperty();
		
		if (property == null)
			throw new IllegalArgumentException("Property '"+propertyName+"' not found in class "+persistentClass);
		
		Column column = null;
		@SuppressWarnings("rawtypes")
		Iterator columnIterator = property.getColumnIterator();
		if (columnIterator != null && columnIterator.hasNext())
			column = (Column) columnIterator.next();	// must throw exception when not cast-able!
		
		return (column != null) ? column.getName() : property.getName();
	}
	
	@Override
	public String getPropertyName(Class<? extends PersistentObject> domainObjectClass, String attributeName) {
		final PersistentClass persistentClass = getPersistentClass(domainObjectClass);
		
		@SuppressWarnings("unchecked")
		Property property = findColumn(persistentClass.getPropertyIterator(), attributeName);
		
		if (property == null)	{
			Column column = null;
			@SuppressWarnings("rawtypes")
			Iterator columnIterator = persistentClass.getIdentifierProperty().getColumnIterator();
			if (columnIterator != null && columnIterator.hasNext())
				column = (Column) columnIterator.next();	// must throw exception when not cast-able!
			
			if (column != null && column.getName().equals(attributeName))
				property = persistentClass.getIdentifierProperty();
		}
		
		if (property == null)
			throw new IllegalArgumentException("Attribute '"+attributeName+"' not found in table "+persistentClass);
		
		return property.getName();
	}
	
	
	@Override
	public String getPropertyType(Class<? extends PersistentObject> domainObjectClass, String propertyName) {
		final PersistentClass persistentClass = getPersistentClass(domainObjectClass);
		
		@SuppressWarnings("unchecked")
		Property property = findProperty(persistentClass.getPropertyIterator(), propertyName);
		
		if (property == null && persistentClass.getIdentifierProperty().getName().equals(propertyName))	// look if it is the primary key
			property = persistentClass.getIdentifierProperty();
		
		if (property == null)
			throw new IllegalArgumentException("Property '"+propertyName+"' not found in class "+persistentClass);
			// TODO: this maybe occurs because we do not search in super-classes here!
		
		return property.getGetter(domainObjectClass).getReturnType().getName();
	}
	
	
	@Override
	public String getCollectionType(Class<? extends PersistentObject> domainObjectClass, String propertyName) {
		final Property property = getPropertyOrThrowException(domainObjectClass, propertyName);
		final Value value = property.getValue();
		final Value referencedEntityName = ((org.hibernate.mapping.Collection) value).getElement();
		if (referencedEntityName instanceof org.hibernate.mapping.OneToMany)
			return ((org.hibernate.mapping.OneToMany) referencedEntityName).getReferencedEntityName();
		
		if (referencedEntityName instanceof org.hibernate.mapping.SimpleValue)	{
			// TODO: for class PoAction and property attributes, table name is "PoActionAttributes" - but how to find f.q. class name?
			//return ((org.hibernate.mapping.SimpleValue) referencedEntityName).getTable().getName();
		}
		
		return null;
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public String getMappedByPropertyName(Class<? extends PersistentObject> domainObjectClass, String propertyName)	{
		int i;
		do	{
			i = propertyName.indexOf(".");
			if (i > 0)	{
				try {
					final String firstPart = propertyName.substring(0, i);
					final String nextPart = propertyName.substring(i + 1);
					domainObjectClass = (Class<? extends PersistentObject>) Class.forName(getPropertyType(domainObjectClass, firstPart));
					propertyName = nextPart;
				}
				catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
		}
		while (i > 0);
		
		final RuntimeException notFound = new RuntimeException("Can not find Java field '"+propertyName+"' in "+domainObjectClass);
		
		//final Property property = getPropertyOrThrowException(domainObjectClass, propertyName);
		// fri_2013-05-03: could not find such information in Hibernate metaData.
		// Maybe here is a way to find this out: http://www.massapi.com/source/hibernate-distribution-3.6.5.Final/project/hibernate-envers/src/main/java/org/hibernate/envers/configuration/metadata/CollectionMetadataGenerator.java.html
		
		// For now the strategy is: read the annotation, ignore XML, which presumes that no XML was used for HR data model.
		while (domainObjectClass != null)	{
			try {
				final Field javaField = domainObjectClass.getDeclaredField(propertyName);
				OneToMany annotation = javaField.getAnnotation(OneToMany.class);
				return annotation.mappedBy();
			}
			catch (NoSuchFieldException e)	{	// search in super-class
				final Class<?> superClass = domainObjectClass.getSuperclass();
				if (PersistentObject.class.isAssignableFrom(superClass) == false)
					throw new RuntimeException(e);
				
				domainObjectClass = (Class<? extends PersistentObject>) superClass;
			}
		}
		
		throw notFound;
	}
	

	@Override
	public String getPrimaryKeyConstraintName(Class<? extends PersistentObject> domainObjectClass)	{
		PersistentClass persistentClass = getPersistentClass(domainObjectClass);
		return persistentClass.getTable().getPrimaryKey().getName();
	}
	
	
	@Override
	public boolean isUniqueProperty(Class<? extends PersistentObject> beanClass, String propertyName) {
		final Property property = getPropertyOrThrowException(beanClass, propertyName);
		final Iterator<?> columns = property.getColumnIterator();
		if (columns.hasNext())	{
			final Column column = (Column) columns.next();
			return column.isUnique();
		}
		return false;
	}
	
	
	
	
	private Configuration getPersistenceLayerConfiguration() {
		return ExtLocalSessionFactoryBean.getConfiguration(sessionFactoryBeanName);
	}
	
	private boolean isMapped(PersistentClass pc, String propertyName)	{
		try	{
			@SuppressWarnings("unchecked")
			Property p = findProperty(pc.getPropertyIterator(), propertyName);
			// is mapped when there is a property and it is not an entity-collection (back-references)
			return p != null && p.getType().isCollectionType() == false;
		}
		catch (MappingException e)	{	// this always is thrown in case of a non-mapped property
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	private boolean isNullable(String propertyName, PersistentClass persistentClass) {
		Property p = findProperty(persistentClass.getPropertyIterator(), propertyName);
		if (p != null)
			return p.isOptional();

		do	{
			persistentClass = persistentClass.getSuperclass();
			if (persistentClass != null)	{
				p = findProperty(persistentClass.getPropertyIterator(), propertyName);
				if (p != null)
					return p.isOptional();
				persistentClass = persistentClass.getSuperclass();
			}
		}
		while (persistentClass != null);
		return true;	// not found, assume it is nullable
	}
	
	private Property getPropertyOrThrowException(Class<? extends PersistentObject> domainObjectClass, String propertyName) {
		PersistentClass persistenceClass = getPersistentClass(domainObjectClass);
		
		Property property = null;
		while (property == null && persistenceClass != null)	{
			@SuppressWarnings("unchecked")
			final Iterator<Property> properties = persistenceClass.getPropertyIterator();
			property = findProperty(properties, propertyName);
			if (property == null)
				persistenceClass = persistenceClass.getSuperclass();
		}
		
		if (property == null)
			throw new IllegalArgumentException("Property '"+propertyName+"' not found in "+domainObjectClass);
		
		return property;
	}
	
	private Property findProperty(Iterator<Property> it, String propertyName) {
		while (it.hasNext())	{
			final Property property = it.next();
			if (property.getName().equals(propertyName))
				return property;
			
			if (property.isComposite())	 {
				@SuppressWarnings("unchecked")
				final Property child = findProperty(((Component) property.getValue()).getPropertyIterator(), propertyName);
				if (child != null)
					return child;
			}
		}
		return null;
	}

	private Property findColumn(Iterator<Property> it, String columnName) {
		while (it.hasNext())	{
			final Property property = it.next();
			Column column = null;
			@SuppressWarnings("rawtypes")
			Iterator columnIterator = property.getColumnIterator();
			if (columnIterator != null && columnIterator.hasNext())
				column = (Column) columnIterator.next();	// must throw exception when not cast-able!
			
			if (column != null && column.getName().equals(columnName))
				return property;
			
			if (property.isComposite())	 {
				@SuppressWarnings("unchecked")
				final Property child = findColumn(((Component) property.getValue()).getPropertyIterator(), columnName);
				if (child != null)
					return child;
			}
		}
		return null;
	}

	/** @throws UnsupportedOperationException when the given property does not support a length! */
	private Column getDatabaseColumnSupportingLength(PersistentClass pc, String propertyName)	{
		@SuppressWarnings("unchecked")
		Property p = findProperty(pc.getPropertyIterator(), propertyName);
		Object o = null;
		if (p != null)
			o = p.getColumnIterator().next();	// must throw exception when non-mapped!
		return (Column) o;	// must throw exception when not cast-able!
	}
	
	private PersistentClass getPersistentClass(Class<? extends PersistentObject> persistentClass)	{
		return getPersistentClass(persistentClass.getName());
	}

	private PersistentClass getPersistentClass(String persistentClassName)	{
		Configuration configuration = getPersistenceLayerConfiguration();
		return configuration.getClassMapping(persistentClassName);
	}
	


	/** Spring XML noise, do not use. */
	public void setSessionFactoryBeanName(String sessionFactoryBeanName) {
		this.sessionFactoryBeanName = sessionFactoryBeanName;
	}

}
