package smpl.order;

import static org.junit.Assert.*;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import smpl.order.controller.OrderController;
import smpl.order.model.Order;
import smpl.order.model.OrderEventInfo;
import smpl.order.model.OrderStatus;
import smpl.order.model.OrderUpdateInfo;
import smpl.order.repository.model.OrderDetails;
import smpl.order.repository.service.OrderService;

@SuppressWarnings("unchecked")
@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
public class OrdersControllerTest {

	OrderController ordercontroller;
	OrderService OrderServiceMock;
	MongoOperations mongoOperation;
	ConfigurableApplicationContext ctx;
	MockHttpServletRequest requestMock = new MockHttpServletRequest();
	String strURLZipkins;
	
	@Test
    public void testCreateOrder() throws Exception
	{
		ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
		strURLZipkins = (String) ctx.getBean("getZipkinURL");
		ordercontroller = new OrderController(null, requestMock, strURLZipkins);
    	
    	//Test creating new Order successfully
		String strQuoteId = "quote_100"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    	ResponseEntity response = ordercontroller.createOrder(strQuoteId);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Order q = ((ResponseEntity<Order>) response).getBody();
        assertNotNull(q);
        assertEquals(strQuoteId, q.getQuoteId());
        
        //Test creating new Order with null input
        response = ordercontroller.createOrder(null);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        //Test creating duplicate Order 
        response = ordercontroller.createOrder(strQuoteId);
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        
    }

