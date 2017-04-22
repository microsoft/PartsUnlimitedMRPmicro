package com.ms.mrpclient.controllers;

import java.util.LinkedList;
import java.util.List;

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
import org.springframework.web.bind.annotation.ResponseBody;

import com.ms.mrpclient.data.entities.DealerDetails;
import com.ms.mrpclient.services.DealerService;

import brave.Tracer;
import brave.Span.Kind;
import brave.opentracing.BraveSpan;
import brave.opentracing.BraveTracer;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.okhttp3.OkHttpSender;

@Controller
public class DealerController {
	private static final Logger logger = LoggerFactory.getLogger(DealerController.class);
	@Autowired
	DealerService dealerService;
	private String zipkinUrl;

	OkHttpSender sender;
	AsyncReporter<Span> reporter;
	io.opentracing.Tracer tracer;
	Tracer braveTracer;
	
	@Autowired
	public
	DealerController(DealerService dealerService, @Value("${zipkin.mrpservice.uri}") String url) {
		this.dealerService = dealerService;		
		this.zipkinUrl=url;		
		sender = OkHttpSender.create(zipkinUrl);
		reporter = AsyncReporter.builder(sender).build();
		braveTracer = Tracer.newBuilder().localServiceName("mrp-client").reporter(reporter).build();
		tracer = BraveTracer.wrap(braveTracer);
	}
	

	@RequestMapping(value = "/dealer", method = RequestMethod.GET)
	@ResponseBody
	public List<DealerDetails> getDealers() {
		logger.debug("getDealers() executed");
		io.opentracing.Span span = tracer.buildSpan("GetDealers").withTag("Description", "Get Dealers").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		List<DealerDetails> dealersList = dealerService.getDealers(tracer,span);
		if (dealersList == null) {
			logger.debug("dealersList is null");
			dealersList = new LinkedList<DealerDetails>();
		}

		return dealersList;
	}

	@RequestMapping(value = "/dealer/{dealerName}", method = RequestMethod.GET)
	@ResponseBody
	public DealerDetails getDealer(@PathVariable String dealerName) {
		logger.debug("getDealer() executed with dealerName: " + dealerName);
		io.opentracing.Span span = tracer.buildSpan("GetDealer").withTag("Description", "Get Dealer").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		DealerDetails dealerDetails = dealerService.getDealer(dealerName, tracer,span);
		if (dealerDetails == null) {
			logger.debug("dealerDetails is null");
		}
		return dealerDetails;
	}

	@RequestMapping(value = "/dealer/{dealerName}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<?> deleteDealer(@PathVariable String dealerName) {
		logger.debug("deleteDealer() executed with dealerName: " + dealerName);
		io.opentracing.Span span = tracer.buildSpan("DeleteDealer").withTag("Description", "Delete Dealer").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		ResponseEntity<?> result = dealerService.deleteDealer(dealerName,tracer,span);
		return result;
	}

	@RequestMapping(value = "/dealer/{dealerName}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<?> updateDealer(@PathVariable String dealerName, @RequestBody DealerDetails dealerDetails) {
		logger.debug("updateDealer() executed with dealerName & dealerDetails: " + dealerName + "\n" + dealerDetails);
		io.opentracing.Span span = tracer.buildSpan("UpdateDealer").withTag("Description", "Update Dealer").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);		
		ResponseEntity<?> result = dealerService.updateDealer(dealerName, dealerDetails,tracer, span);
		return result;
	}

	@RequestMapping(value = "/dealer", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> createDealer(@RequestBody DealerDetails dealerDetails) {
		
		logger.debug("createDealer() executed with dealerDetails: " + dealerDetails);		
		dealerDetails.setTimestamp(1);
		dealerDetails.setMachine(0);
		dealerDetails.setIncrement(0);
		dealerDetails.setCreationTime("0001-01-01T00:00:00Z");
		io.opentracing.Span span = tracer.buildSpan("CreateDealer").withTag("Description", "Create Dealer").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		ResponseEntity<?> result = dealerService.createDealer(dealerDetails,tracer,span);
		return result;
		
	}
}
