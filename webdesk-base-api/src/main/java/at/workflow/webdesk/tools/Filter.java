package at.workflow.webdesk.tools;

/**
 * Filter bean that allows storing a property name and its value, without any interpretation logic.
 * This is manageable in Maps and Lists due to its equals() and hashCode() overrides:
 * A Filter equals another Filter when they have the same name (value does not matter).
 */
public class Filter
{
	private final String name;
	private final Object value;
	private final Datatype dataType;
	
	public Filter(String name, Object value) {
		this(name, value, null);
	}
	
	public Filter(String name, Object value, Datatype dataType) {
		assert name != null : "A filter needs a name";
		
		this.name = name;
		this.value = value;
		this.dataType = dataType;
	}
	
	/** The name of the property where the value of this filter might reside. */
	public String getName()  {
		return name;
	}
	
	/** The value of the property to search. */
	public Object getValue() {
		return value;
	}
	
	/**
	 * The data-type of the property to search, or null if has not been set.
	 * Mind that this filter field is a bug-fix done for WDHREXPERT-501 and mostly will be null.
	 */
	public Datatype getDataType() {
		return dataType;
	}

	/** @return true when name is equals to other name, does <b>not</b> include value in this comparison! */
	@Override
	public boolean equals(Object o) {
		Filter other = (Filter) o;	// should never be mixed with other types
		return getName().equals(other.getName());
	}
	
	/** @return name.hashCode(). */
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public String toString() {
		return super.toString()+": name="+getName()+", value="+getValue();
	}

}
