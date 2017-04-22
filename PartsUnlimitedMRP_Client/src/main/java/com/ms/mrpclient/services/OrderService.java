package com.ms.mrpclient.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import io.opentracing.*;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapInjectAdapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ms.mrpclient.data.entities.OrderDetails;
import com.ms.mrpclient.data.entities.OrderEventInfo;
import com.ms.mrpclient.data.entities.OrderStatus;
import com.ms.mrpclient.data.entities.OrderUpdateInfo;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@Service
public class OrderService {
	private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
	@Value("${service.order.uri}")
	String ORDER_ENDPOINT;
	
	RestClientHelper restHelper;

	public OrderService() {
		restHelper = new RestClientHelper();
	}

	/**
	 * run 'GET /orders?status={status}&dealer={dealer]' and returns Order List
	 * 
	 * @param status
	 * @param dealer
	 * @return List<OrderDetails>
	 */
	@HystrixCommand(fallbackMethod = "getOrdersFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public List<OrderDetails> getOrders(final OrderStatus status, final String dealer,Tracer tracer, Span span) {
		logger.debug("getOrders() executed, status: " + status + ", dealer: " + dealer);
		try{
		List<OrderDetails> orderDetailsList = null;
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));
		
		// Creating HttpHeader
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setAll(map);
		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		// Preparing URI
		String statusURI = "status=";
		if (status != null) {
			statusURI = statusURI + status;
		}
		String dealerURI = "dealer=";
		if (dealer != null) {
			dealerURI = dealerURI + dealer;
		}

		// Executing Http Request
		ResponseEntity<?> result = restHelper.executeRequest(ORDER_ENDPOINT + "?" + statusURI + "&" + dealerURI,
				HttpMethod.GET, entity);

		// Parsing response body
		if (result != null && result.getStatusCode() == HttpStatus.OK) {
			orderDetailsList = restHelper.parseJSONtoCollectionObject((ResponseEntity<String>)result, new TypeReference<List<OrderDetails>>() {
			});
		}

