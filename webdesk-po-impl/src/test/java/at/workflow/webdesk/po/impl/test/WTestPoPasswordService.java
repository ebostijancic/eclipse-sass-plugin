package at.workflow.webdesk.po.impl.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import at.workflow.webdesk.po.PoRuntimeException;
import at.workflow.webdesk.po.impl.PoPasswordQuality;
import at.workflow.webdesk.po.impl.PoPasswordResetPolicy;
import at.workflow.webdesk.po.model.PoPerson;
import at.workflow.webdesk.tools.api.MessageConstants;

/**
 * Tests all of the password-service except any mail-sending functionality.
 * 
 * @author fritzberger 25.10.2010
 */
public class WTestPoPasswordService extends AbstractPoPasswordTestCase {

	private static final String WEF_ORIGINAL_PASSWORD = "wef";
	private static final String SOMEVALIDPASSWORD = "wefpassword1";
	private static final String OTHERVALIDPASSWORD = SOMEVALIDPASSWORD+"1";
	
	private PoPasswordQuality passwordQuality;
	private int defaultLength;
	private int defaultDigits;
	private int defaultSpecialChars;
	private boolean defaultUpperLower;
	private int defaultNumberDifferentLatestPasswords;
	private int validityDays;
	
	private PoPasswordResetPolicy resetPolicy;
	private String standardResetPassword;
	private boolean forcePasswordChangeAfterResetToRandomPassword;
	private boolean useUsernameAsStandardPassword;
	
	@Override
	protected void onSetUpAfterDataGeneration() throws Exception {
		passwordQuality = (PoPasswordQuality) getBean("PoPasswordQuality");
		defaultLength = passwordQuality.getMinimalLength();
		defaultDigits = passwordQuality.getMinimalDigitsCount();
		defaultSpecialChars = passwordQuality.getMinimalSpecialCharactersCount();
		defaultUpperLower = passwordQuality.isRequiresUpperAndLowerCharacters();
		defaultNumberDifferentLatestPasswords = passwordQuality.getNumberOfDifferingLatestPasswords();
		validityDays = passwordQuality.getValidityDays();
		
		resetPolicy = (PoPasswordResetPolicy) getBean("PoPasswordResetPolicy");
		standardResetPassword = resetPolicy.getStandardResetPassword();
		forcePasswordChangeAfterResetToRandomPassword = resetPolicy.isForcePasswordChangeAfterResetToRandomPassword();
		useUsernameAsStandardPassword = resetPolicy.isUseUsernameAsStandardPassword();
	}
	
	@Override
	protected void onTearDownAfterTransaction() {
		passwordQuality.setMinimalLength(defaultLength);
		passwordQuality.setMinimalDigitsCount(defaultDigits);
		passwordQuality.setMinimalSpecialCharactersCount(defaultSpecialChars);
		passwordQuality.setRequiresUpperAndLowerCharacters(defaultUpperLower);
		passwordQuality.setNumberOfDifferingLatestPasswords(defaultNumberDifferentLatestPasswords);	
		passwordQuality.setValidityDays(validityDays);
		
		resetPolicy.setStandardResetPassword(standardResetPassword);
		resetPolicy.setForcePasswordChangeAfterResetToRandomPassword(forcePasswordChangeAfterResetToRandomPassword);
		resetPolicy.setUseUsernameAsStandardPassword(useUsernameAsStandardPassword);
	}
	
	public void testSetNewPasswordAndCheckValidityDays()	{
		logger.info("testSetNewPasswordAndCheckValidityDays starting");
		
		PoPerson person = readPerson();
		
		getPasswordService().setNewPassword(person, WEF_ORIGINAL_PASSWORD, SOMEVALIDPASSWORD);
		assertTrue(getPasswordService().isPasswordCorrect(person, SOMEVALIDPASSWORD));
		assertEquals(30, getPasswordService().getPasswordValidityDays(person));
	}

