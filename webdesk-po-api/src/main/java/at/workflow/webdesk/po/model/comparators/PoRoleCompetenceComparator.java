package at.workflow.webdesk.po.model.comparators;

import java.util.Comparator;

import at.workflow.webdesk.po.model.PoRoleCompetenceBase;

public class PoRoleCompetenceComparator implements Comparator<PoRoleCompetenceBase> {

	@Override
	public int compare(PoRoleCompetenceBase o1, PoRoleCompetenceBase o2) {
		return o1.getName().compareTo(o2.getName());
	}

}
