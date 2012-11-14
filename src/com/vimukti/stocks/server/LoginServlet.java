package com.vimukti.stocks.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (req.getSession(true).getAttribute("emailId") != null) {
			resp.sendRedirect("/home");
		} else {
			doPost(req, resp);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String email = req.getParameter("email");
		String pass = req.getParameter("pass");
		if (email == null || pass == null) {
			req.getRequestDispatcher("index.html").forward(req, resp);
			return;
		}

		if (!ServerMain.getUserName().equals(email)
				|| !ServerMain.getPassword().equals(pass)) {
			req.getRequestDispatcher("index.html").forward(req, resp);
			return;
		}

		req.getSession().setAttribute("emailId", email);
		if (req.getSession().getAttribute("status") == null) {
			resp.sendRedirect("/home");
		} else {
			req.getRequestDispatcher("wait.jsp").forward(req, resp);
		}
	}
}