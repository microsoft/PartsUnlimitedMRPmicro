package smpl.catalog;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import smpl.catalog.controller.CatalogController;
import smpl.catalog.model.Catalog;


@SuppressWarnings("unchecked")
@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
public class CatalogControllerTest {

	CatalogController catalogcontroller;
	ConfigurableApplicationContext ctx;
	MockHttpServletRequest requestMock = new MockHttpServletRequest();
	String strURLZipkins;
	
	@Test
    public void testAddCatalogItem() throws Exception
	{
		ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
		strURLZipkins = (String)ctx.getBean("getZipkinURL");
		catalogcontroller = new CatalogController(null,requestMock,strURLZipkins);
    	
    	Catalog catalog = new Catalog();
    	String skunum = "TEST-001-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()); 
    	catalog.setSkuNumber(skunum);
    	catalog.setDescription("Test catalog item type 1");
		catalog.setUnitPrice(100.50);
		catalog.setUnit(1);
		
		//Test creating new Catalog successfully
		ResponseEntity response = catalogcontroller.addCatalogItem(catalog);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
                
        //Test creating new Catalog with null input
        response = catalogcontroller.addCatalogItem(null);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        //Test creating duplicate Catalog 
        response = catalogcontroller.addCatalogItem(catalog);
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testUpdateCatalogItem() throws Exception
    {
    	ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
    	strURLZipkins = (String)ctx.getBean("getZipkinURL");
		catalogcontroller = new CatalogController(null,requestMock, strURLZipkins);
    	
    	
        //valid update
    	Catalog catalog = new Catalog();
    	String skunum = "TEST-002-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()); 
    	catalog.setSkuNumber(skunum);
    	catalog.setDescription("Test catalog item type 2");
		catalog.setUnitPrice(100.50);
		catalog.setUnit(1);
				
        //Test updating Catalog with non existing catalog id
    	ResponseEntity response = catalogcontroller.updateCatalogItem("abc", catalog);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        //Create new catalog successfully and test update for one of its field
		response = catalogcontroller.addCatalogItem(catalog);
		assertNotNull(response);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		
		catalog.setUnitPrice(200.50);
		response = catalogcontroller.updateCatalogItem(skunum, catalog);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        response = catalogcontroller.getCatalogItem(skunum);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Catalog q = ((ResponseEntity<Catalog>) response).getBody();
		assertNotNull(q);
		assertEquals(skunum, q.getSkuNumber());
		assertEquals(200.50, q.getUnitPrice(), 0.0);
		
        //Test updating Catalog with null as catalog id
        response = catalogcontroller.updateCatalogItem(null, catalog);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        //Test updating Catalog with null as catalog object
        response = catalogcontroller.updateCatalogItem(skunum, null);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        //Test updating Catalog with invalid field in catalog object
        catalog.setSkuNumber("");
        response = catalogcontroller.updateCatalogItem(skunum, catalog);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	
    }
    
    @Test
    public void testGetCatalogItemWithIDInput() throws Exception
    {
    	ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
    	strURLZipkins = (String)ctx.getBean("getZipkinURL");
		catalogcontroller = new CatalogController(null,requestMock, strURLZipkins);
    	
    	Catalog catalog = new Catalog();
    	String skunum = "TEST-003-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()); 
    	catalog.setSkuNumber(skunum);
    	catalog.setDescription("Test catalog item type 3");
		catalog.setUnitPrice(100.50);
		catalog.setUnit(1);
		
        //Create new catalog successfully and get it back by Id
		ResponseEntity response = catalogcontroller.addCatalogItem(catalog);
		assertNotNull(response);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		
		response = catalogcontroller.getCatalogItem(skunum);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Catalog q = ((ResponseEntity<Catalog>) response).getBody();
		assertNotNull(q);
		assertEquals(skunum, q.getSkuNumber());
            
		//Test updating Catalog with non existing catalog id
    	response = catalogcontroller.getCatalogItem("abc");
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        //Test updating Catalog with empty string as catalog id
    	response = catalogcontroller.getCatalogItem("");
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        //Test updating Catalog with null as catalog id
    	response = catalogcontroller.getCatalogItem(null);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}
    
    @Test
    public void testGetCatalogItem() throws Exception
    {
    	ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
    	strURLZipkins = (String)ctx.getBean("getZipkinURL");
		catalogcontroller = new CatalogController(null,requestMock, strURLZipkins);
    	
    	ResponseEntity response = catalogcontroller.getCatalogItems();
        assertNotNull(response);
        assertTrue("Valid Return",response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.NOT_FOUND);
        if(response.getStatusCode() == HttpStatus.OK)
        {
        	List<Catalog> items = ((ResponseEntity<List<Catalog>>) response).getBody();
            assertNotNull(items);
            assertNotEquals(0, items.size());
        }
   }
    
    @Test
    public void testRemoveCatalogItem() throws Exception
    {
    	ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
    	strURLZipkins = (String)ctx.getBean("getZipkinURL");
		catalogcontroller = new CatalogController(null,requestMock, strURLZipkins);
    	
    	
        //valid delete
    	Catalog catalog = new Catalog();
    	String skunum = "TEST-004-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()); 
    	catalog.setSkuNumber(skunum);
    	catalog.setDescription("Test catalog item type 4");
		catalog.setUnitPrice(100.50);
		catalog.setUnit(1);

		
        //Create new catalog successfully and test deleting that
		ResponseEntity response = catalogcontroller.addCatalogItem(catalog);
		assertNotNull(response);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		
		response = catalogcontroller.removeCatalogItem(skunum);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        response = catalogcontroller.getCatalogItem(skunum);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        //Test delete Catalog with non existing catalog id
    	response = catalogcontroller.removeCatalogItem("abc");
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        //Test delete Catalog with null as catalog id
        response = catalogcontroller.removeCatalogItem(null);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        //Test delete Catalog with empty string as catalog id
        response = catalogcontroller.removeCatalogItem("");
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
    } 
 }
