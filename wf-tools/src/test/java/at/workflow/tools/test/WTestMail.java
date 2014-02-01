package at.workflow.tools.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import at.workflow.tools.FileTools;
import at.workflow.tools.mail.Mail;
import at.workflow.tools.mail.MailServerConfiguration;
import at.workflow.tools.mail.MailService;
import at.workflow.tools.mail.MailServiceImpl;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

/**
 * Tests mail sending and receiving via MailServiceImpl(): HTML, plain text, attachments.
 * Sent test mails can be found under http://asterix:81/mail/tmail.nsf, user = tmail, pwd = testmail.
 * 
 * @author hentner
 * @author fritzberger 2012-03-29 improved test criteria.
 */
public class WTestMail extends TestCase {
	
	private final String address = "test@localhost.com";
	
	private final String sentHtmlText =     "<h3>This is an html - message.</h3><p>Content</p>Sincerly, <i>name</i><br/><br/>adress<br/>city";
	private final String receivedHtmlText = "<h3>This is an html - message.</h3><p>Content</p>Sincerly, <i>Mäx Müster</i><br/><br/>Dannebergplatz 6 / 23<br/>1030 Vienna";
	private final Map<String,String> variables = new HashMap<String,String>();
	{
		variables.put("name", "Mäx Müster");
		variables.put("adress", "Dannebergplatz 6 / 23");
		variables.put("city", "1030 Vienna");
	}
	
	private MailService mailService;
	private GreenMail smtpServer;
	private GreenMail popServer;
	
	
	@Override
	public void setUp() {
		ServerSetup smtpSetup = new ServerSetup(25, "localhost", ServerSetup.PROTOCOL_SMTP);
		smtpServer = new GreenMail(smtpSetup); //uses test ports by default greenMail.start(); 
		smtpServer.start();
		
		ServerSetup popSetup = new ServerSetup(110, "localhost", ServerSetup.PROTOCOL_POP3);
		popServer = new GreenMail(popSetup);
		popServer.start();
		
		mailService = new MailServiceImpl();
		MailServerConfiguration mailServiceConfig = new MailServerConfiguration();
		mailServiceConfig.setAdress("localhost");
		mailServiceConfig.setDefaultSender(address);
		mailServiceConfig.setUserName("test");
		mailServiceConfig.setPassword(address);
		mailServiceConfig.setPort(25);
		mailService.setConfiguration(mailServiceConfig);
	}
	
	@Override
	public void tearDown() {
		smtpServer.stop();
		popServer.stop();
	}
	
	
	public void testSendPlainTextMail() throws IOException, MessagingException {
		final String plainText = "This is a < html > Test";
		final String subject = "Testcase: WTestMail - Plain Message / "+new Date();
		
		Mail mail = new Mail(address, subject);
		mail.setMessage(plainText);
		mailService.sendMail(mail);
		
		MimeMessage [] mails = smtpServer.getReceivedMessages();
		assertEquals(1, mails.length);
		
		String mailText = getBodyText(mails[0]);	//GreenMailUtil.getBody(mails[0]);
		assertEquals(subject, mails[0].getSubject());
		System.out.println(mailText);
		assertEquals(plainText, mailText.trim());	// trim to remove trailing \r\n
	}
	
	public void testSendHtmlMail() throws IOException, MessagingException {
		List<String> addresses = new ArrayList<String>();
		addresses.add("test@workflow.at");
		
		Mail mail = new Mail("test@workflow.at", "Testcase: WTestMail - Html Message / "+new Date());
		mail.setCopyTo(addresses);
		mail.setBlindCopyTo(addresses);
		mail.setVariables(variables);
		mail.setMessage(sentHtmlText );
		mailService.sendMail(mail);
		
		MimeMessage [] mails = smtpServer.getReceivedMessages();
		assertEquals(3, mails.length);	// mail, cc, bcc
		
		String mailText = getBodyText(mails[0]);	//GreenMailUtil.getBody(mails[0]);
		assertEquals(receivedHtmlText, mailText);
		
		for (Map.Entry<String,String> e : variables.entrySet())	{
			String variable = e.getKey();
			String value = e.getValue();
			assertFalse(mailText.contains(variable));
			assertTrue(mailText.contains(value));
		}
	}
	
	public void testSendMailWithAttachments() throws IOException, MessagingException {
		Mail mail = new Mail("test@workflow.at", "Testcase: WTestMail - Html Message with testcase / " + new Date());
		List<String> addresses = new ArrayList<String>();
		addresses.add("test@workflow.at");
		mail.setCopyTo(addresses);
		mail.setVariables(variables);
		mail.setMessage(sentHtmlText);
		
		List<String> filenames = new ArrayList<String>();
		filenames.add("File1.jpg");
		mail.setFilenames(filenames);
		
		Resource imageResource = new ClassPathResource("image.jpg");
		List<byte[]> byteArrays = new ArrayList<byte[]>();
		byteArrays.add(FileTools.readFile(imageResource.getFile()));
		mail.setByteArrays(byteArrays);
		mailService.sendMail(mail);
		
		MimeMessage [] mails = smtpServer.getReceivedMessages();
		assertEquals(2, mails.length);
		
		Multipart multipart = (Multipart) mails[0].getContent();
		List<byte[]> attachments = getAttachments(multipart);
		assertEquals(1, attachments.size());
		
		byte [] attachment = attachments.get(0);
		byte [] testArray = byteArrays.get(0);
		assertEquals(attachment.length, testArray.length);
		for (int i = 0; i < attachment.length; i++)
			assertEquals(attachment[i], testArray[i]);
	}

	
	private List<byte[]> getAttachments(Multipart multipart) throws IOException, MessagingException {
		List<byte[]> attachments = new ArrayList<byte[]>();
		for (int i = 0; i < multipart.getCount(); i++)	{
			BodyPart bodyPart = multipart.getBodyPart(i);
			Object content = bodyPart.getContent();
			if (content instanceof Multipart)	{
				attachments.addAll(getAttachments((Multipart) content));
			}
			else if (content instanceof InputStream)	{
				InputStream in = (InputStream) content;
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				int c;
				while ((c = in.read()) != -1)
					out.write(c);
				attachments.add(out.toByteArray());
			}
		}
		return attachments;
	}

	private String getBodyText(MimeMessage message) throws MessagingException, IOException	{
		Object content = message.getContent();
		
		if (content instanceof Multipart)
			return getBodyText((Multipart) content);
		
		if (content instanceof String)
			return (String) content;
		
		throw new IllegalStateException("Unknown message content: "+content);
	}
	
	private String getBodyText(Multipart multipart) throws MessagingException, IOException	{
		for (int i = 0; i < multipart.getCount(); i++)	{
			BodyPart bodyPart = multipart.getBodyPart(i);
			Object content = bodyPart.getContent();
			if (content instanceof Multipart)	{
				String bodyText = getBodyText((Multipart) content);
				if (bodyText != null)
					return bodyText;
			}
			else if (content instanceof String)	{
				return (String) content;
			}
		}
		return null;
	}
}
