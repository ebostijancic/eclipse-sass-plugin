package at.workflow.webdesk.tools.hibernate;

import junit.framework.TestCase;

/**
 * @author ggruber 03.2013
 * @author fritzberger 18.03.2013, 17.12.2013
 */
public class WTestHqlUtils extends TestCase {
	
	public void testExtractWhereClause() {
		
		String hql, where;
		
		hql = null;
		assertEquals("", HqlUtils.extractWhereClause(hql));
		
		hql = "";
		assertEquals("", HqlUtils.extractWhereClause(hql));
		
		where = "where firstName='Hugo'";
		hql = "select firstName, lastName from PoPerson "+where;
		assertEquals(where, HqlUtils.extractWhereClause(hql));
		
		where = where.toUpperCase();
		hql = hql.toUpperCase();
		assertEquals(where, HqlUtils.extractWhereClause(hql));
		
		hql = "SELECT Customer,SUM(OrderPrice) FROM Orders GROUP BY Customer HAVING SUM(OrderPrice)<2000";
		assertEquals("", HqlUtils.extractWhereClause(hql));
		
		where = "WHERE Customer='Hansen' OR Customer='Jensen'";
		hql = "SELECT Customer,SUM(OrderPrice)\n"+
			"FROM Orders\n" +
			where+"\n" + 
			"GROUP BY Customer\n" +
			"HAVING SUM(OrderPrice) > 1500";
		assertEquals(where, HqlUtils.extractWhereClause(hql));
		
		hql = "select cat.weight + sum(kitten.weight) from Cat cat join cat.kittens kitten group by cat.id, cat.weight order by cat.weight";
		assertEquals("", HqlUtils.extractWhereClause(hql));
		
		where = "where foo.startDate = bar.date";
		hql = "select foo from Foo foo, Bar bar "+where+" order by foo.name";
		assertEquals(where, HqlUtils.extractWhereClause(hql));
		
		where = "where account.owner.id.country = 'AU' and account.owner.id.medicareNumber = 123456";
		hql = "from bank.Account account "+where;
		assertEquals(where, HqlUtils.extractWhereClause(hql));
	}
	
	public void testRemoveWhereClause() {
		
		assertEquals(null, HqlUtils.removeWhereClause(null));
		assertEquals(" ", HqlUtils.removeWhereClause(" "));
		
		String hql, where;

		hql = "from PoPerson";
		assertEquals(hql, HqlUtils.removeWhereClause(hql));
		
		hql = "select firstName, lastName from PoPerson";
		where = "where firstName='Hugo'";
		assertEquals(hql, HqlUtils.removeWhereClause(hql+" "+where));
		
	}
	
	public void testReplaceWhereClause() {
		
		assertEquals(null, HqlUtils.replaceWhereClause(null, null));
		assertEquals("     ", HqlUtils.replaceWhereClause("     ", null));

		String hql, where, newWhere, groupBy, orderBy;
		
		hql = "from PoPerson";
		where = "where firstName='Hugo'";
		assertEquals(hql+" "+where, HqlUtils.replaceWhereClause(hql, where));
		
		where = "where firstName='Hugo'";
		newWhere = "where oe=1 and zu=3";
		assertEquals(hql+" "+newWhere, HqlUtils.replaceWhereClause(hql+" "+where, newWhere));
		
		where = "where oe=1 and zu=3";
		groupBy = "group by lastName";
		newWhere = "where firstName='Hugo'";
		assertEquals(hql+" "+newWhere+" "+groupBy, HqlUtils.replaceWhereClause(hql+" "+where+" "+groupBy, newWhere));
		
		where = "where oe=1 and zu=3";
		orderBy = "order by lastName";
		newWhere = "where firstName='Hugo'";
		assertEquals(hql+" "+newWhere+" "+orderBy, HqlUtils.replaceWhereClause(hql+" "+where+" "+orderBy, newWhere));
		
		where = "where oe = 1 and zu = 3";
		groupBy = "group by lastName";
		orderBy = "order by lastName";
		newWhere = "where firstName = 'Hugo'";
		assertEquals(hql+" "+newWhere+" "+groupBy+" "+orderBy, HqlUtils.replaceWhereClause(hql+" "+where+" "+groupBy+" "+orderBy, newWhere));
		
		hql = "select whereToPay, nowhere from PoPerson";
		where = "where orders = 1 and groups = 3";
		groupBy = "group by lastName";
		orderBy = "order by lastName";
		newWhere = "where firstName = 'Hugo'";
		assertEquals(hql+" "+newWhere+" "+groupBy+" "+orderBy, HqlUtils.replaceWhereClause(hql+" "+where+" "+groupBy+" "+orderBy, newWhere));
		
		hql = "from PoPerson";
		orderBy = "order by lastName";
		newWhere = "where firstName = 'Hugo'";
		assertEquals(hql+" "+newWhere+" "+orderBy, HqlUtils.replaceWhereClause(hql+" "+orderBy, newWhere));
	}
	

