package edu.nku.device.resource;

public class DeviceMetadataResult extends Result {

	private String deviceId;
	private String vendor;
	private String productName;
	private Boolean encryptionEnabled;
//	private int modelId;
//	private int firmwareVersion;
//	
//	public int getModelId() {
//		return modelId;
//	}
//
//	public void setModelId(int modelId) {
//		this.modelId = modelId;
//	}
//
//	public int getFirmwareVersion() {
//		return firmwareVersion;
//	}
//
//	public void setFirmwareVersion(int firmwareVersion) {
//		this.firmwareVersion = firmwareVersion;
//	}

	public DeviceMetadataResult(String pAction) {
		super(pAction);
	}
	
	public void setViaModel(IoTDeviceModel pModel)
	{
		deviceId = pModel.getDeviceId();
		vendor = pModel.getVendor();
		productName = pModel.getProductName();
		encryptionEnabled = pModel.getEncryptionEnabled();
//		modelId = pModel.getModelId();
//		firmwareVersion = pModel.getFirmwareVersion();		
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceID) {
		this.deviceId = deviceID;
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

}
