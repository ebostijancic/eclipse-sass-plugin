package at.workflow.webdesk.po.timeline;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import at.workflow.webdesk.po.model.HistorizationComparator;
import at.workflow.webdesk.tools.NamingConventionI18n;
import at.workflow.webdesk.tools.api.BusinessLogicException;
import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.api.I18nMessage;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.date.DateInterval;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.date.HistorizationHelper;
import at.workflow.webdesk.tools.date.Interval;

/**
 * This class provides following methods for timeline checking:
 * - compare and sort of collection of Historization objects
 * - check whether there is no overlap in timeline
 * - check whether there is no space on timeline
 * - list of all gaps
 * - list all overlaps
 * 
 * @author sdzuban 26.03.2013
 */
public class HistorizationTimelineUtils {
	
	public static final String I18N_KEY_PREFIX = "po_timelineException_";
	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
	
	/**
	 * Replaces HistorizationHelper isValid() because that does not work with infinity.
	 * The reason is that for timeline the exact overlap of validity of entities  
	 * like 23.3.2013 - 23.6.2013 with 23.3.2013 - 23.6.2013 is fully acceptable. 
	 * @param hist
	 * @param from
	 * @param to
	 * @param other the other object of the assignment 
	 * @throws runtime exception stating what failed 
	 */
	public static void checkValidityDates(Historization hist, Date from, Date to, Object... others) {

		Object other = null;
		if (others != null && others.length > 0)
			other = others[0];
		
		if (hist.getValidfrom().getTime() > from.getTime()) {
			String i18nKey = I18N_KEY_PREFIX + 
					"{0}_{1}_is_not_valid_at_beginning_of_assignment" + 
					(other == null ? "" : "_to_{2}_{3}") +
					"_{4}_vs_{5}";
			I18nMessage message = 
					getInsufficientValidityMessage(i18nKey, hist, other, hist.getValidfrom(), from);
			throw new BusinessLogicException(message);
		}
		if (hist.getValidto().getTime() < to.getTime()) {
			String i18nKey = I18N_KEY_PREFIX + 
					"{0}_{1}_is_not_valid_at_end_of_assignment" + 
					(other == null ? "" : "_to_{2}_{3}") +
					"_{4}_vs_{5}";
			I18nMessage message = 
					getInsufficientValidityMessage(i18nKey, hist, other, hist.getValidto(), to);
			throw new BusinessLogicException(message);
		}
	}

	private static I18nMessage getInsufficientValidityMessage(String i18nKey, Historization hist, Object other, Date from, Date to) {
		
		String[] parameters = {
				NamingConventionI18n.getI18nKey(hist.getClass()),
				getIdentity(hist),
				other == null ? "" : NamingConventionI18n.getI18nKey(other.getClass()),
				other == null ? "" : getIdentity(other),
				sdf.format(from),
				sdf.format(to)
		};
		Boolean[] flags = { 
				Boolean.TRUE, 
				Boolean.FALSE, 
				new Boolean(other != null), 
				Boolean.FALSE,
				Boolean.FALSE, 
				Boolean.FALSE
		};
		return new I18nMessage(i18nKey, parameters, flags);
	}
	
	private static String getIdentity(Object object) {

		Class<?> clazz = object.getClass();
		Method[] methods = clazz.getMethods();
		// try shortName
		for (Method method : methods) {
			String methodName = method.getName();
			if ("getShortName".equals(methodName))
				try {
					return (String) method.invoke(object);
				} catch (Exception e) {
					// nothing to do - next method will be tried
				}
		}
		// try other names
		for (Method method : methods) {
			String methodName = method.getName();
			if (Arrays.asList("getName", "getFullName").contains(methodName))
				try {
					return (String) method.invoke(object);
				} catch (Exception e) {
					// nothing to do - next method will be tried
				}
		}
		return object.toString();
	}

