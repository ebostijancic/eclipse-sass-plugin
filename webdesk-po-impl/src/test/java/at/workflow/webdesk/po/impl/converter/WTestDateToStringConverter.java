package at.workflow.webdesk.po.impl.converter;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

/**
 * @author sdzuban 19.07.2012
 */
public class WTestDateToStringConverter extends TestCase {

	private DateToStringConverter converter = new DateToStringConverter();
	
	public void testConverter() {

		assertEquals("", converter.convert(null, null, null));
		
		String today = new SimpleDateFormat().format(new Date());
		assertEquals(today, converter.convert(new Date(), null, null));
		
		today = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
		assertEquals(today, converter.convert(new Date(), "dd.MM.yyyy", null));
	}
}
