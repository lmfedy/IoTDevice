package edu.nku.device.resource;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.security.PublicKey;
import java.util.Random;
import java.util.concurrent.Future;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import com.google.gson.Gson;

import edu.nku.device.resource.response.AccessoryCodePost;
import edu.nku.device.resource.response.CodeValidationResponse;
import edu.nku.device.resource.response.DeviceStatusResponse;
import edu.nku.device.resource.response.DiscoveryResponse;
import edu.nku.device.resource.response.Response;
import edu.nku.device.resource.response.UpdatePackagePayload;
import edu.nku.device.resource.response.UpdateSuccessResponse;
import edu.nku.device.utility.CryptoUtility;
import edu.nku.device.utility.DataUtility;
import edu.nku.device.utility.ServiceLogger;

@Path("/device")
public class IoTDevice {
	@Context
	private Application appContext;

	private static String SERVER_ADDRESS = "http://ec2-54-209-7-138.compute-1.amazonaws.com:7060/updateService/resumeUpdate/";
	private static int GLOBAL_READ_TIMEOUT = 30000;
	private static int GLOBAL_CONNECT_TIMEOUT = 10000;
	private static PublicKey SERVER_PUBLIC_KEY;

	// Discovery Service: Step 2 - Get Discovery Request message from middleware
	// Return Discovery Response message
	@GET
	@Path("/enroll")
	@Produces(MediaType.APPLICATION_JSON)
	public String enrollDevice() {
		ServiceLogger logger = ServiceLogger.getInstance();
		String deviceNumber = appContext.getProperties().get("deviceId").toString();
		logger.writeLog("Attempting to enroll device: " + deviceNumber);
		DiscoveryResponse oResponse = new DiscoveryResponse("enroll");
		DataUtility data = DataUtility.getInstance();
		oResponse.setViaModel(data.getDeviceMetadata(deviceNumber));
		data.closeConnection();

		Gson gson = new Gson();
		logger.writeLog("Enroll device: " + gson.toJson(oResponse));
		return gson.toJson(oResponse, DiscoveryResponse.class);
	}

	// Discovery Service: Step 5 - Get accessory code from middleware. Verify
	// against pre-existing key.
	// Return Code Validation Response with status
	@POST
	@Path("/codeValidation")
	@Consumes(MediaType.APPLICATION_JSON)
	public String getAccess(String pAccessCode) {
		ServiceLogger logger = ServiceLogger.getInstance();
		String deviceNumber = appContext.getProperties().get("deviceId").toString();
		CryptoUtility crypto = new CryptoUtility();
		logger.writeLog("Device: " + deviceNumber + ". Received Access Code: " + pAccessCode);
		Gson gson = new Gson();
		AccessoryCodePost codePost = gson.fromJson(pAccessCode, AccessoryCodePost.class);
		DataUtility data = DataUtility.getInstance();
		IoTDeviceModel device = data.getDeviceMetadata(deviceNumber);
		data.closeConnection();

		if (device.getEncryptionEnabled())
			SERVER_PUBLIC_KEY = crypto.inflatePublicKeyFromString(codePost.getPubkey());

		CodeValidationResponse oResponse = new CodeValidationResponse("access");
		if (!device.getAccessoryCode().equals(codePost.getAccessoryCode())) {
			oResponse.setDeviceId(device.getDeviceId());
			oResponse.setStatus(new StatusCode("ERROR", "Accessory code not valid."));
		} else {
			oResponse.setStatus(new StatusCode("ACCEPTED", "Accessory Code Accepted"));
			if (device.getEncryptionEnabled())
				oResponse.setPublicKey(crypto.getPublicKeyString());
			else
				oResponse.setPublicKey("");
			oResponse.setViaModel(device);
		}

		String responseString = gson.toJson(oResponse, CodeValidationResponse.class);

		if (device.getEncryptionEnabled() && !oResponse.getStatus().getStatus().equals("ERROR")) {
			responseString = crypto.encryptMessage(responseString,
					crypto.inflatePublicKeyFromString(codePost.getPubkey()));
		}

		logger.writeLog("Device to Service: " + responseString);
		return responseString;
	}