	/**
	 * Is intended for checks of "X at all times must relate exactly to one Y".
	 * Checks following conditions:
	 * - not null & not empty timeline
	 * - covering at least validity of the entity, if null than now to infinity is considered
	 * - no overlapping relationships
	 * - no gaps between relationships
	 * 
	 * @param timeline between the entity and other entities of same class
	 * @param valildity the time range for which the timeline is checked.
	 * @throws runtime exception if timeline is empty or more than one validity is supplied
	 * BusinessLogicException if the timeline is not complete, has overlaps or gaps
	 */
	public static void checkTimeline(Collection<? extends Historization> timeline, Interval... validity) {

		if (CollectionUtils.isEmpty(timeline))
			throw new IllegalArgumentException("Timeline historization must not be null nor empty");
		if (validity != null && validity.length > 1)
			throw new IllegalArgumentException("Maximal one validity can be supplied");
		
		// Defaults
		Date usefulFrom = DateTools.now();
		Date usefulTo = DateTools.INFINITY;
		// if available, entity lifetime is taken
		if (validity != null && validity.length > 0) {
			usefulFrom = HistorizationHelper.generateUsefulValidFrom(validity[0].getFrom());
			usefulTo = HistorizationHelper.generateUsefulValidTo(validity[0].getTo());
		}
		
		Class<? extends Historization> histEntityClass = timeline.iterator().next().getClass();
		
		if (isTimelineNotComplete(timeline, usefulFrom, usefulTo)) {
			String i18nKey = I18N_KEY_PREFIX + "timeline_of_{0}_does_not_cover_whole_interval";
			throw new BusinessLogicException(getTimelineErrorMessage(i18nKey, histEntityClass));
		}
		if (isTimelineOverlapping(timeline)) {
			String i18nKey = I18N_KEY_PREFIX + "timeline_of_{0}_contains_overlaps";
			throw new BusinessLogicException(getTimelineErrorMessage(i18nKey, histEntityClass));
		}
		if (isTimelineNotContinuous(timeline)) {
			String i18nKey = I18N_KEY_PREFIX + "timeline_of_{0}_contains_gaps";
			throw new BusinessLogicException(getTimelineErrorMessage(i18nKey, histEntityClass));
		}
	}
	
	private static I18nMessage getTimelineErrorMessage(String i18nKey, Class<? extends Historization> clazz) {
		return new I18nMessage(
				i18nKey, 
				new String[] { NamingConventionI18n.getI18nKey(clazz) }, 
				new Boolean[] { Boolean.TRUE }
				);
	}
	
	/** @return true if there is overlapping of any of given historizations */
	public static boolean isTimelineOverlapping(Collection<? extends Historization> historizations) {
		return isTimelineNonOverlapping(historizations) == false;
	}
	
	/** @return true if there is no overlapping of any of given historizations */
	public static boolean isTimelineNonOverlapping(Collection<? extends Historization> historizations) {
		
		if (historizations == null)
			throw new IllegalArgumentException("Historizations collection must be non-null");
		if (historizations.size() <= 1)
			return true;
		
		List<Historization> historizationList = getSortedHistorizationList(historizations);

		for (int i = 1; i < historizationList.size(); i++) {
			Historization previous = historizationList.get(i-1);
			Historization current = historizationList.get(i);
			if ( previous.getValidto().after(current.getValidfrom()))
				return false;
		}
		return true;
	}
	
	/** @return overlapping time intervals of consecutive historizations */
	public static List<DateInterval> getTimelineOverlaps(Collection<? extends Historization> historizations) {
		
		if (historizations == null)
			throw new IllegalArgumentException("Historizations collection must be non-null");
		if (historizations.size() <= 1)
			return Collections.<DateInterval>emptyList();
		
		List<Historization> historizationList = getSortedHistorizationList(historizations);

		List<DateInterval> timelineOverlaps = new ArrayList<DateInterval>();
		for (int i = 1; i < historizationList.size(); i++) {
			Historization previous = historizationList.get(i-1);
			Historization current = historizationList.get(i);
			if ( previous.getValidto().after(current.getValidfrom()))
				timelineOverlaps.add(new DateInterval(current.getValidfrom(), previous.getValidto()));
		}
		return timelineOverlaps;
	}
	
