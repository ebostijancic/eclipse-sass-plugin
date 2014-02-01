package at.workflow.webdesk.po.model.comparators;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


import at.workflow.webdesk.po.model.PoAPermissionBase;
import at.workflow.webdesk.po.model.PoAPermissionClient;
import at.workflow.webdesk.po.model.PoAPermissionGroup;
import at.workflow.webdesk.po.model.PoAPermissionPerson;
import at.workflow.webdesk.po.model.PoAPermissionRole;
import at.workflow.webdesk.po.model.PoRole;
import at.workflow.webdesk.po.model.PoRoleCompetenceBase;
import at.workflow.webdesk.po.model.PoRoleHolderGroup;
import at.workflow.webdesk.po.model.PoRoleHolderLink;
import at.workflow.webdesk.po.model.PoRoleHolderPerson;

/**
 * @author hentner
 *
 * Use this <code>Comparator</code> in order to sort 
 * <code>PoAPermissionBase</code> objects. The owner's name 
 * of the concrete Implementation is used to determine
 * the result of the comparison.  
 * <p>
 * Collection.sort(List l, Comparator c)
 */
public class PoActionPermissionNameComparator implements Comparator<PoAPermissionBase> {

	@Override
	public int compare(PoAPermissionBase ap1, PoAPermissionBase ap2) {
		String name1 = getName(ap1);
		String name2 = getName(ap2);
		return name1.compareToIgnoreCase(name2);
	}

	private String getName(PoAPermissionBase apb) {
		if (apb instanceof PoAPermissionClient)
			return ((PoAPermissionClient)apb).getClient().getName();
		
		if (apb instanceof PoAPermissionGroup)
			return ((PoAPermissionGroup)apb).getGroup().getShortName();
		
		if (apb instanceof PoAPermissionPerson)
			return ((PoAPermissionPerson)apb).getPerson().getFullName();
		
		if (apb instanceof PoAPermissionRole) {
			// there could be two cases
			//		-> the role is the roleholder
			// 		-> the role is a dummy-role, then we need to determine the roleHolder
			PoRole role=((PoAPermissionRole)apb).getRole();
			if (role.getRoleType()==PoRole.NORMAL_ROLE) 
				return ((PoAPermissionRole)apb).getRole().getName();
			if (role.getRoleHolders().size()>0) {
				PoRoleCompetenceBase rcb = (PoRoleCompetenceBase) role.getRoleHolders().toArray()[0];
				List<PoRoleHolderLink> roleHolders= rcb.getRoleHolders();
				if (roleHolders!=null) {
					for (Iterator<PoRoleHolderLink> i = roleHolders.iterator(); i.hasNext();) {
						PoRoleHolderLink o = i.next();
						if (o instanceof PoRoleHolderPerson) 
							return ((PoRoleHolderPerson) o).getPerson().getFullName();
						
						if (o instanceof PoRoleHolderGroup) 
							return ((PoRoleHolderGroup) o).getGroup().getShortName();
						
						// there is no way to calculate the dynamic owner, when there is no executor  
					}
				} else	
					return "";
				
			}
		}
		return "";
	}

}
