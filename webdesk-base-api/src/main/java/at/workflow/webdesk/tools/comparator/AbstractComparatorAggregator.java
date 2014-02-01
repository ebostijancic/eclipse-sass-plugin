package at.workflow.webdesk.tools.comparator;

import java.util.Comparator;

/**
 * The Comparator "container".
 * Multiple Comparator implementations can be managed by this Comparator.
 * This is useful for sorting by several criteria like an "ORDER BY" clause does:
 * <pre>ORDER BY name, birth, address</pre>.
 * 
 * @author fritzberger 17.09.2012
 */
public abstract class AbstractComparatorAggregator<T> implements Comparator<T>
{
	/**
	 * Sub-classes provide one or more Comparators that will be used for evaluation by this Comparator.
	 * @return the ordered list of Comparators to be applied by this "aggregator".
	 */
	protected abstract Iterable<Comparator<T>> comparators();

	/**
	 * Applies all comparators provided by sub-classes to given objects.
	 * The return value of the first one that returns non-zero will be returned.
	 * If none detects a difference, zero is returned.
	 * @return -1 for go to front, +1 for go to back, 0 for given objects being equal.
	 */
	@Override
	public int compare(T o1, T o2) {
		// consult all criteria in order
		for (Comparator<T> c : comparators())	{
			final int compareValue = c.compare(o1, o2);
			if (compareValue != 0)	// current criterion detected difference
				return compareValue;
		}
		return 0;	// no differences were detected
	}

}