	/** @return true if there is no overlapping of and no gap between consecutive historizations */
	public static boolean isTimelineContinuous(Collection<? extends Historization> historizations) {
		
		if (historizations == null)
			throw new IllegalArgumentException("Historizations collection must be non-null");
		if (historizations.size() <= 1)
			return true;
		
		List<Historization> historizationList = getSortedHistorizationList(historizations);

		for (int i = 1; i < historizationList.size(); i++) {
			Historization previous = historizationList.get(i-1);
			Historization current = historizationList.get(i);
			if (isNotContinuous(previous, current))
				return false;
		}
		return true;
	}
	
	/** @return true if there are gaps between consecutive historizations */
	public static boolean isTimelineNotContinuous(Collection<? extends Historization> historizations) {
		return isTimelineContinuous(historizations) == false;
	}
	
	/** @return timeline gaps between consecutive historizations */
	public static List<DateInterval> getTimelineGaps(Collection<? extends Historization> historizations, Interval validity) {
		
		if (historizations == null)
			throw new IllegalArgumentException("Historizations collection must be non-null");
		
		DateInterval dateValidity = validity == null 
				? new DateInterval(DateTools.now(), DateTools.INFINITY) 
				: (DateInterval) validity;
				
		if (historizations.size() == 0) {
			
			List<DateInterval> result = new ArrayList<DateInterval>();
			result.add(dateValidity);
			return result;
			
		} else {
			
			List<Historization> orderedHistorizations = getSortedHistorizationList(historizations);
			Historization first = orderedHistorizations.get(0);
			Historization last = orderedHistorizations.get(orderedHistorizations.size() - 1);
			
			ArrayList<DateInterval> gaps = (ArrayList<DateInterval>) getTimelineGaps(historizations); // inner gaps
			DateInterval firstGap = new DateInterval(validity.getFrom(), first.getValidfrom());
			if (firstGap.isPositive())
				gaps.add(firstGap);
			// to prevent case where last.validto = 23:59:59,000 (from MySQL DB)
			// and validity.to = 23:59:59,900 (computed) being reported
			if (DateTools.isLastMomentOfDay(last.getValidto()) && DateTools.isLastMomentOfDay(validity.getTo()) &&
					DateTools.isOnSameDay(last.getValidto(), validity.getTo()))
				return gaps;
			
			DateInterval lastGap = new DateInterval(last.getValidto(), validity.getTo());
			if (lastGap.isPositive())
				gaps.add(lastGap);
			
			return gaps;
		}
	}
		
	/** @return timeline gaps between consecutive historizations */
	public static List<DateInterval> getTimelineGaps(Collection<? extends Historization> historizations) {
		
		if (historizations == null)
			throw new IllegalArgumentException("Historizations collection must be non-null");
		
		List<Historization> historizationList = new ArrayList<Historization>(historizations);
		if (historizationList.size() <= 1)
			return new ArrayList<DateInterval>();
		
		Collections.sort(historizationList, new HistorizationComparator());
		List<DateInterval> timelineGaps = new ArrayList<DateInterval>();
		for (int i = 1; i < historizationList.size(); i++) {
			Historization previous = historizationList.get(i-1);
			Historization current = historizationList.get(i);
			DateInterval gap = new DateInterval(previous.getValidto(), current.getValidfrom());
			if (false == isContinuous(previous, current) && gap.isPositive()) // gap.isNegative() means overlap
				timelineGaps.add(gap);
		}
		return timelineGaps;
	}

	/** @return true if the timeline starts before or on from and ends on to or after */
	public static boolean isTimelineComplete(Collection<? extends Historization> historizations, Date from, Date to) {
		
		if (historizations == null)
			throw new IllegalArgumentException("Historizations collection must be non-null");
		
		List<Historization> historizationList = new ArrayList<Historization>(historizations);
		if (historizationList.size() == 0)
			return true;
		
		if (historizationList.size() == 1) 
			return isComplete(historizationList.get(0), from, to);
		
		Collections.sort(historizationList, new HistorizationComparator());
		return isComplete(historizationList.get(0), historizationList.get(historizationList.size() - 1), from, to);
	}
	
