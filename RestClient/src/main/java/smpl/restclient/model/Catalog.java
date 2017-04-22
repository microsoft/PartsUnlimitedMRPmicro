package smpl.restclient.model;


/**
 * Represents an catalog item item description
 */
public class Catalog
{
    private String skuNumber;
    private String description;
    private double price;
    private int inventory;
    private int leadTime;

    public Catalog()
    {
    }

    public Catalog(String skuNumber, String description, double price, int inventory, int leadTime)
    {
        this.skuNumber = skuNumber;
        this.description = description;
        this.price = price;
        this.inventory = inventory;
        this.leadTime = leadTime;
    }

    public Catalog(Catalog catalogItem)
    {
        this.skuNumber = catalogItem.getSkuNumber();
        this.description = catalogItem.getDescription();
        this.price = catalogItem.getPrice();
        this.inventory = catalogItem.getInventory();
        this.leadTime  = catalogItem.getLeadTime();
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    public String getSkuNumber()
    {
        return skuNumber;
    }

    public void setSkuNumber(String skuNumber)
    {
        this.skuNumber = skuNumber;
    }

    public int getInventory() {
        return inventory;
    }

    public void setInventory(int inventory) {
        this.inventory = inventory;
    }

    public int getLeadTime() {
        return leadTime;
    }

    public void setLeadTime(int leadTime) {
        this.leadTime = leadTime;
    }



}
