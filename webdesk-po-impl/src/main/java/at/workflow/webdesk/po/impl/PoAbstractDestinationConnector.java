package at.workflow.webdesk.po.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.PoDestinationConnectorInterface;

/**
 * Created on 10.04.2006
 * @author hentner (Harald Entner)
 * @author fritzberger 23.02.2012 refactoring.
 */
public abstract class PoAbstractDestinationConnector extends PoAbstractConnector implements PoDestinationConnectorInterface {

	/**
	 * Default implementation that calls findAllObjects() for primaryKey = id.
	 * @return true when list returned by findAllObjects() is not empty.
	 */
	@Override
	public boolean containsObject(String searchValue, String searchKey) {
		assert searchKey != null : "Can not check for existence when searchKey is null!";
		return false == findAllObjects(Arrays.asList(new String [] { searchKey }), searchKey+" = '"+searchValue+"'").isEmpty();
	}

	/**
	 * Default implementation that calls findAllObjects() for primaryKey = id.
	 * When this returns exactly one record, doDeleteObject() is called with the primary key value to delete.
	 */
	@Override
	public void deleteObject(String id, String fieldName) {
		// look for objects matching the key/value pair
		List<String> fieldList = new ArrayList<String>();
		fieldList.add(getSearchKeyName());
		List<Map<String, Object>> searchList = findAllObjects(fieldList, fieldName+" = '"+id+"'");

		// if found exactly one -> delete it
		if (searchList.size() == 1) {
			doDeleteObject((String) searchList.get(0).get(getSearchKeyName()));
		}
	}

	/**
	 * Called by deleteObject() only.
	 * This is called when it was made sure that a record with given ID exists.
	 * Implementers delete that record physically (knowing the primary key name).
	 * @param searchKeyValue primary key value of object to be deleted.
	 */
	protected void doDeleteObject(String searchKeyValue)	{
		throw new IllegalStateException("Must implement me when not overriding deleteObject("+searchKeyValue+")");
	}

	/**
	 * Called by deleteObject() only.
	 * @return the field name of a natural or primary key of the domain object this connector deals with.
	 * 		This default implementation returns "UID".
	 */
	protected String getSearchKeyName()	{
		return "UID";
	}

	/** Default implementation that does nothing. To be overridden. */
	@Override
	public void postProcessImportedObjects(List<Object> result) {
	}

}
