package at.workflow.webdesk.po.impl.adminactions;

import java.util.List;

import org.junit.Test;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.PoSearchService;
import at.workflow.webdesk.po.impl.helper.PoDataGenerator;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.tools.DaoJdbcUtil;
import at.workflow.webdesk.tools.testing.AbstractAutoCommitSpringHsqlDbTestCase;
import at.workflow.webdesk.tools.testing.DataGenerator;

public class WTestRefreshLuceneSearchIndex extends
		AbstractAutoCommitSpringHsqlDbTestCase {

	private static final String PATH_TO_DATA = "at/workflow/webdesk/po/impl/"
			+ "adminactions/WTestRefreshLuceneSearchIndex.xml";
	
	private List<PoGroup> groups ;

	private PoSearchService poSearchIndexService;
	
	@Override
	protected void onSetUp() throws Exception {
		super.onSetUp();
		poSearchIndexService =
				(PoSearchService) WebdeskApplicationContext.getBean("PoSearchService");
		DataGenerator dataGenerator = new PoDataGenerator(PATH_TO_DATA, false);
		dataGenerator.create(applicationContext);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testRefreshIndexing() throws Exception {
		groups = (List<PoGroup>) poSearchIndexService.search("G", new Class[]{PoGroup.class});
		assertTrue(groups.size() > 0);

		CleanLuceneSearchIndex adminCleanAction = new CleanLuceneSearchIndex();
		adminCleanAction.run();

		groups = (List<PoGroup>) poSearchIndexService.search("G", new Class[]{PoGroup.class});
		assertTrue(groups.size() == 0);
		
		RefreshLuceneSearchIndex adminAction = new RefreshLuceneSearchIndex();
		adminAction.run();

		groups = (List<PoGroup>) poSearchIndexService.search("G", new Class[]{PoGroup.class});
		
		assertTrue(groups.size() > 0);
	}

	@Override
	protected void clearUsedTables() {
		DaoJdbcUtil daoJdbcUtil = (DaoJdbcUtil) this.applicationContext.getBean("DaoJdbcUtil");
		daoJdbcUtil.execute("delete from PoPersonGroup", DaoJdbcUtil.DATASOURCE_WEBDESK);
		daoJdbcUtil.execute("delete from PoPassword", DaoJdbcUtil.DATASOURCE_WEBDESK);
		daoJdbcUtil.execute("delete from PoPerson", DaoJdbcUtil.DATASOURCE_WEBDESK);
		daoJdbcUtil.execute("delete from PoParentGroup", DaoJdbcUtil.DATASOURCE_WEBDESK);
		daoJdbcUtil.execute("delete from PoGroup", DaoJdbcUtil.DATASOURCE_WEBDESK);
		daoJdbcUtil.execute("delete from PoOrgStructure", DaoJdbcUtil.DATASOURCE_WEBDESK);
		daoJdbcUtil.execute("delete from PoClient", DaoJdbcUtil.DATASOURCE_WEBDESK);
	}

	/** @return true to enable lazy loading of references to other persistent objects. */
	@Override
	protected boolean createAndKeepOpenPersistenceSession() {
		return true;
	}


}
