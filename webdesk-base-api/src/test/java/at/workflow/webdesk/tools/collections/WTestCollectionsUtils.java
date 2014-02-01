package at.workflow.webdesk.tools.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * @author sdzuban 27.11.2012
 */
public class WTestCollectionsUtils {

	public class TestClass {
		private boolean ok;
		private String name;
		
		public TestClass(boolean ok, String name) {
			this.ok = ok;
			this.name = name;
		}

		public boolean isOk() {
			return ok;
		}
		public void setOk(boolean ok) {
			this.ok = ok;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	}
	
	public class TestService {
		public String makeString(Integer i) {
			return "string_" + i;
		}
	}
	
	private Collection<TestClass> result;
	
	@Test
	public void testGetFirstNotNullElement() {
		
		assertNull(CollectionsUtils.getFirstNotNullElement(null));
		assertNull(CollectionsUtils.getFirstNotNullElement(Collections.emptyList()));
		assertNull(CollectionsUtils.getFirstNotNullElement(Arrays.asList(null, null, null)));
		assertEquals("first", CollectionsUtils.getFirstNotNullElement(Arrays.asList(null, null, "first")));
		assertEquals("first", CollectionsUtils.getFirstNotNullElement(Arrays.asList(null, null, "first", "second")));
	}
	
