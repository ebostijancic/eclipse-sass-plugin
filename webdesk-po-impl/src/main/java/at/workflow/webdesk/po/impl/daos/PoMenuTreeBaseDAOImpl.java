package at.workflow.webdesk.po.impl.daos;

import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.po.model.PoMenuTreeBase;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

public class PoMenuTreeBaseDAOImpl extends GenericHibernateDAOImpl<PoMenuTreeBase> {

	@Override
	protected Class<PoMenuTreeBase> getEntityClass() {
		return PoMenuTreeBase.class;
	}

	public PoMenuTreeBase findMenuTreeByClient(PoClient client) {
		return find(new Filter("client", client)).get(0);
	}

}
