package at.workflow.webdesk.tools.pagination;

import at.workflow.webdesk.tools.Filter;

/**
 * Adds filtering behavior to the pagination cursor.
 */
public abstract class FilterAndSortPaginationCursor extends PaginationCursor {
	
	/**
	 * Apply a list of Filter Objects to the current data set. Previously applied filters will be removed.
	 * After applying the filters, the paging is reorganized and the method
	 * {@link #getTotalNumberOfElements()} will retrieve a potentially smaller amount of elements as before.
	 * A Filter contains the column name (filter name) and its value.
	 * <br/><br/>
	 * If you want to use unprecise query arguments, use the 'like' operator together with the '%' wildcard sign.
	 * F.i. filter.name = 'firstName' and filter.value = 'like %ori%' will find all records where the 'firstName'
	 * column matches strings which contain 'ori', including f.i. 'Florian'.
	 */
	public abstract void applyFilters(Filter[] filters);
	
	/** Remove all filters which have been applied with the last method call {@link #applyFilters(Filter[])}. */
	public abstract void resetFilters();

	
	/** Sets another set of "order-by" properties, i.e. replaces existing ones by given ones. */
	public abstract void applySortOrder(String [] propertyIds, boolean [] ascending);

	/** Removes any "order-by" properties and restores the original ones (if existed). */
	public abstract void resetSortOrder();

}
