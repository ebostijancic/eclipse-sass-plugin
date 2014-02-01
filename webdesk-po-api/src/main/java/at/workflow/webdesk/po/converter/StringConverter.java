package at.workflow.webdesk.po.converter;

import java.math.BigDecimal;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import at.workflow.tools.LocaleAndSymbolUtils;

/**
 * General purpose converter converting between
 * - strings and
 * - boolean
 * - numbers (integer, double, BigDecimal) with given locale and default patterning.
 * 
 * All data properties and methods are computed during instantiation.
 * If the conversion is not possible exception is thrown.
 * 
 * During conversion found methods are just applied.
 * 
 * @author sdzuban 18.07.2012
 */
public class StringConverter implements AutomaticConverter {

	/**
	 * Class names used because it is not possible to obtain Class of primitives by Class.forName().
	 */
	private String inputClassName, outputClassName;
	private String locale;
	private NumberParserAndFormatter parserAndFormatter;
	private boolean isInputString = false, isOutputString = false;
	private boolean isInputBoolean = false, isOutputBoolean = false;
	private boolean isInputInteger = false, isOutputInteger = false;
	private boolean isInputDouble = false, isOutputDouble = false;
	private boolean isInputBigDecimal = false, isOutputBigDecimal = false;
	
	
	
	public StringConverter(Class<?> inputClass, Class<?> outputClass, String locale) {
		super();
		if (inputClass == null || outputClass == null)
			throw new RuntimeException("Null class.");
		this.inputClassName = inputClass.getName();
		this.outputClassName = outputClass.getName();
		this.locale = locale;
		checkConditions();
	}
	
	public StringConverter (String inputClassName, String outputClassName, String locale) {
		if (StringUtils.isBlank(inputClassName) || StringUtils.isBlank(outputClassName))
			throw new RuntimeException("Blank class name.");
		this.inputClassName = inputClassName;
		this.outputClassName = outputClassName;
		this.locale = locale; 
		checkConditions();
	}
	
	private void checkConditions() {
		
		isInputString = inputClassName == String.class.getName();
		isOutputString = outputClassName == String.class.getName();
		isInputBoolean = isBoolean(inputClassName);
		isOutputBoolean = isBoolean(outputClassName);
		isInputInteger = isInteger(inputClassName);
		isOutputInteger = isInteger(outputClassName);
		isInputDouble = isDouble(inputClassName);
		isOutputDouble = isDouble(outputClassName);
		isInputBigDecimal = inputClassName == BigDecimal.class.getName();
		isOutputBigDecimal = outputClassName == BigDecimal.class.getName();
		
		if (!isInputString && !isOutputString)
			throw new RuntimeException("Can convert only strings.");
		
		if (isInputString && !isOutputBoolean && !isOutputInteger && !isOutputDouble && !isOutputBigDecimal)
			throw new RuntimeException("Can convert only to boolean, integer, double and big decimal.");
		
		if (isOutputString &&!isInputBoolean && !isInputInteger && !isInputDouble && !isInputBigDecimal)
			throw new RuntimeException("Can convert only from boolean, integer, double and big decimal.");
		
		Locale loc = LocaleAndSymbolUtils.getLocaleFromString(locale);
		parserAndFormatter = new NumberParserAndFormatter(loc);
	}
	
	/** Converts between string and boolean or number */ 
	@Override
	public Object convert(Object input) {
		
		if (isInputString && isOutputString)
			return input;
		else if (isInputString)
			return convertFromString((String) input);
		else if (isOutputString)
			return convertToString(input);
		return input;
	}
	
	private Object convertFromString(String input) {
		if (isOutputBoolean)
			return Boolean.valueOf(input);
		else if (isOutputInteger)
			return StringUtils.isBlank(input) ? 0 : parserAndFormatter.parseInteger(input); 
		else if (isOutputDouble)
			return StringUtils.isBlank(input) ? 0 : parserAndFormatter.parseDouble(input); 
		else if (isOutputBigDecimal)
			return StringUtils.isBlank(input) ? 0 : parserAndFormatter.parseBigDecimal(input); 
		return input;
	}

	private String convertToString(Object input) {
		if (input == null)
			return null;
		if (isInputBoolean)
			return ((Boolean) input).toString();
		else if (isInputInteger)
			return parserAndFormatter.formatInteger(((Number) input).intValue());
		else if (isInputDouble)
			return parserAndFormatter.formatDouble(((Number) input).doubleValue());
		else if (isInputBigDecimal)
			return parserAndFormatter.formatBigDecimal((BigDecimal) input);
		return input.toString();
	}

	private boolean isBoolean(String className) {
		return className.equals(boolean.class.getName()) || className.equals(Boolean.class.getName());
	}
	
	private boolean isInteger(String className) {

		return className.equals(byte.class.getName()) || className.equals(Byte.class.getName()) ||
			className.equals(short.class.getName()) || className.equals(Short.class.getName()) ||
			className.equals(int.class.getName()) || className.equals(Integer.class.getName()) ||
			className.equals(long.class.getName()) || className.equals(Long.class.getName());
	}
	
	private boolean isDouble(String className) {
		
		return className.equals(float.class.getName()) || className.equals(Float.class.getName()) ||
				className.equals(double.class.getName()) || className.equals(Double.class.getName());
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + inputClassName + " to " + outputClassName;
	}
}
