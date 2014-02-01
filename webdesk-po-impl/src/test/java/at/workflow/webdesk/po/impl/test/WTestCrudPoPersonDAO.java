package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoPersonDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoPerson> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoPerson.class);
		super.onSetUpAfterDataGeneration();
	}

}
