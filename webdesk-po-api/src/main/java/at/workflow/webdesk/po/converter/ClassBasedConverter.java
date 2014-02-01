package at.workflow.webdesk.po.converter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.commons.lang.StringUtils;

import at.workflow.webdesk.po.NumbersDatesAndTimes;

/**
 * This converter lets through values of same class 
 * and converts automatically between boolean, between numbers and between DateTime.
 * 
 * For number conversion Number superclass of Float, Integer, ... is used. 
 * The only clue is to apply the right method for getting the value.
 * 
 * DateTime objects are first changed to long time milliseconds and then constructed from that number.
 * 
 * All data properties and methods are computed during instantiation.
 * If the conversion is not possible exception is thrown.
 * 
 * During conversion found methods are just applied.
 * 
 * @author sdzuban 06.07.2012
 */
public class ClassBasedConverter implements AutomaticConverter {

	/**
	 * Class names used because it is not possible to obtain Class of primitives by Class.forName().
	 */
	private String inputClassName, outputClassName;
	private boolean isInputBoolean = false, isOutputBoolean = false;
	private boolean isInputNumber = false, isOutputNumber = false;
	private boolean isInputDateTime = false, isOutputDateTime = false;
	
	private Method getNumber;
	private Method getTimeMillis;
	private Constructor<?> getDateTime;

	public ClassBasedConverter (String inputClassName, String outputClassName) {
		if (StringUtils.isBlank(inputClassName) || StringUtils.isBlank(outputClassName))
			throw new RuntimeException("Blank class name.");
		this.inputClassName = inputClassName;
		this.outputClassName = outputClassName;
		checkConditionsAndPrepareMethods(inputClassName, outputClassName);
	}
	
	public ClassBasedConverter(Class<?> inputClass, Class<?> outputClass) {
		if (inputClass == null || outputClass == null)
			throw new RuntimeException("Null class.");
		this.inputClassName = inputClass.getName();
		this.outputClassName = outputClass.getName();
		checkConditionsAndPrepareMethods(inputClassName, outputClassName);
	}
	
	/**
	 * Instantiates converter for conversion between compatible types, i.e. 
	 * between same type, between numbers and between date/time objects.
	 * If the inputClass and outputClass are not compatible RuntimeException is thrown. 
	 * @param inputClassName class of input data for the converter
	 * @param outputClassName class of output date from converter
	 */
	private void checkConditionsAndPrepareMethods(String inputClassName, String outputClassName) {
		
		if (inputClassName == null || outputClassName == null)
			throw new RuntimeException("Cannot convert null class");
		
		if (inputClassName != outputClassName) {
			
			isInputBoolean = isBoolean(inputClassName);
			isOutputBoolean = isBoolean(outputClassName);
			isInputNumber = isNumber(inputClassName);
			isOutputNumber = isNumber(outputClassName);
			isInputDateTime = NumbersDatesAndTimes.isAnyDateTime(inputClassName);
			isOutputDateTime = NumbersDatesAndTimes.isAnyDateTime(outputClassName);

			if (! isInputBoolean && ! isOutputBoolean &&
					! isInputNumber && ! isOutputNumber && 
					! isInputDateTime && ! isOutputDateTime)
				throw new RuntimeException("Can convert only between booleans, between numbers and between dateTimes");
			if (isInputBoolean && ! isOutputBoolean ||
					isOutputBoolean && !isInputBoolean)
				throw new RuntimeException("Cannot convert between boolean and non-boolean.");
			if (isInputNumber && ! isOutputNumber ||
					isOutputNumber && !isInputNumber)
				throw new RuntimeException("Cannot convert between number and non-number.");
			if (isInputDateTime && ! isOutputDateTime ||
					isOutputDateTime && !isInputDateTime)
				throw new RuntimeException("Cannot convert between dateTime and non-dateTime");
			
			if (isInputNumber && isOutputNumber)
				getNumber = getNumberMethod(outputClassName);
			else if (isInputDateTime && isOutputDateTime) {
				getTimeMillis = getTimeMillis(inputClassName);
				getDateTime = getDateTime(outputClassName);
			}
		}
	}
	
	/**
	 * Lets through null, all objects if instantiated with two same classes,
	 * converts between numbers if instantiated with two number classes,
	 * converts between date/times if instantiated with two date/time classes. 
	 * @param input
	 * @return same as input or converted object with equivalent value.
	 */
	@Override
	public Object convert(Object input) {
		
		if (input == null)
			return null;
		
		try {
			if (inputClassName == outputClassName)
				return input;
			else if (isInputBoolean && isOutputBoolean)
				return input;
			else if (isInputNumber && isOutputNumber) {
				
				return getNumber.invoke(input);
				
			} else if (isInputDateTime && isOutputDateTime) {
				
				long time = (Long) getTimeMillis.invoke(input);
				return getDateTime.newInstance(time);
			}
		} catch (Exception e) {
			throw new RuntimeException("Exception while converting data: " + e);
		}
		return null;
	}
	
	private boolean isBoolean(String className) {
		return className.equals(boolean.class.getName()) || className.equals(Boolean.class.getName());
	}
	
	private boolean isNumber(String className) {
		
		return className.equals(byte.class.getName()) || className.equals(Byte.class.getName()) ||
		className.equals(short.class.getName()) || className.equals(Short.class.getName()) ||
		className.equals(int.class.getName()) || className.equals(Integer.class.getName()) || className.equals(BigInteger.class.getName()) ||
		className.equals(long.class.getName()) || className.equals(Long.class.getName()) ||
		className.equals(float.class.getName()) || className.equals(Float.class.getName()) ||
		className.equals(double.class.getName()) || className.equals(Double.class.getName()) || className.equals(BigDecimal.class.getName());
	}
	
	private Method getNumberMethod(String outputClassName) {
		
		String name = "";
		if (outputClassName.equals(byte.class.getName()) || outputClassName.equals(Byte.class.getName()))
			name = "byteValue";
		else if (outputClassName.equals(short.class.getName()) || outputClassName.equals(Short.class.getName()))
			name = "shortValue";
		else if (outputClassName.equals(int.class.getName()) || outputClassName.equals(Integer.class.getName()) || outputClassName.equals(BigInteger.class.getName()))
			name = "intValue";
		else if (outputClassName.equals(long.class.getName()) || outputClassName.equals(Long.class.getName()))
			name = "longValue";
		else if (outputClassName.equals(float.class.getName()) || outputClassName.equals(Float.class.getName()))
			name = "floatValue";
		else if (outputClassName.equals(double.class.getName()) || outputClassName.equals(Double.class.getName()) || outputClassName.equals(BigDecimal.class.getName()))
			name = "doubleValue";
		else
			throw new RuntimeException("Cannot convert number to " + outputClassName);
			
		try {
			return Number.class.getMethod(name);
		} catch (Exception e) {
			throw new RuntimeException("Exception while initializing converter: " + e);
		}
	}
	
	private Method getTimeMillis(String inputClassName) {
	
		try {
			Class<?> inputClass = Class.forName(inputClassName);
			return inputClass.getMethod("getTime");
		} catch (Exception e) {
			throw new RuntimeException("Exception while initializing converter: " + e);
		}
	}
	
	private Constructor<?> getDateTime(String outputClassName) {
		
		try {
			Class<?> outputClass = Class.forName(outputClassName);
			return outputClass.getConstructor(long.class);
		} catch (Exception e) {
			throw new RuntimeException("Exception while initializing converter: " + e);
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + inputClassName + " to " + outputClassName;
	}
}
