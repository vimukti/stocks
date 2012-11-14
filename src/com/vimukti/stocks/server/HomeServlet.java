package com.vimukti.stocks.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HomeServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (req.getSession(true).getAttribute("emailId") == null) {
			resp.sendRedirect("/login");
			return;
		}
		req.setAttribute("andhra_jyothy",
				ServerMain.getProperty("andhra_jyothy"));
		req.setAttribute("andhra_prabha",
				ServerMain.getProperty("andhra_prabha"));
		req.setAttribute("deccan", ServerMain.getProperty("deccan"));
		req.setAttribute("nt", ServerMain.getProperty("nt"));
		req.setAttribute("vartha", ServerMain.getProperty("vartha"));
		req.setAttribute("mf", ServerMain.getProperty("mf"));

		req.getRequestDispatcher("/home.jsp").forward(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
}
