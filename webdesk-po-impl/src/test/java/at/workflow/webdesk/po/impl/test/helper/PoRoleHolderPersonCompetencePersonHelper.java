package at.workflow.webdesk.po.impl.test.helper;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.po.PoRoleService;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleHolderLink;

public class PoRoleHolderPersonCompetencePersonHelper extends PoHelper {
	private int ranking;
	private PoPerson target;
	private PoPerson officeHolder;

	public PoRoleHolderPersonCompetencePersonHelper(PoRoleService poRoleService, PoRole role, PoPerson officeHolder, PoPerson target, int i) {
		this.roleService= poRoleService;
		this.role = role;
		this.officeHolder = officeHolder;
		this.target = target;
		this.ranking = i;
	}

	public void doAssignment(Date validFrom, Date validTo) {
		this.roleService.assignRoleWithPersonCompetence(role, officeHolder, target, validFrom, validTo, ranking);

	}

	public List<? extends PoRoleHolderLink> findRoleHolder(Date date) {
		return this.roleService.findRoleHolderPersonWithCompetence4PersonF(role, officeHolder, target, date);
	}

}
