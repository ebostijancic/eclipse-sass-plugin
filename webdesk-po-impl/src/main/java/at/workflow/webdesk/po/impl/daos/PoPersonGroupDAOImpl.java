package at.workflow.webdesk.po.impl.daos;

import at.workflow.webdesk.po.model.PoPersonGroup;
import at.workflow.webdesk.tools.HistoricizingDAOImpl;

/**
 * PoPersonGroup DAO as needed for Timeline Repairer.
 * 
 * For all other purposes use PoPersonDAO.
 * 
 * @author sdzuban
 */
public class PoPersonGroupDAOImpl extends HistoricizingDAOImpl<PoPersonGroup> {

	@Override
	protected Class<PoPersonGroup> getEntityClass() {
		return PoPersonGroup.class;
	}
}

