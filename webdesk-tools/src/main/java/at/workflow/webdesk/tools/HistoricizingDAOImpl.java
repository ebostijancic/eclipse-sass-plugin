package at.workflow.webdesk.tools;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.workflow.webdesk.HistoricizingDAO;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.date.DateInterval;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.date.HistorizationHelper;
import at.workflow.webdesk.tools.date.Interval;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

/**
 * Represents the DAO for historicized entity-types that have a validForm and a validTo date.
 * Provides a method to check database attributes in a POJO for uniqueness
 * (can't declare such as unique constraint in database because of temporal nature).
 * <p/>
 * To define unique attributes, write checkXxx() methods for each unique constraint XXX.
 * That method should contain the name(s) of the checked attribute (set). Values must be taken from
 * the runtime POJO (passed in as parameter for that method). From that method then call
 * <code>isAttributeSetUniqueWithinValidityTime(Xxx.class.getSimpleName(), attributesMap, pojo).</code>
 * <p/>
 * Expose each unique constraint checker method to the service layer for calls by the UI.
 * <p/>
 * Additionally override <code>beforeSave()</code> in DAO, from there call all checker
 * methods and throw an exception when one returns true.
 * 
 * Created on 13.03.2006 as DaoHibernateUtil.java
 * 
 * @author hentner (Harald Entner)
 * @author fritzberger 2011-10-12 refactoring and documentation.
 */
public abstract class HistoricizingDAOImpl<E extends Historization> extends GenericHibernateDAOImpl<E> implements HistoricizingDAO<E> {

	/**
	 * Checks the validFrom and validTo dates and sets defaults into them when null.
	 * {@inheritDoc}
	 */
	@Override
	protected void beforeSave(E h) {
		if (h.getValidfrom() == null)
			h.setValidfrom(DateTools.now());
		
		if (h.getValidto() == null)
			h.setValidto(DateTools.INFINITY);
	}
	
	/** {@inheritDoc} */
	@Override
	public List<E> loadAllValid()	{
		return loadAllValid(null);
	}
	
	/** {@inheritDoc} */
	@Override
	public List<E> loadAllValid(Date validityDate)	{
		return loadAllValid(validityDate, validityDate);
	}
	
	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public List<E> loadAllValid(Date validFrom, Date validTo)	{
		List<Object> parameters = new ArrayList<Object>();
		String query = from()+" where "+isValid(validFrom, validTo, parameters, null);
		return getHibernateTemplate().find(query, parameters.toArray(new Object[parameters.size()]));
	}
	
	/** Calls isValid(null, ...). */
	protected String isValid(List<Object> queryParameters, String tableAlias)	{
		return isValid(null, queryParameters, tableAlias);
	}
	
	/** Calls isValid(validityDate, validityDate, ...). */
	protected String isValid(Date validityDate, List<Object> queryParameters, String tableAlias)	{
		return isValid(validityDate, validityDate, queryParameters, tableAlias);
	}
	
	/**
	 * This is to be used with find(Filter) for Historization entities.
	 * @param timePoint the reference date for which filters should be returned.
	 * @return two filters that carry the condition that the retrieved object must be valid in the sense of HistorizationHelper.isValid().
	 */
	protected Filter [] validityFilters(Date timePoint)	{
		final Filter [] filters = new Filter[2];
		// TODO: following is code duplication with HistorizationHelper
		filters[0] = new Filter(Historization.VALID_FROM, Filter.CompareOperator.LESS_EQUAL, timePoint);
		filters[1] = new Filter(Historization.VALID_TO, Filter.CompareOperator.GREATER_THAN, timePoint);
		return filters;
	}

	/**
	 * Use this to add text to your WHERE clause that describes that the record must be valid.
	 * A record is valid when its valid-from is before or equal to <i>now</i>, and its valid-to is after <i>now</i>.
	 * @param validFrom the from-validity time point, can be null, then now is used.
	 * @param validTo the until-validity time point, can be null, then now is used.
	 * @param queryParameters the parameter list to append values to, according to returned query.
	 * @param tableAlias can be null, else it is the alias to use before attribute names: " and p.validfrom <= ?".
	 * @return " validfrom <= ? and validto > ?".
	 */
	protected String isValid(Date validFrom, Date validTo, List<Object> queryParameters, String tableAlias)	{
		return HistorizationHelper.isValid(validFrom, validTo, queryParameters, tableAlias);
	}
	
