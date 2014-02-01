package at.workflow.webdesk.po.impl.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.sql.DataSource;

import at.workflow.webdesk.po.PoDataSourceService;
import at.workflow.webdesk.po.PoGeneralSqlService;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.impl.helper.PoDataGenerator;
import at.workflow.webdesk.po.model.PoBeanPropertyValue;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.tools.PaginableQuery;
import at.workflow.webdesk.tools.PositionalQuery;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;
import at.workflow.webdesk.tools.testing.DataGenerator;

/**
 * @author sdzuban 07.05.2012
 */
public class WTestPoGeneralSqlService extends AbstractTransactionalSpringHsqlDbTestCase {

	private static final String DESCRIPTION = "description";
	private static final String ACTIVE_USER = "activeUser";
	private static final String TOP_LEVEL = "isTopLevel";
	private static final String NAME = "name";
	private static final String SHORT_NAME = "shortName";
	private static final String GROUP_PREFIX = "gr";
	private static final String USERNAME_PREFIX = "un";
	private static final String EMPLOYEEID_PREFIX = "eid";
	private static final String VALIDFROM = "validfrom";
	private static final String VALIDTO = "validto";
	private static final String ORGSTRUCTURE_UID = "ORGSTRUCTURE_UID";
	private static final String CLIENT_UID = "CLIENT_UID";
	private static final String GROUP_UID = "GROUP_UID";
	
	
	private PoGeneralSqlService sqlService;
	private PoOrganisationService orgService;
	private PoDataSourceService dataSourceService;
	private DataSource webdeskDataSource;

	@Override
	protected final DataGenerator[] getDataGenerators() {
		return new DataGenerator[] { new PoDataGenerator("at/workflow/webdesk/po/impl/test/data/MinTestData.xml", false) };
	}

	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		
		sqlService = (PoGeneralSqlService) getBean("PoGeneralSqlService");
		orgService = (PoOrganisationService) getBean("PoOrganisationService");
		dataSourceService = (PoDataSourceService) getBean("PoDataSourceService");
		webdeskDataSource = dataSourceService.getDataSource(PoDataSourceService.WEBDESK);
	}
	
