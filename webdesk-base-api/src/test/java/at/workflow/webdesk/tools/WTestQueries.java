package at.workflow.webdesk.tools;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

/**
 * @author sdzuban 09.03.2012
 */
public class WTestQueries extends TestCase {
	
	@SuppressWarnings("unchecked")
	public void testPagination () {
	
		List<String> list = Arrays.asList("1","2","3","4","5","6","7");
		
		PaginableQuery query = new PaginableQuery("");
		
    	query.setMaxResults(-1);
    	List<String> result = PaginationUtil.getPaginatedResult(query,list);
    	assertNotNull(result);
    	assertEquals(7, result.size());
    	assertEquals("1", result.get(0));
    	assertEquals("7", result.get(6));
    	
    	query.setMaxResults(13);
    	result = PaginationUtil.getPaginatedResult(query, list);
    	assertNotNull(result);
    	assertEquals(7, result.size());
    	assertEquals("1", result.get(0));
    	assertEquals("7", result.get(6));
    	
    	query.setMaxResults(3);
    	result = PaginationUtil.getPaginatedResult(query, list);
    	assertNotNull(result);
    	assertEquals(3, result.size());
    	assertEquals("1", result.get(0));
    	assertEquals("3", result.get(2));
    	
    	query.setFirstResult(0);	// must succeed
    	
    	try	{
    		query.setFirstResult(-1);
    		fail("First index can not be smaller than 1!");
    	}
    	catch (IllegalArgumentException e)	{
    		// ignore this, is expected here
    	}
    	
    	query.setFirstResult(12);
    	query.setMaxResults(0);
    	result = PaginationUtil.getPaginatedResult(query, list);
    	assertNotNull(result);
    	assertEquals(0, result.size());
    	
    	query.setFirstResult(2);
    	query.setMaxResults(2);
    	result = PaginationUtil.getPaginatedResult(query, list);
    	assertNotNull(result);
    	assertEquals(2, result.size());
    	assertEquals("3", result.get(0));
    	assertEquals("4", result.get(1));
    	
	}

	public void testPaginableQuery() {
		
		try {
			new PaginableQuery((String) null);
			fail("PaginableQuery accepted null hql");
		} catch (Exception e) {System.out.println(e);}
		
		PaginableQuery query = new PaginableQuery("");
		assertNotNull(query);
		assertEquals("", query.getQueryText());
		
		query = new PaginableQuery("from a");
		assertNotNull(query);
		assertEquals("from a", query.getQueryText());
		
		query = new PaginableQuery("    from abc   ");
		assertNotNull(query);
		assertEquals("from abc", query.getQueryText());
		
	}
	
	public void testPositionalQuery() {
		
		try {
			new PositionalQuery(null, new Object[] {1});
			fail("PositionalQuery accepted null hql and 1 parameter value");
		} catch (Exception e) {System.out.println(e);}
		
		try {
			new PositionalQuery("from x where x > ?", null);
			fail("PositionalQuery accepted 'from x where x > ?' hql and null");
		} catch (Exception e) {System.out.println(e);}
		
		try {
			new PositionalQuery("from x where x > ?", new Object[]{});
			fail("PositionalQuery accepted 'from x where x > ?' hql and new Object[]{}");
		} catch (Exception e) {System.out.println(e);}
		
		try {
			new PositionalQuery("from x where x > ?", new Object[] {1, 2});
			fail("PositionalQuery accepted 'from x where x > ?' hql and [1, 2] parameter value");
		} catch (Exception e) {System.out.println(e);}

		PositionalQuery query = new PositionalQuery("from x", new Object[] {});
		assertNotNull(query);
		assertEquals(0, query.getParamValues().length);
		
		query = new PositionalQuery("from x where x > ?", new Object[] {1});
		assertNotNull(query);
		assertEquals(1, query.getParamValues()[0]);
		
		query = new PositionalQuery("from x where x > ? and y < ?", new Object[] {1, 2});
		assertNotNull(query);
		assertEquals(1, query.getParamValues()[0]);
		assertEquals(2, query.getParamValues()[1]);
	}
	
