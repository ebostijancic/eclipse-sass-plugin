package at.workflow.webdesk.po.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.FieldTypeAwareConnector;
import at.workflow.webdesk.po.PoGeneralDbService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.PoSourceConnectorInterface;
import at.workflow.webdesk.po.PrimaryKeyAwareConnector;
import at.workflow.webdesk.tools.BeanReflectUtil;
import at.workflow.webdesk.tools.PositionalQuery;
import at.workflow.webdesk.tools.ReflectionUtils;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.dbmetadata.PersistenceMetadata;
import at.workflow.webdesk.tools.dbmetadata.PersistenceMetadataUtil;

/**
 * This provides reflective reading of properties and their values from Hibernate POJOs (domain objects).
 * 
 * @author fritzberger 24.02.2012
 * 
 * It was reading connector but without PoSourceConnectorInterface. Added.
 * 
 * sdzuban 19.07.2012
 */
public abstract class PoAbstractDomainObjectReadingConnector extends PoAbstractDestinationConnector 
	implements PoSourceConnectorInterface, FieldTypeAwareConnector, PrimaryKeyAwareConnector {

	protected static final String BEANREFERENCE_PROPERTY_PREFIX = "$";
	
	/** Public for unit tests. */
    public static final String LIST_OF_CONNECTOR_FIELDS_IS_NULL = "List of connector-fields is null!";
    public static final String SELF_REFERENCE = BEANREFERENCE_PROPERTY_PREFIX + "this";

    
	private PoGeneralDbService dbService;
	
	private PersistenceMetadata metaData;
	
	private final Map<String, Class<?>> fieldTypes = new HashMap<String, Class<?>>();
	
	private boolean fieldTypesLoaded = false;
    
	/**
	 * Called from <code>getFieldNames()</code> to retrieve ORM-fields by reflection.
	 * @return the domain-object class this connector deals with, e.g. return PoAction.class.
	 */
	protected abstract Class<? extends PersistentObject> getDomainObjectClass();

	/**
	 * A natural key is a unique not-null attribute of the entity type given by <code>getDomainObjectClass()</code>.
	 * @param beanReferencePropertyName the name of the bean reference, without leading "$", e.g. "costCenter".
	 * @param bean the bean hopefully holding the desired natural key value.
	 * @return a natural key value that identifies the given domain object uniquely.
	 */
	protected abstract Object getNaturalKeyValueFor(String beanReferencePropertyName, PersistentObject bean);

	/** Uses a PoGeneralDbService for reading source data, this is allocated here. */
    @Override
    public void init() {
		dbService = (PoGeneralDbService) getBean("PoGeneralDbService");
		metaData = (PersistenceMetadata) getBean("PersistenceMetadata");
		loadFieldTypes(getDomainObjectClass());
    }
    
	/**
	 * This default implementation delegates to <code>getFieldNames()</code>
	 * using the abstract <code>getDomainObjectClass()</code> to find out the POJO class.
	 * It then sorts the names alphabetically.
	 * {@inheritDoc}
	 */
	@Override
	protected void initializeFieldNames() {
		List<String> fieldNames = getFieldNames(getDomainObjectClass());
		for (String fieldName : fieldNames)
			addFieldName(fieldName);
	}
	
	// export methods
	
	/**
	 * @return a List of all <i>object-relational-mapped</i> field names of passed POJO class,
	 * 		excluding Collections and Maps, including bean references (foreign entities).
	 */
	protected List<String> getFieldNames(Class<? extends PersistentObject> domainObjectClass) {
		assert domainObjectClass != null : "Can not return field names of a null POJO class!";
		
		List<BeanReflectUtil.BeanProperty> persistentProperties =
			PersistenceMetadataUtil.getPersistentProperties(domainObjectClass, excludedFieldNames(), metaData);
		
		List<String> fieldNames = new ArrayList<String>();
		// add standard-properties with original name
		for (BeanReflectUtil.BeanProperty field : PersistenceMetadataUtil.getStandardProperties(persistentProperties))
			// fri_2013-05-13: enums are currently ignored - TODO
			fieldNames.add(field.propertyName);
		
		// add bean references with name preceded by "$"
		for (BeanReflectUtil.BeanProperty field : PersistenceMetadataUtil.getBeanReferenceProperties(persistentProperties))
			fieldNames.add(BEANREFERENCE_PROPERTY_PREFIX+field.propertyName);
		
		fieldNames.add(SELF_REFERENCE);
		
		return fieldNames;
	}

	/**
	 * fills Map of Types of all <i>object-relational-mapped</i> field names of passed POJO class,
	 * 		excluding Collections and Maps, including bean references (foreign entities).
	 */
	private void loadFieldTypes(Class<? extends PersistentObject> domainObjectClass) {
		assert domainObjectClass != null : "Can not return field types of a null POJO class!";
		
		if(fieldTypesLoaded == false) {
			
			List<BeanReflectUtil.BeanProperty> persistentProperties =
					PersistenceMetadataUtil.getPersistentProperties(domainObjectClass, excludedFieldNames(), metaData);
			
			// add standard-properties with original name
			for (BeanReflectUtil.BeanProperty field : PersistenceMetadataUtil.getStandardProperties(persistentProperties))
				fieldTypes.put(field.propertyName, field.propertyClass);
	
			// add bean references with name preceded by "$" that are usually mapped to String (shortName, name, code, ...)
			for (BeanReflectUtil.BeanProperty field : PersistenceMetadataUtil.getBeanReferenceProperties(persistentProperties)) {
				String fieldName = BEANREFERENCE_PROPERTY_PREFIX + field.propertyName;
				fieldTypes.put(fieldName, getTypeForReferenceField(fieldName));
			}

			fieldTypes.put(SELF_REFERENCE, domainObjectClass);
			
			fieldTypesLoaded = true;
		}
	}

	/**
	 * Can be overridden by concrete implementation which is necessary when bean reference type is not String
	 */
	protected Class<?> getTypeForReferenceField(String fieldName) {
		return String.class;
	}
	
	@Override
	public Class<?> getTypeOfField(String fieldId) {
		if(fieldTypesLoaded == false) {
			throw new IllegalStateException("The method loadFieldTypes was not yet called!");
		}
		return fieldTypes.get(fieldId);
	}
	
	/**
	 * Default implementation that calls <code>findDomainObjects()</code> and turns the resulting
	 * persistent objects into records (Map).
	 */
	@Override
	public final List<Map<String,Object>> findAllObjects(List<String> fieldNames, String constraint) {
		if (assertFindArguments(fieldNames) == false)
			return new ArrayList<Map<String,Object>>();
		
		Collection<? extends PersistentObject> domainObjects = findDomainObjects(constraint);
		List<Map<String,Object>> records = new ArrayList<Map<String,Object>>();
		for (PersistentObject domainObject : domainObjects)
			records.add(domainObjectToRecord(fieldNames, domainObject));

		return records;
	}

	/**
	 * Reads persistent objects from exactly one entity type (database table).
	 * Called from <code>findAllObjects()</code>.
	 * @param constraint an optional runtime-estimated WHERE clause to be appended to the query.
	 * @param fieldNames optional just for the possibility to launch a selective query (selecting just several properties).
	 * @return the persistent objects conforming to given constraint, using the entity given by getDomainObjectClass().
	 */
	@SuppressWarnings("unchecked")
	protected List<? extends PersistentObject> findDomainObjects(String constraint)	{
		String queryText = "from "+ getDomainObjectClass().getSimpleName();
		if (constraint != null && constraint.trim().length() > 0)
			queryText += " where " + constraint;
		return dbService.getElementsAsList(queryText, null);
	}

    /**
     * @param key the name of the field to query.
     * @param value the value the given field must have.
     * @return exactly one domain object that conforms to given constraint.
     */
	protected final PersistentObject findObject(String key, Object value) {
		String queryText = "from " + getDomainObjectClass().getSimpleName()+" where "+key+" = ?";
		PositionalQuery query = new PositionalQuery(queryText, new Object[] { value }); 
		List<?> objects = dbService.getElementsAsList(query);
		
    	if (objects.size() > 1)	// illegal case
    		throw new PoRuntimeException("Field " + key + " seems to be not unique in " + getDomainObjectClass().getSimpleName());
    	else if (objects.size() == 1)
    		return (PersistentObject) objects.get(0);	// found existing one
    	else // not found, this is legal
    		return null;
	}

	/**
	 * This is called from <code>findAllObjects()</code>.
	 * @return a Map of all <i>object-relational-mapped</i> fields of passed POJO,
	 * 		key is property name (e.g. "count" for "getCount() method"),
	 * 		value is the value the POJO holds for that property (result of calling e.g. "getCount()").
	 */
	private Map<String,Object> domainObjectToRecord(List<String> fieldNames, PersistentObject domainObject) {
		assert domainObject != null : "Can not return field values of a null POJO!";
		assert fieldNames != null && fieldNames.size() > 0 : "No field names present to read values for!";
		
		Map<String,Object> record = new HashMap<String,Object>();
		
		for (final String fieldName : fieldNames)    {
			
			if (fieldName.equals( SELF_REFERENCE )) {
				record.put( fieldName, domainObject );
			} else {
				
				final boolean isBeanReference = fieldName.startsWith(BEANREFERENCE_PROPERTY_PREFIX);
				final String propertyName = isBeanReference
				? fieldName.substring(BEANREFERENCE_PROPERTY_PREFIX.length())
						: fieldName;
				
				if (shouldExportField(propertyName))	{
					try {
						Object value = ReflectionUtils.invokeGetter(domainObject, propertyName);
						
						if (isBeanReference)
							value = getNaturalKeyValueFor(propertyName, (PersistentObject) value);
						
						record.put(fieldName, value);	
					}
					catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		
		afterExport(record, domainObject, fieldNames);
		
		return record;
	}

	/**
	 * Called before getting a property from domain object and putting it into record.
	 * This implementation returns true. Override to prevent properties from being read from POJO.
	 * @return true when the given property should be set from record to domain object.
	 */
	protected boolean shouldExportField(String propertyName) {
		return true;
	}

	/**
	 * Called after a record has been packed with all its property values.
	 * This implementation does nothing. Override to pack additional information into the record.
	 */ 
	protected void afterExport(Map<String,Object> result, PersistentObject domainObject, List<String> fieldNames)	{
	}


	private boolean assertFindArguments(List<String> fieldNames) {
		if (fieldNames == null || fieldNames.isEmpty())
			throw new IllegalArgumentException(LIST_OF_CONNECTOR_FIELDS_IS_NULL);
		
		return true;
	}

	@Override
	public void postProcessImportedRecords(List<Map<String,Object>> records) {
		// nothing to do
	}


	protected PersistenceMetadata getMetaData() {
		return metaData;
	}
}
