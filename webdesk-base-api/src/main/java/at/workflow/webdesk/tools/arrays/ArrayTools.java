/**
 * 
 */
package at.workflow.webdesk.tools.arrays;

/**
 * @author sdzuban 07.11.2013
 *
 */
public class ArrayTools {

    public static String[] mergeStringArrays(String[] first, String[] second) {
    	if (first == null || first.length == 0)
    		return second;
    	if (second == null || second.length == 0)
    		return first;

    	String[] result = new String[first.length + second.length];
    	int idx = 0;
    	for (String s : first)
    		result[idx++] = s;
    	for (String s : second)
    		result[idx++] = s;
    	return result;
    }
    
    public static Boolean[] mergeBooleanArrays(Boolean[] first, Boolean[] second) {
    	if (first == null || first.length == 0)
    		return second;
    	if (second == null || second.length == 0)
    		return first;
    	
    	Boolean[] result = new Boolean[first.length + second.length];
    	int idx = 0;
    	for (Boolean b : first)
    		result[idx++] = b;
    	for (Boolean b : second)
    		result[idx++] = b;
    	return result;
    }
}
