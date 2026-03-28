package com.github.dimitryivaniuta.gateway.realestate.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Application-specific configuration for lead security, topics, portal behavior, and request protection.
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(Security security, RateLimit rateLimit, Portal portal, Topics topics) {

    /**
     * Security and bot-protection configuration.
     */
    public record Security(List<String> blockedPathPrefixes, Duration denylistTtl, int blockThreshold) {
    }

    /**
     * Reactive IP rate limiting configuration.
     */
    public record RateLimit(int replenishPerMinute, int burstCapacity) {
    }

    /**
     * Browser portal origin validation configuration.
     */
    public record Portal(List<String> allowedOrigins) {
    }

    /**
     * Kafka topic names.
     */
    public record Topics(String leadPhaseChanged, String securityThreatDetected) {
    }
}
