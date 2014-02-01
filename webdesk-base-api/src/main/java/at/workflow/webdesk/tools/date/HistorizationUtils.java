/**
 * 
 */
package at.workflow.webdesk.tools.date;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import at.workflow.webdesk.tools.api.Historization;

/**
 * This is a collection of methods for handling historizations. 
 * 
 * @author sdzuban 09.01.2014
 *
 */
public class HistorizationUtils {

	/**
	 * TODO: document this.
	 */
	public static List<Historization> getOverlappingEntries(List<? extends Historization> list, Date validFrom, Date validTo) {
		Iterator<? extends Historization> it = list.iterator();
		List<Historization> overlappedEntries = new ArrayList<Historization>();
		while (it.hasNext()) {
			Historization histObj = it.next();
			if (DateTools.doesOverlap(histObj.getValidfrom(), histObj.getValidto(), validFrom, validTo, false)) {
				overlappedEntries.add(histObj);
			}
		}
		return overlappedEntries;
	}

	/**
	 * This function returns a <code>List</code> of overlapping
	 * <code>Historicizable</code> objects.
	 * 
	 * @param list
	 *            a <code>List</code> of <code>Historicizable</code> objects.
	 * @param validFrom
	 *            <code>Date</code>
	 * @param validTo
	 *            <code>Date</code>
	 * @param checkDateOnly
	 *            <code>boolean</code> should the time portion be ignored?
	 */
	public static List<? extends Historization> getOverlappingEntriesIncludingPast(List<? extends Historization> list, Date validFrom, Date validTo) {
		return getOverlappingEntriesIncludingPast(list, validFrom, validTo, true);
	}

	/**
	 * This function returns a <code>List</code> of overlapping
	 * <code>Historicizable</code> objects.
	 * 
	 * @param list
	 *            a <code>List</code> of <code>Historicizable</code> objects.
	 * @param validFrom
	 *            <code>Date</code>
	 * @param validTo
	 *            <code>Date</code>
	 * @param checkDateOnly
	 *            <code>boolean</code> should the time portion be ignored? <br/>
	 */
	public static List<? extends Historization> getOverlappingEntriesIncludingPast(List<? extends Historization> list, Date validFrom, Date validTo, boolean checkDateOnly) {
		List<Historization> overlappedEntries = new ArrayList<Historization>();
		for (Historization histObj : list) {
			if (DateTools.doesOverlap(histObj.getValidfrom(), histObj.getValidto(), validFrom, validTo, checkDateOnly)) {
				overlappedEntries.add(histObj);
			}
		}
		return overlappedEntries;
	}

}
