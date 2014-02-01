package at.workflow.webdesk.po.impl.converter;

import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;

import at.workflow.webdesk.po.converter.Converter;


public class DateToStringConverter implements Converter {
	
	/** {@inheritDoc} */
	@Override
	public Object convert(Object o, String pattern, String locale) {
		if (o == null)
			return "";
		SimpleDateFormat sdf = new SimpleDateFormat();
		if (!StringUtils.isBlank(pattern))
			sdf.applyPattern(pattern);
		return sdf.format(o);
	}

	/** {@inheritDoc} */
	@Override
	public String getDefaultPattern() {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return "converter_DateToStringConverter";
	}

	/** {@inheritDoc} */
	@Override
	public boolean isPatternRequired() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isLocaleRequired() {
		return false;
	}


}
