package smpl.shipment.controller;

import java.text.DateFormat;
import java.util.Date;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import brave.Tracer;
import brave.Span.Kind;
import brave.opentracing.BraveSpan;
import brave.opentracing.BraveSpanContext;
import brave.opentracing.BraveTracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.prometheus.client.Summary;
import smpl.shipment.BadRequestException;
import smpl.shipment.model.OrderStatus;
import smpl.shipment.model.ShipmentEventInfo;
import smpl.shipment.model.ShipmentRecord;
import smpl.shipment.repository.service.ShipmentService;												  
import smpl.shipment.repository.RepositoryFactory;
import smpl.shipment.repository.ShipmentRepository;
import smpl.shipment.repository.TestPath;									   
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.okhttp3.OkHttpSender;

@RestController

@RequestMapping("/api/shipments")
public class ShipmentController {
	private static final Logger log = LoggerFactory.getLogger(ShipmentController.class);
	private final ShipmentRepository service;

	private String  zipkinUrl;

	@Autowired
	private HttpServletRequest request ; 

	OkHttpSender sender;
	AsyncReporter<Span> reporter;
	io.opentracing.Tracer tracer;
	Tracer braveTracer;

	@Autowired
	public ShipmentController(ShipmentService service, HttpServletRequest httpRequest,@Value("${zipkin.mrpservice.uri}") String url) {
			if (service != null) {
			this.service = service;
		} else {
			// call from test so reset repository also
			RepositoryFactory.reset("memory");
			this.service = RepositoryFactory.getShipmentRepository();
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
				.localServiceName("shipment-svc")
				.reporter(reporter)
				.build();
		tracer = BraveTracer.wrap(braveTracer);
	}

	/**
	 * Gets a list of existing shipments, regardless of status
	 *
	 * @return An HttpResponse containing a list of shipments.
	 */
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getShipments(
			@RequestParam(value = "status", required = false, defaultValue = "None") OrderStatus status) {
		BraveSpanContext  openTracingContext = getTracingContext();		
		io.opentracing.Span span = tracer.buildSpan("GetShipmentsFromService")				 
				.asChildOf(openTracingContext)
				.withTag("Description", "Get Shipments")
				.withTag("http_request_url", request.getRequestURI())
				.start();     
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		try {
			Prometheus.createCounters.inc();
			Prometheus.inProgressRequests.inc();
			log.info("getShipments() method of shipment controller is called");
			io.opentracing.Span mongospan = tracer.buildSpan("MongoService")
					.asChildOf(span)
					.withTag("Description", "MongoDB Service Call")
					.start();
			brave.Span braveMongoSpan = ((BraveSpan) mongospan).unwrap();
			List<ShipmentRecord> shipments = service.getShipments(status,mongospan);
			braveMongoSpan.finish();
			if (shipments == null || shipments.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			} else {
				return new ResponseEntity<List<ShipmentRecord>>(shipments, HttpStatus.OK);
			}
		} catch (Exception exc) {
			Prometheus.requestFailures.inc();
			span.setTag("error", exc.getMessage());
			log.error("Error in getShipments ", exc);
			return new ResponseEntity<String>(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		finally{
			requestTimer.observeDuration();
			Prometheus.inProgressRequests.dec();
			span.finish();
		}
	}




	/*@RequestMapping(value = "/deliveries", method = RequestMethod.GET)
    public ResponseEntity getDeliveries()
    {
        try
        {
            List<ShipmentRecord> shipments = service.getShipments(OrderStatus.DeliveryConfirmed);

            if (shipments == null || shipments.size() == 0)
            {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
            else
            {
                List<Delivery> deliveries = new ArrayList<Delivery>();
                OrderRepository orderRepository = getOrderRepository();
                QuoteRepository quoteRepository = getQuoteRepository();

                for (int n = 0; n < shipments.size(); n++) {
                    Delivery delivery = new Delivery();

                    ShipmentRecord shipment = shipments.get(n);
                    delivery.setShipmentRecord(shipment);

                    Order order = orderRepository.getOrder(shipment.getOrderId());
                    delivery.setOrder(order);

                    Quote quote = quoteRepository.getQuote(order.getQuoteId());
                    delivery.setQuote(quote);

                    deliveries.add(delivery);
                }

                return new ResponseEntity<List<Delivery>>(deliveries, HttpStatus.OK);
            }
        }
        catch (Exception exc)
        {
            return new ResponseEntity<String>(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
	 */
	/**
	 * Gets a specific shipment by its corresponding order id.
	 *
	 * @param id The order id
	 * @return An HttpResponse containing a shipment record.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{id}")
	public ResponseEntity<?> getShipmentById(@PathVariable String id)
	{
		BraveSpanContext  openTracingContext = getTracingContext();		
		io.opentracing.Span span = tracer.buildSpan("GetShipmentById")				 
				.asChildOf(openTracingContext)
				.withTag("Description", "Get Shipment by Id")
				.withTag("http_request_url", request.getRequestURI())
				.start();     
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		try
		{
			Prometheus.createCounters.inc();
			Prometheus.inProgressRequests.inc();
			log.info("getShipmentById() method of shipment controller is called");
			io.opentracing.Span mongospan = tracer.buildSpan("MongoService")
					.asChildOf(span)
					.withTag("Description", "MongoDB Service Call")
					.start();
			brave.Span braveMongoSpan = ((BraveSpan) mongospan).unwrap();
			ShipmentRecord sr = service.getShipmentById(id,mongospan);
			braveMongoSpan.finish();
			if (sr == null)
			{
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			else
			{
				return new ResponseEntity<ShipmentRecord>(sr, HttpStatus.OK);
			}
		}
		catch (Exception exc)
		{
			Prometheus.requestFailures.inc();
			span.setTag("error", exc.getMessage());
			log.error("Error in getShipmentById ", exc);
			return new ResponseEntity<String>(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		finally {
			requestTimer.observeDuration();
			Prometheus.inProgressRequests.dec();
			span.finish();
		}
	}

	/**
	 * Creates a shipment record
	 *
	 * @param info Information about the SKU
	 * @return An HTTP status code.
	 */
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> createShipmentRecord(@RequestBody ShipmentRecord info)
	{
		BraveSpanContext  openTracingContext = getTracingContext();		
		io.opentracing.Span span = tracer.buildSpan("CreateShipmentService")				 
				.asChildOf(openTracingContext)
				.withTag("Description", "Create Shipment")
				.withTag("http_request_url", request.getRequestURI())
				.start();     
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		try
		{
			Prometheus.createCounters.inc();
			Prometheus.inProgressRequests.inc();
			log.info("createShipmentRecord() method of shipment controller is called");
			if(info == null)
			{
				return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
			}
			String errorMsg = info.validate();
			if (errorMsg != null)
			{
				return new ResponseEntity<String>(errorMsg, HttpStatus.BAD_REQUEST);
			}

			io.opentracing.Span mongospan = tracer.buildSpan("MongoService")
					.asChildOf(span)
					.withTag("Description", "MongoDB Service Call")
					.start();
			brave.Span braveMongoSpan = ((BraveSpan) mongospan).unwrap();
			ShipmentRecord sr = service.getShipmentById(info.getOrderId(),mongospan);
			braveMongoSpan.finish();
			if (sr != null)
			{
				return new ResponseEntity<String>("A shipment record already exists", HttpStatus.CONFLICT);
			}
			io.opentracing.Span createshipmentspan = tracer.buildSpan("CreateShipmentMongoService")
					.asChildOf(span)
					.withTag("Description", "MongoDB Service Call")
					.start();
			brave.Span bravecreateshipmentspan= ((BraveSpan) createshipmentspan).unwrap();
			boolean result = service.createShipment(info,createshipmentspan) != null;
			bravecreateshipmentspan.finish();
			return new ResponseEntity<>(result ? HttpStatus.CREATED : HttpStatus.NOT_FOUND);
		}
		catch (BadRequestException bre)
		{
			Prometheus.requestFailures.inc();
			span.setTag("error", bre.getMessage());
			log.error("BadRequestException in Create Shipment  ", bre);
			return new ResponseEntity<String>(bre.getMessage(), HttpStatus.BAD_REQUEST);
		}
		catch (Exception exc)
		{
			Prometheus.requestFailures.inc();
			span.setTag("error", exc.getMessage());
			log.error("error in create shipment ", exc);
			return new ResponseEntity<String>(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		finally {
			requestTimer.observeDuration();
			Prometheus.inProgressRequests.dec();
			span.finish();
		}
	}
	/**
	 * Updates a shipment record
	 *
	 * @param id  The order id
	 * @param record A shipment record
	 * @return An HTTP status code.
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/{id}")
	public ResponseEntity<?> updateShipment(@PathVariable String id, @RequestBody ShipmentRecord record)
	{ 		
		BraveSpanContext  openTracingContext = getTracingContext();		
		io.opentracing.Span span = tracer.buildSpan("UpdateShipmentService")				 
				.asChildOf(openTracingContext)
				.withTag("Description", "Update Shipment")
				.withTag("http_request_url", request.getRequestURI())
				.start();     
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		try
		{
			Prometheus.createCounters.inc();
			Prometheus.inProgressRequests.inc();
			log.info("updateShipment() method of shipment controller is called");
			if(record == null || id == null)
			{
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			String errorMsg = record.validate();
			if (errorMsg != null)
			{
				return new ResponseEntity<>(errorMsg, HttpStatus.BAD_REQUEST);
			}

			if (!id.equals(record.getOrderId()))
			{
				return new ResponseEntity<>("mismatched ids", HttpStatus.BAD_REQUEST);
			}
			io.opentracing.Span mongospan = tracer.buildSpan("MongoService")
					.asChildOf(span)
					.withTag("Description", "MongoDB Service Call")
					.start();
			brave.Span braveMongoSpan = ((BraveSpan) mongospan).unwrap();
			ShipmentRecord sr = service.getShipmentById(id,mongospan);
			braveMongoSpan.finish();

			if (sr == null)
			{
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			io.opentracing.Span updateshipmentspan = tracer.buildSpan("UpdateShipmentMongoService")
					.asChildOf(span)
					.withTag("Description", "MongoDB Service Call")
					.start();
			brave.Span braveupdateshipmentspan= ((BraveSpan) updateshipmentspan).unwrap();
			service.updateShipment(record,updateshipmentspan);
			braveupdateshipmentspan.finish();
			return new ResponseEntity<>(HttpStatus.OK);
		}
		catch (Exception exc)
		{
			Prometheus.requestFailures.inc();
			span.setTag("error", exc.getMessage());
			log.error("updateShipment ", exc);
			return new ResponseEntity<>(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		finally {
			requestTimer.observeDuration();
			Prometheus.inProgressRequests.dec();
			span.finish();
		}
	}
	/**
	 * Updates a shipment record with a new event.
	 *
	 * @param id  The order id
	 * @param event A shipment event record
	 * @return An HTTP status code.
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/{id}/events")
	public ResponseEntity<?> addEvent(@PathVariable String id, @RequestBody ShipmentEventInfo event)
	{
		BraveSpanContext  openTracingContext = getTracingContext();		
		io.opentracing.Span span = tracer.buildSpan("AddEventtoShipmentService")				 
				.asChildOf(openTracingContext)
				.withTag("Description", "Add Event to Shipment")
				.withTag("http_request_url", request.getRequestURI())
				.start();     
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		try
		{
			Prometheus.createCounters.inc();
			Prometheus.inProgressRequests.inc();
			log.info("addEvent() method of shipment controller is called");
			if(id == null || event == null)
			{
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			String errorMsg = event.validate();
			if (errorMsg != null)
			{
				return new ResponseEntity<>(errorMsg, HttpStatus.BAD_REQUEST);
			}
			io.opentracing.Span mongospan = tracer.buildSpan("MongoService")
					.asChildOf(span)
					.withTag("Description", "MongoDB Service Call")
					.start();
			brave.Span braveMongoSpan = ((BraveSpan) mongospan).unwrap();		
			ShipmentRecord sr = service.getShipmentById(id,mongospan);
			braveMongoSpan.finish();
			if (sr == null)
			{
				return new ResponseEntity<ShipmentRecord>(HttpStatus.NOT_FOUND);
			}

			event.setDate(DateFormat.getDateInstance(DateFormat.SHORT).format(new Date()));
			io.opentracing.Span addEventspan = tracer.buildSpan("AddEventMongoService")
					.asChildOf(span)
					.withTag("Description", "MongoDB Service Call")
					.start();
			brave.Span braveaddEventspan= ((BraveSpan) addEventspan).unwrap();
			boolean result = service.addEvent(id, event,addEventspan);
			braveaddEventspan.finish();
			return new ResponseEntity<>(result ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
		}
		catch (Exception exc)
		{
			Prometheus.requestFailures.inc();
			span.setTag("error", exc.getMessage());
			log.error("Shipment add event ", exc);
			return new ResponseEntity<>(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		finally {
			requestTimer.observeDuration();
			Prometheus.inProgressRequests.dec();
			span.finish();
		}
	}

	/**
	 * Removes an existing shipment from the system.
	 *
	 * @param orderId The order id.
	 * @return An HTTP status code
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/{orderId}")
	public ResponseEntity<?> deleteShipment(@PathVariable String orderId)
	{
		BraveSpanContext  openTracingContext = getTracingContext();		
		io.opentracing.Span span = tracer.buildSpan("DeleteShipmentService")				 
				.asChildOf(openTracingContext)
				.withTag("Description", "Delete Shipment")
				.withTag("http_request_url", request.getRequestURI())
				.start();     
		brave.Span braveSpan = ((BraveSpan) span).unwrap();
		braveSpan.kind(Kind.SERVER);
		Summary.Timer requestTimer = Prometheus.requestLatency.startTimer();
		try
		{
			Prometheus.createCounters.inc();
			Prometheus.inProgressRequests.inc();
			log.info("deleteShipment() method of shipment controller is called");
			boolean ok = service.removeShipment(orderId, null,span);
			return new ResponseEntity<>(ok ? HttpStatus.NO_CONTENT : HttpStatus.NOT_FOUND);
		}
		catch (Exception exc)
		{
			Prometheus.requestFailures.inc();
			span.setTag("error", exc.getMessage());
			log.error("Delete Shipment ", exc);
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
