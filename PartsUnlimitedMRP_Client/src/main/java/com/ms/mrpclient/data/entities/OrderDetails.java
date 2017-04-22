package com.ms.mrpclient.data.entities;

import java.util.Arrays;

public class OrderDetails {
	private String orderId;
	private String quoteId;
	private String orderDate;
	private OrderStatus status;
	private OrderEventInfo[] events;

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getQuoteId() {
		return quoteId;
	}

	public void setQuoteId(String quoteId) {
		this.quoteId = quoteId;
	}

	public String getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public OrderEventInfo[] getEvents() {
		return events;
	}

	public void setEvents(OrderEventInfo[] events) {
		this.events = events;
	}

	@Override
	public String toString() {
		return "OrderDetails [orderId=" + orderId + ", quoteId=" + quoteId + ", orderDate=" + orderDate + ", status="
				+ status + ", events=" + Arrays.toString(events) + "]";
	}
}
