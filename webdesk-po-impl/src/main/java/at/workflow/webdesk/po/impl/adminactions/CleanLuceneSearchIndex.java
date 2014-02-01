package at.workflow.webdesk.po.impl.adminactions;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.PoSearchService;
import at.workflow.webdesk.po.SpecificAdminAction;

public class CleanLuceneSearchIndex implements SpecificAdminAction {

	@Override
	public void run() {
		PoSearchService poSearchIndexService =
				(PoSearchService) WebdeskApplicationContext.getBean("PoSearchService");
		poSearchIndexService.cleanSearchIndex();
	}

	@Override
	public String getI18nKey() {
		return getClass().getSimpleName() + "_caption";
	}

	@Override
	public String getErrorMessage() {
		return getClass().getSimpleName() + "_error";
	}

	@Override
	public String getSuccessMessage() {
		return getClass().getSimpleName() + "_success";
	}
}
