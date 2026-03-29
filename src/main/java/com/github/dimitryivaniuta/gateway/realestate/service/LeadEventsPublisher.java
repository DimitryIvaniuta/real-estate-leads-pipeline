package com.github.dimitryivaniuta.gateway.realestate.service;

import com.github.dimitryivaniuta.gateway.realestate.config.AppProperties;
import com.github.dimitryivaniuta.gateway.realestate.domain.Lead;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Publishes lead lifecycle events to Kafka for downstream analytics and automation.
 */
@Component
@RequiredArgsConstructor
public class LeadEventsPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AppProperties appProperties;

    /**
     * Publishes a phase transition event.
     *
     * @param lead changed lead
     * @return completion signal
     */
    public Mono<Void> publishLeadPhaseChanged(final Lead lead) {
        return Mono.fromRunnable(() -> kafkaTemplate.send(
                appProperties.topics().leadPhaseChanged(),
                lead.getId().toString(),
                Map.of(
                        "leadId", lead.getId(),
                        "tenantId", lead.getTenantId(),
                        "phase", lead.getPhase().name(),
                        "agentId", lead.getAgentId(),
                        "occurredAt", Instant.now().toString())));
    }

    /**
     * Publishes a suspicious request event.
     *
     * @param path request path
     * @param ip source ip
     * @param reason classification reason
     * @return completion signal
     */
    public Mono<Void> publishSecurityThreat(final String path, final String ip, final String reason) {
        return Mono.fromRunnable(() -> kafkaTemplate.send(
                appProperties.topics().securityThreatDetected(),
                ip,
                Map.of(
                        "ip", ip,
                        "path", path,
                        "reason", reason,
                        "occurredAt", Instant.now().toString())));
    }
}
