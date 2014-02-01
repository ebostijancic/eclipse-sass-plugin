package at.workflow.webdesk.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

public class WTestBeanAccessor extends TestCase
{
	public void testAccessByDottedPropertyNames()	{
		final BeanAccessor accessor = new BeanAccessor(A.class);
		
		// test retrieving types and nested types
		assertEquals(B.class, accessor.getType("b"));
		assertEquals(Date.class, accessor.getType("b.date"));
		assertEquals(C.class, accessor.getType("b.c"));
		assertEquals(boolean.class, accessor.getType("b.c.bool"));
		
		final A a = new A();
		
		// test retrieving values
		assertTrue(accessor.existsProperty("b"));
		assertNull(accessor.getValue(a, "b"));
		
		// test that certain properties do not exist
		try	{
			accessor.getValue(a, "x");
			fail("X must not be among properties, has no setter");
		}
		catch (IllegalArgumentException e)	{
		}
		assertFalse(accessor.existsProperty("x"));
		assertFalse(accessor.existsProperty("y"));
		
		// test setting values
		final B b = new B();
		accessor.setValue(a, "b", b);
		assertEquals(b, a.getB());
		assertEquals(b, accessor.getValue(a, "b"));
		
		// test setting nested values
		final C c = new C();
		accessor.setBean(a);
		accessor.setValue("b.c", c);
		assertEquals(c, a.getB().getC());
		assertEquals(c, accessor.getValue("b.c"));
		
		final boolean bool = true;
		accessor.setValue("b.c.bool", bool);
		assertEquals(bool, a.getB().getC().getBool());
		assertEquals(c.getBool(), accessor.getValue("b.c.bool"));
		
		try	{
			accessor.getValue(a, "b.");
			fail("Illegal property expression 'b.' must cause exception!");
		}
		catch (IllegalArgumentException e)	{
			// is expected here
		}
		
		try	{
			accessor.getValue(a, ".b");
			fail("Illegal property expression '.b' must cause exception!");
		}
		catch (IllegalArgumentException e)	{
			// is expected here
		}
		
		try	{
			accessor.getValue(a, "");
			fail("Illegal property expression '' must cause exception!");
		}
		catch (IllegalArgumentException e)	{
			// is expected here
		}
		
		try	{
			accessor.getValue(a, null);
			fail("Illegal property expression 'null' must cause exception!");
		}
		catch (IllegalArgumentException e)	{
			// is expected here
		}
	}
	
	public void testPropertyAccessByIndex()	{
		final A a = new A();
		a.setCollectionOfC(new ArrayList<C>());
		
		final C firstElement = new C();
		a.getCollectionOfC().add(firstElement);
		final C secondElement = new C();
		a.getCollectionOfC().add(secondElement);
		final C thirdElement = new C();
		thirdElement.setBool(true);
		a.getCollectionOfC().add(thirdElement);
		
		final BeanAccessor accessor = new BeanAccessor(A.class);
		assertEquals(firstElement, accessor.getValue(a, "collectionOfC[0]"));
		assertEquals(secondElement, accessor.getValue(a, "collectionOfC[1]"));
		assertEquals(thirdElement, accessor.getValue(a, "collectionOfC[2]"));
		
		assertNotSame(firstElement, secondElement);
		assertNotSame(firstElement, thirdElement);
		
		assertEquals(false, accessor.getValue(a, "collectionOfC[0].bool"));
		assertEquals(false, accessor.getValue(a, "collectionOfC[1].bool"));
		assertEquals(true, accessor.getValue(a, "collectionOfC[2].bool"));
		
		try	{
			accessor.getValue(a, "collectionOfC[3]");
			fail("Illegal property index '3' must cause exception!");
		}
		catch (NoSuchElementException e)	{
			// is expected here
		}
	}
		
	public void testPropertyAccessByIndex2()	{
		final IndexTest indexTest = new IndexTest();
		indexTest.setCollectionOfA(new ArrayList<A>());
		
		final A a1 = new A();
		indexTest.getCollectionOfA().add(a1);
		
		final A a2 = new A();
		a2.setCollectionOfC(new ArrayList<C>());
		final C c1 = new C();
		a2.getCollectionOfC().add(c1);
		final C c2 = new C();
		c2.setBool(true);
		a2.getCollectionOfC().add(c2);
		indexTest.getCollectionOfA().add(a2);
		
		final A a3 = new A();
		indexTest.getCollectionOfA().add(a3);
		
		final BeanAccessor accessor = new BeanAccessor(indexTest);
		assertEquals(c1, accessor.getValue("collectionOfA[1].collectionOfC[0]"));
		assertEquals(c2, accessor.getValue("collectionOfA[1].collectionOfC[1]"));
		assertEquals(false, accessor.getValue("collectionOfA[1].collectionOfC[0].bool"));
		assertEquals(true, accessor.getValue("collectionOfA[1].collectionOfC[1].bool"));
		
		indexTest.setIndexTest(indexTest);
		assertEquals(indexTest, accessor.getValue("indexTest"));
		assertEquals(c1, accessor.getValue("indexTest.collectionOfA[1].collectionOfC[0]"));
		assertEquals(c2, accessor.getValue("indexTest.collectionOfA[1].collectionOfC[1]"));
	}
	
