package at.workflow.webdesk.po.timeline;

import java.util.List;

import at.workflow.webdesk.tools.api.Historization;

/**
 * This interface serves for injection of implementing
 * verifier into link processing to provide unspecified verification.
 * 
 * Example is verification of timeline according to some criteria
 * and throwing Exception if it fails.
 * 
 * @author sdzuban 02.09.2013
 */
public interface TimelineVerifier<LINK extends Historization> {
	
	/** performs verification of links sorted as timeline */
	void verify(List<LINK> links);

}
