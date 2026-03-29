package com.github.dimitryivaniuta.gateway.realestate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.dimitryivaniuta.gateway.realestate.domain.Lead;
import com.github.dimitryivaniuta.gateway.realestate.domain.LeadPhase;
import com.github.dimitryivaniuta.gateway.realestate.dto.CreateLeadRequest;
import com.github.dimitryivaniuta.gateway.realestate.exception.InvalidLeadPhaseTransitionException;
import com.github.dimitryivaniuta.gateway.realestate.repository.LeadRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for lead service.
 */
@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private LeadEventsPublisher leadEventsPublisher;

    @Mock
    private LeadActivityService leadActivityService;

    private LeadService leadService;

    @BeforeEach
    void setUp() {
        leadService = new LeadService(leadRepository, new LeadPhasePolicy(), leadEventsPublisher, leadActivityService);
    }

    @Test
    void shouldCreateLeadInDiscoveryPhase() {
        final CreateLeadRequest request = new CreateLeadRequest(
                UUID.randomUUID(),
                "agent-1",
                "John Client",
                "john@example.com",
                "+48123123123",
                "Gdansk",
                BigDecimal.valueOf(900000),
                "website");

        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(leadActivityService.recordCreated(any(Lead.class), any())).thenReturn(Mono.empty());
        when(leadEventsPublisher.publishLeadPhaseChanged(any(Lead.class))).thenReturn(Mono.empty());

        StepVerifier.create(leadService.createLead(request, "agent-1"))
                .assertNext(lead -> {
                    assertThat(lead.getPhase()).isEqualTo(LeadPhase.DISCOVERY);
                    assertThat(lead.getClientName()).isEqualTo("John Client");
                })
                .verifyComplete();
    }

    @Test
    void shouldRejectInvalidTransition() {
        final UUID leadId = UUID.randomUUID();
        final UUID tenantId = UUID.randomUUID();
        final Lead lead = Lead.builder()
                .id(leadId)
                .tenantId(tenantId)
                .agentId("agent")
                .clientName("Client")
                .clientPhone("123")
                .propertyArea("Krakow")
                .budgetAmount(BigDecimal.TEN)
                .phase(LeadPhase.DISCOVERY)
                .sourceChannel("site")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .lastModifiedBy("agent")
                .build();

        when(leadRepository.findByIdAndTenantId(leadId, tenantId)).thenReturn(Mono.just(lead));

        StepVerifier.create(leadService.advanceLeadPhase(leadId, tenantId, LeadPhase.DEAL, "agent"))
                .expectError(InvalidLeadPhaseTransitionException.class)
                .verify();
    }
}
