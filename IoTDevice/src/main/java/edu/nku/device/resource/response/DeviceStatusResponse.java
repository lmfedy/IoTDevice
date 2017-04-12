package edu.nku.device.resource.response;

public class DeviceStatusResponse extends Response {
	private String deviceStatus;

	public DeviceStatusResponse(String pAction) {
		super(pAction);
	}

	public String getStatus() {
		return deviceStatus;
	}

	public void setStatus(String status) {
		this.deviceStatus = status;
	}

}
