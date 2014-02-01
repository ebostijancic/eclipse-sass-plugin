package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoRoleCompetenceBase;
import at.workflow.webdesk.po.model.PoRoleHolderGroup;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoRoleHolderGroupDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoRoleHolderGroup> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoRoleHolderGroup.class);
		super.onSetUpAfterDataGeneration();
	}

	@Override
	protected String getDaoName(Class<?> clazz) {
		if (PoRoleCompetenceBase.class.isAssignableFrom(clazz))
			return "PoRoleCompetenceDAO";
		
		return super.getDaoName(clazz);
	}

}
