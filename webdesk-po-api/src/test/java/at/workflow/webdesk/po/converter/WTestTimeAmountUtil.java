package at.workflow.webdesk.po.converter;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import junit.framework.TestCase;

/**
 * @author fritzberger 01.12.2011
 */
public class WTestTimeAmountUtil extends TestCase {

	public void testNegativeMinuteAmounts()	{
		String duration;
		
		duration = TimeAmountUtil.format(Locale.GERMAN, -4);
		assertEquals("-0,04", duration);
		
		duration = TimeAmountUtil.format(Locale.GERMAN, -104);
		assertEquals("-1,44", duration);
		
		duration = TimeAmountUtil.format(Locale.GERMAN, -10004);
		assertEquals("-166,44", duration);
	}
	
	public void testToDisplayHours()	{
		float hours = TimeAmountUtil.toDisplayHours(101);
		assertEquals(1.41f, hours);
		int minutes = TimeAmountUtil.toMinutes(1.41f);
		assertEquals(101, minutes);
	}
	
	public void testMinima()	throws Exception	{
		String s = "0,01";
		int minutes = TimeAmountUtil.parse(Locale.GERMAN, s);
		assertEquals(1, minutes);
		
		s = "0";
		try	{
			minutes = TimeAmountUtil.parse(Locale.GERMAN, s);
			fail("Time amount must be at least 0,01 (one minute): "+s);
		}
		catch (ParseException e)	{	// is expected here
			System.err.println(e.getMessage());
		}
		
		try	{
			s = "0.001";
			minutes = TimeAmountUtil.parse(Locale.ENGLISH, s);
			fail("Minimum is one minute: "+s);
		}
		catch (ParseException e)	{	// is expected here
			System.err.println(e.getMessage());
		}
	}
	
	public void testMaxima()	throws Exception	{
		String s = "1,59";
		int minutes = TimeAmountUtil.parse(Locale.GERMAN, s);
		assertEquals(119, minutes);
		
		s = "1,60";
		try	{
			minutes = TimeAmountUtil.parse(Locale.GERMAN, s);
			fail("Too much 'normal' minutes to the right of decimal point: "+s);
		}
		catch (ParseException e)	{	// is expected here
			System.err.println(e.getMessage());
		}
		
		s = "12,345";
		try	{
			minutes = TimeAmountUtil.parse(Locale.GERMAN, s);
			fail("Too much digits to the right of decimal point: "+s);
		}
		catch (ParseException e)	{	// is expected here
			System.err.println(e.getMessage());
		}
	}
	
	public void testAmount()	throws Exception	{
		String s = "2,4";
		int minutes = TimeAmountUtil.parse(Locale.GERMAN, s);
		assertEquals(160, minutes);
		
		s = "2,40";	// trailing zero, must be same as 2.4
		minutes = TimeAmountUtil.parse(Locale.GERMAN, s);
		assertEquals(160, minutes);
	}
	
	public void testSeparators()	throws Exception	{
		String s = "0.01";	// tolerate "." as decimal separator in GERMAN
		int minutes = TimeAmountUtil.parse(Locale.GERMAN, s);
		assertEquals(1, minutes);
		
		s = "00,01";	// but not in ENGLISH
		try	{
			minutes = TimeAmountUtil.parse(Locale.ENGLISH, s);
			fail("Wrong decimal point in: "+s);
		}
		catch (ParseException e)	{	// is expected here
			System.err.println(e.getMessage());
		}
		
		s = "12:34";
		try	{
			minutes = TimeAmountUtil.parse(Locale.GERMAN, s);
			fail("Invalid comma separator: "+s);
		}
		catch (ParseException e)	{	// is expected here
			System.err.println(e.getMessage());
		}
	}
	
	public void testLocales() throws ParseException	{
		final double number = 999.59;
		
		for (Locale locale : Locale.getAvailableLocales())	{
			String userInput = NumberFormat.getInstance(locale).format(number);
			int minutes = TimeAmountUtil.parse(locale, userInput);
			
			assertEquals("Locale "+locale+" ("+locale.getDisplayName()+") failed!", 59999, minutes);
		}
	}

}
