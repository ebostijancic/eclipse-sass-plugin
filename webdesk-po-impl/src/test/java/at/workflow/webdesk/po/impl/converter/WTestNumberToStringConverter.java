package at.workflow.webdesk.po.impl.converter;

import junit.framework.TestCase;

/**
 * @author sdzuban 19.07.2012
 */
public class WTestNumberToStringConverter extends TestCase {

	private NumberToStringConverter converter = new NumberToStringConverter();
	
	public void testConverter() {
		
		try {
			converter.convert(null, null, null);
			fail("Accepted null pattern");
		} catch (Exception e) {}
		
		assertEquals("0", converter.convert(null, "0", null));
		assertEquals("0,", converter.convert(null, "0.", null));
		assertEquals("0,0", converter.convert(null, "0.0", null));
		assertEquals("0.", converter.convert(null, "0.", "en_GB"));
		assertEquals("0.0", converter.convert(null, "0.0", "en_GB"));
		
		assertEquals("123", converter.convert(123, "0", null));
		assertEquals("123,", converter.convert(123, "0.", null));
		assertEquals("123,00", converter.convert(123, "0.00", null));
		
		assertEquals("123", converter.convert(123.45, "0", null));
		assertEquals("123,", converter.convert(123.45, "0.", null));
		assertEquals("123,45", converter.convert(123.45, "0.##", null));
	}
}
