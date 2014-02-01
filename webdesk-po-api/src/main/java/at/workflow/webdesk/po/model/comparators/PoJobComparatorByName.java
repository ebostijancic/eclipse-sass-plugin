package at.workflow.webdesk.po.model.comparators;

import java.util.Comparator;

import at.workflow.webdesk.po.model.PoJob;

public class PoJobComparatorByName implements Comparator<PoJob> {

	@Override
	public int compare(PoJob job1, PoJob job2) {
		
		return job1.getName().toUpperCase().compareTo(job2.getName().toUpperCase());
	}

}
