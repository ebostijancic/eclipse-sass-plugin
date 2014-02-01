package at.workflow.webdesk.tools.numbers;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author sdzuban 03.04.2013
 */
public class WTestNumberSumCalculator {

	@Test
	public void testNull() {
		
		try {
			new NumberSumCalculator(null);
		} catch (Exception e) { }
	}

	@Test
	public void testIntegerSum() {
		
		NumberSumCalculator c = new NumberSumCalculator(Integer.class);
		
		c.reset();
		assertTrue(c.getResult() instanceof Integer);
		assertEquals(0, c.getResult());
		
		c.add(10);
		assertTrue(c.getResult() instanceof Integer);
		assertEquals(10, c.getResult());
		
		c.add(1000);
		assertTrue(c.getResult() instanceof Integer);
		assertEquals(1010, c.getResult());
		
	}
	
	@Test
	public void testDoubleSum() {
		
		NumberSumCalculator c = new NumberSumCalculator(Double.class);
		
		c.reset();
		assertTrue(c.getResult() instanceof Double);
		assertEquals(0.0, c.getResult());
		
		c.add(10.0);
		assertTrue(c.getResult() instanceof Double);
		assertEquals(10.0, c.getResult());
		
		c.add(1000.0);
		assertTrue(c.getResult() instanceof Double);
		assertEquals(1010.0, c.getResult());
		
	}
	
}
