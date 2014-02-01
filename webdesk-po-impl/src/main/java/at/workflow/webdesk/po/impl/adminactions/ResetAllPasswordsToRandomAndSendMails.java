package at.workflow.webdesk.po.impl.adminactions;

import at.workflow.webdesk.po.model.PoPerson;

/**
 * An admin action that lets reset all passwords to random passwords and
 * sends mails to the affected users containing that password.
 * 
 * @author fritzberger 04.11.2010
 */
public class ResetAllPasswordsToRandomAndSendMails extends ResetAllPasswords {
	

	/** Overridden to execute the <code>resetToRandomPasswordAndSendMail</code> service method. */
	@Override
	protected void resetPassword(PoPerson person) {
		passwordService.resetToRandomPasswordAndSendMail(person);
	}
	
}
