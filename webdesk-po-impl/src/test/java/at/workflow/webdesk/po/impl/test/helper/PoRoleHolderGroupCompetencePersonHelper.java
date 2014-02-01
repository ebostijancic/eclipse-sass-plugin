package at.workflow.webdesk.po.impl.test.helper;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.po.PoRoleService;
import at.workflow.webdesk.po.model.PoGroup;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleHolderLink;

public class PoRoleHolderGroupCompetencePersonHelper extends PoHelper {

	private PoPerson target;
	private PoGroup officeHolder;

	public PoRoleHolderGroupCompetencePersonHelper(PoRoleService poRoleService, PoRole role, PoGroup tlGroup, PoPerson tlPerson, int i) {
		this.ranking = i;
		this.role = role;
		this.roleService = poRoleService;
		this.officeHolder = tlGroup;
		this.target = tlPerson;
	}

	public void doAssignment(Date validFrom, Date validTo) {
		this.roleService.assignRoleWithPersonCompetence(role, officeHolder, target, validFrom, validTo, ranking);
	}

	public List<? extends PoRoleHolderLink> findRoleHolder(Date date) {
		return this.roleService.findRoleHolderGroupWithCompetence4PersonF(role, officeHolder, target, date);
	}

}
