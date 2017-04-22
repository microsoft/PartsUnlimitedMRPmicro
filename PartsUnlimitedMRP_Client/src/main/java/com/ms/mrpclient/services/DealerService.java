package com.ms.mrpclient.services;

import java.util.Arrays;
import java.util.List;

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
import com.ms.mrpclient.data.entities.DealerDetails;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import io.opentracing.*;


@Service
public class DealerService {
	private static final Logger logger = LoggerFactory.getLogger(DealerService.class);
	@Value("${service.dealer.uri}")
	String DEALER_ENDPOINT;
	
	RestClientHelper restHelper;

	public DealerService() {
		restHelper = new RestClientHelper();
	}

	/**
	 * run 'GET /dealer' and returns list of dealers
	 * 
	 * @return List<DealerDetails>
	 */
	@HystrixCommand(fallbackMethod = "getDealersFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public List<DealerDetails> getDealers(Tracer tracer, Span span) {
		logger.debug("getDealers() executed");
		try{
		List<DealerDetails> dealerDetailsList = null;

		// Creating HttpHeader
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		// Executing Http Request
		ResponseEntity<?> result = restHelper.executeRequest(DEALER_ENDPOINT, HttpMethod.GET, entity);

		// Parsing response body
		if (result != null && result.getStatusCode() == HttpStatus.OK) {
			dealerDetailsList = restHelper.parseJSONtoCollectionObject((ResponseEntity<String>)result,
					new TypeReference<List<DealerDetails>>() {
					});
		}

		return dealerDetailsList;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return null;
		} finally {
			span.finish();
		}
	}
	public List<DealerDetails> getDealersFB(Tracer tracer, Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("getDealersFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}

	/**
	 * run 'GET /dealer/{dealerName}' and returns Dealer details
	 * 
	 * @param dealerName
	 * @return DealerDetails
	 */
	@HystrixCommand(fallbackMethod = "getDealerFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public DealerDetails getDealer(final String dealerName, Tracer tracer, Span span) {
		logger.debug("getDealer() executed with dealerName: " + dealerName);
		try{
		DealerDetails dealerDetails = null;

		// Creating HttpHeader
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		// Executing Http Request
		ResponseEntity<?> result = restHelper.executeRequest(DEALER_ENDPOINT + "/" + dealerName, HttpMethod.GET,
				entity);

		// Parsing response body
		if (result != null && result.getStatusCode() == HttpStatus.OK) {
			dealerDetails = restHelper.parseJSONtoObject((ResponseEntity<String>)result, DealerDetails.class);
		}

		return dealerDetails;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return null;
		} finally {
			span.finish();
		}
	}
	public DealerDetails getDealerFB(final String dealerName, Tracer tracer, Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("getDealerFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}
	
	/**
	 * run 'PUT /dealer/{dealerName}' to update Dealer details
	 * 
	 * @param dealerName
	 * @param dealerDetails
	 * @return
	 */
	@HystrixCommand(fallbackMethod = "updateDealerFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public ResponseEntity<?> updateDealer(String dealerName, DealerDetails dealerDetails, Tracer tracer, Span span) {
		logger.debug("updateDealer() executed with dealerName & dealerDetails: " + dealerName + "\n" + dealerDetails);
		try{
		// Creating HttpHeader
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// Creating Body
		String delearDetailsJsonStr = restHelper.parseObjectToJsonString(dealerDetails);
		

		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>(delearDetailsJsonStr, headers);

		// Executing Http Request
		ResponseEntity<?> result = restHelper.executeRequest(DEALER_ENDPOINT + "/" + dealerName, HttpMethod.PUT,
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
	public ResponseEntity<?> updateDealerFB(String dealerName, DealerDetails dealerDetails, Tracer tracer, Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("updateDealerFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}

	/**
	 * run 'DELETE /dealer/{dealerName}' to delete dealer
	 * 
	 * @param dealerName
	 * @return
	 */
	@HystrixCommand(fallbackMethod = "deleteDealerFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public ResponseEntity<?> deleteDealer(final String dealerName, Tracer tracer, Span span) {
		logger.debug("deleteDealer() executed with dealerName: " + dealerName);
		try{
		// Executing Http Request
		ResponseEntity<?> result = restHelper.executeRequest(DEALER_ENDPOINT + "/" + dealerName, HttpMethod.DELETE,
				null);
		return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			span.finish();
		}
	}
	public ResponseEntity<?> deleteDealerFB(final String dealerName, Tracer tracer, Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("deleteDealerFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}

	/**
	 * run 'POST /dealer' to create new Dealer
	 * 
	 * @param dealerDetails
	 * @return
	 */
	@HystrixCommand(fallbackMethod = "createDealerFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public ResponseEntity<?> createDealer(DealerDetails dealerDetails, Tracer tracer, Span span) {
		logger.debug("createDealer() executed with dealerDetails: " + dealerDetails);
		try{
		// Creating HttpHeader
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// Creating Body
		String dealerDetailsJsonStr = restHelper.parseObjectToJsonString(dealerDetails);

		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>(dealerDetailsJsonStr, headers);

		// Executing Http Request
		ResponseEntity<?> result = restHelper.executeRequest(DEALER_ENDPOINT, HttpMethod.POST, entity);

		return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			span.finish();
		}
	}
	
	public ResponseEntity<?> createDealerFB(DealerDetails dealerDetails, Tracer tracer, Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("createDealerFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}
}
