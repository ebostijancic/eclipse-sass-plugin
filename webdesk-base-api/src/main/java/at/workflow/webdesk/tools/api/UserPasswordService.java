package at.workflow.webdesk.tools.api;

/**
 * Models part of the <i>PoPasswordService</i> in this module (that does not know the PO module).
 * 
 * @author fritzberger 28.10.2010
 */
public interface UserPasswordService {
	
	/**
	 * @param user the username of the person to check password for.
	 * @param password String (in default encoding) containing the password bytes to check.
	 * @return true when the password is correct, else false.
	 * @throws PoRuntimeException when password has expired, text see <code>MessageConstants.ERROR_NEW_PASSWORD_REQUIRED</code>.
	 */
	boolean isPasswordCorrect(User user, String password);

}
