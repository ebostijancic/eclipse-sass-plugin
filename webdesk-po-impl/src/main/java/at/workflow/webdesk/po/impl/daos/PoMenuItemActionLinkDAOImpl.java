package at.workflow.webdesk.po.impl.daos;

import java.util.ArrayList;
import java.util.List;

import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoMenuItemActionLink;
import at.workflow.webdesk.po.model.PoMenuTreeBase;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

public class PoMenuItemActionLinkDAOImpl extends GenericHibernateDAOImpl<PoMenuItemActionLink> {

	@Override
	protected Class<PoMenuItemActionLink> getEntityClass() {
		return PoMenuItemActionLink.class;
	}
	
	public List<PoMenuItemActionLink> findMenuItemsForAction(PoAction action) {
		return find( new Filter("action", action) );
	}
	
	public PoMenuItemActionLink findActionLinkInTree(PoMenuTreeBase tree, PoAction action) {
		List<PoMenuItemActionLink> actionLinks = new ArrayList<PoMenuItemActionLink>(); 
		actionLinks.addAll(find(new Filter[] {new Filter("menuTree", tree), new Filter("action", action)}));
		if(actionLinks.isEmpty()) {
			return null;
		}
		return actionLinks.get(0);
	}
}
