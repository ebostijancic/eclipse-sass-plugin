package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoPersonBankAccount;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoPersonBankAccountDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoPersonBankAccount> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoPersonBankAccount.class);
		super.onSetUpAfterDataGeneration();
	}

}