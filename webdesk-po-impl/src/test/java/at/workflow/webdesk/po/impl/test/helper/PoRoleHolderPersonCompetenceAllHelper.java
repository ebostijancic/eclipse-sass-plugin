package at.workflow.webdesk.po.impl.test.helper;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.po.PoRoleService;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleHolderLink;

public class PoRoleHolderPersonCompetenceAllHelper  extends PoHelper{
	
	private int ranking;
	private PoPerson officeHolder;

	public PoRoleHolderPersonCompetenceAllHelper(PoRoleService roleService, PoRole role, PoPerson officeHolder, int i) {
		this.roleService = roleService;
		this.role = role;
		this.officeHolder = officeHolder;
		this.ranking = i;
	}

	public void doAssignment(Date validFrom, Date validTo) {
		roleService.assignRole(role, officeHolder, validFrom, validTo, ranking);
	}

	public List<? extends PoRoleHolderLink> findRoleHolder(Date date) {
		return roleService.findRoleHolderPersonWithCompetence4AllF(role, officeHolder, date);
	}
}
