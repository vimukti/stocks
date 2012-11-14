package com.vimukti.stocks.server;

import org.hibernate.Session;

import com.vimukti.stocks.core.HibernateUtil;
import com.vimukti.stocks.mail.EmailManager;

public class ServerMain {
	private static String userName;
	private static String password;
	private static int port;
	private static String filepath;

	private static PropertyParser prop;

	public static void main(String[] args) throws Exception {
		loadProperties(args);
		EmailManager.getInstance().start();
		Session openSession = HibernateUtil.openSession();
		JettyServer.start(port);
		System.out.println("Server started at 0.0.0.0:" + port);
		openSession.close();
	}

	private static void loadProperties(String[] args) throws Exception {
		String configPath = "";
		if (args.length > 1) {
			configPath = args[0];
		} else {
			configPath = "config/config.ini";
		}
		prop = new PropertyParser();
		prop.loadFile(configPath);
		userName = prop.getProperty("username", "");
		password = prop.getProperty("password", "");
		port = Integer.parseInt(prop.getProperty("port", "8080"));
		filepath = prop.getProperty("filepath", "./war/stocks");
		System.setProperty("db.user", prop.getProperty("db_username", ""));
		System.setProperty("db.pass", prop.getProperty("db_password", ""));

	}

	public static String getUserName() {
		return userName;
	}

	public static String getPassword() {
		return password;
	}

	public static String getFilepath() {
		return filepath;
	}

	public static String getProperty(String name) {
		return getProperty(name, "");
	}

	public static String getProperty(String name, String def) {
		return prop.getProperty(name, def);
	}
}
