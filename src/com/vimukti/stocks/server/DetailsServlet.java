package com.vimukti.stocks.server;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;

import com.vimukti.stocks.Stocks;
import com.vimukti.stocks.core.HibernateUtil;

public class DetailsServlet extends HttpServlet {

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

		req.setAttribute("details", getDetails());
		req.getRequestDispatcher("details.jsp").forward(req, resp);
	}

	private String getDetails() {
		String details = "";
		details += getCompaniesDetails();
		File file = new File(ServerMain.getFilepath());
		details += getSizes(file);
		details += "</br>";
		details += getSizes(new File(file, "mf"));
		return details;
	}

	private String getCompaniesDetails() {
		Session session = HibernateUtil.openSession();
		try {
			String data = "";
			Query namedQuery = session.getNamedQuery("get.total.count");
			Object uniqueResult = namedQuery.uniqueResult();
			data += "Total bse companies:" + uniqueResult + "</br></br>";
			namedQuery = session.getNamedQuery("get.top.companies");
			namedQuery.setParameter("dhlimit", Stocks.getDhlimit());
			List list = namedQuery.list();
			data += "Name----Closing---PrevClose----Volume---high52---low52</br>";
			for (Object obj : list) {
				Object[] objs = (Object[]) obj;
				data += objs[0] + "----" + objs[1] + "---" + objs[2] + "----"
						+ objs[3] + "---" + objs[4] + "---" + objs[5] + "</br>";
			}
			data += "</br></br>";
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
		return null;
	}

	private String getSizes(File file) {
		if (!file.exists()) {
			return "";
		}
		String details = "";
		for (File f : file.listFiles()) {
			if (f.getName().endsWith(".zip")) {
				details += getData(f);
			}
		}
		String folds = "";
		for (File f : file.listFiles()) {
			if (f.isDirectory()) {
				folds += "</br>" + getSizes(f);
			}
		}

		return details + folds;
	}

	private String getData(File f) {
		String data = f.getName() + ":" + getLength(f.length()) + "Kb</br>";
		return data;
	}

	private double getLength(long length) {
		return length / 1024.0;
	}
}
