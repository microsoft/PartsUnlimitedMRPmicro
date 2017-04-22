package smpl.restclient.service;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import smpl.restclient.model.Quote;

@Service
public class QuoteService {
//	private static final Logger log = LoggerFactory.getLogger(QuoteService.class);

	RestClientHelper restHelper;

	@Value("${service.quote.uri}")
	private String quoteUrl;

	public QuoteService() {
		restHelper = new RestClientHelper();
	}

	/**
	 * This method executes GET /getQuotesByCustomerName
	 * 
	 * @return ResponseEntity<Quote>
	 */
	public ResponseEntity<?> getQuotesByCustomerName(String name) {

		// Building URI with Request Parameter String
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("name", name);
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(quoteUrl).queryParams(params);
		String uriBuilder = builder.build().encode().toUriString();
		System.out.println(uriBuilder);
		// URI building Ends
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
		ResponseEntity<String> result = restHelper.executeRequest(uriBuilder, HttpMethod.GET, entity);
		return result;
	}

	/**
	 * This method executes GET /Quote/{quoteId}
	 * 
	 * @param quoteId
	 * @return Quote
	 */
	public ResponseEntity<String> getQuoteByID(String quoteId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
		ResponseEntity<String> result = restHelper.executeRequest(quoteUrl + quoteId, HttpMethod.GET, entity);
		return result;
	}

	/**
	 * This method executes DELETE /quote/{quoteId}
	 * 
	 * @param quoteId
	 * @return Success / Failure
	 */
	public ResponseEntity<String> deleteQuoteByID(String quoteId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
		ResponseEntity<String> result = restHelper.executeRequest(quoteUrl + quoteId, HttpMethod.DELETE, entity);
		return result;
	}

	/**
	 * This method executes POST /quote
	 * 
	 * @param Quote
	 * @return Success / Failure
	 */
	public ResponseEntity<?> createQuote(Quote quote) {
		if (quote == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<?> entity = new HttpEntity<Quote>(quote, headers);
		ResponseEntity<String> result = restHelper.executeRequest(quoteUrl, HttpMethod.POST, entity);
		return result;
	}

	/**
	 * This method executes PUT /quote/{quoteId}
	 * 
	 * @param quoteId
	 * @param quote
	 * @return Success / Failure
	 */
	public ResponseEntity<?> updateQuoteItem(String quoteId, Quote quote) {
		if (quote == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<?> entity = new HttpEntity<Quote>(quote, headers);
		ResponseEntity<String> result = restHelper.executeRequest(quoteUrl + quoteId, HttpMethod.PUT, entity);
		return result;
	}

	/*
	 * public static void main(String[] args) { CatalogService catalogService =
	 * new CatalogService(); catalogService.getCatalogs(); //
	 * catalogService.getCatalogByID("LIG-0002"); //
	 * catalogService.deleteCatalogByID("LIG-0002"); // // Catalog catalog = new
	 * Catalog(); // catalog.setSkuNumber("WHE-0002"); //
	 * catalog.setDescription("Rim (2 Packs)"); // catalog.setInventory(5); //
	 * catalog.setPrice(67.77); // catalog.setLeadTime(5); // //
	 * catalogService.saveCatalog(catalog); //
	 * catalogService.updateCatalog("WHE-0002", catalog); }
	 */
}
