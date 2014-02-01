package at.workflow.tools.mail;

/**
 * Sends mails created using class <code>Mail</code>.
 * Retrieve this service from dependency-injection container (Spring) by "PoMailService".
 */
public interface MailService {

	/**
	 * Sends passed mail.
	 * Following fields <b>must</b> have been assigned to given Mail (mandatory, must not be null):
	 * <ul>
	 * 	<li>to (receiver mail address)</li>
	 * 	<li>subject</li>
	 * 	<li>message (mail body text)</li>
	 * </ul>
	 * All other fields can be null (are not mandatory).
	 */
	void sendMail(Mail mail);

	/** Sends mail using given configuration. For required Mail fields see JavaDoc above. */
	void sendMail(Mail mail, MailServerConfiguration configuration);

	/** Sets another configuration for the MailService.  */
	void setConfiguration(MailServerConfiguration configuration);

}
