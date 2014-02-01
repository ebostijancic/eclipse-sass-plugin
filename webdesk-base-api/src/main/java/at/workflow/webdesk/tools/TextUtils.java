package at.workflow.webdesk.tools;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

/**
 * Utilities in relation with text modifications.
 * 
 * @author fritzberger 21.11.2012
 * @author sdzuban 10.05.2013
 */
public class TextUtils
{
	/**
	 * @return the simple class name from a fully qualified (or even simple) one, i.e. returns
	 * 		"D" for "D",
	 * 		null for "d",
	 * 		"D" for "a.b.c.D",
	 * 		null from "a.b.c",
	 * 		"D" for "a.b.C.D",
	 * 		"C$D" for "a.b.C$D",
	 */
	public static String getSimpleClassName(String fullyQualifiedOrSimpleClassName)	{
		assert StringUtils.isNotEmpty(fullyQualifiedOrSimpleClassName);
		
		final int i = fullyQualifiedOrSimpleClassName.lastIndexOf(".");
		String simpleClassname = fullyQualifiedOrSimpleClassName;
		if (i > 0)	{
			assert i < fullyQualifiedOrSimpleClassName.length() - 1 : "Has dot at end: "+fullyQualifiedOrSimpleClassName;
			
			simpleClassname = fullyQualifiedOrSimpleClassName.substring(i + 1);
		}
		
		return Character.isUpperCase(simpleClassname.charAt(0)) ? simpleClassname : null;
	}
	
	/**
	 * Cuts or fills up a given text to a given length. Always cuts tail when too long,
	 * but can fill up at head or tail, according to <code>alignTextLeftWhenFillingUp</code>.
	 * @param toFormat the text to fill up or chop, can be null, then null is returned.
	 * @param length the required length of the output text.
	 * @param fillUpChar when text is shorter, fill up with this character.
	 * @param alignTextLeftWhenFillingUp when true, fillUpChar get appended to end, else inserted at start.
	 * @return the input text, formatted to given length according to given parameters, or null when null was passed in.
	 */
	public static String cutOrFillup(String toFormat, int length, char fillUpChar, boolean alignTextLeftWhenFillingUp)	{
		if (toFormat == null)
			return null;
		
		if (toFormat.length() == length)
			return toFormat;
		
		if (toFormat.length() > length)
			return toFormat.substring(0, length);
		
		return alignTextLeftWhenFillingUp
			? StringUtils.rightPad(toFormat, length, fillUpChar)
			: StringUtils.leftPad(toFormat, length, fillUpChar);
	}
	
	/**
	 * Turns the last of a "camelCase.and.moreCamelCase" word into a human readable representation like "More Camel Case",
	 * with spaces in between, all initial characters capitalized.
	 * @param dottedCamelCase the text to turn from CamelCase into human readable text with spaces in between.
	 * @return the given text as human readable text with spaces in between.
	 */
	public static String lastOfDottedCamelCaseToWords(String dottedCamelCase)	{
		final int i = dottedCamelCase.lastIndexOf(".");
		if (i > 0)
			return camelCaseToWords(dottedCamelCase.substring(i + 1));
		return camelCaseToWords(dottedCamelCase);
	}
	
	
	/**
	 * Turns a "camelCase.and.moreCamelCase" word into a human readable representation like "Camel Case And More Camel Case",
	 * with spaces in between, all initial characters capitalized.
	 * @param dottedCamelCase the possibly dotted text to turn from CamelCase into human readable text with spaces in between.
	 * @return the given text as human readable text with spaces in between.
	 */
	public static String dottedCamelCaseToWords(String dottedCamelCase)	{
		String s = "";
		int i;
		do	{
			i = dottedCamelCase.indexOf(".");
			String part = dottedCamelCase;
			if (i > 0)	{
				part = dottedCamelCase.substring(0, i);
				dottedCamelCase = dottedCamelCase.substring(i + 1);
			}
			
			if (s.length() > 0)
				s += " ";
			s += TextUtils.camelCaseToWords(part);	// TODO: i18n
		}
		while (i > 0);
		return s;
	}
	
