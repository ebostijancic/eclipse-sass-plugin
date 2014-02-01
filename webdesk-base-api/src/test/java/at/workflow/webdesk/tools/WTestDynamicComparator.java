package at.workflow.webdesk.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import at.workflow.webdesk.tools.comparator.ReflectiveComparator;

/**
 * @author sdzuban 14.09.2012
 */
public class WTestDynamicComparator extends TestCase {
	
	public class Bean {
		public String string;
		public int intnum;
		public Date date;
		public boolean bool;
		public double dble;
		
		public String getString() {
			return string;
		}
		public int getIntnum() {
			return intnum;
		}
		public Date getDate() {
			return date;
		}
		public boolean isBool() {
			return bool;
		}
		public double getDble() {
			return dble;
		}
	}
	
	public void testInstantiation() {
		
		try {
			new ReflectiveComparator(null);
			fail("Accepted null argument.");
		} catch (Exception e) {
		}
	}
	
	public void testNullBeans() {
		
		ReflectiveComparator comparator = new ReflectiveComparator("string");
		Bean b = new Bean();
		
		assertEquals(0, comparator.compare(null, null));
		assertEquals(1, comparator.compare(b, null));
		assertEquals(-1, comparator.compare(null, b));
	}

	public void testStrings() {
		
		ReflectiveComparator comparator = new ReflectiveComparator("string");
		Bean b1 = new Bean();
		Bean b2 = new Bean();
		
		assertEquals(0, comparator.compare(b1, b2));
		
		b1.string = "string";
		assertTrue(comparator.compare(b1, b2) > 0);
		
		b1.string = null;
		b2.string = "string";
		assertTrue(comparator.compare(b1, b2) < 0);
		
		b1.string = "Www";
		b2.string = "string";
		assertTrue(comparator.compare(b1, b2) > 0);
		
		b1.string = "www";
		b2.string = "String";
		assertTrue(comparator.compare(b1, b2) > 0);
	}
	
	public void testIntegers() {
		
		ReflectiveComparator comparator = new ReflectiveComparator("intnum");
		Bean b1 = new Bean();
		Bean b2 = new Bean();
		
		assertEquals(0, comparator.compare(b1, b2));
		
		b1.intnum = 123;
		assertTrue(comparator.compare(b1, b2) > 0);
		
		b2.intnum = 456;
		assertTrue(comparator.compare(b1, b2) < 0);
	}
	
	public void testDoubles() {
		
		ReflectiveComparator comparator = new ReflectiveComparator("dble");
		Bean b1 = new Bean();
		Bean b2 = new Bean();
		
		assertEquals(0, comparator.compare(b1, b2));
		
		b1.dble = 123.23;
		b2.dble = 123.45;
		assertTrue(comparator.compare(b1, b2) < 0);
	}
	
	public void testDates() {
		
		ReflectiveComparator comparator = new ReflectiveComparator("date");
		Bean b1 = new Bean();
		Bean b2 = new Bean();
		
		assertEquals(0, comparator.compare(b1, b2));
		
		b1.date = new Date();
		assertTrue(comparator.compare(b1, b2) > 0);
		
		b1.date = null;
		b2.date = new Date();
		assertTrue(comparator.compare(b1, b2) < 0);
		
		//b1.date = new Date();
		b1.date = b2.date;
		assertEquals(0, comparator.compare(b1, b2));
		// fri_2013-02-06: failed on Hudson, not reproducible locally: "expected:<0> but was:<1>"
		// I do not understand this test: how do you expect b1 and b2 to be the same when they have different dates?
		// Commented this out and added "b1.date = b2.date;", hope that was the intent of this test.
	}
	
	public void testBooleans() {
		
		ReflectiveComparator comparator = new ReflectiveComparator("bool");
		Bean b1 = new Bean();
		Bean b2 = new Bean();
		
		assertEquals(0, comparator.compare(b1, b2));
		
		b1.bool = true;
		assertTrue(comparator.compare(b1, b2) > 0);
		
		b1.bool = false;
		b2.bool = true;
		assertTrue(comparator.compare(b1, b2) < 0);
	}
	
	public void testNumbersWithMethodRetention() {
		
		ReflectiveComparator comparator = new ReflectiveComparator("intnum", true);
		Bean b1 = new Bean();
		Bean b2 = new Bean();
		
		assertEquals(0, comparator.compare(b1, b2));
		
		b1.intnum = 123;
		assertTrue(comparator.compare(b1, b2) > 0);
		
		b2.intnum = 456;
		assertTrue(comparator.compare(b1, b2) < 0);
	}
	
	public void testSortOrder()	{
		final Bean b1 = new Bean();
		final Bean b2 = new Bean();
		final ReflectiveComparator comparator = new ReflectiveComparator("intnum", true);
		
		b1.intnum = 1;
		b1.string = "1";
		List<Bean> list = new ArrayList<Bean>();
		list.add(b1);
		list.add(b2);
		Collections.sort(list, comparator);
		
		// TODO why is null sorted to front?
		assertEquals(null, list.get(0).getString());
		assertEquals("1", list.get(1).getString());
		
		b2.intnum = 2;
		b2.string = "2";
		list.clear();
		list.add(b2);
		list.add(b1);
		Collections.sort(list, comparator);
		
		assertEquals("1", list.get(0).getString());
		assertEquals("2", list.get(1).getString());
	}

}
