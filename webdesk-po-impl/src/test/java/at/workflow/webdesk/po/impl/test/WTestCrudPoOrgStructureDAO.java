package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoOrgStructure;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoOrgStructureDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoOrgStructure> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoOrgStructure.class);
		super.onSetUpAfterDataGeneration();
	}

	/** Overridden to force a value for "client" property in PoOrgStructure. */
	@Override
	protected boolean shouldLeaveOutWhenNullable(Class<? extends PersistentObject> parentType, String propertyName) {
		if (parentType == PoOrgStructure.class && propertyName.equals("client"))
			return false;
		return super.shouldLeaveOutWhenNullable(parentType, propertyName);
	}
}
