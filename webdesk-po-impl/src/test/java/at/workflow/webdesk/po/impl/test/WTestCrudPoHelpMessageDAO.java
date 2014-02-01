package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoHelpMessage;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoHelpMessageDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoHelpMessage> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoHelpMessage.class);
		super.onSetUpAfterDataGeneration();
	}

}
