package at.workflow.webdesk.tools.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import at.workflow.webdesk.QueryBuilderHelper;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.date.Interval;

/**
 * Generic data access object (DAO) implementation for the Hibernate persistence layer,
 * providing utility methods.
 * 
 * @author ggruber
 * @author fritzberger (refactorings and extensions).
 */
public abstract class GenericHibernateDaoSupport extends HibernateDaoSupport {

	/** Sub-classes must return the type they are supporting, i.e. the Java class representing a database table. */
	protected abstract Class<?> getEntityClass();

	/** Sub-classes must return an entity of the type they are supporting, i.e. read the entity of given unique identifier. */
	protected abstract PersistentObject getPersistentObject(String entityUid);

	// general helpers

	/** Checks for null or no entries. */
	protected final boolean isEmpty(List<?> list) {
		return list == null || list.isEmpty();
	}

	/** Checks for null or no entries. */
	protected final boolean isNotEmpty(List<String> list) {
		return isEmpty(list) == false;
	}

	// finder query helpers

	/**
	 * Helper class for a generic find() implementation.
	 * Does not support logical operators (AND, OR) because this would require another helper class.
	 * 
	 * @author fritzberger 07.02.2012
	 */
	protected static class Filter extends at.workflow.webdesk.tools.Filter
	{
		/** Compare-operators usable with this Filter class. */
		public enum CompareOperator
		{
			EQUAL("="),
			GREATER_THAN(">"),
			GREATER_EQUAL(">="),
			LESS_THAN("<"),
			LESS_EQUAL("<="),
			IN("in"),
			LIKE("like");

			private String symbol;

			private CompareOperator(String symbol) {
				this.symbol = symbol;
			}

			@Override
			public String toString() {
				return symbol;
			}
		}

		/** Property-value needed internally to control behavior of null-values. */
		private static final Object IGNORE_IN_QUERY = new Object();

		private final CompareOperator compareOperator;

		/** Default constructor for "=" comparisons. */
		public Filter(String name, Object value) {
			this(name, CompareOperator.EQUAL, value, false);
		}

		/** IN-clause constructor. */
		public Filter(String name, List<?> possibleValues) {
			this(name, CompareOperator.IN, possibleValues, false);
		}

		/** General constructor, declares a null value to be <b>not</b> ignored. */
		public Filter(String name, CompareOperator compareOperator, Object value) {
			this(name, compareOperator, value, false);
		}

		/** Luxury constructor. */
		public Filter(String name, CompareOperator compareOperator, Object value, boolean ignoreWhenValueNull) {
			super(name, (ignoreWhenValueNull && value == null) ? IGNORE_IN_QUERY : value);

			assert name != null && compareOperator != null;

			this.compareOperator = compareOperator;
		}
	}

	/**
	 * Use this method when <b>all</b> filters should be ignored when having a null value.
	 * @return only those filters that have a non-null value.
	 */
	protected Filter[] nonNullFilters(Filter[] filters) {
		List<Filter> nonNullFilters = new ArrayList<Filter>();
		for (Filter filter : filters)
			if (filter.getValue() != null)
				nonNullFilters.add(filter);
		return nonNullFilters.toArray(new Filter[nonNullFilters.size()]);
	}

