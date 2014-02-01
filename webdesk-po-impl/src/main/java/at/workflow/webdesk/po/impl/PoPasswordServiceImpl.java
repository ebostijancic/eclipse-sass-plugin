package at.workflow.webdesk.po.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import at.workflow.tools.mail.Mail;
import at.workflow.tools.mail.MailService;
import at.workflow.webdesk.po.PoPasswordService;
import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.PoScriptingService;
import at.workflow.webdesk.po.daos.PoPasswordDAO;
import at.workflow.webdesk.po.model.PoPassword;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.tools.api.MessageConstants;
import at.workflow.webdesk.tools.date.DateTools;
import at.workflow.webdesk.tools.security.CryptUtil;

/**
 * Spring bean that implements the according service.
 * 
 * @author fritzberger 21.10.2010
 */
public class PoPasswordServiceImpl implements PoPasswordService {

	private static final Logger log = Logger.getLogger(PoPasswordServiceImpl.class);
	
	private MailService mailService;
	private PoScriptingService scriptingService;
	
	private PoPasswordQuality passwordQuality;
	private PoPasswordResetPolicy resetPolicy;
	private PoPasswordResetMail resetMail;

	private PoPasswordDAO passwordDAO;
	
	private CryptUtil cryptUtil = new CryptUtil();

	/** Implements PoPasswordService. */
	@Override
	public int getPasswordValidityDays(PoPerson person) {
		return getPasswordValidityDays(getPoPassword(person));
	}

	/** Implements PoPasswordService. */
	@Override
	public void hasPasswordNeededQuality(String password, PoPerson person) throws PoRuntimeException {
		password = avoidNull(password);
		String message = passwordQuality.conforms(password, getNewestPasswords(person, passwordQuality.getNumberOfDifferingLatestPasswords()));
		if (message != null)
			throw new PoRuntimeException(message);
	}

	/** Implements PoPasswordService. */
	@Override
	public void setNewPassword(PoPerson person, String oldPassword, String newPassword) throws PoRuntimeException {
		if (isOldPasswordCorrect(person, oldPassword) == false)
			throw new PoRuntimeException("Invalid old password for "+person.getUserName());
		
		hasPasswordNeededQuality(newPassword, person);	// throws Exception when not conforming
		setPassword(newPassword, person, false);
	}

	/** Implements PoPasswordService. */
	@Override
	public boolean isPasswordCorrect(PoPerson person, String password) {
		boolean checkExpiration = (passwordQuality.getValidityDays() != PoPasswordQuality.PASSWORD_NEVER_EXPIRES);
		return isPasswordCorrect(person, password, true, checkExpiration, false);
	}

	/** Implements PoPasswordService. */
	@Override
	public boolean isOldPasswordCorrect(PoPerson person, String oldPassword) {
		return isPasswordCorrect(person, oldPassword, false, false, true);
	}

	/** Implements PoPasswordService. */
	@Override
	public void resetPassword(PoPerson person) {
		if (resetPolicy.isUseUsernameAsStandardPassword())
			setPassword(person.getUserName(), person, resetPolicy.isForcePasswordChangeAfterReset());
		else
			setPassword(resetPolicy.getStandardResetPassword(), person, resetPolicy.isForcePasswordChangeAfterReset());
	}

	/** Implements PoPasswordService. */
	@Override
	public void resetToRandomPasswordAndSendMail(PoPerson person) {
		String recipient = resetMail.getExplicitRecipient();
		if (StringUtils.isEmpty(recipient))
			recipient = person.getEmail();
		
		if (StringUtils.isEmpty(recipient))
			throw new PoRuntimeException(PoRuntimeException.MAIL_ADDRESS_NOT_AVAILABLE_FOR_PASSWORD_RESET);
		
		final String randomPassword = passwordQuality.createConformantRandomPassword(getNewestPasswords(person, passwordQuality.getNumberOfDifferingLatestPasswords()));
		setPassword(randomPassword, person, resetPolicy.isForcePasswordChangeAfterResetToRandomPassword());
		
		Mail mail = new Mail();
		mail.setTo(recipient);
		mail.setFrom(resetMail.getSenderMailAddress());
		
		final Map<String,Object> placeholderValues = new HashMap<String,Object>();
		placeholderValues.put("person", person);
		placeholderValues.put("password", randomPassword);
		
		final String subject = scriptingService.velocitySubstitution(resetMail.getSubject(), placeholderValues);
		mail.setSubject(subject);
		final String messageBody = scriptingService.velocitySubstitution(resetMail.getTemplateBody(), placeholderValues);
		mail.setMessage(messageBody);
		mailService.sendMail(mail);
	}

