package smpl.quote.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import brave.Tracer;
import brave.Span.Kind;
import brave.opentracing.BraveSpan;
import brave.opentracing.BraveSpanContext;
import brave.opentracing.BraveTracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.prometheus.client.Summary;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import smpl.quote.BadRequestException;
import smpl.quote.model.Quote;
import smpl.quote.repository.service.QuoteService;
import smpl.quote.repository.QuoteRepository;
import smpl.quote.repository.RepositoryFactory;
import smpl.quote.repository.TestPath;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.okhttp3.OkHttpSender;

@RestController

@RequestMapping("/api/quotes")
public class QuoteController {
	private static final Logger log = LoggerFactory.getLogger(QuoteController.class);

	private final QuoteRepository service;


	private String  zipkinUrl;

	@Autowired
	private HttpServletRequest request ; 

	OkHttpSender sender;
	AsyncReporter<Span> reporter;
	io.opentracing.Tracer tracer;
	Tracer braveTracer;

	@Autowired
	public
	QuoteController(QuoteService service,HttpServletRequest httpRequest,@Value("${zipkin.mrpservice.uri}") String url) {
		if (service != null) {
			this.service = service;
		} else {
			// call from test so reset repository also
			RepositoryFactory.reset("memory");
			this.service = RepositoryFactory.getQuoteRepository();
			((TestPath) this.service).reset();
		}
		if(request ==null)
		{
			request = httpRequest;
		}
		this.zipkinUrl=url;
		sender = OkHttpSender.create(zipkinUrl);
		reporter = AsyncReporter.builder(sender).build();
		braveTracer = Tracer.newBuilder()
				.localServiceName("quote-svc")
				.reporter(reporter)
				.build();
		tracer = BraveTracer.wrap(braveTracer);
	}



	/*
	 * @RequestMapping(method = RequestMethod.GET) List<Quote> getAllQuote() {
	 * return service.getAllQuotes(); }
	 */

