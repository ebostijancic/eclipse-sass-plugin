package at.workflow.webdesk.tools;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Queries with positional parameters are those with ? (question mark) within query text.
 * 
 * This is a transport class encapsulating all the information
 * necessary for execution of paginated query with positional parameters.
 * 
 * Simple check of the count of the positional parameters and values is performed.
 * 
 * @author sdzuban 12.01.2012
 */
public final class PositionalQuery extends PaginableQuery {

	/**pattern for extraction of names of all parameters preceded with the 'like' operator*/ 
	private static final Pattern pattern = Pattern.compile("((like)?\\s+\\?)");
	
	private Object[] paramValues;
	
	/**
	 * @param queryText can be in any language supported by services, e.g. SQL, HQL
	 * @param paramValues
	 */
	public PositionalQuery(String queryText, Object[] paramValues) {
		this(queryText, paramValues, false);
	}

	/**
	 * HQL constructor for one certain database table.
	 * @param persistenceClass the persistence class related to the table to be queried.
	 * @param whereClause the WHERE clause of the HQL query, without leading "WHERE", or also an "ORDER BY" clause, or both.
	 * @param paramValues the parameter values for any embedded "?" within query
	 *		(number and order must conform to query semantic).
	 */
	public PositionalQuery(Class<?> persistenceClass, String whereClause, Object[] paramValues) {
		this("from "+persistenceClass.getSimpleName()+(whereClause != null ? " "+whereClause : ""), paramValues, false);
	}

	/**
	 * @param decorateLikeParameterValues if true all the parameter values preceded with like operator are decorated with %%
	 */
	public PositionalQuery(String queryText, Object[] paramValues, boolean decorateLikeParameterValues) {
		super(queryText);
		
		if (decorateLikeParameterValues) {
			Object[] decoratedValues = decorateParameterValues(queryText, paramValues);
			setParameterValues(decoratedValues);
		} else
			setParameterValues(paramValues);
	}
	
	public Object[] getParamValues() {
		return paramValues;
	}

	/**
	 * This is conversion method for transforming this query
	 * into an equivalent NamedQuery object.
	 * The transformation is very simple - every value is given a name
	 * with index as number and these names replace ? consecutively.
	 * @return NamedQuery object equivalent to this query
	 */
	public NamedQuery getNamedQuery() {
		
		if (paramValues == null || paramValues.length == 0) {
			NamedQuery newQuery = new NamedQuery(getQueryText(), new String[] {}, new Object[] {});
			newQuery.setFirstResult(getFirstResult());
			newQuery.setMaxResults(getMaxResults());
			return newQuery;
		}
		
		String queryText = getQueryText();
		String[] paramNames = new String[paramValues.length];
		for (int i = 0; i < paramNames.length; i++) {
			paramNames[i] = "name" + (i + 1);
			queryText = queryText.replaceFirst("\\?", ":" + paramNames[i]);
		}
		
		NamedQuery newQuery = new NamedQuery(queryText, paramNames, paramValues);
		newQuery.setFirstResult(getFirstResult());
		newQuery.setMaxResults(getMaxResults());
		return newQuery;
	}
	
	private void setParameterValues(Object[] paramValues) {
		if (areValuesAndParametersAligned(paramValues))
			this.paramValues = paramValues;
	}
	
	private boolean areValuesAndParametersAligned(Object[] values) {
		if ((values == null || values.length == 0) && 
			(getQueryText() == null || getQueryText().indexOf("?") < 0))
			return true;
		
		boolean correction = getQueryText().endsWith("?");
	    String[] fragments = getQueryText().split("\\?");
	    int paramCount = correction ? fragments.length : fragments.length - 1;
	     
	    if (values == null && paramCount != 0 || 
    		values != null && paramCount != values.length)
	         throw new RuntimeException("Number of positional parameters and count of ? are different");
		return true;
	}

	// TODO: document what this does!
	private Object[] decorateParameterValues(String queryText, Object[] parameterValues) {
		
		final int length = parameterValues.length;
		Object[] result = new Object[length];
		
		Matcher matcher = pattern.matcher(queryText.toLowerCase());
		for (int idx = 0; idx < length; idx++) {
			final Object value = parameterValues[idx];
			if (matcher.find() && matcher.group(1).indexOf("like") >= 0)
				result[idx] = "%" + (value == null ? "" : value + "%");
			else
				result[idx] = value;
		}
		return result;
	}
	
	@Override
	public String toString() {
		return super.toString()+", paramValues="+(paramValues == null ? "null" : ""+Arrays.asList(paramValues));
	}

}
