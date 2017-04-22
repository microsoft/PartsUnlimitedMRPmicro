package smpl.shipment.repository.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import smpl.shipment.model.DeliveryAddress;
import smpl.shipment.model.PhoneInfo;
import smpl.shipment.model.ShipmentEventInfo;
import smpl.shipment.model.ShipmentRecord;

@Document(collection = "shipments")
public class ShipmentDetails
{
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    @Id
    private String id;

    @Indexed
    private String orderId;
    
    private String deliveryDate;

    private ShipmentEventInfo[] events;

    private DeliveryAddress deliveryAddress;

    private String contactName;

    private PhoneInfo primaryContactPhone;

    private PhoneInfo alternateContactPhone;

    public ShipmentDetails()
    {
    }

    public ShipmentDetails(ShipmentRecord from)
    {
        this.orderId = from.getOrderId();
        this.deliveryDate=from.getDeliveryDate();
        this.events = (from.getEvents() != null) ?
                from.getEvents().toArray(new ShipmentEventInfo[from.getEvents().size()]) :
                new ShipmentEventInfo[0];
        this.deliveryAddress = from.getDeliveryAddress();
        this.contactName = from.getContactName();
        this.primaryContactPhone = from.getPrimaryContactPhone();
        this.alternateContactPhone = from.getAlternateContactPhone();
    }

    public ShipmentRecord toShipmentRecord()
    {
        ShipmentRecord result = new ShipmentRecord();
        result.setOrderId(orderId);
        result.setDeliveryDate(deliveryDate);
        result.setDeliveryAddress(deliveryAddress);
        result.setPrimaryContactPhone(primaryContactPhone);
        result.setContactName(contactName);
        result.setAlternateContactPhone(alternateContactPhone);
        if (events != null)
        {
            for (ShipmentEventInfo info : events)
            {
                result.addEvent(info);
            }
        }
        return result;
    }
}
