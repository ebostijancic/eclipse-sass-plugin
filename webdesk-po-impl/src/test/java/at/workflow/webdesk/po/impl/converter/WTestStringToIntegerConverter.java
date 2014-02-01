package at.workflow.webdesk.po.impl.converter;

import junit.framework.TestCase;

/**
 * @author sdzuban 19.07.2012
 */
public class WTestStringToIntegerConverter extends TestCase {

	private StringToIntegerConverter converter = new StringToIntegerConverter();
	
	public void testConverter() {
		
		assertEquals(0, converter.convert(null, null, null));
		
		assertEquals(123456, converter.convert("123456", null, null));
		assertEquals(123456, converter.convert("123.456", null, "de_AT"));
		assertEquals(123456, converter.convert("123,456", null, "en_GB"));
		
		try {
			converter.convert("123.456", null, null);
			fail("Accepted formatted number without locale");
		} catch (Exception e) { }
		try {
			converter.convert("123,456", null, "de_AT");
			fail("Accepted formatted number with wrong locale");
		} catch (Exception e) { }
		try {
			converter.convert("123.456", null, "en_GB");
			fail("Accepted formatted number with wrong locale");
		} catch (Exception e) { }
	}
	
}
