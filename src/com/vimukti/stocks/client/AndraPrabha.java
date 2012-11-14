package com.vimukti.stocks.client;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import com.vimukti.stocks.Stocks;
import com.vimukti.stocks.core.HibernateUtil;

public class AndraPrabha extends Client {

	public AndraPrabha() {
		super("andhra_prabha");
	}

	@Override
	public void run() throws Exception {
		Stocks.info("ap_stocks.html");
		createStocks();

		Stocks.info("bse500.html");
		base500();

		copy("bsegl.txt");
	}

	private void base500() throws Exception {
		File vaBseNse = new File(parent, "bse500.html");
		FileWriter writer = new FileWriter(vaBseNse);

		Session session = HibernateUtil.getCurrentSession();
		Query query = session.getNamedQuery("ap.bse500");
		writer.write("<BR><B>BSE 500</B><BR>\n");
		List list = query.list();
		for (Object object : list) {
			Object[] row = (Object[]) object;
			String val = "";
			val = row[0] + " " + row[1] + '(' + row[2] + ')' + row[3] + "<BR>";
			if (row[4] != null) {
				val += "<I>" + row[4] + '(' + row[5] + ')' + row[6]
						+ "</I><BR>";
			}
			val += "\n";
			writer.write(val);
		}

		writer.write("</BODY></HTML>");

		writer.flush();
		writer.close();
	}

	private void createStocks() throws Exception {
		File vaBseNse = new File(parent, "ap_stocks.html");
		FileWriter writer = new FileWriter(vaBseNse);

		Session session = HibernateUtil.getCurrentSession();
		Query query = session.getNamedQuery("ap.stocks");
		writer.write("<BR><B>Andhra Stocks</B><BR>\n");
		List list = query.list();
		for (Object object : list) {
			Object[] row = (Object[]) object;
			String val = "";
			val = formateName(row[0]) + " " + row[1] + '(' + row[2] + ')'
					+ row[3] + "<BR>";
			if (row[4] != null) {
				val += "<I>" + row[4] + '(' + row[5] + ')' + row[6]
						+ "</I><BR>";
			}
			val += "\n";
			writer.write(val);
		}

		pdData(writer);

		writer.write("</BODY></HTML>");

		writer.flush();
		writer.close();

	}
}
