package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoRoleDeputy;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoRoleDeputyDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoRoleDeputy> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoRoleDeputy.class);
		super.onSetUpAfterDataGeneration();
	}

}
