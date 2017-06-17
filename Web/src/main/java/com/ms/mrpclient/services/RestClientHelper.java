package com.ms.mrpclient.services;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestClientHelper {
	private static final Logger logger = LoggerFactory.getLogger(RestClientHelper.class);
	RestTemplate restTemplate;

	public RestClientHelper() {
		restTemplate = new RestTemplate();
	}

	/**
	 * This method executes HTTP Request
	 * 
	 * @param Service-Endpoint
	 * @param HttpMethod
	 *            (GET/PUT/POST/DELETE etc)
	 * @param HttpEntity
	 *            (Request Body / Header)
	 * @return ResponseEntity<String>
	 */
	public ResponseEntity<?> executeRequest(String url, HttpMethod httpMethod, HttpEntity<String> httpEntity) {
		logger.debug("url: " + url + ", httpMethod: " + httpMethod);
		ResponseEntity<?> result = restTemplate.exchange(url, httpMethod, httpEntity, String.class);
		if (result != null) {
			logger.debug("ResponseStatus: " + result.getStatusCode());
		}
		return result;
	}

	/**
	 * Reads ResponseBody(Json) and converts it into corresponding Object
	 * 
	 * @param ResponseEntity<String>
	 * @param Class
	 * 
	 * @return
	 */
	public <T> T parseJSONtoObject(ResponseEntity<String> result, Class<T> classs) {
		if (result == null) {
			return null;
		}

		T object = null;

		final String responseBodyStr = result.getBody();
		logger.debug("jsonStr to be converted into object: " + responseBodyStr);

		if(responseBodyStr == null)
		{
			return null;
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			object = mapper.readValue(responseBodyStr, classs);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return object;
	}

	/**
	 * Reads ResponseBody(Json) and converts it into corresponding Collection
	 * Object
	 * 
	 * @param ResponseEntity<String>
	 * @param TypeReference
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public <T> T parseJSONtoCollectionObject(ResponseEntity<String> result, TypeReference valueTypeRef) {
		if (result == null) {
			return null;
		}

		T object = null;

		final String responseBodyStr = result.getBody();
		logger.debug("jsonStr to be converted into object: " + responseBodyStr);

		ObjectMapper mapper = new ObjectMapper();
		try {
			object = mapper.readValue(responseBodyStr, valueTypeRef);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return object;
	}

	/**
	 * Converts Object into Json string
	 * 
	 * @param object
	 * @return
	 */
	public String parseObjectToJsonString(Object object) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = null;
		try {
			jsonStr = mapper.writeValueAsString(object);
			logger.debug("converted jsonStr: " + jsonStr);
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return jsonStr;
	}
}
