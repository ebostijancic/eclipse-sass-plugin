package at.workflow.webdesk.tools.hibernate;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import at.workflow.webdesk.tools.Datatype;
import at.workflow.webdesk.tools.Filter;
import at.workflow.webdesk.tools.date.DateTools;

/**
 * This class builds a where clause for a HQL/JPA Query. It assumes that filter values are supplied
 * as strings and have to be converted to the appropriate datatype which is either passed as additional parameter or
 * implicitly defined by the called method.
 * 
 * For a one-stop-shop use {@link HqlWhereClauseBuilder#appendFilterToWhereClause(Locale, ConcatOp, String, List, String, Datatype, String, boolean)}
 * in a row for all filter values to add to the where clause. In order to do this, you should either supply the correct filterDataType or null (in that
 * case the datatype is being guessed by parsing the supplied string filtervalue)
 * 
 * @author sdzuban
 * @author ggruber (refactoring)
 *
 * TODO: please document all public methods, and every parameter and return of it.
 */
public class HqlWhereClauseBuilder {
	
	/** The text to be used for marking wildcard-search patterns. */
	public static final String LIKE_PREFIX = "like ";
	
	/** enum describing the 2 possible logical concatenation operators **/ 
	public enum ConcatOp {
		AND, OR;
		
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	
	private Pattern timePattern = Pattern.compile("\\D*(\\d\\d?:\\d\\d?)\\D*(\\d\\d?:\\d\\d?)?.*");
	
	/**
	 * @return a Filter that has the same name as given filter, but
	 * its value is embedded into "like %x%", whereby x is the old value.
	 * Mind that a non-String filter would be converted into a String by calling this method!
	 */
	public Filter toContains(Filter filter)	{
		return toStartsWithOrContains(filter, true);
	}
	
	/**
	 * @return a Filter that has the same name as given filter, but
	 * its value is embedded into "like x%", whereby x is the old value.
	 * Mind that a non-String filter would be converted into a String by calling this method!
	 */
	public Filter toStartsWith(Filter filter)	{
		return toStartsWithOrContains(filter, false);
	}

	private Filter toStartsWithOrContains(Filter filter, boolean isContains)	{
		return (filter != null && filter.getValue() != null)
				? new Filter(filter.getName(), LIKE_PREFIX+(isContains ? "%" : "")+filter.getValue()+"%")
				: filter;
	}

	
	public String appendFilterToWhereClause(ConcatOp concatOperator, String whereClause, List<Object> parameterValues, String filterColumnName, Datatype filterType, Object filterValue, boolean searchExact) {
		
		if (filterType == Datatype.DATE || filterType == Datatype.DATETIME)	{
			whereClause = appendDateFilterToWhereClause(whereClause, filterColumnName, (Date)filterValue, concatOperator, parameterValues);
		}
		else if (filterType == Datatype.TIME)	{
			whereClause = appendTimeFilterToWhereClause(whereClause, filterColumnName, (Date)filterValue, concatOperator, parameterValues);
		}
		else if (filterType == Datatype.NUMBER || filterType == Datatype.INTEGER || filterType == Datatype.LONG || filterType == Datatype.FLOAT || filterType == Datatype.DOUBLE) {
			whereClause = appendNumberFilterToWhereClause(whereClause, filterColumnName, (Number)filterValue, concatOperator, parameterValues);
		}
		else if (filterType == Datatype.BOOLEAN)	{
			whereClause = appendBooleanFilterToWhereClause(whereClause, filterColumnName, (Boolean)filterValue, concatOperator, parameterValues);
		}
		else if (filterType == Datatype.STRING || filterValue.toString().startsWith(LIKE_PREFIX))	{
			whereClause = appendStringFilterToWhereClause(whereClause, searchExact, filterColumnName, (String)filterValue, concatOperator, parameterValues);
		}
		else
			whereClause = appendTypedFilterToWhereClause(whereClause, filterColumnName, filterValue, concatOperator, parameterValues);

		return whereClause;
		
	}
	
