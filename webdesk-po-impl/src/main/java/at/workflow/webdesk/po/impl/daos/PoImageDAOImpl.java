package at.workflow.webdesk.po.impl.daos;

import java.util.List;

import at.workflow.webdesk.po.model.PoImage;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

/**
 * @author sdzuban 11.02.2013
 */
public class PoImageDAOImpl extends GenericHibernateDAOImpl<PoImage> {

	/** {@inheritDoc} */
	@Override
	protected Class<PoImage> getEntityClass() {
		return PoImage.class;
	}

	/**
	 * @param fileName
	 * @return
	 */
	public List<PoImage> findImageByFileName(String fileName) {
		final String query = "from PoImage where filename = ?";
		return getHibernateTemplate().find(query, fileName);
	}

}
