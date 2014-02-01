package at.workflow.webdesk.po.impl.test.nontransactional;

import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.PoSearchService;
import at.workflow.webdesk.po.impl.PoSearchServiceImpl;
import at.workflow.webdesk.po.impl.helper.PoDataGenerator;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.tools.DaoJdbcUtil;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.testing.AbstractAutoCommitSpringHsqlDbTestCase;
import at.workflow.webdesk.tools.testing.DataGenerator;

/**
 * Please take into account that this test is data dependent, namely
 * the assertions depend a lot on the data defined in the xml file.
 * @author iaranibar December 2013
 */
public class WTestSearchService extends AbstractAutoCommitSpringHsqlDbTestCase {

	private static final String PATH_TO_DATA = "at/workflow/webdesk/po/impl/test/data/WTestSearchService.xml";
	
	private PoSearchService poSearchIndexService;

	// configurations

	/** @return true to enable lazy loading of references to other persistent objects. */
	@Override
	protected boolean createAndKeepOpenPersistenceSession() {
		return true;
	}

	@Override
	protected void onSetUp() throws Exception {
		super.onSetUp();
		DataGenerator dataGenerator = new PoDataGenerator(PATH_TO_DATA, false);
		dataGenerator.create(applicationContext);
		poSearchIndexService =
				(PoSearchService) WebdeskApplicationContext.getBean("PoSearchService");

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

	// test methods

	@SuppressWarnings("unchecked")
	public void testSearchPerson() throws Exception {

		List<? extends PersistentObject> result =
				poSearchIndexService.search("wef", new Class[]{PoPerson.class});
		assertEquals(1, result.size());

		final String FILTER1 = "an";
		result = poSearchIndexService.search(FILTER1, new Class[]{PoPerson.class});
		assertEquals(39, result.size());

		final String FILTER2 = "og";
		result = poSearchIndexService.search(FILTER2, new Class[]{PoPerson.class});
		assertEquals(4, result.size());

		// test PoGroup ...
		final String FILTER = "Beratung";

		result = poSearchIndexService.search(FILTER, new Class[]{PoGroup.class});
		assertTrue(8 == result.size());

	}

	/**
	 * This test shows that the {@link MultiFieldQueryParser}, that is used 
	 * for the search in the {@link PoSearchServiceImpl#search(String, Class[])}
	 * needs that {@link StandardAnalyzer} not to be null
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void testShouldThrowExceptionWhenSearchWithUnderLineAndSpace() throws Exception {
		final String searchPhrase= "Doe_ John_ ";
		poSearchIndexService.search(searchPhrase, new Class[]{PoPerson.class});
	}
	
	/**
	 * TODO: for some reason this does not work, Lucene accepts everything as search phrase!
	 * 
	public void _testWrongSearchPhrase() throws Exception {
		try	{
			List<? extends PersistentObject> result = poSearchIndexService.search("\\0001", new Class[] { PoPerson.class });
			fail("Lucene should not accept control chars!");
		}
		catch (Exception e)	{
			// this is expected here
		}
	}
	 */

}
