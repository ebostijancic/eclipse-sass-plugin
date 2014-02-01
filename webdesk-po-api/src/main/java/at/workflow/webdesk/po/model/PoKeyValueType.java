package at.workflow.webdesk.po.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.workflow.webdesk.po.PoConstants;

/**
 * Hibernate POJO representing a runtime-bound enumeration (Aufzählungstyp).
 * As the enumeration can be changed after compile-time, be careful when
 * attaching programmed semantic to one of its enumeration items,
 * the user may see a label text indicating another semantic
 * (because it was changed at runtime).
 * 
 * A KeyValueType is used to dynamically build a SelectionList.
 * 
 * @author ggruber
 */
@SuppressWarnings("serial")
public class PoKeyValueType extends PoHistorization {

	private String UID;
	private String name;
	private String description;
	private boolean allowUpdateOnVersionChange;
	private List<PoKeyValue> keyValues = new ArrayList<PoKeyValue>();
	
	/** Unique identifier of this enumeration. Equal to the id attribute in XML, e.g. &lt;keyvaluetype id="APPROVAL_STATUS"&gt;. */
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getUID() {
		return UID;
	}
	@Override
	public void setUID(String uid) {
		UID = uid;
	}

	public List<PoKeyValue> getKeyValues() {
		return keyValues;
	}
	public void setKeyValues(List<PoKeyValue> keyValues) {
		this.keyValues = keyValues;
	}
	
	/** Convenience method that does all necessary persistence preparations when adding an enumeration item. */
	public void addKeyValue(PoKeyValue value) {
		if (value.getValidfrom() == null)
			value.setValidfrom(new Date());
		
		if (value.getValidto() == null)
			value.setValidto(PoConstants.getInfDate());
		
		value.setKeyValueType(this);
		
		if (value.getTextModule() == null) {
			PoTextModule myModule = new PoTextModule();
			value.setTextModule(myModule);
		}
		
		keyValues.add(value);
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isAllowUpdateOnVersionChange() {
		return allowUpdateOnVersionChange;
	}
	public void setAllowUpdateOnVersionChange(boolean allowUpdateOnVersionChange) {
		this.allowUpdateOnVersionChange = allowUpdateOnVersionChange;
	}

}
