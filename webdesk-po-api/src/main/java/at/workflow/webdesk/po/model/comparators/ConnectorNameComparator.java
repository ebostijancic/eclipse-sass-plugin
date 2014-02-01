package at.workflow.webdesk.po.model.comparators;

import java.util.Comparator;

import at.workflow.webdesk.po.model.PoConnector;

/**
 * Moved from PoEditConnectorLinkActionHandler.
 * 
 * @author sdzuban 02.07.2012
 */
public class ConnectorNameComparator implements Comparator<PoConnector> {

	@Override
	public int compare(PoConnector c1, PoConnector c2) {
		if (c1 == null && c2 == null)
			return 0;
		if (c1 == null)
			return -1;
		if (c2 == null)
			return 1;
		final String name1 = c1.getName();
		final String name2 = c2.getName();
		if (name1 == null && name2 == null)
			return 0;
		if (name1 == null)
			return -1;
		if (name2 == null)
			return 1;
		return name1.compareToIgnoreCase(name2);
	}
}
