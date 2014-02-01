package at.workflow.webdesk.tools;

import java.util.Collections;
import java.util.List;

/**
 * @author fritzberger 18.03.2013
 */
public class PaginationUtil {
	
	/**
	 * Method for full pagination emulation.
	 * @param result non paginated result
	 * @return required result page
	 * 
	 * @deprecated undocumented fake pagination will be taken as real pagination but it is not.
	 */
	public static List getPaginatedResult(PaginableQuery query, List result) {
		
		int firstRes = query.getFirstResult();
		if (firstRes < 0)
			firstRes = 0;
		
		int maxRes = query.getMaxResults();
		if (maxRes <= 0)
			maxRes = result.size();
		
		if (result != null && !result.isEmpty()) {
			if (firstRes >= result.size())
				return Collections.emptyList();
			else if (firstRes + maxRes <= result.size()) 
				return result.subList(firstRes, firstRes + maxRes);
			else
				return result.subList(firstRes, result.size());
		}
		return result;		
	}

}
