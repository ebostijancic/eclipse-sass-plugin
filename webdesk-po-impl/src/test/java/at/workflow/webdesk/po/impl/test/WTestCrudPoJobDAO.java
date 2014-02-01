package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoJob;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoJobDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoJob> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoJob.class);
		super.onSetUpAfterDataGeneration();
	}

}
