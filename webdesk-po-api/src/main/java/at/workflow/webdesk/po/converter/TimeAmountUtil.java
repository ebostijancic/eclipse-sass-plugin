package at.workflow.webdesk.po.converter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * UI utilities for localized time amounts in normal minutes.
 * This by default implements a form where minutes are from 0-59 behind hours,
 * separated by an national decimal number separator.
 * So a time amount would look like <i>2,59</i> in German and <i>2.59</i> in English.
 * <p/>
 * Industry time is not (yet) covered here.
 * 
 * @author fritzberger 29.11.2011
 */
public class TimeAmountUtil {

	/**
	 * Display-hours are "hh,mm", the part behind decimal point are normal minutes, the part before decimal point is hours.
	 * @param minutes the minutes to turn to hours as display number.
	 * @return the display number, for 100 this returns 1.40 where 1 is one hour and .40 is 40 minutes.
	 */
	public static float toDisplayHours(int minutes)	{
		final int hours = minutes / 60;
		minutes = minutes % 60;
		return hours + ((float) minutes / (float) 100);	// shift right minutes two positions, to be behind decimal point
	}
	
	/**
	 * @param displayHours the display-hours to turn to (normal) minutes.
	 * @return the minutes from given display-hours, for 1.40 this returns 100.
	 */
	public static int toMinutes(float displayHours)	{
		final int hours = (int) displayHours;	// cut off the decimal part
		final int minutes = Math.round((displayHours - hours) * 100);
		return hours * 60 + minutes;
	}
	
	
	
	/** @return the decimal separator for given language/region. */
	public static char getDecimalSeparator(Locale locale)	{
		return new DecimalFormatSymbols(locale != null ? locale : Locale.getDefault()).getDecimalSeparator();
	}
	
	/**
	 * @param formatMask the format mask to use for DecimalFormat constructor, see JavaDoc of that class, can be null for ",##0.00".
	 * @param decimalSeparator the character that represents a configured arbitrary decimal point, can be null for national decimal separator.
	 * @param locale the internationalization for the returned formatter/parser, can be null for Locale.getDefault().
	 * @return a new NumberFormat instance with given Locale and decimal point.
	 */
	public static NumberFormat newTimeAmountFormatInstance(String formatMask, String decimalSeparator, Locale locale) {
		// make defaults when undefined parameters
		if (formatMask == null || formatMask.length() <= 0)	{
			formatMask = ",##0.00";
		}
		
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale != null ? locale : Locale.getDefault());
		if (decimalSeparator != null && decimalSeparator.length() > 0)	{
			assert decimalSeparator.length() == 1 : "Invalid decimal separator, longer than 1: "+decimalSeparator;
			symbols.setDecimalSeparator(decimalSeparator.charAt(0));
		}
		// else the Locale's decimal separator is default
		
