package at.workflow.webdesk.po.impl.daos;

import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.UUIDHexGenerator;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import at.workflow.webdesk.po.daos.PoQueryUtils;

public class PoQueryUtilsImpl extends HibernateDaoSupport implements PoQueryUtils {

	@Override
	public void evictObject(Object obj) {
		getHibernateTemplate().evict(obj);
	}

	@Override
	public void refreshObject(Object obj) {
		getHibernateTemplate().refresh(obj);
	}

	@Override
	public String generateUID() {
		UUIDHexGenerator myGen = new UUIDHexGenerator();
		return (String) myGen.generate((SessionImplementor) getHibernateTemplate().getSessionFactory().getCurrentSession(),null);
	}
}
