package at.workflow.webdesk.po.converter;

/**
 * Can be used to convert an object into another object.
 * e.g. 
 * <pre>
 * String -> Date 
 * String -> Number
 * ....
 * </pre>
 * Converters have to implement the singleton pattern!
 * 
 * fri_2011-11-19: but only one sub-class actually does ...
 *
 *	sdzuban 2012-07-18
 *	For locale-based number conversion following additions were made
 *	- added isLocaleRequired()
 *  - added locale parameter in convert()
 *
 * @author hentner
 */
public interface Converter {

	/**
	 * The name of the converter. Can be an 18n key. 
	 */
	public String getName();
	
	/**
	 * @return a <code>boolean</code> value. If <code>true</code> is returned, 
	 * an additional pattern definition is needed.
	 */
	public boolean isPatternRequired();
	
	/**
	 * @return the default pattern. 
	 */
	public String getDefaultPattern();
	
	/**
	 * @return a <code>boolean</code> value. If <code>true</code> is returned, 
	 * an locale definition is needed.
	 */
	public boolean isLocaleRequired();
	
	/**
	 * @param o the object which has to be converted.
	 * @param pattern optional, the pattern that is used to convert.
	 * @param locale optional, locale to use for conversion
	 * @return an Object if the conversion was ok, null otherwise.
	 */
	public Object convert(Object o, String pattern, String locale);

}
