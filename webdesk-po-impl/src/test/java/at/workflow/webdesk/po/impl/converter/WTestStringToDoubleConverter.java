package at.workflow.webdesk.po.impl.converter;

import junit.framework.TestCase;

/**
 * @author sdzuban 19.07.2012
 */
public class WTestStringToDoubleConverter extends TestCase {

	private StringToDoubleConverter converter = new StringToDoubleConverter();
	
	public void testConverter() {
		
		assertEquals(0, converter.convert(null, null, null));
		
		assertEquals(5.23, converter.convert("5.23", null, null));
		assertEquals(5.23, converter.convert("5,23", null, "de_AT"));
		assertEquals(5.23, converter.convert("5.23", null, "en_GB"));
		
		assertEquals(1005.23, converter.convert("1.005,23", null, "de_AT"));
		assertEquals(1005.23, converter.convert("1,005.23", null, "en_GB"));
		
		assertEquals(523., converter.convert("5.23", null, "de_AT"));
		assertEquals(523., converter.convert("5,23", null, "en_GB"));
		
	}
	
}
