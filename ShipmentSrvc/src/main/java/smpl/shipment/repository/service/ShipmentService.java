package smpl.shipment.repository.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import io.opentracing.Span;
import smpl.shipment.BadRequestException;
import smpl.shipment.ConflictingRequestException;
import smpl.shipment.controller.ShipmentController;
import smpl.shipment.model.OrderStatus;
import smpl.shipment.model.ShipmentEventInfo;
import smpl.shipment.model.ShipmentRecord;
import smpl.shipment.repository.ShipmentRepository;
import smpl.shipment.repository.model.ShipmentDetails;

@Service
public class ShipmentService implements ShipmentRepository
{
	private static final Logger log = LoggerFactory.getLogger(ShipmentService.class);
	private MongoOperations mongoOperations;

	@Autowired
	public ShipmentService(MongoOperations operations) {
		this.mongoOperations = operations;

	}

    @Override
    @HystrixCommand(fallbackMethod = "getShipmentsFB", groupKey = "ShipmentService", commandKey = "ShipmentService")
    public List<ShipmentRecord> getShipments(OrderStatus status,Span span)
    {
    	log.info("getShipments called");
    	List<ShipmentDetails> found = mongoOperations.findAll(ShipmentDetails.class);

    	List<ShipmentRecord> result = new ArrayList<>();

    	if (found != null) {
    		for (ShipmentDetails s : found) {
    			result.add(s.toShipmentRecord());
    		}
    	}
    	span.log("GetShipments");
    	return result;
    }

	@Override
	@HystrixCommand(fallbackMethod = "getShipmentByIdFB", groupKey = "ShipmentService", commandKey = "ShipmentService", threadPoolKey = "ShipmentService")
	public ShipmentRecord getShipmentById(String id, Span span) {
		log.info("getShipmentById called");
		ShipmentDetails existing = findExistingShipment(id);
		span.log("GetShipmentById");
		return (existing != null) ? existing.toShipmentRecord() : null;
	}

	private ShipmentDetails findExistingShipment(String id) {
		log.info("getCatalogItems called");
		Query findExisting = new Query(Criteria.where("orderId").is(id));
		return mongoOperations.findOne(findExisting, ShipmentDetails.class);
	}

	@Override
	@HystrixCommand(fallbackMethod = "createShipmentFB", groupKey = "ShipmentService", commandKey = "ShipmentService", threadPoolKey = "ShipmentService")
	public ShipmentRecord createShipment(ShipmentRecord info, Span span) throws BadRequestException {
		
		
		/*		Order assocOrder = orders.getOrder(info.getOrderId());
        if (assocOrder == null)
        {
            throw new BadRequestException(String.format("Order '%s' could not be found: ", info.getOrderId()));
        }*/
		log.info("createShipment called");
		 ShipmentDetails existing = findExistingShipmentDetails(info.getOrderId());
	        if (existing != null)
	        {
	            throw new ConflictingRequestException(String.format("A shipment record for order '%s' already exists", info.getOrderId()));
	        }

	        mongoOperations.insert(new ShipmentDetails(info));
	        span.log("CreateShipment");
	        return new ShipmentRecord(info);
		
	}
	 private ShipmentDetails findExistingShipmentDetails(String id)
	    {
	        Query q = new Query(Criteria.where("orderId").is(id));
	        return mongoOperations.findOne(q, ShipmentDetails.class);
	    }

	@Override
	@HystrixCommand(fallbackMethod = "addEventFB", groupKey = "ShipmentService", commandKey = "ShipmentService", threadPoolKey = "ShipmentService")
	public boolean addEvent(String id, ShipmentEventInfo event, Span span) {
		log.info("addEvent called");
		 ShipmentDetails existing = findExistingShipmentDetails(id);
	        if (existing == null) return false;
	        span.log("GetShipment");
	        ShipmentRecord result = existing.toShipmentRecord();
	        result.addEvent(event);
	        return saveUpdates(existing, result,span);
	}
	  private boolean saveUpdates(ShipmentDetails existing, ShipmentRecord result, Span span)
	    {
		  
	        ShipmentDetails updated = new ShipmentDetails(result);
	        updated.setId(existing.getId());
     
	        mongoOperations.save(updated);
	        span.log("Updatedshipment");
	        return true;
	    }

	@Override
	@HystrixCommand(fallbackMethod = "updateShipmentFB", groupKey = "ShipmentService", commandKey = "ShipmentService", threadPoolKey = "ShipmentService")
	public boolean updateShipment(ShipmentRecord info, Span span) {
		log.info("updateShipment called");
        ShipmentDetails existing = findExistingShipmentDetails(info.getOrderId());
        return (existing != null) && saveUpdates(existing, info,span);
	}

	@Override
	@HystrixCommand(fallbackMethod = "removeShipmentFB", groupKey = "ShipmentService", commandKey = "ShipmentService", threadPoolKey = "ShipmentService")
	public boolean removeShipment(String id, String eTag, Span span) {
		log.info("removeShipment called");
        Query findExisting = new Query(Criteria.where("orderId").is(id));
        ShipmentDetails existing = mongoOperations.findAndRemove(findExisting, ShipmentDetails.class);
        span.log("DeleteShipment");
        return existing != null;
	}

	
	@Override	
	public List<ShipmentRecord> getShipmentsFB(OrderStatus status, Span span) {
		// TODO Auto-generated method stub
		log.info("getShipmentsFB fall back method reached...");
		span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
		return null;
	}

	@Override
	public ShipmentRecord getShipmentByIdFB(String id, Span span) {
		// TODO Auto-generated method stub
		log.info("getShipmentByIdFB fall back method reached...");
		span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
		return null;
	}

	@Override
	public ShipmentRecord createShipmentFB(ShipmentRecord info, Span span) throws BadRequestException {
		// TODO Auto-generated method stub
		log.info("createShipmentFB fall back method reached...");
		span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
		return null;
	}

	@Override
	public boolean addEventFB(String id, ShipmentEventInfo event, Span span) {
		// TODO Auto-generated method stub
		log.info("addEventFB fall back method reached...");
		span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
		return false;
	}

	@Override
	public boolean updateShipmentFB(ShipmentRecord info, Span span) {
		// TODO Auto-generated method stub
		log.info("updateShipmentFB fall back method reached...");
		span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
		return false;
	}

	@Override
	public boolean removeShipmentFB(String id, String eTag, Span span) {
		// TODO Auto-generated method stub
		log.info("removeShipmentFB fall back method reached...");
		span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
		return false;
	}

 
}
