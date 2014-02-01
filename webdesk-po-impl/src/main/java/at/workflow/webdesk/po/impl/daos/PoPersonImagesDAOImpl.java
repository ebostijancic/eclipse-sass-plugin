package at.workflow.webdesk.po.impl.daos;

import at.workflow.webdesk.po.model.PoPersonImages;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

/**
 * @author sdzuban 11.02.2013
 */
public class PoPersonImagesDAOImpl extends GenericHibernateDAOImpl<PoPersonImages> {

	/** {@inheritDoc} */
	@Override
	protected Class<PoPersonImages> getEntityClass() {
		return PoPersonImages.class;
	}

}
