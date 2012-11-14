package com.vimukti.stocks.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class PropertyParser {

	File fileToRead;
	HashMap<String, String> keyValue = new HashMap<String, String>();

	public void loadFile(String fileName) throws FileNotFoundException,
			IOException {
		fileToRead = new File(fileName);
		FileReader fr = new FileReader(fileToRead);
		BufferedReader br = new BufferedReader(fr);
		String line;
		while (true) {
			line = br.readLine();
			if (line == null) {
				break;
			}
			line = line.trim();
			if (line.length() == 0 || line.charAt(0) == '#')
				continue;

			// String[] pair = line.split("[ \t]*=[ \t]*");
			int indexOf = line.indexOf("=");
			if (indexOf != -1) {
				String name = line.substring(0, indexOf);
				String value = line.substring(indexOf + 1);
				keyValue.put(name, value);
			}
		}
		fr.close();
	}

	public String getProperty(String prop, String def) {
		String value = get(prop);
		if (value == null) {
			return def;
		} else {
			return value;
		}
	}

	private String get(String prop) {
		return this.keyValue.get(prop);
	}

	public static void main(String[] args) {
		PropertyParser ps = new PropertyParser();
		try {
			ps.loadFile("config.ini");
		} catch (IOException e) {
			e.printStackTrace();
		}
		int i = 1;
		for (Object obj : ps.keyValue.keySet()) {
			System.out.println(i + " " + obj + ":" + ps.keyValue.get(obj));
			i++;
		}
	}

}
