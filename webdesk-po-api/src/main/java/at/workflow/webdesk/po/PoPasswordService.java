package at.workflow.webdesk.po;

import java.util.Date;

import at.workflow.webdesk.po.model.PoPerson;

/**
 * Contains methods to read and manipulate user passwords, including their encryption and
 * password quality criteria.
 * <p />
 * This was made for RZB users that are not in their LDAP system and thus need password security.
 * <p />
 * Exception messages currently can be found in PoRuntimeException class.
 * 
 * TODO move exception messages to here!
 * 
 * @author fritzberger 21.10.2010
 */
public interface PoPasswordService {

	/**
	 * Returns the validity of the password in days, can be positive, zero or negative.
	 * By convenience only a positive day number makes up a valid password.
	 * @param person the person to retrieve password validity duration for.
	 * @return Integer.MAX_VALUE when it never expires, positive integer for valid password, zero or negative for invalid password.
	 */
	int getPasswordValidityDays(PoPerson person);

	/**
	 * Checks the password quality.
	 * @param password the clear-text password to check for quality criteria, can be empty but must not be null. 
	 * @param person the person to check for (number of different expired passwords).
	 * @exception PoRuntimeException telling the reason when password does not conform to quality criteria.
	 */
	void hasPasswordNeededQuality(String password, PoPerson person) throws PoRuntimeException;

	/**
	 * Sets a new password for the given user. Checks the quality of the password and throws an
	 * exception when it does not conform.
	 * @param person the user the password should be stored for, must not be null, but can be empty string: "",
	 * 		then the password has a 'deleted' state and will not expire.
	 * @param newPassword the new clear-text password candidate for the given person, can be empty but must not be null.
	 * @param oldPassword the old clear-text password of the given person, must be <code>null</code>
	 * 		when there was no password until now.
	 * @exception PoRuntimeException when new password does not conform to quality criteria
	 * 		(forgot to call <code>hasPasswordNeededQuality()</code> before calling this?),
	 * 		or the passed person does not match with old password.
	 */
	void setNewPassword(PoPerson person, String oldPassword, String newPassword) throws PoRuntimeException;

	/**
	 * Checks if the given clear-text password is the correct one for given person.
	 * @param person the user the password belongs to, must not be null.
	 * @param password the clear-text password to check for given person, can be empty but must not be null.
	 * @return true when the password is the the correct password of the given person.
	 * @exception PoRuntimeException when password is expired, or a change was forced by some admin.
	 */
	boolean isPasswordCorrect(PoPerson person, String password);

	/**
	 * Checks if the given clear-text password is currently the correct one for given person.
	 * This call does not check if the password is expired, or its change has been forced by some admin.
	 * @param person the user the password belongs to, must not be null.
	 * @param password the clear-text password to check for given person, can be empty but must not be null.
	 * @return true when the password is the the correct password of the given person, without checking expiration or force-flag.
	 */
	boolean isOldPasswordCorrect(PoPerson person, String oldPassword);

	/**
	 * Resets the password for given person (according to the configured reset-policy).
	 * This is a method available to the admin user (not normal user) when resetting the
	 * password of somebody to a well-known value (person.username, or commonly known password).
	 * @param person the user the password has to be reset for, must not be null.
	 */
	void resetPassword(PoPerson person);

	/**
	 * Resets the password for given person to a password conforming to configured criteria
	 * and sends a mail with a newly defined password.
	 * This is a method available to the normal user (not admin) when resetting the own password.
	 * @param person the person the password has to be reset for.
	 */
	void resetToRandomPasswordAndSendMail(PoPerson person);	

	/**
	 * The latest password creation date.
	 * @param person the person to read the latest password creation data for.
	 * @return the exact date/time when the latest password has been created, or null if no password exists at all.
	 */
	Date getLatestPasswordCreationDate(PoPerson person);

	/**
	 * Deletes the password for given person.
	 * @param person the person the password has to be deleted for.
	 */
	void deletePassword(PoPerson person);	

	/**
	 * @param person the person to check the password for.
	 * @return false if the password either does not exist or is not valid (by date).
	 */
	boolean hasValidPassword(PoPerson person);

}
