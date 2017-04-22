package smpl.shipment.repository;

import java.util.List;

import smpl.shipment.BadRequestException;
import smpl.shipment.model.OrderStatus;
import smpl.shipment.model.ShipmentEventInfo;
import smpl.shipment.model.ShipmentRecord;
import io.opentracing.*;


/**
 * Interface for repositories holding catalog item data.
 */
public interface ShipmentRepository
{
    List<ShipmentRecord> getShipments(OrderStatus status,Span span);

    ShipmentRecord getShipmentById(String id,Span span);

    ShipmentRecord createShipment(ShipmentRecord info, Span span) throws BadRequestException;

    boolean addEvent(String id, ShipmentEventInfo event, Span span);

    boolean updateShipment(ShipmentRecord info, Span span);

    boolean removeShipment(String id, String eTag, Span span);
    
    //Fallback methods
    
    List<ShipmentRecord> getShipmentsFB(OrderStatus status,Span span);

    ShipmentRecord getShipmentByIdFB(String id,Span span);

    ShipmentRecord createShipmentFB(ShipmentRecord info, Span span) throws BadRequestException;

    boolean addEventFB(String id, ShipmentEventInfo event, Span span);

    boolean updateShipmentFB(ShipmentRecord info, Span span);

    boolean removeShipmentFB(String id, String eTag, Span span);
    
}
