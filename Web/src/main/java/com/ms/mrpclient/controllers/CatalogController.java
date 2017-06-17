package com.ms.mrpclient.controllers;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ms.mrpclient.data.entities.Catalog;
import com.ms.mrpclient.services.CatalogService;

import brave.Tracer;
import brave.Span.Kind;
import brave.opentracing.BraveSpan;
import brave.opentracing.BraveTracer;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.okhttp3.OkHttpSender;

@Controller
public class CatalogController {
	private static final Logger logger = LoggerFactory.getLogger(CatalogController.class);

	private final CatalogService catalogService;
    
	private String zipkinUrl;
	
	OkHttpSender sender;
	AsyncReporter<Span> reporter;
	io.opentracing.Tracer tracer;
	Tracer braveTracer;
	
	@Autowired
	public CatalogController(CatalogService service, @Value("${zipkin.mrpservice.uri}") String url) {
		this.catalogService = service;
		this.zipkinUrl = url;
		sender = OkHttpSender.create(zipkinUrl);
		reporter = AsyncReporter.builder(sender).build();
		braveTracer = Tracer.newBuilder().localServiceName("mrp-client").reporter(reporter).build();
		tracer = BraveTracer.wrap(braveTracer);
	}

	@RequestMapping(value = "/catalog", method = RequestMethod.GET)
	@ResponseBody
	public List<Catalog> getCatalog() {
		logger.debug("getCatalog() executed");
		io.opentracing.Span span = tracer.buildSpan("GetCatalog").withTag("Description", "Get All Catalogs").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		List<Catalog> catalogList = catalogService.getCatalogList(tracer,span);
		
		  if (catalogList == null) { 
			  logger.debug("catalogList returned from service is null.");
			  catalogList = new LinkedList<Catalog>();
			  
		  }
		 
		return catalogList;
	}

	@RequestMapping(value = "/catalog/{skuNumber}", method = RequestMethod.GET)
	@ResponseBody
	public Catalog getCatalogByID(@PathVariable String skuNumber) {
		logger.debug("getCatalogByID() executed with skuNumber: " + skuNumber);
		io.opentracing.Span span = tracer.buildSpan("GetCatalogByID").withTag("Description", "Get Catalogs By Ids").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		Catalog catalog = catalogService.getCatalogByID(skuNumber,tracer, span);
		if (catalog == null) {
			logger.debug("catalog is null");
			catalog = new Catalog();
		}

		return catalog;
	}

	@RequestMapping(value = "/catalog/{skuNumber}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteCatalogByID(@PathVariable String skuNumber) {
		logger.debug("deleteCatalogByID() executed with skuNumber: " + skuNumber);
		io.opentracing.Span span = tracer.buildSpan("DeleteCatalog").withTag("Description", "Delete Catalogs").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		ResponseEntity<?> result = (ResponseEntity<?>) catalogService.deleteCatalogByID(skuNumber,tracer,span);
		return result;
	}

	@RequestMapping(value = "/catalog/{skuNumber}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<?> updateCatalog(@PathVariable String skuNumber, @RequestBody Catalog catalog) {
		logger.debug("updateCatalog() executed with skuNumber: " + skuNumber);
		io.opentracing.Span span = tracer.buildSpan("UpdateCatalog").withTag("Description", "Update Catalog").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		ResponseEntity<?> result = (ResponseEntity<?>) catalogService.updateCatalog(skuNumber, catalog,tracer, span);
		return result;
	}

	@RequestMapping(value = "/catalog", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> saveCatalog(@RequestBody Catalog catalog) {
		logger.debug("saveCatalog() executed");
		io.opentracing.Span span = tracer.buildSpan("SaveCatalog").withTag("Description", "Save Catalogs").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		ResponseEntity<?> result = (ResponseEntity<?>) catalogService.saveCatalog(catalog,tracer,span);
		return result;
	}
}
