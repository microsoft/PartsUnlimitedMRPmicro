package smpl.shipment;

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

public class ShipmentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShipmentServiceApplication.class, args);
		System.out.printf("Shipment Service Ready for use %n");
	}
}

