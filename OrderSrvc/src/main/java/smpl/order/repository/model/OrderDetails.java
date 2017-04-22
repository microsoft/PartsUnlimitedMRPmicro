package smpl.order.repository.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import smpl.order.model.Order;
import smpl.order.model.OrderEventInfo;
import smpl.order.model.OrderStatus;

import java.util.List;

@Document(collection = "orders")
public class OrderDetails
{
    @Id
    private String id;

    @Indexed
    private String orderId;
    
    
    @Indexed
    private String quoteId;

    private String orderDate;

    @Indexed
    private OrderStatus status;
    private OrderEventInfo[] events;

    public OrderDetails()
    {
    }

    public OrderDetails(Order from)
    {
        this.orderId = from.getOrderId();
        this.quoteId = from.getQuoteId();
        this.orderDate = from.getOrderDate();
        this.status = from.getStatus();

        List<OrderEventInfo> es = from.getEvents();
        this.events = (es != null && !es.isEmpty()) ?
                es.toArray(new OrderEventInfo[es.size()]) :
                new OrderEventInfo[0];
    }

    public Order toOrder()
    {
        Order result = new Order();
        result.setOrderId(orderId);
        result.setQuoteId(quoteId);
        result.setStatus(status);
        result.setOrderDate(orderDate);
        if (events != null)
        {
            for (OrderEventInfo item : events)
            {
                result.addEvent(item);
            }
        }
        return result;
    }
    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
}
