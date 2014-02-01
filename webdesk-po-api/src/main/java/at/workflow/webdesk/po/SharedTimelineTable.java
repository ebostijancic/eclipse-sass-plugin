package at.workflow.webdesk.po;

import java.util.List;

import at.workflow.webdesk.po.timeline.TimelineFragment;
import at.workflow.webdesk.tools.api.PersistentObject;
import at.workflow.webdesk.tools.date.EditableDateInterval;

/**
 * This interface defines parameterization of EditableTable interface
 * as used for handling of timelines with shares.
 * {@link at.workflow.webdesk.po.EditableTable}
 *
 * @author sdzuban 13.04.2013
 *
 * @param <ASSIGNED> the assigned entity, typically cost center
 * @param <SHARE> share of the assigned entity on the sum of all 
 * assigned entities, typically in per cent
 */
public interface SharedTimelineTable<ASSIGNED extends PersistentObject, SHARE extends Number>
		extends EditableTable<EditableDateInterval, ASSIGNED, SHARE> {

	<ASSIGNMENT extends HistorizationWithShare> void 
		fillTableFromFragments(List<TimelineFragment<ASSIGNED, ASSIGNMENT>> fragments);
	
}


