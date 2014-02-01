package at.workflow.webdesk.po;

import java.util.ArrayList;
import java.util.List;

/**
 * This is utility class providing methods for determination whether
 * class name or class is 
 *  - date or time
 *  - integer/long/BigInteger
 *  - float/double/BigDecimal
 *  - any number
 *  
 * @author sdzuban 11.07.2012
 */
public class NumbersDatesAndTimes {
	
	private static final String DATE = java.util.Date.class.getName();
	private static final String SQL_DATE = java.sql.Date.class.getName();
	private static final String TIME = java.sql.Time.class.getName();
	private static final String TIMESTAMP = java.sql.Timestamp.class.getName();
	private static final String INT = int.class.getName();
	private static final String INTEGER = java.lang.Integer.class.getName();
	private static final String LNG = long.class.getName();
	private static final String LONG = java.lang.Long.class.getName();
	private static final String FLT = float.class.getName();
	private static final String FLOAT = java.lang.Float.class.getName();
	private static final String DBL = double.class.getName();
	private static final String DOUBLE = java.lang.Double.class.getName();
	private static final String BIG_DECIMAL = java.math.BigDecimal.class.getName();
	private static final String BIG_INTEGER = java.math.BigInteger.class.getName();

	private static final List<String> DATETIMES = new ArrayList<String>();
	private static final List<String> INTEGERS = new ArrayList<String>();
	private static final List<String> FLOATS = new ArrayList<String>();
	private static final List<String> NUMBERS = new ArrayList<String>();
	
	static {
		DATETIMES.add(DATE);
		DATETIMES.add(SQL_DATE);
		DATETIMES.add(TIME);
		DATETIMES.add(TIMESTAMP);
		
		INTEGERS.add(INT);
		INTEGERS.add(INTEGER);
		INTEGERS.add(LNG);
		INTEGERS.add(LONG);
		INTEGERS.add(BIG_INTEGER);
		
		FLOATS.add(FLT);
		FLOATS.add(FLOAT);
		FLOATS.add(DBL);
		FLOATS.add(DOUBLE);
		FLOATS.add(BIG_DECIMAL);
		
		NUMBERS.addAll(INTEGERS);
		NUMBERS.addAll(FLOATS);
	}
	
	/** @return is className any of the dates, time or timestamp */
	public static boolean isAnyDateTime(String className) {
		return DATETIMES.contains(className);
	}
	
	/** @return is clss any of the dates, time or timestamp */
	public static boolean isAnyDateTime(Class<?> clss) {
		if (clss != null)
			return DATETIMES.contains(clss.getName());
		return false;
	}

	/** @return is className any of int, Integer, long, Long, BigInteger */
	public static boolean isIntegerOrLong(String className) {
		return INTEGERS.contains(className);
	}
	
	/** @return is clss any of int, Integer, long, Long, BigInteger */
	public static boolean isIntegerOrLong(Class<?> clss) {
		if (clss != null)
			return INTEGERS.contains(clss.getName());
		return false;
	}
	
	/** @return is className any of float, Float, double, Double, BigDecimal */
	public static boolean isFloatOrDouble(String className) {
		return FLOATS.contains(className);
	}
	
	/** @return is clss any of float, Float, double, Double, BigDecimal */
	public static boolean isFloatOrDouble(Class<?> clss) {
		if (clss != null)
			return FLOATS.contains(clss.getName());
		return false;
	}
	
	/** @return is className any number */
	public static boolean isAnyNumber(String className) {
		return NUMBERS.contains(className);
	}
	
	/** @return is clss any number */
	public static boolean isAnyNumber(Class<?> clss) {
		if (clss != null)
			return NUMBERS.contains(clss.getName());
		return false;
	}
}