	public String appendFilterToWhereClause(Locale locale, ConcatOp concatOperator, String whereClause, List<Object> parameterValues, String filterColumnName, String filterValue, boolean searchExact) {
		if (StringUtils.isBlank(filterValue) == false) {
			
			// TODO filterType == null should be answered with an exception, see 
			// http://intranet/intern/ifwd_mgm.nsf/0/E14195B4E3495D95C12579C60036160D?OpenDocument notes://asterix/intern/ifwd_mgm.nsf/0/E14195B4E3495D95C12579C60036160D?EditDocument
			
			if (isDate(filterValue, locale))	{
				whereClause = appendDateFilterToWhereClause(whereClause, filterColumnName, filterValue, concatOperator, parameterValues, locale);
			}
			else if (isTime(filterValue))	{
				whereClause = appendTimeFilterToWhereClause(whereClause, filterColumnName, filterValue, concatOperator, parameterValues);
			}
			else if (isNumber(filterValue, locale))	{
				whereClause = appendNumberFilterToWhereClause(whereClause, filterColumnName, filterValue, concatOperator, parameterValues, Datatype.NUMBER, locale);
			}
			else	{	// default is String
				whereClause = appendStringFilterToWhereClause(whereClause, searchExact, filterColumnName, filterValue, concatOperator, parameterValues);
			}
		}
		return whereClause;
		
	}

	public String appendFilterToWhereClause(Locale locale, ConcatOp concatOperator, String whereClause, List<Object> parameterValues, String filterColumnName, Datatype filterType, String filterValue, boolean searchExact) {
		
		if (filterValue != null && !"".equals(filterValue)) {	// if a value was really passed
			
			if (filterType == null)	{
				return appendFilterToWhereClause(locale, concatOperator, whereClause, parameterValues, filterColumnName, filterValue, searchExact);
			}
			else if (filterType == Datatype.DATE || filterType == Datatype.DATETIME)	{
				whereClause = appendDateFilterToWhereClause(whereClause, filterColumnName, filterValue, concatOperator, parameterValues, locale);
			}
			else if (filterType == Datatype.TIME)	{
				whereClause = appendTimeFilterToWhereClause(whereClause, filterColumnName, filterValue, concatOperator, parameterValues);
			}
			else if (filterType == Datatype.NUMBER || filterType == Datatype.INTEGER || filterType == Datatype.LONG || filterType == Datatype.FLOAT || filterType == Datatype.DOUBLE) {
				whereClause = appendNumberFilterToWhereClause(whereClause, filterColumnName, filterValue, concatOperator, parameterValues, filterType, locale);
			}
			else if (filterType == Datatype.BOOLEAN)	{
				whereClause = appendBooleanFilterToWhereClause(whereClause, filterColumnName, filterValue, concatOperator, parameterValues);
			}
			else if (filterType == Datatype.STRING)	{
				whereClause = appendStringFilterToWhereClause(whereClause, searchExact, filterColumnName, filterValue, concatOperator, parameterValues);
			}
			else
				throw new RuntimeException("Unknown datatype, not yet implemented: "+filterType);
			
		}	// end if constraint != null && !"".equals(constraint)
		return whereClause;
	}
	
	public String appendBooleanFilterToWhereClause(String whereClause, String columnName, String filterValue, ConcatOp concatOperator, List<Object> values) {
		Boolean boolFilter = Boolean.valueOf(filterValue);
		return appendBooleanFilterToWhereClause(whereClause, columnName, boolFilter, concatOperator, values);
	}
	
	public String appendBooleanFilterToWhereClause(String whereClause, String columnName, Boolean filterValue, ConcatOp concatOperator, List<Object> values) {
		values.add( filterValue );
		return " " + whereClause + " " + concatOperator.toString() + " " + columnName + " = ? ";
	}

	/** Calls appendStringFilterToWhereClause() with startsWith == false. */
	public String appendStringFilterToWhereClause(String whereClause, boolean searchExact, String columnName, String filterValue, ConcatOp concatOperator, List<Object> values) {
		return appendStringFilterToWhereClause(whereClause, searchExact, false, columnName, filterValue, concatOperator, values);
	}
	
