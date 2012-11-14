package com.vimukti.stocks.client;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import com.vimukti.stocks.Stocks;
import com.vimukti.stocks.core.HibernateUtil;

public class Vartha extends Client {

	public Vartha() {
		super("vartha");
	}

	public void run() throws Exception {

		// va_bse_nse.html
		Stocks.info("va_bse_nse.html");
		createVaBseNse();
		
		// stocks.html
		Stocks.info("stocks.html");
		createGroups("stocks.html", "vartha.stocks.others");

		// bse100
		// createBSE100();

		copy("bsegl.txt", "adv_dec.txt");
	}

	@Override
	protected String makeString(Object[] row) {
		String val = "";
		val = formateName(row[0]) + " " + row[1] + '(' + row[2] + ')' + row[3];
		if (row.length > 4) {
			val += "<BR><I>" + row[4] + '(' + row[5] + ')' + row[6] + "</I>";
		}
		return val;
	}

	@Override
	protected void afterGrups(FileWriter writer) throws Exception {
		writer.write("<br>");
		writer.write("<b>NSE</b><br>");
		Session session = HibernateUtil.getCurrentSession();
		Query query = session.getNamedQuery("vartha.stocks.nseonly");
		List list = query.list();
		for (Object object : list) {
			Object[] row = (Object[]) object;
			String line = "<I>" + row[0] + ' ' + row[1] + '(' + row[2] + ')'
					+ row[3] + "</I>";
			writer.write(line);
			writer.write("<br>");
		}
	}

	private void createVaBseNse() throws Exception {
		File vaBseNse = new File(parent, "va_bse_nse.html");
		FileWriter writer = new FileWriter(vaBseNse);

		//
		writeData(writer, "Andhra Stocks", "vartha.andhra");

		//
		writeData(writer, "NSE_NIFTY", "vartha.nse");

		//
		writeData(writer, "NSE_JUNIOR", "vartha.nsejunior");

		//
		writeData(writer, "NSE_OTHER", "vartha.nseother");

		writer.flush();
		writer.close();
	}

	private void writeData(FileWriter writer, String name, String queryName)
			throws Exception {
		Session session = HibernateUtil.getCurrentSession();
		Query query = session.getNamedQuery(queryName);
		writer.write("<BR><B>" + name + "</B><BR>\n");
		List list = query.list();
		for (Object object : list) {
			Object[] row = (Object[]) object;
			writer.write(formateName(row[0]) + " " + row[1] + " (" + row[2]
					+ ")" + row[3] + "<BR>\n");
		}
	}

	private void createBSE100() throws Exception {
		File vaBseNse = new File(parent, "bse100.html");
		FileWriter writer = new FileWriter(vaBseNse);
		writer.write("<HTML><BODY>\n");
		writer.write("<BR><B>BSE100</B><BR>\n");
		Session session = HibernateUtil.getCurrentSession();
		Query query = session.getNamedQuery("vartha.bse100");
		List list = query.list();
		for (Object object : list) {
			Object[] row = (Object[]) object;
			String line = formateName(row[0]) + " " + row[1] + '(' + row[2]
					+ ')' + row[3] + "<BR>\n";
			writer.write(line);
		}
		writer.write("</BODY></HTML>");

		writer.flush();
		writer.close();
	}

}
