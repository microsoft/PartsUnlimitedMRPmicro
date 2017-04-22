package smpl.order.controller;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
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
import smpl.order.BadRequestException;
import smpl.order.ConflictingRequestException;
import smpl.order.model.Order;
import smpl.order.model.OrderEventInfo;
import smpl.order.model.OrderStatus;
import smpl.order.model.OrderUpdateInfo;
import smpl.order.repository.OrderRepository;
import smpl.order.repository.RepositoryFactory;
import smpl.order.repository.TestPath;
import smpl.order.repository.service.OrderService;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.okhttp3.OkHttpSender;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
	private static final Logger log = LoggerFactory.getLogger(OrderController.class);
	private final OrderRepository service;

	private String  zipkinUrl;

	@Autowired
	private HttpServletRequest request ; 

	OkHttpSender sender;
	AsyncReporter<Span> reporter;
	io.opentracing.Tracer tracer;
	Tracer braveTracer;

	@Autowired
	public OrderController(OrderService service,HttpServletRequest httpRequest,@Value("${zipkin.mrpservice.uri}") String url) {
		if(service != null) {             
			this.service = service;
        } else {
        	//call from test so reset repository also
            RepositoryFactory.reset("memory");
            this.service = RepositoryFactory.getOrderRepository();                 
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
				.localServiceName("order-svc")
				.reporter(reporter)
				.build();
		tracer = BraveTracer.wrap(braveTracer);
	}

	/**
	 * Gets an order identified by its id.
	 *
	 * @param orderId
	 *            The order id
	 * @return An HttpResponse containing the quote, if found.
	 */

	@RequestMapping(method = RequestMethod.GET, value = "/{orderId}")
	public ResponseEntity<?> getOrderById(@PathVariable String orderId) {
		BraveSpanContext  openTracingContext = getTracingContext();		
		io.opentracing.Span span = tracer.buildSpan("GetOrderByIdFromService")				 
				.asChildOf(openTracingContext)
				.withTag("Description", "Get Order By Id")
				.withTag("http_request_url", request.getRequestURI())
				.start();     
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		try {
			Prometheus.updateCounters.inc();
			Prometheus.inProgressRequests.inc();
			log.debug("Calling getOrderById() method of order controller is called");
			io.opentracing.Span mongospan = tracer.buildSpan("MongoService")
					.asChildOf(span)
					.withTag("Description", "MongoDB Service Call")
					.start();
			brave.Span braveMongoSpan = ((BraveSpan) mongospan).unwrap();
			Order o = service.getOrder(orderId,mongospan);
			braveMongoSpan.finish();
			if (o == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			} else {
				return new ResponseEntity<>(o, HttpStatus.OK);
			}
		} catch (Exception exc) {
			Prometheus.requestFailures.inc();
			log.error("Error in getOrderById ", exc);
			span.setTag("error",exc.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		finally {
			requestTimer.observeDuration();
			Prometheus.inProgressRequests.dec();
			span.finish();
		}
	}

	/**
	 * Gets a list of orders for a given dealer.
	 *
	 * @param dealer
	 *            The dealer name.
	 * @return An HttpResponse containing the quotes, if found.
	 */
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getOrdersByDealerName(
			@RequestParam(value = "dealer", required = false, defaultValue = "") String dealer,
			@RequestParam(value = "status", required = false, defaultValue = "None") OrderStatus status) {

		BraveSpanContext  openTracingContext = getTracingContext();		
		io.opentracing.Span span = tracer.buildSpan("GetOrderByDealerFromService")				 
				.asChildOf(openTracingContext)
				.withTag("Description", "Get Order By Dealer name")
				.withTag("http_request_url", request.getRequestURI())
				.start();     
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		try {
			Prometheus.updateCounters.inc();
			Prometheus.inProgressRequests.inc();
			log.debug("Calling getOrdersByDealerName() method of order controller is called");
			List<Order> o = null;
			if (dealer.length() == 0) {
				io.opentracing.Span mongospan = tracer.buildSpan("MongoService")
						.asChildOf(span)
						.withTag("Description", "MongoDB Service Call")
						.start();
				brave.Span braveMongoSpan = ((BraveSpan) mongospan).unwrap();
				o = service.getOrdersByStatus(status,mongospan);
				braveMongoSpan.finish();
			} else {
				// o = service.getOrdersByDealerName(dealer, status);
			}
			if (o == null || o.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			} else {
				return new ResponseEntity<>(o, HttpStatus.OK);
			}
		} catch (Exception exc) {
			Prometheus.requestFailures.inc();
			span.setTag("error",exc.getMessage());
			log.error("Error in getOrdersByDealerName ", exc);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		finally {
			requestTimer.observeDuration();
			Prometheus.inProgressRequests.dec();
			span.finish();
		}
	}

	/**
	 * Creates a new order.
	 *
	 * @param from
	 *            The id of the quote from which this order will be created.
	 * @return An HttpResponse containing the quote.
	 */

	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<?> createOrder(@RequestParam(value = "fromQuote") String from) {
		BraveSpanContext  openTracingContext = getTracingContext();		
		io.opentracing.Span span = tracer.buildSpan("CreateorderService")				 
				.asChildOf(openTracingContext)
				.withTag("Description", "Create Order")
				.withTag("http_request_url", request.getRequestURI())
				.start();     
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		if (!StringUtils.isBlank(from)) {
			Order order;
			try {
				Prometheus.updateCounters.inc();
				Prometheus.inProgressRequests.inc();
				log.info("createOrder() method of order controller is called");
				io.opentracing.Span mongospan = tracer.buildSpan("MongoService")
						.asChildOf(span)
						.withTag("Description", "MongoDB Service Call")
						.start();
				brave.Span braveMongoSpan = ((BraveSpan) mongospan).unwrap();
				order = service.createOrder(from,mongospan);
				braveMongoSpan.finish();
				return new ResponseEntity<>(order, HttpStatus.CREATED);
			} catch (ConflictingRequestException cre) {
				Prometheus.requestFailures.inc();
				span.setTag("error",cre.getMessage());
				log.error("Error in createOrder ", cre);
				return new ResponseEntity<>(cre.getMessage(), HttpStatus.CONFLICT);
			} catch (Exception exc) {
				Prometheus.requestFailures.inc();
				log.error("Error in createOrder ", exc);
				span.setTag("error",exc.getMessage());
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
			finally {
				requestTimer.observeDuration();
				Prometheus.inProgressRequests.dec();
				span.finish();
			}

		} else {
			span.finish();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		/*
		 * try { Quote quote = getQuotes().getQuote(from);
		 * 
		 * if (quote != null) { Order order = service.createOrder(from);
		 * 
		 * return new ResponseEntity<>(order, HttpStatus.CREATED); } else {
		 * return new ResponseEntity<>("There is no such quote",
		 * HttpStatus.BAD_REQUEST); } } catch (ConflictingRequestException bre)
		 * { return new ResponseEntity<>(bre.getMessage(), HttpStatus.CONFLICT);
		 * } catch (BadRequestException bre) { return new
		 * ResponseEntity<>(bre.getMessage(), HttpStatus.BAD_REQUEST); } catch
		 * (Exception exc) {
		 * 
		 * return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR); }
		 */
	}

	/**
	 * Creates a new order.
	 *
	 * @return An HttpResponse containing the quote.
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/{orderId}/events")
	public ResponseEntity<?> addEvent(@PathVariable String orderId, @RequestBody OrderEventInfo info) {
		BraveSpanContext  openTracingContext = getTracingContext();		
		io.opentracing.Span span = tracer.buildSpan("AddorderEventService")				 
				.asChildOf(openTracingContext)
				.withTag("Description", "Add Event to Order")
				.withTag("http_request_url", request.getRequestURI())
				.start();     
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		try {
			Prometheus.updateCounters.inc();
			Prometheus.inProgressRequests.inc();
			log.info("addEvent() method of order controller is called");
			if (info == null ||StringUtils.isBlank(orderId)) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			io.opentracing.Span mongospan = tracer.buildSpan("MongoService")
					.asChildOf(span)
					.withTag("Description", "MongoDB Service Call")
					.start();
			brave.Span braveMongoSpan = ((BraveSpan) mongospan).unwrap();		
			Order order = service.getOrder(orderId,mongospan);
			braveMongoSpan.finish();
			if (order != null) {
				DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
				info.setDate(df.format(new Date()));
				order.addEvent(info);
				io.opentracing.Span updatemongospan = tracer.buildSpan("UpdateOrderMongoService")
						.asChildOf(span)
						.withTag("Description", "MongoDB Service Call")
						.start();
				brave.Span updatebraveMongoSpan = ((BraveSpan) updatemongospan).unwrap();	
				service.updateOrder(order, order.getOrderId(), updatemongospan);
				updatebraveMongoSpan.finish();
				return new ResponseEntity<>(HttpStatus.CREATED);
			} else {
				return new ResponseEntity<>("There is no such order", HttpStatus.BAD_REQUEST);
			}
		} catch (Exception exc) {
			Prometheus.requestFailures.inc();
			log.error("Error in addEvent ", exc);
			span.setTag("error",exc.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		finally {
			requestTimer.observeDuration();
			Prometheus.inProgressRequests.dec();
			span.finish();
		}
	}

	/**
	 * Update a new order.
	 *
	 * @return An HttpResponse containing the quote.
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/{orderId}/status")
	public ResponseEntity<?> updateStatus(@PathVariable String orderId, @RequestBody OrderUpdateInfo info) {
		BraveSpanContext  openTracingContext = getTracingContext();		
		io.opentracing.Span span = tracer.buildSpan("UpdateorderStatusService")				 
				.asChildOf(openTracingContext)
				.withTag("Description", "Update Order")
				.withTag("http_request_url", request.getRequestURI())
				.start();     
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		try {
			Prometheus.updateCounters.inc();
			Prometheus.inProgressRequests.inc();
			log.info("updateStatus() method of order controller is called");
			if (info == null || StringUtils.isBlank(orderId)) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			if (service.hasOrder(orderId)) {
				DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
				info.getEventInfo().setDate(df.format(new Date()));
				io.opentracing.Span mongospan = tracer.buildSpan("MongoService")
						.asChildOf(span)
						.withTag("Description", "MongoDB Service Call")
						.start();
				brave.Span braveMongoSpan = ((BraveSpan) mongospan).unwrap();
				service.updateOrder(orderId, info,mongospan);
				braveMongoSpan.finish();
				return new ResponseEntity<>(HttpStatus.OK);
			} else {
				return new ResponseEntity<>("There is no such order", HttpStatus.BAD_REQUEST);
			}
		} catch (BadRequestException bre) {
			Prometheus.requestFailures.inc();
			log.error("Error in updateStatus ", bre);
			span.setTag("error",bre.getMessage());
			return new ResponseEntity<>(bre.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (Exception exc) {
			Prometheus.requestFailures.inc();
			span.setTag("error",exc.getMessage());
			log.error("Error in updateStatus ", exc);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		finally {
			requestTimer.observeDuration();
			Prometheus.inProgressRequests.dec();
			span.finish();
		}
	}

	/**
	 * Removes an existing order from the system.
	 *
	 * @param orderId
	 *            The order id.
	 * @return An HTTP status code
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/{orderId}")
	public ResponseEntity<?> deleteOrder(@PathVariable String orderId) {
		BraveSpanContext  openTracingContext = getTracingContext();		
		io.opentracing.Span span = tracer.buildSpan("deleteorderService")				 
				.asChildOf(openTracingContext)
				.withTag("Description", "Delete Order")
				.withTag("http_request_url", request.getRequestURI())
				.start();     
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		try {
			Prometheus.updateCounters.inc();
			Prometheus.inProgressRequests.inc();
			log.info("deleteOrder() method of order controller is called");
			boolean ok = service.removeOrder(orderId,span);
			return new ResponseEntity<>(ok ? HttpStatus.NO_CONTENT : HttpStatus.NOT_FOUND);
		} catch (Exception exc) {
			Prometheus.requestFailures.inc();
			log.error("Error in deleteOrder ", exc);
			span.setTag("error",exc.getMessage());
			return new ResponseEntity<>(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		finally {
			requestTimer.observeDuration();
			Prometheus.inProgressRequests.dec();
			span.finish();
		}
	}

	/**
	 * Updates an order.
	 *
	 * @return An HttpResponse containing the quote.
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/{orderId}")
	public ResponseEntity<?> updateOrder(@PathVariable String orderId, @RequestBody @Valid Order order) {
		BraveSpanContext  openTracingContext = getTracingContext();		
		io.opentracing.Span span = tracer.buildSpan("UpdateeorderService")				 
				.asChildOf(openTracingContext)
				.withTag("Description", "Update Order")
				.withTag("http_request_url", request.getRequestURI())
				.start();     
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		try {
			Prometheus.updateCounters.inc();
			Prometheus.inProgressRequests.inc();
			log.info("updateOrder() method of order controller is called");
			if (order == null || StringUtils.isBlank(orderId)) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			// Input data validation
			String errorMsg = order.validate();
			if (errorMsg != null) {
				return new ResponseEntity<>(errorMsg, HttpStatus.BAD_REQUEST);
			}
			io.opentracing.Span mongospan = tracer.buildSpan("MongoService")
					.asChildOf(span)
					.withTag("Description", "MongoDB Service Call")
					.start();
			brave.Span braveMongoSpan = ((BraveSpan) mongospan).unwrap();		
			boolean ok = service.updateOrder(order, orderId,mongospan);
			braveMongoSpan.finish();
			return new ResponseEntity<>(ok ? HttpStatus.OK : HttpStatus.NOT_FOUND);
		} catch (Exception exc) {
			Prometheus.requestFailures.inc();
			log.error("Error in updateOrder ", exc);
			return new ResponseEntity<>(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		finally {
			requestTimer.observeDuration();
			Prometheus.inProgressRequests.dec();
			span.finish();
		}
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
