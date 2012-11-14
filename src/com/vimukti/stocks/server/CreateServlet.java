package com.vimukti.stocks.server;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.vimukti.stocks.MutualFunds;
import com.vimukti.stocks.Stocks;

public class CreateServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (req.getSession().getAttribute("status") == null) {
			resp.sendRedirect("/home");
		} else {
			req.setAttribute("status", Stocks.getStatus());
			req.getRequestDispatcher("wait.jsp").forward(req, resp);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Object attribute = req.getSession().getAttribute("emailId");
		if (attribute == null) {
			resp.sendRedirect("/");
			return;
		}
		final HttpSession session = req.getSession();
		final File file = new File(ServerMain.getFilepath());
		session.setAttribute("status", "creating");
		final String type = req.getParameter("type");
		final String date = req.getParameter("date");
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					if (type != null && type.equals("mf")) {
						MutualFunds mutualFunds = new MutualFunds(file, date);
						mutualFunds.start();
						mutualFunds.join();
					} else {
						Stocks.createZipFeils(file);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				session.setAttribute("status", null);
				session.removeAttribute("status");
			}
		}).start();
		req.setAttribute("status", "started...");
		req.getRequestDispatcher("wait.jsp").forward(req, resp);
	}
}
