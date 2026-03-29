package com.github.dimitryivaniuta.gateway.realestate.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

import com.github.dimitryivaniuta.gateway.realestate.domain.Lead;
import com.github.dimitryivaniuta.gateway.realestate.domain.LeadPhase;
import com.github.dimitryivaniuta.gateway.realestate.dto.LeadPipelineSummaryResponse;
import com.github.dimitryivaniuta.gateway.realestate.exception.GlobalExceptionHandler;
import com.github.dimitryivaniuta.gateway.realestate.security.TenantAccessService;
import com.github.dimitryivaniuta.gateway.realestate.service.LeadActivityService;
import com.github.dimitryivaniuta.gateway.realestate.service.LeadService;
import com.github.dimitryivaniuta.gateway.realestate.service.LeadSummaryService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

/**
 * Web tests for lead endpoints.
 */
@WebFluxTest(controllers = LeadController.class)
@Import({GlobalExceptionHandler.class, TenantAccessService.class})
class LeadControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private LeadService leadService;

    @MockBean
    private LeadActivityService leadActivityService;

    @MockBean
    private LeadSummaryService leadSummaryService;

    @Test
    void shouldReturnLeadById() {
        final UUID leadId = UUID.randomUUID();
        final UUID tenantId = UUID.randomUUID();
        final Lead lead = Lead.builder()
                .id(leadId)
                .tenantId(tenantId)
                .agentId("agent-1")
                .clientName("Alice Client")
                .clientEmail("alice@example.com")
                .clientPhone("+48111111111")
                .propertyArea("Warsaw")
                .budgetAmount(BigDecimal.valueOf(1000000))
                .phase(LeadPhase.QUALIFIED)
                .sourceChannel("website")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .lastModifiedBy("agent-1")
                .build();

        when(leadService.getLead(leadId, tenantId)).thenReturn(Mono.just(lead));

        webTestClient.mutateWith(mockJwt().jwt(jwt -> jwt.subject("agent-1")
                        .claim("roles", List.of("AGENT"))
                        .claim("tenant_id", tenantId.toString())))
                .get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/leads/{id}")
                        .queryParam("tenantId", tenantId)
                        .build(leadId))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.clientName").isEqualTo("Alice Client")
                .jsonPath("$.phase").isEqualTo("QUALIFIED");
    }

    @Test
    void shouldReturnSummary() {
        final UUID tenantId = UUID.randomUUID();
        when(leadSummaryService.summarize(tenantId)).thenReturn(Mono.just(
                new LeadPipelineSummaryResponse(Map.of("DISCOVERY", 1L, "QUALIFIED", 2L))));

        webTestClient.mutateWith(mockJwt().jwt(jwt -> jwt.subject("agent-1")
                        .claim("roles", List.of("AGENT"))
                        .claim("tenant_id", tenantId.toString())))
                .get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/leads/summary")
                        .queryParam("tenantId", tenantId)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalsByPhase.DISCOVERY").isEqualTo(1)
                .jsonPath("$.totalsByPhase.QUALIFIED").isEqualTo(2);
    }
}
