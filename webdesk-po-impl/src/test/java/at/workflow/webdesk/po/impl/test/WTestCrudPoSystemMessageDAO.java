package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoSystemMessage;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoSystemMessageDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoSystemMessage> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoSystemMessage.class);
		super.onSetUpAfterDataGeneration();
	}

}
