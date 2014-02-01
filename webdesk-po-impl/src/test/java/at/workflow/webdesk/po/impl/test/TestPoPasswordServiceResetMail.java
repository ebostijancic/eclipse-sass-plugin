package at.workflow.webdesk.po.impl.test;

import at.workflow.tools.mail.MailServerConfiguration;
import at.workflow.tools.mail.MailService;
import at.workflow.webdesk.po.impl.PoPasswordResetPolicy;
import at.workflow.webdesk.po.model.PoPerson;

/**
 * CAUTION: This is not a test that can be run in every nightly build, as it sends a mail!
 * This test checks the "reset password mail" use-case.
 * 
 * Relies on at/workflow/webdesk/po/impl/test/data/TestData.xml.
 * 
 * @author fritzberger 25.10.2010
 */
public class TestPoPasswordServiceResetMail extends AbstractPoPasswordTestCase {

	public void testResetToRandomPassword()	{
		MailService mailService = (MailService) getBean("PoMailService");
		MailServerConfiguration config = new MailServerConfiguration();
		config.setAdress("smtp.acw.at");
		config.setDefaultSender("fritz.ritzberger@workflow.at");
		config.setPort(25);
		mailService.setConfiguration(config);
		
		PoPasswordResetPolicy resetPolicy = (PoPasswordResetPolicy) getBean("PoPasswordResetPolicy");
		
		PoPerson person = readPerson();
		person.setEmail("fritz.ritzberger@workflow.at");
		
		getPasswordService().resetToRandomPasswordAndSendMail(person);
		assertFalse(getPasswordService().isPasswordCorrect(person, person.getUserName()));
		assertFalse(getPasswordService().isPasswordCorrect(person, resetPolicy.getStandardResetPassword()));
	}

}
