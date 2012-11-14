package com.vimukti.stocks.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vimukti.stocks.mail.MailSender;

public class SendServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (req.getSession(true).getAttribute("emailId") == null) {
			resp.sendRedirect("/login");
			return;
		}
		String type = req.getParameter("type");
		if (type != null && type.equals("mf")) {
			String mf = req.getParameter("mf");
			String[] bccs = mf.split(",");
			MailSender.sendMF(bccs);
		} else if (type.equals("stocks")) {
			String to = req.getParameter("to");
			String client = req.getParameter("client");
			send(to, client);
		} else {
			String[] clients = new String[] { "andhra_jyothy", "andhra_prabha",
					"deccan", "nt", "vartha" };
			for (String s : clients) {
				send(ServerMain.getProperty(s, ""), s);
			}
		}
		resp.sendRedirect("/home");
	}

	private void send(String to, String client) {
		String[] split = to.split(",");
		MailSender.send(split, client);
	}
}
