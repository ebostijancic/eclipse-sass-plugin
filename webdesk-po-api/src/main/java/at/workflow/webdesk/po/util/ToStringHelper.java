package at.workflow.webdesk.po.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import at.workflow.webdesk.tools.ReflectionUtils;
import at.workflow.webdesk.tools.comparator.TemplateDrivenComparator;

/**
 * Convention for outputting PoBase derivations (persistent domain objects).
 * 
 * @author sdzuban 03.04.2013
 * @author fritzberger 2013-11-07 removed unused code
 */
public class ToStringHelper {
	
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.s");
	
	/**
	 * For description see class header.
	 */
	public static String toString(Object object) {
		return toString(object, ToStringStyle.SHORT_PREFIX_STYLE);
	}
		
	/**
	 * For description see class header.
	 * @param toStringStyle see {@link org.apache.commons.lang.builder.ToStringStyle},
	 * 		default is SHORT_PREFIX_STYLE.
	 */
	public static String toString(Object object, ToStringStyle toStringStyle) {

		final ToStringStyle style = toStringStyle != null ? toStringStyle : ToStringStyle.SHORT_PREFIX_STYLE;
		final ToStringBuilder toStringBuilder = new ToStringBuilder(object, style);
		
		try {
			// sort order for shown properties
			final String [] sortOrder = new String [] { "shortName", "name", "lastName", "firstName", "userName", "uID", "taID", "employeeId", "gender", "email" };
			final SortedMap<String,Object> sortedStandardProperties = new TreeMap<String, Object>(new TemplateDrivenComparator(sortOrder));
			sortedStandardProperties.putAll(ReflectionUtils.getStandardPropertyValues(object));
			
			// do not show following properties
			final List<String> unwanted = new ArrayList<String>(Arrays.asList(new String []	{
					"description", "comment", "valid", "validtoNull", "label", "validity"
			}));
			
			// maximum count for shown properties
			final int MAXIMUM_SHOWN_PROPERTIES = 6;
			int count = 0;
			
			// append loop
			for (Map.Entry<String,Object> property : sortedStandardProperties.entrySet())	{
				final String name = property.getKey();
				final Object value = property.getValue();
				
				if (unwanted.contains(name) == false) {
					if (value instanceof Date) 
						toStringBuilder.append(name, dateFormat.format((Date) value));
					else
						toStringBuilder.append(name, value);
					
					count++;
					if (count >= MAXIMUM_SHOWN_PROPERTIES)
						break;
				}
			}
		}
		catch (Exception e) {
			toStringBuilder.append("Exception occurred:", e.toString());
		}
		
		return toStringBuilder.toString().replaceAll(",", ", ");
	}
}
