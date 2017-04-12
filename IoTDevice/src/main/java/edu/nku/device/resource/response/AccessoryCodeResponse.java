package edu.nku.device.resource.response;

import edu.nku.device.resource.IoTDeviceModel;

public class AccessoryCodeResponse  extends Response{

	private String status;
	private String deviceId;
	private int modelId;
	private String publicKey;
	private String type;
	private int firmware;
	
	public int getFirmware() {
		return firmware;
	}

	public void setFirmware(int firmware) {
		this.firmware = firmware;
	}

	public void setViaModel(IoTDeviceModel pDevice){
		deviceId = pDevice.getDeviceId();
		modelId = pDevice.getModelId();
		type = pDevice.getEncryptionEnabled() ? "Multi-Function" : "Simple";
		firmware = pDevice.getFirmwareVersion();
	}
	
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public int getModelId() {
		return modelId;
	}

	public void setModelId(int modelId) {
		this.modelId = modelId;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public AccessoryCodeResponse(String pAction) {
		super(pAction);
	}

}
