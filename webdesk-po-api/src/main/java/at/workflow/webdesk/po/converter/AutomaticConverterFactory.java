package at.workflow.webdesk.po.converter;

import org.apache.commons.lang.StringUtils;

/**
 * This factory provides methods for automatic creation of AutomaticConverter
 * according to given classes/classNames and locale.
 * 
 * @author sdzuban 24.10.2012
 */
public class AutomaticConverterFactory {
	
	private static final String STRING = String.class.getName();
	
	private AutomaticConverterFactory() {} // not to be instantiated
	

	/** Provides automatic converter for input to output class conversion or null if none can be find. */
	public static AutomaticConverter getConverter(Class<?> inputClass, Class<?> outputClass, String ... locale) {
		
		if (inputClass == null || outputClass == null)
			throw new IllegalArgumentException((inputClass == null ? "Input" : "Output") + " class is null.");
		
		return getConverter(inputClass.getName(), outputClass.getName(), locale);
	}
	
	/** Provides automatic converter for input to output class conversion or null if none can be find. */
	public static AutomaticConverter getConverter(String inputClassName, String outputClassName, String ... locale) {
	
		if (StringUtils.isBlank(inputClassName) || StringUtils.isBlank(outputClassName))
			throw new IllegalArgumentException((StringUtils.isBlank(inputClassName) ? "Input" : "Output") + " class name is null or empty.");
		
		if (STRING.equals(inputClassName) || STRING.equals(outputClassName)) {
			String localeString = locale == null || locale.length == 0 ? null : locale[0]; 
			try {
				return new StringConverter(inputClassName, outputClassName, localeString);
			} catch (Exception e) {
				// String converter cannot handle this combination
			}
		} else {
			try {
				return new ClassBasedConverter(inputClassName, outputClassName);
			} catch (Exception e) {
				// Class based converter cannot handle this combination
			}
		}
		return null;
	}
}
