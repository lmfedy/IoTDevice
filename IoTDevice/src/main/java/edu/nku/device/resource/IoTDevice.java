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
import edu.nku.device.resource.response.AccessoryCodeResponse;
import edu.nku.device.resource.response.DeviceStatusResponse;
import edu.nku.device.resource.response.DiscoveryResponse;
import edu.nku.device.resource.response.Response;
import edu.nku.device.utility.CryptoUtility;
import edu.nku.device.utility.DataUtility;

@Path("/device")
public class IoTDevice {
	@Context
	private Application appContext;

	@GET
	@Path("/enroll")
	@Produces(MediaType.APPLICATION_JSON)
	public String enrollDevice() {
		String deviceNumber = appContext.getProperties().get("deviceId").toString();
		DiscoveryResponse oResponse = new DiscoveryResponse("enroll");
		DataUtility data = DataUtility.getInstance();
		oResponse.setViaModel(data.getDeviceMetadata(deviceNumber));
		Gson gson = new Gson();
		return gson.toJson(oResponse, DiscoveryResponse.class);
	}

	@POST
	@Path("/access")
	@Consumes(MediaType.APPLICATION_JSON)
	public String getAccess(String pAccessCode) {
		String deviceNumber = appContext.getProperties().get("deviceId").toString();
		CryptoUtility crypto = (CryptoUtility) appContext.getProperties().get("CryptoUtility");

		Gson gson = new Gson();
		AccessoryCodePost codePost = gson.fromJson(pAccessCode, AccessoryCodePost.class);
		DataUtility data = DataUtility.getInstance();
		IoTDeviceModel device = data.getDeviceMetadata(deviceNumber);

		AccessoryCodeResponse oResponse = new AccessoryCodeResponse("access");
		if (device.getAccessoryCode() != codePost.getAccessoryCode())
			oResponse.setStatus("error");
		else {
			oResponse.setStatus("success");
			oResponse.setPublicKey(crypto.getPublicKeyString());
			oResponse.setViaModel(device);
		}

		String responseString = gson.toJson(oResponse, AccessoryCodeResponse.class);

		if (device.getEncryptionEnabled() && oResponse.getStatus() != "error") {
			responseString = crypto.encryptMessage(responseString,
					crypto.inflatePublicKeyFromString(codePost.getPublicKey()));
		}

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
