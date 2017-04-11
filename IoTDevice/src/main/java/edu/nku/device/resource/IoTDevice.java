package edu.nku.device.resource;

import java.util.Random;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import edu.nku.device.resource.Result;
import edu.nku.device.utility.DataUtility;

@Path("/device")
public class IoTDevice {
	@Context
	private Application appContext;

	@GET
	@Path("/enroll")
	@Produces(MediaType.APPLICATION_JSON)
	public Result enrollDevice() {
		String deviceNumber = appContext.getProperties().get("deviceId").toString();
		DeviceMetadataResult oResult = new DeviceMetadataResult("enroll");
		DataUtility data = DataUtility.getInstance();
		oResult.setViaModel(data.getDeviceMetadata(deviceNumber));
		return oResult;
	}

	@GET
	@Path("/access/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	public Result getAccess(@PathParam("code") String pAccessCode) {
		Result oResult = new DeviceMetadataResult("access");

		return oResult;
	}

	@GET
	@Path("/status")
	@Produces(MediaType.APPLICATION_JSON)
	public Result getDeviceStatus() {
		int iPercentBusy = (int) appContext.getProperties().get("percentBusy");
		int iPercentReady = (int) appContext.getProperties().get("percentReady");
		int iPercentNoResponse = (int) appContext.getProperties().get("percentNoResponse");

		DeviceStatusResult oResult = new DeviceStatusResult("status");

		Random rand = new Random();
		int result = rand.nextInt(100);
		if (result < iPercentBusy) {
			// % Busy
			oResult.setStatus("Busy");
		} else if (result >= iPercentBusy && result < (iPercentReady + iPercentBusy)) {
			// % Ready
			oResult.setStatus("Ready");
		}

		return oResult;
	}

}
