package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoRoleCompetenceBase;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoRoleCompetenceDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoRoleCompetenceBase> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoRoleCompetenceBase.class);
		super.onSetUpAfterDataGeneration();
	}

	@Override
	protected String getDaoName(Class<?> clazz) {
		if (PoRoleCompetenceBase.class.isAssignableFrom(clazz))
			return "PoRoleCompetenceDAO";
		
		return super.getDaoName(clazz);
	}
}
