package edu.nku.device.resource;

public class IoTDeviceModel {
	private String deviceId;
	private String vendor;
	private String productName;
	private Boolean encryptionEnabled;
	private int firmwareVersion;
	private int modelId;
	private String accessoryCode = "ABCD-1234";

	public String getAccessoryCode() {
		return "ABCD-1234";
	}

	public int getModelId() {
		return modelId;
	}

	public void setModelId(int modelId) {
		this.modelId = modelId;
	}

	public IoTDeviceModel(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Boolean getEncryptionEnabled() {
		return encryptionEnabled;
	}

	public void setEncryptionEnabled(Boolean encryptionEnabled) {
		this.encryptionEnabled = encryptionEnabled;
	}

	public int getFirmwareVersion() {
		return firmwareVersion;
	}

	public void setFirmwareVersion(int firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}

}
