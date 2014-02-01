package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoRoleDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoRole> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoRole.class);
		super.onSetUpAfterDataGeneration();
	}

}
