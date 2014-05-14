package com.vimukti.stocks;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.vimukti.stocks.client.AndraJyothi;
import com.vimukti.stocks.client.AndraPrabha;
import com.vimukti.stocks.client.Client;
import com.vimukti.stocks.client.Dh;
import com.vimukti.stocks.client.Vartha;
import com.vimukti.stocks.core.BseData;
import com.vimukti.stocks.core.HibernateUtil;
import com.vimukti.stocks.core.NseData;

public class Stocks {
	private final static int size = 1024;
	private static final int THREADS = 50;
	private File parent;
	private static String status = "";
	private int totalDataFailed;

	public Stocks(File parent) {
		this.parent = parent;
	}

	public static List<String> createZipFeils(final File parent)
			throws Exception {
		info("Started....:" + new Date());
		parent.mkdirs();
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				Stocks stocks = new Stocks(parent);
				try {
					stocks.run();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
		thread.join();

		Session openSession = HibernateUtil.openSession();
		List<String> list = new ArrayList<String>();
		List<Client> clients = new ArrayList<Client>();
		clients.add(new Vartha());
		clients.add(new Dh());
		clients.add(new AndraJyothi());
		clients.add(new AndraPrabha());

		for (Client client : clients) {
			try {
				client.process(parent);
				list.add(client.getName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Stocks.copy(new File(parent, "andhra_jyothy.zip"), new File(parent,
				"nt.zip"));
		list.add("nt");
		deleteFiles(parent);
		openSession.close();
		info("Completed....:" + new Date());
		return list;
	}

	public synchronized static void info(String string) {
		setStatus(string);
		System.out.println(string);
	}

	public static void setStatus(String s) {
		synchronized (status) {
			status = s;
		}
	}

	public static String getStatus() {
		synchronized (status) {
			return status;
		}
	}

	public void run() throws Exception {
		Session openSession = HibernateUtil.openSession();
		try {
			getNseData();
			getBseData();
			bsegl();
			advDec();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			openSession.close();
		}
	}

	public static void main(String[] args) throws Exception {
		createZipFeils(new File("c:/naga"));
	}

	private void advDec() throws Exception {
		info("Creating adv_dec.txt");
		URL url = new URL("http://in.finance.yahoo.com/advances");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.connect();
		if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String data = "";
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				data += inputLine;
			}
			in.close();

			data = data.replaceAll("\n", "");// s/\n//gs
			data = data.replaceAll("<table", "\n<table");// s/<table/\n<table/gs;
			String[] split = data.split("\n");
			for (String line : split) {
				if (line.contains("Advancing Issues")) {
					data = line;
					break;
				}
			}
			data = data.replaceAll("<tr", "\n<tr");// $content =~
													// s/<tr/\n<tr/gs;
			data = data.replaceAll("<.+?>", ":");// $content =~ s/<.+?>/:/gs;
			data = data.replaceAll(":+", "\t");// $content =~ s/:+/\t/gs;
			data = data.replaceAll("\n\t", "\n");// $content =~ s/\n\t/\n/gs;
			split = data.split("\n");
			File vaBseNse = new File(parent, "adv_dec.txt");
			FileWriter writer = new FileWriter(vaBseNse);
			for (String line : split) {
				String[] cols = line.split("\t");
				writer.write("\t" + cols[0] + "\t" + cols[1] + "\n");
			}
			writer.flush();
			writer.close();
		}
		info("Completed adv_dec.txt");
	}

	private void bsegl() throws Exception {
		info("Creating bsegl.txt");
		File vaBseNse = new File(parent, "bsegl.txt");
		FileWriter writer = new FileWriter(vaBseNse);
		writer.write("BSE Gainers Sensex\n\n");

		createBsegl(writer, "bsegl.topgains");
		createBsegl(writer, "bsegl.toploses");

		writer.write("\nBSE Gainers Non-Sensex\n\n");
		createBsegl(writer, "bsegl.topgain");
		createBsegl(writer, "bsegl.toplose");

		writer.flush();
		writer.close();
		info("Completed bsegl.txt");
	}

	private void createBsegl(FileWriter writer, String queryName)
			throws Exception {
		Session session = HibernateUtil.getCurrentSession();
		Query query = session.getNamedQuery(queryName);
		writer.write("\nCompany Name	Closing Price	Prev Close	%Change\n");
		List list = query.list();
		for (Object object : list) {
			Object[] row = (Object[]) object;
			if (row[1] == null) {
				continue;
			}
			writer.write(row[0] + "\t" + Double.parseDouble(row[1].toString())
					+ "\t" + Double.parseDouble(row[2].toString()) + "\t"
					+ getTwoDecimal(row[3]) + "\n");
		}
	}

	private String getTwoDecimal(Object object) {
		String str = object.toString();
		double d = Math.round(Double.parseDouble(str) * 100.0) / 100.0;
		return String.valueOf(d);
	}

	private List<Integer> companyBseCodes;
	private Map<Integer, Integer> requestCount;
	private int index;
	private int requestFailed;
	private int requestRetryFailed;
	private HashMap<String, Long> volumes;

	private void getBseData() {
		info("Downloading BseData started");
		Session session = HibernateUtil.getCurrentSession();
		Transaction transaction = session.beginTransaction();
		session.getNamedQuery("delete.bsedata").executeUpdate();
		transaction.commit();
		List list = session.getNamedQuery("get.all.company.bsecodes").list();
		try {
			volumes = getVolumes();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		companyBseCodes = list;
		requestCount = new HashMap<Integer, Integer>();
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < THREADS; i++) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					Session session2 = HibernateUtil.openSession();
					Transaction beginTransaction = session2.beginTransaction();
					HttpClient client = new HttpClient();
					try {
						String code = getCode();
						while (code != null) {
							if (!volumes.containsKey(code)) {
								code = getCode();
								continue;
							}
							try {
								makeRequest(code, client);
							} catch (Exception e) {
								e.printStackTrace();
							}
							code = getCode();
						}
						beginTransaction.commit();
					} catch (Exception e) {
					} finally {
						if (session2.isOpen()) {
							session2.close();
						}
					}
				}
			});
			thread.start();
			threads.add(thread);
		}
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		info("Total failed:" + totalDataFailed);
		info("Total Request failed:" + requestFailed);
		info("Total Request Retry failed:" + requestRetryFailed);
		info("Downloading BseData completed");
	}

	private HashMap<String, Long> getVolumes() throws HttpException,
			IOException {
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(
				"http://www.bseindia.com/download/BhavCopy/Equity/EQ"
						+ getDate() + "_CSV.ZIP");
		method.addRequestHeader(
				"User-Agent",
				"Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.10) Gecko/20100915 Ubuntu/9.04 (jaunty) Firefox/3.6.10");
		int status = client.executeMethod(method);
		Session session = HibernateUtil.getCurrentSession();
		InputStream inputStream = method.getResponseBodyAsStream();
		ZipInputStream zis = new ZipInputStream(inputStream);
		ZipEntry ze = zis.getNextEntry();

		HashMap<String, Long> map = new HashMap<String, Long>();
		while (ze != null) {
			byte[] data = new byte[(int) ze.getSize()];
			int remaining = data.length;
			while (remaining != 0) {
				remaining -= zis.read(data, data.length - remaining, remaining);
			}
			String[] split = new String(data).split("\n");
			int count = 0;
			for (String l : split) {
				if (count == 0) {
					count++;
					continue;
				}
				String[] vals = l.split(",");
				map.put(vals[0], Long.valueOf(vals[11]));
			}
			return map;
		}
		return null;
	}

	protected synchronized String getCode() {
		if (index >= companyBseCodes.size()) {
			return null;
		}
		Integer integer = companyBseCodes.get(index++);
		Integer count = requestCount.get(integer);
		if (count == null) {
			count = 0;
		}
		count++;
		requestCount.put(integer, count);

		info("Requesting bsedata :" + integer + ":" + index);

		return String.valueOf(integer);
	}

	protected synchronized void addCode(String code) {
		Integer c = Integer.valueOf(code);
		Integer count = requestCount.get(c);
		if (count < 5) {
			companyBseCodes.add(c);
		} else {
			requestRetryFailed++;
		}
	}

	private void makeRequest(String code, HttpClient client) throws Exception {
		GetMethod method = new GetMethod(
				"http://www.bseindia.com/stock-share-price/SiteCache/EQHeaderData.aspx?text="
						+ code);
		// GetMethod method = new GetMethod(
		// "http://www.bseindia.com/bseplus/StockReach/AdvStockReach.aspx?scripcode="
		// + code
		// + "&section=tab1&IsPF=undefined&random=0.6778654475327867");
		method.addRequestHeader("Referer",
				"http://www.bseindia.com/bseplus/StockReach/AdvanceStockReach.aspx?scripcode="
						+ code);
		method.addRequestHeader(
				"User-Agent",
				"Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.10) Gecko/20100915 Ubuntu/9.04 (jaunty) Firefox/3.6.10");

		int status = client.executeMethod(method);
		Session session = HibernateUtil.getCurrentSession();
		InputStream inputStream = method.getResponseBodyAsStream();
		byte[] data = new byte[inputStream.available()];
		inputStream.read(data);
		String string = new String(data);
		String high = get52High(code, client);
		info("Data (" + code + ")" + string);
		if (status == HttpURLConnection.HTTP_OK) {
			info("Request:Ok");
			BseData bseData = BseData.getInstance(string, high, code);
			if (bseData != null) {
				info("Status:Ok");
				Long val = volumes.get(code);
				if (val == null) {
					val = 0l;
				}
				bseData.setVolume(val.intValue());
				session.save(bseData);
			} else {
				info("Status:Fail");
				totalDataFailed++;
			}
		} else {
			addCode(code);
			info("Request:" + status);
			requestFailed++;
		}
	}

	private String get52High(String code, HttpClient client)
			throws HttpException, IOException {
		GetMethod method = new GetMethod(
				"http://www.bseindia.com/stock-share-price/SiteCache/52WeekHigh.aspx?Type=EQ&text="
						+ code);
		// GetMethod method = new GetMethod(
		// "http://www.bseindia.com/bseplus/StockReach/AdvStockReach.aspx?scripcode="
		// + code
		// + "&section=tab1&IsPF=undefined&random=0.6778654475327867");
		method.addRequestHeader("Referer",
				"http://www.bseindia.com/bseplus/StockReach/AdvanceStockReach.aspx?scripcode="
						+ code);
		method.addRequestHeader(
				"User-Agent",
				"Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.10) Gecko/20100915 Ubuntu/9.04 (jaunty) Firefox/3.6.10");

		int status = client.executeMethod(method);
		Session session = HibernateUtil.getCurrentSession();
		InputStream inputStream = method.getResponseBodyAsStream();
		byte[] data = new byte[inputStream.available()];
		inputStream.read(data);
		String string = new String(data);
		return string;
	}

	private void getNseData() throws Exception {
		download();
		extract();
		loadData();
	}

	private void loadData() throws Exception {
		info("Loading NseData to DB");
		FileInputStream fstream = new FileInputStream(new File(parent,
				Stocks.getPdFileName()));
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		Session session = HibernateUtil.getCurrentSession();
		Transaction transaction = session.beginTransaction();
		while ((strLine = br.readLine()) != null) {
			NseData data = NseData.getInstance(strLine);
			if (data == null) {
				continue;
			}
			info("Got NseData for:" + data.getNsecode());
			session.save(data);
		}
		in.close();
		transaction.commit();
		info("Completed");
	}

	private void extract() throws Exception {
		BufferedOutputStream out = null;
		ZipInputStream in = new ZipInputStream(new BufferedInputStream(
				new FileInputStream(new File(parent, "PR.zip"))));
		ZipEntry entry;
		String pdFileName = Stocks.getPdFileName();
		info("Extracting " + pdFileName);
		boolean found = false;
		while ((entry = in.getNextEntry()) != null) {
			if (!entry.getName().equals(pdFileName)) {
				continue;
			}
			found = true;
			int count;
			byte data[] = new byte[1000];
			out = new BufferedOutputStream(new FileOutputStream(new File(
					parent, pdFileName)), 1000);
			while ((count = in.read(data, 0, 1000)) != -1) {
				out.write(data, 0, count);
			}
			out.flush();
			out.close();
			break;
		}
		in.close();
		if (found) {
			info("Found file:" + pdFileName);
			Session session = HibernateUtil.getCurrentSession();
			Transaction transaction = session.beginTransaction();
			session.getNamedQuery("delete.nsedata").executeUpdate();
			transaction.commit();
			info("NseData deleted");

		} else {
			info(pdFileName + " File not found");
		}
		info("Completed extracting " + pdFileName);
	}

	private void download() {
		info("Downloading PR.zip");
		OutputStream outStream = null;
		URLConnection uCon = null;

		InputStream is = null;
		try {
			URL Url;
			byte[] buf;
			int byteRead = 0;
			Url = new URL("http://www.nseindia.com/content/equities/PR.zip");
			outStream = new BufferedOutputStream(new FileOutputStream(new File(
					parent, "PR.zip")));

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
			info("Completed.");
		}
	}

	public static void deleteFiles(File parent) {
		new File(parent, "adv_dec.txt").delete();
		new File(parent, "bsegl.txt").delete();
		new File(parent, "PR.zip").delete();
		new File(parent, Stocks.getPdFileName()).delete();
	}

	public static void copy(File src, File dest) throws Exception {
		InputStream in = new FileInputStream(src);

		OutputStream out = new FileOutputStream(dest);

		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	public static String getPdFileName() {
		String date = getDate();
		return "Pd" + date + ".csv";
	}

	public static String getDate() {
		Date date = new Date();
		int day = date.getDate();
		int month = date.getMonth() + 1;
		int year = date.getYear() - 100;
		return getNum(day) + getNum(month) + getNum(year);
	}

	private static String getNum(int i) {
		String s = "";
		if (i < 10) {
			s += "0";
		}
		s += i;
		return s;
	}

	public static String getDateStr() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		return dateFormat.format(new Date());
	}

	public static void makeZip(File parent, String name) throws Exception {
		byte[] buffer = new byte[18024];
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
				new File(parent, name + ".zip")));
		out.setLevel(Deflater.DEFAULT_COMPRESSION);
		File[] files = new File(parent, name).listFiles();
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

	private static int dhlimit;

	public static void setDHLimit(int dhlimit) {
		Stocks.dhlimit = dhlimit;
	}

	public static int getDhlimit() {
		return dhlimit;
	}
}