	@RequestMapping(method = RequestMethod.GET, value = "{quoteId}")
	public ResponseEntity<?> getQuoteById(@PathVariable String quoteId) {
		BraveSpanContext  openTracingContext = getTracingContext();		
		io.opentracing.Span span = tracer.buildSpan("GetQuoteByIdFromService")				 
				.asChildOf(openTracingContext)
				.withTag("Description", "Get Quote By Id")
				.withTag("http_request_url", request.getRequestURI())
				.start();     
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		try{
			Prometheus.createCounters.inc();
			Prometheus.inProgressRequests.inc();
			log.info("getQuoteById() method of Quote controller is called");
			io.opentracing.Span mongospan = tracer.buildSpan("MongoService")
					.asChildOf(span)
					.withTag("Description", "MongoDB Service Call")
					.start();
			brave.Span braveMongoSpan = ((BraveSpan) mongospan).unwrap();
			Quote result= service.getQuote(quoteId,mongospan);
			braveMongoSpan.finish();
			if (result == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			} else {
				return new ResponseEntity<>(result, HttpStatus.OK);
			}
		}
		catch (Exception exc) {	
			Prometheus.requestFailures.inc();
			log.error("Error in getQuoteByID", exc);
			span.setTag("error",exc.getMessage());
			return new ResponseEntity<>(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			Prometheus.inProgressRequests.dec();
			span.finish();
		}

	}

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getQuotesByCustomerName(@RequestParam(value = "name") String name) {
		BraveSpanContext  openTracingContext = getTracingContext();		
		io.opentracing.Span span = tracer.buildSpan("GetQuoteByCustomerNameFromService")				 
				.asChildOf(openTracingContext)
				.withTag("Description", "Get Quote By Customer Name")
				.withTag("http_request_url", request.getRequestURI())
				.start();     
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		try {
			Prometheus.createCounters.inc();
			Prometheus.inProgressRequests.inc();
			log.info("getQuotesByCustomerName() method of Quote controller is called");
			io.opentracing.Span mongospan = tracer.buildSpan("MongoService")
					.asChildOf(span)
					.withTag("Description", "MongoDB Service Call")
					.start();
			brave.Span braveMongoSpan = ((BraveSpan) mongospan).unwrap();
			List<Quote> q = service.getQuotesByCustomerName(name,mongospan);
			braveMongoSpan.finish();
			if (q == null || q.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			} else {
				return new ResponseEntity<>(q, HttpStatus.OK);
			}
		} catch (Exception exc) {
			Prometheus.requestFailures.inc();
			log.error("Error in getQuotesByCustomerName",exc);
			span.setTag("error",exc.getMessage());
			return new ResponseEntity<>(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		finally {
			Prometheus.inProgressRequests.dec();
			span.finish();
		}

	}

	@RequestMapping(value = "/quote/bydealer/{dealername}", method = RequestMethod.GET)
	public ResponseEntity<?> getQuoteIdsByDealerName(@PathVariable String dealername) {
		BraveSpanContext  openTracingContext = getTracingContext();		
		io.opentracing.Span span = tracer.buildSpan("getQuoteIdsByDealerNameFromService")				 
				.asChildOf(openTracingContext)
				.withTag("Description", "Get Quote By Dealer Name")
				.withTag("http_request_url", request.getRequestURI())
				.start();     
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		try {
			Prometheus.createCounters.inc();
			Prometheus.inProgressRequests.inc();
			log.info("getQuoteIdsByDealerName() method of Quote controller is called");
			io.opentracing.Span mongospan = tracer.buildSpan("MongoService")
					.asChildOf(span)
					.withTag("Description", "MongoDB Service Call")
					.start();
			brave.Span braveMongoSpan = ((BraveSpan) mongospan).unwrap();

			List<String> q = service.getQuoteIdsByDealerName(dealername,mongospan);
			braveMongoSpan.finish();

			if (q == null || q.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			} else {
				return new ResponseEntity<>(q, HttpStatus.OK);
			}
		} catch (Exception exc) {
			Prometheus.requestFailures.inc();
			log.error("Error in getQuoteIdsByDealerName",exc); 
			span.setTag("error",exc.getMessage());
			return new ResponseEntity<>(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		finally {
			Prometheus.inProgressRequests.dec();
			span.finish();
		}
	}


	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> createQuote(@RequestBody Quote quote)
	{ 
		BraveSpanContext  openTracingContext = getTracingContext();		
		io.opentracing.Span span = tracer.buildSpan("CreateQuoteService")				 
				.asChildOf(openTracingContext)
				.withTag("Description", "Create Quote")
				.withTag("http_request_url", request.getRequestURI())
				.start();     
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		log.info("createQuote() method of Quote controller is called");
		try {
			Prometheus.createCounters.inc();
			Prometheus.inProgressRequests.inc();
			if (quote == null)
			{
				return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
			}


			String errorMsg = quote.validate();
			if (errorMsg != null)
			{
				return new ResponseEntity<>(errorMsg, HttpStatus.BAD_REQUEST);
			}

			io.opentracing.Span mongospan = tracer.buildSpan("MongoService")
					.asChildOf(span)
					.withTag("Description", "MongoDB Service Call")
					.start();
			brave.Span braveMongoSpan = ((BraveSpan) mongospan).unwrap();
			Quote result = service.createQuote(quote,mongospan);
			braveMongoSpan.finish();
			if (result != null)
			{
				return new ResponseEntity<String>(HttpStatus.CREATED);
			}
			else
			{
				return new ResponseEntity<String>(HttpStatus.CONFLICT);
			}
		}
		catch (BadRequestException bre)
		{
			Prometheus.requestFailures.inc();
			span.setTag("error",bre.getMessage());
			log.error("Error in createQuote",bre);
			return new ResponseEntity<>(bre.getMessage(), HttpStatus.BAD_REQUEST);
		}
		catch (Exception exc)
		{
			Prometheus.requestFailures.inc();
			span.setTag("error",exc.getMessage());
			log.error("Error in createQuote",exc);
			return new ResponseEntity<>(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		finally {
			Prometheus.inProgressRequests.dec();
			span.finish();
		}
	}



	@RequestMapping(method = RequestMethod.DELETE, value = "/{quoteId}")
	public ResponseEntity<String> deleteQuote(@PathVariable String quoteId) {
		BraveSpanContext  openTracingContext = getTracingContext();		
		io.opentracing.Span span = tracer.buildSpan("DeleteQuoteService")				 
				.asChildOf(openTracingContext)
				.withTag("Description", "Delete Quote")
				.withTag("http_request_url", request.getRequestURI())
				.start();     
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		try {	
			Prometheus.createCounters.inc();
			Prometheus.inProgressRequests.inc();
			log.info("deleteQuote() method of Quote controller is called");
			boolean ok = service.removeQuote(quoteId,span);
			return new ResponseEntity<String>(ok ? HttpStatus.NO_CONTENT : HttpStatus.NOT_FOUND);
		} catch (Exception exc) {
			Prometheus.requestFailures.inc();
			log.error("Error in deleteQuote",exc);
			span.setTag("error",exc.getMessage());
			return new ResponseEntity<String>(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		finally {
			Prometheus.inProgressRequests.dec();
			span.finish();
		}
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/{quoteId}")
	public ResponseEntity<?> updateQuote(@PathVariable String quoteId, @RequestBody Quote quote) {
		BraveSpanContext  openTracingContext = getTracingContext();		
		io.opentracing.Span span = tracer.buildSpan("UpdateQuoteService")				 
				.asChildOf(openTracingContext)
				.withTag("Description", "Update Quote")
				.withTag("http_request_url", request.getRequestURI())
				.start();     
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		try {
			Prometheus.createCounters.inc();
			Prometheus.inProgressRequests.inc();
			log.info("updateQuote() method of Quote controller is called");
			if (quote == null)
			{
				return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
			} 
			//Input data validation;
			String errorMsg = quote.validate();
			if (errorMsg !=null)
			{ 
				return new ResponseEntity<>(errorMsg, HttpStatus.BAD_REQUEST); 
			}
			io.opentracing.Span mongospan = tracer.buildSpan("MongoService")
					.asChildOf(span)
					.withTag("Description", "MongoDB Service Call")
					.start();
			brave.Span braveMongoSpan = ((BraveSpan) mongospan).unwrap();
			boolean ok = service.updateQuote(quoteId, quote,mongospan);
			braveMongoSpan.finish();
			return new ResponseEntity<>(ok ? HttpStatus.OK : HttpStatus.NOT_FOUND);

		} catch (Exception exc) {
			Prometheus.requestFailures.inc();
			log.error("Error in updateQuote",exc);
			span.setTag("error",exc.getMessage());
			return new ResponseEntity<>(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		finally {
			Prometheus.inProgressRequests.dec();
			span.finish();
		}

	}

	@ExceptionHandler
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public void handleOrderNotFound(QuoteNotFoundException ex) {
	}

	private BraveSpanContext getTracingContext(){
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
