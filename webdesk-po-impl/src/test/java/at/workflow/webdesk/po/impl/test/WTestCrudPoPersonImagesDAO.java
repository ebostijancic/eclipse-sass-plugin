package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.model.PoPersonImages;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author sdzuban 11.02.2013
 */
public class WTestCrudPoPersonImagesDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoPersonImages> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoPersonImages.class);
		super.onSetUpAfterDataGeneration();
	}

}