	public void testExtractOrderByClause() {
		String hql, orderBy;
		
		hql = null;
		assertEquals("", HqlUtils.extractOrderByClause(hql));
		
		hql = "";
		assertEquals("", HqlUtils.extractOrderByClause(hql));
		
		orderBy = "order by lastName";
		hql = "select firstName, lastName from PoPerson "+orderBy;
		assertEquals(orderBy, HqlUtils.extractOrderByClause(hql));
		
		orderBy = "order by firstName, lastName, dateOfBirth";
		hql = "select * from PoPerson where orders = ? and byorder = ? group by lastName having orders < ? "+orderBy;
		assertEquals(orderBy, HqlUtils.extractOrderByClause(hql));
	}

	public void testCreateOrderByClause() {
		assertNull(HqlUtils.createOrderByClause(null, null));
		assertNull(HqlUtils.createOrderByClause(new String[0], new boolean[0]));
		
		String [] propertyIds;
		boolean [] ascending;
		
		propertyIds = new String [] { "firstName" };
		ascending = new boolean [] { true };
		assertEquals("order by firstName asc", HqlUtils.createOrderByClause(propertyIds, ascending));
		
		propertyIds = new String [] { "p.firstName", "p.lastName", "p.dateOfBirth" };
		ascending = new boolean [] { true, false, true };
		assertEquals("order by p.firstName asc, p.lastName desc, p.dateOfBirth asc", HqlUtils.createOrderByClause(propertyIds, ascending));
	}

	public void testReplaceOrderByClause() {
		assertEquals(null, HqlUtils.replaceOrderByClause(null, null));
		assertEquals(" ", HqlUtils.replaceOrderByClause(" ", null));
		
		String hql, newOrderBy;
		
		newOrderBy = "order by firstName";
		hql = "select firstName, lastName from PoPerson";
		assertEquals(hql+" "+newOrderBy, HqlUtils.replaceOrderByClause(hql+" order by lastName", newOrderBy));
		
		newOrderBy = "order by orderP.firstName, porder.lastName, porder.dateOfBirth";
		hql = "select * from PoPerson orderP where orderP.orders = ? and orderP.byorder = ? group by orderP.lastName having orders < ?";
		assertEquals(hql+" "+newOrderBy, HqlUtils.replaceOrderByClause(hql, newOrderBy));
	}

	
	
	public void testExtractFromClause() {
		final String query = "from PoPerson";
		final String from = HqlUtils.extractFromClause(query);
		assertEquals(query, from);
		
		final String query1 = "select lastName, firstName from PoPerson order by lastName, firstName";
		final String from1 = HqlUtils.extractFromClause(query1);
		assertEquals("from PoPerson", from1);
		
		final String query2 = "select p.lastName, p.firstName from PoPerson p order by p.lastName, p.firstName";
		final String from2 = HqlUtils.extractFromClause(query2);
		assertEquals("from PoPerson p", from2);
		
		final String query3 = "select p.lastName, p.firstName from PoPerson p where true group by p.dateOfBirth order by p.lastName, p.firstName";
		final String from3 = HqlUtils.extractFromClause(query3);
		assertEquals("from PoPerson p", from3);
	}
	
	
	public void testExtractSelectClause() {
		final String query = "from PoPerson";
		final String select = HqlUtils.extractSelectClause(query);
		assertEquals("", select);
		
		final String query1 = "select lastName, firstName from PoPerson order by lastName, firstName";
		final String select1 = HqlUtils.extractSelectClause(query1);
		assertEquals("select lastName, firstName", select1);
		
		final String query2 = "select p.lastName, p.firstName from PoPerson p group by p.lastName, p.firstName";
		final String select2 = HqlUtils.extractSelectClause(query2);
		assertEquals("select p.lastName, p.firstName", select2);
	}
	
	
	public void testTokenizeSelectClause()	{
		final String query =	// this query is not according to Webdesk database!
				" select  p.fromName ,p.orderName , \t pg.selectName \r\n  "+
				"from  PoPerson  as  p ,  PoPersonGroup  pg \t ,PoOrgStructure   os\n"+
				"where p.group = pg and pg.orgStructure = os ";
		
		final String select = HqlUtils.extractSelectClause(query);
		final String [] selectTokens = HqlUtils.tokenizeSelectClause(select);
		assertEquals(3, selectTokens.length);
		assertEquals("p.fromName", selectTokens[0]);
		assertEquals("p.orderName", selectTokens[1]);
		assertEquals("pg.selectName", selectTokens[2]);
	}
	