	/** @return true if the timeline does not start before or on from or does not end on to or after */
	public static boolean isTimelineNotComplete(Collection<? extends Historization> historizations, Date from, Date to) {
		return isTimelineComplete(historizations, from, to) == false;
	}
		
	/**
	 * Checks for smooth continuation from previous to next historization-
	 * @param previous
	 * @param next
	 * @return true when
	 *  - previous ends on 23:59:59
	 *  - next starts on 00:00:00
	 *  - next starts on next day of previous end
	 *  
	 *  TODO ? do we need also continuity for validto = 26.03.2013 10:06:00 / validfrom = 26.03.2013 10:06:??,??
	 */
	public static final boolean isContinuous(Historization previous, Historization next) {
		
		if (previous == null || next == null)
			throw new IllegalArgumentException("Both historizations must be non-null");
		if (previous.getValidto() == null || next.getValidfrom() == null)
			throw new IllegalArgumentException("Both previous valid to date and next valid from date must be non-null");
		
		return 
//				previous.getValidto().equals(next.getValidfrom()) || // assignments done in now. TODO or are all assignments from dateOnly to lastMinute?
				DateTools.isLastMomentOfDay(previous.getValidto()) &&
				next.getValidfrom().equals(DateTools.dateOnly(next.getValidfrom())) &&
				DateTools.isOnSameDay(DateUtils.addDays(previous.getValidto(), 1), next.getValidfrom());
	}
	
	/**
	 * Checks for gap between previous and next historization-
	 * @param previous
	 * @param next
	 * @return true when
	 *  - previous ends not on 23:59:59
	 *  - next starts not on 00:00:00
	 *  - next starts 2nd+ next day of previous end
	 *  
	 *  TODO ? do we need also continuity for validto = 26.03.2013 10:06:00 / validfrom = 26.03.2013 10:06:??,??
	 */
	public static final boolean isNotContinuous(Historization previous, Historization next) {
		return isContinuous(previous, next) == false;
	}
	
	/**
	 * This method can be used to determine whether one or two historization objects
	 * do not cover now to infinity timespan 
	 * @param hist1 one object or first object in timeline
	 * @param hist2 one object or last object in timeline
	 * @return true if the timespan now - infinity is not completely covered
	 */
	public static final boolean isNotComplete(Historization hist1, Historization hist2, Date from, Date to) {
		return isComplete(hist1, hist2, from, to) == false;
	}
	
	/**
	 * This method can be used to determine whether one Historization object is valid in specified timerange
	 * @param hist one object 
	 * @return true if the specified timerange is covered
	 */
	public static final boolean isComplete(Historization hist, Date from, Date to) {
		return isComplete(hist, hist, from, to);
	}
	
	/**
	 * This method can be used to determine whether one Historization object is not valid in specified timerange
	 * @param hist one object 
	 * @return false if the specified timerange is covered
	 */
	public static final boolean isNotComplete(Historization hist, Date from, Date to) {
		return isComplete(hist, from, to) == false;
	}
	
	/**
	 * This method can be used to determine 
	 * or whether the first Historization object in the timeline is valid on from
	 * and the second Historization object is valid on to
	 * @param hist1 one object or first object in timeline
	 * @param hist2 one object or last object in timeline
	 * @return true if the timespan from - to is covered
	 */
	public static final boolean isComplete(Historization hist1, Historization hist2, Date from, Date to) {
		
		if (hist1 == null || hist2 == null)
			throw new IllegalArgumentException("Both historizations must be non-null");
		if (hist2.getValidto() == null || hist1.getValidfrom() == null)
			throw new IllegalArgumentException("Both hist1 valid from date and hist2 valid to date must be non-null");
		
		return !from.before(hist1.getValidfrom()) && 
				(!hist2.getValidto().before(to) || 
						DateTools.isLastMomentOfDay(hist2.getValidto()) &&
						DateTools.isLastMomentOfDay(to) &&
						DateTools.isOnSameDay(hist2.getValidto(), to));
	}
	