	/**
	 * Turns a "camelCase" word into a human readable representation like "Camel Case",
	 * with spaces in between, all initial characters capitalized. 
	 * @param camelCase the text to turn from CamelCase into human readable text with spaces in between.
	 * @return the given text as human readable text with spaces in between.
	 */
    public static String camelCaseToWords(String camelCase) {
    	if (camelCase == null)
    		return null;
    	
        final StringBuilder wordGroup = new StringBuilder();
        wordGroup.append(Character.toUpperCase(camelCase.charAt(0)));	// start with upper case in any case
        boolean wasUpperCase = true;
        boolean wasSpace = false;
        
        for (int i = 1; i < camelCase.length(); i++) {
        	final char c = camelCase.charAt(i);
        	final boolean isUpperCase = Character.isUpperCase(c);
        	final boolean isLowerCase = Character.isLowerCase(c);
        	if (isUpperCase == false && isLowerCase == false)	{	// turn any non-alfa into a space
        		if (wasSpace == false)
        			wordGroup.append(' ');
        		wasSpace = true;
        	}
        	else	{
	        	if (isUpperCase && wasSpace == false &&
	        			(wasUpperCase == false || i < camelCase.length() - 1 && Character.isLowerCase(camelCase.charAt(i + 1))))
	        		// starting upper case character, there was none before, or there is coming a lower case char after
	        		wordGroup.append(' ');
	        	
	    		wordGroup.append(c);
	    		wasUpperCase = isUpperCase;
        		wasSpace = false;
        	}
        }
        return wordGroup.toString();
    }


    /**
     * Turns a property name, given as plural (e.g. "jobFamilies") into its singular form (e.g. "JobFamily").
     * This is needed to construct singular names of bean-adder/remover-methods from plural property names (e.g. "addJobFamily").
     * <p/>
     * For problems like "phase" - "phases" vs "kiss" - "kisses"
     * 		see http://www.informatics.sussex.ac.uk/research/groups/nlp/carroll/morph.html
     * 
     * @param propertyNamePlural the plural property name to convert to singular.
     * @return the singular property name, or the given name if it is not plural (no trailing "ies" or "s").
     */
    public static String getSingularPropertyName(String propertyNamePlural)	{
		if (propertyNamePlural.endsWith("ies"))
			return propertyNamePlural.substring(0, propertyNamePlural.length() - "ies".length())+"y";
		
		if (propertyNamePlural.endsWith("sses"))
			return propertyNamePlural.substring(0, propertyNamePlural.length() - "es".length());
		
		if (propertyNamePlural.endsWith("s"))
			return propertyNamePlural.substring(0, propertyNamePlural.length() - "s".length());
		
		return propertyNamePlural;
    }

    
	/**
	 * Splits a property expression by ".".
	 * @return e.g. ["job", "name"] from "job.name", but ["job", null] from "job".
	 */
	public static String [] getFirstAndNextPart(String propertyExpression)	{
		final int i = propertyExpression.indexOf(".");
		if (i < 0)
			return new String[] { propertyExpression, null };
		
		final String firstPart = propertyExpression.substring(0, i);
		final String remainderPart = propertyExpression.substring(i + 1);
		return new String[] { firstPart, remainderPart };
	}
	
	
	/**
	 * @return e.g. "name" from "job.name", but "job" from "job".
	 */
	public static String getRightmostPropertyName(String propertyExpression)	{
		final int i = propertyExpression.lastIndexOf(".");
		return (i < 0) ? propertyExpression : propertyExpression.substring(i + 1);
	}
	
	
	/**
	 * @return "PoPerson" from "at.workflow.webdesk.po.model.PoPerson$Gender",
	 * 		but null from "at.workflow.webdesk.po.model.PoPerson".
	 */
	public static String getEnclosingSimpleClassname(String fullyQualifiedClassName)	{
		final int end = fullyQualifiedClassName.lastIndexOf("$");
		if (end < 0)
			return null;
		
		final int start = fullyQualifiedClassName.lastIndexOf(".") + 1;
		return fullyQualifiedClassName.substring(start, end);
	}

	/**
	 * @return "One Two" for "ONE_TWO", but also "One Two" for "OneTwo" (CamelCase).
	 */
	public static String upperCaseAndUnderScoreToWords(String constantIdentifier)	{
		if (constantIdentifier.toUpperCase().equals(constantIdentifier))	// all is upper-case
			return WordUtils.capitalizeFully(constantIdentifier, new char [] { '_' });	// separator '_'
		
		return camelCaseToWords(constantIdentifier);
	}

}
