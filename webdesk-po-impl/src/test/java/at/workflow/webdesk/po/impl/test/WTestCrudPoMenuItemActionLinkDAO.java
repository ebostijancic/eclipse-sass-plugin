package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoMenuItemActionLink;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

public class WTestCrudPoMenuItemActionLinkDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoMenuItemActionLink>{
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoMenuItemActionLink.class);
		super.onSetUpAfterDataGeneration();
	}
}
