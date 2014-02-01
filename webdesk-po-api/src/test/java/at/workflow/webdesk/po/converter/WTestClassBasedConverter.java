package at.workflow.webdesk.po.converter;

import java.math.BigDecimal;
import java.math.BigInteger;

import at.workflow.webdesk.po.converter.ClassBasedConverter;
import junit.framework.TestCase;

/**
 * @author sdzuban 06.07.2012
 */
public class WTestClassBasedConverter extends TestCase {

	public void testConverterInstantiation() {
		
		// now conversion between numbers also includes BigDecimal and BigInteger
		new ClassBasedConverter(java.math.BigDecimal.class, float.class);
		new ClassBasedConverter(java.math.BigInteger.class, float.class);
		
		try {
			new ClassBasedConverter(Boolean.class, null);
			fail("Accepted unconvertable classes");
		} catch (Exception e) { }
		try {
			new ClassBasedConverter(Boolean.class, String.class);
			fail("Accepted unconvertable classes");
		} catch (Exception e) { }
		try {
			new ClassBasedConverter(float.class, String.class);
			fail("Accepted unconvertable classes");
		} catch (Exception e) { }
		try {
			new ClassBasedConverter(float.class, boolean.class);
			fail("Accepted unconvertable classes");
		} catch (Exception e) { }
		try {
			new ClassBasedConverter(java.util.Date.class, String.class);
			fail("Accepted unconvertable classes");
		} catch (Exception e) { }
		try {
			new ClassBasedConverter(java.util.Date.class, boolean.class);
			fail("Accepted unconvertable classes");
		} catch (Exception e) { }
	}
	
	public void testPrimitiveClassNames() {
		
		assertNotNull(new ClassBasedConverter("boolean", "java.lang.Boolean"));
		assertNotNull(new ClassBasedConverter("long", "int"));
	}
	
	public void testConvertSameClasses() {
		
		ClassBasedConverter converter = new ClassBasedConverter(String.class, String.class);
		assertEquals("test", converter.convert("test"));
		
		converter = new ClassBasedConverter(Boolean.class, Boolean.class);
		assertTrue((Boolean) converter.convert(true));
		
		converter = new ClassBasedConverter(ClassBasedConverter.class, ClassBasedConverter.class);
		assertEquals(converter, converter.convert(converter));
	}
	
	public void testConvertBoolean() {
		
		ClassBasedConverter converter = new ClassBasedConverter(boolean.class, Boolean.class);
		assertTrue((Boolean) converter.convert(true));
		
		converter = new ClassBasedConverter(Boolean.class, boolean.class);
		assertTrue((Boolean) converter.convert(true));
		
	}
	
	public void testNumbers() {
		
		ClassBasedConverter converter = new ClassBasedConverter(double.class, float.class);
		assertEquals(5.234F, converter.convert(5.234));
		
		converter = new ClassBasedConverter(double.class, Integer.class);
		assertEquals(5, converter.convert(5.234));
		assertEquals(5, converter.convert(5.934));
		
		converter = new ClassBasedConverter(double.class, short.class);
		short result = 5;
		assertEquals(result, converter.convert(5.234));
		assertEquals(result, converter.convert(5.934));
		
		converter = new ClassBasedConverter(BigInteger.class, Integer.class);
		assertEquals(500, converter.convert(new BigInteger("500")));
		
		converter = new ClassBasedConverter(BigDecimal.class, Integer.class);
		assertEquals(5, converter.convert(new BigDecimal("5.383882")));
		assertEquals(5, converter.convert(new BigDecimal("5.38383332")));

	}
	
	public void testDateTime() {
		
		java.util.Date date = new java.util.Date();
		
		ClassBasedConverter converter = new ClassBasedConverter(java.util.Date.class, java.sql.Timestamp.class);
		assertTrue(converter.convert(date) instanceof java.sql.Timestamp);
		assertEquals(date.getTime(), ((java.sql.Timestamp) converter.convert(date)).getTime());
		
		converter = new ClassBasedConverter(java.util.Date.class, java.sql.Time.class);
		assertTrue(converter.convert(date) instanceof java.sql.Time);
		assertEquals(date.getTime(), ((java.sql.Time) converter.convert(date)).getTime());
		
		converter = new ClassBasedConverter(java.util.Date.class, java.sql.Date.class);
		assertTrue(converter.convert(date) instanceof java.sql.Date);
		assertEquals(date.getTime(), ((java.sql.Date) converter.convert(date)).getTime());
		
		java.sql.Time time = new java.sql.Time(date.getTime());
		converter = new ClassBasedConverter(java.sql.Time.class, java.util.Date.class);
		assertTrue(converter.convert(time) instanceof java.util.Date);
		assertEquals(time.getTime(), ((java.util.Date) converter.convert(time)).getTime());

		java.sql.Date sqlDate = new java.sql.Date(date.getTime());
		converter = new ClassBasedConverter(java.sql.Date.class, java.util.Date.class);
		assertTrue(converter.convert(sqlDate) instanceof java.util.Date);
		assertEquals(sqlDate.getTime(), ((java.util.Date) converter.convert(sqlDate)).getTime());
		
		java.sql.Timestamp timestamp = new java.sql.Timestamp(date.getTime());
		converter = new ClassBasedConverter(java.sql.Timestamp.class, java.util.Date.class);
		assertTrue(converter.convert(timestamp) instanceof java.util.Date);
		assertEquals(timestamp.getTime(), ((java.util.Date) converter.convert(timestamp)).getTime());
		
	}

