package smpl.order.repository.service;


import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import smpl.order.BadRequestException;
import smpl.order.ConflictingRequestException;
import smpl.order.model.Order;
import smpl.order.model.OrderStatus;
import smpl.order.model.OrderUpdateInfo;
import smpl.order.repository.OrderRepository;
import smpl.order.repository.model.OrderDetails;


@Service
public class OrderService implements OrderRepository{
	private static final Logger log = LoggerFactory.getLogger(OrderService.class);
	List<Order> orders = new ArrayList<Order>();

	private MongoOperations mongoOperations;

	@Autowired
	public OrderService(MongoOperations operations) {
		this.mongoOperations = operations;

	}

   @Override
   @HystrixCommand(fallbackMethod = "createOrderFB", groupKey = "OrderService", commandKey = "OrderService", threadPoolKey = "OrderService")
   public Order createOrder(String from, Span span) throws ConflictingRequestException 
    {
	   log.info("createOrder called");
        /*Quote q = quotes.getQuote(from);
        if (q == null)
        {
            throw new BadRequestException(String.form6at("No such quote: %s", from));
        }
*/
        Order assocOrder = getOrderByQuoteId(from, span);
        
        if (assocOrder != null)
        {
           	throw new ConflictingRequestException(String.format("The quote has already been used to create an order: %s", assocOrder.getOrderId()));
        }

        Order result = new Order();
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        result.setOrderDate(df.format(new Date()));
        result.setOrderId(String.format("order-%s", from));
        result.setQuoteId(from);
        result.setStatus(OrderStatus.Created);

        mongoOperations.insert(new OrderDetails(result));
        span.log("CreatedOrder");
        return result;
    }

	
	  @Override
	  @HystrixCommand(fallbackMethod = "updateOrderFB", groupKey = "OrderService", commandKey = "OrderService", threadPoolKey = "OrderService")
	    public boolean updateOrder(Order order,String id, Span span)
	    {
		  log.info("updateOrder called");
	        OrderDetails existing = findExistingOrder(id);
	        return (existing != null) && saveOrder(id, order, existing, span);
	    }

	    private boolean saveOrder(String id, Order order, OrderDetails existing, Span span)
	    {
	        order.setOrderId(id); // Just to make sure

	        OrderDetails details = new OrderDetails(order);
	        details.setId(existing.getId());

	        mongoOperations.save(details);
            span.log("OrderSaved");
	        return true;
	    }
	    
	    @Override
	    @HystrixCommand(fallbackMethod = "removeOrderFB", groupKey = "OrderService", commandKey = "OrderService", threadPoolKey = "OrderService")
		public boolean removeOrder(String id, Span span) {
	    	log.info("removeOrder called");
			Query query = new Query(Criteria.where("orderId").is(id));
			OrderDetails result = mongoOperations.findAndRemove(query, OrderDetails.class);
		    span.log("DeletedOrder");
			return result != null;
		}
    
		@Override
		@HystrixCommand(fallbackMethod = "getOrderFB", groupKey = "OrderService", commandKey = "OrderService", threadPoolKey = "OrderService")
	  	public Order getOrder(String id, Span span)
	    {
			log.info("getOrder called");
	        OrderDetails existing = findExistingOrder(id);
	        span.log("GetOrder");
	        return (existing != null) ? existing.toOrder() : null;
	    }