	public void testNamedQuery() {
		
		try {
			new NamedQuery(null, null, new Object[] {1});
			fail("NamedQuery accepted null hql no name and 1 value");
		} catch (Exception e) {System.out.println(e);}
		
		try {
			new NamedQuery(null, new String[] {"w"}, null);
			fail("NamedQuery accepted null hql 1 name and no value");
		} catch (Exception e) {System.out.println(e);}
		
		try {
			new NamedQuery(null, new String[] {"w"}, new Object[] {1});
			fail("NamedQuery accepted null hql 1 name and 1 value");
		} catch (Exception e) {System.out.println(e);}
		
		try {
			new NamedQuery("from x where x > :name", null, null);
			fail("NamedQuery accepted 'from x where x > :name' hql null and null");
		} catch (Exception e) {System.out.println(e);}
		
		try {
			new NamedQuery("from x where x > :name", new String[] {}, new Object[]{});
			fail("NamedQuery accepted 'from x where x > :name' hql new String[] {} and new Object[]{}");
		} catch (Exception e) {System.out.println(e);}
		
		try {
			new NamedQuery("from x where x > :name", new String[] {"w"}, new Object[]{});
			fail("NamedQuery accepted 'from x where x > :name' hql {'w'} and {1}");
		} catch (Exception e) {System.out.println(e);}
		
		try {
			new NamedQuery("from x where x > :name", null, new Object[]{});
			fail("NamedQuery accepted 'from x where x > :name' hql {'w'} and {1}");
		} catch (Exception e) {System.out.println(e);}
		
		try {
			new NamedQuery("from x where x > :name", new String[] {"w"}, new Object[]{1});
			fail("NamedQuery accepted 'from x where x > ?' hql {'w'} and {1}");
		} catch (Exception e) {System.out.println(e);}
		
		try {
			new NamedQuery("from x where x > :name", new String[] {"name", "extra"}, new Object[]{1});
			fail("NamedQuery accepted 'from x where x > ?' hql {'w'} and {1}");
		} catch (Exception e) {System.out.println(e);}
		
		try {
			new NamedQuery("from x where x > :name", new String[] {"name"}, new Object[]{1, 2, 3});
			fail("NamedQuery accepted 'from x where x > ?' hql {'w'} and {1}");
		} catch (Exception e) {System.out.println(e);}
		
		
		NamedQuery query = new NamedQuery("from x where x > :name", new String[] {"name"}, new Object[] {1});
		assertNotNull(query);
		
		query = new NamedQuery("from x where x > :name and y < :name or z = :name", new String[] {"name"}, new Object[] {1});
		assertNotNull(query);
		
	}
	
	public void testLikeParameterDecorationInPositionalQuery() {
		
		Object[] values = new Object[] {1234, "text", true};
		
		String hql = " something    >     ?  sthg others = ? or sthg else < ?";
		
		PositionalQuery query = new PositionalQuery(hql, values, true);
		assertNotNull(query.getParamValues());
		assertEquals(1234, query.getParamValues()[0]);
		assertEquals("text", query.getParamValues()[1]);
		assertEquals(true, query.getParamValues()[2]);
		
		hql = " something    <>     ?  sthg others like ? or sthg else <= ?";
		
		query = new PositionalQuery(hql, values, true);
		assertNotNull(query.getParamValues());
		assertEquals(1234, query.getParamValues()[0]);
		assertEquals("%text%", query.getParamValues()[1]);
		assertEquals(true, query.getParamValues()[2]);

		hql = " something    like     ?  sthg others >= ? or sthg else LIKE ?";
		
		query = new PositionalQuery(hql, values, true);
		assertNotNull(query.getParamValues());
		assertEquals("%1234%", query.getParamValues()[0]);
		assertEquals("text", query.getParamValues()[1]);
		assertEquals("%true%", query.getParamValues()[2]);
	}

	public void testLikeParameterDecorationInNamedQuery() {
		
		String[] names = new String[] {"name", "other", "none"};
		Object[] values = new Object[] {1234, "text", true};
		
		String hql = " something    >     :name  sthg others = :other or sthg else < :none";

		NamedQuery query = new NamedQuery(hql, names, values, true);
		assertNotNull(query.getParamValues());
		assertEquals(1234, query.getParamValues()[0]);
		assertEquals("text", query.getParamValues()[1]);
		assertEquals(true, query.getParamValues()[2]);
		
		hql = " something    <>     :name  sthg others like :other or sthg else <= :none";
		
		query = new NamedQuery(hql, names, values, true);
		assertNotNull(query.getParamValues());
		assertEquals(1234, query.getParamValues()[0]);
		assertEquals("%text%", query.getParamValues()[1]);
		assertEquals(true, query.getParamValues()[2]);

		hql = " something    like     :name  sthg others >= :other or sthg else LIKE :none";
		
		query = new NamedQuery(hql, names, values, true);
		assertNotNull(query.getParamValues());
		assertEquals("%1234%", query.getParamValues()[0]);
		assertEquals("text", query.getParamValues()[1]);
		assertEquals("%true%", query.getParamValues()[2]);
		
	}
	
