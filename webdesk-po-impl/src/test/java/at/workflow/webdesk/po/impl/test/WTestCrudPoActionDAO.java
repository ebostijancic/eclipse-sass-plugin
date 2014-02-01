package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 18.10.2010
 */
public class WTestCrudPoActionDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoAction> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoAction.class);
		super.onSetUpAfterDataGeneration();
	}

}
