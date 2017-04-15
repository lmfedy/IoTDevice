package edu.nku.device.resource;

import java.util.Random;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;

import edu.nku.device.resource.response.AccessoryCodePost;
import edu.nku.device.resource.response.CodeValidationResponse;
import edu.nku.device.resource.response.DeviceStatusResponse;
import edu.nku.device.resource.response.DiscoveryResponse;
import edu.nku.device.resource.response.Response;
import edu.nku.device.utility.CryptoUtility;
import edu.nku.device.utility.DataUtility;
import edu.nku.device.utility.ServiceLogger;

@Path("/device")
public class IoTDevice {
	@Context
	private Application appContext;

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

		CodeValidationResponse oResponse = new CodeValidationResponse("access");
		if (!device.getAccessoryCode().equals(codePost.getAccessoryCode())) {
			oResponse.setDeviceId(device.getDeviceId());
			oResponse.setStatus(new StatusCode("ERROR", "Accessory code not valid."));
		} else {
			oResponse.setStatus(new StatusCode("ACCEPTED", "Accessory Code Accepted"));
			oResponse.setPublicKey(crypto.getPublicKeyString());
			oResponse.setViaModel(device);
		}

		String responseString = gson.toJson(oResponse, CodeValidationResponse.class);

		if (device.getEncryptionEnabled() && !oResponse.getStatus().getStatus().equals("ERROR")) {
			responseString = crypto.encryptMessage(responseString, crypto.inflatePublicKeyFromString(codePost.getPubkey()));
		}
        
		logger.writeLog("Device to Service: " + responseString);
		return responseString;
	}

	@GET
	@Path("/status")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDeviceStatus() {
		int iPercentBusy = (int) appContext.getProperties().get("percentBusy");
		int iPercentReady = (int) appContext.getProperties().get("percentReady");
		int iPercentNoResponse = (int) appContext.getProperties().get("percentNoResponse");

		DeviceStatusResponse oResponse = new DeviceStatusResponse("status");

		Random rand = new Random();
		int result = rand.nextInt(100);
		if (result < iPercentBusy) {
			// % Busy
			oResponse.setStatus("Busy");
		} else if (result >= iPercentBusy && result < (iPercentReady + iPercentBusy)) {
			// % Ready
			oResponse.setStatus("Ready");
		}

		return oResponse;
	}

}
