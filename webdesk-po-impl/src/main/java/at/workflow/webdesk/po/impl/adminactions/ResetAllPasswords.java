package at.workflow.webdesk.po.impl.adminactions;

import at.workflow.webdesk.po.PoOrganisationService;
import at.workflow.webdesk.po.PoPasswordService;
import at.workflow.webdesk.po.model.PoPerson;

/**
 * An admin action that lets reset all passwords.
 * 
 * @author fritzberger 04.11.2010
 */
public class ResetAllPasswords extends AbstractAdminAction {
	
	private PoOrganisationService organisationService;
	protected PoPasswordService passwordService;

	@Override
	public void run() {
		for (PoPerson person : organisationService.loadAllPersons())	{
			resetPassword(person);
		}
	}
	
	/** Override to execute another service method than <code>resetPassword</code>. */
	protected void resetPassword(PoPerson person)	{
		passwordService.resetPassword(person);
	}
	
	/** Spring accessor. */
	public void setOrganisationService(PoOrganisationService organisationService) {
		this.organisationService = organisationService;
	}

	/** Spring accessor. */
	public void setPasswordService(PoPasswordService passwordService) {
		this.passwordService = passwordService;
	}

}
