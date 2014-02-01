package at.workflow.webdesk.po.daos;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.GenericDAO;
import at.workflow.webdesk.po.model.PoKeyValue;
import at.workflow.webdesk.po.model.PoKeyValueType;

public interface PoKeyValueTypeDAO extends GenericDAO<PoKeyValueType>{

	/**
	 * loads specified KeyValueType with all KeyValues valid at the specified date or
	 * will be valid in the future (after the specified date)
	 * 
	 * @param uid
	 * @param referenceDate
	 * @return
	 */
	public PoKeyValueType getKeyValueTypeF(String uid, Date referenceDate);
	
	/**
	 * finds the KeyValueType with the given keyValueTypeName
	 * 
	 * @param keyValueTypeName
	 * @return
	 */
	public PoKeyValueType findKeyValueTypeByName(String keyValueTypeName);
	

	
	/**
	 * Returns a <code>PoKeyValue</code> object with the given <code>keyValue</code> and the given 
	 * <code>PoKeyValueType</code>. As the signature says, also old objects are included. (where 
	 * validTo is in the past. 
	 * 
	 * 
	 * @param keyValueType
	 * @param keyValue
	 * @return a list of <code>PoKeyValue</code> objects.
	 */
	public PoKeyValue findKeyValueIncludingOld(PoKeyValueType keyValueType, String keyValue);

	
	
	/**
	 * <p>
	 * Returns a list of <code>PoKeyValueType</code> objects, which are valid at the 
	 * current date.
	 * </p>
	 * 
	 * @return a list of <code>PoKeyValueType</code>'s.
	 */
	public List<PoKeyValueType> findKeyValueTypes();
	
}
