package at.workflow.webdesk.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Queries with positional parameters are those with :xxx (colon, some parameter name) within query text.
 * 
 * This is transport class encapsulating all the information
 * necessary for execution of paginated query with named parameters.
 * 
 * It provides also possibility to automatically decorate values of 
 * parameters bound by 'like' operator with %%.
 * 
 * Additionally, some basic plausibility tests are performed:
 *  - values and names arrays are same length
 *  - all names in where clause are contained in names array
 * 
 * @author sdzuban 12.01.2012
 */
public final class NamedQuery extends PaginableQuery {
	
	/** pattern for extraction of all parameter names */ 
	private static final Pattern namePattern = Pattern.compile(":(\\S+)");

	/** pattern for extraction of names of all parameters preceded with the 'like' operator*/ 
	private static final Pattern likePattern = Pattern.compile("\\s+like\\s+:(\\S+)");
	
	private String[] paramNames;
	private Object[] paramValues;

	/**
	 * @param queryText can be in any query language supported by the services, e.g. SQL, HQL
	 */
	public NamedQuery(String queryText, String[] parameterNames, Object[] parameterValues) {
		super(queryText);

		setParameterNamesAndValues(parameterNames, parameterValues);
	}

	/**
	 * @param queryText can be in any query language supported by the services, e.g. SQL, HQL
	 * @param decorateLikeParameterValues if true all the parameter values preceded with like operator are decorated with %%
	 */
	public NamedQuery(String queryText, String[] parameterNames, Object[] parameterValues, boolean decorateLikeParameterValues) {
		super(queryText);
		
		if (decorateLikeParameterValues) {
			Set<String> likeParameterNames = getLikeParameterNames(queryText);
			Object[] decoratedValues = decorateParameterValues(likeParameterNames, parameterNames, parameterValues);
			setParameterNamesAndValues(parameterNames, decoratedValues);
		} else
			setParameterNamesAndValues(parameterNames, parameterValues);
	}
	
	/**
	 * This method converts NamedQuery to equivalent PositionalQuery.
	 * Main usage is in PoGeneralSqlService for prepared statement.
	 * @return equivalent PositionalQuery
	 */
	public PositionalQuery getPositionalQuery() {
		
		if (paramNames == null || paramNames.length == 0 ||
				paramValues == null || paramValues.length == 0) {
			
			PositionalQuery newQuery = new PositionalQuery(getQueryText(), new Object[]{});
			newQuery.setFirstResult(getFirstResult());
			newQuery.setMaxResults(getMaxResults());
			return newQuery;
		}
		
		Map<String, Integer> nameToIndexMap = getNameToIndexMap();
		
		List<?> positionalValues = getPositionalValues(nameToIndexMap);
		
		String positionalQueryText = getPositionalQueryText();
		
		PositionalQuery newQuery = new PositionalQuery(positionalQueryText, positionalValues.toArray(new Object[] {}));
		newQuery.setFirstResult(getFirstResult());
		newQuery.setMaxResults(getMaxResults());
		
		return newQuery;
	}
	
	public String[] getParamNames() {
		return paramNames;
	}
	public Object[] getParamValues() {
		return paramValues;
	}

//	---------------------------- PRIVATE METHODS -------------------------------------
	
	private Map<String, Integer> getNameToIndexMap() {
		
		Map<String, Integer> result = new HashMap<String, Integer>();
		for (int idx = 0; idx < paramNames.length; idx++)
			result.put(paramNames[idx], idx);
		return result;
	}

	private List<Object> getPositionalValues(Map<String, Integer> nameToIndexMap) {
		
		List<Object> positionalValues = new ArrayList<Object>();
		Matcher matcher = namePattern.matcher(getQueryText().toLowerCase());
		while (matcher.find()) {
			final String name = matcher.group(1);
			final Integer index = nameToIndexMap.get(name);
			positionalValues.add(paramValues[index]);
		}

		return positionalValues;
	}

	private String getPositionalQueryText() {
		return getQueryText().replaceAll(":\\S+", "?");
	}

	private void setParameterNamesAndValues(String[] paramNames, Object[] paramValues) {
		if (areNamesAndValuesAligned(paramNames, paramValues) &&
				areNamesAndWhereClauseAligned(paramNames)) {
			this.paramNames = paramNames;
			this.paramValues = paramValues;
		} else {
		     throw new RuntimeException("Not correlated query,  names and values. " +
		    		 "Query text: " + getQueryText() + 
		    		 ", Names: " + (paramNames == null ? "null" : " length = " + paramNames.length) + 
		    		 ", Values: " + (paramValues == null ? "null" : " length = " + paramValues.length));
		}
	}
	
	private boolean areNamesAndValuesAligned(String[] names, Object[] values) {
		
		return names == null && values == null || 
				names == null && values.length == 0 ||
				names.length == 0 && values == null ||
				names.length == values.length;
	}
	
	private boolean areNamesAndWhereClauseAligned(String[] names) {
		
		int namePrefixIdx = getQueryText().indexOf(":");
		if (namePrefixIdx < 0 && (names == null || names.length == 0))
			return true;
		
		if (namePrefixIdx < 0 && names != null && names.length > 0)
			return false;
		
		String[] fragments = getQueryText().split(":");
		if (fragments.length > 1 && (names == null || names.length == 0))
				return false;
		
		List<String> namesNotFound = new ArrayList<String>();
		for (int idx = 1; idx < fragments.length; idx++) {
			boolean found = false;
			for (String name : names) {
				if (fragments[idx] != null && fragments[idx].startsWith(name)) {
					found = true;
					break;
				}
			}
			if (!found)
				namesNotFound.add(":" + fragments[idx].split(" ")[0]);
		}
		if (!namesNotFound.isEmpty())
			throw new RuntimeException("Following names were not found in the name array: " + namesNotFound);
		return true;
	}

	/** extraction method of 'like' parameter names */
	private Set<String> getLikeParameterNames(String queryText) {
		
		Set<String> result = new HashSet<String>();
		Matcher matcher = likePattern.matcher(queryText.toLowerCase());
		while (matcher.find())
			result.add(matcher.group(1));
		return result;
	}

	private Object[] decorateParameterValues(Set<String> likeParameterNames, String[] parameterNames, Object[] parameterValues) {
		
		final int length = parameterValues.length;
		Object[] result = new Object[length];
		
		for (int idx = 0; idx < length; idx++) {
			final Object value = parameterValues[idx];
			if (likeParameterNames.contains(parameterNames[idx]))
				result[idx] = "%" + (value == null ? "" : value + "%");
			else
				result[idx] = value;
		}
		return result;
	}
	
	@Override
	public String toString() {
		return super.toString()+", paramNames="+(paramNames == null ? "null" : ""+Arrays.asList(paramNames))+", paramValues="+(paramValues == null ? "null" : ""+Arrays.asList(paramValues));
	}

}
