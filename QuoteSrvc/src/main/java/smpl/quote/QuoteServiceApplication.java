package smpl.quote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector;
import smpl.quote.controller.QuoteController;

@SpringBootApplication()
@EnableCircuitBreaker
@EnableHystrixDashboard
@EnablePrometheusEndpoint // Exposes Application to Prometheus
@EnableSpringBootMetricsCollector


public class QuoteServiceApplication {
	private static final Logger log = LoggerFactory.getLogger(QuoteServiceApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(QuoteServiceApplication.class, args);
		log.info("Quote Service Ready for use");
		
	}
}