//  ------------------------- QUERY STATEMENT TESTS ---------------------------------	
	
	public void testWrongQueryStatements() {
		
		String sql = "insert into poclient values('UID', 'name', 'description', 'shortName', 'gr', 'un' 'eid')";
		PaginableQuery query = new PaginableQuery(sql);
		try {
			sqlService.select(webdeskDataSource, query);
			fail("INSERT statement not rejected");
		} catch (Exception e) {
			
		}
		
		sql = "update poclient set name = 'name2' where UID = 'UID'";
		query = new PaginableQuery(sql);
		try {
			sqlService.select(webdeskDataSource, query);
			fail("UPDATE statement not rejected");
		} catch (Exception e) {
			
		}
		
		sql = "delete from poclient where uid = 'UID'";
		query = new PaginableQuery(sql);
		try {
			sqlService.select(webdeskDataSource, query);
			fail("DELETE statement not rejected");
		} catch (Exception e) {
			
		}
	}
	
	public void testSimpleReadOut() {
		
		String sql = "select lastName, firstName from poperson";
		PaginableQuery query = new PaginableQuery(sql);
		List<Map<String, ?>> result = sqlService.select(webdeskDataSource, query);
		assertNotNull(result);
		assertEquals(orgService.loadAllPersons().size(), result.size());
		
	}

	public void testSimpleReadOutWithObjects() {
		
		String sql = "select lastName, firstName from poperson";
		PaginableQuery query = new PaginableQuery(sql);
		List<Map<String, Object>> result = sqlService.selectRecords(webdeskDataSource, query);
		assertNotNull(result);
		assertEquals(orgService.loadAllPersons().size(), result.size());
		
	}
	
	public void testPagination() {
		
		String sql = "select lastName, firstName from poperson order by lastName";
		PaginableQuery query = new PaginableQuery(sql);
		List<Map<String, ?>> result = sqlService.select(webdeskDataSource, query);
		assertNotNull(result);
		assertTrue(result.size() > 3);
		
		// remember selected names
		String lastName1 = (String) result.get(1).get("lastName");
		String lastName2 = (String) result.get(2).get("lastName");
		// remember first name on 2nd page
		String lastName21 = (String) result.get(3).get("lastName");
		
		query.setFirstResult(1);
		query.setMaxResults(2);
		result = sqlService.select(webdeskDataSource, query);
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(lastName1, result.get(0).get("lastName"));
		assertEquals(lastName2, result.get(1).get("lastName"));
		
		query.setFirstResult(2 + 1);
		query.setMaxResults(2);
		result = sqlService.select(webdeskDataSource, query);
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(lastName21, result.get(0).get("lastName"));
	}
	
	public void testReadOutWithWhere() {
		
		String sql = "select lastName, firstName from poperson where lastName = 'Weiss'";
		PaginableQuery query = new PaginableQuery(sql);
		List<Map<String, ?>> result = sqlService.select(webdeskDataSource, query);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(2, result.get(0).size());
		assertEquals("Weiss", result.get(0).get("lastName"));
		assertEquals("Florian", result.get(0).get("firstName"));
	}
	
	public void testReadOutWithWhereAndParam() {
		
		String sql = "select lastName, firstName from poperson where lastName = ?";
		Object[] paramValues = new Object[] {"Weiss"};
		PaginableQuery query = new PositionalQuery(sql, paramValues);
		List<Map<String, ?>> result = sqlService.select(webdeskDataSource, query);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(2, result.get(0).size());
		assertEquals("Weiss", result.get(0).get("lastName"));
		assertEquals("Florian", result.get(0).get("firstName"));
	}
	
	public void testReadOutWithWhereAndBoolean() {
		
		String sql = "select lastName, firstName from poperson where activeUser = ?";
		
		Object[] paramValues = new Object[] {true};
		PaginableQuery query = new PositionalQuery(sql, paramValues);
		List<Map<String, ?>> result = sqlService.select(webdeskDataSource, query);
		assertNotNull(result);
		assertEquals(orgService.findAllPersons(new Date(), true).size(), result.size());

		paramValues = new Object[] {false};
		query = new PositionalQuery(sql, paramValues);
		result = sqlService.select(webdeskDataSource, query);
		assertNotNull(result);
		assertEquals(orgService.findAllPersons(new Date(), false).size(), result.size());
	}
	
	public void testReadOutWithWhereAndDate() {
		
		String sql = "select lastName, firstName from poperson where validfrom > ?";
		
		Object[] paramValues = new Object[] {DateTools.yesterday()};
		PaginableQuery query = new PositionalQuery(sql, paramValues);
		List<Map<String, ?>> result = sqlService.select(webdeskDataSource, query);
		assertNotNull(result);
		assertEquals(orgService.loadAllPersons().size(), result.size());
		
		sql = "select lastName, firstName from poperson where validfrom < ?";
		
		paramValues = new Object[] {DateTools.yesterday()};
		query = new PositionalQuery(sql, paramValues);
		result = sqlService.select(webdeskDataSource, query);
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	public void testReadOutWithWhereAndInteger() {
		
		String sql = "select lastName, firstName from poperson group by lastName, firstName having count(firstName) = ?";
		
		Object[] paramValues = new Object[] {1};
		PaginableQuery query = new PositionalQuery(sql, paramValues);
		List<Map<String, ?>> result = sqlService.select(webdeskDataSource, query);
		assertNotNull(result);
		assertEquals(orgService.loadAllPersons().size(), result.size());

		paramValues = new Object[] {10};
		query = new PositionalQuery(sql, paramValues);
		result = sqlService.select(webdeskDataSource, query);
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

//	---------------------------------- UPDATE TESTS ---------------------------------------------
	
	public void testWrongUpdateStatements() {
		
		String sql = "select name, description, shortName from poclient";
		PaginableQuery query = new PaginableQuery(sql);
		try {
			sqlService.execute(webdeskDataSource, query);
			fail("SELECT statement not rejected");
		} catch (Exception e) {
			
		}
	}
		
	public void testUpdateStatements() {
		
		int beforeCount = orgService.loadAllClients().size();
		
		String sql = "insert into poclient values('UID', 'name', 'description', 'shortName', 'gr', 'un', 'eid')";
		PaginableQuery query = new PaginableQuery(sql);
		assertEquals(1, sqlService.execute(webdeskDataSource, query));
		assertEquals(beforeCount + 1, orgService.loadAllClients().size());
		
		sql = "update poclient set name = 'name2' where name = 'name'";
		query = new PaginableQuery(sql);
		assertEquals(1, sqlService.execute(webdeskDataSource, query));
		assertEquals(beforeCount + 1, orgService.loadAllClients().size());
		
		sql = "delete from poclient where name = 'name2'";
		query = new PaginableQuery(sql);
		assertEquals(1, sqlService.execute(webdeskDataSource, query));
		assertEquals(beforeCount, orgService.loadAllClients().size());
	}

	public void testUpdatePreparedStatements() {
		
		int beforeCount = orgService.loadAllClients().size();
		
		String sql = "insert into poclient values(?, ?, ?, ?, ?, ?, ?)";
		Object[] paramValues = new Object[] {"UID", NAME, DESCRIPTION, SHORT_NAME, GROUP_PREFIX, USERNAME_PREFIX, EMPLOYEEID_PREFIX};
		PositionalQuery query = new PositionalQuery(sql,paramValues);
		assertEquals(1, sqlService.execute(webdeskDataSource, query));
		assertEquals(beforeCount + 1, orgService.loadAllClients().size());
		
		sql = "update poclient set name = 'name2' where name = ?";
		paramValues = new Object[] {NAME};
		query = new PositionalQuery(sql, paramValues);
		assertEquals(1, sqlService.execute(webdeskDataSource, query));
		assertEquals(beforeCount + 1, orgService.loadAllClients().size());
		
		sql = "delete from poclient where name = ?";
		paramValues = new Object[] {"name2"};
		query = new PositionalQuery(sql, paramValues);
		assertEquals(1, sqlService.execute(webdeskDataSource, query));
		assertEquals(beforeCount, orgService.loadAllClients().size());
	}
	
//	------------------------- SIMPLE INSERT --------------------------------------------
	
	public void testSimpleInsert() {
		
		int beforeCount = orgService.loadAllClients().size();
		
		Map<String, Object> namedValues = getClient(1);
		
		int result = sqlService.insert(webdeskDataSource, "poclient", namedValues);
		assertEquals(1, result);
		
		assertEquals(beforeCount + 1, orgService.loadAllClients().size());
		List<Map<String, ?>> fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from poclient where client_uid = 'UID1'"));
		checkClient(1, fromDb);
		
		namedValues = new TreeMap<String, Object>(); // different key order than hash map to demonstrate indepencency on it
		namedValues.put(CLIENT_UID, "UID2"); // UID is not null column and must be supplied
		namedValues.put(SHORT_NAME, "shortName2");
//		namedValues.put("description", "description2"); left intentionally blank to test it
		namedValues.put(NAME, "name2");
		
		result = sqlService.insert(webdeskDataSource, "poclient", namedValues);
		assertEquals(1, result);
		
		assertEquals(beforeCount + 2, orgService.loadAllClients().size());
		
		fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from poclient where client_uid = 'UID2'"));
		assertNotNull(fromDb);
		assertEquals(1, fromDb.size());
		Map<String, ?> first = fromDb.get(0);
		assertEquals("shortName2", first.get(SHORT_NAME));
		assertEquals("name2", first.get(NAME));
		assertNull(first.get(DESCRIPTION));
	}
	
//	------------------------- SIMPLE UPDATE --------------------------------------------
	
	public void testSimpleUpdate() {
		
		int beforeCount = orgService.loadAllClients().size();
		
		Map<String, Object> namedValues = getClient(1);
		int result = sqlService.insert(webdeskDataSource, "poclient", namedValues);
		assertEquals(1, result);
		
		assertEquals(beforeCount + 1, orgService.loadAllClients().size());
		List<Map<String, ?>> fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from poclient where client_uid = 'UID1'"));
		checkClient(1, fromDb);
		
		namedValues = new TreeMap<String, Object>(); // different key order than hash map
		namedValues.put(CLIENT_UID, "UID1"); // UID is a not null column and must be supplied
		namedValues.put(SHORT_NAME, "shortName2");
		namedValues.put(NAME, "name2");
		
		result = sqlService.update(webdeskDataSource, "poclient", CLIENT_UID, namedValues);
		assertEquals(1, result);
		
		assertEquals(beforeCount + 1, orgService.loadAllClients().size()); // no new record
		
		fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from poclient where client_uid = 'UID1'"));
		assertNotNull(fromDb);
		assertEquals(1, fromDb.size());
		Map<String, ?> first = fromDb.get(0);
		assertEquals("shortName2", first.get(SHORT_NAME));
		assertEquals("name2", first.get(NAME));
		assertEquals("description1", first.get(DESCRIPTION)); // remained unchanged
	}
	
//	------------------------- SIMPLE INSERT OR UPDATE --------------------------------------------
	
	public void testSimpleInsertOrUpdate() {

		Map<String, Object> namedValues = getClient(1);
		int result = sqlService.insertOrUpdate(webdeskDataSource, "poclient", CLIENT_UID, namedValues);
		assertEquals(1, result);
		
		List<Map<String, ?>> fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from poclient where client_uid = 'UID1'"));
		checkClient(1, fromDb);
		
		namedValues = new TreeMap<String, Object>(); // different key order than hash map
		namedValues.put(CLIENT_UID, "UID1"); // UID is not null column and must be supplied
		namedValues.put(SHORT_NAME, "shortName2");
		namedValues.put(NAME, "name2");
		
		result = sqlService.insertOrUpdate(webdeskDataSource, "poclient", CLIENT_UID, namedValues);
		assertEquals(1, result);
		
		fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from poclient where client_uid = 'UID1'"));
		assertNotNull(fromDb);
		assertEquals(1, fromDb.size());
		Map<String, ?> first = fromDb.get(0);
		assertEquals("shortName2", first.get(SHORT_NAME));
		assertEquals("name2", first.get(NAME));
		assertEquals("description1", first.get(DESCRIPTION)); // remained unchanged
	}

//	------------------------- SIMPLE DELETE --------------------------------------------
	
	public void testSimpleDelete() {
		
		Map<String, Object> namedValues = getClient(1);
		int result = sqlService.insertOrUpdate(webdeskDataSource, "poclient", CLIENT_UID, namedValues);
		assertEquals(1, result);
		
		List<Map<String, ?>> fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from poclient where client_uid = 'UID1'"));
		checkClient(1, fromDb);
		
		result = sqlService.delete(webdeskDataSource, "poclient", CLIENT_UID, "nonsense");
		assertEquals(0, result);
		fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from poclient where client_uid = 'UID1'"));
		checkClient(1, fromDb);
		
		result = sqlService.delete(webdeskDataSource, "poclient", CLIENT_UID, "UID1");
		assertEquals(1, result);
		fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from poclient where client_uid = 'UID1'"));
		assertNotNull(fromDb);
		assertEquals(0, fromDb.size());
		
	}

//	------------------------- TEST SIMPLE BATCH -----------------------------------------

	public void testBatchUpdateStatements() {
		
		int beforeCount = orgService.loadAllClients().size();
		
		String sql1 = "insert into poclient values('UID1', 'name1', 'description1', 'shortName1', 'gr1', 'un1', 'eid1')";
		String sql2 = "insert into poclient values('UID2', 'name2', 'description2', 'shortName2', 'gr2', 'un2', 'eid2')";
		String sql3 = "insert into poclient values('UID3', 'name3', 'description3', 'shortName3', 'gr3', 'un3', 'eid3')";
		String[] sqls = new String[] {sql1, sql2, sql3};
		int[] result = sqlService.batchExecute(webdeskDataSource, sqls);
		assertEquals(3, result.length);
		assertEquals(1, result[0]);
		assertEquals(1, result[1]);
		assertEquals(1, result[2]);
		assertEquals(beforeCount + 3, orgService.loadAllClients().size());
		
		sqls = new String[] {"update poclient set name = 'name5' where name = 'name1'"};
		result = sqlService.batchExecute(webdeskDataSource, sqls);
		assertEquals(1, result.length);
		assertEquals(1, result[0]);
		assertEquals(beforeCount + 3, orgService.loadAllClients().size());
		
		sql1 = "delete from poclient where name = 'name5'";
		sql2 = "delete from poclient where name = 'name2' or name ='name3'";
		sqls = new String[] {sql1, sql2};
		result = sqlService.batchExecute(webdeskDataSource, sqls);
		assertEquals(2, result.length);
		assertEquals(1, result[0]);
		assertEquals(2, result[1]);
		assertEquals(beforeCount, orgService.loadAllClients().size());
	}

//	------------------------- TEST PREPARED STATEMENT BATCH -----------------------------------------
	
	public void testBatchUpdatePreparedStatements() {
		
		int beforeCount = orgService.loadAllClients().size();
		
		String sql = "insert into poclient values(?, ?, ?, ?, ?, ?, ?)";
		Object[] values1 = new Object[] {"UID1", "name1", "description1", "shortName1", "gr1", "un1", "eid1"};
		Object[] values2 = new Object[] {"UID2", "name2", "description2", "shortName2", "gr2", "un2", "eid2"};
		Object[] values3 = new Object[] {"UID3", "name3", "description3", "shortName3", "gr3", "un3", "eid3"};
		List<Object[]> parameterValues = new ArrayList<Object[]>();
		parameterValues.add(values1);
		parameterValues.add(values2);
		parameterValues.add(values3);
		int[] result = sqlService.batchExecute(webdeskDataSource, sql, parameterValues);
		assertEquals(3, result.length);
		assertEquals(1, result[0]);
		assertEquals(1, result[1]);
		assertEquals(1, result[2]);
		assertEquals(beforeCount + 3, orgService.loadAllClients().size());
		
		sql = "update poclient set name = ? where name = ?";
		values1 = new Object[] {"name5", "name1"};
		parameterValues.clear();
		parameterValues.add(values1);
		result = sqlService.batchExecute(webdeskDataSource, sql, parameterValues);
		assertEquals(1, result.length);
		assertEquals(1, result[0]);
		assertEquals(beforeCount + 3, orgService.loadAllClients().size());
		
		sql = "delete from poclient where name = ?";
		values1 = new Object[] {"name5"}; 
		values2 = new Object[] {"name2"}; 
		values3 = new Object[] {"name3"}; 
		parameterValues.clear();
		parameterValues.add(values1);
		parameterValues.add(values2);
		parameterValues.add(values3);
		result = sqlService.batchExecute(webdeskDataSource, sql, parameterValues);
		assertEquals(3, result.length);
		assertEquals(1, result[0]);
		assertEquals(1, result[1]);
		assertEquals(1, result[2]);
		assertEquals(beforeCount, orgService.loadAllClients().size());
	}
	
//	------------------------- TEST PREPARED STATEMENT BATCH WITH MAPS -----------------------------------------
	
	public void testBatchUpdatePreparedStatementsWithNamedParameters() {
		
		int beforeCount = orgService.loadAllClients().size();
		
		String sql = "insert into poclient values(?, ?, ?, ?, ?, ?, ?)";
		List<String> namesInRightOrder = Arrays.asList(CLIENT_UID, NAME, DESCRIPTION, SHORT_NAME, GROUP_PREFIX, USERNAME_PREFIX, EMPLOYEEID_PREFIX);
		
		Map<String, Object> namedValues1 = getClient(1);
		Map<String, Object> namedValues2 = getClient(2);
		Map<String, Object> namedValues3 = getClient(3);
		List<Map<String, Object>> namedValues = new ArrayList<Map<String, Object>>();
		namedValues.add(namedValues1);
		namedValues.add(namedValues2);
		namedValues.add(namedValues3);
		
		int[] result = sqlService.batchExecute(webdeskDataSource, sql, namesInRightOrder, namedValues);
		assertEquals(3, result.length);
		assertEquals(1, result[0]);
		assertEquals(1, result[1]);
		assertEquals(1, result[2]);
		assertEquals(beforeCount + 3, orgService.loadAllClients().size());
		
		sql = "update poclient set name = ? where name = ?";
		namesInRightOrder = Arrays.asList("col1", "col2");
		
		namedValues1.clear();
		namedValues1.put("col1", "name5");
		namedValues1.put("col2", "name1");
		namedValues.clear();
		namedValues.add(namedValues1);
		result = sqlService.batchExecute(webdeskDataSource, sql, namesInRightOrder, namedValues);
		assertEquals(1, result.length);
		assertEquals(1, result[0]);
		assertEquals(beforeCount + 3, orgService.loadAllClients().size());
		
		sql = "delete from poclient where name = ?";
		namesInRightOrder = Arrays.asList(NAME);
		
		namedValues1.clear();
		namedValues1.put(NAME, "name5"); 
		namedValues2.clear();
		namedValues2.put(NAME, "name2"); 
		namedValues3.clear();
		namedValues3.put(NAME, "name3"); 
		namedValues.clear();
		namedValues.add(namedValues1);
		namedValues.add(namedValues2);
		namedValues.add(namedValues3);
		result = sqlService.batchExecute(webdeskDataSource, sql, namesInRightOrder, namedValues);
		assertEquals(3, result.length);
		assertEquals(1, result[0]);
		assertEquals(1, result[1]);
		assertEquals(1, result[2]);
		assertEquals(beforeCount, orgService.loadAllClients().size());
	}
	
//	------------------------- TEST INSERT BATCH WITH MAPS -----------------------------------------
	
	public void testBatchInsertWithGroupsAndNamedParameters() {
		
		// PoGroup used because it contains strings, boolean and dates
		// so the correct parameter positioning and binding can be tested

		final PoClient client = orgService.loadAllClients().get(0);
		String clientUID = client.getUID();
		String orgStrUID = orgService.loadAllOrgStructures(client).get(0).getUID();
		
		Date from = DateTools.yesterday();
		Date to = DateTools.tomorrow();
		
		Map<String, Object> namedValues1 = new HashMap<String, Object>();
		namedValues1.put(SHORT_NAME, "shortName1");
		namedValues1.put(NAME, "name1");
		namedValues1.put(GROUP_UID, "UID1");
		namedValues1.put(CLIENT_UID, clientUID);
		namedValues1.put(ORGSTRUCTURE_UID, orgStrUID);
		namedValues1.put(VALIDTO, to);
		namedValues1.put(VALIDFROM, from);
		namedValues1.put(TOP_LEVEL, true);
		// different ordering by TreeMap is important for test
		Map<String, Object> namedValues2 = new TreeMap<String, Object>();
		namedValues2.put(GROUP_UID, "UID2");
		namedValues2.put(CLIENT_UID, clientUID);
		namedValues2.put(ORGSTRUCTURE_UID, orgStrUID);
		namedValues2.put(SHORT_NAME, "shortName2");
		namedValues2.put(NAME, "name2");
		namedValues2.put(TOP_LEVEL, false);
		namedValues2.put(VALIDFROM, from);
		namedValues2.put(VALIDTO, to);
		Map<String, Object> namedValues3 = new HashMap<String, Object>();
		namedValues3.put(NAME, "name3");
		namedValues3.put(GROUP_UID, "UID3");
		namedValues3.put(CLIENT_UID, clientUID);
		namedValues3.put(ORGSTRUCTURE_UID, orgStrUID);
		namedValues3.put(SHORT_NAME, "shortName3");
		namedValues3.put(TOP_LEVEL, true);
		namedValues3.put(VALIDFROM, from);
		namedValues3.put(VALIDTO, to);
		List<Map<String, Object>> namedValues = new ArrayList<Map<String, Object>>();
		namedValues.add(namedValues1);
		namedValues.add(namedValues2);
		namedValues.add(namedValues3);
		
		int[] result = sqlService.batchInsert(webdeskDataSource, "pogroup", namedValues);
		assertEquals(3, result.length);
		assertEquals(1, result[0]);
		assertEquals(1, result[1]);
		assertEquals(1, result[2]);
		
		// assertions via orgService do not work, probably because of hibernate caching
		
		List<Map<String, ?>> fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from pogroup where GROUP_UID = 'UID1'"));
		assertNotNull(fromDb);
		assertEquals(1, fromDb.size());
		Map<String, ?> first = fromDb.get(0);
		assertEquals("shortName1", first.get(SHORT_NAME));
		assertEquals("name1", first.get(NAME));
		assertEquals(orgStrUID, first.get(ORGSTRUCTURE_UID));
		assertEquals(clientUID, first.get(CLIENT_UID));
		assertEquals(from, first.get(VALIDFROM));
		assertEquals(to, first.get(VALIDTO));
		assertTrue((Boolean) first.get(TOP_LEVEL));
		
		fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from pogroup where GROUP_UID = 'UID2'"));
		assertNotNull(fromDb);
		assertEquals(1, fromDb.size());
		first = fromDb.get(0);
		assertEquals("shortName2", first.get(SHORT_NAME));
		assertEquals("name2", first.get(NAME));
		
		fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from pogroup where GROUP_UID = 'UID3'"));
		assertNotNull(fromDb);
		assertEquals(1, fromDb.size());
		first = fromDb.get(0);
		assertEquals("shortName3", first.get(SHORT_NAME));
		assertEquals("name3", first.get(NAME));
		
	}

//	------------------------- TEST PREPARED STATEMENT BATCH UPDATE WITH MAPS -----------------------------------------
	
	public void testSimpleBatchUpdate() {
		
		int beforeCount = orgService.loadAllClients().size();
		
		Map<String, Object> namedValues1 = getClient(1);
		Map<String, Object> namedValues2 = getClient(2);
		Map<String, Object> namedValues3 = getClient(3);
		List<Map<String, Object>> namedValues = new ArrayList<Map<String, Object>>();
		namedValues.add(namedValues1);
		namedValues.add(namedValues2);
		namedValues.add(namedValues3);
		
		int[] result = sqlService.batchInsert(webdeskDataSource, "poclient", namedValues);
		assertEquals(3, result.length);
		assertEquals(1, result[0]);
		assertEquals(1, result[1]);
		assertEquals(1, result[2]);
		assertEquals(beforeCount + 3, orgService.loadAllClients().size());
		
		namedValues1 = getClient(11); // update values
		namedValues1.put(CLIENT_UID, "UID1");
		namedValues2 = getClient(22); // update values
		namedValues2.put(CLIENT_UID, "UID2");
		namedValues3 = getClient(33); // update values
		namedValues3.put(CLIENT_UID, "UID3");
		namedValues.clear();
		namedValues.add(namedValues1);
		namedValues.add(namedValues2);
		namedValues.add(namedValues3);
		
		result = sqlService.batchUpdate(webdeskDataSource, "poclient", CLIENT_UID, namedValues);
		assertEquals(3, result.length);
		assertEquals(1, result[0]);
		assertEquals(1, result[1]);
		assertEquals(1, result[2]);
		assertEquals(beforeCount + 3, orgService.loadAllClients().size());
		
		List<Map<String, ?>> fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from poclient where CLIENT_UID = 'UID1'"));
		checkClient(11, fromDb);
		fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from poclient where CLIENT_UID = 'UID2'"));
		checkClient(22, fromDb);
		fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from poclient where CLIENT_UID = 'UID3'"));
		checkClient(33, fromDb);
		
		result = sqlService.batchDelete(webdeskDataSource, "poclient", CLIENT_UID, Arrays.asList("UID1", "UID2", "UID3"));
		assertEquals(3, result.length);
		assertEquals(1, result[0]);
		assertEquals(1, result[1]);
		assertEquals(1, result[2]);
		assertEquals(beforeCount, orgService.loadAllClients().size());
	}
	
//	------------------------- SIMPLE INSERT OR UPDATE --------------------------------------------
	
    public class SimpleMapComparator implements Comparator<Map<String,Object>> {

		@Override
		public int compare(Map<String, Object> paramT1, Map<String, Object> paramT2) {
			
			if (paramT1.equals(paramT2))
				return 0;
			
			return -1;
		}
    	
    }
	
	public void testSimpleBatchInsertOrUpdate() {

		Map<String, Object> namedValues1 = getClient(1);
		int result = sqlService.insert(webdeskDataSource, "poclient", namedValues1);
		assertEquals(1, result);
		
		Map<String, Object> namedValues2 = getClient(2); // for update
		namedValues2.put(CLIENT_UID, "UID1"); // change the UID

		Map<String, Object> namedValues3 = getClient(3); // for insert
		
		List<Map<String, Object>> namedValuesList = new ArrayList<Map<String,Object>>();
		namedValuesList.add(namedValues2);
		namedValuesList.add(namedValues3);
		
		int[] res = sqlService.batchInsertOrUpdate(webdeskDataSource, "poclient", CLIENT_UID, namedValuesList);
		assertEquals(2, res.length);
		assertEquals(1, res[0]); // inserts
		assertEquals(1, res[1]); // updates
		
		List<Map<String, ?>> fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from poclient where client_uid = 'UID1'"));
		checkClient(2, fromDb);
		fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from poclient where client_uid = 'UID3'"));
		checkClient(3, fromDb);
		
		
		// try to save PoClient3 again
		// which has not changed -> this should result in Update, if no comparator is passed
		namedValuesList = new ArrayList<Map<String,Object>>();
		namedValuesList.add( getClient(3) );
		
		res = sqlService.batchInsertOrUpdate(webdeskDataSource, "poclient", CLIENT_UID, namedValuesList);
		assertEquals(2, res.length);
		assertEquals(0, res[0]); // inserts
		assertEquals(1, res[1]); // updates
		
		// try again, this time with a comparator
		// now the code should discover, that the values are already in the database and
		// that no additional UPDATE has to be done.
		res = sqlService.batchInsertOrUpdate(webdeskDataSource, "poclient", CLIENT_UID, namedValuesList, new SimpleMapComparator());
		assertEquals(2, res.length);
		assertEquals(0, res[0]); // inserts
		assertEquals(0, res[1]); // updates
		
		// try again, this time with a comparator, but with slightly changed data!
		// this time an UPDATE has to take effect
		namedValuesList.get(0).put(SHORT_NAME,"new shortname");
		res = sqlService.batchInsertOrUpdate(webdeskDataSource, "poclient", CLIENT_UID, namedValuesList, new SimpleMapComparator());
		assertEquals(2, res.length);
		assertEquals(0, res[0]); // inserts
		assertEquals(1, res[1]); // updates
		
	}
	
//	------------------------- BATCH DELETE --------------------------------------------
	
	public void testBatchDelete() {
		
		Map<String, Object> namedValues = getClient(1);
		int result = sqlService.insert(webdeskDataSource, "poclient", namedValues);
		assertEquals(1, result);
		namedValues = getClient(2);
		result = sqlService.insert(webdeskDataSource, "poclient", namedValues);
		assertEquals(1, result);
		
		List<Map<String, ?>> fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from poclient where client_uid = 'UID1'"));
		checkClient(1, fromDb);
		fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from poclient where client_uid = 'UID2'"));
		checkClient(2, fromDb);
		
		int[] res = sqlService.batchDelete(webdeskDataSource, "poclient", CLIENT_UID, Arrays.asList("nonsense1", "nonsense2"));
		assertEquals(2, res.length);
		assertEquals(0, res[0]);
		assertEquals(0, res[1]);
		fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from poclient where client_uid = 'UID1'"));
		checkClient(1, fromDb);
		fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from poclient where client_uid = 'UID2'"));
		checkClient(2, fromDb);
		
		res = sqlService.batchDelete(webdeskDataSource, "poclient", CLIENT_UID, Arrays.asList("UID1", "UID2"));
		assertEquals(2, res.length);
		assertEquals(1, res[0]);
		assertEquals(1, res[1]);
		fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from poclient where client_uid = 'UID1'"));
		assertNotNull(fromDb);
		assertEquals(0, fromDb.size());
		fromDb = sqlService.select(webdeskDataSource, new PaginableQuery("select * from poclient where client_uid = 'UID2'"));
		assertNotNull(fromDb);
		assertEquals(0, fromDb.size());
		
		
	}
	
//	----------------------- DIFFERENT PARAMETER TYPES ------------------------------
	
	public void testBatchUpdateWithDifferentParameterTypes() {
		
		Date tomorrow = DateTools.dateOnly(DateTools.tomorrow());
		
		String sql = "select validfrom, activeUser from poperson where lastName in ('Weiss', 'Haider', 'Duschek')";
		PaginableQuery query = new PositionalQuery(sql, new Object[] {});
		List<Map<String, ?>> res = sqlService.select(webdeskDataSource, query);
		assertNotNull(res);
		assertEquals(3, res.size());
		assertEquals(2, res.get(0).size());
		assertTrue(((Date) res.get(0).get(VALIDFROM)).before(tomorrow));
		assertEquals(true, res.get(0).get(ACTIVE_USER));
		assertTrue(((Date) res.get(1).get(VALIDFROM)).before(tomorrow));
		assertEquals(true, res.get(1).get(ACTIVE_USER));
		assertTrue(((Date) res.get(2).get(VALIDFROM)).before(tomorrow));
		assertEquals(true, res.get(2).get(ACTIVE_USER));
		
		sql = "update poperson set validfrom = ?, activeUser = ? where lastName = ?";
		Object[] values1 = new Object[] {tomorrow, false, "Weiss"};
		Object[] values2 = new Object[] {tomorrow, false, "Haider"};
		Object[] values3 = new Object[] {tomorrow, false, "Duschek"};
		List<Object[]> paramValues = new ArrayList<Object[]>();
		paramValues.add(values1);
		paramValues.add(values2);
		paramValues.add(values3);
		int[] result = sqlService.batchExecute(webdeskDataSource, sql, paramValues);
		assertEquals(3, result.length);
		assertEquals(1, result[0]);
		assertEquals(1, result[1]);
		assertEquals(1, result[2]);
		
		sql = "select validfrom, activeUser from poperson where lastName in ('Weiss', 'Haider', 'Duschek')";
		query = new PositionalQuery(sql, new Object[] {});
		res = sqlService.select(webdeskDataSource, query);
		assertNotNull(result);
		assertEquals(3, res.size());
		assertEquals(2, res.get(0).size());
		assertEquals(tomorrow, res.get(0).get(VALIDFROM));
		assertEquals(false, res.get(0).get(ACTIVE_USER));
		assertEquals(tomorrow, res.get(1).get(VALIDFROM));
		assertEquals(false, res.get(1).get(ACTIVE_USER));
		assertEquals(tomorrow, res.get(2).get(VALIDFROM));
		assertEquals(false, res.get(2).get(ACTIVE_USER));
		
	}
	
	public void testContainsRecord() {
		
		List<PoClient> clients = orgService.loadAllClients();
		String clientUID = clients.get(0).getUID();
		
		assertFalse(sqlService.containsRecord(webdeskDataSource, "poclient", "CLIENT_UID", "nonsense"));
		assertTrue(sqlService.containsRecord(webdeskDataSource, "poclient", "CLIENT_UID", clientUID));
	}
	
	public void testGetColumnNames() {
		
		try {
			sqlService.getColumnNames(webdeskDataSource, "nonsense");
			fail("Accepted non-existent table");
		} catch (Exception e) { }
		
		List<String> names = sqlService.getColumnNames(webdeskDataSource, "poclient");
		
		assertNotNull(names);
		assertEquals(7, names.size());
		assertTrue(names.contains("CLIENT_UID"));
		assertTrue(names.contains("NAME"));
		assertTrue(names.contains("SHORTNAME"));
		assertTrue(names.contains("DESCRIPTION"));
		assertTrue(names.contains("GROUPSHORTNAMEPREFIX"));
		assertTrue(names.contains("PERSONUSERNAMEPREFIX"));
		assertTrue(names.contains("PERSONEMPLOYEEIDPREFIX"));
	}
	
	public void testGetColumnNamesFromSQL() {
		
		String sql = "select p.firstName as Vorname, p.lastName as NachName, userName from PoPerson as p";
		List<String> names = sqlService.getColumnNamesFromSQL(webdeskDataSource, sql);
		assertNotNull(names);
		assertEquals(3, names.size());
		assertTrue(names.contains("VORNAME"));
		assertTrue(names.contains("NACHNAME"));
		assertTrue(names.contains("USERNAME"));
	}
	
	
	
	public void testGetColumnClassNames() {
		
		String testTableName = "PoGroup";
		
		DataSource dataSource = dataSourceService.getDataSource(PoDataSourceService.WEBDESK); 
		Map<String, String> classNames = sqlService.getColumnClassNames(dataSource, testTableName);
		
		for (String colName : classNames.keySet())
			System.out.println(colName + ": " + classNames.get(colName));
		
		assertEquals(9, classNames.size());
		
		assertEquals("java.sql.Timestamp", classNames.get("VALIDFROM"));
		assertEquals("java.sql.Timestamp", classNames.get("VALIDTO"));
		assertEquals("java.lang.String", classNames.get("NAME"));
		assertEquals("java.lang.String", classNames.get("CLIENT_UID"));
		assertEquals("java.lang.String", classNames.get("SHORTNAME"));
		assertEquals("java.lang.String", classNames.get("ORGSTRUCTURE_UID"));
		assertEquals("java.lang.String", classNames.get("GROUP_UID"));
		assertEquals("java.lang.Boolean", classNames.get("ISTOPLEVEL"));
		
	}
	
	public void testGetColumnClassNamesFromSQL() {
		
		String sql = "select p.firstName as Vorname, p.lastName as NachName, userName, dateOfBirth as Geburtstag from PoPerson as p";
		Map<String, String> classNames = sqlService.getColumnClassNamesFromSQL(webdeskDataSource, sql);
		
		for (String colName : classNames.keySet())
			System.out.println(colName + ": " + classNames.get(colName));
		
		assertEquals(4, classNames.size());
		
		assertEquals("java.lang.String", classNames.get("VORNAME"));
		assertEquals("java.lang.String", classNames.get("NACHNAME"));
		assertEquals("java.lang.String", classNames.get("USERNAME"));
		assertEquals("java.sql.Timestamp", classNames.get("GEBURTSTAG"));
		
	}

	public void testGetColumnMandatoriness() {
		
		String testTableName = "PoGroup";
		
		DataSource dataSource = dataSourceService.getDataSource(PoDataSourceService.WEBDESK); 
		Map<String, Boolean> columnMandatoriness = sqlService.getColumnMandatoriness(dataSource, testTableName);
		
		for (String colName : columnMandatoriness.keySet())
			System.out.println(colName + ": " + columnMandatoriness.get(colName));
		
		assertEquals(9, columnMandatoriness.size());
		
		assertTrue(columnMandatoriness.get("VALIDFROM"));	// fri_2013-03-11: AssertionFailedError, not reproducible locally
		assertTrue(columnMandatoriness.get("NAME"));
		assertTrue(columnMandatoriness.get("VALIDTO"));
		assertFalse(columnMandatoriness.get("DESCRIPTION"));
		assertTrue(columnMandatoriness.get("CLIENT_UID"));
		assertTrue(columnMandatoriness.get("SHORTNAME"));
		assertTrue(columnMandatoriness.get("ORGSTRUCTURE_UID"));
		assertTrue(columnMandatoriness.get("GROUP_UID"));
		assertFalse(columnMandatoriness.get("ISTOPLEVEL"));
	}
	
	public void testPrimaryKeyConstraintName()	{
		DataSource webdeskData = dataSourceService.getDataSource(PoDataSourceService.WEBDESK); 
		Map<String,List<String>> constraintNames = sqlService.getConstraintNames(webdeskData, PoBeanPropertyValue.class.getSimpleName());
		assertTrue(constraintNames.size() > 0);
		assertTrue(constraintNames.get(PoGeneralSqlService.PRIMARY_KEY_CONSTRAINT_TAG).size() == 1);	// must be exactly one primary key constraint
	}
	
	// ------------------------------- PRIVATE METHODS -------------------------------------
	
	private Map<String, Object> getClient(int i) {
		
		Map<String, Object> namedValues = new HashMap<String, Object>();
		namedValues.put(CLIENT_UID, "UID" + i);
		namedValues.put(SHORT_NAME, "shortName" + i);
		namedValues.put(DESCRIPTION, "description" + i);
		namedValues.put(NAME, "name" + i);
		return namedValues;
	}
	
	private void checkClient(int i, List<Map<String, ?>> fromDb) {
		
		assertNotNull(fromDb);
		assertEquals(1, fromDb.size());
		Map<String, ?> first = fromDb.get(0);
		assertEquals("shortName" + i, first.get(SHORT_NAME));
		assertEquals("name" + i, first.get(NAME));
		assertEquals("description" + i, first.get(DESCRIPTION));
	}
	
}
