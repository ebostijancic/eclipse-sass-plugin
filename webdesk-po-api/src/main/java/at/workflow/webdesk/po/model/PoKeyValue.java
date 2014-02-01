package at.workflow.webdesk.po.model;

/**
 * Represents an enumeration item as part of a KeyValueType.
 * 
 * @author ggruber
 */
@SuppressWarnings("serial")
public class PoKeyValue extends PoHistorization {
	
	private String UID;
	private String key;
	private PoKeyValueType keyValueType;
	private PoTextModule textModule;
	private String filter;
	private int ranking;
	
	public PoKeyValueType getKeyValueType() {
		return keyValueType;
	}
	public void setKeyValueType(PoKeyValueType keyValueType) {
		this.keyValueType = keyValueType;
	}
	
	@Override
	public String getUID() {
		return UID;
	}
	@Override
	public void setUID(String uid) {
		UID = uid;
	}
	
	/** @return the "id" XML attribute of this enumeration value, e.g. &lt;keyvalue id="0">. */
	public String getKey() {
		return key;
	}
	/** Sets the "id" XML attribute of this enumeration value, e.g. &lt;keyvalue id="0">. */
	public void setKey(String key) {
		this.key = key;
	}
	
	/** @return the text module that holds the translation by value property. */
	public PoTextModule getTextModule() {
		return textModule;
	}
	public void setTextModule(PoTextModule textModule) {
		this.textModule = textModule;
	}
	
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
	
	public int getRanking() {
		return ranking;
	}
	public void setRanking(int ranking) {
		this.ranking = ranking;
	}
	
}
