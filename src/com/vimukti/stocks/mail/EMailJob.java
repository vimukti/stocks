package com.vimukti.stocks.mail;

import java.util.ArrayList;
import java.util.List;

public class EMailJob {

	List<EMailMessage> messages = new ArrayList<EMailMessage>();

	public EMailJob() {

	}

	public EMailJob(EMailMessage emailMsg) {
		this.messages.add(emailMsg);
	}

	public List<EMailMessage> getMailMessages() {
		return this.messages;
	}

	public List<EMailMessage> getMessages() {
		return messages;
	}

	public void setMessages(List<EMailMessage> messages) {
		this.messages = messages;
	}
}
