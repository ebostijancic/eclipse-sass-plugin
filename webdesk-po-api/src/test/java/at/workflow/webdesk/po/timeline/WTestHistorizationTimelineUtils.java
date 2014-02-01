package at.workflow.webdesk.po.timeline;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.lang.time.DateUtils;

import at.workflow.webdesk.po.model.PoHistorization;
import at.workflow.webdesk.po.model.HistorizationComparator;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.date.DateInterval;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.date.Interval;

/**
 * @author sdzuban 26.03.2013
 */
public class WTestHistorizationTimelineUtils extends TestCase {

	public static class TestHistorization extends PoHistorization {

		private String uid;
		private Historization assignee;
		private Historization assigned;
		
		public TestHistorization(String uid) {
			this.uid = uid;
		}
		@Override
		public String getUID() {
			return uid;
		}
		@Override
		public void setUID(String uid) {
			this.uid = uid;
		}
		public Historization getAssignee() {
			return assignee;
		}
		public void setAssignee(Historization assignee) {
			this.assignee = assignee;
		}
		public Historization getAssigned() {
			return assigned;
		}
		public void setAssigned(Historization assigned) {
			this.assigned = assigned;
		}
	}
	

	private Date date1 = DateTools.toDate(2100, 1, 15);
	private Date date2 = DateTools.toDate(2100, 3, 15);
	private Date date3 = DateTools.toDate(2100, 5, 15);
	private Date date4 = DateTools.toDate(2100, 7, 15);
	private Date date5 = DateTools.toDate(2100, 9, 15);
	private Date date6 = DateTools.toDate(2100, 12, 15);
	private Historization h1, h2;
	private Set<Historization> historizations;
	private List<DateInterval> result;
	
