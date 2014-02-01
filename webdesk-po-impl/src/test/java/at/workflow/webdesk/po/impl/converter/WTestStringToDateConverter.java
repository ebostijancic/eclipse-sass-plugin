package at.workflow.webdesk.po.impl.converter;

import at.workflow.webdesk.tools.date.DateTools;
import junit.framework.TestCase;

/**
 * @author sdzuban 19.07.2012
 */
public class WTestStringToDateConverter extends TestCase {

	private StringToDateConverter converter = new StringToDateConverter();
	
	public void testConverter() {
		
		assertNull(converter.convert(null, null, null));
		
		// default sdf pattern
		assertEquals(DateTools.toDate(2012, 7, 19, 10, 15), converter.convert("19.07.12 10:15", null, null));
		
		assertEquals(DateTools.toDate(2012, 7, 19, 10, 15), converter.convert("19.07.2012 10:15", "dd.MM.yyyy hh:mm", null));
	}
}
