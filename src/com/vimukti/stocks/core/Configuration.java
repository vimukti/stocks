package com.vimukti.stocks.core;

import org.hibernate.HibernateException;

public class Configuration extends org.hibernate.cfg.Configuration {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8637938454740435432L;

	@Override
	public org.hibernate.cfg.Configuration configure()
			throws HibernateException {
		return super.configure(getClass().getClassLoader().getResource(
				"mapping/hibernate.cfg.xml"));
	}

}
