package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoModule;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoModuleDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoModule> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoModule.class);
		super.onSetUpAfterDataGeneration();
	}

}