	/** @return historization in new List */
	public static List<Historization> getHistorizationList(Collection<? extends Historization> historization) {
		return new ArrayList<Historization>(historization);
	}

	/** @return historization list sorted according to validFrom values */
	public static List<Historization> getSortedHistorizationList(Collection<? extends Historization> historization) {
		List<Historization> result = getHistorizationList(historization);
		HistorizationComparator comparator = new HistorizationComparator();
		Collections.sort(result, comparator);
		return result;
	}
	
	/** Generics version of getHistorizationList 
	 * @return historization in new List */
	public static <LINK extends Historization> List<LINK> getLinkList(Collection<LINK> links) {
		return new ArrayList<LINK>(links);
	}
	
	/** Generics version of getSortedHistorizationList
	 *  @return historization list sorted according to validFrom values, oldest at start. */
	public static <LINK extends Historization> List<LINK> getSortedLinkList(Collection<LINK> links) {
		List<LINK> result = getLinkList(links);
		HistorizationComparator comparator = new HistorizationComparator();
		Collections.sort(result, comparator);
		return result;
	}
	
	/** @return ordered validity intervals of consecutive historizations */
	public static List<Interval> getTimelineIntervals(Collection<? extends Historization> historizations) {
		
		if (historizations == null)
			throw new IllegalArgumentException("Historizations collection must be non-null");
		
		List<Historization> historizationList = getSortedHistorizationList(historizations);

		List<Interval> validities = new ArrayList<Interval>();
		for (Historization historization : historizationList)
			validities.add(historization.getValidity());
			
		return validities;
	}
	
	
	/**
	 * Calculates useful dates with HistorizationHelper and returns dates as interval.
	 * Checks that one of the entities is persisted.
	 * If assignee or assigned is not historized it is treated as if historized from now to infinity.
	 * null from, validFrom is treated as now see {@link at.workflow.webdesk.tools.date.HistorizationHelper#generateUsefulValidFrom(Date)}
	 * null to, validTo is treated as Infinity see {@link at.workflow.webdesk.tools.date.HistorizationHelper#generateUsefulValidTo(Date)}
	 * Dates in the future (beginning from tomorrow) are trimmed to day precision, 
	 * i.e. validFrom is trimmed to dateOnly, validTo to lastMomentOfDay.
	 * 
	 * @param assignee entity to be assigned to, the 'many' object of the ManyToOne timeline relationships
	 * @param assigned entity to be assigned, the 'one' object of the ManyToOne timeline relationships
	 * @param validFrom proposed date for link validity start
	 * @param validTo proposed date for link validity end
	 * @return useful from and to dates for assignment as produced by HistorizationHelper
	 */
	public static Interval prepareAssignment(Object assignee, Object assigned, Date from, Date to) {
		
		return prepareAssignment(assignee, assigned, from, to, false);
	}