	// Firmware Distribution: Step 5a Payload with update package
	// Respond with Update Completion Status
	@POST
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	public String postUpdatePackage(String pPackage) {
		ServiceLogger logger = ServiceLogger.getInstance();
		String deviceNumber = appContext.getProperties().get("deviceId").toString();
		CryptoUtility crypto = new CryptoUtility();
		logger.writeLog("Device: " + deviceNumber + ". Received Update Package: " + pPackage);

		if (!pPackage.startsWith("{")) {
			try {
				pPackage = crypto.decryptMessage(pPackage, SERVER_PUBLIC_KEY);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Gson gson = new Gson();
		UpdatePackagePayload oUpdatePackage = gson.fromJson(pPackage, UpdatePackagePayload.class);

		DataUtility data = DataUtility.getInstance();
		IoTDeviceModel device = data.getDeviceMetadata(deviceNumber);
		data.closeConnection();

		UpdateSuccessResponse oResponse = new UpdateSuccessResponse("updateStatus");
		oResponse.setFirmwareVersion(oUpdatePackage.getVersion());
		oResponse.setDeviceId(deviceNumber);
		oResponse.setModelId(device.getModelId());

		StatusCode updateStatus;
		Random rand = new Random();
		int result = rand.nextInt(100);

		// Success rate
		if (result <= Integer.parseInt(appContext.getProperties().get("updateSuccess").toString()))
			updateStatus = new StatusCode("COMPLETE", "Update Complete");
		else
			updateStatus = new StatusCode("ERROR", "Update Failed");

		oResponse.setUpdateStatus(updateStatus);

		String responseString = gson.toJson(oResponse, CodeValidationResponse.class);

		if (device.getEncryptionEnabled()) {
			responseString = crypto.encryptMessage(responseString);
		}

		logger.writeLog("Device to Service: " + responseString);
		return responseString;
	}

	// Firmware Distribution: Step 4 Request for Device Status
	// Return Device Status
	@GET
	@Path("/status")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDeviceStatus() {
		int iPercentBusy = Integer.parseInt(appContext.getProperties().get("percentBusy").toString());
		int iPercentReady = Integer.parseInt(appContext.getProperties().get("percentReady").toString());
		int iPercentNoResponse = Integer.parseInt(appContext.getProperties().get("percentNoResponse").toString());

		DeviceStatusResponse oResponse = new DeviceStatusResponse("status");

		Random rand = new Random();
		int result = rand.nextInt(100);
		if (result < iPercentBusy) {
			// % Busy
			oResponse.setStatus("Busy");
			waitForReady(appContext.getProperties().get("deviceId").toString());
		} else if (result >= iPercentBusy && result < (iPercentReady + iPercentBusy)) {
			// % Ready
			oResponse.setStatus("Ready");
		}

		return oResponse;
	}

	public void waitForReady(String id) {
		ServiceLogger logger = ServiceLogger.getInstance();
		new Thread() {
			@Override
			public void run() {
				try {
					Random rand = new Random();
					int seconds = rand.nextInt(10);
					logger.writeLog("Thread Sleep - Waiting for Ready status (" + seconds + " seconds)");
					Thread.sleep(seconds * 1000);
					String serverAddress = SERVER_ADDRESS + id;

					logger.writeLog("Sending Resume Update to: " + SERVER_ADDRESS);
					ClientConfig configuration = new ClientConfig();
					configuration.property(ClientProperties.CONNECT_TIMEOUT, GLOBAL_CONNECT_TIMEOUT);
					configuration.property(ClientProperties.READ_TIMEOUT, GLOBAL_READ_TIMEOUT);
					Client client = ClientBuilder.newClient(configuration);
					final AsyncInvoker asyncInvoker = client.target(serverAddress).request(MediaType.APPLICATION_JSON)
							.async();
					final Future<javax.ws.rs.core.Response> responseFuture = asyncInvoker
							.get(new InvocationCallback<javax.ws.rs.core.Response>() {
						@Override
						public void completed(javax.ws.rs.core.Response response) {
							if (response.getStatus() != 200) {
								logger.writeLog("Resume update call failed with status code " + response.getStatus());
							} else {
								logger.writeLog("Resume update call success");
							}
						}

						@Override
						public void failed(Throwable throwable) {
							logger.writeLog("Resume update call failed with exception: " + throwable.getMessage());
							throwable.printStackTrace();
						}

					});
				} catch (Exception e) {
					logger.writeLog("Error - waitForReady thread");
					e.printStackTrace();
				}
			}
		}.start();
	}

}
