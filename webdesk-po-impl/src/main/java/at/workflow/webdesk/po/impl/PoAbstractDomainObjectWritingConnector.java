package at.workflow.webdesk.po.impl;

import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.tools.BeanReflectUtil;
import at.workflow.webdesk.tools.ReflectionUtils;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.dbmetadata.PersistenceMetadataUtil;

/**
 * This provides reflective saving of records (field Maps) into domain objects (Hibernate POJOs).
 * 
 * @author fritzberger 14.03.2012
 */
public abstract class PoAbstractDomainObjectWritingConnector extends PoAbstractDomainObjectReadingConnector {

	/**
	 * A natural key is a unique not-null attribute of the entity type given by <code>getDomainObjectClass()</code>.
	 * @param beanReferencePropertyName the name of the bean reference, without leading "$", e.g. "costCenter".
	 * @param naturalKeyValue the natural key value that uniquely identifies a bean.
	 * @return a domain object for given natural key value.
	 */
	protected abstract PersistentObject resolveNaturalKeyValueFrom(String beanReferencePropertyName, Object naturalKeyValue);

	/**
	 * Default implementation that imports database domain objects via JPA layer. 
	 * {@inheritDoc}
	 */
	@Override
	public final Object saveObject(Map<String,Object> record, String uniqueKeyFieldName, List<String> fieldsInCorrectOrder) {
		if (assertSaveArguments(record, uniqueKeyFieldName) == false)
			return null;
		
        // find entity, or construct one
		PersistentObject domainObject = findObject(uniqueKeyFieldName, record.get(uniqueKeyFieldName));
        if (domainObject == null)	{
			try {
				domainObject = getDomainObjectClass().newInstance();
			}
			catch (Exception e) {
				throw new PoRuntimeException(e);
			}
        }
        
        final List<BeanReflectUtil.BeanProperty> properties = PersistenceMetadataUtil.getPersistentProperties(getDomainObjectClass(), null, getMetaData());

        // put record values into domain object, resolve foreign entities
        for (Map.Entry<String,Object> entry : record.entrySet())	{
        	String propertyName = entry.getKey();
        	Object value = entry.getValue();
        	
        	final boolean isBeanReference = propertyName.startsWith(BEANREFERENCE_PROPERTY_PREFIX);
        	if (isBeanReference)
        		propertyName = propertyName.substring(BEANREFERENCE_PROPERTY_PREFIX.length());
        	
        	final boolean shouldImportField = shouldImportField(propertyName);
        	
        	if (shouldImportField)	{
            	if (isBeanReference)
                    value = resolveNaturalKeyValueFrom(propertyName, value);

            	try {	// set value into domain object
    				ReflectionUtils.invokeSetter(domainObject, propertyName, value, getArgumentClass(propertyName, properties));
    			}
    			catch (Exception e) {
    				throw new PoRuntimeException("Problems setting property '" + propertyName + "' with value='" + value + "' on domainObject=" + domainObject, e);
    			}
        	}	// else: might be processed in beforeImport() or saveDomainObject()
        }
        
        beforeImport(domainObject, record);
        saveDomainObject(domainObject);	// save to persistence (via service)
        
        return domainObject;
    }

	/**
	 * Called before setting a property from record to domain object.
	 * This implementation returns true. Override to prevent properties from being written to POJO.
	 * @return true when the given property should be set from record to domain object.
	 */
	protected boolean shouldImportField(String fieldName)	{
		return fieldName.equals( SELF_REFERENCE ) == false;
	}
	
	/**
	 * Called before a record will be saved with all its already set property values.
	 * This implementation does nothing. Override to perform additional logic related to the record.
	 */
    protected void beforeImport(PersistentObject domainObject, Map<String, Object> fields) {
	}

	/** Specific saving of an domain object via corresponding service. */
    protected abstract void saveDomainObject(PersistentObject domainObject);

    
	private Class<?> getArgumentClass(String propertyName, List<BeanReflectUtil.BeanProperty> properties) {
		for (BeanReflectUtil.BeanProperty property : properties)
			if (property.propertyName.equals(propertyName))
				return property.setter.getParameterTypes()[0];

		throw new IllegalArgumentException("Unknown property for "+getDomainObjectClass()+": "+propertyName);
	}

	private boolean assertSaveArguments(Map<String, Object> record, String uniqueKeyFieldName) {
		if (record == null)
			throw new IllegalArgumentException("Record (Map) is null on import!");
		
		if (uniqueKeyFieldName == null || uniqueKeyFieldName.trim().length() <= 0)
			throw new IllegalArgumentException("uniqueKeyFieldName is null or empty on import!");
		
		if (record.isEmpty())
			throw new IllegalArgumentException("Can not import an empty record!");
		
		return true;
	}
	
}
