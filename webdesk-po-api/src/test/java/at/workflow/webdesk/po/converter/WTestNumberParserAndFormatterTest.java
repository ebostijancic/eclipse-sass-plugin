package at.workflow.webdesk.po.converter;

import java.math.BigDecimal;
import java.util.Locale;

import junit.framework.TestCase;

/**
 * @author sdzuban 20.06.2012
 */
public class WTestNumberParserAndFormatterTest extends TestCase {

	private NumberParserAndFormatter numberHandler = new NumberParserAndFormatter(Locale.GERMANY);
	
	public void testFormattingDouble() {
		
		final double dbl = 1234.56;
		String withoutPattern = numberHandler.formatDouble(dbl);
		assertEquals("1.234,56", withoutPattern);
		assertEquals("1234,6", numberHandler.formatDouble(dbl, "0.#"));
		assertEquals("The default formatting pattern was changed", withoutPattern, numberHandler.formatDouble(dbl));
		assertEquals("1234,56", numberHandler.formatDouble(dbl, "0.0#"));
		assertEquals("The default formatting pattern was changed", withoutPattern, numberHandler.formatDouble(dbl));
		assertEquals("1.235", numberHandler.formatDouble(dbl, "#,000"));
		assertEquals("The default formatting pattern was changed", withoutPattern, numberHandler.formatDouble(dbl));
	}
	
	public void testFormattingPercent() {
		
		final double percent = 0.12345;
		String withoutPattern = numberHandler.formatPercent(percent);
		assertEquals("12%", withoutPattern);
		assertEquals("12,3%", numberHandler.formatPercent(percent, "0.#%"));
		assertEquals("The default formatting pattern was changed", withoutPattern, numberHandler.formatPercent(percent));
		assertEquals("12,34%", numberHandler.formatPercent(percent, "0.0#%"));
		assertEquals("The default formatting pattern was changed", withoutPattern, numberHandler.formatPercent(percent));
		assertEquals("12,345%", numberHandler.formatPercent(percent, "0.00#%"));
		assertEquals("The default formatting pattern was changed", withoutPattern, numberHandler.formatPercent(percent));
	}
	
	public void testFormattingCurrency() {
		
		final double currency = 1234567.789;
		String withoutPattern = numberHandler.formatCurrency(currency);
		assertEquals("1.234.567,79 €", withoutPattern);
		assertEquals("1234567,8 €", numberHandler.formatCurrency(currency, "0.# €"));
		assertEquals("€1234567,8", numberHandler.formatCurrency(currency, "€0.#"));
		assertEquals("The default formatting pattern was changed", withoutPattern, numberHandler.formatCurrency(currency));
		assertEquals("1.234.567,79 €", numberHandler.formatCurrency(currency, "#,##0.0# €"));
		assertEquals("The default formatting pattern was changed", withoutPattern, numberHandler.formatCurrency(currency));
		assertEquals("1.234.567,789 €", numberHandler.formatCurrency(currency, "#,##0.000 €"));
		assertEquals("The default formatting pattern was changed", withoutPattern, numberHandler.formatCurrency(currency));
	}
	
	public void testFormattingBigDecimal() {
		
		final BigDecimal bigDecimal = new BigDecimal(1234.56);
		String withoutPattern = numberHandler.formatBigDecimal(bigDecimal);
		assertEquals("1.234,56", withoutPattern);
		assertEquals("1234,6", numberHandler.formatBigDecimal(bigDecimal, "0.#"));
		assertEquals("The default formatting pattern was changed", withoutPattern, numberHandler.formatDouble(1234.56));
		assertEquals("1234,56", numberHandler.formatBigDecimal(bigDecimal, "0.0#"));
		assertEquals("The default formatting pattern was changed", withoutPattern, numberHandler.formatDouble(1234.56));
		assertEquals("1.235", numberHandler.formatBigDecimal(bigDecimal, "#,000"));
		assertEquals("The default formatting pattern was changed", withoutPattern, numberHandler.formatDouble(1234.56));
	}
	
	
	public void testParsingInteger() {
		
		assertEquals(123, numberHandler.parseInteger("123"));
		assertEquals(123456789, numberHandler.parseInteger("123456789"));
		assertEquals(123456789, numberHandler.parseInteger("123.456.789"));
		// this one is tricky, but is the correct integer with grouping symbol on incorrect place
		assertEquals(123, numberHandler.parseInteger("12.3"));
		try {
			numberHandler.parseInteger("12,3");
			fail("Accepted non-integer number");
		} catch (Exception e) {}
		try {
			numberHandler.parseInteger("12e3");
			fail("Accepted non-integer number");
		} catch (Exception e) {}
	}
	
