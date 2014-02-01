package at.workflow.webdesk.po.impl.daos;

import java.util.List;

import org.springframework.dao.support.DataAccessUtils;

import at.workflow.webdesk.po.daos.PoClientDAO;
import at.workflow.webdesk.po.model.PoClient;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

/**
 * This class is the <b>Hibernate</b> implementation of the <code>PoClientDAO</code> interface.
 * 
 * @author ggruber
 * @author hentner
 */
public class PoClientDAOImpl extends GenericHibernateDAOImpl<PoClient> 
									implements PoClientDAO  {

	@Override
	public PoClient findClientByName(String name) {
	    Object[] keys = {name};
		return (PoClient) DataAccessUtils.uniqueResult(getHibernateTemplate().find("from PoClient c"                
                + " where c.name=?", keys));
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public boolean isClientExistent(String name) {
		Object[] keyValues={name};
		List<Long> l = getHibernateTemplate().find("select count(*) from PoClient where name=?",keyValues);
		int i = ( l.get(0)).intValue();
		return (i>0);
	}

	@Override
	protected Class<PoClient> getEntityClass() {
		return PoClient.class;
	}


	/** {@inheritDoc} */
	@Override
	public PoClient findClientByGroupShortNamePrefix(String prefix) {
		Object[] keyValues={prefix};
		return (PoClient) DataAccessUtils.uniqueResult(getHibernateTemplate().find("from PoClient where groupShortNamePrefix=?",keyValues));
	}


	/** {@inheritDoc} */
	@Override
	public PoClient findClientByPersonUserNamePrefix(String prefix) {
		Object[] keyValues={prefix};
		return (PoClient) DataAccessUtils.uniqueResult(getHibernateTemplate().find("from PoClient where personUserNamePrefix=?",keyValues));
	}


	/** {@inheritDoc} */
	@Override
	public PoClient findClientByPersonEmployeeIdPrefix(String prefix) {
		Object[] keyValues={prefix};
		return (PoClient) DataAccessUtils.uniqueResult(getHibernateTemplate().find("from PoClient where personEmployeeIdPrefix=?",keyValues));
	}

}
