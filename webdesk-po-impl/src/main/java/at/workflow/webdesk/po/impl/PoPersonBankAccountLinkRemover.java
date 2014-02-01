package at.workflow.webdesk.po.impl;

import at.workflow.webdesk.po.link.LinkRemover;
import at.workflow.webdesk.po.model.PoPersonBankAccount;
import at.workflow.webdesk.tools.api.Historization;

public class PoPersonBankAccountLinkRemover implements LinkRemover {

	@Override
	public void remove(Historization link) {
		((PoPersonBankAccount) link).getPerson().getBankAccounts().remove(link);
	}
}
