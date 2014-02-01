package at.workflow.webdesk.po.impl.converter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import at.workflow.tools.LocaleAndSymbolUtils;
import at.workflow.webdesk.po.converter.Converter;

public class NumberToStringConverter implements Converter {

	private static class SingletonHolder
	{
		private final static Converter INSTANCE = new NumberToStringConverter();
	}
	// fri_2011-11-29 for that kind of singleton see http://c2.com/cgi/wiki?JavaSingleton
	// It is to defer singleton allocation until getInstance() is called ...

	public static Converter getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	/** {@inheritDoc} */
	@Override
	public Object convert(Object o, String pattern, String locale) {
		
		DecimalFormat df = new DecimalFormat(pattern);
		if (StringUtils.isNotBlank(locale)) {
			Locale loc = LocaleAndSymbolUtils.getLocaleFromString(locale);
	    	DecimalFormatSymbols dfs = new DecimalFormatSymbols(loc);
	    	df.setDecimalFormatSymbols(dfs);
		}
		
		if (o==null)
			o=new Long(0);
		return df.format(o);
	}

	/** {@inheritDoc} */
	@Override
	public String getDefaultPattern() {
		return "0.00";
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return "converter_NumberToStringConverter";
	}

	/** {@inheritDoc} */
	@Override
	public boolean isPatternRequired() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isLocaleRequired() {
		return true;
	}


}
