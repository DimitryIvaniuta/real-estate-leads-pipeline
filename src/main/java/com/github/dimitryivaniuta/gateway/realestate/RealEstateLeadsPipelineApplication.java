package com.github.dimitryivaniuta.gateway.realestate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point for the real estate leads pipeline service.
 */
@SpringBootApplication
public class RealEstateLeadsPipelineApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args raw command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(RealEstateLeadsPipelineApplication.class, args);
    }
}
