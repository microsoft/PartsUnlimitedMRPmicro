package smpl.catalog.repository.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import smpl.catalog.model.Catalog;

@Document(collection = "catalog")
public class CatalogItem {

	@Id
	private String id;

	// @Indexed
	private String skuNumber;
	private String description;
	private double price;
	private int inventory;
//	private int leadTime;

	public CatalogItem() {
	}
	
	public CatalogItem(Catalog from) {
		this.skuNumber = from.getSkuNumber();
		this.description = from.getDescription();
		this.price = from.getUnitPrice();
		this.inventory = from.getUnit();

	}

	public Catalog toCatalogItem() {
		Catalog result = new Catalog();
		result.setSkuNumber(skuNumber);
		result.setDescription(description);
		result.setUnitPrice(price);
		result.setUnit(inventory);
		return result;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
