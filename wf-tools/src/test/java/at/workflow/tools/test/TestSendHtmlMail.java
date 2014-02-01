package at.workflow.tools.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.workflow.tools.mail.Mail;
import at.workflow.tools.mail.MailServerConfiguration;
import at.workflow.tools.mail.MailService;
import at.workflow.tools.mail.MailServiceImpl;

import junit.framework.TestCase;

public class TestSendHtmlMail extends TestCase {

	private MailServerConfiguration msc;
	private MailService mailService;
	
	@Override
	public void setUp() {

		msc = new MailServerConfiguration();
		msc.setAdress("smtp.acw.at");
		msc.setDefaultSender("gabriel@workflow.at");
		msc.setPort(25);
		
		mailService = new MailServiceImpl();
		mailService.setConfiguration(msc);
	}
	
	public void testPlainTextMail() {
		Mail plainTextMail = new Mail("gabriel.gruber@workflow.at", "testmail " + new Date());
		plainTextMail.setMessage("textmessage");
		mailService.sendMail(plainTextMail);
	}
	
	public void testHtmlMail() {
		Mail plainTextMail = new Mail("gabriel.gruber@workflow.at", "testmail " + new Date());
		plainTextMail.setMessage("textmessage");
		mailService.sendMail(plainTextMail);
		
		Mail htmlMail = new Mail();
		
		List<String> recipients = new ArrayList<String>();
		recipients.add("gabriel.gruber@workflow.at");
		htmlMail.setSendTo(recipients);
		
		htmlMail.setSubject("test HTML mail " + new Date());
		
		String htmlMsg = "<html><body><h1>Überschrift</h1><p>Dies ist der Paragraph</p></body></html>";
		
		htmlMail.setMessage(htmlMsg);
		
		mailService.sendMail(htmlMail);
	}

}
