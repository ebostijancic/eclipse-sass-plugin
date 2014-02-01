package at.workflow.webdesk;

import java.util.Iterator;
import java.util.List;

public abstract class QueryBuilderHelper {
	
	/**
	 * Creates a text that contains comma-separated UID values.
	 * The text will be '' when null or an empty list is passed.
	 * @param uids List of uid values to convert to a comma-separated string, can be null or empty.
	 * @param isString when true, each item of the list will be enclosed into single 'quotes'.
	 * @return the empty string '' when list null or empty,
	 * 		else 'uid1','uid2',... when isString == true, uid1,uid2,... when isString == false.
	 */
	public static String generateCommaList(List<String> uids, boolean isString) {
		if (uids == null || uids.size() <= 0)
			return "''";
        
        StringBuffer sUids = new StringBuffer();
        for (Iterator<String> i = uids.iterator(); i.hasNext(); ) {
            String uid = i.next();
            if (isString)
            	uid = "'" + uid + "'";
            
            if (i.hasNext())
            	sUids.append(uid+",");
            else
            	sUids.append(uid);
        }
        
        return sUids.toString();
    }
}
