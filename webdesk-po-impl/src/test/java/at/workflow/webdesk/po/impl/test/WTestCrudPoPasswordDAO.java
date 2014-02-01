package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoPassword;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 18.10.2010
 */
public class WTestCrudPoPasswordDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoPassword> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoPassword.class);
		super.onSetUpAfterDataGeneration();
	}

}
