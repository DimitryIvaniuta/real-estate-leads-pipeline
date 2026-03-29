package com.github.dimitryivaniuta.gateway.realestate.service;

import com.github.dimitryivaniuta.gateway.realestate.domain.Lead;
import com.github.dimitryivaniuta.gateway.realestate.domain.LeadPhase;
import com.github.dimitryivaniuta.gateway.realestate.dto.CreateLeadRequest;
import com.github.dimitryivaniuta.gateway.realestate.exception.InvalidLeadPhaseTransitionException;
import com.github.dimitryivaniuta.gateway.realestate.exception.LeadNotFoundException;
import com.github.dimitryivaniuta.gateway.realestate.repository.LeadRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Main application service for lead persistence and state changes.
 */
@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository leadRepository;
    private final LeadPhasePolicy leadPhasePolicy;
    private final LeadEventsPublisher leadEventsPublisher;
    private final LeadActivityService leadActivityService;

    /**
     * Creates a new lead in DISCOVERY phase.
     *
     * @param request create payload
     * @param actor user that creates the lead
     * @return persisted lead
     */
    public Mono<Lead> createLead(final CreateLeadRequest request, final String actor) {
        final Instant now = Instant.now();
        final Lead lead = Lead.builder()
                .id(UUID.randomUUID())
                .tenantId(request.tenantId())
                .agentId(request.agentId())
                .clientName(request.clientName())
                .clientEmail(request.clientEmail())
                .clientPhone(request.clientPhone())
                .propertyArea(request.propertyArea())
                .budgetAmount(request.budgetAmount())
                .phase(LeadPhase.DISCOVERY)
                .sourceChannel(request.sourceChannel())
                .createdAt(now)
                .updatedAt(now)
                .lastModifiedBy(actor)
                .build();

        return leadRepository.save(lead)
                .flatMap(saved -> leadActivityService.recordCreated(saved, actor)
                        .then(leadEventsPublisher.publishLeadPhaseChanged(saved))
                        .thenReturn(saved));
    }

    /**
     * Fetches a lead by id and tenant.
     *
     * @param id lead identifier
     * @param tenantId tenant identifier
     * @return found lead or error
     */
    public Mono<Lead> getLead(final UUID id, final UUID tenantId) {
        return leadRepository.findByIdAndTenantId(id, tenantId)
                .switchIfEmpty(Mono.error(new LeadNotFoundException(id)));
    }

    /**
     * Lists leads for a tenant.
     *
     * @param tenantId tenant identifier
     * @return matching leads
     */
    public Flux<Lead> listByTenant(final UUID tenantId) {
        return leadRepository.findAllByTenantId(tenantId);
    }

    /**
     * Moves a lead to the next allowed phase.
     *
     * @param id lead id
     * @param tenantId tenant identifier
     * @param targetPhase target phase
     * @param actor user performing the action
     * @return updated lead
     */
    public Mono<Lead> advanceLeadPhase(final UUID id,
                                       final UUID tenantId,
                                       final LeadPhase targetPhase,
                                       final String actor) {
        return getLead(id, tenantId)
                .flatMap(existing -> {
                    if (!leadPhasePolicy.isAllowed(existing.getPhase(), targetPhase)) {
                        return Mono.error(new InvalidLeadPhaseTransitionException(existing.getPhase(), targetPhase));
                    }
                    final LeadPhase previousPhase = existing.getPhase();
                    existing.setPhase(targetPhase);
                    existing.setUpdatedAt(Instant.now());
                    existing.setLastModifiedBy(actor);
                    return leadRepository.save(existing)
                            .flatMap(saved -> leadActivityService.recordPhaseChanged(saved, previousPhase, targetPhase, actor)
                                    .then(leadEventsPublisher.publishLeadPhaseChanged(saved))
                                    .thenReturn(saved));
                });
    }
}
