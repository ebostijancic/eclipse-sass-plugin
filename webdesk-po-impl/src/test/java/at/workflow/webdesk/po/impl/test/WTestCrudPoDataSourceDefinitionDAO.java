package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoDataSourceDefinition;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoDataSourceDefinitionDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoDataSourceDefinition> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoDataSourceDefinition.class);
		super.onSetUpAfterDataGeneration();
	}

}
