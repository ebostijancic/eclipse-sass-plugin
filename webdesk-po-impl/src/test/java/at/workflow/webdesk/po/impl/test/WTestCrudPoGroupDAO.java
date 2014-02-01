package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoGroupDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoGroup> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoGroup.class);
		super.onSetUpAfterDataGeneration();
	}

}
