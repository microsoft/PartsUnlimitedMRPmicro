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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ms.mrpclient.data.entities.OrderStatus;
import com.ms.mrpclient.data.entities.ShipmentDetails;
import com.ms.mrpclient.data.entities.ShipmentEventInfo;
import com.ms.mrpclient.services.OrderService;
import com.ms.mrpclient.services.ShipmentService;

import brave.Tracer;
import brave.Span.Kind;
import brave.opentracing.BraveSpan;
import brave.opentracing.BraveTracer;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.okhttp3.OkHttpSender;

@Controller
public class ShipmentController {
	private static final Logger logger = LoggerFactory.getLogger(ShipmentController.class);
	@Autowired
	ShipmentService shipmentService;
	
	private String zipkinUrl;

	OkHttpSender sender;
	AsyncReporter<Span> reporter;
	io.opentracing.Tracer tracer;
	Tracer braveTracer;
    
	@Autowired
	public
	ShipmentController(ShipmentService shipmentService, @Value("${zipkin.mrpservice.uri}") String url) {
		this.shipmentService = shipmentService;		
		this.zipkinUrl=url;		
		sender = OkHttpSender.create(zipkinUrl);
		reporter = AsyncReporter.builder(sender).build();
		braveTracer = Tracer.newBuilder().localServiceName("mrp-client").reporter(reporter).build();
		tracer = BraveTracer.wrap(braveTracer);
	}

	@RequestMapping(value = "/shipments", method = RequestMethod.GET)
	@ResponseBody
	public List<ShipmentDetails> getShipments(
			@RequestParam(value = "status", required = false, defaultValue = "None") OrderStatus status) {
		logger.debug("getShipments() executed, OrderStatus: " + status);
		io.opentracing.Span span = tracer.buildSpan("GetShipments").withTag("Description", "Get Shipments").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		List<ShipmentDetails> shipmentDetailsList = shipmentService.getShipments(status,tracer,span);
		if (shipmentDetailsList == null) {
			logger.debug("shipmentDetailsList is null");
			shipmentDetailsList = new LinkedList<ShipmentDetails>();
		}

		return shipmentDetailsList;
	}

	@RequestMapping(value = "/shipments/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ShipmentDetails getShipment(@PathVariable String id) {
		logger.debug("getShipment() executed, orderId: " + id);
		io.opentracing.Span span = tracer.buildSpan("GetShipment").withTag("Description", "Get Shipment").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		ShipmentDetails shipmentDetails = shipmentService.getShipmentById(id,tracer,span);
		if (shipmentDetails == null) {
			logger.debug("shipmentDetails is null");
			shipmentDetails = new ShipmentDetails();
		}

		return shipmentDetails;
	}

	@RequestMapping(value = "/shipments", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> createShipmentRecord(@RequestBody ShipmentDetails shipmentDetails) {
		logger.debug("createShipmentRecord() executed, ShipmentDetails:\n" + shipmentDetails);
		io.opentracing.Span span = tracer.buildSpan("CreateShipment").withTag("Description", "Create Shipment").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		ResponseEntity<?> result = shipmentService.createShipmentRecord(shipmentDetails,tracer,span);
		return result;
	}

	@RequestMapping(value = "/shipments/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<?> updateShipment(@PathVariable String id,
			@RequestBody ShipmentDetails shipmentDetails) {
		logger.debug("updateShipment() executed, orderId: " + id + "\nShipmentDetails:\n" + shipmentDetails);
		io.opentracing.Span span = tracer.buildSpan("Updateshipemnt").withTag("Description", "Update shipment").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		ResponseEntity<?> result = shipmentService.updateShipment(id, shipmentDetails,tracer,span);
		return result;
	}

	@RequestMapping(value = "/shipments/{id}/events", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> addEvent(@PathVariable String id, @RequestBody ShipmentEventInfo shipmentEventInfo) {
		logger.debug("addEvent() executed, orderId: " + id + "\nShipmentEventInfo:\n" + shipmentEventInfo);
		io.opentracing.Span span = tracer.buildSpan("AddEventToShipment").withTag("Description", "Add event to shipment").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		ResponseEntity<?> result = shipmentService.addEvent(id, shipmentEventInfo,tracer,span);
		return result;
	}

	@RequestMapping(value = "/shipments/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<?> deleteShipment(@PathVariable String id) {
		logger.debug("deleteShipment() executed, orderId: " + id);
		io.opentracing.Span span = tracer.buildSpan("DeleteShipment").withTag("Description", "Delete Shipment").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		ResponseEntity<?> result = shipmentService.deleteShipment(id,tracer,span);
		return result;
	}
}
