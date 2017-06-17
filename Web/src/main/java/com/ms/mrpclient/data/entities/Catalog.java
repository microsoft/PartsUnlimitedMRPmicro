package com.ms.mrpclient.data.entities;

public class Catalog {
	private String skuNumber;
	private String description;
	private double unitPrice;
	private int unit;
	
	public String getSkuNumber() {
		return skuNumber;
	}

	public void setSkuNumber(String skuNumber) {
		this.skuNumber = skuNumber;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(double unitPrice) {
		this.unitPrice = unitPrice;
	}

	public int getUnit() {
		return unit;
	}

	public void setUnit(int unit) {
		this.unit = unit;
	}
	
	@Override
	public String toString() {
		return "Catalog [skuNumber=" + skuNumber + ", description=" + description + ", unitPrice=" + unitPrice + ", unit=" + unit + "]";
	}
}