    @Test
    public void testUpdateOrder() throws Exception
    {
		ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
		strURLZipkins = (String) ctx.getBean("getZipkinURL");
		ordercontroller = new OrderController(null, requestMock, strURLZipkins);
    	
        //Create new Order successfully and test update for one of its field
        String strQuoteId = "quote_101"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    	ResponseEntity response = ordercontroller.createOrder(strQuoteId);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Order q = ((ResponseEntity<Order>) response).getBody();
        assertNotNull(q);
        assertEquals(strQuoteId, q.getQuoteId());
        assertNotNull(q.getEvents());
        assertEquals(0, q.getEvents().size());
        
        String strOrderId = q.getOrderId();
        
        Order order = new Order();
    	order.setOrderId(q.getOrderId());
    	order.setOrderDate(q.getOrderDate());
    	order.setQuoteId(q.getQuoteId());
    	order.setStatus(q.getStatus());
		OrderEventInfo objEventInfo = new OrderEventInfo();
		objEventInfo.setComments("New Creation");
		objEventInfo.setDate("2017-02-28T20:43:37+0000");
		ArrayList arlstEvent = new ArrayList();
		arlstEvent.add(objEventInfo);
		order.setEvents(arlstEvent);
        
		response = ordercontroller.updateOrder(strOrderId, order);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        response = ordercontroller.getOrderById(strOrderId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Order responseobj = ((ResponseEntity<Order>) response).getBody();
        assertNotNull(responseobj);
        assertEquals(strOrderId, responseobj.getOrderId());
        assertEquals(order.getQuoteId(), responseobj.getQuoteId());
        assertNotNull(responseobj.getEvents());
        assertEquals(1, responseobj.getEvents().size());
        
        //Test updating Order with non existing Order id
    	response = ordercontroller.updateOrder("Order-1010", order);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        //Test updating Order with null as Order id
        response = ordercontroller.updateOrder(null, order);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        //Test updating Order with null as Order object
        response = ordercontroller.updateOrder(strOrderId, null);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        //Test updating Order with invalid field in Order object
        order.setOrderDate("");
        response = ordercontroller.updateOrder(strOrderId, order);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	
    }
    
    @Test
    public void testGetOrderById() throws Exception
    {
		ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
		strURLZipkins = (String) ctx.getBean("getZipkinURL");
		ordercontroller = new OrderController(null, requestMock, strURLZipkins);
    	
    	//create one Order successfully and get it back by specifying same id
    	String strQuoteId = "quote_102"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    	ResponseEntity response = ordercontroller.createOrder(strQuoteId);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Order q = ((ResponseEntity<Order>) response).getBody();
        assertNotNull(q);
        assertEquals(strQuoteId, q.getQuoteId());
        
        response = ordercontroller.getOrderById(q.getOrderId());
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Order returniobj = ((ResponseEntity<Order>) response).getBody();
        assertNotNull(returniobj);
        assertEquals(q.getQuoteId(), returniobj.getQuoteId());
        assertEquals(q.getOrderId(), returniobj.getOrderId());
        
         //Test getting Order with non existing Order id
        response = ordercontroller.getOrderById("abc");
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        //Test getting Order with null as Order id
        response = ordercontroller.getOrderById(null);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    
    }

    @Test
    public void testGetOrdersByDealerName() throws Exception
    {
		ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
		strURLZipkins = (String) ctx.getBean("getZipkinURL");
		ordercontroller = new OrderController(null, requestMock, strURLZipkins);
    	
    	//Create valid order and set its status test to get it by that status
    	String strQuoteId = "quote_102"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    	ResponseEntity response = ordercontroller.createOrder(strQuoteId);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Order q = ((ResponseEntity<Order>) response).getBody();
        assertNotNull(q);
        assertEquals(strQuoteId, q.getQuoteId());
        
        q.setStatus(OrderStatus.DeliveryConfirmed);
        response = ordercontroller.updateOrder(q.getOrderId(), q);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        //Test getting same order back with status
        response = ordercontroller.getOrdersByDealerName("",OrderStatus.DeliveryConfirmed);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<Order> orders = ((ResponseEntity<List<Order>>) response).getBody();
        boolean found = false;
        assertNotNull(orders);
        for (Order orderobj : orders)
        {
            if(orderobj.getStatus() == OrderStatus.DeliveryConfirmed && orderobj.getOrderId() != null && orderobj.getOrderId().equalsIgnoreCase(q.getOrderId()))
        	{
            	found = true;
            	break;
        	}
        }
        assertEquals(true, found);
        
        
        //Test getting same with null status
        response = ordercontroller.getOrdersByDealerName("",null);
        assertNotNull(response);
        assertTrue("Test successful for getOrdersByDealerName", response.getStatusCode() == HttpStatus.NOT_FOUND || response.getStatusCode() == HttpStatus.OK);
        
        
   }

    @Test
    public void testDeleteOrder() throws Exception
    {
		ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
		strURLZipkins = (String) ctx.getBean("getZipkinURL");
		ordercontroller = new OrderController(null, requestMock, strURLZipkins);
    	
    	//test deleting Order with non existing Order id
        ResponseEntity response = ordercontroller.deleteOrder("abc");
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        //create one Order successfully and delete it by specifying same Order id
       
    	String strQuoteId = "quote_103"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    	response = ordercontroller.createOrder(strQuoteId);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Order q = ((ResponseEntity<Order>) response).getBody();
        assertNotNull(q);
        assertEquals(strQuoteId, q.getQuoteId());
        
        response = ordercontroller.deleteOrder(q.getOrderId());
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        //test deleting Order with id as blank
        response = ordercontroller.deleteOrder("");
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        //test deleting Order with null Order id
        response = ordercontroller.deleteOrder(null);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testUpdateStatus() throws Exception
    {
		ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
		strURLZipkins = (String) ctx.getBean("getZipkinURL");
		ordercontroller = new OrderController(null, requestMock, strURLZipkins);
    	
    	OrderUpdateInfo objupdateinfo = new OrderUpdateInfo();
    	OrderEventInfo objEventInfo = new OrderEventInfo();
		objEventInfo.setComments("EventInfoUpdateTest");
		objEventInfo.setDate("2017-02-28T20:43:37+0000");
		objupdateinfo.setEventInfo(objEventInfo);
		objupdateinfo.setStatus(OrderStatus.Confirmed);
    	
		//test update status Order with non existing Order id
        ResponseEntity response = ordercontroller.updateStatus("abc",objupdateinfo);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        //create one Order successfully and delete it by specifying same Order id
       
    	String strQuoteId = "quote_104"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    	response = ordercontroller.createOrder(strQuoteId);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Order q = ((ResponseEntity<Order>) response).getBody();
        assertNotNull(q);
        assertEquals(strQuoteId, q.getQuoteId());
        
        response = ordercontroller.updateStatus(q.getOrderId(),objupdateinfo);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        response = ordercontroller.getOrderById(q.getOrderId());
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Order returniobj = ((ResponseEntity<Order>) response).getBody();
        assertNotNull(returniobj);
        boolean found = false;
        for (OrderEventInfo event : returniobj.getEvents())
        {
            if(event.getComments().equalsIgnoreCase("EventInfoUpdateTest"))
        	{
            	found = true;
            	break;
        	}
        }
        assertEquals(true, found);
        
        //test updating status Order with update info null
        response = ordercontroller.updateStatus(q.getOrderId(),null);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
                
      //test updating status Order with order id null
        response = ordercontroller.updateStatus(null,objupdateinfo);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testAddEvent() throws Exception
    {
		ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
		strURLZipkins = (String) ctx.getBean("getZipkinURL");
		ordercontroller = new OrderController(null, requestMock, strURLZipkins);
    	
    	OrderEventInfo objEventInfo = new OrderEventInfo();
		objEventInfo.setComments("EventInfoUpdateTest");
		objEventInfo.setDate("2017-02-28T20:43:37+0000");
		
    	
		//test update status Order with non existing Order id
        ResponseEntity response = ordercontroller.addEvent("abc",objEventInfo);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        //create one Order successfully and delete it by specifying same Order id
       
    	String strQuoteId = "quote_105"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    	response = ordercontroller.createOrder(strQuoteId);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Order q = ((ResponseEntity<Order>) response).getBody();
        assertNotNull(q);
        assertEquals(strQuoteId, q.getQuoteId());
        
        response = ordercontroller.addEvent(q.getOrderId(),objEventInfo);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        response = ordercontroller.getOrderById(q.getOrderId());
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Order returniobj = ((ResponseEntity<Order>) response).getBody();
        assertNotNull(returniobj);
        boolean found = false;
        for (OrderEventInfo event : returniobj.getEvents())
        {
            if(event.getComments().equalsIgnoreCase("EventInfoUpdateTest"))
        	{
            	found = true;
            	break;
        	}
        }
        assertEquals(true, found);
        
        //test add event in  Order with update info null
        response = ordercontroller.addEvent(q.getOrderId(),null);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
                
        //test add event in  Order with order id info null
        response = ordercontroller.addEvent(null, objEventInfo);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

}
