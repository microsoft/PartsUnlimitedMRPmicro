package smpl.shipment;

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

import smpl.shipment.controller.ShipmentController;
import smpl.shipment.model.DeliveryAddress;
import smpl.shipment.model.OrderStatus;
import smpl.shipment.model.PhoneInfo;
import smpl.shipment.model.ShipmentEventInfo;
import smpl.shipment.model.ShipmentRecord;
import smpl.shipment.repository.model.ShipmentDetails;
import smpl.shipment.repository.service.ShipmentService;

@SuppressWarnings("unchecked")
@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
public class ShipmentControllerTest {

	ShipmentController shipmentcontroller;
	ShipmentService ShipmentServiceMock;
	MongoOperations mongoOperation;
	ConfigurableApplicationContext ctx;
	MockHttpServletRequest requestMock = new MockHttpServletRequest();
	String strURLZipkins;

	@Test
    public void testCreateShipmentRecord() throws Exception
	{
		ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
        strURLZipkins = (String)ctx.getBean("getZipkinURL");
    	shipmentcontroller = new ShipmentController(null,requestMock,strURLZipkins);
    	
    	//Create valid shipment and set its status test to get it by that status
    	String strOrderId = "shipmentorder_103"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    	ShipmentRecord objShipment = new ShipmentRecord();
    	objShipment.setOrderId(strOrderId);
    	objShipment.setDeliveryDate(new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()));
    	ShipmentEventInfo eventinfo = new ShipmentEventInfo();
    	eventinfo.setDate(new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()));
    	eventinfo.setComments("New Shipment Creation Event From Test");
    	objShipment.addEvent(eventinfo);
    	DeliveryAddress objAddress = new DeliveryAddress();
    	objAddress.setStreet("200");
    	objAddress.setCity("San Fransisco");
    	objAddress.setState("CA");
    	objAddress.setPostalCode("94016");
    	objAddress.setSpecialInstructions("");
    	objShipment.setDeliveryAddress(objAddress);
    	objShipment.setContactName("ABC");
    	PhoneInfo objphone = new PhoneInfo();
    	objphone.setPhoneNumber("12345678");
    	objphone.setKind("");
    	objShipment.setPrimaryContactPhone(objphone);
    	objphone.setPhoneNumber("12345677");
    	objShipment.setAlternateContactPhone(objphone);
 	    
    	//Test successful creation of shipment record
    	ResponseEntity response = shipmentcontroller.createShipmentRecord(objShipment);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        response = shipmentcontroller.getShipmentById(strOrderId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ShipmentRecord q = ((ResponseEntity<ShipmentRecord>) response).getBody();
        assertNotNull(q);
        assertEquals(strOrderId, q.getOrderId());
        
        //Test null as input
        response = shipmentcontroller.createShipmentRecord(null);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        //Test duplicate creation
        response = shipmentcontroller.createShipmentRecord(objShipment);
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        
        //Test invalid input
        objShipment.setOrderId(objShipment.getOrderId() + "_2");
        objShipment.setDeliveryDate(null);
        response = shipmentcontroller.createShipmentRecord(objShipment);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
    }

    @Test
    public void testUpdateShipment() throws Exception
    {
    	ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
        strURLZipkins = (String)ctx.getBean("getZipkinURL");
    	shipmentcontroller = new ShipmentController(null,requestMock,strURLZipkins);
    	
    	//Create valid shipment and set its status test to get it by that status
    	String strOrderId = "shipmentorder_104"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    	ShipmentRecord objShipment = new ShipmentRecord();
    	objShipment.setOrderId(strOrderId);
    	objShipment.setDeliveryDate(new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()));
    	ShipmentEventInfo eventinfo = new ShipmentEventInfo();
    	eventinfo.setDate(new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()));
    	eventinfo.setComments("New Shipment Creation Event From Test");
    	objShipment.addEvent(eventinfo);
    	DeliveryAddress objAddress = new DeliveryAddress();
    	objAddress.setStreet("200");
    	objAddress.setCity("San Fransisco");
    	objAddress.setState("CA");
    	objAddress.setPostalCode("94016");
    	objAddress.setSpecialInstructions("");
    	objShipment.setDeliveryAddress(objAddress);
    	objShipment.setContactName("ABC");
    	PhoneInfo objphone = new PhoneInfo();
    	objphone.setPhoneNumber("12345678");
    	objphone.setKind("");
    	objShipment.setPrimaryContactPhone(objphone);
    	objphone.setPhoneNumber("12345677");
    	objShipment.setAlternateContactPhone(objphone);
 	    
    	//Test successful creation and updation of shipment record
    	ResponseEntity response = shipmentcontroller.createShipmentRecord(objShipment);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        response = shipmentcontroller.getShipmentById(strOrderId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ShipmentRecord q = ((ResponseEntity<ShipmentRecord>) response).getBody();
        assertNotNull(q);
        assertEquals(strOrderId, q.getOrderId());
        
        PhoneInfo objNewPhone = new PhoneInfo();
        objNewPhone.setPhoneNumber("1111");
        objNewPhone.setKind("");
        objShipment.setPrimaryContactPhone(objNewPhone);
        response = shipmentcontroller.updateShipment(strOrderId, objShipment);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        response = shipmentcontroller.getShipmentById(strOrderId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        q = ((ResponseEntity<ShipmentRecord>) response).getBody();
        assertNotNull(q);
        assertEquals("1111", q.getPrimaryContactPhone().getPhoneNumber());
        
         //Test null and empty inputs
        response = shipmentcontroller.updateShipment(null,null);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        response = shipmentcontroller.updateShipment(null, objShipment);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        response = shipmentcontroller.updateShipment(strOrderId, null);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        response = shipmentcontroller.updateShipment("", objShipment);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        //Test updating Shipment with mismaching id
        response = shipmentcontroller.updateShipment("shipmentnonexisting", objShipment);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        //Test updating Shipment with nonexisting id
        String orgId = objShipment.getOrderId();
        objShipment.setOrderId("shipmentnonexisting");
        response = shipmentcontroller.updateShipment("shipmentnonexisting", objShipment);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        //Test updating Shipment with invalid field in Shipment object
        objShipment.setOrderId(orgId);
        objShipment.setDeliveryAddress(null);
        response = shipmentcontroller.updateShipment(strOrderId, objShipment);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	
    }
    
    @Test
    public void testGetShipmentById() throws Exception
    {
    	ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
        strURLZipkins = (String)ctx.getBean("getZipkinURL");
    	shipmentcontroller = new ShipmentController(null,requestMock,strURLZipkins);
    	
    	//Create valid shipment and set its status test to get it by that status
    	String strOrderId = "shipmentorder_101"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    	ShipmentRecord objShipment = new ShipmentRecord();
    	objShipment.setOrderId(strOrderId);
    	objShipment.setDeliveryDate(new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()));
    	ShipmentEventInfo eventinfo = new ShipmentEventInfo();
    	eventinfo.setDate(new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()));
    	eventinfo.setComments("New Shipment Creation Event From Test");
    	objShipment.addEvent(eventinfo);
    	DeliveryAddress objAddress = new DeliveryAddress();
    	objAddress.setStreet("200");
    	objAddress.setCity("San Fransisco");
    	objAddress.setState("CA");
    	objAddress.setPostalCode("94016");
    	objAddress.setSpecialInstructions("");
    	objShipment.setDeliveryAddress(objAddress);
    	objShipment.setContactName("ABC");
    	PhoneInfo objphone = new PhoneInfo();
    	objphone.setPhoneNumber("12345678");
    	objphone.setKind("");
    	objShipment.setPrimaryContactPhone(objphone);
    	objphone.setPhoneNumber("12345677");
    	objShipment.setAlternateContactPhone(objphone);
 	    
    	
    	ResponseEntity response = shipmentcontroller.createShipmentRecord(objShipment);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        response = shipmentcontroller.getShipmentById(strOrderId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ShipmentRecord q = ((ResponseEntity<ShipmentRecord>) response).getBody();
        assertNotNull(q);
        assertEquals(strOrderId, q.getOrderId());
        
        response = shipmentcontroller.getShipmentById(null);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        response = shipmentcontroller.getShipmentById("");
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
   }

    @Test
    public void testGetShipmentsByOrderStatus() throws Exception
    {
    	ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
        strURLZipkins = (String)ctx.getBean("getZipkinURL");
    	shipmentcontroller = new ShipmentController(null,requestMock,strURLZipkins);
    	
    	//Create valid shipment and set its status test to get it by that status
    	String strOrderId = "shipmentorder_100"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    	ShipmentRecord objShipment = new ShipmentRecord();
    	objShipment.setOrderId(strOrderId);
    	objShipment.setDeliveryDate(new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()));
    	ShipmentEventInfo eventinfo = new ShipmentEventInfo();
    	eventinfo.setDate(new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()));
    	eventinfo.setComments("New Shipment Creation Event From Test");
    	objShipment.addEvent(eventinfo);
    	DeliveryAddress objAddress = new DeliveryAddress();
    	objAddress.setStreet("200");
    	objAddress.setCity("San Fransisco");
    	objAddress.setState("CA");
    	objAddress.setPostalCode("94016");
    	objAddress.setSpecialInstructions("");
    	objShipment.setDeliveryAddress(objAddress);
    	objShipment.setContactName("ABC");
    	PhoneInfo objphone = new PhoneInfo();
    	objphone.setPhoneNumber("12345678");
    	objphone.setKind("");
    	objShipment.setPrimaryContactPhone(objphone);
    	objphone.setPhoneNumber("12345677");
    	objShipment.setAlternateContactPhone(objphone);
 	    
    	
    	ResponseEntity response = shipmentcontroller.createShipmentRecord(objShipment);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        response = shipmentcontroller.getShipments(OrderStatus.Created);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        List<ShipmentRecord> shipments = ((ResponseEntity<List<ShipmentRecord>>) response).getBody();
        boolean found = false;
        assertNotNull(shipments);
        for (ShipmentRecord shipmentobj : shipments)
        {
            if(shipmentobj.getOrderId() != null && shipmentobj.getOrderId().equals(strOrderId))
        	{
            	found = true;
            	break;
        	}
        }
        assertEquals(true, found);
        
        /*
        response = shipmentcontroller.getShipments(null);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        */
        //TODO uncomment when sending shipments as per order status input 
        //TODO incomplete as service is not returning list as per input status
        
       
   }

    @Test
    public void testDeleteShipment() throws Exception
    {
    	ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
        strURLZipkins = (String)ctx.getBean("getZipkinURL");
    	shipmentcontroller = new ShipmentController(null,requestMock,strURLZipkins);
    	
    	//test deleting Shipment with non existing Shipment id
        ResponseEntity response = shipmentcontroller.deleteShipment("abc");
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        //create one Shipment successfully and delete it by specifying same Shipment id
       	String strOrderId = "shipmentorder_106"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    	ShipmentRecord objShipment = new ShipmentRecord();
    	objShipment.setOrderId(strOrderId);
    	objShipment.setDeliveryDate(new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()));
    	ShipmentEventInfo eventinfo = new ShipmentEventInfo();
    	eventinfo.setDate(new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()));
    	eventinfo.setComments("New Shipment Creation Event From Test");
    	objShipment.addEvent(eventinfo);
    	DeliveryAddress objAddress = new DeliveryAddress();
    	objAddress.setStreet("200");
    	objAddress.setCity("San Fransisco");
    	objAddress.setState("CA");
    	objAddress.setPostalCode("94016");
    	objAddress.setSpecialInstructions("");
    	objShipment.setDeliveryAddress(objAddress);
    	objShipment.setContactName("ABC");
    	PhoneInfo objphone = new PhoneInfo();
    	objphone.setPhoneNumber("12345678");
    	objphone.setKind("");
    	objShipment.setPrimaryContactPhone(objphone);
    	objphone.setPhoneNumber("12345677");
    	objShipment.setAlternateContactPhone(objphone);
 	    
    	//Test successful creation of shipment record
    	response = shipmentcontroller.createShipmentRecord(objShipment);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        response = shipmentcontroller.getShipmentById(strOrderId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ShipmentRecord q = ((ResponseEntity<ShipmentRecord>) response).getBody();
        assertNotNull(q);
        assertEquals(strOrderId, q.getOrderId());
        
        response = shipmentcontroller.deleteShipment(strOrderId);
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        
        response = shipmentcontroller.getShipmentById(strOrderId);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
         
        //test deleting Shipment with id as blank
        response = shipmentcontroller.deleteShipment("");
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        //test deleting Shipment with null Shipment id
        response = shipmentcontroller.deleteShipment(null);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    
    @Test
    public void testAddEvent() throws Exception
    {
    	ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
        strURLZipkins = (String)ctx.getBean("getZipkinURL");
    	shipmentcontroller = new ShipmentController(null,requestMock,strURLZipkins);
    	
    	//Create valid shipment and set its status test to get it by that status
    	String strOrderId = "shipmentorder_105"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    	ShipmentRecord objShipment = new ShipmentRecord();
    	objShipment.setOrderId(strOrderId);
    	objShipment.setDeliveryDate(new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()));
    	ShipmentEventInfo eventinfo = new ShipmentEventInfo();
    	eventinfo.setDate(new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()));
    	eventinfo.setComments("New Shipment Creation Event From Test");
    	objShipment.addEvent(eventinfo);
    	DeliveryAddress objAddress = new DeliveryAddress();
    	objAddress.setStreet("200");
    	objAddress.setCity("San Fransisco");
    	objAddress.setState("CA");
    	objAddress.setPostalCode("94016");
    	objAddress.setSpecialInstructions("");
    	objShipment.setDeliveryAddress(objAddress);
    	objShipment.setContactName("ABC");
    	PhoneInfo objphone = new PhoneInfo();
    	objphone.setPhoneNumber("12345678");
    	objphone.setKind("");
    	objShipment.setPrimaryContactPhone(objphone);
    	objphone.setPhoneNumber("12345677");
    	objShipment.setAlternateContactPhone(objphone);
 	    
    	//Test successful creation and updation of shipment record
    	ResponseEntity response = shipmentcontroller.createShipmentRecord(objShipment);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        response = shipmentcontroller.getShipmentById(strOrderId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ShipmentRecord q = ((ResponseEntity<ShipmentRecord>) response).getBody();
        assertNotNull(q);
        assertEquals(strOrderId, q.getOrderId());
        
        ShipmentEventInfo eventinfo1 = new ShipmentEventInfo();
        eventinfo1.setDate(new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()));
        eventinfo1.setComments("Second Event From Test");
    	
    	response = shipmentcontroller.addEvent(strOrderId, eventinfo1);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        response = shipmentcontroller.getShipmentById(strOrderId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        q = ((ResponseEntity<ShipmentRecord>) response).getBody();
        assertNotNull(q);
        assertEquals(strOrderId, q.getOrderId());
        assertNotNull(q.getEvents());
        assertEquals(2, q.getEvents().size());
        assertEquals("Second Event From Test", q.getEvents().get(q.getEvents().size()-1).getComments());
        
         //Test null and empty inputs
        response = shipmentcontroller.addEvent(null,null);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        response = shipmentcontroller.addEvent(null, eventinfo1);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        response = shipmentcontroller.addEvent(strOrderId, null);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        response = shipmentcontroller.addEvent("", eventinfo1);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        //Test updating Shipment with non existing Shipment id
        response = shipmentcontroller.addEvent("shipmentnonexisting", eventinfo1);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        //Test updating Shipment with invalid field in Shipment object
        eventinfo1.setComments(null);
        response = shipmentcontroller.addEvent(strOrderId, eventinfo1);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

}
