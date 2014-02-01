package at.workflow.tools;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;

/**
 * This utility class provides usefull static methods 
 * for work with loacle and decimal symbols.
 * 
 * @author sdzuban 27.06.2012
 */
public class LocaleAndSymbolUtils {

	public static Locale getLocaleFromString(String sLocale) {
		
		Locale locale = Locale.getDefault();
		if (sLocale != null && !StringUtils.isBlank(sLocale)) {
			locale = LocaleUtils.toLocale(sLocale);
		}
		return locale;
	}
	
	public static String getCurrencySymbol(Locale locale) {
		DecimalFormatSymbols decimalSymbols = new DecimalFormatSymbols(locale);
		return decimalSymbols.getCurrencySymbol();
	}
	
	public static String getPercentSymbol(Locale locale) {
		DecimalFormatSymbols decimalSymbols = new DecimalFormatSymbols(locale);
		char percentSign = decimalSymbols.getPercent();
		return "" + percentSign;
	}
}
