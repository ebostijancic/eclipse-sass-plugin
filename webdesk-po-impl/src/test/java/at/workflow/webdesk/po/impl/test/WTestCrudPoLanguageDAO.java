package at.workflow.webdesk.po.impl.test;

import at.workflow.webdesk.po.PoConstants;
import at.workflow.webdesk.po.PoLanguageService;
import at.workflow.webdesk.po.model.PoLanguage;
import at.workflow.webdesk.tools.testing.AbstractRandomDataAndReferencesProvidingTestCase;

/**
 * @author fritzberger 23.11.2010
 */
public class WTestCrudPoLanguageDAO extends AbstractRandomDataAndReferencesProvidingTestCase<PoLanguage> {

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		setBeanClass(PoLanguage.class);
		((PoLanguageService) getBean("PoLanguageService")).init();
		
		super.onSetUpAfterDataGeneration();
		
		// fri_2011-04-04 fixing no default languages
		((PoLanguageService) getBean("PoLanguageService")).init();
	}

	/** fri_2011-03-30: Overridden to return 4 because since 10.2.2011 there are already two languages at startup, inserted via Spring. */
	@Override
	protected int loadAllTestResultSize() {
		return super.loadAllTestResultSize() + PoConstants.getStandardLanguagesMap().size();
	}

}
