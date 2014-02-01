package at.workflow.webdesk.tools.pagination.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import at.workflow.webdesk.tools.Filter;
import at.workflow.webdesk.tools.NamedQuery;
import at.workflow.webdesk.tools.PositionalQuery;
import at.workflow.webdesk.tools.hibernate.HqlUtils;
import at.workflow.webdesk.tools.hibernate.HqlWhereClauseBuilder;
import at.workflow.webdesk.tools.hibernate.HqlWhereClauseBuilder.ConcatOp;
import at.workflow.webdesk.tools.pagination.FilterAndSortPaginationCursor;

/**
 * Page extension for use in HQL/SQL queries
 * 
 * @author ggruber, hentner
 */
public class QueryPaginationCursor extends FilterAndSortPaginationCursor {

	private String hqlQuery;
	private Object[] parameterValues;
	private String[] parameterNames;

	private int appliedFilterCount = 0;
	private String appliedFilterWhereCondition;

	private String originalOrderBy;
	
	private boolean cacheQueryResults = true;

	private long totalElements = -1L;

	public QueryPaginationCursor(NamedQuery namedQuery, int pageSize) {
		this(namedQuery.getQueryText(), namedQuery.getParamNames(), namedQuery.getParamValues(), pageSize);
	}

	public QueryPaginationCursor(PositionalQuery query, int pageSize) {
		this(query.getNamedQuery(), pageSize);
	}

	public QueryPaginationCursor(String hqlQuery, String[] parameterNames, Object[] parameterValues, int pageSize) {
		this.hqlQuery = hqlQuery;
		this.parameterNames = parameterNames;
		this.parameterValues = parameterValues;
		
		setPageSize(pageSize);
	}

	/** Overridden to buffer totalNumberOfElements and resolve it only when filter changes. */
	@Override
	public long getTotalNumberOfElements() {
		if (totalElements >= 0L)
			return totalElements;

		totalElements = super.getTotalNumberOfElements();
		return totalElements;
	}

	/** @return the current HQL query. */
	public String getHqlQuery() {
		return hqlQuery;
	}

	/** @return the current parameter values. */
	public Object[] getParameterValues() {
		return parameterValues;
	}

	/** @return the current parameter names. */
	public String[] getParameterNames() {
		return parameterNames;
	}

	public boolean isCacheQueryResults() {
		return cacheQueryResults;
	}

	public void setCacheQueryResults(boolean cacheQueryResults) {
		this.cacheQueryResults = cacheQueryResults;
	}

	@Override
	public void applyFilters(Filter[] filters) {
		assert hqlQuery.contains("?") == false : "Other than named queries are not implemented here: "+hqlQuery;

		resetFilters();

		final HqlWhereClauseBuilder whereBuilder = new HqlWhereClauseBuilder();

		List<String> newQueryParamNames = new ArrayList<String>();
		List<Object> newQueryParamValues = new ArrayList<Object>();
		String whereClause = "";

		int nextNameIndex = parameterValues.length + 1;
		for (Filter filter : filters) {
			final boolean searchExact = filter.getValue().toString().startsWith(HqlWhereClauseBuilder.LIKE_PREFIX) == false;
			final List<Object> queryParamValues = new ArrayList<Object>(Arrays.asList(parameterValues));
			whereClause = whereBuilder.appendFilterToWhereClause(ConcatOp.AND, whereClause, queryParamValues, filter.getName(), filter.getDataType(), filter.getValue(), searchExact).trim();

			final String paramName = "name"+nextNameIndex;
			nextNameIndex++;
			
			newQueryParamNames.add(paramName);
			final Object appendedParamValue = queryParamValues.get(queryParamValues.size() - 1);
			newQueryParamValues.add(appendedParamValue);
			
			whereClause = whereClause.replace("?", ":"+paramName);	// see assert above
		}

		appliedFilterWhereCondition = whereClause;
		appliedFilterCount = filters.length;

		final String originalWhereClause = HqlUtils.extractWhereClause(hqlQuery);
		if (StringUtils.isBlank(originalWhereClause)) { // when there was no WHERE clause
			whereClause = StringUtils.removeStart(whereClause, ConcatOp.AND+" "); // remove leading "AND " from conditions
			whereClause = "where "+whereClause; // add a leading "where"
		} else {
			whereClause = originalWhereClause+" "+whereClause; // whereClause always starts with "AND "
		}

		hqlQuery = HqlUtils.replaceWhereClause(hqlQuery, whereClause);

		int noOfStaticFilters = parameterValues.length;
		for (int i = 0; i < newQueryParamNames.size(); i++) {
			parameterValues = ArrayUtils.add(parameterValues, noOfStaticFilters + i, newQueryParamValues.get(i));
			parameterNames = (String[]) ArrayUtils.add(parameterNames, noOfStaticFilters + i, newQueryParamNames.get(i));
		}

		// initialize again to go to first page
		setActPage(1);
	}

	@Override
	public void resetFilters() {

		totalElements = -1L;

		final int noOfFilters = appliedFilterCount;
		final int noOfParameters = (parameterNames == null ? 0 : parameterNames.length);

		final int noOfOriginalParameters = noOfParameters - noOfFilters;

		for (int i = (noOfParameters - 1); i >= noOfOriginalParameters; i--) {
			parameterNames = (String[]) ArrayUtils.remove(parameterNames, i);
			parameterValues = ArrayUtils.remove(parameterValues, i);
		}

		if (appliedFilterWhereCondition != null && hqlQuery.contains(appliedFilterWhereCondition)) {

			String whereClause = HqlUtils.extractWhereClause(hqlQuery);
			String newWhereClause = StringUtils.remove(whereClause, appliedFilterWhereCondition).trim();

			if (newWhereClause.trim().equals("where")) {
				// where clause is now empty
				hqlQuery = HqlUtils.removeWhereClause(hqlQuery);
			} else {
				// remove trailing 'and'
				if (newWhereClause.toLowerCase().endsWith("and"))
					newWhereClause = StringUtils.removeEnd(newWhereClause, " and");

				hqlQuery = HqlUtils.replaceWhereClause(hqlQuery, newWhereClause);
			}
		}

		appliedFilterWhereCondition = null;
		this.appliedFilterCount = 0;
	}


	
	@Override
	public void applySortOrder(String[] propertyIds, boolean[] ascending) {
		final String orderBy = HqlUtils.extractOrderByClause(hqlQuery);
		originalOrderBy = StringUtils.isBlank(orderBy) ? null : orderBy;
		
		final String appliedOrderBy = HqlUtils.createOrderByClause(propertyIds, ascending);
		hqlQuery = HqlUtils.replaceOrderByClause(hqlQuery, appliedOrderBy);
	}

	@Override
	public void resetSortOrder() {
		if (originalOrderBy == null)
			return;
		
		hqlQuery = HqlUtils.replaceOrderByClause(hqlQuery, originalOrderBy);
		originalOrderBy = null;
	}
	
}
