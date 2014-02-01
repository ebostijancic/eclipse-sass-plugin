package at.workflow.webdesk.po;

import java.util.Date;
import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.model.PoKeyValue;
import at.workflow.webdesk.po.model.PoKeyValueType;
import at.workflow.webdesk.po.model.PoLanguage;

/**
 * <p>To be used to access <code>PoKeyValue</code> and <code>PoKeyValueType</code> objects.
 * Additionally it can be used to convert the information into <code>SelectionList</code>'s.
 * </p>
 * created at:       02.07.2007
 * 
 * @see at.workflow.webdesk.po.PoOrganisationService
 * @see at.workflow.webdesk.po.PoRegistrationService
 * @see at.workflow.webdesk.po.webclient.SelectionListHelper
 * 
 * @author Fritz Povysil, DI Harald Entner<br>
 */
public interface PoKeyValueService {

	/**
	 * Mind that TextModule will not be loaded when using this call!
	 * @param uid of wanted PoKeyValuetype.
	 * @return PoKeyValueType Object with that UID.
	 */
	PoKeyValueType getKeyValueType(String uid);
	
	/**
	 * Loads specified KeyValueType with all KeyValues valid at the specified date or
	 * will be valid in the future (after the specified date).
	 * TextModules will be loaded!
	 */
	PoKeyValueType getKeyValueTypeF(String uid, Date referenceDate);
	
	/**
	 * Saves the given object.
	 * Mind that this always saves for default language!
	 * TODO make this language-specific!
	 */
	void saveKeyValueType(PoKeyValueType keyValueType);
	
	/**
	 * Softly deletes the given Object. 
	 */
	void deleteKeyValueType(PoKeyValueType keyValueType);
	
	/**
	 * TextModules will be loaded!
	 * @param keyValueType id of the KeyValueType to search for.
	 * @return a List of objects suitable for a SelectionList.
	 */
	List<PoKeyValue> getSelectionList(String keyValueType);
	
	/**
	 * Checks if oldKey is Part of the list, if not adds the corresponding
	 * PoKeyValue Object. This addresses the problem that a particular PoKeyValue
	 * Object might have expired (valid-to), but should still be available in the 
	 * option list because it is still a persistent value in the database.
	 * TextModules will be loaded!
	 * 
	 * @param keyValueType id of the KeyValueType to search for.
	 * @param oldKey a possibly no-more-existing item in the searched keyValueType, to be added.
	 * @return a List of objects suitable for a SelectionList.
	 */
	List<PoKeyValue> getSelectionList(String keyValueType, String oldKey);
	
	/**
	 * TextModules will be loaded!
	 * TODO please comment what is a filter!
	 * 
	 * @param keyValueType id of the KeyValueType to search for.
	 */
	List<PoKeyValue> getSelectionListWithFilter(String keyValueType, String oldKey, String filter);
	
	/**
	 * @return a <code>Map</code> m(x,y), where <code>x</code> is the name
	 * 		of the <code>PoKeyValueType</code> and <code>y</code> is a list of 
	 * 		<code>PoKeyValue</code> objects.
	 */
	Map<String, List<PoKeyValue>> getKeyValueTypesAsMap();
	
	/**
	 * @deprecated seems to be no more used.
	 * @return true if duplicate values exist in the given key/value-type.
	 */
	boolean hasDuplicateEntries(PoKeyValueType keyValueType);
	
	/**
	 * @return a <code>Map</code> m(x,y), where x is a <code>String</code> object, which correspond
	 * 		to the <code>key</code> of the linked <code>PoKeyValue</code> and y is the <code>uid</code> of 
	 * 		the linked <code>PoKeyValue</code>.
	 */
	Map<String, String> getKeyValueKeys(PoKeyValueType keyValueType);
	
	/**
	 * @param keyValueType the XML "id" attribute value of the KeyValueType, e.g. APPROVAL_STATUS for &lt;keyvaluetype id="APPROVAL_STATUS"&gt;.
	 * @return a <code>PoKeyValueType</code> object whose id attribute is equal to the given <code>id</code>.
	 */
	PoKeyValueType findKeyValueType(String keyValueType);
	
	/** Finds a whole key/value-type with all items, including their TextModules for given language. */
	PoKeyValueType findKeyValueTypeWithTextModules(String key, PoLanguage language);

	/** Translates a key/value item to given language. */
	String translateKeyValue(String keyValueTypeId, String key, PoLanguage language);

	/** Translates a key of given key value type with locale. */
	String getLabel(String keyValueTypeName, String key, String locale);
}
