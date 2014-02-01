package at.workflow.webdesk.tools.hibernate;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import at.workflow.webdesk.tools.AnnotationUtil;
import at.workflow.webdesk.tools.BeanReflectUtil.BeanProperty;
import at.workflow.webdesk.tools.api.BusinessLogicException;
import at.workflow.webdesk.tools.api.BusinessMessages;
import at.workflow.webdesk.tools.api.DomainObjectCrudService;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.date.Interval;
import at.workflow.webdesk.tools.dbmetadata.PersistenceMetadata;
import at.workflow.webdesk.tools.dbmetadata.PersistenceMetadataUtil;
import at.workflow.webdesk.tools.model.annotations.UniqueInTime;

/**
 * Generic implementations for services.
 * 
 * @author fritzberger 06.06.2013
 * @author sdzuban 14.06.2013 logging, historicization
 */
public abstract class AbstractDomainObjectCrudService<T extends PersistentObject> implements DomainObjectCrudService<T>, ApplicationContextAware
{
    private final Logger logger = Logger.getLogger(this.getClass());
    
	private ApplicationContext applicationContext;
	
	
	/** Sub-classes must deliver some DAO instance. */
	protected abstract GenericHibernateDAOImpl<T> getDao();
	
	
	/** Needing application context for PersistenceMetaData. */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	
	/** Default implementation that directly delegates to DAO. */
	@Override
	public T get(Serializable uid) {
		return getDao().get(uid);
	}
	
	/**
	 * Default implementation.
	 */
	@Override
	public BusinessMessages checkSave(T domainObject) {
		return null;
	}
	
	/**
	 * Default implementation that directly delegates to DAO after calling a unique constraint check.
	 * @param domainObject the entity to save to persistence.
	 * @return always null, to be overridden.
	 */
	@Override
	public void save(T domainObject, BusinessMessages ... messages) {
		final String done = assertAndLogBeforeSave(domainObject);
		
		// perform automatic check of all unique properties, both historicized and normal
		checkUniqueProperties(domainObject, null, getDomainObjectClass());
		
		// save to persistence
		getDao().save(domainObject);
		
		logAfterSave(domainObject, done);
	}


	/** Default implementation that directly delegates to DAO. */
	@Override
	public void delete(T domainObject) {
		final String uid = domainObject.getUID();
		logBeforeDelete(domainObject, uid, "Delete");
		
		if (StringUtils.isEmpty(uid) == false) {
			getDao().delete(domainObject);
			
			logAfterDelete(domainObject, uid, "Delete");
		}
	}


	@Override
	public void checkUniqueProperties(T domainObject, Integer collectionIndex, Class<? extends PersistentObject> owningEntityType)	{
		assert domainObject != null;
		
		final Class<? extends PersistentObject> domainClass = domainObject.getClass();
		final PersistenceMetadata metaData = (PersistenceMetadata) applicationContext.getBean("PersistenceMetadata");
		final Interval validity = getValidty(domainObject);
		
		for (BeanProperty property : PersistenceMetadataUtil.getStandardProperties(PersistenceMetadataUtil.getPersistentProperties(domainClass, null, metaData)))	{
			if (metaData.isUniqueProperty(domainClass, property.propertyName) || isUniqueInTime(domainClass, property.propertyName))	{
				final Object value;
				try {
					value = property.getter.invoke(domainObject);
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
				
				if (value != null && getDao().isAttributeUnique(property.propertyName, value, domainObject.getUID(), validity) == false)	{
					if (collectionIndex == null || owningEntityType == null)
						throw new BusinessLogicException("po_error_unique_constraint_violation", domainClass, property.propertyName);
					else
						// TODO: this might not work ... what is propertyName, where is the property-name behind the index []
						throw new BusinessLogicException("po_error_unique_constraint_violation", owningEntityType, property.propertyName+"["+collectionIndex+"]");
				}
			}
		}
	}


	/** @return null. To be overridden by historicizing service variant. TODO what does null mean? eternal or invalid?*/
	@SuppressWarnings("unused")	// parameter is for overriders
	protected Interval getValidty(T domainObject) {
		return null;
	}
	

	/**
	 * Call this to check unique properties of related PersistentObject instances.
	 * For instance you need to check all elements in Collection "skills" when saving a HrSkillGroup.
	 * @param collection the collection of the entities to check for uniqueness.
	 * @param service the service to use for checking uniqueness.
	 */
	protected <D extends PersistentObject> void checkRelatedUniqueProperties(Collection<D> collection, DomainObjectCrudService<D> service) {
		int i = 0;
		for (D domainObject : collection)	{
			service.checkUniqueProperties(domainObject, new Integer(i), getDomainObjectClass());
			i++;
		}
	}


	
	protected final boolean isUniqueInTime(Class<? extends PersistentObject> beanClass, String propertyName) {
		for (Field field : AnnotationUtil.findAnnotatedFields(beanClass, UniqueInTime.class))	{
			if (propertyName.equals(field.getName()))	{
				return true;
			}
		}
		return false;
	}



	// logging

	private String assertAndLogBeforeSave(T domainObject) {
		if (domainObject == null)
			throw new IllegalArgumentException("Domain object to save must be non-null");
		
		if (logger.isDebugEnabled() == false)
			return null;
		
		final String uid = domainObject.getUID();
		final String uidString = (uid == null) ? "" : " UID=" + uid; 
		String toDo = "update ", done = "Updated ";
		if (uid == null) {
			toDo = "save new ";
			done = "Saved new ";
		}
		
		logger.debug("Going to " + toDo + domainObject.getClass().getSimpleName() + " " + uidString);
		
		return done;
	}
	
	private void logAfterSave(T domainObject, final String done) {
		if (logger.isDebugEnabled() == false)
			return;
		
		logger.debug(done + domainObject.getClass().getSimpleName() + " UID=" + domainObject.getUID());
	}
	
	protected final void logBeforeDelete(T domainObject, String uid, String kindOfDelete) {
		if (logger.isDebugEnabled() == false)
			return;
		
		if (StringUtils.isEmpty(uid))
			logger.debug(kindOfDelete+": ignoring domain object without UID");
		else
			logger.debug("Going to "+kindOfDelete+" " + domainObject.getClass().getSimpleName() + " with UID=" + uid);
	}

	protected final void logAfterDelete(T domainObject, final String uid, String kindOfDelete) {
		if (logger.isDebugEnabled() == false)
			return;
		
		logger.debug(kindOfDelete+" " + domainObject.getClass().getSimpleName() + " with UID=" + uid);
	}

}
