package at.workflow.webdesk.po.timeline;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import at.workflow.webdesk.po.model.PoHistorization;
import at.workflow.webdesk.tools.date.DateTools;

/**
 * @author sdzuban 26.03.2013
 */
public class WTestTimelineComparator extends TestCase {

	public static class TestClass extends PoHistorization {

		private String UID;
		private String property;
		
		public TestClass(String uid, String property, Date from, Date to) {
			super();
			UID = uid;
			this.property = property;
			setValidfrom(from);
			setValidto(to);
		}
		
		@Override
		public String getUID() {
			return UID;
		}
		@Override
		public void setUID(String uid) {
			UID = uid;
		}
		public String getProperty() {
			return property;
		}
		public void setProperty(String first) {
			this.property = first;
		}
	}
	
	private static final Date FROM_1 = DateTools.toDate(2101, 1, 1);
	private static final Date FROM_2 = DateTools.toDate(2102, 1, 1);
	private static final Date FROM_3 = DateTools.toDate(2103, 1, 1);
	private static final Date TO_1 = DateTools.lastMomentOfDay(DateTools.toDate(2101, 12, 31));
	private static final Date TO_2 = DateTools.lastMomentOfDay(DateTools.toDate(2102, 12, 31));
	private static final Date TO_3 = DateTools.lastMomentOfDay(DateTools.toDate(2103, 12, 31));
	
	private List<TestClass> links1, links2;
	
	private TimelineComparator<TestClass> comparator = new TimelineComparator<TestClass>();

