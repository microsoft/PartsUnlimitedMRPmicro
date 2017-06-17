package com.ms.mrpclient.data.entities;

public class OrderUpdateInfo {
	private OrderStatus status;
	private OrderEventInfo eventInfo;

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public OrderEventInfo getEventInfo() {
		return eventInfo;
	}

	public void setEventInfo(OrderEventInfo eventInfo) {
		this.eventInfo = eventInfo;
	}

	@Override
	public String toString() {
		return "OrderUpdateInfo [status=" + status + ", eventInfo=" + eventInfo + "]";
	}
}
