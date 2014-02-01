package at.workflow.webdesk.po.impl.daos;

import java.util.List;

import at.workflow.webdesk.po.daos.PoActionParameterDAO;
import at.workflow.webdesk.po.model.PoAction;
import at.workflow.webdesk.po.model.PoActionParameter;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

public class PoActionParameterDAOImpl extends GenericHibernateDAOImpl<PoActionParameter> implements PoActionParameterDAO{

	@Override
	@SuppressWarnings("unchecked")
	public List<PoActionParameter> getActionParameters(PoAction action) {
		
		Object[] keyValues = { action };
		return getHibernateTemplate().find(
				"from PoActionParameter where action = ? ",
				keyValues);
	}
	
	
	@Override
	protected Class<PoActionParameter> getEntityClass() {
		return PoActionParameter.class;
	}
	

}
