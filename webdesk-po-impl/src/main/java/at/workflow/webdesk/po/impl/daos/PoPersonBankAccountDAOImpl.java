package at.workflow.webdesk.po.impl.daos;

import java.util.List;

import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.po.model.PoPersonBankAccount;
import at.workflow.webdesk.tools.HistoricizingDAOImpl;

/**
 * @author sdzuban 21.08.2013
 */
public class PoPersonBankAccountDAOImpl extends HistoricizingDAOImpl<PoPersonBankAccount> {

	/** {@inheritDoc} */
	@Override
	protected Class<PoPersonBankAccount> getEntityClass() {
		return PoPersonBankAccount.class;
	}

	/**
	 * @param person
	 * @return
	 */
	@SuppressWarnings({ "cast", "unchecked" })
	public List<PoPersonBankAccount> findBankAccountsForPerson(PoPerson person) {
		return (List<PoPersonBankAccount>) getHibernateTemplate().find(
				"from PoPersonBankAccount a where a.person = ?", 
				new Object[] {person});
	}

}
