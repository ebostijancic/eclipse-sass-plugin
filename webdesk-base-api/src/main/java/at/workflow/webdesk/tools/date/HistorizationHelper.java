package at.workflow.webdesk.tools.date;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import at.workflow.webdesk.tools.api.Historization;

/**
 * Contains code that was previously in PoHistorization.
 * 
 * It generates useful valid-from and valid-to values in relation to the current system time.
 * If a valid-from is generated, usually the time-portion gets cut if the given date is after today.
 * If a valid-to is generated, we take the 'last moment of the day' if the given date is in future.
 * 
 * @author ggruber
 */
public final class HistorizationHelper {
	
	/**
	 * Initializes the given domain-object with useful valid-from and valid-to values.
	 * @param historicizable the domain-object to initialize with useful validfrom and validto dates.
	 */
	public static void initialize(Historization historicizable)	{
		historicizable.setValidfrom(null);
		historicizable.setValidto(null);
	}
	
	/**
	 * For null, the current millisecond will be returned (millisecond precision).
	 * Any <code>validFrom</code> value before start of current day will be returned unchanged (precision estimated by caller).
	 * Values after start of current day (future, including today) will be changed to be the first moment of current day (day precision).
	 * The maximum future is standardized to <code>DateTools.INFINITY_TIMEMILLIS</code> (which is 1.1.3000 00:00:00.000).
	 * 
	 * @param validFrom the date to be used as valid-from Date, can be null, then current time is returned.
	 * @return a valid-from Date comparable with valid-from dates used in historization.
	 */
	public static Date generateUsefulValidFrom(Date validFrom) {
		if (validFrom == null)
			return now();
		
		if (validFrom.getTime() == DateTools.INFINITY_TIMEMILLIS)
			return validFrom;
		
		if (validFrom.getTime() > DateTools.INFINITY_TIMEMILLIS)
			return new Date(DateTools.INFINITY_TIMEMILLIS);
		
		final Date validityDayStart = DateTools.dateOnly(validFrom);
		final Date currentDayStart = DateTools.dateOnly(now());
		if (validityDayStart.after(currentDayStart))
			return validityDayStart;

		return validFrom;
	}
	
	/**
	 * For null, the maximum future (see below) will be returned.
	 * Any <code>validTo</code> value before or equal to the current millisecond will be returned unchanged (precision estimated by caller).
	 * Values after current milliseconds (future) will be changed to be the last moment of current day (day precision).
	 * The maximum future is standardized to <code>DateTools.INFINITY_TIMEMILLIS</code> (which is 1.1.3000 00:00:00.000).
	 * 
	 * @param validTo the date to be used as valid-to Date, can be null, then current time is returned.
	 * @return a valid-to Date comparable with valid-to dates used in historization.
	 */
	public static Date generateUsefulValidTo(Date validTo) {
		if (validTo == null || validTo.getTime() > DateTools.INFINITY_TIMEMILLIS)
			return new Date(DateTools.INFINITY_TIMEMILLIS);
		
		if (validTo.getTime() == DateTools.INFINITY_TIMEMILLIS)
			return validTo;
		
		if (validTo.getTime() > now().getTime()) 
			return DateTools.lastMomentOfDay(validTo);

		return validTo;
	}
	
	/**
	 * For null, the beginning of today will be returned.
	 * Any <code>validFrom</code> value that represents the start of day will be returned unchanged.
	 * Values after start of a day will be changed to be the first moment of the day (day precision).
	 * The maximum future is standardized to <code>DateTools.INFINITY_TIMEMILLIS</code> (which is 1.1.3000 00:00:00.000).
	 * 
	 * @param validFrom the date to be used as valid-from Date, can be null, then today is returned.
	 * @return a valid-from Date comparable with valid-from dates used in historization.
	 */
	public static Date generateUsefulValidFromDay(Date validFrom) {
		if (validFrom == null)
			return DateTools.today();
		
		if (validFrom.getTime() == DateTools.INFINITY_TIMEMILLIS)
			return validFrom;
		
		if (validFrom.getTime() > DateTools.INFINITY_TIMEMILLIS)
			return new Date(DateTools.INFINITY_TIMEMILLIS);
		
		return DateTools.dateOnly(validFrom);
	}
	