		return orderDetailsList;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return null;
		} finally {
			span.finish();
		}
	}
    
	public List<OrderDetails> getOrdersFB(final OrderStatus status, final String dealer,Tracer tracer, Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("getOrdersFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}
	
	/**
	 * run 'GET /orders/{orderId}' and returns order details
	 * 
	 * @param orderId
	 * @return OrderDetails
	 */
	@HystrixCommand(fallbackMethod = "getOrderByIDFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public OrderDetails getOrderByID(final String orderId,Tracer tracer, Span span) {
		logger.debug("getOrderByID() executed, orderId: " + orderId);
		try{
		OrderDetails orderDetails = null;
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));

		// Creating HttpHeader
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setAll(map);
		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		// Executing Http Request
		ResponseEntity<?> result = restHelper.executeRequest(ORDER_ENDPOINT + "/" + orderId, HttpMethod.GET,
				entity);

		// Parsing response body
		if (result != null && result.getStatusCode() == HttpStatus.OK) {
			orderDetails = restHelper.parseJSONtoObject((ResponseEntity<String>)result, OrderDetails.class);
		}

		return orderDetails;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return null;
		} finally {
			span.finish();
		}
	}
	public OrderDetails getOrderByIDFB(final String orderId, Tracer tracer, Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("getOrderByIDFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}

	/**
	 * run 'PUT /orders/{orderId}' and update order details
	 * 
	 * @param orderId
	 * @param orderDetails
	 * @return
	 */
	@HystrixCommand(fallbackMethod = "updateOrderByIdFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public ResponseEntity<?> updateOrderById(String orderId, OrderDetails orderDetails,Tracer tracer,Span span) {
		logger.debug("updateOrderById() executed, orderId: " + orderId + "\nOrderDetails: " + orderDetails);
		try{
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));
		// Creating HttpHeader
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAll(map);
		// Creating Body
		String orderDetailsJsonStr = restHelper.parseObjectToJsonString(orderDetails);

		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>(orderDetailsJsonStr, headers);

		// Executing Http Request
		ResponseEntity<?> result = restHelper.executeRequest(ORDER_ENDPOINT + "/" + orderId, HttpMethod.PUT,
				entity);

		return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			span.finish();
		}
	}
	public ResponseEntity<?> updateOrderByIdFB(String orderId, OrderDetails orderDetails,Tracer tracer, Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("updateOrderByIdFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}

	/**
	 * run 'PUT /orders/{orderId}/status' and update order's status
	 * 
	 * @param orderId
	 * @param orderUpdateInfo
	 * @return
	 */
	@HystrixCommand(fallbackMethod = "updateStatusFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public ResponseEntity<?> updateStatus(String orderId, OrderUpdateInfo orderUpdateInfo, Tracer tracer, Span span) {
		logger.debug("updateStatus() executed, orderId: " + orderId + "\nOrderUpdateInfo: " + orderUpdateInfo);
		try{
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));
		// Creating HttpHeader
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAll(map);
		// Creating Body
		String orderUpdateInfoJsonStr = restHelper.parseObjectToJsonString(orderUpdateInfo);

		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>(orderUpdateInfoJsonStr, headers);

		// Executing Http Request
		ResponseEntity<?> result = restHelper.executeRequest(ORDER_ENDPOINT + "/" + orderId + "/status",
				HttpMethod.PUT, entity);

		return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			span.finish();
		}
	}
   
	public ResponseEntity<?> updateStatusFB(String orderId, OrderUpdateInfo orderUpdateInfo, Tracer tracer, Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("updateStatusFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}
	/**
	 * run 'DELETE /orders/{orderId}' and delete order
	 * 
	 * @param orderId
	 * @return
	 */
	@HystrixCommand(fallbackMethod = "deleteOrderByIDFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public ResponseEntity<?> deleteOrderByID(final String orderId, Tracer tracer,Span span) {
		try{
		logger.debug("deleteOrderByID() executed, orderId: " + orderId);
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));

		// Creating HttpHeader
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setAll(map);
		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		// Executing Http Request
		ResponseEntity<?> result = restHelper.executeRequest(ORDER_ENDPOINT + "/" + orderId, HttpMethod.DELETE,
				entity);
		return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			span.finish();
		}
	}
	
	public ResponseEntity<?> deleteOrderByIDFB(final String orderId, Tracer tracer, Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("deleteOrderByIDFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}

	/**
	 * run 'POST /orders/{orderId}/events' to add a new event to order
	 * 
	 * @param orderId
	 * @param orderEventInfo
	 * @return
	 */
	@HystrixCommand(fallbackMethod = "addEventFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public ResponseEntity<?> addEvent(String orderId, OrderEventInfo orderEventInfo, Tracer tracer, Span span) {
		logger.debug("addEvent() executed, orderId: " + orderId + "\nOrderEventInfo: " + orderEventInfo);
		try{
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));
		// Creating HttpHeader
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAll(map);
		// Creating Body
		String orderEventInfoJsonStr = restHelper.parseObjectToJsonString(orderEventInfo);

		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>(orderEventInfoJsonStr, headers);

		// Executing Http Request
		ResponseEntity<?> result = restHelper.executeRequest(ORDER_ENDPOINT + "/" + orderId + "/events",
				HttpMethod.POST, entity);

		return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			span.finish();
		}
	}
	public ResponseEntity<?> addEventFB(String orderId, OrderEventInfo orderEventInfo, Tracer tracer, Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("addEventFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}

	/**
	 * run 'POST /orders?fromQuote={quoteId}' to create a new order from quote
	 * id
	 * 
	 * @param quoteId
	 * @return
	 */
	@HystrixCommand(fallbackMethod = "createOrderFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public OrderDetails createOrder(final String quoteId,Tracer tracer, Span span) {
		logger.debug("createOrder() executed, quoteId: " + quoteId);
		try{
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));
		OrderDetails objOrderDetails = null;
		// Creating HttpHeader
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAll(map);
		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		// Executing Http Request
		ResponseEntity<?> result = restHelper.executeRequest(ORDER_ENDPOINT + "?fromQuote=" + quoteId,
				HttpMethod.POST, entity);
		if(result != null){
			objOrderDetails = restHelper.parseJSONtoObject((ResponseEntity<String>)result, OrderDetails.class);
			
		}
		return objOrderDetails;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return null;
		} finally {
			span.finish();
		}
	}
	
	public OrderDetails createOrderFB(final String quoteId,Tracer tracer,Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("createOrderFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}
}
