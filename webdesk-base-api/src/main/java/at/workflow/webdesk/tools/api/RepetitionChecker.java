package at.workflow.webdesk.tools.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import at.workflow.webdesk.tools.comparator.PropertiesComparator;

/**
 * Checks for repetition in collection of Historization objects.
 * 
 * @author sdzuban 16.10.2013
 *
 */
public class RepetitionChecker {
	
	/** @return true if the beans contains at least two beans with all properties (except UID) having identical values */
	public static <T extends Historization> boolean containsRepetition(Collection<T> beans) {
		
		List<T> beanList = new ArrayList<T>();
		beanList.addAll(beans);
		
		for (int i = 0; i < beanList.size() - 1; i++) {
			for (int k = i + 1; k < beanList.size(); k++) {
				if (PropertiesComparator.isEqual(beanList.get(i), beanList.get(k), "UID"))
					return true;
			}
		}
		return false;
	}

}
