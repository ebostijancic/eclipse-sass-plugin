package at.workflow.webdesk.po.impl.daos;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import at.workflow.webdesk.po.daos.PoKeyValueTypeDAO;
import at.workflow.webdesk.po.model.PoKeyValue;
import at.workflow.webdesk.po.model.PoKeyValueType;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

public class PoKeyValueTypeDAOImpl extends GenericHibernateDAOImpl<PoKeyValueType> implements PoKeyValueTypeDAO {
	
	private static final String FILTER_PO_FROM_DATE2_INFINITE = "po_fromDate2Infinite";

	private void disableFilter(String filterName) {
		Session session = SessionFactoryUtils.getSession(getSessionFactory(), false);
		if(session.getEnabledFilter(filterName)!=null) {
			session.disableFilter(filterName);
		}
	}

	public void deleteAndFlushKeyValueType(PoKeyValueType keyValueType) {
		keyValueType.setValidto(new java.util.Date());
		save(keyValueType);
	}

	@Override
	public PoKeyValueType getKeyValueTypeF(String uid, Date referenceDate) {
		
		getHibernateTemplate().enableFilter( FILTER_PO_FROM_DATE2_INFINITE ).setParameter("filterFromDate", referenceDate);
		PoKeyValueType ret = getHibernateTemplate().get(PoKeyValueType.class, uid);
		disableFilter(FILTER_PO_FROM_DATE2_INFINITE);
		
		return ret;
	}

	@Override
	protected void beforeSave(PoKeyValueType keyValueType)	{
		if (keyValueType.getValidfrom() == null)
			keyValueType.setValidfrom(new Date());
		if (keyValueType.getValidto() == null)
			keyValueType.setValidto(new Date(DateTools.INFINITY_TIMEMILLIS));
	}
	
	@Override
	public PoKeyValueType findKeyValueTypeByName(String keyValueTypeName) {
        Object[] keyValues = { keyValueTypeName };
		
		@SuppressWarnings("unchecked")
		PoKeyValueType myKeyValueType = DataAccessUtils.uniqueResult(
					(List<PoKeyValueType>)(
					getHibernateTemplate()
					.find("from PoKeyValueType where name=?", keyValues ))
			);		
		
		
		return myKeyValueType;
	}
	
	@Override
	public PoKeyValue findKeyValueIncludingOld(PoKeyValueType keyValueType, String keyValue) {
        Object[] keyValues = { keyValueType, keyValue };
		@SuppressWarnings("unchecked")
		List<PoKeyValue> l = getHibernateTemplate().find("from PoKeyValue where keyValueType=? and key=? " +
				"and validfrom < current_timestamp() order by validfrom desc", keyValues);
		if (l.size()>0)
			return l.get(0);
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<PoKeyValueType> findKeyValueTypes() {
		return getHibernateTemplate().find("from PoKeyValueType");
	}

	@Override
	protected Class<PoKeyValueType> getEntityClass() {
		return PoKeyValueType.class;
	}


}
