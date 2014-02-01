package at.workflow.webdesk.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.util.ObjectUtils;

/**
 * This incorporates an record like it is read from a relational database.
 * Names are attribute names, values are the record's values associated to that attributes.
 * <p/>
 * This is not meant for being modified, so its nature is immutable, no put() is provided,
 * and returned Collections are clones.
 * <p/>
 * Motivation of this class is to be an alternative to Webdesk's return- and parameter-types
 * that look like <code>List&lt;Map&lt;String,Object>></code>. It is type-safe (can be derived
 * to represent different database tables), and it does not offer methods which should
 * not be called or are useless on a databse record, like <code>put()</code> or <code>clear()</code>.
 * 
 * @author fritzberger 12.06.2012
 */
public class DbRecord {
	
	/** A name/value pair encapsulation. */
	public static class Tuple
	{
		public final String name;
		public final Object value;
		
		public Tuple(String name, Object value)	{
			assert name != null : "Makes no sense to construct a Tuple without a name!";
			this.name = name;
			this.value = value;
		}
		
		@Override
		public int hashCode() {
			return name.hashCode() + ObjectUtils.nullSafeHashCode(value);
		}
		
		@Override
		public boolean equals(Object other) {
			return name.equals(((Tuple) other).name) && ObjectUtils.nullSafeEquals(value, ((Tuple) other).value);
		}
	}
	
	
	private Map<String,Object> namedValues = new HashMap<String,Object>();
	
	
	/** Map compatibility constructor. */
	public DbRecord(Map<String,Object> namedValues)	{
		if (namedValues != null)
			this.namedValues.putAll(namedValues);
	}

	/** Arbitrary number of Tuple arguments. */
	public DbRecord(Tuple ... tuples)	{
		for (Tuple tuple : tuples)	{
			namedValues.put(tuple.name, tuple.value);
		}
	}
	
	/** Arguments must be: name1, value1, name2, value2, name3, value3, .... */
	public DbRecord(Object ... nameValuePairs)	{
		assert nameValuePairs.length % 2 == 0 : "Arguments count must be an even number, was "+nameValuePairs.length;
		
		final String [] attributeNames = new String[nameValuePairs.length / 2];
		final Object [] values = new Object[attributeNames.length];
		
		for (int i = 0, j = 0; i < nameValuePairs.length; i += 2, j++)	{	// must throw IndexOutOfBoundsException when uneven argument count
			attributeNames[j] = (String) nameValuePairs[i];
			values[j] = nameValuePairs[i + 1];
		}
		
		for (int i = 0; i < attributeNames.length; i++)	{
			this.namedValues.put(attributeNames[i], values[i]);	// must throw NullPointerException when name is null
		}
	}

	
	/** @return the value of given attribute in this record. @throws NoSuchElementException when attribute does not exist here. */
	public Object get(String attributeName)	{
		if (namedValues.containsKey(attributeName) == false)
			throw new NoSuchElementException("Unknown attribute: "+attributeName);
		return namedValues.get(attributeName);
	}
	
	/** @return all attribute values of this record. Changes on the returned Collection do not affect this record. */
	public Collection<Object> values()	{
		return new ArrayList<Object>(namedValues.values());
	}
	
	/** @return all attribute names of this record. Changes on the returned Collection do not affect this record. */
	public Set<String> keys()	{
		return new HashSet<String>(namedValues.keySet());
	}
	
	/** @return all name/value pairs of this record. Changes on the returned Collection do not affect this record. */
	public Set<Map.Entry<String,Object>> entries()	{
		return new HashSet<Map.Entry<String,Object>>(namedValues.entrySet());
	}
	
	/** @return the number of attributes of this record. */
	public int size()	{
		return namedValues.size();
	}

	/** @return this record as Map. */
	public Map<String,Object> toMap()	{
		return new HashMap<String,Object>(namedValues);
	}
	
	/** @return this record as Tuple array. */
	public Tuple [] toTuples()	{
		Tuple [] tuples = new Tuple[size()];
		int i = 0;
		for (Map.Entry<String,Object> entry : entries())	{
			tuples[i] = new Tuple(entry.getKey(), entry.getValue());
			i++;
		}
		return tuples;
	}

	/** Overridden to delegate to private namedValues Map. */
	@Override
	public int hashCode() {
		return namedValues.hashCode();
	}
	
	/** Overridden to delegate to private namedValues Map. */
	@Override
	public boolean equals(Object other) {
		return namedValues.equals(((DbRecord) other).namedValues);
	}

}