	public void testParsingDouble() {
		
		assertEquals(123.45, numberHandler.parseDouble("123,45"));
		assertEquals(123456789.1, numberHandler.parseDouble("123456789,1"));
		assertEquals(123456789.1, numberHandler.parseDouble("123.456.789,1"));
		assertEquals(123.45E-6, numberHandler.parseDouble("123,45E-6"));
		// this one is tricky, but is the correct double with grouping symbol on incorrect place
		assertEquals(123., numberHandler.parseDouble("12.3"));
		assertEquals(12., numberHandler.parseDouble("12"));
		try {
			numberHandler.parseDouble("12f3");
			fail("Accepted non-double number");
		} catch (Exception e) {}
	}

	
	public void testParsingPercent() {
		
		assertEquals(1.23, numberHandler.parsePercent("123%"));
		assertEquals(1.23, numberHandler.parsePercent("123,0%"));
		assertEquals(-0.12, numberHandler.parsePercent("-12%"));
		try {
			numberHandler.parsePercent("12,3");
			fail("Accepted non-percent number");
		} catch (Exception e) {}
		try {
			numberHandler.parsePercent("12.3");
			fail("Accepted non-percent number");
		} catch (Exception e) {}
		try {
			numberHandler.parsePercent("12f3");
			fail("Accepted non-percent number");
		} catch (Exception e) {}
	}
	
	
	public void testParsingCurrency() {

		assertEquals(123.45, numberHandler.parseCurrency("123,45 €"));
		assertEquals(12345., numberHandler.parseCurrency("123.45 €"));
		assertEquals(123456789.1, numberHandler.parseCurrency("123456789,1 €"));
		assertEquals(123456789.1, numberHandler.parseCurrency("123.456.789,10 €"));
		assertEquals(0.00012345, numberHandler.parseCurrency("123,45E-6 €"));
		try {
			numberHandler.parseCurrency("€ 12,3");
			fail("Accepted wrong formatted currency number");
		} catch (Exception e) {}
		try {
			numberHandler.parseCurrency("€12.3");
			fail("Accepted wrong formatted currency number");
		} catch (Exception e) {}
		try {
			numberHandler.parseCurrency("12,3");
			fail("Accepted non-currency number");
		} catch (Exception e) {}
		try {
			numberHandler.parseCurrency("12.3");
			fail("Accepted non-currency number");
		} catch (Exception e) {}
		try {
			numberHandler.parseCurrency("12f3 €");
			fail("Accepted non-currency number");
		} catch (Exception e) {}
		try {
			numberHandler.parseCurrency("$123,45");
			fail("Accepted wrong-currency number");
		} catch (Exception e) {}
	}
	
	public void testParsingCurrencyWithPattern() {
		
		assertEquals(123.45, numberHandler.parseCurrency("123,45€", "0.00€"));
		assertEquals(123456789.1, numberHandler.parseCurrency("€123456789,1", "€0.0"));
		assertEquals(123456789.1, numberHandler.parseCurrency("€123.456.789,10", "€#,##0.00"));
	}
	
	public void testParsingBigDecimal() {
		
		assertEquals(new BigDecimal("123.45"), numberHandler.parseBigDecimal("123,45"));
		assertEquals(new BigDecimal("123456789.1"), numberHandler.parseBigDecimal("123456789,1"));
		assertEquals(new BigDecimal("123456789.1"), numberHandler.parseBigDecimal("123.456.789,1"));
		assertEquals(new BigDecimal("123.45E-6"), numberHandler.parseBigDecimal("123,45E-6"));
		// this one is tricky, but is the correct BigDecimal with grouping symbol on incorrect place
		assertEquals(new BigDecimal("123."), numberHandler.parseBigDecimal("12.3"));
		try {
			numberHandler.parseBigDecimal("12f3");
			fail("Accepted non-BigDecimal number");
		} catch (Exception e) {}
	}

	
}