	/**
	 * For null, the maximum future (see below) will be returned.
	 * Any <code>validTo</code> value equal to the lastMomentOfDay (DateTools) will be returned unchanged.
	 * Values different from lastMomentOfDay (DateTools) will be changed to be the last moment of the day (day precision).
	 * The maximum future is standardized to <code>DateTools.INFINITY_TIMEMILLIS</code> (which is 1.1.3000 00:00:00.000).
	 * 
	 * @param validTo the date to be used as valid-to Date, can be null.
	 * @return a valid-to Date comparable with valid-to dates used in historization.
	 */
	public static Date generateUsefulValidToDay(Date validTo) {
		if (validTo == null || validTo.getTime() > DateTools.INFINITY_TIMEMILLIS)
			return new Date(DateTools.INFINITY_TIMEMILLIS);
		
		if (validTo.getTime() == DateTools.INFINITY_TIMEMILLIS)
			return validTo;
		
		return DateTools.lastMomentOfDay(validTo);
	}
	
	private static Date now()	{
		return DateTools.now();
	}
	
	/**
	 * @param domainObject the historicized object to be tested for being valid at passed time.
	 * @param queryDate the time to be tested for being within validity interval of passed object.
	 * @return true when the passed historicized object is valid at referenceDate.
	 */
	public static boolean isValid(Historization domainObject, Date queryDate) {
		return domainObject.getValidity().isValid(queryDate);
	}
	
	/**
	 * @param domainObject the historicized object to be tested for being valid at passed or future time.
	 * @param queryDate the time to be tested for being below valid-to time of passed object.
	 * @return true when the passed historicized object is valid at referenceDate, of will be valid in future.
	 */
	public static boolean isValidOrFuture(Historization domainObject, Date queryDate) {
		return domainObject.getValidity().isValidOrFuture(queryDate);	// fri_2013-09-30: instable unit test fail in WTestHrCreateMedicalDemoData, will be fixed by introducing Null-Object instead of null validity
	}
	
	/**
	 * @param domainObject the historicized object to be tested for being valid at future time, assumed that given queryDate is current date.
	 * @param queryDate the time to be tested for being before both valid-from and valid-to time of passed object.
	 * @return true when the passed historicized object will be valid in future only.
	 */
	public static boolean isFuture(Historization domainObject, Date queryDate) {
		return domainObject.getValidity().isFuture(queryDate);
	}
	
	/**
	 * @param domainObject the historicized object to be tested for being valid now.
	 * @return true when the passed historicized object is currently valid.
	 */
	public static boolean isValid(Historization domainObject) {
		return isValid(domainObject, now());
	}

	/**
	 * @param domainObject the historicized object to be tested for being valid at passed or future time.
	 * @param referenceDate the time to be tested for being below valid-to time of passed object.
	 * @return true when the passed historicized object is valid at referenceDate, of will be valid in future.
	 */
	public static boolean isValidOrFuture(Historization domainObject) {
		return isValidOrFuture(domainObject, now());
	}
	
	
	/**
	 * Use this to add text to your WHERE clause that describes that the record must be valid now.
	 * A record is valid when its valid-from is before or equal to <i>now</i>, and its valid-to is after <i>now</i>.
	 * @param queryParameters the parameter list to append values to, according to returned query.
	 * @param tableAlias can be null, else it is the alias (without training dot) to be used before attribute names: "... p.validfrom <= ? ...".
	 * @return " validfrom <= ? and validto > ?", adds parameters to queryParameters list.
	 */
	public static String isValid(List<Object> queryParameters, String tableAlias)	{
		return isValid(null, null, queryParameters, tableAlias);
	}
	
	/**
	 * Use this to add text to your WHERE clause that describes that the record must be valid within given interval.
	 * A record is valid when its valid-from is before or equal to <i>now</i>, and its valid-to is after <i>now</i>.
	 * @param validFrom the from-validity time point, can be null, then now is used.
	 * @param validTo the until-validity time point, can be null, then now is used.
	 * @param queryParameters the parameter list to append values to, according to returned query.
	 * @param tableAlias can be null, else it is the alias (without training dot) to be used before attribute names: "... p.validfrom <= ? ...".
	 * @return " validfrom <= ? and validto > ?", adds parameters to queryParameters list.
	 */
	public static String isValid(Date validFrom, Date validTo, List<Object> queryParameters, String tableAlias)	{
		final Date now = DateTools.now();
		queryParameters.add(validFrom != null ? validFrom : now);
		queryParameters.add(validTo != null ? validTo : now);
		return isValid(tableAlias);
	}
	
	/**
	 * Use this to add text to your WHERE clause that describes that the record must be valid.
	 * @param tableAlias can be null, else it is the alias (without training dot) to be used before attribute names: "... p.validfrom <= ? ...".
	 * @return " validfrom <= ? and validto > ?".
	 */
	public static String isValid(String tableAlias)	{
		final String alias = (tableAlias != null && tableAlias.length() > 0) ? tableAlias+"." : "";
		// TODO: following is code duplication with HistoricizingDAOImpl
		return " "+alias+Historization.VALID_FROM+" <= ? and "+isValidOrFuture(tableAlias);
	}
	
