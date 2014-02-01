package at.workflow.webdesk.po.model;

import at.workflow.webdesk.po.util.ToStringHelper;
import at.workflow.webdesk.tools.api.PersistentObject;

/**
 * Base class for all Hibernate POJO classes.
 * <p />
 * <b>CAUTION:</b>
 * 		When a POJO was not yet persisted (is transient), no UID exists (is null).
 * 		When the POJO is inserted into a Map then, without having an UID,
 * 		and is persisted afterwards (UID is no more null then),
 * 		it might not be found anymore in that Map.
 * 		Reason is that another hashCode() will be calculated, or equals() will fail.
 * 		See hashCode/equals contract in Java class Object.
 */
@SuppressWarnings("serial")
public abstract class PoBase implements PersistentObject {

	/**
	 * Sub-classes must implement an unique identifier.
	 */
	@Override
	public abstract String getUID();

	/**
	 * Public only due to implementation constraints.
	 * This is needed by the JPA layer (Hibernate) and should not be called by the application.
	 */
	public abstract void setUID(String uid);

	/**
	 * @return true if the UID's (database primary keys) of this and
	 * 		the other object are defined (persistent state) and equal, else
	 * 		delegates to <code>super.equals()</code> which normally uses
	 * 		the object's memory address.
	 */
	@Override
	public final boolean equals(Object other) {
		if (equalsPrecondition(other) == false)
			return false;

		final String uid = getUID();
		if (uid != null)
			return uid.equals(((PoBase) other).getUID());

		return isIdentical(other);
	}

	/**
	 * This is called before UID is checked for existence. To be overridden.
	 * @return true when passed object is not null and instanceof PoBase, else false.
	 */
	protected boolean equalsPrecondition(Object other) {
		return other instanceof PoBase;	// instanceof gives false when other is null
	}

	/**
	 * @return the UID's hash-code (database primary key), when UID
	 * is defined (persistent state), else delegates to <code>super.hashCode()</code>
	 * which normally returns the object's memory address.
	 */
	@Override
	public final int hashCode() {
		final String uid = getUID();
		if (uid != null)
			return uid.hashCode();

		return super.hashCode();
	}

	/**
	 * This is to find out if POJOs are identical, i.e. the same Java object in memory.
	 * This can not be done using equals(), because there might be more than one POJO
	 * with the same UID in memory.
	 * This implementation delegates to <code>super.equals(other)</code>.
	 * @param other the object to check if it is identical with this one.
	 * @return true if this is identical with given object,
	 * 		meaning that this is the same Java object in memory as the given one.
	 */
	public final boolean isIdentical(Object other)	{
		return super.equals(other);
	}

	
	/**
	 * @return a string representation of the object and its internal state
	 * as described in {@link at.workflow.webdesk.po.ToStringHelper}.  
	 */
	@Override
	public String toString() {
		return ToStringHelper.toString(this);
	}
}
