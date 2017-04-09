package edu.nku.device.server;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import edu.nku.device.resource.IoTDevice;
import edu.nku.device.utility.ServiceLogger;

public class DeviceServer {
	private static final int DEFAULT_PORT = 8081;
	private ServiceLogger logger;
	private int serverPort;

	public DeviceServer(int serverPort, ServiceLogger logger) throws Exception {
		this.serverPort = serverPort;

		Server server = configureServer(logger);
		server.start();
		server.join();
	}

	private Server configureServer(ServiceLogger logger) throws NoSuchAlgorithmException, NoSuchProviderException {
		
		Map<String, Object> oPropertyMap = new HashMap<>();
		oPropertyMap.put("deviceID", serverPort);
		
		ResourceConfig resourceConfig = new ResourceConfig();
		resourceConfig.packages(IoTDevice.class.getPackage().getName());
		resourceConfig.register(JacksonFeature.class);
		resourceConfig.setProperties(oPropertyMap);		
		
		//TODO: INSERT Vendor, Port, URL & Public Key into SQL Table
		// TODO: Randomize models/version to put into SQL Table
		
		ServletContainer servletContainer = new ServletContainer(resourceConfig);
		ServletHolder sh = new ServletHolder(servletContainer);
		Server server = new Server(serverPort);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.addServlet(sh, "/*");
		server.setHandler(context);
		return server;
	}

	public static void main(String[] args) throws Exception {
		int serverPort = DEFAULT_PORT;
		ServiceLogger logger =  ServiceLogger.getInstance();
		if (args.length >= 1) {
			try {
				serverPort = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

		new DeviceServer(serverPort, logger);
	}

}