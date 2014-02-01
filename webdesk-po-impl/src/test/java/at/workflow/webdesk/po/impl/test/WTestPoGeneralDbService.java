package at.workflow.webdesk.po.impl.test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.springframework.context.ApplicationContext;

import at.workflow.webdesk.po.PoGeneralDbService;
import at.workflow.webdesk.po.PoLanguageService;
import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.impl.PoGeneralDbServiceImpl;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoLanguage;
import at.workflow.webdesk.po.model.PoTextModule;
import at.workflow.webdesk.tools.NamedQuery;
import at.workflow.webdesk.tools.PaginableQuery;
import at.workflow.webdesk.tools.PositionalQuery;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;
import at.workflow.webdesk.tools.testing.DataGenerator;

/**
 * @author sdzuban 24.11.2011
 */
@SuppressWarnings("rawtypes")
public class WTestPoGeneralDbService extends AbstractTransactionalSpringHsqlDbTestCase {

	private static final String TEXTMODULE_NAME_BEGIN = "po_";
	private static final String TEXTMODULE_VALUE_BEGIN = "This is ";
	
	private PoOrganisationService orgService;
	private PoGeneralDbService dbService;
	private PoClient[] client = new PoClient[5];
	private String client2Uid;


	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		
		orgService = (PoOrganisationService) getBean("PoOrganisationService");
		dbService = (PoGeneralDbService) getBean("PoGeneralDbService");
		
