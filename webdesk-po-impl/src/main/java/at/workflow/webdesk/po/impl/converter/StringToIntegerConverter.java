package at.workflow.webdesk.po.impl.converter;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import at.workflow.tools.LocaleAndSymbolUtils;
import at.workflow.webdesk.po.converter.Converter;
import at.workflow.webdesk.po.converter.NumberParserAndFormatter;


public class StringToIntegerConverter implements Converter {

	
	/** {@inheritDoc} */
	@Override
	public Object convert(Object o, String pattern, String locale) {

		if (o == null || "".equals(o))
			return 0;
		
		if (StringUtils.isNotBlank(locale)) {
			Locale loc = LocaleAndSymbolUtils.getLocaleFromString(locale);
			NumberParserAndFormatter parser = new NumberParserAndFormatter(loc);
			return parser.parseInteger(o.toString());
		}
		// this is for backward compatibility so that old configurations work - DO NOT CHANGE
		return new Integer(o.toString());
	}

	/** {@inheritDoc} */
	@Override
	public String getDefaultPattern() {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return "converter_StringToIntegerConverter";
	}

	/** {@inheritDoc} */
	@Override
	public boolean isPatternRequired() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isLocaleRequired() {
		return true;
	}

}
