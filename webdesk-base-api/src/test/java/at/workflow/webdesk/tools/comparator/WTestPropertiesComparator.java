package at.workflow.webdesk.tools.comparator;

import java.util.List;

import junit.framework.TestCase;
import at.workflow.webdesk.tools.BeanReflectUtil;
import at.workflow.webdesk.tools.BeanReflectUtil.BeanProperty;
import at.workflow.webdesk.tools.comparator.PropertiesComparator;

/**
 * @author sdzuban 26.03.2013
 */
public class WTestPropertiesComparator extends TestCase {

	public static class TestClass  {

		private String first;
		private String second;
		
		public TestClass(String first, String second) {
			super();
			this.first = first;
			this.second = second;
		}
		
		public String getFirst() {
			return first;
		}
		public void setFirst(String first) {
			this.first = first;
		}
		public String getSecond() {
			return second;
		}
		public void setSecond(String second) {
			this.second = second;
		}
	}
	
	private TestClass bean1;
	private TestClass bean2;
	private List<BeanProperty> properties =
			BeanReflectUtil.properties(TestClass.class, new String[] {"first"}, new String[] {});

	/** {@inheritDoc} */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		bean1	= new TestClass("a", "b");
		bean2	= new TestClass("a", "b");
	}
	
	public void testNullAll() {
		
		assertTrue(PropertiesComparator.isEqual(null, null));
		assertFalse(PropertiesComparator.isEqual(null, bean1));
		assertFalse(PropertiesComparator.isEqual(bean1, null));
		
		assertFalse(PropertiesComparator.isNotEqual(null, null));
		assertTrue(PropertiesComparator.isNotEqual(null, bean1));
		assertTrue(PropertiesComparator.isNotEqual(bean1, null));
	}
	
	public void testNullExcept() {
		
		assertTrue(PropertiesComparator.isEqual(null, null, "first"));
		assertFalse(PropertiesComparator.isEqual(null, bean1, "first"));
		assertFalse(PropertiesComparator.isEqual(bean1, null, "first"));
		
		assertFalse(PropertiesComparator.isNotEqual(null, null, "first"));
		assertTrue(PropertiesComparator.isNotEqual(null, bean1, "first"));
		assertTrue(PropertiesComparator.isNotEqual(bean1, null, "first"));
	}
	
	public void testNullProperties() {
		
		assertTrue(PropertiesComparator.isEqual(null, null, properties));
		assertFalse(PropertiesComparator.isEqual(null, bean1, properties));
		assertFalse(PropertiesComparator.isEqual(bean1, null, properties));
	}
	
	public void testSameAll() {
		
		assertTrue(PropertiesComparator.isEqual(bean1, bean2));
		
		assertFalse(PropertiesComparator.isNotEqual(bean1, bean2));
	}
	
	public void testSameExcept() {
		
		assertTrue(PropertiesComparator.isEqual(bean1, bean2, "first"));
		assertFalse(PropertiesComparator.isNotEqual(bean1, bean2, "first"));
		bean2.setFirst("c");
		assertTrue(PropertiesComparator.isEqual(bean1, bean2, "first"));
		assertFalse(PropertiesComparator.isNotEqual(bean1, bean2, "first"));
	}
	
	public void testSameProperties() {
		
		assertTrue(PropertiesComparator.isEqual(bean1, bean2, properties));
		bean2.setFirst("c");
		assertTrue(PropertiesComparator.isEqual(bean1, bean2, properties));
	}
	
	public void testDifferentAll() {
		
		bean2.setFirst("c");
		assertFalse(PropertiesComparator.isEqual(bean1, bean2));
		assertTrue(PropertiesComparator.isNotEqual(bean1, bean2));
		bean2.setFirst("a");
		bean2.setSecond("d");
		assertFalse(PropertiesComparator.isEqual(bean1, bean2));
		assertTrue(PropertiesComparator.isNotEqual(bean1, bean2));
		bean2.setFirst("c");
		assertFalse(PropertiesComparator.isEqual(bean1, bean2));
		assertTrue(PropertiesComparator.isNotEqual(bean1, bean2));
	}
	
	public void testDifferentExcept() {
		
		bean2.setSecond("d");
		assertFalse(PropertiesComparator.isEqual(bean1, bean2, "first"));
		assertTrue(PropertiesComparator.isNotEqual(bean1, bean2, "first"));
	}
	
	public void testDifferentProperties() {
		
		bean2.setSecond("d");
		assertFalse(PropertiesComparator.isEqual(bean1, bean2, properties));
	}
}
