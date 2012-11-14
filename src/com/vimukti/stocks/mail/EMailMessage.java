package com.vimukti.stocks.mail;

import java.io.File;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class EMailMessage {

	private String from;

	public String subject;

	public String content;

	Set<File> attachment;

	public Set<String> recepeiants = new HashSet<String>();
	public Set<String> ccrecepeiants = new HashSet<String>();
	public Set<String> bccrecepeiants = new HashSet<String>();

	private String replayTO;

	public boolean isPlain;

	public Set<String> getRecipeants() {
		return recepeiants;
	}

	public void setRecepeants(Set<String> to) {
		this.recepeiants = to;
	}

	public void setRecepeant(String to) {
		this.recepeiants.add(to);
	}

	public void setccRecepeant(String cc) {
		this.ccrecepeiants.add(cc);
	}

	public Set<String> getccRecipeants() {
		return ccrecepeiants;
	}

	public void setccRecepeants(Set<String> cc) {
		this.ccrecepeiants = cc;
	}

	public Set<File> getAttachments() {
		return attachment;
	}

	public void setAttachments(Set<File> attachment) {
		this.attachment = attachment;
	}

	public void setAttachment(File file) {

		if (attachment == null) {
			attachment = new HashSet<File>();
		}
		this.attachment.add(file);
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setReplayTO(String replayTO) {
		this.replayTO = replayTO;
	}

	public String getReplayTO() {
		return replayTO;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void setbccRecepeant(String string) {
		this.bccrecepeiants.add(string);
	}

	public Set<String> getbccRecipeants() {
		return bccrecepeiants;
	}
}
