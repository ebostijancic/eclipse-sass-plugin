package at.workflow.webdesk.po.impl.daos;

import at.workflow.webdesk.po.model.PoParentGroup;
import at.workflow.webdesk.tools.HistoricizingDAOImpl;

/**
 * PoPersonGroup DAO as needed for Timeline Repairer.
 * 
 * For all other purposes use PoPersonDAO.
 * 
 * @author sdzuban
 */
public class PoParentGroupDAOImpl extends HistoricizingDAOImpl<PoParentGroup> {

	@Override
	protected Class<PoParentGroup> getEntityClass() {
		return PoParentGroup.class;
	}
}

