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

import com.fasterxml.jackson.core.type.TypeReference;
import com.ms.mrpclient.controllers.ShipmentController;
import com.ms.mrpclient.data.entities.OrderStatus;
import com.ms.mrpclient.data.entities.ShipmentDetails;
import com.ms.mrpclient.data.entities.ShipmentEventInfo;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import io.opentracing.*;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapInjectAdapter;

@Service
public class ShipmentService {
	private static final Logger logger = LoggerFactory.getLogger(ShipmentController.class);
	@Value("${service.shipment.uri}")
	String SHIPMENT_ENDPOINT;

	RestClientHelper restHelper;

	public ShipmentService() {
		restHelper = new RestClientHelper();
	}

	/**
	 * run 'GET /shipments?status={status}' and returns shipment details list
	 * 
	 * @param status
	 * @return List<ShipmentDetails>
	 */
	@HystrixCommand(fallbackMethod = "getShipmentsFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public List<ShipmentDetails> getShipments(OrderStatus status,Tracer tracer,Span span) {
		logger.debug("getShipments() executed, OrderStatus: " + status);
		try{
		List<ShipmentDetails> shipmentDetailsList = null;
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));
		// Creating HttpHeader
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setAll(map);

		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		// Executing Http Request
		ResponseEntity<?> result = restHelper.executeRequest(SHIPMENT_ENDPOINT + "?" + "status=" + status,
				HttpMethod.GET, entity);

		// Parsing response body
		if (result != null && result.getStatusCode() == HttpStatus.OK) {
			shipmentDetailsList = restHelper.parseJSONtoCollectionObject((ResponseEntity<String>)result,
					new TypeReference<List<ShipmentDetails>>() {
					});
		}

		return shipmentDetailsList;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return null;
		} finally {
			span.finish();
		}
	}
	
	public List<ShipmentDetails> getShipmentsFB(OrderStatus status,Tracer tracer,Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("getShipmentsFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}

	/**
	 * run 'GET /shipments/{orderId}' and returns shipment details
	 * 
	 * @param Id
	 * @return ShipmentDetails
	 */
	@HystrixCommand(fallbackMethod = "getShipmentByIdFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public ShipmentDetails getShipmentById(final String id,Tracer tracer,Span span) {
		logger.debug("getShipmentById() executed, orderId: " + id);
		try{
		ShipmentDetails shipmentDetails = null;
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));
		// Creating HttpHeader
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setAll(map);

		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		// Executing Http Request
		ResponseEntity<?> result = restHelper.executeRequest(SHIPMENT_ENDPOINT + "/" + id, HttpMethod.GET, entity);

		// Parsing response body
		if (result != null && result.getStatusCode() == HttpStatus.OK) {
			shipmentDetails = restHelper.parseJSONtoObject((ResponseEntity<String>)result, ShipmentDetails.class);
		}

		return shipmentDetails;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return null;
		} finally {
			span.finish();
		}
	}
    
	public ShipmentDetails getShipmentByIdFB(final String id,Tracer tracer,Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("getShipmentByIdFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}
	/**
	 * run 'POST /shipments' to create shipment record
	 * 
	 * @param shipmentDetails
	 * @return
	 */
	@HystrixCommand(fallbackMethod = "createShipmentRecordFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public ResponseEntity<?> createShipmentRecord(ShipmentDetails shipmentDetails,Tracer tracer,Span span) {
		logger.debug("createShipmentRecord() executed, ShipmentDetails:\n" + shipmentDetails);
		try{
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));
		// Creating HttpHeader
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAll(map);

		// Creating Body
		String shipmentDetailsJsonStr = restHelper.parseObjectToJsonString(shipmentDetails);

		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>(shipmentDetailsJsonStr, headers);

		// Executing Http Request
		ResponseEntity<?> result = restHelper.executeRequest(SHIPMENT_ENDPOINT, HttpMethod.POST, entity);

		return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			span.finish();
		}
	}
	
	public ResponseEntity<?> createShipmentRecordFB(ShipmentDetails shipmentDetails,Tracer tracer,Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("createShipmentRecordFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}

	/**
	 * run 'PUT /shipments/{orderId}' to update shipment details
	 * 
	 * @param id
	 * @param shipmentDetails
	 * @return
	 */
	@HystrixCommand(fallbackMethod = "updateShipmentFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public ResponseEntity<?> updateShipment(final String id, ShipmentDetails shipmentDetails,Tracer tracer,Span span) {
		logger.debug("updateShipment() executed, orderId: " + id + "\nShipmentDetails:\n" + shipmentDetails);
		try{
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));
		// Creating HttpHeader
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAll(map);

		// Creating Body
		String shipmentDetailsJsonStr = restHelper.parseObjectToJsonString(shipmentDetails);

		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>(shipmentDetailsJsonStr, headers);

		// Executing Http Request
		ResponseEntity<?> result = restHelper.executeRequest(SHIPMENT_ENDPOINT + "/" + id, HttpMethod.PUT, entity);

		return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			span.finish();
		}
	}
	public ResponseEntity<?> updateShipmentFB(final String id, ShipmentDetails shipmentDetails,Tracer tracer,Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("updateShipmentFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}

	/**
	 * run 'POST /shipments/{orderId}/events' to add shipment event
	 * 
	 * @param id
	 * @param shipmentEventInfo
	 * @return
	 */
	@HystrixCommand(fallbackMethod = "addEventFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public ResponseEntity<?> addEvent(final String id, ShipmentEventInfo shipmentEventInfo,Tracer tracer,Span span) {
		logger.debug("addEvent() executed, orderId: " + id + "\nShipmentEventInfo:\n" + shipmentEventInfo);
		try{
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));
		
		// Creating HttpHeader
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAll(map);

		// Creating Body
		String shipmentEventInfoJsonStr = restHelper.parseObjectToJsonString(shipmentEventInfo);

		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>(shipmentEventInfoJsonStr, headers);

		// Executing Http Request
		ResponseEntity<?> result = restHelper.executeRequest(SHIPMENT_ENDPOINT + "/" + id + "/events",
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
	public ResponseEntity<?> addEventFB(final String id, ShipmentEventInfo shipmentEventInfo,Tracer tracer,Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("addEventFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}

	/**
	 * run 'DELETE /shipments/{orderId}' to delete shipment record
	 * 
	 * @param id
	 * @return
	 */
	@HystrixCommand(fallbackMethod = "deleteShipmentFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public ResponseEntity<?> deleteShipment(final String id,Tracer tracer,Span span) {
		logger.debug("deleteShipment() executed, orderId: " + id);
		try{
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setAll(map);

		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		headers.setAll(map);
		
		// Executing Http Request
		ResponseEntity<?> result = restHelper.executeRequest(SHIPMENT_ENDPOINT + "/" + id, HttpMethod.DELETE,
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
	
	public ResponseEntity<?> deleteShipmentFB(final String id,Tracer tracer,Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("deleteShipmentFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}
}
