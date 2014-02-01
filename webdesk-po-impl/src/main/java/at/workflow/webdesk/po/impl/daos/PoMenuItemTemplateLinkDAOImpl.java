package at.workflow.webdesk.po.impl.daos;

import java.util.List;
import at.workflow.webdesk.po.model.PoMenuItemTemplateLink;
import at.workflow.webdesk.po.model.PoMenuTreeTemplate;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

public class PoMenuItemTemplateLinkDAOImpl extends GenericHibernateDAOImpl<PoMenuItemTemplateLink> {

	@Override
	protected Class<PoMenuItemTemplateLink> getEntityClass() {
		return PoMenuItemTemplateLink.class;
	}
	
	public List<PoMenuItemTemplateLink>	findAllMenuItemTemplateLinks(PoMenuTreeTemplate template) {
		return find(new Filter("treeTemplate", template));
	}
}
