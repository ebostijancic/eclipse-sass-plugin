package at.workflow.webdesk.po.impl;

/**
 * Spring bean for configuration of password reset policy.
 * <p />
 * When <code>useUsernameAsStandardPassword</code> is true, the username is used
 * despite any defined <code>standardResetPassword</code>.
 * When it is false, the standard password is used, when it is empty or null the
 * empty string will be the reset password.
 * 
 * @author fritzberger 21.10.2010
 */
public class PoPasswordResetPolicy {

	/** fri_2011-01-31: due to problems with invalid configurations provide a default password that is not empty (would cause denial of user login). */
	private static final String HARDCODED_DEFAULT_PASSWORD = "webdesk";
	
	private String standardResetPassword;
	private boolean useUsernameAsStandardPassword;
	private boolean forcePasswordChangeAfterResetToRandomPassword;
	private boolean forcePasswordChangeAfterReset;

	/** @return the standard reset password any person receives when "Reset Password" was clicked. */
	public String getStandardResetPassword() {
		return standardResetPassword != null && standardResetPassword.length() > 0 ? standardResetPassword : HARDCODED_DEFAULT_PASSWORD;
	}

	/** @param standardResetPassword the standard reset password any person receives when "Reset Password" was clicked. */
	public void setStandardResetPassword(String standardResetPassword) {
		this.standardResetPassword = standardResetPassword;
	}

	/** @return whether the username is used as standard reset password. */
	public boolean isUseUsernameAsStandardPassword() {
		return useUsernameAsStandardPassword;
	}

	/** @param useUsernameAsStandardPassword whether the username is used as standard reset password. */
	public void setUseUsernameAsStandardPassword(boolean useUsernameAsStandardPassword) {
		this.useUsernameAsStandardPassword = useUsernameAsStandardPassword;
	}

	/** @return whether the user should be prompted to change password after reset-to-random (mail). */
	public boolean isForcePasswordChangeAfterResetToRandomPassword() {
		return forcePasswordChangeAfterResetToRandomPassword;
	}

	/** @param forcePasswordChangeAfterResetToRandomPassword whether the user should be prompted to change password after reset-to-random (mail). */
	public void setForcePasswordChangeAfterResetToRandomPassword(boolean forcePasswordChangeAfterResetToRandomPassword) {
		this.forcePasswordChangeAfterResetToRandomPassword = forcePasswordChangeAfterResetToRandomPassword;
	}

	public boolean isForcePasswordChangeAfterReset() {
		return forcePasswordChangeAfterReset;
	}

	public void setForcePasswordChangeAfterReset(
			boolean forcePasswordChangeAfterReset) {
		this.forcePasswordChangeAfterReset = forcePasswordChangeAfterReset;
	}
	
}
