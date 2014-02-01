package at.workflow.webdesk.tools.numbers;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author sdzuban 03.04.2013
 */
public class WTestNumberComparator {

	@Test
	public void testNulls() {
		
		try {
			new NumberComparator(null);
			fail("Instantiated comparator with null number type");
		} catch (Exception e) {}
	}

	@Test
	public void testComparatorIntegerAndLong() {
		
		NumberComparator c = new NumberComparator(Integer.class);
		
		assertEquals(-1, c.compare(6521, 30655));
		assertEquals(0, c.compare(30655, 30655));
		assertEquals(1, c.compare(36521, 30655));
		
		c = new NumberComparator(Long.class, 10);
		
		assertEquals(-1, c.compare(30644L, 30655L));
		assertEquals(0, c.compare(30645L, 30655L));
		assertEquals(0, c.compare(30655L, 30655L));
		assertEquals(0, c.compare(30665L, 30655L));
		assertEquals(1, c.compare(30666L, 30655L));
	}	
	
	
	@Test
	public void testComparatorFloatAndDouble() {
		
		NumberComparator c = new NumberComparator(Float.class);
		
		assertEquals(-1, c.compare(6521.0F, 30655.0F));
		assertEquals(0, c.compare(30655.0F, 30655.0F));
		assertEquals(1, c.compare(36521.0F, 30655.0F));
		
		c = new NumberComparator(Double.class, 10.0);
		
		assertEquals(-1, c.compare(30644.9, 30655.0));
		assertEquals(0, c.compare(30645.0, 30655.0));
		assertEquals(0, c.compare(30655.0, 30655.0));
		assertEquals(0, c.compare(30665.0, 30655.0));
		assertEquals(1, c.compare(30665.1, 30655.0));
	}	
	
	
	@Test
	public void test() {
	}	
}
