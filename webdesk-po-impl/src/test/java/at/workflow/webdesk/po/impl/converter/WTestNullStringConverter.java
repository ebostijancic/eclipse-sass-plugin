package at.workflow.webdesk.po.impl.converter;

import junit.framework.TestCase;

/**
 * @author sdzuban 11.07.2012
 */
public class WTestNullStringConverter extends TestCase {
	
	public void testNull() {
		
		NullStringConverter converter = new NullStringConverter(null);
		assertEquals("", converter.convert(null));
		assertEquals("sdf", converter.convert("sdf"));
	}

	public void testString() {
		
		NullStringConverter converter = new NullStringConverter(java.lang.String.class);
		assertEquals("", converter.convert(null));
		assertEquals("sdf", converter.convert("sdf"));
	}
	
	public void testNumber() {
		
		NullStringConverter converter = new NullStringConverter(double.class);
		assertEquals("0", converter.convert(null));
		assertEquals("sdf", converter.convert("sdf"));
	}
	
	public void testDate() {
		
		NullStringConverter converter = new NullStringConverter(java.util.Date.class);
		assertNull(converter.convert(null));
		assertEquals("sdf", converter.convert("sdf"));
	}
	
	public void testBoolean() {
		
		NullStringConverter converter = new NullStringConverter(boolean.class);
		assertEquals("", converter.convert(null));
		assertEquals("sdf", converter.convert("sdf"));
	}
	
}
