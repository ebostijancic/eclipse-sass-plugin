package at.workflow.webdesk.tools.comparator;

import at.workflow.webdesk.tools.api.PersistentObject;

/**
 * Utility methods to be called from Comparator or Comparable.
 * 
 * @author fritzberger 07.09.2012
 */
public final class CompareUtil {

	/** Value returned from testForNulls(), telling that parameters have to be compared by the caller. */
	public static final int BOTH_ARE_NOT_NULL_AND_NOT_IDENTICAL = 999;
	
	/**
	 * This result sorts nulls to front. TODO: this should not always be that way?
	 * @return 0 for both are null or identical, -1 for o1 is null, +1 for o2 is null, else BOTH_ARE_NOT_NULL_AND_NOT_IDENTICAL.
	 */
	public static int testForNulls(Object o1, Object o2)	{
		if (o1 == o2)	// null == null returns true in Java
			return 0;	// identical, or both are null
		
		if (o1 == null)	// first parameter is null, second is not null
			return -1;	// first goes to head, TODO: should be tail?
		
		if (o2 == null)	// first parameter is not null, second is null
			return +1;	// first goes to tail, TODO: should be head?
		
		return BOTH_ARE_NOT_NULL_AND_NOT_IDENTICAL;
	}
	
	/**
	 * @return a value that would sort given Comparable ascending.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static int compareNullSafe(Comparable c1, Comparable c2)	{
		final int testForNulls = testForNulls(c1, c2);
		return (testForNulls != BOTH_ARE_NOT_NULL_AND_NOT_IDENTICAL) ? testForNulls : c1.compareTo(c2);
	}
	
	/**
	 * @return a value that would sort given domain objects by UID. 
	 */
	public static int compareNullSafe(PersistentObject p1, PersistentObject p2)	{
		final int testForNulls = testForNulls(p1, p2);
		return (testForNulls != BOTH_ARE_NOT_NULL_AND_NOT_IDENTICAL) ? testForNulls : p1.getUID().compareTo(p2.getUID());
	}
	
	/**
	 * @return a value that would sort true before false.
	 */
	public static int compare(boolean b1, boolean b2)	{
		return b1 == b2 ? 0 : b1 ? -1 : +1;
	}
	
	/**
	 * @return a value that would sort given numbers ascending.
	 */
	public static int compare(int i1, int i2)	{
		return i1 - i2;
	}
	
	private CompareUtil()	{}	// do not instantiate

}