	@Test
	public void testGetAllNotNullElement() throws InstantiationException, IllegalAccessException {
		
		Collection<String> result = CollectionsUtils.getAllNotNullElements(null);
		assertNull(result);
		
		List<String> input = new ArrayList<String>();
		result = CollectionsUtils.getAllNotNullElements(input);
		assertNotNull(result);
		assertTrue(result.isEmpty());

		input.add(null); input.add(null); input.add(null);
		result = CollectionsUtils.getAllNotNullElements(input);
		assertNotNull(result);
		assertTrue(result.isEmpty());
		
		input.add("first");
		result = CollectionsUtils.getAllNotNullElements(input);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		input.add("second");
		result = CollectionsUtils.getAllNotNullElements(input);
		assertNotNull(result);
		assertEquals(2, result.size());
		assertTrue(result.contains("first"));
		assertTrue(result.contains("second"));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFilterCollectionNullsAndEmpty() throws Exception {
		
		result = CollectionsUtils.filterCollection(null, "test", null);
		assertNull(result);
		
		result = CollectionsUtils.filterCollection(new HashSet(), "test", null);
		assertNotNull(result);
		assertTrue(result instanceof HashSet);
		assertTrue(result.isEmpty());
		
		result = CollectionsUtils.filterCollection(new ArrayList(), "test", null);
		assertNotNull(result);
		assertTrue(result instanceof ArrayList);
		assertTrue(result.isEmpty());
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testFilterCollectionList() throws Exception {
		
		List input = new ArrayList();
		input.add(new TestClass(true, "one")); input.add(new TestClass(false, "two")); input.add(new TestClass(true, null));
		
		result = CollectionsUtils.<TestClass>filterCollection(input, "ok", true);
		assertNotNull(result);
		assertEquals(2, result.size());
		Iterator<TestClass> it = result.iterator();
		assertTrue(it.next().isOk());
		assertTrue(it.next().isOk());
		
		result = CollectionsUtils.filterCollection(input, "name", null);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertNull(result.iterator().next().getName());
		
		result = CollectionsUtils.filterCollection(input, "name", "one");
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("one", result.iterator().next().getName());
		
		result = CollectionsUtils.filterCollection(input, "name", "xx");
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testFilterCollectionSet() throws Exception {
		
		Set input = new HashSet();
		input.add(new TestClass(true, "one")); input.add(new TestClass(false, "two")); input.add(new TestClass(true, null));
		
		result = CollectionsUtils.filterCollection(input, "ok", true);
		assertNotNull(result);
		assertEquals(2, result.size());
		Iterator<?> it = result.iterator();
		assertTrue(((TestClass) it.next()).isOk());
		assertTrue(((TestClass) it.next()).isOk());
		
		result = CollectionsUtils.filterCollection(input, "name", null);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertNull(result.iterator().next().getName());
		
		result = CollectionsUtils.filterCollection(input, "name", "one");
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("one", result.iterator().next().getName());
		
		result = CollectionsUtils.filterCollection(input, "name", "xx");
		assertNotNull(result);
		assertEquals(0, result.size());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testFilterCollectionNegative() throws Exception {
		
		Set input = new HashSet();
		input.add(new TestClass(true, "one")); input.add(new TestClass(false, "two")); input.add(new TestClass(true, null));
		
		result = CollectionsUtils.filterCollectionNegative(input, "ok", true);
		assertNotNull(result);
		assertEquals(1, result.size());
		Iterator<?> it = result.iterator();
		assertFalse(((TestClass) it.next()).isOk());
		
		result = CollectionsUtils.filterCollectionNegative(input, "name", null);
		assertNotNull(result);
		assertEquals(2, result.size());
		
		result = CollectionsUtils.filterCollectionNegative(input, "name", "xx");
		assertNotNull(result);
		assertEquals(3, result.size());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testApplyMethod() throws Exception {
		
		List input = new ArrayList();
		input.add(new TestClass(true, "one")); input.add(new TestClass(false, "two")); input.add(new TestClass(true, null));
		
		result = CollectionsUtils.applyMethod(input, "getName");
		assertNotNull(result);
		assertEquals(3, result.size());
		assertEquals("one", ((List) result).get(0));
		assertEquals("two", ((List) result).get(1));
		assertNull(((List) result).get(2));
		
		result = CollectionsUtils.applyMethod(input, "isOk");
		assertNotNull(result);
		assertEquals(3, result.size());
		assertTrue((Boolean) ((List) result).get(0));
		assertFalse((Boolean) ((List) result).get(1));
		assertTrue((Boolean) ((List) result).get(2));
		
	}
	
	@SuppressWarnings({ "rawtypes" })
	@Test
	public void testApplyMethodArray() throws Exception {
		
		TestClass[] in = new TestClass[]{new TestClass(true, "one"), new TestClass(false, "two"), new TestClass(true, null)};
		
		result = CollectionsUtils.applyMethod(in, "getName");
		assertNotNull(result);
		assertEquals(3, result.size());
		assertEquals("one", ((List) result).get(0));
		assertEquals("two", ((List) result).get(1));
		assertNull(((List) result).get(2));
		
		result = CollectionsUtils.applyMethod(in, "isOk");
		assertNotNull(result);
		assertEquals(3, result.size());
		assertTrue((Boolean) ((List) result).get(0));
		assertFalse((Boolean) ((List) result).get(1));
		assertTrue((Boolean) ((List) result).get(2));
		
	}
	
	@SuppressWarnings({ "rawtypes" })
	@Test
	public void testApplyServiceMethod() throws Exception {
		
		List<Integer> input = new ArrayList<Integer>();
		input.add(1); input.add(2); input.add(3);
		
		try {
			CollectionsUtils.applyServiceMethod(input, null, null);
			fail("Accepted null service");
		} catch (Exception e) {}
		
		TestService service = new TestService();
		
		try {
			CollectionsUtils.applyServiceMethod(input,service, null);
			fail("Accepted null method");
		} catch (Exception e) {}
		
		Collection<String> myResult = CollectionsUtils.applyServiceMethod(input, service, "makeString");
		assertNotNull(myResult);
		assertEquals(3, myResult.size());
		assertEquals("string_1", ((List) myResult).get(0));
		assertEquals("string_2", ((List) myResult).get(1));
		assertEquals("string_3", ((List) myResult).get(2));
		
	}
	
	@SuppressWarnings({ "rawtypes" })
	@Test
	public void testApplyServiceMethodArray() throws Exception {
		
		TestService service = new TestService();
		
		Collection<String> myResult = CollectionsUtils.applyServiceMethod(new Integer[] {1, 2, 3}, service, "makeString");
		assertNotNull(myResult);
		assertEquals(3, myResult.size());
		assertEquals("string_1", ((List) myResult).get(0));
		assertEquals("string_2", ((List) myResult).get(1));
		assertEquals("string_3", ((List) myResult).get(2));
		
	}
	
}