	public void testTokenizeOrderByClause()	{
		final String query =	// this query is not according to Webdesk database!
				" select   * \r\n  "+
				"from  PoPerson  as  p ,  PoPersonGroup  pg \t ,PoOrgStructure   os\n"+
				"order by p.lastName asc, p.firstName, p.userName desc";
		
		final String orderBy = HqlUtils.extractOrderByClause(query);
		final String [] orderByTokens = HqlUtils.tokenizeOrderByClause(orderBy);
		assertEquals(3, orderByTokens.length);
		assertEquals("p.lastName asc", orderByTokens[0]);
		assertEquals("p.firstName", orderByTokens[1]);
		assertEquals("p.userName desc", orderByTokens[2]);
	}
	
	public void testTokenizeFromClause()	{
		final String JOIN = "p.client c with (c.active = 1 or c.active = 2) \n   join c.textModule as tm with tm.valid = 1 and c.active = 1";
		final String query =	// this query is not according to Webdesk database!
				" select  p.fromName ,p.orderName , \t pg.selectName \r\n  "+
				"from  PoPerson  as  p ,  PoPersonGroup  pg \t ,PoOrgStructure   os\n   left outer join "+
				"p.client c with (c.active = 1 or c.active = 2) \n   join c.textModule as tm with tm.valid = 1 and c.active = 1"+
				",PoGroup g\r\n"+
				"order by p.lastName";
		
		final String from = HqlUtils.extractFromClause(query);
		final String [][] fromParts = HqlUtils.splitFromClause(from);
		assertEquals(4, fromParts.length);
		assertEquals("PoPerson p", fromParts[0][0]);
		assertEquals("", fromParts[0][1]);
		assertEquals("PoPersonGroup pg", fromParts[1][0]);
		assertEquals("", fromParts[1][1]);
		assertEquals("PoOrgStructure os", fromParts[2][0]);
		final String join = fromParts[2][1];
		assertEquals(JOIN, join);
		assertEquals("PoGroup g", fromParts[3][0]);
		assertEquals("", fromParts[3][1]);
		
		final String [] joinParts = HqlUtils.tokenizeJoins(join);
		assertEquals(2, joinParts.length);
		assertEquals("p.client c", joinParts[0]);
		assertEquals("c.textModule tm", joinParts[1]);
	}
	
	public void testSplitBy() {
		final String query = "from PoPerson";
		final String [] parts = HqlUtils.splitBy(query, HqlUtils.FROM);
		assertNotNull(parts);
		assertEquals("", parts[0]);
		assertEquals(query, parts[1]);
		
		final String query1Part1 = "select p.select, p.from, p.group, p.order";
		final String query1Part2 = "from PoPerson p";
		final String query1 = query1Part1+" "+query1Part2;
		final String [] parts1 = HqlUtils.splitBy(query1, HqlUtils.FROM);
		assertNotNull(parts1);
		assertEquals(query1Part1, parts1[0]);
		assertEquals(query1Part2, parts1[1]);
		
		final String query2 = "from HrPerson hrp join hrp.person.memberOfGroups left outer join hrpp.emplyomentState";
		final String [] parts2 = HqlUtils.splitBy(query2, HqlUtils.FROM);
		assertNotNull(parts2);
		assertEquals("", parts2[0]);
		assertEquals(query2, parts2[1]);
	}

}
