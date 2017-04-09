package edu.nku.device.resource;

public class DeviceStatusResult extends Result {

	private String deviceStatus;

	public DeviceStatusResult(String pAction) {
		super(pAction);
	}

	public String getStatus() {
		return deviceStatus;
	}

	public void setStatus(String status) {
		this.deviceStatus = status;
	}

}