	public void testConverterInstantiationWithNames() {
		
		try {
			new ClassBasedConverter("java.lang.Boolean", null);
			fail("Accepted unconvertable classes");
		} catch (Exception e) { }
		try {
			new ClassBasedConverter("java.lang.Boolean", "java.lang.String");
			fail("Accepted unconvertable classes");
		} catch (Exception e) { }
		try {
			new ClassBasedConverter("java.lang.Float", "java.lang.String");
			fail("Accepted unconvertable classes");
		} catch (Exception e) { }
		try {
			new ClassBasedConverter("java.lang.Float", "java.lang.Boolean");
			fail("Accepted unconvertable classes");
		} catch (Exception e) { }
		try {
			new ClassBasedConverter("java.util.Date", "java.lang.String");
			fail("Accepted unconvertable classes");
		} catch (Exception e) { }
		try {
			new ClassBasedConverter("java.util.Date", "java.lang.Boolean");
			fail("Accepted unconvertable classes");
		} catch (Exception e) { }
	}
	
	public void testConvertSameClassesWithNames() {
		
		ClassBasedConverter converter = new ClassBasedConverter("java.lang.String", "java.lang.String");
		assertEquals("test", converter.convert("test"));
		
		converter = new ClassBasedConverter("java.lang.Boolean", "java.lang.Boolean");
		assertTrue((Boolean) converter.convert(true));
		
		converter = new ClassBasedConverter("at.workflow.webdesk.po.converter.ClassBasedConverter", "at.workflow.webdesk.po.converter.ClassBasedConverter");
		assertEquals(converter, converter.convert(converter));
	}
	
	public void testNumbersWithNames() {
		
		ClassBasedConverter converter = new ClassBasedConverter("java.lang.Double", "java.lang.Float");
		assertEquals(5.234F, converter.convert(5.234));
		
		converter = new ClassBasedConverter("java.lang.Double", "java.lang.Integer");
		assertEquals(5, converter.convert(5.234));
		assertEquals(5, converter.convert(5.934));
	}
	
	public void testDateTimeWithNames() {
		
		java.util.Date date = new java.util.Date();
		
		ClassBasedConverter converter = new ClassBasedConverter("java.util.Date", "java.sql.Timestamp");
		assertTrue(converter.convert(date) instanceof java.sql.Timestamp);
		assertEquals(date.getTime(), ((java.sql.Timestamp) converter.convert(date)).getTime());
		
		converter = new ClassBasedConverter("java.util.Date", "java.sql.Time");
		assertTrue(converter.convert(date) instanceof java.sql.Time);
		assertEquals(date.getTime(), ((java.sql.Time) converter.convert(date)).getTime());
		
		converter = new ClassBasedConverter("java.util.Date", "java.sql.Date");
		assertTrue(converter.convert(date) instanceof java.sql.Date);
		assertEquals(date.getTime(), ((java.sql.Date) converter.convert(date)).getTime());
		
		java.sql.Time time = new java.sql.Time(date.getTime());
		converter = new ClassBasedConverter("java.sql.Time", "java.util.Date");
		assertTrue(converter.convert(time) instanceof java.util.Date);
		assertEquals(time.getTime(), ((java.util.Date) converter.convert(time)).getTime());

		java.sql.Date sqlDate = new java.sql.Date(date.getTime());
		converter = new ClassBasedConverter("java.sql.Date", "java.util.Date");
		assertTrue(converter.convert(sqlDate) instanceof java.util.Date);
		assertEquals(sqlDate.getTime(), ((java.util.Date) converter.convert(sqlDate)).getTime());
		
		java.sql.Timestamp timestamp = new java.sql.Timestamp(date.getTime());
		converter = new ClassBasedConverter("java.sql.Timestamp", "java.util.Date");
		assertTrue(converter.convert(timestamp) instanceof java.util.Date);
		assertEquals(timestamp.getTime(), ((java.util.Date) converter.convert(timestamp)).getTime());
		
	}
}