	/**
	 * Adds one condition to a given WHERE clause, adds one parameter to given parameter list.
	 * Mind that appending a condition using ConcatOp.OR could change the semantic of the WHERE clause dramatically!
	 * @param whereClause the WHERE clause to add a condition to.
	 * @param searchExact false for 'LIKE', true for '=' operator.
	 * @param startsWith matters only if searchExact is false, false for '%input%', true for 'input%'.
	 * @param columnName the HQL column name the condition refers to.
	 * @param filterValue must not be null, the value to add to given parameterValues, originally or customized (see searchExact).
	 * @param concatOperator the logical association operator to use for the condition.
	 * @param parameterValues the parameter list to add the condition value to.
	 * @return the WHERE clause with given condition appended, which was NOT enclosed in parentheses.
	 */
	public String appendStringFilterToWhereClause(String whereClause, boolean searchExact, boolean startsWith, String columnName, String filterValue, ConcatOp concatOperator, List<Object> parameterValues) {
		// TODO: this is not null-safe for filterValue!
		// TODO: appending a condition using ConcatOp.OR could change the semantic of the WHERE clause dramatically!
		
		if (searchExact) {
			whereClause = " " + whereClause + " " + (concatOperator != null ? concatOperator : "") + " " + columnName;
			whereClause += "=? ";
			parameterValues.add(filterValue);
		}
		else 	{
			// TODO: assert that filterValue is either String or null, else you are converting a Date to String!
			
			whereClause = " " + whereClause + " " + (concatOperator != null ? concatOperator : "");
			
			// add lower() function to ensure we are comparing only lowercase characters
			if (columnName.trim().contains("lower(")) {
				whereClause += " " + columnName;
			} else {
				whereClause += " lower(" + columnName +")";
			}
			whereClause += " "+LIKE_PREFIX+"? ";
			
			String argFilter = filterValue.replaceFirst(LIKE_PREFIX, "");
			
			if (argFilter.contains("%")) {	// caller already has put wildcards into
				parameterValues.add(argFilter.toLowerCase());
			}
			else {	// for searchExact == false, add wildcards at start and end
				argFilter = argFilter.toLowerCase();
				argFilter = argFilter+"%";
				if (startsWith == false)
					argFilter = "%"+argFilter;
				parameterValues.add(argFilter);
			}
		}
		return whereClause;
	}

	public String appendNumberFilterToWhereClause(String whereClause, String columnName, String filterValue, ConcatOp concatOperator, List<Object> values) {
		return appendTypedFilterToWhereClause(whereClause, columnName, filterValue, concatOperator, values);
	}
	
	public String appendNumberFilterToWhereClause(String whereClause, String columnName, String constraint, ConcatOp concatOperator, List<Object> values, Datatype columnType, Locale locale) {
		try {
			// transform back if received via URL
			constraint = reconvertOperators(constraint);
			
			// recover operator
			String operator = getOperatorFromFilterText(constraint);
			if ("".equals(operator))
				operator = "=";

			// eliminate all operator signs
			constraint = removeLeadingNonDigits(constraint);
			
			ParsePosition position = new ParsePosition(0);
			Number searchNumber1 = parseToNumber(constraint, locale, position);
			
			// there could be a second separate number for ranges: lookup for the second number
			Number searchNumber2 = null;
			if (position.getIndex() < constraint.length()) {
				constraint = constraint.substring(position.getIndex());
				constraint = removeLeadingNonDigits(constraint);
				if (constraint != null) {	// we do not know what was after the first number
					position.setIndex(0);
					searchNumber2 = parseToNumber(constraint, locale, position);
				}
			}
			whereClause = " " + whereClause + " " + concatOperator.toString() + " " + columnName;
			if (searchNumber2 != null) {
				if (searchNumber2.doubleValue() < searchNumber1.doubleValue()) {
					// swap the numbers because SQL requires the smaller one to be on first position
					Number temp = searchNumber1;
					searchNumber1 = searchNumber2;
					searchNumber2 = temp;
				}
				values.add(convertNumber(searchNumber1, columnType));
				values.add(convertNumber(searchNumber2, columnType));
				whereClause += " between ? and ? ";
			}
			else {
				values.add(convertNumber(searchNumber1, columnType));
				whereClause += " " + operator + " ? ";
			}
		}
		catch (Exception e) {
			throw new RuntimeException("something went wrong while processing number into WHERE clause", e);
		}

		return whereClause;
	}
	
