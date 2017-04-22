package smpl.restclient.service;

import java.io.IOException;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestClientHelper {
//	private static final Logger log = LoggerFactory.getLogger(RestClientHelper.class);
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
	public ResponseEntity<String> executeRequest(String url, HttpMethod httpMethod, HttpEntity<?> httpEntity) {

		ResponseEntity<String> responseEntity = null;
		try {

			responseEntity = restTemplate.exchange(url, httpMethod, httpEntity, String.class);

			return responseEntity;

		} catch (HttpClientErrorException e) {
			return new ResponseEntity<String>(e.getStatusCode());

		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

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

		ObjectMapper mapper = new ObjectMapper();
		try {
			object = mapper.readValue(responseBodyStr, classs);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
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
		ObjectMapper mapper = new ObjectMapper();
		try {
			object = mapper.readValue(responseBodyStr, valueTypeRef);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
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
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonStr;
	}
}
