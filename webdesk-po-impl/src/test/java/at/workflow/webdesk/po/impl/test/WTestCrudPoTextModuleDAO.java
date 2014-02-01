package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoTextModule;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoTextModuleDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoTextModule> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoTextModule.class);
		super.onSetUpAfterDataGeneration();
	}

}
