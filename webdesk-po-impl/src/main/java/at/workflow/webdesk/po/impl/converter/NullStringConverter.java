package at.workflow.webdesk.po.impl.converter;

import at.workflow.webdesk.po.NumbersDatesAndTimes;

/**
 * Convertor that converts null string to either:
 *  - empty string when destinationType is string, unknown or not number and not date
 *  - "0" string when destinationType is number
 *  - retains null when destinationType is date 
 * 
 * @author sdzuban 11.07.2012
 */
public class NullStringConverter {
	
	private Class<?> destinationType;
	private boolean isNumber;
	private boolean isDateTime;
	private boolean isString;
	
	public NullStringConverter(Class<?> destinationType) {
		super();
		this.destinationType = destinationType;
		isNumber = NumbersDatesAndTimes.isAnyNumber(destinationType);
		isDateTime = NumbersDatesAndTimes.isAnyDateTime(destinationType);
		isString = destinationType == java.lang.String.class;
	}


	public String convert(String input) {
		if (input == null) {
			if (destinationType == null || isString)
				return "";
			else if (isNumber)
				return "0";
			else if (isDateTime)
				return null;
			return ""; // eg. Boolean
		}
		return input;		
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return getClass().getSimpleName() + " for " + (destinationType == null ? "String" : destinationType.getSimpleName());
	}
}
