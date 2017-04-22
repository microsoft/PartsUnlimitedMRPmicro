package smpl.shipment.model;

//import smpl.shipment.Utility;

/**
 * Represents the aggregate information stored about delivery (Quote, Order, Shipment).
 */
public class Delivery {
	//TODO to be handled from rest call to Quote and Order
    /*private Quote quote;
    private Order order;
    

    public Quote getQuote() {
        return this.quote;
    }

    public void setQuote(Quote quote) {
        this.quote = quote;
    }

    public Order getOrder() {
        return this.order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }*/
	private ShipmentRecord shipmentRecord;

    public ShipmentRecord getShipmentRecord() {
        return this.shipmentRecord;
    }

    public void setShipmentRecord(ShipmentRecord shipmentRecord) {
        this.shipmentRecord = shipmentRecord;
    }
}