		// create a NumberFormat instance
		return new DecimalFormat(formatMask, symbols);
	}

	/**
	 * Calls <code>parse()</code> with null formatMask and null decimalSeparator, so defaults will be used.
	 * @return the time amount in "normal" minutes.
	 */
	public static int parse(Locale locale, String timeAmountInHours) throws ParseException	{
		return parse(null, null, locale, timeAmountInHours);
	}
	
	/**
	 * Calculates the "normal time" (not industry time) of given user input.
	 * Input is e.g. "4,50" which means 4 hours and 50 minutes.
	 * The decimal point might vary in different countries.
	 * @param formatMask the format mask to use for DecimalFormat constructor, see JavaDoc of that class, can be null for ",##0.00".
	 * @param decimalSeparator the character that represents a configured arbitrary decimal point, can be null for national decimal separator.
	 * @param locale for the national number formatting convention.
	 * @param timeAmountInHours the user input, something like
	 * 		"0,01" (minimum digits to the right of decimal point is 0.00)
	 * 		or "1,59" (maximum to the right of decimal point is 59).
	 * 		Minimum amount is "0.01", negative amounts are not accepted.
	 * @return the time amount in "normal" minutes.
	 * @throws ParseException when input is not a number, is negative, or there are more than 3 digits after decimal point.
	 */
	public static int parse(String formatMask, String decimalSeparator, Locale locale, String timeAmountInHours) throws ParseException	{
		timeAmountInHours = preprocessNumericInput(timeAmountInHours, decimalSeparator, locale);

		NumberFormat parser = newTimeAmountFormatInstance(formatMask, decimalSeparator, locale);
		Number n = parser.parse(timeAmountInHours);
		if (n.doubleValue() <= 0)
			throw new ParseException("Keine oder negative Zeitangabe nicht erlaubt: "+timeAmountInHours, 0);
		
		String s = n.toString();	// turns "1,2" to "1.2" removing the localized decimal point
		int decimalPointPosition = s.indexOf(".");	// "." is the hardcoded Java separator used in toString()
		String beforeDecimalPoint = (decimalPointPosition >= 0) ? s.substring(0, decimalPointPosition) : s;
		String afterDecimalPoint = (decimalPointPosition >= 0 && decimalPointPosition < s.length() - 1) ? s.substring(decimalPointPosition + 1) : null;
		
		int minutes = 0;
		if (afterDecimalPoint != null)	{
			assert afterDecimalPoint.length() > 0;
			
			if (afterDecimalPoint.length() > 2)	// case "12,345"
				throw new ParseException("Maximal zwei Ziffern sind nach dem Dezimalzeichen erlaubt: "+afterDecimalPoint, decimalPointPosition);
			
			minutes = Integer.valueOf(afterDecimalPoint).intValue();
			
			if (afterDecimalPoint.length() == 1)	// case "1,2"
				minutes *= 10;
			
			if (minutes > 59)
				throw new ParseException("Mehr als 59 Minuten sind nach dem Dezimalzeichen nicht erlaubt: "+minutes, decimalPointPosition);
		}
		
		final int hours = Integer.valueOf(beforeDecimalPoint);
		return hours * 60 + minutes;
	}
	
	/**
	 * Calls <code>format()</code> with null formatMask and null decimalSeparator, so defaults will be used.
	 * @return the time amount in hours, as user input text.
	 */
	public static String format(Locale locale, int amountInMinutes)	{
		return format(null, null, locale, amountInMinutes);
	}
	
	/**
	 * Formats the output for the given "normal time" (not industry time) in minutes. 
	 * (also negative minutes are processed!)
	 * For instance an amount of 80 minutes would be returned as "1,2".
	 * @param formatMask the format mask to use for DecimalFormat constructor, see JavaDoc of that class, can be null for ",##0.00".
	 * @param decimalSeparator the character that represents a configured arbitrary decimal point, can be null for national decimal separator.
	 * @param locale for the national number formatting convention.
	 * @param amountInMinutes the time amount in "normal" minutes (not industry minutes) to render.
	 * @return the time amount in hours, as user input text.
	 */
	public static String format(String formatMask, String decimalSeparator, Locale locale, int amountInMinutes)	{
		final int hours = amountInMinutes / 60;
		final int minutes = Math.abs(amountInMinutes % 60);	// remove any minus sign to NOT get "0,-15"
		
		final String amountInHoursString = hours+"."+(minutes < 10 ? "0"+minutes : minutes);
		final double amountInHours = Double.valueOf(amountInHoursString);
		
		final NumberFormat formatter = newTimeAmountFormatInstance(formatMask, decimalSeparator, locale);
		// to NOT lose the minus sign when hours were zero, make an explicit sign
		final String prefix = (hours == 0 && amountInMinutes < 0 ? "-" : "");
		
		return prefix+formatter.format(amountInHours);
	}

	
	
	/**
	 * Checks the input for invalid characters. Minus signs and spaces are not accepted.
	 * As DecimalFormat simply stops when finding an invalid character, not throwing
	 * an exception, we prefer to check the input manually.
	 * DecimalFormat would return "12:34" as 12.
	 * Further DecimalFormat converts 2.0 into 20 (fatal error!),
	 * thus we substitute any "." by the localized decimal point.
	 * Grouping separator (for number above 1000) is not supported here because
	 * time amount is given in hours and would not exceed 999.
	 * <p/>
	 * fri_2011-03-02: this is a fix for
	 * <pre>
	 * http://intranet/intern/ifwd_mgm.nsf/0/532CD9190152176FC12578420056A2B0?OpenDocument
	 * notes://Miraculix/intern/ifwd_mgm.nsf/0/532CD9190152176FC12578420056A2B0?EditDocument
	 * </pre>
	 * @param amountInput the input from textfield, denoting a time amount in "normal" hours, can not be null.
	 * @param decimalSeparator an arbitrary 1-char decimal separator if there is a configured one, can be null.
	 * @param locale the national Locale for retrieving the decimal separator, can be null.
	 */
	public static String preprocessNumericInput(String amountInput, String decimalSeparator, Locale locale) throws ParseException {
		assert amountInput != null;
		
		char decimalSeparatorChar;
		if (decimalSeparator == null || decimalSeparator.length() <= 0)	{
			decimalSeparatorChar = getDecimalSeparator(locale);
		}
		else	{
			decimalSeparatorChar = decimalSeparator.charAt(0);
		}
		
		// check that no other character than one of the allowed ones occurs in input, not even a space
		for (int i = 0; i < amountInput.length(); i++)	{
			final char c = amountInput.charAt(i);
			if (Character.isDigit(c) == false && c != decimalSeparatorChar && c != '.')
				throw new ParseException("Ungueltiges Zeichen '"+c+"' in \""+amountInput+"\", erlaubt sind Ziffern, '.' und '"+decimalSeparatorChar+"'", i);
		}
		
		return (decimalSeparatorChar != '.') ? amountInput.replace('.', decimalSeparatorChar) : amountInput;
	}

}