		 private OrderDetails findExistingOrder(String id)
		   {
		       Query findExisting = new Query(Criteria.where("orderId").is(id));
		       return mongoOperations.findOne(findExisting, OrderDetails.class);
		   }
		 
		 
   @Override
   @HystrixCommand(fallbackMethod = "getOrderByQuoteIdFB", groupKey = "OrderService", commandKey = "OrderService", threadPoolKey = "OrderService")
   public Order getOrderByQuoteId(String id, Span span)
   {
	   log.info("getOrderByQuoteId called");
       Query findExisting = new Query(Criteria.where("quoteId").is(id));
       OrderDetails existing = mongoOperations.findOne(findExisting, OrderDetails.class);
       span.log("GetQutesByIdFromDB");
       return (existing != null) ? existing.toOrder() : null;
   }
   
   
   @Override
   @HystrixCommand(fallbackMethod = "getOrdersByStatusFB", groupKey = "OrderService", commandKey = "OrderService", threadPoolKey = "OrderService")
   public List<Order> getOrdersByStatus(OrderStatus status, Span span)
   {
	   log.info("getOrdersByStatus called");
       List<OrderDetails> found;
       if (status == OrderStatus.None)
       {
           found = mongoOperations.findAll(OrderDetails.class);
       }
       else
       {
           Query findExisting = new Query(Criteria.where("status").is(status));
           found = mongoOperations.find(findExisting, OrderDetails.class);
       }

       List<Order> result = new ArrayList<>();
       if (found != null && !found.isEmpty())
       {
           for (OrderDetails details : found)
           {
               result.add(details.toOrder());
           }
       }
       span.log("GetOrderByStatus");
       return result;
   }
   
   
 @Override
 @HystrixCommand(fallbackMethod = "getOrdersByDealerNameFB", groupKey = "OrderService", commandKey = "OrderService", threadPoolKey = "OrderService")
   public List<Order> getOrdersByDealerName(String dealer, OrderStatus status, Span span)
   {
	 log.info("getOrdersByDealerName called");
	 List<Order> result = new ArrayList<>();
	 System.out.println("getOrdersByDealerName");
	 /*List<String> quotesIds = quotes.getQuoteIdsByDealerName(dealer);

       Criteria criteria = Criteria.where("quoteId").in(quotesIds);

       if (status != OrderStatus.None)
       {
           criteria = criteria.and("status").is(status);
       }

       Query findExisting = new Query(criteria);

       List<OrderDetails> found = mongoOperations.find(findExisting, OrderDetails.class);

       List<Order> result = new ArrayList<>();
       if (found != null && found.size() > 0)
       {
           for (OrderDetails details : found)
           {
               result.add(details.toOrder());
           }
       }*/
	 span.log("GetOrderByDealer");
       return result;
   }


		


	
	
    

    /*
     * @Override
	public boolean updateOrder(Order input, String id) {
		Order order = new Order();
		order = findById(id);
		if (order.getOrderId().equals(id)) {
			order.setOrderDate(input.getOrderDate());
			order.setQuoteId(input.getQuoteId());
			order.setStatus(input.getStatus());
			order.setEvents(input.getEvents());
		}
		mongoOperations.save(order);
	}

		*/

   @Override
   @HystrixCommand(fallbackMethod = "hasOrderFB", groupKey = "OrderService", commandKey = "OrderService", threadPoolKey = "OrderService")
   public boolean hasOrder(String id)
   {
	   log.info("hasOrder called");
       Query findExisting = new Query(Criteria.where("orderId").is(id));
       return  mongoOperations.exists(findExisting, OrderDetails.class);
   }
	
   @Override
   @HystrixCommand(fallbackMethod = "updateOrderFB", groupKey = "OrderService", commandKey = "OrderService", threadPoolKey = "OrderService")
   public boolean updateOrder(String id, OrderUpdateInfo info, Span span) throws BadRequestException
   {
	   log.info("updateOrder called");
       OrderDetails existing = findExistingOrder(id);

       Order old = existing.toOrder();
       old.addEvent(info.getEventInfo());
       old.setStatus(info.getStatus());
       return saveOrder(id, old, existing,span);
   }


@Override
public Order createOrderFB(String quoteId, Span span) throws BadRequestException {
	// TODO Auto-generated method stub
	log.info("createOrderFB fall back method reached...");
	span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
	return null;
}

@Override
public boolean updateOrderFB(Order order, String id, Span span) {
	// TODO Auto-generated method stub
	log.info("updateOrderFB fall back method reached...");
	span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
	return false;
}

@Override
public boolean removeOrderFB(String id, Span span) {
	// TODO Auto-generated method stub
	log.info("removeOrderFB fall back method reached...");
	span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
	return false;
}

@Override
public List<Order> getOrdersByDealerNameFB(String dealer, OrderStatus status, Span span) {
	// TODO Auto-generated method stub
	log.info("getOrdersByDealerNameFB fall back method reached...");
	span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
	return null;
}

@Override
public Order getOrderFB(String id, Span span) {
	// TODO Auto-generated method stub
	log.info("getOrderFB fall back method reached...");
	span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
	return null;
}

@Override
public Order getOrderByQuoteIdFB(String id, Span span) {
	// TODO Auto-generated method stub
	log.info("getOrderByQuoteIdFB fall back method reached...");
	span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
	return null;
}

@Override
public List<Order> getOrdersByStatusFB(OrderStatus status, Span span) {
	// TODO Auto-generated method stub
	log.info("getOrdersByStatusFB fall back method reached...");
	span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
	return null;
}

@Override
public boolean hasOrderFB(String id) {
	// TODO Auto-generated method stub
	log.info("hasOrderFB fall back method reached...");	
	return false;
}

@Override
public boolean updateOrderFB(String id, OrderUpdateInfo info, Span span) throws BadRequestException {
	// TODO Auto-generated method stub
	log.info("updateOrderFB fall back method reached...");
	span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
	return false;
}


  
}
