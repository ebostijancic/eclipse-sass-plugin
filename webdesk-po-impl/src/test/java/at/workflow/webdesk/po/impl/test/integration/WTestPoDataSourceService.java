package at.workflow.webdesk.po.impl.test.integration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import at.workflow.webdesk.po.PoDataSourceService;
import at.workflow.webdesk.po.impl.helper.PoDataGenerator;
import at.workflow.webdesk.po.model.PoDataSourceDefinition;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;
import at.workflow.webdesk.tools.testing.DataGenerator;


/**
 * Unit-Tests for the PoDataSourceService
 * 
 * @author sdzuban
 */
public class WTestPoDataSourceService extends AbstractTransactionalSpringHsqlDbTestCase {

	private static final String TEST_DELETE = "testDelete";
	private static final String TEST_SAVE = "testSave";
	
	private static PoDataSourceService  dataSourceService;

	@Override
	protected final DataGenerator[] getDataGenerators() {
		return new DataGenerator[] { new PoDataGenerator("at/workflow/webdesk/po/impl/test/data/MinTestData.xml", false) };
	}

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		logger.info("onSetUpAfterDataGeneration()");

		super.onSetUpAfterDataGeneration();
		
		dataSourceService = (PoDataSourceService) getBean("PoDataSourceService");
	}
	
	
	public void testDataSourceAfterStart() throws SQLException {

		assertNull(dataSourceService.getDataSourceDefinition("egalWas"));
		
		assertNull(dataSourceService.getDataSource("egalWas"));
		
		final PoDataSourceDefinition webdeskDS = dataSourceService.findDataSourceDefinitionByName(PoDataSourceService.WEBDESK);
		assertNotNull(webdeskDS);
		assertEquals(PoDataSourceService.WEBDESK, webdeskDS.getName());
		assertEquals("org.hsqldb.jdbcDriver", webdeskDS.getDriver());
		final String webdeskURL = webdeskDS.getUrl();
		assertNotNull(webdeskURL);
		assertTrue(webdeskURL.startsWith("jdbc:hsqldb:file:"));
		assertTrue(webdeskURL.endsWith("/webdesk"));
		assertEquals("sa", webdeskDS.getUserName());
		assertEquals("", webdeskDS.getPassword());

		final PoDataSourceDefinition sharkDS = dataSourceService.findDataSourceDefinitionByName(PoDataSourceService.SHARK);
		assertNull(sharkDS);
//		assertEquals(PoDataSourceService.SHARK, sharkDS.getName());
//		assertEquals("org.hsqldb.jdbcDriver", sharkDS.getDriver());
//		final String sharkURL = sharkDS.getUrl();
//		assertTrue(sharkURL.startsWith("jdbc:hsqldb:file:"));
//		assertTrue(sharkURL.endsWith("/shark"));
//		assertEquals("sa", sharkDS.getUserName());
//		assertEquals("", sharkDS.getPassword());
		
		assertNotNull(dataSourceService.getDataSource(PoDataSourceService.WEBDESK));
		Connection connection = dataSourceService.getDataSource(PoDataSourceService.WEBDESK).getConnection();
		assertNotNull(connection);
		connection.close();
		
		assertNotNull(dataSourceService.loadAllDataSourceDefinitions());
		assertTrue(dataSourceService.loadAllDataSourceDefinitions().isEmpty());
		assertEquals(1, dataSourceService.loadAllDataSourceNames().size());
		assertEquals(PoDataSourceService.WEBDESK, dataSourceService.loadAllDataSourceNames().get(0));
	}
	
	public void testNewDataSource() throws Exception {

		PoDataSourceDefinition def = new PoDataSourceDefinition();
		
		def.setName("name");
		def.setDriver("driver");
		def.setUrl("url");
		def.setUserName("username");
		def.setPassword("password");
		def.setMaxActive(20);
		def.setMaxIdle(10);
		
		assertEquals(0, dataSourceService.loadAllDataSourceDefinitions().size());
		
		dataSourceService.saveDataSourceDefinition(def);
		assertNotNull(def.getUID());
		assertEquals(1, dataSourceService.loadAllDataSourceDefinitions().size());
		PoDataSourceDefinition fromDb = dataSourceService.getDataSourceDefinition(def.getUID());
		assertNotNull(fromDb);
		assertEquals("name", fromDb.getName());
		assertEquals("driver", fromDb.getDriver());
		assertEquals("url", fromDb.getUrl());
		assertEquals("username", fromDb.getUserName());
		assertEquals("password", fromDb.getPassword());
		
		List<String> names = dataSourceService.loadAllDataSourceNames();
		assertEquals(2, names.size());
		assertEquals("name", names.get(0));
		assertEquals(PoDataSourceService.WEBDESK, names.get(1));
		
		DataSource ds = dataSourceService.getDataSource("name");
		assertNotNull(ds);
		try {
			ds.getConnection();
			fail();
		} catch (Exception e) { 
			// JDBC driver 'driver' cannot be loaded
		}
		
		ds = dataSourceService.getDataSource(PoDataSourceService.WEBDESK);
		assertNotNull(ds);
		Connection connection = ds.getConnection();
		assertNotNull(connection);
		connection.close();
		
		dataSourceService.deleteDataSourceDefinition(def);
		assertTrue(dataSourceService.loadAllDataSourceDefinitions().isEmpty());
		assertNull(dataSourceService.getDataSource("name"));
		assertEquals(1, dataSourceService.loadAllDataSourceNames().size());
		assertEquals(PoDataSourceService.WEBDESK, dataSourceService.loadAllDataSourceNames().get(0));
	}

	public void testJdbcDataSourceRetrieval() {

		assertNotNull(dataSourceService.getDataSource(PoDataSourceService.WEBDESK));
		DataSource dataSource = dataSourceService.getDataSource(PoDataSourceService.WEBDESK);
		assertNotNull(dataSource);
		assertTrue(checkTheDataSource(dataSource));
	}
	
	public void testRejectSharkAndWebdeskName() {
		
		PoDataSourceDefinition def = new PoDataSourceDefinition();
		def.setName(PoDataSourceService.SHARK);
		def.setDriver("driver");
		def.setUrl("url");
		def.setUserName("username");
		def.setPassword("password");
		def.setMaxActive(20);
		def.setMaxIdle(10);

		try {
			dataSourceService.saveDataSourceDefinition(def);
			fail(PoDataSourceService.SHARK + " allowed as name");
		} catch (Exception e) { }

		def.setName(PoDataSourceService.WEBDESK);
		
		try {
			dataSourceService.saveDataSourceDefinition(def);
			fail(PoDataSourceService.WEBDESK + " allowed as name");
		} catch (Exception e) { }
	}
	
	public void testRemovalFromMapsOnDelete() {
	
		PoDataSourceDefinition def = new PoDataSourceDefinition();
		def.setName(TEST_DELETE);
		def.setDriver("driver");
		def.setUrl("url");
		def.setUserName("username");
		def.setPassword("password");
		def.setMaxActive(20);
		def.setMaxIdle(10);

		dataSourceService.saveDataSourceDefinition(def);
		
		assertNotNull(dataSourceService.getDataSource(TEST_DELETE));
		
		dataSourceService.deleteDataSourceDefinition(def);

		assertNull(dataSourceService.getDataSource(TEST_DELETE));
		
	}
	
	public void testRemovalFromMapsOnSave() {
		
		PoDataSourceDefinition def = new PoDataSourceDefinition();
		def.setName(TEST_SAVE);
		def.setDriver("driver");
		def.setUrl("url");
		def.setUserName("username");
		def.setPassword("password");
		def.setMaxActive(20);
		def.setMaxIdle(10);

		dataSourceService.saveDataSourceDefinition(def);
		
		final DataSource dataSource = dataSourceService.getDataSource(TEST_SAVE);
		assertNotNull(dataSource);
		
		def.setUserName("username2");
		dataSourceService.saveDataSourceDefinition(def);

		// now a new instances must be returned
		final DataSource dataSource2 = dataSourceService.getDataSource(TEST_SAVE);
		assertNotNull(dataSource2);
		assertTrue(dataSource2 != dataSource);
		
		// clear db
		dataSourceService.deleteDataSourceDefinition(def);
	}

	public void testCaching() {
		
		PoDataSourceDefinition def = new PoDataSourceDefinition();
		def.setName(TEST_SAVE);
		def.setDriver("driver");
		def.setUrl("url");
		def.setUserName("username");
		def.setPassword("password");
		def.setMaxActive(20);
		def.setMaxIdle(10);
		
		dataSourceService.saveDataSourceDefinition(def);
		
		final DataSource dataSource = dataSourceService.getDataSource(TEST_SAVE);
		assertNotNull(dataSource);
		
		// now the same instances must be returned
		final DataSource dataSource2 = dataSourceService.getDataSource(TEST_SAVE);
		assertNotNull(dataSource2);
		assertTrue(dataSource2 == dataSource);
		
		// clear db
		dataSourceService.deleteDataSourceDefinition(def);
	}
	
	public void testGetDataSource() {
		
		DataSource ds = dataSourceService.getDataSource("nonsense", "nonsense", "nonsense", "nonsense", 1, 1);
		assertNull(ds);
		
		ds = dataSourceService.getDataSource("", "jdbc:mysql://webdesk3/webdesk", "wdadm", "wdadm", 1, 1);
		assertNull(ds);
		
		ds = dataSourceService.getDataSource("com.mysql.jdbc.Driver", "jdbc:mysql://webdesk3/webdesk", "wdadm", "wdadm", 1, 1);
		assertNotNull(ds);
		assertTrue(checkTheDataSource(ds));
	}

	// checks that the connection is real
	private boolean checkTheDataSource(DataSource dataSource) {
		try {
			Connection conn = dataSource.getConnection();
			conn.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