	public String appendTypedFilterToWhereClause(String whereClause, String columnName, Object filterValue, ConcatOp concatOperator, List<Object> values ) {
		values.add( filterValue );
		return " " + whereClause + " " + concatOperator.toString() + " " + columnName + "=? ";
	}
	
	public String appendNumberFilterToWhereClause(String whereClause, String columnName, Number filterValue, ConcatOp concatOperator, List<Object> values ) {
		return appendTypedFilterToWhereClause(whereClause, columnName, filterValue, concatOperator, values);
	}
	
	public String appendTimeFilterToWhereClause(String whereClause, String columnName, Date filterValue, ConcatOp concatOperator, List<Object> values) {
		Date passedTime = DateTools.timeOnly(filterValue);
		return appendTypedFilterToWhereClause(whereClause, columnName, passedTime, concatOperator, values);
	}
	
	public String appendDateFilterToWhereClause(String whereClause, String columnName, Date filterValue, ConcatOp concatOperator, List<Object> values) {
		Date passedTime = DateTools.dateOnly(filterValue);
		return appendTypedFilterToWhereClause(whereClause, columnName, passedTime, concatOperator, values);
	}
	
	public String appendTimeFilterToWhereClause(String whereClause, String columnName, String constraint, ConcatOp concatOperator, List<Object> values) {
		try {
			// transform back if received via URL
			constraint = reconvertOperators(constraint);
			// recover operator
			String operator = getOperatorFromFilterText(constraint);
			
			// eliminate all operator signs
			Matcher timeMatcher = timePattern.matcher(constraint);
			timeMatcher.matches();
			int times = timeMatcher.groupCount();
			if (times == 0)
				throw new RuntimeException("No time to search for");
			String searchTime1 = timeMatcher.group(1);
			// lookup for the second time 
			String searchTime2 = timeMatcher.group(2);
			// = or no operator must be treated as 'between' because of timestamps
			if (searchTime2 == null &&
					(operator == null || "".equals(operator) || "=".equals(operator))) {
				// shift time to the next day 00:00:00
				searchTime2 = searchTime1;
			}
			whereClause = " " + whereClause + " " + concatOperator.toString() + " " + columnName;
			if (searchTime2 != null) {
				String[] t1 = searchTime1.split(":");
				String[] t2 = searchTime2.split(":");
				if (t1[0].compareTo(t2[0]) < 0 // hours
						|| t1[0].compareTo(t2[0]) == 0 && t1[1].compareTo(t2[1]) < 0) {
					String temp = searchTime1;
					searchTime1 = searchTime2;
					searchTime2 = temp;
				}
				
				values.add(searchTime1);
				values.add(searchTime2 + ":59");
				whereClause += " between ? and ? ";
			} else {
				
				whereClause += " " + operator + " ? ";
				if ("<=".equals(operator) || ">".equals(operator)) {
					values.add(searchTime1 + ":59");
				} else {
					values.add(searchTime1);
				}
			}
		}
		catch (Exception e) {
			throw new RuntimeException("something went wrong while processing date into WHERE clause", e);
		}
		return whereClause;
	}
	
