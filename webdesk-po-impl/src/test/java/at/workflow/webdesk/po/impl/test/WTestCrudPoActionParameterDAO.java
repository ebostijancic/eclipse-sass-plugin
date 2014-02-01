package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoActionParameter;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoActionParameterDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoActionParameter> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoActionParameter.class);
		super.onSetUpAfterDataGeneration();
	}

}