	/** {@inheritDoc} */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		links1 = new ArrayList<TestClass>();
		links2 = new ArrayList<TestClass>();
	}
	
	public void testStrictNulls() {
		
		assertTrue(comparator.areEqual(null, null, true));
		assertTrue(comparator.areEqual(links1, null, true));
		assertTrue(comparator.areEqual(null, links2, true));
	}
	
	public void testNonStrictNulls() {
		
		assertTrue(comparator.areEqual(null, null, false));
		assertTrue(comparator.areEqual(links1, null, false));
		assertTrue(comparator.areEqual(null, links2, false));
	}
	
	public void testStrictSame() {

		links1.add(new TestClass("one", "a", FROM_1, TO_1));
		links2.add(new TestClass("two", "a", FROM_1, TO_1));
		assertTrue(comparator.areEqual(links1, links2, true));

		links1.add(new TestClass("three", "b", FROM_2, TO_2));
		links2.add(new TestClass("four", "b", FROM_2, TO_2));
		assertTrue(comparator.areEqual(links1, links2, true));
	}
	
	public void testNonStrictSame() {
		
		links1.add(new TestClass("one", "a", FROM_1, TO_1));
		links2.add(new TestClass("two", "a", FROM_1, TO_1));
		assertTrue(comparator.areEqual(links1, links2, false));

		links1.add(new TestClass("three", "b", FROM_2, TO_2));
		links2.add(new TestClass("four", "b", FROM_2, TO_2));
		assertTrue(comparator.areEqual(links1, links2, false));
	}
	
	public void testStrictSameDiscontinuous() {
		
		links1.add(new TestClass("one", "a", FROM_1, TO_1));
		links2.add(new TestClass("two", "a", FROM_1, TO_1));
		assertTrue(comparator.areEqual(links1, links2, true));
		
		links1.add(new TestClass("three", "b", FROM_3, TO_3));
		links2.add(new TestClass("four", "b", FROM_3, TO_3));
		assertTrue(comparator.areEqual(links1, links2, true));
	}
	
	public void testNonStrictSameDiscontinuous() {
		
		links1.add(new TestClass("one", "a", FROM_1, TO_1));
		links2.add(new TestClass("two", "a", FROM_1, TO_1));
		assertTrue(comparator.areEqual(links1, links2, false));
		
		links1.add(new TestClass("three", "b", FROM_3, TO_3));
		links2.add(new TestClass("four", "b", FROM_3, TO_3));
		assertTrue(comparator.areEqual(links1, links2, false));
	}
	
	public void testStrictDifferentLinkCount() {
		
		links1.add(new TestClass("one", "a", FROM_1, TO_1));
		assertFalse(comparator.areEqual(links1, links2, true));

		links1.add(new TestClass("three", "b", FROM_2, TO_2));
		links2.add(new TestClass("four", "b", FROM_2, TO_2));
		assertFalse(comparator.areEqual(links1, links2, true));
	}
	
	public void testStrictDifferentLinkValidity() {
		
		links1.add(new TestClass("one", "a", FROM_1, TO_1));
		links2.add(new TestClass("two", "a", FROM_1, TO_2));
		assertFalse(comparator.areEqual(links1, links2, true));
		
		links1.add(new TestClass("three", "b", FROM_2, TO_2));
		links2.add(new TestClass("four", "b", FROM_3, TO_3));
		assertFalse(comparator.areEqual(links1, links2, true));
	}
	
	public void testStrictDifferentContinuity() {
		
		links1.add(new TestClass("one", "a", FROM_1, TO_1));
		links2.add(new TestClass("two", "a", FROM_1, TO_1));
		links1.add(new TestClass("three", "b", FROM_2, TO_3));
		links2.add(new TestClass("four", "b", FROM_3, TO_3));
		assertFalse(comparator.areEqual(links1, links2, true));
	}
	
	public void testStrictDifferentContent() {
		
		links1.add(new TestClass("one", "a", FROM_1, TO_1));
		links2.add(new TestClass("two", "b", FROM_1, TO_1));
		assertFalse(comparator.areEqual(links1, links2, true));
	}
	
	public void testNonStrictDifferentLinkCount1() {
		
		links1.add(new TestClass("one", "a", FROM_1, TO_1));
		assertFalse(comparator.areEqual(links1, links2, false));
		assertTrue(comparator.areNotEqual(links1, links2, false));
		
		links1.add(new TestClass("three", "b", FROM_2, TO_2));
		links2.add(new TestClass("four", "b", FROM_2, TO_2));
		assertTrue(comparator.areEqual(links1, links2, false));
		assertFalse(comparator.areNotEqual(links1, links2, false));
	}
	
	public void testNonStrictDifferentLinkCount2() {
		
		links2.add(new TestClass("two", "a", FROM_1, TO_1));
		assertTrue(comparator.areEqual(links1, links2, false));
		
		links1.add(new TestClass("three", "b", FROM_2, TO_2));
		links2.add(new TestClass("four", "b", FROM_2, TO_3));
		assertTrue(comparator.areEqual(links1, links2, false));
	}
	
	public void testNonStrictDifferentContinuityOneGap() {
		
		links1.add(new TestClass("one", "a", FROM_1, TO_1));
		links2.add(new TestClass("two", "a", FROM_1, TO_1));
		links1.add(new TestClass("three", "b", FROM_3, TO_3));
		links2.add(new TestClass("four", "b", FROM_2, TO_3));
		assertTrue(comparator.areEqual(links1, links2, false));
	}
	
	public void testNonStrictDifferentContinuityTwoGap() {
		
		links1.add(new TestClass("one", "a", FROM_1, TO_1));
		links2.add(new TestClass("two", "a", FROM_1, TO_1));
		links1.add(new TestClass("three", "b", FROM_2, TO_3));
		links2.add(new TestClass("four", "b", FROM_3, TO_3));
		assertFalse(comparator.areEqual(links1, links2, false));
	}
	
	public void testNonStrictDifferentContent() {
		
		links1.add(new TestClass("one", "a", FROM_1, TO_1));
		links2.add(new TestClass("two", "b", FROM_1, TO_1));
		assertFalse(comparator.areEqual(links1, links2, false));
	}
	
}
