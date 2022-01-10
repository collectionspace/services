package org.collectionspace.services.common;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;

import org.collectionspace.services.config.tenant.EmailConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailUtil {
	final static Logger logger = LoggerFactory.getLogger(EmailUtil.class);

	private static final String MAIL_SMTP_HOST = "mail.smtp.host";
	private static final String MAIL_SMTP_PORT = "mail.smtp.port";
	private static final String MAIL_SMTP_TLS = "mail.smtp.starttls.enable";
	private static final String MAIL_SMTP_SSL_PROTOCOLS = "mail.smtp.ssl.protocols";

	private static final String MAIL_FROM = "mail.from";
	private static final String MAIL_DEBUG = "mail.debug";

	public static void main(String [] args) {
		String username = "collectionspace.lyrasis@gmail.com";
	    String password = "bogus_password";
	    String recipient = "remillet@gmail.com";

	    Properties props = new Properties();

	    props.setProperty(MAIL_SMTP_HOST, "smtp.gmail.com");
	    props.setProperty(MAIL_SMTP_PORT, "587");
	    props.setProperty(MAIL_SMTP_TLS, "true");
	    props.setProperty(MAIL_FROM, "collectionspace.lyrasis@gmail.com");
	    props.setProperty(MAIL_DEBUG, "true");
		props.setProperty(MAIL_SMTP_SSL_PROTOCOLS, "TLSv1.2");

	    Session session = Session.getInstance(props, null);
	    MimeMessage msg = new MimeMessage(session);

	    try {
		    msg.setRecipients(Message.RecipientType.TO, recipient);
		    msg.setFrom(new InternetAddress(props.getProperty("mail.from")));
		    msg.setSubject("JavaMail hello world example");
		    msg.setSentDate(new Date());
		    msg.setText("Hello, world!\n");

		    Transport transport = session.getTransport("smtp");

		    transport.connect(username, password);
		    transport.sendMessage(msg, msg.getAllRecipients());
		    transport.close();
	    } catch (Exception e) {
	    	System.err.println(e.getMessage());
	    }
	}

	/*
	 * recipients - Is a comma separated sequence of addresses
	 */
	public static String sendMessage(EmailConfig emailConfig, String recipients, String message) {
		String result = null;

	    Properties props = new Properties();

	    props.setProperty(MAIL_SMTP_HOST, emailConfig.getSmtpConfig().getHost());
	    props.setProperty(MAIL_SMTP_PORT, emailConfig.getSmtpConfig().getPort().toString());
		props.setProperty(MAIL_SMTP_TLS,
			new Boolean(emailConfig.getSmtpConfig().getSmtpAuth().isEnabled()).toString());

	    props.setProperty(MAIL_FROM, emailConfig.getFrom());
	    props.setProperty(MAIL_DEBUG, new Boolean(emailConfig.getSmtpConfig().isDebug()).toString());

	    Session session = Session.getInstance(props, null);
	    MimeMessage msg = new MimeMessage(session);

	    try {
		    msg.setRecipients(Message.RecipientType.TO, recipients);
		    msg.setFrom(new InternetAddress(emailConfig.getFrom()));
		    msg.setSubject(emailConfig.getPasswordResetConfig().getSubject());
		    msg.setSentDate(new Date());
		    msg.setText(message);

		    Transport transport = session.getTransport("smtp");
		    if (emailConfig.getSmtpConfig().getSmtpAuth().isEnabled()) {
			    String username = emailConfig.getSmtpConfig().getSmtpAuth().getUsername();
			    String password = emailConfig.getSmtpConfig().getSmtpAuth().getPassword();
				props.setProperty(MAIL_SMTP_SSL_PROTOCOLS,
					emailConfig.getSmtpConfig().getSmtpAuth().getProtocols());
			    transport.connect(username, password);
		    } else {
		    	transport.connect();
		    }

		    transport.sendMessage(msg, msg.getAllRecipients());
		    transport.close();
	    } catch (MessagingException e) {
	    	logger.error(e.getMessage(), e);
	    	result = e.getMessage();
	    }

	    return result;
	}

}