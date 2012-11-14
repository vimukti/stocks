package com.vimukti.stocks.client;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import com.vimukti.stocks.Stocks;
import com.vimukti.stocks.core.HibernateUtil;

public class Dh extends Client {

	public Dh() {
		super("deccan");
	}

	private int dhlimit = 10000;
	private int cc;

	@Override
	public void run() throws Exception {
		int prevcc = 0;
		do {
			Stocks.info("dh_only_bse.html,dhlimit:" + dhlimit);
			createGroups("dh_only_bse.html", "deccan");
			prevcc = cc;
			if (cc > 785) {
				if (cc < 900) {
					dhlimit += 20000;
				} else {
					dhlimit += 30000;
				}
			}
			cc = 0;
		} while (prevcc > 785);

		Stocks.info("pv_onlybse.html");
		createPvOnlyBse();
		Stocks.setDHLimit(dhlimit);
		copy("bsegl.txt");
	}

	@Override
	protected void setDhlimit(Query query) {
		query.setParameter("dhlimit", dhlimit);
	}

	private void createPvOnlyBse() throws Exception {
		File vaBseNse = new File(parent, "pv_onlybse.html");
		FileWriter writer = new FileWriter(vaBseNse);
		writer.write("<html><body>");

		writer.write("<b>GROUP A</b><br>");
		Session session = HibernateUtil.getCurrentSession();
		Query query = session.getNamedQuery("deccan");
		query.setParameter("cat", "A");
		setDhlimit(query);
		List list = query.list();
		List<String> ra = new ArrayList<String>();
		for (Object object : list) {
			Object[] row = (Object[]) object;
			String val = "";
			String formateName = formateName(row[0]);
			double parseDouble = Double.parseDouble(row[1].toString());
			val = formateName + " " + parseDouble;
			if (row.length > 6 && row[6] != null) {
				val += "<BR><I>" + row[6] + " " + row[7] + '(' + row[8]
						+ ")<B>" + getInt(row[10]) + '-' + getInt(row[11])
						+ "</B>(" + (getInt(row[9]) / 1000) + ")</I>";
			}
			ra.add(val);
		}
		write(writer, ra);

		writer.write("</body></html>");

		writer.flush();
		writer.close();
	}

	protected String makeString(Object[] row) {
		String val = "";
		String formateName = formateName(row[0]);
		val = formateName + " " + row[1] + '(' + row[2] + ")<B>"
				+ getInt(row[4]) + '-' + getInt(row[5]) + "</B>("
				+ (getInt(row[3]) / 1000) + ')';
		cc++;
		if (row.length > 6 && row[6] != null) {
			val += "<BR><I>" + row[6] + '(' + row[7] + ")<B>" + getInt(row[9])
					+ '-' + getInt(row[10]) + "</B>(" + (getInt(row[8]) / 1000)
					+ ")</I>";
			cc++;
		}
		return val;
	}

	private int getInt(Object object) {
		Double parseDouble = Double.parseDouble(object.toString());
		return parseDouble.intValue();
	}
}
