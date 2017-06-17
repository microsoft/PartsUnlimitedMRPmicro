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
import com.ms.mrpclient.data.entities.Catalog;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/*import brave.Span.Kind;
import brave.Tracer;
import brave.opentracing.BraveSpan;
import brave.opentracing.BraveTracer;

import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.okhttp3.OkHttpSender;*/
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapInjectAdapter;
import io.opentracing.*;

@Service
public class CatalogService {
	private static final Logger logger = LoggerFactory.getLogger(CatalogService.class);
	@Value("${service.catalog.uri}")
	private String CATALOG_ENDPOINT;
	RestClientHelper restHelper;

	public CatalogService() {
		restHelper = new RestClientHelper();
	}
	

	/**
	 * This method executes GET /catalog
	 * 
	 * @return List<Catalog>
	 */
	@HystrixCommand(fallbackMethod = "getCatalogListFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public List<Catalog> getCatalogList(Tracer tracer,Span span) {
		logger.debug("getCatalogList() executed");
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));
		try {
			List<Catalog> catalogList = null;

			// Creating HttpHeader
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			// Initiate span

			headers.setAll(map);

			// Creating HttpEntity contains header and body
			HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

			// Executing Http Request
			ResponseEntity<?> result = restHelper.executeRequest(CATALOG_ENDPOINT, HttpMethod.GET, entity);

			// Parsing response body
			if (result != null && result.getStatusCode() == HttpStatus.OK) {
				logger.debug("ResponseStatus: " + result.getStatusCode());
				catalogList = restHelper.parseJSONtoCollectionObject((ResponseEntity<String>)result, new TypeReference<List<Catalog>>() {
				});
			}

			return catalogList;

		} catch (final HttpClientErrorException e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return null;
		} catch (Exception e) {
			// Set this tag to display error
			span.setTag("error", e.getMessage());
			return null;
		} finally {
			span.finish();
		}
	}

	public List<Catalog> getCatalogListFB(Tracer tracer,Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.debug("getCatalogItems fall back method reached new	...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}

	/**
	 * This method executes GET /catalog/{skuNumber}
	 * 
	 * @param skuNumber
	 * @return Catalog
	 */
	@HystrixCommand(fallbackMethod = "getCatalogByIDFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public Catalog getCatalogByID(String skuNumber,Tracer tracer,Span span) {
		logger.debug("getCatalogByID() executed with skuNumber: " + skuNumber);
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));
		try {
			Catalog catalog = null;
			// Creating HttpHeader
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

			headers.setAll(map);
			// Creating HttpEntity contains header and body
			HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

			// Executing Http Request
			ResponseEntity<?> result = restHelper.executeRequest(CATALOG_ENDPOINT + "/" + skuNumber,
					HttpMethod.GET, entity);

			// Parsing response body
			if (result != null && result.getStatusCode() == HttpStatus.OK) {
				logger.debug("ResponseStatus: " + result.getStatusCode());
				catalog = restHelper.parseJSONtoObject((ResponseEntity<String>)result, Catalog.class);
			}
			return catalog;
		} catch (final HttpClientErrorException e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return null;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			// Set this tag to display error
			span.setTag("error", e.getMessage());
			return null;
		} finally {
			span.finish();
		}

	}

	public Catalog getCatalogByIDFB(String skuNumber,Tracer tracer,Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("getCatalogItem by ID fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}

	/**
	 * This method executes DELETE /catalog/{skuNumber}
	 * 
	 * @param skuNumber
	 * @return Success / Failure
	 */
	@HystrixCommand(fallbackMethod = "deleteCatalogByIDFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public ResponseEntity<?> deleteCatalogByID(String skuNumber,Tracer tracer, Span span) {
		logger.debug("deleteCatalogByID() executed with skuNumber: " + skuNumber);
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			// Initiate span

			headers.setAll(map);

			// Creating HttpEntity contains header and body
			HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

			// Executing Http Request
			ResponseEntity<?> result = restHelper.executeRequest(CATALOG_ENDPOINT + "/" + skuNumber,
					HttpMethod.DELETE, entity);
			logger.debug("ResponseStatus: " + result.getStatusCode());

			/*
			 * if (result != null && result.getStatusCode() ==
			 * HttpStatus.NO_CONTENT) { return true; } return false;
			 */
			return result;
		} catch (final HttpClientErrorException e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return null;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			// Set this tag to display error
			span.setTag("error", e.getMessage());
			return null;
		} finally {
			span.finish();
		}

	}

	public ResponseEntity<?> deleteCatalogByIDFB(String skuNumber, Tracer tracer, Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("deleteCatalog by ID fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}

	/**
	 * This method executes POST /catalog
	 * 
	 * @param Catalog
	 * @return Success / Failure
	 */
	@HystrixCommand(fallbackMethod = "saveCatalogFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public ResponseEntity<?> saveCatalog(Catalog catalog, Tracer tracer, Span span) {		
		logger.debug("saveCatalog() executed with Catalog: " + catalog);

		// Creating Span
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));

		try {
			if (catalog == null) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			// Creating HttpHeader
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			// Creating Body
			String catalogJsonStr = restHelper.parseObjectToJsonString(catalog);

			headers.setAll(map);
			// Creating HttpEntity contains header and body
			HttpEntity<String> entity = new HttpEntity<String>(catalogJsonStr, headers);

			// Executing Http Request
			ResponseEntity<?> result = restHelper.executeRequest(CATALOG_ENDPOINT, HttpMethod.POST, entity);
			logger.debug("ResponseStatus: " + result.getStatusCode());

			return result;

		} catch (final HttpClientErrorException e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return new ResponseEntity<>(e.getStatusCode());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			span.finish();
		}
	}

	public ResponseEntity<?> saveCatalogFB(Catalog catalog, Tracer tracer, Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("saveCatalog fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}

	/**
	 * This method executes PUT /catalog/{skuNumber}
	 * 
	 * @param skuNumber
	 * @param catalog
	 * @return Success / Failure
	 */
	@HystrixCommand(fallbackMethod = "updateCatalogFB", groupKey = "MrpClient", commandKey = "MrpClient")
	public ResponseEntity<?> updateCatalog(String skuNumber, Catalog catalog, Tracer tracer, Span span) {
		logger.debug("updateCatalog() executed with skunumber & Catalog: " + skuNumber + "\n" + catalog);
		// Creating Span
		Map<String, String> map = new HashMap<String, String>();		
		tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));
		try {
			if (catalog == null) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}

			// Creating HttpHeader
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			// Creating Body
			String catalogJsonStr = restHelper.parseObjectToJsonString(catalog);

			headers.setAll(map);

			// Creating HttpEntity contains header and body
			HttpEntity<String> entity = new HttpEntity<String>(catalogJsonStr, headers);

			// Executing Http Request
			ResponseEntity<?> result = restHelper.executeRequest(CATALOG_ENDPOINT + "/" + skuNumber,
					HttpMethod.PUT, entity);
			logger.debug("ResponseStatus: " + result.getStatusCode());

			return result;
		} catch (final HttpClientErrorException e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return new ResponseEntity<>(e.getStatusCode());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			span.setTag("error", e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			span.finish();
		}
	}

	public ResponseEntity<?> updateCatalogFB(String skuNumber, Catalog catalog, Tracer tracer, Span span) {
		span.setTag("error","Unable to connect to service, fallback method reached");
		logger.info("updateCatalog fall back method reached...");
		throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Fallback method reached");
	}

	public static void main(String[] args) {
		CatalogService catalogService = new CatalogService();
		// catalogService.getCatalogList();
		// catalogService.getCatalogByID("LIG-0002");
		// catalogService.deleteCatalogByID("LIG-0002");

		Catalog catalog = new Catalog();
		catalog.setSkuNumber("WHE-0002");
		catalog.setDescription("Rim (2 Packs)");
		catalog.setUnit(5);
		// catalogService.saveCatalog(catalog);
		//catalogService.updateCatalog("WHE-0002", catalog,tracer, span);
	}

}