	public String appendDateFilterToWhereClause(String whereClause, String columnName, String constraint, ConcatOp concatOperator, List<Object> values, Locale locale) {
		try {
			// transform back if received via URL
			constraint = reconvertOperators(constraint);
			// recover operator
			String operator = getOperatorFromFilterText(constraint);

			// eliminate all operator signs
			constraint = removeLeadingNonDigits(constraint);
			ParsePosition position = new ParsePosition(0);
			Date searchDate1 = parseToDate(constraint, locale, position);
			// lookup for the second date 
			Date searchDate2 = null;
			if (position.getIndex() < constraint.length()) {
				constraint = constraint.substring(position.getIndex());
				constraint = removeLeadingNonDigits(constraint);
				if (constraint != null) {
					position.setIndex(0);
					searchDate2 = parseToDate(constraint, locale, position);
				}
			}
			
			// = or no operator must be treated as 'between' because of timestamps
			if (searchDate2 == null &&
					(operator == null || "".equals(operator) || "=".equals(operator))) {
				// shift time to the next day 00:00:00
				searchDate2 = searchDate1;
			}
			
			whereClause = " " + whereClause + " " + concatOperator.toString() + " " + columnName;
			if (searchDate2 != null) {
				if (searchDate2.before(searchDate1)) {
					Date temp = searchDate1;
					searchDate1 = searchDate2;
					searchDate2 = temp;
				}
				values.add(DateTools.dateOnly(searchDate1));
				values.add(DateTools.lastMomentOfDay(searchDate2));
				 whereClause += " between ? and ? ";
			}
			else {
				whereClause += " " + operator + " ? ";
				if ("<=".equals(operator) || ">".equals(operator)) {
					values.add(DateTools.lastMomentOfDay(searchDate1));
				} else {
					values.add(DateTools.dateOnly(searchDate1));
				}
			}
		}
		catch (Exception e) {
			throw new RuntimeException("something went wrong while processing date into WHERE clause", e);
		}
		return whereClause;
	}

	private Object convertNumber(Number number, Datatype columnType) {
		if (columnType == Datatype.INTEGER)
			return number.intValue();
		if (columnType == Datatype.LONG)
			return number.longValue();
		if (columnType == Datatype.FLOAT)
			return number.floatValue();
		if (columnType == Datatype.DOUBLE)
			return number.doubleValue();
		return number;
	}


	private boolean isDate(String value, Locale locale) {
		ParsePosition position = new ParsePosition(0);
		try {
			// eliminate possible date comparation operators
			String potentialDate = removeLeadingNonDigits(value);
			if (parseToDate(potentialDate, locale, position) != null)
				return true;
			return false;
		}
		catch (Exception e) {
			return false;
		}
	}

	private boolean isTime(String value) {
		try {
			// eliminate possible date comparation operators
			String potentialTime = removeOperator(value);
			Matcher matcher = timePattern.matcher(potentialTime);
			return matcher.matches();
		}
		catch (Exception e) {
			return false;
		}
	}

	private boolean isNumber(String value, Locale locale) {
		ParsePosition position = new ParsePosition(0);
		try {
			if (value == null || value.length() >= 32) // UIDs and to long numbers
				return false;
			// eliminate possible comparison operators
			String potentialNumber = removeOperator(value);
			if (parseToNumber(potentialNumber, locale, position) != null)
				return true;
			return false;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	private String removeOperator(String constraint) {
		if (constraint == null || constraint.length() == 0)
			return constraint;
		String operand = constraint.replaceAll("<", "").replaceAll("=", "").replaceAll(">", "");
		for (int i = 0; i < constraint.length(); i++)
			if (!Character.isSpaceChar(operand.charAt(i)))
				return operand.substring(i);
		return null;
	}

	private String removeLeadingNonDigits(String constraint) {
		if (constraint == null || constraint.length() == 0)
			return constraint;
		for (int i = 0; i < constraint.length(); i++)
			if (Character.isDigit(constraint.charAt(i)))
				return constraint.substring(i);
		return null;
	}
	
	
	
	private Date parseToDate(String value, Locale locale, ParsePosition position) {
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
		return df.parse(value, position);
	}

	private Number parseToNumber(String value, Locale locale, ParsePosition position) {
		NumberFormat nf = NumberFormat.getInstance(locale);
		return nf.parse(value, position);
	}
	
	private String reconvertOperators(String constraint) {
		return constraint
				.replaceAll("_le_", "<=")
				.replaceAll("_ge_", ">=")
				.replaceAll("_lt_", "<")
				.replaceAll("_gt_", ">")
				.replaceAll("_eq_", "=")
				.trim();
	}
	
	private String getOperatorFromFilterText(String filterText) {
		String operator = "";

		if (filterText == null)
			return operator;

		if (filterText.startsWith(">=")) {
			operator = ">=";
		} else if (filterText.startsWith(">")) {
			operator = ">";
		} else if (filterText.startsWith("=")) {
			operator = "=";
		} else if (filterText.startsWith("<=")) {
			operator = "<=";
		} else if (filterText.startsWith("<")) {
			operator = "<";
		}

		return operator;
	}

}