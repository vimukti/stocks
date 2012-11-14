package com.vimukti.stocks.client;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import com.vimukti.stocks.Stocks;
import com.vimukti.stocks.core.HibernateUtil;

public class AndraJyothi extends Client {

	public AndraJyothi() {
		super("andhra_jyothy");
	}

	@Override
	public void run() throws Exception {
		Stocks.info("aj_special_data.html");
		ajSpecialData();

		copy("bsegl.txt");
	}

	private void ajSpecialData() throws Exception {
		File vaBseNse = new File(parent, "aj_special_data.html");
		FileWriter writer = new FileWriter(vaBseNse);

		Session session = HibernateUtil.getCurrentSession();
		Query query = session.getNamedQuery("aj_special_data");
		writer.write("<HTML><BODY><B>BSE A</B><BR>\n");
		List list = query.list();
		for (Object object : list) {
			Object[] row = (Object[]) object;
			String val = "";
			val = formateName(row[0]) + " " + row[1] + '(' + row[2] + ')'
					+ row[3] + "<BR>";
			val += "\n";
			writer.write(val);
		}

		query = session.getNamedQuery("aj.stocks");
		writer.write("<BR><B>Andhra Stocks</B><BR>\n");
		list = query.list();
		for (Object object : list) {
			Object[] row = (Object[]) object;
			String val = "";
			val = formateName(row[0]) + " " + row[1] + '(' + row[2] + ')'
					+ row[3] + "<BR>";
			writer.write(val);
		}

		pdData(writer);

		writer.write("</BODY></HTML>");

		writer.flush();
		writer.close();
	}
}
