package smpl.order.controller;

import org.springframework.stereotype.Controller;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;

@Controller
class Prometheus {

	static final String srvc = "Order";
	/* introducing prometheus metrics */
	static final Summary requestLatency = Summary.build()
			.name(srvc + "ServiceApplication_requests_latency_seconds")
			.help("Request latency in seconds").register();

	// Number of time request failed in general
	static final Counter requestFailures = Counter.build()
			.name(srvc + "ServiceApplication_requests_failures_total")
			.help("Request failures").register();

	// Number of time get Catalog is called
	static final Counter getCounters = Counter.build()
			.name(srvc + "ServiceApplication_requests_get_" + srvc + "_total")
			.help("Request get calls for " + srvc).register();

	// Number of time create Catalog is called
	static final Counter createCounters = Counter.build()
			.name(srvc + "ServiceApplication_requests_create_" + srvc + "_total")
			.help("Request create calls for " + srvc).register();

	// Number of time update Catalog is called
	static final Counter updateCounters = Counter.build()
			.name(srvc + "ServiceApplication_requests_update_" + srvc + "_total")
			.help("Request update calls for " + srvc).register();

	// Number of time remove order is called
	static final Counter removeCounters = Counter.build()
			.name(srvc + "ServiceApplication_requests_remove_" + srvc + "_total")
			.help("Request remove calls for " + srvc).register();

	// Number of active request
	static final Gauge inProgressRequests = Gauge.build()
			.name(srvc + "ServiceApplication_inprogress_requests")
			.help("Inprogress requests").register();

}
