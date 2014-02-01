package at.workflow.webdesk.po.impl.test.helper;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.po.PoRoleService;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleHolderLink;

public class PoRoleHolderGroupCompetenceAllHelper extends PoHelper {

	private PoGroup officeHolder;
	private int ranking;

	public PoRoleHolderGroupCompetenceAllHelper(PoRoleService poRoleService, PoRole role, PoGroup officeHolder, int i) {
		this.roleService = poRoleService;
		this.role = role;
		this.officeHolder = officeHolder;
		this.ranking = i;
	}

	public void doAssignment(Date validFrom, Date validTo) {
		this.roleService.assignRole(role, officeHolder, validFrom, validTo,ranking);

	}

	public List<? extends PoRoleHolderLink> findRoleHolder(Date date) {
		return this.roleService.findRoleHolderGroupWithCompetence4AllF(role, officeHolder, date);
	}

}
