package smpl.catalog.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import brave.Span.Kind;
import brave.Tracer;
import brave.opentracing.BraveSpan;
import brave.opentracing.BraveSpanContext;
import brave.opentracing.BraveTracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.prometheus.client.Summary;
import smpl.catalog.model.Catalog;
import smpl.catalog.repository.CatalogItemsRepository;
import smpl.catalog.repository.RepositoryFactory;
import smpl.catalog.repository.TestPath;
import smpl.catalog.repository.service.CatalogService;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.okhttp3.OkHttpSender;

@RestController

@RequestMapping("/api/catalog")
public class CatalogController {
	private static final Logger log = LoggerFactory.getLogger(CatalogController.class);

	private final CatalogItemsRepository service;

	private String zipkinUrl;

	@Autowired
	private HttpServletRequest request;

	OkHttpSender sender;
	AsyncReporter<Span> reporter;
	io.opentracing.Tracer tracer;
	Tracer braveTracer;

	@Autowired
	public CatalogController(CatalogService service, HttpServletRequest httpRequest,
			@Value("${zipkin.mrpservice.uri}") String url) {

		if (service != null) {
			// RepositoryFactory.reset("mongodb");
			// this.service = RepositoryFactory.getCatalogItemsRepository();
			this.service = service;

		} else {
			// call from test so reset repository also
			RepositoryFactory.reset("memory");
			this.service = RepositoryFactory.getCatalogItemsRepository();
			((TestPath) this.service).reset();
		}

		if (request == null) {
			request = httpRequest;
		}
		this.zipkinUrl = url;
		sender = OkHttpSender.create(zipkinUrl);
		reporter = AsyncReporter.builder(sender).build();
		braveTracer = Tracer.newBuilder().localServiceName("catalog-svc").reporter(reporter).build();
		tracer = BraveTracer.wrap(braveTracer);
	}

	/*
	 * OkHttpSender sender =
	 * OkHttpSender.create("http://168.63.132.38:9411/api/v1/spans");
	 * AsyncReporter<Span> reporter = AsyncReporter.builder(sender).build();
	 * 
	 * Tracer braveTracer = Tracer.newBuilder() .localServiceName("catalog-svc")
	 * .reporter(reporter) .build(); io.opentracing.Tracer tracer =
	 * BraveTracer.wrap(braveTracer);
	 */ /**
		 * Gets a list of available catalog item.
		 *
		 * @return An HttpResponse containing a list of catalog item.
		 */
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getCatalogItems() {
		BraveSpanContext openTracingContext = getTracingContext();
		io.opentracing.Span span = tracer.buildSpan("GetCatalogFromService").asChildOf(openTracingContext)
				.withTag("Description", "Get All Catalogs")
				.withTag("http_request_url", request.getRequestURI()).start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);

		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		try {
			log.debug("Calling getCatalogItems() method of catalog controller is called");
			Prometheus.getCounters.inc();
			Prometheus.inProgressRequests.inc();
			io.opentracing.Span mongospan = tracer.buildSpan("MongoService").asChildOf(span)
					.withTag("Description", "MongoDB Service Call").start();
			brave.Span braveMongoSpan = ((BraveSpan) mongospan).unwrap();
			log.debug("Calling getCatalogItems() method of catalog service");
			List<Catalog> catalog = service.getCatalogItems(mongospan);
			braveMongoSpan.finish();
			if (catalog == null) {
				log.debug("No records found. Returning NOT_FOUND status.");
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			} else {
				log.debug("Returning list of items.");
				return new ResponseEntity<>(catalog, HttpStatus.OK);
			}
		} catch (Exception exc) {
			Prometheus.requestFailures.inc();
			log.error("Error in getCatalogItems", exc);
			span.setTag("error", exc.getMessage());
			return new ResponseEntity<>(exc.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			requestTimer.observeDuration();
			Prometheus.inProgressRequests.dec();
			span.finish();
		}
	}

