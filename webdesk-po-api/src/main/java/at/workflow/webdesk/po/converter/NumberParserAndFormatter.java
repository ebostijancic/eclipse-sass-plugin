package at.workflow.webdesk.po.converter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

/**
 * This class offers methods for parsing and formatting of 
 *  - Integer
 *  - Double
 *  - Percent
 *  - Currency
 *  - BigDecimal
 *  
 *  Parsing of numbers is done with ParsePosition check to be sure the whole string was parsed.
 *  If the whole string cannot be parsed correctly runtime exception is thrown.
 *  
 *  Special attention needs to be payed to the currency parsing - the currency must be clearly specified by locale.
 *  E.g. Locale.GERMAN represents the language, but no currency - it could be Swiss Franc or €.
 *  Locale.GERMAY is the right one for €.
 *  
 *  MOTIVATION
 *  The problem with NumberFormat parsing is not strict parsing where 12f34 results in 12
 *  without exception being thrown.
 * 
 * @author sdzuban 20.06.2012
 */
public class NumberParserAndFormatter {

	private NumberFormat nfInteger;
	private NumberFormat nfDouble;
	private NumberFormat nfCurrency;
	private NumberFormat nfPercent;
	private NumberFormat nfBigDecimal;
	
	/**
	 * Be aware to use precise locale, from which the currency can be derived.
	 * Otherwise currency parsing and formatting does not work.
	 * E.g. GERMAN locale is for Germany and Switzerland vs. GERMANY is for Germany, €. 
	 * @param locale
	 */
	public NumberParserAndFormatter (Locale locale) {
		
		nfInteger = NumberFormat.getIntegerInstance(locale);
		nfDouble = NumberFormat.getInstance(locale);
		nfCurrency = NumberFormat.getCurrencyInstance(locale);
		nfPercent = NumberFormat.getPercentInstance(locale);
		nfBigDecimal = NumberFormat.getInstance(locale);
		((DecimalFormat) nfBigDecimal).setParseBigDecimal(true);
	}
	
	public String formatInteger(int i) {
		return nfInteger.format(i);
	}
	
	public String formatDouble(double d) {
		return nfDouble.format(d);
	}
	
	public String formatCurrency(double d) {
		return nfCurrency.format(d);
	}
	
	public String formatPercent(double d) {
		return nfPercent.format(d);
	}
	
	public String formatBigDecimal(BigDecimal bd) {
		return nfBigDecimal.format(bd);
	}
	
	
	
	/**
	 * Formats double with formatting pattern
	 * @param dbl amount to be formatted
	 * @param formattingPattern detailed formatting pattern
	 */
	public String formatDouble(double dbl, String formattingPattern) { 
		
		String originalPattern = null;
		if (!StringUtils.isBlank(formattingPattern)) {
			originalPattern = ((DecimalFormat) nfDouble).toPattern();
			((DecimalFormat) nfDouble).applyPattern(formattingPattern);
		}
		
		String result = nfDouble.format(dbl);
		
		if (!StringUtils.isBlank(formattingPattern)) {
			((DecimalFormat) nfDouble).applyPattern(originalPattern);
		}
		return result;
	}
	
	/**
	 * Formats percent with formatting pattern
	 * @param percent amount to be formatted
	 * @param formattingPattern detailed formatting pattern
	 */
	public String formatPercent(double percent, String formattingPattern) { 
		
		String originalPattern = null;
		if (!StringUtils.isBlank(formattingPattern)) {
			originalPattern = ((DecimalFormat) nfPercent).toPattern();
			((DecimalFormat) nfPercent).applyPattern(formattingPattern);
		}
		
		String result = nfPercent.format(percent);
		
		if (!StringUtils.isBlank(formattingPattern)) {
			((DecimalFormat) nfPercent).applyPattern(originalPattern);
		}
		return result;
	}
	
	/**
	 * Formats currency with formatting pattern
	 * @param currency to be formatted
	 * @param formattingPattern detailed formatting pattern
	 */
	public String formatCurrency(double currency, String formattingPattern) { 
		
		String originalPattern = null;
		if (!StringUtils.isBlank(formattingPattern)) {
			originalPattern = ((DecimalFormat) nfCurrency).toPattern();
			((DecimalFormat) nfCurrency).applyPattern(formattingPattern);
		}
		
		String result = nfCurrency.format(currency);
		
		if (!StringUtils.isBlank(formattingPattern)) {
			((DecimalFormat) nfCurrency).applyPattern(originalPattern);
		}
		return result;
	}
	