	public void testPasswordExpirationWhenValidityDaysIsZero()	{
		logger.info("testPasswordExpirationWhenValidityDaysIsZero starting");
		
		PoPerson person = readPerson();
		
		getPasswordService().setNewPassword(person, WEF_ORIGINAL_PASSWORD, SOMEVALIDPASSWORD);
		assertTrue(getPasswordService().isPasswordCorrect(person, SOMEVALIDPASSWORD));
		
		passwordQuality.setValidityDays(0);	// force users to change their password
		try	{
			getPasswordService().isPasswordCorrect(person, SOMEVALIDPASSWORD);
			fail("Password now must be expired due to zero validity days");
		}
		catch (PoRuntimeException e)	{
			// is expected here
		}
	}

	public void testPasswordQuality()	{
		logger.info("testPasswordQuality starting");
		
		passwordQuality.setMinimalLength(16);
		passwordQuality.setMinimalDigitsCount(4);
		passwordQuality.setMinimalSpecialCharactersCount(4);
		passwordQuality.setRequiresUpperAndLowerCharacters(true);
		passwordQuality.setNumberOfDifferingLatestPasswords(2);

		final PoPerson person = readPerson();
		
		final String password1 = "123456789Abc#?+=";	// should be OK
		assertTrue(hasPasswordNeededQuality(password1, person));
		
		final String password2 = "23456789Abc#?+=";	// too short
		assertFalse(hasPasswordNeededQuality(password2, person));
		
		final String password3 = "123456789abc#?+=";	// no uppercase
		assertFalse(hasPasswordNeededQuality(password3, person));
		
		final String password4 = "123456789ABC#?+=";	// no lowercase
		assertFalse(hasPasswordNeededQuality(password4, person));
		
		final String password5 = "Abcdefghi123#?+=";	// not enough digits
		assertFalse(hasPasswordNeededQuality(password5, person));
		
		final String password6 = "Abcdefghi1234?+=";	// not enough special chars
		assertFalse(hasPasswordNeededQuality(password6, person));
		
		// test NumberOfDifferingLatestPasswords
		// set new passwords
		final String newPassword1 = password1;
		final String newPassword2 = newPassword1+"2";
		final String newPassword3 = newPassword1+"3";
		getPasswordService().setNewPassword(person, WEF_ORIGINAL_PASSWORD, newPassword1);
		getPasswordService().setNewPassword(person, newPassword1, newPassword2);
		
		// check that a new password must not match the 2 latest passwords
		assertFalse(hasPasswordNeededQuality(newPassword1, person));
		assertFalse(hasPasswordNeededQuality(newPassword2, person));

		getPasswordService().setNewPassword(person, newPassword2, newPassword3);
		
		assertTrue(hasPasswordNeededQuality(newPassword1, person));
		assertFalse(hasPasswordNeededQuality(newPassword2, person));
		assertFalse(hasPasswordNeededQuality(newPassword3, person));
		
		final String newPassword4 = newPassword1+"4";
		assertTrue(hasPasswordNeededQuality(newPassword4, person));
		
		// create some random passwords and check if they differ
		String [] newestPasswords = { newPassword1, newPassword2, newPassword3 };
		List<String> created = new ArrayList<String>();	// remember all created passwords
		for (int i = 0; i < Math.max(passwordQuality.getMinimalLength(), 8); i++)	{
			String p = passwordQuality.createConformantRandomPassword(Arrays.asList(newestPasswords));
			System.err.println("random password: "+p);
			if (created.contains(p))
				fail("A random password was created twice: '"+p+"'. This failure might show up a createConformantRandomPassword() weakness but also can happen accidentally!");
			
			created.add(p);
		}
	}

	public void testPasswordCorrectness()	{
		logger.info("testPasswordCorrectness starting");
		
		PoPerson person = readPerson();
		
		getPasswordService().setNewPassword(person, WEF_ORIGINAL_PASSWORD, SOMEVALIDPASSWORD);
		
		assertTrue(getPasswordService().isPasswordCorrect(person, SOMEVALIDPASSWORD));
		assertFalse(getPasswordService().isPasswordCorrect(person, OTHERVALIDPASSWORD));
		
		getPasswordService().setNewPassword(person, SOMEVALIDPASSWORD, OTHERVALIDPASSWORD);
		
		assertFalse(getPasswordService().isPasswordCorrect(person, SOMEVALIDPASSWORD));
		assertTrue(getPasswordService().isPasswordCorrect(person, OTHERVALIDPASSWORD));
	}

