package com.ms.mrpclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

@SpringBootApplication
@EnableCircuitBreaker
@EnableHystrixDashboard
public class AppLauncher extends SpringBootServletInitializer {
	private static final Logger log = LoggerFactory.getLogger(AppLauncher.class);
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(AppLauncher.class);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(AppLauncher.class, args);
        log.info("PartsunlimitedMRP Client application has been Launched");
    }

}