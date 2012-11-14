package com.vimukti.stocks.mail;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.vimukti.stocks.Stocks;

public class EmailManager extends Thread {

	static LinkedBlockingQueue<EMailJob> queue = new LinkedBlockingQueue<EMailJob>();
	private static EmailManager manager;
	private boolean shutdown;

	public EmailManager() {
		super("Email Manager");
	}

	public void run() {
		while (!shutdown) {
			try {
				EMailJob mail = getQueue().take();
				sendMail(mail);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void sendMail(final EMailJob mail) {
		// Set the host smtp address
		final EMailSenderAccount sender = EMailSenderAccount.getDefaultSender();
		Properties p = getProperties(mail);

		Session session = null;
		if (sender.isSmtpAuthentication()) {
			Authenticator authenticator = new javax.mail.Authenticator() {
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(
							sender.getSenderEmailID(),
							sender.getSenderPassword());
				}
			};
			session = Session.getInstance(p, authenticator);
		} else {
			session = Session.getInstance(p);
		}
		session.setDebug(false);

		for (EMailMessage emsg : mail.getMailMessages()) {
			try {
				MimeMessage msg = createMimeMessage(sender, emsg, session);
				Transport transport = session.getTransport("smtp");
				transport.connect(sender.getOutGoingMailServer(),
						sender.getPortNumber(), sender.getSenderEmailID(),
						sender.getSenderPassword());
				transport.sendMessage(msg, msg.getAllRecipients());
				transport.close();
				// Transport.send(msg);
				Stocks.info("Mail has been sent");
			}

			catch (Exception ex) {
				ex.printStackTrace();
				break;
			}

		}

	}

	private Properties getProperties(EMailJob mail) {
		final EMailSenderAccount sender = EMailSenderAccount.getDefaultSender();
		Properties p = new Properties();
		if (sender.isAutheticationRequired()) {
			// p.put("mail.smtps.auth", true);
			p.put("mail.smtp.auth", true);
			p.put("mail.smtp.user", sender.getSenderEmailID());
		} else {
			p.put("mail.smtp.auth", false);
		}

		if (sender.isEnabledTls()) {
			// #smtp protocol over TLS (was ssmtp)
			if (sender.getPortNumber() == 465) {
				p.put("mail.smtp.tls", "true");
				p.put("mail.smtp.socketFactory.port", sender.getPortNumber());
				p.put("mail.smtp.socketFactory.class",
						"javax.net.ssl.SSLSocketFactory");
			} else {
				// Submission for Simple Mail Transfer
				p.put("mail.smtp.starttls.enable", true);
			}
		}
		p.put("mail.smtp.host", sender.getOutGoingMailServer());
		p.put("mail.smtp.port", sender.getPortNumber());
		p.put("mail.smtp.8BITMIME", "true");
		p.put("mail.smtp.PIPELINING", "true");
		// p.put("mail.smtp.debug", "true");
		p.put("mail.smtp.socketFactory.fallback", "false");
		p.put("protocol", "smtp");
		return p;
	}

	private MimeMessage createMimeMessage(EMailSenderAccount acc,
			EMailMessage emsg, Session session) {
		// create a message
		MimeMessage msg = new MimeMessage(session);

		// for to address
		InternetAddress addressTo[] = new InternetAddress[emsg.getRecipeants()
				.size()];
		int i = 0;
		try {
			for (String email : emsg.getRecipeants()) {
				InternetAddress address = new InternetAddress(email);
				addressTo[i] = address;
				i++;
			}

			msg.setRecipients(Message.RecipientType.TO, addressTo);

			// for cc address

			if (emsg.getccRecipeants().size() > 0) {
				InternetAddress addressCC[] = new InternetAddress[emsg
						.getccRecipeants().size()];
				i = 0;
				for (String ccEmail : emsg.getccRecipeants()) {
					InternetAddress address = new InternetAddress(ccEmail);
					addressCC[i] = address;
					i++;
				}
				msg.setRecipients(Message.RecipientType.CC, addressCC);
			}
			// For BCC address
			if (emsg.getbccRecipeants().size() > 0) {
				InternetAddress addressBCC[] = new InternetAddress[emsg
						.getbccRecipeants().size()];
				i = 0;
				for (String ccEmail : emsg.getbccRecipeants()) {
					InternetAddress address = new InternetAddress(ccEmail);
					addressBCC[i] = address;
					i++;
				}
				msg.setRecipients(Message.RecipientType.BCC, addressBCC);
			}

			if (emsg.getReplayTO() != null) {
				msg.setReplyTo(new InternetAddress[] { new InternetAddress(emsg
						.getReplayTO()) });
			}
			String from = emsg.getFrom();
			// If there is no from supplied with the EmailMessage we will use
			// the from the settings
			if (from == null) {
				from = acc.getSenderEmailID();
			}
			msg.setFrom(new InternetAddress(from));

			// MultiPart body part

			Multipart multipart = new MimeMultipart();

			// This is the message body content part

			MimeBodyPart messageContentPart = new MimeBodyPart();

			String content = "--\nVimukti Technologies Private limited,\nH.No : 1-1-298/28,Plot #28 , Arul Colony ,\nDr A S Rao nagar, Ecil post ,\nkapra,Hyderabad-500062\nPh: +91-9989696513";
			messageContentPart.setContent(content, "text/plain; charset=UTF-8");
			multipart.addBodyPart(messageContentPart);

			// This is the template Attachment part
			if (emsg.getAttachments() != null) {
				for (File file : emsg.getAttachments()) {
					MimeBodyPart messageAttachmentBodyPart = new MimeBodyPart();
					messageAttachmentBodyPart = new MimeBodyPart();

					DataSource source = new FileDataSource(file);

					messageAttachmentBodyPart.setDataHandler(new DataHandler(
							source));
					messageAttachmentBodyPart.setFileName(file.getName());
					multipart.addBodyPart(messageAttachmentBodyPart);
				}
			}

			// Put parts in message
			msg.setContent(multipart);

			// end

			// Optional : You can also set your custom headers in the Email
			// if
			// you
			// Want
			msg.addHeader("MyHeaderName", "myHeaderValue");

			// Setting the Subject and Content Type
			msg.setSubject(emsg.subject, "UTF-8");
			// msg.setSubject(emsg.subject);//text/plain; charset=ISO-8859-1
			// msg.setContent( map.getValue().getContent(), "text/html");
		} catch (AddressException e) {

			e.printStackTrace();
		} catch (MessagingException e) {

			e.printStackTrace();
		}
		return msg;
	}

	private LinkedBlockingQueue<EMailJob> getQueue() {
		return queue;
	}

	public static EmailManager getInstance() {
		if (manager == null) {
			manager = new EmailManager();
		}
		return manager;
	}

	public void addJob(EMailJob job) {
		queue.add(job);

	}

	public void stopThread() {
		shutdown = true;
		this.interrupt();
	}

}
