package com.vimukti.stocks.server;

import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.LocalConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyServer {
	public static Server jettyServer;

	public static void start(int port) throws Exception {
		jettyServer = new Server();
		Connector connector = new SelectChannelConnector();
		connector.setPort(port);

		SessionHandler sessionHandler = new SessionHandler();

		SecurityHandler securityHandler = new ConstraintSecurityHandler();

		WebAppContext webappcontext = new WebAppContext(sessionHandler,
				securityHandler, null, null);

		webappcontext.setContextPath("/");
		LocalConnector wsConnector = new LocalConnector();
		webappcontext.setAttribute("wsConnector", wsConnector);
		// webappcontext.setWar("webapp");

		webappcontext.getSessionHandler().getSessionManager()
				.setSessionIdPathParameterName("none");

		Resource resource = Resource.newClassPathResource("war");

		webappcontext.setBaseResource(resource);

		jettyServer.setConnectors(new Connector[] { connector, wsConnector });
		webappcontext.setClassLoader(JettyServer.class.getClassLoader());

		webappcontext.setAttribute("documentDomain", "www.stocks.com");

		// for max post data
		webappcontext.getServletContext().getContextHandler()
				.setMaxFormContentSize(10000000);

		// HandlerCollection handlers = new HandlerCollection();
		// handlers.setHandlers(new Handler[] { webappcontext,
		// new DefaultHandler() });
		jettyServer.setHandler(webappcontext);
		jettyServer.start();

	}

	public static void stop() {
		try {
			jettyServer.stop();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
