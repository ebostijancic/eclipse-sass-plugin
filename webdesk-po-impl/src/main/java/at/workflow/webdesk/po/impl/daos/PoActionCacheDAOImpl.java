package at.workflow.webdesk.po.impl.daos;

import org.springframework.dao.support.DataAccessUtils;

import at.workflow.webdesk.po.daos.PoActionCacheDAO;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoActionCache;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

/**
 *<p>Hibernate Implementation of the <code>PoActionCacheDAO</code> interface.</p>
 * @author DI Harald Entner <br>
 * created at:       07.11.2007<br>
 */
public class PoActionCacheDAOImpl extends GenericHibernateDAOImpl<PoActionCache> implements PoActionCacheDAO {

	@Override
	protected Class<PoActionCache> getEntityClass() {
		return PoActionCache.class;
	}
	
	@Override
	public PoActionCache findActionCache(PoAction action, PoPerson person) {
		if (person != null) {
			Object keys[] = { action, person };
			return (PoActionCache) DataAccessUtils.uniqueResult(getHibernateTemplate().find("from PoActionCache where action=? and person=?", keys));
		}
		Object keys[] = { action };
		return (PoActionCache) DataAccessUtils.uniqueResult(getHibernateTemplate().find("from PoActionCache where action=? and person is null", keys));
	}

	@Override
	protected void beforeSave(PoActionCache actionCache)	{
		PoActionCache ac = findActionCache(actionCache.getAction(), actionCache.getPerson());
		if (ac != null)	{
			getHibernateTemplate().evict(ac);
			actionCache.setUID(ac.getUID());
		}
	}
	
}
