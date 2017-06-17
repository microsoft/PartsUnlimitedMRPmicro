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

import com.ms.mrpclient.data.entities.OrderDetails;
import com.ms.mrpclient.data.entities.OrderEventInfo;
import com.ms.mrpclient.data.entities.OrderStatus;
import com.ms.mrpclient.data.entities.OrderUpdateInfo;
import com.ms.mrpclient.services.OrderService;

import brave.Tracer;
import brave.Span.Kind;
import brave.opentracing.BraveSpan;
import brave.opentracing.BraveTracer;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.okhttp3.OkHttpSender;

@Controller
public class OrderController {
	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
	@Autowired
	OrderService orderService;
	
	private String zipkinUrl;

	OkHttpSender sender;
	AsyncReporter<Span> reporter;
	io.opentracing.Tracer tracer;
	Tracer braveTracer;
    
	@Autowired
	public
	OrderController(OrderService orderService, @Value("${zipkin.mrpservice.uri}") String url) {
		this.orderService = orderService;		
		this.zipkinUrl=url;		
		sender = OkHttpSender.create(zipkinUrl);
		reporter = AsyncReporter.builder(sender).build();
		braveTracer = Tracer.newBuilder().localServiceName("mrp-client").reporter(reporter).build();
		tracer = BraveTracer.wrap(braveTracer);
	}

	@RequestMapping(value = "/orders", method = RequestMethod.GET)
	@ResponseBody
	public List<OrderDetails> getOrders(@RequestParam(value = "status") OrderStatus status,
			@RequestParam(value = "dealer") String dealer) {
		logger.debug("getOrders() executed, status: " + status + ", dealer: " + dealer);
		io.opentracing.Span span = tracer.buildSpan("GetOrders").withTag("Description", "Get Orders").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		List<OrderDetails> orderDetailsList = orderService.getOrders(status, dealer,tracer,span);
		if (orderDetailsList == null) {
			logger.debug("orderDetailsList is null");
			orderDetailsList = new LinkedList<OrderDetails>();
		}

		return orderDetailsList;
	}

	@RequestMapping(value = "/orders/{orderID}", method = RequestMethod.GET)
	@ResponseBody
	public OrderDetails getOrderByID(@PathVariable(value = "orderID") String orderID) {
		logger.debug("getOrderByID() executed, orderID: " + orderID);
		io.opentracing.Span span = tracer.buildSpan("GetOrderById").withTag("Description", "Get order by ID").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		OrderDetails orderDetails = orderService.getOrderByID(orderID,tracer,span);
		if (orderDetails == null) {
			logger.debug("orderDetails is null");
			orderDetails = new OrderDetails();
		}

		return orderDetails;
	}

	@RequestMapping(value = "/orders/{orderID}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<?> updateOrder(@PathVariable(value = "orderID") String orderID,
			@RequestBody OrderDetails orderDetails) {
		logger.debug("updateOrder() executed, orderID: " + orderID + "\nOrderDetails: " + orderDetails);
		io.opentracing.Span span = tracer.buildSpan("UpdateOrder").withTag("Description", "Update order By Id").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		ResponseEntity<?> result = orderService.updateOrderById(orderID, orderDetails, tracer,span);
		return result;
	}

	@RequestMapping(value = "/orders/{orderId}/status", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<?> updateStatus(@PathVariable(value = "orderID") String orderID,
			@RequestBody OrderUpdateInfo orderUpdateInfo) {
		logger.debug("updateStatus() executed, orderID: " + orderID + "\nOrderUpdateInfo: " + orderUpdateInfo);
		io.opentracing.Span span = tracer.buildSpan("UpdateorderStatus").withTag("Description", "Update Order Status").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		ResponseEntity<?> result = orderService.updateStatus(orderID, orderUpdateInfo, tracer,span);
		return result;
	}

	@RequestMapping(value = "/orders/{orderID}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<?> deleteOrderByID(@PathVariable(value = "orderID") String orderID) {
		logger.debug("deleteOrderByID() executed, orderID: " + orderID);
		io.opentracing.Span span = tracer.buildSpan("DeleteOrder").withTag("Description", "Delete Order").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		ResponseEntity<?> result = orderService.deleteOrderByID(orderID,tracer,span);
		return result;
	}

	@RequestMapping(value = "/orders/{orderId}/events", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> addEvent(@PathVariable(value = "orderID") String orderID,
			@RequestBody OrderEventInfo orderEventInfo) {
		logger.debug("addEvent() executed, orderID: " + orderID + "\nOrderEventInfo: " + orderEventInfo);
		io.opentracing.Span span = tracer.buildSpan("AddEventToOrder").withTag("Description", "Add Event to order").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		ResponseEntity<?> result = orderService.addEvent(orderID, orderEventInfo,tracer,span);
		return result;
	}

	@RequestMapping(value = "/orders", method = RequestMethod.POST)
	@ResponseBody
	public OrderDetails createOrder(@RequestParam(value = "fromQuote") String quoteId) {
		logger.debug("createOrder() executed, quoteId: " + quoteId);
		io.opentracing.Span span = tracer.buildSpan("CreateOrder").withTag("Description", "Create Order").start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.CLIENT);
		OrderDetails result = orderService.createOrder(quoteId,tracer,span);
		return result;
	}
}
