package at.workflow.webdesk.po.converter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;

import junit.framework.TestCase;

/**
 * @author sdzuban 06.07.2012
 */
public class WTestAutomaticConverterFactory extends TestCase {
	
	private String locale = Locale.getDefault().toString();
	private AutomaticConverter converter;

	public void testConverterInstantiationErrorsWithClasses() {
		
		try {
			AutomaticConverterFactory.getConverter(Boolean.class, null);
			fail("Accepted null class");
		} catch (Exception e) { }
		try {
			AutomaticConverterFactory.getConverter(null, Boolean.class);
			fail("Accepted null class");
		} catch (Exception e) { }
		
		try {
			AutomaticConverterFactory.getConverter(Boolean.class, null, (String[]) null);
			fail("Accepted null class");
		} catch (Exception e) { }
		try {
			AutomaticConverterFactory.getConverter(null, Boolean.class, (String[]) null);
			fail("Accepted null class");
		} catch (Exception e) { }
		
	}
		
	
	public void testConverterInstantiationErrorsWithClassNames() {
			
		try {
			AutomaticConverterFactory.getConverter(Boolean.class.getName(), null);
			fail("Accepted null class");
		} catch (Exception e) { }
		try {
			AutomaticConverterFactory.getConverter(null, Boolean.class.getName());
			fail("Accepted null class");
		} catch (Exception e) { }
		
		try {
			AutomaticConverterFactory.getConverter(Boolean.class.getName(), null, (String[]) null);
			fail("Accepted null class");
		} catch (Exception e) { }
		try {
			AutomaticConverterFactory.getConverter(null, Boolean.class.getName(), (String[]) null);
			fail("Accepted null class");
		} catch (Exception e) { }

		
		try {
			AutomaticConverterFactory.getConverter(Boolean.class.getName(), "");
			fail("Accepted empty class name");
		} catch (Exception e) { }
		try {
			AutomaticConverterFactory.getConverter("   ", Boolean.class.getName());
			fail("Accepted empty class name");
		} catch (Exception e) { }
		
		try {
			AutomaticConverterFactory.getConverter(Boolean.class.getName(), "   ", (String[]) null);
			fail("Accepted empty class name");
		} catch (Exception e) { }
		try {
			AutomaticConverterFactory.getConverter("", Boolean.class.getName(), (String[]) null);
			fail("Accepted empty class name");
		} catch (Exception e) { }
		
	}
		
	
	public void testNoConverter() {
		
		assertNull(AutomaticConverterFactory.getConverter(BigDecimal.class, Boolean.class));
		assertNull(AutomaticConverterFactory.getConverter(Boolean.class, BigDecimal.class));
		assertNull(AutomaticConverterFactory.getConverter(BigDecimal.class, Boolean.class, (String[]) null));
		assertNull(AutomaticConverterFactory.getConverter(Boolean.class, BigDecimal.class, (String[]) null));
		assertNull(AutomaticConverterFactory.getConverter(BigDecimal.class, Boolean.class, new String[] {}));
		assertNull(AutomaticConverterFactory.getConverter(Boolean.class, BigDecimal.class, new String[] {}));
		
		assertNull(AutomaticConverterFactory.getConverter(String.class, HashMap.class));
		assertNull(AutomaticConverterFactory.getConverter(HashMap.class, String.class));
		assertNull(AutomaticConverterFactory.getConverter(String.class, HashMap.class, (String[]) null));
		assertNull(AutomaticConverterFactory.getConverter(HashMap.class, String.class, (String[]) null));
		assertNull(AutomaticConverterFactory.getConverter(String.class, HashMap.class, new String[] {}));
		assertNull(AutomaticConverterFactory.getConverter(HashMap.class, String.class, new String[] {}));
		assertNull(AutomaticConverterFactory.getConverter(String.class, HashMap.class, locale));
		assertNull(AutomaticConverterFactory.getConverter(HashMap.class, String.class, locale));
		
	}
	
	
	public void testClassBasedConverter() {

		converter = AutomaticConverterFactory.getConverter(double.class, float.class);
		assertNotNull(converter);
		assertTrue(converter instanceof ClassBasedConverter);
		assertEquals(5.234F, converter.convert(5.234));
		
		converter = AutomaticConverterFactory.getConverter(double.class, float.class, locale);
		assertNotNull(converter);
		assertTrue(converter instanceof ClassBasedConverter);
		assertEquals(5.234F, converter.convert(5.234));
		
	}
	
	
	public void testStringConverter() {
		
		converter = AutomaticConverterFactory.getConverter(double.class, String.class, "de_AT");
		assertNotNull(converter);
		assertTrue(converter instanceof StringConverter);
		assertEquals("123.456,78", converter.convert(123456.78));
		
		converter = AutomaticConverterFactory.getConverter(double.class, String.class, "en_GB");
		assertNotNull(converter);
		assertTrue(converter instanceof StringConverter);
		assertEquals("123,456.78", converter.convert(123456.78));
		
	}

}
