package at.workflow.webdesk.tools.date;

import java.util.Comparator;

import at.workflow.webdesk.tools.api.Historization;

/**
 * Helper class to sort a list of <code>Historicizable</code>
 * objects with validfrom as sort attribute.
 * 
 * @author ggruber
 */
public class HistoricizableValidFromComparator implements Comparator<Historization> {

	@Override
	public int compare(Historization timeRange1, Historization timeRange2) {
		return timeRange1.getValidfrom().compareTo(timeRange2.getValidfrom());
	}

}
