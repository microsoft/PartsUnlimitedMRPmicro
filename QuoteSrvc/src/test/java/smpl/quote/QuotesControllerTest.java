package smpl.quote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import smpl.quote.controller.QuoteController;
import smpl.quote.model.Quote;
import smpl.quote.model.QuoteItemInfo;
import smpl.quote.repository.service.QuoteService;

@SuppressWarnings("unchecked")
@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
public class QuotesControllerTest {

	QuoteController quotecontroller;
	QuoteService QuoteServiceMock;
	MongoOperations mongoOperation;
	ConfigurableApplicationContext ctx;
	MockHttpServletRequest requestMock = new MockHttpServletRequest();
	String strURLZipkins;

	@Test
	public void testCreateQuote() throws Exception {
		ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
		strURLZipkins = (String) ctx.getBean("getZipkinURL");
		quotecontroller = new QuoteController(null, requestMock, strURLZipkins);

		Quote quote = new Quote();
		String QuoteId = "quote-1000" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		quote.setQuoteId(QuoteId);
		quote.setValidUntil("2017-02-28T20:43:37+0000");
		quote.setCustomerName("Cust0");
		quote.setDealerName("Dealer1");
		QuoteItemInfo objQuoteInfo = new QuoteItemInfo();
		objQuoteInfo.setSkuNumber("1");
		objQuoteInfo.setAmount(1000.00);
		ArrayList arlstQuoteitems = new ArrayList();
		arlstQuoteitems.add(objQuoteInfo);
		quote.setQuoteItems(arlstQuoteitems);
		quote.setTotalCost(900.00);
		quote.setDiscount(100.00);
		quote.setCity("Los Angeles");
		quote.setPostalCode("90070");
		quote.setState("California");

		// Test creating new Quote successfully
		ResponseEntity response = quotecontroller.createQuote(quote);
		assertNotNull(response);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		
		// Test creating new Quote with null input
		response = quotecontroller.createQuote(null);
		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

		// Test creating duplicate Quote
		response = quotecontroller.createQuote(quote);
		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	public void testUpdateQuote() throws Exception {
		ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
		strURLZipkins = (String) ctx.getBean("getZipkinURL");
		quotecontroller = new QuoteController(null, requestMock, strURLZipkins);
		
		// valid update
		Quote quote = new Quote();
		String QuoteId = "quote-1001" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		quote.setQuoteId(QuoteId);
		quote.setValidUntil("2017-03-01T20:43:37+0000");
		quote.setCustomerName("Cust1");
		quote.setDealerName("Dealer2");
		QuoteItemInfo objQuoteInfo = new QuoteItemInfo();
		objQuoteInfo.setSkuNumber("2");
		objQuoteInfo.setAmount(2000.00);
		ArrayList arlstQuoteitems = new ArrayList();
		arlstQuoteitems.add(objQuoteInfo);
		quote.setQuoteItems(arlstQuoteitems);
		quote.setTotalCost(1900.00);
		quote.setDiscount(100.00);
		quote.setCity("Los Angeles");
		quote.setPostalCode("90070");
		quote.setState("California");

		// Test updating Quote with non existing quote id
		ResponseEntity response = quotecontroller.updateQuote("quote-1010", quote);
		assertNotNull(response);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

		// Create new quote successfully and test update for one of its field
		response = quotecontroller.createQuote(quote);
		assertNotNull(response);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		
		quote.setValidUntil("2017-03-15T20:43:37+0000");
		response = quotecontroller.updateQuote(QuoteId, quote);
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		// Test updating Quote with null as quote id
		response = quotecontroller.updateQuote(null, quote);
		assertNotNull(response);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

		// Test updating Quote with null as quote object
		response = quotecontroller.updateQuote(QuoteId, null);
		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

		// Test updating Quote with invalid field in quote object
		quote.setDealerName("");
		response = quotecontroller.updateQuote(QuoteId, quote);
		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

	}

	@Test
	public void testGetQuoteById() throws Exception {
		ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
		strURLZipkins = (String) ctx.getBean("getZipkinURL");
		quotecontroller = new QuoteController(null, requestMock, strURLZipkins);

		// create one quote successfully and get it back by specifying same id
		Quote quote = new Quote();
		String QuoteId = "quote-1002" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		quote.setQuoteId(QuoteId);
		quote.setValidUntil("2017-03-01T20:43:37+0000");
		quote.setCustomerName("Cust2");
		quote.setDealerName("Dealer2");
		QuoteItemInfo objQuoteInfo = new QuoteItemInfo();
		objQuoteInfo.setSkuNumber("2");
		objQuoteInfo.setAmount(2000.00);
		ArrayList arlstQuoteitems = new ArrayList();
		arlstQuoteitems.add(objQuoteInfo);
		quote.setQuoteItems(arlstQuoteitems);
		quote.setTotalCost(1900.00);
		quote.setDiscount(100.00);
		quote.setCity("Los Angeles");
		quote.setPostalCode("90070");
		quote.setState("California");

		ResponseEntity response = quotecontroller.createQuote(quote);
		assertNotNull(response);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		
		response = quotecontroller.getQuoteById(QuoteId);
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		Quote q = ((ResponseEntity<Quote>) response).getBody();
		assertNotNull(q);
		assertEquals(QuoteId, q.getQuoteId());

		// Test updating Catalog with non existing catalog id
		response = quotecontroller.getQuoteById("quote-1011");
		assertNotNull(response);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

	}

	@Test
	public void testGetQuotesByCustomerName() throws Exception {
		ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
		strURLZipkins = (String) ctx.getBean("getZipkinURL");
		quotecontroller = new QuoteController(null, requestMock, strURLZipkins);

		// Test getting Quote with non existing customer name
		ResponseEntity response = quotecontroller.getQuotesByCustomerName("abc");
		assertNotNull(response);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

		// create one quote successfully and get it back by specifying same
		// customer name
		Quote quote = new Quote();
		String QuoteId = "quote-1003" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String customername = "UniqueCust" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		quote.setQuoteId(QuoteId);
		quote.setValidUntil("2017-03-01T20:43:37+0000");
		quote.setCustomerName(customername);
		quote.setDealerName("Dealer2");
		QuoteItemInfo objQuoteInfo = new QuoteItemInfo();
		objQuoteInfo.setSkuNumber("2");
		objQuoteInfo.setAmount(2000.00);
		ArrayList arlstQuoteitems = new ArrayList();
		arlstQuoteitems.add(objQuoteInfo);
		quote.setQuoteItems(arlstQuoteitems);
		quote.setTotalCost(1900.00);
		quote.setDiscount(100.00);
		quote.setCity("Los Angeles");
		quote.setPostalCode("90070");
		quote.setState("California");

		response = quotecontroller.createQuote(quote);
		assertNotNull(response);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		
		response = quotecontroller.getQuotesByCustomerName(customername);
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		List<Quote> quotes = ((ResponseEntity<List<Quote>>) response).getBody();
		assertNotNull(quotes);
		assertEquals(1, quotes.size());

	}

	@Test
	public void testDeleteQuote() throws Exception {
		ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
		strURLZipkins = (String) ctx.getBean("getZipkinURL");
		quotecontroller = new QuoteController(null, requestMock, strURLZipkins);

		// test deleting quote with non existing quote id
		ResponseEntity response = quotecontroller.deleteQuote("quote-1012");
		assertNotNull(response);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

		// create one quote successfully and delete it by specifying same quote
		// id
		Quote quote = new Quote();
		String QuoteId = "quote-1004" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		quote.setQuoteId(QuoteId);
		quote.setValidUntil("2017-03-01T20:43:37+0000");
		quote.setCustomerName("Cust4");
		quote.setDealerName("Dealer3");
		QuoteItemInfo objQuoteInfo = new QuoteItemInfo();
		objQuoteInfo.setSkuNumber("2");
		objQuoteInfo.setAmount(2000.00);
		ArrayList arlstQuoteitems = new ArrayList();
		arlstQuoteitems.add(objQuoteInfo);
		quote.setQuoteItems(arlstQuoteitems);
		quote.setTotalCost(1900.00);
		quote.setDiscount(100.00);
		quote.setCity("Los Angeles");
		quote.setPostalCode("90070");
		quote.setState("California");

		response = quotecontroller.createQuote(quote);
		assertNotNull(response);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		
		response = quotecontroller.deleteQuote(QuoteId);
		assertNotNull(response);
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

		// test deleting quote with id as blank
		response = quotecontroller.deleteQuote("");
		assertNotNull(response);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

		// test deleting quote with null quote id
		response = quotecontroller.deleteQuote(null);
		assertNotNull(response);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

}
