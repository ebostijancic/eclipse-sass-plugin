package at.workflow.webdesk.po.model;

import java.util.Comparator;

import at.workflow.webdesk.tools.api.Historization;

/**
 * Uses sort order by (1) valid-from and (2) valid-to. Thus items starting at the same
 * time will be sorted by the length of their interval, whereby the longer interval goes
 * back in list.
 * 
 * @author sdzuban 26.03.2013
 * @author fritzberger 20.06.2013 made this a top level class and moved it to module base-api.
 */
public class HistorizationComparator implements Comparator<Historization>
{
	private boolean newestFirst;
	
	/**
	 * Oldest item should be at head of list (first), newest should be at tail (last).
	 */
	public HistorizationComparator()	{
		this(false);
	}
	
	/**
	 * Newest item should be at head of list (first), oldest should be at tail (last).
	 * This is the standard sorter for UI representation of lists of historicized entities.
	 * @param newestFirst when true, the newest item will be first and oldest last, vice versa when false.
	 */
	public HistorizationComparator(boolean newestFirst)	{
		this.newestFirst = newestFirst;
	}
	
	/**
	 * This uses the valid-from field to estimate sort order.
	 * If comparison of valid-from field yields 0, valid-to fields are compared. 
	 * This ensures deterministic ordering of entities starting at the same time:
	 * the item with the longer the interval goes back in list. 
	 */
	@Override
	public int compare(Historization entity1, Historization entity2) {
		final int result = compareForOldestFirst(entity1, entity2);
		return newestFirst ? -result : result;
	}

	private int compareForOldestFirst(Historization entity1, Historization entity2) {
		assert entity1.getValidfrom() != null && entity2.getValidfrom() != null;
		
		final int fromResult = entity1.getValidfrom().compareTo(entity2.getValidfrom());
		if (fromResult == 0)
			return entity1.getValidto().compareTo(entity2.getValidto());
		
		return fromResult;
	}

}
