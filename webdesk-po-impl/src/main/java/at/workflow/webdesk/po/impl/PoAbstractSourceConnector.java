package at.workflow.webdesk.po.impl;

import java.util.List;
import java.util.Map;

import at.workflow.webdesk.po.PoSourceConnectorInterface;

/**
 * @author fritzberger 27.02.2012
 */
public abstract class PoAbstractSourceConnector extends PoAbstractConnector implements PoSourceConnectorInterface {

	/** Default implementation that does nothing. To be overridden. */
	@Override
	public void postProcessImportedRecords(List<Map<String, Object>> result) {
	}

}
