package smpl.catalog.model;

import smpl.catalog.Utility;

/**
 * Represents an catalog item item description
 */
public class Catalog {
	private String skuNumber;
	private String description;
	private int unit;
	private double unitPrice;

	public Catalog() {
	}

	public Catalog(String skuNumber, String description, double price, int inventory) {
		this.skuNumber = skuNumber;
		this.description = description;
		this.unit = inventory;
		this.unitPrice = price;
	}

	public Catalog(Catalog catalogItem) {
		this.skuNumber = catalogItem.getSkuNumber();
		this.description = catalogItem.getDescription();
		this.unitPrice = catalogItem.getUnitPrice();
		this.unit = catalogItem.getUnit();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSkuNumber() {
		return skuNumber;
	}

	public void setSkuNumber(String skuNumber) {
		this.skuNumber = skuNumber;
	}

	public int getUnit() {
		return unit;
	}

	public void setUnit(int unit) {
		this.unit = unit;
	}

	public double getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(double unitPrice) {
		this.unitPrice = unitPrice;
	}

	public String validate() {
		int count = 0;
		StringBuilder errors = new StringBuilder("{\"errors\": [");
		count = Utility.validateStringField(skuNumber, "SKU #", count, errors);
		count = Utility.validateStringField(description, "description", count, errors);
		errors.append("]}");

		return (count > 0) ? errors.toString() : null;
	}

}