		// create clients
		int i = 0;
		for (PoClient c : client) {
			c = orgService.findClientByName("Test client" + i);
			if (c == null)	{
				client[i] = new PoClient();
				client[i].setName("Test client" + i);
				orgService.saveClient(client[i]);
			}
			i++;
		}
		client2Uid = client[2].getUID();
	}
	

	/** fri_2013-03-08: need this for test of PaginableQuery with tables that have more than 8262 rows. */
	@Override
	protected DataGenerator[] getDataGenerators() {
		final DataGenerator generator = new DataGenerator()	{
			@Override
			public void create(ApplicationContext appCtx) throws IOException {
				final PoLanguageService langService = (PoLanguageService) appCtx.getBean("PoLanguageService");
				final PoLanguage language = langService.findDefaultLanguage();
				
				for (int i = 1; i <= 15000; i++)	{
					final PoTextModule textModule = new PoTextModule();
					textModule.setName(TEXTMODULE_NAME_BEGIN + pad(i));
					textModule.setValue(TEXTMODULE_VALUE_BEGIN + pad(i));
					textModule.setLanguage(language);
					langService.saveTextModule(textModule);
				}
			}
		};
		return new DataGenerator [] { generator };
	}

	private String pad(int i)	{
		String s = Integer.toString(i);
		while (s.length() < 8)
			s = "0" + s;
		return s;
	}
	
	
	// start of tests
	
	/** fri_2013-03-08: test of PaginableQuery with tables that have more than 8262 rows. */
	public void testPaginableQueryWithBigTable()	{
		final int FIRST_ROW = 9000;
		final int PAGE_SIZE = 100;
		
    	final PositionalQuery query = new PositionalQuery("from "+PoTextModule.class.getSimpleName()+" order by name", null);
		query.setFirstResult(FIRST_ROW);
		query.setMaxResults(PAGE_SIZE);
		
		final List results = dbService.selectObjectsOrArrays(query);
		assertEquals(PAGE_SIZE, results.size());
		
		int i = FIRST_ROW + 1;
		for (Object o : results)	{
			final PoTextModule m = (PoTextModule) o;
			assertTrue(m.getName().startsWith(TEXTMODULE_NAME_BEGIN));
			assertTrue(m.getValue().startsWith(TEXTMODULE_VALUE_BEGIN));
			
			final String numberString = m.getName().substring(TEXTMODULE_NAME_BEGIN.length());
			final int number = Integer.valueOf(numberString);
			assertEquals(i, number);
			i++;
		}
	}
	
	
	
    public void testBasicPagination() {

    	PositionalQuery query = new PositionalQuery("from PoClient", null);
    	List result = dbService.getElementsAsList(query);
    	assertNotNull(result);
    	assertEquals(5, result.size());
    	
    	query.setMaxResults(-1);
    	result = dbService.getElementsAsList(query);
    	assertNotNull(result);
    	assertEquals(5, result.size());
    	
    	query.setMaxResults(13);
    	result = dbService.getElementsAsList(query);
    	assertNotNull(result);
    	assertEquals(5, result.size());
    	
    	query.setFirstResult(12);
    	query.setMaxResults(0);
    	result = dbService.getElementsAsList(query);
    	assertNotNull(result);
    	assertEquals(0, result.size());
    	
    }


    public void testPositionalQueryWoParameter() {

    	PositionalQuery query = new PositionalQuery("from PoClient", null);
    	List result = dbService.getElementsAsList(query);
    	assertNotNull(result);
    	assertEquals(5, result.size());
    	for (PoClient cl : client)
    		assertTrue(result.contains(cl));
    	
    	query.setMaxResults(3);
    	result = dbService.getElementsAsList(query);
    	assertNotNull(result);
    	assertEquals(3, result.size());
    	assertTrue(result.contains(client[0]));
    	assertTrue(result.contains(client[1]));
    	assertTrue(result.contains(client[2]));
    	
    	query.setFirstResult(1);
    	result = dbService.getElementsAsList(query);
    	assertNotNull(result);
    	assertEquals(3, result.size());
    	assertTrue(result.contains(client[1]));
    	assertTrue(result.contains(client[2]));
    	assertTrue(result.contains(client[3]));
    	
    }
    
    public void testPositionalQueryWithParameter() {
    	
    	PositionalQuery query = new PositionalQuery("from PoClient where UID <> ?", new Object[] {client2Uid});
    	List result = dbService.getElementsAsList(query);
    	assertNotNull(result);
    	assertEquals(4, result.size());
    	int idx = 0;
    	for (PoClient c : client) {
    		if (idx != 2)
    			assertTrue(result.contains(c));
    		idx++;
    	}
    	
    	query.setMaxResults(3);
    	result = dbService.getElementsAsList(query);
    	assertNotNull(result);
    	assertEquals(3, result.size());
    	assertTrue(result.contains(client[0]));
    	assertTrue(result.contains(client[1]));
    	assertTrue(result.contains(client[3]));
    	
    	query.setFirstResult(1);
    	result = dbService.getElementsAsList(query);
    	assertNotNull(result);
    	assertEquals(3, result.size());
    	assertTrue(result.contains(client[1]));
    	assertTrue(result.contains(client[3]));
    	assertTrue(result.contains(client[4]));
    }
    
	public void testNamedQueryWoParameter() {
    	
    	NamedQuery query = new NamedQuery("from PoClient", null, null);
    	List result = dbService.getElementsAsList(query);
    	assertNotNull(result);
    	assertEquals(5, result.size());
    	for (PoClient c : client)
    		assertTrue(result.contains(c));
    	
    	query.setMaxResults(3);
    	result = dbService.getElementsAsList(query);
    	assertNotNull(result);
    	assertEquals(3, result.size());
    	assertTrue(result.contains(client[0]));
    	assertTrue(result.contains(client[1]));
    	assertTrue(result.contains(client[2]));
    	
    	query.setFirstResult(1);
    	result = dbService.getElementsAsList(query);
    	assertNotNull(result);
    	assertEquals(3, result.size());
    	assertTrue(result.contains(client[1]));
    	assertTrue(result.contains(client[2]));
    	assertTrue(result.contains(client[3]));
    }
    
    public void testNamedQueryWithParameter() {
    	
    	NamedQuery query = new NamedQuery("from PoClient where UID <> :client2Uid", new String[] {"client2Uid"}, new Object[] {client2Uid});
    	List result = dbService.getElementsAsList(query);
    	assertNotNull(result);
    	assertEquals(4, result.size());
    	int idx = 0;
    	for (PoClient c : client) {
    		if (idx != 2)
    			assertTrue(result.contains(c));
    		idx++;
    	}
    	
    	query.setMaxResults(3);
    	result = dbService.getElementsAsList(query);
    	assertNotNull(result);
    	assertEquals(3, result.size());
    	assertTrue(result.contains(client[0]));
    	assertTrue(result.contains(client[1]));
    	assertTrue(result.contains(client[3]));
    	
    	query.setFirstResult(1);
    	result = dbService.getElementsAsList(query);
    	assertNotNull(result);
    	assertEquals(3, result.size());
    	assertTrue(result.contains(client[1]));
    	assertTrue(result.contains(client[3]));
    	assertTrue(result.contains(client[4]));
    }
    
    public void testQueryWithParameterPolymorfic() {
    	
    	PaginableQuery query = new PositionalQuery("from PoClient where UID <> ?", new Object[] {client2Uid});
    	List result = dbService.selectObjectsOrArrays(query);
    	assertNotNull(result);
    	assertEquals(4, result.size());
    	int idx = 0;
    	for (PoClient c : client) {
    		if (idx != 2)
    			assertTrue(result.contains(c));
    		idx++;
    	}
    	
    	query.setMaxResults(3);
    	result = dbService.selectObjectsOrArrays(query);
    	assertNotNull(result);
    	assertEquals(3, result.size());
    	assertTrue(result.contains(client[0]));
    	assertTrue(result.contains(client[1]));
    	assertTrue(result.contains(client[3]));
    	
    	query.setFirstResult(1);
    	result = dbService.selectObjectsOrArrays(query);
    	assertNotNull(result);
    	assertEquals(3, result.size());
    	assertTrue(result.contains(client[1]));
    	assertTrue(result.contains(client[3]));
    	assertTrue(result.contains(client[4]));

    	query = new NamedQuery("from PoClient where UID <> :client2Uid", new String[] {"client2Uid"}, new Object[] {client2Uid});
    	result = dbService.selectObjectsOrArrays(query);
    	assertNotNull(result);
    	assertEquals(4, result.size());
    	idx = 0;
    	for (PoClient c : client) {
    		if (idx != 2)
    			assertTrue(result.contains(c));
    		idx++;
    	}
    	
    	query.setMaxResults(3);
    	result = dbService.selectObjectsOrArrays(query);
    	assertNotNull(result);
    	assertEquals(3, result.size());
    	assertTrue(result.contains(client[0]));
    	assertTrue(result.contains(client[1]));
    	assertTrue(result.contains(client[3]));
    	
    	query.setFirstResult(1);
    	result = dbService.selectObjectsOrArrays(query);
    	assertNotNull(result);
    	assertEquals(3, result.size());
    	assertTrue(result.contains(client[1]));
    	assertTrue(result.contains(client[3]));
    	assertTrue(result.contains(client[4]));
    }
    
    public void testNamedMapQuery() {
    	
    	PaginableQuery query = new PositionalQuery("select name, UID from PoClient where UID = ?", new Object[] {client2Uid});
    	List result = dbService.select(query);
    	assertNotNull(result);
    	assertEquals(1, result.size());
    	assertTrue(result.get(0) instanceof Map);
    	Map oneName = (Map) result.get(0);
    	assertEquals(2, oneName.size());
    	assertEquals("Test client2", oneName.get("name"));
    	
    	query = new NamedQuery("select name, UID from PoClient where UID = :client2Uid", new String[] {"client2Uid"}, new Object[] {client2Uid});
    	result = dbService.select(query);
    	assertNotNull(result);
    	assertEquals(1, result.size());
    	assertTrue(result.get(0) instanceof Map);
    	oneName = (Map) result.get(0);
    	assertEquals(2, oneName.size());
    	assertEquals("Test client2", oneName.get("name"));
    	
    }

    public void testQueryWithoutSelect() {
    	
    	try {
	    	PaginableQuery query = new PositionalQuery("from PoClient where UID = ?", new Object[] {client2Uid});
	    	dbService.select(query);
	    	fail("No exception on statement without SELECT clause.");
    	} catch (Exception e) {}    	
    }
    
    public void testSelectTermsExtractorRegex() {
    	
    	// the capital/lower letter writing is intentional and part of the test
    	
		Matcher matcher = PoGeneralDbServiceImpl.SELECT_TERMS_EXTRACTOR_PATTERN.matcher("");
		assertFalse(matcher.matches());
		
		// negative matches
		
		matcher = PoGeneralDbServiceImpl.SELECT_TERMS_EXTRACTOR_PATTERN.matcher("sElEct x, y z, fromdate table");
		assertFalse(matcher.matches());
		
		matcher = PoGeneralDbServiceImpl.SELECT_TERMS_EXTRACTOR_PATTERN.matcher("sElEct x, y z, validfrom table");
		assertFalse(matcher.matches());

		// positive matches
		
		matcher = PoGeneralDbServiceImpl.SELECT_TERMS_EXTRACTOR_PATTERN.matcher("sElEct x, y z");
		assertFalse(matcher.matches());
		
		matcher = PoGeneralDbServiceImpl.SELECT_TERMS_EXTRACTOR_PATTERN.matcher("sEleCT x, y, z fROm table");
		assertTrue(matcher.matches());
		
		assertEquals("x, y, z", matcher.group(1));
		
		matcher = PoGeneralDbServiceImpl.SELECT_TERMS_EXTRACTOR_PATTERN.matcher("\nsEleCT x, y, z fROm table");
		assertTrue(matcher.matches());
		
		assertEquals("x, y, z", matcher.group(1));
		
		matcher = PoGeneralDbServiceImpl.SELECT_TERMS_EXTRACTOR_PATTERN.matcher("sEleCT\n x, y, z fROm table");
		assertTrue(matcher.matches());
		
		assertEquals("x, y, z", matcher.group(1));
		
		matcher = PoGeneralDbServiceImpl.SELECT_TERMS_EXTRACTOR_PATTERN.matcher("sEleCT\nx, y, z fROm table");
		assertTrue(matcher.matches());
		
		assertEquals("x, y, z", matcher.group(1));
		
		matcher = PoGeneralDbServiceImpl.SELECT_TERMS_EXTRACTOR_PATTERN.matcher("sEleCT\nx, y, z fROm\ntable");
		assertTrue(matcher.matches());
		
		assertEquals("x, y, z", matcher.group(1));
		
		matcher = PoGeneralDbServiceImpl.SELECT_TERMS_EXTRACTOR_PATTERN.matcher("sEleCT\nx, \ty, \rz fROm\ntable");
		assertTrue(matcher.matches());
		
		assertEquals("x, \ty, \rz", matcher.group(1));
		
		matcher = PoGeneralDbServiceImpl.SELECT_TERMS_EXTRACTOR_PATTERN.matcher("sEleCT\nx, y, z\n fROm table");
		assertTrue(matcher.matches());
		
		assertEquals("x, y, z", matcher.group(1));
		
		matcher = PoGeneralDbServiceImpl.SELECT_TERMS_EXTRACTOR_PATTERN.matcher("sEleCT\nx, y, z \nfROm table");
		assertTrue(matcher.matches());
		
		assertEquals("x, y, z", matcher.group(1));
		
		matcher = PoGeneralDbServiceImpl.SELECT_TERMS_EXTRACTOR_PATTERN.matcher("sEleCT\n\tx, y, z\nfROm ntable");
		assertTrue(matcher.matches());
		
		assertEquals("x, y, z", matcher.group(1));
		
		matcher = PoGeneralDbServiceImpl.SELECT_TERMS_EXTRACTOR_PATTERN.matcher("sEleCT\n\tx, y, z\nfROm\ntable");
		assertTrue(matcher.matches());
		
		assertEquals("x, y, z", matcher.group(1));

    }

}
