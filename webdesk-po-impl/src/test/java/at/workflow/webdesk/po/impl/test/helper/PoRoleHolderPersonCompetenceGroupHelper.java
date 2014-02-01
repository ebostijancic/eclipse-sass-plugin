package at.workflow.webdesk.po.impl.test.helper;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.po.PoRoleService;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleHolderLink;

public class PoRoleHolderPersonCompetenceGroupHelper extends PoHelper {

	private PoPerson officeHolder;
	private PoGroup target;

	public PoRoleHolderPersonCompetenceGroupHelper(PoRoleService poRoleService, PoRole role, PoPerson officeHolder, PoGroup target, int ranking) {
		this.role=role;
		this.roleService=poRoleService;
		this.ranking = ranking;
		this.officeHolder = officeHolder;
		this.target = target;
	}

	public void doAssignment(Date validFrom, Date validTo) {
		this.roleService.assignRoleWithGroupCompetence(role, officeHolder, target, validFrom, validTo, ranking);
	}

	public List<? extends PoRoleHolderLink> findRoleHolder(Date date) {
		return this.roleService.findRoleHolderPersonWithCompetence4GroupF(role, officeHolder, target, date);
	}

}
