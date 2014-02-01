package at.workflow.tools.mail;

import java.io.File;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

public class MailServiceImpl implements MailService {

	private static final Logger logger = Logger.getLogger(MailService.class);

	private MailServerConfiguration configuration;
	private boolean active = true;

	@Override
	public void setConfiguration(MailServerConfiguration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	public void sendMail(Mail mail) {
		sendMail(mail, configuration);
	}

	@Override
	public void sendMail(Mail mail, MailServerConfiguration configuration) {
		
		if (active==false) {
			logger.warn("Trying to send Mail to " + mail.getTo() + " but Mailing is not enabled! Turn on in the Systemparameter 'MailServiceImpl.active'!");
			return;
		}
		
		try {
			if (mail.getFrom() == null)
				mail.setFrom(configuration.getDefaultSender());

			if (mail.getFrom() == null) {
				mail.setFrom("automated@workflow.at");
				logger.warn("Given MailServerConfiguration does not define the default sender: " + configuration.getDefaultSender());
			}

			String template = mail.getMessage();
			
			// replace variables if contained in template
			if (mail.getVariables() != null) {
				for (Map.Entry<String,String> e : mail.getVariables().entrySet()) {
					template = template.replaceAll(e.getKey(), e.getValue());
				}
			}

			JavaMailSenderImpl sender = new JavaMailSenderImpl();

			if (logger.isDebugEnabled()) {
				logger.debug("create JavaMailSender with host=[" + configuration.getAdress() +
						"], username=[" + configuration.getUserName() +
						"], password=[" + configuration.getPassword() +
						"], port=[" + configuration.getPort() + "]");
			}

			sender.setHost(configuration.getAdress());
			sender.setUsername(configuration.getUserName());
			sender.setPassword(configuration.getPassword());
			sender.setPort(configuration.getPort());

			final boolean isMessageHtml = (
					template.indexOf("<html>") > -1 ||
					template.indexOf("<body>") > -1 ||
					template.indexOf("<p>") > -1 ||
					template.indexOf("<div>") > -1 ||
					template.indexOf("<br/>") > -1
					);
			final boolean hasAttachments = (mail.getFilenames() != null && mail.getFilenames().size() > 0);

			if (hasAttachments == false && isMessageHtml == false) {
				assert hasAttachments == false : "Sending PLAIN text message with attachments will not work, no attachments implemented!";
				
				if (logger.isDebugEnabled())
					logger.debug("use plain text mail format");

				// create Mail Message
				SimpleMailMessage msg = new SimpleMailMessage();
				msg.setFrom(mail.getFrom());
				msg.setTo(mail.getToAsArray());
				msg.setCc(mail.getCopyToAsArray());
				msg.setBcc(mail.getBlindCopyToAsArray());
				msg.setSubject(mail.getSubject());

				// variables have been already replaced, thus use the local variable
				msg.setText(template);

				if (logger.isDebugEnabled())
					logger.debug("try to send mail with mime-msg=" + msg + ", text=" + template);

				// send mail
				sender.send(msg);
			}
			else {	// either HTML message or has attachments
				if (logger.isDebugEnabled())
					logger.debug("use html mail format");

				if (isMessageHtml == false)
					;	// TODO convert plain message text to HTML to keep formatting (paragraphs, empty lines)?
				
				// create Html Mail Message
				MimeMessage msg = sender.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(msg, true);
				helper.setFrom(mail.getFrom());

				if (mail.getTo() != null)
					helper.setTo(mail.getToAsArray());

				if (mail.getCopyTo() != null)
					helper.setCc(mail.getCopyToAsArray());

				if (mail.getBlindCopyTo() != null)
					helper.setBcc(mail.getBlindCopyToAsArray());

				helper.setSubject(mail.getSubject());
				helper.setText(template, true);

				if (hasAttachments) {
					File f = null;
					FileSystemResource fsr = null;
					InputStreamSource iss = null;
					for (int i = 0; i < mail.getFilenames().size(); i++) {
						String fname = mail.getFilenames().get(i);

						if (mail.getByteArrays() != null) {
							byte[] myByteArray = mail.getByteArrays().get(i);
							iss = new ByteArrayResource(myByteArray);
							helper.addAttachment(fname, iss);
						} else {
							f = new File(fname);
							fsr = new FileSystemResource(f);
							helper.addAttachment(fname, fsr);
						}
					}
				}

				if (logger.isDebugEnabled())
					logger.debug("try to send mail with mime-msg=" + msg + ", text=" + template);

				// send mail
				sender.send(msg);
			}
		}
		catch (MessagingException e) {
			logger.error("could not send mail:", e);
			throw new RuntimeException(e);
		}
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
