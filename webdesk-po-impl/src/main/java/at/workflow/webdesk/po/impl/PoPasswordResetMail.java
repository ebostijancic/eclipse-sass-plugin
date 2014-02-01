package at.workflow.webdesk.po.impl;

/**
 * Spring bean for configuring the auto-mail that is sent to a user when his password got reset.
 * 
 * @author fritzberger 21.10.2010
 */
public class PoPasswordResetMail {
	
	private String subject;
	private String templateBody;
	private String senderMailAddress;
	private String explicitRecipient;
	
	/**
	 * Spring configuration parameter.
	 * @return the subject line of the mail to send in case of password-reset.
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * Spring configuration parameter.
	 * @param the subject line of the mail to send in case of password-reset.
	 */
	public void setSubject(String mailSubject)	{
		this.subject = mailSubject;
	}
	
	/**
	 * Spring configuration parameter.
	 * @return the (template) text of the mail to send in case of password-reset.
	 */
	public String getTemplateBody() {
		return templateBody;
	}

	/**
	 * Spring configuration parameter.
	 * @param templateText the (template) text of the mail to send in case of password-reset.
	 */
	public void setTemplateBody(String templateText)	{
		this.templateBody = templateText;
	}
	
	/**
	 * Spring configuration parameter, must not be null.
	 * @return the sender address of the mail to send in case of password-reset.
	 */
	public String getSenderMailAddress() {
		return senderMailAddress;
	}

	/**
	 * Spring configuration parameter.
	 * @param senderMailAddress the sender address of the mail to send in case of password-reset.
	 */
	public void setSenderMailAddress(String senderMailAddress)	{
		this.senderMailAddress = senderMailAddress;
	}
	
	/**
	 * Spring configuration parameter.
	 * @return the explicit recipient (overriding person-email) of the mail to send in case of password-reset.
	 */
	public String getExplicitRecipient() {
		return explicitRecipient;
	}

	/**
	 * Spring configuration parameter.
	 * @param explicitRecipient the explicit recipient (overriding person-email) of the mail to send in case of password-reset.
	 */
	public void setExplicitRecipient(String explicitRecipient)	{
		this.explicitRecipient = explicitRecipient;
	}
	
}
