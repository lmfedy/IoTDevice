package edu.nku.device.resource.response;

import edu.nku.device.resource.StatusCode;

public class UpdateSuccessResponse extends Response {

	private String firmwareVersion;
	private StatusCode updateStatus;
	private String deviceId;
	private String modelId;
	
	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	public void setFirmwareVersion(String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}

	public StatusCode getUpdateStatus() {
		return updateStatus;
	}

	public void setUpdateStatus(StatusCode updateStatus) {
		this.updateStatus = updateStatus;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public UpdateSuccessResponse(String pAction) {
		super(pAction);
	}

}
