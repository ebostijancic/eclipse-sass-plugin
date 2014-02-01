package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoJobTrigger;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoJobTriggerDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoJobTrigger> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoJobTrigger.class);
		super.onSetUpAfterDataGeneration();
	}

}
