package at.workflow.webdesk.tools;

import java.util.Date;

/**
 * this enum describes all datatypes which can be defined in cocoon based
 * simple datalists inside the colum tag as 'datatype' attribute. Be sure to
 * use a lowercase form of the string. <br/>
 * f.i.
 * <pre>
 * &lt;column header="po_active" .. datatype="boolean"/&gt; 
 * </pre>
 * 
 * @author ggruber
 * @author fritzberger 2013-12-04: added util methods to integrate classes as types, and to convert String values.
 */
public enum Datatype { 
	STRING,
	DATE,
	DATETIME,
	TIME,
	NUMBER,
	BOOLEAN,
	INTEGER,
	LONG,
	FLOAT,
	DOUBLE,
	ENUM;

	/**
	 * TODO: this does never return DATETIME or TIME.
	 * @return the Datatype enum according to the given class. 
	 */
	public static Datatype fromClass(Class<?> clazz)	{
		if (clazz == null)
			return null;
		
		if (String.class.isAssignableFrom(clazz))
			return STRING;
		
		if (Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz))
			return BOOLEAN;
		
		if (Enum.class.isAssignableFrom(clazz))
			return ENUM;
		
		if (Date.class.isAssignableFrom(clazz))
			return DATE;
		
		if (Integer.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz))
			return INTEGER;
		
		if (Long.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz))
			return LONG;
		
		if (Float.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz))
			return FLOAT;
		
		if (Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz))
			return DOUBLE;
		
		if (Number.class.isAssignableFrom(clazz) || short.class.isAssignableFrom(clazz))
			return NUMBER;
		
		throw new IllegalArgumentException("Unknown Datatype: "+clazz);
	}

	/**
	 * TODO: this does never process DATETIME or TIME (throws Exception then).
	 * @return the class according to the given Datatype enum. 
	 */
	public static Class<?> toClass(Datatype dataType)	{
		if (dataType == null)
			return null;
		
		switch (dataType)	{
			case STRING:
				return String.class;
			case BOOLEAN:
				return Boolean.class;
			case ENUM:
				return Enum.class;
			case INTEGER:
				return Integer.class;
			case LONG:
				return Long.class;
			case FLOAT:
				return Float.class;
			case DOUBLE:
				return Double.class;
			case DATE:
				return Date.class;
			case NUMBER:
				return Number.class;
			default:
				throw new IllegalArgumentException("Datatype not implemented: "+dataType);
		}
	}

	/**
	 * TODO: this does never convert DATETIME or TIME (throws Exception then).
	 * @param value the String value to convert to Integer, Double, Boolean, ....
	 * @return the given value as an object of the class according to given dataType. 
	 */
	public static Object toTypedValue(String value, Datatype dataType)	{
		if (dataType == null)
			return null;
		
		switch (dataType)	{
			case STRING:
				return value;
			case BOOLEAN:
				return Boolean.valueOf(value);
			case INTEGER:
				return Integer.valueOf(value);
			case LONG:
				return Long.valueOf(value);
			case FLOAT:
				return Float.valueOf(value);
			case NUMBER:
			case DOUBLE:
				return Double.valueOf(value);
			case DATE:	// TODO
			case ENUM:	// can not be done here: of which enum type is the value?
			default:
				throw new IllegalArgumentException("Please write a value-conversion for dataType: "+dataType);
		}
	}

}
