package edu.nku.device.resource;

public class StatusCode {
	private String status;
	private String message;

	public StatusCode(String status, String message) {
		this.status = status;
		this.message = message;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "[Status: " + status + " message: " + message + "]";
	}
}
