package smpl.restclient.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import brave.Span.Kind;
import brave.Tracer;
import brave.opentracing.BraveTracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapInjectAdapter;
import smpl.restclient.model.Catalog;
import zipkin.Span;
import brave.opentracing.BraveSpan;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.okhttp3.OkHttpSender;

@Service
public class CatalogService {

	RestClientHelper restHelper;

	@Value("${service.catalog.uri}")
	private String catalogUrl;

	public CatalogService() {
		restHelper = new RestClientHelper();
	}
	
	OkHttpSender sender = OkHttpSender.create("http://168.63.132.38:9411/api/v1/spans");
	AsyncReporter<Span> reporter = AsyncReporter.builder(sender).build();

	Tracer braveTracer = Tracer.newBuilder()
            .localServiceName("rest-client")
            .reporter(reporter)
            .build();
	io.opentracing.Tracer tracer = BraveTracer.wrap(braveTracer);

	/**
	 * This method executes GET /catalog
	 * 
	 * @return List<Catalog>
	 */
	public ResponseEntity<?> getCatalogs() {
 
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		Map<String, String> map = new HashMap<String, String>();
		io.opentracing.Span span = tracer.buildSpan("GetCatalog").withTag("Description", "Get All Catalogs")
				.start();
		 brave.Span braveSpan = ((BraveSpan) span).unwrap();
		 braveSpan.kind(Kind.CLIENT);
		 tracer.inject(span.context(),Format.Builtin.HTTP_HEADERS,new TextMapInjectAdapter(map));	
		 headers.setAll(map);
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
		ResponseEntity<String> result = restHelper.executeRequest(catalogUrl, HttpMethod.GET, entity);
		braveSpan.finish();
		return result;
	}

	/**
	 * This method executes GET /catalog/{skuNumber}
	 * 
	 * @param skuNumber
	 * @return Catalog
	 */
	public ResponseEntity<String> getCatalogByID(String skuNumber) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
		ResponseEntity<String> result = restHelper.executeRequest(catalogUrl + skuNumber, HttpMethod.GET, entity);
		return result;
	}

	/**
	 * This method executes DELETE /catalog/{skuNumber}
	 * 
	 * @param skuNumber
	 * @return Success / Failure
	 */
	public ResponseEntity<String> deleteCatalogByID(String skuNumber) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
		ResponseEntity<String> result = restHelper.executeRequest(catalogUrl + skuNumber, HttpMethod.DELETE, entity);
		return result;
	}

	/**
	 * This method executes POST /catalog
	 * 
	 * @param Catalog
	 * @return Success / Failure
	 */
	public ResponseEntity<?> addCatalogItem(Catalog catalog) {
		if (catalog == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<?> entity = new HttpEntity<Catalog>(catalog, headers);
		ResponseEntity<String> result = restHelper.executeRequest(catalogUrl, HttpMethod.POST, entity);
		return result;
	}

	/**
	 * This method executes PUT /catalog/{skuNumber}
	 * 
	 * @param skuNumber
	 * @param catalog
	 * @return Success / Failure
	 */
	public ResponseEntity<?> updateCatalogItem(String skuNumber, Catalog catalog) {
		if (catalog == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<?> entity = new HttpEntity<Catalog>(catalog, headers);
		ResponseEntity<String> result = restHelper.executeRequest(catalogUrl + skuNumber, HttpMethod.PUT, entity);
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
