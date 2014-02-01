package at.workflow.webdesk.po.impl.daos;

import at.workflow.webdesk.po.model.PoMenuTreeTemplate;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

public class PoMenuTreeTemplateDAOImpl extends GenericHibernateDAOImpl<PoMenuTreeTemplate> {

	@Override
	protected Class<PoMenuTreeTemplate> getEntityClass() {
		return PoMenuTreeTemplate.class;
	}
	
}
