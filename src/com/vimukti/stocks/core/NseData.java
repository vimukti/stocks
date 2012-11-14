package com.vimukti.stocks.core;

public class NseData {
	private long id;
	private String nsecode;
	private float closePrice;
	private float prevClPr;
	private int volume;
	private float hi52wk;
	private float lo52wk;

	public String getNsecode() {
		return nsecode;
	}

	public void setNsecode(String nsecode) {
		this.nsecode = nsecode;
	}

	public float getClosePrice() {
		return closePrice;
	}

	public void setClosePrice(float closePrice) {
		this.closePrice = closePrice;
	}

	public float getPrevClPr() {
		return prevClPr;
	}

	public void setPrevClPr(float prevClPr) {
		this.prevClPr = prevClPr;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public float getHi52wk() {
		return hi52wk;
	}

	public void setHi52wk(float hi52wk) {
		this.hi52wk = hi52wk;
	}

	public float getLo52wk() {
		return lo52wk;
	}

	public void setLo52wk(float lo52wk) {
		this.lo52wk = lo52wk;
	}

	public NseData() {

	}

	public NseData(String nsecode, float closePrice, float prevClPr,
			int volume, float hi52wk, float lo52wk) {
		this.nsecode = nsecode;
		this.closePrice = closePrice;
		this.prevClPr = prevClPr;
		this.volume = volume;
		this.hi52wk = hi52wk;
		this.lo52wk = lo52wk;

	}

	public static NseData getInstance(String line) {
		String[] split = line.split(",");
		if (split.length > 5
				&& (split[1].trim().equals("EQ") || split[1].trim()
						.equals("BE")) && split[0].trim().equals("N")) {
			return new NseData(split[2].trim(), Float.parseFloat(split[8]
					.trim()), Float.parseFloat(split[4].trim()),
					Integer.parseInt(split[10].trim()),
					Float.parseFloat(split[14].trim()),
					Float.parseFloat(split[15].trim()));
		}
		return null;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
