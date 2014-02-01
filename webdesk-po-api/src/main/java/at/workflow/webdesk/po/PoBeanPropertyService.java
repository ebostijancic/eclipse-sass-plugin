package at.workflow.webdesk.po;

import java.util.Collection;
import java.util.List;

import at.workflow.webdesk.po.model.PoBeanProperty;

/**
 *<p>
 * Interface of the PoBeanPropertyService. This Service can be used to 
 * read, write and inject bean properties, which are contained in the 
 * application context of the application. 
 *</p>
 *<p>
 * Bean properties can be read from xml files {@link PobeanPropertyService#fillBeans(String location)} or 
 * from the database. If read from xml files, bean properties can be stored and injected into the corresponding
 * beans.
 *</p>
 *
 * @author hentner, ggruber
 */
public interface PoBeanPropertyService {

	/**
     * This is called from PoStartupImpl during application initialization.
     * Stores the list of bean-properties to database.
     * Only BeanProperties that are not already stored are added.
     * 
     * Use updateBeanValue if you explicitly want to change a value.
     */
    void writeBeanPropertiesToDb();
    
    
	/**
     * This is called from PoStartupImpl during application initialization.
     * Stores the list of bean-properties to database.
     * if ignoreDatabaseValues==false, Only BeanProperties that are not already stored are added.
     * otherwise the database state is ignored and overwritten
     * 
     * Use updateBeanValue if you explicitly want to change a value.
     */
    void writeBeanPropertiesToDb(boolean ignoreDatabaseValues);
    

    /**
     * Reads bean properties from DB. The result is stored
     * in the beans property of the PoModuleService Implementation class. 
     */
    List<PoBeanProperty> readBeanProperties();
    
	List<PoBeanProperty> readBeanPropertiesForModule(String moduleUID);

	List<PoBeanProperty> readBeanPropertiesForBean(String beanName);

	List<PoBeanProperty> readBeanPropertiesForModuleAndBean(String moduleUID, String beanName);
	
	List<String> readBeanNames();
	
	List<String> readBeanNamesForModule(String moduleUID);
	
    
    /**
     * Deletes given beanProperty from database. 
     */
    void deleteBeanProperty(PoBeanProperty beanProperty);

    
    /**
     * @return the existing PoBeanProperty with given beanName and property name, or null if not found.
     */
    PoBeanProperty findBeanPropertyByKey(String beanName, String property);
   
    /**
     * @param UID unique identifier of the domain-object to read.
     * @return the PoBeanProperty with the given UID, or null if not found.
     */
    PoBeanProperty getBeanProperty(String UID);
    

    /**
     * This is called from PoStartupImpl during application initialization.
     * Loads all persisted BeanProperties from DB and injects them into
     * corresponding (and loaded) Spring Beans in the current ApplicationContext.
     * If the list of persisted BeanProperties contains entries where there
     * are no corresponding spring beans in the applicationContext those
     * entries are 
     * either (a) deleted, if the owning module is not detached, or the property is not
     * allowed for editing (by definition in negative list in PoRegistrationBean
     * definition per module),
     * or (b) detached if the owning module is detached.
     */
    void injectAll();
    
    
    /**
     * Injects all <code>PoBeanProperty</code> objects, defined in the given
     * <code>List l</code>.
     * Same as method injectAll() but only processes the list of
     * passed BeanProperties instead of processing the list of 
     * persisted BeanProperties in the database.
     * 
     * @param l a list of <code>PoBeanProperty</code> objects.
     */
    void injectAll(Collection<PoBeanProperty> l);
    
    /**
     * Use this to modify a property value that is <b>not a list</b>.
     * Additionally to persistence this injects the values into 'live' Spring beans in memory.
     * 
     * @param property a PoBeanProperty object to update persistently and in memory.
     * @param value the new value for the property.
     * @throws Exception when the bean value is not possible. eg. 'xxx' for a boolean type
     */
    void updateBeanValueAndInject(PoBeanProperty property, String value) throws Exception;
    
    
    /**
     * Use this method to set a value to a bean property identified by beanName and propertyname.
     * This means that the property is set to the bean in the live system and that the property
     * is also saved in the database inside a PoBeanProperty object.
     * If the specified PoBeanProperty does not exist, an exception is thrown.
     * The passed value can be of String, primitive type or also a Array or collection, if the
     * specified beanproperty holds a collection or array.
     * 
     * @param beanName is the name of the bean
     * @param property is the property of the bean, which is set.
     * @param value is the value to set. Might be a string or a List of strings
     */
    void setBeanProperty(String beanName, String property, Object value);
    
    /**
     * Use this to update or append values of a <b>list-property</b>.
     * Calls <code>updateBeanValue</code> with a 0 (zero) ranking.
     * 
     * @param b a PoBeanProperty object to update.
     * @param uid the UID of the PoBeanPropertyValue in values-list to update, when null, a new value will be appended. 
     * @param value the new value for the list element when uid exists, or the value for the new element when uid doesn't exist.
     * @throws Exception when the bean value is not possible. eg. 'xxx' for a boolean type
     */
    void updateBeanValue(PoBeanProperty property, String uid, String value) throws Exception;
    
    /**
     * Use this to update or append values of a <b>list-property</b>.
     * 
     * @param ranking the new sort order position (list index) of the element in List
     * 		(will be updated when uid exists, else it will be ignored. TODO: is this intended or a bug?).
     * @param rest see above.
     * @throws Exception when the bean value is not possible, e.g. 'xxx' for a boolean type.
     */
    void updateBeanValue(PoBeanProperty property, String uid, String value, int ranking) throws Exception;
    
    
    /**
     * looks for an attached value identified by the given uid
     * removes the corresponding PoBeanPropertyValue from the Collection
     * and saves the beanProperty
     * 
     * @param uid UID of attached PoBeanPropertyValue Object to delete
     */
    void deleteBeanValue(PoBeanProperty property, String uid);
    

    /**
     * Persists the <code>PoBeanProperty b</code>.
     */
    void saveBeanProperty(PoBeanProperty property);
    
    
    /**
     * TODO fri_2010-11-17: this is unclear. What is checked here?
     * @param uid the uid of the <code>PoBeanPropertyValue</code> that was changed (can be null) 
     * @param value the value for the property in its <code>String</code> representation. 
     * @return an empty String if everything was OK, else the errormessage (can be an i18n key) with the given error message.
     */
    String checkPropertyValue(PoBeanProperty property, String value) throws Exception;
    
    
	/**
	 * checks if passed PoBeanProperty is allowed to be saved in the database
	 * and should be able to edit within the adminstration dialogs.
	 */
	boolean isPropertyAllowedForEditing(PoBeanProperty property);

	
	void registerBeanProperties(List<PoBeanProperty> beans);

	
	/**
	 * Use this function if you want to copy the information stored inside a 
	 * <code>PoBeanProperty</code>.  
	 * 
	 * @param oldBeanName the name of the old <code>PoBeanProperty</code>
	 * @param oldPropertyName the name of the old <code>propertyName</code>
	 * @param newBeanName the name of the new <code>PoBeanProperty</code>
	 * @param newPropertyName the name of the new <code>propertyName</code>
	 */
	void copyBeanProperty(String oldBeanName, String oldPropertyName, String newBeanName, String newPropertyName);
	
}