	/** Calls isValid((String) null). */
	protected String isValid()	{
		return isValid((String) null);
	}
	
	/**
	 * Use this to add text to your WHERE clause that describes that the record must be valid.
	 * This method requires to add <b>two</b> <i>"now"</i> arguments in query parameters.
	 * @param tableAlias can be null, else it is the alias to use before attribute names: " and p.validfrom <= ? and p.validto > ?".
	 * @return " validfrom <= ? and validto > ?".
	 */
	protected String isValid(String tableAlias)	{
		return HistorizationHelper.isValid(tableAlias);
	}
	
	/** Calls isValidOrFuture(null). */
	protected String isValidOrFuture()	{
		return isValidOrFuture((String) null);
	}
	
	/**
	 * Use this to add text to your WHERE clause that describes that the record must be valid.
	 * This method requires to add <b>one</b> <i>"now"</i> argument in query parameters.
	 * @param tableAlias can be null, else it is the alias to use before attribute names: " and p.validfrom <= ?".
	 * @return " validto > ?".
	 */
	protected String isValidOrFuture(String tableAlias)	{
		return HistorizationHelper.isValidOrFuture(tableAlias);
	}
	
	/**
	 * This is for programmed checks if a historicizable object is valid now.
	 * @param pojo the entity to check for temporal validity.
	 * @return true if given entity is valid by now.
	 */
	protected boolean isValid(Historization pojo)	{
		return isValid(pojo, DateTools.now());
	}
	
	/**
	 * This is for programmed checks if a historicizable object is valid.
	 * @param pojo the entity to check for temporal validity.
	 * @param validityDate the date/time the object has to be valid at.
	 * @return true if given entity is valid by validityDate.
	 */
	protected boolean isValid(Historization pojo, Date validityDate)	{
		assert pojo != null : "Can not check validity on a null POJO!";
		return HistorizationHelper.isValid(pojo, validityDate);
	}
	
	/**
	 * This is for programmed checks if a historicizable object is valid now or will become valid in future.
	 * @param pojo the entity to check for temporal validity.
	 * @return true if given entity is valid by now.
	 */
	protected boolean isValidOrFuture(Historization pojo)	{
		return isValidOrFuture(pojo, DateTools.now());
	}
	