	/**
	 * Generic find implementation that AND's all passed criteria.
	 * IN-clauses are described by passing a List as attributeValue in Filter.
	 * NULL values are handled via <code>Filter.IGNORE_IN_QUERY</code>.
	 * @param queryStart the SELECT or FROM clause to use,
	 * 		can be null, then <code>from()</code> is called. Must not contain "where"!
	 * @param filters the filters to apply to query, all AND'ed.
	 * @param orderBy the ORDER BY clause text, without leading "oder by", can be null.
	 * @return the List of found entities.
	 */
	@SuppressWarnings({ "rawtypes" })
	protected List find(final String queryStart, final Filter[] filters, final String orderBy) {
		String query = queryStart != null && queryStart.trim().length() > 0 ? queryStart : from();
		List<Object> parameters = new ArrayList<Object>();
		boolean whereClauseStarted = false;
		boolean attributeWritten = true;

		for (Filter filter : filters) {
			if (filter.getValue() != Filter.IGNORE_IN_QUERY) {
				if (whereClauseStarted == false) {
					whereClauseStarted = true;
					query += " where ";
				}
				else if (attributeWritten) {
					query += " and ";
				}

				attributeWritten = true;
				query += filter.getName();

				if (filter.getValue() != null) { // handle non-null value
					query += " " + filter.compareOperator.toString();

					if (filter.getValue() instanceof List) { // handle IN-clause
						assert filter.compareOperator == Filter.CompareOperator.IN : "Can not process a List as value when operator is " + filter.compareOperator;

						List<?> values = (List<?>) filter.getValue();
						query += " (";
						for (int i = 0; i < values.size(); i++) {
							query += (i == 0) ? "?" : ",?";
							parameters.add(values.get(i));
						}
						query += ")";

						if (values.size() > 255)
							logger.warn("This IN-clause might not run on any database, because more than 255 elements were passed (" + values.size() + ") -> implement fragmenting!");
					}
					else { // handle normal value
						query += " ?";
						parameters.add(filter.getValue());
					}
				}
				else { // handle null-value
					query += " is null";
				}
			}
			else {
				attributeWritten = false; // avoid appending "and"
			}
		}

		if (orderBy != null)
			query += " order by " + orderBy;

		return getHibernateTemplate().find(query, parameters.toArray(new Object[parameters.size()]));
	}

	/**
	 * @return for example "PoPerson" for wrapped class "at.workflow.webdesk.model.PoPerson",
	 * 		this can be used as default for the database table name of wrapped entity class.
	 */
	protected String queryEntityName() {
		return getEntityClass().getSimpleName();
	}

	/**
	 * @return for example "from PoPerson" for wrapped class "at.workflow.webdesk.model.PoPerson".
	 */
	protected String from() {
		return "from " + queryEntityName();
	}

	/**
	 * Convert the passed list of UIDs to a text that can be put into the matches of an
	 * SQL IN clause like "WHERE myAttribut IN ('1','2','3')".
	 * Mind that you should check if the return is not "" before building an invalid query!
	 * @param uidList the list of UID to catenize to a CSV text.
	 * @return a String-representation of an uid-list in this form: 'uid1','uid2','uid3' 
	 */
	protected final String toInClauseText(List<String> uidList) {
		if (uidList == null || uidList.size() <= 0)
			return "";

		return QueryBuilderHelper.generateCommaList(uidList, true);
	}

	// unique constraint checking

	/**
	 * This is a replacement for a unique database constraint, to be used by UI validators.
	 * 
	 * MIND: when entityAboutToBeSavedUid is null, this implementation assumes that the newly inserted entity
	 * will be valid from "now" (will not have a validity data in past), so this is not a check
	 * for uniqueness over all times!
	 * 
	 * @param attributeName the name of a database attribute to be checked for uniqueness.
	 * @param attributeValue the value to be checked for uniqueness.
	 * @param entityAboutToBeSavedUid the UID (primary key value) of the object containing the attribute, can be null when new object.
	 * @param validity can be null for entities not implementing Historization, else the validity of the object containing the attribute.
	 * @return true if the attribute value would be unique, false otherwise.
	 */
	protected final boolean isAttributeUnique(String attributeName, Object attributeValue, String entityAboutToBeSavedUid, Interval validity) {
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put(attributeName, attributeValue);
		return isAttributeSetUnique(attributes, entityAboutToBeSavedUid, validity);
	}

