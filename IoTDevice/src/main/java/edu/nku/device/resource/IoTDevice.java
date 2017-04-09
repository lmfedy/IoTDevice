package edu.nku.device.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import edu.nku.device.resource.Result;

@Path("/device")
public class IoTDevice {
	@Context
	private Application appContext;

	@GET
	@Path("/enroll")
	@Produces(MediaType.APPLICATION_JSON)
	public Result getLatestVersion() {
		Result oResult = new Result("enroll");
		
		return oResult;
	}
}
