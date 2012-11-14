package com.vimukti.stocks;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MutualFunds extends Thread {
	private final static int size = 1024;

	private File parent;
	private String date;
	private String name;

	private Map<String, String> global = new HashMap<String, String>();
	private Map<String, List<String>> replacement = new HashMap<String, List<String>>();
	private Map<String, Integer> company = new HashMap<String, Integer>();
	private Map<Integer, List<String>> names = new HashMap<Integer, List<String>>();

	public MutualFunds(File parent, String date) {
		this.parent = new File(parent, "mf");
		this.parent.mkdir();
		this.date = date;
	}

	public void run() {
		try {

			for (File f : parent.listFiles()) {
				if (f.getName().endsWith(".zip")) {
					f.delete();
				}
			}

			download();

			globalReplacements();

			individualReplaceMents();

			readCompanies();

			makeTempCSV();

			makeFinal();

			makeZip();

			delete();
		} catch (Exception e) {
			e.printStackTrace();
			Stocks.info("Got exception in MF");
		}
	}

	private Map<String, Boolean> getUnwantedData() throws Exception {
		File unwantedFile = new File(parent, "unwanted.txt");
		FileInputStream stream = new FileInputStream(unwantedFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		String line;
		Map<String, Boolean> unwanted = new HashMap<String, Boolean>();
		while ((line = br.readLine()) != null) {
			unwanted.put(line, true);
		}
		return unwanted;
	}

	private void makeTempCSV() throws Exception {
		Stocks.info("Making Temp CSV");
		// foreach $name (keys %company) {
		// $revcompany{$company{$name}}=$name;
		// }
		Map<Integer, String> revcompany = new HashMap<Integer, String>();

		for (Entry<String, Integer> entry : company.entrySet()) {
			revcompany.put(entry.getValue(), entry.getKey());
		}

		// temp.csv
		File vaBseNse = new File(parent, "temp.csv");
		FileWriter writer = new FileWriter(vaBseNse);
		for (int i = 0; i < 50; i++) {
			String name = revcompany.get(i);
			if (name == null) {
				continue;
			}

			List<String> array = names.get(i);
			if (array == null) {
				array = new ArrayList<String>();
			}
			writer.write('\n' + name + '\n');

			List<String> reps = replacement.get(name);
			if (reps == null) {
				reps = new ArrayList<String>();
			}
			for (String line : array) {
				String[] split = line.split(";");
				if (split.length != 5) {
					continue;
				}

				String shortname = split[0];
				String nav = split[1];
				String rpp = split[2];
				String spp = split[3];

				for (String rep : reps) {
					// $shortname =~ s/\s*$temp\s*//gi;
					shortname = shortname.replaceAll("(?i)\\s*" + rep, "");
				}

				for (Entry<String, String> entry : global.entrySet()) {
					shortname = shortname.replaceAll("(?i)" + entry.getKey(),
							entry.getValue());
				}

				// $shortname =~ s/^\s+//g;
				shortname = shortname.replaceAll("^\\s+", "");
				// $shortname =~ s/\s+/ /g;
				shortname = shortname.replaceAll("\\s+", " ");
				// $shortname =~ s/(.{4,})\s*-\s*\(*\1\)*/\1/g;
				shortname = shortname.replaceAll("(.{4,})\\s*-\\s*\\(*\\1\\)*",
						"\\1");

				// printf OUT "%s,%.2f,%.2f,%.2f\n",$shortname,$nav,$rpp,$spp;
				writer.write(shortname + ',' + parseDouble(nav) + ','
						+ parseDouble(rpp) + ',' + parseDouble(spp) + '\n');
			}
		}
		writer.flush();
		writer.close();
		Stocks.info("Temp csv completed");
	}

	private void readCompanies() throws Exception {
		Stocks.info("Reading Companies");

		// amfinavdownload.txt
		FileInputStream stream = new FileInputStream(new File(parent,
				"amfinavdownload.txt"));
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		int no = 1;
		Integer index = 0;
		String strLine;
		while ((strLine = br.readLine()) != null) {
			strLine = strLine.trim();
			if (strLine.isEmpty()) {
				continue;
			}
			if (strLine.contains("Open")) {
				continue;
			}

			if (strLine.contains("Close Ended")) {
				continue;
			}

			// $line =~ s/;(.+?);(.+?);/;/;
			String[] split = strLine.split(";");
			if (split.length > 1) {
				strLine = strLine.replace(
						";" + split[1] + ";" + split[2] + ";", ";");
				// $line =~ s/(.+?);(.+)/$2/;
				split = strLine.split(";");
				if (split.length > 1) {
					strLine = strLine.replace(split[0] + ";", "");
				}
			}

			// next if($line =~ /^[\s\d]/);
			char charAt = strLine.charAt(0);
			if (charAt == ' ' || Character.isDigit(charAt)) {
				continue;
			}

			if (!strLine.contains(";")) {
				index = company.get(strLine);
				if (index == null || index < 1) {
					index = no;
					company.put(strLine, no++);
				}
			} else {
				if (!strLine.contains(date)) {
					continue;
				}

				strLine = strLine.replaceAll("- ", "-");
				strLine = strLine.replaceAll(" -", "-");

				// push(@{$names{$index}},$line);
				List<String> list = names.get(index);
				if (list == null) {
					list = new ArrayList<String>();
					names.put(index, list);
				}
				list.add(strLine);
			}
		}
		stream.close();
	}

	private void individualReplaceMents() throws Exception {
		Stocks.info("Individual Replacements");

		// data1.txt
		FileInputStream stream = new FileInputStream(new File(parent,
				"data1.txt"));
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		String strLine = null;
		replacement.clear();
		while ((strLine = br.readLine()) != null) {
			String[] split = strLine.split(",");
			if (split.length > 1) {
				for (int i = 1; i < split.length; i++) {
					List<String> list = replacement.get(split[0]);
					if (list == null) {
						list = new ArrayList<String>();
						replacement.put(split[0], list);
					}
					list.add(split[i]);
				}
			}
		}
		stream.close();
	}

	private void globalReplacements() throws Exception {
		Stocks.info("Global Replacements");

		// data2.txt
		FileInputStream stream = new FileInputStream(new File(parent,
				"data2.txt"));
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		String strLine;
		global.clear();
		while ((strLine = br.readLine()) != null) {
			String[] split = strLine.split("\t");
			if (split.length > 1) {
				global.put(split[0], split[1]);
			} else {
				global.put(split[0], "");
			}
		}
		stream.close();
	}

	private void delete() {
		new File(parent, "temp.csv").delete();
		new File(parent, "amfinavdownload.txt").delete();
		File file = new File(parent, name);
		for (File f : file.listFiles()) {
			f.delete();
		}
		file.delete();
	}

	private void makeZip() throws Exception {
		Stocks.makeZip(parent, name);
	}

	private void download() {
		Stocks.info("Downloading amfinavdownload.txt");
		OutputStream outStream = null;
		URLConnection uCon = null;

		InputStream is = null;
		try {
			URL Url;
			byte[] buf;
			int byteRead = 0;
			Url = new URL("http://amfiindia.com/NavReport.aspx?type=0");
			outStream = new BufferedOutputStream(new FileOutputStream(new File(
					parent, "amfinavdownload.txt")));

			uCon = Url.openConnection();
			is = uCon.getInputStream();
			buf = new byte[size];
			while ((byteRead = is.read(buf)) != -1) {
				outStream.write(buf, 0, byteRead);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
				outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Stocks.info("amfinavdownload Completed.");
		}
	}

	private void makeFinal() throws Exception {
		name = "MF" + Stocks.getDate();

		File parent = new File(this.parent, name);
		parent.mkdir();
		Stocks.info("Making final CSV");

		File csv = new File(parent, name + ".csv");
		FileWriter csvWriter = new FileWriter(csv);

		File txt = new File(parent, name + ".txt");
		FileWriter txtWriter = new FileWriter(txt);

		Map<String, Boolean> unwanted = getUnwantedData();

		FileInputStream stream = new FileInputStream(new File(this.parent,
				"temp.csv"));
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		String line;
		boolean wanted = true;
		while ((line = br.readLine()) != null) {
			line = line.replaceAll(",", "\t");
			if (line.trim().isEmpty()) {
				continue;
			}
			String[] words = line.split("\t");
			String w = words.length > 2 ? words[3] : "0";
			line = line.replaceAll("- ", "-");
			line = line.replaceAll(" -", "-");

			if (!isZero(w)) {
				if (unwanted.get(line) != null) {
					wanted = false;
				}
				if (line.isEmpty()) {
					wanted = true;
				}
				if (wanted) {
					out(csvWriter, txtWriter, line + '\n');
				}
			}

			if (line.split("\\d").length == 1) {
				wanted = true;
				if (line.startsWith("UTI-I")) {
					line = "UTI MUTUAL FUND";
				}
				if (unwanted.get(line) != null) {
					wanted = false;
				}
				if (line.isEmpty()) {
					wanted = true;
				}
				if (wanted) {
					out(csvWriter, txtWriter, '\n' + line + '\n');
				}
			}
		}
		csvWriter.flush();
		csvWriter.close();
		txtWriter.flush();
		txtWriter.close();
		Stocks.info("Final CSV completed");
	}

	private static boolean isZero(String w) {
		double parseDouble = Double.parseDouble(w);
		return ((int) parseDouble) == 0;
	}

	private void out(Writer csvW, Writer txtW, String line) throws Exception {
		if (!line.toLowerCase().contains("gilt")) {
			if (!line.contains("Gov")) {
				if (!line.contains("Flo.Rate")) {
					txtW.write(line);
					String[] words = line.split("\t");
					line = line.replace(words[0], '"' + words[0] + '"');
					csvW.write(line);
				}
			}
		}
	}

	private static String parseDouble(String spp) {
		try {
			double parseDouble = Double.parseDouble(spp);
			return String.valueOf(Math.round(parseDouble * 100) / 100.0);
		} catch (Exception e) {
			return "0.0";
		}
	}
}
