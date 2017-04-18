package edu.nku.device.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import com.google.gson.Gson;

import edu.nku.device.resource.IoTDevice;
import edu.nku.device.resource.IoTDeviceModel;
import edu.nku.device.utility.CryptoUtility;
import edu.nku.device.utility.DataUtility;
import edu.nku.device.utility.ServiceLogger;

public class DeviceServer {
	private static final int DEFAULT_PORT = 8092;
	private int serverPort;

	public DeviceServer(int serverPort) throws Exception {
		this.serverPort = serverPort;

		Server server = configureServer();
		server.start();
		server.join();
	}

	private Server configureServer() throws NoSuchAlgorithmException, NoSuchProviderException {

		ServiceLogger logger = ServiceLogger.getInstance();
		DataUtility data = DataUtility.getInstance();
		CryptoUtility crypto = new CryptoUtility(data, logger);

		Properties prop = new Properties();
		try {
			prop.load(getClass().getResourceAsStream("/config.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		IoTDeviceModel device = new IoTDeviceModel(serverPort + "");

		// Randomize Encryption Enabled
		Random rand = new Random();
		int result = rand.nextInt(100);

		// 50% chance encryption enabled
		boolean encryptionEnabled = result <= Integer.parseInt(prop.getProperty("encryptionEnabled"));
		device.setEncryptionEnabled(encryptionEnabled);

		// Randomize Device Vendor (Ports Firmware Sites are running on)
		result = rand.nextInt(Integer.parseInt(prop.getProperty("numberVendors")));
		device.setVendor((8080 + result) + "");

		// Randomize Device Model (1-10)
		result = rand.nextInt(100);
		device.setModelId(result % Integer.parseInt(prop.getProperty("numberModels")));

		// Randomize Current Firmware Version (1-50??)
		result = rand.nextInt(Integer.parseInt(prop.getProperty("firmwareVersionStart")));
		device.setFirmwareVersion(result);

		// Randomize Device Name: Device_XYZ
		result = rand.nextInt(26);
		String productSuffix = String.valueOf((char) (result + 64));
		result = rand.nextInt(26);
		productSuffix += String.valueOf((char) (result + 64));
		result = rand.nextInt(26);
		productSuffix += String.valueOf((char) (result + 64));
		device.setProductName("Device_" + productSuffix);

		Gson gson = new Gson();
		logger.writeLog("Device Created: " + gson.toJson(device, IoTDeviceModel.class));

		data.storeDeviceMetadata(device);

		Map<String, Object> oPropertyMap = new HashMap<>();
		oPropertyMap.put("deviceId", serverPort);
		oPropertyMap.put("percentBusy", prop.getProperty("percentBusy"));
		oPropertyMap.put("percentReady", prop.getProperty("percentReady"));
		oPropertyMap.put("percentNoResponse", prop.getProperty("percentNotReady"));
		oPropertyMap.put("updateSuccess", prop.getProperty("updateSuccess"));
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

		switch (args.length) {
		case 1:
			try {
				serverPort = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			break;
		}

		new DeviceServer(serverPort);
	}

}