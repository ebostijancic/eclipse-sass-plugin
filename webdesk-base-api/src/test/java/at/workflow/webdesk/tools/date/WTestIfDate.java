package at.workflow.webdesk.tools.date;

import java.util.Arrays;

import junit.framework.TestCase;
import at.workflow.webdesk.tools.IfDate;

/**
 * @author fritzberger 10.12.2010
 */
public class WTestIfDate extends TestCase {
	
	public void test24Hour() throws Exception	{
		IfDate date0000 = new IfDate("02.06.2010 00:00", IfDate.IFDATETIMEFORMATCH);
		IfDate date2400 = new IfDate("01.06.2010 24:00", IfDate.IFDATETIMEFORMATCH);
		IfDate date2359 = new IfDate("01.06.2010 23:59", IfDate.IFDATETIMEFORMATCH);
		
		assertFalse(date0000.equals(date2400));
		assertFalse(date2400.equals(date0000));
		assertFalse(date0000.equals(date2359));
		assertFalse(date2359.equals(date0000));
		assertFalse(date2359.equals(date2400));
		
		assertTrue(date0000.after(date2400));
		assertTrue(date0000.after(date2359));
		assertTrue(date2400.after(date2359));
		
		assertTrue(date2400.before(date0000));
		assertTrue(date2359.before(date0000));
		assertTrue(date2359.before(date2400));

		assertTrue(date0000.afterEqualsSoft(date2400));
		assertTrue(date0000.afterEqualsSoft(date2359));
		assertTrue(date2400.afterEqualsSoft(date2359));
		
		assertTrue(date2400.beforeEqualsSoft(date0000));
		assertTrue(date2359.beforeEqualsSoft(date0000));
		assertTrue(date2359.beforeEqualsSoft(date2400));

		assertFalse(date0000.equalsSoft(date2400));
		assertFalse(date2400.equalsSoft(date2359));
		assertFalse(date0000.equalsSoft(date2359));

		assertTrue(date0000.compareTo(date2400) > 0);
		assertTrue(date2400.compareTo(date2359) > 0);
		assertTrue(date0000.compareTo(date2359) > 0);

		assertTrue(date2400.compareTo(date0000) < 0);
		assertTrue(date2359.compareTo(date2400) < 0);
		assertTrue(date2359.compareTo(date0000) < 0);

		assertFalse(date0000.compareTo(date2400) == 0);
		assertFalse(date2400.compareTo(date2359) == 0);
		assertFalse(date0000.compareTo(date2359) == 0);

		assertFalse(date2400.compareTo(date0000) == 0);
		assertFalse(date2359.compareTo(date2400) == 0);
		assertFalse(date2359.compareTo(date0000) == 0);

		assertTrue(date0000.compareTo(date0000) == 0);
		assertTrue(date2400.compareTo(date2400) == 0);
		assertTrue(date2359.compareTo(date2359) == 0);
	}

	public void testMillisecondSorting() throws Exception	{
		IfDate date0000 = new IfDate("02.06.2010 00:00", IfDate.IFDATETIMEFORMATCH);
		IfDate date2400 = new IfDate("01.06.2010 24:00", IfDate.IFDATETIMEFORMATCH);
		IfDate date2359 = new IfDate("01.06.2010 23:59", IfDate.IFDATETIMEFORMATCH);
		
		IfDate [] array = new IfDate [] {
				date2400,
				date0000,
				date2359
			};
		Arrays.sort(array);
		
		assertEquals(array[0], date2359);
		assertEquals(array[1], date2400);
		assertEquals(array[2], date0000);
	}

	public void testIllegalConstructor() throws Exception	{
		try	{
			IfDate date = new IfDate("2010-12-10", 1234);
			fail("Only legal values are allowed as format-mask, not 1234: "+date);
		}
		catch (IllegalArgumentException e)	{
			// ignore, this was intended
		}
		try	{
			@SuppressWarnings("unused")
			IfDate date = new IfDate("", IfDate.IFDATEFORMATCH);
			fail("Only legal values are allowed as value, not \"\"");
		}
		catch (Exception e)	{
			// ignore, this was intended
		}
	}
	
	public void testSetDateOnly() throws Exception {
		
		IfDate timePoint = new IfDate("31.05.2012 07:05:00", IfDate.IFDATETIMEFORMATCH);
		
		IfDate testDate = (IfDate) timePoint.clone();
		
		IfDate dateToSet = new IfDate("01.11.2010", IfDate.IFDATEFORMATCH);
		
		testDate.setDateOnly( dateToSet );
		assertEquals(testDate.getDateOnly(), dateToSet );
		assertEquals(testDate.toSpecialTimeFormat(), timePoint.toSpecialTimeFormat() );
		
	}

}
