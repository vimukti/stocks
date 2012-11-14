package com.vimukti.stocks.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.hibernate.Query;
import org.hibernate.Session;

import com.vimukti.stocks.Stocks;
import com.vimukti.stocks.core.HibernateUtil;

public abstract class Client {
	protected File parent;
	private String name;

	public Client(String string) {
		this.name = string;
	}

	public abstract void run() throws Exception;

	protected void copy(String... files) throws Exception {
		for (String file : files) {
			Stocks.info(file);
			File f1 = new File(parent.getParent(), file);
			File f2 = new File(parent, file);
			Stocks.copy(f1, f2);
		}
	}

	protected void createGroups(String name, String query) throws Exception {
		File vaBseNse = new File(parent, name);
		FileWriter writer = new FileWriter(vaBseNse);
		writer.write("<html><body>");

		writeHTML(writer, "A", query);
		writeHTML(writer, "B", query);
		writeHTML(writer, "T", query);
		writeHTML(writer, "S", query);
		writeHTML(writer, "Z", query);

		afterGrups(writer);

		writer.write("</body></html>");

		writer.flush();
		writer.close();
	}

	protected void afterGrups(FileWriter writer) throws Exception {

	}

	private void writeHTML(FileWriter writer, String cat, String queryName)
			throws Exception {
		writer.write("<b>GROUP " + cat + "</b><br>");
		Session session = HibernateUtil.getCurrentSession();
		Query query = session.getNamedQuery(queryName);
		query.setParameter("cat", cat);
		setDhlimit(query);
		List list = query.list();
		List<String> ra = new ArrayList<String>();
		for (Object object : list) {
			Object[] row = (Object[]) object;
			ra.add(makeString(row));
		}
		write(writer, ra);
	}

	protected void setDhlimit(Query query) {
	}

	protected String makeString(Object[] row) {
		return "";
	}

	protected void write(FileWriter writer, List<String> ra) throws Exception {
		Map<Character, Integer> nums = new HashMap<Character, Integer>();
		for (String line : ra) {
			line = line.replaceAll("<.+?>", "");// s/<.+?>//gi;
			char ch = line.charAt(0);
			if (ch < 'A') {
				ch = 'A';
			}
			ch = Character.toUpperCase(ch);
			Integer integer = nums.get(ch);
			if (integer == null) {
				integer = 0;
			}
			nums.put(ch, ++integer);
		}

		String[] grouplist = new String[26];
		Set<Entry<Character, Integer>> entrySet = nums.entrySet();
		int max = 0;
		for (Entry<Character, Integer> entry : entrySet) {
			Integer n = entry.getValue();
			max = (n > max) ? n : max;
		}

		int grpindex = 0;
		for (char n = 'A'; n != 'Z';) {
			int totno = 0;
			String totstr = "";
			char tempn = n;
			while ((totno + (nums.get(tempn) == null ? 0 : nums.get(tempn))) <= max) {
				totno += (nums.get(tempn) == null ? 0 : nums.get(tempn));
				totstr += String.valueOf(tempn) + ',';
				if (tempn == 'Z') {
					break;
				}
				++tempn;
			}
			n = tempn;
			totstr = totstr.substring(0, totstr.length() - 1);
			grouplist[grpindex++] = totstr.trim();
		}

		int grp = 0;
		String tempgroup = "";
		for (String line : ra) {
			String a = line;
			a = a.replaceAll("<.+?>", "");// s/<.+?>//gi;
			char ch = a.charAt(0);
			if (ch < 'A') {
				ch = 'A';
			}
			ch = Character.toUpperCase(ch);
			if (tempgroup.indexOf(ch) < 0) {
				if (grouplist[grp] != null) {
					writer.write("<B>" + grouplist[grp] + "</B><BR>\n");
					tempgroup = grouplist[grp];
					grp++;
				}
			}
			writer.write(line + "<BR>\n");
		}
	}

	protected void pdData(FileWriter writer) throws Exception {
		FileInputStream fstream = new FileInputStream(new File(
				parent.getParent(), Stocks.getPdFileName()));
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		int count = 0;
		while ((strLine = br.readLine()) != null) {
			String string = strLine.replaceAll("\\s{2,}", "");// s/\s{2,}//g;
			String[] split = string.split(",\\s*");
			if (split.length < 5) {
				continue;
			}
			if (split[4].isEmpty() && !split[3].isEmpty() && count < 3) {
				count++;
				writer.write("<b>" + split[3] + "</b><br>");
			}

			if (split[1].equals("EQ") && split[0].equals("N")) {
				String name = getName(split[3]);
				writer.write(name + " " + split[8] + "(" + split[4] + ")"
						+ split[10] + "<br>");
			}
		}
		in.close();
	}

	private String getName(String string) {
		string = formateName(string).replace("Ltd", "");
		if (string.length() > 15) {
			string = string.substring(0, 15);
		}
		return string;
	}

	public void process(File p) throws Exception {
		Stocks.info("Started for " + name);
		this.parent = new File(p, name);
		this.parent.mkdirs();
		run();
		makeZip();
		for (File file : this.parent.listFiles()) {
			file.delete();
		}
		this.parent.delete();
		Stocks.info("Completed " + name);
	}

	private void makeZip() throws Exception {
		byte[] buffer = new byte[18024];
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
				new File(parent.getParent(), name + ".zip")));
		out.setLevel(Deflater.DEFAULT_COMPRESSION);
		File[] files = parent.listFiles();
		for (File file : files) {
			FileInputStream in = new FileInputStream(file);
			out.putNextEntry(new ZipEntry(file.getName()));
			int len;
			while ((len = in.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}
			in.close();
			out.closeEntry();
		}
		out.close();

	}

	protected String formateName(Object object) {
		String name = object.toString();
		String[] split = name.split(" ");
		name = "";
		for (String s : split) {
			s = s.trim();
			if (s.isEmpty()) {
				continue;
			}
			String string = s.substring(1);
			name += Character.toUpperCase(s.charAt(0)) + string.toLowerCase()
					+ " ";
		}
		return name.trim();
	}

	public String getName() {
		return name;
	}
}
