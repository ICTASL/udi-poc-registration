package io.mosip.registrationprocessor.externalstage.dto;

import java.io.Serializable;

public class EmailInfoDTO implements Serializable {

	private String name;
	private String phone;
	private String email;
	private String reason;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getReason() { return reason; }
	public void setReason(String reason) { this.reason = reason; }
}