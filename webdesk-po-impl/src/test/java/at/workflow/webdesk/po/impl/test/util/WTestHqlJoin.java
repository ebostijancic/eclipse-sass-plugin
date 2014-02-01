package at.workflow.webdesk.po.impl.test.util;

import java.util.List;

import at.workflow.webdesk.WebdeskApplicationContext;
import at.workflow.webdesk.po.daos.PoGeneralDAO;
import at.workflow.webdesk.po.impl.helper.PoDataGenerator;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoPersonGroup;
import at.workflow.webdesk.tools.testing.AbstractTransactionalSpringHsqlDbTestCase;
import at.workflow.webdesk.tools.testing.DataGenerator;

/**
 * Shows what Hibernate 3.3.2 returns for HQL join queries.
 * 
 * @author fritzberger 2013-11-07
 */
public class WTestHqlJoin extends AbstractTransactionalSpringHsqlDbTestCase
{
	@Override
	protected final DataGenerator[] getDataGenerators() {
		return new DataGenerator[] { new PoDataGenerator("at/workflow/webdesk/po/impl/test/data/testdata.xml", false) };
	}

	public void testHqlJoin()	{
		// the expected structure of the result returned by HQL query is:
		// array of objects of following classes
		final Class<?> [] expectedResultClasses = new Class<?> []	{
				PoPerson.class, PoPersonGroup.class, PoGroup.class
		};
		
		final String query1 =	// returns List of array[3], each holding PoPerson, PoPersonGroup, PoGroup
				"from PoPerson p, PoPersonGroup pg, PoGroup g where pg.group = g and pg.person = p order by p.lastName, p.firstName";
		final List<?> result1 = runHql(query1, expectedResultClasses);
		
		final String query2 =	// other query, but returns the same result with same structure
				"from PoPerson p join p.memberOfGroups pg join pg.group order by p.lastName, p.firstName";
		final List<?> result2 = runHql(query2, expectedResultClasses);
		
		assertEquals(result1.size(), result2.size());
		
		for (int i = 0; i < result1.size(); i++)	{
			Object [] array1 = (Object []) result1.get(i);
			Object [] array2 = (Object []) result2.get(i);
			
			for (int j = 0; j < array1.length; j++)	{
				Object domainObject1 = array1[j];
				Object domainObject2 = array2[j];
				assertEquals(domainObject1, domainObject2);
			}
		}
	}

	private List<?> runHql(String query, Class<?> [] expectedClasses)	{
		final PoGeneralDAO dao = (PoGeneralDAO) WebdeskApplicationContext.getBean("PoGeneralDAO");
		final List<?> result = dao.find(query, null, null, -1, Integer.MAX_VALUE, true);
		
		//System.err.println("Result record count: "+result.size());
		
		for (Object o : result)	{
			Object [] array = (Object []) o;
			
			assertEquals(expectedClasses.length, array.length);
			for (int i = 0; i < expectedClasses.length; i++)
				assertTrue(array[i].getClass().equals(expectedClasses[i]));
			
			//System.err.println(Arrays.asList(array));
		}
		
		return result;
	}

}
