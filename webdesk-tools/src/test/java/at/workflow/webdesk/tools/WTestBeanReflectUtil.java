package at.workflow.webdesk.tools;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import at.workflow.webdesk.tools.BeanReflectUtil.BeanProperty;

/**
 * Tests basic reflection capacities.
 * 
 * @author fritzberger 15.10.2010
 */
public class WTestBeanReflectUtil extends TestCase {
	
	/** Tests retrieving properties by name. A property always has both setter and getter, and both must be public. */
	public void testReadBeanFields()	{
		class A	{
			public int getInt()	{
				return 1234;
			}
			public void setInt(int i)	{
			}
			
			public String getString()	{
				return "dummy";
			}
			public void setString(String s)	{
			}
		};
		
		class B	extends A {
			public Date getDate()	{
				return new Date();
			}
			public void setDate(java.sql.Date d)	{
			}
			
			// must not be found
			public double getLong()	{
				return 1000L;
			}
			
			// must not be found
			private boolean getBoolean()	{
				return true;
			}
			
			// must not be found
			private void setBoolean(boolean b)	{
			}
			
			// must not be found
			public void setA(A a)	{
			}
		};
		
		List<BeanReflectUtil.BeanProperty> basenames = BeanReflectUtil.properties(B.class);
		
		assertTrue(basenames.size() == 3);
		assertTrue(contains(basenames, "Int"));
		assertTrue(contains(basenames, "String"));
		assertTrue(contains(basenames, "Date"));
	}

	private boolean contains(List<BeanProperty> properties, String name) {
		name = ReflectionUtils.getFirstCharLowerCasePropertyName(name);
		for (BeanReflectUtil.BeanProperty p : properties)	{
			if (name.equals(p.propertyName))
				return true;
		}
		return false;
	}
	
	
	/** Test resolving property names like "person.lastName". */
	public void testReferenceResolution()	{
		class B
		{
			public String getLastName()	{
				return "last name";
			}
			public void setLastName(String lastName)	{
			}
		};
		
		class A
		{
			public B getPerson()	{
				return new B();
			}
			public void setPerson(B person)	{
			}
		};
		
		Class<?> ownerClass = BeanReflectUtil.getOwnerBeanClass(A.class, "person.lastName");
		assertNotNull(ownerClass);
		assertEquals(B.class, ownerClass);
	}
	

}
