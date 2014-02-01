package at.workflow.webdesk.po.impl.adminactions;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import at.workflow.webdesk.po.impl.test.nontransactional.WTestSearchService;

@RunWith(Suite.class)
@SuiteClasses({
		WTestCleanLuceneIndex.class,
		WTestRefreshLuceneSearchIndex.class,
		WTestSearchService.class})
public class AllLuceneRelatedTests {

}
