package at.workflow.webdesk.po.impl.test.helper;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.po.PoRoleService;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleDeputy;
import at.workflow.webdesk.po.model.PoRoleHolderGroup;
import at.workflow.webdesk.po.model.PoRoleHolderLink;
import at.workflow.webdesk.po.model.PoRoleHolderPerson;

public class PoDeputyTestMergeHelper extends PoHelper {

	private PoHelper helperGeneration;
	private PoHelper helperSearch;
	private PoRoleDeputy deputy;
	
	private Date lastSearchDate = null;

	public PoDeputyTestMergeHelper(PoRoleService poRoleService, PoRole role, PoRoleDeputy rd, PoHelper helperGeneration, PoHelper helperSearch) {
		this.roleService = poRoleService;
		this.role = role;
		this.deputy=rd;
		this.helperGeneration = helperGeneration;
		this.helperSearch = helperSearch;
	}

	public void doAssignment(Date validFrom, Date validTo) {
		this.helperGeneration.doAssignment(validFrom, validTo);
	}

	public List<? extends PoRoleHolderLink> findRoleHolder(Date date) {
		this.lastSearchDate = date;
		deputy.setValidfrom(date);
		this.roleService.saveDeputy(deputy);
		return this.helperSearch.findRoleHolder(date);
	}
	
	public void removeRoleHolderFromRole(List<? extends PoRoleHolderLink> l, int i) {
		List<? extends PoRoleHolderLink> genList = helperGeneration.findRoleHolder(lastSearchDate);
		PoRoleHolderLink o = genList.get(i);
		if (o instanceof PoRoleHolderPerson) {
			PoRoleHolderPerson rhp = (PoRoleHolderPerson) o;
			this.roleService.removePersonFromRole(rhp);
		} else if (o instanceof PoRoleHolderGroup) {
			PoRoleHolderGroup rhg = (PoRoleHolderGroup) o;
			this.roleService.removeGroupFromRole(rhg);
		}
	}

}