	/**
	 * From assignee's validity calculates useful dates with HistorizationHelper and returns dates as interval.
	 * Checks that one of the entities is persisted.
	 * If assignee or assigned is not historized it is treated as if historized from now to infinity.
	 * null from, validFrom is treated as now see {@link at.workflow.webdesk.tools.date.HistorizationHelper#generateUsefulValidFrom(Date)}
	 * null to, validTo is treated as Infinity see {@link at.workflow.webdesk.tools.date.HistorizationHelper#generateUsefulValidTo(Date)}
	 * Dates in the future (beginning from tomorrow) are trimmed to day precision, 
	 * i.e. validFrom is trimmed to dateOnly, validTo to lastMomentOfDay.
	 * 
	 * @param assignee entity to be assigned to, the 'many' object of the ManyToOne timeline relationships
	 * @param assigned entity to be assigned, the 'one' object of the ManyToOne timeline relationships
	 * @return from and to dates for assignment covering the whole validity of the assignee
	 */
	public static Interval prepareAssignment(Object assignee, Object assigned) {
		
		return prepareAssignment(assignee, assigned, null, null, true);
	}
	
		
	/**
	 * Calculates useful dates with HistorizationHelper and returns dates as interval.
	 * Checks that one of the entities is persisted.
	 * Can perform forced first assignment for the whole validity of assignee regardless of validFrom and validTo to start continuous and complete timeline.
	 * If forced first assignment from now to infinity is required the getValidFrom() and getValidTo() methods of assignee must both return null or now and infinity respectively.
	 * If assignee or assigned is not historized it is treated as if historized from now to infinity.
	 * null from, validFrom is treated as now see {@link at.workflow.webdesk.tools.date.HistorizationHelper#generateUsefulValidFrom(Date)}
	 * null to, validTo is treated as Infinity see {@link at.workflow.webdesk.tools.date.HistorizationHelper#generateUsefulValidTo(Date)}
	 * Dates in the future (beginning from tomorrow) are trimmed to day precision, 
	 * i.e. validFrom is trimmed to dateOnly, validTo to lastMomentOfDay.
	 * 
	 * @param assignee entity to be assigned to, the 'many' object of the ManyToOne timeline relationships
	 * @param assignedEntity entity to be assigned, the 'one' object of the ManyToOne timeline relationships
	 * @param validFrom proposed date for link validity start
	 * @param validTo proposed date for link validity end
	 * @param forceComplete shall the assignment last for the whole validity of assignee and ignore from and to dates?
	 * @return useful from and to dates for assignment as produced by HistorizationHelper
	 */
	private static Interval prepareAssignment(Object assignee, Object assigned, Date from, Date to, boolean forceComplete) {
			
		if (assignee == null || assigned == null)
			throw new IllegalArgumentException("Both assignee and assigned entities must be non-null for assignment");

		if (
				(assignee instanceof Historization && StringUtils.isNotBlank(((Historization) assignee).getUID()) ||
					assignee instanceof PersistentObject && StringUtils.isNotBlank(((PersistentObject) assignee).getUID())) == false
			&&
				(assigned instanceof Historization && StringUtils.isNotBlank(((Historization) assigned).getUID()) ||
					assigned instanceof PersistentObject && StringUtils.isNotBlank(((PersistentObject) assigned).getUID())) == false 
			)
			throw new IllegalArgumentException("At least one of the assignee and assigned entities must be persisted.");
		// if both entities are not persisted it would be impossible to persist the assignment because when saving one entity and by cascading the link 
		// the link would not be saved because the other entity it references is not persisted 
		
		// simulate persisted values if objects are not persisted (e.g. null, null -> now, infinity)
		consolidateValidityDates(assignee);
		consolidateValidityDates(assigned);
		
		Date usefulFrom = null;
		Date usefulTo = null;

		if (forceComplete) {
			// the assignment must be for the whole validity of assignee
			// these must be computed before useful times because of now()
			
			if (assignee instanceof Historization) {
				Historization myHistorization = (Historization) assignee;
				usefulFrom = myHistorization.getValidfrom();
				usefulTo = myHistorization.getValidto();
			} else { // valid without temporal restriction
				usefulFrom = DateTools.now(); 
				usefulTo = DateTools.INFINITY; 
			}
		} else {
			// consolidate validfrom and validto to get day-exact dates
			usefulFrom = HistorizationHelper.generateUsefulValidFrom(from);
			usefulTo = HistorizationHelper.generateUsefulValidTo(to);
		}
		
		return new DateInterval(usefulFrom, usefulTo);
	}
	
	
	/**
	 * Checks that both the assignee and assigned are valid for the whole time of the assignment
	 * @param assignee entity to be assigned to, the 'many' object of the ManyToOne timeline relationships
	 * @param assigned entity to be assigned, the 'one' object of the ManyToOne timeline relationships
	 * @param validity proposed validity for link 
	 * @throw runtime exception if one or both entities objects are not valid through the whole from and to time interval
	 */
	public static void checkValidity(Object assignee, Object assigned, Interval validity) {

		checkValidity(assignee, assigned, validity.getFrom(), validity.getTo());
	}
	

