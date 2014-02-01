package at.workflow.webdesk.po.impl.daos;

import at.workflow.webdesk.po.model.PoMenuItemBase;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

public class PoMenuItemBaseDAOImpl extends GenericHibernateDAOImpl<PoMenuItemBase> {

	@Override
	protected Class<PoMenuItemBase> getEntityClass() {
		return PoMenuItemBase.class;
	}	
}