	/** {@inheritDoc} */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		h1 = new TestHistorization(null);
		h2 = new TestHistorization(null);
		historizations = new HashSet<Historization>();
	}

	/** Proof that sort order is "oldest bottom (last), newest top (first)" */
	public void testTimelineComparatorNewestIsFirst() {
		final Date now = DateTools.now();
		final Date tomorrow = DateTools.tomorrow();
		final Date dayAfterTomorrow = DateUtils.addDays(now, 2);
		
		h1.setValidfrom(tomorrow);
		h1.setValidto(dayAfterTomorrow);
		
		h2.setValidfrom(now);
		h2.setValidto(tomorrow);
		
		final List<Historization> toSort = new ArrayList<Historization>();
		toSort.add(h2);
		toSort.add(h1);
		Collections.sort(toSort, new HistorizationComparator(true));
		
		assertEquals(h1, toSort.get(0));
		assertEquals(h2, toSort.get(1));
		
		Collections.sort(toSort, new HistorizationComparator(false));
		
		assertEquals(h1, toSort.get(1));
		assertEquals(h2, toSort.get(0));
	}
	
	/** Proof that shorter interval will be first when same start date. */
	public void testTimelineComparatorWithIntervalLength () {
		final Date now = DateTools.now();
		final Date tomorrow = DateTools.tomorrow();
		final Date dayAfterTomorrow = DateUtils.addDays(now, 2);
		
		h1.setValidfrom(now);
		h1.setValidto(tomorrow);
		
		h2.setValidfrom(now);	// same start day,
		h2.setValidto(dayAfterTomorrow);	// but interval one day longer
		
		final List<Historization> toSort = new ArrayList<Historization>();
		toSort.add(h2);
		toSort.add(h1);
		Collections.sort(toSort, new HistorizationComparator());
		
		assertEquals(h1, toSort.get(0));
		assertEquals(h2, toSort.get(1));
	}
	
	public void testTimelineComparator() {
		
		final Date now = DateTools.now();
		final Date tomorrow = DateUtils.addDays(now, 1);
		final Date yesterday = DateUtils.addDays(now, -1);
		
		HistorizationComparator comparator = new HistorizationComparator();
		
		h1.setValidfrom(now);
		h2.setValidfrom(now);
		assertEquals(0, comparator.compare(h1, h2));
		
		h1.setValidfrom(now);
		h2.setValidfrom(tomorrow);
		assertEquals(-1, comparator.compare(h1, h2));
		
		h1.setValidfrom(now);
		h2.setValidfrom(yesterday);
		assertEquals(1, comparator.compare(h1, h2));
		
	}
	
	public void testGetTimelineIntervals() {
		
		try {
			HistorizationTimelineUtils.getTimelineIntervals(null);
			fail("Accepted null historizations");
		} catch (Exception e) {}
		
		List<Interval> intervals = HistorizationTimelineUtils.getTimelineIntervals(historizations);
		assertNotNull(intervals);
		assertEquals(0, intervals.size());
		
		h1.setValidfrom(DateTools.dateOnly(date1));
		h1.setValidto(DateTools.lastMomentOfDay(date2));
		historizations.add(h1);
		intervals = HistorizationTimelineUtils.getTimelineIntervals(historizations);
		assertNotNull(intervals);
		assertEquals(1, intervals.size());
		assertEquals(DateTools.dateOnly(date1), intervals.get(0).getFrom());
		assertEquals(DateTools.lastMomentOfDay(date2), intervals.get(0).getTo());
		
		h2.setValidfrom(DateTools.dateOnly(date3));
		h2.setValidto(DateTools.lastMomentOfDay(date5));
		historizations.add(h2);
		intervals = HistorizationTimelineUtils.getTimelineIntervals(historizations);
		assertNotNull(intervals);
		assertEquals(2, intervals.size());
		assertEquals(DateTools.dateOnly(date1), intervals.get(0).getFrom());
		assertEquals(DateTools.lastMomentOfDay(date2), intervals.get(0).getTo());
		assertEquals(DateTools.dateOnly(date3), intervals.get(1).getFrom());
		assertEquals(DateTools.lastMomentOfDay(date5), intervals.get(1).getTo());
		
	}
	
	public void testIsTimelineNonOverlapping() {
		
		try {
			HistorizationTimelineUtils.isTimelineNonOverlapping(null);
			fail("Accepted null historizations");
		} catch (Exception e) {}
		
		assertTrue(HistorizationTimelineUtils.isTimelineNonOverlapping(historizations));
		historizations.add(h1);
		assertTrue(HistorizationTimelineUtils.isTimelineNonOverlapping(historizations));
		h1.setValidfrom(DateTools.dateOnly(date1));
		h1.setValidto(DateTools.lastMomentOfDay(date2));
		historizations.add(h2);
		h2.setValidfrom(DateTools.dateOnly(date3));
		h2.setValidto(DateTools.lastMomentOfDay(date5));
		assertTrue(HistorizationTimelineUtils.isTimelineNonOverlapping(historizations));
		assertFalse(HistorizationTimelineUtils.isTimelineOverlapping(historizations));
		h1.setValidto(DateTools.lastMomentOfDay(date4));
		assertFalse(HistorizationTimelineUtils.isTimelineNonOverlapping(historizations));
		assertTrue(HistorizationTimelineUtils.isTimelineOverlapping(historizations));
		
	}
	
	public void testGetTimelineOverlaps() {
		
		try {
			HistorizationTimelineUtils.getTimelineOverlaps(null);
			fail("Accepted null historizations");
		} catch (Exception e) {}
		
		result = HistorizationTimelineUtils.getTimelineOverlaps(historizations);
		assertNotNull(result);
		assertEquals(0, result.size());
		
		historizations.add(h1);
		result = HistorizationTimelineUtils.getTimelineOverlaps(historizations);
		assertNotNull(result);
		assertEquals(0, result.size());
		
		h1.setValidfrom(DateTools.dateOnly(date1));
		h1.setValidto(DateTools.lastMomentOfDay(date2));
		historizations.add(h2);
		h2.setValidfrom(DateTools.dateOnly(date3));
		h2.setValidto(DateTools.lastMomentOfDay(date5));
		result = HistorizationTimelineUtils.getTimelineOverlaps(historizations);
		assertNotNull(result);
		assertEquals(0, result.size());
		
		h1.setValidto(DateTools.lastMomentOfDay(date4));
		result = HistorizationTimelineUtils.getTimelineOverlaps(historizations);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(DateTools.dateOnly(date3), result.get(0).getFrom());
		assertEquals(DateTools.lastMomentOfDay(date4), result.get(0).getTo());
		
	}
	
	public void testIsTimelineContinuous() {
		
		try {
			HistorizationTimelineUtils.isTimelineContinuous(null);
			fail("Accepted null historizations");
		} catch (Exception e) {}
		
		assertTrue(HistorizationTimelineUtils.isTimelineContinuous(historizations));
		historizations.add(h1);
		assertTrue(HistorizationTimelineUtils.isTimelineContinuous(historizations));

		h1.setValidfrom(DateTools.dateOnly(date1));
		h1.setValidto(DateTools.lastMomentOfDay(date2));
		historizations.add(h2);
		h2.setValidfrom(DateTools.dateOnly(DateUtils.addDays(date2, 1)));
		h2.setValidto(DateTools.lastMomentOfDay(date4));
		assertTrue(HistorizationTimelineUtils.isTimelineContinuous(historizations));
		assertFalse(HistorizationTimelineUtils.isTimelineNotContinuous(historizations));
		h2.setValidfrom(DateTools.dateOnly(date3));
		assertFalse(HistorizationTimelineUtils.isTimelineContinuous(historizations));
		assertTrue(HistorizationTimelineUtils.isTimelineNotContinuous(historizations));
		
	}
	
	public void testGetTimelineGaps() {
		
		try {
			HistorizationTimelineUtils.getTimelineGaps(null);
			fail("Accepted null historizations");
		} catch (Exception e) {}
		
		result = HistorizationTimelineUtils.getTimelineGaps(historizations);
		assertNotNull(result);
		assertEquals(0, result.size());
		
		historizations.add(h1);
		result = HistorizationTimelineUtils.getTimelineGaps(historizations);
		assertNotNull(result);
		assertEquals(0, result.size());
		
		h1.setValidfrom(DateTools.dateOnly(date1));
		h1.setValidto(DateTools.lastMomentOfDay(date2));
		historizations.add(h2);
		h2.setValidfrom(DateTools.dateOnly(DateUtils.addDays(date2, 1)));
		h2.setValidto(DateTools.lastMomentOfDay(date4));
		result = HistorizationTimelineUtils.getTimelineGaps(historizations);
		assertNotNull(result);
		assertEquals(0, result.size());
		
		h2.setValidfrom(DateTools.dateOnly(date3));
		result = HistorizationTimelineUtils.getTimelineGaps(historizations);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(DateTools.lastMomentOfDay(date2), result.get(0).getFrom());
		assertEquals(DateTools.dateOnly(date3), result.get(0).getTo());
		
	}

	public void testGetTimelineGapsWithValidity() {
		
		DateInterval validity = new DateInterval(date1, date6);
		
		try {
			HistorizationTimelineUtils.getTimelineGaps(null, null);
			fail("Accepted null historizations");
		} catch (Exception e) {}
		
		result = HistorizationTimelineUtils.getTimelineGaps(historizations, validity);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(validity, result.get(0));
		
		historizations.add(h1);
		result = HistorizationTimelineUtils.getTimelineGaps(historizations, validity);
		assertNotNull(result);
		assertEquals(0, result.size());
		
		h1.setValidfrom(DateTools.dateOnly(date2));
		h1.setValidto(DateTools.lastMomentOfDay(date3));
		result = HistorizationTimelineUtils.getTimelineGaps(historizations, validity);
		assertNotNull(result);
		assertEquals(2, result.size());
		assertTrue(result.contains(new DateInterval(date1, DateTools.dateOnly(date2))));
		assertTrue(result.contains(new DateInterval(DateTools.lastMomentOfDay(date3), date6)));
		
		historizations.add(h2);
		h2.setValidfrom(DateTools.dateOnly(date4));
		h2.setValidto(DateTools.lastMomentOfDay(date5));
		result = HistorizationTimelineUtils.getTimelineGaps(historizations, validity);
		assertNotNull(result);
		assertEquals(3, result.size());
		
	}
	
	public void testCheckTimeline() {

		try {
			HistorizationTimelineUtils.checkTimeline(null);
			fail("Accepted null historizations");
		} catch (Exception e) {}
		
		try {
			HistorizationTimelineUtils.checkTimeline(Collections.<Historization>emptyList());
			fail("Accepted empty historizations");
		} catch (Exception e) {}
		
		h1.setValidfrom(date1);
		h1.setValidto(date3);
		historizations.add(h1);
		try {
			HistorizationTimelineUtils.checkTimeline(historizations);
			fail("Accepted historization not covering now to infinity");
		} catch (Exception e) {
			System.out.println(e);
		}
		
		h2.setValidfrom(date2);
		h2.setValidto(date5);
		historizations.add(h2);
		try {
			HistorizationTimelineUtils.checkTimeline(historizations, new DateInterval(date1, date5));
			fail("Accepted historizations with overlap");
		} catch (Exception e) {
			System.out.println(e);
		}
		
		h2.setValidfrom(date4);
		try {
			HistorizationTimelineUtils.checkTimeline(historizations, new DateInterval(date1, date5));
			fail("Accepted historizations that are not continuous");
		} catch (Exception e) {
			System.out.println(e);
		}
		
	}
	
	public void testIsTimelineComplete() {
		
		try {
			HistorizationTimelineUtils.isTimelineComplete(null, date2, date4);
			fail("Accepted null historizations");
		} catch (Exception e) {}
		
		assertTrue(HistorizationTimelineUtils.isTimelineComplete(historizations, date2, date4));
		historizations.add(h1);
		
		h1.setValidfrom(new Date());
		h1.setValidto(DateTools.INFINITY);
		assertTrue(HistorizationTimelineUtils.isTimelineComplete(historizations, new Date(), DateTools.INFINITY));

		h1.setValidfrom(new Date());
		h1.setValidto(date2);
		historizations.add(h2);
		h2.setValidfrom(DateUtils.addDays(new Date(), 1));
		h2.setValidto(DateTools.INFINITY);
		assertTrue(HistorizationTimelineUtils.isTimelineComplete(historizations, new Date(), DateTools.INFINITY));
		assertFalse(HistorizationTimelineUtils.isTimelineNotComplete(historizations, new Date(), DateTools.INFINITY));
		h2.setValidto(new Date());
		assertFalse(HistorizationTimelineUtils.isTimelineComplete(historizations, new Date(), DateTools.INFINITY));
		assertTrue(HistorizationTimelineUtils.isTimelineNotComplete(historizations, new Date(), DateTools.INFINITY));
		
	}
	
	/**
	 * Checks for smooth continuation from previous to next historization-
	 *  - previous ends on 23:59:59
	 *  - next starts on 00:00:00
	 *  - next starts on next day of previous end
	 */
	public void testIsContinuous() {
		
		try {
			HistorizationTimelineUtils.isContinuous(null, h1);
			fail("Accepted null historization");
		} catch (Exception e) { }
		
		try {
			HistorizationTimelineUtils.isContinuous(h1, null);
			fail("Accepted null historization");
		} catch (Exception e) { }
				
		h1.setValidto(new Date());
		h2.setValidfrom(new Date());
		// valid to is not 23:59:59
		assertFalse(HistorizationTimelineUtils.isContinuous(h1, h2));
		
		h1.setValidto(DateTools.lastMomentOfDay(DateUtils.addDays(new Date(), -1)));
		h2.setValidfrom(new Date());
		// valid from is not 00:00:00
		assertFalse(HistorizationTimelineUtils.isContinuous(h1, h2));
		
		h1.setValidto(new Date());
		h2.setValidfrom(DateTools.dateOnly(new Date()));
		assertFalse(HistorizationTimelineUtils.isContinuous(h1, h2));
		
		h1.setValidto(DateTools.lastMomentOfDay(new Date()));
		h2.setValidfrom(DateTools.dateOnly(new Date()));
		assertFalse(HistorizationTimelineUtils.isContinuous(h1, h2));
		assertTrue(HistorizationTimelineUtils.isNotContinuous(h1, h2));
		
		h1.setValidto(DateTools.lastMomentOfDay(DateUtils.addDays(new Date(), -1)));
		h2.setValidfrom(DateTools.dateOnly(new Date()));
		assertTrue(HistorizationTimelineUtils.isContinuous(h1, h2));
		assertFalse(HistorizationTimelineUtils.isNotContinuous(h1, h2));
	}

	/**
	 * Checks whether first Historization starts now or before
	 * and second Historization ends on infinity of after
	 */
	public void testIsComplete() {
		
		try {
			HistorizationTimelineUtils.isComplete(null, h1, date2, date4);
			fail("Accepted null historization");
		} catch (Exception e) { }
		
		try {
			HistorizationTimelineUtils.isComplete(h1, null, date2, date4);
			fail("Accepted null historization");
		} catch (Exception e) { }
				
		h2.setValidto(DateTools.INFINITY);
		assertTrue(HistorizationTimelineUtils.isComplete(h1, h2, new Date(), DateTools.INFINITY));

		h1.setValidfrom(DateUtils.addDays(new Date(), 1));
		assertFalse(HistorizationTimelineUtils.isComplete(h1, h2, new Date(), DateTools.INFINITY));
		
		h1.setValidfrom(DateUtils.addDays(new Date(), -1));
		assertTrue(HistorizationTimelineUtils.isComplete(h1, h2, new Date(), DateTools.INFINITY));
		
		h2.setValidto(DateUtils.addDays(DateTools.INFINITY, 1));
		assertTrue(HistorizationTimelineUtils.isComplete(h1, h2, new Date(), DateTools.INFINITY));
		assertFalse(HistorizationTimelineUtils.isNotComplete(h1, h2, new Date(), DateTools.INFINITY));

		h2.setValidto(DateUtils.addDays(DateTools.INFINITY, -1));
		assertFalse(HistorizationTimelineUtils.isComplete(h1, h2, new Date(), DateTools.INFINITY));
		assertTrue(HistorizationTimelineUtils.isNotComplete(h1, h2, new Date(), DateTools.INFINITY));
		
		h1.setValidfrom(new Date());
		h1.setValidto(DateTools.INFINITY);
		assertTrue(HistorizationTimelineUtils.isComplete(h1, new Date(), DateTools.INFINITY));
		assertFalse(HistorizationTimelineUtils.isNotComplete(h1, new Date(), DateTools.INFINITY));

		h1.setValidfrom(DateUtils.addDays(new Date(), 1));
		h1.setValidto(DateTools.INFINITY);
		assertFalse(HistorizationTimelineUtils.isComplete(h1, new Date(), DateTools.INFINITY));
		assertTrue(HistorizationTimelineUtils.isNotComplete(h1, new Date(), DateTools.INFINITY));
		
		h1.setValidfrom(new Date());
		h1.setValidto(DateUtils.addDays(DateTools.INFINITY, -1));
		assertFalse(HistorizationTimelineUtils.isComplete(h1, new Date(), DateTools.INFINITY));
		assertTrue(HistorizationTimelineUtils.isNotComplete(h1, new Date(), DateTools.INFINITY));
	}
	
	public void testCheckValidityDates() {
		
		Historization hist = new TestHistorization(null);
		hist.setValidfrom(date2);
		hist.setValidto(date4);
		
		try {
			HistorizationTimelineUtils.checkValidityDates(hist, date1, date5);
			fail("Accepted historization valid only on part of the interval");
		} catch (Exception e) {
			System.out.println(e);
		}
		
		try {
			HistorizationTimelineUtils.checkValidityDates(hist, date1, date3);
			fail("Accepted historization valid too late");
		} catch (Exception e) {
			System.out.println(e);
		}
		
		try {
			HistorizationTimelineUtils.checkValidityDates(hist, date3, date5);
			fail("Accepted historization valid not long enough");
		} catch (Exception e) {
			System.out.println(e);
		}
		
		try {
			HistorizationTimelineUtils.checkValidityDates(hist, date2, date4);
		} catch (Exception e) {
			fail("Rejected historization valid exactly in the interval " + e);
		}
		
		try {
			HistorizationTimelineUtils.checkValidityDates(hist, date3, date3);
		} catch (Exception e) {
			fail("Rejected historization valid around the interval");
		}
		
	}
	
	public void testPrepareForAssignmentNulls() {
		
		Historization assignee = new TestHistorization(null);
		Historization assigned = new TestHistorization(null);
		
		try {
			HistorizationTimelineUtils.prepareAssignment(null, null, date1, date2);
			fail("Accepted null entities.");
		} catch (Exception e) {}
		try {
			HistorizationTimelineUtils.prepareAssignment(null, assigned, date1, date2);
			fail("Accepted null assignee entity.");
		} catch (Exception e) {}
		try {
			HistorizationTimelineUtils.prepareAssignment(assignee, null, date1, date2);
			fail("Accepted null assigned entity.");
		} catch (Exception e) {}
	}
	
	public void testPrepareForAssignmentNotPersisted() {
		
		Historization assignee = new TestHistorization(null);
		Historization assigned = new TestHistorization(null);
		
		try {
			HistorizationTimelineUtils.prepareAssignment(assignee, assigned, date1, date2);
			fail("Accepted unpersisted entities.");
		} catch (Exception e) {}
		
		assignee = new TestHistorization("uid");
		assignee.setValidfrom(date1);
		assignee.setValidto(date5);
		
		try {
			HistorizationTimelineUtils.prepareAssignment(assignee, assigned, date1, date2);
		} catch (Exception e) {
			fail("Rejected persisted assignee entity. " + e);
		}
		assignee = new TestHistorization(null);
		assigned = new TestHistorization("uid");
		assigned.setValidfrom(date1);
		assigned.setValidto(date5);
		
		try {
			HistorizationTimelineUtils.prepareAssignment(assignee, assigned, date1, date2);
		} catch (Exception e) {
			fail("Rejected persisted assigned entity. " + e);
		}
		assignee = new TestHistorization("uid");
		assignee.setValidfrom(date1);
		assignee.setValidto(date5);
		
		try {
			HistorizationTimelineUtils.prepareAssignment(assignee, assigned, date1, date2);
		} catch (Exception e) {
			fail("Rejected persisted entities. " + e);
		}
	}
		
	public void testPrepareForAssignmentTimelineGeneration() {
		
		Historization assignee = new TestHistorization(null);
		Historization assigned = new TestHistorization("uid");
		Date earlyNow = new Date(); // early now, laying milliseconds before any other now in test
		
		assigned.setValidfrom(date1);
		assigned.setValidto(DateTools.lastMomentOfDay(date4));
		Interval interval = HistorizationTimelineUtils.prepareAssignment(assignee, assigned, date2, date3);
		assertNotNull(interval);
		assertEquals(DateTools.dateOnly(date2), interval.getFrom());
		assertEquals(DateTools.lastMomentOfDay(date3), interval.getTo());

		// forced first assignment -> validity of assignee is taken instead fo date2 and date3
		assignee.setValidfrom(date1);
		assignee.setValidto(DateTools.lastMomentOfDay(date4));
		interval = HistorizationTimelineUtils.prepareAssignment(assignee, assigned);
		assertNotNull(interval);
		assertEquals(date1, interval.getFrom());
		assertEquals(DateTools.lastMomentOfDay(date4), interval.getTo());
		
		// forced first assignment -> validity of assignee is taken instead of date2 and date3
		assignee.setValidfrom(earlyNow);
		assignee.setValidto(DateTools.INFINITY);
		assigned.setValidfrom(earlyNow);
		assigned.setValidto(DateTools.INFINITY);
		interval = HistorizationTimelineUtils.prepareAssignment(assignee, assigned);
		assertNotNull(interval);
		assertEquals(DateTools.now().getTime(), interval.getFrom().getTime(), 1000);
		assertEquals(DateTools.INFINITY, interval.getTo());
	}
	
	public void testCheckValidityAssigned() {
		
		Historization assignee = new TestHistorization(null);
		Historization assigned = new TestHistorization("uid");
		
		assigned.setValidfrom(date2);
		assigned.setValidto(date4);
		
		try { // assignee is taken as now, infinity 
			HistorizationTimelineUtils.checkValidity(assignee, assigned, date1, date5);
			fail("Accepted not long enough valid assigned entity.");
		} catch (Exception e) {
			System.out.println(e);
		}
		
		assignee.setValidfrom(date2);
		assignee.setValidto(date4);
		assigned.setValidfrom(date1);
		assigned.setValidto(date5);
		try { 
			HistorizationTimelineUtils.checkValidity(assignee, assigned, date1, date5);
			fail("Accepted not long enough valid assignee entity.");
		} catch (Exception e) {
			System.out.println(e);
		}

		assigned.setValidfrom(date2);
		assigned.setValidto(DateTools.lastMomentOfDay(date4));
		try { // assignee is taken as now, infinity and determines the validity length 
			HistorizationTimelineUtils.checkValidity(assignee, assigned, date1, date5);
			fail("Accepted not long enough valid assigned entity.");
		} catch (Exception e) {
			System.out.println(e);
		}
		
		assignee.setValidfrom(date1);
		assignee.setValidto(date5);
		assigned.setValidto(DateTools.lastMomentOfDay(date3));
		try { 
			HistorizationTimelineUtils.checkValidity(assignee, assigned, date2, date4);
			fail("Accepted not long enough valid assigned entity.");
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void testPrepareAssignment() {
		
		Historization assignee = new TestHistorization(null);
		Historization assigned = new TestHistorization("uid");
		Date earlyNow = new Date(); // early now, lying milliseconds before any other now in test
		
		assigned.setValidfrom(date1);
		assigned.setValidto(DateTools.lastMomentOfDay(date5));
		// first assignment forced complete
		Interval  interval = HistorizationTimelineUtils.prepareAssignment(assignee, assigned);
		assertNotNull(interval);
		assertEquals(DateTools.now().getTime(), assignee.getValidfrom().getTime(), 1000);
		assertEquals(DateTools.INFINITY, assignee.getValidto());
		// further assignment
		interval = HistorizationTimelineUtils.prepareAssignment(assignee, assigned, date2, date4);
		assertNotNull(interval);
		assertEquals(DateTools.dateOnly(date2), interval.getFrom());
		assertEquals(DateTools.lastMomentOfDay(date4), interval.getTo());

		// now to infinity tests
		assignee.setValidfrom(earlyNow);
		assignee.setValidto(DateTools.INFINITY);
		assigned.setValidfrom(earlyNow);
		assigned.setValidto(DateTools.INFINITY);
		interval = HistorizationTimelineUtils.prepareAssignment(assignee, assigned, date1, date5);
		assertNotNull(interval);
		assertEquals(DateTools.now().getTime(), assignee.getValidfrom().getTime(), 1000);
		assertEquals(DateTools.INFINITY, assignee.getValidto());
		
		interval = HistorizationTimelineUtils.prepareAssignment(assignee, assigned, date1, date5);
		assertNotNull(interval);
		assertEquals(DateTools.now().getTime(), assignee.getValidfrom().getTime(), 1000);
		assertEquals(DateTools.INFINITY, assignee.getValidto());

		assigned.setValidfrom(earlyNow);
		assigned.setValidto(DateTools.INFINITY);
		assignee.setValidfrom(null);
		assignee.setValidto(null);
		// first assignment
		interval = HistorizationTimelineUtils.prepareAssignment(assignee, assigned, date1, date5);
		assertNotNull(interval);
		assertEquals(DateTools.now().getTime(), assignee.getValidfrom().getTime(), 1000);
		assertEquals(DateTools.INFINITY, assignee.getValidto());
		// further assignment
		interval = HistorizationTimelineUtils.prepareAssignment(assignee, assigned);
		assertNotNull(interval);
		assertEquals(DateTools.now().getTime(), assignee.getValidfrom().getTime(), 1000);
		assertEquals(DateTools.INFINITY, assignee.getValidto());
		
	}
	
	public void testPrepareForAssignmentUsefulAsigneeDates() {

		Historization assignee = new TestHistorization(null);
		Historization assigned = new TestHistorization("uid");
		
		assigned.setValidfrom(DateTools.now());
		assigned.setValidto(DateTools.INFINITY);
		HistorizationTimelineUtils.prepareAssignment(assignee, assigned, date2, date3);
		assertEquals(DateTools.now().getTime(), assignee.getValidfrom().getTime(), 1000);
		assertEquals(DateTools.INFINITY, assignee.getValidto());
		
		assignee = new TestHistorization(null);
		HistorizationTimelineUtils.prepareAssignment(assignee, assigned, date2, date3);
		assertEquals(DateTools.now().getTime(), assignee.getValidfrom().getTime(), 1000);
		assertEquals(DateTools.INFINITY, assignee.getValidto());
		
		assignee.setValidfrom(DateUtils.addHours(date1, 5));
		assignee.setValidto(date5);
		HistorizationTimelineUtils.prepareAssignment(assignee, assigned, date2, date3);
		assertEquals(DateTools.dateOnly(date1), assignee.getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(date5), assignee.getValidto());
		
		HistorizationTimelineUtils.prepareAssignment(assignee, assigned, date2, date3);
		assertEquals(DateTools.dateOnly(date1), assignee.getValidfrom());
		assertEquals(DateTools.lastMomentOfDay(date5), assignee.getValidto());
		
	}
	
	public void testIsValid() {
		
		TestHistorization test = new TestHistorization(null);
		test.setValidfrom(date2);
		test.setValidto(date4);
		
		assertTrue(HistorizationTimelineUtils.isValid(test, date2, date4));
		assertTrue(HistorizationTimelineUtils.isValid(test, date1, date3));
		assertTrue(HistorizationTimelineUtils.isValid(test, date3, date5));
		assertTrue(HistorizationTimelineUtils.isValid(test, date1, date5));
		
		assertFalse(HistorizationTimelineUtils.isValid(test, date1, date2));
		final Date date4PlusOneDay = DateUtils.addDays(date4, 1);
		assertFalse(HistorizationTimelineUtils.isValid(test, date4PlusOneDay, date5));
		
		test.setValidfrom(date1);
		test.setValidto(date5);
		
		assertTrue(HistorizationTimelineUtils.isValid(test, date3, date4));
		
		test.setValidfrom(date1);
		test.setValidto(date2);
		
		assertFalse(HistorizationTimelineUtils.isValid(test, date3, date4));
		
		test.setValidfrom(date4);
		test.setValidto(date5);
		
		assertFalse(HistorizationTimelineUtils.isValid(test, date1, date2));
		
	}

	public void testFilterAssignments() {
		
		Collection<Historization> complete = new ArrayList<Historization>();
		
		List<Historization> timeline = HistorizationTimelineUtils.filterAssignments(complete, date3, date4);
		assertNotNull(timeline);
		assertEquals(0, timeline.size());
		
		TestHistorization test = new TestHistorization(null);
		test.setValidfrom(date3);
		test.setValidto(date4);
		
		complete.add(test);

		timeline = HistorizationTimelineUtils.filterAssignments(complete, date1, date2);
		assertEquals(0, timeline.size());
		
		timeline = HistorizationTimelineUtils.filterAssignments(complete, date1, date3);
		assertEquals(0, timeline.size());
		
		timeline = HistorizationTimelineUtils.filterAssignments(complete, date1, date4);
		assertEquals(1, timeline.size());
		
		timeline = HistorizationTimelineUtils.filterAssignments(complete, date1, date5);
		assertEquals(1, timeline.size());

		timeline = HistorizationTimelineUtils.filterAssignments(complete, date1, date6);
		assertEquals(1, timeline.size());
		
		
		timeline = HistorizationTimelineUtils.filterAssignments(complete, date5, date6);
		assertEquals(0, timeline.size());
		
		final Date date4PlusOneDay = DateUtils.addDays(date4, 1);
		timeline = HistorizationTimelineUtils.filterAssignments(complete, date4PlusOneDay, date6);
		assertEquals(0, timeline.size());
		
		timeline = HistorizationTimelineUtils.filterAssignments(complete, date3, date6);
		assertEquals(1, timeline.size());
		
		timeline = HistorizationTimelineUtils.filterAssignments(complete, date2, date6);
		assertEquals(1, timeline.size());
		
		
		test.setValidfrom(date2);
		test.setValidto(date5);

		timeline = HistorizationTimelineUtils.filterAssignments(complete, date3, date4);
		assertEquals(1, timeline.size());
		
		timeline = HistorizationTimelineUtils.filterAssignments(complete, date2, date4);
		assertEquals(1, timeline.size());
		
		timeline = HistorizationTimelineUtils.filterAssignments(complete, date1, date4);
		assertEquals(1, timeline.size());
		
		timeline = HistorizationTimelineUtils.filterAssignments(complete, date3, date5);
		assertEquals(1, timeline.size());
		
		timeline = HistorizationTimelineUtils.filterAssignments(complete, date4, date5);
		assertEquals(1, timeline.size());
		
		
	}
	
	public void testGetAssignmentsWithUncorrelatedValidity() throws Exception {

		Method getAssignee = TestHistorization.class.getMethod("getAssignee");
		Method getAssigned = TestHistorization.class.getMethod("getAssigned");
		Collection<Historization> links = new ArrayList<Historization>();
		
		try {
			HistorizationTimelineUtils.getAssignmentsWithUncorrelatedValidity(null, null, getAssigned);
			fail("Accepted null getAssignee");
		} catch (Exception e) { }
		
		try {
			HistorizationTimelineUtils.getAssignmentsWithUncorrelatedValidity(null, getAssignee, null);
			fail("Accepted null getAssigned");
		} catch (Exception e) { }
		
		List<Historization> result = 
				HistorizationTimelineUtils.getAssignmentsWithUncorrelatedValidity(null, getAssignee, getAssigned);
		assertNotNull(result);
		assertEquals(0, result.size());
		
		Historization assignee = new TestHistorization(null);
		assignee.setValidfrom(date1);
		assignee.setValidto(date6);
		
		Historization assigned = new TestHistorization(null);
		assigned.setValidfrom(date1);
		assigned.setValidto(date6);
		
		TestHistorization link = new TestHistorization(null);
		link.setValidfrom(date2);
		link.setValidto(date5);
		link.setAssignee(assignee);
		link.setAssigned(assigned);
		
		links.add(link);
		
		result = HistorizationTimelineUtils.getAssignmentsWithUncorrelatedValidity(links, getAssignee, getAssigned);
		assertNotNull(result);
		assertEquals(0, result.size());

		assignee.setValidfrom(date3);
		result = HistorizationTimelineUtils.getAssignmentsWithUncorrelatedValidity(links, getAssignee, getAssigned);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		assignee.setValidfrom(date1);
		assignee.setValidto(date4);
		result = HistorizationTimelineUtils.getAssignmentsWithUncorrelatedValidity(links, getAssignee, getAssigned);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		assignee.setValidto(date6);
		assigned.setValidfrom(date3);
		result = HistorizationTimelineUtils.getAssignmentsWithUncorrelatedValidity(links, getAssignee, getAssigned);
		assertNotNull(result);
		assertEquals(1, result.size());
		
		assigned.setValidfrom(date1);
		assigned.setValidto(date4);
		result = HistorizationTimelineUtils.getAssignmentsWithUncorrelatedValidity(links, getAssignee, getAssigned);
		assertNotNull(result);
		assertEquals(1, result.size());
		
	}
}