	/**
	 * Checks that both the assignee and assigned are valid for the whole time of the assignment
	 * @param assignee entity to be assigned to, the 'many' object of the ManyToOne timeline relationships
	 * @param assigned entity to be assigned, the 'one' object of the ManyToOne timeline relationships
	 * @param from proposed date for link validity start
	 * @param to proposed date for link validity end
	 * @throw runtime exception if one or both entities objects are not valid through the whole from and to time interval
	 */
	public static void checkValidity(Object assignee, Object assigned, Date from, Date to) {
		
		if (assignee == null || assigned == null)
			throw new IllegalArgumentException("Both assignee and assigned entities must be non-null for assignment");

		if (assignee instanceof Historization)
			HistorizationTimelineUtils.checkValidityDates((Historization) assignee, from, to, assigned);
		
		if (assigned instanceof Historization) 
			HistorizationTimelineUtils.checkValidityDates((Historization) assigned, from, to, assignee);
	}

	
	/** consolidates validfrom and validto dates of unpersisted entitiy to simulate persisted values */
	private static void consolidateValidityDates(Object entity) {
		
		if (entity instanceof Historization && StringUtils.isBlank(((Historization) entity).getUID())) {
			
			Historization myHistorization = (Historization) entity;
			myHistorization.setValidfrom(HistorizationHelper.generateUsefulValidFrom(myHistorization.getValidfrom()));
			myHistorization.setValidto(HistorizationHelper.generateUsefulValidTo(myHistorization.getValidto()));
		}
	}

	/**
	 * Filters out assignment valid (at least partially) in the range from - to;
	 * @param assignment
	 * @return assignment valid (at least partially) in the range from - to
	 */
	public static <ASSIGNMENT> List<ASSIGNMENT> filterAssignments(Collection<ASSIGNMENT> assignments, Date from, Date to) {
		
		List<ASSIGNMENT> result = new ArrayList<ASSIGNMENT>();
		for (ASSIGNMENT assignment : assignments)
			if (isValid((Historization) assignment, from, to))
				result.add(assignment);
			
		return result;
	}
	
	/**
	 * Checks (at least partial)  validity inside the interval, which is:
	 * <li>
	 * validfrom is inside or on the boundary of interval
	 * </li>
	 * <li>
	 * validto is inside or on the boundary of interval
	 * </li>
	 * <li>
	 * validfrom is before dFrom and valid to is after dTo
	 * </li>
	 * @return
	 */
	public static boolean isValid(Historization assignment, Date dFrom, Date dTo) {
		long from = assignment.getValidfrom().getTime();
		long to = assignment.getValidto().getTime();
		
		return from >= dFrom.getTime() && from < dTo.getTime() || // from is inside interval
			to > dFrom.getTime() && to <= dTo.getTime() || // to is inside interval
			from < dFrom.getTime() && to > dTo.getTime();
	}
	
	/**
	 * Checks that the linked objects are valid for the whole validity of the link.
	 * @param links
	 * @param getAssignee Method to extract assignee from link
	 * @param getAssigned Method to extract assigned from link
	 * @return list of links where assignee and/or assigned is not valid for the whole validity of the link
	 * @throws Exception if there are invocation problems with getAssignee or getAssigned methods
	 */
	public static List<Historization> getAssignmentsWithUncorrelatedValidity(
			Collection<? extends Historization> links, Method getAssignee, Method getAssigned) throws Exception {
	
		if (getAssignee == null)
			throw new IllegalArgumentException("getAssignee must be not null");
		
		if (getAssigned == null)
			throw new IllegalArgumentException("getAssigned must be not null");
		
		List<Historization> result = new ArrayList<Historization>();
		
		if (CollectionUtils.isEmpty(links))
			return result;
		
		for (Historization link : links) {
		
			Object assignee = getAssignee.invoke(link);
			Object assigned = getAssigned.invoke(link);
			try {
				checkValidity(assignee, assigned, link.getValidfrom(), link.getValidto());
			} catch (Exception e) {
				result.add(link);
			}
		}
		return result;
	}
}
