package at.workflow.webdesk.po.impl.daos;

import java.util.List;

import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoMenuTreeClient;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

public class PoMenuTreeClientDAOImpl extends GenericHibernateDAOImpl<PoMenuTreeClient> {

	@Override
	protected Class<PoMenuTreeClient> getEntityClass() {
		return PoMenuTreeClient.class;
	}
	
	public PoMenuTreeClient findMenuTreeForClient(PoClient client) {
		List<PoMenuTreeClient> menuTrees = find(new Filter("client", client));
		if(menuTrees.size() > 0) {
			return menuTrees.get(0);
		}
		return null;
	}
}
