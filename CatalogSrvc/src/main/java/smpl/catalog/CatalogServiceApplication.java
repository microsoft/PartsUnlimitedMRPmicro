package smpl.catalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector;

@SpringBootApplication()
@EnableCircuitBreaker
@EnableHystrixDashboard
@EnablePrometheusEndpoint // Exposes Application to Prometheus
@EnableSpringBootMetricsCollector

public class CatalogServiceApplication {
	private static final Logger log = LoggerFactory.getLogger(CatalogServiceApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(CatalogServiceApplication.class, args);
		log.info("Catalog Service Ready for use");
	}
}
