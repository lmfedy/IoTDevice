package edu.nku.device.server;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import edu.nku.device.resource.IoTDevice;
import edu.nku.device.resource.IoTDeviceModel;
import edu.nku.device.utility.CryptoUtility;
import edu.nku.device.utility.DataUtility;
import edu.nku.device.utility.ServiceLogger;

public class DeviceServer {
	private static final int DEFAULT_PORT = 8081;
	private static final int DEFAULT_VENDORS = 1;
	private int serverPort;
	private int numberVendors;

	public DeviceServer(int serverPort, int numberVendors) throws Exception {
		this.serverPort = serverPort;
		this.numberVendors = numberVendors;

		Server server = configureServer();
		server.start();
		server.join();
	}

	private Server configureServer() throws NoSuchAlgorithmException, NoSuchProviderException {

		ServiceLogger logger = ServiceLogger.getInstance();
		DataUtility data = DataUtility.getInstance();
		CryptoUtility crypto = new CryptoUtility(data, logger);

		IoTDeviceModel device = new IoTDeviceModel(serverPort + "");

		// Randomize Encryption Enabled
		Random rand = new Random();
		int result = rand.nextInt(100);
		boolean encryptionEnabled = result % 2 == 0;
		device.setEncryptionEnabled(encryptionEnabled);

		// Randomize Device Vendor (Ports Firmware Sites are running on)
		result = rand.nextInt(numberVendors);
		device.setVendor((8080 + result) + "");

		// Randomize Device Model (1-10)
		result = rand.nextInt(100);
		device.setModelId(result % 10);

		// Randomize Current Firmware Version (1-50??)
		result = rand.nextInt(25);
		device.setFirmwareVersion(result);

		result = rand.nextInt(26);
		String productSuffix = String.valueOf((char) (result + 64));
		result = rand.nextInt(26);
		productSuffix += String.valueOf((char) (result + 64));
		result = rand.nextInt(26);
		productSuffix += String.valueOf((char) (result + 64));

		device.setProductName("Device_" + productSuffix);
		device.setAccessoryCode("1234-ABCD");

		data.storeDeviceMetadata(device);

		Map<String, Object> oPropertyMap = new HashMap<>();
		oPropertyMap.put("deviceId", serverPort);
		oPropertyMap.put("percentBusy", 35);
		oPropertyMap.put("percentReady", 60);
		oPropertyMap.put("percentNoResponse", 5);
		oPropertyMap.put("CryptoUtility", crypto);

		ResourceConfig resourceConfig = new ResourceConfig();
		resourceConfig.packages(IoTDevice.class.getPackage().getName());
		resourceConfig.register(JacksonFeature.class);
		resourceConfig.setProperties(oPropertyMap);

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
		int numberVendors = DEFAULT_VENDORS;

		switch (args.length) {
		case 1:
			try {
				serverPort = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			break;
		case 2:
			try {
				serverPort = Integer.parseInt(args[0]);
				numberVendors = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			break;
		}

		new DeviceServer(serverPort, numberVendors);
	}

}