package at.workflow.webdesk.po;

import java.util.List;

import at.workflow.webdesk.po.model.FilterCriteria;

/**
 * This Service is used by the for filtering of data.
 */
public interface FilterService {
	
	/**
	 * generates a hql query that can be added to the where statement
	 * 
	 * @param hql
	 * @param filters  List of <code>at.workflow.webdesk.tm.model.TmFilterCriteria</code> objects
	 * @return
	 */
	public String generateHqlFromFilters(List<FilterCriteria> filters);
	
	/**
	 * removes the Travels from the List that don't match the Expression generated from the filtersList
	 * 
	 * @param travels
	 * @param filters
	 */
	public void removeObjectsNotMatchingExpression(List<?> objs, List<FilterCriteria> filters);

}