	public void testConversionOfNamedQueryToPositionalQuery() {
		
		String hql = "some parameterless query";
		
		String[] names = null;
		Object[] values = null;
		
		NamedQuery query = new NamedQuery(hql, names, values);
		PositionalQuery positionalQuery = query.getPositionalQuery();
		assertNotNull(positionalQuery);
		assertEquals(hql, positionalQuery.getQueryText());
		Object[] paramValues = positionalQuery.getParamValues();
		assertNotNull(paramValues);
		assertEquals(0, paramValues.length);
		
		hql = "some parameterless query";

		names = new String[] {};
		values = new Object[] {};
		
		query = new NamedQuery(hql, names, values);
		positionalQuery = query.getPositionalQuery();
		assertNotNull(positionalQuery);
		assertEquals(hql, positionalQuery.getQueryText());
		paramValues = positionalQuery.getParamValues();
		assertNotNull(paramValues);
		assertEquals(0, paramValues.length);
		
		hql = " something    <>     :name  ";
		String hql2 = "something    <>     ?";
		
		names = new String[] {"name"};
		values = new Object[] {1234};
		
		query = new NamedQuery(hql, names, values);
		positionalQuery = query.getPositionalQuery();
		assertNotNull(positionalQuery);
		assertEquals(hql2, positionalQuery.getQueryText());
		paramValues = positionalQuery.getParamValues();
		assertNotNull(paramValues);
		assertEquals(1, paramValues.length);
		assertEquals(1234, paramValues[0]);
		
		hql = " something = :name  sthg others >= :other or sthg else LIKE :name and other <> :other";
		hql2 = "something = ?  sthg others >= ? or sthg else LIKE ? and other <> ?";
		
		names = new String[] {"name", "other"};
		values = new Object[] {1234, "text"};
		
		query = new NamedQuery(hql, names, values);
		positionalQuery = query.getPositionalQuery();
		assertNotNull(positionalQuery);
		assertEquals(hql2, positionalQuery.getQueryText());
		paramValues = positionalQuery.getParamValues();
		assertNotNull(paramValues);
		assertEquals(4, paramValues.length);
		assertEquals(1234, paramValues[0]);
		assertEquals("text", paramValues[1]);
		assertEquals(1234, paramValues[2]);
		assertEquals("text", paramValues[3]);
		
	}
	
	public void testConversionFromPositionalToNamedQuery() {
		
		PositionalQuery query = new PositionalQuery("from x", null);
		NamedQuery namedQuery = query.getNamedQuery();
		assertNotNull(namedQuery);
		assertEquals("from x", namedQuery.getQueryText());
		assertEquals(0, namedQuery.getParamNames().length);
		assertEquals(0, namedQuery.getParamValues().length);
		
		query = new PositionalQuery("from x", new Object[] {});
		namedQuery = query.getNamedQuery();
		assertNotNull(namedQuery);
		assertEquals("from x", namedQuery.getQueryText());
		assertEquals(0, namedQuery.getParamNames().length);
		assertEquals(0, namedQuery.getParamValues().length);
		
		query = new PositionalQuery("from x where x > ?", new Object[] {1});
		namedQuery = query.getNamedQuery();
		assertNotNull(namedQuery);
		assertEquals("from x where x > :name1", namedQuery.getQueryText());
		assertEquals(1, namedQuery.getParamNames().length);
		assertEquals("name1", namedQuery.getParamNames()[0]);
		assertEquals(1, namedQuery.getParamValues().length);
		assertEquals(1, namedQuery.getParamValues()[0]);
		
		query = new PositionalQuery("from x where x > ? and y < ?", new Object[] {1, 2});
		namedQuery = query.getNamedQuery();
		assertNotNull(namedQuery);
		assertEquals("from x where x > :name1 and y < :name2", namedQuery.getQueryText());
		assertEquals(2, namedQuery.getParamNames().length);
		assertEquals("name1", namedQuery.getParamNames()[0]);
		assertEquals("name2", namedQuery.getParamNames()[1]);
		assertEquals(2, namedQuery.getParamValues().length);
		assertEquals(1, namedQuery.getParamValues()[0]);
		assertEquals(2, namedQuery.getParamValues()[1]);
	}
}