	/**
	 * This is a replacement for a unique database constraint, to be used by UI validators.
	 * 
	 * MIND: when entityAboutToBeSaved is null, this implementation assumes that the newly inserted entity
	 * will be valid from "now" (will not have a validity data in past), so this is not a check
	 * for uniqueness over all times!
	 * 
	 * @param attributes a Map that contains at least one name/value tuple.
	 * 		<i>Name</i> is the name of the bean property corresponding to the database attribute,
	 * 		and <i>value</i> is its value to be queried.
	 * @param entityAboutToBeSavedUid the UID (primary key value) of the object containing the attribute, can be null when new object.
	 * @param validity can be null for entities not implementing Historization, else the validity of the object containing the attribute.
	 * @return true if the attribute-set would be unique, false otherwise.
	 */
	protected final boolean isAttributeSetUnique(Map<String, Object> attributes, String entityAboutToBeSavedUid, Interval validity) {
		return isAttributeSetUnique(null, attributes, entityAboutToBeSavedUid, validity);
	}

	/**
	 * This is a replacement for a unique database constraint, to be used by UI validators.
	 * 
	 * MIND: when entityAboutToBeSaved is null, this implementation assumes that the newly inserted entity
	 * will be valid from "now" (will not have a validity data in past), so this is not a check
	 * for uniqueness over all times!
	 * 
	 * @param tableClass the class corresponding to the database table, e.g. PoPerson.class,
	 * 		this parameter can be null when using the entity-class of the DAO.
	 * @param attributes a Map that contains at least one name/value tuple.
	 * 		<i>Name</i> is the name of the bean property corresponding to the database attribute,
	 * 		and <i>value</i> is its value to be queried.
	 * @param entityAboutToBeSavedUid the UID (primary key value) of the object containing the attribute, can be null when new object.
	 * @param validity can be null for entities not implementing Historization, else the validity of the object containing the attribute.
	 * @return true if the attribute-set would be unique, false otherwise.
	 */
	protected boolean isAttributeSetUnique(Class<?> tableClass, Map<String, Object> attributes, String entityAboutToBeSavedUid, Interval validity) {
		assert attributes.size() > 0 : "This would not work without at least one attribute!";

		final String table = tableClass != null ? tableClass.getSimpleName() : queryEntityName();
		final List<Object> parameterValues = new ArrayList<Object>();
		String buildQueryText =
				"select count(*) from " + table + " t where " +
						buildWhereClauseFromAttributeValues(attributes, parameterValues); // does it with "t." alias

		if (entityAboutToBeSavedUid != null) { // persistent POJO
			buildQueryText = buildQueryText + " and t.UID != ?";
			parameterValues.add(entityAboutToBeSavedUid);
		}
		// else: transient

		buildQueryText = addFurtherCriteria(buildQueryText, parameterValues, entityAboutToBeSavedUid, validity);

		return false == foundAtLeastOne(buildQueryText, parameterValues.toArray());
	}

	/** To be overridden. @return the queryText to be applied, at least the given one. */
	@SuppressWarnings("unused")	// this was made for overriding
	protected String addFurtherCriteria(String queryText, List<Object> parameterValues, String entityAboutToBeSavedUid, Interval validity) {
		return queryText;
	}

	/** Launches the passed query with parameters and returns true when the result was > 0. */
	protected boolean foundAtLeastOne(String query, Object[] parameters) {
		@SuppressWarnings("unchecked")
		final List<Long> result = getHibernateTemplate().find(query, parameters);
		return result.get(0) > 0;
	}

	/** Builds a query text from the given attribute names, adds their values to parameterValues List. */
	private String buildWhereClauseFromAttributeValues(Map<String, Object> attributes, final List<Object> parameterValues) {
		final StringBuffer queryTextBuffer = new StringBuffer();

		for (Map.Entry<String, Object> keyAndValue : attributes.entrySet()) {
			if (queryTextBuffer.length() > 0)
				queryTextBuffer.append("and");

			queryTextBuffer.append(" t.");
			queryTextBuffer.append(keyAndValue.getKey());
			queryTextBuffer.append(" = ? ");

			parameterValues.add(keyAndValue.getValue());
		}

		return queryTextBuffer.toString();
	}

}
