package com.vimukti.stocks.mail;

import java.io.File;

import com.vimukti.stocks.Stocks;
import com.vimukti.stocks.server.ServerMain;

public class MailSender {

	public static void send(String subject, String[] to, String[] cc,
			String[] bcc, File attachment) {
		EMailMessage emailMsg = new EMailMessage();
		emailMsg.setSubject(subject);
		emailMsg.setReplayTO(EMailSenderAccount.getDefaultSender()
				.getSenderEmailID());
		emailMsg.setAttachment(attachment);
		if (to != null) {
			for (int i = 0; i < to.length; i++) {
				emailMsg.setRecepeant(to[i]);
			}
		}
		if (cc != null) {
			for (int j = 0; j < cc.length; j++) {
				emailMsg.setccRecepeant(cc[j]);
			}
		}
		if (bcc != null) {
			for (int j = 0; j < bcc.length; j++) {
				emailMsg.setbccRecepeant(bcc[j]);
			}
		}
		EMailJob job = new EMailJob(emailMsg);

		EmailManager.getInstance().addJob(job);
	}

	public static void send(String[] to, String client) {
		send("Stocks As on " + Stocks.getDateStr(), to,
				new String[] { ServerMain.getProperty("cc",
						"rajesh@vimukti.com") }, null,
				new File(ServerMain.getFilepath(), client + ".zip"));
	}

	public static void sendMF(String[] bccs) {
		send("Mutual Funds as on " + Stocks.getDateStr(), null,
				new String[] { ServerMain.getProperty("cc",
						"rajesh@vimukti.com") }, bccs,
				new File(ServerMain.getFilepath() + File.separator + "mf", "MF"
						+ Stocks.getDate() + ".zip"));
	}
}
