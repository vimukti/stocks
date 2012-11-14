package com.vimukti.stocks.mail;

import java.io.Serializable;

import com.vimukti.stocks.server.ServerMain;

public class EMailSenderAccount implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static EMailSenderAccount INSTANCE;
	private String senderEmailID = "";
	private String senderPassword = "";
	private String outGoingMailServer = "";
	private int portNumber = 25;
	private String protocol = "";
	private boolean smtpAuthentication = false;
	private boolean sslAutheticationRequired = false;
	private boolean isTtlsEnabled = false;

	public EMailSenderAccount() {

	}

	public EMailSenderAccount(String senderEmailID, String accountName,
			String senderPassword, String outGoingMailServer, int portNumber,
			String protocol, boolean smtpAuthentication,
			boolean sslAutheticationRequired, boolean startTtlsEnables) {
		this.senderEmailID = senderEmailID;
		this.senderPassword = senderPassword;
		this.outGoingMailServer = outGoingMailServer;
		this.portNumber = portNumber;
		this.protocol = protocol;
		this.smtpAuthentication = smtpAuthentication;
		this.sslAutheticationRequired = sslAutheticationRequired;
		this.isTtlsEnabled = startTtlsEnables;
	}

	public String getSenderEmailID() {
		return senderEmailID;
	}

	public void setSenderEmailID(String senderEmailID) {
		this.senderEmailID = senderEmailID;
	}

	public String getSenderPassword() {
		return senderPassword;
	}

	public void setSenderPassword(String senderPassword) {
		this.senderPassword = senderPassword;
	}

	public String getOutGoingMailServer() {
		return outGoingMailServer;
	}

	public void setOutGoingMailServer(String outGoingMailServer) {
		this.outGoingMailServer = outGoingMailServer;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public boolean isSmtpAuthentication() {
		return smtpAuthentication;
	}

	public void setSmtpAuthentication(boolean smtpAuthentication) {
		this.smtpAuthentication = smtpAuthentication;
	}

	public boolean isAutheticationRequired() {
		return sslAutheticationRequired;
	}

	public void setSslAutheticationRequired(boolean sslAutheticationRequired) {
		this.sslAutheticationRequired = sslAutheticationRequired;
	}

	public boolean isEnabledTls() {
		return isTtlsEnabled;
	}

	public void setEnableTls(boolean startTtlsEnables) {
		this.isTtlsEnabled = startTtlsEnables;
	}

	public static EMailSenderAccount getDefaultSender() {
		if (INSTANCE != null) {
			return INSTANCE;
		}
		EMailSenderAccount acc = new EMailSenderAccount();

		acc.setOutGoingMailServer(ServerMain.getProperty("outGoingMailServer",
				"localhost"));
		acc.setPortNumber(Integer.parseInt(ServerMain.getProperty("portNumber",
				"25")));
		acc.setProtocol(ServerMain.getProperty("protocol", "smtp"));

		acc.setSmtpAuthentication(ServerMain.getProperty("smtpAuthentication",
				"no").equalsIgnoreCase("yes"));

		acc.setSslAutheticationRequired(ServerMain.getProperty(
				"sslAutheticationRequired", "no").equalsIgnoreCase("yes"));

		acc.setEnableTls(ServerMain.getProperty("startTtlsEnables", "no")
				.equalsIgnoreCase("yes"));

		String emailID = ServerMain.getProperty("senderEmailID");
		acc.setSenderEmailID(emailID);

		String password = ServerMain.getProperty("senderPassword");

		acc.setSenderPassword(password);
		INSTANCE = acc;
		return INSTANCE;
	}

}