	/** Implements PoPasswordService. */
	@Override
	public Date getLatestPasswordCreationDate(PoPerson person)	{
		final PoPassword poPassword = getPoPassword(person);
		return (poPassword != null) ? poPassword.getValidfrom() : null;
	}

	/** Implements PoPasswordService. */
	@Override
	public void deletePassword(PoPerson person) {
		setPassword("", person, false);
	}

	/** Implements PoPasswordService. */
	@Override
	public boolean hasValidPassword(PoPerson person) {
		PoPassword poPassword = getPoPassword(person);
		if (poPassword == null)
			return false;
		byte [] bytes = poPassword.getPassword();
		byte [] decrypted = decrypt(bytes);
		return decrypted != null && decrypted.length > 0;
	}


	private PoPassword getPoPassword(PoPerson person)	{
		return passwordDAO.findCurrentPassword(person);
	}
	
	private boolean isPasswordCorrect(PoPerson person, String password, boolean checkForceFlag, boolean checkExpiration, boolean isCheckingOldPassword) {
		PoPassword poPassword = getPoPassword(person);
		if (log.isDebugEnabled())
			log.debug("Authenticating user: person="+person.getFullName()+", checkForceFlag="+checkForceFlag+", checkExpiration="+checkExpiration+" given password is null: "+(password == null)+", persistent password is null: "+(poPassword == null));
		
		if (poPassword == null)	{	// user has no persistent password
			if (password == null && isCheckingOldPassword)	{
				//log.info("Both the persistent and the given password are null, this happens when inserting first password");
				return true;	// needed to let set an inital password
			}
			log.warn("No persistent password found for "+person);
			// happens when somebody logs in without having a database password,
			// but his log-in is configured to use database authentication
			return false;
		}
		
		final boolean authenticated = checkPassword(poPassword, password);
		if (authenticated == false)	{
			if (log.isInfoEnabled())
				log.info("The input password for person=" + person.getUserName() + " is wrong, returning false");
			return false;	// illegal attempt to log in
		}
		
		if (checkForceFlag && poPassword.isForcePasswordChange() || // has been reset by admin
				checkExpiration && getPasswordValidityDays(poPassword) <= 0)	// has expired
		{
			if (log.isInfoEnabled())
				log.info("Password has expired, so redirect to change-password dialog");
			throw new PoRuntimeException(MessageConstants.ERROR_NEW_PASSWORD_REQUIRED);
		}
		
		return authenticated;
	}

	/**
	 * Calculates the number of days the passed password is still valid.
	 * @return Integer.MAX_VALUE when it never expires, positive integer for valid password, zero or negative for invalid password.
	 */
	private int getPasswordValidityDays(PoPassword password) {
		if (password == null)
			return 0;
					
		if (password.isValidtoNull() /*DateTools.INFINITY_DATE*/ || passwordQuality.getValidityDays() == PoPasswordQuality.PASSWORD_NEVER_EXPIRES)
			return Integer.MAX_VALUE;
		
		long consumedValidityDays = (DateTools.now().getTime() - password.getValidfrom().getTime()) / 1000L / 60L / 60L / 24L;
		long leftValidityDays = passwordQuality.getValidityDays() - consumedValidityDays;
		return (int) leftValidityDays;
	}

	private byte[] decrypt(byte[] password) {
		return cryptUtil.decrypt(password);
	}

	private byte[] encrypt(byte[] password) {
		return cryptUtil.encrypt(password);
	}

