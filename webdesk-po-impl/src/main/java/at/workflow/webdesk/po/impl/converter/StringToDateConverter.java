package at.workflow.webdesk.po.impl.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;


import at.workflow.webdesk.po.converter.Converter;

public class StringToDateConverter implements Converter {


	/** {@inheritDoc} */
	@Override
	public Object convert(Object o, String pattern, String locale) {
		
		if (o == null)
			return null;
		SimpleDateFormat sdf = new SimpleDateFormat();
		if (!StringUtils.isBlank(pattern))
			sdf.applyPattern(pattern);
		try {
			return sdf.parse(o.toString());
		} catch (ParseException e) {
			return null; //"String " + o +" was not parseable";
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getDefaultPattern() {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return "converter_StringToDateConverter";

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