	/**
	 * Gets a specific catalog item by its id.
	 *
	 * @param sku
	 *            The SKU number
	 * @return An HttpResponse containing an catalog item record.
	 */

	@RequestMapping(method = RequestMethod.GET, value = "{sku}")
	public ResponseEntity<?> getCatalogItem(@PathVariable String sku) {
		BraveSpanContext openTracingContext = getTracingContext();
		io.opentracing.Span span = tracer.buildSpan("GetCatalogByIdFromService").asChildOf(openTracingContext)
				.withTag("Description", "Get Catalog Item")
				.withTag("http_request_url", request.getRequestURI()).start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		try {
			Prometheus.getCounters.inc();
			Prometheus.inProgressRequests.inc();
			io.opentracing.Span mongospan = tracer.buildSpan("MongoService").asChildOf(span)
					.withTag("Description", "MongoDB Service Call").start();
			brave.Span braveMongoSpan = ((BraveSpan) mongospan).unwrap();
			Catalog catalogItem = service.getCatalogItem(sku, mongospan);
			braveMongoSpan.finish();
			if (catalogItem == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			} else {
				return new ResponseEntity<>(catalogItem, HttpStatus.OK);
			}
		} catch (Exception exc) {
			Prometheus.requestFailures.inc();
			log.error("Error in getCatalogItem", exc);
			span.setTag("error", exc.getMessage());
			return new ResponseEntity<>(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			requestTimer.observeDuration();
			Prometheus.inProgressRequests.dec();
			span.finish();
		}
	}

	/**
	 * Adds or updates an catalog item SKU
	 *
	 * @param info
	 *            Information about the SKU
	 * @return An HTTP status code.
	 */
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> addCatalogItem(@RequestBody Catalog info) {
		BraveSpanContext openTracingContext = getTracingContext();
		io.opentracing.Span span = tracer.buildSpan("AddCatalogService").asChildOf(openTracingContext)
				.withTag("Description", "Create Catalog")
				.withTag("http_request_url", request.getRequestURI()).start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		if (info == null) {
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		}
		String errorMsg = info.validate();
		if (errorMsg != null) {
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		}
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		try {
			Prometheus.createCounters.inc();
			Prometheus.inProgressRequests.inc();
			io.opentracing.Span mongospan = tracer.buildSpan("GetCatalogsFromMongoService").asChildOf(span)
					.withTag("Description", "MongoDB Service Call").start();
			brave.Span braveMongoSpan = ((BraveSpan) mongospan).unwrap();
			Catalog catalogItem = service.getCatalogItem(info.getSkuNumber(), mongospan);
			braveMongoSpan.finish();
			if (catalogItem != null) {
				return new ResponseEntity<String>(HttpStatus.CONFLICT);
			} else {
				io.opentracing.Span addCatalogSpan = tracer.buildSpan("AddCatalogToDB").asChildOf(span)
						.withTag("Description", "MongoDB Service Call").start();
				brave.Span braveaddCatalogSpan = ((BraveSpan) addCatalogSpan).unwrap();
				Catalog result = service.createCatalog(info, addCatalogSpan);
				braveaddCatalogSpan.finish();

				if (result != null) {
					return new ResponseEntity<String>(HttpStatus.CREATED);

				}

			}

		} catch (Exception exc) {
			Prometheus.requestFailures.inc();
			log.error("Error in addCatalogItem", exc);
			span.setTag("error", exc.getMessage());
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			requestTimer.observeDuration();
			Prometheus.inProgressRequests.dec();
			span.finish();
		}
		return null;
	}

	/**
	 * Adds or updates an CatalogItem SKU
	 *
	 * @param sku
	 *            The SKU number
	 * @param info
	 *            Information about the SKU
	 * @return An HTTP status code.
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/{sku}")
	public ResponseEntity<?> updateCatalogItem(@PathVariable String sku, @RequestBody Catalog info) {
		BraveSpanContext openTracingContext = getTracingContext();
		io.opentracing.Span span = tracer.buildSpan("UpdateCatalogService").asChildOf(openTracingContext)
				.withTag("Description", "Update Catalog")
				.withTag("http_request_url", request.getRequestURI()).start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		if (info == null) {
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		}
		String errorMsg = info.validate();

		if (errorMsg != null) {
			return new ResponseEntity<>(errorMsg, HttpStatus.BAD_REQUEST);
		}
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		try {
			Prometheus.updateCounters.inc();
			Prometheus.inProgressRequests.inc();
			io.opentracing.Span mongospan = tracer.buildSpan("GetCatalogsFromMongoService").asChildOf(span)
					.withTag("Description", "MongoDB Service Call").start();
			brave.Span braveMongoSpan = ((BraveSpan) mongospan).unwrap();
			Catalog catalogItem = service.getCatalogItem(sku, mongospan);
			braveMongoSpan.finish();
			if (catalogItem == null) {
				return new ResponseEntity<Catalog>(HttpStatus.NOT_FOUND);
			}
			io.opentracing.Span updateCatalogSpan = tracer.buildSpan("AddCatalogToDB").asChildOf(span)
					.withTag("Description", "MongoDB Service Call").start();
			brave.Span braveupdateCatalogSpan = ((BraveSpan) updateCatalogSpan).unwrap();
			boolean result = service.updateCatalogItem(sku, info, null, updateCatalogSpan);
			braveupdateCatalogSpan.finish();
			return new ResponseEntity<>(result ? HttpStatus.OK : HttpStatus.NOT_FOUND);
		} catch (Exception exc) {
			Prometheus.requestFailures.inc();
			log.error("Error in updateCatalogItem", exc);
			span.setTag("error", exc.getMessage());
			return new ResponseEntity<>(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			requestTimer.observeDuration();
			Prometheus.inProgressRequests.dec();
			span.finish();
		}
	}

	/**
	 * Remove an catalog item SKU from the catalog.
	 *
	 * @param sku
	 *            The SKU number.
	 * @return An HTTP status code.
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/{sku}")
	public ResponseEntity<?> removeCatalogItem(@PathVariable String sku) {
		BraveSpanContext openTracingContext = getTracingContext();
		io.opentracing.Span span = tracer.buildSpan("DeleteCatalogService").asChildOf(openTracingContext)
				.withTag("Description", "Delete Catalog")
				.withTag("http_request_url", request.getRequestURI()).start();
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		try {
			Prometheus.removeCounters.inc();
			Prometheus.inProgressRequests.inc();
			if (service.removeCatalogItem(sku, null, span)) {
				return new ResponseEntity<Catalog>(HttpStatus.OK);
			} else {
				return new ResponseEntity<Catalog>(HttpStatus.NOT_FOUND);
			}
		} catch (Exception exc) {
			Prometheus.requestFailures.inc();
			log.error("Error in removeCatalogItem", exc);
			span.setTag("error", exc.getMessage());
			return new ResponseEntity<>(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			requestTimer.observeDuration();
			Prometheus.inProgressRequests.dec();
			span.finish();
		}
	}

	private BraveSpanContext getTracingContext() {
		String traceId = request.getHeader("X-B3-TraceId");
		String spanId = request.getHeader("X-B3-SpanId");
		String sampleId = request.getHeader("X-B3-Sampled");

		Map<String, String> map = new LinkedHashMap<>();
		map.put("X-B3-TraceId", traceId);
		map.put("X-B3-SpanId", spanId);
		map.put("X-B3-Sampled", sampleId);
		BraveSpanContext openTracingContext = (BraveSpanContext) tracer.extract(Format.Builtin.HTTP_HEADERS,
				new TextMapExtractAdapter(map));
		return openTracingContext;
	}
}
