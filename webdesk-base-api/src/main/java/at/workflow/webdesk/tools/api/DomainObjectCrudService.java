package at.workflow.webdesk.tools.api;

import java.io.Serializable;

/**
 * This service is to be used by business-logic agnostic UIs like the Vaadin FormViewGenerator.
 * CRUD = Create, Read, Update, Delete. 
 * <p/>
 * Not yet supported: creating new relation- or entity-objects.
 * Meanwhile the UI is expected to call Class.newInstance() when needing a new domain objects.
 * 
 * @author ggruber 15.05.2013
 * @param <T> POJO the type managed by this service.
 */
public interface DomainObjectCrudService <T extends PersistentObject>  {
	
	/**
	 * @return the entity identified uniquely by given uid which must not be null
	 * @throws RuntimeException when null was passed or the given uid could not be found.
	 */
	T get(Serializable uid);
	
	/**
	 * This method shall be called before calling save() to obtain messages about object for display to the user.
	 * @param domainObject the entity which should be investigated.
	 * @return business messages about domain object without saving the object.
	 */
	BusinessMessages checkSave(T domainObject);
	
	/**
	 * @param domainObject the entity which should be saved.
	 * @throws RuntimeException when a business logic error occurs, or an error occurs in persistence layer.
	 */
	void save(T domainObject, BusinessMessages ... messages);
	
	/**
	 * @param domainObject the entity which should be deleted.
	 * @throws RuntimeException when a business logic error occurs, or an error occurs in persistence layer.
	 */
	void delete(T domainObject);

	/**
	 * @return the type this CRUD service supports, e.g. <code>PoPerson.class</code>.
	 */
	Class<T> getDomainObjectClass();
	
	/**
	 * Checks uniqueness of all properties (of given domainObject)
	 * that are defined as unique by meta data of any kind.
	 * CAUTION: This is called in default save() implementation, so there is no need to call
	 * this explicitly when using the default save().
	 * Nevertheless there are situations where this makes sense, e.g. when
	 * the default save() is not called because the entities are saved by CASCADE option.
	 * @param domainObject the bean to check for unique constraint violations.
	 * @param collectionIndex can be null for plain or dotted properties, when not null this gives the index in a collection.
	 * @param owningEntityType the type that calls this check,
	 * 		e.g. HrTaksGroup.class when HrTaskGroupService checks its CASCADING HrTask Collection "tasks".
	 * @throws BusinessLogicException when a unique constraint would be violated.
	 */
	void checkUniqueProperties(T domainObject, Integer collectionIndex, Class<? extends PersistentObject> owningEntityType);

}
