package at.workflow.webdesk.tools.date;

import java.util.Comparator;

import at.workflow.webdesk.tools.api.Historization;

/**
 * Helper class to sort a list of <code>Historicizable</code>
 * objects with validfrom as sort attribute.
 * 
 * @author ggruber
 */
public class HistoricizableValidityComparator implements Comparator<Historization> {

	@Override
	public int compare(Historization timeRange1, Historization timeRange2) {
		int result = timeRange1.getValidfrom().compareTo(timeRange2.getValidfrom());
		if (result == 0)
			return timeRange1.getValidto().compareTo(timeRange2.getValidto());
		return result;
	}

}
