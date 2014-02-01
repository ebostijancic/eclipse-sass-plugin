package at.workflow.webdesk.po.impl.adminactions;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.PoSearchService;
import at.workflow.webdesk.po.SpecificAdminAction;

/**
 * The refresh of the Lucene search index is done using the suggested 
 * efficient indexing in Section 6.2 of:
 * {@link http://docs.jboss.org/hibernate/search/3.1/reference/en/html_single/#search-batchindex-indexing}
 * @author iaranibar 04.12.2013
 */
public class RefreshLuceneSearchIndex implements SpecificAdminAction {
	@Override
	public void run() {
		PoSearchService poSearchIndexService =
				(PoSearchService) WebdeskApplicationContext.getBean("PoSearchService");
		poSearchIndexService.refreshSearchIndex();
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