	/**
	 * This is for programmed checks if a historicizable object is valid or will become valid in future.
	 * @param pojo the entity to check for temporal validity.
	 * @param validityDate the date/time the object has to be valid at.
	 * @return true if given entity is valid by validityDate.
	 */
	protected boolean isValidOrFuture(Historization pojo, Date validityDate)	{
		assert pojo != null : "Can not check validity on a null POJO!";
		return HistorizationHelper.isValidOrFuture(pojo, validityDate);
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean historicize(E historizable)	{

		historizable.historicize();
		// check if the permission has never become valid
		final boolean wasNeverValid = ! historizable.getValidity().isPositive();
		if (wasNeverValid)	{	// entities that were not yet valid must be deleted (this is a webdesk business rule)
			delete(historizable);
			return true;	// was deleted physically
		}
		save(historizable);
		return false;	// was historicized
	}
	
	/**
	 * Called by <code>isAttributeSetUnique()</code>.
	 * This is a replacement for a unique database constraint for historicized tables.
	 * Within the time range of POJO's validfrom/validto the given set of attributes must be unique.
	 * <p/>
	 * Checks if an object with a set of specified attribute values has validity-time
	 * overlaps with other objects that have the same attribute values and are in same database table.
	 * Only objects with valid-to time bigger than current time are checked.
	 * <p/>
	 * This can hardly be expressed by a database constraint.
	 * <p/>
	 * This works only with POJO's that have <i>validfrom</i>, <i>validto</i>
	 * and (of course) the set of attributes to check (passed via <i>attributes</i>).
	 * <p/>
	 * Checking constraints by "select" in the same transaction as a dirty POJO resides might lead
	 * to a Hibernate session flush() and thus inserting or updating possibly just the checked POJO.
	 * So this could result in hard-to-detect and maybe non-reproducible bugs.
	 * 
	 * @return true if the attribute-set would be unique considering their validity, false otherwise.
	 */
	@Override
	protected String addFurtherCriteria(String queryText, List<Object> parameterValues, String entityAboutToBeSavedUid, Interval validity) {
		queryText = super.addFurtherCriteria(queryText, parameterValues, entityAboutToBeSavedUid, validity);
		
		if (validity != null && validity.isNegative())
			//throw new IllegalArgumentException("Do not pass a negative interval to uniqueness check for historiziced enitities!");	// can not do this because WTestPoRoleServiceImpl would fail then
			validity = new DateInterval(validity.getFrom(), validity.getFrom());	// check uniqueness on the _intended_ time point
		
		final Date now = DateTools.now();
		final Date validFrom = (validity != null && validity.getFrom() != null) ? validity.getFrom() : now;
		final Date validTo   = (validity != null && validity.getTo() != null)   ? validity.getTo()   : DateTools.INFINITY;

		/*
		 * The condition "t.validto >= ?" (check only for valid and future items)
		 * has been put under an if when a unit test failed because of this.
		 * Now we think that this condition is not needed at all.
		 * This condition makes sense for UI inputs, but not for Webdesk Connectors/Jobs.
		 * 
		// if no entityAboutToBeSavedUid was passed, assume that this check is
		// for valid or future objects only, not for historicized objects.
		// fri_2013-07-02: this is new logic, made for checking uniqueness also for a historicized entityAboutToBeSaved
		if (entityAboutToBeSavedUid == null)	{
			// queried record's valid-to must be after now
			queryText = queryText+" and t."+Historization.VALID_TO+" >= ?";
			parameterValues.add(now);
			// this confirms that only records currently valid will be found
		}
		*/
		
		// queried record's valid-from must be before pending POJO valid-to value
		queryText = queryText+" and t."+Historization.VALID_FROM+" <= ?";
		parameterValues.add(validTo);
		// queried record's valid-to must be after pending POJO valid-from value
		queryText = queryText+"  and t."+Historization.VALID_TO+" >= ?";
		parameterValues.add(validFrom);
		// this confirms that records will be found that include
		// either the start or the end of the POJO validity,
		// i.e. the POJO's validity overlaps in some way with the record's validity
		
		return queryText;

		// fri_2012-01-12 this replaces the 4 queries of old implementation.
		// For transparency following comment contains the check as it was before,
		// with all 4 covered overlap-constellations.
		
		// TODO write a dedicated unit test that covers all overlap-constellations
		
//		// query parameters are:
//		// pojo.validFrom, pojo.validTo, pojo.validFrom, pojo.validTo, now
//	
//		/*
//		 * Overlap on the left side of POJO time range
//		 * RECORD |............| 
//		 * POJO         |...........|
//		 */
//		if (foundAtLeastOne(queryText+
//				" and t.validfrom <= ? and t.validfrom <= ? "+
//				" and t.validto >= ? and t.validto <= ? and t.validto >= ? "+
//				parameters))
//			return true;
//
//		/*
//		 * Overlap on the right side of POJO time range
//		 * RECORD       |............| 
//		 * POJO   |...........|
//		 */
//		if (foundAtLeastOne(queryText+
//				" and t.validfrom >= ? and t.validfrom <= ? "+
//				" and t.validto >= ? and t.validto >= ?  and t.validto >= ? "+
//				parameters))
//			return true;
//	
//		/*
//		 * Totally inside POJO time range
//		 * RECORD     |...| 
//		 * POJO   |...........|
//		 */
//		if (foundAtLeastOne(queryText+
//				" and t.validfrom >= ? and t.validfrom <= ? "+
//				" and t.validto >= ? and t.validto <= ? and t.validto >= ? "+
//				parameters))
//			return true;
//
//		/*
//		 * Includes POJO time range
//		 * RECORD |...........| 
//		 * POJO       |....|
//		 */
//		if (foundAtLeastOne(queryText+
//				" and t.validfrom <= ? and t.validfrom <= ? "+
//				" and t.validto >= ? and t.validto >= ?  and t.validto >= ? "+
//				parameters))
//			return true;
//	
//		return false;
	
	}


}
