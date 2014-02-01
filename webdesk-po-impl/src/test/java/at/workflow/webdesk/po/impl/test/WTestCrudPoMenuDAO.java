package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoMenuItem;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoMenuDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoMenuItem> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoMenuItem.class);
		super.onSetUpAfterDataGeneration();
	}

	@Override
	protected String getDaoName(Class<?> clazz) {
		if (PoMenuItem.class.isAssignableFrom(clazz))
			return "PoMenuDAO";
		
		return super.getDaoName(clazz);
	}
}
