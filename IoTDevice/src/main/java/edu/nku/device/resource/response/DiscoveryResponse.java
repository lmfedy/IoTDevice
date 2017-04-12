package edu.nku.device.resource.response;

import edu.nku.device.resource.IoTDeviceModel;
import edu.nku.device.resource.Result;

public class DiscoveryResponse extends Result {
	private String deviceId;
	private String vendor;
	private String productName;
	private Boolean encryptionEnabled;

	public DiscoveryResponse(String pAction) {
		super(pAction);
	}

	public void setViaModel(IoTDeviceModel pModel) {
		deviceId = pModel.getDeviceId();
		vendor = pModel.getVendor();
		productName = pModel.getProductName();
		encryptionEnabled = pModel.getEncryptionEnabled();
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
