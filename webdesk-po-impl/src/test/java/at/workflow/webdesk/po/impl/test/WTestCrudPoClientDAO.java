package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoClientDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoClient> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoClient.class);
		super.onSetUpAfterDataGeneration();
	}

}
