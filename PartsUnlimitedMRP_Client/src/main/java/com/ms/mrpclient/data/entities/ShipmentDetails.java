package com.ms.mrpclient.data.entities;

import java.util.List;

public class ShipmentDetails {
	private String orderId;
	private String deliveryDate;
	private List<ShipmentEventInfo> events;
	private DeliveryAddress deliveryAddress;
	private String contactName;
	private PhoneInfo primaryContactPhone;
	private PhoneInfo alternateContactPhone;

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getDeliveryDate() {
		return deliveryDate;
	}

	public void setDeliveryDate(String deliveryDate) {
		this.deliveryDate = deliveryDate;
	}

	public List<ShipmentEventInfo> getEvents() {
		return events;
	}

	public void setEvents(List<ShipmentEventInfo> events) {
		this.events = events;
	}

	public DeliveryAddress getDeliveryAddress() {
		return deliveryAddress;
	}

	public void setDeliveryAddress(DeliveryAddress deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public PhoneInfo getPrimaryContactPhone() {
		return primaryContactPhone;
	}

	public void setPrimaryContactPhone(PhoneInfo primaryContactPhone) {
		this.primaryContactPhone = primaryContactPhone;
	}

	public PhoneInfo getAlternateContactPhone() {
		return alternateContactPhone;
	}

	public void setAlternateContactPhone(PhoneInfo alternateContactPhone) {
		this.alternateContactPhone = alternateContactPhone;
	}

	@Override
	public String toString() {
		return "ShipmentDetails [orderId=" + orderId + ", deliveryDate=" + deliveryDate + ", events=" + events
				+ ", deliveryAddress=" + deliveryAddress + ", contactName=" + contactName + ", primaryContactPhone="
				+ primaryContactPhone + ", alternateContactPhone=" + alternateContactPhone + "]";
	}
}