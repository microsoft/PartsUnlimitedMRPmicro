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
import com.ms.mrpclient.data.entities.QuoteDetails;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.opentracing.*;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapInjectAdapter;

@Service
public class QuoteService {
	private static final Logger logger = LoggerFactory.getLogger(QuoteService.class);
	@Value("${service.quote.uri}")
	String QUOTE_ENDPOINT;
	RestClientHelper restHelper;

	public QuoteService() {
		restHelper = new RestClientHelper();
	}

	/**
	 * This method executes GET /quotes?name=
	 * 
	 * @param customerName
	 * @return List<QuoteDetails>
	 */
	@HystrixCommand(fallbackMethod = "getQuotesByNameFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public List<QuoteDetails> getQuotesByName(String customerName,Tracer tracer, Span span) {
		try{
		logger.debug("getQuotesByName() executed, customerName: " + customerName);
		List<QuoteDetails> quoteDetailsList = null;
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));
		
		// Creating HttpHeader
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setAll(map);
		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

		// Executing Http Request
		if (customerName == null) {
			customerName = "";
		}
		ResponseEntity<?> result = restHelper.executeRequest(QUOTE_ENDPOINT + "?name=" + customerName,
				HttpMethod.GET, entity);

		// Parsing response body
		if (result != null && result.getStatusCode() == HttpStatus.OK) {
			quoteDetailsList = restHelper.parseJSONtoCollectionObject((ResponseEntity<String>)result, new TypeReference<List<QuoteDetails>>() {
			});
		}

		return quoteDetailsList;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return null;
		} finally {
			span.finish();
		}
		
	}
	
	public List<QuoteDetails> getQuotesByNameFB(String customerName,Tracer tracer, Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("getQuotesByNameFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}

	/**
	 * This method executes GET /quotes?name=
	 * 
	 * @param customerName
	 * @return List<QuoteDetails>
	 */
	@HystrixCommand(fallbackMethod = "getQuotesByDealerNameFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public List<String> getQuotesByDealerName(String dealername, Tracer tracer, Span span) {	
		try{
		logger.debug("getQuotesByDealerName() executed, dealername: " + dealername);
		List<String> quoteDetailsList = null;
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));
        
		// Creating HttpHeader
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setAll(map);
		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

		// Executing Http Request
		if (dealername == null) {
			dealername = "";
		}
		ResponseEntity<?> result = restHelper.executeRequest(QUOTE_ENDPOINT + "?dealername=" + dealername,
				HttpMethod.GET, entity);

		// Parsing response body
		if (result != null && result.getStatusCode() == HttpStatus.OK) {
			quoteDetailsList = restHelper.parseJSONtoCollectionObject((ResponseEntity<String>)result, new TypeReference<List<String>>() {
			});
		}

		return quoteDetailsList;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return null;
		} finally {
			span.finish();
		}
	}

	public List<String> getQuotesByDealerNameFB(String dealername, Tracer tracer, Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("getQuotesByDealerNameFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}
	/**
	 * This method executes GET /quotes/{quoteId}
	 * 
	 * @param quoteId
	 * @return QuoteDetails
	 */
	@HystrixCommand(fallbackMethod = "getQuoteByIDFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public QuoteDetails getQuoteByID(final String quoteId, Tracer tracer, Span span) {
		try{
		logger.debug("getQuoteByID() executed, quoteId: " + quoteId);
		QuoteDetails quoteDetails = null;
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));

		// Creating HttpHeader
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setAll(map);
		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

		// Executing Http Request
		ResponseEntity<?> result = restHelper.executeRequest(QUOTE_ENDPOINT + "/" + quoteId, HttpMethod.GET,
				entity);

		// Parsing response body
		if (result != null && result.getStatusCode() == HttpStatus.OK) {
			quoteDetails = restHelper.parseJSONtoObject((ResponseEntity<String>)result, QuoteDetails.class);
		}

		return quoteDetails;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return null;
		} finally {
			span.finish();
		}
	}
	
	public QuoteDetails getQuoteByIDFB(final String quoteId, Tracer tracer, Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("getQuoteByIDFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}

	/**
	 * This method executes DELETE /quotes/{quoteId}
	 * 
	 * @param quoteId
	 * @return
	 */
	@HystrixCommand(fallbackMethod = "deleteQuoteByIDFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public ResponseEntity<?> deleteQuoteByID(String quoteId, Tracer tracer, Span span) {
		try{
		logger.debug("deleteQuoteByID() executed, quoteId: " + quoteId);
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setAll(map);
		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
		// Executing Http Request
		ResponseEntity<?> result = restHelper.executeRequest(QUOTE_ENDPOINT + "/" + quoteId, HttpMethod.DELETE,
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
    
	public ResponseEntity<?> deleteQuoteByIDFB(String quoteId, Tracer tracer, Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("deleteQuoteByIDFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}

	/**
	 * This method executes PUT /quotes/{quoteId}
	 * 
	 * @param quoteId
	 * @param quotesDetails
	 * @return
	 */
	@HystrixCommand(fallbackMethod = "updateQuoteFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public ResponseEntity<?> updateQuote(String quoteId, QuoteDetails quotesDetails, Tracer tracer, Span span) {
		try{
		logger.debug("updateQuote() executed, quoteId: " + quoteId + "\nQuoteDetails:\n" + quotesDetails);
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));
		// Creating HttpHeader
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAll(map);
		// Creating Body
		String quotesDetailsJsonStr = restHelper.parseObjectToJsonString(quotesDetails);

		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>(quotesDetailsJsonStr, headers);

		// Executing Http Request
		ResponseEntity<?> result = restHelper.executeRequest(QUOTE_ENDPOINT + "/" + quoteId, HttpMethod.PUT,
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
	
	public ResponseEntity<?> updateQuoteFB(String quoteId, QuoteDetails quotesDetails, Tracer tracer, Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("updateQuoteFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}
	

	/**
	 * This method executes POST /quotes
	 * 
	 * @param QuoteDetails
	 * @return
	 */
	@HystrixCommand(fallbackMethod = "createQuoteFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public ResponseEntity<?> createQuote(QuoteDetails quoteDetails, Tracer tracer, Span span) {
		try{
		logger.debug("createQuote() executed, QuoteDetails:\n" + quoteDetails);
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));
		// Creating HttpHeader
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAll(map);
		// Creating Body
		final String quoteDetailsJsonStr = restHelper.parseObjectToJsonString(quoteDetails);

		// Creating HttpEntity contains header and body
		HttpEntity<String> entity = new HttpEntity<String>(quoteDetailsJsonStr, headers);

		// Executing Http Request
		ResponseEntity<?> result = restHelper.executeRequest(QUOTE_ENDPOINT, HttpMethod.POST, entity);

		return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			span.finish();
		}
	}
	public ResponseEntity<?> createQuoteFB(QuoteDetails quoteDetails, Tracer tracer, Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("createQuoteFB fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}
}
