package at.workflow.webdesk.po;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import at.workflow.webdesk.tools.api.Historization;
import at.workflow.webdesk.tools.date.Interval;

/**
 * Repairs gaps and overlaps in timeline by:
 * <ul><li>deleted link:moving validto of link before gap to just before validfrom of link after gap</li>
 * <li>changed validfrom: moving neighboring validto to just before the changed validfrom</li>
 * <li>changed validto: moving neighboring validfrom to just after the changed validto</li>
 * <li>new link is handled as link with changed validfrom and validto dates</li>
 * </ul>
 * 
 * @author sdzuban 17.05.2013
 */
public interface HistorizationTimelineRepairer {

	/**
	 * Repairs overlaps before timelineBegin and gaps and overlaps after timelineBegin and sets correct validity. 
	 * Is for relationships of kind "at any time maximum  one until timelineBegin, afterwards at any time exactly one".
	 * @param fromDB specimen of links read out from database
	 * @param links links to be repaired. The repair is in place meaning the passed List of links is modified 
	 * (unnecessary ones are deleted, new necessary ones are added, wrong ones are modified!)
	 * @param validity to this validity the timeline will be expanded or trimmed
	 * @param timelineBegin from which date the "maximum one" relationship turns to "exactly one"
	 * @return removed redundant links. These shall be physically deleted and not historicized. 
	 * @throws runtime exception when validfrom >= validto (of the same link)
	 */
	Set<? extends Historization> repairOverlapsAndTimeline(List<? extends Historization> fromDB, Collection<? extends Historization> links, Interval validity, Date timelineBegin);
	
	/**
	 * Repairs gaps and overlaps and sets correct validity. 
	 * Is for relationships of kind "at any time exactly one".
	 * @param fromDB specimen of links read out from database
	 * @param links links to be repaired. The repair is in place meaning the passed List of links is modified (unnecessary ones are deleted,
	 * new necessary ones are added, wrong ones are modified!)
	 * @param validity to this validity the timeline will be expanded or trimmed
	 * @return removed redundant links. These shall be physically deleted and not historicized. 
	 * @throws runtime exception when validfrom >= validto (of the same link)
	 */
	Set<? extends Historization> repairTimeline(List<? extends Historization> fromDB, Collection<? extends Historization> links, Interval validity);
	
	/**
	 * Sets the validfrom of the first timeline link.
	 * If beginning of timeline is same as date it is left untouched.
	 * If beginning of the timeline is after the date it is stretched to date.
	 * If beginning of the timeline is before date all of links before the date are discarded
	 * and timeline beginning is set to date.
	 * @param date intended beginning date, e.g. validFrom of one linked entity
	 * @links timeline
	 * @return discarded redundant links. These shall be physically deleted and not historicized. 
	 */
	Set<? extends Historization>  setBegin(Collection<? extends Historization> links, Date date);
	
	/**
	 * sets the validto of the last timeline link
	 * If end of timeline is same as date it is left untouched.
	 * If end of the timeline is before the date it is stretched to date.
	 * If end of the timeline is after date all of links after the date are discarded
	 * and timeline end is set to date.
	 * @param date intended end date, e.g. validTo of one linked entity
	 * @links timeline
	 * @return discarded redundant links. These shall be physically deleted and not historicized. 
	 */
	Set<? extends Historization>  setEnd(Collection<? extends Historization> links, Date date);

	/**
	 * Repairs only overlaps. Is for relationships of kind "at any time max 1"
	 * @param fromDB specimen of links read out from database
	 * @param links links to be repaired. The repair is in place meaning the passed List of links is modified (unnecessary ones are deleted,
	 * new necessary ones are added, wrong ones are modified!)
	 * @throws runtime exception when validfrom >= validto (of the same link)
	 * @return removed redundant links. These shall be physically deleted and not historicized. 
	 */
	Set<? extends Historization>  repairOverlaps(List<? extends Historization> fromDB, Collection<? extends Historization> links);

	/**
	 * Repairs only overlaps and validity . Is for relationships of kind "at any time max 1"
	 * @param fromDB specimen of links read out from database
	 * @param links links to be repaired. The repair is in place. 
	 * After the repair there are no overlaps. Gaps are not changed.
	 * @param validity to this validity the timeline will be trimmed
	 * @throws runtime exception when validfrom >= validto (of the same link)
	 * @return removed redundant links. These shall be physically deleted and not historicized. 
	 */
	Set<? extends Historization>  repairOverlaps(List<? extends Historization> fromDB, Collection<? extends Historization> links, Interval validity);
	
}
