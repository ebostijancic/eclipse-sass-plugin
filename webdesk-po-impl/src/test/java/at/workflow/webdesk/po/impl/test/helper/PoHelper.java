package at.workflow.webdesk.po.impl.test.helper;

import java.util.Date;
import java.util.List;

import at.workflow.webdesk.po.PoRoleService;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleHolderGroup;
import at.workflow.webdesk.po.model.PoRoleHolderLink;
import at.workflow.webdesk.po.model.PoRoleHolderPerson;

public abstract class PoHelper {

	protected PoRole role;
	
	protected PoRoleService roleService;
	
	protected int ranking;
	
	public abstract void doAssignment(Date validFrom, Date validTo);
	
	public abstract List<? extends PoRoleHolderLink> findRoleHolder(Date date);

	public Date getValidFrom(List<? extends PoRoleHolderLink> l, int i) {
		Object o = l.get(i);
		if (o instanceof PoRoleHolderPerson) {
			PoRoleHolderPerson rhp = (PoRoleHolderPerson) o;
			return rhp.getValidfrom();
		} else if (o instanceof PoRoleHolderGroup) {
			PoRoleHolderGroup rhg = (PoRoleHolderGroup) o;
			return rhg.getValidfrom();
		}
		return null;
	}

	public Date getValidTo(List<? extends PoRoleHolderLink> l, int i) {
		Object o = l.get(i);
		if (o instanceof PoRoleHolderPerson) {
			PoRoleHolderPerson rhp = (PoRoleHolderPerson) o;
			return rhp.getValidto();
		} else if (o instanceof PoRoleHolderGroup) {
			PoRoleHolderGroup rhg = (PoRoleHolderGroup) o;
			return rhg.getValidto();
		}
		return null;
	}

	public void removeRoleHolderFromRole(List<? extends PoRoleHolderLink> l, int i) {
		PoRoleHolderLink o = l.get(i);
		if (o instanceof PoRoleHolderPerson) {
			PoRoleHolderPerson rhp = (PoRoleHolderPerson) o;
			this.roleService.removePersonFromRole(rhp);
		} else if (o instanceof PoRoleHolderGroup) {
			PoRoleHolderGroup rhg = (PoRoleHolderGroup) o;
			this.roleService.removeGroupFromRole(rhg);
		}
	}
	
	
	
}