	private boolean checkPassword(PoPassword poPassword, String password) {
		assert poPassword != null : "Can not check password without persistent password";
		
		password = avoidNull(password);
		byte [] givenPassword = password.getBytes();	// TODO consider platform encoding?
		byte [] persistentPassword = decrypt(poPassword.getPassword());
		boolean authenticated = Arrays.equals(givenPassword, persistentPassword);
		if (log.isDebugEnabled())
			log.debug("User was authenticated: "+authenticated+", given="+givenPassword.length+", persistent="+persistentPassword.length);
		return authenticated;
	}

	private void setPassword(String password, PoPerson person, boolean forcePasswordChange)	{
		password = avoidNull(password);
		final PoPassword oldPoPassword = passwordDAO.findCurrentPassword(person);
		Date now = new Date();
		
		// manage old password: valid-to goes to 'now'
		if (oldPoPassword != null)	{
			now = ensureNowBeingBiggerThanOldValidFrom(oldPoPassword, now);
			oldPoPassword.setValidto(now);
			passwordDAO.save(oldPoPassword);
		}
		
		// manage new password: valid-from goes to now
		final PoPassword newPoPassword = new PoPassword();
		newPoPassword.setPerson(person);
		newPoPassword.setValidfrom(now);
		newPoPassword.setForcePasswordChange(forcePasswordChange);
		if (passwordQuality.getValidityDays() > 0 && password.length() > 0)	{
			long expireTime = now.getTime() + passwordQuality.getValidityDays() * 24L * 60L * 60L * 1000L;
			assert expireTime > now.getTime();
			newPoPassword.setValidto(new Date(expireTime));
			assert newPoPassword.getValidto().after(newPoPassword.getValidfrom());
		}
		// else: "validto" would be set to INFINITY automatically -> empty password (== deleted password) is valid forever!
		
		newPoPassword.setPassword(encrypt(password.getBytes()));	// TODO consider platform encoding
		
		passwordDAO.save(newPoPassword);
	}
	
	/**
	 * Ensure that validFrom of new password is bigger than that of old password.
	 * In unit tests it could happen that those times are equal, which would make the DAO read fail,
	 * but this is also to prevent invalid values caused by database admins.
	 * @param oldPoPassword the old password holding the validFrom.
	 * @param now the date about to be set into new pasword's validFrom.
	 * @return a later "now" Date when the passed one is equal to validFrom of old password. 
	 */
	private Date ensureNowBeingBiggerThanOldValidFrom(PoPassword oldPoPassword, Date now) {
		if (oldPoPassword.getValidfrom().getTime() > now.getTime())	{	// impossible except when some database admin manipulated the valid-from value
			log.warn("Found password valid-from date ("+oldPoPassword.getValidfrom()+") that is later than 'now' ("+now+")! Correcting this to 'now' ...");
			oldPoPassword.setValidfrom(now);	// will be saved afterwards
		}
		while (oldPoPassword.getValidfrom().getTime() == now.getTime())	{
			try { Thread.sleep(10); } catch (InterruptedException e) {/*ignore this exception*/}
			now = new Date();
		}
		return now;
	}

	private String avoidNull(String password) {
		return (password != null) ? password : "";
	}

	private List<String> getNewestPasswords(PoPerson person, int numberOfDifferingPasswords) {
		List<PoPassword> newestPasswords = passwordDAO.findNewestPasswords(person, numberOfDifferingPasswords);
		List<String> newestPasswordStrings = new ArrayList<String>(newestPasswords.size());
		for (PoPassword p : newestPasswords)	{
			newestPasswordStrings.add(new String(decrypt(p.getPassword())));
		}
		return newestPasswordStrings;
	}

	
	/** Spring accessor. */
	public void setScriptingService(PoScriptingService scriptingService) {
		this.scriptingService = scriptingService;
	}

	/** Spring accessor. */
	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

	/** Spring setter. */
	public void setPasswordQuality(PoPasswordQuality passwordQuality) {
		this.passwordQuality = passwordQuality;
	}

	/** Spring setter. */
	public void setResetPolicy(PoPasswordResetPolicy resetPolicy) {
		this.resetPolicy = resetPolicy;
	}

	/** Spring setter. */
	public void setResetMail(PoPasswordResetMail resetMail) {
		this.resetMail = resetMail;
	}

	/** Spring setter. */
	public void setPasswordDAO(PoPasswordDAO passwordDAO) {
		this.passwordDAO = passwordDAO;
	}

}
