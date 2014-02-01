package at.workflow.webdesk.po.impl.daos;

import java.util.List;

import at.workflow.webdesk.po.model.PoDataSourceDefinition;
import at.workflow.webdesk.tools.hibernate.GenericHibernateDAOImpl;

/**
 * Standard DAO with finder function
 * 
 * @author sdzuban 03.05.2012
 */
public class PoDataSourceDefinitionDAOImpl extends GenericHibernateDAOImpl<PoDataSourceDefinition> {

	/** {@inheritDoc} */
	@Override
	protected Class<PoDataSourceDefinition> getEntityClass() {
		return PoDataSourceDefinition.class;
	}
	
	public PoDataSourceDefinition findDataSourceDefinitionByName(String name) {
		Filter filter = new Filter("name", name);
		final List<PoDataSourceDefinition> definitions = find(filter);
		return definitions != null && !definitions.isEmpty() ? definitions.get(0) : null;
	}
}