	public void testResetPassword()	{
		logger.info("testResetPassword starting");
		
		// prepare a known standard-password (for negative checking)
		final String standardPassword = SOMEVALIDPASSWORD+"1";
		resetPolicy.setStandardResetPassword(standardPassword);
		
		PoPerson person = readPerson();
		
		// change the reset policy
		resetPolicy.setUseUsernameAsStandardPassword(true);	// is dominant
		
		// establish user-name as standard password
		getPasswordService().resetPassword(person);
		
		// following MUST NOT throw exception because this is simply an illegal attempt to log in
		assertFalse(getPasswordService().isPasswordCorrect(person, "the wrong password for sure 1"));
		assertFalse(getPasswordService().isPasswordCorrect(person, standardPassword));
		
		try	{	// login with correct password must throw exception to force new password
			getPasswordService().isPasswordCorrect(person, person.getUserName());
			fail("Any password check must throw exception when password has been reset!");
		}
		catch (PoRuntimeException e)	{
			if (false == e.getMessage().equals(MessageConstants.ERROR_NEW_PASSWORD_REQUIRED))
				throw e;
		}
		
		// change back the reset policy
		resetPolicy.setUseUsernameAsStandardPassword(false);	// standard password is valid now
		
		// establish standard-password as current password
		getPasswordService().resetPassword(person);
		
		// following MUST NOT throw exception because this is simply an illegal attempt to log in
		assertFalse(getPasswordService().isPasswordCorrect(person, "the wrong password for sure 2"));
		assertFalse(getPasswordService().isPasswordCorrect(person, person.getUserName()));
		
		try	{	// login with correct password must throw exception to force new password
			assertTrue(getPasswordService().isPasswordCorrect(person, standardPassword));
			fail("Any password check must throw exception when password has been reset!");
		}
		catch (PoRuntimeException e)	{
			if (false == e.getMessage().equals(MessageConstants.ERROR_NEW_PASSWORD_REQUIRED))
				throw e;
		}
		
		// try to reset to some other
		getPasswordService().setNewPassword(person, standardPassword, SOMEVALIDPASSWORD);
		assertTrue(getPasswordService().isPasswordCorrect(person, SOMEVALIDPASSWORD));
	}
	
	
	public void testPasswordValidityDays()	{
		PoPerson person = readPerson();
		getPasswordService().setNewPassword(person, WEF_ORIGINAL_PASSWORD, SOMEVALIDPASSWORD);
		assertEquals(30, getPasswordService().getPasswordValidityDays(person));
		assertTrue(getPasswordService().isPasswordCorrect(person, SOMEVALIDPASSWORD));
		
		// force the validity period to be over with 0
		passwordQuality.setValidityDays(0);
		try	{
			assertTrue(getPasswordService().isPasswordCorrect(person, SOMEVALIDPASSWORD));
			fail("Any password check must throw exception when password has expired!");
		}
		catch (PoRuntimeException e)	{
			if (false == e.getMessage().equals(MessageConstants.ERROR_NEW_PASSWORD_REQUIRED))
				throw e;
		}
		
		// set the validity period to PASSWORD_NEVER_EXPIRES, the password will be valid
		passwordQuality.setValidityDays(PoPasswordQuality.PASSWORD_NEVER_EXPIRES);	// -1
		assertTrue(getPasswordService().isPasswordCorrect(person, SOMEVALIDPASSWORD));
		
		// force the validity period to be over with any negative except -1
		passwordQuality.setValidityDays(-2);
		try	{
			assertTrue(getPasswordService().isPasswordCorrect(person, SOMEVALIDPASSWORD));
			fail("Any password check must throw exception when password has expired!");
		}
		catch (PoRuntimeException e)	{
			if (false == e.getMessage().equals(MessageConstants.ERROR_NEW_PASSWORD_REQUIRED))
				throw e;
		}
	}
	
	
	private boolean hasPasswordNeededQuality(String password, PoPerson person) {
		try	{
			getPasswordService().hasPasswordNeededQuality(password, person);
			return true;
		}
		catch (PoRuntimeException e)	{
			logger.info("Password quality error is: "+e.getMessage());
			return false;
		}
	}

}
