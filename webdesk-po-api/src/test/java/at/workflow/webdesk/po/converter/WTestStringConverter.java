package at.workflow.webdesk.po.converter;

import java.math.BigDecimal;
import java.util.Date;

import junit.framework.TestCase;

/**
 * @author sdzuban 19.07.2012
 */
public class WTestStringConverter extends TestCase {
	
	public void testNullClass() {
		
		try {
			new StringConverter(String.class, null, null);
			fail("Accepted null class");
		} catch (Exception e) { }
		try {
			new StringConverter(Date.class, null, null);
			fail("Accepted null class");
		} catch (Exception e) { }
		try {
			new StringConverter(null, String.class, null);
			fail("Accepted null class");
		} catch (Exception e) { }
		try {
			new StringConverter(null, Date.class, null);
			fail("Accepted null class");
		} catch (Exception e) { }
	}

	public void testNotConvertedClass() {
		
		try {
			new StringConverter(String.class, String.class, null);
			fail("Accepted non convertible class");
		} catch (Exception e) { }
		try {
			new StringConverter(String.class, Date.class, null);
			fail("Accepted non convertible class");
		} catch (Exception e) { }
		try {
			new StringConverter(Date.class, int.class, null);
			fail("Accepted non convertible class");
		} catch (Exception e) { }
		try {
			new StringConverter(StringConverter.class, String.class, null);
			fail("Accepted non convertible class");
		} catch (Exception e) { }
	}
	
	public void testPrimitiveClassNames() {
		
		assertNotNull(new StringConverter("boolean", "java.lang.String", null));
		assertNotNull(new StringConverter("java.lang.String", "int", null));
	}
	
	public void testNullLocale() {
		
		try {
			new StringConverter(String.class, int.class, null);
		} catch (Exception e) { 
			fail("Rejected null locale");
		}
	}
	
	public void testStringToBoolean() {
		
		StringConverter converter = new StringConverter(String.class, boolean.class, null);
		
		assertFalse((Boolean) converter.convert(null));
		assertTrue((Boolean) converter.convert("true"));
		assertFalse((Boolean) converter.convert("x"));
	}
	
	public void testBooleanToString() {
		
		StringConverter converter = new StringConverter(Boolean.class, String.class, null);
		
		assertNull(converter.convert(null));
		assertEquals("true", converter.convert(true));
		assertEquals("false", converter.convert(false));
	}
	
	public void testStringToInteger() {
		
		StringConverter converter = new StringConverter(String.class, int.class, "de_AT");
		
		assertEquals(0, converter.convert(null));
		assertEquals(0, converter.convert(" "));
		assertEquals(123, converter.convert("123"));
		assertEquals(123456, converter.convert("123.456"));
	}
	
	public void testIntegerToString() {
		
		StringConverter converter = new StringConverter(Integer.class, String.class, "de_AT");
		
		assertEquals(null, converter.convert(null));
		assertEquals("0", converter.convert(0));
		assertEquals("123", converter.convert(123));
		assertEquals("123.456", converter.convert(123456));
	}
	
	public void testStringToDouble() {
		
		StringConverter converter = new StringConverter(String.class, double.class, "de_AT");
		
		assertEquals(0., converter.convert(null));
		assertEquals(0., converter.convert(" "));
		assertEquals(123.45, converter.convert("123,45"));
		assertEquals(123456.78, converter.convert("123.456,78"));
	}
	
	public void testDoubleToString() {
		
		StringConverter converter = new StringConverter(Double.class, String.class, "de_AT");
		
		assertEquals(null, converter.convert(null));
		assertEquals("0", converter.convert(0.));
		assertEquals("123", converter.convert(123.));
		assertEquals("123.456,78", converter.convert(123456.78));
	}
	
	public void testStringToBigDecimal() {
		
		StringConverter converter = new StringConverter(String.class, BigDecimal.class, "de_AT");
		
		assertEquals(0, converter.convert(null));
		assertEquals(0, converter.convert(" "));
		assertEquals(new BigDecimal(123.45), converter.convert("123,45"), 0.1E-10);
		assertEquals(new BigDecimal(123456.78), converter.convert("123.456,78"), 0.1E-10);
	}
	
	private void assertEquals(BigDecimal expected, Object actual, double delta) {
		assertTrue(actual instanceof BigDecimal);
		assertTrue(expected.subtract((BigDecimal) actual).abs().doubleValue() <delta);
	}

	public void testBigDecimalToString() {
		
		StringConverter converter = new StringConverter(BigDecimal.class, String.class, "de_AT");
		
		assertEquals(null, converter.convert(null));
		assertEquals("0", converter.convert(new BigDecimal(0)));
		assertEquals("123", converter.convert(new BigDecimal(123)));
		assertEquals("123.456,78", converter.convert(new BigDecimal(123456.78)));
	}
}