	/**
	 * Formats double with formatting pattern
	 * @param bigDecimal amount to be formatted
	 * @param formattingPattern detailed formatting pattern
	 */
	public String formatBigDecimal(BigDecimal bigDecimal, String formattingPattern) { 
		
		String originalPattern = null;
		if (!StringUtils.isBlank(formattingPattern)) {
			originalPattern = ((DecimalFormat) nfBigDecimal).toPattern();
			((DecimalFormat) nfBigDecimal).applyPattern(formattingPattern);
		}
		
		String result = nfBigDecimal.format(bigDecimal);
		
		if (!StringUtils.isBlank(formattingPattern)) {
			((DecimalFormat) nfBigDecimal).applyPattern(originalPattern);
		}
		return result;
	}
	
	
	
	
	
	/**
	 * Parses whole string as int. If successful, the integer is returned.
	 * Otherwise RuntimeException is thrown.
	 * WARNING! if '.' is grouping separator 12.3 is valid integer 123
	 * @param sInteger string to be parsed as whole
	 */
	public int parseInteger(String sInteger) { 

		ParsePosition pp = new ParsePosition(0);
		Number result = nfInteger.parse(sInteger, pp );

		if( sInteger.length() != pp.getIndex() || result == null )
			throw new RuntimeException("Not correct Integer number");
		return result.intValue();
	}

	/**
	 * Parses whole string as double. If successful, the double is returned.
	 * Otherwise RuntimeException is thrown.
	 * WARNING! if '.' is grouping separator 12.3 is valid double 123.
	 * @param sDouble string to be parsed as whole
	 */
	public double parseDouble(String sDouble) { 
		
		ParsePosition pp = new ParsePosition(0);
		Number result = nfDouble.parse(sDouble, pp );
		
		if( sDouble.length() != pp.getIndex() || result == null )
			throw new RuntimeException("Not correct Double number");
		return result.doubleValue();
	}
	
	/**
	 * Parses whole string as percent. If successful, the double is returned.
	 * Otherwise RuntimeException is thrown.
	 * WARNING! if '.' is grouping separator 12.3% is valid double 0.123
	 * @param sPercent string to be parsed as whole
	 */
	public double parsePercent(String sPercent) { 
		
		ParsePosition pp = new ParsePosition(0);
		Number result = nfPercent.parse(sPercent, pp );
		
		if( sPercent.length() != pp.getIndex() || result == null )
			throw new RuntimeException("Not correct Percent number");
		return result.doubleValue();
	}
	
	/**
	 * Parses whole string as currency. If successful, the double is returned.
	 * Otherwise RuntimeException is thrown.
	 * WARNING! if '.' is grouping separator 12.3 € is valid double 123 €.
	 * @param sCurrency string to be parsed as whole
	 */
	public double parseCurrency(String sCurrency) { 
		
		ParsePosition pp = new ParsePosition(0);
		Number result = nfCurrency.parse(sCurrency, pp );
		
		if( sCurrency.length() != pp.getIndex() || result == null )
			throw new RuntimeException("Not correct Currency number");
		return result.doubleValue();
	}

	/**
	 * Parses whole string as currency according to formatting pattern. 
	 * If successful, the double is returned.
	 * Otherwise RuntimeException is thrown.
	 * WARNING! if '.' is grouping separator 12.3 € is valid double 123 €.
	 * @param sCurrency string to be parsed as whole
	 * @param formattingPattern incl. currency symbol
	 */
	public double parseCurrency(String sCurrency, String formattingPattern) { 
		
		String originalPattern = null;
		if (!StringUtils.isBlank(formattingPattern)) {
			originalPattern = ((DecimalFormat) nfCurrency).toPattern();
			((DecimalFormat) nfCurrency).applyPattern(formattingPattern);
		}
		
		ParsePosition pp = new ParsePosition(0);
		Number result = nfCurrency.parse(sCurrency, pp );
		
		if (!StringUtils.isBlank(formattingPattern)) {
			((DecimalFormat) nfCurrency).applyPattern(originalPattern);
		}
		
		if( sCurrency.length() != pp.getIndex() || result == null )
			throw new RuntimeException("Not correct Currency number");
		return result.doubleValue();
	}
	
	/**
	 * Parses whole string as big decimal. If successful, the big decimal is returned.
	 * Otherwise RuntimeException is thrown.
	 * WARNING! if '.' is grouping separator 12.3 € is valid big deciaml 123 €.
	 * @param sBigDecimal string to be parsed as whole
	 */
	public BigDecimal parseBigDecimal(String sBigDecimal) { 
		
		ParsePosition pp = new ParsePosition(0);
		BigDecimal result = (BigDecimal) nfBigDecimal.parse(sBigDecimal, pp );
		
		if( sBigDecimal.length() != pp.getIndex() || result == null )
			throw new RuntimeException("Not correct BigDecimal number");
		return result;
	}
	 
}
