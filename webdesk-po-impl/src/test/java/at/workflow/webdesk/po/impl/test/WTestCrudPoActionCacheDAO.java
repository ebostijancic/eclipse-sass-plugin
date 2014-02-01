package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoActionCache;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 18.10.2010
 */
public class WTestCrudPoActionCacheDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoActionCache> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoActionCache.class);
		super.onSetUpAfterDataGeneration();
	}
	
}
