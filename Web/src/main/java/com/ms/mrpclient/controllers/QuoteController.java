package com.ms.mrpclient.controllers;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;

import com.ms.mrpclient.data.entities.Catalog;
import com.ms.mrpclient.data.entities.QuoteDetails;
import com.ms.mrpclient.services.QuoteService;

import brave.Tracer;
import brave.Span.Kind;
import brave.opentracing.BraveSpan;
import brave.opentracing.BraveTracer;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.okhttp3.OkHttpSender;

@Controller
public class QuoteController {
	private static final Logger logger = LoggerFactory.getLogger(QuoteController.class);
	@Autowired
	QuoteService quoteService;

	private String zipkinUrl;

	OkHttpSender sender;
	AsyncReporter<Span> reporter;
	io.opentracing.Tracer tracer;
	Tracer braveTracer;
    
	@Autowired
	public
	QuoteController(QuoteService quoteService, @Value("${zipkin.mrpservice.uri}") String url) {
		this.quoteService = quoteService;		
		this.zipkinUrl=url;		
		sender = OkHttpSender.create(zipkinUrl);
		reporter = AsyncReporter.builder(sender).build();
		braveTracer = Tracer.newBuilder().localServiceName("mrp-client").reporter(reporter).build();
		tracer = BraveTracer.wrap(braveTracer);
	}

	@RequestMapping(value = "/quote", method = RequestMethod.GET)
	@ResponseBody
	public List<QuoteDetails> getQuotesByName(@RequestParam(value = "name") String customerName) {
		logger.debug("getQuotesByName() executed, customerName: " + customerName);
		io.opentracing.Span span = tracer.buildSpan("GetQuoteByName").withTag("Description", "Get quotes by name").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		List<QuoteDetails> quoteDetailsList = quoteService.getQuotesByName(customerName,tracer,span);
		if (quoteDetailsList == null) {
			logger.debug("quoteDetailsList is null");
			quoteDetailsList = new LinkedList<QuoteDetails>();
		}

		return quoteDetailsList;
	}	

	@RequestMapping(value = "/quote/bydealer/{dealername}", method = RequestMethod.GET)
	@ResponseBody
	public List<String> getQuotesByDealerName(@PathVariable(value = "dealername") String dealername) {
		logger.debug("getQuotesByDealerName() executed, dealername: " + dealername);
		io.opentracing.Span span = tracer.buildSpan("GetQuoteByDealerName").withTag("Description", "Get quote by dealer name").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		List<String> quoteDetailsList = quoteService.getQuotesByDealerName(dealername,tracer,span);
		if (quoteDetailsList == null) {
			logger.debug("quoteIdList is null");
			quoteDetailsList = new LinkedList<String>();
		}

		return quoteDetailsList;
	}


	@RequestMapping(value = "/quote/{quoteID}", method = RequestMethod.GET)
	@ResponseBody
	public QuoteDetails getQuoteByID(@PathVariable(value = "quoteID") String quoteID) {
		logger.debug("getQuoteByID() executed, quoteID: " + quoteID);
		io.opentracing.Span span = tracer.buildSpan("GetQuoteByID").withTag("Description", "Get quote by ID").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		QuoteDetails quoteDetails = quoteService.getQuoteByID(quoteID,tracer,span);
		if (quoteDetails == null) {
			logger.debug("quoteDetails is null");
			quoteDetails = new QuoteDetails();
		}

		return quoteDetails;
	}

	@RequestMapping(value = "/quote/{quoteID}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<?> deleteQuoteByID(@PathVariable(value = "quoteID") String quoteID) {
		logger.debug("deleteQuoteByID() executed, quoteID: " + quoteID);
		io.opentracing.Span span = tracer.buildSpan("DeleteQuote").withTag("Description", "Delete Quote").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		ResponseEntity<?> result = quoteService.deleteQuoteByID(quoteID,tracer,span);
		return result;
	}

	@RequestMapping(value = "/quote/{quoteID}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<?> updateQuote(@PathVariable(value = "quoteID") String quoteID,
			@RequestBody QuoteDetails quoteDetails) {
		logger.debug("updateQuote() executed, quoteID: " + quoteID + "\nQuoteDetails:\n" + quoteDetails);
		io.opentracing.Span span = tracer.buildSpan("UpdateQuote").withTag("Description", "Update quote").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		ResponseEntity<?> result = quoteService.updateQuote(quoteID, quoteDetails,tracer,span);
		return result;
	}

	@RequestMapping(value = "/quote", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> createQuote(@RequestBody QuoteDetails quoteDetails) {
		logger.debug("createQuote() executed, QuoteDetails:\n" + quoteDetails);
		io.opentracing.Span span = tracer.buildSpan("CreateQuote").withTag("Description", "Create quote").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		ResponseEntity<?> result = quoteService.createQuote(quoteDetails,tracer,span);
		return result;
	}
}
