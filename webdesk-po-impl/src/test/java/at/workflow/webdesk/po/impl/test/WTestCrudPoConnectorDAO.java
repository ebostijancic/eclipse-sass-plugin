package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoConnector;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoConnectorDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoConnector> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoConnector.class);
		super.onSetUpAfterDataGeneration();
	}

}
