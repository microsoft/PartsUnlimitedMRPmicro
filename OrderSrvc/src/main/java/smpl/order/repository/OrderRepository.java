package smpl.order.repository;

import java.util.List;

import smpl.order.BadRequestException;
import smpl.order.model.Order;
import smpl.order.model.OrderStatus;
import smpl.order.model.OrderUpdateInfo;
import io.opentracing.*;

public interface OrderRepository {

	Order createOrder(String quoteId,Span span) throws BadRequestException;
	
	boolean updateOrder(Order order, String id,Span span);

	boolean removeOrder(String id, Span span);

     List<Order> getOrdersByDealerName(String dealer, OrderStatus status,Span span);

	Order getOrder(String id, Span span);
	
    Order getOrderByQuoteId(String id, Span span);
    
    List<Order> getOrdersByStatus(OrderStatus status, Span span);
    
    boolean hasOrder(String id);
    
    boolean updateOrder(String id, OrderUpdateInfo info, Span span) throws BadRequestException;
    
    //Fallback Method
    
    Order createOrderFB(String quoteId,Span span) throws BadRequestException;
	
	boolean updateOrderFB(Order order, String id, Span span);

	boolean removeOrderFB(String id,Span span);

     List<Order> getOrdersByDealerNameFB(String dealer, OrderStatus status, Span span);

	Order getOrderFB(String id, Span span);
	
    Order getOrderByQuoteIdFB(String id, Span span);
    
    List<Order> getOrdersByStatusFB(OrderStatus status, Span span);
    
    boolean hasOrderFB(String id);
    
    boolean updateOrderFB(String id, OrderUpdateInfo info, Span span) throws BadRequestException;

}