package com.vimukti.stocks.core;

import java.util.Date;

public class BseData {
	private int bsecode;
	private Date date;
	private float closing;
	private float prevclose;
	private int volume;
	private float high52;
	private float low52;
	private float wavg;

	public int getBsecode() {
		return bsecode;
	}

	public void setBsecode(int bsecode) {
		this.bsecode = bsecode;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public float getClosing() {
		return closing;
	}

	public void setClosing(float closing) {
		this.closing = closing;
	}

	public float getPrevclose() {
		return prevclose;
	}

	public void setPrevclose(float prevclose) {
		this.prevclose = prevclose;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public float getHigh52() {
		return high52;
	}

	public void setHigh52(float high52) {
		this.high52 = high52;
	}

	public float getLow52() {
		return low52;
	}

	public void setLow52(float low52) {
		this.low52 = low52;
	}

	public float getWavg() {
		return wavg;
	}

	public void setWavg(float wavg) {
		this.wavg = wavg;
	}

	public BseData() {
	}

	// public static BseData getInstance(String line, String high) {
	// String[] values = line.split("#@#");
	// if (values.length < 61) {
	// return null;
	// }
	// if (!istoday(values[61].split(" ")[0])) {
	// return null;
	// }
	// BseData data = new BseData();
	// data.bsecode = Integer.parseInt(values[2].trim());
	// data.date = new Date();
	// data.closing = getFloat(getNumberStr(values[15]));
	// data.prevclose = getFloat(values[21].replaceAll(",", "").split(" / ")[0]
	// .trim());
	// String v = values[26].replaceAll(",", "").split(" / ")[0].trim();
	// double parseDouble = Double.parseDouble(v);
	// Double vl = new Double(parseDouble);
	// if (!values[25].trim().isEmpty()) {
	// vl = vl * 100000;
	// }
	// data.volume = vl.intValue();
	// data.high52 = getFloat(getNumberStr(values[53]));
	// data.low52 = getFloat(getNumberStr(values[54]));
	// data.wavg = getFloat(getNumberStr(values[22]));
	// return data;
	// }

	public static BseData getInstance(String line, String high) {
		//	0	SC_CODE		500410	
		//	1	SC_NAME		ACC LTD	
		//	2	SC_GROUP	A
		//	3	SC_TYPE		Q
		//	4	OPEN		1366.00
		//	5	HIGH		1414.70
		//	6	LOW			1366.00
		//	7	CLOSE		1389.25
		//	8	LAST		1389.25
		//	9	PREVCLOSE	1348.00
		//	10	NO_TRADES	4405
		//	11	NO_OF_SHRS	48659
		//	12	NET_TURNOV	67747213.00
		String[] values = line.split(",");

		BseData data = new BseData();
		data.bsecode = Integer.parseInt(values[0]);
		data.date = new Date();
		data.closing = getFloat(values[7]);
		data.prevclose = getFloat(values[9]);

		data.volume = Integer.parseInt(values[11]);

		String[] split = high.replaceAll("<.+?>", "\n").replaceAll("\n+", "\n")
				.split("\n");
		data.high52 = getFloat(getNumberStr(split[3].replaceAll("\\(.+", "")));
		data.low52 = getFloat(getNumberStr(split[5].replaceAll("\\(.+", "")));
		
		data.wavg = getFloat("0");
		return data;
	}

	private static float getFloat(String numberStr) {
		try {
			return Float.parseFloat(numberStr);
		} catch (Exception e) {
		}
		return 0;
	}

	private static String getNumberStr(String string) {
		return string.replaceAll(",", "").trim();
	}

	@SuppressWarnings("deprecation")
	private static boolean istoday(String string) {
		try {
			Date date1 = new Date(string);
			Date date2 = new Date();
			return (date1.getYear() == date2.getYear()
					&& date1.getMonth() == date2.getMonth() && date1.getDate() == date2
					.getDate());
		} catch (Exception e) {
			return false;
		}
	}

	public static void main(String[] args) {
		System.out.println(istoday("13 May 14"));
	}
}
