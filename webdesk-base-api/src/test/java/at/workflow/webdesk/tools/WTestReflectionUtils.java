package at.workflow.webdesk.tools;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import at.workflow.webdesk.tools.api.PersistentObject;

public class WTestReflectionUtils extends TestCase {

	private static class SomeSuperClass	extends Object {
		public String getOver() {
			return "";
		}
		public Date getUnder() {
			return null;
		}
	}
	
	private static class SomeClass extends SomeSuperClass	{
		public int getInner() {
			return 1;
		}
		public void setOuter() {
		}
	}
	
	public void testGetAllMethodNames() {
		// ReflectionUtils.getAllMethodNames() finds all public methods, among them are 9 Object methods:
		// wait(), wait(long), wait(long, int), toString(), hashCode(), equals(Object), getClass(), notify(), notifyAll()
		final List<String> superMethodNames = ReflectionUtils.getAllMethodNames(SomeSuperClass.class);
		final List<String> classMethodNames = ReflectionUtils.getAllMethodNames(SomeClass.class);
		assertEquals(2, classMethodNames.size() - superMethodNames.size());
	}

	private interface SuperInterface
	{
	}
	
	private interface MyInterface
	{
	}
	
	private static class SuperClass implements SuperInterface
	{
	}
	
	private static class MyClass extends SuperClass implements MyInterface
	{
	}
	
	public void testGetAllImplementedInterfaces() {
		final List<Class> implementedInterfaces = ReflectionUtils.getAllImplementedInterfaces(MyClass.class.getName());
		assertNotNull(implementedInterfaces);
		assertTrue(implementedInterfaces.contains(MyInterface.class));
		assertTrue(implementedInterfaces.contains(SuperInterface.class));
	}
	
	
	private static class CollectionPropertiesHolder
	{
		public Collection<?> getCollection()	{
			return null;
		}
		public List<?> getList()	{
			return null;
		}
		public Map<?,?> getMap()	{
			return null;
		}
		public String [] getStringArray()	{
			return null;
		}
		public int [] getIntArray()	{
			return null;
		}
	}
	
	private static class StandardPropertiesHolder extends CollectionPropertiesHolder
	{
		private String myName;
		private boolean real;
		
		public String getName()	{
			return myName;
		}
		public void setName(String name)	{
			this.myName = name;
		}
		public boolean isReal()	{
			return real;
		}
		public void setReal(boolean real)	{
			this.real = real;
		}
		
		public CollectionPropertiesHolder getCollectionPropertiesHolder()	{
			return null;
		}
		public MyClass getMyClass()	{
			return null;
		}
	}
	
	public void testGetProperties() {
		
		final List<String> primitiveAndStrings = ReflectionUtils.getAllPrimitiveAndStringProperties(SomeClass.class);
		assertEquals(2, primitiveAndStrings.size());
		assertTrue("Erster Buchstabe muss klein sein", Character.isLowerCase(primitiveAndStrings.get(0).charAt(0)));

		final List<String> strings = ReflectionUtils.getAllStringProperties(File.class);
		assertTrue("Anzahl muss 5 sein, war aber " + strings.size(), strings.size() == 5);
		assertTrue("Erster Buchstabe muss klein sein", Character.isLowerCase(strings.get(0).charAt(0)));

		final List<String> beans = ReflectionUtils.getAllBeanProperties(Calendar.class);
		assertTrue("Anzahl muss 3 sein, war aber " + beans.size(), beans.size() == 3);
		assertTrue("Erster Buchstabe muss klein sein", Character.isLowerCase(beans.get(0).charAt(0)));

		final List<String> collections = ReflectionUtils.getAllCollectionProperties(CollectionPropertiesHolder.class);
		assertTrue("Anzahl muss 3 sein, war aber " + collections.size(), collections.size() == 3);
		assertTrue("Erster Buchstabe muss klein sein", Character.isLowerCase(collections.get(0).charAt(0)));

		final List<String> standardProperties = ReflectionUtils.getAllStandardProperties(StandardPropertiesHolder.class);
		assertTrue("Anzahl muss 2 sein, war aber " + standardProperties.size(), standardProperties.size() == 2);
		assertTrue("Erster Buchstabe muss klein sein", Character.isLowerCase(standardProperties.get(0).charAt(0)));

		final List<String> standardPropertiesAndGivenTypes = ReflectionUtils.getAllStandardProperties(StandardPropertiesHolder.class, Arrays.asList(new Class<?> [] { CollectionPropertiesHolder.class }));
		assertTrue("Anzahl muss 3 sein, war aber " + standardPropertiesAndGivenTypes.size(), standardPropertiesAndGivenTypes.size() == 3);
		assertTrue("Erster Buchstabe muss klein sein", Character.isLowerCase(standardPropertiesAndGivenTypes.get(0).charAt(0)));
	}

	
	public void testInvoke() throws Exception {
		StandardPropertiesHolder targetObject = new StandardPropertiesHolder();
		final String FRANZ = "Franz";
		final String PROPERTY_NAME = "name";

		ReflectionUtils.invokeSetter(targetObject, PROPERTY_NAME, FRANZ, String.class);
		assertEquals(FRANZ, targetObject.getName());
		
		Object result = ReflectionUtils.invokeGetter(targetObject, PROPERTY_NAME);
		assertEquals(FRANZ, result);
		
		result = ReflectionUtils.invokeGetter(targetObject, "real");
		assertEquals(Boolean.FALSE, result);
		
		ReflectionUtils.invokeSetter(targetObject, PROPERTY_NAME, null, String.class);
		assertEquals(null, targetObject.getName());
		
		result = ReflectionUtils.invokeGetter(targetObject, PROPERTY_NAME);
		assertEquals(null, result);
		
	}

	
	
	@Indexed
	private static class AnotherIndexedClass implements PersistentObject	{
		@Field
		private String embeddedField;
		
		private String nonIndexedEmbeddedField;

		@Override
		public String getUID() {
			return null;
		}
	}
	
	@Indexed
	private static class AnIndexedClass implements PersistentObject	{
		@Field
		private int someField;
		
		private String nonIndexedField;
		
		@IndexedEmbedded
		private AnotherIndexedClass anEmbeddedField;
		
		@Override
		public String getUID() {
			return null;
		}
	}
	
	private class ANotIndexedClass implements PersistentObject	{
		@Override
		public String getUID() {
			return null;
		}
	}
	

	public void testGetFieldsIndexed() throws Exception {
		String[] fieldsIndexed = ReflectionUtils.getIndexedFieldNames(AnIndexedClass.class);
		assertEquals(2, fieldsIndexed.length);
		assertEquals("someField", fieldsIndexed[0]);
		assertEquals("anEmbeddedField.embeddedField", fieldsIndexed[1]);
		
	}
	
	public void testIsIndexed() throws Exception {
		assertTrue(ReflectionUtils.isIndexedForTextSearch(AnIndexedClass.class));
		assertFalse(ReflectionUtils.isIndexedForTextSearch(ANotIndexedClass.class));
	}
	
}