	/** Calls isValid(tableAlias) without table-alias (null). */
	public static String isValid()	{
		return isValid((String) null);
	}
	
	/**
	 * Use this to add text to your WHERE clause that describes that the record must be valid.
	 * @param tableAlias can be null, else it is the alias to use before attribute names: " and p.validfrom <= ?".
	 * @return " validto > ?".
	 */
	public static String isValidOrFuture(String tableAlias)	{
		final String alias = (tableAlias != null && tableAlias.length() > 0) ? tableAlias+"." : "";
		// TODO: following is code duplication with HistoricizingDAOImpl
		return alias + Historization.VALID_TO + " > ? ";
	}

	/** Calls isValidOrFuture(null). */
	public static String isValidOrFuture()	{
		return isValidOrFuture((String) null);
	}
	
	
	
    /**
     * Delegates to <code>getWorkingQueryDate(null)</code>.
     * @return a time that is <code>nowWithPrecisionHour</code>.
     */
    public static Date getNowWithHourPrecision() {
    	return DateTools.nowWithHourPrecision();
    }
    
    /**
     * This is primarily for avoiding null historization dates.
     * @param effectiveDate the date to be used for historization queries,
     * 		can be null, then <code>nowWithPrecisionHour</code> is returned.
     * 		Effective date = Wirkungsdatum, im Gegensatz zu Erfassungsdatum.
     * @return a time usable with historization queries,
     * 		<code>nowWithHourPrecision()</code> when effectiveDate is null,
     * 		else a clone of passed date.
     */
	public static Date getEffectiveDateOrNowWithHourPrecision(Date effectiveDate) {
		return (effectiveDate == null) ? getNowWithHourPrecision() : (Date) effectiveDate.clone();
	}

	
	/**
	 * Extracts the list of valid objects in given list.
	 * @param validAndInvalidObjects the list containing valid and invalid objects.
	 * @param futureIsValid when true, also objects getting valid in future only will be regarded as valid.
	 * @return a new list containing only valid objects, or null when null was given.
	 */
	public static List<? extends Historization> getValidObjects(Collection<? extends Historization> validAndInvalidObjects, boolean futureIsValid)	{
		return getValidOrInvalidObjects(true, validAndInvalidObjects, futureIsValid ? ValidityType.VALID_OR_FUTURE : ValidityType.VALID);
	}
	
	/**
	 * Extracts the list of invalid objects in given list.
	 * @param validAndInvalidObjects the list containing valid and invalid objects.
	 * @param futureIsValid when true, also objects getting valid in future only will be regarded as valid.
	 * @return a new list containing only invalid objects, or null when null was given.
	 */
	public static List<? extends Historization> getInvalidObjects(Collection<Historization> validAndInvalidObjects, boolean futureIsValid)	{
		return getValidOrInvalidObjects(false, validAndInvalidObjects, futureIsValid ? ValidityType.VALID_OR_FUTURE : ValidityType.VALID);
	}
	
	/**
	 * Extracts the list of objects getting valid in future only.
	 * @param validAndInvalidObjects the list containing valid and invalid objects.
	 * @return a new list containing only future-valid objects, or null when null was given.
	 */
	public static List<? extends Historization> getFutureObjects(Collection<Historization> validAndInvalidObjects)	{
		return getValidOrInvalidObjects(true, validAndInvalidObjects, ValidityType.FUTURE);
	}
	
	private enum ValidityType
	{
		VALID,
		VALID_OR_FUTURE,
		FUTURE
	}
	
	private static List<? extends Historization> getValidOrInvalidObjects(boolean takeValidOnes, Collection<? extends Historization> validAndInvalidObjects, ValidityType validityType)	{
		if (validAndInvalidObjects == null)
			return null;
		
		final List<Historization> filteredObjects = new ArrayList<Historization>();
		
		for (Historization object : validAndInvalidObjects)	{
			final boolean valid;
			if (validityType == ValidityType.VALID_OR_FUTURE)
				valid = isValidOrFuture(object);
			else if (validityType == ValidityType.VALID)
				valid = isValid(object);
			else if (validityType == ValidityType.FUTURE)
				valid = isFuture(object, now());
			else
				throw new IllegalArgumentException("Unknown type of validity: "+validityType);
				
			if (valid == takeValidOnes)
				filteredObjects.add(object);
		}
		
		return filteredObjects;
	}
	
	
	private HistorizationHelper() {}	// do not instantiate
}
