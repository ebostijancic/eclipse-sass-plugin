package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoKeyValueType;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoKeyValueTypeDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoKeyValueType> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoKeyValueType.class);
		super.onSetUpAfterDataGeneration();
	}

}