	public void testAccessByType()	{
		final BeanAccessor accessorA = new BeanAccessor(A.class);
		final List<String> scannedPropertiesA = accessorA.getPropertyNames();
		final List<String> propertiesA = Arrays.asList(new String []	{
				"b", "int", "double", "collectionOfC", "mapOfC"
		});
		assertSameContents(propertiesA, scannedPropertiesA);
		
		final BeanAccessor accessorB = new BeanAccessor(B.class);
		final List<String> dateProperties = Arrays.asList(new String []	{
				"date", 
		});
		final List<String> scannedDateProperties = accessorB.getPropertiesAssignableToType(Date.class, true);
		assertSameContents(dateProperties, scannedDateProperties);
		
		final List<String> collectionAndMapProperties = Arrays.asList(new String []	{
				"collectionOfC", "mapOfC"
		});
		final List<String> scannedCollectionProperties = accessorA.getPropertiesAssignableToTypes(
				new Class<?> [] { Collection.class, Map.class },
				true);
		assertSameContents(collectionAndMapProperties, scannedCollectionProperties);
		
		final List<String> nonCollectionAndMapProperties = Arrays.asList(new String []	{
				"b", "int", "double"
		});
		final List<String> scannedNonCollectionProperties = accessorA.getPropertiesAssignableToTypes(
				new Class<?> [] { Collection.class, Map.class },
				false);
		assertSameContents(nonCollectionAndMapProperties, scannedNonCollectionProperties);
	}

	
	private void assertSameContents(final List<String> properties, final List<String> scannedProperties) {
		assertEquals(properties.size(), scannedProperties.size());
		for (String property : properties)
			assertTrue(scannedProperties.contains(property));
	}
	
	
	// test classes
	
	private static class Base
	{
		private int i;
		private Double d;
		private B b;
			
		public int getInt()	{
			return i;
		}
		public void setInt(int i)	{
			this.i = i;
		}
		public Double getDouble()	{
			return d;
		}
		public void setDouble(Double d)	{
			this.d = d;
		}
		public B getB() {
			return b;
		}
		public void setB(B b) {
			this.b = b;
		}
	}
	
	private static class A extends Base
	{
		private Collection<C> collectionOfC;
		private Map<String,C> mapOfC;
		
		public Collection<C> getCollectionOfC()	{
			return collectionOfC;
		}
		public void setCollectionOfC(Collection<C> collectionOfC)	{
			this.collectionOfC = collectionOfC;
		}
		public Map<String,C> getMapOfC()	{
			return mapOfC;
		}
		public void setMapOfC(Map<String,C> mapOfC)	{
			this.mapOfC = mapOfC;
		}
		public Object getX()	{
			return null;
		}
		public void setUnknown()	{
		}
	}
	
	private static class B
	{
		private Date d;
		private String n;
		private C c;
			
		public Date getDate()	{
			return d;
		}
		public void setDate(Date d)	{
			this.d = d;
		}
		public String getName()	{
			return n;
		}
		public void setName(String n)	{
			this.n = n;
		}
		public C getC() {
			return c;
		}
		public void setC(C c) {
			this.c = c;
		}
	}
	
	private static class C
	{
		private boolean b;
		
		public boolean getBool()	{
			return b;
		}
		public void setBool(boolean b)	{
			this.b = b;
		}
	}
	
	
	class IndexTest
	{
		private Collection<A> collectionOfA;
		private IndexTest indexTest;

		public Collection<A> getCollectionOfA() {
			return collectionOfA;
		}

		public void setCollectionOfA(Collection<A> collectionOfA) {
			this.collectionOfA = collectionOfA;
		}

		public IndexTest getIndexTest() {
			return indexTest;
		}

		public void setIndexTest(IndexTest indexTest) {
			this.indexTest = indexTest;
		}
	}
}
