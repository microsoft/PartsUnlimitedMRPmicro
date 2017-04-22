package com.ms.mrpclient.data.entities;

public class OrderEventInfo {
	private String date;
    private String comments;

    public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	@Override
	public String toString() {
		return "OrderEventInfo [date=" + date + ", comments=" + comments + "]";
	}
}
