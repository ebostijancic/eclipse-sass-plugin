package at.workflow.webdesk.po.converter;

/**
 * This interface specifies common methods of automatic converters.
 *  
 * @author sdzuban 24.10.2012
 */
public interface AutomaticConverter {
	
	/** Converts input of one type, e.g. BigDecimal, to object of another type, e.g. double */
	Object convert(Object input);

	/** @return description of converter. At least converter name and input and output class names shall be specified. */
	@Override
	String toString();
}