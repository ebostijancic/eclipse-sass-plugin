package at.workflow.webdesk.po;

import java.util.List;
import java.util.Map;

/**
 * A destination-connector writes data to some medium like database or CSV-file.
 */
public interface PoDestinationConnectorInterface extends PoConnectorInterface	{

    /**
     * Can be used to query the existence of a certain entity (e.g. about to be imported).
     * @param id the value of the primary key field of the questionable record in data-source.
     * @param primaryKeyName the name of the primary key field in data-source.
     * @return true if a record with the given id was found, false otherwise.
     */
    public boolean containsObject(String id, String primaryKeyName);
    
	/**
     * If no corresponding object (primaryKeyFieldName) is found, a new one is stored,
     * otherwise the existing one is overwritten (except its primary key value).
     * 
     * @param record consists of a <code>Map<String,Object>(X,Y)</code> of data records, where <code>X</code> is 
     * 		a <code>String</code> that represents the property name and <code>Y</code> is its value,
     * 		must not be null.
     * @param uniqueKeyName a <code>String</code> representing the primary key or a natural key (not-null, unique)
     * 		of the object, must not be null.
     * @param fieldsInCorrectOrder the fields in a correct order, can be null.
     * 		(FIXME we should let the fieldMap take care of that, the keyset must then be ordered, when it is necessary.
     * 		Changes here will have influences on the frontend)
     * @return the persisted object. (can be any type) 
     */
    public Object saveObject(Map<String,Object> record, String uniqueKeyName, List<String> fieldsInCorrectOrder);
    
    /**
     * Tries to locate a record with the given id value in the field named by given primaryKeyName.
     * If exactly <b>one</b> record is found, it will be deleted.
     * 
     * @param id value of the primary key.
     * @param primaryKeyName the name of the primary key field wherein to search the id.
     */
    public void deleteObject(String id, String primaryKeyName);
    
	/**
	 * This function will be called after all objects have been imported,
	 * and after the source-connector's post-process has been called.
	 * It can be used for functionality that needs the full collection of imported objects to work.
	 *
	 * @param objects list of imported objects (the destination connector should know their type).
	 */
	public void postProcessImportedObjects(List<Object> objects);

}
