package at.workflow.webdesk.tools.api;

import java.io.Serializable;

/**
 * All (at least database) domain objects implement this interface.
 */
public interface PersistentObject extends Serializable {

	/**
	 * The name of the property that holds the UID (unique identifier, primary key)
	 * of an already persisted domain object, or null when not persistent yet.
	 * Mind that this MUST BE upper-case, because it is a property-name used with method-reflection.
	 */
	String UID = "UID";
	
	/**
	 * The globally unique "universal" identifier of this object (ID).
	 * If this returns non-null, the object can be considered to be persistent (not transient).
	 */
	String getUID();


	/** Equals and hashCode MUST be implemented by sub-classes to be cacheable in Maps. */
	@Override
	boolean equals(Object obj);

	/** Equals and hashCode MUST be implemented by sub-classes to be cacheable in Maps. */
	@Override
	int hashCode();
	
}
