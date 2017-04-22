package com.ms.mrpclient.data.entities;

public class DealerDetails {
	public int timestamp;
	public int machine;
	public short pid;
	public int increment;
	public String creationTime;
	public double dealerid;
	public String name;
	public String contact;
	public String address;
	public String email;
	public String phone;

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public int getMachine() {
		return machine;
	}

	public void setMachine(int machine) {
		this.machine = machine;
	}

	public short getPid() {
		return pid;
	}

	public void setPid(short pid) {
		this.pid = pid;
	}

	public int getIncrement() {
		return increment;
	}

	public void setIncrement(int increment) {
		this.increment = increment;
	}

	public String getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(String creationTime) {
		this.creationTime = creationTime;
	}

	public double getDealerid() {
		return dealerid;
	}

	public void setDealerid(double dealerid) {
		this.dealerid = dealerid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Override
	public String toString() {
		return "DealerDetails [timestamp=" + timestamp + ", machine=" + machine + ", pid=" + pid + ", increment="
				+ increment + ", creationTime=" + creationTime + ", dealerid=" + dealerid + ", name=" + name
				+ ", contact=" + contact + ", address=" + address + ", email=" + email + ", phone=" + phone + "]";
	}
}
